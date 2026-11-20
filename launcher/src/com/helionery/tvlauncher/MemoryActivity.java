package com.helionery.tvlauncher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Tela "Memória extra": mostra RAM e a troca (swap) no microSD em tempo real.
 * Ligar/desligar a troca exige root, que um app comum não tem nesta TV;
 * por isso isso é feito pelo script memoria_extra_microsd.sh no computador.
 */
public class MemoryActivity extends Activity {

    private TextView status, ramValue, swapValue, cardValue, swapCfg;
    private View ramBar, swapBar;
    private FrameLayout ramTrack, swapTrack;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override public void run() { refresh(); handler.postDelayed(this, 2000); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);

        LinearLayout root = Ui.row(this, 72);
        root.setGravity(Gravity.TOP);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.px(96), Ui.px(64), Ui.px(96), Ui.px(64));

        // Coluna da esquerda: explicação e aviso
        LinearLayout left = Ui.col(this);
        TextView back = Ui.text(this, "← Início", 22, Ui.ACCENT, false);
        back.setPadding(Ui.px(16), Ui.px(10), Ui.px(16), Ui.px(10));
        Ui.focusRing(back, Ui.BG, 999);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        Ui.add(left, back, -2, -2, 0);
        Ui.add(left, Ui.text(this, "CONFIGURAÇÕES · SISTEMA", 20, Ui.ACCENT, true), -2, -2, 28);
        Ui.add(left, Ui.text(this, "Memória extra", 60, Ui.TEXT, true), -2, -2, 28);
        TextView desc = Ui.text(this, "Usa um cartão microSD (no adaptador USB) como área de troca (swap). "
                + "Quando a RAM enche, a TV move dados pouco usados para o cartão em vez de fechar apps.", 24, Ui.TEXT_DIM, false);
        desc.setLineSpacing(0, 1.4f);
        Ui.add(left, desc, -1, -2, 28);

        LinearLayout warn = Ui.col(this);
        warn.setBackgroundDrawable(Ui.round(Ui.WARN_BG, 20));
        warn.setPadding(Ui.px(28), Ui.px(24), Ui.px(28), Ui.px(24));
        Ui.add(warn, Ui.text(this, "Não é RAM de verdade", 22, Ui.WARN, true), -2, -2, 0);
        TextView w = Ui.text(this, "O cartão é centenas de vezes mais lento que a RAM. Ajuda a manter apps abertos, "
                + "mas pode deixar a TV mais lenta ao trocar de app e desgasta o cartão.", 20, Ui.WARN_TEXT, false);
        w.setLineSpacing(0, 1.4f);
        Ui.add(warn, w, -1, -2, 10);
        Ui.add(left, warn, -1, -2, 28);
        root.addView(left, new LinearLayout.LayoutParams(Ui.px(560), ViewGroup.LayoutParams.WRAP_CONTENT));

        // Coluna da direita: estado ao vivo
        LinearLayout right = Ui.col(this);
        right.setPadding(0, Ui.px(96), 0, 0);

        LinearLayout statusCard = card();
        Ui.add(statusCard, Ui.text(this, "microSD como memória extra", 28, Ui.TEXT, true), -2, -2, 0);
        status = Ui.text(this, "", 20, Ui.TEXT_MUTED, false);
        Ui.add(statusCard, status, -2, -2, 8);
        Ui.add(right, statusCard, -1, -2, 0);

        LinearLayout pair = Ui.row(this, 24);
        LinearLayout ram = card();
        Ui.add(ram, Ui.text(this, "RAM da placa", 22, Ui.TEXT_MUTED, false), -2, -2, 0);
        ramValue = Ui.text(this, "", 44, Ui.TEXT, true);
        Ui.add(ram, ramValue, -2, -2, 18);
        ramTrack = track(); ramBar = bar(Ui.TEXT); ramTrack.addView(ramBar);
        Ui.add(ram, ramTrack, -1, Ui.px(14), 18);
        LinearLayout swap = card();
        Ui.add(swap, Ui.text(this, "Troca no microSD", 22, Ui.TEXT_MUTED, false), -2, -2, 0);
        swapValue = Ui.text(this, "", 44, Ui.TEXT, true);
        Ui.add(swap, swapValue, -2, -2, 18);
        swapTrack = track(); swapBar = bar(Ui.SWAP); swapTrack.addView(swapBar);
        Ui.add(swap, swapTrack, -1, Ui.px(14), 18);
        pair.addView(ram, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sp.leftMargin = Ui.px(24);
        pair.addView(swap, sp);
        Ui.add(right, pair, -1, -2, 24);

        LinearLayout info = Ui.row(this, 24);
        LinearLayout c1 = card(); Ui.add(c1, Ui.text(this, "Cartão", 18, Ui.TEXT_MUTED, false), -2, -2, 0);
        cardValue = Ui.text(this, "", 22, Ui.TEXT, true); Ui.add(c1, cardValue, -2, -2, 6);
        LinearLayout c2 = card(); Ui.add(c2, Ui.text(this, "Prioridade de troca", 18, Ui.TEXT_MUTED, false), -2, -2, 0);
        swapCfg = Ui.text(this, "", 22, Ui.TEXT, true); Ui.add(c2, swapCfg, -2, -2, 6);
        LinearLayout c3 = card(); Ui.add(c3, Ui.text(this, "Após reiniciar", 18, Ui.TEXT_MUTED, false), -2, -2, 0);
        Ui.add(c3, Ui.text(this, "Desliga sozinha", 22, Ui.TEXT, true), -2, -2, 6);
        info.addView(c1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p2.leftMargin = Ui.px(24); info.addView(c2, p2);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p3.leftMargin = Ui.px(24); info.addView(c3, p3);
        Ui.add(right, info, -1, -2, 24);

        LinearLayout how = card();
        Ui.add(how, Ui.text(this, "Para ligar ou desligar", 22, Ui.TEXT_MUTED, false), -2, -2, 0);
        TextView cmd = Ui.text(this, "No computador:  ./memoria_extra_microsd.sh <IP da TV> ligar 512\n"
                + "Para desligar:   ./memoria_extra_microsd.sh <IP da TV> desligar", 20, Ui.TEXT, false);
        cmd.setTypeface(android.graphics.Typeface.MONOSPACE);
        cmd.setLineSpacing(0, 1.5f);
        Ui.add(how, cmd, -2, -2, 12);
        Ui.add(right, how, -1, -2, 24);

        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rp.leftMargin = Ui.px(72);
        root.addView(right, rp);
        setContentView(root);
        back.requestFocus();
    }

    private LinearLayout card() {
        LinearLayout c = Ui.col(this);
        c.setBackgroundDrawable(Ui.round(Ui.SURFACE, 24));
        c.setPadding(Ui.px(36), Ui.px(28), Ui.px(36), Ui.px(28));
        return c;
    }

    private FrameLayout track() {
        FrameLayout t = new FrameLayout(this);
        t.setBackgroundDrawable(Ui.round(Ui.TRACK, 999));
        return t;
    }

    private View bar(int color) {
        View v = new View(this);
        v.setBackgroundDrawable(Ui.round(color, 999));
        v.setLayoutParams(new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

    private void setBar(final FrameLayout trackView, final View barView, final float frac) {
        trackView.post(new Runnable() {
            @Override public void run() {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) barView.getLayoutParams();
                lp.width = Math.round(trackView.getWidth() * Math.max(0f, Math.min(1f, frac)));
                barView.setLayoutParams(lp);
            }
        });
    }

    private void refresh() {
        Mem m = Mem.read();
        long ramUsed = Math.max(0, m.ramTotal - m.ramFree);
        ramValue.setText(m.ramFree + " / " + m.ramTotal + " MB livres");
        setBar(ramTrack, ramBar, m.ramTotal > 0 ? (float) ramUsed / m.ramTotal : 0f);
        if (m.swapOn()) {
            long used = m.swapTotal - m.swapFree;
            status.setText("Ativa · usando o cartão");
            swapValue.setText(used + " / " + m.swapTotal + " MB");
            setBar(swapTrack, swapBar, (float) used / m.swapTotal);
            cardValue.setText(m.swapFile);
        } else {
            status.setText("Desligada · nenhum cartão em uso");
            swapValue.setText("—");
            setBar(swapTrack, swapBar, 0f);
            cardValue.setText("Nenhum");
        }
        swapCfg.setText("swappiness " + m.swappiness);
    }

    @Override protected void onResume() { super.onResume(); handler.post(tick); }
    @Override protected void onPause() { handler.removeCallbacks(tick); super.onPause(); }
}
