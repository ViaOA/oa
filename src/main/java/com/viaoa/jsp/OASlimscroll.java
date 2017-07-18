package com.viaoa.jsp;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * create a slimscrollbar around a component.
 * @author vvia
 */
public class OASlimscroll implements OAJspComponent, OAJspRequirementsInterface {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected OAForm form;
    protected String lastAjaxSent;
    
    protected String height;
    protected String width;


    
    public OASlimscroll(String id, String width, String height) {
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
    public boolean _beforeSubmit() {
        return true;
    }

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        return false;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
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
}
