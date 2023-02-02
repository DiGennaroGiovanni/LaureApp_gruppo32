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
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class NewRequestFragment extends Fragment {

    String average_marks = "";
    String required_exams = "";
    String thesis_name = "";
    String professore_email = "";
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
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.thesisRequestTooolbar));

        View view = inflater.inflate(R.layout.fragment_new_request, container, false);

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

        if (average_marks.equals(getResources().getString(R.string.none))) {
            averageMarksLayout.setVisibility(GONE);
            txtAverageConstraint.setVisibility(GONE);
            averageRadioGroup.setVisibility(GONE);
        } else {
            txtAverageMarks.setText(average_marks);
        }

        if (required_exams.equals(getResources().getString(R.string.none))) {
            requiredExamsLayout.setVisibility(GONE);
            txtExamsConstraint.setVisibility(GONE);
            examsRadioGroup.setVisibility(GONE);
        } else {
            txtRequiredExams.setText(required_exams);
        }


        btnThesisRequest.setOnClickListener(view1 -> requestThesis());

        return view;
    }

    private void requestThesis() {

        if (!required_exams.equals(getResources().getString(R.string.none))  && !rdbExamsYes.isChecked() && !rdbExamsNo.isChecked()) {
            txtExamsConstraint.setError(getString(R.string.have_to_choice));
            Snackbar.make(requireView(), R.string.have_to_choice, Snackbar.LENGTH_LONG).show();

        } else if (!average_marks.equals(getResources().getString(R.string.none)) && !rdbAverageYes.isChecked() && !rdbAverageNo.isChecked()) {
            txtAverageConstraint.setError(getString(R.string.have_to_choice));
            Snackbar.make(requireView(), R.string.have_to_choice, Snackbar.LENGTH_LONG).show();
        } else {
            String average = "";
            String exams = "";

            if (rdbAverageYes.isChecked())
                average = "1";
            else if (rdbAverageNo.isChecked())
                average = "0";

            if (rdbExamsYes.isChecked())
                exams = "1";
            else if (rdbExamsNo.isChecked())
                exams = "0";

            Map<String, String> request = new HashMap<>();
            request.put("Average", txtAverageMarks.getText().toString());
            request.put("Average Constraint Met", average);
            request.put("Exams", txtRequiredExams.getText().toString());
            request.put("Exams Constraint Met", exams);
            request.put("Message", edtNote.getText().toString());
            request.put("Professor", professore_email);
            request.put("Student", MainActivity.account.getEmail());
            request.put("Thesis", thesis_name);

            String accountEmail = MainActivity.account.getEmail();

            db.collection("richieste").document(accountEmail).set(request);

            Map<String, Object> update = new HashMap<>();
            update.put("Request", "yes");

            db.collection("studenti").document(accountEmail).update(update);

            MainActivity.account.setRequest("yes");

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);

            Snackbar.make(requireView(), R.string.request_made, Snackbar.LENGTH_LONG).show();

            getParentFragmentManager().popBackStack();
        }
    }
}
