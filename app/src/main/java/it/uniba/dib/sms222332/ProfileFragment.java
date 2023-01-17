package it.uniba.dib.sms222332;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txtName, txtBadgeNumber, txtFaculty;
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
        email=MainActivity.account.getEmail();

        txtBadgeNumber = view.findViewById(R.id.txtBadgeNumber);
        txtName = view.findViewById(R.id.txtNameSurname);
        txtFaculty = view.findViewById(R.id.txtFaculty);
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);

        txtBadgeNumber.setText(badgeNumber);
        txtName.setText(name +" "+ surname);
        txtFaculty.setText(faculty);

//        String userEmailId = mAuth.getCurrentUser().getEmail();
//        DocumentReference documentReference = db.collection("studenti").document(userEmailId);

        //TODO Confermare scelta e rimandare ad activity conclusiva
//        btnEliminaProfilo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                documentReference.delete();
//                mUser.delete();
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });
        return view;
    }
}
