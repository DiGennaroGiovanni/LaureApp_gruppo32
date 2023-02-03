package it.uniba.dib.sms222332.student.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.commonActivities.Thesis;
import it.uniba.dib.sms222332.student.ThesisDescriptionStudentFragment;

public class FavoritesFragment extends Fragment {
    FavoritesAdapter adapter;
    TextView txtNoFavorites;
    RecyclerView recyclerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.favoritesToolbar));
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        txtNoFavorites = view.findViewById(R.id.noFavorites);


        if(MainActivity.theses.isEmpty())
            txtNoFavorites.setVisibility(View.VISIBLE);
        else
            txtNoFavorites.setVisibility(View.GONE);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        adapter = new FavoritesAdapter(MainActivity.theses,requireActivity());
        adapter.setOnClickListener(view1 -> {

            Thesis thesis = (Thesis) view1.getTag();
            Bundle bundle = new Bundle();

            db.collection("Tesi").document(thesis.getName()).get().addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> thesisData = documentSnapshot.getData();

                assert thesisData != null;
                bundle.putString("correlator", (String) thesisData.get("Correlator"));
                bundle.putString("description", (String) thesisData.get("Description"));
                bundle.putString("estimated_time", (String) thesisData.get("Estimated Time"));
                bundle.putString("faculty", (String) thesisData.get("Faculty"));
                bundle.putString("name", (String) thesisData.get("Name"));
                bundle.putString("type", (String) thesisData.get("Type"));
                bundle.putString("related_projects", (String) thesisData.get("Related Projects"));
                bundle.putString("average_marks", (String) thesisData.get("Average"));
                bundle.putString("required_exams", (String) thesisData.get("Required Exam"));
                bundle.putString("professor_email", (String) thesisData.get("Professor"));

                Fragment thesisDescription = new ThesisDescriptionStudentFragment();
                thesisDescription.setArguments(bundle);

                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, thesisDescription);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            });

        });

        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ThesisTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

    }


    @Override
    public void onPause() {
        super.onPause();
        MainActivity.theses = (ArrayList<Thesis>) adapter.getTheses();
    }
}


