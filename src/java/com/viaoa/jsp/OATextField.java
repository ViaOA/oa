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

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.ds.OADataSource;
import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

/**
 * Controls an html input type=text, 
 * bind to OA hub, using property path
 * set size, maxwidth
 * show/hide, that can be bound to property
 * enabled, that can be bound to property
 * ajax submit on change
 * handle required validation
 * input mask
 * support for calendar popup
 * 
 * @author vvia
 *
 */
public class OATextField implements OAJspComponent, OATableEditor {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OATextField.class.getName());
    protected Hub<?> hub;
    protected String id;
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected String enablePropertyPath;
    protected int width, maxWidth;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit, bSubmit;
    protected String inputMask;
    protected boolean required;
    protected String value;
    protected String lastValue;
    protected boolean bIsDate;
    protected String regex;
    private boolean bFocus;
    protected String forwardUrl;
    protected boolean bAutoComplete;
    protected char conversion;  // 'U'pper, 'L'ower, 'T'itle, 'P'assword

    /** javascript regex */
    
    // http://daringfireball.net/2010/07/improved_regex_for_matching_urls
    // original    
    // (?i)\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))    
    public final static String RegexMatch_URL = "(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))"; 

    
    // http://ntt.cc/2008/05/10/over-10-useful-javascript-regular-expression-functions-to-improve-your-web-applications-efficiency.html
    public final static String RegexMatch_Digits = "^\\s*\\d+\\s*$";
    public final static String RegexMatch_Integer = "^\\s*(\\+|-)?\\d+\\s*$";
    public final static String RegexMatch_Decimal = "^\\s*(\\+|-)?((\\d+(\\.\\d+)?)|(\\.\\d+))\\s*$";
    public final static String RegexMatch_Currency = "^\\s*(\\+|-)?((\\d+(\\.\\d\\d)?)|(\\.\\d\\d))\\s*$";
    // public final static String RegexMatch_Email = "^\\s*[\\w\\-\\+_]+(\\.[\\w\\-\\+_]+)*\\@[\\w\\-\\+_]+\\.[\\w\\-\\+_]+(\\.[\\w\\-\\+_]+)*\\s*$";
    
    // http://www.zparacha.com/validate-email-address-using-javascript-regular-expression/
    public final static String RegexMatch_Email = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
    
    // http://stackoverflow.com/questions/123559/a-comprehensive-regex-for-phone-number-validation
    public final static String RegexMatch_USPhoneNumber = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$"; 
    
    public final static String RegexMatch_DateMMDDYYYY = "^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$"; 
    public final static String RegexMatch_DateMMDDYY = "^\\d{1,2}\\/\\d{1,2}\\/\\d{2}$"; 
    public final static String RegexMatch_Time12hr = "^(0?[1-9]|1[012]):[0-5][0-9]$"; 
    public final static String RegexMatch_Time24hr = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$"; 

    
    
    // see jquery maskedinput js lib
    public final static String MaskInput_Phone = "(999) 999-9999";
    public final static String MaskInput_DateMMDDYYYY = "99/99/9999";
    public final static String MaskInput_DateMMDDYY = "99/99/99";
    public final static String MaskInput_TimeHMS = "99:99:99";
    public final static String MaskInput_TimeHM = "99:99";

    
    
    public OATextField(String id, Hub hub, String propertyPath) {
        this(id, hub, propertyPath, 0, 0);
    }
    public OATextField(String id, Hub hub, String propertyPath, int width, int maxWidth) {
        this.id = id;
        this.hub = hub;
        this.width = width;
        this.maxWidth = maxWidth;
        setPropertyPath(propertyPath);
    }
    
    public OATextField(String id) {
        this.id = id;
    }
    
    
    @Override
    public boolean isChanged() {
        if (value == lastValue) return false;
        if (value == null || lastValue == null) return true;
        return value.equals(lastValue);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        value = lastValue;
        lastAjaxSent = null;
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
    public boolean _beforeSubmit() {
        return true;
    }

    private boolean bWasSubmitted;
    
    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {

        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        bWasSubmitted  = (id != null && id.equals(s));
        
        String name = null;
        OAObject obj = null;
        String[] values = null;
        String value = null;

        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = ex.getKey();
            if (!name.toUpperCase().startsWith(id.toUpperCase())) continue;

            values = ex.getValue();
            if (values == null || values.length == 0 || OAString.isEmpty(values[0])) {
                value = null;        
            }
            else value = values[0];
            
            if (name.equalsIgnoreCase(id)) {
                if (hub != null) { 
                    obj = (OAObject) hub.getAO();
                }
                break;
            }
            else {
                if (name.toUpperCase().startsWith(id.toUpperCase()+"_")) {
                    s = name.substring(id.length()+1);
                    if (s.startsWith("guid.")) {
                        s = s.substring(5);
                        OAObjectKey k = new OAObjectKey(null, OAConv.toInt(s), true);
                        obj = OAObjectCacheDelegate.get(hub.getObjectClass(), k);
                    }
                    else {
                        obj = OAObjectCacheDelegate.get(hub.getObjectClass(), s);
                    }
                    if (obj == null) {
                        LOG.warning("Object not found in cache, request param name="+name+", hub="+hub);
                    }
                    break;
                }
            }
        }

        value = convertInputText(value);
        
        int max = getMaxWidth();
        if (max > 0 && value != null && value.length() > max) {
            value = value.substring(0, max);
        }
        
        if (hub != null) {
            if (obj != null) {
                if (value != null && value.length() == 0) {
                    value = null;
                }
                try {
                    String old = obj.getPropertyAsString(propertyPath);
                    obj.setProperty(propertyPath, value);
                    
                    if (!OAString.isEqual(old, value)) {
                        lastAjaxSent = null;
                    }
                }
                catch (Throwable ex) {
                    s = getName();
                    if (OAString.isEmpty(s)) s = getId();
                    getForm().addError("Error setting "+s+" - "+ex);
                }
            }
        }
        else {
            setValue(value);
        }
        
        return bWasSubmitted;
    }

    public String convertInputText(String text) {
        char conv = getConversion();
        if (text != null && conv != 0) {
            String hold = text;
            if (conv == 'U' || conv == 'u') {
                text = text.toUpperCase();
            }
            else if (conv == 'L' || conv == 'l') {
                text = text.toLowerCase();
            }
            else if (conv == 'T' || conv == 't') {
                if (text.toLowerCase().equals(text) || text.toUpperCase().equals(text)) {
                    text = OAString.toTitleCase(text);
                }
            }
            else if (conv == 'P' || conv == 'p') {
                text = OAString.getSHAHash(text);
            }
        }
        return text;
    }
    
    
    /**
     * 'U'pper, 'L'ower, 'T'itle, 'P'assword
     */
    public void setConversion(char conv) {
        conversion = conv;
    }
    public char getConversion() {
        return conversion;
    }
    
    
    @Override
    public String _afterSubmit(String forwardUrl) {
        if (bWasSubmitted) {
            String furl = getForwardUrl();
            if (furl != null) forwardUrl = furl;
            return onSubmit(forwardUrl); 
        }
        return forwardUrl;
    }

    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
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
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        StringBuilder sb = new StringBuilder(1024);
        sb.append(getAjaxScript());
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");
        
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
        /*was
        if ( (bAjaxSubmit || HubDetailDelegate.hasDetailHubs(hub)) && OAString.isEmpty(getForwardUrl()) ) {
            sb.append("$('#"+id+"').change(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
        }
        else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
            sb.append("$('#"+id+"').change(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
        }
        */
        if (bAjaxSubmit && OAString.isEmpty(getForwardUrl()) ) {
            if (!getAutoComplete()) {
                sb.append("$('#"+id+"').blur(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
        }
        else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
            sb.append("$('#"+id+"').blur(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
        }

        if (isRequired()) {
            sb.append("$('#"+id+"').addClass('oaRequired');\n");
        }
        sb.append("$('#"+id+"').blur(function() {$(this).removeClass('oaError');}); \n");

        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        if (bAutoComplete) {
            sb.append("var cache"+id+" = {}, lastXhr"+id+";\n");
            sb.append("$( '#"+id+"' ).autocomplete({\n");
            sb.append("minLength: 3,\n");
            sb.append("source: function( request, response ) {\n");
            sb.append("    var term = request.term;\n");
            sb.append("    if ( term in cache"+id+" ) {\n");
            sb.append("        response( cache"+id+"[ term ] );\n");
            sb.append("        return;\n");
            sb.append("    }\n");
            sb.append("    \n");
            sb.append("    lastXhr"+id+" = $.getJSON( 'oaautocomplete.jsp?oaform="+form.getId()+"&id="+getId()+"', request, function( data, status, xhr ) {\n");
            sb.append("        cache"+id+"[ term ] = data;\n");
            sb.append("        if ( xhr === lastXhr"+id+" ) {\n");
            sb.append("            response( data );\n");
            sb.append("        }\n");
            sb.append("    });\n");
            sb.append("}\n");

            if (getAjaxSubmit()) {
                sb.append(",\n");
                sb.append("select: function( event, ui ) {\n");
                sb.append("    $('#"+getId()+"').val(ui.item.value);\n");
                sb.append("    $('#oacommand').val('"+getId()+"');\n");
                sb.append("    ajaxSubmit();\n");
                sb.append("}\n");
            }
            sb.append("});\n");
        }
        
        int max = getMaxWidth();
        if (max > 0) {
            sb.append("$('#"+getId()+"').keyup(function(event) {\n");
            sb.append("    var text = $(this).val();\n");
            sb.append("    if (text.length > "+max+") {\n");
            sb.append("        $(this).val(text.slice(0, "+max+"));\n");
            sb.append("    }\n");
            sb.append("});\n");
        }
        
        // sb.append("$('#"+id+"').on('blur', ajaxSubmit);\n");
        String js = sb.toString();
        return js;
    }

    
    @Override
    public String getVerifyScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        // see: OAForm.getInitScript for using "requires[]" and "errors[]"

        if (isRequired()) {
            sb.append("if ($('#"+id+"').val() == '') { requires.push('"+(name!=null?name:id)+"'); $('#"+id+"').addClass('oaError');}");
        }

        int max = getMaxWidth();
        if (max > 0) {
            sb.append("if ($('#"+id+"').val().length > "+max+") { errors.push('length greater then "+max+" characters for "+(name!=null?name:id)+"'); $('#"+id+"').addClass('oaError');}");
        }        
   
        String s = getRegexMatch();
        if (!OAString.isEmpty(s)) {
            sb.append("regex = new RegExp(/"+s+"/); val = $('#"+id+"').val(); if (!val.match(regex)) { errors.push('invalid "+(name!=null?name:id)+"'); $('#"+id+"').addClass('oaError');}");
        }
        if (sb.length() == 0) return null;
        return sb.toString();
    }
    
    private String name;
    /** used when displaying error message for this textfield */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private String lastAjaxSent;
    
    
    
    @Override
    public String getAjaxScript() {
        String js = getTextJavaScript();

        if (bFocus) {
            js += ("$('#"+id+"').focus();\n");
            bFocus = false;
        }

        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        return js;
    }

    protected String getTextJavaScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        String ids = id;
        String value = null;
        
        if (hub != null && !OAString.isEmpty(propertyPath)) {
            OAObject obj = (OAObject) hub.getAO();
            if (obj != null) {
                OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    ids += "_" + objs[0];
                }
                else {
                    ids += "_guid."+key.getGuid();
                }
            }
            if (obj != null) value = obj.getPropertyAsString(propertyPath);
        }
        else {
            value = getValue();
        }
        if (value == null) value = "";
        sb.append("$('#"+id+"').attr('name', '"+ids+"');\n");
        
        value = convertValue(value);
        
        lastValue = value;
        
        sb.append("$('#"+id+"').val('"+value+"');\n");
        if (width > 0) sb.append("$('#"+id+"').attr('size', '"+width+"');\n");
        if (maxWidth > 0) sb.append("$('#"+id+"').attr('maxlength', '"+maxWidth+"');\n");
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");
        if (bVisible) sb.append("$('#"+id+"').show();");
        else sb.append("$('#"+id+"').hide();");

        if (isDate()) {
            // see: http://docs.jquery.com/UI/Datepicker/formatDate
            sb.append("$('#"+id+"').datepicker({ dateFormat: 'mm/dd/yy' });");
        }
        if (!OAString.isEmpty(inputMask)) {
            sb.append("$('#"+id+"').mask('"+inputMask+"');");
        }

        String js = sb.toString();
        return js;
    }
    
    protected String convertValue(String value) {
        value = Util.convert(value, "\r\n", " ");
        value = Util.convert(value, "\n", " ");
        value = Util.convert(value, "\r", " ");
        value = Util.convert(value, "'", "\\'");
        return value;
    }
    
    public boolean isDate() {
        return bIsDate;
    }
    public void setDate(boolean b) {
        this.bIsDate = b;
    }
    
    

    /**
      Set regex 
          example for email:  "\S+@\S+\.\S+"
     */
    public void setRegexMatch(String regex) {
        this.regex = regex;
    }
    public String getRegexMatch() {
        return this.regex;
    }
    

    
    /**
     * ex:  '(999) 999-9999'
     * 
     * last char optional:  ("(99) 999-99-9?9")
     * 
     * see: http://digitalbush.com/projects/masked-input-plugin/
     *  
     * @return
     */
    public String getInputMask() {
        return inputMask;
    }
    public void setInputMask(String inputMask) {
        this.inputMask = inputMask;
    }

    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        if (!bEnabled) return false;
        if (hub == null) return bEnabled;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        if (OAString.isEmpty(enablePropertyPath)) return bEnabled;

        Object value = obj.getPropertyAsString(enablePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }

    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return bVisible;
        
        if (OAString.isEmpty(visiblePropertyPath)) return bVisible;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        Object value = obj.getPropertyAsString(visiblePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }

    public void setValue(String value) {
        if (value != this.value || value == null || value.length()==0 || !value.equals(this.value)) {
            lastAjaxSent = null;
        }
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
    

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
        boolean b = isDate();
        if (hub != null && !OAString.isEmpty(propertyPath)) {
            for (OAPropertyInfo pi : hub.getOAObjectInfo().getPropertyInfos()) {
                if (!propertyPath.equalsIgnoreCase(pi.getName())) continue;
                if (pi.getClassType().equals(OADate.class) || pi.getClassType().equals(OADateTime.class)) {
                    b = true;
                }
                break;
            }
        }        
        setDate(b);
    }
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
    public String getEnablePropertyPath() {
        return enablePropertyPath;
    }
    public void setEnablePropertyPath(String enablePropertyPath) {
        this.enablePropertyPath = enablePropertyPath;
    }

    private int dataSourceMax = -2;
    public int getDataSourceMaxWidth() {
        if (dataSourceMax == -2) {
            if (hub != null) {
                dataSourceMax = -1;
                OADataSource ds = OADataSource.getDataSource(hub.getObjectClass());
                if (ds != null) {
                    dataSourceMax = ds.getMaxLength(hub.getObjectClass(), getPropertyPath());
                    Method method = OAReflect.getMethod(hub.getObjectClass(), "get"+propertyPath, 0);
                    if (method != null) {
                        if (method.getReturnType().equals(String.class)) {
                            if (dataSourceMax > 254) dataSourceMax = -1;
                        }
                        else dataSourceMax = -1;
                    }
                }
            }
        }
        return dataSourceMax;
    }
    public int getMaxWidth() {
        getDataSourceMaxWidth();
        if (maxWidth <= 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        if (dataSourceMax > 0 && maxWidth > dataSourceMax) return dataSourceMax; 
        return maxWidth;
    }
    /** max length of text.  If -1 (default) then unlimited.  
    */
    public void setMaxWidth(int x) {
        maxWidth = x;
        maxWidth = getMaxWidth();  // verify with Datasource
    }

    public void setFocus(boolean b) {
        this.bFocus = b;
    }

    /**
     * True if autoCompelte should be enabled. 
     */
    public void setAutoComplete(boolean b) {
        this.bAutoComplete = b;
    }
    public boolean getAutoComplete() {
        return bAutoComplete;
    }
    /**
     * Called by browser if autoComplete is true.
     * @param value user input 
     * @return list of values to send back to browser.
     */
    public String[] getAutoCompleteText(String value) {
        return null;
    }
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
    /*
    
scrolling with heading not moving    
   http://www.farinspace.com/jquery-scrollable-table-plugin/    
    
resize:     
http://www.audenaerde.org/simpleresizabletables.js

    
resize heading:     
   http://quocity.com/colresizable/#samples    
    
resize heading (small) :::    
    http://jsfiddle.net/ydTCZ/
    
*/    
    @Override
    public String getTableEditorHtml() {
        // let cell take up all space
        width = 0;  // so that the "size" attribute wont be set        
        String s = "<input id='"+id+"' type='text' style='position:absolute; top:0px; left:1px; width:97%;'>";
        return s;
    }
    
}
