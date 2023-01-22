package it.uniba.dib.sms222332.professor;

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
import it.uniba.dib.sms222332.ReceiptsFragment;

public class ProfessorHomeFragment extends Fragment {

    Button buttonNuovaTesi;
    Button buttonRicevimento;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_professor, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.homeToolbar));

        buttonNuovaTesi = view.findViewById(R.id.newThesisBtn);

        buttonNuovaTesi.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.add_thesis_icon, 0);
        buttonNuovaTesi.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new NewThesisFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });


        buttonRicevimento = view.findViewById(R.id.receiptsBtn);

        buttonRicevimento.setOnClickListener(view2 -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new ReceiptsFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        return view;

    }
}
