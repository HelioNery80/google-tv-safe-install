package com.helionery.tvlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Tela inicial no estilo do desenho "Tela inicial". Feita para Android 4.2 (API 17), sem bibliotecas. */
public class HomeActivity extends Activity {

    // Pacotes reais encontrados no firmware da JVC (MStar Edison / base Konka)
    static final String PKG_TV = "com.konka.tvsettings";
    static final String PKG_MM = "com.konka.mm";
    static final String PKG_USB = "com.jrm.localmm";

    /** Ordem preferida da fileira "Seus apps"; o resto vem depois em ordem alfabética. */
    static final String[] PREFERRED = {
        "com.google.android.youtube", PKG_TV, "com.android.browser", "com.android.vending",
        PKG_USB, "com.skype.raider", "com.konka.systemsetting"
    };
    /** Nomes curtos e em português para os apps da TV. */
    static final String[][] NAMES = {
        {PKG_TV, "TV ao vivo"}, {"com.android.browser", "Navegador"}, {"com.android.vending", "Play Store"},
        {PKG_USB, "Mídia USB"}, {"com.konka.systemsetting", "Configurações"}
    };

    private FrameLayout content;
    private View homeView;
    private View appsView;
    private TextView memChip;
    private TextView dateText;
    private View firstAppTile;
    private final Handler handler = new Handler();

    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { rebuildContent(false); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.init(this);

        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(Ui.px(96), Ui.px(48), Ui.px(96), Ui.px(32));
        root.addView(buildHeader(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.px(72)));

        content = new FrameLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        lp.topMargin = Ui.px(40);
        root.addView(content, lp);
        setContentView(root);

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_CHANGED);
        f.addDataScheme("package");
        registerReceiver(pkgReceiver, f);

        rebuildContent(true);
    }

    @Override protected void onDestroy() {
        unregisterReceiver(pkgReceiver);
        super.onDestroy();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    // ---------------------------------------------------------------- cabeçalho

    private View buildHeader() {
        LinearLayout header = Ui.row(this, 0);

        LinearLayout tabs = Ui.row(this, 12);
        addTab(tabs, "Início", true, new View.OnClickListener() {
            @Override public void onClick(View v) { showHome(); }
        });
        addTab(tabs, "Apps", false, new View.OnClickListener() {
            @Override public void onClick(View v) { showApps(); }
        });
        addTab(tabs, "Entradas", false, new View.OnClickListener() {
            @Override public void onClick(View v) { openInputs(); }
        });
        addTab(tabs, "Mídia USB", false, new View.OnClickListener() {
            @Override public void onClick(View v) { launchPackage(PKG_USB); }
        });
        header.addView(tabs, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        memChip = Ui.text(this, "Memória", 20, Ui.ACCENT, true);
        memChip.setGravity(Gravity.CENTER);
        memChip.setPadding(Ui.px(22), 0, Ui.px(22), 0);
        Ui.focusRing(memChip, Ui.ACCENT_BG, 999);
        memChip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(HomeActivity.this, MemoryActivity.class)); }
        });
        header.addView(memChip, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Ui.px(56)));

        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("HH:mm");
        clock.setTextColor(Ui.TEXT);
        clock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, Ui.px(34));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.leftMargin = Ui.px(28);
        header.addView(clock, clp);
        return header;
    }

    private void addTab(LinearLayout tabs, String label, boolean selected, View.OnClickListener click) {
        TextView t = Ui.text(this, label, 24, selected ? Ui.BG : Ui.TEXT_DIM, selected);
        t.setGravity(Gravity.CENTER);
        t.setPadding(Ui.px(28), 0, Ui.px(28), 0);
        Ui.focusRing(t, selected ? Ui.TEXT : Ui.BG, 999);
        t.setOnClickListener(click);
        Ui.add(tabs, t, ViewGroup.LayoutParams.WRAP_CONTENT, Ui.px(60), 12);
    }

    // ---------------------------------------------------------------- conteúdo

    private void rebuildContent(boolean focusFirst) {
        boolean appsVisible = appsView != null && appsView.getParent() != null;
        homeView = buildHome();
        appsView = buildAppsGrid();
        if (appsVisible) showApps(); else showHome();
        if (focusFirst && firstAppTile != null) firstAppTile.requestFocus();
    }

    private void showHome() {
        content.removeAllViews();
        content.addView(homeView);
    }

    private void showApps() {
        content.removeAllViews();
        content.addView(appsView);
    }

    private View buildHome() {
        LinearLayout home = Ui.col(this);

        // Destaque: TV ao vivo (não existe "continuar assistindo" no Android 4.2 desta TV)
        LinearLayout hero = Ui.row(this, 0);
        hero.setBackgroundDrawable(Ui.round(Ui.SURFACE, 28));
        LinearLayout heroText = Ui.col(this);
        heroText.setGravity(Gravity.BOTTOM);
        heroText.setPadding(Ui.px(64), Ui.px(48), Ui.px(64), Ui.px(48));
        Ui.add(heroText, Ui.text(this, "TV AO VIVO", 20, Ui.ACCENT, true), -2, -2, 0);
        Ui.add(heroText, Ui.text(this, "Canais e entradas", 64, Ui.TEXT, true), -2, -2, 20);
        Ui.add(heroText, Ui.text(this, "TV digital ISDB-T, HDMI, AV e USB", 24, Ui.TEXT_DIM, false), -2, -2, 20);
        LinearLayout btns = Ui.row(this, 16);
        TextView watch = button("Assistir", true);
        watch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { launchPackage(PKG_TV); }
        });
        TextView input = button("Trocar entrada", false);
        input.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openInputs(); }
        });
        Ui.add(btns, watch, -2, Ui.px(64), 0);
        Ui.add(btns, input, -2, Ui.px(64), 16);
        Ui.add(heroText, btns, -2, -2, 32);
        hero.addView(heroText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        LinearLayout heroSide = Ui.col(this);
        heroSide.setGravity(Gravity.CENTER);
        heroSide.setBackgroundDrawable(Ui.round(Ui.SURFACE_2, 28));
        dateText = Ui.text(this, "", 44, Ui.TEXT, true);
        dateText.setGravity(Gravity.CENTER);
        heroSide.addView(dateText);
        hero.addView(heroSide, new LinearLayout.LayoutParams(Ui.px(760), ViewGroup.LayoutParams.MATCH_PARENT));
        Ui.add(home, hero, ViewGroup.LayoutParams.MATCH_PARENT, Ui.px(400), 0);

        // Seus apps
        Ui.add(home, Ui.text(this, "Seus apps", 30, Ui.TEXT, true), -2, -2, 36);
        LinearLayout appsRow = Ui.row(this, 24);
        appsRow.setPadding(Ui.px(8), Ui.px(12), Ui.px(8), Ui.px(12));
        List<ResolveInfo> apps = launchableApps();
        firstAppTile = null;
        int n = Math.min(apps.size(), 10);
        for (int i = 0; i < n; i++) {
            View tile = appTile(apps.get(i), i, 220, 124);
            if (i == 0) firstAppTile = tile;
            Ui.add(appsRow, tile, Ui.px(220), Ui.px(124), 24);
        }
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipChildren(false);
        hs.addView(appsRow);
        Ui.add(home, hs, ViewGroup.LayoutParams.MATCH_PARENT, -2, 8);

        // Mídia (telas do app Multimídia da TV)
        Ui.add(home, Ui.text(this, "Mídia", 30, Ui.TEXT, true), -2, -2, 24);
        LinearLayout media = Ui.row(this, 24);
        media.setPadding(Ui.px(8), Ui.px(12), Ui.px(8), Ui.px(12));
        String[][] mm = {
            {"Vídeos", "Do pen drive ou cartão", ".video.VideoDiskActivity"},
            {"Fotos", "Do pen drive ou cartão", ".photo.PhotoDiskActivity"},
            {"Músicas", "Do pen drive ou cartão", ".music.MusicDiskActivity"},
            {"Arquivos", "Todos os arquivos", ".filemanager.FileDiskActivity"},
            {"Rede", "Pastas compartilhadas", ".samba.SambaMainActivity"}
        };
        for (final String[] m : mm) {
            LinearLayout card = Ui.col(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(Ui.px(24), 0, Ui.px(24), 0);
            Ui.focusRing(card, Ui.SURFACE, 18);
            Ui.add(card, Ui.text(this, m[0], 22, Ui.TEXT, true), -2, -2, 0);
            Ui.add(card, Ui.text(this, m[1], 18, Ui.TEXT_MUTED, false), -2, -2, 6);
            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    launch(new ComponentName(PKG_MM, PKG_MM + m[2]), m[0]);
                }
            });
            Ui.add(media, card, Ui.px(300), Ui.px(110), 24);
        }
        Ui.add(home, media, -2, -2, 8);
        return home;
    }

    private View buildAppsGrid() {
        ScrollView sv = new ScrollView(this);
        LinearLayout grid = Ui.col(this);
        grid.setPadding(Ui.px(8), Ui.px(12), Ui.px(8), Ui.px(12));
        Ui.add(grid, Ui.text(this, "Todos os apps", 30, Ui.TEXT, true), -2, -2, 0);
        List<ResolveInfo> apps = launchableApps();
        LinearLayout row = null;
        for (int i = 0; i < apps.size(); i++) {
            if (i % 6 == 0) {
                row = Ui.row(this, 24);
                Ui.add(grid, row, -2, -2, 28);
            }
            Ui.add(row, appTile(apps.get(i), i, 260, 140), Ui.px(260), Ui.px(140), 24);
        }
        sv.addView(grid);
        return sv;
    }

    private TextView button(String label, boolean primary) {
        TextView t = Ui.text(this, label, 24, primary ? Ui.BG : Ui.TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(Ui.px(36), 0, Ui.px(36), 0);
        if (primary) Ui.focusRing(t, Ui.TEXT, 999);
        else Ui.focusRing(t, Ui.SURFACE_2, 999);
        return t;
    }

    private View appTile(final ResolveInfo ri, int index, int w, int h) {
        LinearLayout tile = Ui.col(this);
        tile.setGravity(Gravity.BOTTOM);
        tile.setPadding(Ui.px(20), Ui.px(16), Ui.px(20), Ui.px(18));
        Ui.focusRing(tile, Ui.APP_COLORS[index % Ui.APP_COLORS.length], 18);
        TextView name = Ui.text(this, label(ri), 22, 0xFFFFFFFF, true);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tile.addView(name);
        tile.setContentDescription(label(ri));
        tile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                launch(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name), label(ri));
            }
        });
        return tile;
    }

    // ---------------------------------------------------------------- apps

    private List<ResolveInfo> launchableApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(main, 0);
        // Uma entrada por pacote (o app Multimídia tem 5 atividades; elas ficam na fileira "Mídia")
        List<ResolveInfo> out = new ArrayList<ResolveInfo>();
        List<String> seen = new ArrayList<String>();
        for (ResolveInfo ri : all) {
            String p = ri.activityInfo.packageName;
            if (p.equals(getPackageName()) || p.equals(PKG_MM) || seen.contains(p)) continue;
            seen.add(p);
            out.add(ri);
        }
        final PackageManager fpm = pm;
        Collections.sort(out, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                int ra = rank(a.activityInfo.packageName), rb = rank(b.activityInfo.packageName);
                if (ra != rb) return ra - rb;
                return String.valueOf(a.loadLabel(fpm)).compareToIgnoreCase(String.valueOf(b.loadLabel(fpm)));
            }
        });
        return out;
    }

    private static int rank(String pkg) {
        for (int i = 0; i < PREFERRED.length; i++) if (PREFERRED[i].equals(pkg)) return i;
        return PREFERRED.length;
    }

    private String label(ResolveInfo ri) {
        for (String[] n : NAMES) if (n[0].equals(ri.activityInfo.packageName)) return n[1];
        return String.valueOf(ri.loadLabel(getPackageManager()));
    }

    private void launch(ComponentName cn, String what) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setComponent(cn);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir: " + what, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchPackage(String pkg) {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) {
            Toast.makeText(this, "App não encontrado nesta TV", Toast.LENGTH_SHORT).show();
            return;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(i); } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o app", Toast.LENGTH_SHORT).show();
        }
    }

    /** A tela de fontes da Konka pode não aceitar chamadas de outros apps; aí abre a TV e orienta. */
    private void openInputs() {
        try {
            Intent i = new Intent();
            i.setComponent(new ComponentName(PKG_TV, PKG_TV + ".popup.InputActivity"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            launchPackage(PKG_TV);
            Toast.makeText(this, "Use o botão SOURCE/INPUT do controle para trocar a entrada", Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------------------------------------------------- estado

    private final Runnable tick = new Runnable() {
        @Override public void run() { refreshStatus(); }
    };

    private void refreshStatus() {
        Mem m = Mem.read();
        memChip.setText(m.swapOn() ? "Memória extra ativa" : "Memória");
        if (dateText != null) {
            dateText.setText(new SimpleDateFormat("EEEE\nd 'de' MMMM", new Locale("pt", "BR")).format(new Date()));
        }
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 30000);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(tick);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (appsView != null && appsView.getParent() != null) showHome();
            return true; // o launcher não fecha com "Voltar"
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showHome(); // botão HOME volta para o Início
        if (firstAppTile != null) firstAppTile.requestFocus();
    }
}
