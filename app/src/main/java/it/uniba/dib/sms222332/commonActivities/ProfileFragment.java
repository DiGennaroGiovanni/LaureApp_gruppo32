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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import it.uniba.dib.sms222332.R;


public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txtName, txtBadgeNumber, txtFaculty, txtEmail, txtBadgeTitle;
    Button btnDeleteProfile;
    String studentEmail = "";
    String nameSurname = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.profileToolbar));

        txtBadgeNumber = view.findViewById(R.id.txtBadgeNumber);
        txtName = view.findViewById(R.id.txtNameSurname);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtBadgeTitle = view.findViewById(R.id.txtBadgeTitle);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);

        txtBadgeNumber.setText(MainActivity.account.getBadgeNumber());
        nameSurname = MainActivity.account.getName() + " " + MainActivity.account.getSurname();
        txtName.setText(nameSurname);
        txtFaculty.setText(MainActivity.account.getFaculty());
        txtEmail.setText(MainActivity.account.getEmail());

        if (MainActivity.account.getBadgeNumber() == null)
            txtBadgeTitle.setVisibility(View.GONE);

        btnDeleteProfile.setOnClickListener(view1 -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_deletion);
            builder.setMessage(R.string.confirm_deletion_question);

            builder.setPositiveButton(R.string.yes, (dialog, which) -> deleteProfile());

            builder.setNegativeButton(R.string.no, (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return view;
    }

    private void deleteProfile() {

        if (MainActivity.account.getAccountType().equals("Professor")) {
            deleteProfessorProfile();
        } else {
            deleteStudentProfile();
        }


    }

    private void deleteStudentProfile() {

        DocumentReference documentReference;
        documentReference = db.collection("studenti").document(MainActivity.account.getEmail());
        documentReference.delete();

        if (MainActivity.account.getRequest().equals("yes")) {

            // eliminazione richieste dello studente
            db.collection("richieste")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String studentEmail = document.getString("Student");

                                assert studentEmail != null;
                                if (studentEmail.equals(MainActivity.account.getEmail())) {

                                    document.getReference().delete();
                                    break;
                                }
                            }
                        }
                    });

        } else if (!MainActivity.account.getRequest().equals("no")) {

            // eliminazione tesi dello studente
            db.collection("Tesi").document(MainActivity.account.getRequest())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();

                            document.getReference().update("Student", "");
                        }
                    });

            // eliminazione ricevimenti dello studente
            db.collection("ricevimenti")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String studentEmail = document.getString("Student");

                                assert studentEmail != null;
                                if (studentEmail.equals(MainActivity.account.getEmail())) {

                                    document.getReference().delete();
                                }

                            }
                        }
                    });

            // eliminazione tasks dello studente
            db.collection("tasks")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String studentEmail = document.getString("Student");

                                assert studentEmail != null;
                                if (studentEmail.equals(MainActivity.account.getEmail())) {

                                    document.getReference().delete();
                                }
                            }
                        }
                    });

        }

        // eliminazione messaggi dello studente
        db.collection("messaggi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentEmail = document.getString("Student");

                            assert studentEmail != null;
                            if (studentEmail.equals(MainActivity.account.getEmail())) {

                                document.getReference().delete();
                            }
                        }
                    }
                });


        Intent intent = new Intent(requireActivity(), ProfileDeletedActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void deleteProfessorProfile() {

        // eliminazione tesi del professore
        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");

                            assert professorEmail != null;
                            if (professorEmail.equals(MainActivity.account.getEmail())) {

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

                                // eliminazione dei ricevimenti per la tesi specifica
                                db.collection("ricevimenti")
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {

                                                for (QueryDocumentSnapshot document2 : task2.getResult()) {

                                                    if (document.getString("Name").equals(document2.getString("Thesis"))) {
                                                        document2.getReference().delete();
                                                    }

                                                }
                                            }
                                        });

                                // eliminazione dei tasks per la tesi specifica
                                db.collection("tasks")
                                        .get()
                                        .addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {

                                                for (QueryDocumentSnapshot document2 : task3.getResult()) {

                                                    if (document.getString("Name").equals(document2.getString("Thesis"))) {
                                                        document2.getReference().delete();
                                                    }

                                                }
                                            }
                                        });

                                // impostazione del campo Request dello studente tesista come stringa vuota
                                if (!document.getString("Student").isEmpty()) {

                                    db.collection("studenti")
                                            .document(document.getString("Student")).update("Request", "no");
                                }

                                document.getReference().delete();


                                // impostazione del campo Correlator a stringa vuota per le tesi per le quali
                                // il professore che sta cancellando il profilo è correlatore
                            } else if(document.getString("Correlator").equals(nameSurname)) {
                                document.getReference().update("Correlator", "");
                            }
                        }
                    }
                });

        // eliminazione messaggi del professore
        db.collection("messaggi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");

                            assert professorEmail != null;
                            if (professorEmail.equals(MainActivity.account.getEmail())) {

                                document.getReference().delete();
                            }
                        }
                    }
                });


        // eliminazione richieste del professore
        db.collection("richieste")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");
                            studentEmail = document.getString("Student");

                            assert professorEmail != null;
                            if (professorEmail.equals(MainActivity.account.getEmail())) {

                                document.getReference().delete();

                                // impostazione del campo Request dello studente su "no"
                                db.collection("studenti").document(studentEmail)
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {

                                                DocumentSnapshot documentStudent = task2.getResult();
                                                documentStudent.getReference().update("Request", "no");
                                            }
                                        });

                            }
                        }

                    }
                });

    }
}
