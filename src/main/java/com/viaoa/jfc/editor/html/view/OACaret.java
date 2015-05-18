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