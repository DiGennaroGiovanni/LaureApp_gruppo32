package it.uniba.dib.sms222332.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class ThesisDescriptionStudentFragment extends Fragment {

    TextView txtNameTitle, txtType, txtDepartment, txtTime, txtCorrelator,
            txtDescription, txtRelatedProjects, txtAverageMarks, txtRequiredExams, txtProfessor;
    String relatedProjects = "";
    String avgMarks = "";
    String requiredExam = "";
    String professorEmail = "";
    Button btnThesisRequest, btnContactProf;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description_student, container, false);

        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtType = view.findViewById(R.id.txtTypology);
        txtTime = view.findViewById(R.id.txtTime);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtRelatedProjects = view.findViewById(R.id.txtRelatedProjects);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);
        txtProfessor = view.findViewById(R.id.txtProfessor);
        btnThesisRequest = view.findViewById(R.id.btnThesisRequest);
        btnContactProf = view.findViewById(R.id.btnMessage);

        if (getArguments() != null) {
            setThesisData();

        }

        if (MainActivity.account.getRequest().equals("yes") || MainActivity.account.getRequest().equals("no")
                || MainActivity.account.getRequest().equals(txtNameTitle.getText().toString()))
            btnContactProf.setOnClickListener(view1 -> {

                Fragment thesisMessage = new NewMessageFragment();
                Bundle bundle = new Bundle();

                bundle.putString("thesis_name", txtNameTitle.getText().toString());
                bundle.putString("professor", professorEmail);

                thesisMessage.setArguments(bundle);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisMessage);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            });

        else
            btnContactProf.setOnClickListener(view12 -> Snackbar.make(requireView(), R.string.you_can_send_messages, Snackbar.LENGTH_LONG).show());


        db.collection("richieste").document(MainActivity.account.getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String thesisName = document.getString("Thesis");

                    //HO FATTO RICHIESTA PER QUESTA TESI MA NON MI È STATA ANCORA ACCETTATA
                    if (MainActivity.account.getRequest().equals("yes") && txtNameTitle.getText().toString().equals(thesisName)) {
                        btnThesisRequest.setText(R.string.cancel_request);
                        btnThesisRequest.setOnClickListener(view14 -> {

                            db.collection("richieste").document(MainActivity.account.getEmail()).delete().addOnSuccessListener(unused ->
                                    Snackbar.make(requireView(), R.string.request_canceled, Snackbar.LENGTH_LONG).show());

                            db.collection("studenti").document(MainActivity.account.getEmail()).update("Request", "no");

                            MainActivity.account.setRequest("no");
                            btnThesisRequest.setText(R.string.request_thesis);
                            setRequestButton();
                        });

                    } //HO FATTO UNA RICHIESTA MA NON PER QUESTA TESI
                    else if (!MainActivity.account.getRequest().equals("no")) {
                        btnThesisRequest.setOnClickListener(view13 -> Snackbar.make(requireView(), R.string.already_request, Snackbar.LENGTH_LONG).show());
                    }

                }//NON HO FATTO ALCUNA RICHIESTA
                else if (MainActivity.account.getRequest().equals("no"))
                    setRequestButton();

                    //HO GIÀ UNA TESI
                else {
                    btnThesisRequest.setOnClickListener(view13 -> Snackbar.make(requireView(), R.string.already_request, Snackbar.LENGTH_LONG).show());
                }
            } else
                Log.e("err", "Error");

        });

        return view;
    }

    private void setThesisData() {
        assert getArguments() != null;
        String correlator = getArguments().getString("correlator");
        String description = getArguments().getString("description");
        String estimated_time = getArguments().getString("estimated_time");
        String faculty = getArguments().getString("faculty");
        String name = getArguments().getString("name");
        String type = getArguments().getString("type");
        relatedProjects = getArguments().getString("related_projects");
        avgMarks = getArguments().getString("average_marks");
        requiredExam = getArguments().getString("required_exams");
        professorEmail = getArguments().getString("professor_email");

        txtNameTitle.setText(name);
        txtType.setText(type);
        txtDepartment.setText(faculty);

        String estTime = estimated_time + " " + getResources().getString(R.string.days);
        txtTime.setText(estTime);

        txtDescription.setText(description);
        txtProfessor.setText(professorEmail);


        if (correlator.isEmpty())
            txtCorrelator.setText(R.string.none);
        else
            txtCorrelator.setText(correlator);

        if (avgMarks.isEmpty()) {
            txtAverageMarks.setText(R.string.none);
        } else
            txtAverageMarks.setText(avgMarks);

        if (requiredExam.isEmpty()) {
            txtRequiredExams.setText(R.string.none);
        } else
            txtRequiredExams.setText(requiredExam);

        if (relatedProjects.isEmpty()) {
            txtRelatedProjects.setText(R.string.none);
        } else
            txtRelatedProjects.setText(relatedProjects);
    }

    private void setRequestButton() {

        btnThesisRequest.setOnClickListener(view12 -> {
            Fragment thesisRequest = new NewRequestFragment();
            Bundle bundle = new Bundle();

            bundle.putString("average_marks", txtAverageMarks.getText().toString());
            bundle.putString("required_exams", txtRequiredExams.getText().toString());
            bundle.putString("thesis_name", txtNameTitle.getText().toString());
            bundle.putString("professor", professorEmail);

            thesisRequest.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, thesisRequest);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });
    }
}
