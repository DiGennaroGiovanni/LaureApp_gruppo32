package it.uniba.dib.sms222332.guest;

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

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import it.uniba.dib.sms222332.R;

public class GuestHomeFragment extends Fragment {

    Button btnAvailableTheses, btnMyThesis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_home_student, container, false);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getResources().getString(R.string.homeToolbar));

        btnAvailableTheses = view.findViewById(R.id.allThesisBtn);
        btnMyThesis = view.findViewById(R.id.myThesisBtn);


        btnAvailableTheses.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_available_theses, 0);
        btnAvailableTheses.setOnClickListener(view1 -> seeAvailableTheses());

        btnMyThesis.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_your_thesis, 0);
        btnMyThesis.setOnClickListener(view12 ->
                Snackbar.make(view12, R.string.error_guest, Snackbar.LENGTH_SHORT).show());

        return view;
    }


    private void seeAvailableTheses() {
        Fragment guestAvailableThesisFragment = new GuestAvailableThesesListFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, guestAvailableThesisFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
