package com.viaoa.jfc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JSplitPane;

/**
 * Sets the divider for a splitpane.
 * Corrects some of the JSplitPane issues with setting the divider's location.
 * @author vvia
 */
public class OASplitPane extends JSplitPane {
    
    private ComponentAdapter componentListener;
    
    public OASplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, newLeftComponent, newRightComponent);
        setContinuousLayout(true);
    }
    public OASplitPane(int newOrientation, boolean continousLayout, Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, continousLayout, newLeftComponent, newRightComponent);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();

        if (componentListener == null) {
            addComponentListener(getMyComponentListener());
            componentResized(true);
        }
        bDontAutoAdjust = false;
    }
 
    private boolean bDontAutoAdjust;
    private boolean bIgnore;

    @Override
    public void setDividerLocation(int location) {
        super.setDividerLocation(location);
        if (!bIgnore) bDontAutoAdjust = true;
    }
    
//qqqqqqqq dont auto resize if it was manually changed
//qqqqqq   dont auto size if it was collapsed or expanded
    
    protected ComponentAdapter getMyComponentListener() {
        if (componentListener != null) return componentListener;

        componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (bDontAutoAdjust) return;
                OASplitPane.this.componentResized(false);
            }
        };            
        return componentListener;
    }

    protected void componentResized(boolean bInit) {
        try {
            bIgnore = true;
            _componentResized(bInit);
        }
        finally {
            bIgnore = false;
        }
    }

    private void _componentResized(boolean bInit) {
        Dimension d = getSize();
        if (!bInit && (d.width == 0 || d.height == 0)) return;                

        int div;
        if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            Dimension dTop = getLeftComponent().getPreferredSize();
            Dimension dBottom = getRightComponent().getPreferredSize();
            if (bInit && d.height == 0) {
                div = dTop.height + 18;
            }
            else if (((dTop.height+18) + dBottom.height) < d.height) {
                div = dTop.height + 18;
            }
            else if (dTop.height < d.height/2) {
                div = dTop.height + 18;
            }
            else if (dBottom.height < d.height/2) {
                div = (d.height - dBottom.height);
            }
            else {
                div = d.height/2;
            }
        }
        else {
            Dimension dLeft = getLeftComponent().getPreferredSize();
            Dimension dRight = getRightComponent().getPreferredSize();

            if (bInit && d.width == 0) {
                div = dLeft.width + 18;
            }
            else if (((dLeft.width+18) + dRight.width) < d.width) {
                div = dLeft.width + 18;
            }
            else if (dLeft.width < d.width/2) {
                div = dLeft.width + 18;
            }
            else if (dRight.width < d.width/2) {
                div = (d.width - dRight.width);
            }
            else {
                div = d.width/2;
            }
        }
        setDividerLocation(div);
    }
    
}
