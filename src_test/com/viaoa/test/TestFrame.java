package com.viaoa.test;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.ComponentOrientation;
import javax.swing.table.TableColumn;

public class TestFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JLabel jLabel = null;

	private JMenuBar jJMenuBar = null;

	private JMenu jMenu = null;

	private JMenuItem jMenuItem = null;

	private JMenuItem jMenuItem1 = null;

	private JTree jTree = null;

	private JSplitPane jSplitPane = null;

	private JScrollPane jScrollPane = null;

	private JScrollPane jScrollPane1 = null;

	private JTable jTable = null;

	private JProgressBar jProgressBar = null;

	private JScrollPane jScrollPane2 = null;

	private JPanel jPanel1 = null;

	private JLabel jLabel1 = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JTextField jTextField = null;

	private TableColumn tableColumn = null;   //  @jve:decl-index=0:visual-constraint=""

	/**
	 * This is the default constructor
	 */
	public TestFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(538, 440);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Test Frame");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJTree(), BorderLayout.WEST);
			jContentPane.add(getJSplitPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			flowLayout.setVgap(9);
			flowLayout.setHgap(13);
			jLabel = new JLabel();
			jLabel.setText("This is the label");
			jPanel = new JPanel();
			jPanel.setLayout(flowLayout);
			jPanel.add(jLabel, null);
			jPanel.add(getJProgressBar(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu() {
		if (jMenu == null) {
			jMenu = new JMenu();
			jMenu.setText("Menu Here!");
			jMenu.add(getJMenuItem());
			jMenu.add(getJMenuItem1());
		}
		return jMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem() {
		if (jMenuItem == null) {
			jMenuItem = new JMenuItem();
		}
		return jMenuItem;
	}

	/**
	 * This method initializes jMenuItem1	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem1() {
		if (jMenuItem1 == null) {
			jMenuItem1 = new JMenuItem();
		}
		return jMenuItem1;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
		}
		return jTree;
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setDividerLocation(99);
			jSplitPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			jSplitPane.setTopComponent(getJScrollPane());
			jSplitPane.setBottomComponent(getJScrollPane1());
			jSplitPane.setDividerSize(6);
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJScrollPane2());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJTable());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
		}
		return jTable;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getJPanel1());
			jScrollPane2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if ((e.getPropertyName().equals("enabled"))) {
						System.out.println("propertyChange(enabled)"); // TODO Auto-generated property Event stub "enabled" 
					}
				}
			});
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.weightx = 1.0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridy = 1;
			jLabel3 = new JLabel();
			jLabel3.setText("Cust Name");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = -1;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints.insets = new Insets(0, 6, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText("JLabel");
			jLabel1 = new JLabel();
			jLabel1.setText("Customer #");
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(jLabel1, gridBagConstraints1);
			jPanel1.add(jLabel2, gridBagConstraints);
			jPanel1.add(jLabel3, gridBagConstraints2);
			jPanel1.add(getJTextField(), gridBagConstraints11);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setColumns(22);
			jTextField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					System.out.println("focusLost()"); // TODO Auto-generated Event stub focusLost()
				}
			});
		}
		return jTextField;
	}

	/**
	 * This method initializes tableColumn	
	 * 	
	 * @return javax.swing.table.TableColumn	
	 */
	private TableColumn getTableColumn() {
		if (tableColumn == null) {
			tableColumn = new TableColumn();
			tableColumn.setPreferredWidth(5);
		}
		return tableColumn;
	}

}  //  @jve:decl-index=0:visual-constraint="56,43"
