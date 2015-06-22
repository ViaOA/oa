package com.theice.tsac.delegate.oa;

import java.io.File;
import com.theice.tsac.model.oa.Application;
import com.theice.tsac.model.oa.Server;
import com.theice.tsac.model.oa.ServerFile;
import com.viaoa.util.OADateTime;

public class ServerFileDelegate {
    
    public static void update(File[] files, Application application) {
        if (files == null || application == null) return;
        for (File f : files) {
            ServerFile sf = null;
            for (ServerFile sfx : application.getServerFiles()) {
                if (sfx.getFileName().equals(f.getName())) {
                    sf = sfx;
                    break;
                }
            }
            if (sf == null) {
                sf = new ServerFile();
                sf.setFileName(f.getName());
                application.getServerFiles().add(sf);
            }
            sf.setDateTime(new OADateTime(f.lastModified()));
            sf.setLength(f.length());
        }

        for (ServerFile sfx : application.getServerFiles()) {
            boolean b = false;
            for (File f : files) {
                if (sfx.getFileName().equals(f.getName())) {
                    b = true;
                    break;
                }
            }
            if (!b) application.getServerFiles().remove(sfx);
        }
    }
}
