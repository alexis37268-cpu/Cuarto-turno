package com.alexis.ruedayquincena;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

/** Suena en loop por el canal de ALARMA y vibra hasta que se apaga, se pospone o pasan 5 minutos. */
public class AlarmaService extends Service {
    static final String CANAL = "alarma_pantalla";
    static final int FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    MediaPlayer mp;
    Vibrator vib;
    PowerManager.WakeLock wl;
    final Handler h = new Handler(Looper.getMainLooper());
    static AlarmaService actual;
    static final long MAX_MS = 2 * 60 * 1000L; /* si nadie la apaga, se corta sola a los 2 minutos */

    /** Corta la alarma desde cualquier lado de la app, sin depender de que llegue un Intent. */
    static void detenerYa(Context c) {
        AlarmaService s = actual;
        if (s != null) s.parar();
        try { c.stopService(new Intent(c, AlarmaService.class)); } catch (Exception e) { }
        try { c.getSystemService(NotificationManager.class).cancel(77); c.getSystemService(NotificationManager.class).cancel(78); } catch (Exception e) { }
        AlarmaActivity.cerrar();
    }

    static PendingIntent piCorte(Context c, String accion, int req, String t, String x) {
        Intent i = new Intent(c, AlarmaCorte.class).setAction(accion);
        i.putExtra("titulo", t); i.putExtra("texto", x);
        return PendingIntent.getBroadcast(c, req, i, FLAGS);
    }

    @Override public IBinder onBind(Intent i) { return null; }

    static void crearCanal(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CANAL, "Alarma de turno (pantalla)", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Pantalla de alarma para cada turno");
            ch.setSound(null, null);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            c.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    static void avisoSimple(Context c, String t, String x) {
        crearCanal(c);
        Intent full = new Intent(c, AlarmaActivity.class);
        full.putExtra("titulo", t);
        full.putExtra("texto", x);
        full.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pf = PendingIntent.getActivity(c, 11, full, FLAGS);
        Notification n = new NotificationCompat.Builder(c, CANAL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(t).setContentText(x)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pf, true).setContentIntent(pf).setAutoCancel(true).build();
        c.getSystemService(NotificationManager.class).notify(78, n);
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        actual = this;
        String act = i != null ? i.getAction() : null;
        if ("APAGAR".equals(act)) { parar(); return START_NOT_STICKY; }
        if ("POSPONER".equals(act)) {
            AlarmaStore.programarUna(this, 999998, System.currentTimeMillis() + 10 * 60000L,
                    i.getStringExtra("titulo"), "Pospuesta · " + i.getStringExtra("texto"));
            parar();
            return START_NOT_STICKY;
        }
        String t = i != null && i.getStringExtra("titulo") != null ? i.getStringExtra("titulo") : "Alarma de turno";
        String x = i != null && i.getStringExtra("texto") != null ? i.getStringExtra("texto") : "";
        crearCanal(this);

        Intent full = new Intent(this, AlarmaActivity.class);
        full.putExtra("titulo", t);
        full.putExtra("texto", x);
        full.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        PendingIntent pf = PendingIntent.getActivity(this, 1, full, FLAGS);
        PendingIntent pa = piCorte(this, "APAGAR", 2, t, x);
        PendingIntent pp = piCorte(this, "POSPONER", 3, t, x);

        Notification n = new NotificationCompat.Builder(this, CANAL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(t).setContentText(x)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pf, true).setContentIntent(pf).setOngoing(true).setDeleteIntent(pa)
                .addAction(0, "Apagar", pa)
                .addAction(0, "Posponer 10 min", pp)
                .build();
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(77, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else startForeground(77, n);
        } catch (Exception e) {
            avisoSimple(this, t, x);
        }
        try { startActivity(full); } catch (Exception e) { /* si Android no deja, queda la notificación de pantalla completa */ }
        sonar();
        h.postDelayed(this::parar, MAX_MS);
        return START_NOT_STICKY;
    }

    void sonar() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ruedayquincena:alarma");
            wl.acquire(MAX_MS + 30000L);
        } catch (Exception e) { /* nada */ }
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        if (mp != null) { try { mp.release(); } catch (Exception e) { } mp = null; }
        Uri[] fuentes = { AlarmaStore.uriSonido(this), Uri.parse("android.resource://" + getPackageName() + "/raw/alarma") };
        for (Uri u : fuentes) {
            if (u == null) continue;
            try {
                mp = new MediaPlayer();
                mp.setAudioAttributes(aa);
                mp.setDataSource(this, u);
                mp.setLooping(true);
                mp.setOnErrorListener((m, w, e) -> { parar(); return true; });
                mp.prepare();
                mp.start();
                break;
            } catch (Exception e) {
                try { mp.release(); } catch (Exception e2) { }
                mp = null;
            }
        }
        try {
            vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] patron = {0, 800, 600};
            if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createWaveform(patron, 0));
            else vib.vibrate(patron, 0);
        } catch (Exception e) { /* nada */ }
    }

    void liberar() {
        h.removeCallbacksAndMessages(null);
        try { if (mp != null) { mp.stop(); mp.release(); } } catch (Exception e) { }
        mp = null;
        try { if (vib != null) vib.cancel(); } catch (Exception e) { }
        try { if (wl != null && wl.isHeld()) wl.release(); } catch (Exception e) { }
    }

    void parar() {
        if (actual == this) actual = null;
        liberar();
        AlarmaActivity.cerrar();
        try { stopForeground(true); } catch (Exception e) { }
        stopSelf();
    }

    @Override public void onDestroy() { if (actual == this) actual = null; liberar(); super.onDestroy(); }
    @Override public void onTaskRemoved(Intent i) { parar(); super.onTaskRemoved(i); }
}
