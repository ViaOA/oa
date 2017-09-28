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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.object.OAObject;

/**
 * Interface used for implementing JSP controls, to be contained by OAForm.  
 * OAForm handles the html form and interacts with the components.
 * @author vvia
 */
public interface OAJspComponent extends java.io.Serializable {

    boolean isChanged();
    String getId();
    void reset();
    
    void setForm(OAForm form);
    OAForm getForm();
    
    /**
     * Called by form.beforeSubmit for every jspcomponent   
     * @returns true to continue, false cancel the processing of the request 
     */
    boolean _beforeFormSubmitted();
    
    /** 
     * Called after _beforeFormSubmitted to find out which component called the submit.
     * 
     * returns true if this component caused the form submit 
     * @see #onSubmit(String) to have the code ran for the component that submitted the form.
     */
    boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hashNameValue);

    /** 
     * only called on the component that was responsible for the submit, and called before {@link #onSubmit(String)} 
     */
    public void _beforeOnSubmit();

    /** 
     * This is only called on the component that was responsible for the submit 
     */
    String _onSubmit(String forwardUrl);

    /**
     * Called by _onSubmit, to allow for subclassing.  
     * This is only called on the component that was responsible for the submit
     */
    String onSubmit(String forwardUrl);
    
    /** 
     * Called by form.processSubmit for every jspcomponent, after onSubmit is called.   
     * return forward url 
     */
    String _afterFormSubmitted(String forwardUrl);
    String afterFormSubmitted(String forwardUrl);
    
    String getScript();    // to initialize the html page
    String getVerifyScript();  // called on client before submit 
    String getAjaxScript();  // to update the page on an ajax update
    
    void setEnabled(boolean b);
    boolean getEnabled();
    
    void setVisible(boolean b);
    boolean getVisible();

    public String getForwardUrl();

    /**
     * Called by containers to get html to edit.
     */
    String getEditorHtml(OAObject obj);
    
    /**
     * Called by containers to get html to render a view only version.
     * This is used to display the non-activeObjects, instead of using the actual editor component.
     */
    String getRenderHtml(OAObject obj);

}
