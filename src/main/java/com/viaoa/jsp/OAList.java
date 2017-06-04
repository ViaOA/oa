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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;


/**
 * Used with an HTML <UL/OL> or <OL/OL> to replace the <LI> with hub objects.
 * 
 * @author vvia
 */
public class OAList implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    protected Hub hub;
    protected String id;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit=true;
    protected boolean bSubmit=false;
    protected String forwardUrl;
    protected String nullDescription = "";
    protected String maxHeigth; // ex:  200px,  12em

    protected String format;
//    protected int lineWidth, maxRows, minLineWidth;
    
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected int columns, popupColumns;
    
    protected int rows;
    
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
    public boolean _beforeSubmit() {
        return true;
    }

    private String submitUpdateScript;
    

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        boolean bWasSubmitted = _myOnSubmit(req, resp, hmNameValue);
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        // Enumeration enumx = req.getParameterNames();

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
        
        Object obj = hub.getAt(row);
        onClick(obj);
        
        submitUpdateScript = "$('#oalist"+id+"').val('');";
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
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    @Override
    public String _afterSubmit(String forwardUrl) {
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
        sb.append("$('form').prepend(\"<input id='oalist"+id+"' type='hidden' name='oalist"+id+"' value=''>\");\n");
        
        String s = getScript2();
        if (s != null) sb.append(s);
        
        sb.append(getAjaxScript());
    
        s = getScript3();
        if (s != null) sb.append(s);
        
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
        

        sb.append("function oaList"+id+"Click() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        
        sb.append("    if (v == null) return;\n");
        sb.append("    $('#oalist"+id+"').val(v);\n");
        
        s = getOnLineClickJs();
        if (s != null) {
            sb.append("    "+s);
        }
        
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
        }
        sb.append("}\n");
        
        if (getEnabled()) {
            sb.append("$('#"+id+" li').click(oaList"+id+"Click);\n");
        }
        sb.append("$('#"+id+"').addClass('oaSubmit');\n");

        sb.append("$('#oalist"+id+"').val('');\n"); // set back to blank
        
        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
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
    
    
//qqqqqqq add format, length, etc
    
    public String getHtml(Object obj, int pos) {
        if (obj == null || pos < 0) return getNullDescription();

        String value = ((OAObject) obj).getPropertyAsString(getPropertyPath(), getFormat());
        
        return value;
    }


    public void setMaxHeight(String val) {
        this.maxHeigth = val;
    }
    public String getMaxHeigth() {
        return this.maxHeigth;
    }

}
