package it.uniba.dib.sms222332.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.LoginActivity;

public class StudentRegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText edtNomeStudente,edtCognomeStudente, edtMatricolaStudente, edtEmailRegistrati, edtPasswordRegistrati;
    TextView txtFacoltaStudente;
    Spinner spinnerFacolta;
    Button buttonConcludi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_stud);

        edtEmailRegistrati = findViewById(R.id.edtEmailRegistrati);
        edtPasswordRegistrati = findViewById(R.id.edtPasswordRegistrati);
        edtCognomeStudente = findViewById(R.id.edtCognomeStudente);
        edtNomeStudente = findViewById(R.id.edtNomeStudente);
        edtMatricolaStudente = findViewById(R.id.edtMatricolaStudente);
        spinnerFacolta = findViewById(R.id.spinnerFacolta);
        buttonConcludi = findViewById(R.id.buttonConcludi);
        txtFacoltaStudente = findViewById(R.id.txtFacoltaStudente);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        /*      GESTISCO LO SPINNER     */
        // Creo un ArrayAdapter che contiene i valori dello spinner e gli affiso il layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.faculty_array, android.R.layout.simple_spinner_item);
        // Specifico il layout che appare quando viene cliccato sullo spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Setto l'ArrayAdapter allo spinner
        spinnerFacolta.setAdapter(adapter);

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludi.setOnClickListener(view -> performAuthStud());
    }

    private void insertDataStudente() {
        String nome = edtNomeStudente.getText().toString().toLowerCase();
        char firstChar = Character.toUpperCase(nome.charAt(0));
        nome = firstChar+nome.substring(1);

        String cognome = edtCognomeStudente.getText().toString().toLowerCase();
        firstChar = Character.toUpperCase(cognome.charAt(0));
        cognome = firstChar+cognome.substring(1);

        String matricola = edtMatricolaStudente.getText().toString();
        String facolta = spinnerFacolta.getSelectedItem().toString();
        String email = edtEmailRegistrati.getText().toString().toLowerCase();

        Map<String, String> infoStudente = new HashMap<>();
        infoStudente.put("Name",nome);
        infoStudente.put("Surname",cognome);
        infoStudente.put("Badge Number",matricola);
        infoStudente.put("Faculty",facolta);
        infoStudente.put("Account Type","Student");

        if(nome.isEmpty())
            edtNomeStudente.setError("Inserisci il tuo nome!");
        else if(cognome.isEmpty())
            edtCognomeStudente.setError("Inserisci il tuo cognome!");
        else if(matricola.isEmpty())
            edtMatricolaStudente.setError("Inserisci la tua matricola correttamente!");
        else if(facolta.isEmpty())
            txtFacoltaStudente.setError("Seleziona la tua facoltà!");
        else{
            db.collection("studenti").document(email).set(infoStudente);
        }
    }

    private void performAuthStud() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        String emailPattern = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-z]+";
        if(!email.matches(emailPattern))
        {
            edtEmailRegistrati.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordRegistrati.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{


            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){

                    insertDataStudente();
                    Intent intent = new Intent(StudentRegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("signed up", true);
                    startActivity(intent);

                }else{
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, R.string.email_already_existent, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }
}