package com.angel.neuroaudio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        int id = i.getIntExtra("id", 0);
        int h = i.getIntExtra("h", 8);
        int m = i.getIntExtra("m", 0);
        String title = i.getStringExtra("title");
        String text = i.getStringExtra("text");

        Reminders.ensureChannel(c);
        Intent open = new Intent(c, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 200 + id, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(c, Reminders.CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_wave)
                .setContentTitle(title != null ? title : "Neuro-Audio")
                .setContentText(text != null ? text : "")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        try {
            c.getSystemService(NotificationManager.class).notify(1000 + id, n);
        } catch (SecurityException ignored) {
        }
        Reminders.scheduleOne(c, id, h, m, title, text);
    }
}
