package it.uniba.dib.sms222332;

import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

public class ThesisDescription extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams;
    Button btnModify,btnDelete;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layout_lista_file;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description, container, false);

        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtType = view.findViewById(R.id.txtType);
        txtTime = view.findViewById(R.id.txtTime);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtRelatedProjects = view.findViewById(R.id.txtRelatedProjects);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);
        btnModify = view.findViewById(R.id.btnModify);
        btnDelete = view.findViewById(R.id.btnDelete);

        if (getArguments() != null) {
            String correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("estimated_time");
            String faculty = getArguments().getString("faculty");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String related_projects = getArguments().getString("related_projects");

            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            txtTime.setText(estimated_time);

            if(correlator.isEmpty())
               txtCorrelator.setText("There is no correlator");
            else
                txtCorrelator.setText(correlator);

            txtDescription.setText(description);
            txtRelatedProjects.setText(related_projects);

        }

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment modifyThesis = new ModifyThesisFragment();
                Bundle bundle = new Bundle();

                bundle.putString("name",txtNameTitle.getText().toString());
                bundle.putString("type",txtType.getText().toString());
                bundle.putString("related_projects",txtRelatedProjects.getText().toString());
                bundle.putString("department",txtDepartment.getText().toString());
                bundle.putString("time",txtTime.getText().toString());
                bundle.putString("correlator",txtCorrelator.getText().toString());
                bundle.putString("description",txtDescription.getText().toString());
                //TODO Bisogna passare i vincoli

                modifyThesis.setArguments(bundle);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, modifyThesis);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DocumentReference tesi = db.collection("Tesi").document(txtNameTitle.getText().toString());
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Conferma eliminazione");
                builder.setMessage("Sei sicuro di voler eliminare questo elemento?");

                builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        storageRef.delete();
                        tesi.delete();

                        Snackbar.make(view, "Thesis eliminated", Snackbar.LENGTH_LONG).show();

                        Fragment thesisListFragment = new ThesisListFragment();
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, thesisListFragment);
                        transaction.commit();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());

        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<String> fileNames = new ArrayList<>();
                for (StorageReference item : listResult.getItems()) {
                    fileNames.add(item.getName());
                    String nomeFile = item.getName();
                    addCard(nomeFile);
                }
                Log.d("info", "Nomi dei file: " + fileNames);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w("info", "Errore nel recupero dei file.", exception);
            }
        });

        return view;
    }

    private void addCard(String nomeFile) {
        View view = getLayoutInflater().inflate(R.layout.card_no_delete, null);
        TextView nameView = view.findViewById(R.id.name);
        nameView.setText(nomeFile);

        layout_lista_file.addView(view);
    }

}
