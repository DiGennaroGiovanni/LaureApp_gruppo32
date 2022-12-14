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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrazioneStudActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText edtNomeStudente,edtCognomeStudente, edtMatricolaStudente, edtEmailRegistrati, edtPasswordRegistrati;
    TextView txtFacoltaStudente;
    String userEmailId;
    Spinner spinnerFacolta;
    Button buttonConcludi;
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continua_registrazione_studente);

        edtEmailRegistrati = findViewById(R.id.edtEmailRegistrati);
        edtPasswordRegistrati = findViewById(R.id.edtPasswordRegistrati);
        edtCognomeStudente = findViewById(R.id.edtCognomeStudente);
        edtNomeStudente = findViewById(R.id.edtNomeStudente);
        edtMatricolaStudente = findViewById(R.id.edtMatricolaStudente);
        spinnerFacolta = findViewById(R.id.spinnerFacolta);
        buttonConcludi = findViewById(R.id.buttonConcludi);
        txtFacoltaStudente = findViewById(R.id.txtFacoltaStudente);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


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
                performAuthStud();

            }
        });
    }

    private void insertDataStudente() {
        String nome = edtNomeStudente.getText().toString().toLowerCase();
        char firstChar = Character.toUpperCase(nome.charAt(0));
        nome = firstChar+nome.substring(1);

        String cognome = edtCognomeStudente.getText().toString().toLowerCase();
        firstChar = Character.toUpperCase(cognome.charAt(0));
        cognome = firstChar+cognome.substring(1);

        String matricola = edtMatricolaStudente.getText().toString();


        String facolta = spinnerFacolta.getSelectedItem().toString();
        String email = edtEmailRegistrati.getText().toString().toLowerCase();

        Map<String, String> infoStudente = new HashMap<>();
        infoStudente.put("Nome",nome);
        infoStudente.put("Cognome",cognome);
        infoStudente.put("Matricola",matricola);
        infoStudente.put("Facolt??",facolta);
        infoStudente.put("Tipologia","S");


        if(nome.isEmpty())
            edtNomeStudente.setError("Inserisci il tuo nome!");
        else if(cognome.isEmpty())
            edtCognomeStudente.setError("Inserisci il tuo cognome!");
        else if(matricola.isEmpty())
            edtMatricolaStudente.setError("Inserisci la tua matricola correttamente!");
        else if(facolta.isEmpty())
            txtFacoltaStudente.setError("Seleziona la tua facolt??!");
        else{
            //INSERIMENTO DATI NEL DB RIFERENDOSI AD UN DOCUMENTI IN PARTICOLARE
            db.collection("studenti").document(email).set(infoStudente).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void performAuthStud() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        if(!email.matches(emailPattern))
        {
            edtEmailRegistrati.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordRegistrati.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{


            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        insertDataStudente();
                        Intent intent = new Intent(RegistrazioneStudActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(RegistrazioneStudActivity.this, "Registrazione completata!", Toast.LENGTH_SHORT).show();


                    }else{
                        Toast.makeText(RegistrazioneStudActivity.this, "Email gi?? in uso, ripova!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}