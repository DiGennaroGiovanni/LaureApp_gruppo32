package it.uniba.dib.sms222332.student;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class NewMessageFragment extends Fragment {

    TextView txtNameTitle, txtProfessor;
    EditText edtObject, edtMessage;
    Button btnSendMessage;
    String thesisName, professor, student, object, description;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_new_message, container, false);

        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtProfessor = view.findViewById(R.id.txtProfessor);
        edtObject = view.findViewById(R.id.edtObject);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        student = MainActivity.account.getEmail();

        if (getArguments() != null) {
            thesisName = getArguments().getString("thesis_name");
            professor = getArguments().getString("professor");
            txtNameTitle.setText(thesisName);
            txtProfessor.setText(professor);
        }

        if (savedInstanceState != null) {
            edtObject.setText(savedInstanceState.getString("object"));
            edtMessage.setText(savedInstanceState.getString("msg"));
        }


        btnSendMessage.setOnClickListener(view1 -> sendMessage());

        return view;
    }

    private void sendMessage() {
        object = edtObject.getText().toString();
        description = edtMessage.getText().toString();

        if (object.isEmpty())
            edtObject.setError(getString(R.string.message_object));

        else if (description.isEmpty())
            edtMessage.setError(getString(R.string.message_description));
        else {

            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            Map<String, String> message = new HashMap<>();
            message.put("Thesis Name", thesisName);
            message.put("Professor", professor);
            message.put("Student", student);
            message.put("Object", object);
            message.put("Student Message", description);
            message.put("Professor Message", "");
            message.put("Date", date.format(formatter));
            message.put("State", "Not answered");

            db.collection("messaggi").document().set(message);

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
            Snackbar.make(requireView(), "Message sent!", Snackbar.LENGTH_LONG).show();

            getParentFragmentManager().popBackStack();

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("object", edtObject.getText().toString());
        outState.putString("msg", edtMessage.getText().toString());
    }
}
