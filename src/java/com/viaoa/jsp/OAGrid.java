package com.viaoa.jsp;

import java.util.Enumeration;
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
 * Grid component that will scroll equal sized cells.
 * A template can be defined that will be filled out for each cell. Tags can then
 * be used to embed other components, like servletImages and htmlElemets, or property paths.
 * 
 * @author vvia
 */
public class OAGrid implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    private Hub hub;
    private String id;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit=true;
    private boolean bSubmit=false;
    private String forwardUrl;
    
    private OATablePager pager;
    private int columns;

    /** template that uses ${name} tags to insert values from list of added components. */
    private String template;
    private HashMap<String, OAJspComponent> hm = new HashMap<String, OAJspComponent>();
    
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
        for (Map.Entry<String, OAJspComponent> e : hm.entrySet()) {
            e.getValue().setForm(form);
        }
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    @Override
    public boolean _beforeSubmit() {
        for (Map.Entry<String, OAJspComponent> e : hm.entrySet()) {
            e.getValue()._beforeSubmit();
        }
        return true;
    }

    private String submitUpdateScript;
    
    private boolean bWasSubmitted;

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp) {
        bWasSubmitted = _myOnSubmit(req, resp);
        for (Map.Entry<String, OAJspComponent> e : hm.entrySet()) {
            e.getValue()._beforeSubmit();
        }
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp) {
        Enumeration enumx = req.getParameterNames();
        String name = null;
        OAObject obj = null;
        String value = null;
        for ( ; enumx.hasMoreElements(); ) {
            name = (String) enumx.nextElement();
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
            return true;
        }
        
        int row = OAConv.toInt(value);
        hub.setPos(row);
        
        submitUpdateScript = "$('#oahidden"+id+"').val('');";

        // submitUpdateScript += "$('table#"+id+" tr').removeAttr('oahold');";
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        return true; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        for (Map.Entry<String, OAJspComponent> e : hm.entrySet()) {
            e.getValue()._beforeSubmit();
        }
        if (bWasSubmitted) {
            if (this.forwardUrl != null) forwardUrl = this.forwardUrl;
            return onSubmit(forwardUrl); 
        }
        return forwardUrl;
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
        sb.append("$('form').prepend(\"<input id='oahidden"+id+"' type='hidden' name='oahidden"+id+"' value=''>\");\n");
        sb.append(getAjaxScript());
        
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
        
        
        sb.append("<table id='oa"+id+"' class='oatable'  border='0' cellpadding='0' cellspacing='0'>");
        if (pager != null && pager.isTop()) {
            sb.append("<thead><tr><td colspan='"+columns+"' class='oatablePager'>");
            sb.append(pager.getHtml());
            sb.append("</td></tr></thead>");
        }
        if (pager != null && pager.isBottom()) {
            sb.append("<tfoot><tr><td colspan='"+columns+"' class='oatablePager'>");
            sb.append(pager.getHtml());
            sb.append("</td></tr></tfoot>");
        }
        
        sb.append("<tbody>");
        
        int scrollAmt = (pager == null) ? ((int)(Math.ceil(hub.getSize()/columns))) : pager.getScrollAmount();
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        
        int pos = topRow * columns;
        
        for (int row=0; row < scrollAmt ;row++) {
            
            sb.append("<tr>");
            for (int col=0; col < columns; col++, pos++) {
                Object obj = hub.getAt(pos);
                
                String s = "";
                if (obj == hub.getAO()) s = " class='oatableSelected'";
                
                // 20130407
                if (obj != null) s += " oarow='"+(pos)+"'";
                //was:  s += " oarow='"+(pos)+"'";
                
                String style;
                if (cellHeight > 0 || cellWidth > 0) {
                    style = " style='display: inline-block;";
                    if (cellWidth > 0) {
                        style += "width: "+cellWidth+"px;";
                    }
                    if (cellHeight > 0) {
                        style += "height: "+cellHeight+"px;";
                    }
                    style += "overflow: hidden;";
                    style += "'";
                }
                else style = "";
                
                sb.append("<td"+s+style+">"+getHtml(obj, pos, row, col)+"</td>");
            }
            sb.append("</tr>");
        }
            
        sb.append("</tbody>");
        sb.append("</table>");

        String strTable = sb.toString();
        strTable = Util.convert(strTable, "\\", "\\\\");
        strTable = Util.convert(strTable, "'", "\\'");
        strTable = Util.convert(strTable, "\n", "\\n");
        strTable = Util.convert(strTable, "\r", "\\r");
        
        sb = new StringBuilder(strTable.length() + 2048);
        sb.append("$('#"+id+"').html('"+strTable+"');\n");

        sb.append("$('table#oa"+id+" tbody tr').attr('class', 'oatableEven');");
        
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
        sb.append("$('#oa"+id+" tr td[oarow]').click(oagrid"+id+"CellClick);\n");

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
            sb.append("$('table#oa"+id+" .oatablePager ul li').click(oatablePager"+id+"Click);\n");
        }
        sb.append("$('#oahidden"+id+"').val('');\n"); // set back to blank
        
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
     * Ex: from: AwardType.getAvailableEcards "ECard.PROPERTY_ImageStore"
     */
    public void setServletImagePropertyPath(String propertyPath) {
        this.servletImagePropertyPath = propertyPath;
    }
    
    /**
     * Used by template to insert the img src value. 
     * ex:  template: "<img src='${card}'>" 
     */
    public void add(String name, OAServletImage img) {
        if (name != null && img != null) {
            hm.put(name, img);
            img.setForm(form);
        }
    }
    /**
     * Used by template to insert text. 
     * ex:  template: "<div>${fullName}</div>" 
     */
    public void add(String name, OAHtmlElement ele) {
        if (name != null && ele != null) {
            hm.put(name, ele);
            ele.setForm(form);
        }
    }

    /**
     * Uses ${name} to have HtmlElement and ServletImage components inserted.
     * if 'name' is not found, then it will be used as a property path, using  ${propertyPath, fmt}
     */
    public void setTemplate(String template) {
        this.template = template;
    }
    public String getTemplate() {
        return this.template;
    }
    
   
    public String getTemplate(Object objx, int pos, int row, int col) {
        if (!(objx instanceof OAObject)) return null;
        if (template == null) return null;
        OAObject obj = (OAObject) objx;
        
        String result = "";
        
        int p = 0;
        int pLast = 0;
        for (; pLast < template.length(); ) {
            p = template.indexOf("${", pLast);
            if (p < 0) {
                result += template.substring(pLast);
                break;
            }
            if (p > pLast) result += template.substring(pLast, p);
            p += 2;
            pLast = template.indexOf("}", p);
            if (pLast < 0) break;
            
            String s = template.substring(p, pLast);
            pLast++;
            
            OAJspComponent comp = hm.get(s);
            if (comp == null) {
                // use property path
                int x = s.indexOf(',');
                if (x < 0) x = s.indexOf(' ');
                if (x < 0) x = s.indexOf(':');
                String fmt = "";
                if (x >= 0) {
                    fmt = s.substring(x+1);
                    s = s.substring(x);
                }
                if (OAString.isEmpty(fmt)) {
                    OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), s);
                    fmt = pp.getFormat();
                }
                result += obj.getPropertyAsString(s, fmt);
            }
            else {
                if (comp instanceof OAHtmlElement) {
                    s = ((OAHtmlElement) comp).getHtml(obj);
                    if (s != null) result += s;
                }
                else if (comp instanceof OAServletImage) {
                    s = ((OAServletImage) comp).getHtmlSource(obj);
                    if (s != null) result += s;
                }
            }
        }
        return result;
    }
    
    
    
    
    public String getHtmlServletImage(Object obj, int pos, int row, int col) {
        String result = null;
        if (!OAString.isEmpty(getServletImageBytePropertyName())) {
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
        }
        return result;
    }
    
    public String getHtmlData(Object obj, int pos, int row, int col) {
        String result = null;
        String pp = getPropertyPath();
        if (!OAString.isEmpty(pp)) {
            if (obj != null) {
                result = ((OAObject)obj).getPropertyAsString(pp);
            }
        }
        return result;
    }
    
    
    /**
     * By default, will get servlet image using servletImagePropertyPath, and text using propertyPath.
     * @see #getHtmlImage(Object, int, int, int)
     * @see #getHtmlData(Object, int, int, int)
     */
    public String getHtml(Object obj, int pos, int row, int col) {
        String img = getHtmlServletImage(obj, pos, row, col);
        

        String result = "";
        if (img != null) result = img;
        
        String data = getHtmlData(obj, pos, row, col);
        if (data != null) {
            result += "<span>"+data+"</span>";
        }
        
        String temp = getTemplate(obj, pos, row, col);
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
        return this.bVisible;
    }

}
