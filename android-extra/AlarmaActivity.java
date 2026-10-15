package com.alexis.ruedayquincena;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Pantalla completa de la alarma: aparece aunque el celu esté bloqueado. */
public class AlarmaActivity extends Activity {
    static WeakReference<AlarmaActivity> actual;

    static void cerrar() {
        AlarmaActivity a = actual != null ? actual.get() : null;
        if (a != null) a.finish();
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    Button boton(String txt, int fondo, int letra) {
        Button b = new Button(this);
        b.setText(txt);
        b.setTextSize(22);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(letra);
        GradientDrawable g = new GradientDrawable();
        g.setColor(fondo);
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(72));
        lp.topMargin = dp(16);
        b.setLayoutParams(lp);
        return b;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        actual = new WeakReference<>(this);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String t = getIntent().getStringExtra("titulo");
        String x = getIntent().getStringExtra("texto");

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(dp(28), dp(28), dp(28), dp(28));
        l.setBackgroundColor(Color.rgb(47, 75, 99));

        TextView hora = new TextView(this);
        hora.setText(new SimpleDateFormat("HH:mm", new Locale("es", "AR")).format(new Date()));
        hora.setTextSize(72);
        hora.setTextColor(Color.WHITE);
        hora.setGravity(Gravity.CENTER);
        hora.setTypeface(Typeface.DEFAULT_BOLD);
        l.addView(hora);

        TextView tv = new TextView(this);
        tv.setText(t != null ? t : "Alarma de turno");
        tv.setTextSize(32);
        tv.setTextColor(Color.rgb(242, 193, 78));
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(0, dp(12), 0, dp(8));
        l.addView(tv);

        TextView tx = new TextView(this);
        tx.setText(x != null ? x : "");
        tx.setTextSize(20);
        tx.setTextColor(Color.WHITE);
        tx.setGravity(Gravity.CENTER);
        tx.setPadding(0, 0, 0, dp(24));
        l.addView(tx);

        Button apagar = boton("Apagar", Color.rgb(242, 193, 78), Color.rgb(42, 29, 0));
        apagar.setOnClickListener(v -> {
            AlarmaService.detenerYa(this);
            finish();
        });
        l.addView(apagar);

        Button posponer = boton("Posponer 10 min", Color.rgb(61, 90, 128), Color.WHITE);
        posponer.setOnClickListener(v -> {
            Intent p = new Intent(this, AlarmaCorte.class).setAction("POSPONER");
            p.putExtra("titulo", t);
            p.putExtra("texto", x);
            sendBroadcast(p);
            finish();
        });
        l.addView(posponer);

        setContentView(l);
    }

    /** Cualquier tecla de volumen también apaga. */
    @Override
    public boolean onKeyDown(int code, android.view.KeyEvent e) {
        if (code == android.view.KeyEvent.KEYCODE_VOLUME_UP || code == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            AlarmaService.detenerYa(this);
            finish();
            return true;
        }
        return super.onKeyDown(code, e);
    }

    @Override protected void onDestroy() { super.onDestroy(); }
}
