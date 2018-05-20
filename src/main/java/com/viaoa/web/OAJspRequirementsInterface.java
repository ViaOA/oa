package com.viaoa.web;

public interface OAJspRequirementsInterface {

    
    /**
     * names of CSS to include on page
     * see OAJspDelegate#registerRequiredCss(String, String) 
     */
    public String[] getRequiredCssNames();
    
    
    
    /**
     * names of JS to include on page
     * see OAJspDelegate#registerRequiredCss(String, String) 
     */
    public String[] getRequiredJsNames();
}
