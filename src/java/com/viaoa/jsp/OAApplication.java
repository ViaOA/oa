/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
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
    
    @Override
    public String[] getMessages() {
        int x = alMessage.size();
        String[] s = new String[x];
        alMessage.toArray(s);
        return s;
    }
    public void clearMessages() {
        alMessage.clear();
    }
    @Override
    public String[] getErrors() {
        int x = alError.size();
        String[] s = new String[x];
        alError.toArray(s);
        return s;
    }
    public void clearErrorMessages() {
        alError.clear();
    }
    @Override
    public String[] getHiddenMessages() {
        int x = alHidden.size();
        String[] s = new String[x];
        alHidden.toArray(s);
        return s;
    }
    public void clearHiddenMessages() {
        alHidden.clear();
    }
}

