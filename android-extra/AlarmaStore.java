package com.alexis.ruedayquincena;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/** Guarda y programa las alarmas de turno con AlarmManager (como el reloj del celu). */
public class AlarmaStore {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("alarmas", Context.MODE_PRIVATE); }

    static void guardar(Context c, JSONArray a) { sp(c).edit().putString("lista", a.toString()).apply(); }

    static JSONArray lista(Context c) {
        try { return new JSONArray(sp(c).getString("lista", "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    static boolean puedeExactas(Context c) {
        if (Build.VERSION.SDK_INT < 31) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am.canScheduleExactAlarms();
    }

    static boolean puedePantallaCompleta(Context c) {
        if (Build.VERSION.SDK_INT < 34) return true;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        return nm.canUseFullScreenIntent();
    }

    static PendingIntent pi(Context c, int id, String titulo, String texto) {
        Intent i = new Intent(c, AlarmaReceiver.class);
        i.setAction("com.alexis.ruedayquincena.ALARMA." + id);
        i.putExtra("id", id);
        i.putExtra("titulo", titulo);
        i.putExtra("texto", texto);
        return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void programarUna(Context c, int id, long at, String titulo, String texto) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = pi(c, id, titulo, texto);
        Intent abrir = new Intent(c, MainActivity.class);
        PendingIntent mostrar = PendingIntent.getActivity(c, 424242, abrir, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (puedeExactas(c)) {
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, mostrar), p);
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
        }
    }

    static int programarTodas(Context c) {
        JSONArray a = lista(c);
        long ahora = System.currentTimeMillis();
        int n = 0;
        JSONArray ids = new JSONArray();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            long at = o.optLong("at");
            if (at <= ahora) continue;
            int id = o.optInt("id");
            programarUna(c, id, at, o.optString("titulo"), o.optString("texto"));
            ids.put(id);
            n++;
        }
        sp(c).edit().putString("ids", ids.toString()).apply();
        return n;
    }

    static void cancelarTodas(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        try {
            JSONArray ids = new JSONArray(sp(c).getString("ids", "[]"));
            for (int i = 0; i < ids.length(); i++) am.cancel(pi(c, ids.getInt(i), "", ""));
        } catch (Exception e) { /* nada */ }
    }

    static Uri uriSonido(Context c) {
        String s = sp(c).getString("sonido", null);
        if (s != null) return Uri.parse(s);
        Uri u = RingtoneManager.getActualDefaultRingtoneUri(c, RingtoneManager.TYPE_ALARM);
        if (u == null) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (u == null) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        return u;
    }

    static void guardarSonido(Context c, Uri u) {
        sp(c).edit().putString("sonido", u == null ? null : u.toString()).apply();
    }

    static String nombreSonido(Context c) {
        try {
            Ringtone r = RingtoneManager.getRingtone(c, uriSonido(c));
            return r != null ? r.getTitle(c) : "Predeterminado";
        } catch (Exception e) { return "Predeterminado"; }
    }
}
