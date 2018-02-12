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

//   response.setHeader("refresh", "1200;URL=Login.jsp");


import java.awt.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OAFile;
/*
    processing commands: 
        1: create an OAHtmlAdapter that implements the processCommand() method
        2: form.put("NAME", adapter)
        3: use a submit image with the name="oacommand_nameLength_NAME:[ID]" on html page
        4: the form will call your processCommand(form, command)
        5: your processCommand can return an url to go to or null to keep same
*/
/** Container that represents a HTML Form and holds OAHtmlComponents.
<pre>
    [Java Code]
    OAForm form = new OAForm(oaFormId);
    form.setName("Expert Edit");
    ... add OAHtmlComponents ...
    ....
    [HTML Code]
    &lt;form method="post" action="&lt;%=form.getAction()%&gt;"&gt;

    This will set the scrolling postion 
    &lt;BODY &lt;%=form.getJavaScriptOnLoad()%&gt;&gt;
</pre>
*/
public class OAForm extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Hashtable hashComponent = new Hashtable(13,0.75f);
    protected Vector vecComponent = new Vector(5,5);
    protected String url;  // jsp name
    String status,name;
    boolean bFormEnded = true;
    // name of last command or subCommand processed.  
    // This does not include the command prefix "oacommand_NAME_"
    protected String lastCommand;    
    protected transient OAObject lastObject;  // last object used.  Set by OACommand.processCommand()
    protected transient OASession session;
    protected transient OAFrame frame; // set by oasession
    protected String forwardUrl; // set during processRequest()
    protected boolean bNeedsRefreshed;
    protected int scrollx, scrolly;
    protected boolean readOnly;
    protected int top; // y scroll position
    protected boolean autoScroll=true, bScroll;
    protected String frameName; // set by processRequest, the name of the frame that sent request
    protected String targetName; // set by processRequest, the name of the request target
    
    /** @param url name of jsp that this form is used on; must be unique within session 
        ex: "education.jsp"
    */
    public OAForm(String url) {
        this.url = url;
        if (url == null || url.length() == 0) throw new IllegalArgumentException("OAForm url required");
        if (url.indexOf('-') >= 0) throw new IllegalArgumentException("OAForm url can not have any '-' characters");
    }
    public String getUrl() {
        return url;
    }
    
    public int getTop() {
        return top;
    }
    public void setTop(int y) {
        top = y;
    }

    /** calls OAFrame.getPreviousUrl() to get the return url.  If this form is not yet in a frame, then return
        will be null.
        @see OAFrame#getPreviousUrl
    */
    public String getReturnUrl() {
        if (frame == null) return null;
        return frame.getPreviousUrl();
    }

    /** calls OAFrame.setUrlToPrevious(url).
        @see OAFrame#setUrlToPrevious
    */
    public void setReturnUrl(String url) {
        if (frame != null) frame.setUrlToPrevious(url);
    }
    

    /** scroll position of upper left corner of window. */
    public Point getScrollPosition() {
        return new Point(scrollx, scrolly);
    }
    public void setScrollPosition(Point p) {
        if (p != null) {
            scrollx = p.x;
            scrolly = p.y;
        }
    }

    /** return the frame that this form is currently under.  */
    public OAFrame getFrame() {
        return frame;
    }
    
    
    /** if true then components will not receive data from submit. 
        Note: commands and listeners are not affected.
    */
    public boolean getReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean b) {
        this.readOnly = b;
    }

    /** if true then top of form will automatically be scrolled.  Default is false.
        @see OAForm#getJavaScriptOnLoad
        @see OAForm#getTop
    */
    public boolean getAutoScroll() {
        return autoScroll;
    }
    public void setAutoScroll(boolean b) {
        this.autoScroll = b;
    }

    /** flag to know if the getEndHtml() has been called.  This can be used by a "footer", so
        that it can include a "return" img button
    */
    public boolean hasFormEnded() {
        return bFormEnded;
    }

    /** set by OAForm.processRequest() and OACommand.processCommand() to store the last 
        command name or subCommand name processed.
        This does not include the command prefix "oacommand_NAME_".  This is only needed by
        OAForm so that it will know that it is a command to process
    */
    public String getLastCommand() {
        return lastCommand;
    }
    public void setLastCommand(String s) {
        lastCommand = s;
    }
    /** set by OACommad.processCommand() */
    public OAObject getLastObject() {
        return lastObject;
    }
    public void setLastObject(OAObject obj) {
        lastObject = obj;
    }

    /** automatically turned on when a component is added, and set to false when getAction() is called
    */
    public void setNeedsRefreshed(boolean b) {
        bNeedsRefreshed = b;
    }
    public boolean needsRefreshed() {
        if (bNeedsRefreshed) return true;
        // see if any component needs refreshed
        Enumeration enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            if (oh.needsRefreshed()) return true;
        }
        return false;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String s) {
        this.name = s;
    }


    /** resets the form, takes off any edits not saved */
    public void reset() {
        Enumeration enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            oh.reset();
        }
    }


    /** finds out if any of the values have changed */
    public boolean isChanged() {
        Enumeration enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            if (oh.isChanged()) return true;
        }
        return false;
    }

    /** finds out the name of components that have changed */
    public String[] getChanges() {
        Vector v = new Vector();
        Enumeration enumx = hashComponent.keys();
        for ( ; enumx.hasMoreElements(); ) {
            String s = ((String) enumx.nextElement());
            OAHtmlComponent oc = (OAHtmlComponent)hashComponent.get(s.toUpperCase());
            if ( oc.isChanged()) {
                v.addElement(s);
            }
        }
        String[] ss = new String[v.size()];
        v.copyInto(ss);
        return ss;
    }

    public OAHtmlComponent[] getComponents() {
        OAHtmlComponent[] comps = new OAHtmlComponent[hashComponent.size()];
        Enumeration enumx = hashComponent.elements();
        for (int i=0; enumx.hasMoreElements(); i++) {
            comps[i] = (OAHtmlComponent) enumx.nextElement();
        }
        return comps;
    }
    
    /** updates the objects bound to the form */
    public void update() {
        Enumeration enumx = hashComponent.keys();
        for ( ; enumx.hasMoreElements(); ) {
            String name = (String) enumx.nextElement();
            OAHtmlComponent oh = (OAHtmlComponent) hashComponent.get(name.toUpperCase());
            oh.update();
/****            
            try {
                oh.update();
            }
            catch (Exception e) {
                OAException oae = new OAException(OAForm.class, "OAForm.update() component="+name+" "+e.getMessage());
            }
**/     
        }
    }

    /** returns null if not found 
        @param name is not case sensitive
    */
    public OAHtmlComponent getComponent(String name) {
        if (name == null) return null;
        return (OAHtmlComponent) hashComponent.get(name.toUpperCase());
    }

    /** 
        stores component by name.
        @param name is not case sensitive
    */
    public void put(String name, OAHtmlComponent obj) {
        if (obj == null) return;
        if (name == null || name.length() == 0) throw new IllegalArgumentException("OAForm.add() name required");
        if (hashComponent.get(name.toUpperCase()) != null) {
            String s = "OAForm.add() \""+name+"\" already exists on form - it will be overwritten";
            System.out.println(s);
            this.addError(s);
        }
        hashComponent.put(name.toUpperCase(), obj);
        obj.form = this;
        obj.name = name;
        bNeedsRefreshed = true;
        vecComponent.addElement(obj);
    }
    /** 
        stores component by name.
        @param name is not case sensitive
        @see OAForm#put
    */
    public void add(String name, OAHtmlComponent obj) {
        put(name, obj);
    }


    /**  
        @param name is not case sensitive
    */
    public OARelationshipGrid getRelationshipGrid(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OARelationshipGrid) return (OARelationshipGrid) obj;
        throw new RuntimeException("OAForm.getRelationshipGrid() \""+name+"\" not found");
    }
    
    /**  
        @param name is not case sensitive
    */
    public OARadio getRadio(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OARadio) return (OARadio) obj;
        throw new RuntimeException("OAForm.getRadio() Radio \""+name+"\" not found");
    }

    /**  
        @param name is not case sensitive
    */
    public OACheckBox getCheckBox(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OACheckBox) return (OACheckBox) obj;
        throw new RuntimeException("OAForm.getCheckBox() CheckBox \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAComboBox getComboBox(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAComboBox) return (OAComboBox) obj;
        throw new RuntimeException("OAForm.getComboBox() ComboBox \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAList getList(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAList) return (OAList) obj;
        throw new RuntimeException("OAForm.getList() List \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OATextArea getTextArea(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OATextArea) return (OATextArea) obj;
        throw new RuntimeException("OAForm.getTextArea() TextArea \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OATree getTree(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OATree) return (OATree) obj;
        throw new RuntimeException("OAForm.getTree() Tree \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OATextField getTextField(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OATextField) return (OATextField) obj;
        throw new RuntimeException("OAForm.getTextField() TextField \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAPasswordField getPasswordField(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAPasswordField) return (OAPasswordField) obj;
        throw new RuntimeException("OAForm.getPasswordField() PasswordField \""+name+"\" not found");
    }
    
    /**  
        @param name is not case sensitive
    */
    public OATable getTable(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OATable) return (OATable) obj;
        throw new RuntimeException("OAForm.getTable() Table \""+name+"\" not found");
    }
    public OADataTable getDataTable(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OADataTable) return (OADataTable) obj;
        throw new RuntimeException("OAForm.getDataTable() DataTable \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OACommand getCommand(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OACommand) return (OACommand) obj;
        throw new RuntimeException("OAForm.getCommand() Command \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OATabbedPane getTabbedPane(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OATabbedPane) return (OATabbedPane) obj;
        throw new RuntimeException("OAForm.getTabbedPane() \""+name+"\" not found");
    }
    
    /**  
        @param name is not case sensitive
    */
    public OAImage getImage(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAImage) return (OAImage) obj;
        throw new RuntimeException("OAForm.getImage() name \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAFileInput getFileInput(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAFileInput) return (OAFileInput) obj;
        throw new RuntimeException("OAForm.getFileInput() \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OALink getLink(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OALink) return (OALink) obj;
        throw new RuntimeException("OAForm.getLink() Link \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAToggleButton getToggleButton(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAToggleButton) return (OAToggleButton) obj;
        throw new RuntimeException("OAForm.getToggleButton() ToggleButton \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OAButtonGroup getButtonGroup(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OAButtonGroup) return (OAButtonGroup) obj;
        throw new RuntimeException("OAForm.getButtonGroup() ButtonGroup \""+name+"\" not found");
    }
    /**  
        @param name is not case sensitive
    */
    public OALabel getLabel(String name) {
        Object obj = getComponent(name);
        if (obj instanceof OALabel) return (OALabel) obj;
        throw new RuntimeException("OAForm.getLabel() Label \""+name+"\" not found");
    }


    /** tag to use inside of BODY tag to set the pages top scroll position. */
    public String getJavaScriptOnLoad() {
        if (top != 0) {
            Enumeration enumx = hashComponent.elements();
            for ( ; enumx.hasMoreElements(); ) {
                OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
                if (oh.resetTop()) {
                    top = 0;
                    break;
                }
            }
        }
        bScroll = true;
        if (top != 0) return "onLoad=\"window.scrollTo(0,"+top+");\"";
        return "";
    }

    /** used for supplying needed HTML within a form tag.  This will also set "enctype="multipart/form-data""
        if the form has any OAFileInput components on it.
        Calls all components on the form "getInitScript()" to get any initialization javascript.
        Note: this will set OASession.setCurrentForm(this)
        ex:
        &lt;form method="post" action="&lt;=form.getAction()&gt;"&gt;
        
        &lt;form method="post" action="oaform.jsp" enctype="multipart/form-data"&gt;&lt;input type="hidden" name="oaform" value="formName"&gt;       
    */
    public String getAction() {
        if (session != null) session.setCurrentForm(this);
        
        bNeedsRefreshed = false;
        bFormEnded = false;

        String js = "";
/*********        
        js = "\r\n";
        js += "<SCRIPT LANGUAGE=\"JavaScript\">\r\n";
        js += "document.write(\"VINCE<BR>\");\r\n";
        js += "function displayIt(obj) {\r\n";
        js += "    for(var prop in obj){\r\n";
        js += "        if(typeof(obj[prop])==\"object\" && obj[prop]!=null) displayIt(obj[prop]);\r\n";
        js += "         else {\r\n";
        js += "             document.writeln(prop+\"=\"+obj[prop]+\"<br>\");\r\n";
        js += "         }\r\n";
        js += "     }\r\n";
        js += "}\r\n";

        //
        js += "document.write(\"x\"+pageXOffset+\"<BR>\");\r\n";
        js += "document.write(\"y\"+pageYOffset+\"<BR>\");\r\n";
        js += "    function doSubmit() {\r\n";
        js += "        forms[0].action = forms[0].action + \"?oax=\"+pageXOffset+\"&oay=\"+pageYOffset;\r\n";
        js += "    }\r\n";
        js += "    displayIt(this);\r\n";
        js += "</SCRIPT>\r\n";

                
        String s = "oaform.jsp\" onSubmit=\"doSubmit();\">"+js+"<input type=\"hidden\" name=\"oaform\" value=\""+url+"";
**/   

        js += "\r\n";
        js += "<SCRIPT LANGUAGE=\"JavaScript\">\r\n<!--\r\n";
        js += "function setOA() {\r\n";
        js += "  var y;\r\n";
        js += "  if (document.layers) y = window.pageYOffset;\r\n";
        js += "  else y = document.body.scrollTop;\r\n";
        js += "  document.forms[0].oatop.value = y;\r\n";
        js += "  document.forms[0].oatarget.value = document.forms[0].target;\r\n";
        js += "  document.forms[0].oaname.value = window.name;\r\n";
        js += "  return y;\r\n";
        js += "}\r\n";
        js += "// -->\r\n";

        js += "function submitOA(cmd) {\r\n";
        js += "setOA();";
        js += "document.forms[0].action +=  '?' + cmd + '=1';";
        js += "document.forms[0].submit();";
        js += "";
        js += "}\r\n";
        

        js += "// -->\r\n";

        Enumeration enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
             OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            String s = oh.getInitScript();
            if (s != null) js += s + "\r\n";
        }
        
        
        js += "</SCRIPT>\r\n";
        if (session != null) {
            if (session.bSubmitFlag) {
                session.bSubmitFlag = false;
                if (session.lastForm != null && session.lastForm.forwardUrl != null) {
                    js += session.getJavaScript(session.lastForm.forwardUrl, false); // 12/15
                }
            }
        }

        if (autoScroll && !bScroll) {
            js += "<BODY "+ getJavaScriptOnLoad() + ">";
        }

//qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvvvvv sssssssssssssssss
//qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvvvvv
//qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvvvvv
        String s2 = "";
        enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            // if form has an OAFileInput comp on it, then it will need to be able to get the file and 
            //   parse out the other component name/value pairs
            if (oh instanceof OAFileInput) {
                s2 = "?oaform="+url+"\"";
                s2 += " enctype=\"multipart/form-data";
                break;
            }
        }
/*        
<form method="post" target="_top" action="oaform.jsp" onSubmit="setOA();">
<form method="post" target="_top" action="oaform.jsp?oaform=empEdit.jsp" enctype="multipart/form-data" onSubmit="setOA();">
*/        
        String s = "oaform.jsp"+s2+"\" onSubmit=\"setOA();\" accept-charset=\"UTF-8\">\r\n";

        
        s += "\r\n" + js  + "\r\n";
        s += "<input type=\"hidden\" name=\"oatop\" value=\"0\">\r\n";
        s += "<input type=\"hidden\" name=\"oatarget\" value=\"\">\r\n";
        s += "<input type=\"hidden\" name=\"oaname\" value=\"\">\r\n";
        s += "<input type=\"hidden\" name=\"oaform\" value=\""+url+"";

/*
<form method="post" action="<%=form.getAction()%>">
<form method="post" action="oaform.jsp"><input type="hidden" name="oaform" value="empLogin.jsp">
*/
        // String s = "oaform.jsp\"><input type=\"hidden\" name=\"oaform\" value=\""+url+"";
/*vvvvvvvvvvvvvvvvvvvvvvqqqqqqqqqqqqqqqqqqqqq
//qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvvvvv sssssssssssssssss
        Enumeration enum = hashComponent.elements();
        for ( ; enum.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enum.nextElement();
            // if form has an OAFileInput comp on it, then it will need to be able to get the file and 
            //   parse out the other component name/value pairs
            if (oh instanceof OAFileInput) {
                s = "oaform.jsp?oaform="+url+"\" enctype=\"multipart/form-data";
                break;
            }
        }
vvvvvvvvvvvvvvvvvvvvvvqqqqqqqqqqqqqqqqqqqqq*/
        
        return s;
    }
    
    public String getEndHtml() {
        status = "";
        bFormEnded = true;
        return "</form>";        
    }

    /** calls setEnd(true) */
    public String setEnd() {
        return setEnd(true);
    }
    /** flags the form to know that the &lt;/form&gt; tag has been set. */
    public String setEnd(boolean b) {
        status = "";
        bFormEnded = b;
        return "";
    }

    public void setStatus(String s) {
        status = s;
    }
    public String getStatus() {
        return status;
    }

    public OASession getSession() {
        return session;
    }
    
    
    /** called by (oaform.jsp .. form .. frame) 
        calls setValues on all OAHtml components and processCommand on any commands
        @return forwardUrl
        
        <pre>
        STEPS:
        1: set form.session 
        2: makes sure oasession.topUrl is correct
        3: notify all form comps by calling their beforeSetValuesInternal() 
        4: see if form is a multipart (used for OAFileInput)
        5: set all component values
        6: have components update object(s)
        7: call command component processCommand() method
        8: call session.checkBindings()
        9: call frameset and its parents processRequest() method
       10: return this.forwardUrl
       </pre>
    */
    public String processRequest(OASession session) {
        if (session == null) return null;
        this.session = session;
        session.bSubmitFlag = true; // flag used by getAction() to only get javascript once per submit
        ServletRequest request = session.getRequest();
        session.lastForm = this;
        boolean bError = false;
        Enumeration enumx;
        
        try {
            request.setCharacterEncoding("UTF-8");
        }
        catch (Exception e) {}

        // let all components know that form was submitted.
        //   this is because a checkbox that is not checked will
        //   not send a name/value pair in the request
        enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            if (!readOnly) oh.beforeSetValuesInternal();
            if (oh instanceof OAFileInput) ((OAFileInput)oh).setSaved(false);
        }
        
        
        Hashtable hashNameValue = new Hashtable();
        String contentType = request.getContentType();
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
            try {
                processMultipart(request, hashNameValue);
            }
            catch (Exception e){
                this.addError(e.toString());
                bError = true;
            }
        }
        else {
            enumx = request.getParameterNames();
            while ( enumx.hasMoreElements()) {
                String name = (String) enumx.nextElement();
                String[] values = request.getParameterValues(name);
                hashNameValue.put(name,values);
            }
        }
        
        String[] ss;
        ss = (String[]) hashNameValue.get("oatarget");
        targetName = (ss == null) ? null : ss[0];

        ss = (String[]) hashNameValue.get("oaname");
        frameName = (ss == null) ? null : ss[0];
        bScroll = false;

        ss = (String[]) hashNameValue.get("oaTableName");
        if (ss != null && ss.length == 1) {
        	String s = ss[0];
        	ss = (String[]) hashNameValue.get("oaTableCommand");
            if (ss != null && ss.length == 1) {
            	if (s.length() > 0) hashNameValue.put(s, ss);
            	hashNameValue.remove("oaTableName");
            	hashNameValue.remove("oaTableCommand");
            }
        }
        
        
        // make sure topUrl is correctly set
        String topUrl = this.url; 
        if (this.frame != null) {
            OAFrameSet fs = this.frame.frameSet;
            for ( ;fs != null; ) {
                topUrl = fs.url;
                if (fs.frame == null) fs = null;
                else fs = fs.frame.frameSet;
            }
        }
        
        if (this.frame == null || !topUrl.equalsIgnoreCase(session.frame.getUrl())) {
            if (topUrl != this.url || frameName==null || frameName.length()==0 || frameName.equalsIgnoreCase("_top")) {
                session.setTopUrl(topUrl);
                if (session.frame != null) {
                    session.frame.setChanged(false);  // form has already been display in browser
                }
            }
        }


        forwardUrl = null;
        lastCommand = null;
        lastObject = null;

        Vector vec = new Vector(5);
        
        try {
            ss = (String[]) hashNameValue.get("oatop");
            top = 0;
            if (ss != null) top = Integer.parseInt(ss[0]);
        }
        catch (NumberFormatException e) {
        }
        

        // see if a reset command was submitted
        enumx = hashNameValue.keys();
        while ( enumx.hasMoreElements()) {
            String command = (String) enumx.nextElement();
            if (command.indexOf("oacommand_") != 0) continue;
            if (command.endsWith(".y")) continue;  // only use ".x"
            if (command.endsWith(".x")) {
                command = command.substring(0,command.length() - 2);
            }        
            
            // command to run
            int len = 0;
            try {
                len = Integer.parseInt(com.viaoa.html.Util.field(command,'_',2));
            }
            catch (NumberFormatException e) {
            }
            // oacommand_9_cmdSelect2
            // oacommand_10_tabAddress_9_cmdSelect2
            String cmdid = com.viaoa.html.Util.field(command,'_',3,99);
            if (len > 0) cmdid = cmdid.substring(0, len);
            OAHtmlComponent oh = getComponent(cmdid);
            if (oh != null) {
                vec.addElement(command);
                if ( (oh instanceof OALink) && ((OALink)oh).isReset() ) {
                    hashNameValue.clear();
                    break;
                }
            }
            else System.out.println("OAForm.processRequest() no component for command => "+command);//qqqqqqqqqq
        }


        enumx = hashNameValue.keys();
        while ( enumx.hasMoreElements()) {
            String name = (String) enumx.nextElement();
            String[] values = (String[]) hashNameValue.get(name);

            String fullName = name;
            
            // find out the name of the component in case it was encoded with an object id
            name = Util.getEncodedName(name);
            
            OAHtmlComponent oh = getComponent(name);
            if ((oh == null || oh instanceof OARadio) && values.length == 1) {
                if (name.indexOf("oacommand_") != 0) {
                    name = Util.getEncodedName(values[0]);
                    oh = getComponent(name); // OARadio
                    if (oh != null) fullName = values[0]; // OARadio full name
                }
            }
            if (oh != null) {
                try {                
                    if (!readOnly) oh.setValuesInternal(fullName,values);
                }
                catch (Exception e) {
                    System.out.println("OAForm.processRequest() Exception "+e);
                    e.printStackTrace();
                    this.addError(e.toString());
                    bError = true;
                }
            }
        } 

        if (bError) return getUrl();  // go back to this page
        if (!readOnly) update(); // save values to object 

        String origUrl = null;
        if (this.frame != null) origUrl = this.frame.getUrl();

        // commands
        int x = vec.size();
        for (int i=0; i<x; i++) {
            String command = (String) vec.elementAt(i);
            for (;;) {
                int len = 0;
                try {
                    len = Integer.parseInt(com.viaoa.html.Util.field(command,'_',2));
                }
                catch (NumberFormatException e) {
                }

                // oacommand_9_cmdSelect2
                // oacommand_10_tabAddress_9_cmdSelect2
   
                String cmdid = com.viaoa.html.Util.field(command,'_',3,99);
                if (len > 0) cmdid = cmdid.substring(0, len);
                OAHtmlComponent oh = getComponent(cmdid);
                if (oh != null) {
                    try {                
                        String s = oh.processCommand(session, this, command);
                        if (s != null && s.length() > 0) forwardUrl = s;
                    }
                    catch (Exception e) {
                        System.out.println("OAForm.processRequest() Exception "+e);
                        e.printStackTrace();
                        this.addError(e.toString());
                        return getUrl();
                    }
                }
                else System.out.println("OAForm.processRequest() no component for command => "+command);//qqqqqqqqqq
                break;
            }
        }

        // check bindings in OASession
        // see if command is bound to another command or a frameset/frame/url
        session.checkBindings(url,getLastCommand());
        if (frame != null && frame.getUrl() != null && !frame.getUrl().equalsIgnoreCase(this.url)) forwardUrl = frame.getUrl();

        enumx = hashComponent.elements();
        for ( ; enumx.hasMoreElements(); ) {
            OAHtmlComponent oh = (OAHtmlComponent) enumx.nextElement();
            oh.afterSetValuesInternal();
            if (oh instanceof OAFileInput || oh instanceof OADataTable) { 
                String s = oh.processCommand(session, this,"");
                if (s != null) forwardUrl = s;
            }
        }

        if (forwardUrl == null || forwardUrl.length() == 0) forwardUrl = getUrl();  // go back to this page

        forwardUrl = afterPost(forwardUrl);
        
        // call listeners
        x = vecListener.size();        
        for (int i=0; i<x; i++) {
            OAHtmlListener l = (OAHtmlListener) vecListener.elementAt(i);
            try {
                forwardUrl = l.afterPost(this,forwardUrl);
            }
            catch (Exception e) {
                System.out.println("OAForm.processRequest() Exception "+e);
                e.printStackTrace();
                this.addError(e.toString());
                return getUrl();
            }

            if (forwardUrl == null || forwardUrl.length() == 0) forwardUrl = getUrl();  // go back to this page
        }

        // notify all frameSets
        OAFrame targetFrame = this.frame;
        OAFrame frm = frame;
        for (;frm != null;) {
            OAFrameSet fs = frm.getFrameSet();
            if (fs == null) {
                if (frm.target.equalsIgnoreCase(targetName)) targetFrame = frm;
                break;
            }
            OAFrame[] frames = fs.getFrames();
            for (int i=0; targetFrame==null && i<frames.length; i++) {
                if (frames[i].target.equalsIgnoreCase(targetName)) targetFrame = frames[i];
            }            
            forwardUrl = fs.processRequest(session, this, forwardUrl);
            if (forwardUrl == null || forwardUrl.length() == 0) forwardUrl = getUrl();  // go back to this page
            frm = fs.frame;
        }

        if (forwardUrl.equalsIgnoreCase("previousPage")) {
            if (this.frame != null) {
                this.frame.setUrlToPrevious();
                forwardUrl = targetFrame.getUrl(); // needs to be set to the page for target framee
            }
            else forwardUrl = this.url;
        }
        
        // this will set/update the correct frame with the target that was used in the submit. 
        if (targetFrame == this.frame) {
            if (this.frame != null) {
                /** 2008/01/07
            	if (this.frame.getUrl() != null && !this.frame.getUrl().equalsIgnoreCase(origUrl)) forwardUrl = this.frame.getUrl();  // frame.setUrl() was called directly
                **/
                frame.setUrl(forwardUrl,session);
                frame.setChanged(false);
            }
        }
        else {
            // target is different then the frame that this form is in.
            // need to return the url for the target frame
            if (forwardUrl.equalsIgnoreCase(this.url)) {
                // need to reload targets real url
                // this will send the page that is needed by the target
                if (targetFrame != null) {
                    forwardUrl = targetFrame.getUrl();
                    targetFrame.setChanged(false);
                }
            }
            else {
                // need to internally set the url for the target frame
                if (targetFrame != null) {
                    if (targetFrame.getFrameSet() == null) {  // _top frame
                        session.setTopUrl(forwardUrl);
                        session.frame.setChanged(false); 
                    }
                    else {
                        targetFrame.setUrl(forwardUrl);
                        targetFrame.setChanged(false);
                    }
                }            
            }
        }
        return forwardUrl;
    }

    // returns forwardUrl
    public String afterPost(String forwardUrl) {
        return forwardUrl;
    }
    
    /** set by ProcessRequest to determine the url to forward to.
        The value is set to the return value of the "afterPost()" by listeners.
    */
    public String getForwardUrl() {
        return forwardUrl;
    }
       
    protected Vector vecListener = new Vector(5,5);
    public void addListener(OAHtmlListener l) {
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    public void removeListener(OAHtmlListener l) {
        vecListener.removeElement(l);
    }


    // Parse Multipart posted forms ============================================================
    // Parse Multipart posted forms ============================================================
    // Parse Multipart posted forms ============================================================

    protected void processMultipart(ServletRequest request, Hashtable hashNameValue) throws Exception {
        int len = request.getContentLength();
        if (len <= 1) return;
        String contentType = request.getContentType();
        String sep = "--" + contentType.substring(contentType.indexOf("boundary=")+9);
        sep += "\r\n";
        
        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());

        for (int i=0;;i++) {
            String s = getNext(bis, null, sep);
            if (s == null) break;

            
            /*
				Content-Disposition: form-data; name="txtCreate"\r\n10/21/2008\r\n
				Content-Disposition: form-data; name="fiFile"; filename=""
			 	; filename=""
            */
            String[] nameValue = process(s);
            /*
				[0]=txtCreate [1]=10/21/2008
			 	[0]=fiFile [1]=; filename="budget.txt"
	        */
                
            if (nameValue != null) {
                String name = nameValue[0];
                String[] values = (String[]) hashNameValue.get(name);
                if (values == null) hashNameValue.put(name, new String[] { nameValue[1] });
                else {
                    String[] newValues = new String[values.length+1];
                    System.arraycopy(values,0,newValues,0,values.length);
                    newValues[values.length] = nameValue[1];
                    hashNameValue.put(name,newValues);
                }

                // see if this was an OAFileInput component
                OAHtmlComponent oh = getComponent(name);
                if (oh instanceof OAFileInput) {
                	String fname;
                	if (nameValue.length < 2) fname = "";
                	else {
                		fname = nameValue[1];
                		int x = fname.indexOf('\"');
                		if (x >= 0) fname = fname.substring(x+1);
                		fname = com.viaoa.util.OAString.convert(fname, "\"", null);
                	}
                    /*
					 	[0]=fiFile [1]=budget.txt
	                */
                    hashNameValue.put(name, new String[] { fname });
                	
                	if (fname != null && fname.length() > 0) {
	                    OAFileInput fi = (OAFileInput) oh;
	                    int max = fi.getMax();
	                    if (max >= 0 && len > max) {
	                        throw new RuntimeException("Uploaded file size greater then "+max);
	                    }
	                    fi.setSaved(true);
	                    String sx = fi.getFileName();
	                    OAFile.mkdirsForFile(sx);
	                    File file = new File(sx);
	                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
	                    getNext(bis, bos, "\r\n"+sep);  // this will write to file
	                    bos.flush();
	                    bos.close();
                	}
                }
            }
        }
        bis.close();
    }


    /* returns all data upto sep and "eats" the sep */
    protected String getNext(BufferedInputStream bis, BufferedOutputStream bos, String sep) throws IOException {
        if (sep == null) return null;
        StringBuffer sb = new StringBuffer(1024);
        int c=0;
        boolean eof = false;

        String sep2 = null;
        if (bos == null) sep2 = "\r\nContent-Type:";  // this marks the beginning of a file
        
        int sepLen = sep.length();
        int sep2Len = (sep2!=null)?sep2.length():0;

        for (;;) {
            c = bis.read();
            if (c < 0) {
                eof = true;
                break;
            }

            if (sep2 != null && c == sep2.charAt(0)) {
                int hold = c;
                bis.mark(sep2Len+1);
                int j=1;
                for (;j<sep2Len ; j++) {
                    c = bis.read();
                    if (c != sep2.charAt(j)) break;
                }
                if (j == sep2Len) {
                    // goto end of 2nd LF
                    for (j=0; j<2;) {
                        c = bis.read();
                        if (c == '\n') j++;
                    }
                    break;
                }
                bis.reset();
                c = hold;
            }

            if (c == sep.charAt(0)) {
                int hold = c;
                bis.mark(sepLen+1);
                int j=1;
                for ( ; j<sepLen; j++) {
                    c = bis.read();
                    if (c != sep.charAt(j)) break;
                }
                if (j == sepLen) break;
                bis.reset();
                c = hold;
            }

            if (bos != null) bos.write(c);
            else sb.append((char)c);
        }
        if (eof && sb.length() == 0) return null;
        return new String(sb);
    }


    protected String[] process(String line) {
        String s = "Content-Disposition: form-data; name=";
        int pos = line.indexOf(s);

        // Content-Disposition: form-data; name="txtText"[13][10][13][10]test[13][10]

        if (pos < 0) return null;
        
        line = line.substring(pos + s.length());
        // "txtText"[13][10][13][10]test[13][10]        
        
        pos = line.indexOf('\r');
        if (pos < 0) {
            pos = line.indexOf('\n');
            if (pos < 0) {
                pos = line.indexOf("; ");
                if (pos < 0) return null;
            }
        }
        
        String name = line.substring(0,pos);
        // "txtText"
        
        name = name.replace('"',' ');
        name = name.trim();  // txtText


        String value = line.substring(pos);
        // [13][10][13][10]test[13][10]        
        
        
        // skip 2 CRLF
        for (int j=0;j < 4 && value.length() > 0;) {
            char c = value.charAt(0);
            if (c == '\n' || c == '\r') {
                value = value.substring(1);
                j++;
            }
            else break;
        }
        // test[13][10]        
        
        
        pos = value.indexOf('\r');
        if (pos >= 0) value = value.substring(0,pos);
        // test
        
        return new String[] { name, value };
    }

    /** override from OAObject to make sure object is not saved to datasource. */
    public void onSave() {
    }

    public String getHtml() {
        StringBuffer sb = new StringBuffer(4096);
        
        sb.append("\r\n");
        sb.append("<!-- OAForm HTML Code -->\r\n");
        sb.append("\r\n");
        sb.append(getHtmlStart());
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"3\">\r\n");

        int x = vecComponent.size();
        for (int i=0; i<x; i++) {
            OAHtmlComponent c = (OAHtmlComponent) vecComponent.elementAt(i);
            if (c instanceof OACommand) continue;
            sb.append("    <tr>\r\n");
            sb.append("        <td align=\"right\">\r\n");
            sb.append("            ");
            
            String s = c.getName();
            int xx = s.length();
            boolean b = false;
            for (int j=0; j<xx; j++) {
                char ch = s.charAt(j);
                if (Character.isUpperCase(ch)) {
                    if (!b) b = true;
                    else sb.append(' ');
                }
                if (b) sb.append(ch);
            }
            sb.append("\r\n");
            sb.append("        </td>\r\n");
            sb.append("        <td align=\"left\">\r\n");
            sb.append("            "+c.getHtml()+"\r\n");
            sb.append("        </td>\r\n");
            sb.append("    </tr>\r\n");
        }
        sb.append("    <tr>\r\n");
        sb.append("        <td align=\"center\" colspan=2>\r\n");
        sb.append("             <!-- OAForm Commands -->\r\n");
        for (int i=0; i<x; i++) {
            OAHtmlComponent c = (OAHtmlComponent) vecComponent.elementAt(i);
            if ( !(c instanceof OACommand)) continue;
            String s = c.getHtml();
            if (s != null && s.length() > 0) sb.append("            "+s+"\r\n");
        }
        sb.append("        </td>\r\n");
        sb.append("    </tr>\r\n");
        sb.append("\r\n");
        sb.append("</table>\r\n");
        sb.append("\r\n");
        sb.append("\r\n");

        sb.append(getHtmlEnd());
        sb.append("\r\n");
        sb.append("\r\n");
        return new String(sb);
    }
    public String getHtmlStart() {
        // <form method="post" action="<%=form.getAction()%>">
        String s = "<form method=\"post\" action=\""+getAction()+"\" accept-charset=\"UTF-8\">";
        return s;
    }

    /** sets formEnded=true and returns &lt;/form&gt; */
    public String getHtmlEnd() {
        return getEndHtml();
    }

}



