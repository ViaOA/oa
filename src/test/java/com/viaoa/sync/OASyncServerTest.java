package com.viaoa.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.theicetest.tsactest2.delegate.ModelDelegate;
import com.theicetest.tsactest2.model.oa.cs.ServerRoot;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.util.*;

/**
 *  Run this manually to then run Client junit tests.
 */
public class OASyncServerTest {
    private OASyncServer syncServer;
    private int port = 1099;
    private ServerRoot serverRoot;    
    
    public void serverTest() {
       int xx = 4;
       xx++;
    }
    
    public static void init() throws Exception {
        OASyncServerTest control = new OASyncServerTest();
        control.start();
    }
    
    public OASyncServer getSyncServer() {
        if (syncServer != null) return syncServer;
        syncServer = new OASyncServer(port) {
            @Override
            protected void onClientConnect(Socket socket, int connectionId) {
                super.onClientConnect(socket, connectionId);
                OASyncServerTest.this.onClientConnect(socket, connectionId);
            }
            @Override
            protected void onClientDisconnect(int connectionId) {
                super.onClientDisconnect(connectionId);
                OASyncServerTest.this.onClientDisconnect(connectionId);
            }
        };
        return syncServer;
    }    
    protected void onClientConnect(Socket socket, int connectionId) {
        // TODO Auto-generated method stub
    }

    protected void onClientDisconnect(int connectionId) {
        // TODO Auto-generated method stub
    }


    public void stop() throws Exception {
        if (syncServer != null) {
            syncServer.stop();
        }
    }
    public void start() throws Exception {
        readSerializeFromFile();
        
        // ServerRoot
        ModelDelegate.initialize(serverRoot, null);

        getSyncServer().start();
    }

    private boolean readSerializeFromFile() throws Exception {
        File file = new File(OAFile.convertFileName("runtime/test/tsactest2/data.bin"));
        if (!file.exists()) {
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
    
        Inflater inflater = new Inflater();
        InflaterInputStream inflaterInputStream = new InflaterInputStream(fis, inflater, 1024*3);
        
        ObjectInputStream ois = new ObjectInputStream(inflaterInputStream) {
            @Override
            protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                ObjectStreamClass cd = super.readClassDescriptor();
                try {
                    Field f = cd.getClass().getDeclaredField("name");
                    f.setAccessible(true);
                    String name = (String) f.get(cd);
                    String name2 = OAString.convert(name, ".tsac.", ".tsactest2.");
                    f.set(cd, name2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return cd;
            }
        };
        
        OAObjectSerializer wrap = (OAObjectSerializer) ois.readObject(); 
        serverRoot = (ServerRoot) wrap.getObject();
        String version = (String) ois.readObject();
        
        ois.close();
        fis.close();

        return true;
    }

    public static void main(String[] args) throws Exception {
        OASyncServerTest test = new OASyncServerTest();
        test.init();
        for (int i=1;;i++) {
            System.out.println(i+") ServerTest is running "+(new OATime()));
            Thread.sleep(30 * 1000);
        }
    }
    
}
