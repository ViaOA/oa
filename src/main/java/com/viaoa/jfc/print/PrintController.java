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
package com.viaoa.jfc.print;


import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.viaoa.jfc.OACommand;
import com.viaoa.util.OAString;

/**
 * Controller for handling all aspects of printing.
 * @author vvia
 *
 */
public class PrintController {
    private static Logger LOG = Logger.getLogger(PrintController.class.getName());

    static private PrintController manager;

    private Window parentWindow;
    
    private PrinterJob printerJob;
    private PrintRequestAttributeSet pras;  // not curently used qqqqqqqqqq
    private PrintPreviewController controlPrintPreview;
    private boolean bPreviewing;
    private String title;
    private Printable printable;
    private PageFormat pageFormat; 

    private final String CMD_Print        = "Print";
    private final String CMD_PrintPreview = "PrintPreview";
    private final String CMD_PageSetup    = "PageSetup";
    

    private JButton cmdPrintPreview;
    private JButton cmdPrint;
    private JButton cmdPageSetup;
    private JButton cmdQuickPrint;
    private JButton cmdSaveAsPdf;
    
    protected JMenuItem miPageSetup;
    protected JMenuItem miPrintPreview;
    protected JMenuItem miPrint;
    protected JMenuItem miSaveAsPdf;
    
    
    public PrintController() {
        manager = this;
        LOG.fine("starting");
        printerJob = PrinterJob.getPrinterJob();
        updateCommands();
    }
    public PrintController(Window parent) {
        this();
        setParentWindow(parent);
    }

    public static PrintController createPrintManager() {
        if (manager == null) manager = new PrintController();
        return manager;
    }

    public static PrintController getPrintManager() {
        return manager;
    }

    
    
    public void setParentWindow(Window window) {
        parentWindow = window;
        if (controlPrintPreview != null) {
            controlPrintPreview.setParentWindow(window);
        }
    }
    
    public PrintPreviewController getPrintPreviewController() {
        if (controlPrintPreview != null) return controlPrintPreview;
        controlPrintPreview = new PrintPreviewController() {
            public void onClose() {
                PrintController.this.bPreviewing = false;
                controlPrintPreview.close();
                if (printable instanceof OAPrintable) {
                    OAPrintable p = (OAPrintable) printable;
                    p.afterPreview();
                }
            }
            public void onPageSetup() {
                showPageSetupDialog();
            }
            public void onPrint() {
                if (printable instanceof OAPrintable) {
                    OAPrintable p = (OAPrintable) printable;
                    p.afterPreview();
                }
                onClose();
                PrintController.this.print(true);
            }
        };
        controlPrintPreview.setParentWindow(parentWindow);
        return controlPrintPreview;
    }
    
    
    /**
     * This can be used to get a valid PageFormat based on the current printerJob.
     * This is used when initializing a PageFormat.
     */
    public PageFormat validate(PageFormat pageFormat) {
        if (pageFormat == null) return null;
        return printerJob.validatePage(pageFormat);
    }
    
    /*
     * Used to set the report parameters required for print/preview/pageFormat
     */
    public void setPrintable(String title, Printable printable, PageFormat pageFormat) {
        LOG.finer("title="+title);
        setTitle(title);
        setPrintable(printable);
        if (pageFormat != null) setPageFormat(pageFormat);
        updateCommands();
    }
    protected void updateCommands() {
        boolean b = (getPrintable() != null); 
        getPageSetupAction().setEnabled(b);
        getPrintAction(true).setEnabled(b);
        getPrintAction(false).setEnabled(b);
        getPrintPreviewAction().setEnabled(b);

        if (cmdPrint != null) cmdPrint.setEnabled(b);
        if (cmdQuickPrint != null) cmdQuickPrint.setEnabled(b);
        if (miPrint != null) miPrint.setEnabled(b);

        if (cmdPrintPreview != null) cmdPrintPreview.setEnabled(b);
        if (miPrintPreview != null) miPrintPreview.setEnabled(b);
        
        if (miSaveAsPdf != null) miSaveAsPdf.setEnabled(b);
        if (cmdSaveAsPdf != null) cmdSaveAsPdf.setEnabled(b);
    }
    
    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        if (pras == null) {
            pras = new HashPrintRequestAttributeSet();
        }
        return pras;
    }
    public PageFormat getPageFormat() {
        if (pageFormat == null) pageFormat = new PageFormat();
        return this.pageFormat; 
    }
    public void setPageFormat(PageFormat pf) {
        this.pageFormat = pf;
        getPrintPreviewController().setPageFormat(pf);
        if (bPreviewing) {
            if (printable instanceof OAPrintable) {
                OAPrintable p = (OAPrintable) printable;
                p.afterPreview();
                p.beforePreview(pf);
            }
            getPrintPreviewController().refresh(true);
        }
    }
    public void setPrintable(Printable p) {
        this.printable = p;
        updateCommands();
    }
    public Printable getPrintable() {
        return printable;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Display the pageFormat dialog.  If the pageFormat is changed, then the setPageFormat(newPageFormat) will
     * be called and will replace the current pageFormat.
     */
    public void showPageSetupDialog() {
        try {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    printerJob.setJobName(title==null?"":title);
                    PageFormat pf = getPageFormat();
                    pf = printerJob.pageDialog(pf);
                    if (pf != null && pf != PrintController.this.getPageFormat()) {
                        PrintController.this.setPageFormat(pf);
                    }
                }
                catch (Exception e) {
                    System.out.println("PrintModule.pageFormat() error: "+e);
                    e.printStackTrace();
                }
            }
        });
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public void preview() {
        if (getPrintable() == null) return;
        if (printable instanceof OAPrintable) {
            OAPrintable p = (OAPrintable) printable;
            p.beforePreview(getPageFormat());
        }
        bPreviewing = true;
        if (getPrintPreviewController().getParentWindow() == null) {
            Window window = SwingUtilities.getWindowAncestor(cmdPrintPreview);
            getPrintPreviewController().setParentWindow(window);
        }
        getPrintPreviewController().show(printable, title==null?"":title, getPageFormat());
    }

    public void print(boolean bUseDialog) {
        LOG.fine("called");
        Printable p = getPrintable();
        if (p == null) return;
        Thread t = new PrintThread(this.title, printerJob, p, bUseDialog, getPageFormat(), getPrintRequestAttributeSet());
        // if (frm != null) frm.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        t.start();
    }


    static class PrintThread extends Thread {
        String title;
        PrinterJob printerJob;
        boolean bShowDialog;
        Printable printable;
        PageFormat pageFormat;
        PrintRequestAttributeSet pras;
        
        public PrintThread(String title, PrinterJob pj, Printable p, boolean bShowDialog, PageFormat pf, PrintRequestAttributeSet pras) {
            this.title = title;
            printerJob = pj;
            printable = p;
            this.bShowDialog = bShowDialog;
            this.pageFormat = pf;
            this.pras = pras;
        }
        
        public void run() {
            LOG.fine("start print");
            
            PrinterJob pj = this.printerJob;
            Printable pr = this.printable;

            if (pr == null) return;
            try {
                pj.setJobName(title==null?"":title);
        
                // PageFormat pageFormat#2 = printerJob.validatePage(getPageFormat());
                pj.setPrintable(pr, pageFormat);

                LOG.fine("start print 2");
                
                if (!bShowDialog || pj.printDialog()) {
                    if (pr instanceof OAPrintable) {
                        OAPrintable p = (OAPrintable) pr;
                        p.beforePrint(pageFormat);
                    }

                    LOG.fine("start printing");
                    /* 20110308 qqqqqqq this next line sometimes gets this stacktrace
                    java.lang.NullPointerException
                        at sun.awt.windows.WPrinterJob.setAttributes(Unknown Source)
                        at sun.print.RasterPrinterJob.print(Unknown Source)
                        at sun.print.RasterPrinterJob.print(Unknown Source)
                        at com.viaoa.jfc.print.PrintController$PrintThread.run(OAPrintManager.java:251)
                    */


                    /*                    
                    pras.add(new PrinterResolution(300, 300, ResolutionSyntax.DPI));
                    pras.add(Sides.TWO_SIDED_LONG_EDGE);
                    pras.add(new JobName("report", Locale.getDefault()) );
                    pras.add(new Copies(1));
                    pras.add(new PageRanges(1));
                    // pj.getPageFormat(pras);
                    */
                    
                    /* bug: wont allow copies to be used
                    pras = new HashPrintRequestAttributeSet();
                    pras.add(PrintQuality.HIGH);
                    pj.print(pras);
                    */
                    pj.print();
                    
                    pr.print(null, null, -1); // this is used so that printable will know that the printjob is done.
                    if (pr instanceof OAPrintable) {
                        OAPrintable p = (OAPrintable) pr;
                        p.afterPrint();
                    }
                    LOG.fine("printing done");
                }
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "print error", e);
                // System.out.println("PrintModule.print() error: "+e);
                // e.printStackTrace();
            }
            finally {
                // if (frm != null) frm.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public JMenuItem getPageSetupMenuItem() {
        if (miPageSetup == null) {
            miPageSetup = new JMenuItem("Page setup ...");
            miPageSetup.setToolTipText("Printer setup.");
            //miPageSetup.setActionCommand(CMD_PageSetup);
            miPageSetup.addActionListener(getPageSetupAction());
        }
        return miPageSetup;
    }
    
    public JButton getPrintPreviewButton() {
        if (cmdPrintPreview == null) {
            cmdPrintPreview = new JButton("Preview ...");

            URL url = PrintController.class.getResource("view/image/printPreview.gif");
            ImageIcon icon = new ImageIcon(url);

            cmdPrintPreview.setIcon(icon);
            cmdPrintPreview.setToolTipText("Preview report.");
            cmdPrintPreview.addActionListener(getPrintPreviewAction());
            cmdPrintPreview.setFocusPainted(false);
            cmdPrintPreview.setFocusable(false);
            OACommand.setupButton(cmdPrintPreview);
            updateCommands();
        }
        return cmdPrintPreview;
    }
    public JMenuItem getPrintPreviewMenuItem() {
        if (miPrintPreview == null) {
            miPrintPreview = new JMenuItem("Print Preview ...");
            URL url = PrintController.class.getResource("view/image/printPreview.gif");
            ImageIcon icon = new ImageIcon(url);
            miPrintPreview.setIcon(icon);
            miPrintPreview.setToolTipText("Preview report.");
            // miPrintPreview.setActionCommand(CMD_PrintPreview);
            miPrintPreview.addActionListener(getPrintPreviewAction());
            updateCommands();
        }
        return miPrintPreview;
    }

    public JMenuItem getPrintMenuItem() {
        if (miPrint == null) {
            miPrint = new JMenuItem("Print ...");

            URL url = PrintController.class.getResource("view/image/print.gif");
            ImageIcon icon = new ImageIcon(url);
            
            miPrint.setIcon(icon);
            miPrint.setMnemonic(KeyEvent.VK_P);
            miPrint.setToolTipText("Print report.");
            // miPrint.setActionCommand(CMD_Print);
            miPrint.addActionListener(getPrintAction(true));
            miPrint.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.Event.CTRL_MASK) );
            updateCommands();
        }
        return miPrint;
    }
    
    
    public JButton getQuickPrintButton() {
        if (cmdQuickPrint == null) {
            cmdQuickPrint = new JButton("Print");

            URL url = PrintController.class.getResource("view/image/print.gif");
            ImageIcon icon = new ImageIcon(url);

            cmdQuickPrint.setIcon(icon);
            cmdQuickPrint.setToolTipText("Print report");
            // cmdQuickPrint.setActionCommand(CMD_Print);
            cmdQuickPrint.addActionListener(getPrintAction(false));
            cmdQuickPrint.setFocusPainted(false);
            cmdQuickPrint.setFocusable(false);
            
            OACommand.setupButton(cmdQuickPrint);
            updateCommands();
        }
        return cmdQuickPrint;
    }
    public JButton getPageSetupButton() {
        if (cmdPageSetup == null) {
            cmdPageSetup = new JButton("Page setup ...");
            cmdPageSetup.setToolTipText("Printer setup.");
            cmdPageSetup.addActionListener(getPageSetupAction());
            cmdPageSetup.setFocusPainted(false);
            cmdPageSetup.setFocusable(false);
            
            OACommand.setupButton(cmdPageSetup);
            updateCommands();
        }
        return cmdPageSetup;
    }
    
    public JButton getPrintButton() {
        if (cmdPrint == null) {
            cmdPrint = new JButton("Print ...");

            URL url = PrintController.class.getResource("view/image/print.gif");
            ImageIcon icon = new ImageIcon(url);

            cmdPrint.setIcon(icon);
            cmdPrint.setToolTipText("Print report.");
            // cmdPrint.setActionCommand(CMD_Print);
            cmdPrint.addActionListener(getPrintAction(true));
            cmdPrint.setFocusPainted(false);
            cmdPrint.setFocusable(false);
            
            OACommand.setupButton(cmdPrint);
            updateCommands();
        }
        return cmdPrint;
    }
    
    public JMenuItem getSaveAsPdfMenuItem() {
        if (miSaveAsPdf == null) {
            miSaveAsPdf = new JMenuItem("Save As Pdf file ...");

            URL url = PrintController.class.getResource("view/image/pdf.gif");
            ImageIcon icon = new ImageIcon(url);
            
            miSaveAsPdf.setIcon(icon);
            miSaveAsPdf.setMnemonic(KeyEvent.VK_F);
            miSaveAsPdf.setToolTipText("Save as a Pdf file.");
            // miSaveAsPdf.setActionCommand(CMD_SaveAsPdf);
            miSaveAsPdf.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PrintController.this.onSaveAsPdf();
                }
            });
            // miSaveAsPdf.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.Event.CTRL_MASK) );
            updateCommands();
        }
        return miSaveAsPdf;
    }
    public JButton getSaveAsPdfButton() {
        if (cmdSaveAsPdf == null) {
            cmdSaveAsPdf = new JButton("");

            URL url = PrintController.class.getResource("view/image/pdf.gif"); // or: pdfBig.jpg
            ImageIcon icon = new ImageIcon(url);

            cmdPrint.setIcon(icon);
            cmdPrint.setToolTipText("Save as PDF file.");
            // cmdPrint.setActionCommand(CMD_Print);
            cmdPrint.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PrintController.this.onSaveAsPdf();
                }
            });

            cmdPrint.setFocusPainted(false);
            cmdPrint.setFocusable(false);
            OACommand.setupButton(cmdPrint);
            updateCommands();
        }
        return cmdPrint;
    }

    /**
     *  This should be overwritten to call 
     *  PdfController.saveToPdf(...)
     */
    public void onSaveAsPdf() {
        
    }
    
    
    
    private Action actionPageSetup;
    public Action getPageSetupAction() {
        if (actionPageSetup == null) {
            URL url = PrintController.class.getResource("view/image/pageSetup.gif");
            ImageIcon icon = new ImageIcon(url);
            actionPageSetup = new AbstractAction(CMD_PageSetup, icon) {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                   PrintController.this.showPageSetupDialog();
               } 
            };
            actionPageSetup.putValue(Action.NAME, "Page Setup ...");
            actionPageSetup.putValue(Action.SHORT_DESCRIPTION, "Page Format");            
            // actionPageSetup.putValue(Action.ACCELERATOR_KEY, null);
            actionPageSetup.putValue(Action.ACTION_COMMAND_KEY, CMD_PageSetup);
            actionPageSetup.putValue(Action.LARGE_ICON_KEY, icon);
            actionPageSetup.putValue(Action.SMALL_ICON, icon);
            actionPageSetup.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer(-1)); // char position for Mnemonic
            actionPageSetup.putValue(Action.LONG_DESCRIPTION, "Page Setup");
            actionPageSetup.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
        }
        return actionPageSetup;
    }
    
    private Action actionPrintPreview;
    public Action getPrintPreviewAction() {
        if (actionPrintPreview == null) {
            URL url = PrintController.class.getResource("view/image/printPreview.gif");
            ImageIcon icon = new ImageIcon(url);
            actionPrintPreview = new AbstractAction(CMD_PrintPreview, icon) {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                   PrintController.this.preview();
               } 
            };
            actionPrintPreview.putValue(Action.NAME, "Print Preview ...");
            actionPrintPreview.putValue(Action.SHORT_DESCRIPTION, "Print Preview");            
            // actionPrintPreview.putValue(Action.ACCELERATOR_KEY, null);
            actionPrintPreview.putValue(Action.ACTION_COMMAND_KEY, CMD_PrintPreview);
            actionPrintPreview.putValue(Action.LARGE_ICON_KEY, icon);
            actionPrintPreview.putValue(Action.SMALL_ICON, icon);
            actionPrintPreview.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer(-1)); // char position for Mnemonic
            actionPrintPreview.putValue(Action.LONG_DESCRIPTION, "Print Preview");
            actionPrintPreview.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        }
        return actionPrintPreview;
    }
    
    private Action actionPrint, actionPrintShowDialog;
    public Action getPrintAction(boolean bWithPageDialog) {
        if (actionPrint == null) {
            URL url = PrintController.class.getResource("view/image/print.gif");
            ImageIcon icon = new ImageIcon(url);
            actionPrint = new AbstractAction(CMD_Print, icon) {
               public void actionPerformed(java.awt.event.ActionEvent e) {
                   PrintController.this.print(false);
               } 
            };
            actionPrint.putValue(Action.NAME, "Print");
            actionPrint.putValue(Action.SHORT_DESCRIPTION, "Print");            
            // actionPrint.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false));
            actionPrint.putValue(Action.ACTION_COMMAND_KEY, CMD_Print);
            actionPrint.putValue(Action.LARGE_ICON_KEY, icon);
            actionPrint.putValue(Action.SMALL_ICON, icon);
            actionPrint.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer(-1)); // char position for Mnemonic
            actionPrint.putValue(Action.LONG_DESCRIPTION, "Print");
            actionPrint.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P); 

            actionPrintShowDialog = new AbstractAction(CMD_Print, icon) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    PrintController.this.print(true);
                } 
             };
             actionPrintShowDialog.putValue(Action.NAME, "Print ...");
             actionPrintShowDialog.putValue(Action.SHORT_DESCRIPTION, "Print");            
             actionPrintShowDialog.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false));
             actionPrintShowDialog.putValue(Action.ACTION_COMMAND_KEY, CMD_Print);
             actionPrintShowDialog.putValue(Action.LARGE_ICON_KEY, icon);
             actionPrintShowDialog.putValue(Action.SMALL_ICON, icon);
             actionPrintShowDialog.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer(-1)); // char position for Mnemonic
             actionPrintShowDialog.putValue(Action.LONG_DESCRIPTION, "Print");
             actionPrintShowDialog.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P); 
        }
        if (bWithPageDialog) return actionPrintShowDialog;
        return actionPrint;
    }
    
    public void updateUI() {
        if (controlPrintPreview != null) {
            controlPrintPreview.updateUI();
        }
    }
    

    // =========================================
    public static void main(String[] args) {
        PrintController pc = new PrintController();
            
        PageFormat pf = new PageFormat();
        
        Printable p = new Printable() {
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex < 2) return Printable.PAGE_EXISTS;
                return Printable.NO_SUCH_PAGE;
            }
        };
        
        pc.setPrintable("title here", p, pf);
        pc.showPageSetupDialog();
        pc.preview();
        // pc.print(p, "title", pf, true);
        // pc.print(p, "title", pf, false);
    }

    
/****** 
    import com.lowagie.text.Document;
    import com.lowagie.text.pdf.PdfContentByte;
    import com.lowagie.text.pdf.PdfWriter;
    
    testIText(getPrintable());//qqqqqqqqqqqqqqqqqqqqqq      
    
    void testIText(Printable printable) {
        try {
            _testIText(printable);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: "+e);
        }
    }
    void _testIText(Printable printable) throws Exception {

        PageFormat pf = getPageFormat();
        float w = (float) pf.getWidth();
        float h = (float) pf.getHeight();
        
        Document document = new Document();
        document.setPageSize(new com.lowagie.text.Rectangle(w, h)); // float margin L,R,TB  qqqqqqq might need to add the margins using PageFormat

        FileOutputStream fos = new FileOutputStream("test.pdf");
        PdfWriter writer = PdfWriter.getInstance(document, fos);

        // must be done before document.open()
        document.addAuthor("CDI");
        document.addCreator("CDI");
        document.addCreationDate();
        document.addTitle("Sales Order Estimate");         
        document.addSubject("Sales Order Estimate");
        document.addProducer(); // itext + version
        document.open();

        PdfContentByte cb = writer.getDirectContent();
  
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics gTemp = bi.getGraphics();
        
        for (int p=0; ;p++) {
            if (p > 0) {
                int x = printable.print(gTemp, getPageFormat(), p);
                if (x == Printable.NO_SUCH_PAGE) break;
                document.newPage();
            }
            Graphics2D g2 = cb.createGraphics(w, h);//, mapper, true, .95f);
            int x = printable.print(g2, getPageFormat(), p);
            g2.dispose();
            if (x == Printable.NO_SUCH_PAGE) break;
        }

        document.close();
        fos.flush();
        fos.close();
        
        
    }
***/    
    
}
