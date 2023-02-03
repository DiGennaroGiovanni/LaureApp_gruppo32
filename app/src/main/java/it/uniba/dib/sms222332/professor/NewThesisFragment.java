package it.uniba.dib.sms222332.professor;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.tools.ThesisPDF;


public class NewThesisFragment extends Fragment {


    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    EditText edtThesisName, edtEstimatedTime, edtDescription, edtRequestedExams, edtRelatedProjects, edtAverage;
    RadioGroup radioGroup;
    RadioButton radioButtonSperimentale, radioButtonCompilativa;
    Spinner spinnerFaculty, spinnerCorrelator;
    Button addFile, buttonCreateThesis;
    TextView txtFaculty;
    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Uri fileUri;
    LinearLayout layout;

    CheckBox averageCheck, examCheck;

    ArrayList<Uri> uris;
    ArrayList<String> correlatori;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.createThesisToolbar));

        View view = inflater.inflate(R.layout.fragment_new_thesis, container, false);

        uris = new ArrayList<>();
        correlatori = new ArrayList<>();

        layout = view.findViewById(R.id.layout_lista);
        averageCheck = view.findViewById(R.id.averageCheck);
        examCheck = view.findViewById(R.id.requiredExamsCheck);
        edtRequestedExams = view.findViewById(R.id.edtRequiredExams);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        edtAverage = view.findViewById(R.id.edtAverage);
        edtThesisName = view.findViewById(R.id.edtThesisName);
        spinnerFaculty = view.findViewById(R.id.edtFaculty);
        edtEstimatedTime = view.findViewById(R.id.edtEstimatedTime);
        spinnerCorrelator = view.findViewById(R.id.edtCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioButtonSperimentale = view.findViewById(R.id.radioButtonSperimentale);
        radioButtonCompilativa = view.findViewById(R.id.radioButtonCompilativa);
        addFile = view.findViewById(R.id.addFile);
        buttonCreateThesis = view.findViewById(R.id.buttonCreateThesis);



        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.faculty_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner

        // Setto l'ArrayAdapter allo spinner
        spinnerFaculty.setAdapter(adapter);


        if (savedInstanceState != null)
            restoreInstance(savedInstanceState);

        if (!examCheck.isChecked())
            edtAverage.setEnabled(false);

        if (!averageCheck.isChecked())
            edtRequestedExams.setEnabled(false);


        buttonCreateThesis.setOnClickListener(view1 -> inserisciTesi());

        addFile.setOnClickListener(view12 -> addFileOnClick());

        CollectionReference collectionRef = db.collection("professori");

        collectionRef.get().addOnCompleteListener(this::getCorrelator);

        averageCheck.setOnCheckedChangeListener((compoundButton, b) -> edtAverage.setEnabled(averageCheck.isChecked()));

        examCheck.setOnCheckedChangeListener((buttonView, isChecked) -> edtRequestedExams.setEnabled(examCheck.isChecked()));


        return view;
    }


    private void getCorrelator(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {

            correlatori.add(getResources().getString(R.string.none));

            for (QueryDocumentSnapshot document : task.getResult()) {
                if (!document.getId().equals(MainActivity.account.getEmail())) {
                    String nome = document.getString("Name") + " " + document.getString("Surname");
                    correlatori.add(nome);
                }
            }

            ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, correlatori);
            adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCorrelator.setAdapter(adapterProf);

        } else {
            Log.d(TAG, String.valueOf(R.string.error_documents), task.getException());
        }
    }

    private void addFileOnClick() {
        int permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // permesso già concesso, procedi con la lettura dei file
            takeFile();
        }
    }


    @SuppressLint("NonConstantResourceId")
    private void inserisciTesi() {

        String thesisName = edtThesisName.getText().toString();
        String faculty = spinnerFaculty.getSelectedItem().toString();
        String estimatedTime = edtEstimatedTime.getText().toString();
        String correlator = spinnerCorrelator.getSelectedItem().toString();
        String description = edtDescription.getText().toString();
        String materieRichieste = edtRequestedExams.getText().toString();
        String mediaVoti = edtAverage.getText().toString();
        String relatedProjects = edtRelatedProjects.getText().toString();
        String tipoTesi = "";
        String professore = MainActivity.account.getEmail();


        if (thesisName.isEmpty()) {
            edtThesisName.setError(getString(R.string.enter_thesis_name));
            missingFieldMsg();

        } else if (radioGroup.getCheckedRadioButtonId() == -1) {
            radioButtonCompilativa.setError(getResources().getString(R.string.select_typology));
            missingFieldMsg();

        } else if (estimatedTime.isEmpty()) {
            edtEstimatedTime.setError(getString(R.string.enter_estimated_time));
            missingFieldMsg();

        } else if (description.isEmpty()) {
            edtDescription.setError(getString(R.string.enter_thesis_description));
            missingFieldMsg();

        } else if (averageCheck.isChecked() && mediaVoti.isEmpty()) {
            edtAverage.setError(getString(R.string.valid_constraint));
            missingFieldMsg();

        } else if (averageCheck.isChecked() && Integer.parseInt(mediaVoti) > 30 && Integer.parseInt(mediaVoti) < 18) {
            edtAverage.setError(getString(R.string.average_range));
            missingFieldMsg();

        } else if (examCheck.isChecked() && materieRichieste.isEmpty()) {
            edtRequestedExams.setError(getString(R.string.required_subjects));
            missingFieldMsg();

        } else {

            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radioButtonSperimentale:
                    tipoTesi = radioButtonSperimentale.getText().toString();
                    break;
                case R.id.radioButtonCompilativa:
                    tipoTesi = radioButtonCompilativa.getText().toString();
                    break;
                default:
                    //nessuna tesi selezionata
                    break;
            }

            if (correlator.equals(getResources().getString(R.string.none))) {
                correlator = "";
            }

            Map<String, String> infoTesi = new HashMap<>();
            infoTesi.put("Professor", professore);
            infoTesi.put("Name", thesisName);
            infoTesi.put("Faculty", faculty);
            infoTesi.put("Estimated Time", estimatedTime);
            infoTesi.put("Correlator", correlator);
            infoTesi.put("Description", description);
            infoTesi.put("Related Projects", relatedProjects);
            infoTesi.put("Required Exam", materieRichieste);
            infoTesi.put("Average", mediaVoti);
            infoTesi.put("Student", "");


            switch (tipoTesi) {
                case "Sperimentale":
                case "Experimental":
                    infoTesi.put("Type", "Experimental");
                    break;

                case "Compilativa":
                case "Drafted":
                    infoTesi.put("Type", "Drafted");
                    break;

                default:
                    break;
            }

            db.collection("Tesi").document(thesisName).set(infoTesi);
            for (Uri file : uris) {
                uploadFile(file);
            }
            // Creazione e caricamento del PDF riepilogativo sul database
            createPdf(thesisName, infoTesi);

            Snackbar.make(requireView(), R.string.toast_thesis_created, Snackbar.LENGTH_SHORT).show();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new ProfessorHomeFragment());
            fragmentTransaction.commit();
        }
    }

    private void missingFieldMsg() {
        Snackbar.make(requireView(), getResources().getString(R.string.missing_fields_msg), Snackbar.LENGTH_LONG).show();
    }

    /**
     * Il metodo createPdf permette di generare un file pdf contenente tutte le informazioni
     * inserite dall'utente riguardanti la tesi appena generata. Il file sarò successivamente caricato
     * sul database per poi poter essere condiviso tramite app di terze parti.
     *
     * @param thesisName String contenente il nome della tesi
     * @param infoTesi   Map contenente tutti i dati inseriti della tesi
     */
    private void createPdf(String thesisName, Map<String, String> infoTesi) {
        try {
            ThesisPDF thesisPDF = new ThesisPDF();
            thesisPDF.makePdf(requireContext(), infoTesi);
            File outputFile = new File(requireContext().getExternalFilesDir(null), thesisName + ".pdf");
            Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);
            uploadPDF(uri);
        } catch (Exception e) {
            Log.e("PDF ERROR", getString(R.string.error_pdf));
        }
    }

    private void addMaterialItem(Uri fileUri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);

        String fileName = getNameFromUri(fileUri);

        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);
        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layout.removeView(view);
            uris.remove(fileUri);
        });
        layout.addView(view);
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

    private void takeFile() {
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
            addMaterialItem(fileUri);
            uris.add(fileUri);
        }
    }

    private void uploadFile(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        String pdfName = getNameFromUri(uri);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(edtThesisName.getText().toString() + "/" + pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    private void uploadPDF(Uri uriPDF) {
        File filePDF = new File(uriPDF.getPath());
        String pdfName = filePDF.getName();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("PDF_tesi" + "/" + pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uriPDF);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("thesis_name", edtThesisName.getText().toString());
        outState.putString("description", edtDescription.getText().toString());
        outState.putString("projects", edtRelatedProjects.getText().toString());

        int selectedRadioBtnId = radioGroup.getCheckedRadioButtonId();
        outState.putInt("selected_id", selectedRadioBtnId);

        int facultyPosition = spinnerFaculty.getSelectedItemPosition();
        outState.putInt("faculty", facultyPosition);
        int correlatorPosition = spinnerCorrelator.getSelectedItemPosition();
        outState.putInt("correlator", correlatorPosition);

        if (!edtEstimatedTime.getText().toString().equals(""))
            outState.putString("estimated_time", edtEstimatedTime.getText().toString());

        outState.putBoolean("avg_check", averageCheck.isChecked());
        if (averageCheck.isChecked()) {
            outState.putString("avg_value", edtAverage.getText().toString());
        }
        outState.putBoolean("exams_check", examCheck.isChecked());
        if (examCheck.isChecked()) {
            outState.putString("exams_value", edtRequestedExams.getText().toString());
        }

        outState.putParcelableArrayList("uris", uris);

    }

    private void restoreInstance(Bundle bundle) {
        edtThesisName.setText(bundle.getString("thesis_name", ""));
        edtDescription.setText(bundle.getString("description", ""));
        edtRelatedProjects.setText(bundle.getString("projects", ""));

        radioGroup.check(bundle.getInt("selected_id"));

        spinnerFaculty.setSelection(bundle.getInt("faculty", 0));
        spinnerCorrelator.setSelection(bundle.getInt("correlator", 0));

        edtEstimatedTime.setText(bundle.getString("estimated time", ""));

        averageCheck.setChecked(bundle.getBoolean("avg_check"));
        if (averageCheck.isChecked())
            edtAverage.setText(bundle.getString("avg_value", ""));

        examCheck.setChecked(bundle.getBoolean("exams_check"));
        if (examCheck.isChecked())
            edtRequestedExams.setText(bundle.getString("exams_value"));

        uris = bundle.getParcelableArrayList("uris");
        if (uris != null) {
            for (Uri uri : uris)
                addMaterialItem(uri);
        }

    }
}


