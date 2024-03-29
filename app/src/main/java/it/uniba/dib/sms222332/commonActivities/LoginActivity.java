package it.uniba.dib.sms222332.commonActivities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.connection.NetworkChangeReceiver;
import it.uniba.dib.sms222332.guest.MainActivityGuest;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText edtEmailLogin, edtPasswordLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //CONTROLLO  CONNESSIONE AD INTERNET
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(this), filter);

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (getIntent().getBooleanExtra("logout", false)) {
            View view = findViewById(android.R.id.content);
            Snackbar.make(view, R.string.logged_out, Snackbar.LENGTH_SHORT).show();
        } else if (getIntent().getBooleanExtra("signed up", false)) {
            View view = findViewById(android.R.id.content);
            Snackbar.make(view, R.string.registration_completed, Snackbar.LENGTH_SHORT).show();
        }

        // Dichiaro i pulsanti presenti nella schermata
        Button registerBtn = findViewById(R.id.btnRegistrati);
        Button btnGuest = findViewById(R.id.btnGuest);
        Button loginBtn = findViewById(R.id.btnAccedi);
        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);

        ImageButton en = findViewById(R.id.btn_eng);
        ImageButton it = findViewById(R.id.btn_it);
        LanguageManager lang = new LanguageManager(this);

        en.setOnClickListener(v -> {
            Intent intent = getIntent();
            if (intent.hasExtra("logout"))
                intent.removeExtra("logout");
            lang.updateResource("en");
            this.recreate();
        });
        it.setOnClickListener(v -> {
            Intent intent = getIntent();
            if (intent.hasExtra("logout"))
                intent.removeExtra("logout");
            lang.updateResource("it");
            this.recreate();
        });

        mAuth = FirebaseAuth.getInstance();

        // Passo alla schermata di registrazione
        registerBtn.setOnClickListener(view -> {
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });

        // Passo alla schermata home
        loginBtn.setOnClickListener(view -> performLogin());

        btnGuest.setOnClickListener(view -> {
            Intent guestIntent = new Intent(LoginActivity.this, MainActivityGuest.class);
            startActivity(guestIntent);
            finish();
        });
    }

    private void performLogin() {

        View view = findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        String email = edtEmailLogin.getText().toString();
        String password = edtPasswordLogin.getText().toString();

        String emailPatternFaculty = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-zA-Z._-]+\\.[a-z]+";
        String emailPattern = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-z]+";

        if (!email.matches(emailPatternFaculty) && !email.matches(emailPattern)) {
            edtEmailLogin.setError(getString(R.string.enter_valid_email));
        } else if (password.isEmpty() || password.length() < 6) {
            edtPasswordLogin.setError(getString(R.string.enter_valid_password));
        } else {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    //VERIFICARE LA TIPOLOGIA DI ACCOUNT -> STUDENTE

                    // Crea un riferimento alla raccolta studenti
                    CollectionReference collectionReferenceStud = db.collection("studenti");
                    // Crea un task per ottenere il documento con l'ID specifico
                    Task<DocumentSnapshot> checkStud = collectionReferenceStud.document(email).get();
                    // Aspetta che il task sia completato
                    checkStud.addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            DocumentSnapshot document = task12.getResult();
                            if (document.exists()) {
                                studentLogin(email, document);

                            } else {

                                //VERIFICARE LA TIPOLOGIA DI ACCOUNT -> PROFESSORE
                                // Crea un riferimento alla raccolta studenti
                                CollectionReference collectionReferenceProf = db.collection("professori");
                                // Crea un task per ottenere il documento con l'ID specifico
                                Task<DocumentSnapshot> checkProf = collectionReferenceProf.document(email).get();
                                // Aspetta che il task sia completato
                                checkProf.addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document1 = task1.getResult();
                                        if (document1.exists()) {
                                            professorLogin(email, document1);
                                        }
                                    } else {
                                        Log.e("E", "Error");
                                    }
                                });
                            }
                        } else {
                            Log.e("E", "Error");
                        }
                    });

                } else {
                    Snackbar.make(view, R.string.user_not_found, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void professorLogin(String email, DocumentSnapshot document1) {
        Map<String, Object> datiProfessore = document1.getData();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        assert datiProfessore != null;
        intent.putExtra("account_type", (String) datiProfessore.get("Account Type"));
        intent.putExtra("name", (String) datiProfessore.get("Name"));
        intent.putExtra("surname", (String) datiProfessore.get("Surname"));
        intent.putExtra("faculty", (String) datiProfessore.get("Faculty"));
        intent.putExtra("email", email);

        startActivity(intent);
        finish();
    }

    private void studentLogin(String email, DocumentSnapshot document) {
        Map<String, Object> datiStudente = document.getData();


        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

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
    }
}