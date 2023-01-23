package it.uniba.dib.sms222332.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    EditText edtNameTask,edtDescription;
    String studentName,thesisName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.newTaskToolbar));

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

        String nameTask = edtNameTask.getText().toString();
        String description = edtDescription.getText().toString();

        if(nameTask.isEmpty())
            edtNameTask.setError("Inserisci il nome del task");
        else if(description.isEmpty())
            edtDescription.setError("Inserisci una descrizione per il task");
        else{
            Map<String,String> infoTask = new HashMap<>();
            infoTask.put("Name",nameTask);
            infoTask.put("Description",description);
            infoTask.put("Student",studentName);
            infoTask.put("Thesis",thesisName);
            infoTask.put("State","Non Iniziato");

            db.collection("tasks").document(nameTask).set(infoTask);
            Snackbar.make(view1, "Task added!", Snackbar.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();

        }

    });

    return view;
}


}

