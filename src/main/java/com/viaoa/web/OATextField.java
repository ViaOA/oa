/* Copyright 1999-2017 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.web;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.web.swing.ComponentInterface;

/**
 * Controls an html input type=text, bind to OA hub, using property path set size, maxwidth show/hide,
 * that can be bound to property enabled, that can be bound to property ajax submit on change handle
 * required validation input mask support for calendar popup
 *
 * For datetime, date, time formats - use OADateTime formats.
 *
 * @author vvia
 */
public class OATextField implements OAJspComponent, OATableEditor, OAJspRequirementsInterface, ComponentInterface {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OATextField.class.getName());
    protected Hub hub;
    protected String id;
    protected String propertyPath;
    protected boolean bPropertyPathIsManyLink;
    protected boolean bPropertyPathIsOneLink;
    protected String visiblePropertyPath;
    protected String enablePropertyPath;
    protected int width, minLength, maxLength;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit, bSubmit;
    protected String inputMask;
    protected boolean required;
    protected String value;
    protected String lastValue;
    protected boolean bIsDate, bIsTime, bIsDateTime;
    protected String regex;
    private boolean bFocus;
    protected String forwardUrl;
    protected boolean bAutoComplete;  // using jquery
    protected char conversion; // 'U'pper, 'L'ower, 'T'itle, 'P'assword
    protected boolean bMultiValue;
    protected OATypeAhead typeAhead;
    private String name;
    private boolean bClearButton;

    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;
    protected String placeholder;
    protected String floatLabel;
    
    
    /** javascript regex */

    // http://daringfireball.net/2010/07/improved_regex_for_matching_urls
    // original
    // (?i)\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?������]))
    public final static String RegexMatch_URL = "(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?������]))";

    // http://ntt.cc/2008/05/10/over-10-useful-javascript-regular-expression-functions-to-improve-your-web-applications-efficiency.html
    public final static String RegexMatch_Digits = "^\\s*\\d+\\s*$";
    public final static String RegexMatch_Integer = "^\\s*(\\+|-)?\\d+\\s*$";
    public final static String RegexMatch_Decimal = "^\\s*(\\+|-)?((\\d+(\\.\\d+)?)|(\\.\\d+))\\s*$";
    public final static String RegexMatch_Currency = "^\\s*(\\+|-)?((\\d+(\\.\\d\\d)?)|(\\.\\d\\d))\\s*$";
    
    public final static String RegexMatch_SingleDigit = "^([0-9])$";
    public final static String RegexMatch_DoubleDigit = "^([1-9][0-9])$";

    // http://www.zparacha.com/validate-email-address-using-javascript-regular-expression/
    public final static String RegexMatch_Email = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
    //was: public final static String RegexMatch_Email = "^\\s*[\\w\\-\\+_]+(\\.[\\w\\-\\+_]+)*\\@[\\w\\-\\+_]+\\.[\\w\\-\\+_]+(\\.[\\w\\-\\+_]+)*\\s*$";

    public final static String RegexMatch_CreditCard = "^\\s*\\d+\\s*$";  // qqq currently only checks if digits
    
    
    // http://stackoverflow.com/questions/123559/a-comprehensive-regex-for-phone-number-validation
    public final static String RegexMatch_USPhoneNumber = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$";

    public final static String RegexMatch_DateMMDDYYYY = "^\\d{1,2}\\/\\d{1,2}\\/\\d{4}$";
    public final static String RegexMatch_DateMMDDYY = "^\\d{1,2}\\/\\d{1,2}\\/\\d{2}$";
    public final static String RegexMatch_Time12hr = "^(0?[1-9]|1[012]):[0-5][0-9]$";
    public final static String RegexMatch_Time24hr = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

    // see jquery maskedinput js lib
    public final static String MaskInput_USPhoneNumber = "(999) 999-9999";
    public final static String MaskInput_DateMMDDYYYY = "99/99/9999";
    public final static String MaskInput_DateMMDDYY = "99/99/99";
    public final static String MaskInput_TimeHMS = "99:99:99";
    public final static String MaskInput_TimeHM = "99:99";
    public final static String MaskInput_Integer = "9?999999";
    public final static String MaskInput_Decimal = "9?dddddd";
    public final static String MaskInput_SingleDigit = "9";
    public final static String MaskInput_DoubleDigit = "99";


    // AutoNumeric jquery plugin
    // see: http://www.decorplanit.com/plugin/
    // NOTE: use the older version that supports jquery.  The new version is huge and is not a jq plugin
    //    https://plugins.jquery.com/autoNumeric/ 
    private static class AutoNum {
        String min;
        String max;
        char symbol;
        boolean symbolPrefix;
        int decimalPlaces;
    }
    protected AutoNum autoNum;
    

    /**
     * Used to use the autoNumeric plugin 
     * @param min value
     * @param max value
     * @param symbol example: $
     * @param symbolPrefix true if symber is prefix, else it will be used as suffix
     * @param decimalPlaces number of decimals places for input and display
     */
    public void setNumeric(String min, String max, char symbol, boolean symbolPrefix, int decimalPlaces) {
        autoNum = new AutoNum();
        autoNum.min = min;
        if (max == null) max = "9999999999999";
        autoNum.max = max;
        autoNum.symbol = symbol;
        autoNum.symbolPrefix = symbolPrefix;
        autoNum.decimalPlaces = decimalPlaces;
    }
    
    public void setDecimal(int deci) {
        this.autoNum = new AutoNum();
        setNumeric(null, null, (char) 0, true, deci);
    }
    public void setCurrency() {
        this.autoNum = new AutoNum();
        setNumeric(null, null, (char) '$', true, 2);
    }
    public void setInteger() {
        this.autoNum = new AutoNum();
        setNumeric(null, null, (char) 0, true, 0);
    }
    public void setPositiveDecimal(int deci) {
        this.autoNum = new AutoNum();
        setNumeric("0", null, (char) 0, true, deci);
    }
    public void setPositiveCurrency() {
        this.autoNum = new AutoNum();
        setNumeric("0", null, (char) '$', true, 2);
    }
    public void setPositiveInteger() {
        this.autoNum = new AutoNum();
        setNumeric("0", null, (char) 0, true, 0);
    }


    public OATextField(String id, Hub hub, String propertyPath) {
        this(id, hub, propertyPath, 0, 0);
    }

    public OATextField(String id, Hub hub, String propertyPath, int width, int maxLength) {
        this.id = id;
        this.hub = hub;
        this.width = width;
        this.maxLength = maxLength;
        setPropertyPath(propertyPath);
    }

    public OATextField(String id) {
        this.id = id;
    }

    public OATextField(Hub hub, String prop, int cols) {
        // TODO Auto-generated constructor stub
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
    public boolean _beforeFormSubmitted() {
        return true;
    }

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {

        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted = (id != null && id.equals(s));

        String name = null;
        OAObject obj = null;
        String[] values = null;
        String value = null;

        if (hmNameValue != null) {
            for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
                name = ex.getKey();
                if (!name.toUpperCase().startsWith(id.toUpperCase())) continue;

                values = ex.getValue();
                if (values == null || values.length == 0) {
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
                    if (name.toUpperCase().startsWith(id.toUpperCase() + "_")) {
                        s = name.substring(id.length() + 1);
                        if (s.startsWith("guid.")) {
                            s = s.substring(5);
                            OAObjectKey k = new OAObjectKey(null, OAConv.toInt(s), true);
                            obj = OAObjectCacheDelegate.get(hub.getObjectClass(), k);
                        }
                        else {
                            obj = OAObjectCacheDelegate.get(hub.getObjectClass(), s);
                        }
                        if (obj == null) {
                            LOG.warning("Object not found in cache, request param name=" + name + ", hub=" + hub);
                        }
                        break;
                    }
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
                try {
                    String fmt = getFormat();
                    if ( (bIsDateTime || bIsTime) && !OAString.isEmpty(value)) {
                        boolean b = true;
                        if (!OAString.isEmpty(fmt)) {
                            s = fmt.toUpperCase();
                            if (s.indexOf("X") >= 0 || s.indexOf("Z") >= 0) { // includes timezone in value
                                b = false;
                            }
                        }
                        if (b) {
                            OAForm f = getForm();
                            if (f != null) {
                                OASession sess = f.getSession();
                                if (sess != null) {
                                    if (bIsDateTime) {
                                        OADateTime dt = OADateTime.valueOf(value, fmt);
                                        TimeZone tz = sess.getBrowserTimeZone();
                                        if (tz != null) dt.setTimeZone(tz);
                                        OADateTime d2 = new OADateTime(dt.getTime()); // use this computer's timezone
                                        value = d2.toString(fmt);
                                    }
                                    else {
                                        OATime dt = (OATime) OATime.valueOf(value, fmt);
                                        TimeZone tz = sess.getBrowserTimeZone();
                                        if (tz != null) dt.setTimeZone(tz);
                                        OATime d2 = new OATime(dt.getTime()); // use this computer/server timezone
                                        value = d2.toString(fmt);
                                    }
                                }
                            }
                        }
                    }

                    if (!OAString.isEqual(value, lastValue)) {
                        if (value != null && (value.length() == 0 && lastValue == null)) {
                        }
                        else {
                            if (getTypeAhead()!=null && bPropertyPathIsManyLink) {
                                
                                Object objx = ((OAObject)hub.getAO()).getProperty(propertyPath);
                                if (objx instanceof Hub) {
                                    Hub hub = (Hub) objx;
                                
                                    // tagsinput js will put Ids in comma separated string in value
                                    final String[] ss = value.split(",");
                                    for (int i=0; ss!=null && i<ss.length; i++) {
                                        String sx = ss[i];
                                        sx = sx.trim();
                                        if (!OAString.isInteger(sx)) continue;
                                        Class c = hub.getObjectClass();
                                        if (c == null) continue;
                                        objx = OAObjectCacheDelegate.get(c, OAConv.toInt(sx));
                                        if (!hub.contains(objx)) hub.add(objx);
                                    }
                                    for (int i=0; ;i++) {
                                        obj = (OAObject) hub.getAt(i);
                                        if (obj == null) break;
                                        
                                        OAObjectKey key = obj.getObjectKey();
                                        Object[] ids = key.getObjectIds();
                                        String id;
                                        if (ids == null || ids.length == 0) id = obj.getGuid()+"";
                                        else id = ids[0]+"";
                                        
                                        boolean bFound = false;
                                        for (int j=0; !bFound && ss!=null && j<ss.length; j++) {
                                            String sx = ss[j];
                                            if (id.equals(sx)) bFound = true;
                                        }
                                        if (!bFound) {
                                            hub.remove(obj);
                                            i--;
                                        }
                                    }
                                }
                            }
                            else if (getTypeAhead() != null && bPropertyPathIsOneLink) {
                                // tagsinput js will put Id in value
                                Object val = null;
                                if (OAString.isInteger(value)) {
                                    OALinkInfo li = hub.getOAObjectInfo().getLinkInfo(propertyPath);
                                    Class c = li.getToClass();
                                    if (c != null) {
                                        val = OAObjectCacheDelegate.get(c, OAConv.toInt(value));
                                    }
                                }
                                obj.setProperty(propertyPath, val);
                            }
                            else {
                                obj.setProperty(propertyPath, value, fmt);
                            }
                            lastAjaxSent = null;
                        }
                    }
                }
                catch (Throwable ex) {
                    s = getName();
                    if (OAString.isEmpty(s)) s = getId();
                    getForm().addErrorMessage("Error setting " + s + " - " + ex);
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
    public String _afterFormSubmitted(String forwardUrl) {
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
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
        bHadToolTip = false;
        bFloatLabelJsInit = false;
        StringBuilder sb = new StringBuilder(1024);

        // 20170628 moved to below
        // sb.append(getAjaxScript());
        
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");

        final int max = getMaxWidth();
        
        
        if (getClearButton()) {
            sb.append("$('#"+getId()+"').addClass('oaTextFieldWithClear');\n");
            sb.append("$('#"+getId()+"').wrap('<div id=\""+getId()+"Wrap\" class=\"oaTextFieldWrap\">');\n");
            sb.append("$('#"+getId()+"').after('<span id=\""+getId()+"Clear\" class=\"glyphicon glyphicon-remove oaTextFieldClear\"></span>');\n");

            sb.append("$('#"+getId()+"').keyup(function() {\n");
            sb.append("    var text = $(this).val();\n");
            sb.append("    $('#"+getId()+"Clear').css('visibility', ((text.length > 0)?'visible':'hidden'));\n");
            if (max > 0) {
                sb.append("    if (text.length > " + max + ") {\n");
                sb.append("        $(this).val(text.slice(0, " + max + "));\n");
                sb.append("    }\n");
            }
            sb.append("});\n");

            sb.append("$('#"+getId()+"Clear').mousedown(function() {\n");

            if (bAjaxSubmit) {
                sb.append("    $('#"+getId()+"').ignore=true;\n");
                sb.append("    $('#"+getId()+"').val('');\n");
                sb.append("    $('#oacommand').val('" + getId() + "');\n");
                sb.append("    ajaxSubmit();\n");
                sb.append("    $('#"+getId()+"').ignore=false;\n");
            }
            else if (getSubmit()) {
                sb.append("    $('#"+getId()+"').ignore=true;\n");
                sb.append("    $('#"+getId()+"').val('');\n");
                sb.append("    $('#oacommand').val('" + getId() + "');\n");
                sb.append("    $('form').submit();\n");
                sb.append("    $('#"+getId()+"').ignore=false;\n");
            }
            
            sb.append("    if ($('#"+getId()+"').val().length == 0) return false;\n");
            sb.append("    $('#"+getId()+"').val('');\n");
            // sb.append("    $('#"+getId()+"').blur();\n");
            sb.append("    $('#"+getId()+"Clear').css('visibility', 'hidden');\n");
            sb.append("    $('#"+getId()+"').focus();\n");
            sb.append("    return false;\n");
            sb.append("});\n");
        }
        else if (max > 0) {
            sb.append("$('#" + getId() + "').keyup(function(event) {\n");
            sb.append("    var text = $(this).val();\n");
            sb.append("    if (text.length > " + max + ") {\n");
            sb.append("        $(this).val(text.slice(0, " + max + "));\n");
            sb.append("    }\n");
            sb.append("});\n");
        }
        
        
        if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
            if (!getAutoComplete() && (getTypeAhead()==null)) {
                if (!isDateTime() && !isDate() && !isTime()) { // date/time will use close (see below)
                    sb.append("$('#" + id + "').blur(function(e) {if($(this).ignore){$(this).ignore=false;return;}$('#oacommand').val('" + id + "'); ajaxSubmit();return false;});\n");
                    sb.append("$('#" + id + "').keypress(function(e) { if (e.keyCode != 13) return; e.preventDefault(); $('#oacommand').val('" + id + "'); $(this).ignore=true;ajaxSubmit();$(this).ignore=false;return false;});\n");
                }
            }
        }
        else if (getSubmit() || OAString.notEmpty(getForwardUrl())) {
            if (!isDateTime() && !isDate() && !isTime()) {
                sb.append("$('#" + id + "').blur(function() { $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
            }
        }

        if (isRequired()) {
            sb.append("$('#" + id + "').addClass('oaRequired');\n");
            sb.append("$('#" + id + "').attr('required', true);\n");
        }
        sb.append("$('#" + id + "').blur(function() {$(this).removeClass('oaError');}); \n");

        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#" + id + "').addClass('oaSubmit');\n");
        }

        if (getAutoComplete()) {
            // support for jqueryui autocomplete
            sb.append("var cache" + id + " = {}, lastXhr" + id + ";\n");
            sb.append("$( '#" + id + "' ).autocomplete({\n");
            sb.append("minLength: 3,\n");
            sb.append("source: function( request, response ) {\n");
            sb.append("    var term = request.term;\n");
            sb.append("    if ( term in cache" + id + " ) {\n");
            sb.append("        response( cache" + id + "[ term ] );\n");
            sb.append("        return;\n");
            sb.append("    }\n");
            sb.append("    \n");
            sb.append("    lastXhr" + id + " = $.getJSON( 'oaautocomplete.jsp?oaform=" + form.getId() + "&id=" + getId()
                    + "', request, function( data, status, xhr ) {\n");
            sb.append("        cache" + id + "[ term ] = data;\n");
            sb.append("        if ( xhr === lastXhr" + id + " ) {\n");
            sb.append("            response( data );\n");
            sb.append("        }\n");
            sb.append("    });\n");
            sb.append("}\n");

            if (getAjaxSubmit()) {
                sb.append(",\n");
                sb.append("select: function( event, ui ) {\n");
                sb.append("    $('#" + getId() + "').val(ui.item.value);\n");
                sb.append("    $('#oacommand').val('" + getId() + "');\n");
                sb.append("    ajaxSubmit();\n");
                sb.append("}\n");
            }
            sb.append("});\n");
        }
        else if (getMultiValue() && getTypeAhead()==null && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // free form multiple values
            sb.append("$('#" + id + "').tagsinput();\n");
        }
        else if (getTypeAhead() != null && !getMultiValue() && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // typeAhead one
            sb.append("var " + id + "Bloodhound = new Bloodhound({\n");
            sb.append("  datumTokenizer : Bloodhound.tokenizers.obj.whitespace('display'),\n");
            sb.append("  queryTokenizer : Bloodhound.tokenizers.whitespace,\n");

            sb.append("  remote : {\n");
            sb.append("    url : 'oatypeahead.jsp?oaform="+getForm().getId()+"&id=" + id + "&term=%QUERY',\n");
            sb.append("    wildcard: '%QUERY',\n");
            sb.append("    cache: false\n");  // 20171030
            sb.append("  }\n");
            
            sb.append("});\n");
            sb.append("" + id + "Bloodhound.initialize();\n");

            int minLen = getTypeAhead().getMinimumInputLength();
            if (minLen < 0) minLen = 3;
            
            sb.append("$('#" + id + "').typeahead({\n");
            sb.append("    hint: "+(getTypeAhead().getShowHint()?"true":"false")+",\n");
            sb.append("    highlight: true,\n");
            sb.append("    minLength: "+minLen+"\n");
            sb.append("}, {\n");
            sb.append("    source: " + id + "Bloodhound,\n");
            sb.append("    name: '" + id + "Popup',\n");
            sb.append("    limit: 400,\n"); // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("    display: 'display',\n");
            sb.append("    templates: {\n");
            sb.append("      suggestion: function(data) {return '<p>'+data.dropdowndisplay+'</p>';}\n");
            sb.append("    }\n");
  
            if (bAjaxSubmit) {
                sb.append("  }).on('typeahead:select', function (obj, datum) {\n");  // https://github.com/twitter/typeahead.js/blob/master/doc/jquery_typeahead.md#custom-events
                sb.append("    $('#" + getId() + "').val(datum.display);\n");
                sb.append("    $('#oacommand').val('" + getId() + "');\n");
                sb.append("    ajaxSubmit();\n");
            }
            sb.append("  });\n");
        }
        else if (getTypeAhead() != null && getMultiValue() && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // select multiple from ta list and store displayed value in one property
            sb.append("var " + id + "Bloodhound = new Bloodhound({\n");
            sb.append("  datumTokenizer : Bloodhound.tokenizers.obj.whitespace('display'),\n");
            sb.append("  queryTokenizer : Bloodhound.tokenizers.whitespace,\n");
            
            sb.append("  remote : {\n");
            sb.append("    url : 'oatypeahead.jsp?oaform="+getForm().getId()+"&id=" + id + "&term=%QUERY',\n");
            sb.append("    wildcard: '%QUERY'\n");
            sb.append("  }\n");
            
            sb.append("});\n");
            sb.append("" + id + "Bloodhound.initialize();\n");
            
            int minLen = getTypeAhead().getMinimumInputLength();
            if (minLen < 0) minLen = 3;
            
            sb.append("$('#" + id + "').tagsinput({\n");
            // 20170913       
            sb.append("  itemValue: 'id',\n");  // <-- store id value
            //was: sb.append("  itemValue: 'display',\n");  // <-- store display value
            sb.append("  itemText: 'display',\n");
            sb.append("  freeInput: false,\n");
            sb.append("  typeaheadjs: [\n");
            sb.append("    {\n");
            sb.append("      minLength: "+minLen+",\n");
            sb.append("      hint: "+(getTypeAhead().getShowHint()?"true":"false")+",\n");
            sb.append("      highlight: true\n");
            sb.append("    },\n");
            sb.append("    {\n");
            sb.append("      name: '" + id + "Popup',\n"); 
            sb.append("      limit: 400,\n");  // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("      display: 'display',\n");
            sb.append("      templates: {\n");
            sb.append("        suggestion: function(data) {return '<p>'+data.dropdowndisplay+'</p>';}\n");
            sb.append("      },\n");
            sb.append("      source: " + id + "Bloodhound.ttAdapter()\n");
            sb.append("    }\n");
            sb.append("  ]\n");
            sb.append("});\n");
        }
        else if (getTypeAhead() != null && (bPropertyPathIsOneLink || bPropertyPathIsManyLink)) {
            // use tagInput and ta, selected Ids will pass comma sep values when submitted
            sb.append("var " + id + "Bloodhound = new Bloodhound({\n");
            sb.append("  datumTokenizer : Bloodhound.tokenizers.obj.whitespace('display'),\n");
            sb.append("  queryTokenizer : Bloodhound.tokenizers.whitespace,\n");
            
            sb.append("  remote : {\n");
            sb.append("    url : 'oatypeahead.jsp?oaform="+getForm().getId()+"&id=" + id + "&term=%QUERY',\n");
            sb.append("    wildcard: '%QUERY'\n");
            sb.append("  }\n");
            
            sb.append("});\n");
            sb.append("" + id + "Bloodhound.initialize();\n");
            
            int minLen = getTypeAhead().getMinimumInputLength();
            if (minLen < 0) minLen = 3;
            
            // https://bootstrap-tagsinput.github.io/bootstrap-tagsinput/examples/
            sb.append("$('#" + id + "').tagsinput({\n");
            
            sb.append("  itemValue: 'id',\n");     // <-- store Id value
            sb.append("  itemText: 'display',\n");
            if (bPropertyPathIsOneLink) {  
                sb.append("  maxTags: 1,\n");
            }
            sb.append("  freeInput: false,\n");
            sb.append("  typeaheadjs: [\n");
            sb.append("    {\n");
            sb.append("      minLength: "+minLen+",\n");
            sb.append("      hint: "+(getTypeAhead().getShowHint()?"true":"false")+",\n");
            sb.append("      highlight: true\n");
            sb.append("    },\n");
            sb.append("    {\n");
            sb.append("      name: '" + id + "Popup',\n"); 
            sb.append("      limit: 400,\n");  // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("      display: 'display',\n");
            sb.append("      templates: {\n");
            sb.append("        suggestion: function(data) {return '<p>'+data.dropdowndisplay+'</p>';}\n");
            sb.append("      },\n");
            sb.append("      source: " + id + "Bloodhound.ttAdapter()\n");
            sb.append("    }\n");
            sb.append("  ]\n");
            sb.append("});\n");
        }
        
        
        // 20170628 moved from begin of method
        sb.append(_getAjaxScript(true));
        

        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        StringBuilder sb = new StringBuilder(1024);

        // see: OAForm.getInitScript for using "requires[]" and "errors[]"

        if (isRequired()) {
            String s = name;
            if (OAString.isEmpty(s)) {
                s = placeholder;
                if (OAString.isEmpty(s)) s = id;
            }
            sb.append("if ($('#" + id + "').val() == '') { requires.push('" + s + "'); $('#" + id
                    + "').addClass('oaError');}\n");
        }

        int max = getMaxWidth();
        if (max > 0) {
            sb.append("if ($('#" + id + "').val().length > " + max + ") { errors.push('length greater then " + max + " characters for "
                    + (name != null ? name : id) + "'); $('#" + id + "').addClass('oaError');}\n");
        }

        String s = getRegexMatch();
        if (!OAString.isEmpty(s)) {
            sb.append("regex = new RegExp(/" + s + "/); val = $('#" + id + "').val(); if (!val.match(regex)) { errors.push('invalid "
                    + (name != null ? name : id) + "'); $('#" + id + "').addClass('oaError');}\n");
        }

        // 20170327
        if (isDateTime()) {
            sb.append("if ($('#" + id + "').val().length > 0) $('#" + id + "_ts').val(Date.parse($('#" + id + "').val()));\n");
        }

        if (sb.length() == 0) return null;
        return sb.toString();
    }


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
        return _getAjaxScript(false);
    }
    protected String _getAjaxScript(final boolean bIsInitializing) {
        StringBuilder sb = new StringBuilder(1024);
        String s = getTextJavaScript(bIsInitializing);
        if (s != null) sb.append(s);

        if (bIsInitializing && getClearButton()) {
            sb.append("$('#"+getId()+"Clear').css('visibility', (($('#"+getId()+"').val().length > 0)?'visible':'hidden'));\n");
        }

        s = getPlaceholder();
        if (bIsInitializing && s != null) {
            s = OAJspUtil.createJsString(s, '\'');        
            sb.append("$('#" + id + "').attr('placeholder', '"+s+"');\n");
        }

        if (bIsInitializing) getFloatLabelJs(sb);
        
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
        
        if (bFocus) {
            sb.append("$('#" + id + "').focus();\n");
            bFocus = false;
        }

        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }
        
        return js;
    }

    protected String getTextJavaScript(final boolean bIsInitializing) {
        StringBuilder sb = new StringBuilder(1024);

        String newName = id;
        String value = null;

        if (hub != null && !OAString.isEmpty(propertyPath)) {
            if (getTypeAhead() != null && bPropertyPathIsManyLink) {
                // https://bootstrap-tagsinput.github.io/bootstrap-tagsinput/examples/
                sb.append("$('#" + id + "').tagsinput('removeAll');\n");
                
                OAObject obj = (OAObject) hub.getActiveObject();
                if (obj != null) {
                    Hub h = (Hub) ((OAObject)obj).getProperty(propertyPath);
 
                    // value is a comma separated list of Ids, js code will be sent also
                    for (Object objx : h) {
                        if (!(objx instanceof OAObject)) continue;
                        obj = (OAObject) objx;
                        
                        OAObjectKey key = obj.getObjectKey();
                        Object[] idxs = key.getObjectIds();
                        String idx;
                        if (idxs == null || idxs.length == 0) idx = obj.getGuid()+"";
                        else idx = idxs[0]+"";
                         
                        String s2 = getTypeAhead().getDisplayValue(obj);
                        s2 = OAJspUtil.createJsString(s2, '\"');
                        sb.append("$('#" + this.id + "').tagsinput('add', { \"id\": "+idx+" , \"display\": \""+s2+"\"});\n");
                    }
                }
            }
            else if (getTypeAhead() != null && bPropertyPathIsOneLink) {
                // https://bootstrap-tagsinput.github.io/bootstrap-tagsinput/examples/
                sb.append("$('#" + id + "').tagsinput('removeAll');\n");

                OAObject obj = (OAObject) hub.getActiveObject();
                if (obj != null) {
                    Object objx = ((OAObject)obj).getProperty(propertyPath);
                    if (objx != null) {
                        obj = (OAObject) objx;
                        
                        OAObjectKey key = obj.getObjectKey();
                        Object[] idxs = key.getObjectIds();
                        String idx;
                        if (idxs == null || idxs.length == 0) idx = obj.getGuid()+"";
                        else idx = idxs[0]+"";
                         
                        String s2 = getTypeAhead().getDisplayValue(obj);
                        s2 = OAJspUtil.createJsString(s2, '\"');
                        sb.append("$('#" + this.id + "').tagsinput('add', { \"id\": "+idx+" , \"display\": \""+s2+"\"});\n");
                    }
                }
            }
            else {
                OAObject obj = (OAObject) hub.getAO();
                if (obj != null && nameIncludesObjectId) {
                    OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
                    Object[] objs = key.getObjectIds();
                    if (objs != null && objs.length > 0 && objs[0] != null) {
                        newName += "_" + objs[0];
                    }
                    else {
                        newName += "_guid." + key.getGuid();
                    }
                }
                if (obj != null) {
                    if (isDateTime() || isDate() || isTime()) {
                        String fmt = getFormat();
                        boolean b = true;
    
                        // 20170925 added isTime
                        if (bIsDateTime || bIsTime) {
                            b = false;
                            if (!OAString.isEmpty(fmt)) {
                                String s = fmt.toUpperCase();
                                if (s.indexOf("X") >= 0 || s.indexOf("Z") >= 0) { // includes timezone in value
                                    b = true;
                                }
                            }
                            if (!b) {
                                b = true;
                                OADateTime dt = (OADateTime) obj.getProperty(propertyPath);
                                if (dt != null) {
                                    OAForm f = getForm();
                                    if (f != null) {
                                        OASession sess = f.getSession();
                                        if (sess != null) {
                                            TimeZone tz = sess.getBrowserTimeZone();
                                            dt = dt.convertTo(tz);
                                            if (isTime()) dt = new OATime(dt);
                                            value = dt.toString(fmt);
                                            b = false;
                                        }
                                    }
                                }
                            }
                        }
                        if (b) {
                            value = obj.getPropertyAsString(propertyPath, fmt);
                        }
                    }
                    else {
                        value = obj.getPropertyAsString(propertyPath);
                    }
                }
            }
        }
        else {
            value = getValue();
        }
        if (value == null) value = "";
        sb.append("$('#" + id + "').attr('name', '" + newName + "');\n");

        lastValue = value;

        // set existing value            
        if (getTypeAhead()!=null && (bPropertyPathIsManyLink || bPropertyPathIsOneLink)) {
            // already done
        }
        else if (getTypeAhead()!=null && getMultiValue()) {
            // uses tagsInput+typeAhead+bloodhound and uses array objects <id,value>
            // values are separated by comma and need to be added separately
            sb.append("$('#" + id + "').tagsinput('removeAll');\n");
            for (int i=1;;i++) {
                String sx = OAString.field(value, ",", i);
                if (sx == null) break;
                sx = sx.trim();
                if (sx.length() == 0) continue;
                sx = OAJspUtil.createJsString(sx, '\"');
                // the object.id is not known for the value, will instead use display as the id
                //   this is the same as getTypeAheadJson(..) return value
                sb.append("$('#" + id + "').tagsinput('add', { \"id\": \""+sx+"\" , \"display\": \""+sx+"\"});\n");
            }
        }
        else if (getMultiValue() && !bPropertyPathIsManyLink && !bPropertyPathIsOneLink) {
            // values are separated by comma and need to be added
            sb.append("$('#" + id + "').tagsinput('removeAll');\n");
            for (int i=1;;i++) {
                String sx = OAString.field(value, ",", i);
                if (sx == null) break;
                sx = sx.trim();
                if (sx.length() == 0) continue;
                sx = OAJspUtil.createJsString(sx, '\'');
                sb.append("$('#" + id + "').tagsinput('add', '"+sx+"');\n");
            }
        }
        else {
            value = OAJspUtil.createJsString(value, '\'');
            if (autoNum != null) {
                if (autoNum.decimalPlaces > 0) {
                    double d = OAConv.toDouble(value);
                    value = OAConv.toString(d);
                }
                else {
                    long x = OAConv.toLong(value);
                    value = x+"";
                }
            }
            if (autoNum != null && !bIsInitializing) {
                sb.append("$('#" + id + "').autoNumeric('set', '"+value+"');\n");
                //was: sb.append("$('#" + id + "').val($('#" + id + "').autoNumeric('set', '"+value+"'));\n");
            }
            else {
                sb.append("$('#" + id + "').val('" + value + "');\n");
            }
        }
        
        if (getMaxLength() > 0) sb.append("$('#" + id + "').attr('maxlength', '" + getMaxLength() + "');\n");
        if (width > 0) sb.append("$('#" + id + "').attr('size', '" + width + "');\n");

        
        String s = getEnabledScript(getEnabled());
        if (s != null) sb.append(s);
        
        if (!getMultiValue() && !bPropertyPathIsManyLink && !bPropertyPathIsOneLink) {
            s = getVisibleScript(getVisible());
            if (s != null) sb.append(s);
        }

        String fmt = getFormat();

        if (isDateTime() || isDate() || isTime()) {
            if (OAString.isEmpty(fmt)) {
                if (isDateTime()) {
                    fmt = OADateTime.getGlobalOutputFormat();
                }
                else if (isDate()) {
                    fmt = OADate.getGlobalOutputFormat();
                }
                else fmt = OATime.getGlobalOutputFormat();
            }

            
            // [BEGIN] Jquery date/time formats
            // see: http://api.jqueryui.com/datepicker/#utility-formatDate
            // http://docs.jquery.com/UI/Datepicker/formatDate
            // http://trentrichardson.com/examples/timepicker/
            // https://github.com/trentrichardson/jQuery-Timepicker-Addon
            String dfmtJquery = null;
            String tfmtJquery = null;

            int pos = fmt.indexOf('M');
            if (pos < 0) {
                pos = fmt.indexOf('y');
            }

            if (pos >= 0) {
                pos = fmt.indexOf('H');
                if (pos < 0) {
                    pos = fmt.indexOf('h');
                }
                if (pos >= 0) {
                    dfmtJquery = fmt.substring(0, pos).trim();
                }
                else dfmtJquery = fmt;
                if (dfmtJquery.indexOf("MMM") >= 0) {
                    dfmtJquery = OAString.convert(dfmtJquery, "MMMM", "MM");
                    dfmtJquery = OAString.convert(dfmtJquery, "MMM", "M");
                }
                else dfmtJquery = OAString.convert(dfmtJquery, "M", "m");
                dfmtJquery = OAString.convert(dfmtJquery, "yy", "y");
                dfmtJquery = OAString.convert(dfmtJquery, "E", "D");
            }

            pos = fmt.indexOf('H');
            if (pos < 0) {
                pos = fmt.indexOf('h');
            }
            if (pos >= 0) {
                tfmtJquery = fmt.substring(pos).trim();
                tfmtJquery = OAString.convert(tfmtJquery, "aa", "TT");
                tfmtJquery = OAString.convert(tfmtJquery, "a", "TT");
            }

            if (!isDateTime()) {
                if (!isDate()) dfmtJquery = null;
                if (!isTime()) tfmtJquery = null;
            }

            
            // [BEGIN] Bootstrap date/time formats
            // see: http://momentjs.com/docs/#/displaying/format/            
            String dfmtBS = null;
            String tfmtBS = null;

            pos = fmt.indexOf('M');
            if (pos < 0) {
                pos = fmt.indexOf('y');
            }

            if (pos >= 0) {
                pos = fmt.indexOf('H');
                if (pos < 0) {
                    pos = fmt.indexOf('h');
                }
                if (pos >= 0) {
                    dfmtBS = fmt.substring(0, pos).trim();
                }
                else dfmtBS = fmt;
                dfmtBS = OAString.convert(dfmtBS, "y", "Y");
                dfmtBS = OAString.convert(dfmtBS, "d", "D");
                dfmtBS = OAString.convert(dfmtBS, "E", "d");  // day of week
            }

            pos = fmt.indexOf('H');
            if (pos < 0) {
                pos = fmt.indexOf('h');
            }
            if (pos >= 0) {
                tfmtBS = fmt.substring(pos).trim();
            }

            if (!isDateTime()) {
                if (!isDate()) dfmtBS = null;
                if (!isTime()) tfmtBS = null;
            }
            
            
            if (!OAString.isEmpty(dfmtJquery) && !OAString.isEmpty(tfmtJquery)) {
                // supports jquery.datetimepicker and bootstrap datetimepicker (customized version: had to change name to bsdatetimepicker)
                // see:  https://eonasdan.github.io/bootstrap-datetimepicker/
                if (bIsInitializing) {
                    sb.append("if ($().bsdatetimepicker) {\n");
                    sb.append("  $('#" + id + "').bsdatetimepicker({");
                    sb.append("format: '" + OAJspUtil.createJsString((dfmtBS + " " + tfmtBS), '\'') + "'");
                    sb.append(", sideBySide: true, showTodayButton: true, showClear: true, showClose: true});\n");
    
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                            sb.append("  $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                        sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
                    }
                    sb.append("}\n");  // end bootstrap
                    sb.append("else {\n");
                    sb.append("$('#" + id + "').datetimepicker({ ");
                    sb.append("dateFormat: '" + OAJspUtil.createJsString(dfmtJquery,'\'') + "'");
                    sb.append(", timeFormat: '" + OAJspUtil.createJsString(tfmtJquery,'\'') + "'");
                    if (tfmtJquery != null && tfmtJquery.toLowerCase().indexOf('z') >= 0) {
                        // sb.append(", timezoneList: [{label: 'EDT', value: '-240'}, {label: 'other', value: '-480'}]");
                    }
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;}\n");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;}\n");
                    }
                    sb.append(" });\n");
                    sb.append("}\n");  // end jquery
                }
            }
            else if (!OAString.isEmpty(dfmtJquery)) {
                if (bIsInitializing) {
                    sb.append("if ($().bsdatetimepicker) {\n");
                    sb.append("$('#" + id + "').bsdatetimepicker({");
                    sb.append("format: '" + OAJspUtil.createJsString(dfmtBS, '\'') + "'");
                    sb.append(", showTodayButton: true, showClear: true, showClose: true});\n");
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append("$('#" + id + "').on('dp.change', function (e) {");
                            sb.append("$('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                        sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
                    }
                    sb.append("}\n");  // end bootstrap
                    sb.append("else {\n");
                    sb.append("$('#" + id + "').datepicker({ dateFormat: '" + dfmtJquery + "'");
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;}\n");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;}\n");
                    }
                    sb.append("});\n");
                    sb.append("}\n");  // end jquery
                }
            }
            else if (!OAString.isEmpty(tfmtJquery)) {
                if (bIsInitializing) {
                    sb.append("if ($().bsdatetimepicker) {\n");
                    sb.append("  $('#" + id + "').bsdatetimepicker({ ");
                    sb.append("format: '" + OAJspUtil.createJsString(tfmtBS, '\'') + "'");
                    sb.append(", showClear: true, showClose: true});\n");
    
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                            sb.append("  $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                        sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
                    }
                    sb.append("}\n");  // end bootstrap
                    sb.append("else {\n");
                    sb.append("  $('#" + id + "').timepicker({ timeFormat: '" + OAJspUtil.createJsString(tfmtJquery,'\'') + "'");
                    if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                        if (!getAutoComplete() && (getTypeAhead()!=null)) {
                            sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;}");
                        }
                    }
                    else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                        sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); $('#oacommand').val(''); return false;}");
                    }
                    sb.append("});\n");
                    sb.append("}");  // end jquery
                }
            }

            if (isDateTime() && !getAutoComplete() && (getTypeAhead()!=null) && getForm() != null) {
                if (bIsInitializing) {
                    sb.append("$('#" + getForm().getId() + "').prepend(\"<input type='hidden' id='" + id + "_ts' name='" + id
                            + ".ts' value=''>\");\n");
                }
            }
        }
        else if (bIsInitializing && autoNum != null) {
            s = "";
            if (autoNum.symbol > 0) {
                s = OAString.concat(s, "aSign: '"+autoNum.symbol+"'", ", ");
                if (!autoNum.symbolPrefix) s = OAString.concat(s, "pSign: 's'", ", "); 
            }
        
            if (OAString.isNotEmpty(autoNum.min) || OAString.isNotEmpty(autoNum.max)) {  
                if (OAString.isNotEmpty(autoNum.min)) s = OAString.concat(s, "vMin: '"+autoNum.min+"'", ", ");
                if (OAString.isNotEmpty(autoNum.max)) s = OAString.concat(s, "vMax: '"+autoNum.max+"'", ", ");
            }
            //if (autoNum.decimalPlaces > 0)  {
                s = OAString.concat(s, "mDec: '"+autoNum.decimalPlaces+"'", ", ");
                // s = OAString.concat(s, "aPad: 'true'", ", "); //default
            //}
            sb.append("$('#" + id + "').autoNumeric('init',{"+s+"});\n");
        }
        else if (bIsInitializing && !OAString.isEmpty(inputMask)) {
            if (inputMask.indexOf('d') >= 0) sb.append("$.mask.definitions['d'] = '[0-9.]';\n");
            sb.append("$('#" + id + "').mask('" + inputMask + "');\n");
        }

        String js = sb.toString();
        return js;
    }

    protected String format;

    public String getFormat() {
        return format;
    }

    public void setFormat(String fmt) {
        this.format = fmt;
    }

    public boolean isDate() {
        return bIsDate;
    }

    public void setDate(boolean b) {
        this.bIsDate = b;
    }

    public boolean isDateTime() {
        return bIsDateTime;
    }

    public void setDateTime(boolean b) {
        this.bIsDateTime = b;
    }

    public boolean isTime() {
        return bIsTime;
    }

    public void setTime(boolean b) {
        this.bIsTime = b;
    }

    /**
     * Set regex example for email: "\S+@\S+\.\S+"
     */
    public void setRegexMatch(String regex) {
        this.regex = regex;
    }

    public String getRegexMatch() {
        return this.regex;
    }

    /**
     * ex: '(999) 999-9999'
     *
     * last char optional: ("(99) 999-99-9?9")
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
    public boolean getRequired() {
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
    public void setEnablePropertyPath(String enablePropertyPath) {
        this.enablePropertyPath = enablePropertyPath;
    }

    
    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return true;

        if (OAString.isEmpty(visiblePropertyPath)) return bVisible;

        OAObject obj = (OAObject) hub.getAO();
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
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }

    public void setValue(String value) {
        if (value != this.value || value == null || value.length() == 0 || !value.equals(this.value)) {
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
        boolean bDate = isDate();
        boolean bDateTime = isDateTime();
        boolean bTime = isTime();

        if (hub != null && !OAString.isEmpty(propertyPath)) {
            for (OAPropertyInfo pi : hub.getOAObjectInfo().getPropertyInfos()) {
                if (!propertyPath.equalsIgnoreCase(pi.getName())) continue;
                if (pi.getClassType().equals(OADateTime.class)) {
                    bDateTime = true;
                }
                else if (pi.getClassType().equals(OADate.class)) {
                    bDate = true;
                }
                else if (pi.getClassType().equals(OATime.class)) {
                    bTime = true;
                }
                break;
            }
            
            OAObjectInfo oi = hub.getOAObjectInfo();
            OALinkInfo li = oi.getLinkInfo(propertyPath);
            if (li != null) {
                if (li.getType() == li.TYPE_MANY) {
                    bPropertyPathIsManyLink = true;
                }
                else {
                    bPropertyPathIsOneLink = true;
                }
            }
            
        }
        setDateTime(bDateTime);
        setTime(bTime);
        setDate(bDate);
    }


    private int dataSourceMax = -2;

    public int getDataSourceMaxWidth() {
        if (dataSourceMax == -2) {
            if (hub != null) {
                dataSourceMax = -1;
                OADataSource ds = OADataSource.getDataSource(hub.getObjectClass());
                if (ds != null) {
                    dataSourceMax = ds.getMaxLength(hub.getObjectClass(), getPropertyPath());
                    Method method = OAReflect.getMethod(hub.getObjectClass(), "get" + propertyPath, 0);
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
        if (maxLength <= 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        if (dataSourceMax > 0 && maxLength > dataSourceMax) return dataSourceMax;
        return maxLength;
    }

    /**
     * max length of text. If -1 (default) then unlimited.
     */
    public void setMaxWidth(int x) {
        maxLength = x;
        maxLength = getMaxWidth(); // verify with Datasource
    }

    
    public int getMaxLength() {
        return getMaxWidth();
    }
    public void setMaxLength(int x) {
        maxLength = x;
        maxLength = getMaxWidth(); // verify with Datasource
    }
    
    public int getMinLength() {
        return minLength;
    }
    public void setMinLength(int x) {
        minLength = x;
    }

    public void setFocus(boolean b) {
        this.bFocus = b;
    }
    public void setFocus() {
        this.bFocus = true;
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
    public void setTypeAhead(OATypeAhead ta) {
        this.typeAhead = ta;
    }
    public OATypeAhead getTypeAhead() {
        return typeAhead;
    }

    /**
     * Called by browser if autoComplete is true.
     * Uses oaautocomplete.jsp
     * 
     * @param value user input
     * @return list of values to send back to browser.
     */
    public String[] getAutoCompleteText(String value) {
        return null;
    }

    
    public Object getTypeAheadObject(String searchText) {
        if (typeAhead == null || searchText == null) return null;

        ArrayList<OAObject> al = typeAhead.search(searchText);
        if (al == null) return null;
        if (al.size() == 0) return null;
        
        for (OAObject obj : al) {
            String displayValue = typeAhead.getDisplayValue(obj);
            if (searchText.equals(displayValue)) return obj;
        }
        return null;
    }
    public Object getTypeAheadObject() {
        String searchText = getValue();
        Object obj = getTypeAheadObject(searchText);
        return obj;
    }
    
    /**
     * Called by browser
     * Uses oatypeahead.jsp
     * 
     * Must be json string using double quotes, and "id", "display" for values
     *    ex:  String s = "{\"id\":1,\"display\":\"m-1-1\"},{\"id\":2,\"display\":\"m-2-1\"}"; 
     * 
     * @param searchText user input
     * @return list of values to send back to browser.
     */
    public String getTypeAheadJson(String searchText) {
        String json = null;
        try {
            json = _getTypeAheadJson(searchText);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "Error getting json for typeahead search, searchText="+searchText, e);
            json = "{\"id\":0,\"display\":\""+ OAJspUtil.createJsString(e.getMessage(),'"')+"\"}";
        }
        return json;
    }
    protected String _getTypeAheadJson(String searchText) {
        if (typeAhead == null) return null;

        ArrayList al = typeAhead.search(searchText);
        if (al == null) return null;
        
        String json = "";
        // ex:  String s = "{\"id\":1,\"display\":\"m-1-1\"},{\"id\":2,\"display\":\"m-2-1\"}";
        for (Object objx : al) {
            OAObject obj = (OAObject) objx;
            if (json.length() > 0) json += ",";

            OAObjectKey key = obj.getObjectKey();
            Object[] ids = key.getObjectIds();
            String id;
            if (ids == null || ids.length == 0) id = obj.getGuid()+"";
            else id = ids[0]+"";
            

            String displayValue = typeAhead.getDisplayValue(obj);
            if (displayValue == null) displayValue = "";
            else {
                displayValue = OAJspUtil.createJsString(displayValue, '\"');
                //was: displayValue.replace('\"', ' ');
            }

            String dd = typeAhead.getDropDownDisplayValue(obj);
            if (dd == null) dd = "";
            else {
                dd = OAJspUtil.createJsString(dd, '\"');
                //was: dd.replace('\"', ' ');
            }
            
            boolean bUseId = (bPropertyPathIsManyLink || bPropertyPathIsOneLink);
            if (!bUseId) {
                if (hub == null || hub.getLinkPath(true) == null) {
                    bUseId = true;
                }
            }
            
            if (bUseId) {
                json += "{\"id\":"+id+",\"display\":\""+displayValue+"\",\"dropdowndisplay\":\""+dd+"\"}";
            }
            else {
                // need to send id=displayValue, since they will be stored in property and not the id
                //    and then used when it has to reset the txt value from this data
                json += "{\"id\":"+displayValue+",\"display\":\""+displayValue+"\",\"dropdowndisplay\":\""+dd+"\"}";
            }
        }
        return json;
    }

    
    protected String getTypeAheadDisplayValueForId(int id) {
        if (typeAhead == null) return null;
        Class c = typeAhead.getToClass();
        if (c == null) return null;
        OAObject obj = OAObjectCacheDelegate.get(c, id);
        if (obj == null) return "id "+id+" not found";
        String s = typeAhead.getDisplayValue(obj);
        return s;
    }
    
    
    //qqq
    /* scrolling with heading not moving http://www.farinspace.com/jquery-scrollable-table-plugin/
     * 
     * resize: http://www.audenaerde.org/simpleresizabletables.js
     * 
     * 
     * resize heading: http://quocity.com/colresizable/#samples
     * 
     * resize heading (small) ::: http://jsfiddle.net/ydTCZ/ */
    @Override
    public String getTableEditorHtml() {
        // let cell take up all space
        width = 0; // so that the "size" attribute wont be set
        String s = "<input id='" + id + "' type='text' style='position:absolute; top:0px; left:1px; width:97%; max-height:97%'>";
        return s;
    }
    
    public void setMultiValue(boolean b) {
        this.bMultiValue = b;
    }
    public boolean getMultiValue() {
        return this.bMultiValue;
    }

    
    @Override
    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.JS_jquery);
        al.add(OAJspDelegate.JS_jquery_ui);

        if (getAutoComplete()) {
        }
        if (getInputMask() != null) {
            al.add(OAJspDelegate.JS_jquery_maskedinput);
        }
        
        if (getMultiValue()) {
            al.add(OAJspDelegate.JS_bootstrap);
            al.add(OAJspDelegate.JS_bootstrap_tagsinput);
        }
        else if (getTypeAhead() != null && (bPropertyPathIsOneLink || bPropertyPathIsManyLink)) {
            al.add(OAJspDelegate.JS_bootstrap);
            al.add(OAJspDelegate.JS_bootstrap_tagsinput);
        }

        if (getTypeAhead() != null) {
            al.add(OAJspDelegate.JS_bootstrap);
            al.add(OAJspDelegate.JS_bootstrap_typeahead);
        }
        
        if (isDateTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.JS_jquery_timepicker);
            }
            else {
                al.add(OAJspDelegate.JS_bootstrap);
                al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.JS_bootstrap_datetimepicker);
            }
        }
        else if (isDate()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
            }
            else {
                al.add(OAJspDelegate.JS_bootstrap);
                al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.JS_bootstrap_datetimepicker);
            }
        }
        else if (isTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.JS_jquery_timepicker);
            }
            else {
                al.add(OAJspDelegate.JS_bootstrap);
                al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.JS_bootstrap_datetimepicker);
            }
        }
        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.JS_bootstrap);
        }
        if (autoNum != null) {
            al.add(OAJspDelegate.JS_jquery_autonumeric);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();
        if (getAutoComplete()) {
            al.add(OAJspDelegate.CSS_jquery_ui);
        }
        if (getInputMask() != null) {
        }
        
        if (getMultiValue()) {
            al.add(OAJspDelegate.CSS_bootstrap);
            al.add(OAJspDelegate.CSS_bootstrap_tagsinput);
        }
        else if (getTypeAhead() != null && (bPropertyPathIsOneLink || bPropertyPathIsManyLink)) {
            al.add(OAJspDelegate.CSS_bootstrap);
            al.add(OAJspDelegate.CSS_bootstrap_tagsinput);
        }

        if (getTypeAhead() != null) {
            al.add(OAJspDelegate.CSS_bootstrap);
            al.add(OAJspDelegate.CSS_bootstrap_typeahead);
        }
        
        if (isDateTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.CSS_jquery_ui);
                al.add(OAJspDelegate.CSS_jquery_timepicker);
            }
            else {
                al.add(OAJspDelegate.CSS_bootstrap);
                al.add(OAJspDelegate.CSS_bootstrap_datetimepicker);
            }
        }
        else if (isDate()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.CSS_jquery_ui);
            }
            else {
                al.add(OAJspDelegate.CSS_bootstrap);
                al.add(OAJspDelegate.CSS_bootstrap_datetimepicker);
            }
        }
        else if (isTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.CSS_jquery_ui);
                al.add(OAJspDelegate.CSS_jquery_timepicker);
            }
            else {
                al.add(OAJspDelegate.CSS_bootstrap);
                // al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.CSS_bootstrap_datetimepicker);
            }
        }
        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.CSS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }
    
    public void setClearButton(boolean b) {
        this.bClearButton = b;
    }
    public boolean getClearButton() {
        return this.bClearButton;
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

    protected String getEnabledScript(boolean b) {
        if (b) return ("$('#" + id + "').removeAttr('disabled');\n");
        return ("$('#" + id + "').attr('disabled', 'disabled');\n");
    }
    protected String getVisibleScript(boolean b) {
        if (b) return ("$('#" + id + "').show();\n");
        return ("$('#" + id + "').hide();\n");
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
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        placeholder = null;
    }
    public String getPlaceholder() {
        return this.placeholder;
    }

    public void setFloatLabel(String floatLabel) {
        this.floatLabel = floatLabel;
        floatLabel = null;
    }
    public String getFloatLabel() {
        return this.floatLabel;
    }

    private boolean bFloatLabelJsInit;
    protected void getFloatLabelJs(StringBuilder sb) {
        String s = getFloatLabel();
        if (OAString.isEmpty(s)) {
            if (!bFloatLabelJsInit) return;
        }
        
        if (!bFloatLabelJsInit) {
            sb.append("$('#" + id + "').addClass('oaFloatLabel');\n");

            if (OAString.isEmpty(getFloatLabel())) {
                sb.append("$('#" + id + "').after('<span class='active'></span>');\n");
            }
            else {
                sb.append("$('#" + id + "').after('<span></span>');\n");
                sb.append("$('#"+id+"').on('propertychange change keyup paste input', function() { if ($('#"+id+"').val().length > 0) $('#"+id+" + span').addClass('active'); else $('#"+id+" + span').removeClass('active'); });\n");
                sb.append("if ($('#"+id+"').val().length > 0) $('#"+id+" + span').addClass('active');\n");
            }
            bFloatLabelJsInit = true;
        }
        
        s = getFloatLabel();
        sb.append("$('#"+id+" + span').html(\""+OAJspUtil.createJsString(s,'\"')+"\");\n");            
    }
    
    public String getValidationRules() {
// IMPORTANT NOTE:  validation uses attr name as identifier, not id.
//      need to set nameIncludesObjectId=false
//qqq oaweb needs to set name=id        
        if (!getRequired()) return null; 
        StringBuilder sb = new StringBuilder(80);
        sb.append(id+": {");
        int cnt = 0;
        if (getRequired()) {
            if (cnt++ > 0) sb.append(",\n"); 
            sb.append("required: true");
        }
        int x = getMaxLength();
        if (x > 0) {
            if (cnt++ > 0) sb.append(",\n"); 
            sb.append("maxlength: "+x+"");
        }
        x = getMinLength();
        if (x > 0) {
            if (cnt++ > 0) sb.append(",\n"); 
            sb.append("minlength: "+x+"");
        }
        
        String s = getRegexMatch();
        if (OAString.isNotEmpty(s)) {
            if (s.equals(RegexMatch_Email)) {
                if (cnt++ > 0) sb.append(",\n"); 
                sb.append("email: true");
            }
            else if (s.equals(RegexMatch_CreditCard)) {
                if (cnt++ > 0) sb.append(",\n"); 
                sb.append("creditcard: true");
            }
        }
        
/*qqq add more later
 
                                    $("#my-forms-wizard").validate({
                                        onsubmit: false,
                                        rules: {
                                            txt10: {
                                                required: true,
                                                minlength: 3,
                                                maxlength: 6,
                                                min: 100,
                                                max: 9999,
                                                rangelength: [3, 6],
                                                number: true,
                                                pattern: /^AR\d{4}$/,
                                                digits: true,
                                                creditcard: true,
                                                accept: "image/*",
                                                extension:"doc|docx|pdf|txt",
                                                alphanumeric: true,
                                                integer: true
                                            }
                                        },
                                        messages: {
                                            txt10: {
                                                required: "please enter your fullname",
                                                minlength: "fullname needs to be {0} chars!!",
                                                maxlength: "max len msg",
                                                min: "min msg",
                                                max: "max msg",
                                                rangelength: "range msg",
                                                number: "must be a num",
                                                pattern: "pattern msg",
                                                digits: "digits msg",
                                                creditcard: "creditcard msg",
                                                accept: "accept msg",
                                                extension: "ext msg",
                                                alphanumeric: "alphanum msg",
                                                integer: "integer msg"
                                            }
                                        },    
                                        highlight : function(element) {
                                            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
                                        },
                                        success : function(element) {
                                            $(element).closest('.form-group').removeClass('has-error');
                                            $(element).remove();
                                        },
                                        errorPlacement : function(error, element) {
                                            element.parent().append(error);
                                        }
                                    });
         
         
 */
        sb.append("}");
        return sb.toString();
    }

    public String getValidationMessages() {
        if (!getRequired()) return null; 
        StringBuilder sb = new StringBuilder(80);
        sb.append(id+": {");

        String s = name;
        if (OAString.isEmpty(s)) {
            s = placeholder;
            if (OAString.isEmpty(s)) s = id;
        }
        sb.append("required: '"+s+" is required'");
//qqqqqq  add more later            

        sb.append("}");
        return sb.toString();
    }
    
//qqqqqqqq 20171105 temp so that the attr name is same as attr id (needed by validation.js) 
    public boolean nameIncludesObjectId = true;


    public void setMinimumColumns(int cols) {
        // TODO Auto-generated method stub
        
    }

    public void setMaximumColumns(int cols) {
        // TODO Auto-generated method stub
        
    }

    public void setToolTipText(String string) {
        // TODO Auto-generated method stub
        
    }

    public void setEnabled(Hub hub, String pp, Object value) {
        // TODO Auto-generated method stub
        
    }
    
}
