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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;

public class EditThesisFragment extends Fragment {

    EditText edtTime,edtDescription,edtRelatedProjects,edtAverage,edtRequiredExams;
    TextView txtDepartment,txtNameTitle,txtType;
    Button btnSave,buttonAdd;
    Spinner spinnerCorrelator;
    CheckBox averageCheck,requiredExamsCheck;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    String correlator,nome,name,nomeFile;
    FirebaseUser mUser;
    ArrayList<String> correlatori = new ArrayList<>();
    int numeroIntero;
    String mediaVoti = "";
    String materieRichieste = "";
    LinearLayout layout_lista_file;
    Uri pdfUri;
    ArrayList<Uri> files = new ArrayList<>();

    StorageReference storageReference,folderRef;
    FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.editThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_edit_thesis, container, false);

        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        txtType = view.findViewById(R.id.txtTypology);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        edtTime = view.findViewById(R.id.edtTime);
        spinnerCorrelator = view.findViewById(R.id.spinnerCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        btnSave = view.findViewById(R.id.btnSave);
        averageCheck = view.findViewById(R.id.averageCheck);
        requiredExamsCheck = view.findViewById(R.id.requiredExamsCheck);
        edtAverage = view.findViewById(R.id.edtAverage);
        edtRequiredExams = view.findViewById(R.id.edtRequiredExams);
        buttonAdd = view.findViewById(R.id.buttonAdd);



        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();




        if (getArguments() != null) {

            correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("time");
            String faculty = getArguments().getString("department");
            name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String related_projects = getArguments().getString("related_projects");
            String average = getArguments().getString(("average_marks"));
            String required_exam = getArguments().getString("required_exam");


            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            edtTime.setText(estimated_time);
            edtDescription.setText(description);
            edtRelatedProjects.setText(related_projects);
            edtAverage.setText(average);
            edtRequiredExams.setText(required_exam);
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        edtRequiredExams.setEnabled(false);
        edtAverage.setEnabled(false);


        CollectionReference collectionRef = db.collection("professori");
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals(mUser.getEmail()))
                    {
                        nome = document.getString("Name")+" "+ document.getString("Surname");
                        correlatori.add(nome);
                    }
                }

                correlatori.add("Nessuno");

                ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, correlatori );
                adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCorrelator.setAdapter(adapterProf);

                if(!correlator.equals(""))
                    spinnerCorrelator.setSelection(adapterProf.getPosition(correlator));
                else
                    spinnerCorrelator.setSelection(adapterProf.getPosition("Nessuno"));

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

        requiredExamsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edtRequiredExams.setEnabled(requiredExamsCheck.isChecked());
            }
        });

        if(!edtAverage.getText().toString().equals(""))
        {
            averageCheck.setChecked(true);
            edtAverage.setEnabled(true);
        }

        if(!edtRequiredExams.getText().toString().equals(""))
        {
            requiredExamsCheck.setChecked(true);
            edtRequiredExams.setEnabled(true);
        }

        btnSave.setOnClickListener(view1 -> {

            if(averageCheck.isChecked()){
                mediaVoti = edtAverage.getText().toString();

                if(mediaVoti.isEmpty())
                    edtAverage.setError("Inserisci una media valida");

                else{
                    numeroIntero = Integer.parseInt(mediaVoti);

                    if(numeroIntero > 30 || numeroIntero < 18)
                        edtAverage.setError("Inserisci una media tra il 18 ed il 30");
                }
            }else if(requiredExamsCheck.isChecked()){
                materieRichieste = edtRequiredExams.getText().toString();

                if(materieRichieste.isEmpty()){
                    edtRequiredExams.setError("Inserisci le materie richieste");
                }
            }
            if(edtTime.getText().toString().isEmpty())
                edtTime.setError("Inserisci un tempo stimato per la tesi");

            else if(edtDescription.getText().toString().isEmpty())
                edtDescription.setError("Inserisci una descrizione valida");

            else if(Integer.parseInt(edtTime.getText().toString()) >180)
                edtDescription.setError("Inserisci un valore minore di 180 giorni");

            else{
                DocumentReference docRef = db.collection("Tesi").document(name);
                Map<String, Object> updates = new HashMap<>();
                updates.put("Estimated Time", edtTime.getText().toString());
                updates.put("Description", edtDescription.getText().toString());
                updates.put("Related Projects",edtRelatedProjects.getText().toString());
                updates.put("Correlator",spinnerCorrelator.getSelectedItem().toString());
                if(averageCheck.isChecked()){
                    updates.put("Average",edtAverage.getText().toString());
                }else
                {
                    updates.put("Average","");
                }
                if(requiredExamsCheck.isChecked()){
                    updates.put("Required Exam",edtRequiredExams.getText().toString());
                }else
                {
                    updates.put("Required Exam","");
                }
                docRef.update(updates);

                folderRef = storageReference.child(txtNameTitle.getText().toString());
                folderRef.listAll()
                        .addOnSuccessListener(listResult -> {
                            for (StorageReference item : listResult.getItems()) {
                                item.delete();
                            }
                            folderRef.delete();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting folder: " + e.getMessage());
                        });


                for(Uri file: files){
                    uploadFile(file);
                }


                Snackbar.make(view1, "Thesis updated", Snackbar.LENGTH_LONG).show();

                Fragment thesisList = new ThesisListFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisList);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }

        });

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(name);

        storageRef.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    files.add(uri);
                    nomeFile = item.getName();
                    addExistentMaterial(nomeFile,uri);
                });
            }

        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        buttonAdd.setOnClickListener(view12 -> caricaFile());

        return view;
    }

    private void addExistentMaterial(String nomeFile, Uri uri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        nameView.setText(nomeFile);

        delete.setOnClickListener(viewDelete -> {
            layout_lista_file.removeView(view);
            files.remove(uri);
        });

        layout_lista_file.addView(view);
    }


    private void caricaFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
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

    private void addMaterialItem(String fileName, Uri fileUri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        files.add(fileUri);
        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layout_lista_file.removeView(view);
            files.remove(fileUri);
        });
        layout_lista_file.addView(view);
    }

    private void uploadFile(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        File file = new File(uri.getPath());
        String pdfName = file.getName();
        storageReference = FirebaseStorage.getInstance().getReference(txtNameTitle.getText().toString()).child(pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {

                })
                .addOnFailureListener(e -> {

                });
    }
}
