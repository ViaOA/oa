/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.html;

import java.util.Vector;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**

OATabbedPane tab = new OATabbedPane();
tab.add("Title");
tab.add("Title", "page.jsp");
tab.add("Title1", "page.jsp", "image.gif");
tab.add("Title1", "page.jsp", "image.gif", "selected.gif", "mouseOver.gif");
tab.setHtml("<font size=1>","","</font>");
form.add("tab", tab);

--- Html ----
<table width="100%" height="100%" background="tabBackground.gif" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            <% OATabbedPane tab = form.getTabbedPane("tab"); %>
            <%= tab.getHtml() %>
        </td>
    </tr>
    <tr height="100%">
        <td height="100%" width="100%" valign="top">
            <% switch(tab.getSelectedIndex()) { 
                   case 0:  
                       %>
                       HTML...
                       <%    
                       break;
                   case 1: 
                       %>
                       HTML...
                       <%    
                       break;
            %>
        </td>
    </tr>
</table>
*/
public class OATabbedPane extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected OAButtonGroup buttonGroup = new OAButtonGroup();
    protected Vector vec = new Vector(5,5);
    

    public void add(String title, OAToggleButton cmd) {
        buttonGroup.add(cmd);
        if (title == null) title = "";
        if (cmd == null) cmd = new OAToggleButton("invisible.gif");
        
        buttonGroup.add(cmd);
        OALink link = new OALink("nowhere");
        link.setText(title);
        link.setTargetCommand(cmd);  // so that link will "act" like a toggleButton
        vec.addElement( new Object[] {title,link});
    }

    public void add(OAToggleButton cmd) {
        this.add("", cmd);
    }

    public void add(String title, String imageName, String selectedImageName, String mouseOverImage) {
        OAToggleButton cmd = new OAToggleButton(imageName, selectedImageName, mouseOverImage);
        this.add(title, cmd);
    }
    public void add(String title, String imageName, String selectedImageName) {
        this.add(title, imageName,selectedImageName,null);
    }
    public void add(String title, String imageName) {
        this.add(title, imageName, null, null);
    }
    public void add(String title) {
        this.add(title, (OAToggleButton) null);
    }

    public int getSelectedIndex() {
        return buttonGroup.getSelectedIndex();
    }
    public void setSelectedIndex(int i) {
        buttonGroup.setSelectedIndex(i);
    }


    /** 
        htmlBefore, htmlAfter are used for each tab.        
        Example:
        OATabbedPane pane = new OATabbedPane();
        ---
        <table width="100%" height="100%" background="tabCenter.gif"><tr><td>
        <%= form.getTabbedPane("tabpane").getHtml() %> 
        <jsp:include page="form.getTabbedPane("tabpane").getSelectedUrl()" flush="true" />
        </td></tr></table>
        
    */
    public String getHtml(String htmlTags) {
        String s = "";


        s += "<table background=\"tabCenter.gif\"";
        if ( htmlTags != null) s += " " + htmlTags;
        s += ">";
        s += "<tr>";
        buttonGroup.getSelectedIndex();

        int x = vec.size();
        for (int i=0; i<x; i++) {
            OAToggleButton tog = (OAToggleButton) buttonGroup.vec.elementAt(i);
            if (tog.form == null) {
                String n = this.name+"OAT"+i; // vvvvv replace with container ....
                if (form.getComponent(n) == null) form.add(n, tog);
            }
            Object[] oo = (Object[]) vec.elementAt(i);
            String title = (String) oo[0];
            OALink link = (OALink) oo[1];
            if (link.form == null) {
                String n = this.name+"OAL"+i; // vvvvv replace with container ....
                if (form.getComponent(n) == null) form.add(n, link);
            }


            if (!tog.isSelected()) { 
                s += "<td>";
                s += "<img src=\"tabLeft.gif\">";
                s += "</td>";
                s += "<td>";
                if ( htmlBefore != null) s += " " + htmlBefore;
                s += "<nobr>&nbsp;";
                s += tog.getHtml();
                s += link.getHtml();
                s += "&nbsp;</nobr>";
                if ( htmlAfter != null) s += " " + htmlAfter;
                s += "</td>";
                s += "<td>";
                s += "<img src=\"tabRight.gif\">";
                s += "</td>";
            }
            else {
                s += "<td>";
                s += "<table background=\"tabCenterSelected.gif\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">";
                s += "<tr>";
                s += "<td>";
                s += "<img src=\"tabLeftSelected.gif\">";
                s += "</td>";
                s += "<td>";
                if ( htmlBefore != null) s += " " + htmlBefore;
                s += "<nobr>&nbsp;";
                s += tog.getHtml();
                s += "<font size=\"+1\"><b>"+link.getHtml()+"</b></font>";
                s += "&nbsp;</nobr>";
                if ( htmlAfter != null) s += " " + htmlAfter;
                s += "</td>";
                s += "<td>";
                s += "<img src=\"tabRightSelected.gif\">";
                s += "</td>";
                s += "</tr>";
                s += "</table>";
                s += "</td>";
            }
        }

        s += "<td width=\"100%\">";
        s += "<table background=\"tabEnd.gif\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">";
        s += "<tr>";
        s += "<td>";
        s += "<img src=\"tabLeftSelected.gif\">";
        s += "</td>";
        s += "<td>";
        s += "&nbsp;";
        s += "</td>";
        s += "</tr>";
        s += "</table>";

        s += "</td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }
    

}

