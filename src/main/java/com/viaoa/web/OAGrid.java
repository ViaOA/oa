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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
 * Grid component that will scroll equal sized cells.
 * A template can be defined that will be filled out for each cell. Tags can then
 * be used to embed other components, like servletImages and htmlElemets, or property paths.
 *
 * 
 * note: uses bootstrap, needs to be contained in a "container" or "container-fluid" parent (only one per page)
 * 
 * @author vvia
 */
public class OAGrid implements OAJspComponent, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    private Hub hub;
    private String id;
    private OAForm form;
    private boolean bEnabled = true;
    protected String visiblePropertyPath;
    private boolean bVisible = true;
    private boolean bAjaxSubmit=true;
    private boolean bSubmit=false;
    private String forwardUrl;
    
    private OATablePager pager;
    private int columns;

    /** template that uses ${name} tags to insert values from list of added components. */
    private String htmlTemplate;
    private OATemplate template;
    
    private HashMap<String, OAJspComponent> hmChildren = new HashMap<String, OAJspComponent>();

    
    public OAGrid(String id, Hub hub, int columns) {
        this.id = id;
        this.hub = hub;
        this.columns = columns;
    }
    
    public Hub getHub() {
        return hub;
    }
    public void setPager(int scrollAmt, int maxCount, int pageDisplayCount, boolean bTop, boolean bBottom) {
        pager = new OATablePager(hub, scrollAmt, maxCount, pageDisplayCount, bTop, bBottom);
        pager.setObjectsPerRowCount(this.columns);
    }
    public OATablePager getPager() {
        return pager;
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

    @Override
    public boolean _beforeFormSubmitted() {
        for (Map.Entry<String, OAJspComponent> e : hmChildren.entrySet()) {
            e.getValue()._beforeFormSubmitted();
        }
        return true;
    }

    private String submitUpdateScript;
    private OAJspComponent jcSubmitted;
    private boolean bPageCommandUsed;
    private int rowSubmitted;
    
    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        rowSubmitted = -1;
        jcSubmitted = null;
        bPageCommandUsed = false;
        
        boolean bWasSubmitted = _myOnSubmit(req, resp, hmNameValue);
        for (Map.Entry<String, OAJspComponent> e : hmChildren.entrySet()) {
            OAJspComponent jc = e.getValue();
            boolean b = jc._onFormSubmitted(req, resp, hmNameValue);
            if (b) {
                jcSubmitted = jc;
                bWasSubmitted = true;
            }
        }
        
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        Enumeration enumx = req.getParameterNames();
        String name = null;
        OAObject obj = null;
        String value = null;
        
        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.equalsIgnoreCase("oahidden"+id)) continue;
            value = req.getParameter(name);
            break;
        }

        if (OAString.isEmpty(value)) return false;        

        if (value.charAt(0) == 'P') {
            if (pager == null) return false;
            int page = OAConv.toInt(value.substring(1));
            pager.currentPage = page;
            submitUpdateScript = null;
            bPageCommandUsed = true;
            return true;
        }
        bPageCommandUsed = false;
        
        rowSubmitted = OAConv.toInt(value);
        
    //    submitUpdateScript = "$('#oahidden"+id+"').val('');";

        // submitUpdateScript += "$('table#"+id+" tr').removeAttr('oahold');";
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        return true; // true if this caused the form submit
    }

    @Override
    public String _onSubmit(String forwardUrl) {
        if (rowSubmitted >= 0) hub.setPos(rowSubmitted);
        if (jcSubmitted != null) {
            String s = jcSubmitted.getForwardUrl();
            if (OAString.isNotEmpty(s)) forwardUrl = s;  
            s = jcSubmitted._onSubmit(forwardUrl);
            if (OAString.isNotEmpty(s)) forwardUrl = s;  
        }
        if (bPageCommandUsed) forwardUrl = null; // stay on page
        String s = onSubmit(forwardUrl);
        if (OAString.isNotEmpty(s)) forwardUrl = s;  
        return forwardUrl;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        for (Map.Entry<String, OAJspComponent> e : hmChildren.entrySet()) {
            String s = e.getValue()._afterFormSubmitted(forwardUrl);
            if (s != null) forwardUrl = s;
        }
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        for (Map.Entry<String, OAJspComponent> e : hmChildren.entrySet()) {
            String s = e.getValue().afterFormSubmitted(forwardUrl);
            if (s != null) forwardUrl = s;
        }
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
        sb.append("$('form').prepend(\"<input id='oahidden"+id+"' type='hidden' name='oahidden"+id+"' value=''>\");\n");
        
        sb.append(getAjaxScript());
        
        sb.append("function oagrid"+id+"CellClick() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        sb.append("    if (v == null) return;\n");
        sb.append("    $('#oahidden"+id+"').val(v);\n");
        
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
        }
        sb.append("}\n");

        if (pager != null) {
            sb.append("function oatablePager"+id+"Click() {\n");
            sb.append("    var v = $(this).attr('class');\n");
            sb.append("    if (v == 'oatablePagerDisable') return;\n");
            sb.append("    if (v == 'oatablePagerSelected') return;\n");
            sb.append("    \n");
            sb.append("    v = $(this).attr('oaValue');\n");
            sb.append("    if (typeof v == 'undefined') {\n");
            sb.append("        v = $(this).html();\n");
            sb.append("    }\n");
            sb.append("    if (v == null) return;\n");
            sb.append("    $('#oahidden"+id+"').val('P'+v);\n");
            sb.append("    ajaxSubmit();\n");
            sb.append("}\n");
        }
        
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    private String lastAjaxSent;
    

    @Override
    public String getAjaxScript() {

        if (submitUpdateScript != null) {
            String s = submitUpdateScript;
            submitUpdateScript = null;
            return s;
        }
        StringBuilder sb = new StringBuilder(2048);
        
        
        sb.append("<div id='oa"+id+"' class='oaGrid'>");
        
        if (pager != null && pager.isTop()) {
            sb.append("<div class='row'>");
            sb.append("<div class='col-sm-12 oatablePager'>");
            sb.append(pager.getHtml());
            sb.append("</div>");
            sb.append("</div>");
        }
        
        
        int scrollAmt = (pager == null) ? ((int)(Math.ceil( ((double)hub.getSize())/columns))) : pager.getScrollAmount();
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        
        int pos = topRow * columns;

        // needs to be outside for responsive layout
        sb.append("<div class='row auto-clear'>");
        for (int row=0; row < scrollAmt ;row++) {
            
            for (int col=0; col < columns; col++, pos++) {
                Object obj = hub.getAt(pos);

                int x = columns;
                sb.append("<div class='oaGridCell col-lg-"+(12/x));

                if (x > 1) {
                    x--;
                    sb.append(" col-md-"+(12/x));
                    if (x > 1) {
                        x--;
                        sb.append(" col-sm-"+(12/x));
                    }
                }
                
                if (obj == hub.getAO()) sb.append(" oatableSelected");
                sb.append("'");
                
                
                if (obj != null) sb.append(" oarow='"+(pos)+"'");
                
                if (cellHeight > 0 || cellWidth > 0) {
                    sb.append(" style='display: inline-block;");
                    if (cellWidth > 0) {
                        sb.append("width: "+cellWidth+"px;");
                    }
                    if (cellHeight > 0) {
                        sb.append("height: "+cellHeight+"px;");
                    }
                    sb.append("overflow: hidden;'");
                }
                sb.append(">");
                
                String s = getHtml(obj, pos, row, col);
                
                
                if (s == null) s = "";
                
                sb.append(s+"</div>");
            }
        }
        sb.append("</div>");
            
        if (pager != null && pager.isBottom()) {
            sb.append("<div class='row'>");
            sb.append("<div class='col-sm-12 oatablePager'>");
            sb.append(pager.getHtml());
            sb.append("</div>");
            sb.append("</div>");
        }
        
        sb.append("</div>");  // outer oaGrid

        String strGrid = sb.toString();

        /*was, not needed anymore        
        strGrid = OAString.convert(strGrid, "\\\"", "xQxq");
        strGrid = OAString.convert(strGrid, "\"", "\\\"");
        strGrid = OAString.convert(strGrid, "xQxq", "\\\"");
        
        strGrid = Util.convert(strGrid, "\n", "\\n");
        strGrid = Util.convert(strGrid, "\r", "\\r");
        */        
        
        strGrid = OAJspUtil.createJsString(strGrid, '"');
        
        sb = new StringBuilder(strGrid.length() + 2048);
        sb.append("$('#"+id+"').html(\""+strGrid+"\");\n");

        sb.append("$('#oa"+id+" div.oaGridCell').click(oagrid"+id+"CellClick);\n");
        
        if (pager != null) {
            sb.append("$('div#oa"+id+" .oatablePager ul li').click(oatablePager"+id+"Click);\n");
        }
        
        sb.append("$('#oahidden"+id+"').val('');\n"); // set back to blank
        
        if (getVisible()) sb.append("$('#" + id + "').show();\n");
        else sb.append("$('#" + id + "').hide();\n");
        
        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        return js;
    }
    

    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return bEnabled && hub != null && hub.getAO() != null;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    
    
    protected String propertyPath;
    protected String servletImagePropertyPath;
    protected String servletImageBytePropertyName="bytes"; // name of property that has the bytes
    
    protected int cellWidth;
    protected int cellHeight;

    public void setCellWidth(int w) {
        this.cellWidth = w;
    }
    public int getCellWidth() {
        return this.cellWidth;
    }
    public void setCellHeight(int h) {
        this.cellHeight = h;
    }
    public int getCellHeight() {
        return this.cellHeight;
    }

    protected int imageWidth;
    protected int imageHeight;

    public void setImageWidth(int w) {
        this.imageWidth = w;
    }
    public int getImageWidth() {
        return this.imageWidth;
    }
    public void setImageHeight(int h) {
        this.imageHeight = h;
    }
    public int getImageHeight() {
        return this.imageHeight;
    }

    /**
     * Ex: from: AwardType.getAvailableEcards "ECard.P_ImageStore"
     */
    public void setServletImagePropertyPath(String propertyPath) {
        this.servletImagePropertyPath = propertyPath;
    }
    

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
            protected String getValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props, boolean bUseFormat) {
                String s;
                OAJspComponent comp = hmChildren.get(propertyName);
                if (comp == null) {
                    s = super.getValue(obj, propertyName, width, fmt, props, bUseFormat);
                    String s1 = getTemplateValue(obj, propertyName, width, fmt, props, s);
                    if (OAString.isEqual(s,s1)) {
                        s = OAJspUtil.convertToHtml(s);
                    }
                    else s = s1;
                }
                else {
                    if (obj == getHub().getAO()) {
                        s = comp.getEditorHtml(obj);
                    }
                    else {
                        s = comp.getRenderHtml(obj);
                    }
                    s = getTemplateValue(obj, propertyName, width, fmt, props, s);
                }
                return s;
            }
            @Override
            protected String getOutputText(String s) {
                //String sx = OAJspUtil.createJsString(s, '\"', false, false);
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
     * Callback from {@link #getTemplate(Object, int, int, int)}, before calling getEscapedHtml
     */
    public String getTemplateValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props, String defaultValue) {
        return defaultValue;
    }
    
    /**
     * This will use the OATemplate to create the html template for a single object.  
     */
    public String getTemplateHtml(Object objx, int pos, int row, int col) {
        if (!(objx instanceof OAObject)) return null;
        OAObject obj = (OAObject) objx;
        
        if (getTemplate() == null) return null;
        
        template.setProperty("OAPOS", ""+pos);
        template.setProperty("OACOL", ""+(col+1));
        template.setProperty("OAROW", ""+(row+1));
        
        String s = template.process(obj);
        
        return s;
        
    }
    
    
    public String getHtmlServletImage(Object obj, int pos, int row, int col) {
        if (OAString.isEmpty(servletImagePropertyPath)) return null;
        String result = null;
        Object value = null;
        
        if (obj != null) {
            value = ((OAObject)obj).getProperty(servletImagePropertyPath);
        }
        
        if (value != null) {
            String className = value.getClass().getName();

            Object id = ((OAObject) value).getProperty("id");
            
            String src = String.format("/servlet/img?c=%s&id=%s&p=%s", className, id+"", getServletImageBytePropertyName());
            if (imageHeight > 0) src = String.format("%s&mh=%d", src, imageHeight);
            if (imageWidth > 0) src = String.format("%s&mw=%d", src, imageWidth);
            result = "<img src='" + src +"'>";
        }          
        else {
            String s = "<span";
            if (imageHeight > 0 || imageWidth > 0) {
                s += " style='";
                if (imageWidth > 0) s += "width: "+imageWidth+"px;";
                if (imageHeight > 0) s += "height: "+imageHeight+"px;";
                s += "'";
            }
            s += "></span>";
            result = s;
        }
        return result;
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
    
    
    /*
     * By default, will get servlet image using servletImagePropertyPath, and text using propertyPath.
     * @see #getHtmlImage(Object, int, int, int)
     * @see #getHtmlData(Object, int, int, int)
     */
    public String getHtml(Object obj, int pos, int row, int col) {
        String img = getHtmlServletImage(obj, pos, row, col);
        
        String result = "";
        if (img != null) result = img;
        
        String data = getHtmlPropertyPath(obj, pos, row, col);
        if (data != null) {
            result += "<span>"+data+"</span>";
        }
        
        String temp = getTemplateHtml(obj, pos, row, col);
        if (temp != null) {
            result += temp;
        }
        
        return result;
    }

    
    protected String bytePropertyName="bytes"; // name of property that has the bytes
    // default is "bytes"
    public void setServletImageBytePropertyName(String propName) {
        this.bytePropertyName = propName;
    }
    public String getServletImageBytePropertyName() {
        return this.bytePropertyName;
    }

    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return true;

        if (OAString.isEmpty(visiblePropertyPath)) return true;

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
    
    
    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();
        al.add(OAJspDelegate.JS_jquery);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

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

    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }

    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
}
