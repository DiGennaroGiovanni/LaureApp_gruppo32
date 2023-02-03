package it.uniba.dib.sms222332.professor;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;

public class TaskListFragment extends Fragment {

    Button btnNewTask;
    TextView txtStudent, txtThesisName, txtProfessor, txtNoTask;
    String student, thesisName;
    LinearLayout taskListLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.taskListToolbar));

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        btnNewTask = view.findViewById(R.id.addTaskButton);
        txtStudent = view.findViewById(R.id.txtNomeStudente);
        txtThesisName = view.findViewById(R.id.txtNomeTesi);
        txtProfessor = view.findViewById(R.id.txtProfessor);
        txtNoTask = view.findViewById(R.id.noTasks);

        if (getArguments() != null) {

            student = getArguments().getString("student");
            thesisName = getArguments().getString("thesisName");
            txtStudent.setText(student);
            txtThesisName.setText(thesisName);

            String professor = getArguments().getString("professor");

            if (!professor.equals("")) {
                String label = getResources().getString(R.string.professor_info_message_student) ;
                txtProfessor.setText(label);
                txtStudent.setText(professor);
            }

        }

        taskListLayout = view.findViewById(R.id.taskListLayout);


        CollectionReference collectionReference = db.collection("tasks");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (Objects.equals(document.getString("Thesis"), txtThesisName.getText().toString())){
                        addTaskCard(document);
                        txtNoTask.setVisibility(View.GONE);
                    }

                }
            } else {
                Log.e("db", "Error");
            }
        });

        if (MainActivity.account.getAccountType().equals("Student"))
            btnNewTask.setVisibility(View.GONE);
         else
            btnNewTask.setOnClickListener(view1 -> newTask());

        return view;
    }

    private void newTask() {
        Fragment addTaskFragment = new NewTaskFragment();
        Bundle bundle = new Bundle();

        bundle.putString("thesisName", thesisName);
        bundle.putString("student", student);

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
        TextView txtEstimatedTime = view.findViewById(R.id.txtEstimatedTime);

        Button btnEdit = view.findViewById(R.id.btnEditThesis);
        Button btnDelete = view.findViewById(R.id.btnDeleteThesis);

        txtTaskName.setText(document.getString("Name"));
        txtDescription.setText(document.getString("Description"));
        String estTime = document.getString("Estimated Time") + " " + getResources().getString(R.string.days);
        txtEstimatedTime.setText(estTime);

        switch(Integer.parseInt(Objects.requireNonNull(document.getString("State")))){
            case 0:
                txtState.setText(R.string.not_started_task);
                break;

            case 1:
                txtState.setText(R.string.started_task);
                break;

            case 2:
                txtState.setText(R.string.completed_task);

        }

        if (MainActivity.account.getAccountType().equals("Student")) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setOnClickListener(view12 -> btnDeleteOnClick(view, txtTaskName));
        }

        btnEdit.setOnClickListener(view1 -> editTask(txtTaskName.getText().toString(), txtState.getText().toString(),
                txtDescription.getText().toString(), document.getString("Estimated Time")));

        taskListLayout.addView(view);

    }

    private void editTask(String taskName, String state, String description, String estimatedTime) {

        Bundle bundle = new Bundle();
        Fragment editTask = new EditTaskFragment();

        bundle.putString("student", student);
        bundle.putString("name", taskName);
        bundle.putString("thesis name", thesisName);
        bundle.putString("description", description);
        bundle.putString("estimated_time", estimatedTime);
        bundle.putString("state", state);


        editTask.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, editTask);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void btnDeleteOnClick(View view, TextView txtTaskName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.confirm_deletion);
        builder.setMessage(R.string.task_delete_question);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            taskListLayout.removeView(view);
            db.collection("tasks").document(txtTaskName.getText().toString()).delete();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> {});
        AlertDialog dialog = builder.create();

        dialog.show();
    }

}
