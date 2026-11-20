package com.helionery.tvlauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Medidas e cores do layout. Tudo é desenhado sobre uma tela de 1920x1080 e escalado. */
final class Ui {
    static final int BG = Color.parseColor("#0E1014");
    static final int SURFACE = Color.parseColor("#1A1E27");
    static final int SURFACE_2 = Color.parseColor("#232937");
    static final int TEXT = Color.parseColor("#E9ECF1");
    static final int TEXT_DIM = Color.parseColor("#B4BAC6");
    static final int TEXT_MUTED = Color.parseColor("#A3AAB7");
    static final int ACCENT = Color.parseColor("#9FD8CF");
    static final int ACCENT_BG = Color.parseColor("#16302C");
    static final int SWAP = Color.parseColor("#5CC8B8");
    static final int TRACK = Color.parseColor("#2A303C");
    static final int WARN_BG = Color.parseColor("#2A2418");
    static final int WARN = Color.parseColor("#F2C77E");
    static final int WARN_TEXT = Color.parseColor("#D8CCB4");

    static final int[] APP_COLORS = {
        Color.parseColor("#B3261E"), Color.parseColor("#2F4A8A"), Color.parseColor("#3C5A4F"),
        Color.parseColor("#2D6A5F"), Color.parseColor("#6B4E2E"), Color.parseColor("#1F5F8B"),
        Color.parseColor("#3A4150"), Color.parseColor("#5A3A6B")
    };

    private static float scale = 1f;

    static void init(Activity a) {
        DisplayMetrics dm = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int w = Math.max(dm.widthPixels, dm.heightPixels);
        scale = w / 1920f;
    }

    /** Converte px do desenho (1920 de largura) para px reais da tela. */
    static int px(float designPx) {
        return Math.round(designPx * scale);
    }

    static GradientDrawable round(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(px(radius));
        return d;
    }

    static GradientDrawable roundStroke(int fill, float radius, int stroke, float strokeW) {
        GradientDrawable d = round(fill, radius);
        d.setStroke(px(strokeW), stroke);
        return d;
    }

    static TextView text(Activity a, String s, float sizeDesignPx, int color, boolean bold) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(sizeDesignPx));
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        t.setIncludeFontPadding(false);
        return t;
    }

    static LinearLayout row(Activity a, float gap) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setTag(Float.valueOf(gap));
        return l;
    }

    static LinearLayout col(Activity a) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    /** Adiciona um filho com margem à esquerda (ou acima) para imitar o "gap" do desenho. */
    static void add(LinearLayout parent, View child, int w, int h, float gap) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        if (parent.getChildCount() > 0) {
            if (parent.getOrientation() == LinearLayout.HORIZONTAL) lp.leftMargin = px(gap);
            else lp.topMargin = px(gap);
        }
        parent.addView(child, lp);
    }

    /** Anel branco e leve aumento quando o controle remoto foca o item. */
    static void focusRing(final View v, final int fill, final float radius) {
        v.setFocusable(true);
        v.setBackgroundDrawable(round(fill, radius));
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean has) {
                view.setBackgroundDrawable(has ? roundStroke(fill, radius, TEXT, 5) : round(fill, radius));
                view.animate().scaleX(has ? 1.06f : 1f).scaleY(has ? 1.06f : 1f).setDuration(120).start();
            }
        });
    }

    private Ui() {}
}
