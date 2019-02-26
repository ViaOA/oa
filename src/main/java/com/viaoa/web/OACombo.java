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
package com.viaoa.web;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubLinkDelegate;
import com.viaoa.object.*;
import com.viaoa.util.*;

// <select id="cbo"></select>


/**
 * Controls html select+options
 *
 * bind to hub, property
 * show/hide, that can be bound to property
 * enabled, that can be bound to property
 * ajax submit on change
 * handle required validation
 * recursive, displayed using indentation
 * option to set the null description
 *
 * 
 * @author vvia
 *
 */
public class OACombo implements OAJspComponent, OATableEditor, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    private Hub hub;

    protected Hub topHub;
    protected OALinkInfo recursiveLinkInfo;
    protected Hub hubSelect;  // used by OASelect
    
    protected String id;
    protected int columns;
    protected int rows;
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected String enablePropertyPath;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit, bSubmit;
    protected String nullDescription = "";
    private boolean required;
    private String name;
    private boolean bFocus;
    protected String forwardUrl;

    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;

    protected String htmlTemplate;
    private OATemplate template;
    
    private boolean bAllowSearch;

    protected HashMap<Integer, String> hmHeading;
    protected String headingPropertyPath;
    
    public OACombo(String id, Hub hub, String propertyPath) {
        this(id, hub, propertyPath, 0);
    }
    
    public OACombo(String id, Hub hub, String propertyPath, int columns) {
        this.id = id;
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.columns = columns;
    }
    public OACombo(String id, Hub hub, String propertyPath, int columns, int rows) {
        this.id = id;
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.columns = columns;
        this.rows = rows;
    }

    public void setPropertyPath(String pp) {
        this.propertyPath = pp;
    }
    public String getPropertyPath() {
        return this.propertyPath;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public Hub getHub() {
        return hub;
    }
    public void setHub(Hub hub) {
        this.hub = hub;
    }
    
    @Override
    public void reset() {
        lastAjaxSent = null;
    }

    public void setRows(int x) {
        rows = x;
    }
    public int getRows() {
        return rows;
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

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        String name = null;
        OAObject obj = null;
        String[] values = null;

        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bSubmittedComponent  = (id != null && id.equals(s));
        boolean bWasSubmitted = false;  // was the combo submited with the form

        OAObject objLinkTo = null;
        if (hmNameValue != null) {        
        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.toUpperCase().startsWith(id.toUpperCase())) continue;

            bWasSubmitted = true;
            values = ex.getValue();
            if (values == null) values = new String[0];
            
            if (name.equalsIgnoreCase(id)) {  // no link to hub
                break;
            }
            if (!name.toUpperCase().startsWith(id.toUpperCase()+"_")) continue;
            Hub hubLink = hub.getLinkHub(true);
            if (hubLink == null) continue;
            
            if (name.toUpperCase().startsWith(id.toUpperCase()+"_")) {
                s = name.substring(id.length()+1);
                if (s.startsWith("guid.")) {
                    s = s.substring(5);
                    OAObjectKey k = new OAObjectKey(null, OAConv.toInt(s), true);
                    objLinkTo = OAObjectCacheDelegate.get(hubLink.getObjectClass(), k);
                }
                else {
                    objLinkTo = OAObjectCacheDelegate.get(hubLink.getObjectClass(), s);
                }
                break;
            }
        }
        }
        ArrayList alSelected = new ArrayList();
        Object objSelected = null;
        for (int i=0; values != null && i < values.length; i++) {
            String value = values[i];
            // now get selected object
            if ("oanull".equals(value)) objSelected = null;
            else {
                if (value.startsWith("pos.")) {
                    int pos = OAConv.toInt(value.substring(4));
                    objSelected = hub.getAt(pos);
                }
                else if (hub.isOAObject()) {
                    if (value.startsWith("guid.")) {
                        value = value.substring(5);
                        OAObjectKey k = new OAObjectKey(null, OAConv.toInt(value), true);
                        objSelected = OAObjectCacheDelegate.get(hub.getObjectClass(), k);
                    }
                    else objSelected = OAObjectCacheDelegate.get(hub.getObjectClass(), value);
                }
                alSelected.add(objSelected);
            }
        }

        if (hubSelect != null) {
            for (Object objx : alSelected) {
                if (!hubSelect.contains(objx)) hubSelect.add(objx);
            }
            for (Object objx : hubSelect) {
                if (!alSelected.contains(objx)) hubSelect.remove(objx);
            }
        }
        else {
            if (!bAjaxSubmit || bSubmittedComponent) {
                if (hub != null) {
                    hub.setAO(objSelected);
                }
            }
        }
        return bSubmittedComponent; // true if this caused the form submit
    }

    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
    }
    
    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }
    public void setSubmit(boolean b) {
        bSubmit = b;
    }
    public boolean getSubmit() {
        return bSubmit;
    }

    private String lastAjaxSent;
    @Override
    public String getScript() {
        lastAjaxSent = null;
        bHadToolTip = false;
        StringBuilder sb = new StringBuilder(1024);
        sb.append(_getAjaxScript(true));
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");
        
        if ( (bAjaxSubmit || HubDetailDelegate.hasDetailHubs(hub)) && OAString.isEmpty(getForwardUrl()) ) {
            sb.append("$('#"+id+"').on('change', function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
        }
        else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
            sb.append("$('#"+id+"').change(function() { $('#oacommand').val('"+id+"'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
        }
        
        if (getSubmit() || getAjaxSubmit() || HubDetailDelegate.hasDetailHubs(hub)) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        if (isRequired()) {
            sb.append("$('#"+id+"').addClass('oaRequired');\n");
        }
        sb.append("$('#"+id+"').blur(function() {$(this).removeClass('oaError');}); \n");

        if (rows > 0) sb.append("$('#"+id+"').attr('size', '"+getRows()+"');\n");
        if (hubSelect != null) sb.append("$('#"+id+"').attr('multiple', 'multiple');\n");
        else sb.append("$('#"+id+"').removeAttr('multiple');\n");
        
//qqqqqqqqqqqqqqqqq 
sb.append("if ($().selectpicker) {\n");
        
        sb.append("$('#"+id+"').selectpicker({\n");
        sb.append("  style: 'btn-default',\n");
        String s = OAString.defaultString(getNullDescription(), "select one");
        s = OAJspUtil.createJsString(s, '\'');
        
        sb.append("  noneSelectedText:'"+s+"',\n");
        
        if (getAllowSearch()) {
            sb.append("  liveSearch: true,\n");
        }
        sb.append("  dropupAuto: false,\n");
        
        sb.append("  size: "+(rows > 0?rows:8)+"\n");
        sb.append("});\n");
        
sb.append("}\n");
        
        String js = sb.toString();
        return js;
    }

    public void setAllowSearch(boolean b) {
        this.bAllowSearch = b;
    }
    public boolean getAllowSearch() {
        return this.bAllowSearch ;
    }
    
    @Override
    public String getVerifyScript() {
        if (!isRequired()) return null;
        
        // see: OAForm.getInitScript for using "requires[]" and "errors[]"
        return ("if ($('#"+id+"').val() == 'oanull') { requires.push('"+(name!=null?name:id)+"'); $('#"+id+"').addClass('oaError');}");
        // was: return ("if ($('#"+id+"').val() == '') { oaShowError('"+(name!=null?name:id)+" is required'); $('#"+id+"').addClass('oaError');return false;}");
    }
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
    private Object lastActiveObject;
    

    /**
     * Hook that is called before getting the select options.
     */
    protected void updateOptions() {
    }
    
    @Override
    public String getAjaxScript() {
        return _getAjaxScript(false);
    }
    protected String _getAjaxScript(final boolean bIsInit) {
        StringBuilder sb = new StringBuilder(1024);

        if (hub != null) lastActiveObject = hub.getAO();  //qqqq todo: could be recursive, and a child node was selected (not in hub)
        
        //todo: qqqqq could be link on pos, link on property
        // todo: create script to only send change of selection
        
        String ids = id;
        Hub hubLink = hub==null ? null : hub.getLinkHub(true);
        if (hubLink != null) {
            Object objLink = hubLink.getAO();
            if (objLink != null) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)objLink);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    ids += "_" + objs[0];
                }
                else {
                    ids += "_guid."+key.getGuid();
                }
            }
        }

        // id + "_" + linkToObjectKey
        sb.append("$('#"+id+"').attr('name', '"+ids+"');\n");
        
        String options = "";
        
        updateOptions();
        
        if (recursiveLinkInfo != null) options = getOptions(topHub, 0);
        else options = getOptions(hub, 0);

        String value = nullDescription;
        if (value == null) {
            if (options.length() == 0) value = "";
        }
        if (value != null) {
            if (options.length() == 0) {
//todo: qqqqqqqq let css max-width, text-overflow: ellipsis;                
//                for (int i=value.length(); i<columns; i++) value += " ";
//                value = Util.convert(value, " ", "&nbsp;");
            }
            boolean b;
            if (hubSelect != null) b = hubSelect.getSize() == 0;
            else b = hub.getAO() == null;
            
            String sel = b ? "selected='selected'" : "";
            options += "<option value='oanull' "+sel+">"+value+"</option>";
        }        
        sb.append("$('#"+id+"').empty();\n");
        
        options = OAJspUtil.createJsString(options, '\"');
        sb.append("$('#"+id+"').append(\"" + options + "\");\n");
        
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");
        if (getVisible()) sb.append("$('#"+id+"').show();\n");
        else sb.append("$('#"+id+"').hide();\n");
        
        if (bFocus) {
            sb.append("$('#"+id+"').focus();\n");
            bFocus = false;
        }

        // tooltip
        String prefix = null;
        String tt = getProcessedToolTip();
        if (OAString.isNotEmpty(tt)) {
            if (!bHadToolTip) {
                bHadToolTip = true;
                prefix = "$('#"+id+"').tooltip();\n";
            }
            tt = OAJspUtil.createJsString(tt, '\'');
            
            sb.append("$('#"+id+"').data('bs.tooltip').options.title = '"+tt+"';\n");
            sb.append("$('#"+id+"').data('bs.tooltip').options.placement = 'top';\n");
        }
        else {
            if (bHadToolTip) {
                sb.append("$('#"+id+"').tooltip('destroy');\n");
                bHadToolTip = false;
            }
        }

        
        if (!bIsInit) {
            
//qqqqqqqqqqqqqqqqq 
sb.append("if ($().selectpicker) {\n");
            sb.append("$('#"+id+"').selectpicker('refresh');\n");
sb.append("}\n");
        }

        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }
        
        return js;
    }

    /** 
     * this is called to render each option.
     * @param option is the string formatted value of object 
    */
    protected String getOption(int pos, Object object, String option) {
        return option;
    }
    
    protected String format;
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
    
    protected String getOptions(Hub hubx, int indent) {
        if (hubx == null) {
            return "";
        }
        boolean bHasOptGroup = false;
        String ppHeading = getHeadingPropertyPath();
        String lastHeading = null;
        String options = "";
        for (int i=0; ;i++) {
            Object obj = hubx.getAt(i);
            if (obj == null) break;

            String value = null;
            if (obj instanceof OAObject) {
                value = ((OAObject) obj).getPropertyAsString(propertyPath, getFormat());
            }
            else {
                value = OAConv.toString(obj, getFormat());
            }
            if (value == null) value = "";
            
            value = getOption(i, obj, value);

            if (columns > 0) {
                //qqqqqqqq let css max-width, text-overflow: ellipsis;                
                //  value = OAString.lineBreak(value, columns, " ", 1);
            }
            
            //value = com.viaoa.html.Util.toEscapeString(value);
            if (i == 0) {
                //qqqqqqqq let css max-width, text-overflow: ellipsis;                
                //int addSp = (columns <= 0) ? 0 : (columns - value.length());
                //for (int j=0; j<addSp; j++) value += " ";
                //value = Util.convert(value, " ", "&nbsp;");
            }

            String v = null; 
            if (obj instanceof OAObject) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject) obj);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    v = "" + objs[0];
                }
                else {
                    v = "pos."+i;
                }
            }
            else {
                v = "pos."+i;
            }
            
            boolean b;
            if (hubSelect != null) {
                b = hubSelect.contains(obj);
            }
            else if (recursiveLinkInfo != null) {
                b = (hub.getAO() == obj);
            }
            else {
                b = (hub.getAO() == obj || hub.getPos() == i);
            }

            
            String sel = (b) ? " selected='selected'" : "";

            if (indent > 0) {
                /*qqqqqqq                
                String s = ("&nbsp;&nbsp;&nbsp;");
                for (int j=0; j<indent-1; j++) s += ("&nbsp;&nbsp;&nbsp;");
                s += ("--&nbsp;");
                value = s + value;
                 */                
            }
            
            
            if (hmHeading != null) {
                String heading = hmHeading.get(i);
                if (heading != null) {
                    if (bHasOptGroup) options += "</optgroup>";
                    heading = OAJspUtil.createEmbeddedHtmlString(heading, '\'');
                    options += "<optgroup label='"+heading+"'>";
                    bHasOptGroup = true;
                }
            }
            
            String heading = null;
            if (OAString.isNotEmpty(ppHeading) && obj instanceof OAObject) {
                heading = ((OAObject) obj).getPropertyAsString(ppHeading, getFormat());
                heading = OAJspUtil.createEmbeddedHtmlString(heading, '\'');
                if (!OAString.isEqual(heading, lastHeading)) {
                    if (lastHeading != null) options += "</optgroup>";
                    options += "<optgroup label='"+heading+"'>";
                }
                lastHeading = heading;
            }
            
            options += "<option value='"+v+"'"+sel;

            String temp = getOptionDisplay(obj, i, value);
            if (OAString.isNotEmpty(temp) && !OAString.equals(value, temp)) {
                temp = OAJspUtil.createEmbeddedHtmlString(temp,'\'');
                options += " data-content='"+temp+"'";
            }
            options += ">"+value+"</option>";
            
            
            if (recursiveLinkInfo != null) {
                Hub h = (Hub) recursiveLinkInfo.getValue(obj);
                if (h != null) {
                    options += getOptions(h, indent+1); 
                }
            }
        }
        if (bHasOptGroup) options += "</optgroup>";
        if (lastHeading != null) options += "</optgroup>";
        return options;
    }
    
//qqqqqqqqqqqqq bootstrap-select has an option for this qqqqqqqqqqq    
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }


    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        if (!bEnabled) return false;
        if (hub == null) return true;

        if (!hub.isValid()) return false;

        if (OAString.isEmpty(enablePropertyPath)) return true;
        
        Hub h = hub.getLinkHub(true);
        if (h == null) h = hub;
        
        OAObject obj = (OAObject) h.getAO();
        if (obj == null) return false;
        
        Object value = obj.getProperty(enablePropertyPath);
        boolean b;
        if (value instanceof Hub) {
            b = ((Hub) value).size() > 0;
        }
        else b = OAConv.toBoolean(value);
        return b;
    }
    public String getEnablePropertyPath() {
        return enablePropertyPath;
    }
    /**
     * @param enablePropertyPath name of property in the linkTo hub
     */
    public void setEnablePropertyPath(String enablePropertyPath) {
        this.enablePropertyPath = enablePropertyPath;
    }


    @Override
    public void setVisible(boolean b) {
        lastAjaxSent = null;  
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return true;

        if (OAString.isEmpty(visiblePropertyPath)) return true;
        
        Hub h = hub.getLinkHub(true);
        if (h == null) h = hub;
        
        OAObject obj = (OAObject) h.getAO();
        if (obj == null) return false;

        Object value = obj.getProperty(visiblePropertyPath);
        boolean b;
        if (value instanceof Hub) {
            b = ((Hub) value).size() > 0;
        }
        else b = OAConv.toBoolean(value);
        return b;
    }
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    /**
     * @param visiblePropertyPath name of property in the linkTo hub
     */
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }

    
    
    public boolean setRecursive(boolean b) {
        this.topHub = null;
        this.recursiveLinkInfo = null;
        if (b) {
            recursiveLinkInfo = OAObjectInfoDelegate.getRecursiveLinkInfo(OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass()), OALinkInfo.MANY);
            if (recursiveLinkInfo == null) {
                return false;
            }
            this.topHub = hub.getRootHub();
            if (topHub == null) {
                this.recursiveLinkInfo = null;
                return false;
            }
        }
        return true;
    }
    public boolean getRecursive() {
        return (recursiveLinkInfo != null);
    }
    
    public void setFocus(boolean b) {
        this.bFocus = b;
    }

    @Override
    public String getTableEditorHtml() {
        String s = "<select id='"+id+"' style='position:absolute; top:0px; left:1px; width:97%;'></select>";
        return s;
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
        
        al.add(OAJspDelegate.JS_bootstrap);
        al.add(OAJspDelegate.JS_bootstrap_select);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.CSS_bootstrap);
        al.add(OAJspDelegate.CSS_bootstrap_select);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String getRenderHtml(OAObject obj) {
        return null;
    }
    @Override
    public String getEditorHtml(OAObject obj) {
        return null;
    }
    @Override
    public void _beforeOnSubmit() {
    }

    /**
     * set the heading to use, beginning at a specific row.
     */
    public void addHeading(int row, String heading) {
        if (hmHeading == null) hmHeading = new HashMap<>();
        hmHeading.put(row, heading);
    }
    public String getHeadingPropertyPath() {
        return headingPropertyPath;
    }
    public void setHeadingPropertyPath(String propertyPath) {
        this.headingPropertyPath = propertyPath;
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
            protected String getValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props, boolean bUseFormat) {
                String s = super.getValue(obj, propertyName, width, fmt, props, bUseFormat);
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
    
    /*
     * Callback from {@link #getTemplate(Object, int, int, int)}
     */
    public String getTemplateValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props, String defaultValue) {
        return defaultValue;
    }
    
    /**
     * This will use the OATemplate to create "data-content" value (optional).  
     */
    public String getOptionDisplay(Object objx, int pos, String value) {
        if (!(objx instanceof OAObject)) return null;
        OAObject obj = (OAObject) objx;
        
        if (getTemplate() == null) return value;
        
        template.setProperty("OAPOS", ""+pos);
        template.setProperty("OACOL", ""+(1));
        template.setProperty("OAROW", ""+(pos+1));
        
        String s = template.process(obj);
        
        return s;
    }

    /**
     * used to create a multiselect
     */
    public void setSelectHub(Hub<?> hubSelect) {
        this.hubSelect = hubSelect;
        if (rows < 1) rows = 8;
    }
    public Hub getSelectHub() {
        return hubSelect;
    }
}
