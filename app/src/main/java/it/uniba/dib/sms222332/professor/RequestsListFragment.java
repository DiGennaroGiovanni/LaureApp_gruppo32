package it.uniba.dib.sms222332.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class RequestsListFragment extends Fragment {

    LinearLayout layoutRequestsList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests_list, container, false);

        layoutRequestsList = view.findViewById(R.id.layoutRequestsList);

        db.collection("richieste")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            if(Objects.equals(document.getString("Professor"), MainActivity.account.getEmail()))
                                addRequestCard(document);
                        }
                    }
                });


        return view;
    }

    private void addRequestCard(QueryDocumentSnapshot document) {

        View v = getLayoutInflater().inflate(R.layout.card_requested_thesis, null);
        TextView thesisName = v.findViewById(R.id.txtThesisName);
        TextView studentEmail = v.findViewById(R.id.txtStudentEmail);

        thesisName.setText(document.getString("Thesis Name"));
        studentEmail.setText(document.getString("Student"));

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();


            }
        });

        layoutRequestsList.addView(v);

    }


}
