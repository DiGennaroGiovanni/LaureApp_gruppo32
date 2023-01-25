package it.uniba.dib.sms222332.professor;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
import java.util.LinkedHashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.tools.ThesisPDF;

public class EditThesisFragment extends Fragment {

    EditText edtTime,edtDescription,edtRelatedProjects,edtAverage,edtRequiredExams;
    TextView txtDepartment, txtThesisName, txtTypology;
    Button btnSave, btnAddMaterial;
    Spinner spinnerCorrelators;
    CheckBox checkAvg, checkExams;
    LinearLayout layoutMaterialsList;
    Uri fileUri;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    ArrayList<String> correlators = new ArrayList<>();
    ArrayList<Uri> newMaterials = new ArrayList<>();
    ArrayList<String> deletedOldMaterials = new ArrayList<>();
    LinkedHashMap<String, String> infoTesi = new LinkedHashMap<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.editThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_edit_thesis, container, false);

        layoutMaterialsList = view.findViewById(R.id.layout_lista_file);
        txtTypology = view.findViewById(R.id.txtTypology);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        edtTime = view.findViewById(R.id.edtTime);
        spinnerCorrelators = view.findViewById(R.id.spinnerCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        txtThesisName = view.findViewById(R.id.txtNameTitle);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        btnSave = view.findViewById(R.id.btnSave);
        checkAvg = view.findViewById(R.id.averageCheck);
        checkExams = view.findViewById(R.id.requiredExamsCheck);
        edtAverage = view.findViewById(R.id.edtAverage);
        edtRequiredExams = view.findViewById(R.id.edtRequiredExams);
        btnAddMaterial = view.findViewById(R.id.buttonAdd);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        String correlator = "";

        if (getArguments() != null) {

            correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("time");
            String faculty = getArguments().getString("department");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String related_projects = getArguments().getString("related_projects");
            String average = getArguments().getString(("average_marks"));
            String required_exam = getArguments().getString("required_exam");

            txtThesisName.setText(name);
            txtTypology.setText(type);
            txtDepartment.setText(faculty);
            edtTime.setText(estimated_time);
            edtDescription.setText(description);
            edtRelatedProjects.setText(related_projects);
            edtAverage.setText(average);
            edtRequiredExams.setText(required_exam);
        }

        setCorrelatorSpinner(correlator);

        checkAvg.setOnCheckedChangeListener((compoundButton, isChecked) ->
                edtAverage.setEnabled(isChecked));

        checkExams.setOnCheckedChangeListener((buttonView, isChecked) ->
                edtRequiredExams.setEnabled(isChecked));

        if(!edtAverage.getText().toString().equals("")) {
            checkAvg.setChecked(true);
            edtAverage.setEnabled(true);
        } else {
            checkAvg.setChecked(false);
            edtAverage.setEnabled(false);
        }

        if(!edtRequiredExams.getText().toString().equals("")) {
            checkExams.setChecked(true);
            edtRequiredExams.setEnabled(true);
        } else {
            checkExams.setChecked(false);
            edtRequiredExams.setEnabled(false);
        }

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO

        storage.getReference().child(txtThesisName.getText().toString()).listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileName = item.getName();
                    addExistentMaterial(fileName,uri);
                });
            }

        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        btnAddMaterial.setOnClickListener(view13 -> {


            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // permesso già concesso, procedi con la lettura dei file
                uploadFile();
            }

        });

  btnSave.setOnClickListener(view1 -> {

            String avgMarks = edtAverage.getText().toString();
            String requiredExams = edtRequiredExams.getText().toString();

             if(checkAvg.isChecked() && (avgMarks.isEmpty()))
                 edtAverage.setError("Inserisci una media valida");

             else if (!(avgMarks.isEmpty()) && (Integer.parseInt(avgMarks) > 30 || Integer.parseInt(avgMarks) < 18))
                 edtAverage.setError("Inserisci una media tra il 18 ed il 30");

             else if(checkExams.isChecked() && requiredExams.isEmpty())
                    edtRequiredExams.setError("Inserisci le materie richieste");

             else if(edtTime.getText().toString().isEmpty())
                edtTime.setError("Inserisci un tempo stimato per la tesi");

             else if(edtDescription.getText().toString().isEmpty())
                edtDescription.setError("Inserisci una descrizione valida");

             else if(Integer.parseInt(edtTime.getText().toString()) >180)
                edtTime.setError("Inserisci un valore minore di 180 giorni");

             else{
                 infoTesi.put("Name", txtThesisName.getText().toString());
                 infoTesi.put("Type", txtTypology.getText().toString());
                 infoTesi.put("Faculty", txtDepartment.getText().toString());
                 infoTesi.put("Professor", mUser.getEmail());
                 infoTesi.put("Correlator", spinnerCorrelators.getSelectedItem().toString());
                 infoTesi.put("Description", edtDescription.getText().toString());
                 infoTesi.put("Estimated Time", edtTime.getText().toString());
                 infoTesi.put("Related Projects",edtRelatedProjects.getText().toString());

                 DocumentReference docRef = db.collection("Tesi").document(txtThesisName.getText().toString());
                 Map<String, Object> updates = new HashMap<>();
                 updates.put("Estimated Time", edtTime.getText().toString());
                 updates.put("Description", edtDescription.getText().toString());
                 updates.put("Related Projects",edtRelatedProjects.getText().toString());
                 updates.put("Correlator", spinnerCorrelators.getSelectedItem().toString());

                 if(checkAvg.isChecked()) {
                     updates.put("Average", edtAverage.getText().toString());
                     infoTesi.put("Average", edtAverage.getText().toString());
                 }
                 else {
                     updates.put("Average", "");
                     infoTesi.put("Average", "");
                 }
                 if(checkExams.isChecked()) {
                     updates.put("Required Exam", edtRequiredExams.getText().toString());
                     infoTesi.put("Required Exam", edtRequiredExams.getText().toString());
                 }
                 else {
                     updates.put("Required Exam", "");
                     infoTesi.put("Required Exam", "");
                 }

                 docRef.update(updates);

                 for (String fileName : deletedOldMaterials){
                     storageReference.child(txtThesisName.getText().toString()).child(fileName).delete();
                 }

                 for(Uri uri: newMaterials){
                     uploadToDatabase(uri);
                 }


                 try {
                     ThesisPDF thesisPDF = new ThesisPDF();
                     thesisPDF.makePdf(requireContext(), infoTesi);
                     File outputFile = new File(requireContext().getExternalFilesDir(null), infoTesi.get("Name") + ".pdf");
                     Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);
                     uploadPDF(uri);
                 } catch(Exception e) {
                     Log.e("PDF ERROR", "Errore nella creazione del pdf");
                 }

//                 // chiusura della tastiera quando viene effettuato un cambio di fragment
//                 InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                 imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

                 Snackbar.make(view1, "Thesis updated", Snackbar.LENGTH_LONG).show();

                 getParentFragmentManager().popBackStack();
             }

        });


        return view;
    }

    private void setCorrelatorSpinner(String currentCorrelator) {
        CollectionReference collectionRef = db.collection("professori");
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals(MainActivity.account.getEmail())) {
                        String correlatorName = document.getString("Name")+" "+ document.getString("Surname");
                        correlators.add(correlatorName);
                    }
                }

                correlators.add("Nessuno");

                ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, correlators);
                adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCorrelators.setAdapter(adapterProf);

                if(!currentCorrelator.equals(""))
                    spinnerCorrelators.setSelection(adapterProf.getPosition(currentCorrelator));
                else
                    spinnerCorrelators.setSelection(adapterProf.getPosition("Nessuno"));

            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    private void addExistentMaterial(String fileName, Uri uri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        nameView.setText(fileName);

        delete.setOnClickListener(viewDelete -> {
            layoutMaterialsList.removeView(view);
            deletedOldMaterials.add(fileName);
        });

        layoutMaterialsList.addView(view);
    }


    private void uploadFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) { //CONDIZIONE PER IL CARICAMENTO DEL PDF
            fileUri = data.getData();
            File file = new File(fileUri.getPath());
            String fileName = file.getName();
            addNewMaterial(fileName, fileUri);
        }
    }

    private void addNewMaterial(String fileName, Uri fileUri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        newMaterials.add(fileUri);
        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layoutMaterialsList.removeView(view);
            newMaterials.remove(fileUri);
        });
        layoutMaterialsList.addView(view);
    }

    private void uploadToDatabase(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        File file = new File(uri.getPath());
        String pdfName = file.getName();
        storageReference = FirebaseStorage.getInstance().getReference(txtThesisName.getText().toString()).child(pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri);
    }

    private void uploadPDF(Uri uriPDF) {
        File filePDF = new File(uriPDF.getPath());
        String pdfName = filePDF.getName();
        storageReference.child("PDF_tesi").child(pdfName + ".pdf").delete();
        storageReference = FirebaseStorage.getInstance().getReference("PDF_tesi" +"/" + pdfName);
        storageReference.putFile(uriPDF);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permesso concesso, procedi con la lettura dei file
                } else {
                    // permesso negato, mostra un messaggio all'utente o disabilita la funzionalità
                }
                return;
            }
        }
    }



}
