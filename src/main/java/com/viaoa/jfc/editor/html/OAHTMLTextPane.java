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
package com.viaoa.jfc.editor.html;


import java.io.*;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.OATable;
import com.viaoa.jfc.editor.html.control.FileImageHandler;
import com.viaoa.jfc.editor.html.control.Hub2ImageHandler;
import com.viaoa.jfc.editor.html.control.ImageHandlerInterface;
import com.viaoa.jfc.editor.html.control.OAHTMLTextPaneController;
import com.viaoa.jfc.editor.html.view.PrintBoxView;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.jfc.print.OAPrintUtil;
import com.viaoa.jfc.print.OAPrintable;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/*
    see changes for converting/reading HTML
     OAHTMLDocuemtn.getReader().handleStartTag

*/

/**
 * Text pane used for html styled editor.
 * 
 * Subclasses JTextPane and adds support for pasting and printing:
 * <ul>
 * <li>setBase can be used to set base location to a jar file url.
 * <li>Pasting will clean up html to make it compatible.
 * <li>Printing pages, managing page breaks
 * </ul>
 *
 * Links: 
 * http://java-sl.com/articles.html
 * http://java-sl.com/tips.html
 * 
 * http://java-sl.com/tips.html
      todo: replace smiles with image

 * 
 * Note: sizes are based on pixels. If point sizes were used in HTML, then they
 * are converted to pixel size. Java font sizes are based on pixel, printer font
 * sizes are based on point. <br>
 * 
 * 
 * From Font.getSize():
 * "The Java(tm)2D API adopts the convention that one point is equivalent to one unit in user coordinates"
 * 
 * When printing, the graphics is scaled using the printer/screen dpi settings.
 * For printing, the printer resolution is based on points, 72/inch. Screen dpi
 * can vary, common is 96 (windows).
 * <p>
 * IMPORTANT: need to convert from point to pixel whenever using PageFormat or
 * Paper. For printing, graphics.scale(x,x) is set to make it wysiwyg
 * <p>
 * 
 * 
 * !!! Important !!!! this is from javax.swing.text.html.CSS.java
 * http://docs.oracle.com/javase/6/docs/api/javax/swing/text/html/CSS.html
 * 
 * Defines a set of CSS attributes as a typesafe enumeration. The HTML View
 * implementations use CSS attributes to determine how they will render. This
 * also defines methods to map between CSS/HTML/StyleConstants. Any shorthand
 * properties, such as font, are mapped to the intrinsic properties.
 * <p>
 * The following describes the CSS properties that are supported by the
 * rendering engine: see: javax.swing.text.html.CSS.java
 * 
 * for sizes, values default to "px"
 * 
 * <ul>
 * <li>font-family
 * <li>font-style
 * <li>font-size (supports relative units) 
 * <li>font-weight  (450, normal, bold, etc)
 * <li>font
 * <li>color
 * <li>background-color (with the exception of transparent)
 * <li>background-image ex: style="background-image: url('oaproperty://com.tmgsc.hifive.model.oa.ImageStore/Bytes?3');"
 * <li>background-repeat ex: background-repeat: no-repeat;repeat,no-repeat,repeat-y,repeat-x
 * <li>background-position
 * <li>background
 * 
 * <img src='classpath://com.theice.tsam.view.image/icon.gif'>
 * <img src='oaproperty:://classPath/propName?Id'>  ???? verify that this is correct qqqqqqqqqqq
 * 
 * <li>width
 * <li>height
 * <li>text-decoration ex: underline, overline, not: overline,blink
 * <li>vertical-align (only sup and super)
 * <li>text-align left, center, right, justify=center
 * 
 * <li>margin
 * <li>margin-top
 * <li>margin-right
 * <li>margin-bottom
 * <li>margin-left
 * 
 * <li>padding
 * <li>padding-top
 * <li>padding-right
 * <li>padding-bottom
 * <li>padding-left
 * 
 * <li>border-style  (supports inset, outset, none, solid) 
 * <li>border-width  (only supports one size for all sides)
 * <li>border-color
 *           -top, -right, -bottom, -left 
 * 
 * 
 * ** NOTE **
 *  for <Table>, if the Border attr is used, then it's value will be used for all TD border-width,
 *      for any TD inside of it (even inner tables)
 *  set Table attribute "BORDER=0" to remove lines 
 * 
 * <li>list-style-type
 * <li>list-style-position
 * </ul>
 * 
 * 
 * 
 * The following are modeled, but currently not rendered.
 * <ul>
 * <li>font-variant
 * <li>background-attachment (background always treated as scroll)
 * <li>word-spacing
 * <li>letter-spacing
 * <li>text-indent
 * <li>text-transform
 * <li>line-height
 * <li>border-top-width (this is used to indicate if a border should be used)
 * <li>border-right-width
 * <li>border-bottom-width
 * <li>border-left-width
 * <li>border-width
 * <li>border-top
 * <li>border-right
 * <li>border-bottom
 * <li>border-left
 * <li>border
 * <li>width
 * <li>height
 * <li>float
 * <li>clear
 * <li>display
 * <li>white-space
 * <li>list-style
 * </ul>
 * <p>
 * <b>Note: for the time being we do not fully support relative units, unless
 * noted, so that p { margin-top: 10% } will be treated as if no margin-top was
 * specified.
 * 
 * ******* Tags/Attributes added by OA ******* 
 * <div pagebreak='no'> division block used where page breaks should not occur 
 * <div pagebreak='yes'> used to force a page break before 
 * <tr header> table row that should be repeated if table is multiple pages
 * 
 * 
 * Example: OAHTMLTextPane txt = tabExamVisit.getNoteHTMLTextPane();
 * OAHTMLTextPaneController contEditor = new OAHTMLTextPaneController(txt,
 * SpellCheckDelegate.getSpellChecker(), true);
 * contEditor.bind(model.getExams(), Exam.P_Note);
 * contEditor.createImageHandler(model.getExamImageStores(),
 * ImageStore.P_Bytes, ImageStore.P_OrigFileName,
 * ImageStore.P_Id); // or: contEditor.createFileImageHandler();
 * contEditor.setEnabled(model.getUserAccess(),
 * UserAccess.P_ExamEnabled); OAScroller scroller = new
 * OAScroller(contEditor.getToolBar());
 * tabExamVisit.getNotesPanel().add(scroller, BorderLayout.NORTH);
 * 
 * 
 * @see OAHTMLTextPaneController for binding, added features, usage and sample
 * 
 * @author vvia
 */
public class OAHTMLTextPane extends JTextPane implements OAPrintable {
    private static final long serialVersionUID = 1L;

    /* For printing, OAHTMLTextPane will use a PrintBoxView to set the width of pageFormat.width, which is converted from point size to pixel. When beforePrint() is called the graphics.setScale() will be set to map from from pixel (java ui coordinates) to point size (printer
     * coordinates). */

    private boolean bIsPrinting;
    private PrintBoxView printBoxView; // used to "re-render" to fit
                                       // pageFormat.width
    private Vector<PrintPage> vecPage; // tracks seperate pages

    // used to manage images
    private Class classForImages;
    private String pathForImages;
    private PageFormat pageFormat;
    private HashMap hmTableRowView; // used to store locations for table rows,
                                    // needed for tables that span multiple
                                    // pages
    private ImageHandlerInterface imageHandler;
    private int imageChangeCount;


    /**
     * Creates a new editor and installs an OAHTMLEditorKit.
     */
    public OAHTMLTextPane() {
        /* OACaret caret = new OACaret(2.0f); caret.setBlinkRate(500); setCaret(caret); */

        // this is so that CSS will use correct font sizes. This is based on
        // screen, not printer - so printing will have to "undo"
        // ex: A 12pt font will have a size of 16px (since Java fonts are in
        // pixel, not pt)
        putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.TRUE);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        final OAHTMLEditorKit editorKit = new OAHTMLEditorKit() {
            @Override
            protected Image getImage(String src, URL url) {
                return OAHTMLTextPane.this.getImage(src, url);
            }
        };
        setEditorKit(editorKit); // this will create default document

        // convert [space] to &nbsp;
        // this is used in combination with processKeyEvent(...)
        String cmd = "Space";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), cmd);
        this.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editorKit.getSpaceAction().actionPerformed(e);
            }
        });
    }

    /**
     * Used to set the base location for relative paths.
     * 
     * @param binDir
     *            ex: bin/com/scheduler
     * @param jarURI
     *            ex: jar:file:dispatcherlg.jar!/com/oldcastle/dispatcher
     */
    public void setBase(String binDir, String jarURI) throws Exception {
        File file = new File(com.viaoa.util.OAString.convertFileName(binDir));
        URL url;
        if (file.exists()) {
            url = file.toURI().toURL();
        }
        else {
            url = new URL(jarURI);
        }
        ((HTMLDocument) getDocument()).setBase(url);
    }

    private String newText;
    private Object LOCK_isPrinting = new Object();

    
    
    @Override
    public String getText() {
        String text = super.getText();
        
        if (text.indexOf("&lt;%=") >= 0) {
            text = OAString.convert(text, "&lt;%=", "<%=");
            text = OAString.convert(text, "%&gt;", "%>");
        }
        return text;
    }
    
    /**
     * Overwritten to first make sure that the document is not being printed.
     * 
     * @param text
     * @param bWaitIfPrinting
     */
    @Override
    public void setText(final String text) {
        try {
            _setText(text);
        }
        catch (Exception e) {
        }
    }
    protected void _setText(final String text) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            _setText1(text);
        }
        else {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    _setText1(text);
                }
            });
        }
    }
    protected void _setText1(String text) {
        for (int i=0; i<5; i++) {
            try {
                _setText2(text);
                break;
            }
            catch (Exception e) {
                // concurrency error, will retry up to 5 times
            }
        }
    }
    protected void _setText2(String text) {
        if (text == null) text = "";

        if (text.indexOf("<%=") >= 0) {
            text = OAString.convert(text, "<%=", "&lt;%="); 
            text = OAString.convert(text, "%>", "%&gt;"); 
        }
        
        synchronized (LOCK_isPrinting) {
            if (bIsPrinting) {
                newText = text; // will be set once printing has completed
                return;
            }
            newText = null;
        }

        // hack? remove any added styles, etc. by resetting the document
        // trying to reset or clear the styles causes problems
        setDocument(((OAHTMLEditorKit) getEditorKit()).createDefaultDocument());

        clearImageCache();
        printBoxView = null;
        super.setText(text);
    }

    // internally used to know if printBoxView is being used to render for
    // printing
    protected void setPrinting(boolean b) {
        synchronized (LOCK_isPrinting) {
            bIsPrinting = b;
        }
        if (b) {
            vecPage = new Vector<PrintPage>(); // break-down of start and stop
                                               // views for each page.
            hmTableRowView = new HashMap();
        }
        else {
            vecPage = null;
            printBoxView = null;
            hmTableRowView = null;

            // see if text was updated
            if (newText != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (newText != null) {
                            OAHTMLTextPane.this.setText(newText);
                        }
                    }
                });
            }
        }
    }

    /**
     * Overwritten to
     */
    @Override
    public void paste() {
        /** create a new transferable to filter using the the original contents */
        Clipboard clipboard = getToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(this);
        MyTransferable newContent = new MyTransferable(content);
        // newContent.bUsePlain = true; // qqqqqqqqqq dont try to use html in VP
        // qqqqqqq
        clipboard.setContents(newContent, null);
        for (;;) {
            try {
                super.paste();
                break;
            }
            catch (Exception e) {
                System.out.println("Paste Error: " + e);
            }
            if (newContent.bUsePlain) break;
            newContent.bUsePlain = true;
        }
    }

    /** used to cleanup clipboard data */
    class MyTransferable implements Transferable {
        DataFlavor htmlDataFlavor, textDataFlavor;
        String html, plain;
        boolean bUsePlain;

        public MyTransferable(Transferable content) {
            // see: http://forum.java.sun.com/thread.jsp?forum=57&thread=258757
            DataFlavor[] flavors = content.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                DataFlavor flavor = flavors[i];
                String mimeType = flavor.getMimeType();
                /* try { System.out.println(i+") "+mimeType+"  "+content.getTransferData (flavor));//qqqqqqqqqqq } catch (Exception e) { System.out.println ("Editor.getTransferDataFlavors() Error: "+e); } */

                if (mimeType.indexOf("java.lang.String") >= 0) {
                    if (mimeType.indexOf("text/plain") >= 0) {
                        textDataFlavor = flavor;
                        try {
                            plain = (String) content.getTransferData(flavor);
                            plain = plain.replace((char) 8216, '\'');
                            plain = plain.replace((char) 8217, '\'');
                            plain = plain.replace((char) 8220, '"');
                            plain = plain.replace((char) 8221, '"');
                            plain = plain.replace((char) 8211, '-');
                        }
                        catch (Exception e) {
                            System.out.println("Editor.getTransferDataFlavors() Error: " + e);
                        }
                    }
                    else if (mimeType.indexOf("text/html") >= 0) {
                        htmlDataFlavor = flavor;
                        try {
                            html = (String) content.getTransferData(flavor);

                            OAHTMLParser hp = new OAHTMLParser();
                            // boolean b = hp.isMicrosoft(html);
                            html = hp.removeBody(html);
                            // if (b) {
                            // Parse out Microsoft Word
                            html = hp.convert(html);
                            // }

                            html = "<html><body>" + html + "</body></html>";
                        }
                        catch (Exception e) {
                            System.out.println("Editor.getTransferDataFlavors() Error: " + e);
                        }
                    }
                }
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            int i = 0;
            if (textDataFlavor != null) i++;
            if (!bUsePlain && htmlDataFlavor != null) i++;

            DataFlavor[] dfs = new DataFlavor[i];
            i = 0;
            if (textDataFlavor != null) dfs[i++] = textDataFlavor;
            if (!bUsePlain && htmlDataFlavor != null) dfs[i] = htmlDataFlavor;
            return dfs;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            getTransferDataFlavors();
            return (flavor == this.textDataFlavor || (!bUsePlain && flavor == this.htmlDataFlavor));
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!bUsePlain && flavor == this.htmlDataFlavor) {
                return html;
            }
            else if (flavor == this.textDataFlavor) { return plain; }
            return null;
        }
    }

    /**
     * Used to reformat to fit page width for printing.
     */
    protected PrintBoxView createPrintBoxView(final int pageWidth, final int pageHeight) {
        if (SwingUtilities.isEventDispatchThread()) {
            createPrintBoxView2(pageWidth, pageHeight);
            return printBoxView;
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createPrintBoxView2(pageWidth, pageHeight);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return printBoxView;
    }

    protected PrintBoxView createPrintBoxView2(int pageWidth, int pageHeight) {
        if (printBoxView == null || printBoxView.getPageWidth() != pageWidth) {
            if (printBoxView == null) {
                printBoxView = new PrintBoxView(this, getDocument().getDefaultRootElement(), pageWidth, pageHeight);
            }
            printBoxView.setSize(pageWidth, Integer.MAX_VALUE / 2);
            printBoxView.getPreferredSpan(View.Y_AXIS); // this will load all
                                                        // children views
        }
        return printBoxView;
    }

    // ============== Printing =====================

    private boolean bBeforeMethodCalled;

    @Override
    public void beforePrint(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
        bBeforeMethodCalled = true;
        if (pageFormat == null) throw new IllegalArgumentException("pageFormat can not be null");
        setPrinting(true);
        int w = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth());
        int h = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());
        createPrintBoxView(w, h);
    }

    @Override
    public void afterPrint() {
        setPrinting(false);
        bBeforeMethodCalled = false;
    }

    private boolean bPreviewing;

    @Override
    public void beforePreview(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
        bBeforeMethodCalled = true;
        if (pageFormat == null) throw new IllegalArgumentException("pageFormat can not be null");
        setPrinting(true);
        bPreviewing = true;
        int w = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth());
        int h = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());
        createPrintBoxView(w, h);
    }

    @Override
    public void afterPreview() {
        printBoxView = null;
        setPrinting(false);
        bPreviewing = false;
        bBeforeMethodCalled = false;
    }

    @Override
    public Dimension getPrintSize(int pageIndex, PageFormat pageFormat, int width) {
        if (pageIndex >= 0 && vecPage != null && printBoxView != null) {
            int pageHeight = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());
            if (allocate(vecPage, printBoxView, width, pageHeight, pageIndex)) {
                PrintPage pp = vecPage.elementAt(pageIndex);
                int h = pp.bottom - pp.top;
                return new Dimension(width, h);
            }
        }
        return null;
    }

    @Override
    public int preview(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
        return doPrint(graphics, pageFormat, pageIndex);
    }

    @Override
    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
        if (!bIsPrinting) {
            if (pageFormat != null) beforePrint(pageFormat);
            bBeforeMethodCalled = false;
        }
        if (this.pageFormat != pageFormat && pageFormat != null) {
            // changes made to pageFormat, need to reload PrintBoxView
            setPrinting(false);
            setPrinting(true);
            int w = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth());
            int h = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());
            createPrintBoxView(w, h);
        }

        AffineTransform trans = null;
        this.pageFormat = pageFormat;
        if (!bPreviewing && graphics != null) {
            Graphics2D g = (Graphics2D) graphics;
            trans = g.getTransform();
            double scale = OAPrintUtil.getPixelToPointScale();
            g.scale(scale, scale); // printer uses point sizes, OAHTMLTextPane
                                   // uses pixels: scaling is best way to
                                   // wysiwyg
        }
        int x = doPrint(graphics, pageFormat, pageIndex);
        if (trans != null) {
            Graphics2D g = (Graphics2D) graphics;
            g.setTransform(trans); // set back
        }
        if (x == NO_SUCH_PAGE && !bBeforeMethodCalled) {
            afterPrint(); // in case not called using OAPrintable - which would
                          // call afterPrint()/Preview() when done.
        }
        return x;
    }

    protected int doPrint(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
        if (pageFormat == null || printBoxView == null) { return NO_SUCH_PAGE; }

        int pageWidth = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableWidth());
        int pageHeight = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableHeight());

        printBoxView.setPageHeight(pageHeight);

        if (!allocate(vecPage, printBoxView, printBoxView.getPageWidth(), pageHeight, pageIndex)) { return NO_SUCH_PAGE; }

        Graphics2D g = (Graphics2D) graphics;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        PrintPage pp = vecPage.elementAt(pageIndex);

        // PrintPage ppNext = null;
        // if ((pageIndex+1) < vecPage.size()) ppNext = (PrintPage)
        // vecPage.elementAt(pageIndex+1);
        PrintPage ppPrev = null;
        if (pageIndex > 0) ppPrev = (PrintPage) vecPage.elementAt(pageIndex - 1);

        // Rectangle rect = new
        // Rectangle(0,0,(int)boxView.getPreferredSpan(View.X_AXIS),
        // (int)boxView.getPreferredSpan(View.Y_AXIS));
        Rectangle rect = new Rectangle(0, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);

        boolean bBuffered = this.isDoubleBuffered();
        if (bBuffered) this.setDoubleBuffered(false);

        boolean bBuffered2 = RepaintManager.currentManager(this).isDoubleBufferingEnabled();
        if (bBuffered2) RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);

        int tx = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableX());
        int ty = (int) OAPrintUtil.convertPointsToPixels(pageFormat.getImageableY()) - pp.top;
        g.translate(tx, ty);
        g.setClip(0, pp.top, pageWidth, pageHeight);// this is needed, else
                                                    // boxView will throw an NPE

        // Note: if a table span multiple pages, then top row is reprinted as
        // the heading
        // NOTE: this will print a Table title row, since the table spans more
        // then one page
        if (pp.viewHeading != null) {
            Rectangle r = (Rectangle) hmTableRowView.get(pp.viewHeading);
            r.y = pp.top;
            // width & height are not correct in "r"
            r.width = (int) pp.viewHeading.getPreferredSpan(View.X_AXIS);
            r.height = (int) pp.viewHeading.getPreferredSpan(View.Y_AXIS);

            pp.viewHeading.paint(g, r);
            g.translate(0, r.height);
            ty += r.height;
        }

        paintViews(g, printBoxView, rect, pp.top, pp.bottom, pageHeight);

        g.translate(-tx, -ty);

        if (bBuffered) this.setDoubleBuffered(bBuffered);
        if (bBuffered2) RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
        return PAGE_EXISTS;
    }

    protected boolean allocate(Vector vec, BoxView boxView, int w, int h, int pageIndex) {
        if (pageIndex < vec.size()) {
            PrintPage pp = (PrintPage) vec.elementAt(pageIndex);
            if (pp != null) return true;
        }
        int top = 0;
        int bottom = (h - 1);
        int page = 0;

        if (pageIndex > 0 && vec.size() > 0) {
            page = Math.min(vec.size() - 1, pageIndex - 1);
            PrintPage pp = (PrintPage) vec.elementAt(page);
            top = pp.bottom + 1;
            bottom = top + (h - 1);
            page++;
        }

        Rectangle rect = new Rectangle(0, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);

        for (; page <= pageIndex; page++) {
            PrintPage pp = new PrintPage();
            pp.top = top;
            pp.bottom = bottom;

            View viewPageBreak = null;
            if (page > 0) {
                PrintPage ppx = (PrintPage) vec.elementAt(page - 1);
                viewPageBreak = ppx.viewPageBreak;
            }

            if (viewPageBreak != null) {
                // qqqqqqqq
                int xx = 4;
                xx++;
            }

            allocateViews(boxView, rect, pp, h, viewPageBreak);

            if (pp.lowestFound == 0) { return false; }
            if (pp.bottom != bottom && pp.viewPageBreak == null) {
                pp.lowestFound = 0;
                allocateViews(boxView, rect, pp, h, viewPageBreak);
            }
            pp.bottom = pp.lowestFound;
            vec.addElement(pp);
            top = pp.bottom + 1;
            bottom = top + (h - 1);
        }
        return true;
    }

    class PrintPage {
        int top;
        int bottom; // bottom to use, initially set to top+heightOfPage, then
                    // set to lowest value once allocateViews is called.
        int lowestFound; // lowest bottom found by allocateViews
        View viewHeading; // for table heading row, for tables that span
                          // multiple rows
        View viewFirst;
        View viewPageBreak; // first child that should be used, based on
                            // previous pageBreak
    }

    protected boolean allocateViews(View view, Rectangle rect, PrintPage pp, int pageHeight, View viewStart) {
        return allocateViews(view, rect, pp, pageHeight, viewStart, false);
    }

    /**
     * @param viewStart
     *            the View that will be at the top of the page
     * @param bIgnorePageBreaks
     *            true if this is already in a pageBreak
     * @return
     */
    protected boolean allocateViews(View view, Rectangle rect, PrintPage pp, int pageHeight, View viewStart, boolean bIgnorePageBreaks) {
        int x = view.getViewCount();
        boolean bFoundStartView = (viewStart == null || view == viewStart);
        bIgnorePageBreaks = bIgnorePageBreaks || view == viewStart;
        for (int i = 0; i < x; i++) {
            Rectangle rectChild = (Rectangle) view.getChildAllocation(i, rect);
            if (rectChild == null) continue;

            // 1: view on previous page
            if ((rectChild.y + rectChild.height) <= pp.top) continue;

            // 2: the start for this view is after the bottom
            if (rectChild.y > pp.bottom) continue;

            View viewChild = view.getView(i);
            if (!bFoundStartView) bFoundStartView = viewStart == viewChild;

            // need to store first table rows, in case the table expands
            // multiple pages, so that the first row (heading) can be reprinted
            if (bFoundStartView && viewChild.getClass().getName().equalsIgnoreCase("javax.swing.text.html.TableView$RowView")) {
                hmTableRowView.put(viewChild, rectChild);
            }

            // need to check to see if first view is in a table that spans
            // multiple pages - so that the first row (heading) can be reprinted
            // on this page.
            if (bFoundStartView && pp.viewFirst == null && pp.top > 0 && rectChild.y >= pp.top) {
                pp.viewFirst = viewChild;
                setTableHeadingView(pp, viewChild);
            }

            boolean bNoBr = bFoundStartView && ((viewChild.getElement().getAttributes().getAttribute("nobr") != null) || (viewChild.getViewCount() == 0));

            String valx = (String) viewChild.getElement().getAttributes().getAttribute("pagebreak");
            if (valx != null) {
                int xx = 4;
                xx++;
            }

            boolean bPageBreak = false;
            if (!bIgnorePageBreaks && bFoundStartView && viewChild != viewStart) {
                String val = (String) viewChild.getElement().getAttributes().getAttribute("pagebreak");
                if (val != null) {
                    boolean b = val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true");
                    if (b) bPageBreak = true;
                    else bNoBr = true;
                }
            }
            // 3: if view is greater then page height
            boolean bJumbo = (bNoBr && rectChild.height > pageHeight);

            // 3.5 20090808 pagebreaks
            if (bPageBreak) {
                // 20120530 have the pagebreak before the tag
                pp.lowestFound = rectChild.y;
                // was: pp.lowestFound = (rectChild.y + (rectChild.height-1));
                pp.viewPageBreak = viewChild;
                break;
            }

            // 4: fits on current page
            if (bJumbo || (rectChild.y >= pp.top && ((rectChild.y + (rectChild.height - 1)) <= pp.bottom))) {
                pp.lowestFound = Math.max(pp.lowestFound, (rectChild.y + (rectChild.height - 1)));
            }
            else {
                // 5: leaf view, cant fit on this page, need to force page
                // break. Adjust bottom so that this will show on next page
                if (bNoBr) {
                    if (rectChild.y >= pp.top && pp.bottom >= rectChild.y) pp.bottom = (rectChild.y - 1); 
                    // note: this will cause allocateViews to rerun, to verify new bottom
                    continue;
                }
            }

            int holdBottom = pp.bottom;
            bFoundStartView = allocateViews(viewChild, rectChild, pp, pageHeight, (bFoundStartView && viewChild != viewStart) ? null : viewStart, bIgnorePageBreaks);
            if (pp.viewPageBreak != null) break;
            if (holdBottom != pp.bottom) break; // bottom has changed need to
                                                // rerun
        }
        return bFoundStartView;
    }

    // 20100218
    private void setTableHeadingView(PrintPage pp, View view) {
        if (view == null) return;

        for (;;) {
            view = view.getParent();
            if (view == null) break;

            if (view.getClass().getName().equalsIgnoreCase("javax.swing.text.html.TableView")) {
                // Note: table view is not public ?!?
                // view is an html table
                break;
            }
        }
        if (view == null) return;

        // get first row and set pp.viewHeading, and adjust pp.bottom

        int x = view.getViewCount();
        for (int i = 0; i < x; i++) {
            View viewChild = view.getView(i);
            if (viewChild.getClass().getName().equalsIgnoreCase("javax.swing.text.html.TableView$RowView")) {

                // if <TR> has an "header" attribute, then use it as header for
                // multiple page tables
                Object objx = viewChild.getElement().getAttributes().getAttribute("header");
                // ex: objx =
                // viewChild.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
                if (objx != null) {
                    pp.viewHeading = viewChild;
                    int h = (int) pp.viewHeading.getPreferredSpan(View.Y_AXIS);
                    pp.bottom -= h;
                    break;
                }
            }
        }
    }

    /**
     * Paints all views between top and bottom for a page.
     */
    protected int paintViews(Graphics g, View view, Rectangle rect, int top, int bottom, int pageHeight) {
        int xx = 0;
        int x = view.getViewCount();
        for (int i = 0; i < x; i++) {
            Rectangle rectChild = (Rectangle) view.getChildAllocation(i, rect);
            if (rectChild == null) continue; // no width and no height

            // 1: view on previous page
            if ((rectChild.y + rectChild.height) <= top) continue;

            // 2: the start for this view is after the bottom
            if (rectChild.y > bottom) continue;

            View viewChild = view.getView(i);

            boolean bNoBr = (viewChild.getElement().getAttributes().getAttribute("nobr") != null) || (viewChild.getViewCount() == 0);
            if (bNoBr && rectChild.y < top) continue; // already printed

            // 3: if view is greater then page height
            // was: boolean bJumbo = (viewChild.getViewCount() == 0 &&
            // rectChild.height > pageHeight);
            boolean bJumbo = (bNoBr && rectChild.height > pageHeight);

            // 4: fits on current page
            if (bJumbo || (rectChild.y >= top && ((rectChild.y + (rectChild.height - 1)) <= bottom))) {
                xx++;
                viewChild.paint(g, rectChild);
                continue;
            }

            // 4.1:
            if (bNoBr) continue;

            // find children that are within range
            xx += paintViews(g, viewChild, rectChild, top, bottom, pageHeight);
        }
        return xx;
    }

    // =========================
    // Image Handling
    // =========================

    public void setImageHandler(ImageHandlerInterface hand) {
        this.imageHandler = hand;
    }

    public ImageHandlerInterface getImageHandler() {
        return this.imageHandler;
    }

    /**
     * Clears the document imageCache
     * 
     * @see OAHTMLDocument
     */
    public void clearImageCache() {
        ((OAHTMLDocument) getDocument()).clearImageCache();
    }

    /**
     * Used to define the default location for finding images.
     * 
     * @param clazz
     *            Class used for classLoaded.getResourceAsStream
     * @param dirPath
     *            path to used to find image, use "/" as separator
     */
    public void setImageLoader(Class clazz, String dirPath) {
        if (clazz == null) dirPath = null;
        classForImages = clazz;
        pathForImages = dirPath;
        if (pathForImages != null && !pathForImages.endsWith("/")) pathForImages += "/";
    }

    public Class getImageLoaderClass() {
        return classForImages;
    }

    public String getImageLoaderDirectory() {
        return pathForImages;
    }

    /**
     * 
     * @param srcName
     * @param img
     * @throws Exception
     * @see #setImageHandler
     * @see ImageHandlerInterface#onInsertImage(String, Image)
     */
    public void insertImage(String srcName, Image img) throws Exception {
        imageChangeCount++;
        if (imageHandler != null) {
            String s = imageHandler.onInsertImage(srcName, img);
            if (s != null) srcName = s;
        }
        MutableAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
        attr.addAttribute(HTML.Attribute.SRC, srcName);
        // attr.addAttribute(HTML.Attribute.HEIGHT,
        // Integer.toString(img.getHeight(null)));
        // attr.addAttribute(HTML.Attribute.WIDTH,
        // Integer.toString(img.getWidth(null)));
        int pos = getCaretPosition();
        try {
            getDocument().insertString(pos, " ", attr);
        }
        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        Dictionary cache = (Dictionary) getDocument().getProperty(OAHTMLDocument.IMAGE_CACHE_PROPERTY);
        if (cache != null) {
            cache.put(srcName, img);
        }
    }

    public void insertString(String text) {
        int pos = getCaretPosition();
        insertString(text, pos);
    }
    public void insertString(String text, int pos) {
        try {
            if (text == null || text.length()== 0) return;
            
            MutableAttributeSet as = getInputAttributes();
            Document doc = getDocument();
            
            // vv hack: there is a problem when inserting html 1: at end of line, or 2: begin of document
            //    the HTML will need to take up a single ' '/space in the doc, but wont be rendered as a space
            doc.insertString(pos, "  ", new SimpleAttributeSet(as)); // adds "fake" chars so that insert is not at begin/end of line

            doc.insertString(pos+1, text, as);
            
            doc.remove(pos, 1);  
            doc.remove(pos+text.length(), 1); 
        }
        catch (Exception ex) {
            System.out.println("OAHTMLTextPane.insertString() exception:"+ex);
            ex.printStackTrace();
        }
    }
    
    /* public void insertDiv_TEST() { if ((!isEditable()) || (!isEnabled())) { UIManager.getLookAndFeel().provideErrorFeedback(this); return; }
     * 
     * 
     * int pos = getCaretPosition(); SimpleAttributeSet tempSet = new SimpleAttributeSet(); tempSet.addAttributes(getInputAttributes());
     * 
     * OAHTMLDocument doc = (OAHTMLDocument) getDocument(); OAHTMLEditorKit kit = (OAHTMLEditorKit) getEditorKit();
     * 
     * TextAction xxz;
     * 
     * int xx = 4; String sx=""; try { sx = doc.getText(0, doc.getLength()); sx = doc.getText(pos, doc.getLength()-pos);
     * 
     * 
     * 
     * Element elem = doc.getParagraphElement(0); elem = doc.getCharacterElement(0); elem = doc.getCharacterElement(pos);
     * 
     * 
     * 
     * 
     * xx++; } catch(Exception e) { xx++; } / * try { kit.insertHTML(doc, pos, "<div></div>", 0, 0, HTML.Tag.DIV); //setCaretPosition(newPos); } catch (Exception ex) { System.out.println("Exception: "+ex); ex.printStackTrace(); } if (true) return; /
     * 
     * //qqqqq this is flaky
     * 
     * Element elem = doc.getParagraphElement(pos);
     * 
     * 
     * 
     * AttributeSet as = elem.getAttributes(); SimpleAttributeSet sas = new SimpleAttributeSet(as); sas.removeAttribute(StyleConstants.NameAttribute); sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.DIV);
     * 
     * int x1 = elem.getStartOffset(); int x2 = elem.getEndOffset();
     * 
     * replaceSelection("\n"); if (pos == x1 && pos+1 == x2) { } else if (pos == x1) { } else if (pos+1 == x2) { pos++; } else { replaceSelection("\n"); pos++; } ((StyledDocument)getDocument()).setParagraphAttributes(pos, 0, sas, true); setCaretPosition(pos);
     * 
     * MutableAttributeSet ia = getInputAttributes(); ia.removeAttributes(ia); ia.addAttributes(tempSet);
     * 
     * // Element elem = doc.getParagraphElement(pos); } */

    /**
     * insert a div that has an background image, and the div is sized to fit the image.
     * This can then be used to place data over the image, like a certficate.
     */
    /* public void insertImageDiv_TESTONLY(String srcName, Image img) throws Exception { if (img == null || srcName == null) return; imageChangeCount++; if (imageHandler != null) { String s = imageHandler.onInsertImage(srcName, img); if (s != null) srcName = s; } else if
     * (srcName.indexOf("://") < 0) { // convert file name to url compatible srcName = srcName.replace('\\', '/'); // srcName = "file:///" + srcName; / * There is a problem with some jpeg files, where a jdk decoder is not found. images for background-image are retrieved by
     * CSS.getImage, which is different then <img> which are controlled by imageHandler
     * 
     * oaimage protocol will fix this by using OAImageUtil to load the image / srcName = "oaimage:///" + srcName; } OAImageUtil.loadImage(img); int w = img.getWidth(null); int h = img.getHeight(null);
     * 
     * String style = "background-image:url(\"" + srcName + "\"); background-repeat:no-repeat;"; style += " height:" + h + "; width:" + w + ";"; //qq // insertDiv(); // put the image in a div
     * 
     * int pos = getCaretPosition(); OAHTMLDocument doc = (OAHTMLDocument) getDocument(); Element elem = doc.getParagraphElement(pos);
     * 
     * AttributeSet as = elem.getAttributes(); SimpleAttributeSet sas = new SimpleAttributeSet(as);
     * 
     * AttributeSet asx = doc.getStyleSheet().getDeclaration(style); sas.addAttributes(asx); / * if (as.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMPLIED) { sas.removeAttribute(StyleConstants.NameAttribute); sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
     * } /
     * 
     * doc.setParagraphAttributes(pos, 0, sas, true);
     * 
     * Dictionary cache = (Dictionary) getDocument().getProperty(OAHTMLDocument.IMAGE_CACHE_PROPERTY); if (cache != null) { cache.put(srcName, img); } } */
    /**
     * This can be overwritten to intercept any images that are updated, so that
     * it can be stored in a central location.
     * 
     * @see ImageHandlerInterface#onUpdateImage(String, Image)
     */
    public void updateImage(String srcName, Image img) {
        imageChangeCount++;
        if (imageHandler != null) {
            imageHandler.onUpdateImage(srcName, img);
        }
        Dictionary cache = (Dictionary) getDocument().getProperty(OAHTMLDocument.IMAGE_CACHE_PROPERTY);
        if (cache != null && srcName != null && img != null) {
            cache.put(srcName, img);
        }
    }

    /**
     * Counter used to track how many ties updateImage has been called, to know
     * if there has been any image changes.
     */
    public int getImageChangeCount() {
        return imageChangeCount;
    }

    /**
     * This is called from the MyImageView to get an image associated with an
     * image tag. It will first use the image cache, and if not found will call
     * loadImage.
     * 
     * @see #loadImage(String, URL)
     */
    public Image getImage(String name, URL url) {
        // System.out.println("OAHTMLTextPane.getImage(), name="+name+", url="+url);
        if (name == null) return null;
        Image newImage = null;
        Dictionary cache = (Dictionary) getDocument().getProperty(OAHTMLDocument.IMAGE_CACHE_PROPERTY);
        if (cache != null) {
            newImage = (Image) cache.get(name);
            if (newImage != null) return newImage;
        }
        newImage = loadImage(name, url);
        if (newImage != null && cache != null) {
            cache.put(name, newImage);
        }
        return newImage;
    }

    /**
     * Called by getImage. This will call imageHandler first. This method can be
     * used as a hook, so that an application can retrieve images that are used
     * in a document.
     * 
     */
    protected Image loadImage(String name, URL url) {
        Image newImage = null;
        if (imageHandler != null) {
            newImage = imageHandler.loadImage(name);
            if (newImage != null) return newImage;
        }

        if (classForImages != null) {
            URL urlNew = classForImages.getResource(pathForImages + name);
            if (urlNew != null) {
                ImageIcon ii = new ImageIcon(urlNew);
                if (ii != null) return ii.getImage();
            }
        }

        if (url != null) {
            String s = url.getProtocol();
            if (s != null && s.equalsIgnoreCase("file")) {
                File f = new File(url.getPath());
                if (f.exists()) {
                    String path = f.getPath();
                    // newImage = Toolkit.getDefaultToolkit().createImage(path);
                    try {
                        newImage = OAImageUtil.loadImage(f);
                    }
                    catch (Exception e) {
                        // qqqqqq
                    }
                }
            }
            if (newImage == null) {
                newImage = Toolkit.getDefaultToolkit().createImage(url);
            }
        }
        return newImage;
    }

    /***
     * qqqqqq int qqq=0;
     * 
     * @Override public Dimension getPreferredSize() { Dimension d =
     *           getPreferredSizeX(); //Dimension d = super.getPreferredSizeX();
     *           System.out.println((qqq++)+" OAHTMLTextPane get pref "+d); if
     *           (d.width > 800) { d = getPreferredSizeX(); //d =
     *           super.getPreferredSize(); int x = 4; x++; } return d; }
     * 
     * 
     *           public boolean XX_getScrollableTracksViewportWidth() { return
     *           true; }
     * @Override public void setSize(Dimension d) {
     *           System.out.println((qqq++)+" OAHTMLTextPane setSize "+d);
     *           super.setSize(d); }
     * @Override public void setSize(int width, int height) {
     *           System.out.println(
     *           (qqq++)+" OAHTMLTextPane setSize2 "+width+","+height); if
     *           (width > 800) { int x = 4; x++; } super.setSize(width, height);
     *           }
     * 
     *           public Dimension getPreferredSizeX() { Dimension d =
     *           super.getPreferredSize();
     * 
     *           if (getParent() instanceof JViewport) { JViewport port =
     *           (JViewport)getParent(); TextUI ui = getUI(); int prefWidth =
     *           d.width; int prefHeight = d.height; if (!
     *           getScrollableTracksViewportWidth()) { int w = port.getWidth();
     *           Dimension min = ui.getMinimumSize(this); if (w != 0 && w <
     *           min.width) { // Only adjust to min if we have a valid size
     *           prefWidth = min.width; } } if (!
     *           getScrollableTracksViewportHeight()) { int h =
     *           port.getHeight(); Dimension min = ui.getMinimumSize(this); if
     *           (h != 0 && h < min.height) { // Only adjust to min if we have a
     *           valid size prefHeight = min.height; } } if (prefWidth !=
     *           d.width || prefHeight != d.height) { d = new
     *           Dimension(prefWidth, prefHeight); } } return d; }
     * @Override public Dimension getMinimumSize() { Dimension d =
     *           super.getMinimumSize();
     *           System.out.println((qqq++)+" get min "+d); return d; }
     * @Override public Dimension getMaximumSize() { Dimension d =
     *           super.getMaximumSize();
     *           System.out.println((qqq++)+" get max "+d); return d; }
     * @Override public void setPreferredSize(Dimension preferredSize) {
     *           System.out.println((qqq++)+" set pref "+preferredSize);
     *           super.setPreferredSize(preferredSize); }
     * @Override public void setMinimumSize(Dimension minimumSize) {
     *           System.out.println((qqq++)+" set min "+minimumSize);
     *           super.setMinimumSize(minimumSize); }
     * @Override public void setMaximumSize(Dimension maximumSize) {
     *           System.out.println((qqq++)+" set max "+maximumSize);
     *           super.setMaximumSize(maximumSize); }
     ***/

    protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyChar() == ' ' && e.getID() == KeyEvent.KEY_TYPED) {
            // super.processKeyEvent(e);
            e.consume();
        }
        else super.processKeyEvent(e);
    }

    /**
     * Base/root class to use when inserting Field tags.
     * Set to non-OAObject class to to have it disabled.
     */
    public void setFieldClass(Class c) {
        getController().setFieldClass(c);
    }
    /**
     * Get the class to use for the list of Fields that a user can insert into a document.
     * If fieldClass was not set by calling setFieldClass(..), then it will look for 
     * a method named "get"+propertyPath+"FieldClass" from the current Hub's class.
     * If not found then it will use the Hub's class.
     * @return
     */
    public Class getFieldClass() {
        return getController().getFieldClass();
    }    
    
    /* was
     * 
     * @Override public JFCController getController() { return control; } HTMLTextPaneController control; public void setController(HTMLTextPaneController control) { this.control = control; } */

    private OAHTMLTextPaneController control;

    public OAHTMLTextPaneController getController() {
        if (control == null) {
            control = new OAHTMLTextPaneController(this, null, true) {
                @Override
                protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
                    return OAHTMLTextPane.this.isEnabled(bIsCurrentlyEnabled);
                }

                @Override
                protected String isValid(Object object, Object value) {
                    return OAHTMLTextPane.this.isValid(object, value);
                }

                @Override
                protected boolean isVisible(boolean bIsCurrentlyVisible) {
                    return OAHTMLTextPane.this.isVisible(bIsCurrentlyVisible);
                }

            };
        }
        return control;
    }

    public void setSpellChecker(SpellChecker spellChecker) {
        getController().setSpellChecker(spellChecker);
    }

    public SpellChecker setSpellChecker() {
        return getController().getSpellChecker();
    }

    /**
     * Other Hub/Property used to determine if component is enabled.
     * 
     * @param hub
     * @param prop
     *            if null, then only checks hub.AO, otherwise will use
     *            OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub, String prop) {
        getController().setEnabled(hub, prop);
    }

    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }

    /**
     * removed, to "not use" the enabledController, need to call it directly -
     * since it has 2 params now, and will need to be turned on and off
     * 
     * @Override public void setEnabled(boolean b) { if (getController() != null
     *           && getController().getBindController() != null) {
     *           getController(
     *           ).getBindController().getEnabledController().directlySet(true,
     *           b); } super.setEnabled(b); }
     */
    /**
     * Other Hub/Property used to determine if component is visible.
     * 
     * @param hub
     * @param prop
     *            if null, then only checks hub.AO, otherwise will use
     *            OAConv.toBoolean to determine.
     */
    public void setVisible(Hub hub, String prop) {
        getController().setVisible(hub, prop);
    }

    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    protected String isValid(Object object, Object value) {
        return null;
    }

    /**
     * 
     * @param hub
     *            Hub that has all images in it.
     * @param byteArrayPropertyName
     *            name of property that is for a byte[] to store "raw"
     * @param sourceNamePropertyName
     *            property to use to store the source of the image (ex: file
     *            name)
     * @param idPropertyName
     *            unique property value to use for the html img src tag. If
     *            null, then sourceNamePropertyName will be used.
     * @see Hub2ImageHandler
     */
    public void createImageHandler(Hub<?> hub, String byteArrayPropertyName, String sourceNamePropertyName, String idPropertyName) {
        getController().createImageHandler(hub, byteArrayPropertyName, sourceNamePropertyName, idPropertyName);
    }

    public void createImageHandler(Class<? extends OAObject> clazz, String byteArrayPropertyName, String sourceNamePropertyName, String idPropertyName) {
        getController().createImageHandler(clazz, byteArrayPropertyName, sourceNamePropertyName, idPropertyName);
    }

    /**
     * Create file image handler.
     * 
     * @see FileImageHandler
     */
    public void createFileImageHandler() {
        getController().createFileImageHandler();
    }

    protected void bind(Hub hub, String property) {
        getController().bind(hub, property);
    }

    public JToolBar getToolBar() {
        return getController().getToolBar();
    }

    /**
     * 
     * @param hub
     * @param property
     * @see #setSpellChecker()
     * @see #createImageHandler(Hub, String, String, String)
     * @see #createFileImageHandler()
     * @see #setEnabled(Hub, String)
     * @see #setVisible(Hub, String)
     * @see #getToolBar()
     */
    public OAHTMLTextPane(Hub hub, String property) {
        this();
        bind(hub, property);
    }

    private int prefCols;
    private int prefRows;

    public void setPreferredSize(int rows, int cols) {
        prefCols = ((cols > 0) ? cols : 1);
        prefRows = ((rows > 0) ? rows : 1);
        calcPreferredSize();
    }

    protected void calcPreferredSize() {
        int h = OATable.getCharHeight(this, getFont());
        h *= prefRows;

        int w = OATable.getCharWidth(this, getFont(), prefCols);

        // setPreferredScrollableViewportSize(new Dimension(w, h));
        setPreferredSize(new Dimension(w, h));
    }

    // 20130225 fixes bug that wont save a last empty paragraph. see: http://java-sl.com/tip_html_kit_last_empty_par.html
    @Override
    public void write(Writer out) throws IOException {
        Document doc = getDocument();
        try {
            getUI().getEditorKit(this).write(out, doc, 0, doc.getLength()+1); //the last +1 did the trick
        } 
        catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public Hub<String> getCustomFields() {
        return getController().getCustomFields();
    }
    public void addDefaultCustomFields() {
        getController().addDefaultCustomFields();
    }

}
