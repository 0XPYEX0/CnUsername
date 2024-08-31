package me.xpyex.module.cnusername;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Logging {
    private static final String PREFIX = "§b[CnUsername] §r";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final AtomicReference<Logger> LOGGER = new AtomicReference<>();

    public static void info(String s) {
        if (LOGGER.get() == null) {
            System.out.println(ColoredConsole.toANSI("[" + FORMAT.format(new Date()) + " §aINFO§r]: " + PREFIX + s));
            return;
        }
        getLogger().info(ColoredConsole.toANSI(PREFIX + s));
    }

    public static void warning(String s) {
        if (LOGGER.get() == null) {
            System.out.println(ColoredConsole.toANSI("[" + FORMAT.format(new Date()) + " §eWARN§r]: " + PREFIX + s));
            return;
        }
        getLogger().warning(ColoredConsole.toANSI(PREFIX + s));
    }

    public static Logger getLogger() {
        return LOGGER.get();
        //
    }

    public static void setLogger(Logger logger) {
        LOGGER.set(logger);
        //
    }

    public static class ColoredConsole {
        private static final HashMap<String, String> MCColorToANSI = new HashMap<>();
        static {
                MCColorToANSI.put("§0", "\u001B[30m");
                MCColorToANSI.put("§1", "\u001B[34m");
                MCColorToANSI.put("§2", "\u001B[32m");
                MCColorToANSI.put("§3", "\u001B[36m");
                MCColorToANSI.put("§4", "\u001B[31m");
                MCColorToANSI.put("§5", "\u001B[35m");
                MCColorToANSI.put("§6", "\u001B[33m");
                MCColorToANSI.put("§7", "\u001B[37m");
                MCColorToANSI.put("§8", "\u001B[90m");
                MCColorToANSI.put("§9", "\u001B[94m");
                MCColorToANSI.put("§a", "\u001B[92m");
                MCColorToANSI.put("§b", "\u001B[96m");
                MCColorToANSI.put("§c", "\u001B[91m");
                MCColorToANSI.put("§d", "\u001B[95m");
                MCColorToANSI.put("§e", "\u001B[93m");
                MCColorToANSI.put("§f", "\u001B[97m");
                MCColorToANSI.put("§k", "\u001B[5m");
                MCColorToANSI.put("§l", "\u001B[1m");
                MCColorToANSI.put("§m", "\u001B[9m");
                MCColorToANSI.put("§n", "\u001B[4m");
                MCColorToANSI.put("§o", "\u001B[3m");
                MCColorToANSI.put("§r", "\u001B[0m");
        }

        public static String toANSI(String s) {
            if (s == null || s.isEmpty()) return "";
            final String[] out = {s};
            MCColorToANSI.forEach((mc, ansi) -> out[0] = out[0].replace(mc, ansi));
            return out[0];
        }
    }
}
