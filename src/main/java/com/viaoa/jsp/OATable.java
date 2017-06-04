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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubListener;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * Ajax table component, supports binding, editors, custom event handling.
 * @author vvia
 *
 */
public class OATable implements OAJspComponent {
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
    private ArrayList<OATableColumn> alColumns = new ArrayList<OATableColumn>();
    private int lastHubPos=-1;
    private Hub hubLast;
    private Hub hubSelect;
    private String selectTitle;

    private int scrollTop;
    private int scrollLeft;
    private String lastAjaxSent;

    private int width;
    private int height;
    private int charWidth = 8; // single X char width in pixels - default is to use table.charWidth
    

    public OATable(String id, Hub hub) {
        this.id = id;
        this.hub = hub;
    }
        
    
    public Hub getHub() {
        return hub;
    }

    public Hub getSelectHub() {
        return hubSelect;
    }
    public void setSelectHub(Hub h) {
        this.hubSelect = h;
    }
    public void setSelectTitle(String title) {
        this.selectTitle = title;
    }
    public String getSelectTitle() {
        return this.selectTitle;
    }

    /** width of a single character.  Used to compute the width based on columns, defaults to 12 */
    public void setAverageCharWidth(int w) {
        charWidth = w;
    }
    public int getAverageCharWidth() {
        return charWidth;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void setPager(int scrollAmt, int maxCount, int pageDisplayCount, boolean bTop, boolean bBottom) {
        pager = new OATablePager(hub, scrollAmt, maxCount, pageDisplayCount, bTop, bBottom);
    }

    public void addColumn(OATableColumn col) {
        alColumns.add(col);
        col.table = this;
    }
    public void addColumn(String propertyPath, String heading, int columns, String format) {
        OATableColumn col = new OATableColumn(propertyPath, heading, columns, format);
        this.addColumn(col);
        col.table = this;
    }
    public void addColumn(String propertyPath, String heading, int columns) {
        OATableColumn col = new OATableColumn(propertyPath, heading, columns, null);
        this.addColumn(col);
        col.table = this;
    }
    public void addColumn(String[] propertyPaths, String heading, int columns) {
        OATableColumn col = new OATableColumn(propertyPaths, heading, columns);
        this.addColumn(col);
        col.table = this;
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
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    @Override
    public boolean _beforeSubmit() {
        for (OATableColumn tc : alColumns) {
            OATableEditor te = tc.getEditor();
            if (te instanceof OAJspComponent) {
                ((OAJspComponent)te)._beforeSubmit();
            }
        }
        return true;
    }

    private String submitUpdateScript;
    private String submitHref;

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        for (OATableColumn tc : alColumns) {
            OATableEditor te = tc.getEditor();
            if (te instanceof OAJspComponent) {
                ((OAJspComponent)te)._onSubmit(req, resp, hmNameValue);
            }
        }
        boolean bWasSubmitted = _myOnSubmit(req, resp, hmNameValue);
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        Enumeration enumx = req.getParameterNames();
        String name = null;
        OAObject obj = null;
        String[] values = null;
        
        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = ex.getKey();
            if (!name.equalsIgnoreCase("oahidden"+id)) continue;
            values = ex.getValue();
            break;
        }

        String s = req.getParameter("oahidden"+id+"Scroller");
        scrollLeft = scrollTop = 0;
        if (s != null) {
            int pos = s.indexOf(',');
            if (pos > 0) {
                scrollLeft = Integer.valueOf(s.substring(0,pos));
                scrollTop = Integer.valueOf(s.substring(pos+1));
            }
        }
        
        if (values == null || values.length == 0 || OAString.isEmpty(values[0])) {
            return false;        
        }
        String value = values[0];
        
        submitHref = req.getParameter("oacommand");
        if (submitHref != null && submitHref.startsWith("href=")) {
            submitHref = submitHref.substring(5);
        }
        else {
            submitHref = null;
        }
        
        if (hubSelect != null) {
            int min = 0;
            int max = -1;
            if (pager != null) {
                min = pager.getTopRow();
                max = (min + pager.getScrollAmount()) - 1;;
            }
            
            ArrayList<Integer> al = new ArrayList<Integer>();
            enumx = req.getParameterNames();

            String[] ss = req.getParameterValues("table"+id+"check");
            if (ss != null) {
                for (String sx : ss) {
                    if (sx != null && sx.length() > 3 && sx.startsWith("row")) {
                        int x = OAConv.toInt(sx.substring(3));
                        al.add(x);
                    }
                }
            }
            for (Object objx : hubSelect) {
                int pos = hub.getPos(objx);
                if (pos < min || pos > max) continue;
                if (!al.contains(pos)) {
                    hubSelect.remove(objx);
                }
            }
            for (Integer pos : al) {
                Object objx = hub.getAt(pos);
                if (!hubSelect.contains(objx)) hubSelect.add(objx);
            }
        }

        if (value.charAt(0) == 'P') {
            if (pager == null) return false;
            int page = OAConv.toInt(value.substring(1));
            pager.currentPage = page;
            submitUpdateScript = null;
            scrollLeft = scrollTop = 0;
            return true;
        }
        
        int row = OAConv.toInt(value);
        hub.setPos(row);
        
        submitUpdateScript = "$('#oahidden"+id+"').val('');";

        submitUpdateScript += "$('table#oa"+id+" tbody tr:even').each(function(i) { if ($(this).attr('class') != 'oatableDisable') $(this).attr('class', 'oatableEven');});";
        submitUpdateScript += "$('table#oa"+id+" tbody tr:odd').each(function(i) { if ($(this).attr('class') != 'oatableDisable') $(this).attr('class', 'oatableOdd');});";
        
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        if (hub.getPos() >= 0) {
            s = " tr:nth-child("+( (hub.getPos()-topRow)+1)+")";
            submitUpdateScript += "$('table#oa"+id+s+"').attr('class', 'oatableSelected');";
        }
        
        return true; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        for (OATableColumn tc : alColumns) {
            OATableEditor te = tc.getEditor();
            if (te instanceof OAJspComponent) {
                String s = ((OAJspComponent)te)._afterSubmit(forwardUrl);
                if (!OAString.isEmpty(s)) forwardUrl = s;
            }
        }
        return forwardUrl;
    }

    @Override
    public String onSubmit(String forwardUrl) {
        if (submitHref != null) forwardUrl = this.submitHref;
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
        
        for (OATableColumn tc : alColumns) {
            OATableEditor te = tc.getEditor();
            if (te instanceof OAJspComponent) {
                OAForm f = ((OAJspComponent)te).getForm();
                if (f != this.form) {
                    ((OAJspComponent)te).setForm(getForm());
                }
            }
        }

        lastAjaxSent = null;
        submitUpdateScript = null;
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('form').prepend(\"<input id='oahidden"+id+"' type='hidden' name='oahidden"+id+"' value=''>\");\n");
        if (width > 0 && height > 0) {
            sb.append("$('form').prepend(\"<input id='oahidden"+id+"Scroller' type='hidden' name='oahidden"+id+"Scroller' value=''>\");\n");
        }

//qqqqqqqqqqqqqqqqq        
        sb.append("function oatable"+id+"RowClick() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        sb.append("    if (v == null) return;\n");

        // dont submit if it is already the AO
        sb.append("    if ($(this).hasClass('oatableSelected')) {\n");
        sb.append("        if (!$(this).attr('href')) return;\n");
        sb.append("    }\n");
        sb.append("    $('#oahidden"+id+"').val(v);\n");
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
        }
        sb.append("}\n");
        
        if (pager != null) {
            sb.append("function oatable"+id+"PagerClick() {\n");
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
        
        // used by: $('table#oa"+id+" tr td a').click(oatable"+id+"Click1");
        sb.append("function oatable"+id+"Click1() {\n");
        sb.append("  var v = $(this).parents('tr').attr('oarow');\n");
        sb.append("  if (v == null) return;\n");
        sb.append("  $('#oahidden"+id+"').val(v);\n");

        sb.append("  if ($(this).attr('target').length > 0) {\n");
        sb.append("      return true;\n");
        sb.append("  }\n");
        sb.append("  if ($(this).attr('tabindex').length > 0) {\n");
        sb.append("      return true;\n");
        sb.append("  }\n");

        sb.append("  $('#oacommand').val('href='+$(this).attr('href'));\n"); 
        sb.append("  $('form').submit();\n");
        sb.append("  return false;\n");
        sb.append("}\n");

        if (hubSelect != null) {
            // used by: $('table#oa"+id+" tr td input:checkbox').click(oatable"+id+"Click2");
            
            sb.append("function oatable"+id+"Click2(event) {\n");
            sb.append("  event.stopPropagation();");
            sb.append("  return true;");
            sb.append("}");

            // used by: "$('table#oa"+id+" tr td:nth-child(2)').click(");
            sb.append("function oatable"+id+"Click3(event) {\n");
            // sb.append("        event.stopPropagation();");
            sb.append("  return false;");
            sb.append("}");
        }
        
        
        
        sb.append(getAjaxScript());


        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAjaxScript() {
        int x;

        Hub h = hub.getSharedHub();
        if (h != hubLast) {
            if (hubLast != null) {
                scrollLeft = scrollTop = 0;
                if (pager != null) {
                    pager.setCurrentPage(0);
                }
                if (hubSelect != null) hubSelect.clear();
            }
            hubLast = h;
        }
        
        int scrollAmt = (pager == null) ? hub.getSize() : pager.getScrollAmount();

        if (hub.getPos() != lastHubPos) {
            lastHubPos = hub.getPos();
            if (pager != null) {
                x = pager.getScrollAmount();
                x = lastHubPos < 1 ? 0 : (int) Math.floor(lastHubPos/((double)x));
                pager.setCurrentPage(x);
            }
        }
        
        int topRow = (pager == null) ? 0 : pager.getTopRow();
        
        if (submitUpdateScript != null) {
            String s = submitUpdateScript;
            submitUpdateScript = null;

            // check to see if editors are being used
            x = hub.getPos();
            if (x >= topRow && x < topRow+scrollAmt) {
                for (OATableColumn col : alColumns) {
                    OATableEditor ed = col.getEditor();
                    if (ed != null) {
                        s = null;
                        break;
                    }
                }
            }
            if (s != null) return s;
        }
        StringBuilder sb = new StringBuilder(2048);

        int colCount = alColumns.size();
        int colSpan = colCount + 1;
        if (hubSelect != null) {
            colSpan++;
        }

        if (width > 0 && height > 0) {
            if (pager != null) {
                if (pager.isTop() && pager.isBottom()) {
                    sb.append("<div id='"+id+"Scroller1' class='oatableScroller1a' style='width:"+width+"px;'>");
                }
                else if (pager.isBottom()) {
                    sb.append("<div id='"+id+"Scroller1' class='oatableScroller1b' style='width:"+width+"px;'>");
                }
                else { // top
                    sb.append("<div id='"+id+"Scroller1' class='oatableScroller1c' style='width:"+width+"px;'>");
                }
            }
            else {
                sb.append("<div id='"+id+"Scroller1' class='oatableScroller1' style='width:"+width+"px;'>");
            }
            sb.append("<div id='"+id+"Scroller' class='oatableScroller' style='width:"+width+"px; height:+"+height+"px;'>");
        }
        
        if (width > 0 && height > 0) {
            sb.append("<table id='oa"+id+"' class='oatable' style='width: 0px'>"); // must be fixed so heading & rows will align
        }
        else sb.append("<table id='oa"+id+"' class='oatable'>");
        
        
        // THEAD
        if (pager != null && pager.isTop()) {
            if (width > 0 && height > 0) {
                sb.append("<thead style='position:absolute; top:0;'><tr><td colspan='"+colSpan+"' class='oatablePager'>");
                sb.append(pager.getHtml());
                sb.append("</td></tr>");
            }
            else if (pager != null && pager.isTop()){
                sb.append("<thead><tr><td colspan='"+colSpan+"' class='oatablePager'>");
                sb.append(pager.getHtml());
                sb.append("</td></tr>");
            }
        }
        else if (width > 0 && height > 0) {
            sb.append("<thead style='position:absolute; top:0;'>");
        }
        else {
            sb.append("<thead>");
        }
        
        sb.append("<tr><th class='oatableColumnCount' style='width: "+getPxWidth(3)+"px;'><div style='width: "+getPxWidth(3)+"px;'>#</div></th>");
        
        if (hubSelect != null) {
            if (selectTitle != null) {
                x = selectTitle.length() + 1;
                sb.append("<th style='width:"+getPxWidth(x)+"px;'><div style='width:"+getPxWidth(x)+"px;'>"+selectTitle+"</div></th>");
            }
            else sb.append("<th style='width:"+getPxWidth(2)+"px;'><div style='width:"+getPxWidth(2)+"px;'> </div></th>");
        }
        
        for (OATableColumn col : alColumns) {
            String s = col.getHtml(hub, hub.getAO(), 0, true);
            if (s == null) s = "";
            
            sb.append("<th style='width:"+getPxWidth(col)+"px;'><div style='width:"+getPxWidth(col)+"px; overflow:hidden; white-space: nowrap;'>"+s+"</div></th>");
        }
        sb.append("</tr>");
        sb.append("</thead>");

        
        // TFOOT
        if (pager != null && pager.isBottom()) {
            if (width > 0 && height > 0) {
                sb.append("<tfoot style='position:absolute; bottom:0;'><tr><td colspan='"+colSpan+"' class='oatablePager'>");
                sb.append("<div style='width:"+width+"px; overflow: hidden;'>");            
                sb.append(pager.getHtml());
                sb.append("</div>");
                sb.append("</td></tr></tfoot>");
            }
            else {
                sb.append("<tfoot><tr><td colspan='"+colSpan+"' class='oatablePager'>");
                sb.append(pager.getHtml());
                sb.append("</td></tr></tfoot>");
            }
        }
        
        
        sb.append("<tbody>");
        
        for (int row=0; row < scrollAmt ;row++) {
            Object obj = hub.getAt(topRow+row);
            
            if (obj == null) {
                if (width > 0 && height > 0 && row > 0) break;
                sb.append("<tr class='oatableDisable'>");
                sb.append("<td style='width: "+getPxWidth(3)+"px;'><div style='width: "+getPxWidth(3)+"px;'> </div></td>");
                
                if (hubSelect != null) {
                    if (selectTitle != null) x = selectTitle.length() + 1;
                    else x = 2;
                    sb.append("<td style='width:"+getPxWidth(x)+"px;'><div style='width:"+getPxWidth(x)+"px;'> </div></td>");
                }
                
                for (int i=0; i<colCount; i++) {
                    OATableColumn col = alColumns.get(i);
                    
                    if (width > 0 && height > 0) {
                        sb.append("<td style='width:"+getPxWidth(col)+"px;'><div style='width:"+getPxWidth(col)+"px; overflow:hidden;'>&nbsp;</div></td>");
                    }
                    else {
                        sb.append("<td><div style='width:"+getPxWidth(col)+"px; overflow:hidden;'>&nbsp;</div></td>");
                    }
                }
                sb.append("</tr>");
                continue;
            }

            String s = "";
            if (obj == hub.getAO()) s = " class='oatableSelected'";
            s += " oarow='"+(topRow+row)+"'";
            sb.append("<tr"+s+"><td style='width: "+getPxWidth(3)+"px'><div style='width: "+getPxWidth(3)+"px'>"+(topRow+row+1)+"</div></td>");

            if (hubSelect != null) {
                if (selectTitle != null) x = selectTitle.length() + 1;
                else x = 2;
                sb.append("<td style='width:"+getPxWidth(x)+"px;'><div style='width:"+getPxWidth(x)+"px; overflow: hidden;'><input type='checkbox' name='table"+id+"check' value='row"+(topRow+row)+"'"+(hubSelect.contains(obj)?" checked=yes":"")+"></div></td>");                
            }
            
            for (OATableColumn col : alColumns) {
                boolean bComponent = false;
                s = null;
                // 20120917 get editor code
                if (obj == hub.getAO()) {
                    OATableEditor ed = col.getEditor();
                    if (ed != null) {
                        if (ed instanceof OAJspComponent) {
                            ((OAJspComponent)ed).reset();
                        }
                        s = ed.getTableEditorHtml();
                        bComponent = true;
                    }
                }
                if (s == null) s = col.getHtml(hub, obj, topRow+row, false);
                if (s == null) s = "&nbsp;";

                if (bComponent) sb.append("<td style='position:relative; width:"+getPxWidth(col)+"px;'><div style='width:"+getPxWidth(col)+"px; overflow:hidden;'>"+s+"</div></td>");
                else sb.append("<td style='width:"+getPxWidth(col)+"px;'><div style='width:"+getPxWidth(col)+"px; overflow:hidden; white-space: nowrap;'>"+s+"</div></td>");
            }
            sb.append("</tr>");
            
            /* qqqqq use key instead of row #
            OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
            Object[] objs = key.getObjectIds();
            if (objs != null && objs.length > 0) {
                ids += "_" + objs[0];
            }
            else {
                ids += "_"+key.getGuid();
            }
             */
        }
        sb.append("</tbody>");
        sb.append("</table>");

        if (width > 0 && height > 0) {
            sb.append("</div>");
            sb.append("</div>");
        }
        
        String strTable = sb.toString();
        strTable = Util.convert(strTable, "\\", "\\\\");
//20140130 this was commented out        
        strTable = Util.convert(strTable, "\r\n", "<br>");
        strTable = Util.convert(strTable, "\n", "<br>");
        strTable = Util.convert(strTable, "\r", "<br>");
//end        
        strTable = Util.convert(strTable, "'", "\\'");
        
//qqqq 20140130        
        // if txt is long, then split into multiple lines
        x = strTable.length();
        if (x > 100) {
            StringBuilder sbx = new StringBuilder(x+20);
            int amt = 80;
            for (int i=0; i<x; i+=amt) {
                if (i + amt >= x) {
                    amt = x - i;
                    sbx.append(strTable.substring(i, i+amt));
                    break;
                }
                if (strTable.charAt(i+amt-1) == '\\') amt--;
                sbx.append(strTable.substring(i, i+amt) + "\'+\n\'");
            }
            strTable = sbx.toString();
        }        
        
        sb = new StringBuilder(strTable.length() + 2048);
        sb.append("$('#"+id+"').html('"+strTable+"');\n");

        sb.append("$('table#oa"+id+" tbody tr').find('td:first').addClass('oatableColumnCount');\n");

        sb.append("$('table#oa"+id+" tbody tr:even').each(function(i) { if ($(this).attr('class') != 'oatableDisable') $(this).attr('class', 'oatableEven');});");
        sb.append("$('table#oa"+id+" tbody tr:odd').each(function(i) { if ($(this).attr('class') != 'oatableDisable') $(this).attr('class', 'oatableOdd');});");
        
        if (hub.getPos() >= 0) {
            sb.append("$('table#oa"+id+" tr:nth-child("+((hub.getPos()-topRow)+1)+")').attr('class', 'oatableSelected');\n");
        }

        sb.append("$('table#oa"+id+" tr').click(oatable"+id+"RowClick);\n");
        
        //20120915
        sb.append("$('table#oa"+id+" tr td a').click(oatable"+id+"Click1);\n");

        if (hubSelect != null) {
            sb.append("$('table#oa"+id+" tr td input:checkbox').click(oatable"+id+"Click2);");
            sb.append("$('table#oa"+id+" tr td:nth-child(2)').click(oatable"+id+"Click3);\n");
        }
        
        
        sb.append("$('#oa"+id+"').addClass('oaSubmit');\n");

        if (pager != null) {
            sb.append("$('table#oa"+id+" .oatablePager ul li').click(oatable"+id+"PagerClick);\n");
        }
        sb.append("$('#oahidden"+id+"').val('');"); // set back to blank
        if (width > 0 && height > 0) {
            sb.append("$('#oahidden"+id+"Scroller').val('');"); // set back to blank
        }
        
        
        // 20120917 get editor code
        int min = 0;
        int max = -1;
        if (pager != null) {
            min = pager.getTopRow();
            max = (min + pager.getScrollAmount()) - 1;;
        }
        int pos = hub.getPos();
        if ((max == -1 && pos >= 0) || (pos >= min && pos <= max)) {
            for (OATableColumn tc : alColumns) {
                OATableEditor te = tc.getEditor();
                if (te instanceof OAJspComponent) {
                    String s = ((OAJspComponent)te).getAjaxScript();
                    sb.append(s);
                }
            }
        }

        // capture scroll Positions
        if (width > 0 && height > 0) {
            sb.append("$('#"+id+"Scroller').scroll(function() {");
            sb.append("    $('#"+id+" thead').css('left', '-'+$(this).scrollLeft()+'px');");
            sb.append("    $('#oahidden"+id+"Scroller').val($(this).scrollLeft() +','+ $(this).scrollTop());\n");
            sb.append("});");    
        }
        
        if (width > 0 && height > 0) {
            // set unscrolling header and footer
            sb.append("$(document).ready(function() {");
            sb.append("    var h = $('#oa"+id+" thead').height();");
            sb.append("    if (h) $('div#"+id+"Scroller1').css('padding-top', h+'px');");
            sb.append("    h = $('#oa"+id+" tfoot').height();");
            sb.append("    if (h) $('div#"+id+"Scroller1').css('padding-bottom', h+'px');");
            sb.append("});");
        }
        
        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        
        if (js != null && (width > 0 && height > 0) && (scrollLeft != 0 || scrollTop != 0)) {
            sb.append("$('#"+id+"Scroller').scrollLeft("+scrollLeft+");");
            sb.append("$('#"+id+"Scroller').scrollTop("+scrollTop+");");
            js = sb.toString();
        }
        
        return js;
    }

    protected int getPxWidth(int columns) {
        return columns * charWidth;
    }
    protected int getPxWidth(OATableColumn col) {
        int x = col.getAverageCharWidth();
        if (x <= 0) x = this.charWidth;
        return col.getColumns() * x;
    }

    
    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return bEnabled && hub != null && hub.getAO() != null;
    }


    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return this.bVisible;
    }

    public void setScrollPosition(int left, int top) {
        this.scrollLeft = left;
        this.scrollTop = top;
    }
    public int getScrollTop() {
        return scrollTop;
    }
    public int getScrollLeft() {
        return scrollLeft;
    }
    
    public static void main(String[] args) {
        // if txt is long, then split into multiple lines
        String strTable = "this is \"\"\"\"\"\"\"a test\" \'\"a";
        strTable = Util.convert(strTable, "'", "\\'");
        
        int x = strTable.length();
        StringBuilder sbx = new StringBuilder(x+20);
        int amt = 4;
        for (int i=0; i<x; i+=amt) {
            if (i + amt >= x) {
                amt = x - i;
                sbx.append(strTable.substring(i, i+amt));
                break;
            }
            if (strTable.charAt(i+amt-1) == '\\') amt--;
            sbx.append(strTable.substring(i, i+amt) + "\'+\n\'");
        }
        strTable = sbx.toString();
        
        String s = ("$('#id').html('"+strTable+"');");
        System.out.println("==>"+s);
    }
}
