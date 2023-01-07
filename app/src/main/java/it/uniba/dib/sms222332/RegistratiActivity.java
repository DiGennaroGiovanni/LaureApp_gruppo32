package it.uniba.dib.sms222332;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class RegistratiActivity extends AppCompatActivity {

    Button buttonContinuaProfessore, buttonContinuaStudente;
    EditText edtEmailRegistrati, edtPasswordRegistrati;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrati);

        // Dichiaro i pulsanti presenti nella schermata
        buttonContinuaProfessore = findViewById(R.id.buttonContinuaProfessore);
        buttonContinuaStudente = findViewById(R.id.buttonContinuaStudente);
        edtEmailRegistrati = findViewById(R.id.edtEmailRegistrati);
        edtPasswordRegistrati = findViewById(R.id.edtPasswordRegistrati);

        progressDialog = new ProgressDialog(this);;
        mAuth = FirebaseAuth.getInstance();;
        mUser = mAuth.getCurrentUser();;


        // Passo alla schermata di continua registrazione per il professore
        buttonContinuaProfessore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerfomrAuthProf();

            }
        });

        // Passo alla schermata di continua registrazione per lo studente
        buttonContinuaStudente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerfomrAuthStud();
            }
        });


    }

    private void continuaComeProfessore() {
        progressDialog.setMessage("ATTENDI...");
        progressDialog.setTitle("Registrazione professore");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //sendUserToUploadFile(); -> INSERIRE METODO CHE PORTA ALLA HOME DA LOGGATO
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 2000);
        Intent intent = new Intent(RegistratiActivity.this,ContinuaRegistrazioneProfessoreActivity.class);
        startActivity(intent);
    }

    private void continuaComeStudente() {
        progressDialog.setMessage("ATTENDI...");
        progressDialog.setTitle("Registrazione studente");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //sendUserToUploadFile(); -> INSERIRE METODO CHE PORTA ALLA HOME DA LOGGATO
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 2000);
        Intent intent = new Intent(RegistratiActivity.this,ContinuaRegistrazioneStudenteActivity.class);
        startActivity(intent);
    }

    private void PerfomrAuthProf() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        if(!email.matches(emailPattern))
        {
            edtEmailRegistrati.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordRegistrati.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{
            //progressDialog.setMessage("Registrazione in corso..");
            //progressDialog.setTitle("Registrazione");
            //progressDialog.setCanceledOnTouchOutside(false);
            //progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        //progressDialog.dismiss();
                        Toast.makeText(RegistratiActivity.this, "Continua come professore!", Toast.LENGTH_SHORT).show();
                        continuaComeProfessore();

                    }else{
                        //Toast.makeText(RegistratiActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(RegistratiActivity.this, "Email già in uso, ripova!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void PerfomrAuthStud() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        if(!email.matches(emailPattern))
        {
            edtEmailRegistrati.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordRegistrati.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{
            //progressDialog.setMessage("Registrazione in corso..");
            //progressDialog.setTitle("Registrazione");
            //progressDialog.setCanceledOnTouchOutside(false);
            //progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        //progressDialog.dismiss();
                        Toast.makeText(RegistratiActivity.this, "Continua come studente", Toast.LENGTH_SHORT).show();
                        continuaComeStudente();

                    }else{
                        Toast.makeText(RegistratiActivity.this, "Email già in uso, ripova!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}