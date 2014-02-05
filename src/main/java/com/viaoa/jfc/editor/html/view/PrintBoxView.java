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
package com.viaoa.jfc.editor.html.view;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;

import com.viaoa.jfc.editor.html.OAHTMLTextPane;


/**
 * This is the rootView for creating a fixed width OAHTMLTextPane that is
 * going to be printed.  OAHTMLTextPane.print()/preview() will create this,
 * giving a width, so that all child views will be laid out fit within the width.
 * Also, all images, which use MyImageView, will be scaled to fit.
 */
public class PrintBoxView extends BoxView {
    private OAHTMLTextPane editor;
    private int pageWidth, pageHeight;

    public PrintBoxView(OAHTMLTextPane editor, Element rootElement, int pageWidth, int pageHeight) {
        super(rootElement, BoxView.Y_AXIS);
        if (editor == null) throw new IllegalArgumentException("editor can not be null");
        this.editor = editor;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        loadChildren(getViewFactory());  // create children views
    }
    
    /** vv hack:
     *  This has to serve as the container.  We can not use Editor, because we need to have the width set to PrintBoxView.width
     *  and we dont want the views to request a repaint on OAHTMLTextPane.
     */
    private JLabel lblDummy = new JLabel() {
        @Override
        public ComponentOrientation getComponentOrientation() {
            return editor.getComponentOrientation();
        }
        @Override
        public FontMetrics getFontMetrics(Font font) {
            return editor.getFontMetrics(font);
        }
        //vv: other methods might need to be implemented to return sizing
        //      so far this is all that is required.
    };
    /** vv hack
     *  This is needed to returned a component for the child views, else a NPE can
     *  happen when a view calls StyleSheet.paint(view) and it does not have a container.
     *  Can not return the editor, since other views call it to get the container width, which needs to be the width for
     *  this boxView, and not the editor.
     */
    @Override
    public Container getContainer() {
        return lblDummy; // if this has issues, then need to set breakpoint here and find out what other code is calling it, to see how it is being used.
        //return super.getContainer();  // this is null, will cause a npe on child view that needs to get Container.orientation
        //return editor;   // will use the width of the editor.viewport instead.  See HTMLEditorKit.BodyBlockView.layoutMinorAxis()
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(pageWidth, height);
    }
    
    @Override
    protected void layout(int width, int height) {
        super.layout(pageWidth, height);
    }

    
    public ViewFactory getViewFactory() {
        ViewFactory vf = editor.getEditorKit().getViewFactory();
        return vf;
    }

    public int getPageWidth() {
        return pageWidth;
    }
    public int getPageHeight() {
        return pageHeight;
    }
    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }
    
}
