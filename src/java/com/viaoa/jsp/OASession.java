/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.jsp;

import java.lang.ref.WeakReference;
import java.util.*;
import javax.servlet.http.*;

/** 
    Object used by one user session.  
    @see OAApplication#createSession()
*/
public class OASession extends OABase {
    private static final long serialVersionUID = 1L;
    
    protected OAApplication application; 
    
    protected transient WeakReference<HttpServletRequest> wrefRequest;  // set by oaform.jsp
    protected transient WeakReference<HttpServletResponse> wrefResponse;


    protected transient ArrayList<OAForm> alBreadcrumbForm = new ArrayList<OAForm>();
    protected transient ArrayList<OAForm> alForm = new ArrayList<OAForm>();
    
    
    public OASession() {
    }

    public void removeAll() {
        super.removeAll();
        alForm.clear();
        alBreadcrumbForm.clear();
    }
    
    public void setApplication(OAApplication app) {
        this.application = app;
    }
    public OAApplication getApplication() {
        return application;
    }

    private ArrayList getBreadcrumbForms() {
        if (alBreadcrumbForm == null) {
            alBreadcrumbForm = new ArrayList<OAForm>(10);
        }
        return alBreadcrumbForm;
    }
    
    public void setLastBreadCrumbForm(OAForm f) {
    	int x = getBreadcrumbForms().indexOf(f);
        if (x < 0) alBreadcrumbForm.add(f);
        else {
	        while (alBreadcrumbForm.size() > (x+1)) {
	        	alBreadcrumbForm.remove(x+1);
	        }
        }
    }
    public void setBreadCrumbForms(OAForm f) {
        if (f == null) return;
        getBreadcrumbForms().clear();
        alBreadcrumbForm.add(f);
    }
    public void clearBreadCrumbForms() {
    	getBreadcrumbForms().clear();
    }
    
    public OAForm[] getBreadCrumbForms() {
    	return (OAForm[]) getBreadcrumbForms().toArray(new OAForm[getBreadcrumbForms().size()]);
    }
    
    
    public OAForm createForm(String id) {
        removeForm(getForm(id));
        OAForm f = new OAForm(id, null);
        addForm(f);
        return f;
    }
    public void addForm(OAForm form) {
        if (form == null) return;
        // System.out.println("OASession.addForm() should be changed to put(form) or putForm(form)");
        removeForm(getForm(form.getId()));
        alForm.add(form);
        form.setSession(this);
    }
    public OAForm getForm(String id) {
        if (id == null) return null;
        for (int i=0; i<alForm.size(); i++) {
            OAForm f = alForm.get(i);
            if (id.equalsIgnoreCase(f.getId())) return f;
        }
        return null;
    }
    public void removeForm(OAForm form) {
        if (form != null) {
            alForm.remove(form);
        }
    }

    public HttpServletRequest getRequest() {
        if (wrefRequest == null) return null;
        HttpServletRequest req = wrefRequest.get();
        return req;
    }
    /** set by oabeans.jsp */
    public void setRequest(HttpServletRequest r) {
        if (r == null) wrefRequest = null;
        else wrefRequest = new WeakReference<HttpServletRequest>(r);
    }

    /** set by oabeans.jsp */
    public HttpServletResponse getResponse() {
        if (wrefResponse == null) return null;
        HttpServletResponse res = wrefResponse.get();
        return res;
    }
    /** set by oabeans.jsp */
    public void setResponse(HttpServletResponse r) {
        if (r == null) wrefResponse = null;
        else wrefResponse = new WeakReference<HttpServletResponse>(r);
    }


    /** For this to work oaheader.jsp needs to call setResponse() & setRequest()*/
    public void putCookie(String name, String value) {
        setCookie(name,value);
    }
    public void putCookie(String name, int x) {
        setCookie(name,x+"");
    }
    /** For this to work oaheader.jsp needs to call setResponse() & setRequest().
        Uses Servlet.Response to create a cookie.  MaxAge is set to max. */
    public void setCookie(String name, String value) {
        HttpServletResponse resp = getResponse();
        if (resp == null) return;
        Cookie c = new Cookie(name, value);
        c.setMaxAge(Integer.MAX_VALUE);
        resp.addCookie(c);
    }
    /** uses Servlet.Request to get a cookie. 
        For this to work oaform.jsp & oabeans.jsp need to call setResponse() & setRequest()
    */
    public String getCookie(String name) {
        HttpServletRequest req = getRequest();
        if (req == null) return null;
        Cookie[] cookies = req.getCookies();
        for (int i=0; i<cookies.length; i++) {
            if (cookies[i].getName().equals(name)) return cookies[i].getValue();
        }
        return null;
    }

    /** same as calling request.getHeader("User-Agent") */
    public String getBrowserName() {
        HttpServletRequest req = getRequest();
        if (req == null) return null;
        return req.getHeader("User-Agent");
    }
}

