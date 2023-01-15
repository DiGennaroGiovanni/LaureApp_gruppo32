package it.uniba.dib.sms222332;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class InsertLaureaFragment extends Fragment {

    EditText edtThesisName, edtEstimatedTime, edtCorrelator,edtDescription, edtThesisConstraints,edtRelatedProjects;
    RadioButton radioButtonSperimentale,radioButtonCompilativa;
    Spinner edtMainSubject;
    Button addFile,buttonCreateThesis;
    TextView textView6;

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Uri pdfUri;
    LinearLayout layout;

    ArrayList<Uri> filePdf ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_insert_laurea, container, false);

        /*View primoLayout = view.findViewById(R.id.first_layout);
        View secondoLayout = primoLayout.findViewById(R.id.second_layout);
        View terzoLayout = primoLayout.findViewById(R.id.third_layout);
       */
        layout = view.findViewById(R.id.layout_lista);

        textView6 = view.findViewById(R.id.textView6);
        edtThesisName = view.findViewById(R.id.edtThesisName);
        edtMainSubject = view.findViewById(R.id.edtMainSubject);
        edtEstimatedTime = view.findViewById(R.id.edtEstimatedTime);
        edtCorrelator = view.findViewById(R.id.edtCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtThesisConstraints = view.findViewById(R.id.edtThesisConstraints);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        radioButtonSperimentale = view.findViewById(R.id.radioButtonSperimentale);
        radioButtonCompilativa = view.findViewById(R.id.radioButtonCompilativa);
        addFile = view.findViewById(R.id.addFile);

        buttonCreateThesis = view.findViewById(R.id.buttonCreateThesis);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        filePdf = new ArrayList<>();
        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.faculty_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        edtMainSubject.setAdapter(adapter);


        buttonCreateThesis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inserisciTesi();


            }
        });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caricaPdf();

            }
        });

        return view;
    }

    private void caricaPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    private void inserisciTesi() {


        String thesisName = edtThesisName.getText().toString();
        String mainSubject = edtMainSubject.getSelectedItem().toString();
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

        if(thesisName.isEmpty()){
            edtThesisName.setError("Inserisci il nome della tesi!");
        }else if(mainSubject.isEmpty()){
            textView6.setError("Inserisci la materia per questa tesi!");
        }else if(estimatedTime.isEmpty()){
            edtEstimatedTime.setError("Inserisci il tempo stimato per eseguire la tesi!");
        }else if(description.isEmpty()){
            edtDescription.setError("Inserisci una descrizione per la tua tesi!");
        }else{
            db.collection("Tesi").document(thesisName).set(infoTesi);
            for(Uri pdf: filePdf){
                uploadPdf(pdf);
            }
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, new ProfessorHomeFragment());
            fragmentTransaction.commit();
            Toast.makeText(getActivity(),"Tesi inserita",Toast.LENGTH_LONG).show();
        }
    }

    private void addCard(String pdfName, Uri pdfUri) {
        View view = getLayoutInflater().inflate(R.layout.card, null);
        //storageReference = FirebaseStorage.getInstance().getReference(edtThesisName.getText().toString()+"/"+pdfName);
        TextView nameView = view.findViewById(R.id.name);
        Button delete = view.findViewById(R.id.delete);
        filePdf.add(pdfUri);
        nameView.setText(pdfName);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(view);
                filePdf.remove(pdfUri);
            }
        });
        layout.addView(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) { //CONDIZIONE PER IL CARICAMENTO DEL PDF
            pdfUri = data.getData();
            File file = new File(pdfUri.getPath());
            String pdfName = file.getName();
            addCard(pdfName,pdfUri);

        } else {
            //Toast.makeText(InsertLaureaFragment.this, "Seleziona un file", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPdf(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        File file = new File(uri.getPath());
        String pdfName = file.getName();
        //StorageReference ref = storageReference.child("pdfs/" + System.currentTimeMillis() + ".pdf");
        storageReference = FirebaseStorage.getInstance().getReference(edtThesisName.getText().toString()+"/"+pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Toast.makeText(Provalista.this, "Caricamento completato", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(Provalista.this, "Caricamento NON avvenuto", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}