package it.uniba.dib.sms222332.professor;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.tools.ThesisPDF;

public class EditThesisFragment extends Fragment {

    EditText edtTime, edtDescription, edtRelatedProjects, edtAverage, edtRequiredExams;
    TextView txtDepartment, txtThesisName, txtTypology, txtStudent;
    Button btnSave, btnAddMaterial;
    Spinner spinnerCorrelators;
    CheckBox checkAvg, checkExams;
    LinearLayout layoutMaterialsList, layoutAvgConstraint, layoutExamsConstraint;
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
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.editThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_edit_thesis, container, false);

        layoutMaterialsList = view.findViewById(R.id.layout_lista_file);
        txtTypology = view.findViewById(R.id.txtTypology);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtStudent = view.findViewById(R.id.txtStudent);
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
        layoutAvgConstraint = view.findViewById(R.id.avgConstraint);
        layoutExamsConstraint = view.findViewById(R.id.examsConstraint);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        String correlator = "";

        if (getArguments() != null) {

            correlator = getArguments().getString("correlator");
            getThesisData();
        }

        setCorrelatorSpinner(correlator);

        if(txtStudent.toString().isEmpty())
            txtStudent.setText(R.string.none);

        checkAvg.setOnCheckedChangeListener((compoundButton, isChecked) ->
                edtAverage.setEnabled(isChecked));

        checkExams.setOnCheckedChangeListener((buttonView, isChecked) ->
                edtRequiredExams.setEnabled(isChecked));

        if (!edtAverage.getText().toString().equals("")) {
            checkAvg.setChecked(true);
            edtAverage.setEnabled(true);
        } else {
            if(txtStudent.getText().toString().isEmpty()){
                checkAvg.setChecked(false);
                edtAverage.setEnabled(false);
            }
            else
                layoutAvgConstraint.setVisibility(View.GONE);
        }

        if (!edtRequiredExams.getText().toString().equals("")) {
            checkExams.setChecked(true);
            edtRequiredExams.setEnabled(true);
        } else {
            if(txtStudent.getText().toString().isEmpty()){
                checkExams.setChecked(false);
                edtRequiredExams.setEnabled(false);
            }
            else
                layoutExamsConstraint.setVisibility(View.GONE);
        }

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO

        storage.getReference().child(txtThesisName.getText().toString()).listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                item.getDownloadUrl().addOnSuccessListener(this::addExistentMaterial);
            }

        }).addOnFailureListener(exception -> Log.w("info", getString(R.string.error_file), exception));

        btnAddMaterial.setOnClickListener(view13 -> addNewMaterial());

        btnSave.setOnClickListener(view1 -> saveMaterials());


        return view;
    }

    private void getThesisData() {
        assert getArguments() != null;
        String description = getArguments().getString("description");
        String estimated_time = getArguments().getString("time");
        String faculty = getArguments().getString("department");
        String name = getArguments().getString("name");
        String type = getArguments().getString("typology");
        String related_projects = getArguments().getString("related_projects");
        String average = getArguments().getString(("average_marks"));
        String required_exam = getArguments().getString("required_exam");
        String student = getArguments().getString("student");

        txtThesisName.setText(name);
        txtTypology.setText(type);
        txtDepartment.setText(faculty);
        txtStudent.setText(student);
        edtTime.setText(estimated_time);
        edtDescription.setText(description);
        edtRelatedProjects.setText(related_projects);
        edtAverage.setText(average);
        edtRequiredExams.setText(required_exam);
    }

    private void addNewMaterial() {
        int permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // permesso già concesso, procedi con la lettura dei file
            uploadFile();

        }
    }

    private void saveMaterials() {
        String avgMarks = edtAverage.getText().toString();
        String requiredExams = edtRequiredExams.getText().toString();

        if (checkAvg.isChecked() && (avgMarks.isEmpty()))
            edtAverage.setError(getString(R.string.error_average));

        else if (!(avgMarks.isEmpty()) && (Integer.parseInt(avgMarks) > 30 || Integer.parseInt(avgMarks) < 18))
            edtAverage.setError(getString(R.string.average_range));

        else if (checkExams.isChecked() && requiredExams.isEmpty())
            edtRequiredExams.setError(getString(R.string.required_subjects));

        else if (edtTime.getText().toString().isEmpty())
            edtTime.setError(getString(R.string.thesis_estimated_time));

        else if (edtDescription.getText().toString().isEmpty())
            edtDescription.setError(getString(R.string.valid_description));

        else if (Integer.parseInt(edtTime.getText().toString()) > 300)
            edtTime.setError(getString(R.string.estimated_time_range));

        else {
            infoTesi.put("Name", txtThesisName.getText().toString());
            infoTesi.put("Type", txtTypology.getText().toString());
            infoTesi.put("Faculty", txtDepartment.getText().toString());
            infoTesi.put("Professor", mUser.getEmail());
            infoTesi.put("Correlator", spinnerCorrelators.getSelectedItem().toString());
            infoTesi.put("Description", edtDescription.getText().toString());
            infoTesi.put("Estimated Time", edtTime.getText().toString());
            infoTesi.put("Related Projects", edtRelatedProjects.getText().toString());

            DocumentReference docRef = db.collection("Tesi").document(txtThesisName.getText().toString());
            Map<String, Object> updates = new HashMap<>();
            updates.put("Estimated Time", edtTime.getText().toString());
            updates.put("Description", edtDescription.getText().toString());
            updates.put("Related Projects", edtRelatedProjects.getText().toString());
            updates.put("Correlator", spinnerCorrelators.getSelectedItem().toString());

            if (checkAvg.isChecked()) {
                updates.put("Average", edtAverage.getText().toString());
                infoTesi.put("Average", edtAverage.getText().toString());
            } else {
                updates.put("Average", "");
                infoTesi.put("Average", "");
            }
            if (checkExams.isChecked()) {
                updates.put("Required Exam", edtRequiredExams.getText().toString());
                infoTesi.put("Required Exam", edtRequiredExams.getText().toString());
            } else {
                updates.put("Required Exam", "");
                infoTesi.put("Required Exam", "");
            }

            docRef.update(updates);

            for (String fileName : deletedOldMaterials) {
                storageReference.child(txtThesisName.getText().toString()).child(fileName).delete();
            }

            for (Uri uri : newMaterials) {
                uploadToDatabase(uri);
            }


            try {
                ThesisPDF thesisPDF = new ThesisPDF();
                thesisPDF.makePdf(requireContext(), infoTesi);
                File outputFile = new File(requireContext().getExternalFilesDir(null), infoTesi.get("Name") + ".pdf");
                Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);
                uploadPDF(uri);
            } catch (Exception e) {
                Log.e("PDF ERROR", getString(R.string.error_pdf));
            }


            Snackbar.make(requireView(), "Thesis updated", Snackbar.LENGTH_LONG).show();

            getParentFragmentManager().popBackStack();
        }
    }

    private void setCorrelatorSpinner(String currentCorrelator) {
        CollectionReference collectionRef = db.collection("professori");
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(MainActivity.account.getEmail())) {
                        String correlatorName = document.getString("Name") + " " + document.getString("Surname");
                        correlators.add(correlatorName);
                    }
                }

                correlators.add("Nessuno");

                ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, correlators);
                adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCorrelators.setAdapter(adapterProf);

                if (!currentCorrelator.equals(""))
                    spinnerCorrelators.setSelection(adapterProf.getPosition(currentCorrelator));
                else
                    spinnerCorrelators.setSelection(adapterProf.getPosition("Nessuno"));

            } else {
                Log.d(TAG, getString(R.string.error_documents), task.getException());
            }
        });
    }

    private void addExistentMaterial(Uri uri) {
        String fileName = getNameFromUri(uri);

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
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) { //CONDIZIONE PER IL CARICAMENTO DEL FILE
            fileUri = data.getData();
            addNewMaterial(fileUri);
            newMaterials.add(fileUri);
        }
    }

    private void addNewMaterial(Uri fileUri) {
        String fileName = getNameFromUri(fileUri);

        View view = getLayoutInflater().inflate(R.layout.card_material, null);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);

        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layoutMaterialsList.removeView(view);
            newMaterials.remove(fileUri);
        });
        layoutMaterialsList.addView(view);
    }

    private void uploadToDatabase(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase e caricamento del file sul server
        String pdfName = getNameFromUri(uri);
        FirebaseStorage.getInstance().getReference(txtThesisName.getText().toString()).child(pdfName).putFile(uri);
    }

    private void uploadPDF(Uri uriPDF) {
        File filePDF = new File(uriPDF.getPath());
        String pdfName = filePDF.getName();
        storageReference.child("PDF_tesi").child(pdfName + ".pdf").delete();
        storageReference = FirebaseStorage.getInstance().getReference("PDF_tesi" + "/" + pdfName);
        storageReference.putFile(uriPDF);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(requireView(), "Permission granted", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(requireView(), "Permission denied", Snackbar.LENGTH_SHORT ).show();
            }
        }
    }

    private String getNameFromUri(Uri pdfUri) {
        String fileName = null;
        Cursor cursor = requireActivity().getContentResolver().query(pdfUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }


}
