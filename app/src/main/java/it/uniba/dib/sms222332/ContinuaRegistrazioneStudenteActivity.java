package it.uniba.dib.sms222332;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class ContinuaRegistrazioneStudenteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continua_registrazione_studente);

        // Dichiaro il menu a tendina per la facoltà
        Spinner spinner = findViewById(R.id.spinnerFacolta);
        // Dichiaro il pulsante presente nella schermata
        Button buttonConcludiRegistrazione = findViewById(R.id.buttonConcludi);

        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.facolta_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        spinner.setAdapter(adapter);

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludiRegistrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ContinuaRegistrazioneStudenteActivity.this, "Registrazione conclusa con successo!", Toast.LENGTH_SHORT).show();
                Intent intentLogin = new Intent(ContinuaRegistrazioneStudenteActivity.this, LoginActivity.class);
                startActivity(intentLogin);
            }
        });
    }
}