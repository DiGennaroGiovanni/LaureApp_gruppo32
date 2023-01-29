package it.uniba.dib.sms222332.commonActivities.Messages;

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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class ThesesMessagesListFragment extends Fragment {

    LinearLayout messageListLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> listaTesi = new ArrayList<>();
    TextView txtNoMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.messages));

        View view = inflater.inflate(R.layout.fragment_theses_messages_list, container, false);

        messageListLayout = view.findViewById(R.id.layoutMessagesList);

        txtNoMessage = view.findViewById(R.id.noMessage);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        CollectionReference collectionReference = db.collection("messaggi");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Student").equals(MainActivity.account.getEmail()) ||
                            document.getString("Professor").equals(MainActivity.account.getEmail())){
                        if(!listaTesi.contains(document.getString("Thesis Name"))){
                            addMessageCard(document);
                            listaTesi.add(document.getString("Thesis Name"));
                            txtNoMessage.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        listaTesi.clear();
    }

    private void addMessageCard(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_thesis_messages, null);

        TextView txtProfessorTitle = view.findViewById(R.id.txtProfessorTitle);
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtProfessor = view.findViewById(R.id.txtProfessor);


        String professor = document.getString("Professor");
        String thesis_name = document.getString("Thesis Name");

        if(MainActivity.account.getAccountType().equals("Professor")){
            txtProfessorTitle.setVisibility(View.GONE);
            txtProfessor.setVisibility(View.GONE);
        }

        txtProfessor.setText(professor);
        txtName.setText(thesis_name);

        view.setOnClickListener(view1 -> {

            Bundle bundle = new Bundle();
            bundle.putString("thesis_name",thesis_name);
            bundle.putString("professor",txtProfessor.getText().toString());

            Fragment info = new MessagesListFragment();

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
