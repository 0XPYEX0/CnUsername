package me.xpyex.module.cnusername;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Logging {
    private static final String PREFIX = "[CnUsername] ";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static Logger LOGGER;

    public static void info(String s) {
        if (LOGGER == null) {
            System.out.println("[" + FORMAT.format(new Date()) + " INFO]: " + PREFIX + s);
            return;
        }
        getLogger().info(PREFIX + s);
        //
    }

    public static void warning(String s) {
        if (LOGGER == null) {
            System.out.println("[" + FORMAT.format(new Date()) + " WARN]: " + PREFIX + s);
            return;
        }
        getLogger().warning(PREFIX + s);
        //
    }

    public static Logger getLogger() {
        return LOGGER;
        //
    }

    public static void setLogger(Logger logger) {
        LOGGER = logger;
        //
    }
}
