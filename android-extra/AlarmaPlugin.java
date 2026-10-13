package com.alexis.ruedayquincena;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;

/** Puente entre la app (JavaScript) y las alarmas nativas. */
@CapacitorPlugin(name = "Alarma")
public class AlarmaPlugin extends Plugin {

    JSObject estadoObj() {
        JSObject r = new JSObject();
        r.put("exactas", AlarmaStore.puedeExactas(getContext()));
        r.put("pantallaCompleta", AlarmaStore.puedePantallaCompleta(getContext()));
        r.put("sonido", AlarmaStore.nombreSonido(getContext()));
        return r;
    }

    @PluginMethod
    public void programar(PluginCall call) {
        try {
            JSArray arr = call.getArray("alarmas", new JSArray());
            JSONArray guardar = new JSONArray();
            for (int i = 0; i < arr.length(); i++) guardar.put(arr.getJSONObject(i));
            AlarmaStore.cancelarTodas(getContext());
            AlarmaStore.guardar(getContext(), guardar);
            int n = AlarmaStore.programarTodas(getContext());
            JSObject r = estadoObj();
            r.put("programadas", n);
            call.resolve(r);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void estado(PluginCall call) { call.resolve(estadoObj()); }

    @PluginMethod
    public void pedirExactas(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 31) {
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
        call.resolve();
    }

    @PluginMethod
    public void pedirPantallaCompleta(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 34) {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
        call.resolve();
    }

    @PluginMethod
    public void elegirSonido(PluginCall call) {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Sonido de la alarma de turno");
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        Uri actual = AlarmaStore.uriSonido(getContext());
        if (actual != null) i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, actual);
        startActivityForResult(call, i, "sonidoElegido");
    }

    @ActivityCallback
    private void sonidoElegido(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri u = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            AlarmaStore.guardarSonido(getContext(), u);
        }
        call.resolve(estadoObj());
    }

    @PluginMethod
    public void probar(PluginCall call) {
        int seg = call.getInt("segundos", 60);
        AlarmaStore.programarUna(getContext(), 999999, System.currentTimeMillis() + seg * 1000L,
                "⏰ Prueba de alarma", "Si suena y ves esta pantalla, las alarmas funcionan.");
        call.resolve();
    }
}
