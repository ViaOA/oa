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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viaoa.ds.OASelect;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

/**
 * Get an image from an Object Property
 * 
 * similar to protocol handler:
 * com.viaoa.jfc.editor.html.protocol.classpath.Handler
 * 
 * Note: images were stored in byte[] property using
 * OAImageUtil.convertToBytes(), which uses *.jpg format
 * 
 * get params: c|class, i|id, p|prop|property, mw|maxw (max width), mh|maxh (max height)
 * 
 * 
 * @author vincevia
 */
public class ImageServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;
    private String defaultPropertyName;
    private Class defaultClass;

    public ImageServlet(String packageName, Class defaultClass, String defaultPropertyName) {
        if (!OAString.isEmpty(packageName)) this.packageName = packageName + ".";
        else packageName = "";
        this.defaultClass = defaultClass;
        this.defaultPropertyName = defaultPropertyName;
    }

    private ConcurrentHashMap<String, Integer> hm = new ConcurrentHashMap<String, Integer>();
    
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

        String maxW = req.getParameter("mw");
        if (maxW == null) maxW = req.getParameter("maxw");
        String maxH = req.getParameter("mh");
        if (maxH == null) maxH = req.getParameter("maxh");
        
        LOG.finer(String.format("class=%s, id=%s, property=%s, maxw=%s, maxh=%s", className, id, propName, maxW, maxH));
        String etag = String.format("%s.%s.%s.%s", className, id, maxW, maxH);

        /*
        Enumeration enumx = req.getHeaderNames();
        for ( ;enumx.hasMoreElements(); ) {
            String sx = (String) enumx.nextElement();
            System.out.println("==> "+sx+", val="+req.getHeader(sx));
        }
        
         example:
            ==> Cookie, val=JSESSIONID=151za46tedr1jmz6mj2l522cr
            ==> Host, val=localhost:8081
            ==> Accept, val=* /*
            ==> Accept-Charset, val=ISO-8859-1,utf-8;q=0.7,*;q=0.3
            ==> Accept-Language, val=en-US,en;q=0.8
            ==> Referer, val=http://localhost:8081/service-award-dvd.jsp
            ==> User-Agent, val=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31
            ==> Connection, val=keep-alive
            ==> Accept-Encoding, val=gzip,deflate,sdch
        */
    
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
            if (bs.length == 0) {
                LOG.fine("image is empty, from property" + (packageName + className) + ", id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        catch (Exception e) {
            LOG.fine("could not read image from property" + (packageName + className) + ", id=" + id + ", Exception=" + e);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // see if browser has image cached, and that it has not been changed
        Object objx = hm.get(etag);
        if (objx instanceof Integer) {
            int len = ((Integer) objx).intValue();
            if (len == bs.length) {
                String sx = req.getHeader("If-None-Match");
                if (sx != null && etag.equals(sx)) {
                    long ts = req.getDateHeader("If-Modified-Since");
                    OADateTime dt = new OADateTime(ts);
                    if (dt.addHours(24).after(new OADateTime())) {
                        resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                }
            }
        }
        hm.put(etag, bs.length);
        
        
        int maxw = maxW == null ? 0 : OAConv.toInt(maxW);
        int maxh = maxH == null ? 0 : OAConv.toInt(maxH);

        if (maxw > 0 || maxh > 0) {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
            bi = OAImageUtil.scaleDownToSize(bi, maxw, maxh);
            bs = OAImageUtil.convertToBytes(bi);
        }
        

        // Open the file and output streams
        OutputStream out = resp.getOutputStream();

        // 20120505
        String imageType = "jpeg";

        String idx = String.format("%s-%s-%s", className, id, propName);
        boolean bCheck = (hmImageAlphaFlag.get(idx) == null);
        if (bCheck) {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
            if (OAImageUtil.hasAlpha(bi)) {
                imageType = "png";
                bs = OAImageUtil.convertToPNG(bs);
            }
            else {
                imageType = "jpeg";
                hmImageAlphaFlag.put(idx, Boolean.TRUE);
            }
        }
        resp.setContentLength(bs.length);
        
        // String mimeType = sc.getMimeType("test."+imageType);
        
        // Set content type
        resp.setContentType("image/"+imageType); 
        
        
        resp.setDateHeader("Date", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());
        resp.setDateHeader("Expires", System.currentTimeMillis() + msOneDay);
        resp.setHeader("Cache-Control", "max-age=3600, must-revalidate");        
        resp.setHeader("Cache-Control", "private");        
        resp.setHeader("ETag", etag);
        
        // Copy the contents of the file to the output stream
        out.write(bs);

        out.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    // images that do not need to be converted to PNG
    private ConcurrentHashMap<String, Boolean> hmImageAlphaFlag = new ConcurrentHashMap<String, Boolean>();

    
    private final long msLastModified = (new Date()).getTime();
    private final long msOneDay = (1000 * 60 * 60 * 24);

    // NOTE: this does not seem to be called.  If needed, needs to match logic in doGet(..)
    // @Override
    protected void XXdoHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // response.sendError(HttpServletResponse.SC_NOT_FOUND);
        
        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if (ifModifiedSince != -1 && ifModifiedSince + 1000 > msLastModified) {
            String eTag = "fileName" + "_" + "1234" + "_" + msLastModified;
            response.setHeader("ETag", eTag); // Required in 304.
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        response.setDateHeader("Last-Modified", msLastModified);
        response.setDateHeader("Expires", System.currentTimeMillis() + msOneDay);
        
        
        super.doHead(request, response);
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
