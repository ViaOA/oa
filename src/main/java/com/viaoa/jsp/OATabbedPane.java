package com.viaoa.jsp;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

public class OATabbedPane implements OAJspComponent, OAJspRequirementsInterface {

    private static final long serialVersionUID = 1L;

    protected Hub hub;
    protected String id;
    
    protected OAForm form;
    protected String lastAjaxSent;

    
    public OATabbedPane(String id) {
        this(id, null);
    }
    public OATabbedPane(String id, Hub hub) {
        this.id = id;
        this.hub = hub;
    }

    public Hub getHub() {
        return hub;
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
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted = (id != null && id.equals(s));

        if (hmNameValue != null) {
            String[] ss = hmNameValue.get("oahidden"+id);
            if (ss != null && ss.length > 0 && OAString.isInteger(ss[0])) {
                setIndex(OAConv.toInt(ss[0]));
            }
        }
        
        return bWasSubmitted;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        return forwardUrl;
    }

    private final ArrayList<TabInfo> alTabInfo = new ArrayList<>();
    
    private static class TabInfo {
        public String name, url;
    }
    
    /**
     * @param name to display
     * @param url name of page to use or '#'+elementId to use html of an element on the page.
     */
    public void addTab(String name, String url) {
        TabInfo ti = new TabInfo();
        ti.name = name;
        ti.url = url;
        alTabInfo.add(ti);
    }
    public void removeTab(int pos) {
        if (pos > 0 && pos < alTabInfo.size()) alTabInfo.remove(pos);
    }
    
    
    private int tabIndex;
    public int getIndex() {
        return tabIndex;
    }
    public void setIndex(int pos) {
        this.tabIndex = pos;
    }
    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("$('form').prepend(\"<input id='"+id+"Index' type='hidden' name='oahidden"+id+"' value='"+getIndex()+"'>\");\n");
        
        // <div id="tabTest" class="container">
        sb.append("$('#"+getId()+"').addClass('container');\n");

        sb.append("$('#"+getId()+"').html(\"");
        
        sb.append("<ul class='nav nav-tabs'>");
        
        int i = 0;
        for (TabInfo ti : alTabInfo) {
            sb.append("<li");
            // if (i == getIndex()) sb.append(" class='active'");
            sb.append("><a href='#' tabIndex='"+i+"'>"+ti.name+"</a></li>");
            i++;
        }
        sb.append("</ul>");
        
        sb.append("<div id='"+id+"Content'>content here</div>");
        
        sb.append("\");\n");

        
        for (TabInfo ti : alTabInfo) {
            if (ti.url != null && ti.url.length() > 0 && ti.url.charAt(0) == '#') {
                sb.append("$('"+ti.url+"').addClass('oaHide');\n");
            }
        }        
        
        sb.append("$('#"+id+" ul li a').click(function(e) { $('#"+id+"Index').val($(this).attr('tabIndex'));$('#oacommand').val('" + getId() + "');ajaxSubmit(); e.stopPropagation(); $(this).blur();return false; });\n");

        String s = getAjaxScript();
        if (s != null) sb.append(s);
        
        String js = sb.toString();
        
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);

        String s = "$('#oahidden"+id+"').val('"+getIndex()+"');";

        sb.append("$('#"+id+" ul li').removeClass('active');\n");
        sb.append("$('#"+id+" ul li:eq("+(getIndex())+")').addClass('active');\n"); 
        
        TabInfo ti = alTabInfo.get(getIndex());
        if (ti.url != null && ti.url.length() > 0 && ti.url.charAt(0) == '#') {
            sb.append("$('#"+id+"Content').html($('"+ti.url+"').html());\n");
        }
        else {
            sb.append("$('#"+id+"Content').load('"+ti.url+"');\n");
        }
        
        String js = sb.toString();
        return js;
    }

    
    @Override
    public void setEnabled(boolean b) {
    }

    @Override
    public boolean getEnabled() {
        return hub == null || hub.isValid();
    }

    @Override
    public void setVisible(boolean b) {
    }

    @Override
    public boolean getVisible() {
        return true;
    }

    @Override
    public String getForwardUrl() {
        return null;
    }


    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.JS_jquery);
        al.add(OAJspDelegate.JS_bootstrap);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.CSS_bootstrap);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }
}
