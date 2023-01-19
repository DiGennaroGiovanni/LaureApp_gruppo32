package it.uniba.dib.sms222332;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.Logger;


public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txtName, txtBadgeNumber, txtFaculty, txtEmail;
    Button btnDeleteProfile;
    String name, surname, badgeNumber, faculty, email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.profileToolbar));

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
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);

        txtBadgeNumber.setText(badgeNumber);
        String nameSurname = name +" "+ surname;
        txtName.setText(nameSurname);
        txtFaculty.setText(faculty);
        txtEmail.setText(email);


        //TODO Confermare scelta e rimandare ad activity conclusiva
        btnDeleteProfile.setOnClickListener(view1 -> {

            DocumentReference documentReference;

            if (badgeNumber.equals(""))
                documentReference = db.collection("professori").document(email);
            else
                documentReference = db.collection("studenti").document(email);

            mUser.delete();
            documentReference.delete();

            Intent intent = new Intent(getActivity(), LogoutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        return view;
    }
}
