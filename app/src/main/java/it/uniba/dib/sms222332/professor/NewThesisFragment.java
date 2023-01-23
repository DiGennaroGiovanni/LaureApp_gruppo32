package it.uniba.dib.sms222332.professor;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;


public class NewThesisFragment extends Fragment {



    EditText edtThesisName, edtEstimatedTime,edtDescription, edtMaterieRichieste,edtRelatedProjects, edtAverage;
    RadioButton radioButtonSperimentale,radioButtonCompilativa;
    Spinner edtMainSubject,edtCorrelator;
    Button addFile,buttonCreateThesis;
    TextView txtFaculty;

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Uri pdfUri;
    ArrayList<Uri> files = new ArrayList<>();
    LinearLayout layout;

    CheckBox averageCheck, subjectCheck;


    ArrayList<String> correlatori = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.createThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_new_thesis, container, false);

        layout = view.findViewById(R.id.layout_lista);

        averageCheck = view.findViewById(R.id.averageCheck);
        subjectCheck = view.findViewById(R.id.requiredExamsCheck);
        edtMaterieRichieste = view.findViewById(R.id.edtRequiredExams);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        edtAverage = view.findViewById(R.id.edtAverage);
        edtThesisName = view.findViewById(R.id.edtThesisName);
        edtMainSubject = view.findViewById(R.id.edtMainSubject);
        edtEstimatedTime = view.findViewById(R.id.edtEstimatedTime);
        edtCorrelator = view.findViewById(R.id.edtCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        radioButtonSperimentale = view.findViewById(R.id.radioButtonSperimentale);
        radioButtonCompilativa = view.findViewById(R.id.radioButtonCompilativa);
        addFile = view.findViewById(R.id.addFile);
        buttonCreateThesis = view.findViewById(R.id.buttonCreateThesis);

        edtMaterieRichieste.setEnabled(false);
        edtAverage.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.faculty_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner

        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        edtMainSubject.setAdapter(adapter);
        buttonCreateThesis.setOnClickListener(view1 -> inserisciTesi());
        addFile.setOnClickListener(view12 -> caricaFile());

        CollectionReference collectionRef = db.collection("professori");

        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                correlatori.add("Nessuno");

                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals(mUser.getEmail()))
                    {
                        String nome = document.getString("Name")+" "+ document.getString("Surname");
                        correlatori.add(nome);
                    }
                }

                ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, correlatori );
                adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                edtCorrelator.setAdapter(adapterProf);

            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });



        averageCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edtAverage.setEnabled(averageCheck.isChecked());
            }
        });

      subjectCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edtMaterieRichieste.setEnabled(subjectCheck.isChecked());
            }
        });

        return view;
    }

    private void caricaFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    private void inserisciTesi() {

        String thesisName = edtThesisName.getText().toString();
        String mainSubject = edtMainSubject.getSelectedItem().toString();
        String estimatedTime = edtEstimatedTime.getText().toString();
        String correlator = edtCorrelator.getSelectedItem().toString();
        String description = edtDescription.getText().toString();
        String materieRichieste = "";
        String mediaVoti = "";
        String relatedProjects = edtRelatedProjects.getText().toString();
        String tipoTesi = "";
        String professore = mUser.getEmail();
        int  numeroIntero;


        if(radioButtonSperimentale.isChecked()){
            tipoTesi = radioButtonSperimentale.getText().toString();
        }else if(radioButtonCompilativa.isChecked()){
            tipoTesi = radioButtonCompilativa.getText().toString();
        }

        if(subjectCheck.isChecked())
        {
            materieRichieste = edtMaterieRichieste.getText().toString();

            if(materieRichieste.isEmpty())
                edtMaterieRichieste.setError("Inserisci le materie richieste");
        }

        if(averageCheck.isChecked()) {
            mediaVoti = edtAverage.getText().toString();

            if(mediaVoti.isEmpty())
                edtAverage.setError("Inserisci una vincolo valido");
            else{
                numeroIntero = Integer.parseInt(mediaVoti);
                if(numeroIntero > 30 || numeroIntero < 18)
                    edtAverage.setError("Inserisci una media tra il 18 ed il 30");
            }
        }

        if(correlator.equals("Nessuno")){
            correlator="";
        }

        Map<String,String> infoTesi = new HashMap<>();
        infoTesi.put("Professor",professore);
        infoTesi.put("Name",thesisName);
        infoTesi.put("Faculty",mainSubject);
        infoTesi.put("Estimated Time",estimatedTime);
        infoTesi.put("Correlator",correlator);
        infoTesi.put("Description",description);
        infoTesi.put("Related Projects",relatedProjects);
        infoTesi.put("Required Exam",materieRichieste);
        infoTesi.put("Average",mediaVoti);
        infoTesi.put("Type",tipoTesi);
        infoTesi.put("Student","");

        if(thesisName.isEmpty()){
            edtThesisName.setError("Inserisci il nome della tesi!");
        }else if(estimatedTime.isEmpty()){
            edtEstimatedTime.setError("Inserisci il tempo stimato per eseguire la tesi!");
        }else if(description.isEmpty()){
            edtDescription.setError("Inserisci una descrizione per la tua tesi!");
        }else{
            db.collection("Tesi").document(thesisName).set(infoTesi);
            for(Uri file: files){
                uploadFile(file);
            }
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, new ProfessorHomeFragment());
            fragmentTransaction.commit();
            View view = getLayoutInflater().inflate(R.layout.card_material, null);
            Snackbar.make(view, "Tesi inserita", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void addMaterialItem(String fileName, Uri fileUri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        files.add(fileUri);
        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layout.removeView(view);
            files.remove(fileUri);
        });
        layout.addView(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) { //CONDIZIONE PER IL CARICAMENTO DEL PDF
            pdfUri = data.getData();
            File file = new File(pdfUri.getPath());
            String fileName = file.getName();
            addMaterialItem(fileName,pdfUri);

        }
    }

    private void uploadFile(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        File file = new File(uri.getPath());
        String pdfName = file.getName();
        storageReference = FirebaseStorage.getInstance().getReference(edtThesisName.getText().toString()+"/"+pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                })
                .addOnFailureListener(e -> {
                });
    }
}


