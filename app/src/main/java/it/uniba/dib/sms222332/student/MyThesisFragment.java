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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

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
    LinearLayout layoutThesisAccepted, layoutButtons, layoutMaterials, layoutRequiredExams, layoutButtonCancelRequest, layoutState, layoutAverageMarks;
    LinearLayout layout_lista_file;
    RelativeLayout   layoutNoThesis;
    StorageReference storageReference, ref;
    FirebaseStorage storage;
    Button buttonAdd, btnSave, btnTask, btnReceipt, btnSendMessage, btnCancelRequest;
    Uri fileUri;
    ArrayList<Uri> newMaterials;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.thesisInfoTooolbar));

        View view = inflater.inflate(R.layout.fragment_my_thesis, container, false);

        layoutButtonCancelRequest = view.findViewById(R.id.layoutRequested);
        btnCancelRequest = view.findViewById(R.id.btnDeleteRequest);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnSave = view.findViewById(R.id.btnSave);
        btnTask = view.findViewById(R.id.btnTask);
        btnReceipt = view.findViewById(R.id.btnReceipt);
        layoutButtons = view.findViewById(R.id.layoutButtons);

        buttonAdd = view.findViewById(R.id.buttonAdd);
        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        layoutThesisAccepted = view.findViewById(R.id.layoutThesisAccepted);
        layoutMaterials = view.findViewById(R.id.layoutMaterials);
        layoutRequiredExams = view.findViewById(R.id.layoutRequiredExams);
        layoutAverageMarks = view.findViewById(R.id.layoutAverageMarks);
        layoutNoThesis = view.findViewById(R.id.layoutNoThesis);
        layoutState = view.findViewById(R.id.notAcceptedState);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(MainActivity.account.getRequest().equals("no"))
            noRequest();
        else if(MainActivity.account.getRequest().equals("yes"))
            hasRequested();
        else
            haveThesis(savedInstanceState);
    }

    private void hasRequested() {
        layoutState.setVisibility(View.VISIBLE);
        layoutButtons.setVisibility(View.GONE);
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

                                if (Objects.equals(document.getString("Correlator"), ""))
                                    txtCorrelator.setText(R.string.none);
                                else
                                    txtCorrelator.setText(document.getString("Correlator"));

                                String estimatedTime = document.getString("Estimated Time") +" " + getResources().getString(R.string.days);
                                txtTime.setText(estimatedTime);

                                txtDescription.setText(document.getString("Description"));

                                if (Objects.equals(document.getString("Related Projects"), ""))
                                    txtRelatedProjects.setText(R.string.none);
                                else
                                    txtRelatedProjects.setText(document.getString("Related Projects"));

                                if (Objects.equals(document.getString("Average"), ""))
                                    txtAverageMarks.setText(R.string.none);
                                else
                                    txtAverageMarks.setText(document.getString("Average"));

                                if (Objects.equals(document.getString("Required Exam"), ""))
                                    txtRequiredExams.setText(R.string.none);
                                else
                                    txtRequiredExams.setText(document.getString("Required Exam"));

                            }
                        } else {
                            Log.d(TAG, "get failed with ", task2.getException());
                        }
                    });
        });


    }

    private void noRequest() {
        layoutThesisAccepted.setVisibility(View.GONE);
        layoutNoThesis.setVisibility(View.VISIBLE);
        layoutButtons.setVisibility(View.GONE);
    }

    private void haveThesis(Bundle bundle) {
        layoutAverageMarks.setVisibility(View.GONE);
        layoutRequiredExams.setVisibility(View.GONE);
        layoutMaterials.setVisibility(View.VISIBLE);
        layoutButtons.setVisibility(View.VISIBLE);

        newMaterials = new ArrayList<>();


        String thesisName = MainActivity.account.getRequest();

        db.collection("Tesi")
                .document(thesisName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            txtNameTitle.setText(thesisName);
                            txtType.setText(document.getString("Type"));
                            txtDepartment.setText(document.getString("Faculty"));
                            txtProfessor.setText(document.getString("Professor"));

                            if (Objects.equals(document.getString("Correlator"), ""))
                                txtCorrelator.setText(R.string.none);
                            else
                                txtCorrelator.setText(document.getString("Correlator"));

                            String estimatedTime = document.getString("Estimated Time") + " days";
                            txtTime.setText(estimatedTime);

                            txtDescription.setText(document.getString("Description"));

                            if (Objects.equals(document.getString("Related Projects"), ""))
                                txtRelatedProjects.setText(R.string.none);
                            else
                                txtRelatedProjects.setText(document.getString("Related Projects"));
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });

        //AGGIUNGO MATERIALI DEL DATABASE
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference().child(thesisName).listAll().addOnSuccessListener(listResult -> {

            for (StorageReference item : listResult.getItems()) {

                addDownloadableMaterial(item.getName());
            }

            if(bundle != null){
                newMaterials = bundle.getParcelableArrayList("new_materials");
                for(Uri uri : newMaterials)
                    newMaterial(uri);
            }

        }).addOnFailureListener(exception -> Log.w("info", getString(R.string.error_file), exception));

        buttonAdd.setOnClickListener(view -> addNewMaterial());

        btnSave.setOnClickListener(view -> saveNewMaterials(thesisName));

        btnTask.setOnClickListener(view -> viewTasks(thesisName));

        btnReceipt.setOnClickListener(view -> viewReceipts(thesisName));

        btnSendMessage.setOnClickListener(view -> sendMessage(thesisName));

    }

    private void sendMessage(String thesis_name) {
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

    private void viewReceipts(String thesis_name) {
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

    private void viewTasks(String thesis_name) {
        Fragment taskListFragment = new TaskListFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesisName", thesis_name);
        bundle.putString("professor", txtProfessor.getText().toString());
        bundle.putString("student", MainActivity.account.getEmail());

        taskListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, taskListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void saveNewMaterials(String thesisName) {

        for (Uri uri : newMaterials) {
            uploadToDatabase(thesisName,uri);
        }
        Snackbar.make(requireView(), R.string.thesis_updated, Snackbar.LENGTH_LONG).show();

        getParentFragmentManager().popBackStack();
    }

    private void addNewMaterial() {
        int permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
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

    private void uploadToDatabase(String thesisName, Uri uri) {
        // Creazione del riferimento al file sul server di Firebase
        String pdfName = getNameFromUri(uri);
        storageReference = FirebaseStorage.getInstance().getReference(thesisName).child(pdfName);
        // Caricamento del file sul server
        storageReference.putFile(uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) { //CONDIZIONE PER IL CARICAMENTO DEL PDF
            fileUri = data.getData();
            newMaterial(fileUri);
            newMaterials.add(fileUri);
        }
    }

    private void newMaterial(Uri uri) {
        View view = getLayoutInflater().inflate(R.layout.card_material, null);

        String fileName = getNameFromUri(uri);
        TextView nameView = view.findViewById(R.id.materialName);
        Button delete = view.findViewById(R.id.deleteMaterial);

        nameView.setText(fileName);

        delete.setOnClickListener(v -> {
            layout_lista_file.removeView(view);
            newMaterials.remove(uri);
        });
        layout_lista_file.addView(view);
    }

    private void addDownloadableMaterial(String fileName) {
        View view = getLayoutInflater().inflate(R.layout.card_material_downloadable, null);
        TextView nameView = view.findViewById(R.id.materialName);
        nameView.setText(fileName);

        Button downloadMaterial = view.findViewById(R.id.downloadMaterial);

        downloadMaterial.setOnClickListener(view1 -> {

            int permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // permesso già concesso, procedi con la lettura dei file
                download(fileName);
                Snackbar.make(view1, getResources().getString(R.string.downloading) + fileName, Snackbar.LENGTH_LONG).show();
            }

        });

        layout_lista_file.addView(view);
    }

    private void download(String nomeFile) {

        storageReference = FirebaseStorage.getInstance().getReference();
        ref = storageReference.child(MainActivity.account.getRequest()).child(nomeFile);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String url = uri.toString();
            downloadFile(requireActivity(), nomeFile, DIRECTORY_DOWNLOADS, url);

        });
    }

    private void downloadFile(Context context, String nomeFile, String destinationDirectory, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, nomeFile + "");
        downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Snackbar.make(requireView(), "Permission granted", Snackbar.LENGTH_SHORT).show();
                break;

            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    uploadFile();
                 else
                    buttonAdd.setOnClickListener(view -> Snackbar.make(requireView(), R.string.not_read_permissions, Snackbar.LENGTH_LONG).show());
                break;

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (newMaterials != null)
            outState.putParcelableArrayList("new_materials",newMaterials);
    }
}
