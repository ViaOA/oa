package com.viaoa.sync.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;
import com.viaoa.sync.OASync;
import com.viaoa.util.OAString;

/**
 * Send/receive files from ServerFile using multiplexer socket.
 * Used by OASyncClient
 * @author vvia
 */
public class ClientFile {
    
    private static Logger LOG = Logger.getLogger(ClientFile.class.getName());
 
    /**
     * Download file from server and save to file.
     */
    public boolean download(String fname, File fileSaveAs) throws Exception {
        LOG.fine("download fname="+fname+", save as file="+fileSaveAs);
        if (OAString.isEmpty(fname) || fileSaveAs == null) return false;
        final Socket socket = OASync.getSyncClient().getRemoteMultiplexerClient().getMultiplexerClient().createSocket(ServerFile.FileDownload);
        
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        dos.writeUTF(fname);
        dos.flush();
        
        if (fileSaveAs.exists()) fileSaveAs.delete();
        fileSaveAs.createNewFile();
        
        int x = dis.readInt();
        final boolean bValid = (x == 2);
        LOG.fine("download fname="+fname+", save as file="+fileSaveAs+", return value="+2+", valid="+bValid);

        if (bValid) {
            OutputStream osFile = new FileOutputStream(fileSaveAs);
            
            final byte[] bs = new byte[8196];
            int total = 0;
            for ( ; ; ) {
                final int xAmt = dis.readInt();
                if (xAmt <= 0) break;

                int tot = 0;
                for ( ;tot < xAmt; ) {
                    x = Math.min(bs.length, xAmt-tot);
                    x = dis.read(bs, 0, x);
                    osFile.write(bs, 0, x);
                    tot += x;
                }
                total += xAmt;
                status(total);
            }
            osFile.close();
            LOG.fine("download finished, fname="+fname+", save as file="+fileSaveAs+", size="+total);
        }
        dos.writeInt(0); // done
        dos.flush();
        
        dos.close();
        dis.close();
        socket.close();
        return bValid;
    }
    
    
    public boolean upload(String fname, File fileOpen) throws IOException {
        LOG.fine("upload to fname="+fname+", from file="+fileOpen);
        if (OAString.isEmpty(fname) || fileOpen == null) return false;
        Socket socket = OASync.getSyncClient().getRemoteMultiplexerClient().getMultiplexerClient().createSocket(ServerFile.FileUpload);

        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        
        dos.writeUTF(fname);
        dos.flush();

        final boolean bValid = (dis.readInt() == 1);
        LOG.fine("upload to fname="+fname+", from file="+fileOpen+", valid="+bValid);
        
        if (bValid) {
            final BufferedInputStream bisFile = new BufferedInputStream(new FileInputStream(fileOpen));
            final byte[] bs = new byte[8196];
            
            int total = 0;
            for (int i=0;;i++) {
                int x = bisFile.read(bs, 0, bs.length);
                dos.writeInt(Math.max(x, 0));
                if (x <= 0) break;
                dos.write(bs, 0, x);
                total += x;
                status(total);
            }
            bisFile.close();
            LOG.fine("upload finished, to fname="+fname+", from file="+fileOpen+", size="+total);
            dos.flush();
        }
        // wait for server to finsih reading
        dis.readInt();
        
        dos.close();
        dis.close();
        socket.close();
        return bValid;
    }
    
    protected void status(int x) {
    }
}
