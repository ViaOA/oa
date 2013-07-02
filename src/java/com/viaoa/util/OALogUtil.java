/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
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
