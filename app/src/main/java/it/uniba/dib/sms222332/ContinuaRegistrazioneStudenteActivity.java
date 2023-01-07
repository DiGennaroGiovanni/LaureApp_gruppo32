package it.uniba.dib.sms222332;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ContinuaRegistrazioneStudenteActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText edtNomeStudente,edtCognomeStudente, edtMatricolaStudente;
    TextView txtFacoltaStudente;
    String userEmailId;
    Spinner spinnerFacolta;
    Button buttonConcludi;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continua_registrazione_studente);

        edtCognomeStudente = findViewById(R.id.edtCognomeStudente);
        edtNomeStudente = findViewById(R.id.edtNomeStudente);
        edtMatricolaStudente = findViewById(R.id.edtMatricolaStudente);
        spinnerFacolta = findViewById(R.id.spinnerFacolta);
        buttonConcludi = findViewById(R.id.buttonConcludi);
        userEmailId = mAuth.getCurrentUser().getEmail();
        txtFacoltaStudente = findViewById(R.id.txtFacoltaStudente);
        progressDialog = new ProgressDialog(this);

        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.facolta_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        spinnerFacolta.setAdapter(adapter);

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertDataStudente(userEmailId);
            }
        });
    }

    private void insertDataStudente(String userEmailId) {
        String nome = edtNomeStudente.getText().toString();
        String cognome = edtCognomeStudente.getText().toString();
        String matricola = edtMatricolaStudente.getText().toString();
        String facolta = spinnerFacolta.getSelectedItem().toString();

        Map<String, String> infoStudente = new HashMap<>();
        infoStudente.put("Nome",nome);
        infoStudente.put("Cognome",cognome);
        infoStudente.put("Matricola",matricola);
        infoStudente.put("Facoltà",facolta);


        if(nome.isEmpty())
            edtNomeStudente.setError("Inserisci il tuo nome!");
        else if(cognome.isEmpty())
            edtCognomeStudente.setError("Inserisci il tuo cognome!");
        else if(matricola.isEmpty())
            edtMatricolaStudente.setError("Inserisci la tua matricola!");
        else if(facolta.isEmpty())
            txtFacoltaStudente.setError("Seleziona la tua facoltà!");
        else{
            //INSERIMENTO DATI NEL DB RIFERENDOSI AD UN DOCUMENTI IN PARTICOLARE
            db.collection("studenti").document(userEmailId).set(infoStudente).addOnSuccessListener(new OnSuccessListener<Void>() {
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