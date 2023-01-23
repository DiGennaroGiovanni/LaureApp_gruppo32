package it.uniba.dib.sms222332;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ThesisListFragment extends Fragment {

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
                sharePdf();
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

    private void sharePdf() {
        // Crea un documento vuoto
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page1 = document.startPage(pageInfo);
        Canvas canvas = page1.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(50, 50, 30, paint);
        document.finishPage(page1);

        File outputFile = new File(requireContext().getExternalFilesDir(null), "tesi.pdf");
        Uri uri = FileProvider.getUriForFile(requireContext(), "it.uniba.dib.sms222332", outputFile);

        try {
            document.writeTo(new FileOutputStream(outputFile));
            document.close();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Condividi informazioni tesi"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
