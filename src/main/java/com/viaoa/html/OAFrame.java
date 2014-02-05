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
import java.io.*;

public class OAFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    OAFrameSet frameSet;  
    protected String target; // set by frameset.add(), same as frameset target
    private String url; // forward url
    protected boolean bChanged;
    protected Stack stack = new Stack();
    protected OASession session;
    
    
    public OAFrame(String url) {
        setUrl(url);
    }

    /** remove all previous urls from this frame. */
    public void removeAllPreviousUrls() {
        stack.removeAllElements();
    }
    
    /** used to goto previous form.  If there is not a previous form, then this will do recursive calls
        to the frameSet's form and call performReturn(). 
    */
    public void setUrlToPrevious() {
        String s = null;
        if (stack.size() > 0) s = (String) stack.pop();  // always leave one in frame
        if (s != null) {
            bChanged = true;
            this.url = null;
            setUrl(s,true);
            if (session != null) session.storeFrameUrl(this.url, this);
        }
        else {
            if (frameSet != null) {
                OAFrame frm = frameSet.frame;
                if (frm != null) frm.setUrlToPrevious();
            }
        }
    }

    /** set return url back to a previous URL (page).  
        If not found, then this will do recursive calls to the frameSet's form and call rewindReturnUrl(). 
        If page is not found, then all previous pages will be removed
        and Url will be set up as the return url.
    */
    public void setUrlToPrevious(String url) {
        for ( ;!stack.empty();) {
            String s = (String) stack.pop();
            if (s.equalsIgnoreCase(url)) return;
        }
        if (frameSet != null) frameSet.frame.setUrlToPrevious(url);
        else stack.push(url);
    }

    /** returns url of previous page.  If this frame does not have a previous url, then it will
        (recursively) go to the frame of the frameset that this frame is a memeber.
    */
    public String getPreviousUrl() {
        if (!stack.empty()) return (String) stack.peek();
        if (frameSet != null && frameSet.frame != null) return frameSet.frame.getPreviousUrl();
        return null;
    }
    
    /** url of page.
        @see OAFrame#getUrlValue
    */
    public String getUrl() {
        return url;
    }
    /** @param url of page to display. 
    */
    public void setUrl(String url) {
        OASession sess = null;
        if (session == null && frameSet != null && frameSet.getSession() != null) sess = frameSet.getSession();
        setUrl(url, sess, false);
    }
    
    protected void setUrl(String url, boolean bPrevious) {
        OASession sess = null;
        if (session == null && frameSet != null && frameSet.getSession() != null) sess = frameSet.getSession();
        setUrl(url, sess,true);
    }    
    
    public void setUrl(String url, OASession sess) {
        setUrl(url,sess,false);
    }
    protected void setUrl(String url, OASession sess, boolean bPrevious) {
        // first see if url is the return url
        if (sess != null) this.session = sess;
        if (!stack.empty()) {
            String s = (String) stack.peek();
            if (s != null && s.equalsIgnoreCase(url)) {
                setUrlToPrevious();
                return;
            }
        }
        if (this.url != null && !bPrevious && !this.url.equalsIgnoreCase(url)) {
            bChanged = true;
            stack.push(this.url);
        }
        this.url = url;
        if (session != null && url != null) session.storeFrameUrl(url, this);
        // otherwise FrameSet will do this
    }


    
    /** stack of previous urls (Strings) where position is oldest and stack.size()-1 is active. */
    public Vector getStack() {
        return stack;
    }
    
    public void setChanged(boolean b) {
        bChanged = b;
    }
    public boolean getChanged() {
        return bChanged;
    }
    
    /** set by frameset.add() */
    protected void setFrameSet(OAFrameSet fs) {
        frameSet = fs;
        setUrl(url);
    }        
    public OAFrameSet getFrameSet() {
        return frameSet;
    }

    /** returns the string needed within the FRAME tags for the page url.
        This will also make sure that the URL is safe and wont cause an endless loop.
    */
    public String getUrlValue() {

        // qqqqqqqq to do:
        // make sure frame does not point to a frameset that will cause an endless loop

        // make sure form/frameset knows the frame it is in
        setUrl(url);
        return url;  
    }

}

