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
package com.viaoa.jfc.report;

import java.awt.*;
import java.awt.print.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.jfc.print.*;

/**
 * Creates a report that contains a Header, Footer and Detail.
 * 
 * @author vincevia
 */
public class OAReport implements OAPrintable {
    // IMPORTANT: need to convert from point to pixel whenever using PageFormat or Paper.  For printing, graphics.scale(x,x) is set to make it wysiwyg
    private static Logger LOG = Logger.getLogger(OAReport.class.getName());
    private String title;
    private OAPrintable prtTitleHead, prtHead, prtDetail, prtFoot;

    private boolean bIsPreviewing;
    private PageFormat pageFormat;
    
    
    public Printable getTitleHeader() {
        return prtTitleHead;
    }
    public void setTitleHeader(OAPrintable prtTitleHead) {
        this.prtTitleHead = prtTitleHead;
    }

    public Printable getHeader() {
        return prtHead;
    }
    public void setHeader(OAPrintable prtHead) {
        this.prtHead = prtHead;
    }
    
    public Printable getDetail() {
        return prtDetail;
    }
    public void setDetail(OAPrintable prtDetail) {
        this.prtDetail = prtDetail;
    }

    public Printable getFooter() {
        return prtFoot;
    }
    public void setFooter(OAPrintable prtFoot) {
        this.prtFoot = prtFoot;
    }

    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = new PageFormat();
        }
        return pageFormat;
    }
    public void setPageFormat(PageFormat pf) {
        this.pageFormat = pf;
    }
    
    void outlinePage(Graphics graphics, PageFormat pageFormat) {
        if (pageFormat == null || graphics == null) return;
        
        Graphics2D g = (Graphics2D) graphics;        

        Font f = g.getFont();
        g.setFont(f.deriveFont(Font.BOLD, 10.0f));
        
        // top-left
        int x = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableX());
        int y = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableY());

        g.setColor(Color.gray);
        g.fillRect(0,0, x, y);

        g.setColor(Color.lightGray);
        g.drawLine(x/2,0,x/2,y);

        g.setColor(Color.black);
        String s = (int)pageFormat.getImageableX()+","+(int)pageFormat.getImageableY()+" => "+x+","+y;
        g.drawString(s, x+5, (y/2)+5);        

        
        // top-right
        x = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableX() + pageFormat.getImageableWidth());
        int x2 = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getWidth());
        g.setColor(Color.gray);
        g.fillRect(x, 0, x2, y);

        int x3 = x + (x2 - x)/2;
        g.setColor(Color.lightGray);
        g.drawLine(x3, 0, x3, y);
        

        x = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableX());
        y = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableY());
        int w = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth());
        int h = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());

        g.setColor(Color.yellow.brighter());
        g.fillRect(x, y, w, h);
    }
    
    private final static Dimension dimZero = new Dimension(0,0);
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageFormat == null || graphics == null || prtDetail == null) return NO_SUCH_PAGE;
        
        Graphics2D g = (Graphics2D) graphics;        

//outlinePage(g, pageFormat);
        
        // Note: use PageFormat to get paper measurements, since it will take orientation into account; paper does not.
        Paper paper = pageFormat.getPaper();
        
        OAPrintable p;
        if (pageIndex == 0) {
            p = prtTitleHead;
            if (p == null) p = prtHead;
        }
        else {
            p = prtHead;
            if (p == null) p = prtTitleHead;
        }
        
        if (p != null) {
            p.print(graphics, pageFormat, pageIndex);
        }
        
        Dimension dimHead;
        if (p == null) dimHead = dimZero; 
        else {
            dimHead = p.getPrintSize(pageIndex, pageFormat, (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth()));
            if (dimHead == null) dimHead = dimZero;
        }
/*
int x = (int) convertPointsToPixels(pageFormat.getImageableX());
int y = (int) convertPointsToPixels(pageFormat.getImageableY());
g.setColor(Color.red);
g.setStroke(new BasicStroke(2));
g.drawRect(x, y, dimHead.width, dimHead.height);
*/

        Dimension dimFoot;
        if (prtFoot == null) dimFoot = dimZero;
        else {
            dimFoot = prtFoot.getPrintSize(pageIndex, pageFormat, (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth()));
            if (dimFoot == null) dimFoot = dimZero;
        }
/*
int w = (int) convertPointsToPixels(pageFormat.getPaper().getImageableWidth());
int h = (int) convertPointsToPixels(pageFormat.getPaper().getImageableHeight());
g.drawRect(x,y+h-dimFoot.height,dimFoot.width, dimFoot.height);
*/


        Paper paperHold = pageFormat.getPaper();
        paper = (Paper) paperHold.clone();


        int hFootPoints = (int) Math.round(OAPrintUtil.convertPixelsToPoints(dimFoot.height));  // needs to be converted to points (which is used by printer)
        if (pageFormat.getOrientation() == pageFormat.PORTRAIT) {
            paper.setImageableArea(paper.getImageableX(), paper.getImageableY() + paper.getImageableHeight() - hFootPoints, paper.getImageableWidth(), hFootPoints);
        }
        else {
            paper.setImageableArea(paper.getImageableX()+paper.getImageableWidth()-hFootPoints, paper.getImageableY(), hFootPoints, paper.getImageableHeight());
        }
        pageFormat.setPaper(paper);

        if (prtFoot != null) {
            prtFoot.print(graphics, pageFormat, pageIndex);
        }
/*
int xx = (int)paper.getImageableX();
int yy = (int) paper.getImageableY();
yy += (int) paper.getImageableHeight();
yy -= (int) convertPixelsToPoints(dimFoot.height);
int ww = (int) paper.getImageableWidth();
int hh = (int) convertPixelsToPoints(dimFoot.height);
g.setColor(Color.red);
g.fillRect(xx, yy, ww, hh);
*/        
        
        paper = (Paper) paperHold.clone();
        int hHeadPoints = (int) Math.round(OAPrintUtil.convertPixelsToPoints(dimHead.height));  // needs to be converted to points (which is used by printer)
        if (pageFormat.getOrientation() == pageFormat.PORTRAIT) {
            paper.setImageableArea(paper.getImageableX(), paper.getImageableY()+hHeadPoints, 
                    paper.getImageableWidth(), paper.getImageableHeight()-(hHeadPoints+hFootPoints));
        }
        else {
            paper.setImageableArea(paper.getImageableX()+hHeadPoints, paper.getImageableY(), paper.getImageableWidth()-(hHeadPoints+hFootPoints), paper.getImageableHeight() );
        }
        pageFormat.setPaper(paper);

        int result = prtDetail.print(graphics, pageFormat, pageIndex);

        pageFormat.setPaper(paperHold);
        return result;
    }

    
    @Override
    public Dimension getPrintSize(int pageIndex, PageFormat pageFormat,int width) {
        Dimension dim = new Dimension(width, Integer.MAX_VALUE);
        return dim;
    }
    @Override
    public void beforePrint(PageFormat pageFormat) {
        if (prtTitleHead != null) prtTitleHead.beforePrint(pageFormat);
        if (prtHead != null) prtHead.beforePrint(pageFormat);
        if (prtDetail != null) prtDetail.beforePrint(pageFormat);
        if (prtFoot != null) prtFoot.beforePrint(pageFormat);
    }
    @Override
    public void afterPrint() {
        if (prtTitleHead != null) prtTitleHead.afterPrint();
        if (prtHead != null) prtHead.afterPrint();
        if (prtDetail != null) prtDetail.afterPrint();
        if (prtFoot != null) prtFoot.afterPrint();
    }

    @Override
    public void beforePreview(PageFormat pageFormat) {
        bIsPreviewing = true;
        if (prtTitleHead != null) prtTitleHead.beforePreview(pageFormat);
        if (prtHead != null) prtHead.beforePreview(pageFormat);
        if (prtDetail != null) prtDetail.beforePreview(pageFormat);
        if (prtFoot != null) prtFoot.beforePreview(pageFormat);
    }
    @Override
    public int preview(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        bIsPreviewing = true;
        try {
            int x = print(graphics, pageFormat, pageIndex);
            return x;
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "getting getting page", e);
        }
        return Printable.NO_SUCH_PAGE;
    }    
    @Override
    public void afterPreview() {
        if (prtTitleHead != null) prtTitleHead.afterPreview();
        if (prtHead != null) prtHead.afterPreview();
        if (prtDetail != null) prtDetail.afterPreview();
        if (prtFoot != null) prtFoot.afterPreview();
        bIsPreviewing = false;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
