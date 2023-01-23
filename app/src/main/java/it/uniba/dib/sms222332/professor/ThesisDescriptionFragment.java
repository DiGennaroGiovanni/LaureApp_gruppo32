package it.uniba.dib.sms222332.professor;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class ThesisDescriptionFragment extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,
            txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams,txtStudentTitle,txtStudent;
    Button btnEdit,btnDelete,btnReceipt,btnTask;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layout_lista_file;
    String related_projects = "" ;
    String average_marks = "" ;
    String required_exam = "";
    String student = "";
    StorageReference storageReference, ref;
    FirebaseStorage storage;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description, container, false);

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
            String correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("estimated_time");
            String faculty = getArguments().getString("faculty");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type");
            related_projects = getArguments().getString("related_projects");
            average_marks = getArguments().getString("average_marks");
            required_exam = getArguments().getString("required_exam");
            student = getArguments().getString("student");

            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            txtTime.setText(estimated_time);
            txtDescription.setText(description);


            if(correlator.isEmpty())
               txtCorrelator.setText("None");
            else
                txtCorrelator.setText(correlator);

            if(student.isEmpty()){
                txtStudent.setText("None");
            }else
                txtStudent.setText(student);

            if(average_marks.isEmpty()){
                txtAverageMarks.setText("None");
            }else
                txtAverageMarks.setText(average_marks);

            if(required_exam.isEmpty()){
                txtRequiredExams.setText("None");
            }else
                txtRequiredExams.setText(required_exam);

            if(related_projects.isEmpty()){
                txtRelatedProjects.setText("None");
            }else
                txtRelatedProjects.setText(related_projects);

        }

        if(!student.isEmpty()){
            btnTask.setVisibility(View.VISIBLE);
            btnReceipt.setVisibility(View.VISIBLE);
        }

        btnEdit.setOnClickListener(view1 -> {
            Fragment editThesisFragment = new EditThesisFragment();
            Bundle bundle = new Bundle();

            bundle.putString("name",txtNameTitle.getText().toString());
            bundle.putString("type",txtType.getText().toString());
            bundle.putString("related_projects",related_projects);
            bundle.putString("department",txtDepartment.getText().toString());
            bundle.putString("time",txtTime.getText().toString());
            bundle.putString("correlator",txtCorrelator.getText().toString());
            bundle.putString("description",txtDescription.getText().toString());
            bundle.putString("student",txtStudent.getText().toString());
            bundle.putString("required_exam",required_exam);
            bundle.putString("average_marks",average_marks);

            editThesisFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editThesisFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        btnDelete.setOnClickListener(view12 -> {

            DocumentReference tesi = db.collection("Tesi").document(txtNameTitle.getText().toString());

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference folderRef = storageRef.child(txtNameTitle.getText().toString());


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Conferma eliminazione");
            builder.setMessage("Sei sicuro di voler eliminare questa tesi ed i suoi materiali?");
            builder.setPositiveButton("No", (dialog, which) -> {

            });

            builder.setNegativeButton("Yes", (dialog, which) -> {

                folderRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                for (StorageReference item : listResult.getItems()) {
                                    item.delete();
                                }
                                folderRef.delete();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error deleting folder: " + e.getMessage()); //TODO INSERIRE MESSAGGIO ERRORE
                            }
                        });

                tesi.delete();

                Snackbar.make(view12, "Thesis eliminated", Snackbar.LENGTH_LONG).show();

                Fragment thesisListFragment = new ThesisListFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, thesisListFragment);
                transaction.commit();

            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        //AGGIUNGO MATERIALI DEL DATABASE
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());

        storageRef.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                String nomeFile = item.getName();
                addMaterialItem(nomeFile);
            }

        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        btnReceipt.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("thesis_name",txtNameTitle.getText().toString());
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

            bundle.putString("thesisName",txtNameTitle.getText().toString());
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
            Snackbar.make(view1, "Downloading "+nomeFile, Snackbar.LENGTH_LONG).show();
        });

        layout_lista_file.addView(view);
    }

    private void download(String nomeFile) {

        storageReference = storage.getInstance().getReference();
        ref = storageReference.child(txtNameTitle.getText().toString()).child(nomeFile);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String url  = uri.toString();
                downloadFile(getActivity(), nomeFile,"", DIRECTORY_DOWNLOADS,url );

        }).addOnFailureListener(e -> {
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
