package it.uniba.dib.sms222332.commonActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.professor.ProfessorAccount;
import it.uniba.dib.sms222332.professor.ProfessorHomeFragment;
import it.uniba.dib.sms222332.professor.ThesisListFragment;
import it.uniba.dib.sms222332.student.FavoritesFragment;
import it.uniba.dib.sms222332.student.StudentAccount;
import it.uniba.dib.sms222332.student.StudentHomeFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;

    public static Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        TextView nameDisplay = headerView.findViewById(R.id.nameSurnameTxt);
        TextView profession = headerView.findViewById(R.id.professionTxt);


        setAccount();

        setBottomNavigationBar(account.getAccountType());

        String nameSurname = account.getName() + " " + account.getSurname();
        nameDisplay.setText(nameSurname);

        setProfession(profession, account.getAccountType());

        bottomNav.setOnItemSelectedListener(navListener);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getProperHome()).commit();
        }
    }

    /**
     * Inserisce i dati del database nell'interfaccia Account
     * L'account può essere di tipo ProfessorAccount o StudentAccount
     */
    private void setAccount() {
        switch (getIntent().getStringExtra("account_type")){

            case "Professor":
                generateProfessor();
                break;

            case "Student":
                generateStudent();
                break;
        }
    }

    private void generateProfessor() {
        String name = getIntent().getStringExtra("name");
        String surname = getIntent().getStringExtra("surname");
        String faculty = getIntent().getStringExtra("faculty");
        String email = getIntent().getStringExtra("email");

        account = new ProfessorAccount(name, surname, faculty, email);
    }

    private void generateStudent() {
        String name = getIntent().getStringExtra("name");
        String surname = getIntent().getStringExtra("surname");
        String badgeNumber = getIntent().getStringExtra("badge_number");
        String faculty = getIntent().getStringExtra("faculty");
        String email = getIntent().getStringExtra("email");
        String request = getIntent().getStringExtra("request");

        account = new StudentAccount(name, surname, badgeNumber, faculty, email, request);
    }

    private void setBottomNavigationBar(String accountType) {
        bottomNav.getMenu().clear();
        switch (accountType) {
            case "Student":
                bottomNav.inflateMenu(R.menu.bottom_navigation_stud);
                break;

            case "Professor":
                bottomNav.inflateMenu(R.menu.bottom_navigation_prof);
                break;
        }
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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = getProperHome();

        switch (item.getItemId()) {
            case R.id.nav_profile:
                selectedFragment = profileBundle(account);
                break;

            case R.id.nav_language:
                selectedFragment = new LanguagesFragment();
                break;

            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Conferma logout");
                builder.setMessage("Sei sicuro di voler effettuare il logout?");

                builder.setNegativeButton("Yes", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                });

                builder.setPositiveButton("No", (dialog, which) -> {

                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment profileBundle(Account account) {
        Bundle bundle = new Bundle();
        bundle.putString("name", account.getName());
        bundle.putString("surname", account.getSurname());
        bundle.putString("email", account.getEmail());
        bundle.putString("faculty", account.getFaculty());

        if (account.getAccountType().equals("Student") )
            bundle.putString("badge_number", account.getBadgeNumber());
        else
            bundle.putString("badge_number", "");

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);

        return profileFragment;
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



    private void selectBottomNavigationBarItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private Fragment getProperHome(){
        Fragment toReturn;
        switch (account.getAccountType()){
            case "Student":
                toReturn = new StudentHomeFragment();
                break;

            case "Professor":
            default:
                toReturn = new ProfessorHomeFragment();
                break;
        }

        return toReturn;
    }

    private void setProfession(TextView profession, String accountType){
        switch (accountType){
            case "Professor":
                profession.setText(R.string.professor);
                break;

            case "Student":
                profession.setText(R.string.studentProfession);
                break;
        }
    }


}