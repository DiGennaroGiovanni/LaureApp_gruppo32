package it.uniba.dib.sms222332.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class AvailableThesisFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_available_thesis, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layout_tesi_disponibili);


        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String faculty = document.getString("Faculty");
                            if (faculty.equals(MainActivity.account.getFaculty())) {
                                addCardThesis(document);
                            }
                        }
                    }
                });

        return view;
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_available_thesis, null);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtTypology);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtProfessor = view.findViewById(R.id.txtProfessor);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);
        String professorEmail = document.getString("Professor");

        DocumentReference docRef = db.collection("professori").document(professorEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                txtProfessor.setText(document.getString("Name")+ " " + document.getString("Surname"));
            }
        });



        txtName.setText(document.getString("Name"));
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));

        if (document.getString("Correlator").equals("")) {
            txtCorrelator.setText("None");
        }

        layout_lista_tesi.addView(view);

        view.setOnClickListener(view1 -> {

            bundle = new Bundle();
            Fragment studentThesis = new StudentThesisFragment();

            Map<String,Object> datiTesi =  document.getData();
            bundle.putString("correlator",(String) datiTesi.get("Correlator"));
            bundle.putString("description",(String) datiTesi.get("Description"));
            bundle.putString("estimated_time",(String) datiTesi.get("Estimated Time"));
            bundle.putString("faculty",(String) datiTesi.get("Faculty"));
            bundle.putString("name",(String) datiTesi.get("Name"));
            bundle.putString("type",(String) datiTesi.get("Type"));
            bundle.putString("related_projects",(String) datiTesi.get("Related Projects"));
            bundle.putString("average_marks",(String) datiTesi.get("Average"));
            bundle.putString("required_exams",(String) datiTesi.get("Required Exam"));
            bundle.putString("professor",txtProfessor.getText().toString());

            studentThesis.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, studentThesis);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }

}

