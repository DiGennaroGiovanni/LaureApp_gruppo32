package it.uniba.dib.sms222332.student;

import static android.view.View.GONE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class NewRequestFragment extends Fragment {

    String average_marks = "" ;
    String required_exams = "";
    String thesis_name = "";
    String professore_email ="";
    LinearLayout averageMarksLayout, requiredExamsLayout;
    TextView txtExamsConstraint, txtAverageConstraint, txtAverageMarks, txtRequiredExams;
    RadioGroup examsRadioGroup, averageRadioGroup;
    Button btnThesisRequest;
    RadioButton rdbAverageYes, rdbAverageNo, rdbExamsYes, rdbExamsNo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText edtNote;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisRequestTooolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_request, container, false);

        averageMarksLayout = view.findViewById(R.id.averageMarksLayout);
        requiredExamsLayout = view.findViewById(R.id.requiredExamsLayout);
        txtExamsConstraint = view.findViewById(R.id.txtExamsConstraint);
        txtAverageConstraint = view.findViewById(R.id.txtAverageConstraint);
        examsRadioGroup = view.findViewById(R.id.examsRadioGroup);
        averageRadioGroup = view.findViewById(R.id.averageRadioGroup);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        btnThesisRequest = view.findViewById(R.id.btnThesisRequest);
        rdbAverageYes = view.findViewById(R.id.rdbAverageYes);
        rdbAverageNo = view.findViewById(R.id.rdbAverageNo);
        rdbExamsYes = view.findViewById(R.id.rdbExamsYes);
        rdbExamsNo = view.findViewById(R.id.rdbExamsNo);
        edtNote = view.findViewById(R.id.edtNote);

        if (getArguments() != null) {
            average_marks = getArguments().getString("average_marks");
            required_exams = getArguments().getString("required_exams");
            thesis_name = getArguments().getString("thesis_name");
            professore_email = getArguments().getString("professor");
        }

        if(average_marks.equals("None")) {
            averageMarksLayout.setVisibility(GONE);
            txtAverageConstraint.setVisibility(GONE);
            averageRadioGroup.setVisibility(GONE);
        } else {
            txtAverageMarks.setText(average_marks);
        }

        if(required_exams.equals("None")) {
            requiredExamsLayout.setVisibility(GONE);
            txtExamsConstraint.setVisibility(GONE);
            examsRadioGroup.setVisibility(GONE);
        } else {
            txtRequiredExams.setText(required_exams);
        }


        btnThesisRequest.setOnClickListener(view1 -> {

            String average = "";
            String exams = "";
            String requestName = "";

            if(rdbAverageYes.isChecked())
                average = rdbAverageYes.getText().toString();
            else if(rdbAverageNo.isChecked())
                average = rdbAverageNo.getText().toString();

            if(rdbExamsYes.isChecked())
                exams = rdbExamsYes.getText().toString();
            else if(rdbExamsNo.isChecked())
                exams = rdbExamsNo.getText().toString();



            if(!required_exams.equals("None") && !rdbExamsYes.isChecked() && !rdbExamsNo.isChecked()){
                txtExamsConstraint.setError("You have to choice!");
                Snackbar.make(view1, "You have to choice!", Snackbar.LENGTH_LONG).show();

            }else if(!average_marks.equals("None") && !rdbAverageYes.isChecked() && !rdbAverageNo.isChecked()){
                txtAverageConstraint.setError("You have to choice!");
                Snackbar.make(view1, "You have to choice!", Snackbar.LENGTH_LONG).show();
            }else{

                Map<String, String> request = new HashMap<>();
                request.put("Average", txtAverageMarks.toString());
                request.put("Average Constraint Met", average);
                request.put("Exams", txtRequiredExams.toString());
                request.put("Exams Constraint Met", exams);
                request.put("Message", edtNote.getText().toString());
                request.put("Professor",professore_email);
                request.put("Student",MainActivity.account.getEmail());
                request.put("Thesis",thesis_name);

                requestName =MainActivity.account.getEmail();

                db.collection("richieste").document(requestName).set(request);

                Map <String, Object> update = new HashMap<>();
                update.put("Request","yes");

                db.collection("studenti").document(MainActivity.account.getEmail()).update(update);

                MainActivity.account.setRequest("yes");

                // chiusura della tastiera quando viene effettuato un cambio di fragment
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

                Snackbar.make(view1, "Request made!", Snackbar.LENGTH_LONG).show();

                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
