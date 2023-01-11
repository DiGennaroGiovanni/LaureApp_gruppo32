package it.uniba.dib.sms222332;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    Button btnAccedi,btnRegistrati;
    EditText edtEmailLogin,edtPasswordLogin;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Dichiaro i pulsanti presenti nella schermata
        btnRegistrati = findViewById(R.id.btnRegistrati);
        btnAccedi = findViewById(R.id.btnAccedi);
        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);

        mAuth = FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();

        // Passo alla schermata di registrazione
        btnRegistrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentRegistrati = new Intent(LoginActivity.this, RegistratiActivity.class);
                startActivity(intentRegistrati);
            }
        });

        // Passo alla schermata home
        btnAccedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String email = edtEmailLogin.getText().toString();
        String password = edtPasswordLogin.getText().toString();

        if(!email.matches(emailPattern))
        {
            edtEmailLogin.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordLogin.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        //VERIFICARE LA TIPOLOGIA DI ACCOUNT

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        //TODO putextra (informarsi bene) passare il parametro come char "s" o "p"
                        startActivity(intent);


                    }else{
                        Toast.makeText(LoginActivity.this, "Utente non trovato, effettua la registrazione o inserisci correttamente i dati ", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}