package com.angel.neuroaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restablece los recordatorios después de reiniciar el teléfono. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            String json = c.getSharedPreferences("neuro", Context.MODE_PRIVATE).getString("reminders", "[]");
            Reminders.schedule(c, json);
        }
    }
}
