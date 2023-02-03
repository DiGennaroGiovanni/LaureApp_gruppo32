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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import it.uniba.dib.sms222332.R;

public class RequestDescriptionFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btnAccept, btnDecline;
    TextView txtStudent, txtThesisName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_request_description, container, false);

        txtStudent = view.findViewById(R.id.txtStudent);
        txtThesisName = view.findViewById(R.id.txtThesisName);
        TextView txtAvgMarks = view.findViewById(R.id.txtAverageMarks);
        TextView txtAvgConstraint = view.findViewById(R.id.txtMarksRequiredOrNot);
        TextView txtExamsRequired = view.findViewById(R.id.txtRequiredExams);
        TextView txtExamsConstraint = view.findViewById(R.id.txtExamsRequiredOrNot);
        TextView txtMessage = view.findViewById(R.id.txtMessageFromStudent);
        LinearLayout layoutForAvgConstraint = view.findViewById(R.id.layoutForAvgConstraint);
        LinearLayout layoutForExamsConstraint = view.findViewById(R.id.layoutForExamsConstraint);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String thesisName = bundle.getString("thesis_name");
            String student = bundle.getString("student");
            String avgMarks = bundle.getString("avg");
            String avgConstraint = bundle.getString("avg_constraint_met");
            String examsRequired = bundle.getString("exams");
            String examsConstraint = bundle.getString("exams_constraint_met");
            String message = bundle.getString("message");

            txtStudent.setText(student);
            txtThesisName.setText(thesisName);
            txtMessage.setText(message);

            if (avgMarks.equals(""))
                layoutForAvgConstraint.setVisibility(View.GONE);
            else {
                txtAvgMarks.setText(avgMarks);
                if (avgConstraint.equals("1"))
                    txtAvgConstraint.setText(R.string.yes);
                else
                    txtAvgConstraint.setText(R.string.no);
            }

            if (examsRequired.equals(""))
                layoutForExamsConstraint.setVisibility(View.GONE);
            else {
                txtExamsRequired.setText(examsRequired);
                if (examsConstraint.equals("1"))
                    txtExamsConstraint.setText(R.string.yes);
                else
                    txtExamsConstraint.setText(R.string.no);
            }

        }

        btnAccept = view.findViewById(R.id.btnAccept);
        btnDecline = view.findViewById(R.id.btnDecline);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        btnAccept.setOnClickListener(view1 -> btnAcceptOnClick());
        btnDecline.setOnClickListener(view12 -> btnDeclineOnClick());
    }

    private void btnDeclineOnClick() {
        db.collection("richieste").document(txtStudent.getText().toString()).delete().addOnSuccessListener(unused ->
                Snackbar.make(requireView(), R.string.request_declined, Snackbar.LENGTH_LONG).show());

        db.collection("studenti").document(txtStudent.getText().toString()).update("Request", "no");

        getParentFragmentManager().popBackStack();
    }

    private void btnAcceptOnClick() {
        db.collection("Tesi").document(txtThesisName.getText().toString()).update("Student", txtStudent.getText().toString());

        db.collection("studenti").document(txtStudent.getText().toString()).update("Request", txtThesisName.getText().toString());

        db.collection("richieste").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot request : task.getResult()) {
                    if (Objects.equals(request.getString("Thesis"), txtThesisName.getText().toString())) {
                        request.getReference().delete();
                        if (!request.getId().equals(txtStudent.getText().toString()))
                            db.collection("studenti").document(request.getId()).update("Request", "no");
                    }
                }
            }
            Snackbar.make(requireView(), R.string.request_accepted, Snackbar.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        });
    }
}
