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

import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.student.NewMessageFragment;

public class MessagesListFragment extends Fragment {

    LinearLayout messageListLayout;
    TextView txtNomeProfessore,txtNomeTesi, txtProf;
    String thesis_name;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btnNewMessage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_messages_list, container, false);
        messageListLayout = view.findViewById(R.id.messageListLayout);

        txtNomeProfessore = view.findViewById(R.id.txtNomeProfessore);
        txtNomeTesi = view.findViewById(R.id.txtNomeTesi);
        btnNewMessage = view.findViewById(R.id.addMessageButton);
        txtProf =  view.findViewById(R.id.txtProf);



        if(getArguments() != null){

            String professor = getArguments().getString("professor");

            thesis_name = getArguments().getString("thesis_name");

            txtNomeProfessore.setText(professor);
            txtNomeTesi.setText(thesis_name);
        }



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        CollectionReference collectionReference = db.collection("messaggi");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(( Objects.equals(document.getString("Student"), MainActivity.account.getEmail()) &&
                            Objects.equals(document.getString("Thesis Name"), thesis_name) ) ||
                            (Objects.equals(document.getString("Professor"), MainActivity.account.getEmail()) &&
                                    Objects.equals(document.getString("Thesis Name"), thesis_name))){
                        addMessageCard(document);

                    }
                }
            }
        });

        if(MainActivity.account.getAccountType().equals("Professor")){
            txtNomeProfessore.setVisibility(View.GONE);
            txtProf.setVisibility(View.GONE);
        }

        if(MainActivity.account.getAccountType().equals("Professor"))
            btnNewMessage.setVisibility(View.GONE);

        else if((MainActivity.account.getRequest().equals("yes") || MainActivity.account.getRequest().equals("no") || MainActivity.account.getRequest().equals(thesis_name) )){
            btnNewMessage.setOnClickListener(view1 -> {

                Bundle bundle = new Bundle();
                bundle.putString("thesis_name",thesis_name);
                bundle.putString("professor",txtNomeProfessore.getText().toString());
                Fragment thesisMessage = new NewMessageFragment();

                thesisMessage.setArguments(bundle);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisMessage);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });
        } else {
            btnNewMessage.setOnClickListener(view12 -> Snackbar.make(requireView(),getResources().getString(R.string.you_can_send_messages),Snackbar.LENGTH_LONG).show());
        }
    }

    private void addMessageCard(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_message_info, null);

        TextView txtObject = view.findViewById(R.id.txtObject);
        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtState = view.findViewById(R.id.txtState);
        TextView txtStudente = view.findViewById(R.id.txtStudente);
        LinearLayout layoutTxtStudent = view.findViewById(R.id.layoutTxtStudent);

        String object = document.getString("Object");
        String professor_message = document.getString("Professor Message");
        String student_message = document.getString("Student Message");
        String date = document.getString("Date");
        String state = document.getString("State");



        txtObject.setText(object);
        txtDate.setText(date);
        txtState.setText(state);

        assert state != null;
        if(state.equals("Not answered"))
            txtState.setTextColor(Color.RED);
        else
            txtState.setTextColor(Color.parseColor("#178c17"));

        if(MainActivity.account.getAccountType().equals("Professor"))

            txtStudente.setText(document.getString("Student"));
        else
            layoutTxtStudent.setVisibility(View.GONE);

        messageListLayout.addView(view);

        view.setOnClickListener(view1 -> {

            Bundle bundle = new Bundle();
            bundle.putString("object",object);
            bundle.putString("professor_message",professor_message);
            bundle.putString("student_message",student_message);
            bundle.putString("thesis_name",thesis_name);
            bundle.putString("id_message", document.getId());


            Fragment infoMessage = new MessageDescriptionFragment();

            infoMessage.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, infoMessage);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });
    }
}
