package it.uniba.dib.sms222332;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class ReceiptsListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button btnNewReceipt;
    private String student, thesisName;
    private TextView txtThesisName, txtStudent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts_list, container, false);

        txtThesisName = view.findViewById(R.id.txtThesisName);
        txtStudent = view.findViewById(R.id.txtStudentNameSurname);

        Bundle bundle = getArguments();
        if(bundle != null){
            thesisName = bundle.getString("thesis_name");
            student = bundle.getString("student");
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

        LinearLayout listView = view.findViewById(R.id.layoutReceiptsList);



        //TODO IMPLEMENTARE PRIMA PAGINA NUOVO RICEVIMENTO POI TORNARE QUA
//        db.collection("ricevimenti")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful()){
//                        for (QueryDocumentSnapshot document : task.getResult()){
//                            if
//                        }
//                    }
//
//                })

        //prova di lista ricevimenti

        for (int i = 0; i < 20; i++){
            View v = getLayoutInflater().inflate(R.layout.card_receipt, null);


            listView.addView(v);

        }




        return view;
    }
}