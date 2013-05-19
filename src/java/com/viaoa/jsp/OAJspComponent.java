package com.viaoa.jsp;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface used for implementing JSP controls.
 * @author vvia
 *
 */
public interface OAJspComponent extends java.io.Serializable{

    boolean isChanged();
    String getId();
    void reset();
    
    void setForm(OAForm form);
    OAForm getForm();
    
    /** returns true to continue, false to not process the request */
    boolean _beforeSubmit();
    
    /** returns true if this component caused the form submit */
    boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp);

    /** return forward url */
    String _afterSubmit(String forwardUrl);
    
    String getScript();    // to initialize the html page
    String getVerifyScript();  // called on client before submit 
    String getAjaxScript();  // to update the page on an ajax update
    
    void setEnabled(boolean b);
    boolean getEnabled();
    
    void setVisible(boolean b);
    boolean getVisible();

    /** used for anonymous subclasses to customize submit */
    String onSubmit(String forwardUrl);
}
