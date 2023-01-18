package it.uniba.dib.sms222332;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class ModifyThesisFragment extends Fragment {

    EditText edtTime,edtCorrelator,edtDescription,edtRelatedProjects;
    TextView txtDepartment,txtNameTitle,txtType;
    Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.modifyThesisToolbar));
        //TODO INSERIRE NOME PAGINA

        View view = inflater.inflate(R.layout.fragment_modify_thesis, container, false);

        txtType = view.findViewById(R.id.txtType);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        edtTime = view.findViewById(R.id.edtTime);
        edtCorrelator = view.findViewById(R.id.edtCorrelator);
        edtDescription = view.findViewById(R.id.edtDescription);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        edtRelatedProjects = view.findViewById(R.id.edtRelatedProjects);
        btnSave = view.findViewById(R.id.btnSave);

        if (getArguments() != null) {
            //String constraints = getArguments().getString("constraints"); //TODO ATTUALMENTE NON UTILIZZATO
            String correlator = getArguments().getString("correlator");
            String description = getArguments().getString("description");
            String estimated_time = getArguments().getString("estimated_time");
            String faculty = getArguments().getString("faculty");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type");
            String related_projects = getArguments().getString("related_projects");

            txtNameTitle.setText(name);
            txtType.setText(type);
            txtDepartment.setText(faculty);
            edtTime.setText(estimated_time);
            edtCorrelator.setText(correlator);
            edtDescription.setText(description);
            edtRelatedProjects.setText(related_projects);
            //TODO AGGIUNGERE OPZIONE PER LA MODIFICA DEI VINCOLI
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }
}
