package it.uniba.dib.sms222332.professor;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.tools.QrGenerator;

public class ThesesListFragment extends Fragment {

    private static final String TAG = ThesesListFragment.class.getSimpleName();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    LinearLayout layout_lista_tesi;
    Bundle bundle;

    Button btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_list, container, false);

        layout_lista_tesi = view.findViewById(R.id.layoutThesisList);

        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");
                            String correlatorEmail = document.getString("Correlator");
                            String myName = MainActivity.account.getName() + " " + MainActivity.account.getSurname();
                            if (Objects.equals(professorEmail, MainActivity.account.getEmail()) || Objects.equals(correlatorEmail, myName)) {
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

        String thesisName = document.getString("Name");
        txtName.setText(thesisName);
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));

        if (Objects.equals(document.getString("Student"), ""))
            txtStudentThesis.setText(R.string.none);
        else
            txtStudentThesis.setText(document.getString("Student"));


        if (Objects.equals(document.getString("Correlator"), ""))
            txtCorrelator.setText(R.string.none);


        btnShare.setOnClickListener(viewCardThesis -> btnShareOnClick(thesisName));

        layout_lista_tesi.addView(view);

        view.setOnClickListener(view1 -> {

            bundle = new Bundle();
            Fragment thesisDescription = new ThesisDescriptionProfessorFragment();

            Map<String, Object> datiTesi = document.getData();
            bundle.putString("correlator", (String) datiTesi.get("Correlator"));
            bundle.putString("description", (String) datiTesi.get("Description"));
            bundle.putString("estimated_time", (String) datiTesi.get("Estimated Time"));
            bundle.putString("faculty", (String) datiTesi.get("Faculty"));
            bundle.putString("name", (String) datiTesi.get("Name"));
            bundle.putString("type", (String) datiTesi.get("Type"));
            bundle.putString("related_projects", (String) datiTesi.get("Related Projects"));
            bundle.putString("average_marks", (String) datiTesi.get("Average"));
            bundle.putString("required_exam", (String) datiTesi.get("Required Exam"));
            bundle.putString("student", (String) datiTesi.get("Student"));
            bundle.putString("professor", (String) datiTesi.get("Professor"));

            thesisDescription.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, thesisDescription);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    private void btnShareOnClick(String thesisName) {

        final Dialog dialogQr = new Dialog(requireContext());
        dialogQr.setContentView(R.layout.dialog_qr);

        ImageView qrImageView = dialogQr.findViewById(R.id.qr_image);
        qrImageView.setImageBitmap(QrGenerator.createQr(thesisName));

        Button buttonShare = dialogQr.findViewById(R.id.share_button);
        buttonShare.setOnClickListener(view12 -> {
            sharePDF(thesisName);
        });

        Button dismissButton = dialogQr.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(view14 -> dialogQr.dismiss());

        try {
            dialogQr.show();
        } catch (Exception e) {
            Log.e(TAG, "Errore nell'onClick del shareButton : " + e);
        }
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
                /*shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(localFile));*/
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, "Condividi PDF informazioni tesi"));
            }).addOnFailureListener(e -> {

                // Controllo se l'error code è riferito al fatto che il dispositivo non è connesso ad internet
                if (e instanceof FirebaseNetworkException) {
                    Snackbar.make(requireView(), R.string.no_internet, Snackbar.LENGTH_LONG).show();
                } else if (e instanceof StorageException) {

                    // Controllo se l'error code è riferito al fatto che non esiste il file sul database
                    if (((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Snackbar.make(requireView(), R.string.file_doesnt_exist, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    // Stampo nella console il messaggio di errore nel caso in cui è di un altro tipo
                    Log.w(getString(R.string.firebase_error), e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", getString(R.string.error_downloading_pdf) + e.getMessage());
        }
    }
}
