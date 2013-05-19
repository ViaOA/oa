package com.dispatcher.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.dispatcher.editor.oa.*;
import com.viaoa.util.*;
import com.viaoa.hub.Hub;
import com.viaoa.jfc.*;

public class EditorController {
	private JFrame frame;
	private JTextPane editor;
	private static EditorKit editorKit;
	private EditorDocument editorDocument;
    private CaretListener caretListener;
    private DocumentListener documentListener;
    private ComponentView viewComponent;
    private JColorChooser fontColorChooser;
    private JDialog dlgFontColorChooser;
    private HtmlSourceDialog dlgHtmlSource;
    private FindDialog dlgFind;
    private SpellChecker spellChecker;
    private boolean bUpdatingAttributes;
    
    boolean bTestUI;
    
	public EditorController(JFrame frm, JTextPane editor, ComponentView viewComponent) {
		this.frame = frm;
		this.editor = editor;

		editorKit = new EditorKit();
		editor.setEditorKit(editorKit);
		this.viewComponent = viewComponent;

        editorDocument = (EditorDocument) editorKit.createDocument();
        editorDocument.putProperty("imageCache", new ImageCache());  // see clearImageCache()
        editor.setDocument(editorDocument);

        setupView();
        setupControls();
        setupActions();
	}

    /** Document property "imageCache" is defined in ImageView to cache images.
	    When image is not found, then it calls Toolkit.getDefaultToolkit().getImage() which caches images.
	    If the file image has changed, then the new image is never retreived.
	*/
	public void clearImageCache() {
	    Object obj = editorDocument.getProperty("imageCache");
	    if (obj instanceof Hashtable) ((Hashtable)obj).clear();
	}
	
	public void clearImageCache(URL key) {
	    Object obj = editorDocument.getProperty("imageCache");
	    if (obj instanceof Hashtable) ((Hashtable)obj).remove(key);
	}
	
	
    public static Action getAction(String name) {
        return editorKit.getAction(name);
    }
	

    
    
    protected void setupView() {
        if (editorKit == null) editorKit = new EditorKit();
		editor.setEditorKit(editorKit);


        editorDocument = (EditorDocument) editorKit.createDocument();
        editorDocument.putProperty("imageCache", new ImageCache());  // see clearImageCache()
        editor.setDocument(editorDocument);

        if (editor != null) {
            editor.removeCaretListener(caretListener);
            editor.getDocument().removeDocumentListener(documentListener);
        }

		// track caret changes to editor and update toolbar/menu to match selected text/cursor position
        if (editor != null) {
            caretListener = new CaretListener() {
			    public void caretUpdate(CaretEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {  // editor will first need to update inputAttributes
                        public void run() {
                        	updateAttributeCommands();
    				    }
    			    });
			    }
		    };
            editor.addCaretListener(caretListener);

            
            documentListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                }
                public void removeUpdate(DocumentEvent e) {
                }
                public void changedUpdate(DocumentEvent e) {
                	updateAttributeCommands();
                }
            	
            };
            editor.getDocument().addDocumentListener(documentListener);
            updateAttributeCommands();
        }
    }
	
	
	protected void setupActions() {
        // see EditorKit for InsertBreakAction class
        // have [shift][Enter] create a <p>.  [Enter] is already mapped in JTextPane, and is changed to use <br>
        editor.getActionMap().put("insert-line-break", editorKit.getAction(EditorKit.insertBreakAction));

        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK, false), "insert-line-break");
        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK, false), "insert-line-break");

        // These cant be used, since the EditorModule needs to be called when they are performed
        // getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK, false), "font-bold");
        // getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK, false), "font-italic");
        // getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK, false), "font-underline");


        // create acellerator for Find ^f
        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onFind();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // create acellerator for Replace ^r
        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onReplace();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // create accellerator for Spellcheck F7
        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onSpellCheck();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);


        // create acellerators for ^b ^i ^u
        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getBoldAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getItalicAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

        editor.registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getUnderlineAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            }
            , "", KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
	}
	
	
    /*  Used by ImageView
	    When image is not found, then it calls Toolkit.getDefaultToolkit().getImage() which caches images.
	    If the file image has changed, then the new image is never retreived.
	*/
	class ImageCache extends Hashtable {
	    public Object get(Object key) {
	        Object result = super.get(key);
	        if (result == null) {
	            result = Toolkit.getDefaultToolkit().createImage((URL)key); // this will not use the Toolkit cache
	            put(key, result);
	        }
	        return result;
	    }
	}

	
    void setupControls() {
    	viewComponent.getBoldToggleButton().addActionListener(getAction("font-bold"));
    	viewComponent.getItalicToggleButton().addActionListener(getAction("font-italic"));
    	viewComponent.getUnderlineToggleButton().addActionListener(getAction("font-underline"));
    	viewComponent.getImageButton().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onInsertImage();
    		}
    	});
    	viewComponent.getFindButton().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onFind();
    		}
    	});
    	viewComponent.getSpellButton().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onSpellCheck();
    		}
    	});
    	viewComponent.getSourceButton().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onEditSourceCode();
    		}
    	});

    
    	viewComponent.getFontColorButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	onFontColorChooser();
            }
        });
        
    	
    	
    	
        viewComponent.getCenterToggleButton().addActionListener(getAction("center-justify"));
        viewComponent.getLeftToggleButton().addActionListener(getAction("left-justify"));
        viewComponent.getRightToggleButton().addActionListener(getAction("right-justify"));
        viewComponent.getJustifiedToggleButton().addActionListener(getAction("justified-justify"));
        viewComponent.getImageMenuItem().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	onInsertImage();
            }
        });

        viewComponent.getTableMenuItem().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	onInsertTable();
            }
        });
    	
        
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                if (viewComponent.getFontComboBox().getSelectedIndex() >= 0) {
				    MutableAttributeSet attr = new SimpleAttributeSet();
				    String s = viewComponent.getFontComboBox().getSelectedItem().toString();
				    StyleConstants.setFontFamily(attr, s);
				    setAttributeSet(attr);
            		if (!bUpdatingAttributes) editor.grabFocus();
                }
			}
		};
	    viewComponent.getFontComboBox().addActionListener(al);
        
	    
        al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                if (viewComponent.getFontSizeComboBox().getSelectedIndex() >= 0) {
				    int fontSize = 0;
				    try {
					    fontSize = Integer.parseInt(viewComponent.getFontSizeComboBox().getSelectedItem().toString());
				    }
				    catch (NumberFormatException ex) { return; }

				    if (fontSize == 0) return;
				    MutableAttributeSet attr = new SimpleAttributeSet();
				    StyleConstants.setFontSize(attr, fontSize);
				    setAttributeSet(attr);
            		if (!bUpdatingAttributes) editor.grabFocus();
                }
			}
		};
		viewComponent.getFontSizeComboBox().addActionListener(al);
	    
	    
    	viewComponent.getFindMenuItem().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onFind();
    		}
    	});
    	viewComponent.getReplaceMenuItem().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onReplace();
    		}
    	});
	    
    	viewComponent.getSourceMenuItem().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onEditSourceCode();
    		}
    	});

    	viewComponent.getFontMenuItem().addActionListener(new ActionListener() {
    		public @Override void actionPerformed(ActionEvent e) {
    			onFont();
    		}
    	});

        al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MutableAttributeSet attr = new SimpleAttributeSet();
				StyleConstants.setBackground(attr, viewComponent.getBackgroundColorMenu().getColor());
				setAttributeSet(attr);
			}
		};
		viewComponent.getBackgroundColorMenu().addActionListener(al);

		MenuListener ml = new MenuListener() {
			public void menuSelected(MenuEvent e) {
				if (editor != null) {
				    int p = editor.getCaretPosition();
				    AttributeSet attr = editorDocument.getCharacterElement(p).getAttributes();
				    Color c = StyleConstants.getBackground(attr);
				    viewComponent.getBackgroundColorMenu().setColor(c);
                }
			}
			public void menuDeselected(MenuEvent e) {}
			public void menuCanceled(MenuEvent e) {}
		};
		viewComponent.getBackgroundColorMenu().addMenuListener(ml);
    

    
        al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MutableAttributeSet attr = new SimpleAttributeSet();
				StyleConstants.setForeground(attr, viewComponent.getForegroundColorMenu().getColor());
				setAttributeSet(attr);
			}
		};
		viewComponent.getForegroundColorMenu().addActionListener(al);

		ml = new MenuListener() {
			public void menuSelected(MenuEvent e) {
				if (editor != null) {
				    int p = editor.getCaretPosition();
				    AttributeSet attr = editorDocument.getCharacterElement(p).getAttributes();
				    Color c = StyleConstants.getForeground(attr);
				    viewComponent.getForegroundColorMenu().setColor(c);
                }
			}
			public void menuDeselected(MenuEvent e) {}
			public void menuCanceled(MenuEvent e) {}
		};
		viewComponent.getForegroundColorMenu().addMenuListener(ml);
    }

    public void onFont() {
		FontDialog dlg = new FontDialog(frame, viewComponent.getFontNames(), viewComponent.getFontSizes());
		AttributeSet a = editorDocument.getCharacterElement(editor.getCaretPosition()).getAttributes();
		dlg.setAttributes(a);
		dlg.setVisible(true);
		if (dlg.succeeded()) {
			setAttributeSet(dlg.getAttributes());
			updateAttributeCommands();
		}
    }
    
    public void onInsertTable() {
		TableDialog dlg = new TableDialog(frame);
		dlg.setVisible(true);
		if (dlg.succeeded()) {
			String tableHtml = dlg.getHTML();
			Element ep = editorDocument.getParagraphElement(editor.getSelectionStart());

			try {
				editorDocument.insertAfterEnd(ep, tableHtml);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			documentChanged();
		}
    }
    
    
    protected JDialog getFontColorChooser() {
    	if (dlgFontColorChooser == null) {
        	fontColorChooser = new JColorChooser();
            AbstractColorChooserPanel[] pans = fontColorChooser.getChooserPanels();
            AbstractColorChooserPanel[] pans2 = new AbstractColorChooserPanel[pans.length+1];
            pans2[0] = new ColorChooser();
            for (int i=0; i < pans.length; i++) {
                pans2[i+1] = pans[i];
            }
            fontColorChooser.setChooserPanels(pans2);
            
    		
    		dlgFontColorChooser = JColorChooser.createDialog(viewComponent.getFontColorButton(), "Font color", true, fontColorChooser,
	                new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	    			        MutableAttributeSet attr = new SimpleAttributeSet();
	    			        StyleConstants.setForeground(attr, fontColorChooser.getColor());
	    			        setAttributeSet(attr);
	                        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
	                    }
	                },
	                new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                        // CANCEL selected
	                    }
	                }
	            );
    	}
    	return dlgFontColorChooser;
    }
    
    
    public void onFontColorChooser() {
		int p = editor.getCaretPosition();
		AttributeSet attr = editorDocument.getCharacterElement(p).getAttributes();
		Color c = StyleConstants.getForeground(attr);
		getFontColorChooser();
        fontColorChooser.setColor(c);
        getFontColorChooser().setVisible(true);
    }
    
    
    public void onEditSourceCode() {
		try {
/*qqq
			StringWriter sw = new StringWriter();
			int x = editor.editorDocument.getLength();
			editor.editorKit.write(sw, editor.editorDocument, 0, x);
			sw.close();
****/
			String s = editor.getText();


            if (dlgHtmlSource == null) dlgHtmlSource = new HtmlSourceDialog(frame);

//    		dlgHtmlSource.setSource(sw.toString());
dlgHtmlSource.setSource(s);
			dlgHtmlSource.setVisible(true);
			if (!dlgHtmlSource.succeeded()) return;


editor.setText(dlgHtmlSource.getSource());
/*
			StringReader sr = new StringReader(dlgHtmlSource.getSource());
			editor.editorDocument = (EditorDocument) editor.editorKit.createDocument();
			// m_context = m_doc.getStyleSheet();
			editor.editorKit.read(sr, editor.editorDocument, 0);
			sr.close();
			editor.setDocument(editor.editorDocument);
*/
			documentChanged();
            editor.requestFocus();  // so that lostFocus will be called, and text will be saved
		}
		catch (Exception ex) {
			showError(ex, "Error: "+ex);
		}
    }
    
    
    public void onSpellCheck() {
        if (spellChecker == null) {
            JOptionPane.showMessageDialog(frame,  "SpellChecker has not been set - see programming");
        }
        else {
            if (editor != null) spellChecker.spellCheck(editor);
        }
        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
	}

    public void onFind() {
    	if (dlgFind==null) dlgFind = new FindDialog(frame, editor);
		dlgFind.setSelectedIndex(0);
		dlgFind.setVisible(true);
    }
	
    protected void onInsertImage() {
        File file = inputFileName("Please enter image URL:", null);
        if (file == null) return;
	    //String url = inputURL("Please enter image URL:", null);
	    //if (url == null) return;

        String fname = file.getPath();
        String newName = fname;
        //was: String newName = "images/" + Application.getNextImageFileName();
        int x = fname.lastIndexOf('.');
        if (x >= 0) newName += fname.substring(x);
        newName = OAString.convertFileName(newName);

	    try {
            // 2005/03/10 might need to resize picture
            ImageIcon ic = new ImageIcon(fname);
            int w = ic.getIconWidth();
            int h = ic.getIconHeight();

            double scW = 600.0/(double)w;
            double scH = 600.0/(double)h;
            double sc = Math.min(scW, scH);

            if (sc < 1.0) {
// System.out.println("Resizing picture from "+fname+"(w:"+w+",h:"+h+") to "+newName+"(w:"+(sc*w)+",h:"+(sc*h)+") scale="+sc);
                ImageResizer ir = new ImageResizer();
                ir.doResize(fname, newName, sc);
                w = (int) (((double)w) * sc);
                h = (int) (((double)h) * sc);
            }
            else {
                OAFile.copy(fname,newName);
            }


            //qqqqq Application.saveImage(newName);
		    MutableAttributeSet attr = new SimpleAttributeSet();
		    attr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
		    attr.addAttribute(HTML.Attribute.SRC, newName);
		    attr.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(h));
		    attr.addAttribute(HTML.Attribute.WIDTH, Integer.toString(w));
		    int p = editor.getCaretPosition();
		    editorDocument.insertString(p, " ", attr);
        }
        catch (Exception ex) {
            System.out.println("Exception: "+ex);
            ex.printStackTrace();
JOptionPane.showMessageDialog(frame,"Error reading image \n"+fname, "VetPlan",JOptionPane.WARNING_MESSAGE);
			return;
        }
        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
    }
	
	
	

    
    // Editor support ==================================================

	protected void setAttributeSet(AttributeSet attr) {
		setAttributeSet(attr, false);
	}

	protected void setAttributeSet(AttributeSet attr, boolean bParagraph) {
		if (bUpdatingAttributes) return;

		int xStart = editor.getSelectionStart();
		int xFinish = editor.getSelectionEnd();

		if (bParagraph) {
		    editorDocument.setParagraphAttributes(xStart,xFinish - xStart, attr, false);

            /*qqq test: only work on current element
		    Element element = editor.editorDocument.getCharacterElement(xStart);
            int b = element.getStartOffset();
            int e = element.getEndOffset();
		    editor.editorDocument.setCharacterAttributes(b, e-b, attr, false);
		    */
		}
		else if (xStart != xFinish) {
		    editorDocument.setCharacterAttributes(xStart, xFinish - xStart, attr, false);
		}
		else {
			MutableAttributeSet inputAttributes = editorKit.getInputAttributes();
			inputAttributes.addAttributes(attr);
		}
	}


    protected AttributeSet getCurrentDocAttributeSet() {
		int xStart = editor.getSelectionStart();
		int xFinish = editor.getSelectionEnd();

		AttributeSet as;
		if (xStart != xFinish) {
            // int pos = editor.getCaret().getDot();
    		as = editorDocument.getCharacterElement(xStart).getAttributes();
        }
		else {
		    as = editorKit.getInputAttributes();
		}

		return as;
    }

    /**
        Gets Attributes from the current View.
        The View's attributes are needed since they have the CSS styles in the attributes
    */
    protected AttributeSet getCurrentViewAttributeSet() {
        int pos = editor.getSelectionStart();
        if (pos < 0) return null;
        View view = editor.getUI().getRootView(editor);
        if (view == null) return null;

	    Element ele = editorDocument.getParagraphElement(pos);
	    boolean bParagraphStart = (ele.getStartOffset() == pos);

        for (int i=0; i < view.getViewCount(); i++) {
            View child = view.getView(i);
            int p1 = child.getStartOffset();
            int p2 = child.getEndOffset();
            if (pos >= p1 && pos <= p2) {
                if (pos != p2 || !bParagraphStart) {
                    view = child;
                    i = -1;
                }
            }
        }
        AttributeSet as = view.getAttributes();
        return as;
    }

	protected void updateAttributeCommands() {
		bUpdatingAttributes = true;
        if (editor == null || !editor.isEnabled()) {
            viewComponent.getBoldToggleButton().setSelected(false);
            viewComponent.getItalicToggleButton().setSelected(false);
            viewComponent.getUnderlineToggleButton().setSelected(false);
            viewComponent.getFontComboBox().setSelectedIndex(-1);
            viewComponent.getFontSizeComboBox().setSelectedIndex(-1);
        }
        else {
            AttributeSet asD = getCurrentDocAttributeSet();
            Font fD = ((EditorDocument)editor.getDocument()).getRealFont(asD);//qqqqqqqqtttttttt

            viewComponent.getBoldToggleButton().setSelected(fD.isBold());
            viewComponent.getItalicToggleButton().setSelected(fD.isItalic());
            viewComponent.getUnderlineToggleButton().setSelected(StyleConstants.isUnderline(asD));

            AttributeSet asV = getCurrentViewAttributeSet();
            Font fV = ((EditorDocument)editor.getDocument()).getRealFont(asV);//qqqqqqqqtttttttt

            String s = fV.getFamily();
            viewComponent.getFontComboBox().setSelectedItem(s);

            int x = fV.getSize();
            viewComponent.getFontSizeComboBox().setSelectedItem(""+x);

		    AttributeSet as = this.editor.getParagraphAttributes();
            x = StyleConstants.getAlignment(as);
            viewComponent.getLeftToggleButton().setSelected( (x == StyleConstants.ALIGN_LEFT) );
            viewComponent.getCenterToggleButton().setSelected( (x == StyleConstants.ALIGN_CENTER) );
            viewComponent.getRightToggleButton().setSelected( (x == StyleConstants.ALIGN_RIGHT) );
            viewComponent.getJustifiedToggleButton().setSelected( (x == StyleConstants.ALIGN_JUSTIFIED) );

            viewComponent.getForegroundColorMenu().setColor(editor.getForeground());
            viewComponent.getBackgroundColorMenu().setColor(editor.getBackground());
        }
        
        bUpdatingAttributes = false;
	}



	public void showError(Exception ex, String message) {
		ex.printStackTrace();
		JOptionPane.showMessageDialog(frame, message, "VetPlan", JOptionPane.WARNING_MESSAGE);
	}


	JFileChooser chooser;
	protected String inputURL(String prompt, String initialValue) {
		if (chooser == null) chooser = new JFileChooser();
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return null;

		File f = chooser.getSelectedFile();
		try {
			return f.toURL().toString();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	protected File inputFileName(String prompt, String initialValue) {
		if (chooser == null) chooser = new JFileChooser();
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return null;
		return chooser.getSelectedFile();
	}


	public void documentChanged() {
		editor.revalidate();
		editor.repaint();
		updateAttributeCommands();
	}

    
    

    public void setSpellChecker(SpellChecker sc) {
        this.spellChecker = sc;
    }


    public void onReplace() {
		if (dlgFind==null) dlgFind = new FindDialog(frame, editor);
		dlgFind.setSelectedIndex(1);
		dlgFind.setVisible(true);
        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
	}



	public void setSelection(int xStart, int xFinish, boolean moveUp) {
        if (editor != null) {
		    if (moveUp) {
			    editor.setCaretPosition(xFinish);
			    editor.moveCaretPosition(xStart);
		    }
		    else editor.select(xStart, xFinish);
        }
	}

	
	
    public static void main(String[] args) {
    	JFrame frm = new JFrame();
    	frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frm.setLayout(new BorderLayout());
    	
    	ComponentView view = new ComponentView();
    	frm.setJMenuBar(view.getMenuBar());
    	frm.add(view.getToolBar(), BorderLayout.NORTH);
    	
    	Editor ed = new Editor();
    	
    	EditorController cont = new EditorController(frm, ed, view);

    	ed.setText("<html><body>this is a<p>Test <b>Vince</b><p>Via</body></html>");
    	
    	SampleSpellChecker chk = new SampleSpellChecker(frm, ed);
    	cont.setSpellChecker(chk);
    	
    	
    	
    	Document doc = ed.getDocument();
    	doc.addDocumentListener(new DocumentListener() {
    		public @Override void changedUpdate(DocumentEvent e) {
    			System.out.println("changedUpdate");
    		}
    		public @Override void insertUpdate(DocumentEvent e) {
    			System.out.println("insertUpdate");
    		}    		
    		public @Override void removeUpdate(DocumentEvent e) {
    			System.out.println("removeUpdate");
    		}    		
    	});
    	

    	frm.add(new JScrollPane(ed));
    	
    	JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	sp.setDividerSize(6);
    	sp.setOneTouchExpandable(true);
    	
    	sp.setLeftComponent(cont.getOutlinePanel());
    	sp.setRightComponent(new JScrollPane(ed));

    	
    	
    	frm.add(sp);
    	frm.pack();
    	Dimension d = frm.getSize();
    	d.height = 400;
    	frm.setSize(d);
    	frm.setVisible(true);
    }
    
    OATree tree;
    public JPanel getOutlinePanel() {
    	JPanel pan = new JPanel();
    	pan.setLayout(new BorderLayout());
    	tree = new OATree(12); 
    	
    	OATreeTitleNode tnode = new OATreeTitleNode("Tree Elements");
    	
    	tree.setRoot(tnode);
    	
    	OATreeNode node = new OATreeNode("name", getElementHub());
    	tnode.add(node);
    	
    	OATreeNode node1 = new OATreeNode("docElements.name");
    	node.add(node1);

    	OATreeNode node2 = new OATreeNode("docAttributes.name");
    	node1.add(node2);
    	
    	
    	node1.add(node1);
    	
    	pan.add(new JScrollPane(tree));
    	
    	JButton cmd = new JButton("OK");
    	cmd.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			onOK();
    		}
    	});
    	
    	JPanel panCmd = new JPanel(new FlowLayout());
    	panCmd.add(cmd);
    	pan.add(panCmd, BorderLayout.SOUTH);
    	
    	return pan;
    }
    
    void onOK() {
    	Element ele = editorDocument.getDefaultRootElement();
    	getElementHub().clear();
    	loadElements(ele, getElementHub());
    	tree.expandAll();
    }

    
    void loadElements(Element ele, Hub hubDocElement) {
    	DocElement de = new DocElement();
    	
    	String name = ele.getName(); 
    	
    	int p1 = ele.getStartOffset();
    	int p2 = ele.getEndOffset();
    	
    	if (ele.isLeaf()) {
    		try {
    			name += ": " + editorDocument.getText(p1, p2-p1);
    		}
    		catch (Exception e) {
    			System.out.println("Error: "+e);
    		}
    	}
    	
    	de.setName(name);
    	hubDocElement.add(de);

    	AttributeSet ats = ele.getAttributes();
    	Enumeration enumx = ats.getAttributeNames();
    	for ( ;enumx.hasMoreElements(); ) {
			Object o1 = enumx.nextElement();
			Object o2 = ats.getAttribute(o1);
			DocAttribute da = new DocAttribute();
			da.setName(o1.toString() + " = " + o2.toString());
			de.getDocAttributes().add(da);
		}
    	
    	int x = ele.getElementCount();
    	for (int i=0; i<x; i++) {
    		Element e = ele.getElement(i);
    		loadElements(e, de.getDocElements());
    	}
    }
    
    
    Hub hubElement;
    Hub getElementHub() {
    	if (hubElement == null) {
    		hubElement = new Hub(DocElement.class);
    	}
    	return hubElement;
    }
    

}























