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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.profileToolbar));

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
            txtBadgeTitle.setVisibility(View.INVISIBLE);

        //TODO Confermare scelta e rimandare ad activity conclusiva
        btnDeleteProfile.setOnClickListener(view1 -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_deletion);
            builder.setMessage(R.string.confirm_deletion_question);

            builder.setPositiveButton(R.string.no, (dialog, which) -> {

            });

            builder.setNegativeButton(R.string.yes, (dialog, which) -> {
                deleteProfile();

            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return view;
    }

    private void deleteProfile() {
        DocumentReference documentReference;

        // controlla se il profilo si riferisce ad un professore, lo è se il badge number è vuoto
        if (badgeNumber.equals("")) {
            documentReference = deleteProfessorProfile();
        } else
            documentReference = db.collection("studenti").document(email);

        mUser.delete();
        documentReference.delete();

        Intent intent = new Intent(getActivity(), ProfileDeletedActivity.class);
        getActivity().finish();
        startActivity(intent);
    }

    @NonNull
    private DocumentReference deleteProfessorProfile() {
        DocumentReference documentReference;
        documentReference = db.collection("professori").document(email);
        // eliminazione tesi del professore
        db.collection("Tesi")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String professorEmail = document.getString("Professor");

                                if (professorEmail.equals(mUser.getEmail())) {

                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference().child(document.getString("Name"));

                                    storageRef.listAll()
                                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                @Override
                                                public void onSuccess(ListResult listResult) {
                                                    for (StorageReference item : listResult.getItems()) {
                                                        item.delete();
                                                    }
                                                    storageRef.delete();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Error deleting folder: " + e.getMessage());
                                                }
                                            });

                                    document.getReference().delete();
                                }
                            }
                        }
                    }
                });
        return documentReference;
    }
}
