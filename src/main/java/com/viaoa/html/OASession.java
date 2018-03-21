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

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.viaoa.hub.*;


/** Object used by one user.  This has methods for storing
    Hubs and misc objects, adding forms, adding message, and keeping track
    of last page used, nextUrl to go to.
    @see OAApplication
*/
public class OASession extends OABase {
    private static final long serialVersionUID = 1L;
    
    // the following will be passed off to the next OAForm used.  see OAForm
    protected transient Hashtable hashFrameUrl = new Hashtable();
    
    protected transient boolean bInit;
    protected transient OAForm lastForm;
    protected transient String nextUrl;
    protected transient OAApplication application;
    protected transient HttpServletRequest request;  // set by oaform.jsp
    protected transient HttpServletResponse response;
    protected transient OAForm currentForm;
    protected transient OAFrame frame;   // use setTopUrl() to change
    protected transient boolean bSubmitFlag; // toggle set/used by OAForm to know if javascript has been called
    protected transient Vector vecListener;
    protected transient ArrayList<OAForm> alBreadcrumbForm;

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }    
    
    public OASession() {
        OAApplication.addUser();
        frame = new OAFrame(null);  //  frame "_top" for browser        
        frame.session = this;
        frame.target = "_top";
    }
    protected void finalize() throws Throwable {
        OAApplication.removeUser();
        if (vecListener != null) {
            int x = vecListener.size();
            for (int i=0; i<x; i++) {
                ((OASessionListener) vecListener.elementAt(i)).onFinalize(this);
            }
        }
        super.finalize();
    }
    public void addListener(OASessionListener l) {
        if (l != null && (vecListener == null || !vecListener.contains(l)) ) {
            if (vecListener == null) vecListener = new Vector(3,3);
            vecListener.add(l);
        }
    }

    public OAForm getForm(OAForm form) {
        if (form == null) return null;
        OAForm formx = getForm(form.getUrl(),false,0);
        return formx;
    }


    /** Store/Retreive forms by name for this session. 
        @param url is not case sensitive
        @see OABase#get
    */
    public OAForm getForm(String url) {
        OAForm form = getForm(url,false,0);
        return form;
    }
    public OAForm getForm(String url, boolean bWait) {
        return getForm(url, bWait, 0);
    }
    private OAForm getForm(String url, boolean bWait, int waitCnt) {
        Object obj = get(url);
        if (obj == null || !(obj instanceof OAForm)) {
            if (bWait) {
                if (waitCnt > 10) return null;
                synchronized (this) {
                    try {
                        Thread.currentThread().sleep(250); // wait(5000) did not work, it throws an exception
                    }
                    catch (Exception e) {
                    }
                }
                return getForm(url, true, ++waitCnt);
            }
            return null;
        }
        return (OAForm) obj;
    }
    
    /** 
        Adds a form to oasession using the form's url as a key.
    */
    public void put(OAForm form) {
        if (form == null) return;
        super.put(form.getUrl(), form);
        form.session = this;
        assignFrame(form);  // this is used to assign a form (using it's url) to a frame that has already been created.
        getBreadcrumbForms().add(form); 
    }

    private ArrayList getBreadcrumbForms() {
        if (alBreadcrumbForm == null) {
            alBreadcrumbForm = new ArrayList<OAForm>(10);
        }
        return alBreadcrumbForm;
    }
    
    public void setLastBreadCrumbForm(OAForm f) {
    	int x = getBreadcrumbForms().indexOf(f);
        if (x < 0) alBreadcrumbForm.add(f);
        else {
	        while (alBreadcrumbForm.size() > (x+1)) {
	        	alBreadcrumbForm.remove(x+1);
	        }
        }
    }
    public void setBreadCrumbForms(OAForm f) {
        if (f == null) return;
        getBreadcrumbForms().clear();
        alBreadcrumbForm.add(f);
    }
    public void clearBreadCrumbForms() {
    	getBreadcrumbForms().clear();
    }
    
    public OAForm[] getBreadCrumbForms() {
    	return (OAForm[]) getBreadcrumbForms().toArray(new OAForm[getBreadcrumbForms().size()]);
    }
    
    
    /** @see OASession#put 
    */
    public void putForm(OAForm form) {
        this.put(form);
    }
    /**
        @see OASession#put
    */
    public void addForm(OAForm form) {
        // System.out.println("OASession.addForm() should be changed to put(form) or putForm(form)");
        this.put(form);
    }

    /** Set by OAForm.getAction()
        @see OASession#getForm
        @see OAForm#getAction
    */
    public OAForm getCurrentForm() {
        return currentForm;
    }
    public void setCurrentForm(OAForm f) {
        currentForm = f;
    }
    
    /** @see OABase#remove */
    public void removeForm(String name) {
        remove(name);
    }
    public void clearForms() {
        Enumeration enumx = hashtable.keys();
        for ( ; enumx.hasMoreElements(); ) {
            String s = (String) enumx.nextElement();
            if (get(s) instanceof OAForm) remove(s);
        }
    }

    public String getTopUrl() {
        return frame.getUrl();
    }
    
    /** top frame, for target "_top" */
    public OAFrame getFrame() {
        if (frame == null) {
            frame = new OAFrame(null);  //  frame "_top" for browser        
            frame.session = this;
            frame.target = "_top";
        }
        return frame;
    }

    /** Top page to display. */
    public void setTopUrl(String topUrl) {
        if (this.frame == null) return;
        if (!bTopWasFrameSet) {
            if (this.frame.getUrl() == null) {
                bTopWasFrameSet = (getFrameSet(topUrl) != null);
            }
            else bTopWasFrameSet = (getFrameSet(this.frame.getUrl()) != null);
        }
        this.frame.setUrl(topUrl, this);
    }
    
    private transient Vector vecJavaScript;
    private Vector getJavaScripts() {
        if (vecJavaScript == null) {
            vecJavaScript = new Vector(20,15);
        }
        return vecJavaScript;
    }

    /** same as calling:  form.getSession().getFrameSet("FS.jsp").getFrame("center").setUrl("center.jsp");
    */
    public void setFrameUrl(String frameSetUrl, String frameName, String url) {
        if (frameSetUrl == null) {
            // try to find using just the frameName
            if (frameName == null) return;
            OAFrame frm = this.getFrame(frameName);
            if (frm != null) frm.setUrl(url,this);
            return;
        }
        
        OAFrameSet fs = getFrameSet(frameSetUrl);
        if (fs != null) {
            OAFrame frm = fs.getFrame(frameName);
            if (frm != null) {
                frm.setUrl(url,this);
                return;
            }
        }
        // store until frameSet is added this.addFrameSet()
        if (frameSetUrl != null && frameName != null) {
            this.put("HOLD_"+frameSetUrl, new String[] { frameName, url } );
        }
        //was: throw new OAException(OASession.class,"setFrameUrl(\""+frameSetUrl+"\",\""+frameName+"\",\""+url+"\" not found");
    }

    protected OAFrame getFrame(String frameName) {
        return getFrame(frame, frameName);
    }
    private OAFrame getFrame(OAFrame frm, String frameName) {
        if (frm == null) return null;
        if (frm.target != null && frm.target.equalsIgnoreCase(frameName)) return frm;

        OAFrameSet fs = getFrameSet(frm.getUrl());
        if (fs != null) {
            OAFrame[] frms = fs.getFrames();
            for (int i=0; i<frms.length; i++) {
                OAFrame f = getFrame(frms[i], frameName);
                if (f != null) return f;
            }
        }
        return null;
    }







    public void submitForm(String fromUrl, String formName, String cmd) {
        OAForm form = getForm(fromUrl);
        if (form == null) return;
        String s = getSubmitFormJavaScript(formName,cmd);
        if (s != null) {
            getJavaScripts().addElement(s);
            if (form.frame != null) form.frame.setChanged(true);  // flag that will have frame refreshed
        }
    }

    protected String getSubmitFormJavaScript(String formName, String cmd) {
        if (formName == null || cmd == null) return null;
        OAForm form = getForm(formName,true);

        if (form == null) return null;
        OAFrame frm = form.frame;
        if (frm == null) return null;

        if (!frm.getUrl().equalsIgnoreCase(formName)) return null;

        OACommand oacmd = form.getCommand(cmd);
        if (oacmd == null) return null;

        // get path to forms from top
        String s = "";
        for ( ;frm != null; ) {
            OAFrameSet fs = frm.frameSet;
            if (fs == null) break;
            s = frm.target + "." + s;
            frm = fs.frame;
        }
        if (frm != frame) return null; // frame not visible

        s = "top." + s;
        String s2 = s + "document.forms[0].";

        // 06/12/01  
        String js = s2 + "action = 'oaform.jsp?"+oacmd.getName()+"=1'; "+s+"setOA();";
        js += s2 + "submit();";

        return js;
    }
  
  
    boolean bTopWasFrameSet;  // need to know if the old top page was a frameset so that "_top" could be reset
    /** called by oaform.jsp to get any javascript code that needs to be sent to browser.  oaform.jsp will
        then only send the java script, which will handle all updates. */
    public String getJavaScript(String url) {
        return getJavaScript(url, true);
    }
    public synchronized String getJavaScript(String url, boolean bCurrentFrame) {
        bSubmitFlag = false;
        if (url == null) return "";
        if (frame == null) return "";
        int x = getJavaScripts().size();
        if (x == 0) {  // else: javascript for submit is currently loaded and will submit another form
            boolean b = true;
            boolean bElse = false;
            if (frame.getChanged()) {
                frame.setChanged(false);
                if (bTopWasFrameSet) {
                    getJavaScripts().addElement("if (top.location.href.indexOf(\""+frame.getUrl()+"\") < 0) window.open(\""+frame.getUrl()+"\",\"_top\");");
                    getJavaScripts().addElement("else {");
                    bElse = true;
                }
                else b = false; // no javascript needed, just pull up frameset Url
            }
            else {
                if (frame.getUrl() != null && frame.getUrl().equalsIgnoreCase(url)) b = false;  // _top frame
            }
            String ss = updateFrames(frame.getUrl(), "", b, url);  // update starting at the top
            if (bCurrentFrame) {
                x = getJavaScripts().size();
                if (x > 0 && ss != null) getJavaScripts().addElement(ss);  // current frame needs to be updated
            }
            if (bElse) getJavaScripts().addElement("}");
            x = getJavaScripts().size();
        }
        bTopWasFrameSet = (getFrameSet(this.frame.getUrl()) != null);
            
        String s = "";
        if (x > 0) {
            s += "<script language=\"JavaScript\">\r\n";
            for (int i=0; i<x; i++) {
                s += "    " + (String) getJavaScripts().elementAt(i) + "\r\n";
            }
            s += "</script>\r\n";
        }
        getJavaScripts().removeAllElements();

//vvvvvvvvddddddd
//  if (s.length() > 0) System.out.println("-------\r\n"+s+"\r\n-------\r\n");
        return s;
    }

    /** called by OAFrame.setUrl() to store the URL of the Form or FrameSet 
        that is assigned to the frame.
        Once the Form/FrameSet is created, it will call OASession.add(itself) and
        it will be assigned a Frame.
        @param formUrl is not case sensitive
    */
    protected void storeFrameUrl(String formUrl, OAFrame frame){
        if (formUrl == null || frame == null) return;
        formUrl = formUrl.toUpperCase();
        OAForm form = getForm(formUrl); // if not found then OASession.addForm() will set form's frame
        if (form != null) form.frame = frame;
        OAFrameSet fs = getFrameSet(formUrl); 
        if (fs != null) fs.frame = frame;
        if (fs == null && form == null) {
            if (hashFrameUrl == null) hashFrameUrl = new Hashtable();
            hashFrameUrl.put(formUrl, frame);
        }
    }
    protected void assignFrame(OAForm form) {
        if (hashFrameUrl == null) hashFrameUrl = new Hashtable();
        OAFrame f = (OAFrame) hashFrameUrl.get(form.getUrl().toUpperCase());
        if (f == null) f = frame;
        form.frame = f;
    }
    protected void assignFrame(OAFrameSet fs) {
        if (hashFrameUrl == null) hashFrameUrl = new Hashtable();
        OAFrame f = (OAFrame) hashFrameUrl.get(fs.getUrl().toUpperCase());
        if (f == null) f = frame;
        if (f != null) {
            fs.frame = f;
            if (f == frame) bTopWasFrameSet = true;
        }
    }

    /** Store/Retreive frameSets by name for this session. 
        @param name is not case sensitive
    */
    public OAFrameSet getFrameSet(String name) {
        Object obj = get(name);
        if (obj == null || !(obj instanceof OAFrameSet)) return null;
        return (OAFrameSet) obj;
    }
    public void putFrameSet(OAFrameSet frameSet) {
        put(frameSet);
    }
    public void addFrameSet(OAFrameSet frameSet) {
// System.out.println("OASession.addFrameSet() should be changed to put(frameSet)");
        put(frameSet);
    }
    public void put(OAFrameSet frameSet) {
        if (frameSet == null) return;

        put(frameSet.getUrl(), frameSet);
        frameSet.setSession(this);
        assignFrame(frameSet);

        String[] s = (String[]) get("HOLD_"+frameSet.url);
        if (s != null) setFrameUrl(frameSet.url, s[0], s[1]);
    }
    /**
        @param name is not case sensitive
    */
    public void removeFrameSet(String name) {
        this.remove(name);
    }
    

    /** the nextUrl to go to. */
    public String getNextUrl() {
        return nextUrl;
    }
    public void setNextUrl(String s) {
        nextUrl = s;
    }


    /** flag that can be used to know if OAApplication has been setup. */
    public boolean isInitialized() {
        return bInit;
    }
    public void setInitialized(boolean b) {
        bInit = b;
    }
    public void setApplication(OAApplication app) {
        this.application = app;
    }
    public OAApplication getApplication() {
        return application;
    }

    /** last form called by oaform.jsp
        @see OAForm#getReturnUrl
    */
    public void setLastForm(OAForm f) {
        lastForm = f;
    }
    public OAForm getLastForm() {
        return lastForm;
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    /** set by oabeans.jsp */
    public void setRequest(HttpServletRequest r) {
        request= r;
/** take out  qqqqqqqqqq
        String s = request.getParameter("oafs");
        if (s != null) {
            OAFrameSet fs = getFrameSet(s);  // sent by OAFrame.getUrlValue()
            s = request.getParameter("oaf");
            lastFrame = fs.get(s);
        }
****/
    }

    /** set by oabeans.jsp */
    public HttpServletResponse getResponse() {
        return response;
    }
    /** set by oabeans.jsp */
    public void setResponse(HttpServletResponse r) {
        response = r;
    }


    /** For this to work oaheader.jsp needs to call setResponse() &amp; setRequest()*/
    public void putCookie(String name, String value) {
        setCookie(name,value);
    }
    public void putCookie(String name, int x) {
        setCookie(name,x+"");
    }
    /** For this to work oaheader.jsp needs to call setResponse() &amp; setRequest().
        Uses Servlet.Response to create a cookie.  MaxAge is set to max. */
    public void setCookie(String name, String value) {
        if (response == null) return;
        Cookie c = new Cookie(name, value);
        c.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(c);
    }
    /** uses Servlet.Request to get a cookie. 
        For this to work oaform.jsp &amp; oabeans.jsp need to call setResponse() &amp; setRequest()
    */
    public String getCookie(String name) {
        if (request == null) return null;
        Cookie[] cookies = request.getCookies();
        for (int i=0; i<cookies.length; i++) {
            if (cookies[i].getName().equals(name)) return cookies[i].getValue();
        }
        return null;
    }


    transient Vector vecBindCommand;
    transient Vector vecBindFrame;
    
    private Vector getBindCommands() {
        if (vecBindCommand == null) {
            vecBindCommand = new Vector(10,10);
        }
        return vecBindCommand;
    }
    private Vector getBindFrames() {
        if (vecBindFrame == null) {
            vecBindFrame = new Vector(10,10);
        }
        return vecBindFrame;
    }
    
    
    /** this will automatically have a OACommand "press/submit" an OACommand on another form. 
        A command can be bound to mutliple commands.  A command will only be invoke if its form is 
        currently being displayed.
    */
    public void bindCommand(String formUrl, String commandName, String toFormUrl, String toCommandName) {
        BindCommand bc = new BindCommand(formUrl, commandName, toFormUrl, toCommandName);
        getBindCommands().addElement(bc);     
    }
    /** this will display a Url in a frame within a frameset when the OACommand or OALink is pressed/submitted.
        @param formUrl form that OACommand/OALink component is on
        @param commandName name of OACommand/OALink component
        @param frameSetUrl url of frameset that needs a new page displayed in one of its frames
        @param frameName name of frame to display a page
        @param url of new page to display
    */
    public void bindCommand(String formUrl, String commandName, String frameSetUrl, String frameName, String url) {
        BindFrame bf = new BindFrame(formUrl, commandName, frameSetUrl, frameName, url);
        getBindFrames().addElement(bf);
    }    
    /* this will display a Url at the "_top" postion when the OACommand or OALink is pressed/submitted.
        @param formUrl form that OACommand/OALink component is on
        @param commandName name of OACommand/OALink component
        @param url of new page to display at the "_top" of browser.  This will call OASession.setTopUrl() when selected
    */
    public void bindCommand(String formUrl, String commandName, String topUrl) {
        BindFrame bf = new BindFrame(formUrl, commandName, topUrl, null, null);
        getBindFrames().addElement(bf);
    }    

    class BindCommand {
        String formUrl, commandName, toFormUrl, toCommandName;
        public BindCommand(String formUrl, String commandName, String toFormUrl, String toCommandName) {
            this.formUrl = formUrl;
            this.commandName = commandName;
            this.toFormUrl = toFormUrl;
            this.toCommandName = toCommandName;
        }
    }
    class BindFrame {
        String formUrl, commandName, frameSetUrl, frameName, url;
        public BindFrame(String formUrl, String commandName, String frameSetUrl, String frameName, String url) {
            this.formUrl = formUrl;
            this.commandName = commandName;
            this.frameSetUrl = frameSetUrl;
            this.frameName = frameName;
            this.url = url;
        }
    }

    protected boolean isUrlVisible(String url) {
        return isUrlVisible(frame, url);
    }
    private boolean isUrlVisible(OAFrame frm, String url) {
        if (frm == null) return false;
        if (frm.getUrl().equalsIgnoreCase(url)) return true;
        OAFrameSet fs = getFrameSet(frm.getUrl());
        if (fs != null) {
            OAFrame[] frms = fs.getFrames();
            for (int i=0; i<frms.length; i++) {
                if (isUrlVisible(frms[i], url)) return true;
            }
        }
        return false;        
    }

    /** called by OAFrameSet.processRequest() */
    protected void checkBindings(String url, String cmd) {
        // see if command is bound to another command or a frameset/frame/url
        if (url != null && cmd != null) {
            int x = getBindCommands().size();
            for (int i=0; i<x; i++) {
                BindCommand bc = (BindCommand) vecBindCommand.elementAt(i);
                if (url.equalsIgnoreCase(bc.formUrl) && cmd.equalsIgnoreCase(bc.commandName)) {
                    if (isUrlVisible(bc.toFormUrl)) {
                        submitForm(url, bc.toFormUrl, bc.toCommandName);
                    }
                }
            }
            x = getBindFrames().size();
            for (int i=0; i<x; i++) {
                BindFrame bf = (BindFrame) getBindFrames().elementAt(i);
                if (url.equalsIgnoreCase(bf.formUrl) && cmd.equalsIgnoreCase(bf.commandName)) {
                    if (bf.frameName == null) setTopUrl(bf.frameSetUrl);
                    else setFrameUrl(bf.frameSetUrl,bf.frameName,bf.url);
                }
            }
        }
    }


    /** returns javascript to update current frame. */
    protected String updateFrames(String url, String targetPath, boolean bGenerateCode, String updateUrl) {
        String sReturn = null;
        String s;
        OAFrameSet fs = getFrameSet(url);
        if (fs == null) return sReturn;
        Enumeration enumx = fs.hashtable.elements();

        for ( ; enumx.hasMoreElements(); ) {
            OAFrame f = (OAFrame) enumx.nextElement();
            OAForm form = getForm(f.getUrl());
            if (f.getChanged() || f.getUrl().equalsIgnoreCase(updateUrl) || (form != null && form.needsRefreshed()) ) {
                f.setChanged(false);
                if (bGenerateCode) {
                    s = "top."+(targetPath+f.target)+".location = \""+f.getUrl()+"\";";

                    if (f.getUrl().equalsIgnoreCase(updateUrl)) {
                        sReturn = s;  // code that needs to be generated for current frame
                    }
                    else {
                        getJavaScripts().addElement(s);
                    }
                }
                s = updateFrames(f.getUrl(), targetPath + f.target + ".", false, updateUrl);
                if (s != null) sReturn = s;
            }
            else {
                if (f.getUrl() != null && f.getUrl().equals(url)) {
                    System.out.println("OASession.updateFrames() is in a loop ... url="+url);
                }
                else {
                    s = updateFrames(f.getUrl(), targetPath + f.target + ".", bGenerateCode, updateUrl);
                    if (s != null) sReturn = s;
                }
            }
        }
        return sReturn;
    }
            
    /** same as calling request.getHeader("User-Agent") */
    public String getBrowserName() {
        return request.getHeader("User-Agent");
    }
}



