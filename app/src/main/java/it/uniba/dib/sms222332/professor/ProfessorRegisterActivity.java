package it.uniba.dib.sms222332.professor;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.LoginActivity;
import it.uniba.dib.sms222332.commonActivities.connection.NetworkChangeReceiver;

public class ProfessorRegisterActivity extends AppCompatActivity {

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    EditText edtNomeProf, edtCognomeProf, edtEmailRegistrati, edtPasswordRegistrati;
    Button buttonConcludiProf;
    Spinner spinnerDepartment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_prof);
        setTitle(getString(R.string.title_new_professor));

        //CONTROLLO  CONNESSIONE AD INTERNET
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(this), filter);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.registration);


        buttonConcludiProf = findViewById(R.id.buttonConcludiProf);
        edtCognomeProf = findViewById(R.id.edtCognomeProf);
        edtNomeProf = findViewById(R.id.edtNomeProf);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);

        edtEmailRegistrati = findViewById(R.id.edtEmailRegistrati);
        edtPasswordRegistrati = findViewById(R.id.edtPasswordRegistrati);

        /*      GESTISCO LO SPINNER     */

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ProfessorRegisterActivity.this, R.array.faculty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludiProf.setOnClickListener(view -> performAuthProf());

    }

    private void insertDataProf() {


        String nome = edtNomeProf.getText().toString().toLowerCase();
        char firstChar = Character.toUpperCase(nome.charAt(0));
        nome = firstChar + nome.substring(1);

        String cognome = edtCognomeProf.getText().toString().toLowerCase();
        firstChar = Character.toUpperCase(cognome.charAt(0));
        cognome = firstChar + cognome.substring(1);

        String department = spinnerDepartment.getSelectedItem().toString();

        String email = edtEmailRegistrati.getText().toString().toLowerCase();


        Map<String, String> infoProfessore = new HashMap<>();
        infoProfessore.put("Name", nome);
        infoProfessore.put("Surname", cognome);
        infoProfessore.put("Faculty", department);
        infoProfessore.put("Account Type", "Professor");

        if (nome.isEmpty())
            edtNomeProf.setError(getString(R.string.enter_name));
        else if (cognome.isEmpty())
            edtCognomeProf.setError(getString(R.string.enter_surname));
        else {
            db.collection("professori").document(email).set(infoProfessore);
        }
    }

    private void performAuthProf() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        String emailPatternFaculty = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-zA-Z._-]+\\.[a-z]+";
        String emailPattern = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-z]+";

        if (!email.matches(emailPatternFaculty) && !email.matches(emailPattern)) {
            edtEmailRegistrati.setError(getString(R.string.enter_valid_email));
        } else if (password.isEmpty() || password.length() < 6) {
            edtPasswordRegistrati.setError(getString(R.string.enter_valid_password));
        } else {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    insertDataProf(); //MEMORIZZO I DATI DEL PROFESSORE

                    Intent intent = new Intent(ProfessorRegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("signed up", true);
                    startActivity(intent);

                } else {
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, R.string.email_already_existent, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }
}