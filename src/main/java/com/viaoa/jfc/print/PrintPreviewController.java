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
	
	private final int[] scales = new int[] { 10, 25, 35, 50, 75, 90, 100, 125, 150, 200 };
	private int scale = 100;
	
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
		    
		    String[] ss = new String[scales.length];
		    int pos = 0;
		    int posNow = 0;
		    for (int x : scales) {
		        ss[pos++] = " " + x + "% ";
                if (x == scale) posNow = pos-1;
		    }
		    
			dlgPrintPreview = new PrintPreviewDialog(parentWindow, ss) {
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
				    int x = dlgPrintPreview.getScaleComboBox().getSelectedIndex();
				    x = scales[x];
				    if (x == scale) return;
				    scale = x;
                    refresh(false);  // resize and repaint
				}
			});
			dlgPrintPreview.getScaleComboBox().setSelectedIndex(posNow);

	        Rectangle r;
	        if (parentWindow == null) r = new Rectangle(20, 20, 200, 200);
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
        }, "PrintPreview").start();
    }

    private volatile boolean bDone;
    
    protected void doRefresh(boolean bRebuild) {
        if (dlgPrintPreview == null || pageFormat == null) return;

        wPage = (int) convertPointsToPixels(pageFormat.getWidth());
        hPage = (int) convertPointsToPixels(pageFormat.getHeight());

	    int w = (int)(wPage * (scale/100.0d));
	    int h = (int)(hPage * (scale/100.0d));

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
				    final Graphics g = image.getGraphics();
                    final int pageIndexx = pageIndex;
			        if (printable instanceof OAPrintable) {
			            final OAPrintable p = (OAPrintable) printable;

			            // 20140717 moved to invokeAndWait
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    int x = p.preview(g, pageFormat, pageIndexx);
                                    bDone =  (x != Printable.PAGE_EXISTS);
                                }
                            });
                        }
                        catch (Exception e) {
                        }

                        // was:
			            // int x = p.preview(g, pageFormat, pageIndex);
	                    // if (x != Printable.PAGE_EXISTS) break;
			        }
			        else {
			            if (printable != null) {

	                        // 20140717 moved to invokeAndWait
	                        try {
	                            SwingUtilities.invokeAndWait(new Runnable() {
	                                @Override
	                                public void run() {
	                                    try {
	                                        int x = printable.print(g, pageFormat, pageIndexx);
	                                        bDone =  (x != Printable.PAGE_EXISTS);
                                        }
                                        catch (Exception e) {
                                            bDone = true;
                                        }
	                                }
	                            });
	                        }
	                        catch (Exception e) {
	                        }
			                // was: if (printable.print(g, pageFormat, pageIndex) != Printable.PAGE_EXISTS) break;
			            }
			        }
                    if (bDone) break;
				    
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


