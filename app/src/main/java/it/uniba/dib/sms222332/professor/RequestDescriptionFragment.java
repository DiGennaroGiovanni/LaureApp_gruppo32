package it.uniba.dib.sms222332.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.uniba.dib.sms222332.R;

public class RequestDescriptionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_request_description, container, false);

        Bundle bundle = getArguments();
        if (bundle != null){
            String thesisName = bundle.getString("thesis_name");
            String student = bundle.getString("student");
            String avgMarks = bundle.getString("avg");
            String marksConstraint = bundle.getString("avg_constraint");
            String examsRequired = bundle.getString("exams");
            String examsConstraint = bundle.getString("exams_constraint");
            String message = bundle.getString("message");

            TextView txtStudent = view.findViewById(R.id.txtStudent);
        }



        return view;
    }
}
