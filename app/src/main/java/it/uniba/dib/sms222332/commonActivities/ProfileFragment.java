package it.uniba.dib.sms222332.commonActivities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import it.uniba.dib.sms222332.R;


public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txtName, txtBadgeNumber, txtFaculty, txtEmail, txtBadgeTitle;
    Button btnDeleteProfile;
    String name, surname, badgeNumber, faculty, email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.profileToolbar));

        //TODO RIMUOVERE IL BUNDLE E PIAZZARE GLI ELEMENTI DALLA MAINACTIVIY.ACCOUNT DIRETTAMENTE NEI SETTEXT
        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
            surname = bundle.getString("surname");
            badgeNumber = bundle.getString("badge_number");
            faculty = bundle.getString("faculty");
            email = bundle.getString("email");
        }

        txtBadgeNumber = view.findViewById(R.id.txtBadgeNumber);
        txtName = view.findViewById(R.id.txtNameSurname);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtBadgeTitle = view.findViewById(R.id.txtBadgeTitle);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);

        txtBadgeNumber.setText(badgeNumber);
        String nameSurname = name + " " + surname;
        txtName.setText(nameSurname);
        txtFaculty.setText(faculty);
        txtEmail.setText(email);

        if (badgeNumber.equals(""))
            txtBadgeTitle.setVisibility(View.GONE);

        btnDeleteProfile.setOnClickListener(view1 -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_deletion);
            builder.setMessage(R.string.confirm_deletion_question);

            builder.setPositiveButton(R.string.no, (dialog, which) -> {

            });

            builder.setNegativeButton(R.string.yes, (dialog, which) -> deleteProfile());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return view;
    }

    private void deleteProfile() {
        DocumentReference documentReference;

        // controlla se il profilo si riferisce ad un professore, lo è se il badge number è vuoto
        if (MainActivity.account.getAccountType().equals("Professor")) {
            deleteProfessorProfile();
        } else {
            deleteStudentProfile();
        }


    }

    //TODO RIMUOVERE OGNI RIFERIMENTO DELLO STUDENTE. TESI ACCETTATE/RICHIESTE, MESSAGGI, RICEVIMENTI, TASK
    private void deleteStudentProfile() {
        DocumentReference documentReference;
        documentReference = db.collection("studenti").document(email);
        mUser.delete();
        documentReference.delete();

        Intent intent = new Intent(requireActivity(), ProfileDeletedActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }


    //TODO A COSA SERVONO LE PRIME DUE RIGHE INUTILIZZATE? FARE UN PO' DI PULIZIA E VERIFICARE COME PER LO STUDENTE CHE VENGA CANCELLATO OGNI RIFERIMENTO SUL DB.
    // TODO PARE CHE QUI SIANO CANCELLATE SOLO LE TESI MA NON TUTTO IL RESTO, ANCHE UNO STUDENTE CONTINUEREBBE AD AVERE LA TESI NEL PROPRIO CAMPO "REQUEST"


    private void deleteProfessorProfile() {
        DocumentReference documentReference;
        documentReference = db.collection("professori").document(email);
        // eliminazione tesi del professore
        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");

                            assert professorEmail != null;
                            if (professorEmail.equals(mUser.getEmail())) {

                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference().child(Objects.requireNonNull(document.getString("Name")));

                                storageRef.listAll()
                                        .addOnSuccessListener(listResult -> {
                                            for (StorageReference item : listResult.getItems()) {
                                                item.delete();
                                            }
                                            storageRef.delete();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error deleting folder: " + e.getMessage()));

                                document.getReference().delete();
                            }
                        }
                    }
                });
    }
}
