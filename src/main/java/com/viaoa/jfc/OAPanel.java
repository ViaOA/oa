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
package com.viaoa.jfc;

import java.awt.LayoutManager;

import javax.swing.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.OAJfcController;

/**
 *
 */
public class OAPanel extends JPanel implements OAJfcComponent {
    private Hub hub;
    private OAJfcController control;
    
    public OAPanel(Hub h, LayoutManager lm) {
        super(lm);
        this.hub = h;
        setup();
    }
    public OAPanel(Hub h) {
        this.hub = h;
        setup();
    }

    protected void setup() {
        control = OAJfcController.createAoNotNull(this, hub);
    }
    
    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLable() {
        return getController().getLabel();
    }
    @Override
    public OAJfcController getController() {
        return control;
    }
    @Override
    public void initialize() {
    }
}
