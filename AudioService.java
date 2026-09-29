package com.angel.neuroaudio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Reproduce las pistas en bucle sin cortes (ExoPlayer) como servicio en primer plano,
 * para que el sonido siga con la pantalla apagada. Se detiene solo al cumplir el tiempo,
 * con desvanecimiento al final, y registra la sesión completada.
 */
public class AudioService extends Service {

    public static final String ACTION_PLAY = "com.angel.neuroaudio.PLAY";
    public static final String ACTION_STOP = "com.angel.neuroaudio.STOP";
    private static final String CHANNEL = "playback";
    private static final int NOTIF_ID = 7;

    static volatile AudioService instance;
    static volatile boolean sPlaying = false;
    static volatile boolean sCompleted = false;
    static volatile long sElapsedMs = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ExoPlayer player;
    private long startAt;
    private long targetMs;
    private float baseVol = 0.5f;
    private int fadeSec = 60;
    private String meta = "";

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (player == null) return;
            long elapsed = SystemClock.elapsedRealtime() - startAt;
            sElapsedMs = elapsed;
            long left = targetMs - elapsed;
            if (left <= 0) {
                finish(true);
                return;
            }
            if (left < fadeSec * 1000L) {
                player.setVolume(Math.max(0f, baseVol * left / (fadeSec * 1000f)));
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Sesión en curso", NotificationManager.IMPORTANCE_LOW));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_PLAY.equals(action)) {
            showForeground(intent.getStringExtra("title"));
            startPlayback(intent);
        } else if (ACTION_STOP.equals(action)) {
            finish(false);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void showForeground(String title) {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stop = new Intent(this, AudioService.class).setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 1, stop,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_wave)
                .setContentTitle(title != null ? title : "Sesión en curso")
                .setContentText("Neuro-Audio · se detiene sola al terminar")
                .setContentIntent(openPi)
                .setOngoing(true)
                .addAction(new Notification.Action.Builder(null, "Detener", stopPi).build())
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, n);
        }
    }

    private void startPlayback(Intent i) {
        releasePlayer();
        handler.removeCallbacksAndMessages(null);

        String file = i.getStringExtra("file");
        int seconds = Math.max(1, i.getIntExtra("seconds", 900));
        baseVol = i.getFloatExtra("volume", 0.5f);
        fadeSec = Math.max(3, i.getIntExtra("fade", 60));
        meta = i.getStringExtra("meta");
        if (meta == null) meta = "";
        targetMs = seconds * 1000L;
        if (fadeSec * 1000L > targetMs / 2) fadeSec = (int) Math.max(3, targetMs / 2000);

        player = new ExoPlayer.Builder(this).build();
        player.setWakeMode(C.WAKE_MODE_LOCAL);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setMediaItem(MediaItem.fromUri("asset:///" + file));
        player.setVolume(0f);
        player.prepare();
        player.play();

        sPlaying = true;
        sCompleted = false;
        sElapsedMs = 0;
        startAt = SystemClock.elapsedRealtime();
        rampIn(1);
        handler.postDelayed(ticker, 500);
    }

    /** Entrada suave de 3 segundos. */
    private void rampIn(final int step) {
        if (player == null || step > 30) return;
        long left = targetMs - (SystemClock.elapsedRealtime() - startAt);
        if (left > fadeSec * 1000L) player.setVolume(baseVol * step / 30f);
        handler.postDelayed(() -> rampIn(step + 1), 100);
    }

    void setBaseVolume(float v) {
        baseVol = Math.max(0f, Math.min(1f, v));
        if (player == null) return;
        long left = targetMs - (SystemClock.elapsedRealtime() - startAt);
        if (left > fadeSec * 1000L) player.setVolume(baseVol);
    }

    void stopPlayback(boolean completed) {
        finish(completed);
    }

    private void finish(boolean completed) {
        if (completed && meta.length() > 0) {
            getSharedPreferences("neuro", MODE_PRIVATE).edit().putString("done", meta).apply();
        }
        sCompleted = completed;
        sPlaying = false;
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        sPlaying = false;
        instance = null;
        super.onDestroy();
    }
}
