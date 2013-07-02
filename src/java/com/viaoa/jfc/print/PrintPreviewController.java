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
package com.viaoa.jfc.print;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;

import javax.swing.*;

import com.viaoa.jfc.print.view.*;

// IMPORTANT: need to convert from point to pixel whenever using PageFormat or Paper.  For printing, graphics.scale(x,x) is set to make it wysiwyg

/**
 * Controller for print preview.
 * @author vvia
 *
 */
public abstract class PrintPreviewController {
    private Window parentWindow;
	private PrintPreviewDialog dlgPrintPreview;
	private Printable printable;
	private PageFormat pageFormat;
    private BufferedImage image;  // one and only image, that is used by PagePanels.
	
	private String[] scales = new String[] { " 10%  ", " 25%  ", " 50%  ", " 75%  ", " 100% " };
	private int selectedScale = 4;
	
	public PrintPreviewController() {
	}

	public void show(Printable printable, String title, PageFormat pageFormat) {
		getPrintPreviewDialog().setTitle(title);
		clear();
		this.pageFormat = pageFormat;
		this.printable = printable;
		
		refresh(true);
		getPrintPreviewDialog().setVisible(true);
    }

    public void setParentWindow(Window window) {
        parentWindow = window;
    }
    public Window getParentWindow() {
        return parentWindow;
    }
	
    private float pointToPixel;
    /**
     * Convert from Point size to pixel size.
     */
    protected float convertPointsToPixels(double pointSize) {
        if (pointToPixel == 0.0) {
            pointToPixel = (float) (Toolkit.getDefaultToolkit().getScreenResolution() / 72.0);
        }
        return (float) (pointToPixel * pointSize);
    }
	
	
	
	/**
	 * Used to change the PageFormat.
	 * @see #onPageSetup
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	
	
	protected PrintPreviewDialog getPrintPreviewDialog() {
		if (dlgPrintPreview == null) {
			dlgPrintPreview = new PrintPreviewDialog(parentWindow, scales) {
				public void onClose() {
					PrintPreviewController.this.onClose();
				}
				public void onPageSetup() {
					PrintPreviewController.this.onPageSetup();
				}
				public void onPrint() {
					PrintPreviewController.this.onPrint();
				}
			};

			dlgPrintPreview.getScaleComboBox().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
/** was: not needed, refresh is ran in a new thread				    
					new Thread() {
						public void run() {
							String str = dlgPrintPreview.getScaleComboBox().getSelectedItem().toString();
	                        str = str.trim();
							if (str.endsWith("%")) str = str.substring(0, str.length()-1);
							str = str.trim();
							try {
							    selectedScale = Integer.parseInt(str);
							}
							catch (NumberFormatException ex) {
							    return;
							}
							refresh(false);  // resize and repaint
						}
					}.start();
*/					
                    String str = dlgPrintPreview.getScaleComboBox().getSelectedItem().toString();
                    str = str.trim();
                    if (str.endsWith("%")) str = str.substring(0, str.length()-1);
                    str = str.trim();
                    try {
                        selectedScale = Integer.parseInt(str);
                    }
                    catch (NumberFormatException ex) {
                        return;
                    }
                    refresh(false);  // resize and repaint

				}
			});
			dlgPrintPreview.getScaleComboBox().setSelectedIndex(selectedScale);

	        Rectangle r;
	        if (parentWindow == null) r = new Rectangle(20,20, 200, 200);
	        else r = parentWindow.getBounds();
	        int x = r.getLocation().x + 20;
	        int y = r.getLocation().y + 20;
	        int w = r.getSize().width - 30;
	        int h = r.getSize().height - 30;
			dlgPrintPreview.setBounds(new Rectangle(x, y, w, h));
		}
		return dlgPrintPreview;
	}
	
	
    protected void clear() {
    	getPrintPreviewDialog().getPreviewPanel().removeAll();
        printable = null;
    }

	protected int wPage;
	protected int hPage;

    public void refresh(final boolean bRebuild) {
    	getPrintPreviewDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(new Runnable() {
            public void run() {
                doRefresh(bRebuild);
                getPrintPreviewDialog().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }).start();
    }

    protected void doRefresh(boolean bRebuild) {
        if (dlgPrintPreview == null || pageFormat == null) return;

        wPage = (int) convertPointsToPixels(pageFormat.getWidth());
        hPage = (int) convertPointsToPixels(pageFormat.getHeight());

	    int w = (int)(wPage * selectedScale/100);
	    int h = (int)(hPage * selectedScale/100);

        if (!bRebuild) {
            Component[] comps = getPrintPreviewDialog().getPreviewPanel().getComponents();
            for (int i=0; i<comps.length; i++) {
                if (comps[i] instanceof PagePanel) {
                    ((PagePanel) comps[i]).setScaledSize(w,h);
                    comps[i].repaint();
                }
            }
        }
        else {
            getPrintPreviewDialog().getPreviewPanel().removeAll();
            getPrintPreviewDialog().repaint();
		    try {
	            image = new BufferedImage(wPage,hPage, BufferedImage.TYPE_INT_RGB);
			    for (int pageIndex = 0; ;pageIndex++) {
				    Graphics g = image.getGraphics();

			        if (printable instanceof OAPrintable) {
			            OAPrintable p = (OAPrintable) printable;
	                    if (p.preview(g, pageFormat, pageIndex) != Printable.PAGE_EXISTS) break;
			        }
			        else {
			            if (printable != null) {
			                if (printable.print(g, pageFormat, pageIndex) != Printable.PAGE_EXISTS) break;
			            }
			        }
				    
				    PagePanel pp = new PagePanel(pageIndex) {
				       @Override
				        protected Image getImage() {
				            return PrintPreviewController.this.getPageImage(getPage());
				        }  
				    };
				    
    		        pp.setScaledSize(w, h);
    		        getPrintPreviewDialog().getPreviewPanel().add(pp);
				    g.dispose();
			    }
                if (printable instanceof OAPrintable) {
                    OAPrintable p = (OAPrintable) printable;
                    p.preview(null, null, -1); // to show end of printjob
                }
                else printable.print(null, null, -1); // to show end of printjob
		    }
		    catch (PrinterException e) {
			    // e.printStackTrace();
		    }
        }
        getPrintPreviewDialog().getPreviewPanel().revalidate();
	}
    
    protected Image getPageImage(int page) {
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, wPage, hPage);
        try {
            if (printable instanceof OAPrintable) {
                OAPrintable p = (OAPrintable) printable;
                p.preview(g, pageFormat, page);
            }
            else {
                printable.print(g, pageFormat, page);
            }
        }
        catch (Exception e) {
            //qqqqqqqq log this
        }
        return image;
    }
    
    
    public void close() {
    	getPrintPreviewDialog().setVisible(false);
    	this.clear();
    }

    public void updateUI() {
        if (dlgPrintPreview != null) {
            SwingUtilities.updateComponentTreeUI(dlgPrintPreview);
        }
    }
    
    
	protected abstract void onClose();
	protected abstract void onPrint();
	protected abstract void onPageSetup();
	

	
	// =========================================
	public static void main(String[] args) {
		PrintPreviewController ppc = new PrintPreviewController() {
			public void onClose() {
			}
			public void onPageSetup() {
			}
			public void onPrint() {
			}
		};
		PageFormat pf = new PageFormat();
		
		ppc.show(new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex < 2) return Printable.PAGE_EXISTS;
				return Printable.NO_SUCH_PAGE;
			}
		}, "test", pf);
	}
}


