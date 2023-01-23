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

public class TaskListFragment extends Fragment {

    Button addTaskButton;
    TextView txtNomeStudente, txtNomeTesi;
    String studentName,thesisName;
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

        if (getArguments() != null) {

            studentName = getArguments().getString("student");
            thesisName = getArguments().getString("thesisName");
            txtNomeStudente.setText(studentName);
            txtNomeTesi.setText(thesisName);

        }

        taskListLayout = view.findViewById(R.id.taskListLayout);


        CollectionReference collectionReference = db.collection("tasks");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Thesis").equals(txtNomeTesi.getText().toString()))
                        addTaskCard(document);
                }
            } else {

            }
        });


        addTaskButton.setOnClickListener(view1 -> {

            Fragment addTaskFragment = new NewTaskFragment();
            Bundle bundle = new Bundle();

            bundle.putString("thesisName",thesisName);
            bundle.putString("student",studentName);

            addTaskFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, addTaskFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        });


        return view;
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


        btnDelete.setOnClickListener(view12 -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Conferma eliminazione");
            builder.setMessage("Sei sicuro di voler questo task?");
            builder.setPositiveButton("No", (dialog, which) -> {

            });
            builder.setNegativeButton("Yes", (dialog, which) -> {
                taskListLayout.removeView(view);
                db.collection("tasks").document(txtTaskName.getText().toString()).delete();

            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });



        btnEdit.setOnClickListener(view1 -> {

            Bundle bundle = new Bundle();
            Fragment editTask = new EditTaskFragment();

            bundle.putString("student",studentName);
            bundle.putString("name",txtTaskName.getText().toString());
            bundle.putString("thesis name",thesisName);
            bundle.putString("description",description);
            bundle.putString("state",txtState.getText().toString());


            editTask.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, editTask);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

}
