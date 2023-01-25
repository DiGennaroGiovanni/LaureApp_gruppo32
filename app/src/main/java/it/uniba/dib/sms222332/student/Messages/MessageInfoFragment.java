package it.uniba.dib.sms222332.student.Messages;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class MessageInfoFragment extends Fragment {

    String object,professore_message,student_message,thesis_name,idMessage;
    TextView txtNameTitle,txtObject,txtMessage;
    EditText edtAnswer;
    Button btnAnswer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_message_info, container, false);

        btnAnswer = view.findViewById(R.id.btnAnswer);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtObject = view.findViewById(R.id.txtObject);
        txtMessage = view.findViewById(R.id.txtMessage);
        edtAnswer = view.findViewById(R.id.edtAnswer);

        if (getArguments() != null){
            object = getArguments().getString("object");
            professore_message = getArguments().getString("professor_message");
            student_message = getArguments().getString("student_message");
            thesis_name = getArguments().getString("thesis_name");
            idMessage = getArguments().getString("idMessage");

            txtNameTitle.setText(thesis_name);
            txtObject.setText(object);
            txtMessage.setText(student_message);

            if(professore_message.equals(""))
                edtAnswer.setHint("Not answered yet");
            else
                edtAnswer.setText(professore_message);
        }

        if(MainActivity.account.getAccountType().equals("Professor")){
            if(professore_message.equals("")){
                btnAnswer.setOnClickListener(view1 -> {

                    if(edtAnswer.getText().toString().isEmpty()){
                        edtAnswer.setError("Inserisci una risposta!");
                    }else{

                        LocalDateTime date = LocalDateTime.now();
                        date = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                        DocumentReference docRef = db.collection("messaggi").document(idMessage);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("Professor Message", edtAnswer.getText().toString());
                        updates.put("State","Answered " +  date.format(formatter));

                        docRef.update(updates);

                        // chiusura della tastiera quando viene effettuato un cambio di fragment
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

                        Snackbar.make(view1, "Message updated", Snackbar.LENGTH_LONG).show();

                        getParentFragmentManager().popBackStack();
                    }
                });
            }else{
            }
        }else {
            btnAnswer.setVisibility(View.GONE);
            edtAnswer.setEnabled(false);
        }
        return view;
    }
}
