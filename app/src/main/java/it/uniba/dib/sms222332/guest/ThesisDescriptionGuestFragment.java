package it.uniba.dib.sms222332.guest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import it.uniba.dib.sms222332.R;

public class ThesisDescriptionGuestFragment extends Fragment {

    TextView txtNameTitle, txtType, txtDepartment,  txtCorrelator, txtProfessor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(R.string.thesis_info);

        View view = inflater.inflate(R.layout.fragment_thesis_description_guest, container, false);

        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtType = view.findViewById(R.id.txtTypology);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtProfessor = view.findViewById(R.id.txtProfessor);

        if (getArguments() != null) {
            getDataFromPreviousFragment();

        }

        return view;
    }

    private void getDataFromPreviousFragment() {
        assert getArguments() != null;

        String name = getArguments().getString("name");
        String type = getArguments().getString("type");
        String professor = getArguments().getString("professor");
        String correlator = getArguments().getString("correlator");
        String faculty = getArguments().getString("faculty");

        txtNameTitle.setText(name);
        txtType.setText(type);
        txtDepartment.setText(faculty);
        txtProfessor.setText(professor);

        if (correlator.isEmpty())
            txtCorrelator.setText(R.string.none);
        else
            txtCorrelator.setText(correlator);
    }
}