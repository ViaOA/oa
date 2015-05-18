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
