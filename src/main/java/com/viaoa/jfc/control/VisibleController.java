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
package com.viaoa.jfc.control;

import javax.swing.JComponent;
import com.viaoa.hub.*;

/**
 * Used to bind a components visible value to a one or more Hub/Property value
 * @author vincevia
 *
 */
public class VisibleController extends HubPropController {
    protected JComponent component;

    public VisibleController(JComponent comp) {
        super();
        this.component = comp;
    }    
    public VisibleController(JComponent comp, Hub hub) {
        super(hub);
        this.component = comp;
    }
    public VisibleController(JComponent comp, Hub hub, String propertyName) {
        super(hub, propertyName);
        this.component = comp;
    }
    public VisibleController(JComponent comp, Hub hub, String propertyName, Object compareValue) {
        super(hub, propertyName, compareValue);
        this.component = comp;
    }
    
    @Override
    protected void onUpdate(boolean bValid) {
        if (this.component != null) {
            this.component.setVisible(bValid);
        }
    }

}
