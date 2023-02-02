package it.uniba.dib.sms222332.student.favorites;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Collections;
import java.util.List;

import it.uniba.dib.sms222332.R;
import it.uniba.dib.sms222332.commonActivities.Thesis;
import it.uniba.dib.sms222332.tools.QrGenerator;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ThesisViewHolder> {

    private final List<Thesis> theses;
    private View.OnClickListener listener;
    private final Activity activity;

    public FavoritesAdapter(List<Thesis> theses, Activity activity) {
        this.theses = theses;
        this.activity = activity;
    }

    public static class ThesisViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName, txtProfessor;
        public Button shareBtn;

        public ThesisViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtThesisName);
            txtProfessor = itemView.findViewById(R.id.txtProfessor);
            shareBtn = itemView.findViewById(R.id.shareBtn);
        }
    }

    public void setOnClickListener( View.OnClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThesisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_favorite, parent, false);
        return new ThesisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThesisViewHolder holder, int position) {
        Thesis current = theses.get(position);
        holder.txtName.setText(current.getName());
        holder.txtProfessor.setText(current.getProfessor());
        holder.itemView.setTag(current);
        holder.itemView.setOnClickListener(listener);
        holder.shareBtn.setOnClickListener(view -> {
            final Dialog dialogQr = new Dialog(holder.itemView.getContext());
            dialogQr.setContentView(R.layout.dialog_qr);

            ImageView qrImageView = dialogQr.findViewById(R.id.qr_image);
            qrImageView.setImageBitmap(QrGenerator.createQr(current.getName()));

            Button buttonShare = dialogQr.findViewById(R.id.share_button);
            buttonShare.setOnClickListener(view12 -> sharePDF(view12,view12.getContext(),activity,current.getName()));

            Button dismissButton = dialogQr.findViewById(R.id.dismiss_button);
            dismissButton.setOnClickListener(view1 -> dialogQr.dismiss());

            try {
                dialogQr.show();
            } catch (Exception e) {
                Log.e(TAG, "Errore nell'onClick del shareButton : " + e);
            }
        });
    }

    private void sharePDF(View view, Context context,Activity activity,  String thesisName) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("PDF_tesi/" + thesisName + ".pdf");

        try {
            final File localFile = new File(context.getExternalFilesDir(null), thesisName + ".pdf");
            fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Uri uri = FileProvider.getUriForFile(context, "it.uniba.dib.sms222332", localFile);
                // in questa uri va il link del pdf creato e salvato nello storage
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                activity.startActivity(Intent.createChooser(shareIntent, "Condividi PDF informazioni tesi"));

            }).addOnFailureListener(e -> {
                // Controllo se l'error code è riferito al fatto che il dispositivo non è connesso ad internet
                if (e instanceof FirebaseNetworkException) {
                    Snackbar.make(view, "No internet connection", Snackbar.LENGTH_LONG).show();
                } else if (e instanceof StorageException) {
                    // Controllo se l'error code è riferito al fatto che non esiste il file sul database
                    if (((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Snackbar.make(view , "File does not exist", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    // Stampo nella console il messaggio di errore nel caso in cui è di un altro tipo
                    Log.w("Firebas storage ERROR", e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", "Errore nel download del PDF dal database: " + e.getMessage());
        }
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
