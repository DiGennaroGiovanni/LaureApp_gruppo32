package it.uniba.dib.sms222332.commonActivities;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.Messages.ThesesMessagesListFragment;
import it.uniba.dib.sms222332.commonActivities.connection.NetworkChangeReceiver;
import it.uniba.dib.sms222332.professor.ProfessorAccount;
import it.uniba.dib.sms222332.professor.ProfessorHomeFragment;
import it.uniba.dib.sms222332.professor.ThesesListFragment;
import it.uniba.dib.sms222332.student.MyThesisFragment;
import it.uniba.dib.sms222332.student.StudentAccount;
import it.uniba.dib.sms222332.student.StudentHomeFragment;
import it.uniba.dib.sms222332.student.favorites.FavoritesFragment;
import it.uniba.dib.sms222332.tools.CaptureAct;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static Account account;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private Menu nav_Menu;
    private NavigationView navigationView;
    public static ArrayList<Thesis> theses;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LanguageManager lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lang = new LanguageManager(this);

        //CONTROLLO  CONNESSIONE AD INTERNET
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(this), filter);

        if(savedInstanceState != null) {
            lang.updateResource(savedInstanceState.getString("lang"));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        nav_Menu = navigationView.getMenu();

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

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getProperHome()).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String language = lang.getLang();
        outState.putString("lang", language);
    }

    /**
     * Inserisce i dati del database nell'interfaccia Account
     * L'account può essere di tipo ProfessorAccount o StudentAccount
     */
    private void setAccount() {
        switch (getIntent().getStringExtra("account_type")) {

            case "Professor":
                generateProfessor();
                break;

            case "Student":
                generateStudent();
                setFavorites();
                break;
        }
    }

    private void setFavorites() {
        theses = new ArrayList<>();
        List<String> thesisNames = getIntent().getStringArrayListExtra("favorite_theses");
        for (String thesisName : thesisNames) {
            Thesis thesis = new Thesis(thesisName, "");
            theses.add(thesis);
        }

        for (Thesis thesis : theses) {
            db.collection("Tesi").document(thesis.getName()).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    if (!Objects.requireNonNull(documentSnapshot.getString("Student")).isEmpty() && !Objects.equals(documentSnapshot.getString("Student"), MainActivity.account.getEmail()))
                        theses.remove(thesis);
                    else
                        thesis.setProfessor(documentSnapshot.getString("Professor"));
                }else
                    theses.remove(thesis);
             });
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
                nav_Menu.findItem(R.id.nav_scan_qr).setVisible(true);
                break;

            case "Professor":
                bottomNav.inflateMenu(R.menu.bottom_navigation_prof);
                nav_Menu.findItem(R.id.nav_scan_qr).setVisible(false);
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;

        switch (item.getItemId()) {

            case R.id.star_button:
                selectedFragment = new FavoritesFragment();
                break;

            case R.id.chat_button:
                selectedFragment = new ThesesMessagesListFragment();
                break;

            case R.id.thesis_list_button:
                selectedFragment = new ThesesListFragment();
                break;

            case R.id.home_button:
            default:
                selectedFragment = getProperHome();
        }

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .replace(R.id.fragment_container, selectedFragment)
                .commit();

        selectBottomNavigationBarItem();
        return true;
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        switch (item.getItemId()) {
            case R.id.nav_profile:
                selectedFragment = new ProfileFragment();
                break;

            case R.id.nav_language:
                selectedFragment = new LanguagesFragment();
                break;

            case R.id.nav_scan_qr:
                if (checkPermission()) scanQrCode();
                break;

            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.logout_confirm);
                builder.setMessage(R.string.logout_question);

                builder.setNegativeButton(R.string.no, (dialog, which) -> {

                });

                builder.setPositiveButton(R.string.yes, (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                    finish();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        assert selectedFragment != null;
        fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ProfessorHomeFragment) && !(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof StudentHomeFragment)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getProperHome()).commit();
            bottomNav.setSelectedItemId(R.id.home_button);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_app_confirm);
            builder.setMessage(R.string.exit_app_question);

            builder.setNegativeButton(R.string.no, (dialog, which) -> {

            });

            builder.setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void selectBottomNavigationBarItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private Fragment getProperHome() {
        Fragment toReturn;
        switch (account.getAccountType()) {
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

    private void setProfession(TextView profession, String accountType) {
        switch (accountType) {
            case "Professor":
                profession.setText(R.string.professor);
                break;

            case "Student":
                profession.setText(R.string.studentProfession);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (account.getAccountType().equals("Student")) {

            ArrayList<String> thesisNames = new ArrayList<>();
            for (Thesis thesis : theses)
                thesisNames.add(thesis.getName());

            db.collection("studenti").document(account.getEmail()).update("Favorites", thesisNames);
        }

    }

    private void scanQrCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on ");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String onlineUser = MainActivity.account.getEmail();
            String jsonInput = result.getContents();
            String thesisName = "";
            try {
                JSONObject json = new JSONObject(jsonInput);
                thesisName = json.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            DocumentReference docRef = db.collection("Tesi").document(thesisName);
            docRef.get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                String student = document.getString("Student");

                assert student != null;
                if (student.equals(onlineUser)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyThesisFragment()).commit();
                } else {

                    Bundle bundle = new Bundle();
                    Fragment guestThesis = new ThesisDescriptionUserFragment();

                    Map<String, Object> datiTesi = document.getData();
                    assert datiTesi != null;
                    db.collection("professori").document(Objects.requireNonNull(datiTesi.get("Professor")).toString()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            bundle.putString("professor", Objects.requireNonNull(task1.getResult().get("Name")) + " " + Objects.requireNonNull(task1.getResult().get("Surname")));
                            bundle.putString("correlator", (String) datiTesi.get("Correlator"));
                            bundle.putString("description", (String) datiTesi.get("Description"));
                            bundle.putString("estimated_time", (String) datiTesi.get("Estimated Time"));
                            bundle.putString("faculty", (String) datiTesi.get("Faculty"));
                            bundle.putString("name", (String) datiTesi.get("Name"));
                            bundle.putString("type", (String) datiTesi.get("Type"));
                            bundle.putString("related_projects", (String) datiTesi.get("Related Projects"));
                            bundle.putString("average_marks", (String) datiTesi.get("Average"));
                            bundle.putString("required_exams", (String) datiTesi.get("Required Exam"));
                            bundle.putString("professor_email", (String) datiTesi.get("Professor"));

                            guestThesis.setArguments(bundle);
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, guestThesis);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                    });
                }
            });
        }
    });

    /**
     * checkPermission è il metodo che gestisce i permessi per utilizzare la fotocamera.
     * <p>
     * Nel caso in cui l'utente non fornisce l'autorizzazioen per utilizzare la fotocamera, il sistemare
     * provvederà a fornire un feedback all'utente per spiegare l'utilità dei permessi.
     * <p>
     * Nel
     *
     * @return result true se i permessi sono stati concessi
     */
    private boolean checkPermission() {
        boolean result = false;

        // controllo se i permessi sono già stati concessi.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Il permesso di utilizzare la fotocamera risulta concesso.
            result = true;

            // Mostro un messaggio all'utente in cui spiego il motivo per il quale sono necessari i permessi.
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.snackbar_title_camera_permission);
            builder.setMessage(getString(R.string.snackbar_camera_permission_message) + "\n\n" + getString(R.string.snackbar_camera_permission_message2));

            // L'utente accetta di concedere i permessi e avvio richiesta accettazione permessi.
            builder.setPositiveButton("Yes", (dialogInterface, i) -> requestPermissionLauncher.launch(CAMERA));

            // L'utente decide di non accettare l'avvio di richiesta accettazione permessi.
            builder.setNegativeButton("No", (dialogInterface, i) -> Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show());

            AlertDialog dialog = builder.create();
            dialog.show();  // Avvio la visualizzazione dell'AlertDialog.
        } else {
            // Avvio la procedura di autorizzazione dei permessi.
            requestPermissionLauncher.launch(CAMERA);
        }

        return result;
    }

    /**
     * Callback che gestisce la risposta dell'utente alla richiesta di autorizzazione permessi per utilizzare la fotocamera.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

        // Se l'utente ha accettato, avvio la scansione.
        if (isGranted) {
            scanQrCode();

        } else {
            // Nel caso di rifiuto della concessione dei permessi, mostro un messaggio all'utente per spiegare la necessità dei permessi.
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show();
        }
    });

}