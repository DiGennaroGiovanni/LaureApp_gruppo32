package it.uniba.dib.sms222332.professor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

public class NewTaskFragment extends Fragment {


    TextView txtStudent, txtThesisName;
    Button create_task_button;
    EditText edtTaskName, edtDescription, edtEstimatedTime;
    String studentName, thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.newTaskToolbar));

        View view = inflater.inflate(R.layout.fragment_new_task, container, false);


        txtStudent = view.findViewById(R.id.txtStudent);
        txtThesisName = view.findViewById(R.id.txtThesisName);
        edtTaskName = view.findViewById(R.id.edtNameTask);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtEstimatedTime = view.findViewById(R.id.edtEstimatedTime);

        create_task_button = view.findViewById(R.id.create_task_button);

        if (getArguments() != null) {

            studentName = getArguments().getString("student");
            txtStudent.setText(studentName);

            thesisName = getArguments().getString("thesisName");
            txtThesisName.setText(thesisName);
        }

        create_task_button.setOnClickListener(view1 -> createTaskButtonOnClick());

        return view;
    }

    private void createTaskButtonOnClick() {
        String taskName = edtTaskName.getText().toString();
        String description = edtDescription.getText().toString();
        String estTime = edtEstimatedTime.getText().toString();

        if (taskName.isEmpty())
            edtTaskName.setError(getResources().getString(R.string.enter_name_task));
        else if (description.isEmpty())
            edtDescription.setError(getResources().getString(R.string.enter_task_description));
        else if(estTime.isEmpty())
            edtEstimatedTime.setError("Enter an estimated time");
        else if(Integer.parseInt(estTime) < 1 || Integer.parseInt(estTime) > 30)
            edtEstimatedTime.setError("Enter a value between 1 and 30");
        else {
            Map<String, String> infoTask = new HashMap<>();
            infoTask.put("Name", taskName);
            infoTask.put("Description", description);
            infoTask.put("Student", studentName);
            infoTask.put("Thesis", thesisName);
            infoTask.put("Estimated Time", estTime);
            infoTask.put("State", "0");

            db.collection("tasks").document(taskName).set(infoTask);

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);

            Snackbar.make(requireView(), R.string.task_added, Snackbar.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();

        }
    }


}

