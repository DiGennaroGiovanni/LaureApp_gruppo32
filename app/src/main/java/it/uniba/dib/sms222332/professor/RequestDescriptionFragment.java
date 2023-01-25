package it.uniba.dib.sms222332.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

        TextView txtStudent = view.findViewById(R.id.txtStudent);
        TextView txtThesisName = view.findViewById(R.id.txtThesisName);
        TextView txtavgMarks = view.findViewById(R.id.txtAverageMarks);
        TextView txtAvgConstraint = view.findViewById(R.id.txtMarksRequiredOrNot);
        TextView txtExamsRequired = view.findViewById(R.id.txtRequiredExams);
        TextView txtExamsConstraint = view.findViewById(R.id.txtExamsRequiredOrNot);
        TextView txtMessage = view.findViewById(R.id.txtMessageFromStudent);
        LinearLayout layoutForAvgConstraint = view.findViewById(R.id.layoutForAvgConstraint);
        LinearLayout layoutForExamsConstraint = view.findViewById(R.id.layoutForExamsConstraint);

        Bundle bundle = getArguments();
        if (bundle != null){
            String thesisName = bundle.getString("thesis_name");
            String student = bundle.getString("student");
            String avgMarks = bundle.getString("avg");
            String avgConstraint = bundle.getString("avg_constraint");
            String examsRequired = bundle.getString("exams");
            String examsConstraint = bundle.getString("exams_constraint");
            String message = bundle.getString("message");

            txtStudent.setText(student);
            txtThesisName.setText(thesisName);
            txtMessage.setText(message);

           if(avgMarks.equals(""))
               layoutForAvgConstraint.setVisibility(View.GONE);
           else {
               txtavgMarks.setText(avgMarks);
               txtAvgConstraint.setText(avgConstraint);
           }

           if(examsRequired.equals(""))
               layoutForExamsConstraint.setVisibility(View.GONE);
           else {
               txtExamsRequired.setText(examsRequired);
               txtExamsConstraint.setText(examsConstraint);
           }

        }

        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);


        btnAccept.setOnClickListener(view1 -> {
         //TODO LIST: 1) IMPOSTARE NELLA TESI IL CAMPO STUDENTE. 2) IMPOSTARE IN STUDENTE IL CAMPO REQUEST COL NOME TESI. 3) ELIMINARE TUTTE LE RICHIESTE DI QUESTA TESI

        });

        btnDecline.setOnClickListener(view12 -> {
        //TODO: 1) CANCELLATA LA RICHIESTA SPECIFICA. 2) IMPOSTATO VALORE REQUEST DI STUDENTE A "no"

        });

        return view;
    }
}
