// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.remote;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import test.xice.tsam.remote.RemoteFileImpl;
import test.xice.tsam.remote.RemoteFileInterface;
import com.viaoa.util.OAString;

public class RemoteFileImpl implements RemoteFileInterface {
    private static Logger LOG = Logger.getLogger(RemoteFileImpl.class.getName());
    
    
    @Override
    public boolean saveFile(File file, byte[] bytes) {
        LOG.fine("file="+file);
        boolean b = false;
        if (bytes != null) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.write(bytes);
                out.flush();
                out.close();
                b = true;
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "exception saving file=" + file.getAbsolutePath(), e);
            }
        }
        return b;
    }

    @Override
    public long getFileTime(String fname) {
        LOG.fine("fname="+fname);
        fname = OAString.convertFileName(fname);
        File f = new File(fname);
        if (f.exists()) {
            return new Long(f.lastModified());
        }
        return -1;
    }

    @Override
    public boolean getDoesFileExist(String fname) {
        LOG.fine("fname="+fname);
        File file = new File(fname);
        return file.exists();
    }

    @Override
    public byte[] getFile(String fname) {
        LOG.fine("fname="+fname);
        if (fname == null) return null;
        fname = OAString.convertFileName(fname);
        File f = new File(fname);
        byte[] bytes = null;
        if (f.exists() && !f.isDirectory()) {
            bytes = new byte[(int)f.length()];
            try {
                FileInputStream in = new FileInputStream(f);
                in.read(bytes);
                in.close();
            }
            catch (Exception e) {
                bytes = null;
                LOG.log(Level.WARNING, "exception geting file=" + f.getAbsolutePath(), e);
            }
        }
        return bytes;
    }

    @Override
    public byte[] getNewerFile(String fname, long ltime) {
        LOG.fine("fname="+fname);
        fname = OAString.convertFileName(fname);
        File f = new File(fname);
        if (f.exists() && !f.isDirectory() && f.lastModified() > ltime) {
            return getFile(fname);
        }
        return null;
    }

    @Override
    public boolean deleteFile(String fname) {
        LOG.fine("fname="+fname);
        fname = OAString.convertFileName(fname);
        File f = new File(fname);
        boolean b = false;
        if (f.exists() && !f.isDirectory()) {
            f.delete();
            b = true;
        }
        return b;
    }
}
