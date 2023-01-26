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

import it.uniba.dib.sms222332.R;

public class StudentHomeFragment extends Fragment {

    Button buttonAllThesis, myThesisBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.homeToolbar));

        buttonAllThesis = view.findViewById(R.id.allThesisBtn);
        myThesisBtn = view.findViewById(R.id.myThesisBtn);

        buttonAllThesis.setOnClickListener(view1 -> {

            Fragment availableThesisFragment = new AvailableThesesListFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, availableThesisFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });

        myThesisBtn.setOnClickListener(view12 -> {
            Fragment myThesis = new MyThesisFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, myThesis);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });


        return view;
    }
}
