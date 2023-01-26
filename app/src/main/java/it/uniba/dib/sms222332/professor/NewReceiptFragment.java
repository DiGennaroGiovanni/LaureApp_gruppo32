package it.uniba.dib.sms222332.professor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222332.R;

public class NewReceiptFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout allTasks;
    ArrayList<String> addressedTasks = new ArrayList<>();
    private TextView txtReceiptDate, txtStartTime, txtEndTime;
    private EditText edtDescription;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_receipt, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.newReceiptToolbar));


        TextView txtStudent = view.findViewById(R.id.txtStudentEmail);
        TextView txtThesisName = view.findViewById(R.id.txtThesisName);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String student = bundle.getString("student");
            String thesisName = bundle.getString("thesis_name");
            txtStudent.setText(student);
            txtThesisName.setText(thesisName);
        }


        txtReceiptDate = view.findViewById(R.id.txtReceiptDateCard);
        txtStartTime = view.findViewById(R.id.txtReceiptStartTimeCard);
        txtEndTime = view.findViewById(R.id.txtReceiptEndTime);
        txtReceiptDate.setPaintFlags(txtReceiptDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtStartTime.setPaintFlags(txtStartTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtEndTime.setPaintFlags(txtEndTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        edtDescription = view.findViewById(R.id.edtDescription);
        allTasks = view.findViewById(R.id.layoutAddressedTasks);
        Button addReceipt = view.findViewById(R.id.btnAddReceipt);


        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        txtReceiptDate.setOnClickListener(view1 -> {

            txtReceiptDateOnClick(calendar, year, month, day, hour, minute);

        });


        db.collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (txtThesisName.getText().toString().equals(document.getString("Thesis")))
                                addTask(document.getString("Name"));

                        }
                    }
                });

        addReceipt.setOnClickListener(view12 -> {

            addReceiptOnClick(txtStudent, txtThesisName, view12);

        });


        return view;
    }

    private void txtReceiptDateOnClick(Calendar calendar, int year, int month, int day, int hour, int minute) {
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (datePicker, i, i1, i2) -> {

            String correctDay, correctMonth;

            i1 = i1 + 1;

            if (i2 < 10) correctDay = "0" + i2;
            else correctDay = String.valueOf(i2);

            if (i1 < 10) correctMonth = "0" + i1;
            else correctMonth = String.valueOf(i1);


            String date = correctDay + "/" + correctMonth + "/" + i;
            txtReceiptDate.setText(date);
            txtReceiptDate.setTextColor(Color.BLACK);

            if (!txtStartTime.hasOnClickListeners() && !txtEndTime.hasOnClickListeners()) {
                txtStartTime.setOnClickListener(view2 -> {


                    TimePickerDialog startTimeDialog = new TimePickerDialog(getActivity(), (timePicker, i3, i11) -> {


                        String correctHour, correctMinute;

                        if (i3 < 10) correctHour = "0" + i3;
                        else correctHour = String.valueOf(i3);

                        if (i11 < 10) correctMinute = "0" + i11;
                        else correctMinute = String.valueOf(i11);

                        String startTime = correctHour + ":" + correctMinute;

                        txtStartTime.setTextColor(Color.BLACK);
                        txtStartTime.setText(startTime);
                    }, hour, minute, true);

                    startTimeDialog.show();
                });

                txtEndTime.setOnClickListener(view3 -> {


                    TimePickerDialog endTimeDialog = new TimePickerDialog(getActivity(), (timePicker, i3, i11) -> {

                        String correctHour, correctMinute;

                        if (i3 < 10) correctHour = "0" + i3;
                        else correctHour = String.valueOf(i3);

                        if (i11 < 10) correctMinute = "0" + i11;
                        else correctMinute = String.valueOf(i11);

                        String endTime = correctHour + ":" + correctMinute;
                        txtEndTime.setTextColor(Color.BLACK);
                        txtEndTime.setText(endTime);
                    }, hour, minute, true);

                    endTimeDialog.show();
                });

            }


        }, year, month, day);
        dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dialog.show();
    }

    private void addReceiptOnClick(TextView txtStudent, TextView txtThesisName, View view12) {
        String data = txtReceiptDate.getText().toString();
        String startTime = txtStartTime.getText().toString();
        String endTime = txtEndTime.getText().toString();
        String description = edtDescription.getText().toString();

        if (data.equals(getResources().getString(R.string.select_date)))
            txtReceiptDate.setError(getString(R.string.insert_date));
        else if (startTime.equals(getResources().getString(R.string.select_start_time)))
            txtStartTime.setError(getString(R.string.start_time));
        else if (endTime.equals(getResources().getString(R.string.select_end_time)))
            txtEndTime.setError(getString(R.string.end_time));
        else if (description.isEmpty())
            edtDescription.setError(getString(R.string.describe_receipt));

        else {
            Map<String, Object> infoReceipt = new HashMap<>();
            infoReceipt.put("Thesis", txtThesisName.getText().toString());
            infoReceipt.put("Student", txtStudent.getText().toString());
            infoReceipt.put("Date", data);
            infoReceipt.put("Start Time", startTime);
            infoReceipt.put("End Time", endTime);
            infoReceipt.put("Description", description);
            if (addressedTasks.isEmpty())
                infoReceipt.put("Tasks", "");
            else
                infoReceipt.put("Tasks", addressedTasks);


            long time = System.currentTimeMillis();
            String title = txtThesisName.getText().toString() + ":" + time;
            db.collection("ricevimenti").document(title).set(infoReceipt);

            // chiusura della tastiera quando viene effettuato un cambio di fragment
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);

            Snackbar.make(view12, R.string.receipt_added, Snackbar.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();

        }
    }

    public void addTask(String taskName) {

        View task = getLayoutInflater().inflate(R.layout.selectable_item_task, null);
        TextView name = task.findViewById(R.id.txtTaskName);
        name.setText(taskName);

        name.setOnClickListener(view -> {

            if (addressedTasks.contains(taskName)) {
                addressedTasks.remove(taskName);
                name.setTextColor(Color.parseColor("#B308275A"));
                Drawable newShape = ContextCompat.getDrawable(getActivity(), R.drawable.item_task_background);
                name.setBackground(newShape);

            } else {
                addressedTasks.add(taskName);
                name.setTextColor(Color.WHITE);
                Drawable newShape = ContextCompat.getDrawable(getActivity(), R.drawable.item_task_background_selected);
                name.setBackground(newShape);
            }

        });

        allTasks.addView(task);

    }


}
