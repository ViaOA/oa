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
    
    /**
     * Called by form.processSubmit for every jspcomponent   
     *
     * @returns true to continue, false to not process the request 
     */
    boolean _beforeSubmit();
    
    /** 
     * Called after _beforeSubmit to find out which component called the submit.
     * 
     * returns true if this component caused the form submit 
     * @see #onSubmit(String) to have the code ran for the component that submitted the form.
     */
    boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hashNameValue);

    /** 
     * only called on the component that was responsible for the submit 
     * 
     */
    String onSubmit(String forwardUrl);
    
    
    /** 
     * Called by form.processSubmit for every jspcomponent, after onSubmit is called.   
     * return forward url 
     */
    String _afterSubmit(String forwardUrl);
    
    String getScript();    // to initialize the html page
    String getVerifyScript();  // called on client before submit 
    String getAjaxScript();  // to update the page on an ajax update
    
    void setEnabled(boolean b);
    boolean getEnabled();
    
    void setVisible(boolean b);
    boolean getVisible();

    public String getForwardUrl();
}
