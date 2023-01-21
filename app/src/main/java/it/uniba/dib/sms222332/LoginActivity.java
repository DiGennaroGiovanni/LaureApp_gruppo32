package it.uniba.dib.sms222332;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailLogin,edtPasswordLogin;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getIntent().getBooleanExtra("logout", false)){
            View view = findViewById(android.R.id.content);
            Snackbar.make(view, "Logout effettuato!", Snackbar.LENGTH_SHORT).show();
        }

        // Dichiaro i pulsanti presenti nella schermata
        Button registerBtn = findViewById(R.id.btnRegistrati);
        Button loginBtn = findViewById(R.id.btnAccedi);
        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);

        mAuth = FirebaseAuth.getInstance();

        // Passo alla schermata di registrazione
        registerBtn.setOnClickListener(view -> {
            Intent registerIntent = new Intent(LoginActivity.this, RegistratiActivity.class);
            startActivity(registerIntent);
        });

        // Passo alla schermata home
        loginBtn.setOnClickListener(view -> performLogin());
    }

    private void performLogin( ) {
        String email = edtEmailLogin.getText().toString();
        String password = edtPasswordLogin.getText().toString();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(!email.matches(emailPattern))
        {
            edtEmailLogin.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordLogin.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){

                    //VERIFICARE LA TIPOLOGIA DI ACCOUNT -> STUDENTE

                    // Crea un riferimento alla raccolta studenti
                    CollectionReference collectionReferenceStud = db.collection("studenti");
                    // Crea un task per ottenere il documento con l'ID specifico
                    Task<DocumentSnapshot> checkStud = collectionReferenceStud.document(email).get();
                    // Aspetta che il task sia completato
                    checkStud.addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            DocumentSnapshot document = task12.getResult();
                            if (document.exists()) {

                                Map<String,Object> datiStudente =  document.getData();


                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                intent.putExtra("account_type",(String) datiStudente.get("Account Type"));
                                intent.putExtra("name",(String) datiStudente.get("Name"));
                                intent.putExtra("surname",(String) datiStudente.get("Surname"));
                                intent.putExtra("badge_number",(String) datiStudente.get("Badge Number"));
                                intent.putExtra("faculty",(String) datiStudente.get("Faculty"));
                                intent.putExtra("email", email);

                                startActivity(intent);
                                finish();

                            } else {

                                //VERIFICARE LA TIPOLOGIA DI ACCOUNT -> PROFESSORE
                                // Crea un riferimento alla raccolta studenti
                                CollectionReference collectionReferenceProf = db.collection("professori");
                                // Crea un task per ottenere il documento con l'ID specifico
                                Task<DocumentSnapshot> checkProf = collectionReferenceProf.document(email).get();
                                // Aspetta che il task sia completato
                                checkProf.addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document1 = task1.getResult();
                                        if (document1.exists()) {
                                            Map<String,Object> datiProfessore =  document1.getData();

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                            intent.putExtra("account_type",(String) datiProfessore.get("Account Type"));
                                            intent.putExtra("name",(String) datiProfessore.get("Name"));
                                            intent.putExtra("surname",(String) datiProfessore.get("Surname"));
                                            intent.putExtra("faculty",(String) datiProfessore.get("Faculty"));
                                            intent.putExtra("email", email);

                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        // Errore durante la lettura del documento
                                    }
                                });




                            }
                            } else {
                                   // Errore durante la lettura del documento
                                }
                            });
                    //----------------------------------------------------------------------------------------------------------------------



                }else{

                    Toast.makeText(LoginActivity.this, "Utente non trovato, effettua la registrazione o inserisci correttamente i dati ", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}