package it.uniba.dib.sms222332.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import it.uniba.dib.sms222332.R;

public class StudentHomeFragment extends Fragment {

    Button btnAvailableTheses, btnMyThesis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.homeToolbar));

        btnAvailableTheses = view.findViewById(R.id.allThesisBtn);
        btnMyThesis = view.findViewById(R.id.myThesisBtn);

        btnAvailableTheses.setOnClickListener(view1 -> seeAvailableTheses());
        btnMyThesis.setOnClickListener(view12 -> getToMyThesis());

        return view;
    }

    private void getToMyThesis() {
        Fragment myThesis = new MyThesisFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, myThesis);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void seeAvailableTheses() {
        Fragment availableThesisFragment = new AvailableThesesListFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, availableThesisFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
