package it.uniba.dib.sms222332.student;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class AvailableThesesListFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;
    Button btnFilter;
    int seekBarValue = 30;
    boolean isRequestedExamChecked = false;
    CheckBox examsCheckbox;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.availableThesisTooolbar));

        View view = inflater.inflate(R.layout.fragment_available_theses_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layout_tesi_disponibili);
        SearchView searchView = view.findViewById(R.id.search_view);
        btnFilter = view.findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(view1 -> {


            // chiusura della tastiera
            closeKeyboard(view);

            // Istanzio l'AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            // Imposto il titolo customizzato
            TextView titleView = new TextView(requireContext());
            titleView.setText("Research a thesis by setting filters on the constraints");
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextSize(18);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setTextColor(Color.BLACK);
            titleView.setPadding(0, 50, 0, 0);
            builder.setCustomTitle(titleView);

            // Definisco il layout per l'inserimento del qr code
            LinearLayout researchLayout = new LinearLayout(requireContext());
            researchLayout.setOrientation(LinearLayout.VERTICAL);


            SeekBar seekBar = new SeekBar(requireContext());
            final TextView average = new TextView(requireContext());

            seekBar.setProgress(seekBarValue - 18);
            average.setText("Average lower than: " + seekBarValue);
            average.setTextColor(Color.BLACK);
            seekBar.setMax(12);

            int initialSeekBarValue = seekBarValue;
            boolean initialChecked = isRequestedExamChecked;

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int selectedValue = progress + 18;
                    average.setText("Average lower than: " + selectedValue);
                    seekBarValue = selectedValue;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            examsCheckbox = new CheckBox(requireContext());
            examsCheckbox.setChecked(isRequestedExamChecked);
            examsCheckbox.setText("Hide thesis with required exams");
            examsCheckbox.setOnCheckedChangeListener((compoundButton, b) -> isRequestedExamChecked = b);

            // Definisco il bottone di ricerca
            builder.setPositiveButton("Research", (dialogInterface, i) ->
            {
                searchView.setQuery("", true);
                db.collection("Tesi")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                layout_lista_tesi.removeAllViews();
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String faculty = document.getString("Faculty");

                                    if (faculty.equals(MainActivity.account.getFaculty())) {

                                        addCheckConstraint(document);

                                    }
                                }
                            }
                        });
            });

            // Aggiungo gli elementi creati al layout
            researchLayout.addView(examsCheckbox);
            researchLayout.addView(average);
            researchLayout.addView(seekBar);


            builder.setNegativeButton(R.string.close, (dialog, which) -> {
                seekBar.setProgress(initialSeekBarValue - 18);
                isRequestedExamChecked = initialChecked;
                examsCheckbox.setChecked(isRequestedExamChecked);
            });

            builder.setOnCancelListener(dialogInterface -> {
                seekBar.setProgress(initialSeekBarValue - 18);
                isRequestedExamChecked = initialChecked;
                examsCheckbox.setChecked(isRequestedExamChecked);
            });

            // Aggiungo il layout all'AlertDialog
            builder.setView(researchLayout);

            try {
                builder.create().show();
            } catch (Exception e) {
                Log.e(TAG, "Errore nell'onClick del btnResearch : " + e.toString());
            }

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

                db.collection("Tesi").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    layout_lista_tesi.removeAllViews();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String faculty = document.getString("Faculty");

                        if (faculty.equals(MainActivity.account.getFaculty())) {

                            if (document.get("Name").toString().toLowerCase().contains(newText.trim().toLowerCase())) {

                                addCheckConstraint(document);
                            }

                        }
                    }
                });

                if (newText.equals("")) {

                    db.collection("Tesi")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    layout_lista_tesi.removeAllViews();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String faculty = document.getString("Faculty");
                                        if (faculty.equals(MainActivity.account.getFaculty())) {

                                            // chiusura della tastiera
                                            closeKeyboard(view);

                                            addCheckConstraint(document);
                                        }
                                    }
                                }
                            });

                }


                return true;
            }
        });


        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String faculty = document.getString("Faculty");
                            if (faculty.equals(MainActivity.account.getFaculty())) {
                                addCardThesis(document);
                            }
                        }
                    }
                });

        return view;
    }

    private void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void addCheckConstraint(QueryDocumentSnapshot document) {
        int thesisAverage;

        if (document.getString("Average").toString().equals("")) {
            thesisAverage = 18;

        } else {
            thesisAverage = Integer.parseInt(document.getString("Average").toString());
        }

        if (thesisAverage <= seekBarValue) {

            if (examsCheckbox.isChecked() && document.getString("Required Exam").toString().equals("")) {

                addCardThesis(document);

            } else if (!examsCheckbox.isChecked()) {
                addCardThesis(document);
            }

        }
    }

    @Override
    public void onResume() {

        examsCheckbox = new CheckBox(requireContext());

        db.collection("Tesi").get().addOnSuccessListener(queryDocumentSnapshots -> {
            layout_lista_tesi.removeAllViews();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                String faculty = document.getString("Faculty");

                if (faculty.equals(MainActivity.account.getFaculty())) {

                    addCheckConstraint(document);

                }
            }
        });

        super.onResume();
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_available_thesis, null);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtTypology);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtProfessor = view.findViewById(R.id.txtProfessor);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);
        String professorEmail = document.getString("Professor");
        Button shareBtn = view.findViewById(R.id.shareBtn);
        String thesisName = document.getString("Name");

        DocumentReference docRef = db.collection("professori").document(professorEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                txtProfessor.setText(document.getString("Name") + " " + document.getString("Surname"));
            }
        });

        txtName.setText(thesisName);
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));

        if (document.getString("Correlator").equals("")) {
            txtCorrelator.setText("None");
        }

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Istanzio l'AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                // Imposto il titolo customizzato
                TextView titleView = new TextView(requireContext());
                titleView.setText(R.string.alertdialog_title);
                titleView.setGravity(Gravity.CENTER);
                titleView.setTextSize(25);
                titleView.setTypeface(null, Typeface.BOLD);
                titleView.setTextColor(Color.BLACK);
                titleView.setPadding(0, 50, 0, 0);
                builder.setCustomTitle(titleView);

                // Definisco il layout per l'inserimento del qr code
                LinearLayout qrLayout = new LinearLayout(requireContext());
                qrLayout.setOrientation(LinearLayout.VERTICAL);

                // Definisco l'ImageView che contiene il qr code generato
                ImageView qr_code_IW = new ImageView(requireContext());
                qr_code_IW.setImageBitmap(createQr(thesisName));

                // Definisco il TextView per la descrizione del qr code
                TextView qr_description = new TextView(requireContext());
                qr_description.setText(R.string.dialogalert_qr_subtitle);
                qr_description.setGravity(Gravity.CENTER);
                qr_description.setPadding(0, 0, 0, 30);

                // Definisco il bottone sotto l'ImageView
                Button buttonShare = new Button(requireContext());
                buttonShare.setText(R.string.share_thesis_info);
                buttonShare.setGravity(Gravity.CENTER);
                buttonShare.setOnClickListener(view12 -> {
                    sharePDF(thesisName);
                });

                // Imposto i parametri di layout per il bottone
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(700, 170);
                buttonParams.gravity = Gravity.CENTER;
                buttonShare.setLayoutParams(buttonParams);

                // Aggiungo gli elementi creati al layout
                qrLayout.addView(qr_code_IW);
                qrLayout.addView(qr_description);
                qrLayout.addView(buttonShare);

                builder.setNegativeButton(R.string.close, (dialog, which) -> {
                });

                // Aggiungo il layout all'AlertDialog
                builder.setView(qrLayout);

                try {
                    builder.create().show();
                } catch (Exception e) {
                    Log.e(TAG, "Errore nell'onClick del shareButton : " + e.toString());
                }
            }
        });

        layout_lista_tesi.addView(view);

        view.setOnClickListener(view1 -> {

            bundle = new Bundle();
            Fragment studentThesis = new ThesisDescriptionStudentFragment();

            Map<String, Object> datiTesi = document.getData();
            bundle.putString("correlator", (String) datiTesi.get("Correlator"));
            bundle.putString("description", (String) datiTesi.get("Description"));
            bundle.putString("estimated_time", (String) datiTesi.get("Estimated Time"));
            bundle.putString("faculty", (String) datiTesi.get("Faculty"));
            bundle.putString("name", (String) datiTesi.get("Name"));
            bundle.putString("type", (String) datiTesi.get("Type"));
            bundle.putString("related_projects", (String) datiTesi.get("Related Projects"));
            bundle.putString("average_marks", (String) datiTesi.get("Average"));
            bundle.putString("required_exams", (String) datiTesi.get("Required Exam"));
            bundle.putString("professor", txtProfessor.getText().toString());
            bundle.putString("professor_email", professorEmail);

            studentThesis.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, studentThesis);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }

    private Bitmap createQr(String name) {

        int width = 700;
        int height = 700;
        // NEW
        JSONObject jsonDatiTesi = new JSONObject();
        Bitmap bitmap = null;

        try {
            jsonDatiTesi.put("name", name);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonDatiTesi.toString(), BarcodeFormat.QR_CODE, width, height);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void sharePDF(String thesisName) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("PDF_tesi/" + thesisName + ".pdf");

        try {
            final File localFile = new File(requireContext().getExternalFilesDir(null), thesisName + ".pdf");
            fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", localFile);
                // in questa uri va il link del pdf creato e salvato nello storage
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, "Condividi PDF informazioni tesi"));
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Controllo se l'error code è riferito al fatto che il dispositivo non è connesso ad internet
                    if(e instanceof FirebaseNetworkException) {
                        Snackbar.make(requireView(), "No internet connection", Snackbar.LENGTH_LONG).show();
                    } else if(e instanceof StorageException) {
                        // Controllo se l'error code è riferito al fatto che non esiste il file sul database
                        if (((StorageException)e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Snackbar.make(requireView(), "File does not exist", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        // Stampo nella console il messaggio di errore nel caso in cui è di un altro tipo
                        Log.w("Firebas storage ERROR", e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", "Errore nel download del PDF dal database: " + e.getMessage());
        }
    }

}
