/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.servlet;

import java.io.IOException;
import java.io.OutputStream;
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

/**
 * Get byte[] for PDF from an Object Property
 * 
 * 
 * get params: c|class, i|id, p|prop|property)
 * 
 * @author vincevia
 */
public class PdfServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;
    private String defaultPropertyName;
    private Class defaultClass;

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

        LOG.finer(String.format("class=%s, id=%s, property=%s", className, id, propName));

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
        
        resp.addHeader("Content-Disposition","attachment; filename="+className+"_"+id+".pdf");
        
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
