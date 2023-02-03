package it.uniba.dib.sms222332.commonActivities.connection;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.SplashActivity;

public class NoConnectionActivity extends AppCompatActivity {

    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(view -> {

            //CONTROLLA LO STATO DELLA CONNESSIONE E SALVA IL RISULTATO IN UN BOOLEAN
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                Snackbar.make(findViewById(android.R.id.content), "Connessione assente", Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}