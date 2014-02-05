package com.viaoa.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAString;

/**
 * servlet used to serve up JNLP files, and dynamically set the codebase data.
 * This works with JettyController and assumes that all jnlp files are in the "[root]/jnlp" directory
 * @author vvia
 */
public class JNLPServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private String appTitle;
    public JNLPServlet(String appTitle) {
        this.appTitle = appTitle;
    }
    public JNLPServlet() {
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        String s;

        // ex:   "http://localhost:8082/jnlp/template.jnlp"

        /*
        s = req.getQueryString(); // null,  would be anything past "?"
        s = req.getServletPath(); // "/jnlp/template.jnlp
        s = req.getContextPath(); // ""
        s = req.getLocalAddr();
        s = req.getLocalName();
        s = req.getMethod();     // "GET"
        s = req.getPathInfo();  // "/template.jnlp"
        s = req.getRemoteAddr();
        s = req.getRemoteHost();
        s = req.getServletPath(); // "/jnlp/template.jnlp"
        s = req.getScheme();   // "http"
        s = req.getPathInfo();  // null
        s = req.getPathTranslated(); // null
        s = getServletContext().getServerInfo();  // jetty/7.6.0.v20120127
        s = getServletContext().getServletContextName(); // "/"
        s = getServletContext().getMajorVersion()+"";  // "2"
        */

        String path = getServletContext().getContextPath(); // ""
        path = getServletContext().getRealPath(path);  // C:\Projects\java\Hifive\webcontent_final
        
        String serverPort = req.getServerName();  // "localhost"
        int x = req.getLocalPort();  // 8082
        if (x > 0) serverPort += ":" + x;
        
        String sURI = req.getRequestURI(); // "/jnlp/template.jnlp"
        String fname = (path + sURI);
        
        String jnlpDir = sURI;
        int pos = jnlpDir.lastIndexOf('/');
        if (pos > 0) jnlpDir = jnlpDir.substring(0, pos); 
        
        String txt;
        try {
            txt = OAFile.readTextFile(fname, 2048);
            
            // codebase="http://localhost:8081/jnlp"  <== replace server:port with actual server
            pos = txt.indexOf("codebase");
            if (pos >= 0) {
                pos = txt.indexOf("\"", pos);;
                if (pos >= 0) {
                    int epos = txt.indexOf("\"", pos+1);;
                    if (epos >= 0 ) {
                        txt = txt.substring(0, pos+1) + "http://" + (serverPort + jnlpDir) + txt.substring(epos);
                    }
                }
            }
            
            // <argument>JWSCLIENT</argument>            
            pos = txt.indexOf("JWSCLIENT");
            if (pos >= 0) {
                s = "ServerName="+req.getServerName()+"</argument><argument>";
                txt = txt.substring(0, pos) + s + txt.substring(pos);
            }      
            
            // <title>OABuilder</title>
            if (!OAString.isEmpty(appTitle)) {
                pos = txt.indexOf("<title>");
                if (pos >= 0) {
                    int epos = txt.indexOf("</title>", pos+1);;
                    if (epos >= 0 ) {
                        txt = txt.substring(0, pos) + "<title>"+ appTitle + txt.substring(epos);
                    }
                }
            }
            
            resp.setContentType("application/x-java-jnlp-file");
        }
        catch (Exception e) {
            txt = "<html><h3>exception</h3>jnlp file name="+fname+"<br> error=" + e.getMessage();
        }
        
        PrintWriter out = resp.getWriter();
        out.println(txt);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doGet(req, resp);
    }
}
