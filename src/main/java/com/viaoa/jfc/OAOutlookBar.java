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
package com.viaoa.jfc;

//Import the GUI classes
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.viaoa.jfc.OAButton;

//Import the Java classes
import java.util.*;

/**
 * A JOutlookBar provides a component that is similar to a JTabbedPane, but instead of maintaining tabs,
 * it uses Outlook-style bars to control the visible component
 * 
 */
public class OAOutlookBar extends JPanel implements ActionListener {
    /**
     * The top panel: contains the buttons displayed on the top of the JOutlookBar
     */
    private JPanel topPanel = new JPanel(new GridLayout(1, 1));

    /**
     * The bottom panel: contains the buttons displayed on the bottom of the JOutlookBar
     */
    private JPanel bottomPanel = new JPanel(new GridLayout(1, 1));

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private HashMap<String, BarInfo> hashBars = new HashMap<String, BarInfo>();
    private ArrayList<String> arrayList = new ArrayList<String>(10);

    /**
     * The currently visible bar (zero-based index)
     */
    private int visibleBar = 0;

    /**
     * A place-holder for the currently visible component
     */
    private JComponent visibleComponent = null;

    /**
     * Creates a new JOutlookBar; after which you should make repeated calls to addBar() for each bar
     */
    public OAOutlookBar() {

        cardLayout = new CardLayout(0, 0);
        cardPanel = new JPanel(cardLayout);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(cardPanel, BorderLayout.CENTER);
    }

    /**
     * Adds the specified component to the JOutlookBar and sets the bar's name
     * 
     * @param name
     *            The name of the outlook bar
     * @param componenet
     *            The component to add to the bar
     */
    public void addBar(String name, JComponent component) {
        addBar(name, null, component, -1);
    }

    public void addBar(String name, JComponent component, int pos) {
        addBar(name, null, component, pos);
    }

    public void addBar(String name, Icon icon, JComponent component) {
        addBar(name, icon, component, -1);
    }

    public void addBar(String name, Icon icon, JComponent component, int pos) {
        addBar(name, name, icon, component, -1);
    }

    public void addBar(String name, String title, Icon icon, JComponent component) {
        addBar(name, title, icon, component, -1);
    }

    /**
     * Adds the specified component to the JOutlookBar and sets the bar's name
     * 
     * @param name
     *            The name of the outlook bar
     * @param icon
     *            An icon to display in the outlook bar
     * @param componenet
     *            The component to add to the bar
     * @param title
     *            the title on the outlook bar button
     */
    public void addBar(String name, String title, Icon icon, JComponent component, int pos) {
        BarInfo barInfo = hashBars.get(name);
        if (barInfo != null) {
            cardPanel.remove(barInfo.component);
            hashBars.remove(name);
            arrayList.remove(name);
        }

        if (pos < 0 || pos >= arrayList.size()) {
            this.arrayList.add(name);
        }
        else {
            this.arrayList.add(pos, name);
        }

        cardPanel.add(component, name);
        barInfo = new BarInfo(name, title, icon, component);
        barInfo.getButton().addActionListener(this);
        this.hashBars.put(name, barInfo);
        render();
    }

    public void setTitle(String barName, String title) {
        BarInfo barInfo = (BarInfo) hashBars.get(barName);
        if (barInfo != null) {
            barInfo.title = title;
            barInfo.button.setText(title);
        }
    }

    public void setIcon(String barName, Icon icon) {
        BarInfo barInfo = (BarInfo) hashBars.get(barName);
        if (barInfo != null) {
            barInfo.button.setIcon(icon);
        }
    }

    public void setEnabled(String barName, boolean bEnabled) {
        BarInfo barInfo = (BarInfo) hashBars.get(barName);
        if (barInfo != null) {
            barInfo.button.setEnabled(bEnabled);
        }
    }

    public void setVisible(String barName, boolean bVisible) {
        BarInfo barInfo = (BarInfo) hashBars.get(barName);
        if (barInfo != null) {
            barInfo.button.setVisible(bVisible);
            render();
        }
    }

    /**
     * Removes the specified bar from the JOutlookBar
     * 
     * @param name
     *            The name of the bar to remove
     */
    public void removeBar(String name) {
        BarInfo barInfo = hashBars.get(name);
        if (barInfo != null) {
            cardPanel.remove(barInfo.component);
            hashBars.remove(name);
            arrayList.remove(name);
            if (visibleBar >= arrayList.size()) visibleBar--;
            render();
        }
    }

    public void removeBar(int pos) {
        if (pos >= arrayList.size()) return;
        String name = arrayList.get(pos);
        BarInfo barInfo = hashBars.get(name);
        if (barInfo != null) {
            cardPanel.remove(barInfo.component);
            hashBars.remove(name);
            arrayList.remove(name);
            if (visibleBar >= arrayList.size()) visibleBar--;
            render();
        }
    }

    /**
     * Returns the index of the currently visible bar (zero-based)
     * 
     * @return The index of the currently visible bar
     */
    public int getVisibleBar() {
        return this.visibleBar;
    }

    /**
     * Programmatically sets the currently visible bar; the visible bar index must be in the range of 0
     * to size() - 1
     * 
     * @param visibleBar
     *            The zero-based index of the component to make visible
     */
    public void setVisibleBar(int visibleBar) {
        if (visibleBar >= 0 && visibleBar < this.arrayList.size() - 1) {
            this.visibleBar = visibleBar;
            render();
        }
    }

    public void setVisibleBar(String visibleBar) {
        int x = arrayList.indexOf(visibleBar);
        setVisibleBar(x);
    }

    /**
     * Causes the outlook bar component to rebuild itself; this means that it rebuilds the top and
     * bottom panels of bars as well as making the currently selected bar's panel visible
     */
    public void render() {
        // Compute how many bars we are going to have where
        int totalBars = this.arrayList.size();

        // Get an iterator to walk through out bars with
        Iterator itr = this.hashBars.keySet().iterator();

        // Render the top bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        this.topPanel.removeAll();
        GridLayout topLayout = (GridLayout) this.topPanel.getLayout();

        // make sure that the visibleBar is still valid
        BarInfo barInfo = null;
        int newVisibleBar = -1;
        for (int i = 0; i < arrayList.size(); i++) {
            String barName = arrayList.get(i);
            barInfo = (BarInfo) hashBars.get(barName);
            if (barInfo.getButton().isVisible() && barInfo.getButton().isEnabled()) {
                if (i == visibleBar) {
                    newVisibleBar = i;
                    break;
                }
                if (i > visibleBar) {
                    if (newVisibleBar >= 0) break;
                }
                newVisibleBar = i;
            }
        }
        visibleBar = newVisibleBar;

        int cnt = 0;
        for (int i = 0; i <= visibleBar; i++) {
            String barName = arrayList.get(i);
            barInfo = (BarInfo) hashBars.get(barName);
            if (barInfo.getButton().isVisible()) cnt++;
        }
        topLayout.setRows(cnt);
        // was: topLayout.setRows( visibleBar+1 );

        barInfo = null;
        int barPos = 0;
        for (int i = 0; i <= visibleBar; i++) {
            String barName = arrayList.get(barPos++);
            barInfo = (BarInfo) this.hashBars.get(barName);
            if (barInfo.getButton().isVisible()) {
                this.topPanel.add(barInfo.getButton());
            }
        }
        this.topPanel.validate();

        // Render the center component: remove the current component (if there
        // is one) and then put the visible component in the center of this panel
        if (this.visibleComponent != null) {
            this.remove(this.visibleComponent);
        }

        this.visibleComponent = barInfo == null ? null : barInfo.getComponent();
        if (barInfo != null) cardLayout.show(cardPanel, barInfo.name);

        // Render the bottom bars: remove all components, reset the GridLayout to
        // hold to correct number of bars, add the bars, and "validate" it to
        // cause it to re-layout its components
        this.bottomPanel.removeAll();
        GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();

        cnt = 0;
        for (int i = visibleBar + 1; i < arrayList.size(); i++) {
            String barName = arrayList.get(i);
            barInfo = (BarInfo) hashBars.get(barName);
            if (barInfo.getButton().isVisible()) cnt++;
        }
        bottomLayout.setRows(cnt);

        int x = (arrayList.size() - visibleBar) - 1;
        // bottomLayout.setRows(x);

        for (int i = 0; i < x; i++) {
            String barName = arrayList.get(barPos++);
            barInfo = (BarInfo) this.hashBars.get(barName);
            if (barInfo.getButton().isVisible()) {
                this.bottomPanel.add(barInfo.getButton());
            }
        }
        this.bottomPanel.validate();

        // Validate all of our components: cause this container to re-layout its subcomponents
        this.validate();
    }

    /**
     * Invoked when one of our bars is selected
     */
    public void actionPerformed(ActionEvent e) {
        for (Iterator i = this.hashBars.keySet().iterator(); i.hasNext();) {
            String barName = (String) i.next();
            BarInfo barInfo = (BarInfo) this.hashBars.get(barName);
            if (barInfo.getButton() == e.getSource()) {
                this.visibleBar = arrayList.indexOf(barName);

                render();

                onBarSelected(barName, this.visibleBar);
                return;
            }
        }
    }

    protected void onBarSelected(String barName, int currentBar) {
        onBarSelected(barName);
        onBarSelected(currentBar);
    }

    protected void onBarSelected(String barName) {
    }

    protected void onBarSelected(int currentBar) {
    }

    /**
     * Debug, dummy method
     */
    public static JPanel getDummyPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(name, JLabel.CENTER));
        return panel;
    }

    /**
     * Debug test...
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("JOutlookBar Test");
        OAOutlookBar outlookBar = new OAOutlookBar();
        outlookBar.addBar("One", getDummyPanel("One"));
        outlookBar.addBar("Two", getDummyPanel("Two"));
        outlookBar.addBar("Three", getDummyPanel("Three"));
        outlookBar.addBar("Four", getDummyPanel("Four"));
        outlookBar.addBar("Five", getDummyPanel("Five"));
        outlookBar.setVisibleBar(2);
        frame.getContentPane().add(outlookBar);

        frame.setSize(800, 600);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(d.width / 2 - 400, d.height / 2 - 300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Internal class that maintains information about individual Outlook bars; specifically it
     * maintains the following information:
     * 
     * name The name of the bar button The associated JButton for the bar component The component
     * maintained in the Outlook bar
     */
    class BarInfo {
        /**
         * The name of this bar
         */
        private String name;

        private String title;

        /**
         * The JButton that implements the Outlook bar itself
         */
        private JButton button;

        /**
         * The component that is the body of the Outlook bar
         */
        private JComponent component;

        /**
         * Creates a new BarInfo
         * 
         * @param name
         *            The name of the bar
         * @param component
         *            The component that is the body of the Outlook Bar
         */
        public BarInfo(String name, String title, JComponent component) {
            this.name = name;
            this.component = component;
            this.title = title;
            this.button = new JButton(title);
            setupButton(button);
        }

        public void setupButton(AbstractButton cmd) {
            cmd.setFocusPainted(false);
            cmd.setBorderPainted(true);
            cmd.setFocusable(false);
            cmd.setContentAreaFilled(false);
            cmd.setMargin(new Insets(0, 0, 0, 0));

            Font font = cmd.getFont();
            font = font.deriveFont((float) (font.getSize() - 1));
            // cmd.setFont(font);

            cmd.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    AbstractButton b = (AbstractButton) e.getComponent();
                    if (b.isEnabled()) {
                        b.setContentAreaFilled(true);
                        // b.setBorderPainted(true);
                    }
                }

                public void mouseExited(MouseEvent e) {
                    AbstractButton but = (AbstractButton) e.getComponent();
                    but.setContentAreaFilled(false);
                }
            });
        }

        /**
         * Creates a new BarInfo
         * 
         * @param name
         *            The name of the bar
         * @param icon
         *            JButton icon
         * @param component
         *            The component that is the body of the Outlook Bar
         */
        public BarInfo(String name, String title, Icon icon, JComponent component) {
            this.name = name;
            this.component = component;
            this.button = new JButton(title, icon);
            setupButton(button);
        }

        /**
         * Returns the name of the bar
         * 
         * @return The name of the bar
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets the name of the bar
         * 
         * @param The
         *            name of the bar
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the outlook bar JButton implementation
         * 
         * @return The Outlook Bar JButton implementation
         */
        public JButton getButton() {
            return this.button;
        }

        /**
         * Returns the component that implements the body of this Outlook Bar
         * 
         * @return The component that implements the body of this Outlook Bar
         */
        public JComponent getComponent() {
            return this.component;
        }
    }
    public JComponent getVisibleComponent() {
        return visibleComponent;
    }
}
