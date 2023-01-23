package it.uniba.dib.sms222332;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.tools.PDFUtility;

public class ThesisListFragment extends Fragment implements PDFUtility.OnDocumentClose{

    private static final String TAG = ThesisListFragment.class.getSimpleName();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    LinearLayout layout_lista_tesi;
    Bundle bundle;
    Button btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        layout_lista_tesi = view.findViewById(R.id.layout_lista_tesi);

        db.collection("Tesi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professorEmail = document.getString("Professor");
                            if (professorEmail.equals(mUser.getEmail())) {
                                addCardThesis(document);
                            }
                        }
                    }
                });

        return view;
    }

    private void addCardThesis(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_my_thesis, null);
        btnShare = view.findViewById(R.id.shareBtn);


        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtType = view.findViewById(R.id.txtTypology);
        TextView txtDepartment = view.findViewById(R.id.txtDepartment);
        TextView txtCorrelator = view.findViewById(R.id.txtCorrelator);
        TextView txtStudentThesis = view.findViewById(R.id.txtStudentThesis);

        txtName.setText(document.getString("Name"));
        txtType.setText(document.getString("Type"));
        txtDepartment.setText(document.getString("Faculty"));
        txtCorrelator.setText(document.getString("Correlator"));

        if (document.getString("Student").equals("")) {
            txtStudentThesis.setText("None");
        } else {
            txtStudentThesis.setText(document.getString("Student"));
        }

        if (document.getString("Correlator").equals("")) {
            txtCorrelator.setText("None");
        }

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewCardThesis) {
                sharePdf(viewCardThesis, "FILIPPO TESI", "Sperimental", "I.T.P.S.", "25", "No correlator", "Provo a scrivere una descrizione", "None");
            }
        });

        layout_lista_tesi.addView(view);

        view.setOnClickListener(view1 -> {

            bundle = new Bundle();
            Fragment thesisDescription = new ThesisDescriptionFragment();

            Map<String, Object> datiTesi = document.getData();
            //TODO BISOGNA ELIMINARE QUESTO E METTERE Required Subjects e Media voti
            bundle.putString("constraints", (String) datiTesi.get("Constraints"));

            bundle.putString("correlator", (String) datiTesi.get("Correlator"));
            bundle.putString("description", (String) datiTesi.get("Description"));
            bundle.putString("estimated_time", (String) datiTesi.get("Estimated Time"));
            bundle.putString("faculty", (String) datiTesi.get("Faculty"));
            bundle.putString("name", (String) datiTesi.get("Name"));
            bundle.putString("type", (String) datiTesi.get("Type"));
            bundle.putString("related_projects", (String) datiTesi.get("Related Projects"));

            thesisDescription.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, thesisDescription);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    private void sharePdf(View v, String name, String type, String faculty, String estimated_time, String correlator, String description, String related_projects) {

        try {

            File outputFile = new File(requireContext().getExternalFilesDir(null), "tesi.pdf");
            Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);

            Map<String, String> datiTesi = new HashMap<>();
            datiTesi.put("name", name);
            datiTesi.put("type", type);
            datiTesi.put("faculty", faculty);
            datiTesi.put("estimated_time", estimated_time);
            datiTesi.put("correlator", correlator);
            datiTesi.put("description", description);
            datiTesi.put("related_project", related_projects);

            PDFUtility.createPdf(requireContext(), ThesisListFragment.this, datiTesi, name, true, outputFile);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Condividi informazioni tesi"));

        } catch (Exception e) {
            Log.e(TAG,"Errore nella creazione del pdf");
        }
    }


    @Override
    public void onPDFDocumentClose(File file) {
        if(file.exists()) Log.e(TAG, "File pdf creato.");
    }

}
