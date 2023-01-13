package it.uniba.dib.sms222332;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;
    private String tipologia = "Professore";

    String tipologiaUtente;
    String nomeUtente;
    String cognomeUtente;
    String matricolaUtente;
    String universitaUtente;
    String ruolo_utente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         tipologiaUtente = getIntent().getStringExtra("tipologia_utente"); // -> LEGGE IL PARAMETRO S o P che viene passato da LoginActivity
         nomeUtente = getIntent().getStringExtra("nome_utente");
         cognomeUtente = getIntent().getStringExtra("cognome_utente");
         matricolaUtente = getIntent().getStringExtra("matricola_utente");
         universitaUtente = getIntent().getStringExtra("universita_utente");
         ruolo_utente = getIntent().getStringExtra("ruolo_utente");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        setBottomNavigationBar();


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        bottomNav.setOnItemSelectedListener(navListener);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getProperHome()).commit();
        }
    }

    private void setBottomNavigationBar() {
        bottomNav.getMenu().clear();
        switch (tipologia) {
            case "Studente":
                bottomNav.inflateMenu(R.menu.bottom_navigation_stud);
                break;

            case "Professore":
                bottomNav.inflateMenu(R.menu.bottom_navigation_prof);
                break;
        }
    }

    private Fragment creazioneBundle(String nomeUtente, String cognomeUtente, String matricolaUtente, String universitaUtente) {
        Bundle bundle = new Bundle();
        bundle.putString("nome_utente", nomeUtente);
        bundle.putString("cognome_utente", cognomeUtente);
        bundle.putString("matricola_utente", matricolaUtente);
        bundle.putString("universita_utente", universitaUtente);

        ProfileFragment fragmentProfiloStud = new ProfileFragment();
        fragmentProfiloStud.setArguments(bundle);

        return fragmentProfiloStud;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = new StudentHomeFragment();

        switch (item.getItemId()) {
            case R.id.nav_profile:
                selectedFragment = creazioneBundle(nomeUtente, cognomeUtente, matricolaUtente, universitaUtente);
                break;

            case R.id.nav_language:
                selectedFragment = new LanguagesFragment();
                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        else
            super.onBackPressed();


    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;

        switch (item.getItemId()){

            case R.id.star_button:
                selectedFragment = new FavoritesFragment();
                break;

            case R.id.chat_button:
                selectedFragment = new MessagesFragment();
                break;

            case R.id.thesis_list_button:
                selectedFragment = new ThesisListFragment();
                break;

            case R.id.home_button:
            default:
                selectedFragment = getProperHome();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        selectBottomNavigationBarItem();
        return true;
    };

    private void selectBottomNavigationBarItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private Fragment getProperHome(){
        Fragment toReturn;
        switch (tipologia){
            case "Studente":
                toReturn = new StudentHomeFragment();
                break;

            case "Professore":
            default:
                toReturn = new ProfessorHomeFragment();
                break;
        }

        return toReturn;
    }


}