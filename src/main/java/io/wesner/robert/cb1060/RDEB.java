package io.wesner.robert.cb1060;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Recursion-Detection-Early-Break
 * Because betajank™
 */
public class RDEB {
    private static final int THRESHOLD = 10;
    
    private static final Map<String, Integer> tracker = new HashMap<>();

    public static void up() {
        String k = key(getCaller());
        Integer v = tracker.get(k);

        if (v == null) {
            v = 0;
        }

        tracker.put(k, v + 1);
    }

    public static void down() {
        String k = key(getCaller());
        Integer v = tracker.get(k);

        if (v == null) {
            v = 0;
        }

        tracker.put(k, Math.max(0, v - 1));
    }

    public static boolean shouldBreak() {
        String k = key(getCaller());
        Integer v = tracker.get(k);

        if (v == null || v < THRESHOLD) {
            return false;
        }

        tracker.remove(k);

        Bukkit.getLogger().severe("[RDEB_REPORT] Detected invalid recursion, please investigate!\n" + ExceptionUtils.getStackTrace(new Throwable()));

        return true;
    }

    private static StackTraceElement getCaller() {
        return Thread.currentThread().getStackTrace()[3];
    }

    private static String key(StackTraceElement element) {
        return element.getClassName() + "::" + element.getMethodName() + "()";
    }
}
