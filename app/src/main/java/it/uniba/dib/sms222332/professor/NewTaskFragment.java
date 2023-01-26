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

import it.uniba.dib.sms222332.R;

public class NewTaskFragment extends Fragment {


    TextView txtStudent, txtThesisName;
    Button create_task_button;
    EditText edtNameTask, edtDescription;
    String studentName, thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.newTaskToolbar));

        View view = inflater.inflate(R.layout.fragment_new_task, container, false);


        txtStudent = view.findViewById(R.id.txtStudent);
        txtThesisName = view.findViewById(R.id.txtThesisName);
        edtNameTask = view.findViewById(R.id.edtNameTask);
        edtDescription = view.findViewById(R.id.edtDescription);
        create_task_button = view.findViewById(R.id.create_task_button);

        if (getArguments() != null) {

            studentName = getArguments().getString("student");
            txtStudent.setText(studentName);

            thesisName = getArguments().getString("thesisName");
            txtThesisName.setText(thesisName);
        }

        create_task_button.setOnClickListener(view1 -> {

            createTaskButtonOnClick(view1);

        });

        return view;
    }

    private void createTaskButtonOnClick(View view1) {
        String nameTask = edtNameTask.getText().toString();
        String description = edtDescription.getText().toString();

        if (nameTask.isEmpty())
            edtNameTask.setError(getString(R.string.enter_name_task));
        else if (description.isEmpty())
            edtDescription.setError(getString(R.string.enter_task_description));
        else {
            Map<String, String> infoTask = new HashMap<>();
            infoTask.put("Name", nameTask);
            infoTask.put("Description", description);
            infoTask.put("Student", studentName);
            infoTask.put("Thesis", thesisName);
            infoTask.put("State", "Non Iniziato");

            db.collection("tasks").document(nameTask).set(infoTask);

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

            Snackbar.make(view1, R.string.task_added, Snackbar.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();

        }
    }


}

