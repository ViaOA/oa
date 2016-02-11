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
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viaoa.ds.OASelect;
import com.viaoa.hub.Hub;
import com.viaoa.object.OACalcInfo;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.util.OAJsonWriter;
import com.viaoa.util.OAString;

/**
 * Get JSON data.
 * 
 * Example:
 * //localhost:8080/servlet/json?c=Site&query="environments.silos.servers.applications.applicationType.code like 'fix*'"
 * 
 * /json = context
 * c|class = name of class for search
 * query = object query
 * [ALL] = show all data, else hubs will only show id
 *
 * //localhost:8080/servlet/json?c=Site&id=2
 * id = pkey Id
 * 
 * @author vincevia
 */
public class JsonServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;

    public JsonServlet(String packageName) {
        if (!OAString.isEmpty(packageName)) this.packageName = packageName + ".";
        else packageName = "";
    }

    // class, id, [prop]
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();

        String className = null;
        String id = null;
        String propName = null;
        String query = null;
        boolean bSendAllData = false;
        
        LOG.finer(String.format("class=%s, id=%s, property=%s, query=%s", className, id, propName, query));

        boolean bDescribe = req.getParameterMap().containsKey("describe");
        String badParams = null;
        
        
        for (Object n : req.getParameterMap().keySet()) {
            String s = (String) n;
            if (s.equalsIgnoreCase("c")) className = req.getParameter(s);
            else if (s.equalsIgnoreCase("class")) className = req.getParameter(s);
            else if (s.equalsIgnoreCase("id")) id = req.getParameter(s);
            else if (s.equalsIgnoreCase("i")) id = req.getParameter(s);
            else if (s.equalsIgnoreCase("property")) propName = req.getParameter(s);
            else if (s.equalsIgnoreCase("property")) propName = req.getParameter(s);
            else if (s.equalsIgnoreCase("p")) propName = req.getParameter(s);
            else if (s.equalsIgnoreCase("all")) bSendAllData = true;
            else if (s.equalsIgnoreCase("query")) query = req.getParameter(s);
            else if (s.equalsIgnoreCase("q")) query = req.getParameter(s);
            else if (s.equalsIgnoreCase("describe")) bDescribe = true;
            else if (s.equalsIgnoreCase("desc")) bDescribe = true;
            else if (s.equalsIgnoreCase("d")) bDescribe = true;
            else {
                if (badParams == null) badParams = s;
                else badParams += ", "+s;
            }
        }

        if (badParams != null) {
            LOG.fine("badParams="+badParams);
            resp.getOutputStream().write(("bad params="+badParams).getBytes());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        if (className == null || className.length() == 0) {
            LOG.fine("className is required");
            resp.sendRedirect("/jsonHelp.html");
            //was: resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        
        // Set content type
        resp.setContentType("application/json");  // more generic:  "text/html"
        
        /*
        if (id == null || id.length() == 0) {
            LOG.fine("id is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        */
        

        Class c;
        try {
            c = Class.forName(packageName + className);
        }
        catch (ClassNotFoundException e) {
            LOG.fine("class not found, class=" + (packageName + className));
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        Object newObject = null;

        if (query != null && query.length() > 0) {
            if (query != null && query.length() > 1) {
                char ch = query.charAt(0);
                if ( (ch == '\'' || ch == '\"') && query.charAt(query.length()-1) == ch) {
                    query = query.substring(1, query.length()-1);
                }
            }
            Hub hub = new Hub(c);
            hub.select(query);
            newObject = hub;
        }
        else if (id == null || id.length() == 0) {
            if (!bDescribe) {
                ArrayList al = new ArrayList();
                OAObjectCacheDelegate.find(null, c, 500, al);
                newObject = new Hub();
                for (Object objx : al) {
                    ((Hub) newObject).add(objx);
                }
            }
        }
        else {
            newObject = OAObjectCacheDelegate.get(c, id);
            if (newObject == null) {
                OASelect sel = new OASelect(c);
                sel.select("ID = ?", new Object[] { id });
                newObject = sel.next();
                sel.cancel();
                
                if (newObject == null) {
                    LOG.fine("object not found, class=" + (packageName + className) + ", id=" + id);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            if (!OAString.isEmpty(propName)) {
                newObject = ((OAObject)newObject).getProperty(propName);
                if (newObject == null || ( !(newObject instanceof OAObject) && !(newObject instanceof Hub) ) ) {
                    LOG.fine("object found, property is not an OAObject" + (packageName + className) + ", id=" + id+", property="+propName);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }


        String result = "";
        final boolean bOnlySendId = !bSendAllData && !(newObject instanceof OAObject);

        if (newObject != null) {
            OAJsonWriter json = new OAJsonWriter() {
                @Override
                public boolean shouldIncludeProperty(Object obj, String propertyName, Object value, OALinkInfo li) {
                    String sx = getCurrentPath();                
                    if (!bOnlySendId && (sx == null || sx.length() == 0)) return true;  // send all for root object
     
                    if (bOnlySendId || li == null) {  // only send "Id"
                        return ("id".equalsIgnoreCase(propertyName));
                    }
                    return false;
                }
            };
            if (newObject instanceof Hub) result = json.write( (Hub) newObject);
            else result = json.write( (OAObject) newObject);
        }
        
        if (bDescribe) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            result += "{ \"class\": {\n";
            result += "  \"name\": "+oi.getForClass().getSimpleName()+",\n";
            result += "  \"displayName\":\""+oi.getDisplayName()+"\",\n";
            
            result += "  \"properties\": [\n";
            int cnt = 0;
            for (OAPropertyInfo pp : oi.getPropertyInfos()) {
                if (cnt++ > 0) result += ",\n";
                result += "    {\"name\": \""+pp.getName()+"\", ";
                result += "\"type\": \""+pp.getClassType().getSimpleName()+"\", ";
                result += "\"max\": \""+pp.getMaxLength()+"\", ";
                result += "\"displayName\": \""+OAString.toString(pp.getDisplayName())+"\", ";
                result += "\"displayLength\": \""+pp.getDisplayLength()+"\"}";
            }
            result += "\n  ],\n";

            result += "  \"calcProperties\": [\n";
            cnt = 0;
            for (OACalcInfo cp : oi.getCalcInfos()) {
                if (cnt++ > 0) result += ",\n";
                result += "    {\"name\": \""+cp.getName()+"\"}";
            }
            result += "\n  ],\n";
            
            cnt = 0;
            result += "  \"references\": [\n";
            for (OALinkInfo li : oi.getLinkInfos()) {
                if (cnt++ > 0) result += ",\n";
                result += "    {\"name\": \""+li.getName()+"\", ";
                result += "\"type\": \""+li.getToClass().getSimpleName()+"\", ";
                result += "\"referenceType\": \""+(li.getType()==li.MANY?"many":"one")+"\", ";
                result += "\"owned\": \""+li.getOwner()+"\", ";
                result += "\"recursive\": \""+li.getRecursive()+"\", ";
                result += "\"reverseName\": \""+li.getReverseName()+"\", ";
                result += "\"calculated\": \""+li.getCalculated()+"\"}";
            }
            result += "\n  ]\n";
            
            result += "}\n";
        }

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
