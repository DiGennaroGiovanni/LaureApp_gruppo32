package it.uniba.dib.sms222332;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegistratiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrati);

        // Dichiaro i pulsanti presenti nella schermata
        Button buttonContinuaProfessore = findViewById(R.id.buttonContinuaProfessore);
        Button buttonContinuaStudente = findViewById(R.id.buttonContinuaStudente);

        // Passo alla schermata di continua registrazione per il professore
        buttonContinuaProfessore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentContinuaRegistrazioneProfessore = new Intent(RegistratiActivity.this, ContinuaRegistrazioneProfessoreActivity.class);
                startActivity(intentContinuaRegistrazioneProfessore);
            }
        });

        // Passo alla schermata di continua registrazione per lo studente
        buttonContinuaStudente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentContinuaRegistrazioneStudente = new Intent(RegistratiActivity.this, ContinuaRegistrazioneStudenteActivity.class);
                startActivity(intentContinuaRegistrazioneStudente);
            }
        });
    }
}