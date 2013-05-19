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
