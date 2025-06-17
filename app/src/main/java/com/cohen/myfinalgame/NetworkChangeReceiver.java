package com.cohen.myfinalgame;

    import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

    public class NetworkChangeReceiver extends BroadcastReceiver {

        private AlertDialog alertDialog;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInternetAvailable(context)) {
                showNoInternetDialog(context);
            } else {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        }

        private boolean isInternetAvailable(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network network = cm.getActiveNetwork();
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            return false;
        }

        private void showNoInternetDialog(Context context) {
            if (alertDialog != null && alertDialog.isShowing()) return;

            if (context instanceof Activity) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("אין חיבור לאינטרנט");
                builder.setMessage("אנא בדוק את החיבור שלך.");
                builder.setCancelable(false);
                builder.setPositiveButton("אישור", (dialog, which) -> dialog.dismiss());
                alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }



