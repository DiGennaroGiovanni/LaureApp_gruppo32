package it.uniba.dib.sms222332.student;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.professor.ReceiptsListFragment;
import it.uniba.dib.sms222332.professor.TaskListFragment;

public class MyThesisFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    TextView txtNameTitle, txtType, txtDepartment, txtTime, txtCorrelator, txtState,
            txtDescription, txtRelatedProjects, txtAverageMarks, txtRequiredExams, txtProfessor, txtNoRequest;
    String thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layoutThesisAccepted, layoutMaterials, layoutRequiredExams, layoutState, layoutAverageMarks, layoutNoThesis;
    LinearLayout layout_lista_file;
    RelativeLayout layoutButton, layoutButtonCancelRequest;
    StorageReference storageReference, ref;
    FirebaseStorage storage;
    Button buttonAdd, btnSave, btnTask, btnReceipt, btnSendMessage, btnCancelRequest;
    Uri fileUri;
    ArrayList<Uri> newMaterials = new ArrayList<>();
    ArrayList<String> deletedOldMaterials = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisInfoTooolbar));

        View view = inflater.inflate(R.layout.fragment_my_thesis, container, false);

        layoutButtonCancelRequest = view.findViewById(R.id.layoutButtonRequest);
        btnCancelRequest = view.findViewById(R.id.btnDeleteRequest);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!MainActivity.account.getRequest().equals("no") && !MainActivity.account.getRequest().equals("yes"))//LO STUDENTE  HA UNA TESI
            studenteHaveThesis();
        else if (MainActivity.account.getRequest().equals("no")) //LO STUDENTE NON HA ANCORA FATTO RICHIESTA
            studentNotRequest();
        else// LO STUDENTE HA FATTO RICHIESTA MA NON E' STATA ANCORA ACCETTATA
            studentYesRequest();
    }

    private void studentYesRequest() {
        layoutState.setVisibility(View.VISIBLE);
        layoutButton.setVisibility(View.GONE);
        layoutButtonCancelRequest.setVisibility((View.VISIBLE));


        btnCancelRequest.setOnClickListener(view -> {

            db.collection("richieste").document(MainActivity.account.getEmail()).delete().addOnSuccessListener(unused ->
                    Snackbar.make(requireView(), R.string.request_canceled, Snackbar.LENGTH_LONG).show());

            db.collection("studenti").document(MainActivity.account.getEmail()).update("Request", "no");

            MainActivity.account.setRequest("no");
            onResume();

        });


        db.collection("richieste").document(MainActivity.account.getEmail()).get().addOnSuccessListener(documentSnapshot ->
                thesisName = documentSnapshot.getString("Thesis")).continueWith(task -> {
            if (!task.isSuccessful()) {
                return null;
            }
            return db.collection("Tesi")
                    .document(thesisName)
                    .get()
                    .addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document = task2.getResult();
                            if (document.exists()) {
                                txtNameTitle.setText(thesisName);
                                txtType.setText(document.getString("Type"));
                                txtDepartment.setText(document.getString("Faculty"));
                                txtProfessor.setText(document.getString("Professor"));

                                if (document.getString("Correlator").equals(""))
                                    txtCorrelator.setText(R.string.none);
                                else
                                    txtCorrelator.setText(document.getString("Correlator"));

                                String estimatedTime = document.getString("Estimated Time") + " days";
                                txtTime.setText(estimatedTime);

                                txtDescription.setText(document.getString("Description"));

                                if (document.getString("Related Projects").equals(""))
                                    txtRelatedProjects.setText(R.string.none);
                                else
                                    txtRelatedProjects.setText(document.getString("Related Projects"));

                                if (document.getString("Average").equals(""))
                                    txtAverageMarks.setText(R.string.none);
                                else
                                    txtAverageMarks.setText(document.getString("Average"));

                                if (document.getString("Required Exam").equals(""))
                                    txtRequiredExams.setText(R.string.none);
                                else
                                    txtRequiredExams.setText(document.getString("Required Exam"));

                                String state = getString(R.string.not_accepted_yet);
                                txtState.setTextColor(Color.RED);
                                txtState.setText(state);

                            }
                        } else {
                            Log.d(TAG, "get failed with ", task2.getException());
                        }
                    });
        });


    }

    private void studentNotRequest() {
        layoutThesisAccepted.setVisibility(View.GONE);
        layoutNoThesis.setVisibility(View.VISIBLE);
        layoutButton.setVisibility(View.GONE);
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

                                if (document.getString("Correlator").equals(""))
                                    txtCorrelator.setText("None");
                                else
                                    txtCorrelator.setText(document.getString("Correlator"));

                                String estimatedTime = document.getString("Estimated Time") + " days";
                                txtTime.setText(estimatedTime);

                                txtDescription.setText(document.getString("Description"));

                                if (document.getString("Related Projects").equals(""))
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

        }).addOnFailureListener(exception -> Log.w("info", getString(R.string.error_file), exception));

        buttonAdd.setOnClickListener(view -> {

            buttonAddOnClick();


        });

        btnSave.setOnClickListener(view -> {

            btnSaveOnClick(thesis_name, view);

        });

        btnTask.setOnClickListener(view -> {

            btnTaskOnClick(thesis_name);

        });

        btnReceipt.setOnClickListener(view -> {

            btnReceiptOnClick(thesis_name);

        });


        btnSendMessage.setOnClickListener(view -> {

            btnSendMessageOnClick(thesis_name);

        });

    }

    private void btnSendMessageOnClick(String thesis_name) {
        Fragment thesisMessage = new NewMessageFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesis_name", thesis_name);
        bundle.putString("professor", txtProfessor.getText().toString());

        thesisMessage.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, thesisMessage);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnReceiptOnClick(String thesis_name) {
        Bundle bundle = new Bundle();
        bundle.putString("thesis_name", thesis_name);
        bundle.putString("professor", txtProfessor.getText().toString());
        Fragment receiptsListFragment = new ReceiptsListFragment();
        receiptsListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, receiptsListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnTaskOnClick(String thesis_name) {
        Fragment taskListFragment = new TaskListFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesisName", thesis_name);
        bundle.putString("professor", txtProfessor.getText().toString());

        taskListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, taskListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnSaveOnClick(String thesis_name, View view) {
        for (String fileName : deletedOldMaterials) {
            storageReference.child(thesis_name).child(fileName).delete();
        }

        for (Uri uri : newMaterials) {
            uploadToDatabase(uri);
        }

        Snackbar.make(view, R.string.thesis_updated, Snackbar.LENGTH_LONG).show();

        getParentFragmentManager().popBackStack();
    }

    private void buttonAddOnClick() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // permesso già concesso, procedi con la lettura dei file
            uploadFile();
        }
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

            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // permesso già concesso, procedi con la lettura dei file
                download(nomeFile);
                Snackbar.make(view1, R.string.downloading + nomeFile, Snackbar.LENGTH_LONG).show();
            }

        });

        layout_lista_file.addView(view);
    }

    private void download(String nomeFile) {

        storageReference = storage.getInstance().getReference();
        ref = storageReference.child(MainActivity.account.getRequest()).child(nomeFile);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permesso concesso, procedi con la lettura dei file
                } else {
                    // permesso negato, mostra un messaggio all'utente o disabilita la funzionalità
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadFile();
                } else {
                    buttonAdd.setOnClickListener(view -> {
                        Snackbar.make(getView(), R.string.not_read_permissions, Snackbar.LENGTH_LONG).show();
                    });
                    // permesso negato, mostra un messaggio all'utente o disabilita la funzionalità
                }
                return;
            }
        }
    }

}
