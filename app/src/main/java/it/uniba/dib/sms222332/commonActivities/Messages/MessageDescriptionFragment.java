package it.uniba.dib.sms222332.commonActivities.Messages;

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

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class MessageDescriptionFragment extends Fragment {

    String idMessage;
    TextView txtNameTitle,txtObject,txtMessage;
    EditText edtAnswer;
    Button btnAnswer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> updateAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_message_description, container, false);

        btnAnswer = view.findViewById(R.id.btnAnswer);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtObject = view.findViewById(R.id.txtObject);
        txtMessage = view.findViewById(R.id.txtMessage);
        edtAnswer = view.findViewById(R.id.edtAnswer);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null){
            String object = getArguments().getString("object");
            String professorMessage = getArguments().getString("professor_message");
            String studentMessage = getArguments().getString("student_message");
            String thesisName = getArguments().getString("thesis_name");
            idMessage = getArguments().getString("id_message");

            txtNameTitle.setText(thesisName);
            txtObject.setText(object);
            txtMessage.setText(studentMessage);

            if(professorMessage.equals(""))
                edtAnswer.setHint("Not answered yet");
            else
                edtAnswer.setText(professorMessage);
        }

        if(MainActivity.account.getAccountType().equals("Professor")){
            if(edtAnswer.getText().toString().equals("")){

                btnAnswer.setOnClickListener(view1 -> {

                    if(edtAnswer.getText().toString().isEmpty())
                        edtAnswer.setError(getString(R.string.add_an_answer));

                    else{
                        LocalDateTime date;
                        date = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                        updateAnswer = new HashMap<>();
                        updateAnswer.put("Professor Message", edtAnswer.getText().toString());
                        updateAnswer.put("State","Answered " +  date.format(formatter));

                        db.collection("messaggi").document(idMessage).update(updateAnswer);

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);

            Snackbar.make(view1, R.string.message_updated, Snackbar.LENGTH_LONG).show();

                        getParentFragmentManager().popBackStack();
                    }
                });
            } else {
                btnAnswer.setVisibility(View.GONE);
                edtAnswer.setEnabled(false);
            }
        }else {
            btnAnswer.setVisibility(View.GONE);
            edtAnswer.setEnabled(false);
        }
    }
}
