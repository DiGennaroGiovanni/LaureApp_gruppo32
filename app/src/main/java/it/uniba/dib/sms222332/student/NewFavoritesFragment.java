package it.uniba.dib.sms222332.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.commonActivities.Thesis;

public class NewFavoritesFragment extends Fragment {

    List<String> tesiPreferite = new ArrayList<>();
    List<Thesis> theses = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.favoritesToolbar));
        View view = inflater.inflate(R.layout.fragment_new_favorite, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);



        DocumentReference docStud = db.collection("studenti").document(MainActivity.account.getEmail());
        docStud.get().
                addOnCompleteListener(task -> {


                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> map = document.getData();

                            tesiPreferite = (List<String>) map.get("Favorites");

                            for (String thesisName : tesiPreferite){
                                db.collection("Tesi").document(thesisName).get().addOnSuccessListener(documentSnapshot -> {
                                    if(documentSnapshot.getString("Student").isEmpty() || documentSnapshot.getString("Student").equals(MainActivity.account.getEmail())){
                                        Thesis thesis = new Thesis(thesisName, documentSnapshot.getString("Professor"));
                                        theses.add(thesis);
                                    }
                                });
                            }


                        }
                    }
                });


            FavoritesAdapter adapter = new FavoritesAdapter(theses);
            recyclerView.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new ThesisTouchHelperCallback(adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //    private void loadFavorites(NewFavoritesFragment.Callback<List<String>> callback) {
//
//
//    private void loadThesis(NewFavoritesFragment.Callback<ArrayList<Thesis>> callback){
//
//
//    }
//
//
//    interface Callback<T> {
//        void onResult(T result);
//    }
}


