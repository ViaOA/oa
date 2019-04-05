// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface
public interface RemoteSpellCheckInterface {

    public final static String BindName = "RemoteSpellCheck";
    
    public String[] getMatchingWords(String word);
    public String[] getSoundexMatchingWords(String word);
    public void addNewWord(String word);
    public boolean isWordFound(String word);
    
}
