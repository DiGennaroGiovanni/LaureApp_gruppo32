package it.uniba.dib.sms222332;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Dichiaro i pulsanti presenti nella schermata
        Button buttonRegistrati = findViewById(R.id.buttonRegistrati);
        Button buttonAccedi = findViewById(R.id.buttonAccedi);

        // Passo alla schermata di registrazione
        buttonRegistrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentRegistrati = new Intent(LoginActivity.this, RegistratiActivity.class);
                startActivity(intentRegistrati);
            }
        });

        // Passo alla schermata home
        buttonAccedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Controllo dei dati inseriti e passaggio a schermata home
                /* inserimento intent home dopo il login

                Intent intentHome = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intentHome);
                */
            }
        });
    }
}