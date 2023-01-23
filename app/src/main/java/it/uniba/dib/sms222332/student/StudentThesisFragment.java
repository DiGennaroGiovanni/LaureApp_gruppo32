package it.uniba.dib.sms222332.student;

import android.os.Bundle;
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

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class StudentThesisFragment extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,
            txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams, txtProfessor;
    String related_projects = "" ;
    String average_marks = "" ;
    String required_exam = "";
    String professore_email = "";
    Button btnThesisRequest, btnContactProf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_student_thesis, container, false);

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
        btnContactProf = view.findViewById(R.id.btnContactProf);

        if (getArguments() != null) {
            String correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("estimated_time") + " days";
            String faculty = getArguments().getString("faculty");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String professor = getArguments().getString("professor");
            related_projects = getArguments().getString("related_projects");
            average_marks = getArguments().getString("average_marks");
            required_exam = getArguments().getString("required_exams");
            professore_email = getArguments().getString("professor_email");

            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            txtTime.setText(estimated_time);
            txtDescription.setText(description);
            txtProfessor.setText(professor);


           if(correlator.isEmpty())
                txtCorrelator.setText("None");
            else
                txtCorrelator.setText(correlator);

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

        btnContactProf.setOnClickListener(view1 -> {

        });

        if(MainActivity.account.getRequest().equals("yes")){
            btnThesisRequest.setOnClickListener(view13 -> {
                Snackbar.make(view13,"Already requested a thesis!",Snackbar.LENGTH_LONG).show();
            });
        }else{
            btnThesisRequest.setOnClickListener(view12 -> {
                Fragment thesisRequest = new ThesisRequestFragment();
                Bundle bundle = new Bundle();

                bundle.putString("average_marks",txtAverageMarks.getText().toString());
                bundle.putString("required_exams", txtRequiredExams.getText().toString());
                bundle.putString("thesis_name", txtNameTitle.getText().toString());
                bundle.putString("professor",professore_email);

                thesisRequest.setArguments(bundle);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisRequest);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            });
        }




        return view;
    }
}
