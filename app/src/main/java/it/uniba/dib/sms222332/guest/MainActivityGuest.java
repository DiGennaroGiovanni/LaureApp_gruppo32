package it.uniba.dib.sms222332.guest;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import androidx.fragment.app.FragmentManager;
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
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.LanguagesFragment;
import it.uniba.dib.sms222332.commonActivities.LoginActivity;
import it.uniba.dib.sms222332.commonActivities.NetworkChangeReceiver;
import it.uniba.dib.sms222332.commonActivities.ThesisDescriptionGuestFragment;
import it.uniba.dib.sms222332.professor.ProfessorHomeFragment;


import it.uniba.dib.sms222332.student.StudentHomeFragment;
import it.uniba.dib.sms222332.tools.CaptureAct;

public class MainActivityGuest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_activity_main);

        //CONTROLLO COSTANTEMENTE LA CONNESSIONE AD INTERNET
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(this), filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav.setOnItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuestHomeFragment()).commit();
        }
    }



    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {

        switch (item.getItemId()) {

            case R.id.star_button:
                messageError();
            case R.id.chat_button:
                messageError();
                break;

            case R.id.home_button:
            default:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .replace(R.id.fragment_container, new GuestHomeFragment())
                        .commit();

                selectBottomNavigationBarItem();
        }

        return true;
    };

    private void messageError() {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, R.string.error_guest , Snackbar.LENGTH_SHORT).show();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        switch (item.getItemId()) {
            case R.id.nav_language:
                selectedFragment = new LanguagesFragment();
                break;

            case R.id.nav_scan_qr:
                if(checkPermission()) scanQrCode();
                break;

            case R.id.nav_logout:
                    Intent intent = new Intent(MainActivityGuest.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
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
        } else if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ProfessorHomeFragment) && !(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof StudentHomeFragment)){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuestHomeFragment()).commit();
            bottomNav.setSelectedItemId(R.id.home_button);
        }

        else {
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

                    Bundle bundle = new Bundle();
                    Fragment guestThesis = new ThesisDescriptionGuestFragment();

                    Map<String, Object> datiTesi = document.getData();
                assert datiTesi != null;
                db.collection("professori").document(Objects.requireNonNull(datiTesi.get("Professor")).toString()).get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
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
            builder.setPositiveButton("Yes", (dialogInterface, i) -> requestPermissionLauncher.launch(CAMERA));

            // L'utente decide di non accettare l'avvio di richiesta accettazione permessi.
            builder.setNegativeButton("No", (dialogInterface, i) -> Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show());

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