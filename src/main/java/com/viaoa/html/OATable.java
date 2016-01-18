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

/******
bottom row ....

<tr>
    <td colspan="99">
        <table width="100%" border="0" <%= table.getRowColor(row+100)%>>
            <tr>
                <TD ALIGN="LEFT" width="30%" rowspan="2" valign="middle">
                    <input type="image" name="<%=table.getPreviousCommand().getName()%>" <%=table.getPreviousCommand().getSourceTag()%> ALT="Previous" border="0">
                </TD>
                <td align="CENTER" width="35%" nowrap>
                    <input type="image" name="<%=table.getCommand("cmdNew").getName()%>" <%=table.getCommand("cmdNew").getSourceTag()%> src="images/new.gif" ALT="New" border="0">
                </td>
                <TD ALIGN="right" width="30%" rowspan="2" valign="middle">
                    <input type="image" name="<%=table.getNextCommand().getName()%>" <%=table.getNextCommand().getSourceTag()%>  ALT="Next" border="0">
                </td>
            </TR>
            <tr>
                <td align="CENTER">
                    <nobr><small><b>
                        <%= table.getCount()%>
                        &nbsp;&nbsp;&nbsp;
                        Page <%=table.getPageLinks(table.getHub().getCount(), 10)%>
                    </b></small></nobr>
                </td>
            </tr>
        </table>
    </td>
</tr>



******/

package com.viaoa.html;

import java.util.*;
import java.lang.reflect.*;

import com.viaoa.hub.*;
import com.viaoa.ds.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;


/**<pre>
    [Java Code]
    OATable table = new OATable(hubEducation);
    table.setScrollAmount(5);
    table.setSelected(true);
    
    OACommand cmd = new OACommand("select.gif");
    table.addRowCommand("cmdSelect", cmd);

    cmd = new OACommand("next.gif",OACommand.NEXT);
    cmd.setInvisible("invisible.gif");
    table.addRowCommand("cmdNext", cmd);
    cmd = new OACommand("next.gif",OACommand.PREVIOUS);
    cmd.setInvisible("invisible.gif");
    table.addRowCommand("cmdPrevious", cmd);


    OATableColumn tc = new OATableColumn("educationid");
    table.add(tc);
        
    tc = new OATableColumn(new String[] {"educationId","name"});
    table.add(tc);
    form.add("tabEducation", table);

    ...

    [HTML Code]
    &lt;table border="0"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;tr&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td valign="top"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;th align="LEFT" valign="TOP" width=80&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Type
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/th&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;th align="LEFT" valign="TOP" width=176&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Address
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/th&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;/tr&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;% 
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;table = form.getTable("tabAddress");
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rowCount = table.getScrollAmount();
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for (row=0; row&lt;rowCount; row++) { 
    &nbsp;&nbsp;&nbsp;&nbsp;%&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;tr &lt;%= table.getRowColor(row) %&gt; &gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td align="LEFT" valign="TOP"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;input type="image" name="&lt;%=table.getRowCommand("cmdSelect",row).getName()%&gt;" &lt;%=table.getRowCommand("cmdSelect",row).getSource()%&gt; src="select.gif" ALT="Select" border="0"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td align="LEFT" valign="TOP" width="80"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;%= table.getCellValue(row,0) %&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td align="LEFT" valign="TOP" width="176"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;%= table.getCellValue(row,1) %&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/tr&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;% } %&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;tr&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td colspan="99"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;table width="100%"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;TR&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;TD ALIGN="LEFT"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;input type="image" name="&lt;%=table.getCommand("cmdPrevious").getName()%&gt;" &lt;%=table.getCommand("cmdPrevious").getSource()%&gt; src="tablePrevious.gif" ALT="Previous" border="0"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/TD&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td align="CENTER"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;input type="image" name="&lt;%=table.getCommand("other").getName()%&gt;" &lt;%=table.getCommand("other").getSource()%&gt; src="other.gif" ALT="Other" border="0"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;td align="RIGHT"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;input type="image" name="&lt;%=table.getCommand("cmdNext").getName()%&gt;" &lt;%=table.getCommand("cmdNext").getSource()%&gt; src="tableNext.gif" ALT="Next" border="0"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/TR&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/table width="100%"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/td colspan="99"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;/tr&gt;
    &lt;/table&gt;

    </pre>
*/
public class OATable extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Vector columns = new Vector(3,3);
    int scrollAmount = 0;
    int topRow = 0;
    boolean bSelect;  // should the row of the activeObject be highlighted
    public Hashtable hashRowCommand = new Hashtable(11);
    protected Vector vecRowCommand = new Vector(3,3);
    public Hashtable hashRowLink = new Hashtable(7);
    public Hashtable hashCommand = new Hashtable(11);
    protected Object lastMasterObject;
    protected Object lastActiveObject;
    protected int lastChangeCount, newListCount;
    
    // these are used to know when objects in hub change
    protected Object activeObject;
    
    String colorEven = "#dddddd";  // even lines
    String colorOdd = "#eeeeee";  // odd lines
    String colorSelected = "#FFFF40";// "#ffffcc";   selected line

    /** @param name unique name of table (within the form it is in), can not have '.' */
    public OATable(Hub hub) {
        setHub(hub);
    }

    /** add a command button to table
        @see OACommand.TABLENEXT
        @see OACommand.TABLEPREVIOUS
        @param name is not case sensitive
    */
    public void addCommand(String name, OACommand cmd) {
        if (name == null || cmd == null) return;
        hashCommand.put(name.toUpperCase(), cmd);
        cmd.name = name;
    }
    /** Calls the commands setHub() and setTable(). 
        if command type is TABLENEXT or TABLEPREVIOUS, then cmd.setVisible() will be set.
        @param name is not case sensitive
    */
    public OACommand getCommand(String name) {
        return getCommand(name, true);
    }
    protected OACommand getCommand(String name, boolean bFlag) {
        initialize();
        OACommand cmd = null;
        if (name != null) cmd = (OACommand) hashCommand.get(name.toUpperCase());
        if (cmd == null) {
            if (!bFlag) return null;
            throw new RuntimeException("OATable.getCommand() Command \""+name+"\" not found");
        }

        cmd.table = this;
        cmd.form = this.form;
        cmd.setHub(hub);

        if (cmd.command == OACommand.TABLENEXT) {
            boolean b = false;
            if (hub != null) {
                if (hub.elementAt(topRow + scrollAmount) != null) b = true;
            }
            cmd.setVisible(b);
        }
        else if (cmd.command == OACommand.TABLEPREVIOUS) {
            if (topRow > 0) cmd.setVisible(true);
            else cmd.setVisible(false);
        }
        
        return cmd;
    }

    /** returns built in command "cmdNext" with built in settings: 
        <BR><PRE>
            cmd = new OACommand(OACommand.NEXT);
            cmd.setImageName("tableNext.gif");
            cmd.setInvisibleImageName("invisible.gif");
            this.addCommand("cmdNext", cmd);
        </PRE>
    */
    public OACommand getNextCommand() {
        String name = "cmdNext".toUpperCase();
        OACommand cmd;
        if (hashCommand.get(name) == null) {
            cmd = new OACommand(OACommand.NEXT);
            cmd.setImageName("tableNext.gif");
            cmd.setInvisibleImageName("invisible.gif");
            this.addCommand(name, cmd);
        }
        cmd = getCommand(name);
        return cmd;
    }
    /** returns built in command "cmdPrevious" with built in settings: 
        <BR><PRE>
            cmd = new OACommand(OACommand.PREVIOUS);
            cmd.setImageName("tablePrevious.gif");
            cmd.setInvisibleImageName("invisible.gif");
            this.addCommand("cmdPrevious", cmd);
        </PRE>
    */
    public OACommand getPreviousCommand() {
        String name = "cmdPrevious".toUpperCase();
        OACommand cmd;
        if (hashCommand.get(name) == null) {
            cmd = new OACommand(OACommand.PREVIOUS);
            cmd.setImageName("tablePrevious.gif");
            cmd.setInvisibleImageName("invisible.gif");
            this.addCommand(name, cmd);
        }
        cmd = getCommand(name);
        return cmd;
    }

    /**
        @param name is not case sensitive
    */
    public void addRowCommand(String name, OACommand cmd) {
        if (cmd != null && name != null) {
            hashRowCommand.put(name.toUpperCase(),cmd);
            vecRowCommand.add(name);
            cmd.name = name;
        }
    }

    /**
        @param name is not case sensitive
    */
    public void addRowLink(String name, OALink lnk) {
        if (name != null && lnk != null) {
            hashRowLink.put(name.toUpperCase(),lnk);
            lnk.name = name;
        }
    }

    /** add a command button to every row in table. 
        The following will be automatically done for the command:
        <pre>   
            cmd.form = this.form;
            cmd.setHub(hub);
            cmd.setObject((OAObject)obj);
            cmd.setActiveObject(true);
            cmd.setEnabled(obj!=null);
            cmd.setVisible(obj!=null);
        </pre>
        @param name is not case sensitive
    */
    public OACommand getRowCommand(String name, int row) {
        initialize();
        OACommand cmd = null;
        if (name != null) cmd = (OACommand) hashRowCommand.get(name.toUpperCase());
        if (cmd == null) {
            throw new RuntimeException("OATable.getRowCommand() Command \""+name+"\" not found");
        }
        setupRowCommand(cmd,row);
        return cmd;
    }
    
    protected void setupRowCommand(OALink cmd, int row) {
        cmd.form = this.form;
        Object obj;
        if (hub != null) obj = hub.elementAt(topRow + row);
        else obj = null;
        cmd.table = this;
        cmd.setHub(hub);
        cmd.setObject((OAObject)obj);
        cmd.setActiveObject(true);
        cmd.bTableEnabled = (obj!=null);
        cmd.bTableVisible = (obj!=null);
    }


    /**
        @param name is not case sensitive
    */
    public OACommand getRowCommand(String name) {
        return getRowCommand(name, true);
    }
    protected OACommand getRowCommand(String name, boolean bFlag) {
        initialize();
        OACommand cmd = null;
        if (name != null) cmd = (OACommand) hashRowCommand.get(name.toUpperCase());
        if (cmd == null) {
            if (!bFlag) return null;
            throw new RuntimeException("OATable.getRowCommand() Command \""+name+"\" not found");
        }
        return cmd;
    }

    /**
        @param name is not case sensitive
    */
    public OALink getRowLink(String name, int row) {
        initialize();
        OALink lnk = null;
        if (name != null) lnk = (OALink) hashRowLink.get(name.toUpperCase());
        if (lnk == null) throw new RuntimeException("OATable.getRowLink() Link \""+name+"\" not found");
        
        setupRowCommand(lnk,row);
        return lnk;
    }
    /**
        @param name is not case sensitive
    */
    public OALink getRowLink(String name) {
        return getRowLink(name, true);
    }
    public OALink getRowLink(String name, boolean bFlag) {
        initialize();
        OALink lnk = null;
        if (name != null) lnk = (OALink) hashRowLink.get(name.toUpperCase());
        if (lnk == null) {
            if (!bFlag) return null;
            throw new RuntimeException("OATable.getRowLink() Command \""+name+"\" not found");
        }
        return lnk;
    }


    /** color to use for even numbered rows */
    public void setEvenColor(String s) {
        colorEven = s;
    }
    /** color to use for odd numbered rows */
    public void setOddColor(String s) {
        colorOdd = s;
    }
    /** color to use for selected row */
    public void setSelectedColor(String s) {
        colorSelected = s;
    }


    /** if set to true, then the row of the active object is highlighted 
        default: false
    */
    public boolean getSelected() {
        return bSelect;
    }
    public void setSelected(boolean b) {
        bSelect = b;
    }
    

    public void scrollUp() {
        setTopRow(topRow - scrollAmount);
    }
    public void scrollDown() {
        setTopRow(topRow + scrollAmount);
    }
    public int getTopRow() {
        initialize();  // see if top row needs to be adjusted (in case active obj was changed, etc.)
        return topRow;
    }
    boolean bTopRowSet;
    public void setTopRow(int tr) {
        bTopRowSet = true;
        this.topRow = tr;
        if (hub == null) {
            topRow = 0;
        }
        else {
            if (topRow >= 0 && hub.elementAt(topRow) == null) {
                topRow = hub.getSize()-1;
            }
        }
        if (topRow < 0) topRow = 0;
    }
    

    protected void resetHubOrProperty() {
        super.resetHubOrProperty();
        int x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            tc.resetMethods();
        }
    }
    

    /** number of rows that are displayed in table.  Set to 0 to turn scrolling off. */
    public void setScrollAmount(int amt) {
        this.scrollAmount = amt;
        if (amt == 0) setTopRow(0);
    }
    public int getScrollAmount() {
        return scrollAmount;
    }

    
    public void addColumn(OATableColumn tc) {
        insertColumn(tc,-1);
    }
    public void add(OATableColumn tc) {
        insertColumn(tc,-1);
    }
    

    
    public void add(String propertyPath) {
        addColumn(new OATableColumn(propertyPath));
    }

//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvqqqqqqqqqqq NEW
    public void add(OAHtmlComponent comp) {
        addColumn(new OATableColumn(comp));
    }
    public void add(OAHtmlComponent comp, String heading) {
        addColumn(new OATableColumn(comp,heading));
    }
    
    /** @parma htmlColumn html within the TD tags 
    */
    public void add(OAHtmlComponent comp, String heading, String htmlColumn) {
        OATableColumn tc = new OATableColumn(comp,heading);
        tc.setHtmlBetween(htmlColumn);
        addColumn(tc);
    }
    /** @parma htmlColumn html within the TD tags 
        @param htmlBefore / After code around column data
    */
    public void add(OAHtmlComponent comp, String heading, String htmlColumn, String htmlBefore, String htmlAfter) {
        OATableColumn tc = new OATableColumn(comp,heading);
        tc.setHtmlBetween(htmlColumn);
        tc.setHtmlBefore(htmlBefore);
        tc.setHtmlAfter(htmlAfter);
        addColumn(tc);
    }
    
    
    public void insert(OATableColumn tc, int index) {
        insertColumn(tc,index);
    }
    public void insertColumn(OATableColumn tc, int index) {
        if (index == -1) columns.addElement(tc);
        else columns.insertElementAt(tc, index );
    }
    
    public void removeColumn(int pos) {
        columns.removeElementAt(pos);
    }

    public OATableColumn getColumn(int pos) {
        return (OATableColumn) columns.elementAt(pos);
    }


    /************************** OAHtmlComponent ************************/
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
    }
    /** called by OAForm.processRequest() */
    protected String processCommand(OASession session, OAForm form, String command) {
        String s = com.viaoa.html.Util.field(command,'_',5,1);

        if (s == null || s.length() == 0) {
            // goto page from table.getPageLinks()
            //   oacommand_10_tabAddress_3
            s = com.viaoa.html.Util.field(command,'_',4,1);
            try {
                int i = Integer.parseInt(s);
                setTopRow((i-1) * scrollAmount);
            }
            catch (Exception e) {
            }
            return null;
        }
        
        // oacommand_10_tabAddress_7_cmdView527  ==> oacommand_7_cmdView527
        command = "oacommand_" + com.viaoa.html.Util.field(command,'_',4,999);

        // get command name
        int len = 0;
        try {
            len = Integer.parseInt(com.viaoa.html.Util.field(command,'_',2));
        }
        catch (Exception e) {
        }
        s = com.viaoa.html.Util.field(command,'_',3,99);
        if (len > 0) s = s.substring(0, len);


        OAHtmlComponent oh = getRowCommand(s,false); 
        if (oh == null) {
            oh = getRowLink(s, false);
            if (oh == null) {
                oh = getCommand(s, false);
                if (oh == null) return null;
            }
        }
        return oh.processCommand(session, form, command);
    }

    public OAHtmlComponent getComponent(int col) {
        OATableColumn tc = (OATableColumn) columns.elementAt(col);
        return tc.comp;
    }

    public String getValue(int row, int col) {
        return getCellValue(row,col);
    }
    public String getCellValue(int row, int col) {
        bNeedsRefreshed = false;
        bResetTop = false;
        initialize();
        if (col < 0 || col >= columns.size()) return "Error: invalid column#";

        // if (row >= scrollAmount) return "Error: row is greater then Table scrollAmount";
        Object obj;
        if (hub == null) obj = null;
        else obj = hub.elementAt(topRow + row);
        OATableColumn tc = (OATableColumn) columns.elementAt(col);

        if (row + topRow == hub.getPos() && tc.comp != null) {
            String n = this.name+"OA"+col;
            if (form.getComponent(n) == null) form.add(n, tc.comp);
            return tc.comp.getHtml();
        }


        String s = null;
        try {        
            s = tc.getValue(obj);
        }
        catch (Exception e) {
            handleException(e,"getCellValue("+row+","+col+")");
            return "Exception Occured";
        }

        if (s.length() == 0) s = "&nbsp;";
        else {
            if (tc.getPassword()) s = "******";
        }
        return s;
    }

    /** @returns string in format of "1-10 of 57" */
    public String getCount() {
        if (hub == null) return "no hub";
        int tot = hub.getSize();
        return getCount(tot," of "+tot);
    }
    /** @returns string in format of "1-10"+msg or "0 Listed" if no rows. */
    public String getCount(int total, String msg) {
        initialize();
        int lastRow = topRow + scrollAmount;
        
        if (lastRow > total) lastRow = total;
        
        String s;
        if (topRow == 0 && lastRow == 0) s = "0 Listed";
        else s = (topRow+1) + "-" + lastRow + msg;
        return s;
    }
    
    
    public String getPageLinks(int pages) {
        if (hub == null) return "";
        hub.loadAllData();
        return getPageLinks(hub.getSize(), pages);
    }
    
    /** @param totalObjects in table.
        @param pages number of page links to return
    */
    public String getPageLinks(int totalObjects, int pages) {
        initialize();
        if (scrollAmount == 0) return "";
        int currentPage = (topRow / scrollAmount) + 1;
        int totalPages = (int) Math.ceil(((double)totalObjects) / ((double)scrollAmount));
        if (totalPages == 0) totalPages = 1;

        int beginPage = currentPage - (pages/2);
        if (beginPage < 1) beginPage = 1;
        int endPage = beginPage + pages - 1;
        
        if (endPage > totalPages) endPage = totalPages;

        // readjust begin page
        beginPage = (endPage - pages) + 1;
        if (beginPage < 1) beginPage = 1;

        
        String line = "";
        for (int i = beginPage; i <= endPage; i++) {

            String s = "oacommand_" + name.length() + "_" + name + "_" + i;
//06/12/01            s = "javascript:document.forms[0].action = document.forms[0].action + '?"+s+"=1'; document.forms[0].submit();";
            s = "javascript:document.forms[0].action = 'oaform.jsp?"+s+"=1'; setOA(); document.forms[0].submit();";
            
            // oacommand_10_tabAddress_PAGE3
            
            String s2 = i+"";
            if (i == endPage && endPage < totalPages) s2 += "+";
            else if (i == beginPage && beginPage > 1) s2 = "-"+s2;
            
            s = "<A HREF=\""+s+"\">"+s2+"</A>&nbsp;";
            if (i == currentPage) s = i + "&nbsp;";
            line += s;
        }
        return line;
    }
    
    
    /** Returns a bgcolor set to line to colorEven, colorOdd or colorSelected; depending
        on the row that is requested.
        example: bgcolor="#FF00FF" 
    */
    public String getRowColor(int row) {
        bNeedsRefreshed = false;
        bResetTop = false;
        initialize();
        String color = ""; //
        if ( (row%2) == 0 ) color = colorEven;
        else color = colorOdd;
        
        if (hub != null && bSelect) {
            if (hub.getPos() == (topRow+row)) color = colorSelected;
        }
        return "bgcolor=\""+color+"\"";
    }

    protected void initialize() {
        boolean b = false;
        if (hub != null) { 
            if (HubDataDelegate.getChangeCount(hub) != lastChangeCount) {
                lastChangeCount = HubDataDelegate.getChangeCount(hub);
                bNeedsRefreshed = true;
            }
            if (lastActiveObject != hub.getActiveObject()) b = true;
            if (HubDetailDelegate.getMasterObject(hub) != lastMasterObject) b = true; 
/* 20160118 remove hub.data.datax.newListCount       
            if (HubDataDelegate.getNewListCount(hub) != newListCount) {
                newListCount = HubDataDelegate.getNewListCount(hub);
                b = true;
            }
*/            
        }
        
        if (b) {
            bNeedsRefreshed = true;
            lastActiveObject = hub.getActiveObject();
            lastMasterObject = HubDetailDelegate.getMasterObject(hub);
/* 20160118 remove hub.data.datax.newListCount       
            newListCount = HubDataDelegate.getNewListCount(hub);
*/
            if (!bTopRowSet && scrollAmount > 0) {
                int pos = Math.max(hub.getPos(),0);
                if (pos < topRow) {
                    topRow = pos - (scrollAmount-1);
                }
                else if (pos >= topRow + scrollAmount) {
                    topRow = pos - (scrollAmount-1);
                }
            }
            if (topRow < 0) topRow = 0;
        }
        bTopRowSet = false;
    }

    private boolean bNeedsRefreshed;
    public boolean needsRefreshed() {
        initialize();
        return bNeedsRefreshed;
    }
    private boolean bResetTop;
    /**  @return true if this component wants top of page reset back to 0. */
    public boolean resetTop() {
        initialize();
        return bResetTop;
    }

    /** @returns javascript needed for initialization. */
    public String getInitScript() {
        String line = "";
        Enumeration enumx = hashCommand.elements();
        while (enumx.hasMoreElements()) {
            OACommand cmd = (OACommand) enumx.nextElement();
            String s = cmd.getInitScript();
            if (s != null) line += s;
        }

        enumx = hashRowCommand.elements();
        while (enumx.hasMoreElements()) {
            OACommand cmd = (OACommand) enumx.nextElement();
            String s = cmd.getInitScript();
            if (s != null) line += s;
        }
        if (line.length() == 0) return null;
        return line;
    }

    public String getHtml(String htmlTags) {
        bNeedsRefreshed = false;
        bResetTop = false;
        initialize();
        
        String s = "";
        if (htmlBefore != null) s += htmlBefore;

        s += "<table";

        if ( htmlTags != null) s += " " + htmlTags;
        if ( htmlBetween != null) s += " " + htmlBetween;
        s += ">";

        for (int row=-1; scrollAmount < 1 || row < scrollAmount; row++) {
            Object obj = hub.elementAt(topRow + row);
            if (scrollAmount < 1 && obj == null) break;
    	    s += "<tr " + (row >= 0 ? getRowColor( row) : "") + ">";
                	    
            int x = columns.size();
            boolean bActive = row >= 0 && (hub.getPos() == topRow + row);
            for (int c=-1; c<x; c++) {
                if (c < 0) {  // row commands
                    int xx = vecRowCommand.size();
                    if (xx == 0) continue;
                    if (row < 0) s += "<TH>&nbsp;</TH>";
                    else {
                        s += "<TD>";
                        for (int rc=0; rc<xx; rc++) {
                            String n = (String) vecRowCommand.elementAt(rc);
                            OACommand cmd = getRowCommand(n, row);
                            s += cmd.getHtml();
                        }
                        s += "</TD>";
                    }
                }
                else {
                    OATableColumn tc = (OATableColumn) columns.elementAt(c);
                    String n = this.name+"OA"+c; // vvvvv replace with container ....
                    if (form.getComponent(n) == null) form.add(n, tc.comp);
                    s += tc.getHtml(hub, obj, (row < 0), bActive);
                }
            }
    	    s += "</tr>";
        }

        s += "<tr>";
        s += "<td colspan=\""+(columns.size()+1)+"\">";
        s += "<table border=\"0\" width=\"100%\">";
        s += " <TR>";
        s += "<TD ALIGN=\"LEFT\" width=\"10%\">";
        s += "" + getPreviousCommand().getHtml();
        s += "</TD>";
        s += "<td align=\"CENTER\">";
        s += "<nobr><small><b>";
        s += ""+ getCount();
        s += "&nbsp;&nbsp;&nbsp;";
        s += "Page " + getPageLinks(getHub().getSize(), 10);
        s += "</b></font></small></nobr>";
        s += "</td>";
        s += "<td align=\"RIGHT\" width=\"10%\">";
        s += "" + getNextCommand().getHtml();
        s += "</td>";
        s += "</TR>";
        s += "</table>";
        s += "</td>";
        s += "</tr>";

        s += "</table>";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }

}

