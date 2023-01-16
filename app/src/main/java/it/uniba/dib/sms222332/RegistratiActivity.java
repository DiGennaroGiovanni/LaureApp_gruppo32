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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrati);

        // Dichiaro i pulsanti presenti nella schermata
        buttonContinuaProfessore = findViewById(R.id.buttonContinuaProfessore);
        buttonContinuaStudente = findViewById(R.id.buttonContinuaStudente);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        // Passo alla schermata di continua registrazione per il professore
        buttonContinuaProfessore.setOnClickListener(view -> continuaComeProfessore());

        // Passo alla schermata di continua registrazione per lo studente
        buttonContinuaStudente.setOnClickListener(view -> continuaComeStudente());


    }

    private void continuaComeProfessore() {


        Intent intent = new Intent(RegistratiActivity.this, ProfessorRegisterActivity.class);
        startActivity(intent);
    }

    private void continuaComeStudente() {

        Intent intent = new Intent(RegistratiActivity.this, StudentRegisterActivity.class);
        startActivity(intent);
    }




}