// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.remote;

import java.io.File;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface
public interface RemoteFileInterface {

    public final static String BindName = "RemoteFile";

    public boolean saveFile(File file, byte[] bytes);
    public long getFileTime(String fname);
    public boolean getDoesFileExist(String fname);
    public byte[] getFile(String fname);
    public byte[] getNewerFile(String fname, long ltime);
    public boolean deleteFile(String fname);

    
}
