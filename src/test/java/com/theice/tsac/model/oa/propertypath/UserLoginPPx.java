// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class UserLoginPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public UserLoginPPx(String name) {
        this(null, name);
    }

    public UserLoginPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null && name.length() > 0) {
            if (s.length() > 0 && name.charAt(0) != ':') s += ".";
            s += name;
        }
        pp = s;
    }

    public ClientAppTypePPx clientAppType() {
        ClientAppTypePPx ppx = new ClientAppTypePPx(this, UserLogin.P_ClientAppType);
        return ppx;
    }

    public LLADClientPPx lladClient() {
        LLADClientPPx ppx = new LLADClientPPx(this, UserLogin.P_LLADClient);
        return ppx;
    }

    public LoginTypePPx loginType() {
        LoginTypePPx ppx = new LoginTypePPx(this, UserLogin.P_LoginType);
        return ppx;
    }

    public UserPPx user() {
        UserPPx ppx = new UserPPx(this, UserLogin.P_User);
        return ppx;
    }

    public String id() {
        return pp + "." + UserLogin.P_Id;
    }

    public String login() {
        return pp + "." + UserLogin.P_Login;
    }

    public String gateway() {
        return pp + "." + UserLogin.P_Gateway;
    }

    public String password() {
        return pp + "." + UserLogin.P_Password;
    }

    public String forceLogout() {
        return pp + ".forceLogout";
    }

    public String refreshUserCache() {
        return pp + ".refreshUserCache";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
