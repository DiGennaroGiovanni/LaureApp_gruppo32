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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class ReceiptsListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button btnNewReceipt;
    String thesisName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts_list, container, false);




        btnNewReceipt = view.findViewById(R.id.btnNewReceipt);
        btnNewReceipt.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, new NewReceiptFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        View listView = view.findViewById(R.id.layoutReceiptsList);


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

        //prova di lista task

//        for (int i = 0; i < 20; i++){
//
//        }




        return view;
    }
}