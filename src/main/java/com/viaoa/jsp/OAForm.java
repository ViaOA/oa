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

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.process.OAProcess;
import com.viaoa.util.OAConv;
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
 * calls each component 3 times on submission: before, beforeOnSubmit, onSubmit, getReturn js script
 *
 *
 * @author vvia
 *
 */
public class OAForm extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final ArrayList<OAJspComponent> alComponent = new ArrayList<OAJspComponent>();
    protected final ArrayList<OAJspComponent> alNewAddComponent = new ArrayList<OAJspComponent>();

    protected OASession session;
    protected String id;
    protected String url;  // jsp name

    protected String forwardUrl;
    
    private int jsLibrary; 

    /** add script to be returned to browser on initialize. */
    public String jsAddScript;
    /** add script to be returned to browser, only once on initialize (then cleared) */
    public String jsAddScriptOnce;

    private final ArrayList<FormProcess> alProcess = new ArrayList<>(3);
    private volatile boolean bFormProcessClosed;

    protected final ArrayList<String> alRequiredCssName = new ArrayList<>();
    protected final ArrayList<String> alRequiredJsName = new ArrayList<>();

    // where to redirect after displaying any error messages
    protected String urlRedirect;
    
    private static class FormProcess {
        OAProcess p; 
        boolean bShowInDialog;
        
        FormProcess(OAProcess p) {
            this.p = p;
            bShowInDialog = true;
        }
    }
    
    /**
     * set the preferred js library to use. 
     * @param type see {@link OAApplication#JSLibrary_JQueryUI} {@link OAApplication#JSLibrary_Bootstrap}
     */
    public void setDefaultJsLibrary(int type) {
        this.jsLibrary = type;
    }
    public int getDefaultJsLibrary() {
        if (this.jsLibrary != 0 || getSession() == null) return this.jsLibrary;
        return getSession().getDefaultJsLibrary();
    }
    
    public enum Type {
        None,
        Bootstrap
    }
    protected Type type = Type.None;

    public void setType(Type t) {
        this.type = t;
    }
    public Type getType() {
        return type;
    }


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

    public void addProcess(OAProcess p) {
        if (p == null) return;
        if (bFormProcessClosed) {
            bFormProcessClosed = false;
            addScript("$('#oaFormProcessClosed').val('');");
        }
            
        for (FormProcess fp : alProcess) {
            if (fp.p == p) return;
        }
        alProcess.add(new FormProcess(p));
    }
    public void removeProcess(OAProcess p) {
        if (p == null) return;
        for (FormProcess fp : alProcess) {
            if (fp.p == p) {
                alProcess.remove(fp);
                break;
            }
        }
    }
    public void clearProcesses() {
        alProcess.clear();
    }
    
    public OAProcess[] getProcesses() {
        FormProcess[] fps = new FormProcess[0]; 
        fps = alProcess.toArray(fps);
        
        OAProcess[] ps = new OAProcess[fps.length];
        int i = 0;
        for (FormProcess fp : fps) {
            ps[i++] = fp.p;
        }
        return ps;
    }
    
    
    /**
     * Flag to have the current processes shown/hidden to the user.
     */
    public void showProcesses(boolean b) {
        if (b) {
            if (bFormProcessClosed) {
                addScript("$('#oaFormProcessClosed').val('');");
            }
        }
        bFormProcessClosed = !b;
        for (FormProcess fp : alProcess) {
            fp.bShowInDialog = b;
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
            if (!comp._beforeFormSubmitted()) return false;
        }
        return true;
    }
    /**
     * Returns the component that initiated the submit;
     * @param req
     * @param resp
     */
    protected OAJspComponent onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        OAJspComponent compSubmit = null;
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (comp._onFormSubmitted(req, resp, hmNameValue)) compSubmit = comp;
        }
        return compSubmit;
    }
    protected String afterSubmit(String forwardUrl) {
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            forwardUrl = comp._afterFormSubmitted(forwardUrl);
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
        String js = getInitScript();
        String s = getAjaxCallbackScript();
        if (s != null) js += s;
        
        return js;
    }

    
    // javascript code to initialize client/browser
    /**
     * @deprecated use {@link #getScript()} instead, to include support for ajax callback
     * @return
     */
    public String getInitScript() {
        getSession().put("oaformLast", this);  // used by oadebug.jsp, oaenable.jsp to know the last page that was viewed

        alNewAddComponent.clear();
        
        if (!getEnabled()) return "";
        StringBuilder sb = new StringBuilder(2048);

        sb.append("<script>\n");

       
        // outside JS methods

        
        sb.append("var oaShowMessage;");
        sb.append("if ($().modal) {");
        // bootstrap
        sb.append("oaShowMessage = function(title, msg) {\n");
        sb.append("  $('#oaformDialog .modal-title').html(title);\n");
        sb.append("  $('#oaformDialog .modal-body').html(msg);\n");
        sb.append("  $('#oaformDialog').modal({keyboard: true});\n");
        sb.append("}\n");

        sb.append("}");
        sb.append("else {");
        // jquery version
        sb.append("oaShowMessage = function(title, msg) {\n");
        sb.append("    $('#oaformDialog').dialog('option', 'title', title);\n");
        sb.append("    $('#oaformDialog').html(msg);\n");
        sb.append("    $('#oaformDialog').dialog('open');\n");
        sb.append("}\n");
        sb.append("}");
        
        
        // bootstrap
        sb.append("function oaShowSnackbarMessage(msg) {\n");
        sb.append("    $('#oaFormSnackbarMessage').html(msg);\n");
        sb.append("    $('#oaFormSnackbarMessage').css({visibility:'visible', opacity: 0.0}).animate({opacity: 1.0},300);\n");
        sb.append("    setTimeout(function() {\n");
        sb.append("      $('#oaFormSnackbarMessage').animate({opacity: 0.0}, 2000,\n"); 
        sb.append("        function(){\n");
        sb.append("          $('#oaFormSnackbarMessage').css('visibility','hidden');\n");
        sb.append("        }\n");
        sb.append("      );\n");
        sb.append("    }, 2000);\n");
        sb.append("  }\n");

        
        sb.append("$(document).ready(function() {\n");

        
        // form dialog
        sb.append("if ($().modal) {");
        // bootstrap version
        sb.append("$('#"+id+"').prepend(\"");
        sb.append("<div id='oaformDialog' class='modal fade' tabindex='-1'>");
        sb.append("  <div class='modal-dialog'>");
        sb.append("    <div class='modal-content'>");
        sb.append("      <div class='modal-header'>");
        sb.append("        <button type='button' class='close' data-dismiss='modal'><span>&times;</span></button>");
        sb.append("        <h4 class='modal-title'>");
        sb.append("        </h4>");
        sb.append("      </div>");
        sb.append("      <div class='modal-body'>");
        sb.append("      </div>");
        sb.append("      <div class='modal-footer'>");
        sb.append("        <button type='button' class='btn btn-primary' data-dismiss='modal'>Ok</button>");
        sb.append("      </div>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("</div>");
        sb.append("\");\n");
        sb.append("}");
        sb.append("else {");
        // jquery version
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
        sb.append("}");
        
        // bootstrap progress modal
        sb.append("$('#"+id+"').prepend(\"");
        sb.append("<div id='oaFormProcess' class='modal fade' tabindex='-1'>");
        sb.append("<div class='modal-dialog'>");
        sb.append("  <div class='modal-content'>");
        sb.append("    <div class='modal-header'>");
        sb.append("      <button id='oaFormProcessClose' type='button' class='close' data-dismiss='modal'>&times;</button>");
        sb.append("      <h4 class='modal-title' id='myModalLabel'>Please Wait ... <span id='oaFormProcessTitle'></span></h4>");
        sb.append("    </div>");
        sb.append("    <div class='modal-body center-block'>");
        sb.append("      <div class='progress'>");
        sb.append("        <div id='oaFormProcessProgress' class='progress-bar bar' role='progressbar' style='min-width: 2em; width: 5%;'>");
        sb.append(""); // ex: '25%'          
        sb.append("        </div>");
        sb.append("      </div>");
        sb.append("      <div>");
        sb.append("      <span id='oaFormProcessStep'></span> ... <span id='oaFormProcessMessage'></span>");
        sb.append("      </div>");
        sb.append("    </div>");
        sb.append("    <div id='oaFormProcessFooter' class='modal-footer'>");
        sb.append("      <button type='button' class='btn btn-default' data-dismiss='modal'>Close</button>");
        sb.append("    </div>");
        sb.append("  </div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("\");\n");

        // update hidden oaFormProcessClosed if progress dlg is closed
        sb.append("$('#oaFormProcess button').click(function() { $('#oaFormProcessClosed').val('true'); } );");
        
        
        String js = getProcessingScript();
        if (js != null) sb.append(js); 
        
        sb.append("$('body').append(\"");
        sb.append("<div id='oaFormSnackbarMessage'></div>");
        sb.append("\");\n");
        
        sb.append("$('body').append(\"<div id='oaWait'><img src='image/oawait.gif'></div>\");");

        sb.append("    $('#"+id+"').attr('method', 'post');\n");
        sb.append("    $('#"+id+"').attr('action', 'oaform.jsp');\n");
        sb.append("    $('#"+id+"').prepend(\"<input type='hidden' name='oaform' value='"+getId()+"'>\");\n");

        sb.append("    $('#"+id+"').prepend(\"<input id='oaajaxid' type='hidden' name='oaajaxid' value='"+aiAjaxIdLastUsed.get()+"'>\");\n");

        sb.append("    $('#"+id+"').prepend(\"<input id='oaFormProcessClosed' type='hidden' name='oaFormProcessClosed' value=''>\");\n");
        
        // hidden command used by label,button when it is submitted
        sb.append("    $('#"+id+"').prepend(\"<input id='oacommand' type='hidden' name='oacommand' value=''>\");\n");

        // hidden param used for a component 
        sb.append("    $('#"+id+"').prepend(\"<input id='oaparam' type='hidden' name='oaparam' value=''>\");\n");
        
        
        // hidden command that can be used to know if any data on page has been changed
        sb.append("    $('#"+id+"').prepend(\"<input id='oachanged' type='hidden' name='oachanged' value=''>\");\n");

        // hidden input for browser javascript date.timezoneOffset  (its sign [+/-] is opposite of java timezone offset)
        sb.append("    $('#"+id+"').prepend(\"<input id='jsDateTzOffset' type='hidden' name='jsDateTzOffset' value='\"+((new Date()).getTimezoneOffset())+\"'>\");\n");


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
            }
        }
        getMessages(sb);
        sb.append("\n");

        // add form submit, to verify components
        sb.append("    $('#"+id+"').on('submit', oaSubmit);\n");
        sb.append("    var oaSubmitCancelled;\n");
        sb.append("    function oaSubmit(event) {\n");
        sb.append("        oaSubmitCancelled = true;\n");
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
        sb.append("    event.preventDefault();\n");
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
        sb.append("    event.preventDefault();\n");
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
        sb.append("        oaSubmitCancelled = false;\n");
        sb.append("        return true;\n");
        sb.append("    }\n");

        sb.append("var cntAjaxSubmit = 0;\n");
        sb.append("function ajaxSubmit(cmdName) {\n");
        sb.append("  cntAjaxSubmit++;\n");
        sb.append("  var bUseAsync = (cntAjaxSubmit == 1);\n");
        sb.append("  if (bUseAsync && cntAjaxSubmit == 1) {\n");
        sb.append("    $('#oaWait').fadeIn(200, function(){ if (cntAjaxSubmit < 1) {cntAjaxSubmit=0;$('#oaWait').hide();}});\n");  
        sb.append("  }\n");
        sb.append("  var f1 = function(data) {\n");
        sb.append("    if (--cntAjaxSubmit < 1) {\n");
        sb.append("      cntAjaxSubmit=0; \n");
        sb.append("      $('#oaWait').hide();\n");
        sb.append("    }\n");
        sb.append("    if (data) eval(data);\n");
        sb.append("  }\n");
        sb.append("  var f2 = function() {\n");
        sb.append("    if (--cntAjaxSubmit < 1) {\n");
        sb.append("      cntAjaxSubmit=0; \n");
        sb.append("      $('#oaWait').hide();\n");
        sb.append("    }\n");
        sb.append("  }\n");
        
        sb.append("  var args = $('#"+id+"').serialize();\n");
        sb.append("  if (cmdName != undefined && cmdName) args = cmdName + '=1&' + args;\n");
        sb.append("  $.ajax({\n");
        sb.append("    type: 'POST',\n");
        sb.append("    url: 'oaajax.jsp',\n");
        sb.append("    data: args,\n");
        sb.append("    success: f1,\n");
        sb.append("    error: f2,\n");
        sb.append("    dataType: 'text',\n");
        sb.append("    timeout: 30000,\n");
        sb.append("    async: bUseAsync");
        sb.append("  });\n");
        sb.append("}\n");

        sb.append("function ajaxSubmit2(cmdName) {\n");
        sb.append("  var args = $('#"+id+"').serialize();\n");
        sb.append("  if (cmdName != undefined && cmdName) args = cmdName + '=1&' + args;\n");
        sb.append("  $.ajax({\n");
        sb.append("    type: 'POST',\n");
        sb.append("    data: args,\n");
        sb.append("    url: 'oaajax.jsp',\n");
        sb.append("    success: function(data) {if (data) eval(data);},\n");
        sb.append("    dataType: 'text'\n");
        sb.append("  });\n");
        sb.append("}\n");
        
        
        
        if (!OAString.isEmpty(jsAddScript)) {
            sb.append("    " + jsAddScript + "\n");
        }
        if (!OAString.isEmpty(jsAddScriptOnce)) {
            sb.append("    " + jsAddScriptOnce + "\n");
            jsAddScriptOnce = null;
        }


        js = sb.toString();
        if (js.indexOf(".focus()") < 0) {
            sb.append("    $('input:enabled:first').focus();\n");
        }

        String s = getRedirect();
        if (OAString.isNotEmpty(s)) {
            setRedirect(null);
            sb.append("window.location = '"+s+"';\n");            
        }
        
        sb.append("\n});\n"); // end jquery.ready ****


        sb.append("</script>\n");
        js = sb.toString();

        return js;
    }

    /**
     * Use to get any changes since the last time getScript was called.
     */
    public String getUpdateScript() {
        String s = getAjaxScript();
        return s;
    }    
    
    private boolean bLastDebug;
    /**
     * same as calling {@link #getUpdateScript()}
     */
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
            
            String s;
            if (alNewAddComponent.contains(comp)) {
                s = comp.getScript();
            }
            else {
                s = comp.getAjaxScript();
            }
            
            if (!OAString.isEmpty(s)) sb.append(s + "\n");
            if (bLastDebug != bDebugx) {
                if (bDebugx) sb.append("    $('#"+comp.getId()+"').addClass('oaDebug');\n");
                else sb.append("    $('#"+comp.getId()+"').removeClass('oaDebug');\n");
            }
        }
        alNewAddComponent.clear();

        sb.append("$('#oacommand').val('');"); // set back to blank
        sb.append("$('#oaparam').val('');"); // set back to blank

        if (!OAString.isEmpty(jsAddScriptOnce)) {
            sb.append(jsAddScriptOnce);
            jsAddScriptOnce = null;
        }
        
        String js = getProcessingScript();
        if (js != null) sb.append(js); 
        
        js = sb.toString();
        if (js == null) js = "";

        String s = getAjaxCallbackScript();
        if (s != null) js += s;
        
        s = getRedirect();
        if (OAString.isNotEmpty(s)) {
            setRedirect(null);
            js += "window.location = '"+s+"';";            
        }
        
        bLastDebug = bDebugx;

        // js = OAString.convert(js, "\n", "\\n");
        return js;
    }

    /**
     * Used to show popup showing any oaprocesses that are running
     * @see #addProcess(OAProcess)
     */
    protected String getProcessingScript() {
        StringBuilder sb = new StringBuilder(1024);

        boolean b = false;
        String title = null;
        int cnt = 0;
        int perc = 0;
        int step1 = 0;
        int step2 = 0;
        String step = "";
        boolean bBlock = false;

        for (FormProcess fp : alProcess) {
            OAProcess p = fp.p;
            if (!fp.bShowInDialog) continue;
            if (!p.isDone()) {
                cnt++;
                if (!bBlock && p.getBlock()) {
                    if (!p.isBlockTimedout()) {
                        if (!p.isTimedout()) {
                            bBlock = true;
                        }
                    }
                }
            }
            
            b = true;
            String s = p.getName();
            
            if (p.isDone()) {
                if (s == null) s = "";
                s += " - Done";
            }
            if (p.getCancelled()) {
                if (s == null) s = "";
                s += " - Cancelled";
            }
            
            if (OAString.isNotEmpty(s)) {
                if (title == null) title = s;
                else title += ", "+ s;
            }
            
            step1 = p.getCurrentStep();
            step2 = p.getTotalSteps();
            
            if (p.isDone()) perc = 100;
            else if (step1 > step2) perc = 98;
            else if (step1 < 2) perc = 0;
            else {
                perc = (int) ( ( (step1-1)/((double)step2) ) * 100.0);
            }
            
            String[] ss = p.getSteps();
            if (ss != null && step1 > 0 && (step1-1) < ss.length) step = ss[step1-1];
        }
        
        if (!b) {
            sb.append("$('#oaFormProcess').modal('hide');\n");
        }
        else {
            if (title == null) title = "";
            else title = OAString.convert(title, "'", "\\'");
            sb.append("$('#oaFormProcessTitle').html('"+title+"');\n");
            if (bBlock && cnt > 0) {
                sb.append("$('#oaFormProcessClose').hide();\n");
                sb.append("$('#oaFormProcessFooter').hide();\n");
            }
            else {
                sb.append("$('#oaFormProcessClose').show();\n");
                sb.append("$('#oaFormProcessFooter').show();\n");
            }
            
            sb.append("$('#oaFormProcessProgress').css({width: '"+perc+"%'}).html('"+perc+"%');\n");
            
            if (cnt > 1 || (step1 == 0 && step2 == 0 && OAString.isEmpty(step))) {
                sb.append("$('#oaFormProcessStep').html('');\n");
                sb.append("$('#oaFormProcessMessage').html('');\n");
            }
            else {
                sb.append("$('#oaFormProcessStep').html('Step "+step1+" of "+step2+"');\n");
                sb.append("$('#oaFormProcessMessage').html('"+OAString.getNonNull(step)+"');\n");
            }
            
            sb.append("if ($('#oaFormProcessClosed').val() != 'true') {");
            sb.append("$('#oaFormProcess').modal('show'); }\n");
            if (!bFormProcessClosed) requestAjaxCallback();
        }
        return sb.toString();
    }    
    
    
    
    protected void getMessages(StringBuilder sb) {
        String[] msg1, msg2;

        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getMessages();
            msg2 = session.getMessages();
            session.clearMessages();
        }
        boolean b = _addMessages(sb, "oaFormMessage", "Message", msg1, msg2, this.getMessages());

        clearMessages();

        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getErrorMessages();
            msg2 = session.getErrorMessages();
            session.clearErrorMessages();
        }
        b = b || _addMessages(sb, "oaFormErrorMessage", "Error", msg1, msg2, this.getErrorMessages());
        clearErrorMessages();
        
        
        // popup
        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getPopupMessages();
            msg2 = session.getPopupMessages();
            session.clearPopupMessages();
        }
        b = b || _addMessages(sb, null, null, msg1, msg2, this.getPopupMessages());
        clearPopupMessages();

        if (b) {  // have redirect show after message is displayed
            String s = getRedirect();
            if (OAString.isNotEmpty(s)) {
                setRedirect(null);
                s = "$('#oaformDialog').on('hidden.bs.modal', function () {"+
                    "$('#oaWait').show();" + 
                    "window.location = '"+s+"';"+            
                    "});";
                addScript(s);
            }
        }        
        
        
        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getHiddenMessages();
            msg2 = session.getHiddenMessages();
            session.clearHiddenMessages();
        }
        _addMessages(sb, "oaFormHiddenMessage", "", msg1, msg2, this.getHiddenMessages());
        clearHiddenMessages();
        

        

        // snackbar
        msg1 = msg2 = null;
        if (session != null) {
            msg1 = session.getApplication().getSnackbarMessages();
            msg2 = session.getSnackbarMessages();
            session.clearSnackbarMessages();
        }
        _addSnackbarMessages(sb, null, null, msg1, msg2, this.getSnackbarMessages());
        clearSnackbarMessages();
        
        
        // console.log
        for (String s : alConsole) {
            if (OAString.isEmpty(s)) continue;
            s = OAString.convert(s, "'", "\\'");
            addScript("console.log('"+s+"');");
        }
        alConsole.clear();
    }

    protected transient ArrayList<String> alConsole = new ArrayList<String>(5);
    public void addConsoleMessage(String msg) {
        alConsole.add(msg);
    }
    
    
    private boolean _addMessages(StringBuilder sb, String id, String title, String[] msgs1, String[] msgs2, String[] msgs3) {
        boolean bResult = (msgs1 != null && msgs1.length > 0) || (msgs2 != null && msgs2.length > 0) || (msgs3 != null && msgs3.length > 0);
        if (title == null) title = "";
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
            if (getDebug()) sb.append("    $('#"+id+"').addClass('oaDebug');\n");
            else sb.append("    $('#"+id+"').removeClass('oaDebug');\n");
        }

        msg = OAString.convert(msg, "'", "\\'");
        if (msg.length() > 0) {
            if (id != null) {
                sb.append("if ($('#"+id+"').length) {");
                sb.append("  $('#"+id+"').html('"+msg+"');");
                sb.append("  $('#"+id+"').show();");
                sb.append("} else {");
                sb.append("    oaShowMessage('"+title+"', '"+msg+"');\n");
                sb.append("}");
            }
            else {
                sb.append("oaShowMessage('"+title+"', '"+msg+"');\n");
            }
        }
        else {
            if (id != null) {
                sb.append("$('#"+id+"').hide();");
            }
        }
        return bResult;
    }

    private void _addSnackbarMessages(StringBuilder sb, String id, String title, String[] msgs1, String[] msgs2, String[] msgs3) {
        if (title == null) title = "";
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

        msg = OAString.convert(msg, "'", "\\'");
        if (msg.length() > 0) {
            sb.append("oaShowSnackbarMessage('"+msg+"');\n");
        }
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
        if (!alNewAddComponent.contains(comp)) {
            alNewAddComponent.add(comp);
        }
        comp.setForm(this);
    }

    

    /** used to manage ajax callbacks from the browser, so that not too many will be created on the browser. */
    private final AtomicInteger aiAjaxIdLastRequest = new AtomicInteger();
    
    private final AtomicInteger aiAjaxIdLastUsed = new AtomicInteger();
    private final AtomicInteger aiAjaxIdLastReceived = new AtomicInteger();

    
    /** used to have the browser ajax callback in 2500ms */
    public void requestAjaxCallback() {
        aiAjaxIdLastRequest.incrementAndGet();
    }    

    protected String getAjaxCallbackScript() {
        try {
            lockSubmit.writeLock().lock();
            return _getAjaxCallbackScript();
        }
        finally {
            lockSubmit.writeLock().unlock();
        }
    }
    protected String _getAjaxCallbackScript() {
        int x1 = aiAjaxIdLastUsed.get();
        int x2 = aiAjaxIdLastRequest.get();
        if (x1 == x2) return null;

        x2 = aiAjaxIdLastReceived.get();
        if (x1 > x2) return null;  // will be returning
        
        aiAjaxIdLastUsed.set(aiAjaxIdLastRequest.get());
        
        String s = "$('#oaajaxid').val('"+aiAjaxIdLastUsed.get()+"');";
        s = "window.setTimeout(function() {"+s+"ajaxSubmit2();}, 2500);";
        return s;
    }
    
    
    private final ReentrantReadWriteLock lockSubmit = new ReentrantReadWriteLock();
    
    /** called to process the form.
     *  See oaform.jsp
     */
    public String processSubmit(OASession session, HttpServletRequest request, HttpServletResponse response) {
        if (this.session == null) this.session = session;
        try {
            lockSubmit.writeLock().lock();
            return _processSubmit(session, request, response);
        }
        finally {
            lockSubmit.writeLock().unlock();
        }
    }    
    protected String _processSubmit(OASession session, HttpServletRequest request, HttpServletResponse response) {
        if (this.session == null) this.session = session;

        try {
            // Thread.sleep(350); //qqqqq test delay            
            request.setCharacterEncoding("UTF-8");
        }
        catch (Exception e) {}


        HashMap<String, String[]> hmNameValue = new HashMap<String, String[]>();

        
        String contentType = request.getContentType();
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
            try {
                processMultipart(request, hmNameValue);
            }
            catch (Exception e){
                this.addErrorMessage(e.toString());
            }
        }
        else {
            Enumeration enumx = request.getParameterNames();
            while ( enumx.hasMoreElements()) {
                String name = (String) enumx.nextElement();
                String[] values = request.getParameterValues(name);
                hmNameValue.put(name,values);
            }
        }

        if (!bFormProcessClosed) {
            String[] ss = hmNameValue.get("oaFormProcessClosed");
            if (ss != null && ss.length == 1) {
                bFormProcessClosed = OAConv.toBoolean(ss[0]);
            }
        }
        
        String[] ss = hmNameValue.get("oaajaxid");
        if (ss != null && ss.length == 1 && OAString.isInteger(ss[0])) {
            int x = OAConv.toInt(ss[0]);
            int x2 = aiAjaxIdLastReceived.get();
            if (x > x2) {
                aiAjaxIdLastReceived.set(x);
            }
        }

        ss = hmNameValue.get("jsDateTzOffset");
        if (session != null && ss != null && ss.length > 0) {
            int tzSecs = OAConv.toInt(ss[0]); // minutes
            tzSecs *= 60 * 1000;
            tzSecs *= -1; // tz offset are different between javascript and java.  Need to multiple by -1.
            session.setBrowserTimeZoneOffset(tzSecs);
        }

        boolean bShouldProcess = beforeSubmit();

        String forward = null;

        if (bShouldProcess) {
            forward = getForwardUrl();
            if (OAString.isEmpty(forward)) forward = this.getUrl();
            
            OAJspComponent compSubmit = onSubmit(request, response, hmNameValue);
            
            forward = onSubmit(compSubmit, forward);
            forward = afterSubmit(forward);
            forward = onJspSubmit(compSubmit, forward);
        }
        if (OAString.isEmpty(forward)) {
            forward = this.getUrl();
        }
        return forward;
    }

    protected String onSubmit(OAJspComponent compSubmit, String forward) {
        if (compSubmit != null) {
            String s = compSubmit.getForwardUrl();
            if (s != null) forward = s;
            compSubmit._beforeOnSubmit();
            forward = compSubmit._onSubmit(forward);
        }
        return forward;
        
    }
    
    public String processForward(OASession session, HttpServletRequest request, HttpServletResponse response) {
        HashMap<String,String[]> hmNameValue = new HashMap<String, String[]>();
        Enumeration enumx = request.getParameterNames();
        while ( enumx.hasMoreElements()) {
            String name = (String) enumx.nextElement();
            String[] values = request.getParameterValues(name);
            hmNameValue.put(name,values);
        }
        return processForward(session, request, response, null);
    }

    
    /**
     * Called by oaforward.jsp to be able to have a link call submit method without doing a form submit.
     */
    public String processForward(OASession session, HttpServletRequest request, HttpServletResponse response, HashMap<String,String[]> hmNameValue) {
        if (this.session == null) this.session = session;
        try {
            request.setCharacterEncoding("UTF-8");
        }
        catch (Exception e) {}

        String id = request.getParameter("oacommand");
        OAJspComponent comp = getComponent(id);
        if (comp == null) return getUrl();

        String forward = comp.getForwardUrl();

        comp._onFormSubmitted(request, response, hmNameValue);
        comp._beforeOnSubmit();
        forward = comp._onSubmit(forward);
    
        if (OAString.isEmpty(forward)) {
            forward = this.getUrl();
        }
        
        return forward;
    }


    // Parse Multipart posted forms ============================================================
    protected void processMultipart(ServletRequest request, HashMap<String, String[]> hmNameValue) throws Exception {
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
            String[] values = (String[]) hmNameValue.get(name);
            if (values == null) hmNameValue.put(name, new String[] { nameValue[1] });
            else {
                String[] newValues = new String[values.length+1];
                System.arraycopy(values,0,newValues,0,values.length);
                newValues[values.length] = nameValue[1];
                hmNameValue.put(name,newValues);
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
    public OAButtonList getButtonList(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAButtonList) return (OAButtonList) comp;
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
        if (comp instanceof OACombo) return (OACombo) comp;
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
    public OAHtmlSelect getSelect(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAHtmlSelect) return (OAHtmlSelect) comp;
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
    public OAList getList(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAList) return (OAList) comp;
        return null;
    }
    public OADialog getDialog(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OADialog) return (OADialog) comp;
        return null;
    }
    public OAPopup getPopup(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAPopup) return (OAPopup) comp;
        return null;
    }
    public OAPopupList getPopupList(String id) {
        OAJspComponent comp = getComponent(id);
        if (comp instanceof OAPopupList) return (OAPopupList) comp;
        return null;
    }

    
    protected final ArrayList<String> alCss = new ArrayList<>();
    /**
     * filepath to include on CSS files on the page
     * @param filePath full path of page (relative to webcontent)
     */
    public void addCss(String filePath) {
        if (filePath == null) return;
        if (!alCss.contains(filePath)) alCss.add(filePath);
        
    }

    protected final ArrayList<String> alJs = new ArrayList<>();
    /**
     * filepath to include on JS files on the page
     * @param filePath full path of page (relative to webcontent)
     */
    public void addJs(String filePath) {
        if (filePath == null) return;
        if (!alJs.contains(filePath)) alJs.add(filePath);
    }
    
    
    /**
     * add names of CSS to include on page
     * @param name of css to include,  see {@link OAJspDelegate#registerRequiredCss(String, String)} 
     */
    public void addRequiredCssName(String name) {
        if (name == null) return;
        name = name.toUpperCase();
        if (!alRequiredCssName.contains(name)) alRequiredCssName.add(name);
        
    }
    /**
     * add names of JS to include on page
     * @param name of js to include,  see {@link OAJspDelegate#registerRequiredJs(String, String)} 
     */
    public void addRequiredJsName(String name) {
        if (name == null) return;
        name = name.toUpperCase();
        if (!alRequiredJsName.contains(name)) alRequiredJsName.add(name);
    }
    
    
    public String getCssInsert() {
        ArrayList<String> alName = new ArrayList<>();
        
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (!(comp instanceof OAJspRequirementsInterface)) continue;
            String[] ss = ((OAJspRequirementsInterface) comp).getRequiredCssNames();
            if (ss == null) continue;

            for (String s : ss) {
                if (!alName.contains(s.toUpperCase())) alName.add(s.toUpperCase());
            }
        }
        
        // include oajsp.css after components
        if (!alName.contains(OAJspDelegate.CSS_oajsp.toUpperCase())) alName.add(OAJspDelegate.CSS_oajsp.toUpperCase());

        for (String s : alRequiredCssName) {
            if (!alName.contains(s.toUpperCase())) alName.add(s.toUpperCase());
        }
        
        ArrayList<String> alFilePath = new ArrayList<>();

        for (String s : alName) {
            s = OAJspDelegate.getCssFilePath(s);
            if (s != null && !alFilePath.contains(s)) alFilePath.add(s);
        }
        
        for (String s : alCss) {
            if (s != null && !alFilePath.contains(s)) alFilePath.add(s);
        }

        StringBuilder sb = new StringBuilder(1024); 
        for (String s : alFilePath) {
            s = "<link rel=\"stylesheet\" type=\"text/css\" href=\""+s+"\">\n";
            sb.append(s);
        }
        
        return sb.toString();
    }

    public String getJsInsert() {
        ArrayList<String> alName = new ArrayList<>();
        
        alName.add(OAJspDelegate.JS_jquery);
        
        for (String s : alRequiredJsName) {
            s = s.toUpperCase();
            if (!alName.contains(s)) alName.add(s);
        }
        
        for (int i=0; ;i++) {
            if (i >= alComponent.size()) break;
            OAJspComponent comp = alComponent.get(i);
            if (!(comp instanceof OAJspRequirementsInterface)) continue;
            String[] ss = ((OAJspRequirementsInterface) comp).getRequiredJsNames();
            if (ss == null) continue;

            for (String s : ss) {
                s = s.toUpperCase();
                if (!alName.contains(s)) alName.add(s);
            }
        }        
        
        ArrayList<String> alFilePath = new ArrayList<>();

        for (String s : alName) {
            s = OAJspDelegate.getJsFilePath(s);
            if (s != null && !alFilePath.contains(s)) alFilePath.add(s);
        }
        
        for (String s : alJs) {
            if (s != null && !alFilePath.contains(s)) alFilePath.add(s);
        }

        StringBuilder sb = new StringBuilder(1024); 
        for (String s : alFilePath) {
            s = "<script type=\"text/javascript\" language=\"javascript\" src=\""+s+"\"></script>\n";
            sb.append(s);
        }
        
        return sb.toString();
    }
    
    public void setRedirect(String url) {
        this.urlRedirect = url;
    }
    public String getRedirect() {
        return urlRedirect;
    }
    
    
}
