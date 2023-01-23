package it.uniba.dib.sms222332.professor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;


public class ReceiptsListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button btnNewReceipt;
    private TextView txtThesisName, txtStudent;
    LinearLayout listView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts_list, container, false);

        txtThesisName = view.findViewById(R.id.txtThesisName);
        txtStudent = view.findViewById(R.id.txtStudentNameSurname);

        Bundle bundle = getArguments();
        if(bundle != null){
            String thesisName = bundle.getString("thesis_name");
            String student = bundle.getString("student");
            txtThesisName.setText(thesisName);
            txtStudent.setText(student);
        }



        btnNewReceipt = view.findViewById(R.id.btnNewReceipt);
        btnNewReceipt.setOnClickListener(view1 -> {

            Bundle newReceiptBundle = new Bundle();
            newReceiptBundle.putString("thesis_name", txtThesisName.getText().toString());
            newReceiptBundle.putString("student", txtStudent.getText().toString());

            Fragment newReceiptFragment = new NewReceiptFragment();
            newReceiptFragment.setArguments(newReceiptBundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


            fragmentTransaction.replace(R.id.fragment_container, newReceiptFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

         listView = view.findViewById(R.id.layoutReceiptsList);


        db.collection("ricevimenti")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            if(document.getString("Thesis").equals(txtThesisName.getText().toString()))
                                addReceiptCard(document);
                        }
                    }

                });


        return view;
    }

    private void addReceiptCard(QueryDocumentSnapshot document) {
        View v = getLayoutInflater().inflate(R.layout.card_receipt, null);

        TextView date = v.findViewById(R.id.txtReceiptDateCard);
        TextView startTime = v.findViewById(R.id.txtReceiptStartTimeCard);
        TextView endTime = v.findViewById(R.id.txtReceiptEndTimeCard);
        TextView description = v.findViewById(R.id.txtReceiptDescriptionCard);
        TextView tasks = v.findViewById(R.id.addressedTasksCard);

        date.setText(document.getString("Date"));
        startTime.setText((document.getString("Start Time")));
        endTime.setText(document.getString("End Time"));
        description.setText(document.getString("Description"));


        Map<String, Object> map = document.getData();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("Tasks")) {
                String value = entry.getValue().toString();
                tasks.setText(value.substring(1, value.length()-1));
            }
        }

        listView.addView(v);

    }
}