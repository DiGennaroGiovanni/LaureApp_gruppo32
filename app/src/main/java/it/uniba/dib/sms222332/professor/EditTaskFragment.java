package it.uniba.dib.sms222332.professor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class EditTaskFragment extends Fragment {

    TextView txtTaskTitle, txtThesis, txtStudent;
    EditText edtDescription;
    RadioButton rdbDaCompletare, rdbCompletato, rdbNonIniziato;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btnSave;
    String state;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.editTaskToolbar));

        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        txtTaskTitle = view.findViewById(R.id.txtTaskTitle);
        txtThesis = view.findViewById(R.id.txtThesis);
        txtStudent = view.findViewById(R.id.txtStudent);
        edtDescription = view.findViewById(R.id.edtDescription);
        rdbDaCompletare = view.findViewById(R.id.rdbDaCompletare);
        rdbCompletato = view.findViewById(R.id.rdbCompletato);
        rdbNonIniziato = view.findViewById(R.id.rdbNonIniziato);
        btnSave = view.findViewById(R.id.btnSave);

        if (getArguments() != null) {

            txtTaskTitle.setText(getArguments().getString("name"));
            txtThesis.setText(getArguments().getString("thesis name"));
            txtStudent.setText(getArguments().getString("student"));
            edtDescription.setText(getArguments().getString("description"));
            state = getArguments().getString("state");
        }

        if (!MainActivity.account.getAccountType().equals("Professor"))
            edtDescription.setEnabled(false);

        switch (state) {
            case "Completato":
                rdbCompletato.setChecked(true);
                break;
            case "Da Completare":
                rdbDaCompletare.setChecked(true);
                break;
            case "Non Iniziato":
                rdbNonIniziato.setChecked(true);
                break;
        }

        btnSave.setOnClickListener(this::onClick);

        return view;
    }

    private void onClick(View view1) {


        if (edtDescription.getText().toString().isEmpty())
            edtDescription.setError(getString(R.string.task_description_error));

        else {

            editTask(view1);
        }
    }

    private void editTask(View view1) {
        DocumentReference docRef = db.collection("tasks").document(txtTaskTitle.getText().toString());
        Map<String, Object> updates = new HashMap<>();
        updates.put("Description", edtDescription.getText().toString());

        if (rdbNonIniziato.isChecked())
            updates.put("State", rdbNonIniziato.getText().toString());
        else if (rdbDaCompletare.isChecked())
            updates.put("State", rdbDaCompletare.getText().toString());
        else if (rdbCompletato.isChecked())
            updates.put("State", rdbCompletato.getText().toString());


        docRef.update(updates);

        // chiusura della tastiera quando viene effettuato un cambio di fragment
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

        Snackbar.make(view1, R.string.task_updated, Snackbar.LENGTH_LONG).show();

        getParentFragmentManager().popBackStack();
    }
}
