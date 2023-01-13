package it.uniba.dib.sms222332;

import static android.content.ContentValues.TAG;
import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;


public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txtNome, txtMatricola, txtUniversita;
    Button btnEliminaProfilo;
    String nome_utente, matricola_utente, universita_utente, cognome_utente;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
             nome_utente = bundle.getString("nome_utente");
             matricola_utente = bundle.getString("matricola_utente");
             universita_utente = bundle.getString("universita_utente");
             cognome_utente = bundle.getString("cognome_utente");
        }

        txtMatricola = view.findViewById(R.id.text_uni_mat);
        txtNome = view.findViewById(R.id.text_name);
        txtUniversita = view.findViewById(R.id.text_uni);
        btnEliminaProfilo = view.findViewById(R.id.btnEliminaProfilo);

        txtMatricola.setText(matricola_utente);
        txtNome.setText(nome_utente+" "+cognome_utente);
        txtUniversita.setText(universita_utente);

        String userEmailId = mAuth.getCurrentUser().getEmail();
        DocumentReference documentReference = db.collection("studenti").document(userEmailId);

        //AGGIUNGERE ACTIVITY INTERMEDIA INVECE DI ELIMINARE DIRETTAMENTE I DATI
        btnEliminaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                documentReference.delete();
                mUser.delete();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        return view;
    }
}
