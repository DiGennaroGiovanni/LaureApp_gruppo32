package it.uniba.dib.sms222332.commonActivities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.connection.NoConnectionActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LanguageManager lang = new LanguageManager(this);
        lang.updateResource(lang.getLang());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Objects.requireNonNull(getSupportActionBar()).hide();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Intent intent = new Intent(SplashActivity.this, NoConnectionActivity.class);
            startActivity(intent);
            finish();
        } else if (currentUser != null) {
            //UTENTE LOGGATO

            String email = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            assert email != null;
            DocumentReference docRefStud = db.collection("studenti").document(email);

            docRefStud.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // L'UTENTE E' UNO STUDENTE
                        studentDirectLogin(email, document);

                    } else {
                        // L'UTENTE E' UN PROFESSORE
                        professorDirectLogin(email, db);
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            });
        } else {
            // UTENTE NON HA FATTO L'ACCESSO

            // Mostra l'immagine splash per 2 secondi
            new Handler().postDelayed(() -> {
                final Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, SPLASH_DISPLAY_LENGTH);
        }
    }


    private void professorDirectLogin(String email, FirebaseFirestore db) {
        DocumentReference docRefProf = db.collection("professori").document(email);
        docRefProf.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document1 = task1.getResult();
                if (document1.exists()) {
                    // L'UTENTE E' UNO STUDENTE
                    Map<String, Object> datiProfessore = document1.getData();

                    // Mostra l'immagine splash per 2 secondi
                    new Handler().postDelayed(() -> {

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                        assert datiProfessore != null;
                        intent.putExtra("account_type", (String) datiProfessore.get("Account Type"));
                        intent.putExtra("name", (String) datiProfessore.get("Name"));
                        intent.putExtra("surname", (String) datiProfessore.get("Surname"));
                        intent.putExtra("faculty", (String) datiProfessore.get("Faculty"));
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }, SPLASH_DISPLAY_LENGTH);
                }
            }
        });
    }

    private void studentDirectLogin(String email, DocumentSnapshot document) {
        Map<String, Object> datiStudente = document.getData();

        // Mostra l'immagine splash per 2 secondi
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);

            assert datiStudente != null;
            intent.putExtra("account_type", (String) datiStudente.get("Account Type"));
            intent.putExtra("name", (String) datiStudente.get("Name"));
            intent.putExtra("surname", (String) datiStudente.get("Surname"));
            intent.putExtra("badge_number", (String) datiStudente.get("Badge Number"));
            intent.putExtra("faculty", (String) datiStudente.get("Faculty"));
            intent.putExtra("email", email);
            intent.putExtra("request", (String) datiStudente.get("Request"));
            intent.putExtra("favorite_theses", (ArrayList<?>) datiStudente.get("Favorites"));
            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }
}