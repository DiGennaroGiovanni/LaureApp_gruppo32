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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;


public class ReceiptsListFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button btnNewReceipt;
    LinearLayout listView;
    private TextView txtThesisName, txtStudent, txtNoReceipt;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts_list, container, false);

        txtNoReceipt = view.findViewById(R.id.noReceipts);
        txtThesisName = view.findViewById(R.id.txtThesisName);
        txtStudent = view.findViewById(R.id.txtStudentEmail);
        TextView txtProfessor = view.findViewById(R.id.txtProfessor);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String thesisName = bundle.getString("thesis_name");
            String student = bundle.getString("student");
            String professor = bundle.getString("professor");

            if (!professor.equals("")) {
                String label = getResources().getString(R.string.professor_info_message_student);
                txtProfessor.setText(label);
                txtStudent.setText(professor);
            }else
                txtStudent.setText(student);

            txtThesisName.setText(thesisName);

        }

        btnNewReceipt = view.findViewById(R.id.btnNewReceipt);
        listView = view.findViewById(R.id.layoutReceiptsList);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.account.getAccountType().equals("Student")) {
            btnNewReceipt.setVisibility(View.GONE);
        } else {
            btnNewReceipt.setOnClickListener(view1 -> newReceipt());
        }

        db.collection("ricevimenti")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (Objects.equals(document.getString("Thesis"), txtThesisName.getText().toString())){
                                addReceiptCard(document);
                                txtNoReceipt.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private void newReceipt() {
        Bundle newReceiptBundle = new Bundle();
        newReceiptBundle.putString("thesis_name", txtThesisName.getText().toString());
        newReceiptBundle.putString("student", txtStudent.getText().toString());

        Fragment newReceiptFragment = new NewReceiptFragment();
        newReceiptFragment.setArguments(newReceiptBundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newReceiptFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void addReceiptCard(DocumentSnapshot document) {
        View v = getLayoutInflater().inflate(R.layout.card_receipt, null);

        TextView date = v.findViewById(R.id.txtReceiptDateCard);
        TextView startTime = v.findViewById(R.id.txtReceiptStartTimeCard);
        TextView endTime = v.findViewById(R.id.txtReceiptEndTimeCard);
        TextView description = v.findViewById(R.id.txtReceiptDescriptionCard);
        TextView tasks = v.findViewById(R.id.addressedTasksCard);

        date.setText(document.getString("Date"));
        startTime.setText((document.getString("Start Time")));
        endTime.setText(document.getString("End Time"));
        description.setText(document.getString("Description"));

        Map<String, Object> map = document.getData();
        ArrayList<String> taskList = new ArrayList<>();
        assert map != null;
        if(Objects.equals(map.get("Tasks"), ""))
            tasks.setText(R.string.none);
        else if (map.get("Task") instanceof String){
            tasks.setText(Objects.requireNonNull(map.get("Tasks")).toString());
        }

        else {
            for( Object obj : (ArrayList<?>) Objects.requireNonNull(map.get("Tasks")))
                taskList.add(obj.toString());

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < taskList.size(); i++) {
                sb.append(taskList.get(i));
                if (i < taskList.size() - 1) {
                    sb.append(", ");
                }
            }
            String result = sb.toString();
            tasks.setText(result);
        }
        listView.addView(v);
    }
}