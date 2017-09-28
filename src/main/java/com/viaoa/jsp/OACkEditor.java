package com.viaoa.jsp;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.object.OAObject;

/**
 * create qqqqqqqqqqqqqqqqqqqq
 * @author vvia
 */
public class OACkEditor implements OAJspComponent, OAJspRequirementsInterface {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected OAForm form;
    protected String lastAjaxSent;
    
    protected String height;
    protected String width;


    
    public OACkEditor(String id, String width, String height) {
        this.id = id;
        this.height = height;
        this.width = width;
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
    public boolean _beforeFormSubmitted() {
        return true;
    }

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        return false;
    }
    
    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
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
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("$('#"+id+"').slimscroll({\n");
        sb.append("    height: '"+getHeight()+"',\n");
        sb.append("    width: '"+getWidth()+"',\n");
        sb.append("    distance: '3px',\n");
        sb.append("    size: '9px'\n");
        sb.append("});\n");
        
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
        return null;
    }
    

    @Override
    public void setEnabled(boolean b) {
    }
    @Override
    public boolean getEnabled() {
        return true;
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
        al.add(OAJspDelegate.JS_jquery_slimscroll);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }
    public void setWidth(String width) {
        this.width = width;
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
}
