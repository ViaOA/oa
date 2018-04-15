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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

/**
    Object used by one user session.
    @see OAApplication#createSession()
*/
public class OASession extends OABase {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OASession.class.getName());
    
    protected OAApplication application;

    protected transient WeakReference<HttpServletRequest> wrefRequest;  // set by oaform.jsp
    protected transient WeakReference<HttpServletResponse> wrefResponse;


    protected transient ArrayList<OAForm> alBreadcrumbForm = new ArrayList<OAForm>();
    protected transient final ArrayList<OAForm> alForm = new ArrayList<OAForm>();

    private TimeZone timeZone;

    private int jsLibrary = OAApplication.JSLibrary_JQueryUI; 
    
    /**
     * set the preferred js library to use. 
     * @param type see {@link OAApplication#JSLibrary_JQueryUI} {@link OAApplication#JSLibrary_Bootstrap}
     */
    public void setDefaultJsLibrary(int type) {
        this.jsLibrary = type;
    }
    public int getDefaultJsLibrary() {
        if (this.jsLibrary != 0 || getApplication() == null) return this.jsLibrary;
        return getApplication().getDefaultJsLibrary();
    }

    
    public OASession() {
    }

    public void removeAll() {
        super.removeAll();
        synchronized (alForm) {
            alForm.clear();
        }
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
    /**
     * 
     * @param id ex: "employee"
     * @param page ex: "employee.jsp"
     * @return
     */
    public OAForm createForm(String id, String page) {
        removeForm(getForm(id));
        OAForm f = new OAForm(id, page);
        addForm(f);
        return f;
    }
    public void addForm(OAForm form) {
        if (form == null) return;
        String id = form.getId();
        if (id == null) return;

        synchronized (alForm) {
            for (int i=0; i<alForm.size(); i++) {
                OAForm f = alForm.get(i);
                if (id.equalsIgnoreCase(f.getId())) {
                    LOG.fine("replacing form, id="+id+", from="+f+", to="+form);
                }
            }
        }
        
        removeForm(getForm(form.getId()));
        synchronized (alForm) {
            alForm.add(form);
        }
        form.setSession(this);
    }
    public OAForm getForm(String id) {
        if (id == null) return null;
        synchronized (alForm) {
            for (int i=0; i<alForm.size(); i++) {
                OAForm f = alForm.get(i);
                if (id.equalsIgnoreCase(f.getId())) return f;
            }
        }
        return null;
    }
    public void removeForm(OAForm form) {
        if (form != null) {
            synchronized (alForm) {
                alForm.remove(form);
            }
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


    /** For this to work oaheader.jsp needs to call setResponse() &amp; setRequest()*/
    public void putCookie(String name, String value) {
        setCookie(name,value);
    }
    public void putCookie(String name, int x) {
        setCookie(name,x+"");
    }
    /** For this to work oaheader.jsp needs to call setResponse() &amp; setRequest().
        Uses Servlet.Response to create a cookie.  MaxAge is set to max. */
    public void setCookie(String name, String value) {
        HttpServletResponse resp = getResponse();
        if (resp == null) return;
        Cookie c = new Cookie(name, value);
        c.setMaxAge(Integer.MAX_VALUE);
        resp.addCookie(c);
    }
    /** uses Servlet.Request to get a cookie.
        For this to work oaform.jsp &amp; oabeans.jsp need to call setResponse() &amp; setRequest()
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


    // javascript data sent from browser (see OAForm)
    private String browserDate;     // js Date.toString()    EEE MMM dd yyyy '00:00:00' 'GMT'Z '('z')'
    private int browserRawTzOffset;    // in minutes
    private boolean browserSupportsDST;
    private String browserAcceptLanguage;
    
    public void setBrowserInfo(String date, int rawTzOffset, boolean bSupportsDST, String acceptLanguage) {
        if (rawTzOffset != browserRawTzOffset || bSupportsDST != browserSupportsDST) { 
            timeZone = null;
        }
        this.browserDate = date;
        this.browserRawTzOffset = rawTzOffset;
        this.browserSupportsDST = bSupportsDST;
        this.browserAcceptLanguage = acceptLanguage;
    }
    
    private final ConcurrentHashMap<Integer, TimeZone> hmTzDst = new ConcurrentHashMap<>(); 
    private final ConcurrentHashMap<Integer, TimeZone> hmTzNoDst = new ConcurrentHashMap<>(); 
    
    public TimeZone getBrowserTimeZone() {
        if (timeZone != null) return timeZone;

        final int msRawTzOffset = -(browserRawTzOffset * 60 * 1000);
        
        if (browserSupportsDST) {
            timeZone = hmTzDst.get(msRawTzOffset);
        }
        else {
            timeZone = hmTzNoDst.get(msRawTzOffset);
        }
        if (timeZone != null) return timeZone;
        
        String[] ss = TimeZone.getAvailableIDs(msRawTzOffset);
        if (ss != null && ss.length > 0) {
            TimeZone tz = null;
            for (int i=0; ss != null && i < ss.length; i++) {
                tz = TimeZone.getTimeZone(ss[i]);
                if (tz.useDaylightTime() == browserSupportsDST) {
                    timeZone = tz;
                    break;
                }
                if (tz != null) timeZone = tz;  // fallback
            }
        }

        if (timeZone != null) {
            if (browserSupportsDST) {
                hmTzDst.put(msRawTzOffset, timeZone);
            }
            else {
                hmTzNoDst.put(msRawTzOffset, timeZone);
            }
        }
        return timeZone;
    }

    protected OADateTime dtCreated;
    protected String sessionId;
    protected OADateTime dtLastAccessed;
    protected int maxInactiveSeconds;
    
    public OADateTime getCreated() {
        return dtCreated;
    }
    public String getSessionId() {
        return sessionId;
    }
    public OADateTime getLastAccessed() {
        return dtLastAccessed;
    }
    public int getMaxInactiveSeconds() {
        return maxInactiveSeconds;
    }
    
    protected void update(HttpSession session) {
        long ts = session.getCreationTime();
        this.dtCreated = new OADateTime(ts);
        this.sessionId = session.getId();
        long tsLast = session.getLastAccessedTime();
        this.dtLastAccessed = new OADateTime(tsLast);
        this.maxInactiveSeconds = session.getMaxInactiveInterval();
    }
}
