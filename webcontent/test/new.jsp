


oaBldr
    set root hub

scrolling images
    with pager




gohi5 
    image scroller
        using tablePager
        number of columns
        hub, image prop
        custom render
        forwardUrl, (use submit, setting hidden row)
    banner 
        image scroller
            use Madisons js
            hub of images
        turn on/off getitnow, inspire nomination

    toolbar
        submit
        forwardUrl
        
    menu        
        submit
        check if form data has been changed
            form: have other components use hidden isChanged
            confirm with user
                use yes/no dialog
        forwardUrl




jquery
    get selected row
    yes/no/cancel dialog


SearchModels:  need a nav from ModelDelegate object or from other known search obj link
       ex: Employee.program.location -> EmployeeSearch.location


oaBldr on Apple
  AwtUtilies
     splash
     other windows/frames

oa javadoc on website













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

<!DOCTYPE html>
<html>

<HEAD>
<TITLE>New OA|JSP</TITLE>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META content="text/html; charset=iso-8859-1" http-equiv=Content-Type>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>

<%
    String id = "formNew";
    form = oasession.getForm(id);
//form = null;    
    if (form == null) {
        form = new OAForm(id, "new.jsp") {
    public String onJspSubmit(OAJspComponent submitComponent, String forwardUrl) {
        return null;
    }
        };  
        oasession.addForm(form);
        
        Hub<Employee> hub = new Hub<Employee>(Employee.class);
        Employee emp = new Employee();
        hub.add(emp);
        hub.setPos(0);
        
        emp.setFirstName("FirstName");
        emp.setMiddleName("MiddleName");
        emp.setLastName("LastName");

        OATextField txt = new OATextField("txtFirstName", hub, Employee.PROPERTY_FirstName, 0, 25); 
        form.add(txt);        
        txt = new OATextField("txtLastName", hub, Employee.PROPERTY_LastName, 0, 35); 
        form.add(txt);
        
        OAButton but = new OAButton("cmdSubmit", hub) {
    public String onSubmit(String forwardUrl) {
        getForm().addScript("alert('cmdSubmit clicked')", true);
        return null;
    }
        };
        form.add(but);

        but = new OAButton("cmdAjaxSubmit", hub) {
    public String onSubmit(String forwardUrl) {
        getForm().addScript("alert('cmdAjaxSubmit clicked')");
        return null;
    }
        };
        but.setAutoSubmit(true);
        form.add(but);
        
        
        OAHtml lbl = new OAHtml("lblFullName", hub, Employee.PROPERTY_FullName, 35);
        lbl.setMinLineWidth(25);
        form.add(lbl);

        OATextArea txta = new OATextArea("txtaMiddleName", hub, Employee.PROPERTY_MiddleName, 15, 6, 25); 
        form.add(txta);        
        
        Hub<EmployeeType> hubEmployeeType = new Hub<EmployeeType>(EmployeeType.class);
        for (int i=0; i<3; i++) {
    EmployeeType et = new EmployeeType();
    et.setName("EmployeeType."+i);
    hubEmployeeType.add(et);
        }
        hubEmployeeType.setLinkHub(hub, Employee.PROPERTY_EmployeeType);
        
        OACombo cbo = new OACombo("cboEmployeeType", hubEmployeeType, EmployeeType.PROPERTY_Name, 35);
        // cbo.setRequired(true);
        form.add(cbo);        

        
        Hub<Employee> hubx = hub.getDetailHub(Employee.PROPERTY_Employees);
        for (int i=0; i<5; i++) {
    Employee empx = new Employee();
    empx.setLastName("subEmp."+i);
    emp.getEmployees().add(empx);
        }
        
        cbo = new OACombo("cboEmployees", hubx, Employee.PROPERTY_FullName, 25);
        cbo.setAutoSubmit(true);
        form.add(cbo);        
        txt = new OATextField("txtLastNameSub", hubx, Employee.PROPERTY_LastName, 0, 35); 
        form.add(txt);

        cbo = new OACombo("cboAllEmployees", hub, Employee.PROPERTY_FullName, 25);
        cbo.setRecursive(true);
        form.add(cbo);        

        OATable table = new OATable("table", hubx);
        form.add(table);
        
        
        // form.addInitScript("alert('welcome');");
    }
    // form.getTextField("txtFirstName").setVisible(true);
%> 



<style type="text/css">

/* OATable CSS ========================================== */
TABLE.oatable {
    border: green 1px solid;
    BACKGROUND-COLOR: white;
    color: black;
    cursor: default;
    font-size: 11pt;
}
TABLE.oatable TD {
    border-bottom: 3px;
    border-bottom-color: silver;
    border-bottom-style: solid;
}

TR.oatableSelected {
    BACKGROUND-COLOR: #FFC430; /* goldish */
}

TR.oatableOdd {
    BACKGROUND-COLOR: blue;
}
TR.oatableEven {
    BACKGROUND-COLOR: green;
}
TR.oatableOver {
    BACKGROUND-COLOR: silver;
    color: black;
    cursor: pointer;
}
TR.oaTableBottom {
}

TABLE.oatable TD,TH {
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 4px;
    padding-bottom: 4px;
}

TABLE.oatable TH {
    BACKGROUND-COLOR: gray;
    color: white;
    font-size: 11.5pt;
    border-left: 1px;
    border-left-color: silver;
    border-left-style: solid;
    padding-top: 10px;
    padding-bottom: 10px;
}
TABLE.oatable TD.oatableColumnCount {
    BACKGROUND-COLOR: gray;
    color: white;
}

<!--
qqqqqq --> /* Page Navigation bar ========================= */
    .oaTableNav {
    BACKGROUND-COLOR: #070E66; /* dark blue */
    color: #C0C0C0; /* silver */
    text-align: center;
    vertical-align: middle;
    font-size: 10pt;
}

.oaTableNav TD {
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 0px;
    padding-right: 0px;
}

.oaTableNav ul {
    display: inline;
}

.oaTableNav li {
    display: inline;
    padding-left: 2px;
    padding-right: 2px;
}

.oaTableNav a,.oaTableNav a:visited {
    text-decoration: none;
    BACKGROUND-COLOR: #070E66; /* dark blue */
    color: #C0C0C0; /* silver */
    border: 1px solid #C0C0C0; /* silver */
    padding: 0px 5px 0px 5px;
}

.oaTableNav a.oaTableNavCurrentPage {
    background-color: #FFC430; /* goldish */
    color: #FFF !important;
    font-weight: bold;
    cursor: default;
}

.oaTableNav a:hover {
    background-color: green;
    color: white;
}

.oaTableNav a.oaTableNavCurrentPage:hover {
    background-color: #FFC430; /* goldish */
}

.oaTableNav a.disablelink,.oaTableNav a.disablelink:hover {
    BACKGROUND-COLOR: #070E66; /* dark blue */
    cursor: default;
    color: #777777;
    border-color: #777777; /*#070E66;  dark blue */
    font-weight: normal !important;
}

.oaTableNav a.prevnext {
    font-weight: bold;
}

.oaTableNav LI.oaTableNavMsg {
    font-style: italic;
    font-size: 9.5pt;
    color: #FFC430; /* goldish */
}

.oaTableNav LI.oaTableNavPageText {
    font-size: 9.5pt;
}</style>


<%=%>

<script>
  $(document).ready(function() {
      // $("#cboEmployeeType").
  });
</script>


</HEAD>
<body>

<h2>OA|JSP</h2>

<form id="formNew">
<input type="hidden" name="oaTableName"  value="">
<input type="hidden" name="oaTableCommand" value="">

    First Name: <input id="txtFirstName" type="text" value="test" size="15" maxlength="3" >
    <br>
    Last Name: <input id="txtLastName" type="text" value="test" size="12" maxlength="3" >
    <br>
    Full Name: <span id="lblFullName" style="border:5px solid red;">XXXX</span>
    <br>
    TextArea: <textarea id="txtaMiddleName" cols='25' rows="8" wrap="hard">abc 123 xyz</textarea>    
    <br>
    Employee Type:
    <select id="cboEmployeeType">
        <option value="1"  selected="selected">Number one</option>
        <option value="2" >Number two</option>
        <option value="3" >none</option>
    </select>

    <br>
    Manager Employees:
    <select id="cboEmployees">
        <option value="1"  selected="selected">Number one</option>
        <option value="2" >Number two</option>
        <option value="3" >none</option>
    </select>
    <br>
    <nobr>&nsp;&nsp;&nsp;Last Name:</nobr> <input id="txtLastNameSub" type="text" value="test" size="12" maxlength="3" >
    <br>

    All Emps (recursive):
    <select id="cboAllEmployees">
        <option value="3" >none</option>
    </select>
    <br>

    <input id="cmdSubmit" type="submit" value="Submit">
    <input id="cmdAjaxSubmit" type="submit" value="Ajax Submit">
    
    <p>

    <div id="table">
        table will be placed here    
    </div>

</form>
 
</body>
</html>

