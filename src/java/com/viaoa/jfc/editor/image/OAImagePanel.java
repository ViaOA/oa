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
package com.viaoa.jfc.editor.image;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.util.Tuple;

/**
 * Manages a BufferedImage that is based on an original image and a collection
 * of undoable changes that have been made to it.
 * 
 * @author vincevia
 */
public class OAImagePanel extends JPanel {

    // image that is being view/edited
    private Image image;

    // current version of image, based on changes that have been made to it
    private BufferedImage bufferedImage;
    private BufferedImage bufferedImageTemp;

    // selected rectangle for crop, relative to actual size of image
    private int t, l, b, r; // 
    // flag to know if crop rectangle is visible
    private boolean bRectOn;

    // used for drawing rect
    private boolean bMouseDown;
    private Point ptStart, ptLast;
    private int cursor;

    // used to set a temporary brightess to display
    private boolean bDemoBrightness;
    private int iDemoBrightness;
    private boolean bDemoContrast;
    private int iDemoContrast;

    // used for display only, not part of image changes
    private double zoomScale = 1.0;

    // changes that have be done to image, that are only applied to
    // bufferedImage
    private ArrayList<MyUndoableEdit> alChanges = new ArrayList<MyUndoableEdit>();

    private static final int CHANGE_Brighter = 0;
    private static final int CHANGE_Darker = 1;
    private static final int CHANGE_MoreContrast = 2;
    private static final int CHANGE_LessContrast = 3;
    private static final int CHANGE_Sharpen = 4;
    private static final int CHANGE_Blur = 5;
    private static final int CHANGE_RotateCW = 6;
    private static final int CHANGE_RotateCCW = 7;

    // used to rebuild bufferedImage, using image and then reapplying the
    // changes/undoables
    private boolean bReplayingChanges;

    /**
     * Create a new ImagePanel that allows mouse control to select a crop
     * rectangle.
     */
    public OAImagePanel() {
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);

        String cmdName = "escape";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), cmdName);
        this.getActionMap().put(cmdName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (OAImagePanel.this.bRectOn) {
                    OAImagePanel.this.setSelectArea(null);
                }
            }

            @Override
            public boolean isEnabled() {
                return (OAImagePanel.this.bRectOn);
            }
        });

        cmdName = "enter";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), cmdName);
        this.getActionMap().put(cmdName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (OAImagePanel.this.bRectOn) {
                    OAImagePanel.this.crop();
                }
            }

            @Override
            public boolean isEnabled() {
                return (OAImagePanel.this.bRectOn);
            }
        });

        cmdName = "undo";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, false), cmdName);
        this.getActionMap().put(cmdName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                OAImagePanel.this.undo();
            }

            @Override
            public boolean isEnabled() {
                return (OAImagePanel.this.alChanges.size() > 0);
            }
        });

        cmdName = "zoomin";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK, false), cmdName);
        this.getActionMap().put(cmdName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double d = getZoomScale();
                if (d < .1) d += .01;
                else d += .10;
                setZoomScale(d);
            }

            @Override
            public boolean isEnabled() {
                return (OAImagePanel.this.image != null);
            }
        });

        cmdName = "zoomout";
        this.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false), cmdName);
        this.getActionMap().put(cmdName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double d = getZoomScale();
                if (d < .10) d -= .01;
                else {
                    d -= .10;
                    if (d < .1) d = .09;
                }
                setZoomScale(Math.max(d, .01));
            }

            @Override
            public boolean isEnabled() {
                return (OAImagePanel.this.image != null);
            }
        });

        setRequestFocusEnabled(true);
    }

    /**
     * This is called whenever there are savable changes that have been made.
     */
    protected void onImageChanged() {
        // qqqqqqqqq
    }

    /**
     * The current image with the applied changes.
     * 
     * @return
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * Crop rectangle.
     */
    public void setShowRectangle(boolean b) {
        bRectOn = b;
    }

    public boolean getShowRectangle() {
        return bRectOn;
    }

    @Override
    public Dimension getMaximumSize() {
        return getBufferedImageSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return getBufferedImageSize();
    }

    @Override
    public Dimension getSize() {
        return getBufferedImageSize();
    }

    /**
     * Image that is being used to build bufferedImage. Note: changes only
     * affect bufferedImage, image is not changed.
     * 
     * @see #getBufferedImage() to get image with changes
     */
    public Image getImage() {
        return image;
    }

    public void setImage(Image img) {
        this.image = img;
        setSelectArea(null);
        if (!bReplayingChanges) {
            zoomScale = 1.0;
            clearUndoables(true);
        }
        if (img == null) {
            setBufferedImage(null);
        }
        else {
            BufferedImage bi = OAImageUtil.createBufferedImage(image);
            setBufferedImage(bi);
        }
    }

    protected Dimension getBufferedImageSize() {
        Dimension d = new Dimension(10, 10);
        if (bufferedImage != null) {
            d.width = (int) (bufferedImage.getWidth(null) * zoomScale);
            d.height = (int) (bufferedImage.getHeight(null) * zoomScale);
        }
        return d;
    }

    /**
     * Internally used to set the bufferedImage, that has the current changes to
     * image applied.
     * 
     * @param bi
     */
    private void setBufferedImage(BufferedImage bi) {
        this.bufferedImage = bi;
        if (!bReplayingChanges) {
            Dimension d = new Dimension(10, 10);
            if (bufferedImage != null) {
                d.width = (int) (bufferedImage.getWidth(null) * zoomScale);
                d.height = (int) (bufferedImage.getHeight(null) * zoomScale);
            }
            setSize(getBufferedImageSize());
            validate();
            // repaint();
        }
    }

    /**
     * Crop rectangle, based on image size, not display image size (use
     * zoomScale to adjust to x,y)
     */
    public void setSelectArea(Rectangle rect) {
        if (rect == null) {
            setShowRectangle(false);
            cursor = Cursor.DEFAULT_CURSOR;
            this.setCursor(Cursor.getPredefinedCursor(cursor));
            repaint();
        }
        else {
            setSelectArea(rect.x, rect.y, rect.y + rect.height, rect.x + rect.width);
        }
    }

    /**
     * Crop rectangle, based on image size, not display image size (use
     * zoomScale to adjust to x,y)
     */
    public void setSelectArea(int t, int l, int b, int r) {
        if (image == null) return;
        if (t <= b) {
            this.t = t;
            this.b = b;
        }
        else {
            this.t = b;
            this.b = t;
        }
        if (l <= r) {
            this.l = l;
            this.r = r;
        }
        else {
            this.l = r;
            this.r = l;
        }

        setShowRectangle(true);
        repaint();
        if (cursor == Cursor.DEFAULT_CURSOR) onMouseMoved(getMousePosition());
    }

    /**
     * Crop rectangle, based on image size, not display image size (use
     * zoomScale to adjust to x,y)
     */
    public Rectangle getSelectArea() {
        if (!bRectOn) return null;
        Rectangle rect = new Rectangle(l, t, (r - l), (b - t));
        return rect;
    }

    /**
     * Paint bufferedImage, using the zoomScale, and displaying crop rectangle.
     */
    int qq;

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        super.paint(g);

        if (image != null) {
            if (bufferedImageTemp != null) {
                g.drawImage(bufferedImageTemp, 0, 0, null);
            }
            else {

                BufferedImage bi = bufferedImage;

                if (bDemoBrightness) {
                    bi = OAImageUtil.brighter(bi, (byte) iDemoBrightness);
                }
                else if (bDemoContrast) {
                    bi = OAImageUtil.contrast(bi, (float) (1.0 + (iDemoContrast * .1)));
                }

                if (zoomScale != 1.0) {
                    bi = OAImageUtil.scale(bi, zoomScale, zoomScale);
                }

                g.drawImage(bi, 0, 0, null);
            }
        }

        if (bRectOn) {
            Rectangle rect = getSelectArea();

            if (rect != null) {
                if (zoomScale != 1.0) {
                    rect = new Rectangle((int) (rect.x * zoomScale), (int) (rect.y * zoomScale), (int) (rect.width * zoomScale), (int) (rect.height * zoomScale));
                }

                g.setColor(Color.white);
                g.setStroke(new BasicStroke(5.0f));

                g.draw(rect);
                BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[] { 5, 1 }, 0);
                g.setStroke(bs);
                g.setColor(Color.blue);
                g.draw(rect);
            }
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
            onMousePressed(e.getPoint());
            e.consume();
            requestFocus();
            break;
        case MouseEvent.MOUSE_RELEASED:
            bMouseDown = false;
            if (Math.abs(t - b) < 2 || Math.abs(l - r) < 2) {
                setSelectArea(null);
            }
            e.consume();
            if (cursor != Cursor.MOVE_CURSOR) {
                cursor = Cursor.DEFAULT_CURSOR;
                this.setCursor(Cursor.getPredefinedCursor(cursor));
            }
            break;
        }

        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        switch (e.getID()) {
        case MouseEvent.MOUSE_DRAGGED:
            if (!bRectOn) break;
            onMouseDragged(e.getPoint());
            e.consume();
            break;
        case MouseEvent.MOUSE_MOVED:
            onMouseMoved(e.getPoint());
            break;
        }
        super.processMouseMotionEvent(e);
    }

    protected void onMousePressed(Point pt) {
        ptLast = pt;

        if (bufferedImage == null) return;

        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);

        if (!bRectOn || cursor == Cursor.DEFAULT_CURSOR) {
            if (pt.x < w && pt.y < h && isEnabled()) {
                ptStart = pt;
                t = b = ptStart.y;
                l = r = ptStart.x;

                cursor = Cursor.CROSSHAIR_CURSOR;
                this.setCursor(Cursor.getPredefinedCursor(cursor));

                bMouseDown = true;
                bRectOn = true;
            }
            else {
                bRectOn = false;
            }
            repaint();
        }
    }

    protected void onMouseDragged(Point pt) {
        if (bufferedImage == null) return;
        // make relative based on zoomScale
        Point ptHold = pt;
        pt = new Point((int) (pt.x / zoomScale), (int) (pt.y / zoomScale));

        Point ptStart = new Point((int) (this.ptStart.x / zoomScale), (int) (this.ptStart.y / zoomScale));

        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);

        if (cursor != Cursor.MOVE_CURSOR) {
            if (pt.x < 0) pt.x = 0;
            if (pt.y < 0) pt.y = 0;

            if (pt.x >= w) pt.x = w - 1;
            if (pt.y >= h) pt.y = h - 1;
        }

        if (cursor == Cursor.CROSSHAIR_CURSOR) {
            setSelectArea(ptStart.y, ptStart.x, pt.y, pt.x);
        }
        else if (cursor == Cursor.W_RESIZE_CURSOR) {
            setSelectArea(t, pt.x, b, r);
            if (pt.x >= r) cursor = Cursor.E_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.E_RESIZE_CURSOR) {
            setSelectArea(t, l, b, pt.x);
            if (pt.x <= l) cursor = Cursor.W_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.N_RESIZE_CURSOR) {
            setSelectArea(pt.y, l, b, r);
            if (pt.y >= b) cursor = Cursor.S_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.S_RESIZE_CURSOR) {
            setSelectArea(t, l, pt.y, r);
            if (pt.y <= t) cursor = Cursor.N_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.NE_RESIZE_CURSOR) {
            setSelectArea(pt.y, l, b, pt.x);
            if (pt.y >= b) cursor = Cursor.SE_RESIZE_CURSOR;
            if (pt.x <= l) cursor = Cursor.NW_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.SE_RESIZE_CURSOR) {
            setSelectArea(t, l, pt.y, pt.x);
            if (pt.y <= t) cursor = Cursor.NE_RESIZE_CURSOR;
            if (pt.x <= l) cursor = Cursor.SW_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.NW_RESIZE_CURSOR) {
            setSelectArea(pt.y, pt.x, b, r);
            if (pt.y >= b) cursor = Cursor.SW_RESIZE_CURSOR;
            if (pt.x >= r) cursor = Cursor.NE_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.SW_RESIZE_CURSOR) {
            setSelectArea(t, pt.x, pt.y, r);
            if (pt.y <= t) cursor = Cursor.NW_RESIZE_CURSOR;
            if (pt.x >= r) cursor = Cursor.SE_RESIZE_CURSOR;
        }
        else if (cursor == Cursor.MOVE_CURSOR) {
            ptLast = new Point((int) (ptLast.x / zoomScale), (int) (ptLast.y / zoomScale));
            int dx = (int) (pt.x - ptLast.x);
            int dy = (int) (pt.y - ptLast.y);
            setSelectArea(t + dy, l + dx, b + dy, r + dx);
            ptLast = ptHold;
        }
    }

    protected void onMouseMoved(Point pt) {
        // make relative based on zoomScale
        pt = new Point((int) (pt.x / zoomScale), (int) (pt.y / zoomScale));

        /*
         * qqq testing if(bufferedImage!=null) { int x = 0; if (pt.x >= 0 &&
         * pt.x < bufferedImage.getWidth(null)) { if (pt.y >= 0 && pt.y <
         * bufferedImage.getHeight(null)) { x = bufferedImage.getRGB(pt.x,
         * pt.y); } } System.out.println("pt="+pt+", rgb="+x); }
         */

        int cur = Cursor.DEFAULT_CURSOR;
        if (bMouseDown || !bRectOn)
        ; // creating area
        else if (isClose(pt.x, r) && isBetween(pt.y, t, b)) {
            if (isClose(pt.y, t)) cur = Cursor.NE_RESIZE_CURSOR;
            else if (isClose(pt.y, b)) cur = Cursor.SE_RESIZE_CURSOR;
            else cur = Cursor.E_RESIZE_CURSOR;
        }
        else if (isClose(pt.x, l) && isBetween(pt.y, t, b)) {
            if (isClose(pt.y, t)) cur = Cursor.NW_RESIZE_CURSOR;
            else if (isClose(pt.y, b)) cur = Cursor.SW_RESIZE_CURSOR;
            else cur = Cursor.W_RESIZE_CURSOR;
        }
        else if (isClose(pt.y, t) && isBetween(pt.x, l, r)) cur = Cursor.N_RESIZE_CURSOR;
        else if (isClose(pt.y, b) && isBetween(pt.x, l, r)) cur = Cursor.S_RESIZE_CURSOR;
        else {
            if (pt.x > l && pt.x < r && pt.y > t && pt.y < b) cur = Cursor.MOVE_CURSOR;
        }

        this.setCursor(Cursor.getPredefinedCursor(cur));
        this.cursor = cur;
    }

    private boolean isClose(int x1, int x2) {
        return (Math.abs(x1 - x2) < 5);
    }

    private boolean isBetween(int pos, int pos1, int pos2) {
        return pos >= pos1 && pos <= pos2;
    }

    // rect values need to be relative to size of image
    public void crop(Rectangle rect) {
        if (rect == null || bufferedImage == null) return;

        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);

        if (rect.x >= w) return;
        if (rect.y >= h) return;

        if (rect.x < 0) {
            rect.width += rect.x;
            rect.x = 0;
        }

        if (rect.x + rect.width > w) {
            rect.width = w - rect.x;
        }

        if (rect.y < 0) {
            rect.height += rect.y;
            rect.y = 0;
        }

        if (rect.y + rect.height > h) {
            rect.height = h - rect.y;
        }

        BufferedImage bi = OAImageUtil.crop(bufferedImage, rect);
        setBufferedImage(bi);
        if (!bReplayingChanges) addUndoable("Crop", rect);
        setSelectArea(null);
        onImageChanged();
    }

    public void crop() {
        if (!bRectOn || bufferedImage == null) return;
        Rectangle rect = new Rectangle(l, t, r - l, b - t);
        crop(rect);
    }

    public double getZoomScale() {
        return this.zoomScale;
    }

    public void setZoomScale(double d) {
        if (d <= 0.0) return;
        this.zoomScale = d;
        setBufferedImage(bufferedImage); // reset, using new zoomScale
    }

    public int getBrightness() {
        if (bDemoBrightness) return iDemoBrightness;
        int foundBrightness = 0;
        for (MyUndoableEdit ue : alChanges) {
            Object obj = ue.object;
            if (obj instanceof Tuple) {
                Tuple t = (Tuple) obj;
                String s = (String) t.a;
                if ("Brightness".equalsIgnoreCase(s)) {
                    foundBrightness = ((Integer) t.b).intValue();
                }
            }
        }
        return foundBrightness;
    }

    public void setBrightness(int brightness) {
        if (bufferedImage == null) return;

        iDemoBrightness = brightness;
        if (bDemoBrightness) {
            updateTempBufferedImage();
        }
        else {
            if (!bReplayingChanges) {
                if (getBrightness() != brightness) {
                    addUndoable("Brightness", new Tuple("Brightness", brightness));
                    replayChanges();
                    onImageChanged();
                }
            }
            else {
                BufferedImage bi = OAImageUtil.brighter(bufferedImage, (byte) brightness);
                setBufferedImage(bi);
            }
        }
    }

    private volatile AtomicInteger aiUpdateTempCnt = new AtomicInteger();

    public void updateTempBufferedImage() {
        final int pUpdatTempCnt = aiUpdateTempCnt.incrementAndGet();
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (aiUpdateTempCnt.get() > pUpdatTempCnt+3) return null;

                BufferedImage bi = bufferedImage;
                if (bDemoBrightness) {
                    bi = OAImageUtil.brighter(bi, (byte) iDemoBrightness);
                }
                else if (bDemoContrast) {
                    bi = OAImageUtil.contrast(bi, (float) (1.0 + (iDemoContrast * .1)));
                }
                else {
                    return null;
                }
                if (aiUpdateTempCnt.get() > pUpdatTempCnt+3) return null;
                
                if (bi != null && zoomScale != 1.0) {
                    bi = OAImageUtil.scale(bi, zoomScale, zoomScale);
                }

                bufferedImageTemp = bi;
                return null;
            }

            @Override
            protected void done() {
                repaint();
            }
        };
        sw.execute();
    }

    public int getContrast() {
        if (bDemoContrast) return iDemoContrast;
        int foundContrast = 0;
        for (MyUndoableEdit ue : alChanges) {
            Object obj = ue.object;
            if (obj instanceof Tuple) {
                Tuple t = (Tuple) obj;
                String s = (String) t.a;
                if ("Contrast".equalsIgnoreCase(s)) {
                    foundContrast = ((Integer) t.b).intValue();
                }
            }
        }
        return foundContrast;
    }

    public void setContrast(int contrast) {
        if (bufferedImage == null) return;

        iDemoContrast = contrast;
        if (bDemoContrast) {
            updateTempBufferedImage();
        }
        else {
            if (!bReplayingChanges) {
                if (getContrast() != contrast) {
                    addUndoable("Contrast", new Tuple("Contrast", contrast));
                    replayChanges();
                    onImageChanged();
                }
            }
            else {
                BufferedImage bi = OAImageUtil.contrast(bufferedImage, (float) (1.0 + (contrast * .1)));
                setBufferedImage(bi);
            }
        }
    }

    public void refresh() {
        if (bufferedImage == null) return;
        replayChanges();
    }

    public void setDemoBrightness(boolean b) {
        if (bDemoBrightness != b) {
            bufferedImageTemp = null;
            if (b) iDemoBrightness = getBrightness();
            bDemoBrightness = b;
            replayChanges(); // if b=true, this will load changes without
                             // brightness
            if (b) updateTempBufferedImage();
        }
    }

    public void setDemoContrast(boolean b) {
        if (bDemoContrast != b) {
            bufferedImageTemp = null;
            if (b) iDemoContrast = getContrast();
            bDemoContrast = b;
            replayChanges(); // if b=true, this will load changes without
                             // brightness
            if (b) updateTempBufferedImage();
        }
    }

    public void brighter() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        RescaleOp rop = new RescaleOp(1.0f, 10, null);
        rop.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("Brighter", CHANGE_Brighter);
            onImageChanged();
        }
    }

    public void darker() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        RescaleOp rop = new RescaleOp(1.0f, -10, null);
        rop.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("Darker", CHANGE_Darker);
            onImageChanged();
        }
    }

    public void moreContrast() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        RescaleOp rop = new RescaleOp(1.2f, 0, null);
        rop.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("More Contrast", CHANGE_MoreContrast);
            onImageChanged();
        }
    }

    public void lessContrast() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        RescaleOp rop = new RescaleOp(.8f, 0, null);
        rop.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("Less Contrast", CHANGE_LessContrast);
            onImageChanged();
        }
    }

    public void sharpen() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        float data[] = { -1.0f, -1.0f, -1.0f, -1.0f, 9.0f, -1.0f, -1.0f, -1.0f, -1.0f };
        // float data[] = { -.25f, -.25f, -.25f, -.25f, 2.25f, -.25f, -.25f,
        // -.25f, -.25f };
        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        convolve.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("Sharpen", CHANGE_Sharpen);
            onImageChanged();
        }
    }

    public void blur() {
        if (bufferedImage == null) return;
        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };
        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        convolve.filter(bufferedImage, bi);
        setBufferedImage(bi);
        if (!bReplayingChanges) {
            addUndoable("Blur", CHANGE_Blur);
            onImageChanged();
        }
    }

    public void rotateCW() {
        if (bufferedImage == null) return;
        setBufferedImage(OAImageUtil.rotate(bufferedImage, 90));
        setSelectArea(null);
        if (!bReplayingChanges) {
            addUndoable("Rotate CW", CHANGE_RotateCW);
            onImageChanged();
        }
    }

    public void rotateCCW() {
        if (bufferedImage == null) return;
        setBufferedImage(OAImageUtil.rotate(bufferedImage, -90));
        setSelectArea(null);
        if (!bReplayingChanges) {
            addUndoable("Rotate CCW", CHANGE_RotateCCW);
            onImageChanged();
        }
    }

    // qqqqqqqq testing
    public void shear() {
        setZoomScale(zoomScale + .25);
    }

    /*
     * qqqqqqqqqqqqqq under development I was trying to get a 3d rotation look
     * using J2D, which does not appear to work very well, since there isnt a
     * way to change the gradual scaling along an axis
     */
    public void shear_Hold() {
        if (bufferedImage == null) return;

        int w = bufferedImage.getWidth(null);
        int h = bufferedImage.getHeight(null);

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        // g.shear(.2, .0);

        // g.scale(2.5, -1.5);
        // g.translate(-10.0, 0.0);
        // g.shear(0.5, 0.15);

        // compress width
        bi = OAImageUtil.scale(bufferedImage, 0.25, 1.0);

        h = bi.getHeight();
        w = bi.getWidth();
        BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) bi2.getGraphics();

        g.shear(0, -0.8);
        // g.rotate( Math.toRadians(60));

        g.drawImage(bi, 0, 0, null);

        // if (!bReplayingChanges) addUndoable("Rotate CCW", CHANGE_RotateCCW);
        setBufferedImage(bi2);
    }

    /*
     * flip
     * 
     * A similar but shorter way to flip a picture is to do this:
     * 
     * AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0,
     * -1.0); g.drawImage(image, reflectTransform, null);
     */

    public void scale(double percent) {
        if (bufferedImage == null) return;
        setBufferedImage(OAImageUtil.scale(bufferedImage, percent, percent));
        setSelectArea(null);
        if (!bReplayingChanges) {
            addUndoable("Scale", percent);
            onImageChanged();
        }
    }

    protected void replayChanges() {
        if (bReplayingChanges) return;
        bReplayingChanges = true;
        setImage(image);
        int foundBrightness = 0;
        int foundContrast = 0;
        boolean bFoundBrightness = false;
        boolean bFoundContrast = false;
        for (MyUndoableEdit ue : alChanges) {
            Object obj = ue.object;
            if (obj instanceof Integer) {
                switch (((Integer) obj).intValue()) {
                case CHANGE_Brighter:
                    brighter();
                    break;
                case CHANGE_Darker:
                    darker();
                    break;
                case CHANGE_MoreContrast:
                    moreContrast();
                    break;
                case CHANGE_LessContrast:
                    lessContrast();
                    break;
                case CHANGE_Sharpen:
                    sharpen();
                    break;
                case CHANGE_Blur:
                    blur();
                    break;
                case CHANGE_RotateCW:
                    rotateCW();
                    break;
                case CHANGE_RotateCCW:
                    rotateCCW();
                    break;
                }
            }
            else if (obj instanceof Rectangle) { // crop
                Rectangle rect = (Rectangle) obj;
                BufferedImage bi = OAImageUtil.crop(bufferedImage, rect);
                setBufferedImage(bi);
            }
            else if (obj instanceof Double) { // scale
                Double d = (Double) obj;
                BufferedImage bi = OAImageUtil.scale(bufferedImage, d.doubleValue(), d.doubleValue());
                setBufferedImage(bi);
            }
            else if (obj instanceof Tuple) { // brightness
                Tuple t = (Tuple) obj;
                String s = (String) t.a;
                if (s.equals("Brightness")) {
                    foundBrightness = ((Integer) t.b).intValue();
                    bFoundBrightness = true;
                }
                else {
                    foundContrast = ((Integer) t.b).intValue();
                    bFoundContrast = true;
                }
            }
        }

        if (!bDemoBrightness && bFoundBrightness) {
            setBrightness(foundBrightness);
        }
        if (!bDemoContrast && bFoundContrast) {
            setContrast(foundContrast);
        }
        bReplayingChanges = false;
        setBufferedImage(bufferedImage);
    }

    protected void addUndoable(String name, Object change) {
        if (!bReplayingChanges) {
            MyUndoableEdit ue = new MyUndoableEdit(name, change);
            alChanges.add(ue);
        }
    }

    public boolean isChanged() {
        return getUndoableCount() > 0;
    }

    public int getUndoableCount() {
        return alChanges.size();
    }

    public void clearUndoables() {
        clearUndoables(false);
    }

    private void clearUndoables(boolean bInternal) {
        if (alChanges.size() > 0) {
            alChanges.clear();
            if (!bInternal) onImageChanged();
        }
    }

    public String getUndoableEventMessage() {
        int x = alChanges.size();
        if (x == 0) return "no undoable events";
        MyUndoableEdit ue = alChanges.get(x - 1);
        return ue.getUndoPresentationName();
    }

    public void undo() {
        if (bDemoBrightness) return;
        if (bDemoContrast) return;
        int pos = alChanges.size();
        if (pos == 0) return;
        MyUndoableEdit ue = alChanges.remove(pos - 1);
        if (ue != null) ue.bCanUndo = false;
        replayChanges();
        onImageChanged();
    }

    public class MyUndoableEdit extends AbstractUndoableEdit {
        String name;
        Object object;
        boolean bCanUndo = true;

        public MyUndoableEdit(String name, Object object) {
            this.name = name;
            this.object = object;
        }

        @Override
        public String getPresentationName() {
            return name;
        }

        @Override
        public boolean canUndo() {
            return (bCanUndo && super.canUndo());
        }
    }

}
