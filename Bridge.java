package com.angel.neuroaudio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Puente JavaScript <-> Android. Todos los métodos se llaman desde la página web. */
public class Bridge {

    private final Context ctx;
    private final Handler main = new Handler(Looper.getMainLooper());

    Bridge(Context c) {
        ctx = c.getApplicationContext();
    }

    @JavascriptInterface
    public boolean isNative() {
        return true;
    }

    @JavascriptInterface
    public void play(String file, int seconds, float volume, String title, String meta, int fadeSec) {
        AudioService.sPlaying = true;
        AudioService.sCompleted = false;
        AudioService.sElapsedMs = 0;
        Intent i = new Intent(ctx, AudioService.class);
        i.setAction(AudioService.ACTION_PLAY);
        i.putExtra("file", file);
        i.putExtra("seconds", seconds);
        i.putExtra("volume", volume);
        i.putExtra("title", title);
        i.putExtra("meta", meta);
        i.putExtra("fade", fadeSec);
        ctx.startForegroundService(i);
    }

    @JavascriptInterface
    public void stop() {
        AudioService.sPlaying = false;
        main.post(() -> {
            AudioService s = AudioService.instance;
            if (s != null) s.stopPlayback(false);
        });
    }

    @JavascriptInterface
    public void setVolume(float v) {
        main.post(() -> {
            AudioService s = AudioService.instance;
            if (s != null) s.setBaseVolume(v);
        });
    }

    @JavascriptInterface
    public String getStatus() {
        try {
            JSONObject o = new JSONObject();
            o.put("playing", AudioService.sPlaying);
            o.put("completed", AudioService.sCompleted);
            o.put("elapsed", AudioService.sElapsedMs / 1000.0);
            return o.toString();
        } catch (Exception e) {
            return "{\"playing\":false,\"completed\":false,\"elapsed\":0}";
        }
    }

    @JavascriptInterface
    public String consumeDone() {
        android.content.SharedPreferences p = ctx.getSharedPreferences("neuro", Context.MODE_PRIVATE);
        String d = p.getString("done", "");
        p.edit().remove("done").apply();
        return d;
    }

    @JavascriptInterface
    public void setReminders(String json) {
        Reminders.schedule(ctx, json);
    }

    @JavascriptInterface
    public String saveFile(String name, String content) {
        try {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, name.endsWith(".json") ? "application/json" : "text/csv");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) return "No se pudo crear el archivo.";
                try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                    if (os == null) return "No se pudo escribir el archivo.";
                    os.write(data);
                }
                return "Guardado en la carpeta Descargas: " + name;
            } else {
                File f = new File(ctx.getExternalFilesDir(null), name);
                try (FileOutputStream fo = new FileOutputStream(f)) {
                    fo.write(data);
                }
                return "Guardado en: " + f.getAbsolutePath();
            }
        } catch (Exception e) {
            return "Error al guardar: " + e.getMessage();
        }
    }
}
