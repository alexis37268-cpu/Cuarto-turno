package com.alexis.ruedayquincena;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Botones Apagar / Posponer de la notificación: un broadcast llega siempre, aunque la app esté en segundo plano. */
public class AlarmaCorte extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        AlarmaService.detenerYa(c);
        if ("POSPONER".equals(i.getAction())) {
            String t = i.getStringExtra("titulo");
            String x = i.getStringExtra("texto");
            AlarmaStore.programarUna(c, 999998, System.currentTimeMillis() + 10 * 60000L,
                    t != null ? t : "Alarma de turno", "Pospuesta · " + (x != null ? x : ""));
        }
    }
}
