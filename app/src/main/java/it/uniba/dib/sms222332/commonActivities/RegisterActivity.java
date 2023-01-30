package it.uniba.dib.sms222332.commonActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.professor.ProfessorRegisterActivity;
import it.uniba.dib.sms222332.student.StudentRegisterActivity;

public class RegisterActivity extends AppCompatActivity {

    Button btnProf, btnStud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrati);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.registration);

        btnProf = findViewById(R.id.btnProf);
        btnStud = findViewById(R.id.btnStud);

        btnProf.setOnClickListener(view -> profRegistration());

        btnStud.setOnClickListener(view -> studRegistration());
    }

    private void profRegistration() {
        Intent intent = new Intent(RegisterActivity.this, ProfessorRegisterActivity.class);
        startActivity(intent);
    }

    private void studRegistration() {
        Intent intent = new Intent(RegisterActivity.this, StudentRegisterActivity.class);
        startActivity(intent);
    }


}