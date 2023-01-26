package it.uniba.dib.sms222332.professor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class TaskListFragment extends Fragment {

    Button addTaskButton;
    TextView txtNomeStudente, txtNomeTesi, txtProfessor;
    String studentName, thesisName;
    LinearLayout taskListLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.taskListToolbar));

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        addTaskButton = view.findViewById(R.id.addTaskButton);
        txtNomeStudente = view.findViewById(R.id.txtNomeStudente);
        txtNomeTesi = view.findViewById(R.id.txtNomeTesi);
        txtProfessor = view.findViewById(R.id.txtProfessor);

        if (getArguments() != null) {

            studentName = getArguments().getString("student");
            thesisName = getArguments().getString("thesisName");
            txtNomeStudente.setText(studentName);
            txtNomeTesi.setText(thesisName);

            String professor = getArguments().getString("professor");

            if (!professor.equals("")) {
                txtProfessor.setText("Professor: ");
                txtNomeStudente.setText(professor);
            }

        }

        taskListLayout = view.findViewById(R.id.taskListLayout);


        CollectionReference collectionReference = db.collection("tasks");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getString("Thesis").equals(txtNomeTesi.getText().toString()))
                        addTaskCard(document);
                }
            } else {

            }
        });

        if (MainActivity.account.getAccountType().equals("Student")) {
            addTaskButton.setVisibility(View.GONE);
        } else {
            addTaskButton.setOnClickListener(view1 -> {

                addTaskOnClick();

            });
        }


        return view;
    }

    private void addTaskOnClick() {
        Fragment addTaskFragment = new NewTaskFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesisName", thesisName);
        bundle.putString("student", studentName);

        addTaskFragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, addTaskFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void addTaskCard(QueryDocumentSnapshot document) {
        View view = getLayoutInflater().inflate(R.layout.card_task, null);

        TextView txtTaskName = view.findViewById(R.id.txtTaskName);
        TextView txtState = view.findViewById(R.id.txtState);
        TextView txtDescription = view.findViewById(R.id.txtDescription);
        String description = document.getString("Description");
        Button btnEdit = view.findViewById(R.id.btnModify);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        txtTaskName.setText(document.getString("Name"));
        txtState.setText(document.getString("State"));
        txtDescription.setText(description);


        taskListLayout.addView(view);


        if (MainActivity.account.getAccountType().equals("Student")) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setOnClickListener(view12 -> {

                btnDeleteOnClick(view, txtTaskName);
            });
        }

        btnEdit.setOnClickListener(view1 -> {

            btnEditOnClick(txtTaskName, txtState, description);
        });
    }

    private void btnEditOnClick(TextView txtTaskName, TextView txtState, String description) {
        Bundle bundle = new Bundle();
        Fragment editTask = new EditTaskFragment();

        bundle.putString("student", studentName);
        bundle.putString("name", txtTaskName.getText().toString());
        bundle.putString("thesis name", thesisName);
        bundle.putString("description", description);
        bundle.putString("state", txtState.getText().toString());


        editTask.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, editTask);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnDeleteOnClick(View view, TextView txtTaskName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_deletion);
        builder.setMessage(R.string.task_delete_question);
        builder.setPositiveButton(R.string.no, (dialog, which) -> {

        });
        builder.setNegativeButton(R.string.yes, (dialog, which) -> {
            taskListLayout.removeView(view);
            db.collection("tasks").document(txtTaskName.getText().toString()).delete();

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
