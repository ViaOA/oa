<%@page language="java" errorPage="oaerror.jsp"%>
<%@page import="java.io.*, java.util.*"%> 
<%@page import="java.util.logging.* "%>

<%-- OA --%>
<%@page import="com.viaoa.object.*, com.viaoa.hub.*, com.viaoa.util.*, com.viaoa.jsp.*, com.viaoa.ds.*, com.viaoa.ds.jdbc.*, com.viaoa.jfc.image.*"%>

<%-- Model --%>
<%@page import="com.tmgsc.hifivetest.model.oa.*,com.tmgsc.hifivetest.model.oa.search.*"%>
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

    if (!true) {
        System.out.println("oaajax.jsp ----------------- START -------------");
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
        form.processSubmit(oasession, request, response);
        String js = form.getAjaxScript();
        
        js = OAString.convert(js, "\r\n", " ");
        js = OAString.convert(js, "\n", " ");
        js = OAString.convert(js, "\r", " ");
        
        out.createPdf(js);
    }
    else {
        out.println("alert('form "+id+"not found')");        
    }
%>
