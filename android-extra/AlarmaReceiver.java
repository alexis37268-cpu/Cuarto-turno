package com.alexis.ruedayquincena;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Llega la hora: arranca el servicio que suena. Al reiniciar el celu, vuelve a programar todo. */
public class AlarmaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            AlarmaStore.programarTodas(c);
            return;
        }
        Intent s = new Intent(c, AlarmaService.class);
        s.setAction("SONAR");
        s.putExtra("titulo", i.getStringExtra("titulo"));
        s.putExtra("texto", i.getStringExtra("texto"));
        try {
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(s); else c.startService(s);
        } catch (Exception e) {
            AlarmaService.avisoSimple(c, i.getStringExtra("titulo"), i.getStringExtra("texto"));
        }
    }
}
