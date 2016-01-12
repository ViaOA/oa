package com.viaoa.comm.io;

import java.io.*;

import com.theicetest.tsac.model.oa.Server;
import com.viaoa.util.OAFile;

public class OAObjectInputStreamTest {

    
    public void test() throws Exception {

        File file = new File(OAFile.convertFileName("runtime/test/data.bin"));
        OAFile.mkdirsForFile(file);
        
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream oout = new ObjectOutputStream(out);

        Server server = new Server();
        server.setName("serverName");

        oout.writeObject(server);
        oout.flush();
        oout.close();
        
        
        FileInputStream fis = new FileInputStream(file);

        String s = "com.theice.tsac.model.oa";
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
