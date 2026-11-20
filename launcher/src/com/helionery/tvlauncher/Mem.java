package com.helionery.tvlauncher;

import java.io.BufferedReader;
import java.io.FileReader;

/** Lê /proc/meminfo e /proc/swaps (não precisa de root). Valores em MB. */
final class Mem {
    long ramTotal, ramFree, swapTotal, swapFree;
    String swapFile = "";
    String swappiness = "?";

    static Mem read() {
        Mem m = new Mem();
        long memFree = 0, buffers = 0, cached = 0;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader("/proc/meminfo"));
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\s+");
                if (p.length < 2) continue;
                long kb = parse(p[1]);
                if (line.startsWith("MemTotal:")) m.ramTotal = kb / 1024;
                else if (line.startsWith("MemFree:")) memFree = kb;
                else if (line.startsWith("Buffers:")) buffers = kb;
                else if (line.startsWith("Cached:")) cached = kb;
                else if (line.startsWith("SwapTotal:")) m.swapTotal = kb / 1024;
                else if (line.startsWith("SwapFree:")) m.swapFree = kb / 1024;
            }
        } catch (Exception ignored) {
        } finally { close(r); }
        // Kernel 3.1 não tem MemAvailable: livre ≈ MemFree + Buffers + Cached
        m.ramFree = (memFree + buffers + cached) / 1024;

        r = null;
        try {
            r = new BufferedReader(new FileReader("/proc/swaps"));
            r.readLine(); // cabeçalho
            String line = r.readLine();
            if (line != null) m.swapFile = line.trim().split("\\s+")[0];
        } catch (Exception ignored) {
        } finally { close(r); }

        r = null;
        try {
            r = new BufferedReader(new FileReader("/proc/sys/vm/swappiness"));
            String s = r.readLine();
            if (s != null) m.swappiness = s.trim();
        } catch (Exception ignored) {
        } finally { close(r); }
        return m;
    }

    boolean swapOn() { return swapTotal > 0; }

    private static long parse(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0; }
    }

    private static void close(BufferedReader r) {
        if (r != null) try { r.close(); } catch (Exception ignored) {}
    }
}
