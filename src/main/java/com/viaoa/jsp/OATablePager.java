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
package com.viaoa.jsp;

import com.viaoa.hub.*;
import com.viaoa.util.OAString;

/**
 * OATable page control.
 * @author vvia
 */
public class OATablePager implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    protected int scrollAmt;
    protected int columns;
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
    public int getCurrentPage() {
        return currentPage;
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
            sb.append("<span class='oatablePagerMsg'>(only displaying the first " + OAString.format(maxCount, "R,0") + " selected)</span>");
        }
        else {
            sb.append("<span class='oatablePagerMsg'>(" + OAString.format(totalObjects, "R,0") + " selected)</span>");
        }
        if (totalPages > 0) {
            sb.append("<span class='oatablePagerMsg'>Page " + (currentPage + 1) + " of " + OAString.format(totalPages, "R,0") + "</span>");
            sb.append("<ul class='oatablePager'>");
        }

        if (totalPages > 1) {
            String dis = (currentPage > 0) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='0'" + dis + ">\u00ab</li>");

            int x = currentPage - 1;
            if (x < 0) x = 0;
            sb.append("<li oaValue='" + x + "'" + dis + ">"+("&lt;")+"</li>");

            for (int i = beginPage; i <= endPage; i++) {
                String s = (i == currentPage) ? " class='oatablePagerSelected'" : "";
                
                String dots = i == beginPage && i > 0 ? ".." : "";
                String dots2 = (i == endPage && endPage+1 < totalPages) ? ".." : "";
                
                sb.append("<li" + s + " oaValue='" + i + "'>" + dots + (i + 1) + dots2 + "</li>");
            }

            x = currentPage + 1;
            if (x >= totalPages) x = currentPage;
            dis = (currentPage < (totalPages - 1)) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='" + x + "'" + dis + ">"+"&gt;"+"</li>");

            dis = (currentPage + 1 != totalPages) ? "" : " class='oatablePagerDisable'";
            sb.append("<li oaValue='" + (totalPages - 1) + "'" + dis + ">\u00bb</a></li>");
        }
        if (totalPages > 0) {
            sb.append("</ul>");
        }
        return new String(sb);
    }

    private boolean bShowEmptyRows = true;
    
    /**
     * Flag to know if empty rows should be created, to keep the table a consistent height.
     */
    public boolean getShowEmptyRows() {
        return bShowEmptyRows;
    }
    public void setShowEmptyRows(boolean b) {
        this.bShowEmptyRows = b;
    }

}
