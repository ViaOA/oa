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


import java.lang.reflect.*;
import java.util.Vector;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
OALink: Example
<pre>
    [Java Code]
    OALink lnk = new OALink("test.html"); 
    form.add("cmdTest", lnk);
    ....
    [HTML Code]
    &lt;A HREF="&lt;%=form.getLink("lnkTest").getHref()%"&gt; &lt;%=form.getLink("lnkTest").getScript()%"&gt;&gt;Test link&lt;/A&gt;
    -or-
    &lt;A HREF="&lt;%=form.getTable("tabName").getRowLink("lnkTest",row).getHref()%&gt;"&lt;%=form.getTable("tabName").getRowLink("lnkTest",row).getScript()%&gt;&gt;Test link&lt;/A&gt;
</pre>
*/
public class OALink extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected boolean bOnClickScript=true;
    protected Hub sharedHub; // hub that needs to have setShared(hub,true) if this is selected
    protected OAObject oaObject; // object to make the activeObject
    protected int command;
    protected boolean bSetActiveObject = false;  // will be set to true when used as OATable command
    protected boolean bUseReturnUrl;    
    private OASession session;
    protected OATable table;
    protected boolean bTableEnabled = true;
    protected boolean bTableVisible = true;    
    protected boolean bOnTop = false;
    protected String url; // forward url
    protected String targetFrameSet;
    protected String targetFrame;
    protected String targetForm, targetCommand;
    protected OALink cmdTarget;
    protected String text;
    protected boolean bFollowFormReadOnly;


    // built in commands - SAME AS OACOMMAND
    public static final int NONE = 0; 
    /**  save object */
    public static final int SAVE = 1;    
    /** cancel changes to object and call form.reset() */
    public static final int CANCEL = 2;  
    /** delete object */
    public static final int DELETE = 3;  
    /** create new object and call hub.addElement(obj) */
    public static final int NEW = 4;     
    /** reset form to last saved/set value(s) */
    public static final int RESET = 5;   
    /** if form has not changed then return to form.getReturnUrl(), else return to this form and
        display an error message.
        @see OACommand#setUseReturnUrl
    */
    public static final int RETURN = 6;
    /** return to form.getReturnUrl() without checking for changes
        @see OACommand#setUseReturnUrl
    */
    public static final int RETURN_NOCHECK = 7;

    /** This is used to call a search form. Hub should be set to the same hub that the search
        form is using. */
    public static final int FIND = 8;
    
    /** select this object, make it the activeObject in the hub. This is used with OATable as
        a rowCommand.  If the tables Hub has a linkHub, then it will be returned to the 
        form that called it. */
    public static final int SELECT = 9;

    public static final int TABLENEXT = 10;
    public static final int NEXT = 10;
    
    public static final int TABLEPREVIOUS = 11;
    public static final int PREVIOUS = 11;
    

    private static final int MAX = 12; 
    public static String[] defaultImageNames = { "invisible", "save", "cancel", "delete", "new", "reset", "return", "return", "find", "select", "next", "previous" };




    public OALink() {
    }
    /** 
        @param url page to go to, if null then the current page will be displayed. 
    */
    public OALink(String url) {
        setUrl(url);
    }
    /** 
        @param url page to go to, if null then the current page will be displayed. 
        @param text is Text to display for link
    */
    public OALink(String url, String text) {
        setUrl(url);
        setText(text);
    }


    /** text for link. (also, text for OACommand button) */
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return this.text;
    }

    /** if true and Form is readOnly, then this will be disabled.  Default is false */
    public void setFollowFormReadOnly(boolean b) {
        bFollowFormReadOnly = b;
    }
    public boolean getFollowFormReadOnly() {
        return bFollowFormReadOnly;
    }


    /** this is the hub that will have its sharedHub set to the value of 
        this OACommands hub when command is selected. ex: hub.setSharedHub(shub,true); */
    public void setSharedHub(Hub h) {
        this.sharedHub = h;
    }
    public Hub getSharedHub() {
        return sharedHub;
    }
    
    public OAObject getObject() {
        return oaObject;
    }
    /** object that this command is working with.  If using a hub then this is not needed. */
    public void setObject(OAObject obj) {
        oaObject = obj;
    }


    /** one of the built in commands. If command is RETURN, then setUseReturnUrl(true)
        is called.  If command is NEW, then setActiveObject(true).
        If imageName is not set then it will default to value in OACommand.defaultImageNames[] + ".gif"
    */
    public void setCommand(int cmd) {
        command = cmd;
        if (cmd == RETURN || cmd == RETURN_NOCHECK) {
            setUseReturnUrl(true);
        }
        if (cmd == NEW) setActiveObject(true);
    }
    public int getCommand() {
        return command;
    }


    /** default=false. If true then hub.setActiveObject() will be called. 
        Set to true when used in OATable */
    public boolean getActiveObject() {
        return bSetActiveObject;
    }
    public void setActiveObject(boolean b) {
        bSetActiveObject = b;
    }

    /** this will use the forms returnUrl as the Url to forward to. */
    public void setUseReturnUrl(boolean b) {
        bUseReturnUrl = b;
    }
    public boolean getUseReturnUrl() {
        return bUseReturnUrl;
    }


    /** if true, then the url that is returned will be sent to frame "_top".  If false (default) then
        the new page/url will use the same frame.
        @see OASession#bindCommand
    */
    public boolean getOnTop() {
        return bOnTop;
    }
    public void setOnTop(boolean b) {
        this.bOnTop = b;
    }

    /** The hub that this command works with. */
    public void setHub(Hub h) {
        super.setHub(h);
    }

    
    /** returns the encoded name needed on forms. */
    public String getName() {
        if (cmdTarget != null) return cmdTarget.getName();
        String id = "";
        if (oaObject != null) {
            id = Util.getObjectIdAsString(hub,oaObject);
        }

        String s = "";
        if (table != null) {
            s = table.name.length() + "_" + table.name + "_";
        }

        String line = "oacommand_"+s+name.length()+"_"+name+id;
        return line;
    }


    /************************** OAHtmlComponent ************************/
    protected String processCommand(OASession session, OAForm form, String command) { 
        if (table == null && (!bEnabled)) return null;
        if (table == null && (!bTableVisible || !bTableEnabled)) return null;

        String result;

        if (bUseReturnUrl && form != null && form.frame != null) {
            result = form.frame.getPreviousUrl();
            result = "previousPage"; // 12/15/00
            // took out, oaframe.setUrl() will go to previous automatically if prev=newUrl     
            // form.frame.setUrlToPrevious();
        }
        else {
            result = url;
            if (bOnTop && result != null) {
                session.setTopUrl(result);
                result = null;
            }
        }
        this.session = session;

        if (form != null) form.setLastCommand(name);

// oacommand_9_cmdSelect2

        int len = 0;
        try {
            len = Integer.parseInt(com.viaoa.html.Util.field(command,'_',2));
        }
        catch (NumberFormatException e) {
        }

        String id = com.viaoa.html.Util.field(command,'_',3,99);
        if (len > 0) id = id.substring(len);

        try {
            OAObject obj = (OAObject) currentObject;;
            if (id != null && id.length() > 0 && hub != null) {
                obj = (OAObject) com.viaoa.html.Util.getObject(hub, id);
                form.setLastObject(obj);
                if (bSetActiveObject) hub.setActiveObject(obj);
            }
            if (hub != null && sharedHub != null) {
            	if (sharedHub.getSharedHub() != hub) sharedHub.setSharedHub(hub,true);
            }

            // see if this is an internal command
            Vector vec;
            switch (this.command) {
                case NONE:
                    break;
                case SAVE:
                    if (obj == null && hub != null) obj = (OAObject) hub.getActiveObject();
                    if (obj != null) {
                        obj.save();
                    }
                    else {
                        form.addError("OACommand.save Error: form="+form.getUrl()+" not assigned a Hub or Object");
                        result = null;
                    }
                    break;
                case CANCEL:
                    // if FINDing, then go back to form that was performing the "find"
                    vec = (Vector) session.getMisc("iselecting");
                    if (vec != null) {
                        int x = vec.size();
                        if (x > 0) {
                            result = (String) vec.elementAt(x - 1);
                            vec.removeElementAt(x - 1);
                            break;
                        }
                    }
                
                    form.reset();
                    if (obj == null && hub != null) obj = (OAObject) hub.getActiveObject();
                    if (obj != null) {
                        /* 20090703 was:
                        obj.cancel();
                        */
                        if (obj.isNew()) obj.delete();// 04/29/01 was: obj.removeAll();
                    }
                    else {
                        form.addError("OACommand.cancel Error: form="+form.getUrl()+" not assigned a Hub or Object");
                        result = null;
                    }
                    break;
                case DELETE:
                    if (obj == null && hub != null) obj = (OAObject) hub.getActiveObject();
                    if (obj != null) {
                        obj.delete();
                    }
                    else {
                        form.addError("OACommand.delete Error: form="+form.getUrl()+" not assigned a Hub or Object");
                        result = null;
                    }
                    break;
                case NEW:
                    Hub mhub = hub.getMasterHub();
                    if (mhub != null && mhub.getActiveObject() == null) break;
                    
                    Class c = hub.getObjectClass();
                    Constructor constructor = c.getConstructor(new Class[] {});
                    obj = (OAObject) constructor.newInstance(new Object[] {});
                    hub.addElement(obj);
                    if (bSetActiveObject) hub.setActiveObject(obj);
                    break;
                case RESET:
                    form.reset();
                    break;
                case RETURN:
                    boolean bError = false;
                    if (hub != null) {
                        obj = (OAObject) hub.getActiveObject();
                        if (obj != null && (obj.isNew() || obj.getChanged()) ) bError = true;
                    }
                    if (form.isChanged()) {
                        String[] ss = form.getChanges();
                        for (int i=0; i<ss.length; i++) {
                            String s = ss[i]+" has changed";
                            form.addHiddenMessage(s);
                        }
                    }
                    if (bError) {
                        form.addMessage("<h4><font color=\"#FF0000\">Form Data has changed</font></h4> Please choose <b>Save</b> or <b>Cancel</b> before using <b>Return</b>");
                        result = url;
                    }
                    break;
                case FIND:
                    vec = (Vector) session.getMisc("iselecting");
                    if (vec == null) vec = new Vector(2,2);
                    vec.addElement(form.getUrl());
                    session.putMisc("iselecting", vec);
                    break;
                case SELECT:
                    // go back to form that was performing the "find"
                    vec = (Vector) session.getMisc("iselecting");
                    if (vec != null) {
                        int x = vec.size();
                        if (x > 0) {
                            result = (String) vec.elementAt(x - 1);
                            vec.removeElementAt(x - 1);
                        }
                    }
                    break;
                case TABLENEXT:
                    if (table != null) table.scrollDown();
                    break;
                case TABLEPREVIOUS:
                    if (table != null) table.scrollUp();
                    break;
                    
            }
        }
        catch (InvocationTargetException e) {
            result = null;
            form.addError("Exception "+e.getTargetException());   
        }
        catch (Throwable e) {
            result = null;
            form.addError("Exception "+e.toString());
        }

        int x = vecListener.size();        
        for (int i=0; i<x; i++) {
            OAHtmlListener l = (OAHtmlListener) vecListener.elementAt(i);
            result = l.afterPost(form,result);
        }

//06/17/01        if (targetFrameSet != null && targetFrame != null) {
        if (targetFrame != null) {
            session.setFrameUrl(targetFrameSet, targetFrame, result);
            result = form.getUrl();
        }

        if (targetForm != null && targetCommand != null) {
            session.submitForm(form.getUrl(), targetForm, targetCommand);
        }
        return result;
    }

    protected boolean isReset() {
        return (this.command == CANCEL || this.command == RESET);
    }
    /** target frameset and target frame to display URL in 
    */
    public void setTarget(String targetFrameSet, String targetFrame) {
        this.targetFrameSet = targetFrameSet;
        this.targetFrame = targetFrame;
    }
    /** target frame to display URL in 
        @see OAImage#setUrl
    */
    public void setTarget(String targetFrame) {
        this.targetFrame = targetFrame;
    }
        
    /** execute a command in another frame  */
    public void setTargetCommand(String formName, String cmd) {
        this.targetForm = formName;
        this.targetCommand = cmd;
    }

    /** execute a command in this frame.  This is so that an OALink can work like an OACommand.  */
    public void setTargetCommand(OALink cmd) {
        cmdTarget = cmd;
    }


    protected Vector vecListener = new Vector(5,5);
    public void addListener(OAHtmlListener l) {
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    public void removeListener(OAHtmlListener l) {
        vecListener.removeElement(l);
    }

    protected Object currentObject;
    protected void beforeSetValuesInternal() {
        super.beforeSetValuesInternal();
        if (hub != null) currentObject = hub.getActiveObject();
        // called by OAForm
    }

    /** returns the string needed for "HREF=?" to submit the form. */
    public String getHref() {
      
// 2008/01/03
String s = "\"oaform.jsp?oaform="+getForm().getUrl()+"&"+getName()+"=1\"";
s += " onMouseOver=\"this.href='oaform.jsp?oaform="+getForm().getUrl()+"&"+getName()+"=1&oatop='+setOA()+'&oatarget='+this.target+'&oaname='+window.name;\"";
return s;    	
    	
      // this wont work 2008/01/03 - javascript is not allowed in href ?
      // return "javascript:submitOA('"+getName()+"');return false;";
      // 2008/01/03 return "javascript:document.forms[0].action = 'oaform.jsp?"+getName()+"=1'; setOA(); document.forms[0].submit();";
      // 06/12/01  return "javascript:document.forms[0].action = document.forms[0].action + '?"+getName()+"=1'; document.forms[0].submit();";
    }
    
   
    public String getScript() {
        String line = getOnClickJavaScript(); 
        return line;
    }    

    protected String getOnClickJavaScript() {
        if (!bOnClickScript) return null;  // toggle button needs to do a submit so that it will be updated
        
        String line = null;
        if (targetFrame != null) {
            return "onclick=\"top."+targetFrame+".location='"+url+"'\"";
        }
        if (targetForm != null && targetCommand != null) {
            OASession session = form.session;
            if (session != null) {
                if (line == null) line = "";
                line += "onclick=\""+session.getSubmitFormJavaScript(targetForm, targetCommand)+"return false;\"";
            }
        }
        return line;
    }        

    /** url to forward to. */
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtml(String htmlTags) {
// <A HREF="<%=form.getLink("lnkTest").getHref()%">>Test link</A>
// <A HREF="<%=form.getTable("tabName").getRowLink("lnkTest",row).getHref()%">>Test link</A>
// <A HREF="oaform.jsp?oaform=form.jsp&oacommand_7_cmdTest"&gt;">Test link</A>

        if ( !isValid() ) return "<u>"+text+"</u>";

        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<A HREF=\"";
        s += getHref() + "\"";
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        s += ">";
        s += text;
        s += "</a>";
        if (htmlAfter != null) s += htmlAfter;
        
        return s;
    }

    protected boolean isValid() {
        boolean b = bEnabled;
        if (!bTableEnabled) b = false;
        if ( (bFollowFormReadOnly && form != null && form.getReadOnly()) ) b = false;
        
        switch (this.command) {
            case SAVE:
            case CANCEL:
            case DELETE:
                if (hub != null && hub.getActiveObject() == null) b = false;
            case NEW:
                if (hub == null) b = false;
                else if (!hub.isValid()) b = false;
                break;
        }
        return b;
    }

}




