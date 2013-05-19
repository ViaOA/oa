/***qqqqqqq


Printing notes:

when a view spans over a page break the bottom page break is moved to before the view.  If this causes
another view to "block" a page break, then the orig bottom will stay and the top of next page will begin
where the new page break would have been (except that another view blocked it).  This will cause views
to be seperated over the page break.

"Jumbo" Views: if view size > page: do nothing, print where it is, and it will continue on next page

>>>>> JEditorPane HTML notes:  <table> using percent for column width is based on the total width of the whole table.


*/

/*********
I've been using JTextPane (HTML classes) and have a problem with font sizes (along with some other bugs (#4352983 major!) in printing.

This was suggested, but is not a solution
g2d.transform(getConfiguration().getNormalizingTransform());

// Java assumes 72 ppi, windows could be different (96, 120, etc)
int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
int fontSize = (int)Math.round(12.0 * screenRes / 72.0);

http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html
qqqqqqqqqqqqqqq

http://developer.java.sun.com/developer/bugParade/bugs/4352983.html


In the print method of ClipingBug.java
you can paint to offscreen image first and paint the image to graphics.

BufferedImage image = new BufferedImage(holder.getWidth(),holder.getHeight(),
BufferedImage.TYPE_INT_RGB);
Graphics2D graphicsTmp = image.createGraphics();
graphicsTmp.translate( holder.getX( ), holder.getY( ) );
holder.paint( graphicsTmp );
g.drawImage(image,0,0,Color.white,null);

xxxxx@xxxxx 2001-11-08
******/


package com.viaoa.jfc.html;

import java.awt.*;
import java.awt.print.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.viaoa.jfc.print.*;

import java.util.*;
import java.io.*;
import java.net.*;


public class OAHtmlView extends JEditorPane implements OAPrintable {
    protected ReportEditorKit reportEditorKit;

    /**
     * @param cssName  ex: "vetbp.css"
     * @param fileRootUrl ex: "bin/com/oldcastle/dispatcher"
     * @param jarRootUrl  ex: "jar:file:dispatcherlg.jar!/com/oldcastle/dispatcher"
     */
    public OAHtmlView(String cssName, String fileRootUrl, String jarRootUrl) {
        reportEditorKit = new ReportEditorKit(cssName);
		setEditorKit(reportEditorKit);
        MyHTMLDocument doc = new MyHTMLDocument(reportEditorKit.getStyleSheet());
		setDocument(doc);

        try {
			String fn = fileRootUrl; // ex: "bin/com/oldcastle/dispatcher";
			File file = new File(com.viaoa.util.OAString.convertFileName(fn));
			URL url;
			if (file.exists()) {
				url = file.toURI().toURL();
			}
			else {
			    url = new URL(jarRootUrl); // ex: "jar:file:dispatcherlg.jar!/com/oldcastle/dispatcher");
			}
			((HTMLDocument)getDocument()).setBase(url);
        }
        catch (Exception e) {
        	System.out.println("Error creating base URL for HtmlPane: " + e);
        }
		
		doc.putProperty("imageCache", new ImageCache());
        setEditable(false);
    }
    
    

    /** Document property "imageCache" is defined in ImageView to cache images.
        When image is not found, then it calls Toolkit.getDefaultToolkit().getImage() which caches images.
        If the file image has changed, then the new image is never retreived.
    */
    public void clearImageCache() {
        ((Hashtable)getDocument().getProperty("imageCache")).clear();
    }

    //2006/06/19 need to make sure that text does not changed until printing is done
    boolean bIsPrinting, bDonePrinting;

    public void setText(String text) {
        this.setText(text, true);
    }
    public void setText(String text, boolean bWaitIfPrinting) {
        if (bWaitIfPrinting) {
            synchronized (this) {
                //2006/06/19 need to make sure that text does not change until printing is done
                for (int i=0; i < 16; i++) {
                    try {
                        if (!bIsPrinting) {
                            if (!bDonePrinting) break;
                            bDonePrinting = false;
                        }
                        this.wait(250);
                    }
                    catch (Exception e) {
                    }
                }
                bIsPrinting = false;
                bDonePrinting = false;
            }
        }
        clearImageCache();
        if (text == null) text = "";


/**** this has been moved to Tool\FixQuote.java
        // 2005/01/05 clear bogus tags
        boolean bFirst = true;
        for (int pos=0; ; ) {
            String temp = text.toUpperCase();
            pos = temp.indexOf("<BASE", pos);
            if (pos <= 0) break;
            if (bFirst) {
                bFirst = false;
                pos++;
                continue;
            }
            int pos2 = temp.indexOf(">", pos);
            if (pos2 < 0) pos2 = pos + 4;

            if (pos2+1 == text.length()) {
                if (pos == 0) text = "";
                else text = text.substring(0,pos);
            }
            else if (pos == 0) text = text.substring(pos2+1);
            else text = text.substring(0,pos) + text.substring(pos2+1);
        }
********/

        super.setText(text);
        boxView = null;
    }

    private BoxView boxView;
    private Vector vecPage;
    int widthPrint, heightPrint;
    PageFormat previousePageFormat;


    /** size that was used when HtmlPane was printed. */
    public Dimension getPrintSize(int pageWidth) {
    	((MyHTMLDocument)getDocument()).setPrinting(true);
        try {
            ((MyHTMLDocument)getDocument()).pageWidth = pageWidth;
            getBoxView(pageWidth);
        }
        finally {
            ((MyHTMLDocument)getDocument()).setPrinting(false);
        }
//System.out.println("HtmlPane.getPrintSize(pageWidth="+pageWidth+") w="+widthPrint+" h="+heightPrint);//qqqqqqqqqq
        return new Dimension(widthPrint, heightPrint);
    }
    public Dimension getPrintSize() {
//System.out.println("HtmlPane.getPrintSize() w="+widthPrint+" h="+heightPrint);//qqqqqqqqqq
        return new Dimension(widthPrint, heightPrint);
    }

    // called for OAPrintable.
	public Dimension getPrintSize(int pageIndex, PageFormat pageFormat, int width) {
		return getPrintSize(width);
	}

    /**
        Used to reformat to fit page width for printing.  This will use the same Document and EditorKit.
    */
    class PrintBoxView extends BoxView {
        public PrintBoxView(Element rootElement) {
            super(rootElement, BoxView.Y_AXIS);
            loadChildren(getViewFactory());  // create children views
        }
        public ViewFactory getViewFactory() {
            ViewFactory vf = OAHtmlView.this.reportEditorKit.getViewFactory();
            return vf;
        }
        public Document getDocument() {
            Document d = OAHtmlView.this.getDocument();
            return d;
        }
        
        
        // public Container getContainer() // needs to return null, else it will use the width of container

        // test only - not needed qqqqqq 2004/07/02
        public void qqq(int widthPrint) {
            int x = boxView.getViewCount();
            int[] i1 = new int[x];
            int[] i2 = new int[x];
            layoutMajorAxis(widthPrint, BoxView.Y_AXIS, i1, i2 );
            x = 12;
            layout(widthPrint, Integer.MAX_VALUE);
        }
    }

//qqqqqqqqqqqqqqqqqq  2006/07/22
    public BoxView getBoxView(final int pageWidth) {
    	if (SwingUtilities.isEventDispatchThread()) {
    		((MyHTMLDocument)getDocument()).setPrinting(true);
        	getBoxView2(pageWidth);
    		((MyHTMLDocument)getDocument()).setPrinting(false);
    		return boxView;
        }
    
    	try {
	    	SwingUtilities.invokeAndWait(new Runnable() {
	        	public void run() {
	                try {
	                    ((MyHTMLDocument)getDocument()).setPrinting(true);
	                	getBoxView2(pageWidth);
	                }
	                finally {
	                	((MyHTMLDocument)getDocument()).setPrinting(false);
	                }
	        	}
	        });
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    	return boxView;
    }

    private BoxView getBoxView2(int pageWidth) {
        if (boxView == null || pageWidth != widthPrint) {
            widthPrint = pageWidth;
            if (boxView == null) {
                boxView = new PrintBoxView(getDocument().getDefaultRootElement());
            }
            boxView.setSize(widthPrint, Integer.MAX_VALUE/2);
            // dont use this, it will cause the print version to use the same width as the visual component
            // boxView.setParent( ((javax.swing.plaf.basic.BasicTextUI) getUI()).getRootView(HtmlPane.this) );
            heightPrint = (int) boxView.getPreferredSpan(View.Y_AXIS);
        }
        return boxView;
    }

    
    int printResult;
    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
        //2006/06/19 need to make sure that text does not changed until printing is done
    	printResult = NO_SUCH_PAGE;
    	synchronized (this) {
            bIsPrinting = true;
        }
        /*if (bShowDebug) {
	        System.out.println(">>> print() name="+name+"  page="+pageIndex + " *********************");//ttttttttvavtttttqqqqqqqq
	        System.out.println("  > thread    : "+Thread.currentThread());//ttttttttvavtttttqqqqqqqq
	        System.out.println("  > graphics  : "+graphics);//ttttttttvavtttttqqqqqqqq
	        System.out.println("  > pageFormat:"+pageFormat);//ttttttttvavtttttqqqqqqqq
        }*/
        ((MyHTMLDocument)getDocument()).setPrinting(true);
        try {
            ((MyHTMLDocument)getDocument()).pageWidth = (int) pageFormat.getImageableWidth();
            //was:  return doPrint(graphics, pageFormat, pageIndex);
//qqqqqqqqqqqqqqqqqq  2006/07/22
            SwingUtilities.invokeAndWait(new Runnable() {
            	public void run() {
                    try {
	                    ((MyHTMLDocument)getDocument()).setPrinting(true);
	                    printResult = doPrint(graphics, pageFormat, pageIndex);
                    }
                    finally {
                    	((MyHTMLDocument)getDocument()).setPrinting(false);
                    }
            	}
            });
//qqqqqqqqqqqqqqqqqq
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        finally {
            ((MyHTMLDocument)getDocument()).setPrinting(false);
            //2006/06/19 need to make sure that text does not changed until printing is done
            synchronized (this) {
                bIsPrinting = false;
                bDonePrinting = true;
                try {
                    this.notifyAll();
                }
                catch(Exception e){}
            }
        }
        return printResult;
    }

    int doPrint(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (graphics == null || pageIndex < 0) {  // hack that can be called to end printing
            boxView = null;
            return NO_SUCH_PAGE;
        }

        int w = (int) pageFormat.getImageableWidth();
        int h = (int) pageFormat.getImageableHeight();

        double scale = 1.0;
        if (boxView == null || pageFormat != previousePageFormat) {
            previousePageFormat = pageFormat;
        	getBoxView(w);
            vecPage = new Vector();
        }
        if (!allocate(vecPage, boxView, w, h, pageIndex)) return NO_SUCH_PAGE;


        Graphics2D g = (Graphics2D) graphics;
        /*
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        */

        PrintPage pp = (PrintPage) vecPage.elementAt(pageIndex);

        // PrintPage ppNext = null;
        // if ((pageIndex+1) < vecPage.size()) ppNext = (PrintPage) vecPage.elementAt(pageIndex+1);
        PrintPage ppPrev = null;
        if (pageIndex > 0) ppPrev = (PrintPage) vecPage.elementAt(pageIndex-1);

        // g.setClip((int)(pageFormat.getImageableX()), ((int)pageFormat.getImageableY()), w*2, h*2);
        g.setClip(0, 0, w*2, h*2);// This works
        g.translate((int)pageFormat.getImageableX(), ((int)pageFormat.getImageableY())-(pp.top));

        // Rectangle rect = new Rectangle(0,0,(int)boxView.getPreferredSpan(View.X_AXIS), (int)boxView.getPreferredSpan(View.Y_AXIS));
        Rectangle rect = new Rectangle(0,0,Integer.MAX_VALUE/2, Integer.MAX_VALUE/2);

        boolean bBuffered = this.isDoubleBuffered();
        this.setDoubleBuffered(false);

boolean bBuffered2 = RepaintManager.currentManager(this).isDoubleBufferingEnabled();
if (bBuffered2) RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);



    	firstView = null;
    	lastView = null;
        int xx = paintViews(g, boxView, rect, pp.top, pp.bottom, pp.nextTop, h, ppPrev);
        
        
//if (bShowDebug) System.out.println("   top="+pp.top+"  bottom="+pp.bottom+"  "+xx+" views printed");//ttttttttvavtttttqqqqqqqq

        g.translate( -((int)pageFormat.getImageableX()), -(((int)pageFormat.getImageableY())-(pp.top)) );

        this.setDoubleBuffered(bBuffered);

if (bBuffered2) RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
        return PAGE_EXISTS;
    }

    protected boolean allocate(Vector vec, BoxView boxView, int w, int h, int pageIndex) {
        if (pageIndex < vec.size()) {
            PrintPage pp = (PrintPage) vec.elementAt(pageIndex);
            if (pp != null) return true;
        }

        int top = 0;
        int bottom = (h-1);
        int page = 0;

        if (top == 0 && pageIndex > 0 && vec.size() > 0) {
            page = Math.min(vec.size()-1, pageIndex-1);
            PrintPage pp = (PrintPage) vec.elementAt(page);
            top = pp.nextTop;
            bottom = top + (h-1);
            page++;
        }

        if (top > heightPrint) return false;

        Rectangle rect = new Rectangle(0,0,Integer.MAX_VALUE/2, Integer.MAX_VALUE/2);

        for ( ;page<=pageIndex; page++) {
            PrintPage pp = new PrintPage();
            pp.top = top;
            pp.bottom = bottom;
            pp.nextTop = pp.bottom + 1;

            int newBottom = allocateViews(boxView, rect, top, bottom, h);
            if (newBottom == 0) return false;
            if (newBottom != bottom) {
                int x = allocateViews(boxView, rect, top, newBottom, h);
                if (x == newBottom) {
                    pp.bottom = newBottom;
                    pp.nextTop = pp.bottom + 1;
                }
                else {
                    pp.nextTop = newBottom + 1;
                }
            }

            vec.addElement(pp);

            top = pp.nextTop;
            bottom = top + (h-1);
        }
        return true;
    }

    class PrintPage {
        int top;
        int bottom;
        int nextTop;  // where next page should begin.  If views are "split" on page break, then this will be < bottom
    }

    /**
        @returns adjusted bottom, 0 if page not needed
    */
    protected int allocateViews(View view, Rectangle rect, int top, int bottom, int pageHeight) {
        // this is only called when a view does not fit, to find the top position of view that will not fit

        int x = view.getViewCount();
        boolean bFound = false;
        for (int i=0; i < x; i++) {
            Rectangle rectChild =  (Rectangle) view.getChildAllocation(i, rect);
            if (rectChild == null) continue;

//qqqqqqqvvvvvvvvvv
//System.out.println(view.getClass().getName()+" w:"+rectChild.width+" pref:"+view.getPreferredSpan(View.X_AXIS)+" max:"+view.getMaximumSpan(View.X_AXIS));

            // 1: view on previous page
            if ((rectChild.y + rectChild.height) <= top) continue;

            bFound = true;

            // 2: the start for this view is after the bottom
            if (rectChild.y > bottom) continue;

            View viewChild = view.getView(i);

// 2006/03/25
//viewChild.getElement()
// if (viewChild.getElement().getAttributes().getAttribute("nobr") != null) System.out.println("FOUND ONE !!!!!!");//qqqqqq
boolean bNoBr = (viewChild.getElement().getAttributes().getAttribute("nobr") != null) || (viewChild.getViewCount() == 0);
/*
    Enumeration e = viewChild.getElement().getAttributes().getAttributeNames();
    for ( ; e.hasMoreElements(); ) {
        Object obj = e.nextElement();
        System.out.println(obj+" = "+viewChild.getAttributes().getAttribute(obj));
    }
*/

            // 3: if view is greater then page height
            boolean bJumbo = (bNoBr && rectChild.height > pageHeight);

            // 4: fits on currenty page
            if (bJumbo || (rectChild.y >= top && ((rectChild.y + (rectChild.height-1)) <= bottom)) ) continue;

            // 4.1: dont split paragraphs
            boolean bParagraph = (viewChild instanceof javax.swing.text.html.ParagraphView && rectChild.height < pageHeight/4);

            // 5: leaf view, cant fit on page.  Adjust bottom so that this will show on next page
            if (bParagraph || bNoBr) {
                if (rectChild.y >= top && bottom >= rectChild.y) bottom = (rectChild.y-1);
                continue;
            }

            int xx = allocateViews(viewChild, rectChild, top, bottom, pageHeight);
            if (xx < bottom && xx != 0) bottom = xx;
        }
        if (!bFound) bottom = 0;
        return bottom;
    }


View firstView, lastView;
    /**
        Paints all views between top and bottom for a page.
    */
    protected int paintViews(Graphics g, View view, Rectangle rect, int top, int bottom, int nextTop, int pageHeight, PrintPage ppPrev) {

        // 2006/03/25
        int prevTop = 0;
        int prevBottom = 0;
        if (ppPrev != null) {
            prevTop = ppPrev.top;
            prevBottom = ppPrev.bottom;
        }

        int xx = 0;
        int x = view.getViewCount();
        for (int i=0; i < x; i++) {
            Rectangle rectChild =  (Rectangle) view.getChildAllocation(i, rect);
            if (rectChild == null) continue; // no width and no height

            // 1: view on previous page
            if ((rectChild.y + rectChild.height) <= top) continue;

            // 2006/03/25
            if ((rectChild.y + rectChild.height) <= prevBottom) continue;

            // 2: the start for this view is after the bottom
            if (rectChild.y > bottom) continue;

            // 2.1: the start for this view is after the start of next page (staggered pages)
            if (rectChild.y >= nextTop) continue;

            View viewChild = view.getView(i);

// 2006/03/25
boolean bNoBr = (viewChild.getElement().getAttributes().getAttribute("nobr") != null) || (viewChild.getViewCount() == 0);
            if (bNoBr && rectChild.y < top) continue; // already printed


            // 3: if view is greater then page height
            //was: boolean bJumbo = (viewChild.getViewCount() == 0 && rectChild.height > pageHeight);
            boolean bJumbo = (bNoBr && rectChild.height > pageHeight);


            // 4: fits on current page
            if (bJumbo || (rectChild.y >= top && ((rectChild.y + (rectChild.height-1)) <= bottom)) ) {
if (firstView == null) firstView = viewChild;
lastView = viewChild;
            	xx++;
                viewChild.paint(g, rectChild);
                continue; // added 2006/07/23
            }

            // 4.1: dont split paragraphs
            boolean bParagraph = (viewChild instanceof javax.swing.text.html.ParagraphView && rectChild.height < pageHeight/4);
            if (bParagraph) continue;
            if (bNoBr) continue;

            // find children that are within range
            xx += paintViews(g, viewChild, rectChild, top, bottom, nextTop, pageHeight, ppPrev);
        }
        return xx;
    }


    MediaTracker tracker;
    class ReportEditorKit extends HTMLEditorKit {
        public ReportEditorKit(String cssName) {
            super();
            try {
                StyleSheet ss = getStyleSheet();
                try {
                    Reader rin = new InputStreamReader(OAHtmlView.class.getResourceAsStream(cssName));
                    ss.loadRules(rin, null);
                }
                catch (Exception e) {
                    System.out.println("Error loading " + cssName + ": " +e);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
	            System.out.println(""+ex);
            }
        }

        ViewFactory defaultFactory;

        public ViewFactory getViewFactory() {
            if (defaultFactory == null) {
                defaultFactory = new HTMLEditorKit.HTMLFactory() {
                    public View create(Element elem) {
                        View view = null;
                        Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
                        if (o instanceof HTML.Tag) {
                            HTML.Tag kind = (HTML.Tag) o;
                            if (kind == HTML.Tag.TABLE) {
                                view = super.create(elem);
                            }
                            else if (kind == HTML.Tag.COMMENT) {
                                view = new InvisibleView(elem);
                            }
                            else if (kind instanceof HTML.UnknownTag) {
                                view = new InvisibleView(elem);
                            }
                            else if (kind == HTML.Tag.BR) {
                                view = new InlineView(elem) {
                                    boolean bChecking;
                                    public int getBreakWeight(int axis, float pos, float len) {
	                                    if (axis == X_AXIS) return ForcedBreakWeight;
	                                    else return super.getBreakWeight(axis, pos, len);
                                    }
                                    public float getPreferredSpan(int axis) {
                                        float fx = super.getPreferredSpan(axis);
	                                    if (axis == View.X_AXIS) return fx;
                                        return 2f;
                                    }
                                };
                            }
                            else if (kind == HTML.Tag.SPAN) {
                                view = new InlineView(elem) {
                                    public float getPreferredSpan(int axis) {
                                        float fx = super.getPreferredSpan(axis);
	                                    if (axis == View.X_AXIS) return fx;
                                        return 1f;
                                    }
                                };
                            }
                            else if (kind == HTML.Tag.CONTENT) {
                                int p0 = elem.getStartOffset();
                                int p1 = elem.getEndOffset();
                                String vs = null;
                                try {
                                    vs = elem.getDocument().getText(p0,(p1-p0));
                                }
                                catch (Exception e) {
                                }

                                if (vs != null && vs.length() == 1 && vs.charAt(0) == '\n') {
                                    view = new InlineView(elem) {
                                        public float getPreferredSpan(int axis) {
                                            float fx = super.getPreferredSpan(axis);
	                                        if (axis == View.X_AXIS) return fx;
                                            return 2f;
                                        }
                                    };
                                }
                                else {
                                    // 2003/10/07 was: view = super.create(elem);
                                    view = new InlineView(elem) {
                                        // shrinks line spacing, handles underlining

                                        GlyphPainter painter;
                                        float height, newHeight, descent, descentDx;

                                        public float getPreferredSpan(int axis) {
	                                        if (axis == View.X_AXIS) return super.getPreferredSpan(axis);

	                                        if (painter == null) {
                                                painter = getGlyphPainter();
                                                height = painter.getHeight(this);
                                                descent = painter.getDescent(this);
                                                descentDx = descent * .05f;
                                                float ascent = painter.getAscent(this);
                                                float ascentDx = ascent * .09f;
                                                float leading = height - (descent + ascent);
                                                newHeight = height - (leading  + ascentDx + descentDx);
	                                        }
                                            return newHeight;
                                        }

                                        boolean bPainting;
                                        public boolean isUnderline() {
                                            if (bPainting) return false;
                                            return super.isUnderline();
                                        }
                                        public void paint(Graphics g, Shape a) {
                                            Rectangle rec = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
                                            int holdY = rec.y;
                                            rec.y = (int) (rec.y - (height-newHeight) + descentDx);

                                            bPainting = true;
                                            super.paint(g, a);
                                            bPainting = false;

                                            if (isUnderline()) {
                                                //int y = (int) (rec.y + rec.height - newDescent + 1);
                                                int y = (int) (rec.y + (height - descent) + 1);
                                                g.drawLine(rec.x, y, rec.x+rec.width, y);
                                            }
                                            rec.y = holdY;
                                        }

                                    };
                /*
                Element elem = getElement();
                int p0 = elem.getStartOffset();
                int p1 = elem.getEndOffset();
                String vs = null;
                try {
                    vs = elem.getDocument().getText(p0,(p1-p0));
                }
                catch (Exception e) {
                }
                */
                /*
                if (vs.indexOf("VINCE VIA") >= 0) {
                    Font f = ((InlineView)view).getFont();
                    int qqq = 0;
                }
                */
                                }
                            }
                            else if (kind == HTML.Tag.IMG) {
                                /*
                                    Images need to be scaled down to 75%
                                    screens use 96dpi and printers use 72dpi
                                    without scaling, images will look much larger (1.33) then on screen
                                */
                                // 2004/11/15
                                String xx = (String) elem.getAttributes().getAttribute(HTML.Attribute.SRC);
                                if (xx != null) loadImage(xx);
                                // System.out.println("=====> "+xx);
// 2006/03/23  these are used as the outline/box for the UK HM.  It also uses, "TD background" images, 
//              which are not able to be scaled - so dont scale these
if (xx != null) {
    xx = xx.toLowerCase();
    if (xx.indexOf("cen_") >= 0 || xx.indexOf("top_") >= 0 || xx.indexOf("bot_") >= 0) {
//System.out.println("=====> "+xx);
        return super.create(elem);
    }
}

                                
                                
                                view = new ImageView(elem) {
                                    public float getPreferredSpan(int axis) {
                                        return getPreferredSpan(axis, true);
                                    }
                                    public float getPreferredSpan(int axis, boolean bCheckPrinting) {
                                        float f = super.getPreferredSpan(axis);
                                        if (bCheckPrinting) {
                                            if (((MyHTMLDocument) getDocument()).isPrinting()) {
                                                // 2004/04/23
                                                int w;
                                                if (axis == View.X_AXIS) w = (int) f;
                                                else w = (int)getPreferredSpan(View.X_AXIS,false);

                                                float scale = .75f;
                                                int wPage = ((MyHTMLDocument) getDocument()).pageWidth;
                                                if (w > wPage) {
                                                    scale = (float) ((float)wPage / (float)w);
                                                    if (scale > .75f) scale = .75f;
                                                }
                                                f = (int) (f * scale);
                                            }
                                        }
                                        return f;
                                    }
                                    public void paint(Graphics g, Shape a) {
                                        if (((MyHTMLDocument) getDocument()).isPrinting()) {
                                            int w = (int)getPreferredSpan(View.X_AXIS,false);
                                            int h = (int)getPreferredSpan(View.Y_AXIS,false);

                                            // 2004/04/23
                                            float scale = .75f;
                                            int wPage = ((MyHTMLDocument) getDocument()).pageWidth;
                                            if (w > wPage) {
                                                scale = (float) ((float)wPage / (float)w);
                                                if (scale > .75f) scale = .75f;
                                            }

                                            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                                            Graphics2D g2d = (Graphics2D) bi.getGraphics();
                                            g2d.setColor(Color.white);
                                            g2d.fillRect(0,0,w,h);

                                            Rectangle rec = a.getBounds();
                                            rec.x = rec.y = 0;
                                            super.paint(g2d, rec);
                                            g2d.dispose();
                                            Image img = bi.getScaledInstance((int)(w*scale), (int)(h*scale), Image.SCALE_SMOOTH);
                                            rec = a.getBounds();

                                            if (img != null) {
                                                if (tracker == null ) tracker = new MediaTracker(OAHtmlView.this);
                                                tracker.addImage(img, 1);
                                                try {
                                                    tracker.waitForID(1);
                                                }
                                                catch (Exception e) {
                                                    System.out.println("Loading image error: "+e);
                                                }
                                                tracker.removeImage(img, 1);
                                            }
                                            else System.out.println("HtmlPane image is null");
                                            g.drawImage(img, rec.x, rec.y, null);
                                            // g.drawImage(img, rec.x, rec.y, (int)(w*.75), (int)(h*.75), null);
                                            img.flush();
                                        }
                                        else super.paint(g, a);
                                    }
                                };
                                // view.changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
// 2003/11/24 ttttttttttttt
//                                view.changedUpdate(null, null, null);
                            }
                            else {
                                view = super.create(elem);
                            }
                        }
                        else {
                            view = new LabelView(elem);
                        }
                        return view;
                    }
                };
            }
            return defaultFactory;
        }

    }

    class MyHTMLDocument extends HTMLDocument {
        Thread[] printThreads;
        synchronized boolean isPrinting() {
            if (printThreads == null) return false;
            Thread t = Thread.currentThread();
            boolean b = false;
            for (int i=0; !b && i<printThreads.length; i++) {
                if (printThreads[i] == t) b = true;
            }
            return b;
        }
        
        synchronized void setPrinting(boolean b) {
            Thread t = Thread.currentThread();
            if (b) {
                if (printThreads == null) {
                    printThreads = new Thread[3];
                    printThreads[0] = t;
                }
                else {
                    for (int i=0; ; i++) {
                        if (i == printThreads.length) {
                            Thread[] ts = new Thread[i+1];
                            System.arraycopy(printThreads, 0, ts, 0, i);
                            printThreads = ts;
                            printThreads[i] = t;
                            break;
                        }
                        if (printThreads[i] == null || printThreads[i] == t) {
                            printThreads[i] = t;
                            break;
                        }
                    }
                }
            }
            else {
                if (printThreads == null) return;
                for (int i=0; i<printThreads.length; i++) {
                    if (printThreads[i] == t) {
                        printThreads[i] = null;
                        break;
                    }
                }
            }
        }
        
        int pageWidth;
        public MyHTMLDocument(StyleSheet ss) {
            super(ss);
            setAsynchronousLoadPriority(0); // dont load Async  2006/06/30

            //StyleSheet css = getStyleSheet();
            //css.addRule("body { font-family: Sans serif; font-size: 28pt; }");
            //css.addRule("td { padding-left: 1; padding-right: 1; padding-top: 0; padding-bottom: 0 }");
            //css.addRule("th { padding-left: 1; padding-right: 1; padding-top: 0; padding-bottom: 0 }");
        }
        public Font getFont(AttributeSet attr) {
            Font font = super.getFont(attr);

            // magic!!!!!!  Java screen font sizes are based on pixels, printer is based on Point (1/72)
            // this will adjust to make it wysiwyg
            if (!isPrinting()) {
                font = font.deriveFont((float) Math.ceil(font.getSize2D() * 1.3f) );
            }
            return font;
        }

        // Parser/Reader
        public HTMLEditorKit.ParserCallback getReader(int pos) {
            Object desc = getProperty(Document.StreamDescriptionProperty);
            if (desc instanceof URL) setBase((URL)desc);

            HTMLDocument.HTMLReader reader = new HTMLDocument.HTMLReader(pos,0,0,null) {
                // includes SPAN tags
                CharacterAction charAction = new CharacterAction();
                boolean bInSpan = false;

                public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                    fixAttributeSet(t, a);
                    if (t == HTML.Tag.SPAN) {
                        bInSpan = true;
                        charAction.start(t, a);
                    }
                    super.handleStartTag(t, a, pos);
                }

				void fixAttributeSet(HTML.Tag t, MutableAttributeSet a) {
				    Enumeration e = a.getAttributeNames();
				    for ( ; e.hasMoreElements(); ) {
				        Object obj = e.nextElement();

				        if ("style".equalsIgnoreCase(obj.toString())) {
				        	Object obj2 = a.getAttribute(obj);
				            if (obj2 instanceof String) {
				                String sx = (String) obj2;
				                // width: 585px
				                int pos = sx.toUpperCase().indexOf("WIDTH:");
				                if (pos >= 0) {
				                	sx = com.viaoa.util.OAString.convert(sx, "width:", "ignore:", true);
				                	a.addAttribute(obj, sx);
				                }
				            }
				        }				        

				        if ("width".equalsIgnoreCase(obj.toString())) {
				            Object obj2 = a.getAttribute(obj);
				            if (obj2 instanceof String) {
				                String sx = (String) obj2;
				                if (sx.indexOf('%') < 0) {
				                    int x = com.viaoa.util.OAConv.toInt(sx);
				                    if (x > 200) a.removeAttribute(obj);
				                }
				            }
				            break;
				        }
				    }
				}

                public void handleEndTag(HTML.Tag t, int pos) {
                    super.handleEndTag(t, pos);
                    if (t == HTML.Tag.SPAN) {
                        bInSpan = false;
                        charAction.end(t);
                    }
                }

                public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                    super.handleSimpleTag(t, a, pos);
                    if (t == HTML.Tag.SPAN) {
                        if (bInSpan) handleEndTag(t, pos);
                        else handleStartTag(t, a, pos);
                    }
                }
            };
            return reader;
        }
    }


    /*  Used by ImageView
        When image is not found, then it calls Toolkit.getDefaultToolkit().getImage() which caches images.
        If the file image has changed, then the new image is never retreived.
    */
    class ImageCache extends Hashtable {
        public Object get(Object key) {
            Object result = super.get(key);
            if (result == null) {
                result = Toolkit.getDefaultToolkit().createImage((URL)key); // this will not use the Toolkit cache
                put(key, result);
            }
            return result;
        }
    }
    
    /**
     * This can be used to have image created/loaded from another soruce, and put into the directory
     * where it is expected to be.
     */
    protected void loadImage(String src) {
        // example: ServerDelegate.getImage(src);
    	
    }

}



