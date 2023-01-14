package it.uniba.dib.sms222332;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class InsertLaureaFragment extends Fragment {

    EditText edtThesisName, edtMainSubject, edtEstimatedTime, edtCorrelator,edtDescription, edtThesisConstraints,edtRelatedProjects;
    RadioButton radioButtonSperimentale,radioButtonCompilativa;
    Button buttonUploadFile,buttonCreateThesis;
    TextView txtAddMaterial;

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Uri pdfUri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_insert_laurea, container, false);

        View primoLayout = view.findViewById(R.id.first_layout);
        View innerLayout = primoLayout.findViewById(R.id.my_layout3);
        View secondoLayout = primoLayout.findViewById(R.id.second_layout);


        edtThesisName = primoLayout.findViewById(R.id.edtThesisName);
        edtMainSubject = primoLayout.findViewById(R.id.edtMainSubject);
        edtEstimatedTime = secondoLayout.findViewById(R.id.edtEstimatedTime);
        edtCorrelator = primoLayout.findViewById(R.id.edtCorrelator);
        edtDescription = primoLayout.findViewById(R.id.edtDescription);
        edtThesisConstraints = primoLayout.findViewById(R.id.edtThesisConstraints);
        edtRelatedProjects = primoLayout.findViewById(R.id.edtRelatedProjects);
        radioButtonSperimentale = primoLayout.findViewById(R.id.radioButtonSperimentale);
        radioButtonCompilativa = primoLayout.findViewById(R.id.radioButtonCompilativa);
        buttonUploadFile = innerLayout.findViewById(R.id.buttonUploadFile);
        buttonCreateThesis = primoLayout.findViewById(R.id.buttonCreateThesis);
        txtAddMaterial = innerLayout.findViewById(R.id.txtAddMaterial);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        buttonCreateThesis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inserisciTesi();
            }
        });

        buttonUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caricaPdf();

            }
        });


        return view;
    }

    private void caricaPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }


    private void inserisciTesi() {

        String thesisName = edtThesisName.getText().toString();
        String mainSubject = edtMainSubject.getText().toString();
        String estimatedTime = edtEstimatedTime.getText().toString();
        String correlator = edtCorrelator.getText().toString();
        String description = edtDescription.getText().toString();
        String thesisConstraints = edtThesisConstraints.getText().toString();
        String relatedProjects = edtRelatedProjects.getText().toString();
        String tipoTesi = "";
        String professore = mUser.getEmail();


        if(radioButtonSperimentale.isChecked()){
            tipoTesi = radioButtonSperimentale.getText().toString();
        }else if(radioButtonCompilativa.isChecked()){
            tipoTesi = radioButtonCompilativa.getText().toString();
        }

        Map<String,String> infoTesi = new HashMap<>();
        infoTesi.put("Professore",professore);
        infoTesi.put("Nome",thesisName);
        infoTesi.put("Materia",mainSubject);
        infoTesi.put("Tempo Stimato",estimatedTime);
        infoTesi.put("Correlatore",correlator);
        infoTesi.put("Descrizione",description);
        infoTesi.put("Vincoli",thesisConstraints);
        infoTesi.put("ProgettiCorrelati",relatedProjects);
        infoTesi.put("Tipo",tipoTesi);

        String idTesi = professore + "_" + thesisName;

        if(thesisName.isEmpty()){
            edtThesisName.setError("Inserisci il nome della tesi!");
        }else if(mainSubject.isEmpty()){
            edtMainSubject.setError("Inserisci la materia per questa tesi!");
        }else if(estimatedTime.isEmpty()){
            edtEstimatedTime.setError("Inserisci il tempo stimato per eseguire la tesi!");
        }else if(description.isEmpty()){
            edtDescription.setError("Inserisci una descrizione per la tua tesi!");
        }else{
            db.collection("Tesi").document(idTesi).set(infoTesi);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
             pdfUri = data.getData();
             File file = new File(pdfUri.getPath());
             String pdfName = file.getName();
             uploadPdf(pdfName);
            txtAddMaterial.setText(pdfName);
        }
    }

    public void uploadPdf(String pdfName){
        // Creazione del riferimento al file sul server di Firebase
        //StorageReference ref = storageReference.child("pdfs/" + System.currentTimeMillis() + ".pdf");
        storageReference = FirebaseStorage.getInstance().getReference(edtThesisName.getText()+"/pdfs/"+pdfName);
        // Caricamento del file sul server
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //SUCCESSO
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //INSUCCESSO
                    }
                });
    }
}