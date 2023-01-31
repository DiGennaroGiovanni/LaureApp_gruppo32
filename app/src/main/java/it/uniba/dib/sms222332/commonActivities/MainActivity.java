package it.uniba.dib.sms222332.commonActivities;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.professor.ProfessorAccount;
import it.uniba.dib.sms222332.professor.ProfessorHomeFragment;
import it.uniba.dib.sms222332.professor.ThesesListFragment;
import it.uniba.dib.sms222332.student.FavoritesFragment;
import it.uniba.dib.sms222332.student.MyThesisFragment;
import it.uniba.dib.sms222332.student.StudentAccount;
import it.uniba.dib.sms222332.student.StudentHomeFragment;
import it.uniba.dib.sms222332.commonActivities.Messages.ThesesMessagesListFragment;
import it.uniba.dib.sms222332.student.ThesisDescriptionGuestFragment;
import it.uniba.dib.sms222332.tools.CaptureAct;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static Account account;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private Menu nav_Menu;
    private NavigationView navigationView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;

        switch (item.getItemId()) {

            // ??
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


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        selectBottomNavigationBarItem();
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                nav_Menu.findItem(R.id.nav_scan_qr).setVisible(true);
                break;

            case "Professor":
                bottomNav.inflateMenu(R.menu.bottom_navigation_prof);
                nav_Menu.findItem(R.id.nav_scan_qr).setVisible(false);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        switch (item.getItemId()) {
            case R.id.nav_profile:
                selectedFragment = profileBundle(account);
                break;

            case R.id.nav_language:
                selectedFragment = new LanguagesFragment();
                break;

            case R.id.nav_scan_qr:
                if(checkPermission()) scanQrCode();
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
        fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment profileBundle(Account account) {
        Bundle bundle = new Bundle();
        bundle.putString("name", account.getName());
        bundle.putString("surname", account.getSurname());
        bundle.putString("email", account.getEmail());
        bundle.putString("faculty", account.getFaculty());

        if (account.getAccountType().equals("Student"))
            bundle.putString("badge_number", account.getBadgeNumber());
        else
            bundle.putString("badge_number", "");

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);

        return profileFragment;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ProfessorHomeFragment) && !(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof StudentHomeFragment))
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getProperHome()).commit();

        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_app_confirm);
            builder.setMessage(R.string.exit_app_question);

            builder.setNegativeButton(R.string.no, (dialog, which) -> {

            });

            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                super.onBackPressed();
            });

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

    private void scanQrCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on ");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result ->  {
        if(result.getContents() != null) {
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
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    String student = document.getString("Student");

                    if (student.equals(onlineUser)) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyThesisFragment()).commit();
                    } else {

                        Bundle bundle = new Bundle();
                        Fragment guestThesis = new ThesisDescriptionGuestFragment();

                        Map<String, Object> datiTesi = document.getData();
                        db.collection("professori").document(datiTesi.get("Professor").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    bundle.putString("professor", task.getResult().get("Name").toString() + " " + task.getResult().get("Surname").toString());
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
                            }
                        });
                    }
                }
            });
        }
    });

    /**
     * checkPermission è il metodo che gestisce i permessi per utilizzare la fotocamera.
     *
     * Nel caso in cui l'utente non fornisce l'autorizzazioen per utilizzare la fotocamera, il sistemare
     * provvederà a fornire un feedback all'utente per spiegare l'utilità dei permessi.
     *
     * Nel
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
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    requestPermissionLauncher.launch(CAMERA);
                }
            });

            // L'utente decide di non accettare l'avvio di richiesta accettazione permessi.
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();  // Avvio la visualizzazione dell'AlertDialog.
        }
        else {
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