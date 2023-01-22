package it.uniba.dib.sms222332;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class NewReceiptFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String student, thesisName;
    private TextView txtStudent, txtThesisName;
    LinearLayout allTasks;
    ArrayList<String> addressedTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_receipt, container, false);

        txtStudent = view.findViewById(R.id.txtStudentNameSurname);
        txtThesisName = view.findViewById(R.id.txtThesisName);

        Bundle bundle = getArguments();
        if (bundle != null){
            student = bundle.getString("student");
            thesisName = bundle.getString("thesis_name");
            txtStudent.setText(student);
            txtThesisName.setText(thesisName);
        }

        allTasks = view.findViewById(R.id.layoutAddressedTasks);
        Button addReceipt = view.findViewById(R.id.btnAddReceipt);//TODO GESTIRE IL SALVATAGGIO SU DB



        db.collection("task")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(thesisName.equals(document.getString("Thesis")))
                                addTask(document.getString("Name"));

                        }
                    }
                });


        return view;
    }

    public void addTask(String taskName){

        View task = getLayoutInflater().inflate(R.layout.selectable_item_task, null);
        TextView name = task.findViewById(R.id.txtTaskName);
        name.setText(taskName);

        name.setOnClickListener(view -> {


            if (addressedTasks.contains(taskName)){
                addressedTasks.remove(taskName);
                name.setTextColor(Color.parseColor("#B308275A"));
                Drawable newShape = ContextCompat.getDrawable(getActivity(), R.drawable.item_task_background);
                name.setBackground(newShape);

            }
            else {
                addressedTasks.add(taskName);
                name.setTextColor(Color.WHITE);
                Drawable newShape = ContextCompat.getDrawable(getActivity(), R.drawable.item_task_background_selected);
                name.setBackground(newShape);
            }
//#023FA6
        });

        allTasks.addView(task);

    }

}
