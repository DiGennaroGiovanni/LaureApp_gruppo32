package it.uniba.dib.sms222332.student.Messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import it.uniba.dib.sms222332.R;

public class MessageInfoFragment extends Fragment {

    String object,professore_message,student_message,thesis_name;
    TextView txtNameTitle,txtObject,txtMessage,txtAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_message_info, container, false);

        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtObject = view.findViewById(R.id.txtObject);
        txtMessage = view.findViewById(R.id.txtMessage);
        txtAnswer = view.findViewById(R.id.txtAnswer);

        if (getArguments() != null){
            object = getArguments().getString("object");
            professore_message = getArguments().getString("professor_message");
            student_message = getArguments().getString("student_message");
            thesis_name = getArguments().getString("thesis_name");

            txtNameTitle.setText(thesis_name);
            txtObject.setText(object);
            txtMessage.setText(student_message);

            if(professore_message.equals(""))
                txtAnswer.setText("Not answered yet");
            else
                txtAnswer.setText(professore_message);
        }

        return view;
    }
}
