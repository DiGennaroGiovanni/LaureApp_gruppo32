package it.uniba.dib.sms222332;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifyThesisFragment extends Fragment {

    EditText edtTime,edtDescription,edtRelatedProjects;
    TextView txtDepartment,txtNameTitle,txtType;
    Button btnSave;
    Spinner spinnerCorrelator;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    String correlator,nome,name,nomeFile;
    LinearLayout layout_lista_file;
    FirebaseUser mUser;
    ArrayList<String> correlatori = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.modifyThesisToolbar));
        //TODO INSERIRE NOME PAGINA

        View view = inflater.inflate(R.layout.fragment_modify_thesis, container, false);

        txtType = view.findViewById(R.id.txtType);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        edtTime = view.findViewById(R.id.edtTime);
        spinnerCorrelator = view.findViewById(R.id.spinnerCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        btnSave = view.findViewById(R.id.btnSave);
        layout_lista_file = view.findViewById(R.id.layout_lista_file);

        if (getArguments() != null) {
            //String constraints = getArguments().getString("constraints"); //TODO ATTUALMENTE NON UTILIZZATO
            correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("time");
            String faculty = getArguments().getString("department");
            name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String related_projects = getArguments().getString("related_projects");

            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            edtTime.setText(estimated_time);
            edtDescription.setText(description);
            edtRelatedProjects.setText(related_projects);

            //TODO AGGIUNGERE OPZIONE PER LA MODIFICA DEI VINCOLI
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        CollectionReference collectionRef = db.collection("professori");

        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals(mUser.getEmail()))
                    {
                        nome = document.getString("Name")+" "+ document.getString("Surname");
                        correlatori.add(nome);
                    }
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

        correlatori.add("Nessuno");

        ArrayAdapter<String> adapterProf = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, correlatori );
        adapterProf.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCorrelator.setAdapter(adapterProf);

        if(!correlator.equals("")){
            spinnerCorrelator.setSelection(adapterProf.getPosition(correlator)); //TODO DA SISTEMARE PERCHè NON FUNZIONA
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtTime.getText().toString().isEmpty()){
                    edtTime.setError("Inserisci un tempo stimato per la tesi");
                }else if(edtDescription.getText().toString().isEmpty()) {
                    edtDescription.setError("Inserisci una descrizione valida");
                } else if(Integer.parseInt(edtTime.getText().toString()) >180)
                   edtDescription.setError("Inserisci un valore minore di 180 giorni");


                FirebaseStorage storage = FirebaseStorage.getInstance();

                try {
                    StorageReference storageRef = storage.getReference().child(name).child(nomeFile);
                    storageRef.delete();

                } catch (Exception e) {
                    Snackbar.make(view, "Errore", Snackbar.LENGTH_LONG).show();
                }


                DocumentReference docRef = db.collection("Tesi").document(name);
                Map<String, Object> updates = new HashMap<>();
                updates.put("Estimated Time", edtTime.getText().toString());
                updates.put("Description", edtDescription.getText().toString());
                updates.put("Related Projects",edtRelatedProjects.getText().toString());
                updates.put("Correlator",spinnerCorrelator.getSelectedItem().toString());
                //TODO GESTIRE CORRELATORE, MATERIALI, VINCOLI, PROGETTI CORRELATI

                docRef.update(updates);

                Snackbar.make(view, "Thesis updated", Snackbar.LENGTH_LONG).show();

                Fragment thesisList = new ThesisListFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisList);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(name);

        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<String> fileNames = new ArrayList<>();
                for (StorageReference item : listResult.getItems()) {
                    fileNames.add(item.getName());
                    nomeFile = item.getName();
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
        View view = getLayoutInflater().inflate(R.layout.card, null);
        TextView nameView = view.findViewById(R.id.name);
        Button delete = view.findViewById(R.id.delete);
        nameView.setText(nomeFile);


        delete.setOnClickListener(v -> {
            layout_lista_file.removeView(view);

        });
        layout_lista_file.addView(view);
    }
}
