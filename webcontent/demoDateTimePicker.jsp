<%@page trimDirectiveWhitespaces="true"%>
<%@page language="java" errorPage="oaerror.jsp"%>
<%@page import="java.io.*,java.util.*, java.awt.*"%>
<%@page import="java.util.logging.*"%>

<%-- OA --%>
<%@page import="com.viaoa.object.*,com.viaoa.hub.*,com.viaoa.util.*,com.viaoa.jsp.*,com.viaoa.ds.*,com.viaoa.ds.jdbc.*,com.viaoa.jfc.image.*"%>

<%-- Model --%>
<%@page import="com.theice.udm.model.oa.*,com.theice.udm.model.oa.search.*"%>
<%@page import="com.theice.udm.model.*,com.theice.udm.model.search.*"%>

<%-- misc --%>
<%@page import="com.theice.udm.delegate.*"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%-- OAJSP hierarchy: System/Application/Session/Form --%>
<jsp:useBean id="oasystem" scope="application" class="com.viaoa.jsp.OASystem" />

<%!static Logger LOG = Logger.getLogger("oajsp");%>

<%
    String applicationId = "oajsp";
    OAApplication oaapplication = oasystem.getApplication(applicationId, application);
    OASession oasession = oaapplication.getSession(session);
    OAForm form = null;
%>

<!DOCTYPE html>
<html>

<HEAD>
<TITLE>New OA|JSP</TITLE>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META content="text/html; charset=iso-8859-1" http-equiv=Content-Type>

<link rel="stylesheet" href="css/oajsp.css">
<link rel="stylesheet" href="css/jquery-ui.css">
<link rel="stylesheet" href="css/timepicker.css">


<script type="text/javascript" language="javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery-ui.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.maskedinput-1.3.min.js"></script>
<script type="text/javascript" language="javascript" src="js/timepicker.js"></script>

<%
    String id = "formDemo";
    form = oasession.getForm(id);
    // form = null;    
    if (form == null) {
        form = new OAForm(id, "demo.jsp");
        oasession.addForm(form);

        Hub<User> hubUser = new Hub<User>(User.class);
        User user = new User();
        user.setLastLogin(new OADateTime());
        hubUser.add(user);
        hubUser.setPos(0);

        OATextField txt = new OATextField("txtFirstName", hubUser, User.PROPERTY_FirstName, 0, 35);
        form.add(txt);
        
        txt = new OATextField("txtLastName", hubUser, User.PROPERTY_LastName, 0, 35);
        form.add(txt);
        
        txt = new OATextField("txtLastLogin", hubUser, User.PROPERTY_LastLogin, 0, 35);
        txt.setFormat("MM/dd/yyyy hh:mm:ss a");
        // txt.setAjaxSubmit(true);
        // txt.setSubmit(true);
        form.add(txt);
    }
%>

<%-- get jquery getReady() code from OAForm ... this manages all of the binding with OA|JSP --%>
<%=form.getInitScript()%>
</HEAD>

<body>
    <h2>OA|JSP</h2>

    <form id="formDemo">

        User Name:
        <input autofocus id="txtFirstName" type="text" placeholder="First Name" value="" size="15" maxlength="35">
        <input id="txtLastName" type="text" placeholder="Last Name" value="" size="25" maxlength="35">
        <div>
        Last Login:
        <input id="txtLastLogin" type="text" value="" size="25" maxlength="30">
        </div>
        <button type="submit">Submit</button>
    </form>

</body>
</html>

