package it.uniba.dib.sms222332.student.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.Thesis;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ThesisViewHolder> {

    private final List<Thesis> theses;
    private View.OnClickListener listener;

    public FavoritesAdapter(List<Thesis> theses) {
        this.theses = theses;
    }

    public static class ThesisViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName, txtProfessor;

        public ThesisViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtThesisName);
            txtProfessor = itemView.findViewById(R.id.txtProfessor);
        }
    }

    public void setOnClickListener( View.OnClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThesisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_favorite, parent, false);

        // Button shareBtn = view.findViewById(R.id.shareBtn);
        //shareBtn.setOnClickListener(view1 -> Snackbar.make(view1,"PROVA CLICK",Snackbar.LENGTH_SHORT).show());

        return new ThesisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThesisViewHolder holder, int position) {
        Thesis current = theses.get(position);
        holder.txtName.setText(current.getName());
        holder.txtProfessor.setText(current.getProfessor());
        holder.itemView.setTag(current);
        holder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return theses.size();
    }


    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(theses, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(theses, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }


    public List<Thesis> getTheses() {
        return theses;
    }
}
