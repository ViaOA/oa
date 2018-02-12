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

package com.viaoa.html;
import java.util.*;
import com.viaoa.hub.*;
import java.io.*;

public class OAFrameSet implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Hashtable hashtable = new Hashtable();
    protected String url; // forward url, same name used for oasession.getFrameSet("name")
    private OASession session; 
    protected OAFrame frame;
    protected boolean bChanged = true;
    protected String name;
    
    public OAFrameSet(String url) {
        if (url == null) throw new IllegalArgumentException("url cant be null");
        this.url = url;
    }

    /** 
        @param targetName is not case sensitive
    */
    public void putFrame(String targetName, OAFrame frame) {
        addFrame(targetName, frame);
    }
    public void put(String targetName, OAFrame frame) {
        addFrame(targetName, frame);
    }
    public void addFrame(String targetName, OAFrame frame) {
        if (targetName == null || targetName.length() == 0) throw new IllegalArgumentException("OAForm.add() targetUrl required");
        bChanged = true;
        hashtable.put(targetName.toUpperCase(), frame);
        frame.target = targetName;
        frame.setFrameSet(this);
    }

    public OAFrame[] getFrames() {
        int x = hashtable.size();
        OAFrame[] frames = new OAFrame[x];
        Enumeration enumx = hashtable.elements();
        for (int i=0 ; enumx.hasMoreElements(); i++) {
            frames[i] = (OAFrame) enumx.nextElement();
        }
        return frames;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String s) {
        this.name = s;
    }
    
    /** 
        @param targetName is not case sensitive
    */
    public OAFrame getFrame(String targetName) {
        return get(targetName);
    }
    public OAFrame get(String targetName) {
        OAFrame frm = null;
        if (targetName != null) frm = (OAFrame)hashtable.get(targetName.toUpperCase());
        // if (frm == null) throw new OAException(OAFrameSet.class, "Frame \""+targetName+"\" not found");
        return frm;
    }
    
    /** return the frame that this FrameSet is currently under.  */
    public OAFrame getFrame() {
        return frame;
    }

    /** 
        @param targetName is not case sensitive
    */
    public void removeFrame(String targetName) {
        if (targetName != null) hashtable.remove(targetName.toUpperCase());
        bChanged = true;
    }
    
    public String getUrl() {
        return url;
    }
    
    // set by OASession.add()
    protected void setSession(OASession s) {
        this.session = s;
        // all frames need to now register their form with session
        Enumeration enumx = hashtable.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAFrame f = (OAFrame) enumx.nextElement();
            f.setFrameSet(this);
        }
    }
    protected OASession getSession() {
        return session;
    }
    

    protected Vector vecListener = new Vector(3,3);
    public void addListener(OAHtmlListener l) {
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    public void removeListener(OAHtmlListener l) {
        vecListener.removeElement(l);
    }
    
    /** called by (oaform.jsp .. form .. frame) 
        @return forwardPage
    */
    public String processRequest(OASession session, OAForm form, String forwardUrl) {

        // call listeners
        int x = vecListener.size();        
        for (int i=0; i<x; i++) {
            OAHtmlListener l = (OAHtmlListener) vecListener.elementAt(i);
            forwardUrl = l.afterPost(form, forwardUrl);
            if (forwardUrl == null || forwardUrl.length() == 0) forwardUrl = form.getUrl();  // go back to this page
        }
        return forwardUrl;
    }
    
    protected boolean getChanged() {
        return (bChanged);
    }
    protected void setChanged(boolean b) {
        bChanged = b;
    }
}

