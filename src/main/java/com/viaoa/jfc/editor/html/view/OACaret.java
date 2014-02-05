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


import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Custom caret used by HTMLEditor
 * @author vvia
 *
 */
public class OACaret extends DefaultCaret {

	private float caretWidth = 2.0f;
    Stroke stroke;

    public OACaret(float width) {
        setWidth(width);
    }
    
    public void setWidth(float width) {
        stroke = new BasicStroke(caretWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);        
    }
    
    public void paint(Graphics g) {
        ((Graphics2D)g).setStroke(stroke);
        super.paint(g);
    }    

}