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


import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.html.HTMLDocument;

import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
 * Creates a printable report, that uses html as the data to create a report with heading and footing.
 * Use setData, setHeading, setFooting to set the Html data for each.
 * Use setView if an HtmlPane is going to be used for the data portion of the report.
 *
 * Uses an HtmlConverter to convert tags with values.
 * The html code uses special tags "&lt;%= ? %&gt;", where "?" is the name of the Property name, or object property path.
 * By using setProperties and setObject, you can set where the data is retrieved from.
 * Use a "$" prefix in tag name to have it use an internal tag, or a value from the setProperties.
 * Otherwise, the value of the tag will be taken from the object, using the name as the property path.
 * Uses an HtmlConverter to convert special tags to values.  HtmlConverter allows for using loops
 *   and if statments.
 * Internally sets values for $DATE, $TIME, $PAGE parameters
 * 
 * 
 * 
 * Sets up a header, detail and footer report using OAHTMLTextPanes.
 * <p>
 * Note: the OAHTMLTextPanes must be assigned for them to be used.
 * @author vincevia
 */
public class OAHTMLReport extends OAReport {

    private static Logger LOG = Logger.getLogger(OAHTMLReport.class.getName());
    
    public static final String NL = System.getProperty("line.separator");     
    
    protected OAHTMLTextPane txtDetail;
    private OAHTMLTextPaneHeader txtHeader, txtTitleHeader;
    private OAHTMLTextPaneFooter txtFooter;
    
    protected OAProperties properties;
    private String htmlTitleHeader, htmlHeader, htmlDetail, htmlFooter;
    protected OAHTMLConverter htmlConverterTitleHeader;
    protected OAHTMLConverter htmlConverterHeader;
    protected OAHTMLConverter htmlConverterDetail;
    protected OAHTMLConverter htmlConverterFooter;
    
    private OAObject obj;
    private Hub hub;
    
    /**
     * Used to create HTML report, using a OAHTMLTextPane for detail, and HTMLConverter for header/footer
     * @param txtDetail
     * @param htmlHead
     * @param htmlFoot
     * @param props
     */
    public OAHTMLReport(OAHTMLTextPane txtDetail, String htmlHead, String htmlFoot, OAProperties props) {
        this.properties = props;
        setup();
        setDetailTextPane(txtDetail);
        setTitleHeaderHTML(htmlHead);
        setFooterHTML(htmlFoot);
    }
    
    /**
     * Used to create HTML report, using an HTMLConverter for header/detail/footer
     * @param txtDetail HTML for detail
     * @param htmlHead heading for first page  
     * @param htmlHead2 heading for additional pages.  
     * @param htmlDetail
     * @param htmlFoot
     * @param props Properties to use for "$" vars in the HTML ex: &lt;%=$Name%&gt;
     */
    public OAHTMLReport(OAHTMLTextPane txtDetail, String htmlHead, String htmlHead2, String htmlDetail, String htmlFoot, OAProperties props) {
        this.properties = props;
        setup();
        setDetailTextPane(txtDetail);
        setTitleHeaderHTML(htmlHead);
        setHeaderHTML(htmlHead2);
        setDetailHTML(htmlDetail);
        setFooterHTML(htmlFoot);
    }

    public OAHTMLReport(OAHTMLTextPane txtDetail, String htmlHead, String htmlDetail, String htmlFoot, OAProperties props) {
        this.properties = props;
        setup();
        setDetailTextPane(txtDetail);
        setTitleHeaderHTML(htmlHead);
        setDetailHTML(htmlDetail);
        setFooterHTML(htmlFoot);
    }

    public OAHTMLReport(OAHTMLTextPane txtDetail) {
        setup();
        setDetailTextPane(txtDetail);
    }

    public OAHTMLReport() {
        setup();
    }
    
    public void setTitleHeaderHTML(String htmlTitleHeader) {
        this.htmlTitleHeader = htmlTitleHeader;
        htmlConverterTitleHeader.setHtmlTemplate(htmlTitleHeader);
    }
    public String getTitleHeaderHTML() {
        return htmlConverterTitleHeader.getHtmlTemplate();
    }
    public void setHeaderHTML(String htmlHeader) {
        this.htmlHeader = htmlHeader;
        htmlConverterHeader.setHtmlTemplate(htmlHeader);
    }
    public String getHeaderHTML() {
        return htmlConverterHeader.getHtmlTemplate();
    }
    public void setFooterHTML(String htmlFooter) {
        this.htmlFooter = htmlFooter;
        htmlConverterFooter.setHtmlTemplate(htmlFooter);
    }
    public String getFooterHTML() {
        return htmlConverterFooter.getHtmlTemplate();
    }
    public void setDetailHTML(String html) {
        this.htmlDetail = html;
        htmlConverterDetail.setHtmlTemplate(htmlDetail);
    }
    public String getDetailHTML() {
        return htmlConverterDetail.getHtmlTemplate();
    }
    
    
    protected OAHTMLConverter createHTMLConverter() {
        OAHTMLConverter htmlConverter = new OAHTMLConverter() {
            @Override
            protected String getValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props) {
                String value = super.getValue(obj, propertyName, width, fmt, props);
                return OAHTMLReport.this.getValue(value, obj, propertyName, width, fmt, props);
            }
            @Override
            protected Object getProperty(OAObject oaObj, String propertyName) {
                Object obj = super.getProperty(oaObj, propertyName);
                return OAHTMLReport.this.getProperty(obj, oaObj, propertyName);
            }
        };
        return htmlConverter;
    }
    
    protected void setup() {
        if (htmlConverterTitleHeader != null) return;
        htmlConverterTitleHeader = createHTMLConverter();
        htmlConverterHeader = createHTMLConverter();
        htmlConverterDetail = createHTMLConverter();
        htmlConverterFooter = createHTMLConverter();
        
        txtTitleHeader = new OAHTMLTextPaneHeader() {
            @Override
            public String getText(int pageIndex) {
                String s = htmlConverterTitleHeader.getHtml(obj, hub, properties);
                return s;
            }
        };
        setTitleHeader(txtTitleHeader);

        txtHeader = new OAHTMLTextPaneHeader() {
            @Override
            public String getText(int pageIndex) {
                String s = htmlConverterHeader.getHtml(obj, hub, properties);
                return s;
            }
        };
        setHeader(txtHeader);
        
        txtFooter = new OAHTMLTextPaneFooter() {
            @Override
            public String getText(int pageIndex) {
                String s = htmlConverterFooter.getHtml(obj, hub, properties);
                return s;
            }
        };
        setFooter(txtFooter);
    }
    
    public void setHub(Hub hub) {
        this.hub = hub;
    }
    public void setObject(OAObject obj) {
        this.obj = obj;
    }
    public Object getObject() {
        return this.obj;
    }
    
    static final String refreshMessage = "<html><br><center>Building report ...";
    
    /**
     * Updates the Detail component.  Must be manually called
     */
    public void refreshDetail() {
        if (getDetailTextPane() == null) {
            return;
        }
        
        
        if (txtDetail == null || htmlDetail == null) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getDetailTextPane().setText(refreshMessage);
                    }
                });
            }
            else {
                getDetailTextPane().setText(refreshMessage);
            }
            return;
        }
        
        
//qqqqqqqqqqqq        
        getDetailTextPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            String txt;
            @Override
            protected Void doInBackground() throws Exception {
                txt = htmlConverterDetail.getHtml(obj, hub, properties);
                return null;
            }
            @Override
            protected void done() {
                getDetailTextPane().setText(txt);
                getDetailTextPane().getCaret().setDot(0);
                getDetailTextPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
        sw.execute();
        
        
/*was:        
        
        final String txt = htmlConverterDetail.getHtml(obj, hub, properties);
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getDetailTextPane().setText(txt);
                    getDetailTextPane().getCaret().setDot(0);
                }
            });
        }
        else {
            getDetailTextPane().setText(txt);
            getDetailTextPane().getCaret().setDot(0);
        }
        getDetailTextPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
*/        
    }
    
    public void setProperties(OAProperties prop) {
        this.properties = prop;
    }
    public OAProperties getProperties() {
        return this.properties;
    }


    public OAHTMLTextPane getDetailTextPane() {
        return txtDetail;
    }
    public void setDetailTextPane(OAHTMLTextPane txt) {
        txtDetail = txt;;
        if (txtDetail != null) {
            txtHeader.setImageLoader(txtDetail.getImageLoaderClass(), txtDetail.getImageLoaderDirectory());
            ((HTMLDocument)txtHeader.getDocument()).setBase( ((HTMLDocument)txtDetail.getDocument()).getBase());
    
            txtFooter.setImageLoader(txtDetail.getImageLoaderClass(), txtDetail.getImageLoaderDirectory());
            ((HTMLDocument)txtFooter.getDocument()).setBase( ((HTMLDocument)txtDetail.getDocument()).getBase());
        }
        super.setDetail(txt);
    }

    

    @Override
    public void beforePreview(PageFormat pageFormat) {
        // set internal properties for HtmlConverter - $Date, $Time, $Page
        htmlConverterTitleHeader.setProperty("DATE", new OADate());
        htmlConverterHeader.setProperty("DATE", new OADate());
        htmlConverterDetail.setProperty("DATE", new OADate());
        htmlConverterFooter.setProperty("DATE", new OADate());
  
        htmlConverterTitleHeader.setProperty("TIME", new OATime());
        htmlConverterHeader.setProperty("TIME", new OATime());
        htmlConverterDetail.setProperty("TIME", new OATime());
        htmlConverterFooter.setProperty("TIME", new OATime());
        
        super.beforePreview(pageFormat);
    }
    @Override
    public void beforePrint(PageFormat pageFormat) {
        // set internal properties for HtmlConverter - $Date, $Time, $Page
        super.beforePrint(pageFormat);
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageFormat == null || graphics == null) return NO_SUCH_PAGE;

        htmlConverterTitleHeader.setProperty("PAGE", new Integer(pageIndex+1));
        htmlConverterHeader.setProperty("PAGE", new Integer(pageIndex+1));
        htmlConverterDetail.setProperty("PAGE", new Integer(pageIndex+1));
        htmlConverterFooter.setProperty("PAGE", new Integer(pageIndex+1));
        
        return super.print(graphics, pageFormat, pageIndex);
    }

    /**
     * This is used to get each property for the OAHTMLConverter.getProperty
     * @see OAHTMLConverter#getProperty(OAObject, String)
     */
    protected Object getProperty(Object defaultValue, OAObject oaObj, String propertyName) {
        return defaultValue;
    }

    /**
     * This is used to get each value for the OAHTMLConverter.getValue
     * @see OAHTMLConverter#getValue(OAObject, String, int, String, OAProperties)
     */
    protected String getValue(String defaultValue, OAObject obj, String propertyName, int width, String fmt, OAProperties props) {
        return defaultValue;
    }    

}


