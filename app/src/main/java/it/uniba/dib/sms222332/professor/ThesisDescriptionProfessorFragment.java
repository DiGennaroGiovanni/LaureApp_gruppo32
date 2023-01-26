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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import it.uniba.dib.sms222332.R;

public class ThesisDescriptionProfessorFragment extends Fragment {

    TextView txtThesisName,txtType,txtDepartment, txtTime,txtCorrelator,
            txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams,txtStudent;
    Button btnEdit,btnDelete,btnReceipt,btnTask;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    LinearLayout layout_lista_file;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description_professor, container, false);

        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        txtThesisName = view.findViewById(R.id.txtNameTitle);
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
        txtStudent = view.findViewById(R.id.txtStudent);
        btnReceipt = view.findViewById(R.id.btnReceipt);
        btnTask = view.findViewById(R.id.btnTask);

        if (getArguments() != null) {
            String correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("estimated_time");
            String faculty = getArguments().getString("faculty");
            String thesisName = getArguments().getString("name");
            String type = getArguments().getString("type");
            String relatedProjects = getArguments().getString("related_projects");
            String averageMarks = getArguments().getString("average_marks");
            String requiredExam = getArguments().getString("required_exam");
            String student = getArguments().getString("student");

            txtThesisName.setText(thesisName);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            txtTime.setText(estimated_time);
            txtDescription.setText(description);


            if(correlator.isEmpty())
               txtCorrelator.setText("None");
            else
                txtCorrelator.setText(correlator);

            if(student.isEmpty())
                txtStudent.setText("None");
            else
                txtStudent.setText(student);

            if(averageMarks.isEmpty())
                txtAverageMarks.setText("None");
            else
                txtAverageMarks.setText(averageMarks);

            if(requiredExam.isEmpty())
                txtRequiredExams.setText("None");
            else
                txtRequiredExams.setText(requiredExam);

            if(relatedProjects.isEmpty())
                txtRelatedProjects.setText("None");
            else
                txtRelatedProjects.setText(relatedProjects);
        }

        if(!txtStudent.getText().toString().equals("None")){
            btnTask.setVisibility(View.VISIBLE);
            btnReceipt.setVisibility(View.VISIBLE);
        }

        btnEdit.setOnClickListener(view1 -> {
            Fragment editThesisFragment = new EditThesisFragment();
            Bundle bundle = new Bundle();

            bundle.putString("name", txtThesisName.getText().toString());
            bundle.putString("type",txtType.getText().toString());
            bundle.putString("related_projects",txtRelatedProjects.getText().toString());
            bundle.putString("department",txtDepartment.getText().toString());
            bundle.putString("time",txtTime.getText().toString());
            bundle.putString("correlator",txtCorrelator.getText().toString());
            bundle.putString("description",txtDescription.getText().toString());
            bundle.putString("student",txtStudent.getText().toString());
            bundle.putString("required_exam",txtRequiredExams.getText().toString());
            bundle.putString("average_marks", txtAverageMarks.getText().toString());

            editThesisFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editThesisFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        btnDelete.setOnClickListener(view12 -> {

            DocumentReference tesi = db.collection("Tesi").document(txtThesisName.getText().toString());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference folderRef = storageRef.child(txtThesisName.getText().toString());


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_deletion);
            builder.setMessage(R.string.thesis_materials_delete_question);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {

                folderRef.listAll()
                        .addOnSuccessListener(listResult -> {
                            for (StorageReference item : listResult.getItems()) {
                                item.delete();
                            }
                            folderRef.delete();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, R.string.error_deleting_folder + e.getMessage()));

                storageReference.child("PDF_tesi").child(txtThesisName.getText().toString() + ".pdf").delete();
                tesi.delete();


                Snackbar.make(view12, R.string.thesis_eliminated, Snackbar.LENGTH_LONG).show();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ThesesListFragment());
                transaction.commit();

            });

            builder.setNegativeButton(R.string.no, (dialog, which) -> {

            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        //AGGIUNGO MATERIALI DEL DATABASE
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(txtThesisName.getText().toString());

        storageRef.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                String nomeFile = item.getName();
                addMaterialItem(nomeFile);
            }

        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        btnReceipt.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("thesis_name", txtThesisName.getText().toString());
            bundle.putString("student",txtStudent.getText().toString());
            Fragment receiptsListFragment = new ReceiptsListFragment();
            receiptsListFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, receiptsListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });


        btnTask.setOnClickListener(v -> {

            Fragment taskListFragment = new TaskListFragment();
            Bundle bundle = new Bundle();

            bundle.putString("thesisName", txtThesisName.getText().toString());
            bundle.putString("student",txtStudent.getText().toString());

            taskListFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, taskListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });

        return view;
    }



    private void addMaterialItem(String nomeFile) {
        View view = getLayoutInflater().inflate(R.layout.card_material_without_delete, null);
        TextView nameView = view.findViewById(R.id.materialName);
        nameView.setText(nomeFile);

        Button downloadMaterial;
        downloadMaterial = view.findViewById(R.id.downloadMaterial);

        downloadMaterial.setOnClickListener(view1 -> {
            download(nomeFile);
            Snackbar.make(view1, R.string.downloading + nomeFile, Snackbar.LENGTH_LONG).show();
        });

        layout_lista_file.addView(view);
    }

    private void download(String nomeFile) {

        StorageReference ref = storageReference.child(txtThesisName.getText().toString()).child(nomeFile);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String url  = uri.toString();
            downloadFile(getActivity(), nomeFile,"", DIRECTORY_DOWNLOADS,url );

        });
    }

    private void downloadFile(Context context, String nomeFile, String fileExtension, String destinationDirectory, String url ) {
         DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
         Uri uri = Uri.parse(url);
         DownloadManager.Request request = new DownloadManager.Request(uri);

         request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
         request.setDestinationInExternalFilesDir(context, destinationDirectory, nomeFile + fileExtension);
         downloadManager.enqueue(request);
    }
}
