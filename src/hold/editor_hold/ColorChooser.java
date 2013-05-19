package com.dispatcher.editor;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.net.*;
import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
//import javax.help.*;

import com.viaoa.jfc.*;


public class ColorChooser extends AbstractColorChooserPanel {
	
	JColorChooser parent;
	String name = "Common";
	
	protected Border m_unselectedBorder;
	protected Border m_selectedBorder;
	protected Border m_activeBorder;
	protected Border m_pureBorder;
	

	protected Hashtable m_panes;
	protected ColorPane m_selected;

	public ColorChooser() {
        setup();
	}
	
	void setup() {
		m_unselectedBorder = new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, getBackground()),
			new BevelBorder(BevelBorder.LOWERED,
			Color.white, Color.gray));

		m_selectedBorder = new CompoundBorder(
			new MatteBorder(2, 2, 2, 2, Color.red),
			new MatteBorder(1, 1, 1, 1, getBackground()));
		m_activeBorder = new CompoundBorder(
			new MatteBorder(2, 2, 2, 2, Color.blue),
			new MatteBorder(1, 1, 1, 1, getBackground()));

		m_pureBorder = new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, Color.darkGray),
			new BevelBorder(BevelBorder.LOWERED,
			Color.white, Color.gray));


		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.setLayout(new GridLayout(8, 8));
		m_panes = new Hashtable();

		// int[] values = new int[] { 0, 128, 192, 255 };
		int[] values = new int[] { 0, 86, 172, 255 };
		for (int r=0; r<values.length; r++) {
			for (int g=0; g<values.length; g++) {
				for (int b=0; b<values.length; b++) {
					Color c = new Color(values[r], values[g], values[b]);
					boolean bPure =  ((values[r] == 0 || values[r] == 255) && (values[g] == 0 || values[g] == 255) && (values[b] == 0 || values[b] == 255));
					ColorPane pn = new ColorPane(c, bPure);
					p.add(pn);
					m_panes.put(c, pn);
				}
			}
		}
		this.add(p);
	}

	public void setColor(Color c) {
		Object obj = m_panes.get(c);
		if (obj == null)
			return;
		if (m_selected != null)
			m_selected.setSelected(false);
		m_selected = (ColorPane)obj;
		m_selected.setSelected(true);
	}

	public Color getColor() {
		if (m_selected == null)
			return null;
		return m_selected.getColor();
	}

	public void doSelection() {
        if (parent != null) parent.setColor(this.getColor());
	}

	
// Chooser Methods **********************************
    protected void buildChooser() {
    }
    
    public String getDisplayName() {
        return name;
    }
    public Icon getLargeDisplayIcon() {
        return null;
    }
        
    public Icon getSmallDisplayIcon() {
        return null;
    }
    
    public void installChooserPanel(JColorChooser enclosingChooser) {
        parent = enclosingChooser;
    }
	
    public void updateChooser() {
        if (parent != null) {
            setColor(parent.getColor());
        }
    }
	
	
	class ColorPane extends JPanel implements MouseListener {
		protected Color m_c;
		protected boolean m_selected;
		boolean bPure; // if all values are 0 or 255

		public ColorPane(Color c, boolean bPure) {
		    this.bPure = bPure;
			m_c = c;
			setBackground(c);
			setSelected(false);
			String msg = "R "+c.getRed()+", G "+c.getGreen()+
				", B "+c.getBlue();
			setToolTipText(msg);
			addMouseListener(this);
		}

		public Color getColor() {
			return m_c;
		}

		public Dimension getPreferredSize() {
			return new Dimension(18, 18);
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public void setSelected(boolean selected) {
			m_selected = selected;
			if (m_selected)
				setBorder(m_selectedBorder);
			else
				setBorder(bPure ? m_pureBorder : m_unselectedBorder);
		}

		public boolean isSelected() {
			return m_selected;
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseClicked(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {
			setColor(m_c);
			MenuSelectionManager.defaultManager().clearSelectedPath();
			doSelection();
		}

		public void mouseEntered(MouseEvent e) {
			setBorder(m_activeBorder);
		}

		public void mouseExited(MouseEvent e) {
            setSelected(m_selected);
		}
	}
}
