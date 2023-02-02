package it.uniba.dib.sms222332.commonActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import it.uniba.dib.sms222332.R;

public class ThesisDescriptionUserFragment extends Fragment {

    TextView txtNameTitle, txtType, txtDepartment, txtTime, txtCorrelator,
            txtDescription, txtRelatedProjects, txtAverageMarks, txtRequiredExams, txtProfessor;
    String related_projects = "";
    String average_marks = "";
    String required_exam = "";
    String professore_email = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(R.string.thesis_info);

        View view = inflater.inflate(R.layout.fragment_thesis_description_user, container, false);

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

        if (getArguments() != null) {
            getDataFromPreviousFragment();
        }

        return view;
    }

    private void getDataFromPreviousFragment() {
        assert getArguments() != null;
        String correlator = getArguments().getString("correlator");
        String description = getArguments().getString("description");
        String estimated_time = getArguments().getString("estimated_time") + " " + R.string.days;
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


        if (correlator.isEmpty())
            txtCorrelator.setText(R.string.none);
        else
            txtCorrelator.setText(correlator);

        if (average_marks.isEmpty()) {
            txtAverageMarks.setText(R.string.none);
        } else
            txtAverageMarks.setText(average_marks);

        if (required_exam.isEmpty()) {
            txtRequiredExams.setText(R.string.none);
        } else
            txtRequiredExams.setText(required_exam);

        if (related_projects.isEmpty()) {
            txtRelatedProjects.setText(R.string.none);
        } else
            txtRelatedProjects.setText(related_projects);
    }
}