package com.viaoa.sync.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.sync.OASync;
import com.viaoa.util.OAFile;

/**
 * Allows client apps to upload or download files to server, under a specific directory only.
 * Used by OASyncServer to allow clients to use.
 * @author vvia
 */
public class ServerFile {
    private static Logger LOG = Logger.getLogger(ServerFile.class.getName());
    
    public static final String FileUpload = "fileUpload";
    public static final String FileDownload = "fileDownload";
    
    private final String directory;
    private ServerSocket ssUpload;
    private ServerSocket ssDownload;
    
    public ServerFile(String directory) {
        this.directory = directory;
    }

    private final AtomicBoolean abStart = new AtomicBoolean(); 
    public void start() {
        if (!abStart.compareAndSet(false, true)) return;
        LOG.fine("Starting");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startUploadServerSocket();
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "exception in ServerFile.uploadServerSocket", e);
                }
                
            }
        }, "UploadServerSocket");
        t.setDaemon(true);
        t.start();
        
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startDownloadServerSocket();
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "exception in ServerFile.downloadServerSocket", e);
                }
                
            }
        }, "DownloadServerSocket");
        t.setDaemon(true);
        t.start();
        LOG.fine("start completed, started 2 threads");
    }
    
    public void stop() {
        if (!abStart.compareAndSet(true, false)) return;
        try {
            ssUpload.close();
            ssDownload.close();
        }
        catch (Exception e) {
        }
        ssUpload = null;
        ssDownload = null;
    }
    
    
    protected void startDownloadServerSocket() throws Exception {
        LOG.fine("Starting");
        ssDownload = OASync.getSyncServer().getMultiplexerServer().createServerSocket(FileDownload);
        for ( ; abStart.get(); ) {
            final Socket socket = ssDownload.accept();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        downloadFile(socket);
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "ServerFile.fileDownload exception", e);
                    }
                }
            }, "DownloadFileSocket").start();
        }
    }

    /**
     * download (send) a server file to client
     */
    public void downloadFile(Socket socket) throws Exception {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        
        String fn = dis.readUTF();
        LOG.fine("requesting download, file="+fn);
        fn = directory + "/" + fn;
        fn = OAFile.convertFileName(fn);
        File file = new File(fn);

        if (fn.indexOf("..") >= 0) {
            LOG.fine("requesting download, illegal directory, file="+fn);
            dos.writeInt(0);
        }
        else if (!file.exists()) {
            LOG.fine("requesting download, file does not exist, file="+fn);
            dos.writeInt(1);
        }
        else {
            dos.writeInt(2);  // valid
            dos.flush();
            LOG.fine("requesting download starting, file="+fn);
            
            final BufferedInputStream bisFile = new BufferedInputStream(new FileInputStream(file));
            final byte[] bs = new byte[8196];
     
            int total = 0;
            for (int i=0; ;i++) {
                int x = bisFile.read(bs, 0, bs.length);
                dos.writeInt(Math.max(x, 0));
                if (x <= 0) break;
                dos.write(bs, 0, x);
                total += x;
            }
            bisFile.close();
            LOG.fine("requesting download completed, file="+fn+", size="+total);
        }
        dos.flush();
        dis.readInt();

        dis.close();
        dos.close();
        socket.close();
    }
    

    protected void startUploadServerSocket() throws Exception {
        LOG.fine("Starting");
        ssUpload = OASync.getSyncServer().getMultiplexerServer().createServerSocket(FileUpload);
        for ( ; abStart.get(); ) {
            final Socket socket = ssUpload.accept();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        uploadFile(socket);
                    }
                    catch (Exception e) {
                        // TODO: handle exception
                        LOG.log(Level.WARNING, "ServerFile.fileUpload exception", e);
                    }
                }
            }, "UploadFileSocket").start();
        }
    }
    /** 
     * save file from client to server.
     */
    public void uploadFile(Socket socket) throws Exception {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        String fn = dis.readUTF();
        LOG.fine("requesting upload, file="+fn);
        fn = OAFile.convertFileName(directory + "/" + fn);
        LOG.fine("requesting upload, filePath="+fn);

        if (fn.indexOf("..") >= 0) {
            LOG.fine("requesting upload, illegal directory, file="+fn);
            dos.writeInt(0);
        }
        else { 
            OAFile.mkdirsForFile(fn);
            File fileSaveAs = new File(fn);
            LOG.fine("requesting download starting, file="+fn);
            dos.writeInt(1);
            dos.flush();
            
            if (fileSaveAs.exists()) fileSaveAs.delete();
            fileSaveAs.createNewFile();
    
            FileOutputStream osFile = new FileOutputStream(fileSaveAs);
            final byte[] bs = new byte[8196];
            
            int total = 0;
            for (int i=0;;i++) {
                final int xAmt = dis.readInt();
                if (xAmt <= 0) break;
                
                int tot = 0;
                for ( ;tot < xAmt; ) {
                    int x = Math.min(bs.length, xAmt-tot);
                    x = dis.read(bs, 0, x);
                    osFile.write(bs, 0, x);
                    tot += x;
                }
                total += xAmt;
            }
            osFile.close();
            LOG.fine("requesting download completed, file="+fn+", size="+total);
        }
        dos.flush();
        dos.writeInt(0);
        dos.flush();
        
        dis.close();
        dos.close();
        socket.close();
    }
}
