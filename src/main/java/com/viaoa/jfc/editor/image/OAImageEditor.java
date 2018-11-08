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
package com.viaoa.jfc.editor.image;

import java.awt.*;

import javax.swing.*;
import com.viaoa.hub.Hub;
import com.viaoa.jfc.OAJfcComponent;
import com.viaoa.jfc.OAScroller;
import com.viaoa.jfc.editor.image.control.OAImagePanelController;

/**
 * Manages a BufferedImage that is based on an original image and a collection
 * of undoable changes that have been made to it.
 * 
 * @author vincevia
 */
public class OAImageEditor extends JPanel implements OAJfcComponent {

    protected OAImagePanelController control;
    
    /**
     * Create a new ImagePanel that allows mouse control to select a crop
     * rectangle.
     */
    public OAImageEditor(Hub hub, String property) {
        super(new BorderLayout());
        control = new OAImagePanelController(hub, this, property);

        add(new OAScroller(control.getToolBar()), BorderLayout.NORTH);
        add(new JScrollPane(control.getOAImagePanel()), BorderLayout.CENTER);
    }

    @Override
    public OAImagePanelController getController() {
        return control;
    }

    @Override
    public void initialize() {
    }

}
