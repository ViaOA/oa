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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viaoa.ds.OASelect;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAString;
import com.viaoa.util.OAXMLWriter;

/**
 * Get XML data from OA.
 * @author vincevia
 */
public class XMLServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;

    public XMLServlet(String packageName) {
        if (!OAString.isEmpty(packageName)) this.packageName = packageName + ".";
        else packageName = "";
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPut(req, resp);
    }
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doDelete(req, resp);
    }
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doHead(req, resp);
    }
    @Override
    protected void doOptions(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doOptions(arg0, arg1);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(req, resp);
    }
    @Override
    protected void doTrace(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doTrace(arg0, arg1);
    }
    @Override
    public String getServletName() {
        // TODO Auto-generated method stub
        return super.getServletName();
    }
    @Override
    public String getServletInfo() {
        // TODO Auto-generated method stub
        return super.getServletInfo();
    }
    
    // class, id, [prop]
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();

        String className = req.getParameter("c");
        if (className == null) className = req.getParameter("class");
        
        String id = req.getParameter("id");
        if (id == null) id = req.getParameter("id");

        String propName = req.getParameter("p");
        if (propName == null) {
            propName = req.getParameter("prop");
            if (propName == null) propName = req.getParameter("property");
        }

        LOG.fine(String.format("class=%s, id=%s, property=%s", className, id, propName));

        // Set content type
        resp.setContentType("text/xml"); 

        if (className == null || className.length() == 0) {
            LOG.fine("className is required");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (id == null || id.length() == 0) {
            LOG.fine("id is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Class c;
        try {
            c = Class.forName(packageName + className);
        }
        catch (ClassNotFoundException e) {
            LOG.fine("class not found, class=" + (packageName + className));
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
            LOG.fine("object not found, class=" + (packageName + className) + ", id=" + id);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Object newObject = obj;
        if (!OAString.isEmpty(propName)) {
            newObject = obj.getProperty(propName);
            if (newObject == null || ( !(newObject instanceof OAObject) && !(newObject instanceof Hub) ) ) {
                LOG.fine("object found, property is not an OAObject" + (packageName + className) + ", id=" + id+", property="+propName);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        StringWriter sw = new StringWriter(256);
        PrintWriter pw = new PrintWriter(sw);
        OAXMLWriter xw = new OAXMLWriter(pw) {
            @Override
            public int writeProperty(Object obj, String propertyName, Object value) {
                return (value instanceof Hub) ? OAXMLWriter.WRITE_NO : OAXMLWriter.WRITE_YES; 
            }
        };
        if (newObject instanceof Hub) xw.write( (Hub) newObject);
        else xw.write( (OAObject) newObject);
        xw.close();

        
        String result = sw.getBuffer().toString();
        
        // Set to expire far in the past.
        resp.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");
        
        
        // Open the file and output streams
        OutputStream out = resp.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] bs = result.getBytes();
        resp.setContentLength(bs.length);
        out.write(bs);

        out.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // check to see if a session has already been created for this user
        // don't create a new session yet.
        HttpSession session = request.getSession(false);
        super.service(request, response);
/*        
        String requestedPage = request.getParameter(Constants.REQUEST);
        if (session != null) {
            // retrieve authentication parameter from the session
            Boolean isAuthenticated = (Boolean) session.getValue(Constants.AUTHENTICATION);
            // if the user is not authenticated
            if (!isAuthenticated.booleanValue()) {
                // process the unauthenticated request
                unauthenticatedUser(response, requestedPage);
            }
        }
        else // the session does not exist
        {
            // therefore the user is not authenticated
            // process the unauthenticated request
            unauthenticatedUser(response, requestedPage);
        }
*/        
    }

}
