package it.uniba.dib.sms222332;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfessorRegisterActivity extends AppCompatActivity {

    //Istanze del database e del sistema di autenticazione di firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    EditText edtNomeProf,edtCognomeProf, edtRuoloProf, edtEmailRegistrati, edtPasswordRegistrati;
    Button buttonConcludiProf;

    String emailPattern = "[a-zA-Z0-9._-]+@+[a-zA-Z._-]+\\.+[a-z]+";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_prof);

        buttonConcludiProf = findViewById(R.id.buttonConcludiProf);
        edtCognomeProf = findViewById(R.id.edtCognomeProf);
        edtNomeProf = findViewById(R.id.edtNomeProf);
        edtRuoloProf = findViewById(R.id.edtRuoloProf);
        edtEmailRegistrati = findViewById(R.id.edtEmailRegistrati);
        edtPasswordRegistrati = findViewById(R.id.edtPasswordRegistrati);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        /*      GESTISCO IL BOTTONE CONTINUA REGISTRAZIONE      */
        buttonConcludiProf.setOnClickListener(view -> performAuthProf());
    }

    private void insertDataProf() {


        String nome = edtNomeProf.getText().toString().toLowerCase();
        char firstChar = Character.toUpperCase(nome.charAt(0));
        nome = firstChar+nome.substring(1);

        String cognome = edtCognomeProf.getText().toString().toLowerCase();
        firstChar = Character.toUpperCase(cognome.charAt(0));
        cognome = firstChar+cognome.substring(1);

        String ruolo = edtRuoloProf.getText().toString().toLowerCase();
        firstChar = Character.toUpperCase(ruolo.charAt(0));
        ruolo = firstChar+ruolo.substring(1);

        String email = edtEmailRegistrati.getText().toString().toLowerCase();


        Map<String, String> infoProfessore = new HashMap<>();
        infoProfessore.put("Nome",nome);
        infoProfessore.put("Cognome",cognome);
        infoProfessore.put("Ruolo",ruolo);
        infoProfessore.put("Tipologia","Professor");

        if(nome.isEmpty())
            edtNomeProf.setError("Inserisci il tuo nome!");
        else if(cognome.isEmpty())
            edtCognomeProf.setError("Inserisci il tuo cognome!");
        else if(ruolo.isEmpty())
            edtRuoloProf.setError("Inserisci il tuo ruolo!");
        else{
            db.collection("professori").document(email).set(infoProfessore);
        }
    }

    private void performAuthProf() {
        String email = edtEmailRegistrati.getText().toString();
        String password = edtPasswordRegistrati.getText().toString();

        if(!email.matches(emailPattern))
        {
            edtEmailRegistrati.setError("Inserisci un'email valida!");
        }else if(password.isEmpty() || password.length()<6){
            edtPasswordRegistrati.setError("Inserisci una password valida (deve avere almeno 6 caratteri)");
        }else{

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    insertDataProf(); //MEMORIZZO I DATI DEL PROFESSORE

                    Intent intent = new Intent(ProfessorRegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
//                        Toast.makeText(ProfessorRegisterActivity.this, "Registrazione completata!", Toast.LENGTH_SHORT).show();


                }else{
                    Toast.makeText(ProfessorRegisterActivity.this, "Email già in uso, ripova!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}