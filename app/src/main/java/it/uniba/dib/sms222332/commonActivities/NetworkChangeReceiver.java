package it.uniba.dib.sms222332.commonActivities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import it.uniba.dib.sms222332.R;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private Activity activity;

    public NetworkChangeReceiver(Activity activity) {
        this.activity = activity;
    }

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // We have internet
        } else {

            try {
                // codice per mostrare l'alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.no_internet_connection);
                builder.setMessage(R.string.no_internet_connection_first_message);

                builder.setPositiveButton(R.string.close_app, (dialog, which) -> {
                    activity.finishAffinity(); //CHIUDE L'APPLICAZIONE
                });

                //RIMANDA LA SPLASH ACTIVITY
                builder.setNegativeButton(R.string.reload_app, (dialog, which) -> {
                    final Intent intentNegative = new Intent(activity, SplashActivity.class);
                    activity.startActivity(intentNegative);
                    activity.finish();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                Log.e("NetworkChangeReceiver", "TRIMONE Errore durante la gestione della perdita di connessione internet: " + e.getMessage());
            }

        }
    }
}
