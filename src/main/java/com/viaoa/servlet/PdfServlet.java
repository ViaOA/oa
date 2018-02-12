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
package com.viaoa.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viaoa.ds.OASelect;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAString;

/*
 * Get byte[] for PDF from an Object Property
 * 
 * 
 * get params: c|class, i|id, p|prop|property, [fn|filename]
 *      ex: /servlet/pdf?c=com.viaoa.model.oa.Employee&i=23&p=bsImage&fn=certificate.pdf
 * 
 * get param if using result from addRequest:  r
 *      ex: /servlet/pdf?r=2fdae43fw
 *         after calling addRequest("c=com.viaoa.model.oa.Employee&i=23&p=bsImage&fn=certificate.pdf")
 *              which returned: "2fdae43fw"
 *
 * Note:  use #addRequest(..) to create an encode link.
 * 
 * @author vincevia
 */
public class PdfServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;
    private String defaultPropertyName;
    private Class defaultClass;

    private static final ConcurrentHashMap<String, RequestInfo> hmRandomRequestString = new ConcurrentHashMap<>();
    

    private static class RequestInfo {
        Class clazz;
        String id, property, fname;
    }
    
    /**
     * Create and register an encoded request string using the real request string.  
     * Note: this is only valid during the lifetime of the pdf servlet instance.
     * 

     * @return http request to use for the link
     */
    public static String createRandomRequestValue(Class clazz, String id, String property, String fname) {
        if (clazz == null) return null;
        String rand = OAString.createRandomString(14, 28, true, true, false);
        RequestInfo ri = new RequestInfo();
        ri.clazz = clazz;
        ri.id = id;
        ri.property = property;
        ri.fname = fname;
        hmRandomRequestString.put(rand, ri);
        return rand;
    }
    
    public PdfServlet(String packageName, Class defaultClass, String defaultPropertyName) {
        if (!OAString.isEmpty(packageName)) this.packageName = packageName + ".";
        else packageName = "";
        this.defaultClass = defaultClass;
        this.defaultPropertyName = defaultPropertyName;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();
        
        String className = req.getParameter("c");
        if (className == null) className = req.getParameter("class");

        String id = req.getParameter("i");
        if (id == null) id = req.getParameter("id");

        String propName = req.getParameter("p");
        if (propName == null) {
            propName = req.getParameter("prop");
            if (propName == null) propName = req.getParameter("property");
        }
        if (OAString.isEmpty(propName)) propName = defaultPropertyName;

        String fileName = req.getParameter("fn");
        if (fileName == null) {
            fileName = req.getParameter("filename");
        }

        String rand = req.getParameter("r");
        if (rand != null) {
            RequestInfo ri = hmRandomRequestString.remove(rand);
            if (ri != null) {
                className = ri.clazz.getName();
                id = ri.id;
                propName = ri.property;
                fileName = ri.fname;
            }
        }
        
        
        LOG.finer(String.format("class=%s, id=%s, property=%s, fileName=%s", className, id, propName, fileName));

        // String filename = sc.getRealPath("image.gif");

        if (className == null || className.length() == 0) {
            if (defaultClass == null) {
                LOG.fine("className is required");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (propName == null || propName.length() == 0) {
            LOG.fine("propertyName is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        propName = OAString.convert(propName, "/", null);
        if (id == null || id.length() == 0) {
            LOG.fine("id is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Class c;
        String cname = "";
        try {
            if (OAString.isEmpty(className) && defaultClass != null) {
                c = defaultClass;
                cname = c.getName();
            }
            else {
                if (className.indexOf('.') >= 0) cname = className;
                else cname = (packageName + className);

                c = Class.forName(cname);
            }
        }
        catch (ClassNotFoundException e) {
            LOG.fine("class not found, class=" + cname);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        OAObject obj;
        obj = OAObjectCacheDelegate.get(c, id);
        if (obj == null) {
            OASelect sel = new OASelect(c);
            sel.select("ID = ?", new Object[] { id });
            obj = (OAObject) sel.next();
            sel.cancel();
        }
        if (obj == null) {
            LOG.fine("objet not found, class=" + (packageName + className) + ", id=" + id);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] bs;
        try {
            bs = (byte[]) obj.getProperty(propName);
            if (bs == null) {
                LOG.fine("could not read image from property" + (packageName + className) + ", id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (bs == null || bs.length == 0) {
                LOG.fine("image is empty, from property" + (packageName + className) + ", id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        catch (Exception e) {
            LOG.fine("could not read pdf bytes from property" + (packageName + className) + ", id=" + id + ", Exception=" + e);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Open the file and output streams
        OutputStream out = resp.getOutputStream();

        resp.setContentLength(bs.length);
        
        // Set content type
        resp.setContentType("application/pdf"); 
        
        
        if (OAString.isEmpty(fileName)) fileName = className+"_"+id+".pdf";
        resp.addHeader("Content-Disposition","attachment; filename="+fileName);
        
        // Set to expire far in the past.
        resp.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");
        
        
        // Copy the contents of the file to the output stream
        out.write(bs);

        out.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // check to see if a session has already been created for this user
        // don't create a new session yet.
        HttpSession session = request.getSession(false);
        super.service(request, response);
    }

}
