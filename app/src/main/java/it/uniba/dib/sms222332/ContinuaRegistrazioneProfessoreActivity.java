package it.uniba.dib.sms222332;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class ContinuaRegistrazioneProfessoreActivity extends AppCompatActivity {

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText edtNomeProf,edtCognomeProf, edtRuoloProf;
    String userEmailId;
    Button buttonConcludiProf;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continua_registrazione_professore);

        buttonConcludiProf = findViewById(R.id.buttonConcludiProf);
        edtCognomeProf = findViewById(R.id.edtCognomeProf);
        edtNomeProf = findViewById(R.id.edtNomeProf);
        edtRuoloProf = findViewById(R.id.edtRuoloProf);
        userEmailId = mAuth.getCurrentUser().getEmail(); //VIENE USARO NELLA RACCOLTA COME IDENTIFICATIVO, E' LO STESSO DEL SISTEMA DI Authentication
        progressDialog = new ProgressDialog(this);

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludiProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertDataProf(userEmailId); //MEMORIZZO I DATI DEL PROFESSORE
            }
        });
    }

    private void insertDataProf(String id_email) {

        String nome = edtNomeProf.getText().toString();
        String cognome = edtCognomeProf.getText().toString();
        String ruolo = edtRuoloProf.getText().toString();

        Map<String, String> infoProfessore = new HashMap<>();
        infoProfessore.put("Nome",nome);
        infoProfessore.put("Cognome",cognome);
        infoProfessore.put("Ruolo",ruolo);

        if(nome.isEmpty())
            edtNomeProf.setError("Inserisci il tuo nome!");
        else if(cognome.isEmpty())
            edtCognomeProf.setError("Inserisci il tuo cognome!");
        else if(ruolo.isEmpty())
            edtRuoloProf.setError("Inserisci il tuo ruolo!");
        else{
            //INSERIMENTO DATI NEL DB RIFERENDOSI AD UN DOCUMENTI IN PARTICOLARE
            db.collection("professori").document(id_email).set(infoProfessore).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.setMessage("Registrazione in corso...");
                            progressDialog.setTitle("Registrazione");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            //sendUserToUploadFile(); -> INSERIRE METODO CHE PORTA ALLA HOME DA LOGGATO
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"Registrazione conclusa!",Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }, 2000); // 3000 milliseconds is 3 seconds

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Registrazione non avvenuta! Riprova",Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}