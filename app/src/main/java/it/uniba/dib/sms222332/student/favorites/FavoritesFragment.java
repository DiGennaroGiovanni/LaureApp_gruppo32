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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.MainActivity;
import it.uniba.dib.sms222332.commonActivities.Thesis;

public class FavoritesFragment extends Fragment {
    FavoritesAdapter adapter;
    TextView txtNoFavorites;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(( (AppCompatActivity) requireActivity() ).getSupportActionBar()).setTitle(getResources().getString(R.string.favoritesToolbar));
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        txtNoFavorites = view.findViewById(R.id.noFavorites);

        adapter = new FavoritesAdapter(MainActivity.theses);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ThesisTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.theses = (ArrayList<Thesis>) adapter.getTheses();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(MainActivity.theses.isEmpty())
            txtNoFavorites.setVisibility(View.VISIBLE);
        else
            txtNoFavorites.setVisibility(View.GONE);
    }
}


