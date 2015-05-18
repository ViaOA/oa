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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import javax.swing.plaf.basic.ComboPopup;

import com.viaoa.jfc.image.ColorIcon;

/**
 * Popup window that displays a panel of colors to select from.
 * @author vvia
 */
public class OAColorPopup implements ComboPopup, MouseMotionListener, MouseListener, KeyListener, PopupMenuListener {
    
    protected JPopupMenu popup;
    private ColorPanel colorPanel;
    private JComponent parent;
    private String colorChooserTitle="Select Color";
    private JButton cmdMore, cmdClear, cmdCurrentColor;

    private Color currentColor;
    
    
    public OAColorPopup(final JComponent parent) {
        this.parent = parent;

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.black));
        popup.setLayout(new BorderLayout());
        popup.addPopupMenuListener(this);

        colorPanel = new ColorPanel() {
            public void setColor(Color c) {
                super.setColor(c);
                OAColorPopup.this.setColor(c);
            }
        };
        
        popup.add(colorPanel, BorderLayout.NORTH);
        
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        cmdCurrentColor = new JButton("current");
        cmdCurrentColor.setIcon(new ColorIcon(null));
        setupButton(cmdCurrentColor);
        cmdCurrentColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OAColorPopup.this.setColor(currentColor);
            }
        });
        pan.add(cmdCurrentColor);

        cmdClear = new JButton("clear");
        setupButton(cmdClear);
        cmdClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OAColorPopup.this.setColor(null);
            }
        });
        pan.add(cmdClear);

        cmdMore = new JButton("more...");
        setupButton(cmdMore);
        cmdMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OAColorPopup.this.onShowColorChooser();
            }
        });
        pan.add(cmdMore);
        popup.add(pan, BorderLayout.SOUTH);
        popup.pack();
    }

    public void setColorChooserTitle(String s) {
        colorChooserTitle = s;
    }
    public void setMoreButtonText(String s) {
        cmdMore.setText(s);
    }
    public void setClearButtonText(String s) {
        cmdClear.setText(s);
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        cmdCurrentColor.setIcon(new ColorIcon(color));
        String s = "";
        if (color != null) {
            s = "";
            s = "R "+ color.getRed() + ", G "+ color.getGreen() + ", B "+ color.getBlue(); 
        }
        cmdCurrentColor.setToolTipText(s);
    }
    
    protected void setVisible(boolean b) {
        popup.setVisible(b);
    }
    /**
     * Called when cmdMore is selected, will display a ColorChooser, uses JColorChooser by default.
     */
    public void onShowColorChooser() {
        popup.setVisible(false);
        Color c = JColorChooser.showDialog(parent, colorChooserTitle, getColor());
        if (c != null) OAColorPopup.this.setColor(c);
    }
    
    
    private boolean bIsSettingColor;
    public void setColor(Color color) {
        if (bIsSettingColor) return;
        bIsSettingColor = true;
        colorPanel.setColor(color);
        bIsSettingColor = false;
    }
    
    public Color getColor() {
        return colorPanel.getColor();
    }

    public void show() {
        popup.show(parent, 0, parent.getHeight());
    }

    public void hide() {
        popup.setVisible(false);
    }

    protected JList list = new JList();
    public JList getList() {
        return list;
    }

    public MouseListener getMouseListener() {
        return this;
    }

    public MouseMotionListener getMouseMotionListener() {
        return this;
    }

    public KeyListener getKeyListener() {
        return this;
    }

    public boolean isVisible() {
        return popup.isVisible();
    }

    public void uninstallingUI() {
        popup.removePopupMenuListener(this);
    }

    // MouseListener
    public void mousePressed( MouseEvent e ) {
        doPopup(e); 
    }
    public void mouseReleased( MouseEvent e ) {}
    

    // something else registered for MousePressed
    public void mouseClicked(MouseEvent e) {
        // 20080515 was: doPopup(e);
    }
    protected void doPopup(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if (!parent.isEnabled()) return;

        /*
        if (parent.isEditable() ) { 
            comboBox.getEditor().getEditorComponent().requestFocus();
        } 
        else {
            comboBox.requestFocus();
        }
        */
        togglePopup();
    }

    protected boolean mouseInside = false;
    public void mouseEntered(MouseEvent e) {
        mouseInside = true;
    }
    public void mouseExited(MouseEvent e) {
        mouseInside = false;
    }

    // MouseMotionListener
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    // KeyListener
    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased( KeyEvent e ) {
        if ( e.getKeyCode() == KeyEvent.VK_SPACE ||
         e.getKeyCode() == KeyEvent.VK_ENTER ) {
        togglePopup();
        }
    }

    /**
     * Variables hideNext and mouseInside are used to
     * hide the popupMenu by clicking the mouse in the JComboBox
     */
    public void popupMenuCanceled(PopupMenuEvent e) {}
    protected boolean hideNext = false;
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        hideNext = mouseInside;
    }
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }


    protected void togglePopup() {
        if (isVisible()) hide();
        else show();
        hideNext = false;
    }
    
    /**
     * Removes extra affects from a button.
     */
    protected void setupButton(JButton cmd) {
        cmd.setFocusable(false);
        cmd.setBorderPainted(false);
        cmd.setContentAreaFilled(false);
        cmd.setMargin(new Insets(1,1,1,1));
        cmd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                AbstractButton b = (AbstractButton) e.getComponent();
                if (b.isEnabled()) {
                    b.setContentAreaFilled(true);
                    b.setBorderPainted(true);
                }
            }
            public void mouseExited(MouseEvent e) {
                AbstractButton but = (AbstractButton) e.getComponent();
                boolean b = false;
                if (but instanceof JToggleButton) {
                    if ( ((JToggleButton)but).isSelected() ) b = true;
                }
                but.setBorderPainted(b);
                but.setContentAreaFilled(b);
            }
        });
    }
}




// panel that has all of the colors
class ColorPanel extends JPanel {
    protected Border unselectedBorder;
    protected Border selectedBorder;
    protected Border activeBorder;
    protected Border border255;
    protected Hashtable hashPane;
    protected ColorPane selectedColorPane;
    
    public ColorPanel() {
        unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, getBackground()),new BevelBorder(BevelBorder.LOWERED,Color.white, Color.gray));
        selectedBorder = new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.red),new MatteBorder(1, 1, 1, 1, getBackground()));
        activeBorder = new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.blue),new MatteBorder(1, 1, 1, 1, getBackground()));
        
        Border border = new MatteBorder(1, 1, 1, 1, Color.darkGray);
        border = new CompoundBorder(border, new MatteBorder(2, 2, 2, 2, getBackground()));
        border255 = border;

        int[] valuesA = new int[] { 0, 128, 192, 220  };  // values used for rows
        int[] values = new int[] { 0, 128, 255 };         // values used for columns (for each RGB)

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());

        int cols = (values.length-1) * 6;
        int rows = (valuesA.length * 2) - 2;
        
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(rows, cols));
        panel.add(p);

        hashPane = new Hashtable();

        for (int xx=0; xx<2; xx++) {
            boolean bDown = (xx == 0);
            int row = 0;
            if (!bDown) row = 1;
            
            for ( ; row < valuesA.length-(bDown?0:1); row++) {
                int zero = 0;
                int max = 255;

                if (bDown) zero = valuesA[valuesA.length-1-row];
                else max = valuesA[valuesA.length-1-row];
                
                // red=255  blue=255-0
                for (int b=0; b<values.length; b++) {
                    int blue = values[values.length-1-b];
                    if (bDown) {
                        if (blue < zero) blue = zero;
                    }
                    else {
                        if (blue > max) blue = max;
                    }
                    Color c = new Color(max, zero, blue);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
                // red=255  green=0-255
                for (int g=1; g<values.length; g++) {
                    int green = values[g];
                    if (bDown) {
                        if (green < zero) green = zero;
                    }
                    else {
                        if (green > max) green = max;
                    }
                    Color c = new Color(max, green, zero);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
                // green=255  red=255-0
                for (int r=1; r<values.length; r++) {
                    int red = values[values.length-1-r];
                    if (bDown) {
                        if (red < zero) red = zero;
                    }
                    else {
                        if (red > max) red = max;
                    }
                    Color c = new Color(red, max, zero);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
                // green=255  blue=0-255
                for (int b=1; b<values.length; b++) {
                    int blue = values[b];
                    if (bDown) {
                        if (blue < zero) blue = zero;
                    }
                    else {
                        if (blue > max) blue = max;
                    }
                    Color c = new Color(zero, max, blue);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
                // blue=255  green=255-0
                for (int g=1; g<values.length; g++) {
                    int green = values[values.length-1-g];
                    if (bDown) {
                        if (green < zero) green = zero;
                    }
                    else {
                        if (green > max) green = max;
                    }
                    Color c = new Color(zero, green, max);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
                // blue=255  red=0-255
                for (int r=1; r<values.length-1; r++) {
                    int red = values[r];
                    if (bDown) {
                        if (red < zero) red = zero;
                    }
                    else {
                        if (red > max) red = max;
                    }
                    Color c = new Color(red, zero, max);
                    ColorPane pn = new ColorPane(c);
                    p.add(pn);
                    hashPane.put(c, pn);
                }
            }
        }

        p = new JPanel();
        p.setBorder(new EmptyBorder(5, 0,0,0));
        p.setLayout(new GridLayout(1, cols));
        
        panel.add(p, BorderLayout.SOUTH);

        // white to black
        int x = (int) 256/cols;
        for (int i=0; i<cols; i++) {
            int c = (cols - i) * x;
            
            if (i == 0) c = 255;
            if (i+1 == cols) c = 0;
            
            Color color = new Color(c,c,c);
            ColorPane pn = new ColorPane(color);
            p.add(pn);
            hashPane.put(color, pn);
        }
        add(panel);
    }


    public void setColor(Color c) {
        if (c == null) c = Color.black;
        Object obj = hashPane.get(c);
        if (obj == null) return;
        if (selectedColorPane != null) selectedColorPane.setSelected(false);
        selectedColorPane = (ColorPane) obj;
        selectedColorPane.setSelected(true);
    }

    public Color getColor() {
        if (selectedColorPane == null) return null;
        return selectedColorPane.getColor();
    }

    
    // each color block
    class ColorPane extends JPanel implements MouseListener {
        private static final long serialVersionUID = 1L;
        protected Color color;
        protected boolean bSelected;
        boolean b255;

        public ColorPane(Color c) {
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            this.b255 = (r==0||r==255) && (g==0||g==255) && (b==0||b==255);
            color = c;
            setBackground(c);
            setBorder(b255 ? border255 : unselectedBorder);
            String msg = "R "+r+", G "+g+", B "+b;
            setToolTipText(msg);
            addMouseListener(this);
        }

        public Color getColor() {
            return color;
        }

        public Dimension getPreferredSize() {
            return new Dimension(15, 15);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void setSelected(boolean selected) {
            bSelected = selected;
            if (bSelected)
                setBorder(selectedBorder);
            else {
                if (b255) setBorder(border255);
                else setBorder(unselectedBorder);
            }
        }

        public boolean isSelected() {
            return bSelected;
        }

        public void mousePressed(MouseEvent e) {}

        public void mouseClicked(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {
            setColor(color);
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }

        public void mouseEntered(MouseEvent e) {
            setBorder(activeBorder);
        }

        public void mouseExited(MouseEvent e) {
            setBorder(bSelected ? selectedBorder :
                b255 ? border255 : unselectedBorder);
        }
    }
    
}
