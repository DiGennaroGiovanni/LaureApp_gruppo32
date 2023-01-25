package it.uniba.dib.sms222332.student.Messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.text.pdf.parser.Line;

import java.util.ArrayList;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;


public class StudentListMessageFragment extends Fragment {

    LinearLayout messageListLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> listaTesi = new ArrayList<String>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.messageContactTooolbar));

        View view = inflater.inflate(R.layout.fragment_student_list_message, container, false);

        messageListLayout = view.findViewById(R.id.layoutMessagesList);


        CollectionReference collectionReference = db.collection("messaggi");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Student").equals(MainActivity.account.getEmail()) ||
                            document.getString("Professor").equals(MainActivity.account.getEmail())){
                        if(!listaTesi.contains(document.getString("Thesis Name"))){
                            addMessageCard(document);
                            listaTesi.add(document.getString("Thesis Name"));
                        }
                    }
                }
            }
        });


        return view;
    }

    private void addMessageCard(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_student_message, null);

        TextView txtProfessorTitle = view.findViewById(R.id.txtProfessorTitle);
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtProfessor = view.findViewById(R.id.txtProfessor);

        String object = document.getString("Object");
        String professor = document.getString("Professor");
        String professor_message = document.getString("Professor Message");
        String student_message = document.getString("Student Message");
        String thesis_name = document.getString("Thesis Name");
        String date = document.getString("Date");
        String idMessage = document.getId();

        if(MainActivity.account.getAccountType().equals("Professor")){
            txtProfessorTitle.setVisibility(View.GONE);
            txtProfessor.setVisibility(View.GONE);
        }


        txtProfessor.setText(professor);
        txtName.setText(thesis_name);


        Bundle bundle = new Bundle();
        bundle.putString("object",object);
        bundle.putString("professor",professor);
        bundle.putString("professore_message",professor_message);
        bundle.putString("student_message",student_message);
        bundle.putString("thesis_name",thesis_name);
        bundle.putString("date",date);
        bundle.putString("idMessage",idMessage);


        view.setOnClickListener(view1 -> {

            Fragment info = new MessageStudentInfoFragment();

            info.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, info);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });
        messageListLayout.addView(view);


    }
}
