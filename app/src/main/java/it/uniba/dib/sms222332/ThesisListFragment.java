package it.uniba.dib.sms222332;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Map;

public class ThesisListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;



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
        TextView txtStudentThesis = view.findViewById(R.id.txtStudentThesis);

        txtName.setText(document.getString("Name"));
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtTime.setText(document.getString("Estimated Time"));
        txtCorrelator.setText(document.getString("Correlator"));

        if(document.getString("Student").equals(""))
        {
            txtStudentThesis.setText("No student yet");
        }else{
            txtStudentThesis.setText(document.getString("Student"));
        }

        if(document.getString("Correlator").equals(""))
        {
            txtCorrelator.setText("There is no correlator");
        }

        layout_lista_tesi.addView(view);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bundle = new Bundle();
                Fragment thesisDescription = new ThesisDescription();



                Map<String,Object> datiTesi =  document.getData();
                bundle.putString("constraints",(String) datiTesi.get("Constraints"));
                bundle.putString("correlator",(String) datiTesi.get("Correlator"));
                bundle.putString("description",(String) datiTesi.get("Description"));
                bundle.putString("estimated_time",(String) datiTesi.get("Estimated Time"));
                bundle.putString("faculty",(String) datiTesi.get("Faculty"));
                bundle.putString("name",(String) datiTesi.get("Name"));
                bundle.putString("type",(String) datiTesi.get("Type"));
                bundle.putString("related_projects",(String) datiTesi.get("Related Projects"));


                thesisDescription.setArguments(bundle);


                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisDescription);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }
}
