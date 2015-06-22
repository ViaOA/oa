package com.theice.tsac.delegate.oa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


import com.theice.tsac.model.oa.Application;
import com.theice.tsac.model.oa.ApplicationType;
import com.theice.tsac.model.oa.MRADClient;
import com.theice.tsac.model.oa.MRADClientCommand;
import com.theice.tsac.model.oa.SSHExecute;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

public class SSHExecuteDelegate {

    private static Logger LOG = Logger.getLogger(SSHExecuteDelegate.class.getName());
    
    private static final String USERNAME = "helpdesk";
    private static final String PASSWORD = "h0td0gstand";
    private static final String sshKeyFileName = "/home/helpdesk/.ssh/id_rsa";

    private static boolean bFileExists;
    private static AtomicInteger aiCnt = new AtomicInteger(1); 
    
    /**
     * Use SSH to run a command for an MRADClient.
     */
    public static SSHExecute runCommand(final MRADClientCommand mradClientCommand, final String command) throws Exception {
        if (mradClientCommand == null || OAString.isEmpty(command)) return null;
        MRADClient client = mradClientCommand.getMRADClient();
        if (client == null) return null;

        final int id = aiCnt.getAndIncrement();
        
        final SSHExecute sshExecute = new SSHExecute();
        return sshExecute;
    }

}
