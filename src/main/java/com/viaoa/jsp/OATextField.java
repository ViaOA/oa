/* Copyright 1999-2017 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.jsp;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.ds.OADataSource;
import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * Controls an html input type=text, bind to OA hub, using property path set size, maxwidth show/hide,
 * that can be bound to property enabled, that can be bound to property ajax submit on change handle
 * required validation input mask support for calendar popup
 *
 * For datetime, date, time formats - use OADateTime formats.
 *
 * @author vvia
 */
public class OATextField implements OAJspComponent, OATableEditor, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OATextField.class.getName());
    protected Hub hub;
    protected String id;
    protected String propertyPath;
    protected boolean bPropertyPathIsManyLink;
    protected boolean bPropertyPathIsOneLink;
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
    protected boolean bIsDate, bIsTime, bIsDateTime;
    protected String regex;
    private boolean bFocus;
    protected String forwardUrl;
    protected boolean bAutoComplete;  // using jquery
    protected char conversion; // 'U'pper, 'L'ower, 'T'itle, 'P'assword
    protected boolean bMultiValue;
    protected OATypeAhead typeAhead;
    protected String[] lookupValues;
    private String name;
    private boolean bClearButton;

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

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {

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

                    if (bIsDateTime && !OAString.isEmpty(value)) {
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
                                    OADateTime dt = OADateTime.valueOf(value, fmt);
                                    TimeZone tz = sess.getBrowserTimeZone();
                                    dt.setTimeZone(tz);

                                    OADateTime d2 = new OADateTime(dt.getTime()); // use this computer's timezone
                                    value = d2.toString(fmt);
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
    public String _afterSubmit(String forwardUrl) {
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

        // 20170628 moved to below
        // sb.append(getAjaxScript());
        
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");

        final int max = getMaxWidth();
        
        // 20170706 support for clear button
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
                sb.append("$('#" + id + "').blur(function() { $('#oacommand').val('" + id + "'); $('form').submit(); return false;});\n");
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
        else if (getMultiValue() && getLookupValues()==null && getTypeAhead()==null && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // free form multiple values
            sb.append("$('#" + id + "').tagsinput();\n");
        }
        else if (getLookupValues() != null && !getMultiValue() && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // pick one from typeAhead
            sb.append("var " + id + "Bloodhound = new Bloodhound({\n");
            sb.append("  datumTokenizer : Bloodhound.tokenizers.obj.whitespace('display'),\n");
            sb.append("  queryTokenizer : Bloodhound.tokenizers.whitespace,\n");
            // local: ['dog', 'pig', 'moose'],            
            sb.append("  local: [");
            int x = 0;
            for (String s : getLookupValues()) {
                if (x++ > 0) sb.append(",");
                sb.append("'"+s+"'");;
            }
            sb.append("]\n");
            sb.append("});\n");
            sb.append("" + id + "Bloodhound.initialize();\n");

            sb.append("$('#" + id + "').typeahead(null, {\n");
            sb.append("    name: '" + id + "Popup',\n");
            sb.append("    display: 'display',\n");
            sb.append("    source: " + id + "Bloodhound,\n");
            sb.append("    hint: true,\n");
            sb.append("    highlight: true,\n");
            sb.append("    limit: 400,\n"); // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("    minLength: 1\n");
            sb.append("});\n");
        }
        else if (getLookupValues() != null && getMultiValue() && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // pick many using tagInput from typeAhead
            sb.append("var " + id + "Bloodhound = new Bloodhound({\n");
            sb.append("  datumTokenizer : Bloodhound.tokenizers.obj.whitespace('display'),\n");
            sb.append("  queryTokenizer : Bloodhound.tokenizers.whitespace,\n");
            // local: ['dog', 'pig', 'moose'],            
            sb.append("  local: [");
            int x = 0;
            for (String s : getLookupValues()) {
                if (x++ > 0) sb.append(",");
                sb.append("'"+s+"'");;
            }
            sb.append("]\n");
            sb.append("});\n");
            sb.append("" + id + "Bloodhound.initialize();\n");
            
            sb.append("$('#" + id + "').tagsinput({\n");
            
            if (!getMultiValue()) { 
                sb.append("  maxTags: 1,\n");
            }
            
            sb.append("  typeaheadjs: [\n");
            sb.append("    {\n");
            sb.append("      minLength: 1,\n");
            sb.append("      hint: true,\n");
            sb.append("      highlight: true\n");
            sb.append("    },\n");
            sb.append("    {\n");
            sb.append("      name: '" + id + "Popup',\n"); 
            sb.append("      limit: 400,\n");  // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("      source: " + id + "Bloodhound.ttAdapter()\n");
            sb.append("    }\n");
            sb.append("  ]\n");
            sb.append("});\n");
        }
        else if (getTypeAhead() != null && !getMultiValue() && !bPropertyPathIsOneLink && !bPropertyPathIsManyLink) {
            // typeAhead one and only and store display in property
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
            if (minLen < 1) minLen = 3;
            
            sb.append("$('#" + id + "').typeahead(null, {\n");
            sb.append("    name: '" + id + "Popup',\n");
            sb.append("    display: 'display',\n");
            sb.append("    templates: {\n");
            sb.append("      suggestion: function(data) {return '<p>'+data.dropdowndisplay+'</p>';}\n");
            sb.append("    },\n");
            sb.append("    source: " + id + "Bloodhound,\n");
            sb.append("    hint: true,\n");
            sb.append("    highlight: true,\n");
            sb.append("    limit: 400,\n"); // see: https://github.com/twitter/typeahead.js/issues/1232
            sb.append("    minLength: "+minLen+"\n");
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
            if (minLen < 1) minLen = 3;
            
            sb.append("$('#" + id + "').tagsinput({\n");
            sb.append("  itemValue: 'display',\n");  // <-- store display value
            sb.append("  itemText: 'display',\n");
            sb.append("  typeaheadjs: [\n");
            sb.append("    {\n");
            sb.append("      minLength: "+minLen+",\n");
            sb.append("      hint: true,\n");
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
            if (minLen < 1) minLen = 3;
            
            // https://bootstrap-tagsinput.github.io/bootstrap-tagsinput/examples/
            sb.append("$('#" + id + "').tagsinput({\n");
            
            sb.append("  itemValue: 'id',\n");     // <-- store Id value
            sb.append("  itemText: 'display',\n");
            if (bPropertyPathIsOneLink) {  
                sb.append("  maxTags: 1,\n");
            }
            sb.append("  typeaheadjs: [\n");
            sb.append("    {\n");
            sb.append("      minLength: "+minLen+",\n");
            sb.append("      hint: true,\n");
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
        sb.append(getAjaxScript());
        

        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        StringBuilder sb = new StringBuilder(1024);

        // see: OAForm.getInitScript for using "requires[]" and "errors[]"

        if (isRequired()) {
            sb.append("if ($('#" + id + "').val() == '') { requires.push('" + (name != null ? name : id) + "'); $('#" + id
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
        String js = getTextJavaScript();
        if (js == null) js = "";

        if (getClearButton()) {
            js += "$('#"+getId()+"Clear').css('visibility', (($('#"+getId()+"').val().length > 0)?'visible':'hidden'));\n";
        }
        
        if (bFocus) {
            js += ("$('#" + id + "').focus();\n");
            bFocus = false;
        }

        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        return js;
    }

    protected String getTextJavaScript() {
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
                        sb.append("$('#" + this.id + "').tagsinput('add', { \"id\": "+idx+" , \"display\": \""+s2+"\"});\n");
                    }
                }
            }
            else {
                OAObject obj = (OAObject) hub.getAO();
                if (obj != null) {
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
    
                        if (bIsDateTime) {
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

        value = convertValue(value);

        if (value == null) lastValue = null;
        else lastValue = value;

        // set existing value            
        if (getTypeAhead()!=null && (bPropertyPathIsManyLink || bPropertyPathIsOneLink)) {
            // already done
        }
        else if (getLookupValues() != null && !getMultiValue()) {
            // just set value
            sb.append("$('#" + id + "').val('" + value + "');\n");
        }
        else if (getLookupValues() != null && getMultiValue()) {
            // uses tagInput+ta+bloodhound with a string[] of values (not objs)
            // values are separated by comma and need to be added separately
            sb.append("$('#" + id + "').tagsinput('removeAll');\n");
            for (int i=1;;i++) {
                String sx = OAString.field(value, ",", i);
                if (sx == null) break;
                sx = sx.trim();
                if (sx.length() == 0) continue;
                sx = OAString.convert(sx, "\"", "");
                if (sx.length() == 0) continue;
                sb.append("$('#" + id + "').tagsinput('add', '"+sx+"');\n");
            }
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
                sx = OAString.convert(sx, "\"", "");
                if (sx.length() == 0) continue;
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
                sx = OAString.convert(sx, "\"", "");
                if (sx.length() == 0) continue;
                sb.append("$('#" + id + "').tagsinput('add', '"+sx+"');\n");
            }
        }
        else {
            sb.append("$('#" + id + "').val('" + value + "');\n");
        }
        
        if (width > 0) sb.append("$('#" + id + "').attr('size', '" + width + "');\n");
        if (maxWidth > 0) sb.append("$('#" + id + "').attr('maxlength', '" + maxWidth + "');\n");
        if (getEnabled()) sb.append("$('#" + id + "').removeAttr('disabled');\n");
        else sb.append("$('#" + id + "').attr('disabled', 'disabled');\n");
        
        if (!getMultiValue() && !bPropertyPathIsManyLink && !bPropertyPathIsOneLink) {
            if (bVisible) sb.append("$('#" + id + "').show();\n");
            else sb.append("$('#" + id + "').hide();\n");
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

                sb.append("if ($().bsdatetimepicker) {\n");
                sb.append("  $('#" + id + "').bsdatetimepicker({");
                sb.append("format: '" + dfmtBS + " " + tfmtBS + "'");
                sb.append(", sideBySide: true, showTodayButton: true, showClear: true, showClose: true});\n");

                if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                    if (!getAutoComplete() && (getTypeAhead()!=null)) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                        sb.append("  $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                    }
                }
                else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                    sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                    sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); return false;});\n");
                }
                sb.append("}\n");  // end bootstrap
                sb.append("else {\n");
                sb.append("$('#" + id + "').datetimepicker({ ");
                sb.append("dateFormat: '" + dfmtJquery + "'");
                sb.append(", timeFormat: '" + tfmtJquery + "'");
                if (tfmtJquery != null && tfmtJquery.toLowerCase().indexOf('z') >= 0) {
                    // sb.append(", timezoneList: [{label: 'EDT', value: '-240'}, {label: 'other', value: '-480'}]");
                }
                if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                    if (!getAutoComplete() && (getTypeAhead()!=null)) {
                        sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;}\n");
                    }
                }
                else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                    sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); return false;}\n");
                }
                sb.append(" });\n");
                sb.append("}\n");  // end jquery
            }
            else if (!OAString.isEmpty(dfmtJquery)) {
                sb.append("if ($().bsdatetimepicker) {\n");
                sb.append("$('#" + id + "').bsdatetimepicker({");
                sb.append("format: '" + dfmtBS + "'");
                sb.append(", showTodayButton: true, showClear: true, showClose: true});\n");
                if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                    if (!getAutoComplete() && (getTypeAhead()!=null)) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {");
                        sb.append("$('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                    }
                }
                else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                    sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                    sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); return false;});\n");
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
                    sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); return false;}\n");
                }
                sb.append("});\n");
                sb.append("}\n");  // end jquery
            }
            else if (!OAString.isEmpty(tfmtJquery)) {
                sb.append("if ($().bsdatetimepicker) {\n");
                sb.append("  $('#" + id + "').bsdatetimepicker({ ");
                sb.append("format: '" + tfmtBS + "'");
                sb.append(", showClear: true, showClose: true});\n");

                if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                    if (!getAutoComplete() && (getTypeAhead()!=null)) {
                        sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                        sb.append("  $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;});\n");
                    }
                }
                else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                    sb.append("$('#" + id + "').on('dp.change', function (e) {\n");
                    sb.append("  $('#oacommand').val('" + id + "'); $('form').submit(); return false;});\n");
                }
                sb.append("}\n");  // end bootstrap
                sb.append("else {\n");
                sb.append("  $('#" + id + "').timepicker({ timeFormat: '" + tfmtJquery + "'");
                if (!getSubmit() && bAjaxSubmit && OAString.isEmpty(getForwardUrl())) {
                    if (!getAutoComplete() && (getTypeAhead()!=null)) {
                        sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); ajaxSubmit(); return false;}");
                    }
                }
                else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
                    sb.append(", onClose: function() { $('#oacommand').val('" + id + "'); $('form').submit(); return false;}");
                }
                sb.append("});\n");
                sb.append("}");  // end jquery
            }

            if (isDateTime() && !getAutoComplete() && (getTypeAhead()!=null) && getForm() != null) {
                sb.append("$('#" + getForm().getId() + "').prepend(\"<input type='hidden' id='" + id + "_ts' name='" + id
                        + ".ts' value=''>\");\n");
            }
        }
        else if (!OAString.isEmpty(inputMask)) {
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
        if (maxWidth <= 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        if (dataSourceMax > 0 && maxWidth > dataSourceMax) return dataSourceMax;
        return maxWidth;
    }

    /**
     * max length of text. If -1 (default) then unlimited.
     */
    public void setMaxWidth(int x) {
        maxWidth = x;
        maxWidth = getMaxWidth(); // verify with Datasource
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
    public void setTypeAhead(OATypeAhead ta) {
        this.typeAhead = ta;
    }
    public OATypeAhead getTypeAhead() {
        return typeAhead;
    }
    public void setLookupValues(String[] lookupValues) {
        this.lookupValues = lookupValues;
    }
    public String[] getLookupValues() {
        return lookupValues;
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
    
    /**
     * Called by browser
     * Uses oatypeahead.jsp
     * 
     * Must be json string using double quotes, and "id", "display" for values
     *    ex:  String s = "{\"id\":1,\"display\":\"m-1-1\"},{\"id\":2,\"display\":\"m-2-1\"}"; 
     * 
     * @param value user input
     * @return list of values to send back to browser.
     */
    public String getTypeAheadJson(String searchText) {
        if (typeAhead == null) return null;

        ArrayList al = typeAhead.search(searchText);
        
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
            displayValue.replace('\"', ' ');

            String dd = typeAhead.getDropDownDisplayValue(obj);
            if (dd == null) dd = "";
            dd.replace('\"', ' ');
            
            if (bPropertyPathIsManyLink || bPropertyPathIsOneLink) {
                json += "{\"id\":"+id+",\"display\":\""+displayValue+"\",\"dropdowndisplay\":\""+dd+"\"}";
            }
            else {
                // need to send id=displayValue, since they will be stored in property and not the id
                //    and then used when it has to reset the txt value from this data
                json += "{\"id\":\""+displayValue+"\",\"display\":\""+displayValue+"\",\"dropdowndisplay\":\""+dd+"\"}";
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
        if (getAutoComplete()) {
            al.add(OAJspDelegate.JS_jquery_ui);
        }
        if (getInputMask() != null) {
            al.add(OAJspDelegate.JS_jquery_ui);
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

        if (getTypeAhead() != null || getLookupValues() != null) {
            al.add(OAJspDelegate.JS_bootstrap);
            al.add(OAJspDelegate.JS_bootstrap_typeahead);
        }
        
        if (isDateTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.JS_jquery_ui);
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
                al.add(OAJspDelegate.JS_jquery_ui);
            }
            else {
                al.add(OAJspDelegate.JS_bootstrap);
                al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.JS_bootstrap_datetimepicker);
            }
        }
        else if (isTime()) {
            if (getForm() == null || getForm().getDefaultJsLibrary() == OAApplication.JSLibrary_JQueryUI) {
                al.add(OAJspDelegate.JS_jquery_ui);
                al.add(OAJspDelegate.JS_jquery_timepicker);
            }
            else {
                al.add(OAJspDelegate.JS_bootstrap);
                al.add(OAJspDelegate.JS_moment);
                al.add(OAJspDelegate.JS_bootstrap_datetimepicker);
            }
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

        if (getTypeAhead() != null || getLookupValues() != null) {
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

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }
    
    public void setClearButton(boolean b) {
        this.bClearButton = b;
    }
    public boolean getClearButton() {
        return this.bClearButton;
    }
    
}
