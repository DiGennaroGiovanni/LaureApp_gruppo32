package it.uniba.dib.sms222332;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class NewReceiptFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout allTasks;
    ArrayList<String> addressedTasks = new ArrayList<>();
    private TextView txtReceiptDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_receipt, container, false);

        TextView txtStudent = view.findViewById(R.id.txtStudentNameSurname);
        TextView txtThesisName = view.findViewById(R.id.txtThesisName);

        txtReceiptDate = view.findViewById(R.id.txtReceiptDate);
        txtReceiptDate.setPaintFlags(txtReceiptDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        txtReceiptDate.setOnClickListener(view1 -> {

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (datePicker, i, i1, i2) -> {

                i1 = i1 + 1;
                String date = i2 + "/"+i1+"/"+year;
                txtReceiptDate.setText(date);
                txtReceiptDate.setTextColor(Color.BLACK);
            }, year, month, day);
            dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            dialog.show();
        });

        Bundle bundle = getArguments();
        if (bundle != null){
            String student = bundle.getString("student");
            String thesisName = bundle.getString("thesis_name");
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
                            if(txtThesisName.getText().toString().equals(document.getString("Thesis")))
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
