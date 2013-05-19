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
import com.viaoa.object.*;
import com.viaoa.ds.*;
import com.viaoa.util.*;


public class OADataTable extends OAHtmlComponent {
   private static final long serialVersionUID = 1L;
   
   protected int scrollAmt;
   protected int currentPage;
   protected int maxCount;
   protected int pageDisplayCount;
   protected OAHtmlComponent compDefault;
   
   public OADataTable(Hub hub, int scrollAmt, int maxCount, int pageDisplayCount) {
	   this.scrollAmt = scrollAmt;
	   this.maxCount = maxCount;
	   this.pageDisplayCount = pageDisplayCount;
	   setHub(hub);
   }
    
   public int getScrollAmount() {
	   return scrollAmt;
   }
   public int getTopRow() {
	   init();
	   return currentPage * scrollAmt;
   }
   public int getMaxCount() {
	   return maxCount;
   }
   
   private int newListCount;
   protected void init() {
       
       if (HubDataDelegate.getNewListCount(hub) != newListCount) {
    	   newListCount = HubDataDelegate.getNewListCount(hub);
    	   currentPage = 0;
       }
   }
   
   public void setDefaultComponent(OAHtmlComponent comp) {
	   this.compDefault = comp;
   }
   
   
	protected void afterSetValuesInternal() {
		if (bUseDefault && compDefault != null) compDefault.afterSetValuesInternal();
	}
   protected @Override void beforeSetValuesInternal() {
       bUseDefault = false;
	   if (compDefault != null) compDefault.beforeSetValuesInternal();
   }
	 
   	private boolean bUseDefault;
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
       bUseDefault = false;
       init();
	   if (nameUsed == null || values == null || values.length != 1) return;
	   String cmd = values[0];
	   if (cmd.length() < 2) return;
	   char ch = cmd.charAt(0);
	   
	   ch = Character.toLowerCase(ch);
	   
	   if (ch == 'p') {
		   int x = OAConv.toInt(cmd.substring(1));
		   currentPage = x;
	   }
	   else if (ch == 'r') {
		   int x = OAConv.toInt(cmd.substring(1));
		   if (x >= 0) hub.setPos(x);
	       bUseDefault = true;
	   }
	   if (compDefault != null) compDefault.setValuesInternal(nameUsed, values);
    }

    protected String processCommand(OASession session, OAForm form, String command) {
    	if (bUseDefault && compDefault != null) return compDefault.processCommand(session, form, "");
    	return null;
    }
   
    

    public String getNavHtml() {
	   init();
       if (scrollAmt == 0) return "";
       int totalObjects = hub.getSize();
       if (maxCount > 0 && totalObjects > maxCount) totalObjects = maxCount;
	   
       int totalPages = (int) Math.ceil(((double)totalObjects) / ((double)scrollAmt));
       if (currentPage > totalPages) currentPage = 0;

       int beginPage = currentPage - ((int) pageDisplayCount/2);
       if (beginPage < 0) beginPage = 0;
       
       int endPage = beginPage + (pageDisplayCount-1);
       
       if (endPage >= totalPages) {
    	   beginPage = totalPages - pageDisplayCount;
           if (beginPage < 0) beginPage = 0;
       }
       // recalc end page
       endPage = beginPage + (pageDisplayCount-1);
       if (endPage >= totalPages) endPage = (totalPages - 1);
       
       StringBuffer sb = new StringBuffer(256);
       sb.append("<ul>");
       if (maxCount > 0 && totalObjects >= maxCount) {
    	   sb.append("<li class='oaTableNavMsg'>(only displaying the first " + maxCount + " selected)&nbsp;&nbsp;</li>");
       }
       else {
    	   sb.append("<li class='oaTableNavMsg'>(" + totalObjects + " selected)&nbsp;&nbsp;</li>");
       }
       if (totalPages > 0) {
    	   sb.append("<li class='oaTableNavPageText'>Page "+ (currentPage+1) + " of " + totalPages + "&nbsp;&nbsp;</li>");
       }
       
       if (totalPages > 1) {
	       String dis = (currentPage > 0) ? "" : " disablelink";
		   sb.append("<li><a oaValue='p0' href='#' class='prevnext" +dis+"'>««</a></li>");
	   
		   int x = currentPage - 1;
		   if (x < 0) x = 0;
		   sb.append("<li><a oaValue='p"+x+"' href='#' class='prevnext"+dis+"'>«</a></li>");
       
	       for (int i = beginPage; i <= endPage; i++) {
	    	   String s = (i == currentPage) ? " class='oaTableNavCurrentPage'" : "";
	    	   sb.append("<li><a oaValue='p"+i+"' href='#'"+s+">"+(i+1)+"</a></li>");
	       }
	   
		   x = currentPage + 1;
		   if (x >= totalPages) x = currentPage;
		   dis = (currentPage < (totalPages-1)) ? "" : " disablelink";
		   sb.append("<li><a oaValue='p"+x+"' href='#' class='prevnext"+dis+"'>»</a></li>");

		   dis = (currentPage+1 != totalPages) ? "" : " disablelink";
		   sb.append("<li><a oaValue='p"+(totalPages-1)+"' href='#' class='prevnext"+dis+"'>»»</a></li>");
       }	   
       sb.append("</ul>");
	   return new String(sb);
   }

   
}







