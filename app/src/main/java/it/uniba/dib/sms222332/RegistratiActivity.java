package it.uniba.dib.sms222332;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistratiActivity extends AppCompatActivity {

    Button buttonContinuaProfessore, buttonContinuaStudente;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrati);

        // Dichiaro i pulsanti presenti nella schermata
        buttonContinuaProfessore = findViewById(R.id.buttonContinuaProfessore);
        buttonContinuaStudente = findViewById(R.id.buttonContinuaStudente);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        // Passo alla schermata di continua registrazione per il professore
        buttonContinuaProfessore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuaComeProfessore();
            }
        });

        // Passo alla schermata di continua registrazione per lo studente
        buttonContinuaStudente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuaComeStudente();
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
        Intent intent = new Intent(RegistratiActivity.this, RegistrazioneProfActivity.class);
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
        Intent intent = new Intent(RegistratiActivity.this, RegistrazioneStudActivity.class);
        startActivity(intent);
    }




}