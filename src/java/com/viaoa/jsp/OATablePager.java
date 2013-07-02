/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jsp;

import com.viaoa.hub.*;

/**
 * OATable page control.
 * @author vvia
 */
public class OATablePager implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    protected int scrollAmt;
    protected int currentPage;  /* zero based */
    protected int maxCount;
    protected int pageDisplayCount;
    protected int cntObjectsPerRow;
    private Hub hub;
    private boolean bTop, bBottom;
    
    public OATablePager(Hub hub, int scrollAmt, int maxCount, int pageDisplayCount,boolean bTop, boolean bBottom) {
        this.hub = hub;
        this.scrollAmt = scrollAmt;
        this.maxCount = maxCount;
        this.pageDisplayCount = pageDisplayCount;
        this.bTop = bTop;
        this.bBottom = bBottom;
    }

    public int getScrollAmount() {
        return scrollAmt;
    }
    public int getTopRow() {
        return currentPage * scrollAmt;
    }
    public int getMaxCount() {
        return maxCount;
    }
    public boolean isTop() {
        return bTop;
    }
    public boolean isBottom() {
        return bBottom;
    }
    /**
     * This is used by OAGrid, which displays multiple objects per row.
     * @param amt number of objects per row
     */
    public void setObjectsPerRowCount(int amt) {
        cntObjectsPerRow = amt;
    }


    public void setCurrentPage(int x) {
        this.currentPage = x;
    }
    
    public String getHtml() {
        if (scrollAmt == 0) return "";
        int totalObjects = hub.getSize();
        if (maxCount > 0 && totalObjects > maxCount) totalObjects = maxCount;

        int totalRows = totalObjects;
        if (cntObjectsPerRow > 1) {
            totalRows = (int) Math.ceil(((double) totalObjects) / ((double) cntObjectsPerRow));
        }
        
        int totalPages = (int) Math.ceil(((double) totalRows) / ((double) scrollAmt));
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = (totalPages-1);
        }

        int beginPage = currentPage - ((int) pageDisplayCount / 2);
        if (beginPage < 0) beginPage = 0;

        int endPage = beginPage + (pageDisplayCount - 1);

        if (endPage >= totalPages) {
            beginPage = totalPages - pageDisplayCount;
            if (beginPage < 0) beginPage = 0;
        }
        // recalc end page
        endPage = beginPage + (pageDisplayCount - 1);
        if (endPage >= totalPages) endPage = (totalPages - 1);

        StringBuffer sb = new StringBuffer(256);
        if (maxCount > 0 && totalObjects >= maxCount) {
            sb.append("<span class='oatablePagerMsg'>(only displaying the first " + maxCount + " selected)</span>");
        }
        else {
            sb.append("<span class='oatablePagerMsg'>(" + totalObjects + " selected)</span>");
        }
        if (totalPages > 0) {
            sb.append("<span class='oatablePagerMsg'>Page " + (currentPage + 1) + " of " + totalPages + "</span>");
            sb.append("<ul class='oatablePager'>");
        }

        if (totalPages > 1) {
            String dis = (currentPage > 0) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='0'" + dis + ">««</li>");

            int x = currentPage - 1;
            if (x < 0) x = 0;
            sb.append("<li oaValue='" + x + "'" + dis + ">«</li>");

            for (int i = beginPage; i <= endPage; i++) {
                String s = (i == currentPage) ? " class='oatablePagerSelected'" : "";
                
                String dots = i == beginPage && i > 0 ? ".." : "";
                String dots2 = (i == endPage && endPage+1 < totalPages) ? ".." : "";
                
                sb.append("<li" + s + " oaValue='" + i + "'>" + dots + (i + 1) + dots2 + "</li>");
            }

            x = currentPage + 1;
            if (x >= totalPages) x = currentPage;
            dis = (currentPage < (totalPages - 1)) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='" + x + "'" + dis + ">»</li>");

            dis = (currentPage + 1 != totalPages) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='" + (totalPages - 1) + "'" + dis + ">»»</a></li>");
        }
        if (totalPages > 0) {
            sb.append("</ul>");
        }
        return new String(sb);
    }

}
