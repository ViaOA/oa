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


import java.awt.Color;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * Used to control any html element: 
 * hide or show
 * html text: min/max line width, max rows to display
 * click event: ajax or form submit 
 * forward URL: to act as a link
 * helper methods to set attributes
 * 
 * @see #setConvertTextToHtml(boolean) set to false if text is already in html
 * @author vvia
 */
public class OAHtmlElement implements OAJspComponent, OAJspRequirementsInterface{
    private static final long serialVersionUID = 1L;

    protected Hub hub;
    protected String id;
    protected String visiblePropertyPath;
    protected String htmlPropertyPath;
    
    protected OAForm form;
    protected boolean bVisible = true;
    protected boolean bSubmit;
    protected boolean bAjaxSubmit;
    protected String forwardUrl;
    protected ArrayList<OAHtmlAttribute> alAttribute; 
    protected String lastAjaxSent;
    private boolean bIsPlainText;  // true if the text is not HTML

    private HashMap<String, String> hmStyle;
    private HashSet<String> hsClassAdd;
    private HashSet<String> hsClassRemove;

    protected String height; // ex:  200px,  12em
    protected String width; // ex:  200px,  12em
    protected String minHeight; // ex:  200px,  12em
    protected String minWidth; // ex:  200px,  12em
    protected String maxHeight; // ex:  200px,  12em
    protected String maxWidth; // ex:  200px,  12em
    
    protected String overflow;
    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;

    protected String confirmMessage;
    protected OATemplate templateConfirmMessage;
    
    
    public void addAttribute(OAHtmlAttribute attr) {
        if (attr == null) return;
        if (alAttribute == null) alAttribute = new ArrayList<OAHtmlAttribute>();
        alAttribute.add(attr);
    }
    
    // used when setting html text
    protected String format;
    protected int lineWidth, maxRows, minLineWidth; // in characters
   
    
    /**
     * @param overflow visible, hidden, scroll, auto, etc 
     */
    public void setOverflow(String overflow) {
        this.overflow = overflow;
    }
    public String getOverflow() {
        return overflow;
    }
    
    public OAHtmlElement() {
    }
    
    public OAHtmlElement(String id) {
        this.id = id;
    }
    public OAHtmlElement(String id, Hub hub) {
        this.id = id;
        this.hub = hub;
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath, int width) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(width);
        setMinLineWidth(width-3);
        setMaxRows(0);
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(0);
        setMinLineWidth(0);
        setMaxRows(0);
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath, int width, int minWidth, int maxRows) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(width);
        setMinLineWidth(minWidth);
        setMaxRows(maxRows);
    }

    public Hub getHub() {
        return hub;
    }
    
    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
    }
    
    
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    @Override
    public boolean _beforeFormSubmitted() {
        return true;
    }

    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
        if (b) setSubmit(false);
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }

    public void setSubmit(boolean b) {
        bSubmit = b;
        if (b) setAjaxSubmit(false);
    }
    public boolean getSubmit() {
        return bSubmit;
    }

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted  = (id != null && id.equals(s));
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public void _beforeOnSubmit() {
    }
    
    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        bHadToolTip = false;

        StringBuilder sb = new StringBuilder(1024);
        
        String furl = getForwardUrl();

        if (bSubmit || bAjaxSubmit) {
            if (bAjaxSubmit) {
                sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
            }
            
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        else if (!OAString.isEmpty(furl)) {
            sb.append("$('#"+id+"').click(function() {window.location = 'oaforward.jsp?oaform="+getForm().getId()+"&oacommand="+id+"';return false;});\n");
            //was: sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');window.location = '"+furl+"';return false;});\n");
        }
        
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        String js = sb.toString();
        
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    public void setPlainText(boolean b) {
        bIsPlainText = b;
    }
    public boolean isPlainText() {
        return bIsPlainText;
    }
    
    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        if (getVisible()) sb.append("$('#"+id+"').show();");
        else sb.append("$('#"+id+"').hide();");

        
        // tooltip
        String prefix = null;
        String tt = getProcessedToolTip();
        if (OAString.isNotEmpty(tt)) {
            tt = OAString.convertForSingleQuotes(tt);
            if (!bHadToolTip) {
                bHadToolTip = true;
                prefix = "$('#"+id+"').tooltip();\n";
            }
            
            sb.append("$('#"+id+"').data('bs.tooltip').options.title = '"+tt+"';\n");
            sb.append("$('#"+id+"').data('bs.tooltip').options.placement = 'top';\n");
        }
        else {
            if (bHadToolTip) {
                sb.append("$('#"+id+"').tooltip('destroy');\n");
                bHadToolTip = false;
            }
        }
        
        
        String html = getHtml();
        if (html != null) {
            if (isPlainText()) {  // some html properties dont have < or > in them
            //was: if (html.indexOf("<") < 0 && html.indexOf(">") < 0) {
                if (maxRows == 1) {
                    if (lineWidth > 0) html = OAString.lineBreak(html, lineWidth, " ", 1);
                    html = Util.convert(html, "\r\n", " ");
                    html = Util.convert(html, "\n", " ");
                    html = Util.convert(html, "\r", " ");
                }
                else {
                    if (maxRows > 1) {
                        html = OAString.lineBreak(html, lineWidth, "\n", maxRows);
                    }
                }
                html = Util.convert(html, "\r\n", "<BR>");
                html = Util.convert(html, "\n", "<BR>");
                html = Util.convert(html, "\r", "<BR>");
                html = Util.convert(html, "  ", "&nbsp;&nbsp;");
            }
            else {
                // all html needs to be on one line, since it is output in javascript code
                html = Util.convert(html, "\r\n", " ");
                html = Util.convert(html, "\n", " ");
                html = Util.convert(html, "\r", " ");
            }
            
            html = OAString.convert(html, "\\'", "\'");
            html = OAString.convert(html, "\'", "\\'");
            sb.append("$('#"+id+"').html('"+html+"');\n");
        }
        
        if (alAttribute != null) {
            for (OAHtmlAttribute at : alAttribute) {
                String s = at.getScript(id);
                if (!OAString.isEmpty(s)) sb.append(s+"\n");
            }
        }

        String s = getStyleJs();
        if (s != null) sb.append("$('#"+id+"').css("+s+");\n");

        s = getClassJs();
        if (s != null) sb.append(s+"\n");

        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }
        
        return js;
    }

    protected String html;
    public void setHtml(String html) {
        this.html = html;
    }
    protected String getHtml() {
        if (hub == null || getHtmlPropertyPath() == null) return this.html;
        
        Object obj = hub.getAO();
        if (obj != null) {
            if (!(obj instanceof OAObject)) return OAConv.toString(obj, getFormat());
        }
        String value = getHtml((OAObject)obj);
        
        return value;
    }    

    public String getHtml(OAObject obj) {
        String value = null;
        
        if (obj != null) value = obj.getPropertyAsString(htmlPropertyPath, getFormat());
        if (value == null) value = "";
 
        int addSp = (minLineWidth <= 0) ? 0 : (minLineWidth - value.length()); 
        for (int i=0; i<addSp; i++) value += " ";
        
        return value;
    }
    
    @Override
    public String getEditorHtml(OAObject obj) {
        return getHtml(obj);
    }
    
    @Override
    public String getRenderHtml(OAObject obj) {
        return getHtml(obj);
    }
    
    
    
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
    public String getHtmlPropertyPath() {
        return htmlPropertyPath;
    }
    public void setHtmlPropertyPath(String htmlPropertyPath) {
        this.htmlPropertyPath = htmlPropertyPath;
    }
    
    public void setHtml(String htmlPropertyPath, String format, int lineWidth, int minLineWidth, int maxRows) {
        setHtmlPropertyPath(htmlPropertyPath);
        if (!OAString.isEmpty(format)) setFormat(format);
        setLineWidth(lineWidth);
        setMinLineWidth(minLineWidth);
        setMaxRows(maxRows);
    }
    
    private boolean bDefaultFormat=true;
    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format == null && bDefaultFormat && !OAString.isEmpty(htmlPropertyPath) && hub != null) {
            bDefaultFormat = false;
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), htmlPropertyPath);
            if (pp != null) format = pp.getFormat();
        }
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
        bDefaultFormat = false;
    }
    public int getMinLineWidth() {
        return minLineWidth;
    }
    public void setMinLineWidth(int minLineWidth) {
        this.minLineWidth = minLineWidth;
    }
    public int getLineWidth() {
        return lineWidth;
    }
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
    public void setMaxRows(int rows) {
        this.maxRows = rows;
    }
    public int getMaxRows() {
        return this.maxRows;
    }

    
    @Override
    public void setEnabled(boolean b) {
    }
    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (OAString.isEmpty(visiblePropertyPath)) return bVisible;

        if (hub == null) return false;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        Object value = obj.getPropertyAsString(visiblePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }

    public void addCss(String name, Color color) {
        addStyle(name, color);
    }

    public void addStyle(String name, Color color) {
        if (color == null) color = Color.white;
        String s = JspUtil.convertToCss(color);
        addStyle(name, s);
    }
    public void addCss(String name, String value) {
        addStyle(name, value);
    }
    public void addStyle(String name, String value) {
        if (name == null) return;
        if (value == null) value = "";
        if (hmStyle == null) hmStyle = new HashMap<String, String>();
        hmStyle.put(name, value);
    }
    public void removeStyle(String name) {
        addStyle(name, "inherit");
    }

    protected String getStyleJs() {
        ArrayList<String> al = new ArrayList<String>();
        
        if (hmStyle != null) {
            for (Map.Entry<String, String> ex : hmStyle.entrySet()) {
                String sx = ex.getKey();
                String v = ex.getValue();
                al.add(sx + ":'" + v + "'");
            }
        }

        String s1 = getHeight();
        String s2 = getMinHeight();
        String s3 = getMaxHeight();

        if (OAString.isNotEmpty(s1)) {
            if (OAString.isEmpty(s2)) al.add("'min-height':'"+s1+"'");
            if (OAString.isEmpty(s3)) al.add("'max-height':'"+s1+"'");
        }
        if (OAString.isNotEmpty(s2)) {
            al.add("'min-height':'"+s2+"'");
        }
        if (OAString.isNotEmpty(s3)) {
            al.add("'max-height':'"+s3+"'");
        }

        s1 = getWidth();
        s2 = getMinWidth();
        s3 = getMaxWidth();
        boolean bWidth = OAString.isNotEmpty(s1) || OAString.isNotEmpty(s2) || OAString.isNotEmpty(s3);
        
        if (OAString.isNotEmpty(s1)) {
            if (OAString.isEmpty(s2)) al.add("'min-width':'"+s1+"'");
            if (OAString.isEmpty(s3)) al.add("'max-width':'"+s1+"'");
        }
        if (OAString.isNotEmpty(s2)) {
            al.add("'min-width':'"+s2+"'");
        }
        if (OAString.isNotEmpty(s3)) {
            al.add("'max-width':'"+s3+"'");
        }

        if (OAString.isNotEmpty(overflow)) {
            al.add("overflow:'"+overflow+"'");
            if (overflow.equalsIgnoreCase("hidden")) {
                al.add("'text-overflow':'ellipsis'");
            }
        }

        String css = null;
        for (String s : al) {
            if (css == null) css = "{";
            else css += ",";
            css += s;
        }
        if (css != null) css += "}";
        return css;
    }

    
    public void addClass(String name) {
        if (name == null) return;
        if (hsClassAdd == null) hsClassAdd = new HashSet<>();
        hsClassAdd.add(name);
    }
    public void removeClass(String name) {
        if (name == null) return;
        if (hsClassAdd != null) {
            hsClassAdd.remove(name);
        }
        if (hsClassRemove == null) hsClassRemove = new HashSet<>();
        hsClassRemove.add(name);
    }
    protected String getClassJs() {
        String s = null;
        Iterator itx;
        if (hsClassAdd != null) {
            itx = hsClassAdd.iterator();
            for ( ; itx.hasNext() ;  ) {
                String sx = (String) itx.next();
                if (s == null) s = "";
                s += "$('#"+id+"').addClass('"+sx+"');";
            }
        }
        
        if (hsClassRemove != null) {
            itx = hsClassRemove.iterator();
            for ( ; itx.hasNext() ;  ) {
                String sx = (String) itx.next();
                if (s == null) s = "";
                s += "$('#"+id+"').removeClass('"+sx+"');";
            }
        }        
        return s;
    }

    public void setMinHeight(String val) {
        this.minHeight = val;
    }
    public String getMinHeight() {
        return this.minHeight;
    }
    public void setMinWidth(String val) {
        this.minWidth = val;
    }
    public String getMinWidth() {
        return this.minWidth;
    }
    
    public void setMaxHeight(String val) {
        this.maxHeight = val;
    }
    public String getMaxHeight() {
        return this.maxHeight;
    }
    public void setMaxWidth(String val) {
        this.maxWidth = val;
    }
    public String getMaxWidth() {
        return this.maxWidth;
    }
    public void setHeight(String val) {
        this.height = val;
    }
    public String getHeight() {
        return this.height;
    }
    public void setWidth(String val) {
        this.width = val;
    }
    public String getWidth() {
        return this.width;
    }
    
    public void setToolTip(String tooltip) {
        this.toolTip = tooltip;
        templateToolTip = null;
    }
    public String getToolTip() {
        return this.toolTip;
    }
    public String getProcessedToolTip() {
        if (OAString.isEmpty(toolTip) || toolTip.indexOf("<") < 0) return toolTip;
        if (templateToolTip == null) {
            templateToolTip = new OATemplate();
            templateToolTip.setTemplate(getToolTip());
        }
        OAObject obj = null;
        if (hub != null) {
            Object objx = hub.getAO();
            if (objx instanceof OAObject) obj = (OAObject) objx;
        }
        String s = templateToolTip.process(obj, hub, null);
        return s;
    }
    
    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.JS_jquery);
        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.JS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.CSS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    public void setConfirmMessage(String msg) {
        this.confirmMessage = msg;
    }
    public String getConfirmMessage() {
        return confirmMessage;
    }
    public String getProcessedConfirmMessage(OAObject obj) {
        if (OAString.isEmpty(confirmMessage) || confirmMessage.indexOf("<") < 0) return confirmMessage;
        
        if (templateConfirmMessage == null) {
            templateConfirmMessage = new OATemplate();
            templateConfirmMessage.setTemplate(getConfirmMessage());
        }
        String s = templateConfirmMessage.process(obj);
        return s;
    }
    
}
