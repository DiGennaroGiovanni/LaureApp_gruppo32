package it.uniba.dib.sms222332;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ThesisDescriptionFragment extends Fragment {

    TextView txtNameTitle,txtType,txtDepartment, txtTime,txtCorrelator,txtDescription,txtRelatedProjects,txtAverageMarks, txtRequiredExams;
    Button btnModify,btnDelete;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout layout_lista_file;
    ImageView qr_code_thesis;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.thesisListToolbar));

        View view = inflater.inflate(R.layout.fragment_thesis_description, container, false);

        layout_lista_file = view.findViewById(R.id.layout_lista_file);
        txtNameTitle = view.findViewById(R.id.txtNameTitle);
        txtDepartment = view.findViewById(R.id.txtDepartment);
        txtType = view.findViewById(R.id.txtTypology);
        txtTime = view.findViewById(R.id.txtTime);
        txtCorrelator = view.findViewById(R.id.txtCorrelator);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtRelatedProjects = view.findViewById(R.id.txtRelatedProjects);
        txtAverageMarks = view.findViewById(R.id.txtAverageMarks);
        txtRequiredExams = view.findViewById(R.id.txtRequiredExams);
        btnModify = view.findViewById(R.id.btnModify);
        btnDelete = view.findViewById(R.id.btnDelete);
        qr_code_thesis = view.findViewById(R.id.qr_code_thesis);

        if (getArguments() != null) {
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
            txtTime.setText(estimated_time);

            if(correlator.isEmpty())
               txtCorrelator.setText("There is no correlator");
            else
                txtCorrelator.setText(correlator);

            txtDescription.setText(description);
            txtRelatedProjects.setText(related_projects);

            showQr(name, type, faculty, estimated_time, correlator, description, related_projects, qr_code_thesis);

        }


        btnModify.setOnClickListener(view1 -> {
            Fragment modifyThesis = new ModifyThesisFragment();
            Bundle bundle = new Bundle();

            bundle.putString("name",txtNameTitle.getText().toString());
            bundle.putString("type",txtType.getText().toString());
            bundle.putString("related_projects",txtRelatedProjects.getText().toString());
            bundle.putString("department",txtDepartment.getText().toString());
            bundle.putString("time",txtTime.getText().toString());
            bundle.putString("correlator",txtCorrelator.getText().toString());
            bundle.putString("description",txtDescription.getText().toString());
            //TODO Bisogna passare i vincoli

            modifyThesis.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, modifyThesis);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        btnDelete.setOnClickListener(view12 -> {

            DocumentReference tesi = db.collection("Tesi").document(txtNameTitle.getText().toString());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Conferma eliminazione");
            builder.setMessage("Sei sicuro di voler eliminare questo elemento?");

            builder.setPositiveButton("No", (dialog, which) -> {

            });

            builder.setNegativeButton("Yes", (dialog, which) -> {

                storageRef.delete();
                tesi.delete();

                Snackbar.make(view12, "Thesis eliminated", Snackbar.LENGTH_LONG).show();

                Fragment thesisListFragment = new ThesisListFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, thesisListFragment);
                transaction.commit();

            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        //AGGIUNGO CARTE IN BASE AI DOCUMENTI CHE CI SONO
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(txtNameTitle.getText().toString());

        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<String> fileNames = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                fileNames.add(item.getName());
                String nomeFile = item.getName();
                addCard(nomeFile);
            }
            Log.d("info", "Nomi dei file: " + fileNames);
        }).addOnFailureListener(exception -> Log.w("info", "Errore nel recupero dei file.", exception));

        return view;
    }

    private void addCard(String nomeFile) {
        View view = getLayoutInflater().inflate(R.layout.card_material_without_delete, null);
        TextView nameView = view.findViewById(R.id.materialName);
        nameView.setText(nomeFile);

        layout_lista_file.addView(view);
    }

    private void showQr(String name, String type, String faculty, String estimated_time, String correlator, String description, String related_projects, ImageView qr_code_thesis) {
        // NEW
        JSONObject jsonDatiTesi = new JSONObject();

        try {
            jsonDatiTesi.put("nome", name);
            jsonDatiTesi.put("tipo", type);
            jsonDatiTesi.put("dipartimento", faculty);
            jsonDatiTesi.put("tempo_stimato", estimated_time);
            jsonDatiTesi.put("correlatore", correlator);
            jsonDatiTesi.put("descrizione", description);
            // da gestire i dati riferiti ai materiali e i vincoli
            //jsonDatiTesi.put("materiali", materials);
            //jsonDatiTesi.put("vincoli", constraints);
            jsonDatiTesi.put("progetti_correlati", related_projects);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonDatiTesi.toString(), BarcodeFormat.QR_CODE, 350, 350);
            Bitmap bitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.RGB_565);

            for (int x = 0; x < 350; x++) {
                for (int y = 0; y < 350; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qr_code_thesis.setImageBitmap(bitmap);

        } catch (WriterException | JSONException e) {
            e.printStackTrace();
        }
    }

}
