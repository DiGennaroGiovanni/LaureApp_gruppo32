package it.uniba.dib.sms222332.professor;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.uniba.dib.sms222332.R;

public class ThesisDescriptionProfessorFragment extends Fragment {

    TextView txtNameTitle, txtType, txtDepartment, txtTime, txtCorrelator,
            txtDescription, txtRelatedProjects, txtAverageMarks, txtRequiredExams, txtStudentTitle, txtStudent;
    Button btnEdit, btnDelete, btnReceipt, btnTask;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layout_lista_file;
    String related_projects = "";
    String average_marks = "";
    String required_exam = "";
    String student = "";
    StorageReference storageReference, ref;
    FirebaseStorage storage;
    String nameThesis = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description_professor, container, false);

        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtType = view.findViewById(R.id.txtTypology);
        txtTime = view.findViewById(R.id.txtTime);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtRelatedProjects = view.findViewById(R.id.txtRelatedProjects);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);
        btnEdit = view.findViewById(R.id.btnModify);
        btnDelete = view.findViewById(R.id.btnDelete);
        txtStudentTitle = view.findViewById(R.id.txtStudentTitle);
        txtStudent = view.findViewById(R.id.txtStudent);
        btnReceipt = view.findViewById(R.id.btnReceipt);
        btnTask = view.findViewById(R.id.btnTask);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        if (getArguments() != null) {
            getDataFromPreviousFragment();

        }

        if (!student.isEmpty()) {
            btnTask.setVisibility(View.VISIBLE);
            btnReceipt.setVisibility(View.VISIBLE);
        }

        btnEdit.setOnClickListener(view1 -> {
            btnEditOnClick();
        });

        btnDelete.setOnClickListener(view12 -> {
            btnDeleteOnClick(view12);
        });

        //AGGIUNGO MATERIALI DEL DATABASE
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());

        storageRef.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                String nomeFile = item.getName();
                addMaterialItem(nomeFile);
            }

        }).addOnFailureListener(exception -> Log.w("info", getString(R.string.error_documents), exception));

        btnReceipt.setOnClickListener(v -> {

            btnReceiptOnClick();

        });


        btnTask.setOnClickListener(v -> {

            btnTaskOnClick();

        });

        return view;
    }

    private void getDataFromPreviousFragment() {
        String correlator = getArguments().getString("correlator");
        String description = getArguments().getString("description");
        String estimated_time = getArguments().getString("estimated_time");
        String faculty = getArguments().getString("faculty");
        nameThesis = getArguments().getString("name");
        String type = getArguments().getString("type");
        related_projects = getArguments().getString("related_projects");
        average_marks = getArguments().getString("average_marks");
        required_exam = getArguments().getString("required_exam");
        student = getArguments().getString("student");

        txtNameTitle.setText(nameThesis);
        txtType.setText(type);
        txtDepartment.setText(faculty);
        txtTime.setText(estimated_time);
        txtDescription.setText(description);


        if (correlator.isEmpty())
            txtCorrelator.setText(R.string.none);
        else
            txtCorrelator.setText(correlator);

        if (student.isEmpty()) {
            txtStudent.setText(R.string.none);
        } else
            txtStudent.setText(student);

        if (average_marks.isEmpty()) {
            txtAverageMarks.setText(R.string.none);
        } else
            txtAverageMarks.setText(average_marks);

        if (required_exam.isEmpty()) {
            txtRequiredExams.setText(R.string.none);
        } else
            txtRequiredExams.setText(required_exam);

        if (related_projects.isEmpty()) {
            txtRelatedProjects.setText(R.string.none);
        } else
            txtRelatedProjects.setText(related_projects);
    }

    private void btnTaskOnClick() {
        Fragment taskListFragment = new TaskListFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesisName", txtNameTitle.getText().toString());
        bundle.putString("student", txtStudent.getText().toString());

        taskListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, taskListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnReceiptOnClick() {
        Bundle bundle = new Bundle();
        bundle.putString("thesis_name", txtNameTitle.getText().toString());
        bundle.putString("student", txtStudent.getText().toString());
        Fragment receiptsListFragment = new ReceiptsListFragment();
        receiptsListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, receiptsListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnDeleteOnClick(View view12) {
        DocumentReference tesi = db.collection("Tesi").document(txtNameTitle.getText().toString());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderRef = storageRef.child(txtNameTitle.getText().toString());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_deletion);
        builder.setMessage(R.string.thesis_materials_delete_question);
        builder.setPositiveButton(R.string.no, (dialog, which) -> {

        });

        builder.setNegativeButton(R.string.yes, (dialog, which) -> {

            thesisDeletion(view12, tesi, folderRef);

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void thesisDeletion(View view12, DocumentReference tesi, StorageReference folderRef) {
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete();
                    }
                    folderRef.delete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, getString(R.string.error_deleting_folder) + e.getMessage()); //TODO INSERIRE MESSAGGIO ERRORE
                });

        storageReference.child("PDF_tesi").child(nameThesis + ".pdf").delete();
        tesi.delete();

        Snackbar.make(view12, R.string.thesis_eliminated, Snackbar.LENGTH_LONG).show();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ThesesListFragment());
        transaction.commit();
    }

    private void btnEditOnClick() {
        Fragment editThesisFragment = new EditThesisFragment();
        Bundle bundle = new Bundle();

        bundle.putString("name", txtNameTitle.getText().toString());
        bundle.putString("type", txtType.getText().toString());
        bundle.putString("related_projects", related_projects);
        bundle.putString("department", txtDepartment.getText().toString());
        bundle.putString("time", txtTime.getText().toString());
        bundle.putString("correlator", txtCorrelator.getText().toString());
        bundle.putString("description", txtDescription.getText().toString());
        bundle.putString("student", txtStudent.getText().toString());
        bundle.putString("required_exam", required_exam);
        bundle.putString("average_marks", average_marks);

        editThesisFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, editThesisFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private void addMaterialItem(String nomeFile) {
        View view = getLayoutInflater().inflate(R.layout.card_material_without_delete, null);
        TextView nameView = view.findViewById(R.id.materialName);
        nameView.setText(nomeFile);

        Button downloadMaterial;
        downloadMaterial = view.findViewById(R.id.downloadMaterial);

        downloadMaterial.setOnClickListener(view1 -> {
            download(nomeFile);
            Snackbar.make(view1, getString(R.string.downloading) + nomeFile, Snackbar.LENGTH_LONG).show();
        });

        layout_lista_file.addView(view);
    }

    private void download(String nomeFile) {

        storageReference = FirebaseStorage.getInstance().getReference();
        ref = storageReference.child(txtNameTitle.getText().toString()).child(nomeFile);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String url = uri.toString();
            downloadFile(getActivity(), nomeFile, "", DIRECTORY_DOWNLOADS, url);

        }).addOnFailureListener(e -> {
        });
    }

    private void downloadFile(Context context, String nomeFile, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, nomeFile + fileExtension);
        downloadManager.enqueue(request);
    }
}
