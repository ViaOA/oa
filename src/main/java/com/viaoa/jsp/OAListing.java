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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;


/*qqqqqqqqqqqqqqqqqq replaced by OAList.java qqqqqqqqqqqq*/

/**
 * Used with an HTML <UL> or <OL> to replace the <LI> with hub objects.
 * 
 * @author vvia
 */
public class OAListing implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    private Hub hub;
    private String id;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit=true;
    private boolean bSubmit=false;
    private String forwardUrl;
    protected String nullDescription = "";

//qqqqqqq these are not all finished    
    protected String format;
    protected int lineWidth, maxRows, minLineWidth;
    
    protected String propertyPath;
    protected String visiblePropertyPath;
    
    public OAListing(String id, Hub hub, String propertyPath) {
        this.id = id;
        this.hub = hub;
        setPropertyPath(propertyPath);
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
    
    
    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
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

        String name = null;
        String[] values = null;

        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.equalsIgnoreCase("oalisting"+id)) continue;
            values = ex.getValue();
            break;
        }

        if (values == null || values.length == 0 || OAString.isEmpty(values[0])) {
            return false;        
        }

        int row = OAConv.toInt(values[0]);
        
        Object obj = hub.getAt(row);
        onClick(obj);
        
        submitUpdateScript = "$('#oalisting"+id+"').val('');";
        submitUpdateScript += "$('#"+id+" li').removeClass('oaSelected');";

        String s;
        if (hub.getPos() >= 0) {
            s = " li:nth-child("+(hub.getPos()+1)+")";
        }
        else {
            s = " li:nth-child("+(hub.getSize()+1)+")";
        }
        submitUpdateScript += "$('#"+id+s+"').addClass('oaSelected');";

        return true; // true if this caused the form submit
    }
    
    /**
     * can be overwritten to know when an item is selected.
     * @param obj
     */
    public void onClick(Object obj) {
        if (hub != null) hub.setAO(obj);
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
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('form').prepend(\"<input id='oalisting"+id+"' type='hidden' name='oalisting"+id+"' value=''>\");\n");
        
        sb.append(getAjaxScript());
    
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        // TODO Auto-generated method stub
        return null;
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {

        if (submitUpdateScript != null) {
            String s = submitUpdateScript;
            submitUpdateScript = null;
            lastAjaxSent = null;
            return s;
        }
        StringBuilder sb = new StringBuilder(2048);
        
        
        for (int pos=0; ;pos++) {
            Object obj = hub.getAt(pos);
            if (obj == null) break;

            sb.append("<li");
            if (obj == hub.getAO()) sb.append(" class='oaSelected'");
            sb.append(" oarow='"+(pos)+"'>");
            String s = getHtml(obj, pos);
            if (s != null) sb.append(s);
            sb.append("</li>");
        }

        String s = getHtml(null, -1);
        if (s != null) {
            sb.append("<li");
            if (hub.getAO() == null) sb.append(" class='oaSelected'");
            sb.append(" oarow='-1'>");
            sb.append(s);
            sb.append("</li>");
        }        
        
        
        String strListing = sb.toString();
        //strListing = Util.convert(strListing, "\\", "\\\\");
        //strListing = Util.convert(strListing, "'", "\\'");
        
        
        sb = new StringBuilder(strListing.length() + 2048);
        sb.append("$('#"+id+"').addClass('oaListing');\n");
        
        strListing = OAJspUtil.createJsString(strListing, '\'', false, true);
        sb.append("$('#"+id+"').html('"+strListing+"');\n");
        

        sb.append("function oaListing"+id+"Click() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        sb.append("    if (v == null) return;\n");
        sb.append("    $('#oalisting"+id+"').val(v);\n");
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
        }
        sb.append("}\n");
        
        if (getEnabled()) {
            sb.append("$('#"+id+" li').click(oaListing"+id+"Click);\n");
        }
        sb.append("$('#"+id+"').addClass('oaSubmit');\n");

        sb.append("$('#oalisting"+id+"').val('');"); // set back to blank
        
        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        return js;
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
    
    
//qqqqqqq add format, length, etc
    
    public String getHtml(Object obj, int pos) {
        if (obj == null || pos < 0) return getNullDescription();
        String value = ((OAObject) obj).getPropertyAsString(getPropertyPath(), getFormat());
        
        return value;
    }

    @Override
    public String getEditorHtml(OAObject obj) {
        return null;
    }
    
    @Override
    public String getRenderHtml(OAObject obj) {
        return getHtml(obj, getHub().getPos(obj));
    }

    @Override
    public void _beforeOnSubmit() {
    }
}
