package it.uniba.dib.sms222332.student;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.professor.ReceiptsListFragment;
import it.uniba.dib.sms222332.professor.TaskListFragment;

public class StudentThesisInfoFragment extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,txtState,
            txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams,txtProfessor,txtNoRequest;
    String thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layoutThesisAccepted,layoutMaterials,layoutRequiredExams,layoutState,layoutAverageMarks,layoutNoThesis;
    LinearLayout layout_lista_file;
    RelativeLayout layoutButton;
    StorageReference storageReference, ref;
    FirebaseStorage storage;
    Button buttonAdd,btnSave,btnTask,btnReceipt;
    Uri fileUri;
    ArrayList<Uri> newMaterials = new ArrayList<>();
    ArrayList<String> deletedOldMaterials = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisInfoTooolbar));

        View view = inflater.inflate(R.layout.fragment_student_thesis_info, container, false);

        btnSave = view.findViewById(R.id.btnSave);
        btnTask = view.findViewById(R.id.btnTask);
        btnReceipt = view.findViewById(R.id.btnReceipt);
        layoutButton = view.findViewById(R.id.layoutButton);
        buttonAdd = view.findViewById(R.id.buttonAdd);
        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        layoutThesisAccepted = view.findViewById(R.id.layoutThesisAccepted);
        layoutMaterials = view.findViewById(R.id.layoutMaterials);
        layoutRequiredExams = view.findViewById(R.id.layoutRequiredExams);
        layoutAverageMarks = view.findViewById(R.id.layoutAverageMarks);
        layoutNoThesis = view.findViewById(R.id.layoutNoThesis);
        layoutState = view.findViewById(R.id.layoutState);
        txtNoRequest = view.findViewById(R.id.txtNoRequest);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtState = view.findViewById(R.id.txtState);
        txtType = view.findViewById(R.id.txtTypology);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtProfessor = view.findViewById(R.id.txtProfessor);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtTime = view.findViewById(R.id.txtTime);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtRelatedProjects = view.findViewById(R.id.txtRelatedProjects);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        CollectionReference richiesteReference = db.collection("richieste");
        richiesteReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Student").equals(MainActivity.account.getEmail())){
                        thesisName = document.getString("Thesis Name");
                        if(MainActivity.account.getRequest().equals(thesisName))//TODO GESTIRE SE LO STUDENTE  HA UNA TESI
                            studenteHaveThesis();
                        else if(MainActivity.account.getRequest().equals("no")) //LO STUDENTE NON HA ANCORA FATTO RICHIESTA
                            studentNotRequest();
                        else//TODO LO STUDENTE HA FATTO RICHIESTA MA NON E' STATA ANCORA ACCETTATA
                           studentYesRequest();
                    }
                }
            }
        });


        return view;
    }

    private void studentYesRequest() {
        layoutState.setVisibility(View.VISIBLE);
    }

    private void studentNotRequest() {
        layoutThesisAccepted.setVisibility(View.GONE);
        layoutNoThesis.setVisibility(View.VISIBLE);
    }

    private void studenteHaveThesis() {
        layoutAverageMarks.setVisibility(View.GONE);
        layoutRequiredExams.setVisibility(View.GONE);
        layoutMaterials.setVisibility(View.VISIBLE);
        layoutButton.setVisibility(View.VISIBLE);

        String thesis_name = MainActivity.account.getRequest();

        db.collection("Tesi")
                .document(thesis_name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                txtNameTitle.setText(thesis_name);
                                txtType.setText(document.getString("Type"));
                                txtDepartment.setText(document.getString("Faculty"));
                                txtProfessor.setText(document.getString("Professor"));

                                    if(document.getString("Correlator").equals(""))
                                        txtCorrelator.setText("None");
                                    else
                                        txtCorrelator.setText(document.getString("Correlator"));

                                String estimatedTime = document.getString("Estimated Time")+" days";
                                txtTime.setText(estimatedTime);

                                txtDescription.setText(document.getString("Description"));

                                if(document.getString("Related Projects").equals(""))
                                    txtRelatedProjects.setText("None");
                                else
                                   txtRelatedProjects.setText(document.getString("Related Projects"));
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

        //AGGIUNGO MATERIALI DEL DATABASE
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(thesis_name);

        storageRef.listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                String nomeFile = item.getName();
                addMaterialItem(nomeFile);
            }

        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        buttonAdd.setOnClickListener(view -> {
            uploadFile();

        });

        btnSave.setOnClickListener(view -> {
            for (String fileName : deletedOldMaterials){
                storageReference.child(thesis_name).child(fileName).delete();
            }

            for(Uri uri: newMaterials){
                uploadToDatabase(uri);
            }

            Snackbar.make(view, "Thesis updated", Snackbar.LENGTH_LONG).show();

            getParentFragmentManager().popBackStack();
        });

        btnTask.setOnClickListener(view -> {

            Fragment taskListFragment = new TaskListFragment();
            Bundle bundle = new Bundle();

            bundle.putString("thesisName",thesis_name);
            bundle.putString("professor",txtProfessor.getText().toString());

            taskListFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, taskListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });

        btnReceipt.setOnClickListener(view -> {

            Bundle bundle = new Bundle();
            bundle.putString("thesis_name",thesis_name);
            bundle.putString("professor",txtProfessor.getText().toString());
            Fragment receiptsListFragment = new ReceiptsListFragment();
            receiptsListFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, receiptsListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });

    }

    private void uploadFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    private void uploadToDatabase(Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        File file = new File(uri.getPath());
        String pdfName = file.getName();
        storageReference = FirebaseStorage.getInstance().getReference(thesisName).child(pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri);
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
            layout_lista_file.removeView(view);
            newMaterials.remove(fileUri);
            deletedOldMaterials.add(fileName);
        });
        layout_lista_file.addView(view);
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
        ref = storageReference.child(thesisName).child(nomeFile);

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
