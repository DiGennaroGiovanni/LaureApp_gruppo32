package it.uniba.dib.sms222332.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.Thesis;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ThesisViewHolder> {

    private List<Thesis> theses;

    public FavoritesAdapter(List<Thesis> theses) {
        this.theses = theses;
    }

    public class ThesisViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName, txtProfessor;

        public ThesisViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtThesisName);
            txtProfessor = itemView.findViewById(R.id.txtProfessor);
        }
    }

    @NonNull
    @Override
    public ThesisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_available_thesis, parent, false);
        return new ThesisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThesisViewHolder holder, int position) {
        Thesis current = theses.get(position);
        holder.txtName.setText(current.getName());
        holder.txtProfessor.setText(current.getProfessor());
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

    public void onItemDismiss(int position) {
        theses.remove(position);
        notifyItemRemoved(position);
    }
}
