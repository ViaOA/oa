package com.viaoa.comm.io;

import java.io.*;

import org.junit.Test;

import com.viaoa.util.OAFile;

import test.xice.tsac.model.oa.Server;

public class OAObjectInputStreamTest {

    @Test
    public void test() throws Exception {

        File file = new File(OAFile.convertFileName("test.bin"));
        OAFile.mkdirsForFile(file);
        
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream oout = new ObjectOutputStream(out);

        Server server = new Server();
        server.setName("serverName");

        oout.writeObject(server);
        oout.flush();
        oout.close();
        
        
        FileInputStream fis = new FileInputStream(file);

        String s = "com.xice.tsac.model.oa";
        OAObjectInputStream ois = new OAObjectInputStream(new FileInputStream(file), s, s);
        ois.replaceClassName("Server", "SiloX");
        

        // read the object and print the string
        Object obj = ois.readObject();

        System.out.println("done "+obj);
    }
    
    public static void main(String[] args) throws Exception {
        OAObjectInputStreamTest test = new OAObjectInputStreamTest();
        test.test();
    }
}
