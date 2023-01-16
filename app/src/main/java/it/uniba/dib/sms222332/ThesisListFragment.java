package it.uniba.dib.sms222332;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

public class ThesisListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layout_lista_tesi);

        db.collection("Tesi")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String professorEmail = document.getString("Professor");
                                if(professorEmail.equals(mUser.getEmail())){
                                    addCardThesis(document);
                                }
                            }
                        }
                    }
                });

                return view;
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_thesis, null);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtType);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtTime = view.findViewById(R.id.txtTime);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);

        txtName.setText(document.getString("Name"));
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtTime.setText(document.getString("Estimated Time"));
        txtCorrelator.setText(document.getString("Correlator"));

        layout_lista_tesi.addView(view);
    }
}
