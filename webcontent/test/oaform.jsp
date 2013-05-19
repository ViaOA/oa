<%@page language="java" errorPage="oaerror.jsp"%>
<%@page import="java.io.*, java.util.*"%> 
<%@page import="java.util.logging.* "%>

<%-- OA --%>
<%@page import="com.viaoa.object.*, com.viaoa.hub.*, com.viaoa.util.*, com.viaoa.jsp.*, com.viaoa.ds.*, com.viaoa.ds.jdbc.*, com.viaoa.jfc.image.*"%>

<%-- Model --%>
<%@page import="com.tmgsc.hifive.model.oa.*, com.tmgsc.hifive.model.oa.search.*"%>
<%@page import="com.tmgsc.hifive.model.*, com.tmgsc.hifive.model.search.*"%>

<%-- misc --%>
<%@page import="com.tmgsc.hifive.delegate.*"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- OAJSP hierarchy: System/Application/Session/Form --%>
<jsp:useBean id="oasystem" scope="application" class="com.viaoa.jsp.OASystem" />

<%!
    static Logger LOG = Logger.getLogger("com.tmgsc.hifive.jsp");
%> 

<%
     String applicationId = "tmgsc.hifive.NEW";
     OAApplication oaapplication = oasystem.getApplication(applicationId, application);
     OASession oasession = oaapplication.getSession(session);
     OAForm form = null;
 %>


<%-- was 
    <%@ include file="include/jspHeader.jsp" %>
--%>

<%
    request.setCharacterEncoding("UTF-8");

    if (true) {
        System.out.println("oaform.jsp ----------------- START -------------");
        System.out.println("realPath ="+application.getRealPath("TEST"));
        System.out.println("servletPath ="+request.getServletPath());
        System.out.println("pathInfo ="+request.getPathInfo());
        System.out.println("pathTranslated ="+request.getPathTranslated());
        System.out.println("requestURI ="+request.getRequestURI());
        System.out.println("serverName ="+request.getServerName());
        System.out.println("serverPort ="+request.getServerPort());
        Enumeration enumx = request.getParameterNames();
        while ( enumx.hasMoreElements()) {
    String name = (String) enumx.nextElement();
    String[] values = request.getParameterValues(name);
    System.out.println( "--> name=" + name + "  value=" + ((values.length==0)?"":values[0]) );
        }
    }

    String forwardPage = null;
    String id = request.getParameter("oaform");
    form = null;
    if (id != null) form = oasession.getForm(id);
    if (form != null) {
        forwardPage = form.processSubmit(oasession, request, response);
        if (forwardPage == null) forwardPage = id;
        response.sendRedirect(forwardPage);
    }
    else {
        out.println("alert('hello from oaform.jsp')");        
/**qqqqqqqqqqq        
    	System.out.println("Form "+id+" not found");
        try {
    Thread.sleep(10000);            
        }
        catch (Exception e) {} 
        response.sendRedirect("login.jsp");
        // response.sendRedirect("http://www.gohi5.com");
**/        
    }
%>
