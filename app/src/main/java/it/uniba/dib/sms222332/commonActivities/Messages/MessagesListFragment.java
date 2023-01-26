package it.uniba.dib.sms222332.commonActivities.Messages;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.student.NewMessageFragment;

public class MessagesListFragment extends Fragment {

    LinearLayout messageListLayout;
    TextView txtNomeProfessore, txtNomeTesi, txtProf;
    String object, professor, professore_message, student_message, thesis_name, idMessage;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button addMessageButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_messages_list, container, false);
        messageListLayout = view.findViewById(R.id.messageListLayout);

        txtNomeProfessore = view.findViewById(R.id.txtNomeProfessore);
        txtNomeTesi = view.findViewById(R.id.txtNomeTesi);
        addMessageButton = view.findViewById(R.id.addMessageButton);
        txtProf = view.findViewById(R.id.txtProf);


        if (getArguments() != null) {

            object = getArguments().getString("object");
            professor = getArguments().getString("professor");
            professore_message = getArguments().getString("professore_message");
            student_message = getArguments().getString("student_message");
            thesis_name = getArguments().getString("thesis_name");
            idMessage = getArguments().getString("idMessage");

            txtNomeProfessore.setText(professor);
            txtNomeTesi.setText(thesis_name);
        }


        CollectionReference collectionReference = db.collection("messaggi");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if ((document.getString("Student").equals(MainActivity.account.getEmail()) &&
                            document.getString("Thesis Name").equals(thesis_name)) ||
                            document.getString("Professor").equals(MainActivity.account.getEmail()) &&
                                    document.getString("Thesis Name").equals(thesis_name)) {
                        addMessageCard(document);

                    }
                }
            }
        });

        if (MainActivity.account.getAccountType().equals("Professor")) {
            txtNomeProfessore.setVisibility(View.GONE);

            txtProf.setVisibility(View.GONE);
        }

        if (MainActivity.account.getAccountType().equals("Student") && (MainActivity.account.getRequest().equals("yes") || MainActivity.account.getRequest().equals("no")
                || MainActivity.account.getRequest().equals(thesis_name))) {
            addMessageButton.setOnClickListener(view1 -> {

                addMessageButtonOnClick();
            });
        } else if (MainActivity.account.getAccountType().equals("Professor"))
            addMessageButton.setVisibility(View.GONE);
        else {
            addMessageButton.setOnClickListener(view12 -> {
                Snackbar.make(view12, getString(R.string.you_can_send_messages), Snackbar.LENGTH_LONG).show();
            });
        }
        return view;
    }

    private void addMessageButtonOnClick() {
        Bundle bundle = new Bundle();
        bundle.putString("thesis_name", thesis_name);
        bundle.putString("professor", professor);

        Fragment thesisMessage = new NewMessageFragment();

        thesisMessage.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, thesisMessage);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private void addMessageCard(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_message_info, null);

        TextView txtObject = view.findViewById(R.id.txtObject);
        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtState = view.findViewById(R.id.txtState);
        TextView txtStudente = view.findViewById(R.id.txtStudente);
        TextView txtStudentTitle = view.findViewById(R.id.txtStudentTitle);
        LinearLayout layoutTxtStudent = view.findViewById(R.id.layoutTxtStudent);

        String object = document.getString("Object");
        String professor_message = document.getString("Professor Message");
        String student_message = document.getString("Student Message");
        String date = document.getString("Date");
        String state = document.getString("State");


        txtObject.setText(object);
        txtDate.setText(date);
        txtState.setText(state);

        if (state.equals("Not answered"))
            txtState.setTextColor(Color.RED);
        else
            txtState.setTextColor(Color.GREEN);

        if (MainActivity.account.getAccountType().equals("Professor")) {
            txtStudente.setVisibility(View.VISIBLE);
            txtStudentTitle.setVisibility(View.VISIBLE);

            txtStudente.setText(document.getString("Student"));
        } else
            layoutTxtStudent.setVisibility(View.GONE);

        messageListLayout.addView(view);

        view.setOnClickListener(view1 -> {

            viewOnClick(object, professor_message, student_message);

        });
    }

    private void viewOnClick(String object, String professor_message, String student_message) {
        Bundle bundle = new Bundle();
        bundle.putString("object", object);
        bundle.putString("professor_message", professor_message);
        bundle.putString("student_message", student_message);
        bundle.putString("thesis_name", thesis_name);
        bundle.putString("idMessage", idMessage);

        Fragment infoMessage = new MessageDescriptionFragment();

        infoMessage.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, infoMessage);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
