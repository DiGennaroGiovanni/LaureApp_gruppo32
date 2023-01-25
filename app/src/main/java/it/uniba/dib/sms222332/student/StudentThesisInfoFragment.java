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

import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class StudentThesisInfoFragment extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,txtState,
            txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams,txtProfessor,txtNoRequest;
    String thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layoutThesisAccepted,layoutMaterials,layoutRequiredExams,layoutState,layoutAverageMarks,layoutNoThesis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisInfoTooolbar));

        View view = inflater.inflate(R.layout.fragment_student_thesis_info, container, false);

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


        CollectionReference richiesteReference = db.collection("richieste");
        richiesteReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Student").equals(MainActivity.account.getEmail())){
                        thesisName = document.getString("Thesis Name");
                        if(MainActivity.account.getRequest().equals(thesisName)){//TODO GESTIRE SE LO STUDENTE  HA UNA TESI

                            layoutAverageMarks.setVisibility(View.GONE);
                            layoutRequiredExams.setVisibility(View.GONE);
                            layoutMaterials.setVisibility(View.VISIBLE);


                        }else if(MainActivity.account.getRequest().equals("no")){ //LO STUDENTE NON HA FATTO ANCORA RICHIESTA
                            layoutThesisAccepted.setVisibility(View.GONE);
                            layoutNoThesis.setVisibility(View.VISIBLE);
                        }else{//TODO LO STUDENTE HA FATTO RICHIESTA MA NON E' STATA ANCORA ACCETTATA
                            layoutState.setVisibility(View.VISIBLE);

                        }
                    }

                }
            }
        });






        return view;
    }
}
