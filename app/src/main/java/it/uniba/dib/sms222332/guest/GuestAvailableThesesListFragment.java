package it.uniba.dib.sms222332.guest;

import static android.Manifest.permission.CAMERA;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.student.MyThesisFragment;
import it.uniba.dib.sms222332.tools.CaptureAct;

public class GuestAvailableThesesListFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;
    Button btnFilter, btnCamera;
    int seekBarValue = 30;
    boolean isRequestedExamChecked = false;
    CheckBox examsCheckbox;
    SeekBar seekBar;
    SearchView searchView;
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
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    String student = document.getString("Student");

                    assert student != null;
                    if (student.equals(onlineUser)) {
                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyThesisFragment()).commit();
                    } else {

                        bundle = new Bundle();
                        Fragment guestThesis = new ThesisDescriptionGuestFragment();

                        Map<String, Object> datiTesi = document.getData();
                        assert datiTesi != null;
                        db.collection("professori").document(Objects.requireNonNull(datiTesi.get("Professor")).toString()).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                bundle.putString("professor", Objects.requireNonNull(task1.getResult().get("Name")) + " " + Objects.requireNonNull(task1.getResult().get("Surname")));
                                bundle.putString("correlator", (String) datiTesi.get("Correlator"));
                                bundle.putString("faculty", (String) datiTesi.get("Faculty"));
                                bundle.putString("name", (String) datiTesi.get("Name"));
                                bundle.putString("type", (String) datiTesi.get("Type"));

                                guestThesis.setArguments(bundle);
                                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, guestThesis);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        });
                    }
                }
            });
        }
    });
    /**
     * Callback che gestisce la risposta dell'utente alla richiesta di autorizzazione permessi per utilizzare la fotocamera.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

        // Se l'utente ha accettato, avvio la scansione.
        if (isGranted) {
            scanQrCode();

        } else {
            // Nel caso di rifiuto della concessione dei permessi, mostro un messaggio all'utente per spiegare la necessità dei permessi.
            Snackbar.make(requireView(), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_available_theses_list, container, false);
        examsCheckbox = new CheckBox(requireContext());
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layout_tesi_disponibili);
        searchView = view.findViewById(R.id.search_view);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnCamera = view.findViewById(R.id.btnCamera);

        if (savedInstanceState != null) {
            seekBarValue = savedInstanceState.getInt("seekbar");
            isRequestedExamChecked = savedInstanceState.getBoolean("checkbox");
            examsCheckbox.setChecked(isRequestedExamChecked);
        }

        btnFilter.setOnClickListener(view1 -> {

            // chiusura della tastiera
            closeKeyboard(view);

            final Dialog dialogFilter = new Dialog(requireContext());
            dialogFilter.setContentView(R.layout.dialog_filter);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                dialogFilter.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            seekBar = dialogFilter.findViewById(R.id.seekbar_average);
            final TextView average = dialogFilter.findViewById(R.id.average_textview);


            seekBar.setProgress(seekBarValue - 18);
            String avgString = getResources().getString(R.string.max_avg_constr) + seekBarValue;
            average.setText(avgString);
            seekBar.setMax(12);

            int initialSeekBarValue = seekBarValue;
            boolean initialChecked = isRequestedExamChecked;

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int selectedValue = progress + 18;
                    String avgString = getResources().getString(R.string.max_avg_constr) + selectedValue;
                    average.setText(avgString);
                    seekBarValue = selectedValue;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            examsCheckbox = dialogFilter.findViewById(R.id.checkbox_hide_thesis);
            examsCheckbox.setChecked(isRequestedExamChecked);
            examsCheckbox.setOnCheckedChangeListener((compoundButton, b) -> isRequestedExamChecked = b);

            Button searchButton = dialogFilter.findViewById(R.id.search_button);
            searchButton.setOnClickListener(view22 -> {


                searchView.setQuery("", true);

                db.collection("Tesi")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                layout_lista_tesi.removeAllViews();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    addCheckConstraint(document);
                                }
                                dialogFilter.dismiss();
                            }
                        });
            });

            Button dismissButtonFilter = dialogFilter.findViewById(R.id.dismiss_button);
            dismissButtonFilter.setOnClickListener(view2 -> {
                seekBar.setProgress(initialSeekBarValue - 18);
                isRequestedExamChecked = initialChecked;
                examsCheckbox.setChecked(isRequestedExamChecked);
                dialogFilter.dismiss();
            });

            dialogFilter.setOnCancelListener(dialogInterface -> {
                seekBar.setProgress(initialSeekBarValue - 18);
                isRequestedExamChecked = initialChecked;
                examsCheckbox.setChecked(isRequestedExamChecked);
            });

            try {
                dialogFilter.show();
            } catch (Exception e) {
                Log.e(TAG, "Errore nell'onClick del shareButton : " + e);
            }

        });

        btnCamera.setOnClickListener(view12 -> {
            if (checkPermission()) scanQrCode();
        });

        /*
        Creazione query per la ricerca all'interno del database del nome di una specifica tesi.
        La ricerca non è case sensitive e permette di ottenere risultati anche cercando una specifica
        parola del titolo della tesi. La ricerca viene effettuata solo per le tesi del dipartimento
        di cui fa parte lo studente.
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.equals("")) {
                    db.collection("Tesi").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        layout_lista_tesi.removeAllViews();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                            addCheckConstraint(document);


                        closeKeyboard(view);
                    });
                } else {
                    db.collection("Tesi").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        layout_lista_tesi.removeAllViews();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (Objects.requireNonNull(document.get("Name")).toString().toLowerCase().contains(newText.trim().toLowerCase())) {
                                addCheckConstraint(document);
                            }
                        }
                    });
                }
                return true;
            }
        });

        if (savedInstanceState != null)
            searchView.setQuery(savedInstanceState.getString("search"), true);


        db.collection("Tesi").get().addOnSuccessListener(queryDocumentSnapshots -> {
            layout_lista_tesi.removeAllViews();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                addCheckConstraint(document);


        });

        return view;
    }

    private void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void addCheckConstraint(QueryDocumentSnapshot document) {
        int thesisAverage;

        if (Objects.equals(document.getString("Average"), "")) {
            thesisAverage = 18;
        } else {
            thesisAverage = Integer.parseInt(Objects.requireNonNull(document.getString("Average")));
        }

        if (thesisAverage <= seekBarValue) {

            if (examsCheckbox.isChecked() && Objects.equals(document.getString("Required Exam"), "")) {
                addCardThesis(document);
            } else if (!examsCheckbox.isChecked()) {
                addCardThesis(document);
            }
        }
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.card_available_thesis, null);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtTypology);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtProfessorEmail = view.findViewById(R.id.txtProfessor);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);
        Button shareBtn = view.findViewById(R.id.shareBtn);
        Button btnStar = view.findViewById(R.id.btnStar);

        String professorEmail = document.getString("Professor");
        String thesisName = document.getString("Name");


        txtName.setText(thesisName);
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));
        txtProfessorEmail.setText(professorEmail);


        if (Objects.equals(document.getString("Correlator"), "")) {
            txtCorrelator.setText(R.string.none);
        }

        btnStar.setOnClickListener(viewStar -> Snackbar.make(viewStar, R.string.error_guest, Snackbar.LENGTH_LONG).show());

        shareBtn.setOnClickListener(view13 -> Snackbar.make(view13, R.string.error_guest, Snackbar.LENGTH_LONG).show());

        view.setOnClickListener(view1 -> Snackbar.make(view1, R.string.error_guest, Snackbar.LENGTH_LONG).show());

        layout_lista_tesi.addView(view);
    }

    private void scanQrCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on ");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Il permesso di utilizzare la fotocamera risulta concesso.
            result = true;

            // Mostro un messaggio all'utente in cui spiego il motivo per il quale sono necessari i permessi.
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.snackbar_title_camera_permission);
            builder.setMessage(getString(R.string.snackbar_camera_permission_message) + "\n\n" + getString(R.string.snackbar_camera_permission_message2));

            // L'utente accetta di concedere i permessi e avvio richiesta accettazione permessi.
            builder.setPositiveButton("Yes", (dialogInterface, i) -> requestPermissionLauncher.launch(CAMERA));

            // L'utente decide di non accettare l'avvio di richiesta accettazione permessi.
            builder.setNegativeButton("No", (dialogInterface, i) -> Snackbar.make(requireView(), R.string.snackbar_deny_camera_message, Snackbar.LENGTH_LONG).show());

            AlertDialog dialog = builder.create();
            dialog.show();  // Avvio la visualizzazione dell'AlertDialog.
        } else {
            // Avvio la procedura di autorizzazione dei permessi.
            requestPermissionLauncher.launch(CAMERA);
        }
        return result;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("search", searchView.getQuery().toString());
        outState.putBoolean("checkbox", examsCheckbox.isChecked());
        if (seekBar != null)
            outState.putInt("seekbar", seekBar.getProgress() + 18);
    }
}