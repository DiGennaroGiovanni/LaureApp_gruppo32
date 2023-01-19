package it.uniba.dib.sms222332;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewThesisFragment extends Fragment {

    EditText edtThesisName, edtEstimatedTime,edtDescription, edtMaterieRichieste,edtRelatedProjects;
    RadioButton radioButtonSperimentale,radioButtonCompilativa;
    Spinner edtMainSubject,edtCorrelator;
    Button addFile,buttonCreateThesis;
    TextView txtFaculty;

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference ref = db.collection("data");
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Uri pdfUri;
    LinearLayout layout;
    SeekBar seekBar;
    TextView progress_text;
    CheckBox avarageCheck,materieCheck;

    ArrayList<Uri> filePdf = new ArrayList<>() ;
    ArrayList<String> correlatori = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.createThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_insert_laurea, container, false);

        layout = view.findViewById(R.id.layout_lista);

        avarageCheck = view.findViewById(R.id.avarageCheck);
        materieCheck = view.findViewById(R.id.materieCheck);
        edtMaterieRichieste = view.findViewById(R.id.edtMaterieRichieste);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        edtThesisName = view.findViewById(R.id.edtThesisName);
        edtMainSubject = view.findViewById(R.id.edtMainSubject);
        edtEstimatedTime = view.findViewById(R.id.edtEstimatedTime);
        edtCorrelator = view.findViewById(R.id.edtCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        radioButtonSperimentale = view.findViewById(R.id.radioButtonSperimentale);
        radioButtonCompilativa = view.findViewById(R.id.radioButtonCompilativa);
        addFile = view.findViewById(R.id.addFile);
        seekBar = view.findViewById(R.id.seekbar);
        progress_text = view.findViewById(R.id.progress_text);
        //edtMaterieRichieste.setEnabled(false);

        buttonCreateThesis = view.findViewById(R.id.buttonCreateThesis);
        edtMaterieRichieste.setEnabled(false);
        progress_text = view.findViewById(R.id.progress_text);

        seekBar.setProgress(18);
        seekBar.setMax(30);
        seekBar.setMin(18);
        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String progressText = String.valueOf(progress);
                progress_text.setText(progressText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // code to execute when the user starts moving the seekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // code to execute when the user stops moving the seekBar
            }
        });


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.faculty_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        edtMainSubject.setAdapter(adapter);


        buttonCreateThesis.setOnClickListener(view1 -> {
            try {
                inserisciTesi();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        addFile.setOnClickListener(view12 -> caricaPdf());

        CollectionReference collectionRef = db.collection("professori");

        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals(mUser.getEmail()))
                    {
                        String nome = document.getString("Name")+" "+ document.getString("Surname");
                        correlatori.add(nome);
                    }
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

        correlatori.add("Nessuno");

        ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, correlatori );
        adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edtCorrelator.setAdapter(adapterProf);


        materieCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtMaterieRichieste.setEnabled(true);
                } else {
                    edtMaterieRichieste.setEnabled(false);

                }
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

    private void inserisciTesi() throws IOException {

        String thesisName = edtThesisName.getText().toString();
        String mainSubject = edtMainSubject.getSelectedItem().toString();
        String estimatedTime = edtEstimatedTime.getText().toString();
        String correlator = edtCorrelator.getSelectedItem().toString();
        String description = edtDescription.getText().toString();
        String materieRichieste = "";
        String relatedProjects = edtRelatedProjects.getText().toString();
        String tipoTesi = "";
        String professore = mUser.getEmail();

        if(radioButtonSperimentale.isChecked()){
            tipoTesi = radioButtonSperimentale.getText().toString();
        }else if(radioButtonCompilativa.isChecked()){
            tipoTesi = radioButtonCompilativa.getText().toString();
        }

        if(materieCheck.isChecked())
        {
            materieRichieste = edtMaterieRichieste.getText().toString();
            if(materieRichieste.isEmpty())
            {
                edtMaterieRichieste.setError("Inserisci le materie richieste  ");
            }else{
                materieRichieste =edtMaterieRichieste.getText().toString();
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
        infoTesi.put("Constraints",""); //TODO ELIMINARE -> elimnare prima la lettura nella descrizione della tesi
        infoTesi.put("Related Projects",relatedProjects);
        infoTesi.put("Required Subjects",materieRichieste);
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
        TextView nameView = view.findViewById(R.id.name);
        Button delete = view.findViewById(R.id.delete);
        filePdf.add(pdfUri);
        nameView.setText(pdfName);

        delete.setOnClickListener(v -> {
            layout.removeView(view);
            filePdf.remove(pdfUri);
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
                .addOnSuccessListener(taskSnapshot -> {
                    //Toast.makeText(Provalista.this, "Caricamento completato", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(Provalista.this, "Caricamento NON avvenuto", Toast.LENGTH_SHORT).show();
                });
    }



}