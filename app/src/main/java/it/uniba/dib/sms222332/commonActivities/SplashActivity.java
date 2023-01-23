package it.uniba.dib.sms222332.commonActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import it.uniba.dib.sms222332.R;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // user is logged in

            String email = currentUser.getEmail();


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRefStud = db.collection("studenti").document(email);

            docRefStud.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // user is a professor
                        Map<String, Object> datiStudente = document.getData();

                        // Mostra l'immagine splash per 2 secondi
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                            intent.putExtra("account_type", (String) datiStudente.get("Account Type"));
                            intent.putExtra("name", (String) datiStudente.get("Name"));
                            intent.putExtra("surname", (String) datiStudente.get("Surname"));
                            intent.putExtra("badge_number", (String) datiStudente.get("Badge Number"));
                            intent.putExtra("faculty", (String) datiStudente.get("Faculty"));
                            intent.putExtra("email", email);
                            intent.putExtra("request",(String) datiStudente.get("Request"));
                            startActivity(intent);
                            finish();

                        }, SPLASH_DISPLAY_LENGTH);

                    } else {
                        // check if user is a student
                        DocumentReference docRefProf = db.collection("professori").document(email);
                        docRefProf.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document1 = task1.getResult();
                                if (document1.exists()) {
                                    // user is a student
                                    Map<String,Object> datiProfessore =  document1.getData();

                                    // Mostra l'immagine splash per 2 secondi
                                    new Handler().postDelayed(() -> {

                                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                                        intent.putExtra("account_type",(String) datiProfessore.get("Account Type"));
                                        intent.putExtra("name",(String) datiProfessore.get("Name"));
                                        intent.putExtra("surname",(String) datiProfessore.get("Surname"));
                                        intent.putExtra("faculty",(String) datiProfessore.get("Faculty"));
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        finish();
                                    }, SPLASH_DISPLAY_LENGTH);

                                } else {
//                                            Log.d("TAG", "No such document");
                                }
                            } else {
//                                        Log.d("TAG", "get failed with ", task.getException());
                            }
                        });
                    }
                } else {
//                        Log.d("TAG", "get failed with ", task.getException());
                }
            });



        } else {
            // user is not logged in

            // Mostra l'immagine splash per 2 secondi
            new Handler().postDelayed(() -> {
                final Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, SPLASH_DISPLAY_LENGTH);
        }
    }
}