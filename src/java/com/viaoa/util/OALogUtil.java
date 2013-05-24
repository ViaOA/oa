package com.viaoa.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OALogUtil {

    public static void disable() {
        Logger log = Logger.getLogger("");
        log.setLevel(Level.OFF);        
        Handler[] hs = log.getHandlers();
        for (int i=0; hs != null && i<hs.length; i++) {
            hs[i].setLevel(Level.OFF);
        }
    }
    public static void consoleOnly(Level level) {
        consoleOnly(level, "");
    }    
    public static void consoleOnly(Level level, String name) {
        Logger log = Logger.getLogger("");
        log.setLevel(Level.OFF);        

        Handler[] hs = log.getHandlers();
        for (int i=0; hs != null && i<hs.length; i++) {
            hs[i].setLevel(Level.OFF);
            log.removeHandler(hs[i]);
        }

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(level);

        log = Logger.getLogger(name);
        log.setLevel(level);
        log.addHandler(ch);
    }

}
