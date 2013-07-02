/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jsp;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.util.OAString;

/*

<script>
$(document).ready(function() {
    $('#logo-slider').hi5slider({
        speed : 1750,
        pause : 3500,
        transition : 'slide'
    });
});
</script>        

// ID used for messages
    oaFormMessage
    oaFormErrorMessage
    oaFormHiddenMessage

// javascript methods available
    oaShowMessage(msg)
    
    
    divs will be created with these IDs
      '#oaformDialog'
    
// hidden form inputs
    oaform = formId
    oacommand = command that is submitting form
    oachanged = the id of the last changed component   
    
*/


/**
 * Controls an html form and it's components 
 * Form submission
 * support for multipart
 * messages, errorMessage, hiddenMessages
 * default forwardUrl
 * send script to page (addScript)
 * manage ajax or regular submit
 * calls each component 3 times on submission: before, onSubmit, getReturn js script
 * 
 * 
 * @author vvia
 *
 */
public class OAForm extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;

    protected ArrayList<OAJspComponent> alComponent = new ArrayList<OAJspComponent>();

    protected OASession session;
    protected String id;
    protected String url;  // jsp name
    
    protected String forwardUrl; 
    
    /** add script to be returned to browser on initialize. */
    public String jsAddScript;
    /** add script to be returned to browser, only once on initialize (then cleared) */
    public String jsAddScriptOnce;
    
    
    public OAForm() {
    }
    public OAForm(String id, String url) {
        setId(id);
        setUrl(url);
    }

    public OASession getSession() {
        return session;
    }
    public void setSession(OASession s) {
        this.session = s;;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    /** URL for this page */
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    /** page to go to on a submit, unless overwritten by a component or JspSubmit(..) */
    public void setForwardUrl(String urlForward) {
        this.forwardUrl = urlForward;
    }
    public String getForwardUrl() {
        return forwardUrl;
    }

    /** resets the form, takes off any edits not saved */
    public void reset() {
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            comp.reset();
        }
    }
    

    /** javascript to include during the first initialization, (then cleared) */
    public void addScript(String js) {
        addScript(js, true);
    }    
    /** add script to be returned to browser when page is initialized. */
    public void addScript(String js, boolean bOnce) {
        if (OAString.isEmpty(js)) return;
        
        // nees to end in ';'
        if (!js.endsWith(";")) {
            int x = js.length() - 1;
            for ( ; ; x--) {
                if (x < 0) {
                    js += ";";
                    break;
                }
                char ch = js.charAt(x);
                if (ch == ';') break;
                if (Character.isWhitespace(ch)) continue;
                js += ";";
                break;
            }
        }
        if (bOnce) {
            if (jsAddScriptOnce == null) jsAddScriptOnce = "";
            jsAddScriptOnce += js + "\n";
        }
        else {
            if (jsAddScript == null) jsAddScript = "";
            jsAddScript += js + "\n";
        }
    }
    
    
    /** finds out if any of the values have changed */
    public boolean isChanged() {
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (comp.isChanged()) return true;
        }
        return false;
    }

    /** finds out the name of components that have changed */
    public OAJspComponent[] getChangedComponents() {
        ArrayList<OAJspComponent> al = new ArrayList<OAJspComponent>();
        
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (comp.isChanged()) al.add(comp);
        }

        OAJspComponent[] ss = new OAJspComponent[al.size()];
        al.toArray(ss);
        return ss;
    }

    /** returns true to continue, false to not process the request */
    protected boolean beforeSubmit() {
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (!comp._beforeSubmit()) return false;
        }
        return true;
    }
    /**
     * Returns the component that initiated the submit;
     * @param req
     * @param resp
     */
    protected OAJspComponent onSubmit(HttpServletRequest req, HttpServletResponse resp) {
        OAJspComponent compSubmit = null;
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (comp._onSubmit(req, resp)) compSubmit = comp;
        }
        return compSubmit;
    }
    protected String afterSubmit(String forwardUrl) {
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            String s = comp._afterSubmit(forwardUrl);
            if (s != null) forwardUrl = s;
        }
        return forwardUrl;
    }

    /** called after beforeSubmit/onSubmit/afterSubmit 
        This is used inside JSP to process a submit;
     */
    protected String onJspSubmit(OAJspComponent submitComponent, String forwardUrl) {
        return forwardUrl;
    }

    public String getScript() {
        return getInitScript();
    }

    // javascript code to initialize client/browser
    public String getInitScript() {
        getSession().put("oaformLast", this);  // used by oadebug.jsp, oaenable.jsp to know the last page that was viewed
        
        if (!getEnabled()) return "";
        StringBuilder sb = new StringBuilder(1024);

        sb.append("<script>\n");
        
        // outside JS methods
        sb.append("function oaShowMessage(title, msg) {\n");
        sb.append("    $('#oaformDialog').dialog('option', 'title', title);\n");
        sb.append("    $('#oaformDialog').html(msg);\n");
        sb.append("    $('#oaformDialog').dialog('open');\n");
        sb.append("}\n");
        
        
        sb.append("$(document).ready(function() {\n");

        // form dialog
        sb.append("    $('#"+id+"').prepend(\"<div id='oaformDialog'></div>\");\n");
        sb.append("    $('#oaformDialog').dialog({");
        sb.append("        autoOpen : false,");
        sb.append("        title : 'Message',");
        sb.append("        modal : true,");
        sb.append("        width : 420,");
        sb.append("        zIndex: 19999,");
        sb.append("        buttons: [\n");
        sb.append("          { text: 'Ok', click: function() { $(this).dialog('close'); } }\n");
        sb.append("         ]\n");
        sb.append("    });");
        
        sb.append("$('body').append(\"<div id='oaWait'><img src='image/oawait.gif'></div>\");");

        sb.append("    $('#"+id+"').attr('method', 'post');\n");
        sb.append("    $('#"+id+"').attr('action', 'oaform.jsp');\n");
        sb.append("    $('#"+id+"').prepend(\"<input type='hidden' name='oaform' value='"+getId()+"'>\");\n");
        
        // hidden command used by label,button when it is submitted
        sb.append("    $('#"+id+"').prepend(\"<input id='oacommand' type='hidden' name='oacommand' value=''>\");\n");

        // hidden command that can be used to know if any data on page has been changed
        sb.append("    $('#"+id+"').prepend(\"<input id='oachanged' type='hidden' name='oachanged' value=''>\");\n");


        if (getDebug()) {
            sb.append("    $('#"+id+"').addClass('oaDebug');\n");
            sb.append("    $('.oaBindable').addClass('oaDebug');\n");
        }
        else {
            sb.append("    $('#"+id+"').removeClass('oaDebug');\n");
            sb.append("    $('.oaBindable').removeClass('oaDebug');\n");
        }
        
        // else sb.append("    $('#"+id+"').removeClass('oaDebug');\n");
        
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            String s = comp.getScript();
            
            if (getDebug()) sb.append("    $('#"+comp.getId()+"').addClass('oaDebug');\n");
            else sb.append("    $('#"+comp.getId()+"').removeClass('oaDebug');\n");
            
            if (!OAString.isEmpty(s)) sb.append(s + "\n");
            if (comp instanceof OAJspMultipartInterface) {
                sb.append("    $('#"+id+"').attr('enctype', 'multipart/form-data');\n");
                // 20130602 support submit fileInput
                sb.append("    $('#"+id+"').attr('action', 'oaform.jsp?oaform="+getId()+"');\n");
                
//qqqqqqqqqqqqqqqqq                
// qqqqqqq from html.OAImage, need to get clicked button ? not sure
                // if it is in data submitted
//String s = "\"oaform.jsp?oaform="+getForm().getUrl()+"&"+getName()+"=1\"";
//s += " onMouseOver=\"this.href='oaform.jsp?oaform="+getForm().getUrl()+"&"+getName()+"=1&oatop='+setOA()+'&oatarget='+this.target+'&oaname='+window.name;\"";
                
                
                break;
            }
        }
        getMessages(sb);
        sb.append("\n");
        
        // add form submit, to verify components
        sb.append("    $('#"+id+"').on('submit', oaSubmit);\n");
        
        sb.append("    function oaSubmit() {\n");
        sb.append("        var errors = [];\n");
        sb.append("        var requires = [];\n");
        sb.append("        var regex;\n");
        sb.append("        var val;\n");

        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            String s = comp.getVerifyScript();
            if (!OAString.isEmpty(s)) sb.append("    " + s + "\n");
        }
        
        sb.append("if (requires.length > 0) {\n");
        sb.append("    var msg = '';\n");
        sb.append("    for (var i=0; i<requires.length; i++) {\n");
        sb.append("        if (i > 0) {\n");
        sb.append("            msg += ', ';\n");
        sb.append("            if (i % 3 == 0) msg += '<br>';\n");
        sb.append("        }\n");
        sb.append("        msg += requires[i];\n");
        sb.append("    }\n");
        sb.append("    oaShowMessage('Required fields are missing', msg);\n");
        sb.append("    return false;\n");
        sb.append("}\n");   
        sb.append("if (errors.length > 0) {\n");
        sb.append("    var msg = '';\n");
        sb.append("    for (var i=0; i<errors.length; i++) {\n");
        sb.append("        if (i > 0) {\n");
        sb.append("            msg += ', ';\n");
        sb.append("            if (i % 3 == 0) msg += '<br>';\n");
        sb.append("        }\n");
        sb.append("        msg += errors[i];\n");
        sb.append("    }\n");
        sb.append("    oaShowMessage('Errors on page', msg);\n");
        sb.append("    return false;\n");
        sb.append("}\n");   
        sb.append("        return true;\n");
        sb.append("    }\n");

        sb.append("    var cntWait = 0;\n");
        sb.append("    function ajaxSubmit(cmdName) {\n");
        sb.append("        cntWait++;\n");
        sb.append("        var args = $('#"+id+"').serialize();\n");
        sb.append("        if (cmdName != undefined && cmdName) args = cmdName + '=1&' + args;\n");
        sb.append("        var f = function(data) {\n");
        sb.append("            if (--cntWait < 1) {cntWait=0; $('#oaWait').hide();}");
        sb.append("            if (data != null) eval(data);\n");
        sb.append("        }\n");
        sb.append("        $.post('oaajax.jsp', args, f, 'text');\n");  // text: return value type (will be javascript)
//qqqqqqqqqqqqqqqqqqqqqq test the "oaWait" image
//        sb.append("        if (cntWait == 1) $('#oaWait').hide().fadeIn(1500, function(){ if (cntWait < 1) {cntWait=0;$('#oaWait').hide();}});");
        sb.append("    }\n");

        
        if (!OAString.isEmpty(jsAddScript)) {
            sb.append("    " + jsAddScript + "\n");
        }
        if (!OAString.isEmpty(jsAddScriptOnce)) {
            sb.append("    " + jsAddScriptOnce + "\n");
            jsAddScriptOnce = null;
        }
        
        
        String js = sb.toString();
        if (js.indexOf(".focus()") < 0) {
            sb.append("    $('input:enabled:first').focus();\n");
        }
        
        sb.append("\n});\n"); // end jquery.ready ****

        
        sb.append("</script>\n");
        js = sb.toString();
        
        return js;
    }
    
    private boolean bLastDebug; 
    public String getAjaxScript() {
        if (!getEnabled()) return "";
        StringBuilder sb = new StringBuilder(1024);

        getMessages(sb);
        boolean bDebugx = getDebug();
        if (bLastDebug != bDebugx) {
            if (bDebugx) {
                sb.append("    $('#"+id+"').addClass('oaDebug');\n");
                sb.append("    $('.oaBindable').addClass('oaDebug');\n");
            }
            else {
                sb.append("    $('#"+id+"').removeClass('oaDebug');\n");
                sb.append("    $('.oaBindable').removeClass('oaDebug');\n");
            }
        }
        
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            String s = comp.getAjaxScript();
            if (!OAString.isEmpty(s)) sb.append(s + "\n");
            if (bLastDebug != bDebugx) {
                if (bDebugx) sb.append("    $('#"+comp.getId()+"').addClass('oaDebug');\n");
                else sb.append("    $('#"+comp.getId()+"').removeClass('oaDebug');\n");
            }
        }

        if (!OAString.isEmpty(jsAddScriptOnce)) {
            sb.append(jsAddScriptOnce);
            jsAddScriptOnce = null;
        }
        sb.append("$('#oacommand').val('');"); // set back to blank
        
        String js = sb.toString();
        if (js == null) js = "";
        
        bLastDebug = bDebugx;
        
        // js = OAString.convert(js, "\n", "\\n");
        return js;
    }

    protected void getMessages(StringBuilder sb) {
        String[] msg1, msg2;

        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getMessages();
            msg2 = session.getMessages();
        }
        _addMessages(sb, "oaFormMessage", msg1, msg2, this.getMessages());

        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getErrors();
            msg2 = session.getErrors();
        }
        _addMessages(sb, "oaFormErrorMessage", msg1, msg2, this.getErrors());
        
        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getHiddenMessages();
            msg2 = session.getHiddenMessages();
        }
        _addMessages(sb, "oaFormHiddenMessage", msg1, msg2, this.getHiddenMessages());
    }
    
    private void _addMessages(StringBuilder sb, String cssName, String[] msgs1, String[] msgs2, String[] msgs3) {
        String msg = "";
        if (msgs1 != null) { 
            for (String s : msgs1) {
                if (msg.length() > 0) msg += "<br>";
                msg += s;
            }
        }
        if (msgs2 != null) { 
            for (String s : msgs2) {
                if (msg.length() > 0) msg += "<br>";
                msg += s;
            }
        }
        if (msgs3 != null) { 
            for (String s : msgs3) {
                if (msg.length() > 0) msg += "<br>";
                msg += s;
            }
        }
        
        boolean bDebugx = getDebug();
        if (bLastDebug != bDebugx) {
            if (getDebug()) sb.append("    $('#"+cssName+"').addClass('oaDebug');\n");
            else sb.append("    $('#"+cssName+"').removeClass('oaDebug');\n");
        }
        
        msg = Util.convert(msg, "'", "\'");
        if (msg.length() > 0) {
            sb.append("if ($('#"+cssName+"').length) {"); 
            sb.append("  $('#"+cssName+"').show();"); 
            sb.append("} else {");
            sb.append("    oaShowMessage('', '"+msg+"');\n"); 
            sb.append("}"); 
        }
        else sb.append("$('#"+cssName+"').hide();"); 
        sb.append("$('#"+cssName+"').html('"+msg+"');"); 
    }
    
    public ArrayList<OAJspComponent> getComponents() {
        return alComponent;
    }
    
    public OAJspComponent getComponent(String id) {
        if (id == null) return null;
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (id.equalsIgnoreCase(comp.getId())) return comp;
        }
        return null;
    }

    public void remove(String name) {
        OAJspComponent comp = getComponent(name);
        if (comp != null) alComponent.remove(comp);
        super.remove(name);
    }
    public void add(OAJspComponent comp) {
        if (comp == null) return;
        String id = comp.getId();
        if (!OAString.isEmpty(id)) {
            remove(id);
        }
        if (!alComponent.contains(comp)) {
            alComponent.add(comp);
        }
        comp.setForm(this);
    }

    /** called to process the form.
     *  See oaform.jsp
     */
    public String processSubmit(OASession session, HttpServletRequest request, HttpServletResponse response) {
        if (this.session == null) this.session = session;

        try {
            request.setCharacterEncoding("UTF-8");
        }
        catch (Exception e) {}

        
        Hashtable hashNameValue = new Hashtable();
        
        String contentType = request.getContentType();
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
            try {
                processMultipart(request, hashNameValue);
            }
            catch (Exception e){
                this.addError(e.toString());
            }
        }
        else {
            Enumeration enumx = request.getParameterNames();
            while ( enumx.hasMoreElements()) {
                String name = (String) enumx.nextElement();
                String[] values = request.getParameterValues(name);
                hashNameValue.put(name,values);
            }
        }
        
        boolean bProcess = beforeSubmit();
        
        String forward = null;
        
        if (bProcess) {
            forward = forwardUrl;
            if (OAString.isEmpty(forward)) forward = this.getUrl();
            OAJspComponent compSubmit = onSubmit(request, response);
    
            forward = afterSubmit(forward);
            
            String s = onJspSubmit(compSubmit, forward);
            if (!OAString.isEmpty(s)) forward = s;
        }
        if (OAString.isEmpty(forward)) forward = this.getUrl();
        return forward;
    }

    
    /**
     * Called by oaforward.jsp to be able to have a link call submit method without doing a form submit.
     */
    public String processForward(OASession session, HttpServletRequest request, HttpServletResponse response) {
        if (this.session == null) this.session = session;
        try {
            request.setCharacterEncoding("UTF-8");
        }
        catch (Exception e) {}
        
        String forward = forwardUrl;
        String s = request.getParameter("oacommand");
        OAJspComponent comp = getComponent(s);
        if (comp == null) return forward;

        comp._onSubmit(request, response);
        s = comp._afterSubmit(forward);
        if (!OAString.isEmpty(s)) forward = s;
        
        return forward;
    }
       

    // Parse Multipart posted forms ============================================================
    protected void processMultipart(ServletRequest request, Hashtable hashNameValue) throws Exception {
        int len = request.getContentLength();
        if (len <= 1) return;
        String contentType = request.getContentType();
        String sep = "--" + contentType.substring(contentType.indexOf("boundary=")+9);
        sep += "\r\n";
        
        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());

        for (int i=0;;i++) {
            String s = getNextMultipart(bis, null, sep);
            if (s == null) break;

            /*
				Content-Disposition: form-data; name="txtCreate"\r\n10/21/2008\r\n
				Content-Disposition: form-data; name="fiFile"; filename=""
			 	; filename=""
            */
            String[] nameValue = processMultipart(s);
            /*
				[0]=txtCreate [1]=10/21/2008
			 	[0]=fiFile [1]=; filename="budget.txt"
	        */
                
            if (nameValue == null) continue;
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
            OAJspComponent comp = getComponent(name);
            if (comp == null) continue;
            if (!(comp instanceof OAJspMultipartInterface)) continue;
            
        	if (nameValue.length < 2) continue; 
    		String fname = nameValue[1];
    		int x = fname.indexOf('\"');
    		if (x >= 0) fname = fname.substring(x+1);
    		fname = com.viaoa.util.OAString.convert(fname, "\"", null);
        	if (OAString.isEmpty(fname)) continue;
        	
    	    OutputStream os = ((OAJspMultipartInterface)comp).getOutputStream(len, fname);
    	    if (os == null) {
    	        os = new OutputStream() {
    	            @Override
    	            public void write(int b) throws IOException {
    	                // no op
    	            }
    	        };
    	    }
    	    BufferedOutputStream bos = new BufferedOutputStream(os);
            getNextMultipart(bis, bos, "\r\n"+sep);  // this will write to bos
            bos.flush();
            bos.close();
        }
        bis.close();
    }

    protected String[] processMultipart(String line) {
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
    
    
    /* returns all data up to sep and "eats" the sep */
    protected String getNextMultipart(BufferedInputStream bis, BufferedOutputStream bos, String sep) throws IOException {
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

    
    public OATextField getTextField(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OATextField) return (OATextField) comp;
        return null;
    }
    public OATextField getPassword(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAPassword) return (OAPassword) comp;
        return null;
    }
    public OAButton getButton(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAButton) return (OAButton) comp;
        return null;
    }
    public OAHtmlElement getHtmlElement(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAHtmlElement) return (OAHtmlElement) comp;
        return null;
    }
    public OATextArea getTextArea(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OATextArea) return (OATextArea) comp;
        return null;
    }
    public OACombo getCombo(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OATextArea) return (OACombo) comp;
        return null;
    }
    public OATable getTable(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OATable) return (OATable) comp;
        return null;
    }
    public OALink getLink(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OALink) return (OALink) comp;
        return null;
    }
    public OACheckBox getCheckBox(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OACheckBox) return (OACheckBox) comp;
        return null;
    }
    public OAGrid getGrid(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAGrid) return (OAGrid) comp;
        return null;
    }
    public OACombo getList(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAList) return (OAList) comp;
        return null;
    }
    public OAImage getImage(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAImage) return (OAImage) comp;
        return null;
    }
    public OARadio getRadio(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OARadio) return (OARadio) comp;
        return null;
    }
    public OAServletImage getServletImage(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAServletImage) return (OAServletImage) comp;
        return null;
    }
    
}



