// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class LoginImagePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ImageStorePPx imageStore;
    private LoginImageSetPPx loginImageSet;
     
    public LoginImagePPx(String name) {
        this(null, name);
    }

    public LoginImagePPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public ImageStorePPx imageStore() {
        if (imageStore == null) imageStore = new ImageStorePPx(this, LoginImage.P_ImageStore);
        return imageStore;
    }

    public LoginImageSetPPx loginImageSet() {
        if (loginImageSet == null) loginImageSet = new LoginImageSetPPx(this, LoginImage.P_LoginImageSet);
        return loginImageSet;
    }

    public String id() {
        return pp + "." + LoginImage.P_Id;
    }

    public String location() {
        return pp + "." + LoginImage.P_Location;
    }

    public String xPosition() {
        return pp + "." + LoginImage.P_XPosition;
    }

    public String yPosition() {
        return pp + "." + LoginImage.P_YPosition;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
