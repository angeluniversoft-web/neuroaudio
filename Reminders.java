package com.angel.neuroaudio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/** Recordatorios diarios de las sesiones y del ejercicio. */
public class Reminders {

    static final String CHANNEL = "reminders";
    private static final int MAX_ID = 10;

    static void ensureChannel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Recordatorios del plan", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private static PendingIntent pending(Context c, int id, int h, int m, String title, String text, int flags) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.setAction("com.angel.neuroaudio.REMIND." + id);
        i.putExtra("id", id);
        i.putExtra("h", h);
        i.putExtra("m", m);
        i.putExtra("title", title);
        i.putExtra("text", text);
        return PendingIntent.getBroadcast(c, 100 + id, i, flags | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context c, String json) {
        try {
            c.getSharedPreferences("neuro", Context.MODE_PRIVATE).edit().putString("reminders", json).apply();
            AlarmManager am = c.getSystemService(AlarmManager.class);
            for (int id = 1; id <= MAX_ID; id++) {
                PendingIntent old = pending(c, id, 0, 0, "", "", PendingIntent.FLAG_NO_CREATE);
                if (old != null) {
                    am.cancel(old);
                    old.cancel();
                }
            }
            JSONArray arr = new JSONArray(json);
            for (int k = 0; k < arr.length(); k++) {
                JSONObject o = arr.getJSONObject(k);
                scheduleOne(c, o.getInt("id"), o.getInt("h"), o.getInt("m"), o.optString("title"), o.optString("text"));
            }
        } catch (Exception ignored) {
        }
    }

    static void scheduleOne(Context c, int id, int h, int m, String title, String text) {
        if (id < 1 || id > MAX_ID) return;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis() + 5000) cal.add(Calendar.DAY_OF_YEAR, 1);
        AlarmManager am = c.getSystemService(AlarmManager.class);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                pending(c, id, h, m, title, text, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
