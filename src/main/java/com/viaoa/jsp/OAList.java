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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;


/**
 * Used with an HTML <UL/OL> or <OL/OL> to replace the <LI> with hub objects.
 * 
 * @author vvia
 */
public class OAList implements OAJspComponent, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    protected Hub hub;

    // Important Note:  this could be a composite (ex: OAButtonList uses OAPopupList uses OAList)
    //    and this "id" would be different then "getId()" ... see OAButtonList constructor
    protected String id;
    
    
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit=true;
    protected boolean bSubmit=false;
    protected String forwardUrl;
    protected String nullDescription = "";
    protected String maxHeigth; // ex:  200px,  12em
    protected boolean bRequired;
    protected String name;

    protected String format;
//    protected int lineWidth, maxRows, minLineWidth;
    
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected int columns, popupColumns;
    
    protected int rows;
    protected int lastRow;
    
    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;
    
    protected String htmlTemplate;
    private OATemplate template;
    private HashMap<String, OAJspComponent> hmChildren = new HashMap<String, OAJspComponent>();
    
    protected HashMap<Integer, String> hmHeading;
    
    
    public OAList(String id, Hub hub, String propertyPath) {
        this(id, hub, propertyPath, 0, 0);
    }
    public OAList(String id, Hub hub, String propertyPath, int cols, int rows) {
        this.id = id;
        this.hub = hub;
        setPropertyPath(propertyPath);
        this.columns = cols;
        this.rows = rows;
    }

    /**
     * set the heading to use, beginning at a specific row.
     */
    public void addHeading(int row, String heading) {
        if (hmHeading == null) hmHeading = new HashMap<>();
        hmHeading.put(row, heading);
    }
    
    public int getColumns() {
        return columns;
    }
    public void setColumns(int x) {
        this.columns = x;
    }
    public int getPopupColumns() {
        return popupColumns;
    }
    public void setPopupColumns(int x) {
        this.popupColumns = x;
    }

    /**
     * how many rows to display.
     */
    public int getRows() {
        return rows;
    }
    public void setRows(int x) {
        this.rows = x;
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

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    
    /** used when displaying error message for this textfield */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
        for (Map.Entry<String, OAJspComponent> e : hmChildren.entrySet()) {
            e.getValue().setForm(form);
        }
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    private boolean bDefaultFormat=true;
    public void setFormat(String fmt) {
        this.format = fmt;
        bDefaultFormat = false;
    }
    public String getFormat() {
        if (format == null && bDefaultFormat && hub != null) {
            bDefaultFormat = false;
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPath);
            if (pp != null) format = pp.getFormat();
        }
        return format;
    }
    
    
    @Override
    public boolean _beforeFormSubmitted() {
        return true;
    }

    private String submitUpdateScript;
    

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        boolean bWasSubmitted = _myOnSubmit(req, resp, hmNameValue);
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        // Enumeration enumx = req.getParameterNames();
        
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted = (id != null && (id.equalsIgnoreCase(s) || getId().equalsIgnoreCase(s)));
        
        
        String name = null;
        String[] values = null;

        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.equalsIgnoreCase("oalist"+id)) continue;
            values = ex.getValue();
            break;
        }

        if (values == null || values.length == 0 || OAString.isEmpty(values[0])) {
            return false;        
        }

        int row = OAConv.toInt(values[0]);
        if (row == lastRow) return bWasSubmitted; // not changed
        
        
        Object obj = hub.getAt(row);
        if (hub != null) hub.setAO(obj);

        
        submitUpdateScript = "";
        submitUpdateScript += "$('#"+id+" li').removeClass('oaSelected');";

        if (hub.getPos() >= 0) {
            submitUpdateScript += "$('#oalist"+id+"').val('"+hub.getPos()+"');";
            s = " li:nth-child("+(hub.getPos()+1)+")";
        }
        else {
            submitUpdateScript += "$('#oalist"+id+"').val('');";
            s = " li:nth-child("+(hub.getSize()+1)+")";
        }
        submitUpdateScript += "$('#"+id+s+"').addClass('oaSelected');";

        return bWasSubmitted; // true if this caused the form submit
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

    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
        if (b) setSubmit(false);
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }
    public void setSubmit(boolean b) {
        if (b) setAjaxSubmit(false);
        bSubmit = b;
    }
    public boolean getSubmit() {
        return bSubmit;
    }
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        submitUpdateScript = null;
        bHadToolTip = false;
        StringBuilder sb = new StringBuilder(1024);
    
        sb.append("$('form').prepend(\"<input id='oalist"+id+"' type='hidden' name='oalist"+id+"' value=''>\");\n");

        
        sb.append("function oaList"+id+"Click() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        
        sb.append("    if (v == null) return;\n");
        sb.append("    $('#oalist"+id+"').val(v);\n");
        sb.append("    $('#oacommand').val('" + id + "');\n");
        
        
        String s = getOnLineClickJs();
        if (s != null) {
            sb.append("    "+s);
        }
        
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
            sb.append("    $('#oacommand').val('');\n");
        }
        sb.append("}\n");
        
        s = getScript2();
        if (s != null) sb.append(s);
        
        s = getAjaxScript();
        if (s != null) sb.append(s);
        
        s = getScript3();
        if (s != null) sb.append(s);
        
        if (isRequired()) {
            sb.append("$('#" + id + "').addClass('oaRequired');\n");
            sb.append("$('#" + id + "').attr('required', true);\n");
        }
        sb.append("$('#" + id + "').blur(function() {$(this).removeClass('oaError');}); \n");

        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#" + id + "').addClass('oaSubmit');\n");
        }
        
        
        if (OAString.isNotEmpty(maxHeigth)) {
            sb.append("$('#"+id+"').css(\"max-height\", \""+maxHeigth+"\");\n");
        }
        else if (rows > 0) {
            int x = (int) (rows * 1.25);
            x += 3;
            sb.append("$('#"+id+" > li').css(\"line-height\", \"1.1em\");\n");
            sb.append("$('#"+id+"').css(\"max-height\", \""+x+"em\");\n");
        }
        
        if (columns > 0 || popupColumns > 0) {
            int x;
            if (popupColumns < 1) x = (int) (columns * .75);
            else x = (int) (popupColumns * .75);
            
            
            sb.append("$('#"+id+"').css(\"width\", \""+(x+2)+"em\");\n");
            sb.append("$('#"+id+"').css(\"max-width\", \""+(x+3)+"em\");\n");
            
            sb.append("$('#"+id+" > li').css(\"width\", \""+x+"em\");\n");
            sb.append("$('#"+id+" > li').css(\"max-width\", \""+x+"em\");\n");
        }
        
        String js = sb.toString();
        return js;
    }
    
    protected String getScript2() {
        return null;
    }
    protected String getScript3() {
        return null;
    }
    

    @Override
    public String getVerifyScript() {
        StringBuilder sb = new StringBuilder(1024);

        if (isRequired()) {
            sb.append("if ($('#oalist" + id + "').val() == '') { requires.push('" + (name != null ? name : id) + "'); $('#" + id
                    + "').addClass('oaError');}\n");
        }

        if (sb.length() == 0) return null;
        return sb.toString();
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {

        if (submitUpdateScript != null) {
            String s = submitUpdateScript;
            submitUpdateScript = null;
            lastAjaxSent = null;
            lastRow = hub.getPos();
            return s;
        }
        StringBuilder sb = new StringBuilder(2048);
        

        // boolean bInOptGroup = false;
        for (int pos=0; ;pos++) {
            Object obj = hub.getAt(pos);
            if (obj == null) break;

            boolean b = false;
            if (hmHeading != null) {
                String heading = hmHeading.get(pos);
                if (heading != null) {
//qqqqqqqqqqq heading not working ... optgroup is for <select>, need to create a class and <li class='heading'> .., and js to not click on it                    
                    //if (bInOptGroup) sb.append("</optgroup>");
                    // sb.append("<optgroup label=\""+heading+"\"></optgroup>");
                    //bInOptGroup = true;
                }
            }
            
            sb.append("<li");
            if (obj == hub.getAO()) sb.append(" class='oaSelected'");
            sb.append(" oarow='"+(pos)+"'>");
            String s = getHtml(obj, pos);
            if (s != null) sb.append(s);
            sb.append("</li>");
        }
        //if (bInOptGroup) sb.append("</optgroup>");

        String s = getHtml(null, -1);
        if (s != null) {
            if (s.length() == 0) s += "&nbsp;";
            sb.append("<li");
            if (hub.getAO() == null) sb.append(" class='oaSelected'");
            sb.append(" oarow='-1'>");
            sb.append(s);
            sb.append("</li>");
        }        
        
        String strListing = sb.toString();
        strListing = Util.convert(strListing, "\\", "\\\\");
        strListing = Util.convert(strListing, "'", "\\'");
        
        
        sb = new StringBuilder(strListing.length() + 2048);
        
        sb.append("$('#"+id+"').addClass('oaList');\n");
        
        sb.append("$('#"+id+"').html('"+strListing+"');\n");
        
        sb.append("$('#"+id+" li').addClass('oaTextNoWrap');\n");
        
        
        if (getEnabled()) {
            sb.append("$('#"+id+" li').click(oaList"+id+"Click);\n");
        }


        int pos = lastRow = hub.getPos();
        if (pos < 0) {
            sb.append("$('#oalist"+id+"').val('');\n"); // set back to blank
        }        
        else sb.append("    $('#oalist"+id+"').val('"+pos+"');\n");
        
        
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

        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }

        return js;
    }

    protected String getOnLineClickJs() {
        return null;
    }

    @Override
    public void setEnabled(boolean b) {
        lastAjaxSent = null;  
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return bEnabled && hub != null;
    }


    @Override
    public void setVisible(boolean b) {
        lastAjaxSent = null;  
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return this.bVisible;
    }

    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }
    
    
    public String getHtml(Object obj, int pos) {
        if (obj == null || pos < 0) {
            return getNullDescription();
        }

        String value;
        if (obj instanceof OAObject) {
            value = ((OAObject) obj).getPropertyAsString(getPropertyPath(), getFormat());
            
            String temp = getTemplateHtml(obj, pos);
            if (temp != null) {
                value = temp;
            }
        }
        else value = obj.toString();
        
        return value;
    }


    public void setMaxHeight(String val) {
        this.maxHeigth = val;
    }
    public String getMaxHeigth() {
        return this.maxHeigth;
    }

    public boolean isRequired() {
        return bRequired;
    }
    public boolean getRequired() {
        return bRequired;
    }
    public void setRequired(boolean required) {
        this.bRequired = required;
    }

    public void setToolTip(String tooltip) {
        this.toolTip = tooltip;
        templateToolTip = null;
    }
    public String getToolTip() {
        return this.toolTip;
    }
    public String getProcessedToolTip() {
        if (OAString.isEmpty(toolTip)) return toolTip;
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
        al.add(OAJspDelegate.JS_jquery_ui);
        
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

    @Override
    public String getEditorHtml(OAObject obj) {
        return null;
    }
    @Override
    public String getRenderHtml(OAObject obj) {
        return null;
    }
    
    @Override
    public void _beforeOnSubmit() {
    }


//qqqqqqqqqqqqqqqqqqqqqqqqq
    
    public void add(OAJspComponent comp) {
        if (comp != null) {
            hmChildren.put(comp.getId(), comp);
            comp.setForm(form);
        }
    }
    
    
    /**
     * @see #getTemplate()
     */
    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }
    public String getHtmlTemplate() {
        return this.htmlTemplate;
    }
   

    /**
     * The following values are set and available:
     * $OAPOS, $OACOL, $OAROW
     * @see OATemplate
     */
    public OATemplate getTemplate() {
        if (template != null) return template;
        if (OAString.isEmpty(getHtmlTemplate())) return null;
        
        template = new OATemplate() {
            @Override
            protected String getValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props) {
                String s;
                OAJspComponent comp = hmChildren.get(propertyName);
                if (comp == null) {
                    s = super.getValue(obj, propertyName, width, fmt, props);
                }
                else {
                    s = comp.getRenderHtml(obj);
                }
                s = getTemplateValue(obj, propertyName, width, fmt, props, s);
                return s;
            }
        };
        template.setTemplate(getHtmlTemplate());
        
        return template;
    }
    public void setTemplate(OATemplate temp) {
        this.template = temp;
    }
    
    /**
     * Callback from {@link #getTemplate(Object, int, int, int)}
     */
    public String getTemplateValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props, String defaultValue) {
        return defaultValue;
    }
    
    /**
     * This will use the OATemplate to create the html template for a single object.  
     */
    public String getTemplateHtml(Object objx, int pos) {
        if (!(objx instanceof OAObject)) return null;
        OAObject obj = (OAObject) objx;
        
        if (getTemplate() == null) return null;
        
        template.setProperty("OAPOS", ""+pos);
        template.setProperty("OACOL", ""+(1));
        template.setProperty("OAROW", ""+(pos+1));
        
        String s = template.process(obj);
        
        return s;
    }
    
    public String getHtmlPropertyPath(Object obj, int pos, int row, int col) {
        String result = null;
        String pp = getPropertyPath();
        if (!OAString.isEmpty(pp)) {
            if (obj != null) {
                result = ((OAObject)obj).getPropertyAsString(pp);
            }
        }
        return result;
    }
}
