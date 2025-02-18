package com.cohen.myfinalgame;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "game_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create the notification channel (if necessary)
        createNotificationChannel(context);

        // Create an intent for the "Try Again" button action (opens GameActivity)
        Intent retryIntent = new Intent(context, GameActivity.class);
        PendingIntent retryPendingIntent = PendingIntent.getActivity(
                context,
                0,
                retryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Game Over")
                .setContentText("You lost! Try again?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_retry, "Try Again", retryPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check if permission is granted before calling notify
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        } else {
            // Optionally, you could request the permission here or log the issue.
            try {
                notificationManager.notify(1, builder.build());
            } catch (SecurityException e) {
                e.printStackTrace();
                // Handle the lack of permission gracefully.
            }
        }
    }

    // Create the notification channel (required for API 26+)
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Game Notifications";
            String description = "Notifications for game events";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
