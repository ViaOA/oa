/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jsp;

import java.util.*;
import java.io.*;

import javax.servlet.http.HttpSession;

import com.viaoa.hub.*;

/**
 * Application level object.
 * @author vvia
 *
 */
public class OAApplication extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String url;
    protected String name;

    public OAApplication() {
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    
    public OASession createSession() {
        OASession s = new OASession();
        s.setApplication(this);
        return s;
    }

    /** Used by oaheader.jsp. 
        This will make sure that the correct oasession is used for applicationName.
        The main purpose for this is for systems that have multiple applications running.
    */
    public OASession getSession(HttpSession session) {
        OASession oasession = (OASession) session.getAttribute(this.getName()+".OA");
        if (oasession == null) {
            oasession = createSession();
            session.setAttribute(this.getName()+".OA", oasession);
        }
        return oasession;
    }

    public void removeSession(HttpSession session) {
        session.removeAttribute(this.getName()+".OA");
    }
    
}

