package it.uniba.dib.sms222332.professor;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.tools.PDFUtility;

public class ThesisListFragment extends Fragment implements PDFUtility.OnDocumentClose {

    private static final String TAG = ThesisListFragment.class.getSimpleName();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;

    Button btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layoutThesisList);

        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");
                            if (professorEmail.equals(mUser.getEmail())) {
                                addCardThesis(document);
                            }
                        }
                    }
                });

        return view;
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_my_thesis, null);
        btnShare = view.findViewById(R.id.shareBtn);


        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtTypology);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);
        TextView txtStudentThesis = view.findViewById(R.id.txtStudentThesis);

        txtName.setText(document.getString("Name"));
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));

        if (document.getString("Student").equals("")) {
            txtStudentThesis.setText("None");
        } else {
            txtStudentThesis.setText(document.getString("Student"));
        }

        if (document.getString("Correlator").equals("")) {
            txtCorrelator.setText("None");
        }

        btnShare.setOnClickListener(viewCardThesis -> {

            // Istanzio l'AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            // Imposto il titolo customizzato
            TextView titleView = new TextView(requireContext());
            titleView.setText("Share thesis information");
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
            qr_code_IW.setImageBitmap(createQr("FILIPPO TESI"));

            // Definisco il TextView per la descrizione del qr code
            TextView qr_description = new TextView(requireContext());
            qr_description.setText("Scan me and show thesis in your LaureApp!");
            qr_description.setGravity(Gravity.CENTER);
            qr_description.setPadding(0, 0, 0, 30);

            // Definisco il bottone sotto l'ImageView
            Button buttonShare = new Button(requireContext());
            buttonShare.setText(R.string.share_thesis_info);
            buttonShare.setGravity(Gravity.CENTER);
            buttonShare.setOnClickListener(view12 -> {
                // successivamente il pdf verrà scaricato dal database ( in cui è già presente perché creato successivamente all'inserimento di una nuova tesi )
                sharePdf(viewCardThesis, "FILIPPO TESI", "Sperimental", "I.T.P.S.", "25", "No correlator", "Provo a scrivere una descrizione", "None");
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
        });

        layout_lista_tesi.addView(view);

        view.setOnClickListener(view1 -> {

            bundle = new Bundle();
            Fragment thesisDescription = new ThesisDescriptionFragment();

            Map<String,Object> datiTesi =  document.getData();
            bundle.putString("correlator",(String) datiTesi.get("Correlator"));
            bundle.putString("description",(String) datiTesi.get("Description"));
            bundle.putString("estimated_time",(String) datiTesi.get("Estimated Time"));
            bundle.putString("faculty",(String) datiTesi.get("Faculty"));
            bundle.putString("name",(String) datiTesi.get("Name"));
            bundle.putString("type",(String) datiTesi.get("Type"));
            bundle.putString("related_projects",(String) datiTesi.get("Related Projects"));
            bundle.putString("average_marks",(String) datiTesi.get("Average"));
            bundle.putString("required_exam",(String) datiTesi.get("Required Exam"));
            bundle.putString("student",(String) datiTesi.get("Student"));

            thesisDescription.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, thesisDescription);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    private void sharePdf(View v, String name, String type, String faculty, String estimated_time, String correlator, String description, String related_projects) {

        try {

            File outputFile = new File(requireContext().getExternalFilesDir(null), "tesi.pdf");
            Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);

            Map<String, String> datiTesi = new HashMap<>();
            datiTesi.put("name", name);
            datiTesi.put("type", type);
            datiTesi.put("faculty", faculty);
            datiTesi.put("estimated_time", estimated_time);
            datiTesi.put("correlator", correlator);
            datiTesi.put("description", description);
            datiTesi.put("related_project", related_projects);

            PDFUtility.createPdf(requireContext(), ThesisListFragment.this, datiTesi, name, true, outputFile);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Condividi informazioni tesi"));

        } catch (Exception e) {
            Log.e(TAG, "Errore nella creazione del pdf");
        }
    }


    @Override
    public void onPDFDocumentClose(File file) {
        if (file.exists()) Log.e(TAG, "File pdf creato.");
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

}
