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
package com.viaoa.jfc.editor.html.control;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;


import com.viaoa.util.*;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.jfc.OAColorSplitButton;
import com.viaoa.jfc.ComboBoxPopupMenuListener;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OAMultiButtonSplitButton;
import com.viaoa.jfc.OAScroller;
import com.viaoa.jfc.border.CustomLineBorder;
import com.viaoa.jfc.control.HTMLTextPaneController;
import com.viaoa.jfc.editor.html.*;
import com.viaoa.jfc.editor.html.OAHTMLEditorKit.Callback;
import com.viaoa.jfc.editor.html.view.*;
import com.viaoa.jfc.editor.image.control.OAImagePanelController;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.jfc.print.OAPrintUtil;
import com.viaoa.jfc.propertypath.delegate.ObjectDefDelegate;
import com.viaoa.jfc.propertypath.model.oa.ObjectDef;
import com.viaoa.jfc.text.OATextController;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.object.OAObject;

/**
 * Expands on OATextController to create an HTML word processor.
 * 
 * NOTE: this is now used internally by OAHTMLTextPane, and does not need to be create separately.
 * 
 * <ul>
 * <li>menuItems and buttons
 * <li>dialogs for colors, editing source, search/replace, spell check, etc.
 * <li>popup menu
 * <li>printing
 * <li>Inserting property paths for dynamic data, mail merges
 * </ul>  
 * @author vincevia
 *
 */
public class OAHTMLTextPaneController extends OATextController {
    private OAHTMLTextPane htmlEditor;
    private OAHTMLEditorKit editorKit;
    private OAHTMLDocument editorDocument;
    private CaretListener caretListener;
    private DocumentListener documentListener;
    
    protected JToolBar toolBar;
    protected JMenuBar menuBar;
    
    private JColorChooser colorChooser;
    private HtmlSourceDialog dlgHtmlSource;
    private boolean bUpdatingAttributes;
    private FontDialog dlgFont;
    private OATextController textControlSourceDlgText;  // used for popup SourceCode editor

    // used to "know" the selected image
    private MyImageView myImageView;
    private String currentLinkSource;

    private JDialog dlgImageEditor ;
    private OAImagePanelController contImagePanel;

    private boolean bIsControlKeyDown;  // [ctrl] key is down
    private boolean bIsMouseOverLink;   // mouse over a link
    private boolean bIsMouseOverImage;  // mouse over an image
    
    private HTMLTextPaneController hub2HtmlText;
    private OAHTMLTextPaneMergeDialog dlgMerge;
    private BlockController controlBlock;
    private InsertController controlInsert;

    public OAHTMLTextPaneController(OAHTMLTextPane editor, SpellChecker spellChecker, boolean bAddUndoSupport) {
        super(editor, spellChecker, bAddUndoSupport);
        this.htmlEditor = editor;

            // make sure that the "oaproperty://" url handler is installed.
            //   it is used to automatically load URL images from OAObject property.
            com.viaoa.jfc.editor.html.protocol.classpath.Handler.register();
        
        editorKit = (OAHTMLEditorKit) htmlEditor.getEditorKit();
        editorDocument = (OAHTMLDocument) htmlEditor.getDocument();
        setupListeners();
        getPopupMenu(); // initialize accelerator keys
        updateEnabled();
        getSpaceAction(); // initialize <space> to create "&nbsp;" when needed.
    }

    public HTMLTextPaneController getBindController() {
        return hub2HtmlText;
    }

    public OAHTMLTextPane getHTMLTextPane() {
        return htmlEditor;
    }
    
    public HTMLTextPaneController bind(Hub hub, String property) {
        if (hub2HtmlText != null) {
            hub2HtmlText.close();
        }
        hub2HtmlText = new HTMLTextPaneController(hub, (OAHTMLTextPane) editor, property) {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                super.afterChangeActiveObject(e);
                getUndoManager().discardAllEdits(); // start a fresh undo buffer
            }
            @Override
            public boolean saveText() {
                boolean b = super.saveText();
                if (b) getUndoManager().discardAllEdits(); // start a fresh undo buffer
                return b;
            }
            
            @Override
            protected String getValueToUse(String origValue, String currentValue, String newValue) {
                JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(editor), 
                        "The text was changed by another user\nwhile you were editing it.\nYou will need to decide what text to keep.", 
                        "Text change by another", JOptionPane.INFORMATION_MESSAGE);                
                
                
                OAHTMLTextPaneMergeDialog dlg = getOAHTMLTextPaneMergeDialog();
                
                dlg.getOriginalTextArea().setText(origValue);
                dlg.getCurrentTextArea().setText(currentValue);
                dlg.getLocalTextArea().setText(newValue);
                dlg.getNewValueTextArea().setText(newValue);
                
                dlg.setVisible(true);
                return dlg.getNewValueTextArea().getText();
            }
            @Override
            protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
                return OAHTMLTextPaneController.this.isEnabled(bIsCurrentlyEnabled);
            }
            @Override
            public String isValid(Object object, Object value) {
                return OAHTMLTextPaneController.this.isValid(object, value);
            }
            @Override
            protected boolean isVisible(boolean bIsCurrentlyVisible) {
                return OAHTMLTextPaneController.this.isVisible(bIsCurrentlyVisible);
            }
        };
        return hub2HtmlText;
    }
    
    /**
     * Other Hub/Property used to determine if component is enabled.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub, String prop) {
        if (hub2HtmlText == null) {
            throw new RuntimeException("must call bind before calling setEnabled");
        }
        hub2HtmlText.getEnabledChangeListener().add(hub, prop);
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    /**
     * Other Hub/Property used to determine if component is visible.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setVisible(Hub hub, String prop) {
        if (hub2HtmlText == null) {
            throw new RuntimeException("must call bind before calling setVisible");
        }
        hub2HtmlText.getVisibleChangeListener().add(hub, prop);
    }    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }
    
    protected String isValid(Object object, Object value) {
        return null;
    }

    /**
     * 
     * @param hub Hub that has all images in it.
     * @param byteArrayPropertyName name of property that is for a byte[] to store "raw"
     * @param sourceNamePropertyName property to use to store the source of the image (ex: file name) 
     * @param idPropertyName unique property value to use for the html img src tag. If null, then sourceNamePropertyName will be used.
     * @see Hub2ImageHandler
     */
    public void createImageHandler(Hub<?> hub, String byteArrayPropertyName, String sourceNamePropertyName, String idPropertyName) {
        new Hub2ImageHandler(htmlEditor, hub, byteArrayPropertyName, sourceNamePropertyName,  idPropertyName);
    }
    public void createImageHandler(Class<? extends OAObject> clazz, String byteArrayPropertyName, String sourceNamePropertyName, String idPropertyName) {
        new Class2ImageHandler(htmlEditor, clazz, byteArrayPropertyName, sourceNamePropertyName,  idPropertyName);
    }
    /**
     * Create file image handler.
     * @see FileImageHandler
     */
    public void createFileImageHandler() {
        htmlEditor.setImageHandler(new FileImageHandler());
    }
    
    public OAHTMLTextPaneMergeDialog getOAHTMLTextPaneMergeDialog() {
        if (dlgMerge == null) {
            Window win = SwingUtilities.windowForComponent(editor);
            dlgMerge = new OAHTMLTextPaneMergeDialog(win, "Text changed") {
                @Override
                protected void onClose() {
                    setVisible(false);
                }
                @Override
                protected void onOk() {
                    setVisible(false);
                }
            };
        }
        return dlgMerge;
    }

    @Override
    protected void updateEnabled() {
        if (editorKit == null) return; // not yet initialized
        boolean b = htmlEditor != null && htmlEditor.isEnabled() && htmlEditor.isEditable();
        
        if (menuBar != null) setEnabled(menuBar, b); 
        if (toolBar != null) setEnabled(toolBar, b);
        if (popupMenu != null) setEnabled(popupMenu, b);
        
        if (!b) {
            getEditSourceMenuItem().setEnabled(true);
            getPopupEditSourceMenuItem().setEnabled(true);
            getEditSourceButton().setEnabled(true);
            if (menuEdit != null) {
                menuEdit.setEnabled(true);
            }
            if (pmenuEdit != null) {
                pmenuEdit.setEnabled(true);
            }
        }
        super.updateEnabled();
    }
    
    private void setEnabled(Container parent, boolean b) {
        Component[] comps = parent.getComponents();
        for (Component comp : comps) {
            if (comp == null) continue;
            if (comp instanceof Container) {
                setEnabled((Container)comp, b);
            }
            comp.setEnabled(b);
        }
    }

    private URL getImageURL(String imageName) {
        URL url = OAHTMLTextPane.class.getResource("view/image/"+imageName);
        return url;
    }
    
    public Action getAction(String name) {
        return editorKit.getAction(name);
    }
    
    protected void setupListeners() {
        // track caret changes to editor and update toolbar/menu to match selected text/cursor position
        caretListener = new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int x1 = e.getDot();
                int x2 = e.getMark();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateAttributeCommands();
                    }
                });
            }
        };
        htmlEditor.addCaretListener(caretListener);

        editorKit.setCallback(new Callback() {
            @Override
            public String getInsertImageDivHtml(ActionEvent e) {
                if (e != null) e.setSource(htmlEditor);
                return OAHTMLTextPaneController.this.getInsertImageDivHtmlForEditorKitCallback();
            }
        });
        
        
        htmlEditor.getDocument().addDocumentListener(getDocumentListener());
        updateAttributeCommands();
        
        // need to listen to document change, which happens during a call to setText
        htmlEditor.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null) return;
                String s = evt.getPropertyName();
                if (s != null && s.equalsIgnoreCase("document")) {
                    OAHTMLDocument docOld = (OAHTMLDocument) evt.getOldValue();
                    OAHTMLDocument docNew = (OAHTMLDocument) evt.getNewValue();
                    editorDocument = docNew;
                    if (documentListener != null) {
                        docOld.removeDocumentListener(getDocumentListener());
                        docNew.addDocumentListener(getDocumentListener());
                    }
                }
            }
        });
        
        
        // this will only work if Editor.isEditable()==false
        htmlEditor.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    // System.out.println("==> "+url);
                }
            }
        });
        
        // double click support for images and links
        
        
        htmlEditor.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
                bIsControlKeyDown = false;
                updateCursorForLinkOrImage();
            }
            @Override
            public void keyPressed(KeyEvent e) {
                bIsControlKeyDown = ((e.getModifiers() & InputEvent.CTRL_MASK) > 0);
                updateCursorForLinkOrImage();
            }
        });

        htmlEditor.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                bIsMouseOverLink = false; 
                bIsMouseOverLink = false;
                int pos = htmlEditor.viewToModel(e.getPoint());
                if (pos >= 0) {
                    HTMLDocument doc = (HTMLDocument) htmlEditor.getDocument();
                    Element ele = doc.getCharacterElement(pos);
                    if (ele != null) {
                        AttributeSet attrSet = ele.getAttributes();
                        if (attrSet != null) {
                            AttributeSet anchor = (AttributeSet) attrSet.getAttribute(HTML.Tag.A);
                            bIsMouseOverLink = (anchor != null);

                            Object objx =  attrSet.getAttribute(StyleConstants.NameAttribute);
                            bIsMouseOverImage = (objx == HTML.Tag.IMG);
                        }
                    }
                }
                updateCursorForLinkOrImage();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
         
        
        htmlEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    if (!bIsControlKeyDown || (!bIsMouseOverLink && !bIsMouseOverImage)) return;
                }
                
                Point pt = new Point(e.getX(), e.getY());
                int pos = htmlEditor.viewToModel(pt);
                if (pos < 0) return;

                HTMLDocument doc = (HTMLDocument) htmlEditor.getDocument();
                
                Element ele = doc.getCharacterElement(pos);
                if (ele == null) return;
                
                AttributeSet attrSet = ele.getAttributes();
                if (attrSet == null) return;
                
                AttributeSet anchor = (AttributeSet) attrSet.getAttribute(HTML.Tag.A);
                if (anchor != null) {
                    String href = (String) anchor.getAttribute(HTML.Attribute.HREF);
                    // System.out.println("====> "+href);
                    onFollowLink(href);
                }
                else {
                    Object objx =  attrSet.getAttribute(StyleConstants.NameAttribute);
                    if (objx == HTML.Tag.IMG) {
                        // double click on image
                        String src = (String) attrSet.getAttribute(HTML.Attribute.SRC);
                        // System.out.println("====> "+src);
                        
                        View view = OAHTMLTextPaneController.this.getView(pos+1);
                        if (view instanceof MyImageView) {
                            myImageView = (MyImageView) view;
                            onEditImage();
                        }
                    }
                }
            }
        });
    }


    /**
        If [ctrl]+mouseOver a link, then show hand cusror.
    */    
    protected void updateCursorForLinkOrImage() {
        Cursor c;
        if (bIsControlKeyDown && (bIsMouseOverImage || bIsMouseOverLink)) {
            c = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }
        else {
            c = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
        htmlEditor.setCursor(c);
        
        String s;
        if (bIsMouseOverImage) {
            if (bIsControlKeyDown) s = "click the mouse to edit this image";
            else s = "<html>to edit this image: hold down the [Ctrl] key<br> and click the mouse, or double click the mouse";
            if (htmlEditor == null || !htmlEditor.isEnabled() || !htmlEditor.isEditable()) s = null;
        }
        else if (bIsMouseOverLink) {
            if (bIsControlKeyDown) s = "click the mouse to select this link";
            else s = "<html>to select this link: hold down the [Ctrl] key<br> and click the mouse, or double click the mouse";
        }
        else {
            s = null;
        }
        
        htmlEditor.setToolTipText(s);
    }
    

        
    
    @Override
    protected void onRightMouse(MouseEvent e) {
        _onRightMouse(e);
        super.onRightMouse(e);
    }
    
    private void _onRightMouse(MouseEvent e) {
        currentLinkSource = null;
        Point pt = new Point(e.getX(), e.getY());
        int pos = htmlEditor.viewToModel(pt);
        if (pos < 0) return;

        HTMLDocument doc = (HTMLDocument) htmlEditor.getDocument();
        
        Element ele = doc.getCharacterElement(pos);
        if (ele != null) {
            AttributeSet attrSet = ele.getAttributes();
            if (attrSet != null) {
                AttributeSet anchor = (AttributeSet) attrSet.getAttribute(HTML.Tag.A);
                if (anchor != null) {
                    currentLinkSource = (String) anchor.getAttribute(HTML.Attribute.HREF);
                }
            }
        }
        getPopupFollowLinkMenuItem().setEnabled(currentLinkSource != null);
        String s = currentLinkSource == null ? "" : "go to link " + currentLinkSource;
        getPopupFollowLinkMenuItem().setToolTipText(s);
    }    
    
    protected void onFollowLink(String href) {
        if (href == null) return;
        try {
            URI uri = new URI(href);
            if (href.toLowerCase().indexOf("mailto:") >= 0) {
                Desktop.getDesktop().mail(uri);
            }
            else {
                Desktop.getDesktop().browse(uri);
                // or:  .open(uri);
            }
        }
        catch (Exception e) {
            System.out.println("OAHTMLTextPaneController: bad href link "+href+", exception:"+e);
        }
    }
    
    
    private DocumentListener getDocumentListener() {
        if (documentListener == null) {
            documentListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                }
                public void removeUpdate(DocumentEvent e) {
                }
                public void changedUpdate(DocumentEvent e) {
                    updateAttributeCommands();
                }
            };
        }
        return documentListener;
    }
    
    
/* ***********************************   
>>   COMMAND: Space  <space>
*************************************/
    // this is used in combination with 
    private Action actionSpace;
    protected Action getSpaceAction() {
        if (actionSpace == null) {
            actionSpace = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getSpaceAction().actionPerformed(e);
                }
            };
            String cmd = "Space";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), cmd);
            htmlEditor.getActionMap().put(cmd, actionSpace);
        }
        return actionSpace;
    }
    

/* ***********************************   
   COMMAND: ParagraphBreak <br> ^Enter   (creates a new Paragraph)
*************************************/    
    // see OAHTMLEditorKit for InsertBreakAction class
    // have [ctrl][Enter] create a <p>.  [Enter] is already mapped in JTextPane, and is changed to use <br>
    protected KeyStroke getParagraphBreakKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK, false);
    }
    private Action actionParagraphBreak;
    protected Action getParagraphBreakAction() {
        if (actionParagraphBreak == null) {
            actionParagraphBreak = editorKit.getAction(OAHTMLEditorKit.insertParagraphAction);
            String cmd = "myInsert-paragraph-break";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getParagraphBreakKeyStroke(), cmd);
            htmlEditor.getActionMap().put(cmd, actionParagraphBreak);
        }
        return actionParagraphBreak;
    }
    protected JMenuItem createParagraphBreakMenuItem() {
        JMenuItem mi = new JMenuItem("Paragraph break");
        mi.setIcon(new ImageIcon(getImageURL("lineBreak.gif")));
        mi.setAccelerator(getParagraphBreakKeyStroke());
        mi.addActionListener(getParagraphBreakAction());
        return mi;
    }
    private JMenuItem miParagraphBreak, pmiParagraphBreak;
    public JMenuItem getParagraphBreakMenuItem() {
        if (miParagraphBreak == null) {
            miParagraphBreak = createParagraphBreakMenuItem();
        }
        return miParagraphBreak;
    }
    public JMenuItem getPopupParagraphBreakMenuItem() {
        if (pmiParagraphBreak == null) {
            pmiParagraphBreak = createParagraphBreakMenuItem();
        }
        return pmiParagraphBreak;
    }
    private JButton cmdParagraphBreak;
    public JButton getParagraphBreakButton() {
        if (cmdParagraphBreak == null) {
            cmdParagraphBreak = new JButton();
            cmdParagraphBreak.setToolTipText("Insert a paragraph break <p>");
            cmdParagraphBreak.setRequestFocusEnabled(false);
            cmdParagraphBreak.setFocusPainted(false);
            OAButton.setup(cmdParagraphBreak);
            cmdParagraphBreak.setIcon(new ImageIcon(getImageURL("lineBreak.gif")));
            cmdParagraphBreak.addActionListener(getParagraphBreakAction());
        }
        return cmdParagraphBreak;
    }    

/* ***********************************   
>>   COMMAND: Bold ^B
*************************************/    
    protected KeyStroke getBoldKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK, false);
    }
    private Action actionBold;
    protected Action getBoldAction() {
        if (actionBold == null) {
            actionBold = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getBoldAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            };
            String cmd = "Bold";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getBoldKeyStroke(), cmd);
            htmlEditor.getActionMap().put(cmd, actionBold);
        }
        return actionBold;
    }
    protected JMenuItem createBoldMenuItem() {
        JMenuItem miBold = new JMenuItem("Bold");
        miBold.setIcon(new ImageIcon(getImageURL("bold.gif")));
        miBold.addActionListener(getBoldAction());
        //miBold.setAccelerator(getBoldKeyStroke());
        return miBold;
    }
    private JMenuItem miBold, pmiBold;
    protected JMenuItem getBoldMenuItem() {
        if (miBold == null) {
            miBold = createBoldMenuItem();
        }
        return miBold;
    }
    protected JMenuItem getPopupBoldMenuItem() {
        if (pmiBold == null) {
            pmiBold = createBoldMenuItem();
        }
        return pmiBold;
    }
    private JToggleButton cmdBold;
    public JToggleButton getBoldToggleButton() {
        if (cmdBold == null) {
            cmdBold = new JToggleButton();
            cmdBold.setToolTipText("Bold ^B");
            cmdBold.setIcon(new ImageIcon(getImageURL("bold.gif")));
            cmdBold.setRequestFocusEnabled(false);
            cmdBold.setFocusPainted(false);
            // cmdBold.setBorderPainted(false);
            cmdBold.setMargin(new Insets(1,1,1,1));
            cmdBold.addActionListener(getBoldAction());
        }
        return cmdBold;
    }
    
/* ***********************************   
>>   COMMAND: Italic ^I
*************************************/    
    protected KeyStroke getItalicKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false);
    }
    private Action actionItalic;
    protected Action getItalicAction() {
        if (actionItalic == null) {
            actionItalic = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getItalicAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            };
            String cmd = "Italic";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getItalicKeyStroke(), cmd);
            htmlEditor.getActionMap().put(cmd, actionItalic);
        }
        return actionItalic;
    }
    protected JMenuItem createItalicMenuItem() {
        JMenuItem miItalic = new JMenuItem("Italic");
        miItalic.setIcon(new ImageIcon(getImageURL("italic.gif")));
        miItalic.addActionListener(getItalicAction());
        //miItalic.setAccelerator(getItalicKeyStroke());
        return miItalic;
    }
    private JMenuItem miItalic, pmiItalic;
    protected JMenuItem getItalicMenuItem() {
        if (miItalic == null) {
            miItalic = createItalicMenuItem();
        }
        return miItalic;
    }
    protected JMenuItem getPopupItalicMenuItem() {
        if (pmiItalic == null) {
            pmiItalic = createItalicMenuItem();
        }
        return pmiItalic;
    }
    private JToggleButton cmdItalic;
    public JToggleButton getItalicToggleButton() {
        if (cmdItalic == null) {
            cmdItalic = new JToggleButton();
            cmdItalic.setToolTipText("Italic ^I");
            cmdItalic.setIcon(new ImageIcon(getImageURL("italic.gif")));
            cmdItalic.setRequestFocusEnabled(false);
            cmdItalic.setFocusPainted(false);
            // cmdItalic.setBorderPainted(false);
            cmdItalic.setMargin(new Insets(1,1,1,1));
            cmdItalic.addActionListener(getItalicAction());
        }
        return cmdItalic;
    }

    
/* ***********************************   
>>   COMMAND: Underline ^U
*************************************/    
    protected KeyStroke getUnderlineKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK, false);
    }
    private Action actionUnderline;
    protected Action getUnderlineAction() {
        if (actionUnderline == null) {
            actionUnderline = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    editorKit.getUnderlineAction().actionPerformed(e);
                    updateAttributeCommands();
                }
            };
            String cmd = "Underline";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getUnderlineKeyStroke(), cmd);
            htmlEditor.getActionMap().put(cmd, actionUnderline);
        }
        return actionUnderline;
    }
    protected JMenuItem createUnderlineMenuItem() {
        JMenuItem miUnderline = new JMenuItem("Underline");
        miUnderline.setIcon(new ImageIcon(getImageURL("underline.gif")));
        miUnderline.addActionListener(getUnderlineAction());
        //miUnderline.setAccelerator(getUnderlineKeyStroke());
        return miUnderline;
    }
    private JMenuItem miUnderline, pmiUnderline;
    protected JMenuItem getUnderlineMenuItem() {
        if (miUnderline == null) {
            miUnderline = createUnderlineMenuItem();
        }
        return miUnderline;
    }
    protected JMenuItem getPopupUnderlineMenuItem() {
        if (pmiUnderline == null) {
            pmiUnderline = createUnderlineMenuItem();
        }
        return pmiUnderline;
    }
    private JToggleButton cmdUnderline;
    public JToggleButton getUnderlineToggleButton() {
        if (cmdUnderline == null) {
            cmdUnderline = new JToggleButton();
            cmdUnderline.setToolTipText("Underline ^U");
            cmdUnderline.setIcon(new ImageIcon(getImageURL("underline.gif")));
            cmdUnderline.setRequestFocusEnabled(false);
            cmdUnderline.setFocusPainted(false);
            // cmdUnderline.setBorderPainted(false);
            cmdUnderline.setMargin(new Insets(1,1,1,1));
            cmdUnderline.addActionListener(getUnderlineAction());
        }
        return cmdUnderline;
    }
    
/* ***************************    
>>   COMMAND: SelectLine
*****************************/    
    protected KeyStroke getSelectLineKeyStroke() {
        return null;//KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    
    private Action actionSelectLine;
    protected Action getSelectLineAction() {
        if (actionSelectLine == null) {
            actionSelectLine = editorKit.getAction(OAHTMLEditorKit.selectLineAction);
                
            String cmd = "SelectLine";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getSelectLineKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionSelectLine);
        }
        return actionSelectLine;
    }
    protected JMenuItem createSelectLineMenuItem() {
        JMenuItem miSelectLine = new JMenuItem("Select line");
        miSelectLine.setIcon(new ImageIcon(getImageURL("selectLine.gif")));
        miSelectLine.addActionListener(getSelectLineAction());
        return miSelectLine;
    }
    private JMenuItem miSelectLine, pmiSelectLine;
    public JMenuItem getSelectLineMenuItem() {
        if (miSelectLine == null) {
            miSelectLine = createSelectLineMenuItem();
        }
        return miSelectLine;
    }
    public JMenuItem getPopupSelectLineMenuItem() {
        if (pmiSelectLine == null) {
            pmiSelectLine = createSelectLineMenuItem();
        }
        return pmiSelectLine;
    }
    private JButton cmdSelectLine;
    public JButton createSelectLineButton() {
        if (cmdSelectLine == null) {
            cmdSelectLine = new JButton();
            cmdSelectLine.setToolTipText("Select line text");
            cmdSelectLine.setRequestFocusEnabled(false);
            cmdSelectLine.setFocusPainted(false);
            OAButton.setupButton(cmdSelectLine);
            cmdSelectLine.setIcon(new ImageIcon(getImageURL("selectLine.gif")));
            cmdSelectLine.addActionListener(getSelectLineAction());
        }
        return cmdSelectLine;
    }    

/* ***************************    
>>   COMMAND: SelectParagraph
*****************************/    
    protected KeyStroke getSelectParagraphKeyStroke() {
        return null;//KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    
    private Action actionSelectParagraph;
    protected Action getSelectParagraphAction() {
        if (actionSelectParagraph == null) {
            actionSelectParagraph = editorKit.getAction(OAHTMLEditorKit.selectParagraphAction);
                
            String cmd = "SelectParagraph";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getSelectParagraphKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionSelectParagraph);
        }
        return actionSelectParagraph;
    }
    protected JMenuItem createSelectParagraphMenuItem() {
        JMenuItem miSelectParagraph = new JMenuItem("Select Paragraph");
        miSelectParagraph.setIcon(new ImageIcon(getImageURL("selectParagraph.gif")));
        miSelectParagraph.addActionListener(getSelectParagraphAction());
        return miSelectParagraph;
    }
    private JMenuItem miSelectParagraph, pmiSelectParagraph;
    public JMenuItem getSelectParagraphMenuItem() {
        if (miSelectParagraph == null) {
            miSelectParagraph = createSelectParagraphMenuItem();
        }
        return miSelectParagraph;
    }
    public JMenuItem getPopupSelectParagraphMenuItem() {
        if (pmiSelectParagraph == null) {
            pmiSelectParagraph = createSelectParagraphMenuItem();
        }
        return pmiSelectParagraph;
    }
    private JButton cmdSelectParagraph;
    public JButton createSelectParagraphButton() {
        if (cmdSelectParagraph == null) {
            cmdSelectParagraph = new JButton();
            cmdSelectParagraph.setToolTipText("Select paragraph");
            cmdSelectParagraph.setRequestFocusEnabled(false);
            cmdSelectParagraph.setFocusPainted(false);
            OAButton.setupButton(cmdSelectParagraph);
            cmdSelectParagraph.setIcon(new ImageIcon(getImageURL("selectParagraph.gif")));
            cmdSelectParagraph.addActionListener(getSelectParagraphAction());
        }
        return cmdSelectParagraph;
    }    

    
/* ***************************    
>>   COMMAND: InsertImage
*****************************/    
    protected KeyStroke getInsertImageKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertImage;
    protected Action getInsertImageAction() {
        if (actionInsertImage == null) {
            actionInsertImage = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onInsertImage();
                }
            };
            String cmd = "InsertImage";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionInsertImage;
    }
    protected JMenuItem createInsertImageMenuItem() {
        JMenuItem miInsertImage = new JMenuItem("Image ...");
        miInsertImage.setToolTipText("Insert image");
        miInsertImage.setMnemonic(KeyEvent.VK_I);
        miInsertImage.setIcon(new ImageIcon(getImageURL("image.png")));
        miInsertImage.addActionListener(getInsertImageAction());
        // miInsertImage.setAccelerator(getInsertImageKeyStroke());
        return miInsertImage;
    }
    private JMenuItem miInsertImage, pmiInsertImage;
    public JMenuItem getInsertImageMenuItem() {
        if (miInsertImage == null) {
            miInsertImage = createInsertImageMenuItem();
        }
        return miInsertImage;
    }
    public JMenuItem getPopupInsertImageMenuItem() {
        if (pmiInsertImage == null) {
            pmiInsertImage = createInsertImageMenuItem();
        }
        return pmiInsertImage;
    }
    private JButton cmdInsertImage;
    public JButton getInsertImageButton() {
        if (cmdInsertImage == null) {
            cmdInsertImage = new JButton("Insert image ...");
            cmdInsertImage.setToolTipText("Insert image at the current location");
            cmdInsertImage.setRequestFocusEnabled(false);
            cmdInsertImage.setFocusPainted(false);
            OAButton.setupButton(cmdInsertImage);
            cmdInsertImage.setIcon(new ImageIcon(getImageURL("image.png")));
            cmdInsertImage.addActionListener(getInsertImageAction());
        }
        return cmdInsertImage;
    }    

    private OAMultiButtonSplitButton cmdSplitImage;
    public OAMultiButtonSplitButton getSplitImageButton() {
        if (cmdSplitImage == null) {
            cmdSplitImage = new OAMultiButtonSplitButton();
            cmdSplitImage.setShowTextInSelectedButton(false);
            cmdSplitImage.addButton(getInsertImageButton(), true);
            cmdSplitImage.addButton(getEditImageButton());
            cmdSplitImage.addButton(getInsertImageDivButton());
            cmdSplitImage.setRequestFocusEnabled(false);
            cmdSplitImage.setFocusPainted(false);
            // cmdSplitImage.setIcon(new ImageIcon(getImageURL("image.png")));
            OAButton.setup(cmdSplitImage);

        }
        return cmdSplitImage;
    }    
    
/* ***************************    
>>   COMMAND: EditImage
*****************************/    
    protected KeyStroke getEditImageKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionEditImage;
    protected Action getEditImageAction() {
        if (actionEditImage == null) {
            actionEditImage = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onEditImage();
                }
            };
            String cmd = "EditImage";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getEditImageKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionEditImage);
        }
        return actionEditImage;
    }
    protected JMenuItem createEditImageMenuItem() {
        JMenuItem miEditImage = new JMenuItem("Image ...");
        miEditImage.setToolTipText("Edit Image");
        //miEditImage.setMnemonic(KeyEvent.VK_I);
        miEditImage.setToolTipText("Edit Image");
        miEditImage.setIcon(new ImageIcon(getImageURL("image.png")));
        miEditImage.addActionListener(getEditImageAction());
        // miEditImage.setAccelerator(getEditImageKeyStroke());
        return miEditImage;
    }
    private JMenuItem miEditImage, pmiEditImage;
    public JMenuItem getEditImageMenuItem() {
        if (miEditImage == null) {
            miEditImage = createEditImageMenuItem();
        }
        return miEditImage;
    }
    public JMenuItem getPopupEditImageMenuItem() {
        if (pmiEditImage == null) {
            pmiEditImage = createEditImageMenuItem();
        }
        return pmiEditImage;
    }
    private JButton cmdEditImage;
    public JButton getEditImageButton() {
        if (cmdEditImage == null) {
            cmdEditImage = new JButton("Edit image ...");
            cmdEditImage.setToolTipText("Edit image");
            cmdEditImage.setRequestFocusEnabled(false);
            cmdEditImage.setFocusPainted(false);
            OAButton.setupButton(cmdEditImage);
            cmdEditImage.setIcon(new ImageIcon(getImageURL("image.png")));
            cmdEditImage.addActionListener(getEditImageAction());
        }
        return cmdEditImage;
    }    

        
    /* ***************************    
    >>   COMMAND: InsertHyperLink
    *****************************/    
        protected KeyStroke getInsertHyperLinkKeyStroke() {
            return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
        }
        private Action actionInsertHyperLink;
        protected Action getInsertHyperLinkAction() {
            if (actionInsertHyperLink == null) {
                actionInsertHyperLink = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        onInsertHyperLink();
                    }
                };
                String cmd = "InsertHyperLink";
                // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertHyperLinkKeyStroke(), cmd);
                // htmlEditor.getActionMap().put(cmd, actionInsertHyperLink);
            }
            return actionInsertHyperLink;
        }
        protected JMenuItem createInsertHyperLinkMenuItem() {
            JMenuItem miInsertHyperLink = new JMenuItem("Web page link ...");
            miInsertHyperLink.setToolTipText("Insert a link to a web page");
            miInsertHyperLink.setMnemonic(KeyEvent.VK_H);
            miInsertHyperLink.setIcon(new ImageIcon(getImageURL("hyperLink.gif")));
            miInsertHyperLink.addActionListener(getInsertHyperLinkAction());
            return miInsertHyperLink;
        }
        private JMenuItem miInsertHyperLink, pmiInsertHyperLink;
        public JMenuItem getInsertHyperLinkMenuItem() {
            if (miInsertHyperLink == null) {
                miInsertHyperLink = createInsertHyperLinkMenuItem();
            }
            return miInsertHyperLink;
        }
        public JMenuItem getPopupInsertHyperLinkMenuItem() {
            if (pmiInsertHyperLink == null) {
                pmiInsertHyperLink = createInsertHyperLinkMenuItem();
            }
            return pmiInsertHyperLink;
        }
        private JButton cmdInsertHyperLink;
        public JButton getInsertHyperLinkButton() {
            if (cmdInsertHyperLink == null) {
                cmdInsertHyperLink = new JButton("Insert web page link ...");
                cmdInsertHyperLink.setToolTipText("Insert a web page link");
                cmdInsertHyperLink.setRequestFocusEnabled(false);
                cmdInsertHyperLink.setFocusPainted(false);
                OAButton.setupButton(cmdInsertHyperLink);
                cmdInsertHyperLink.setIcon(new ImageIcon(getImageURL("hyperLink.gif")));
                cmdInsertHyperLink.addActionListener(getInsertHyperLinkAction());
            }
            return cmdInsertHyperLink;
        }    

        private OAMultiButtonSplitButton cmdSplitHyperLink;
        public OAMultiButtonSplitButton getSplitHyperLinkButton() {
            if (cmdSplitHyperLink == null) {
                cmdSplitHyperLink = new OAMultiButtonSplitButton();
                cmdSplitHyperLink.setShowTextInSelectedButton(false);
                cmdSplitHyperLink.addButton(getInsertHyperLinkButton(), true);
                cmdSplitHyperLink.addButton(getInsertMailtoButton());
                cmdSplitHyperLink.addButton(getInsertFieldButton());
                cmdSplitHyperLink.setRequestFocusEnabled(false);
                cmdSplitHyperLink.setFocusPainted(false);
                OAButton.setup(cmdSplitHyperLink);
            }
            return cmdSplitHyperLink;
        }    
        
/* ***************************    
>>   COMMAND: InsertMailto
*****************************/    
    protected KeyStroke getInsertMailtoKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertMailto;
    protected Action getInsertMailtoAction() {
        if (actionInsertMailto == null) {
            actionInsertMailto = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onInsertMailto();
                }
            };
            String cmd = "InsertMailto";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getEditImageKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionEditImage);
        }
        return actionInsertMailto;
    }
    protected JMenuItem createInsertMailtoMenuItem() {
        JMenuItem miInsertMailto = new JMenuItem("mailto address ...");
        miInsertMailto.setToolTipText("Insert a link to an email address");
        miInsertMailto.setIcon(new ImageIcon(getImageURL("mailto.gif")));
        miInsertMailto.addActionListener(getInsertMailtoAction());
        return miInsertMailto;
    }
    private JMenuItem miInsertMailto, pmiInsertMailto;
    public JMenuItem getInsertMailtoMenuItem() {
        if (miInsertMailto == null) {
            miInsertMailto = createInsertMailtoMenuItem();
        }
        return miInsertMailto;
    }
    public JMenuItem getPopupInsertMailtoMenuItem() {
        if (pmiInsertMailto == null) {
            pmiInsertMailto = createInsertMailtoMenuItem();
        }
        return pmiInsertMailto;
    }
    private JButton cmdInsertMailto;
    public JButton getInsertMailtoButton() {
        if (cmdInsertMailto == null) {
            cmdInsertMailto = new JButton("Insert mailto address ...");
            cmdInsertMailto.setToolTipText("Insert a link to an email address");
            cmdInsertMailto.setRequestFocusEnabled(false);
            cmdInsertMailto.setFocusPainted(false);
            OAButton.setupButton(cmdInsertMailto);
            cmdInsertMailto.setIcon(new ImageIcon(getImageURL("mailto.gif")));
            cmdInsertMailto.addActionListener(getInsertMailtoAction());
        }
        return cmdInsertMailto;
    }    
        
    /* ***************************    
    >>   COMMAND: InsertField
    *****************************/    
        protected KeyStroke getInsertFieldKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false);
        }
        private Action actionInsertField;
        protected Action getInsertFieldAction() {
            if (actionInsertField == null) {
                actionInsertField = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        onInsertField();
                    }
                };
                String cmd = "InsertField";
                
                htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertFieldKeyStroke(), cmd);
                htmlEditor.getActionMap().put(cmd, actionInsertField);
            }
            return actionInsertField;
        }
        protected JMenuItem createInsertFieldMenuItem() {
            JMenuItem miInsertField = new JMenuItem("Insert field ...") {
                @Override
                public void setEnabled(boolean b) {
                    // TODO Auto-generated method stub
                    super.setEnabled(b);
                }
                
            };
            miInsertField.setToolTipText("Insert a Field to use for dynamic data/mail merges");
            miInsertField.setIcon(new ImageIcon(getImageURL("field.gif")));
            miInsertField.addActionListener(getInsertFieldAction());
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false);
            miInsertField.setAccelerator(ks);
            return miInsertField;
        }
        private JMenuItem miInsertField, pmiInsertField;
        public JMenuItem getInsertFieldMenuItem() {
            if (miInsertField == null) {
                miInsertField = createInsertFieldMenuItem();
            }
            return miInsertField;
        }
        public JMenuItem getPopupInsertFieldMenuItem() {
            if (pmiInsertField == null) {
                pmiInsertField = createInsertFieldMenuItem();
            }
            return pmiInsertField;
        }
        private JButton cmdInsertField;
        public JButton getInsertFieldButton() {
            if (cmdInsertField == null) {
                cmdInsertField = new JButton("Insert field ...");
                cmdInsertField.setToolTipText("Insert a Field to use for dynamic data/mail merges");
                cmdInsertField.setRequestFocusEnabled(false);
                cmdInsertField.setFocusPainted(false);
                OAButton.setupButton(cmdInsertField);
                cmdInsertField.setIcon(new ImageIcon(getImageURL("field.gif")));
                cmdInsertField.addActionListener(getInsertFieldAction());
            }
            return cmdInsertField;
        }    

/* ***************************    
>>   COMMAND: EditSource
*****************************/    
    protected KeyStroke getEditSourceKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionEditSource;
    protected Action getEditSourceAction() {
        if (actionEditSource == null) {
            actionEditSource = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onEditSourceCode();
                }
            };
            String cmd = "EditSource";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getEditSourceKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionEditSource);
        }
        return actionEditSource;
    }
    protected JMenuItem createEditSourceMenuItem() {
        JMenuItem miEditSource = new JMenuItem("Source Code ...");
        miEditSource.setToolTipText("Edit HTML source code");
        miEditSource.setMnemonic(KeyEvent.VK_I);
        miEditSource.setIcon(new ImageIcon(getImageURL("source.gif")));
        miEditSource.addActionListener(getEditSourceAction());
        // miEditSource.setAccelerator(getEditSourceKeyStroke());
        return miEditSource;
    }
    private JMenuItem miEditSource, pmiEditSource;
    public JMenuItem getEditSourceMenuItem() {
        if (miEditSource == null) {
            miEditSource = createEditSourceMenuItem();
        }
        return miEditSource;
    }
    public JMenuItem getPopupEditSourceMenuItem() {
        if (pmiEditSource == null) {
            pmiEditSource = createEditSourceMenuItem();
        }
        return pmiEditSource;
    }
    private JButton cmdEditSource;
    public JButton getEditSourceButton() {
        if (cmdEditSource == null) {
            cmdEditSource = new JButton();
            cmdEditSource.setToolTipText("Edit HTML source code");
            cmdEditSource.setRequestFocusEnabled(false);
            cmdEditSource.setFocusPainted(false);
            OAButton.setupButton(cmdEditSource);
            cmdEditSource.setIcon(new ImageIcon(getImageURL("source.gif")));
            cmdEditSource.addActionListener(getEditSourceAction());
        }
        return cmdEditSource;
    }    
    

/* ***************************    
>>   COMMAND: BackgroundColor
*****************************/    
    protected KeyStroke getBackgroundColorKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionBackgroundColor;
    protected Action getBackgroundColorAction() {
        if (actionBackgroundColor == null) {
            actionBackgroundColor = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    ColorMenu cm = (ColorMenu) e.getSource();
                    StyleConstants.setBackground(attr, cm.getColor());
                    addAttributeSet(attr);
                }
            };
            String cmd = "BackgroundColor";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getBackgroundColorKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionBackgroundColor);
        }
        return actionBackgroundColor;
    }
    protected ColorMenu createBackgroundColorMenu() {
        ColorMenu menu = new ColorMenu("Background color");
        menu.setToolTipText("Set background color");
        menu.setIcon(new ImageIcon(getImageURL("highlight.gif")));
        menu.addActionListener(getBackgroundColorAction());
        // menu.setAccelerator(getBackgroundColorKeyStroke());
        return menu;
    }
    private ColorMenu menuBackgroundColor, pmenuBackgroundColor;
    public ColorMenu getBackgroundColorMenu() {
        if (menuBackgroundColor == null) {
            menuBackgroundColor = createBackgroundColorMenu();
        }
        return menuBackgroundColor;
    }
    public ColorMenu getPopupBackgroundColorMenu() {
        if (pmenuBackgroundColor == null) {
            pmenuBackgroundColor = createBackgroundColorMenu();
        }
        return pmenuBackgroundColor;
    }
    private OAColorSplitButton cmdBackgroundColor;
    public OAColorSplitButton getBackgroundColorButton() {
        if (cmdBackgroundColor == null) {
            cmdBackgroundColor = new OAColorSplitButton();
            cmdBackgroundColor.setToolTipText("Background color");
            cmdBackgroundColor.setRequestFocusEnabled(false);
            cmdBackgroundColor.setFocusPainted(false);
            cmdBackgroundColor.setIcon(new ImageIcon(getImageURL("highlight.gif")));
            OAButton.setup(cmdBackgroundColor);
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onBackgroundColorChooser();
                }
            };
            cmdBackgroundColor.addActionListener(al);
            cmdBackgroundColor.setColor(Color.white);
        }
        return cmdBackgroundColor;
    }

/* ***************************    
>>   COMMAND: FontColor
*****************************/    
    protected KeyStroke getFontColorKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionFontColor;
    protected Action getFontColorAction() {
        if (actionFontColor == null) {
            actionFontColor = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    ColorMenu cm = (ColorMenu) e.getSource();
                    StyleConstants.setForeground(attr, cm.getColor());
                    addAttributeSet(attr);
                }
            };
            String cmd = "FontColor";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getFontColorKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionFontColor);
        }
        return actionFontColor;
    }
    protected ColorMenu createFontColorMenu() {
        ColorMenu menu = new ColorMenu("Text color");
        menu.setToolTipText("Set text color");
        menu.setIcon(new ImageIcon(getImageURL("fontColor.gif")));
        menu.addActionListener(getFontColorAction());
        // menu.setAccelerator(getFontColorKeyStroke());
        return menu;
    }
    private ColorMenu menuFontColor, pmenuFontColor;
    public ColorMenu getFontColorMenu() {
        if (menuFontColor == null) {
            menuFontColor = createFontColorMenu();
        }
        return menuFontColor;
    }
    public ColorMenu getPopupFontColorMenu() {
        if (pmenuFontColor == null) {
            pmenuFontColor = createFontColorMenu();
        }
        return pmenuFontColor;
    }
    private OAColorSplitButton cmdFontColor;
    public OAColorSplitButton getFontColorButton() {
        if (cmdFontColor == null) {
            cmdFontColor = new OAColorSplitButton();
            cmdFontColor.setToolTipText("Text color");
            cmdFontColor.setRequestFocusEnabled(false);
            cmdFontColor.setFocusPainted(false);
            cmdFontColor.setIcon(new ImageIcon(getImageURL("fontColor.gif")));
            OAButton.setup(cmdFontColor);
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onFontColorChooser();
                }
            };
            cmdFontColor.addActionListener(al);
            cmdFontColor.setColor(Color.black);
        }
        return cmdFontColor;
    }
    
    
/* ***************************    
>>   COMMAND: Alignment
*****************************/    
    private JToggleButton cmdCenter, cmdLeft, cmdRight;
    private JMenuItem miAlignLeft, miAlignRight, miAlignCenter;
    private JMenuItem pmiAlignLeft, pmiAlignRight, pmiAlignCenter;
    
    public JToggleButton getCenterToggleButton() {
        if (cmdCenter == null) {
            cmdCenter = new JToggleButton();
            cmdCenter.setToolTipText("Center-Align Paragraph");
            cmdCenter.setIcon(new ImageIcon(getImageURL("alignCenter.gif")));
            cmdCenter.setRequestFocusEnabled(false);
            cmdCenter.setFocusPainted(false);
            // cmdCenter.setBorderPainted(false);
            cmdCenter.setMargin(new Insets(1,1,1,1));
            cmdCenter.addActionListener(getAction("center-justify"));
        }
        return cmdCenter;
    }
    protected JMenuItem createAlignCenterMenuItem() {
        JMenuItem miAlignCenter = new JMenuItem("Center Aligned ...");
        miAlignCenter.setIcon(new ImageIcon(getImageURL("alignCenter.gif")));
        miAlignCenter.addActionListener(getAction("center-justify"));
        return miAlignCenter;
    }   
    
    public JMenuItem getAlignCenterMenuItem() {
        if (miAlignCenter == null) {
            miAlignCenter = createAlignCenterMenuItem();
        }
        return miAlignCenter;
    }   
    public JMenuItem getPopupAlignCenterMenuItem() {
        if (pmiAlignCenter == null) {
            pmiAlignCenter = createAlignCenterMenuItem();
        }
        return pmiAlignCenter;
    }   


    public JToggleButton getLeftToggleButton() {
        if (cmdLeft == null) {
            cmdLeft = new JToggleButton();
            cmdLeft.setToolTipText("Left-Align Paragraph");
            cmdLeft.setIcon(new ImageIcon(getImageURL("alignLeft.gif")));
            cmdLeft.setRequestFocusEnabled(false);
            cmdLeft.setFocusPainted(false);
            // cmdLeft.setBorderPainted(false);
            cmdLeft.setMargin(new Insets(1,1,1,1));
            cmdLeft.addActionListener(getAction("left-justify"));
        }
        return cmdLeft;
    }
    protected JMenuItem createAlignLeftMenuItem() {
        JMenuItem miAlignLeft = new JMenuItem("Left Aligned ...");
        miAlignLeft.setIcon(new ImageIcon(getImageURL("alignLeft.gif")));
        miAlignLeft.addActionListener(getAction("left-justify"));
        return miAlignLeft;
    }   
    
    public JMenuItem getAlignLeftMenuItem() {
        if (miAlignLeft == null) {
            miAlignLeft = createAlignLeftMenuItem();
        }
        return miAlignLeft;
    }   
    public JMenuItem getPopupAlignLeftMenuItem() {
        if (pmiAlignLeft == null) {
            pmiAlignLeft = createAlignLeftMenuItem();
        }
        return pmiAlignLeft;
    }   

    public JToggleButton getRightToggleButton() {
        if (cmdRight == null) {
            cmdRight = new JToggleButton();
            cmdRight.setToolTipText("Right-Align Paragraph");
            cmdRight.setIcon(new ImageIcon(getImageURL("alignRight.gif")));
            cmdRight.setRequestFocusEnabled(false);
            cmdRight.setFocusPainted(false);
            // cmdRight.setBorderPainted(false);
            cmdRight.setMargin(new Insets(1,1,1,1));
            cmdRight.addActionListener(getAction("right-justify"));
        }
        return cmdRight;
    }
    protected JMenuItem createAlignRightMenuItem() {
        JMenuItem miAlignRight = new JMenuItem("Right Aligned ...");
        miAlignRight.setIcon(new ImageIcon(getImageURL("alignRight.gif")));
        miAlignRight.addActionListener(getAction("right-justify"));
        return miAlignRight;
    }   
    
    public JMenuItem getAlignRightMenuItem() {
        if (miAlignRight == null) {
            miAlignRight = createAlignRightMenuItem();
        }
        return miAlignRight;
    }   
    public JMenuItem getPopupAlignRightMenuItem() {
        if (pmiAlignRight == null) {
            pmiAlignRight = createAlignRightMenuItem();
        }
        return pmiAlignRight;
    }   
    
    
/* ***************************    
>>   COMMAND: Insert Table
*****************************/    
    protected KeyStroke getInsertTableKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertTable;
    protected Action getInsertTableAction() {
        if (actionInsertTable == null) {
            actionInsertTable = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onInsertTable();
                }
            };
            String cmd = "InsertTable";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionInsertTable;
    }
    private JMenuItem miInsertTable;
    private JMenuItem pmiInsertTable;
    
    protected JMenuItem createInsertTableMenuItem() {
        JMenuItem mi = new JMenuItem("Table ...");
        mi.setMnemonic(KeyEvent.VK_T);
        mi.setToolTipText("Insert Table");
        mi.setIcon(new ImageIcon(getImageURL("table.gif")));
        mi.addActionListener(getInsertTableAction());
        return mi;
    }   
    
    public JMenuItem getInsertTableMenuItem() {
        if (miInsertTable == null) {
            miInsertTable = createInsertTableMenuItem();
        }
        return miInsertTable;
    }   
    public JMenuItem getPopupInsertTableMenuItem() {
        if (pmiInsertTable == null) {
            pmiInsertTable = createInsertTableMenuItem();
        }
        return pmiInsertTable;
    }   

    private JButton cmdInsertTable;
    public JButton getInsertTableButton() {
        if (cmdInsertTable == null) {
            cmdInsertTable = new JButton();
            cmdInsertTable.setToolTipText("Insert a table");
            OAButton.setup(cmdInsertTable);
            cmdInsertTable.setRequestFocusEnabled(false);
            cmdInsertTable.setFocusPainted(false);
            cmdInsertTable.setIcon(new ImageIcon(getImageURL("table.gif")));
            cmdInsertTable.addActionListener(getInsertTableAction());
        }
        return cmdInsertTable;
    }
    

/* ***************************    
>>   COMMAND: Insert Table Row
*****************************/    
    protected KeyStroke getInsertTableRowKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertTableRow;
    protected Action getInsertTableRowAction() {
        if (actionInsertTableRow == null) {
            actionInsertTableRow = editorKit.getAction("InsertTableRow");
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionInsertTableRow;
    }
    private JMenuItem miInsertTableRow;
    private JMenuItem pmiInsertTableRow;
    
    protected JMenuItem createInsertTableRowMenuItem() {
        JMenuItem mi = new JMenuItem("Table Row...");
        mi.setMnemonic(KeyEvent.VK_R);
        mi.setToolTipText("Insert Table Row");
        mi.setIcon(new ImageIcon(getImageURL("table.gif")));
        mi.addActionListener(getInsertTableRowAction());
        return mi;
    }   
    
    public JMenuItem getInsertTableRowMenuItem() {
        if (miInsertTableRow == null) {
            miInsertTableRow = createInsertTableRowMenuItem();
        }
        return miInsertTableRow;
    }   
    public JMenuItem getPopupInsertTableRowMenuItem() {
        if (pmiInsertTableRow == null) {
            pmiInsertTableRow = createInsertTableRowMenuItem();
        }
        return pmiInsertTableRow;
    }   

/* ***************************    
>>   COMMAND: Insert Table Col
*****************************/    
    protected KeyStroke getInsertTableColumnKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertTableColumn;
    protected Action getInsertTableColumnAction() {
        if (actionInsertTableColumn == null) {
            actionInsertTableColumn = editorKit.getAction("InsertTableDataCell");
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionInsertTableColumn;
    }
    private JMenuItem miInsertTableColumn;
    private JMenuItem pmiInsertTableColumn;
    
    protected JMenuItem createInsertTableColumnMenuItem() {
        JMenuItem mi = new JMenuItem("Table Column...");
        mi.setMnemonic(KeyEvent.VK_C);
        mi.setToolTipText("Insert Table Column");
        mi.setIcon(new ImageIcon(getImageURL("table.gif")));
        mi.addActionListener(getInsertTableColumnAction());
        return mi;
    }   
    
    public JMenuItem getInsertTableColumnMenuItem() {
        if (miInsertTableColumn == null) {
            miInsertTableColumn = createInsertTableColumnMenuItem();
        }
        return miInsertTableColumn;
    }   
    public JMenuItem getPopupInsertTableColumnMenuItem() {
        if (pmiInsertTableColumn == null) {
            pmiInsertTableColumn = createInsertTableColumnMenuItem();
        }
        return pmiInsertTableColumn;
    }   
    
/* ***************************    
>>   COMMAND: Insert Div
*****************************/    
    protected KeyStroke getInsertDivKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertDiv;
    protected Action getInsertDivAction() {
        if (actionInsertDiv == null) {
            actionInsertDiv = editorKit.getAction(OAHTMLEditorKit.insertDivAction);
            String cmd = "insert-div";
            htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertDivKeyStroke(), cmd);
            htmlEditor.getActionMap().put(cmd, actionInsertDiv);
        }
        return actionInsertDiv;
    }
    
    private JMenuItem miInsertDiv;
    private JMenuItem pmiInsertDiv;
    
    protected JMenuItem createInsertDivMenuItem() {
        JMenuItem mi = new JMenuItem("Html <div>");
        mi.setMnemonic(KeyEvent.VK_D);
        mi.setToolTipText("Insert an html <div>");
        mi.setIcon(new ImageIcon(getImageURL("div.gif")));
        mi.addActionListener(getInsertDivAction());
        return mi;
    }   
    
    public JMenuItem getInsertDivMenuItem() {
        if (miInsertDiv == null) {
            miInsertDiv = createInsertDivMenuItem();
        }
        return miInsertDiv;
    }   
    public JMenuItem getPopupInsertDivMenuItem() {
        if (pmiInsertDiv == null) {
            pmiInsertDiv = createInsertDivMenuItem();
        }
        return pmiInsertDiv;
    }   

    private JButton cmdInsertDiv;
    public JButton getInsertDivButton() {
        if (cmdInsertDiv == null) {
            cmdInsertDiv = new JButton();
            cmdInsertDiv.setToolTipText("Insert a Div");
            OAButton.setup(cmdInsertDiv);
            cmdInsertDiv.setRequestFocusEnabled(false);
            cmdInsertDiv.setFocusPainted(false);
            cmdInsertDiv.setIcon(new ImageIcon(getImageURL("div.gif")));
            cmdInsertDiv.addActionListener(getInsertDivAction());
        }
        return cmdInsertDiv;
    }
    
/* ***************************    
>>   COMMAND: FontName combo
*****************************/
    private String[] fontNames;
    private String[] genericFontFamily;
    private JComboBox cboFontName;
    public String[] getFontNames() {
        if (fontNames == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontNames = ge.getAvailableFontFamilyNames(Locale.getDefault());
        }
        return fontNames;
    }
    private Action actionFontName;
    protected Action getFontNameAction() {
        if (actionFontName == null) {
            actionFontName = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                   if (bUpdatingAttributes) return;
                   if (getFontNameComboBox().getSelectedIndex() >= 0) {
                       MutableAttributeSet attr = new SimpleAttributeSet();
                       String s = getFontNameComboBox().getSelectedItem().toString();
                       StyleConstants.setFontFamily(attr, s);
                       addAttributeSet(attr);
                       htmlEditor.requestFocusInWindow();
                       updateAttributeCommands();
                   }
               }
            };
            String cmd = "FontName";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionFontName;
    }
    public String[] getGenericFontNames() {
        if (genericFontFamily == null) {
            // these are the CSS default fonts
            // genericFontFamily = new String[] {"serif", "sans-serif", "cursive", "fantasy", "monospace" };
            
            // from Java doc, Java default/logical fonts
            genericFontFamily = new String[] {"Serif", "Sans-serif", "Monospaced", "Dialog", "DialogInput" };
        }
        return genericFontFamily;
    }
    public JComboBox getFontNameComboBox() {
        if (cboFontName == null) {
            getGenericFontNames();
            Vector vec = new Vector();
            for (int i=0; i<genericFontFamily.length; i++) vec.add(genericFontFamily[i]);
    
            getFontNames();
            for (int i=0; fontNames != null && i<fontNames.length; i++) vec.add(fontNames[i]);
    
            cboFontName = new JComboBox(vec);
            cboFontName.setToolTipText("Set/Change Font");
            cboFontName.setMaximumRowCount(22);
            cboFontName.setRequestFocusEnabled(false);
            cboFontName.setFocusable(false);
    
            String s = "";
            int maxWidth = 0;
            for (int i=0; fontNames != null && i<fontNames.length; i++) {
                int w = cboFontName.getFontMetrics(cboFontName.getFont()).stringWidth(fontNames[i]);
                maxWidth = Math.max(w, maxWidth);
            }
    
            cboFontName.setRenderer(new FontNameComboBoxCellRenderer(getGenericFontNames().length, maxWidth));
            
            cboFontName.setPrototypeDisplayValue("12345ABCDE FGhijklmnOOOxyz");
            
            Dimension d = cboFontName.getMinimumSize();
            d.width = maxWidth + 12 + 6;
            
            cboFontName.setMaximumSize(d);
            cboFontName.setMinimumSize(d);
            cboFontName.setPreferredSize(d);
            cboFontName.setSelectedIndex(-1);
            
            cboFontName.addPopupMenuListener(new ComboBoxPopupMenuListener(cboFontName, maxWidth*2+12+6));
            cboFontName.addActionListener(getFontNameAction());
        }
        return cboFontName;
    }

    
/* ***************************    
>>   COMMAND: FontSize combo
*****************************/
    private JComboBox cboFontSize;
    public static final int[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 30, 32, 36, 48, 72};
    private Action actionFontSize;
    protected Action getFontSizeAction() {
        if (actionFontSize == null) {
            actionFontSize = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (bUpdatingAttributes) return;
                         
                    String s = getFontSizeTextField().getText();

                    // 20121112
                    if (s.matches("[\\d]+[\\s]*pt")) {
//qqqqqqqqq get correct regex                        
                        int fontSize = OAString.parseInt(s);
                        //System.out.println("Selected fontSize="+fontSize);                  
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        // StyleConstants.setFontSize(attr, fontSize);  CSS will try to convert to one of the 7 fonts
                        attr.addAttribute(StyleConstants.FontSize, fontSize+"pt");  // so that CSS will convert to correctly
                        addAttributeSet(attr);
                        htmlEditor.requestFocusInWindow();
                        updateAttributeCommands();
                    }
                    
                    /* was:
                    int pos = getFontSizeComboBox().getSelectedIndex();
                    if (pos >= 0 && pos < fontSizes.length) {
                        int fontSize = fontSizes[pos];
                        //System.out.println("Selected fontSize="+fontSize);                  
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        // StyleConstants.setFontSize(attr, fontSize);  CSS will try to convert to one of the 7 fonts
                        attr.addAttribute(StyleConstants.FontSize, fontSize+"pt");  // so that CSS will convert to correctly
                        addAttributeSet(attr);
                        htmlEditor.requestFocusInWindow();
                        updateAttributeCommands();
                    }
                    */
                }
            };
        }
        return actionFontSize;
    }
    public JComboBox getFontSizeComboBox() {
        if (cboFontSize == null) {
            String[] ss = new String[fontSizes.length];
            for (int i=0; i<fontSizes.length; i++) ss[i] = fontSizes[i] + "pt";
            cboFontSize = new JComboBox(ss);
            
            cboFontSize.setToolTipText("Set/Change size of font");
            // cboFontSize.setEditable(true);
// commented out when textfield editor was added            
            cboFontSize.setRequestFocusEnabled(false);
//            cboFontSize.setFocusable(false);

            getFontNameComboBox(); // so that same height is used
            
            Dimension d = cboFontName.getMinimumSize();
            d.width = cboFontSize.getFontMetrics(cboFontSize.getFont()).stringWidth("XXXXX") + 12 + 6;
            cboFontSize.setMinimumSize(d);
            cboFontSize.setMaximumSize(d);
            cboFontSize.setPreferredSize(d);
            cboFontSize.setMaximumRowCount(16);
            cboFontSize.setSelectedIndex(-1);
            cboFontSize.addActionListener(getFontSizeAction());
            
            cboFontSize.setEditor( getFontSizeComboBoxEditor() );
            cboFontSize.setEditable(true);
        }
        return cboFontSize;
    }
    
    // 20121112
    public JTextField getFontSizeTextField() {
        if (txtFontSize == null) {
            txtFontSize = new JTextField(5);
            // txtFontSize.setBorder(null);
            txtFontSize.setEnabled(true);
            txtFontSize.setEditable(true);
        }
        return txtFontSize;
    }
    
    // 20121112
    private JTextField txtFontSize; 
    private ComboBoxEditor fontSizeComboBoxEditor;
    public ComboBoxEditor getFontSizeComboBoxEditor() {
        if (fontSizeComboBoxEditor != null) return fontSizeComboBoxEditor;
        getFontSizeTextField();
        fontSizeComboBoxEditor = new ComboBoxEditor() {
            @Override
            public void setItem(Object anObject) {
                if (bUpdatingAttributes) return;
                String s;
                if (anObject != null) s = anObject.toString();
                else s = "";
                txtFontSize.setText(s);
            }
            @Override
            public void selectAll() {
                txtFontSize.selectAll();
                txtFontSize.requestFocus();
            }
            @Override
            public Object getItem() {
                String s = txtFontSize.getText();
                return s;
            }
            @Override
            public Component getEditorComponent() {
                return txtFontSize;
            }
            @Override
            public void addActionListener(ActionListener l) {
                txtFontSize.addActionListener(l);
            }
            @Override
            public void removeActionListener(ActionListener l) {
                txtFontSize.removeActionListener(l);
            }
        };
        return fontSizeComboBoxEditor;
    }
    
    
/* ***************************    
>>   COMMAND: Font dialog
*****************************/
    private Action actionFontDialog;
    protected Action getFontDialogAction() {
        if (actionFontDialog == null) {
            actionFontDialog = new AbstractAction() {
                public @Override void actionPerformed(ActionEvent e) {
                    onFontChange();
                }
            };
        }
        return actionFontDialog;
    }
    public JMenuItem createFontDialogMenuItem() {
        JMenuItem mi = new JMenuItem("Font ...");
        mi.addActionListener(getFontDialogAction());
        return mi;
    }
    private JMenuItem miFontDialog, pmiFontDialog;
    public JMenuItem getFontMenuItem() {
        if (miFontDialog == null) {
            miFontDialog = createFontDialogMenuItem();
        }
        return miFontDialog;
    }
    public JMenuItem getPopupFontMenuItem() {
        if (pmiFontDialog == null) {
            pmiFontDialog = createFontDialogMenuItem();
        }
        return pmiFontDialog;
    }


/* ***********************************    
>>   COMMAND: Insert Unordered List
*************************************/
    private Action actionInsertUnorderedList;
    protected Action getInsertUnorderedListAction() {
        if (actionInsertUnorderedList == null) {
            actionInsertUnorderedList = editorKit.getAction("InsertUnorderedListItem");
            //was: actionInsertUnorderedList = editorKit.getAction(OAHTMLEditorKit.insertUnorderedListAction);
        }
        return actionInsertUnorderedList;
    }
    
    private JButton cmdInsertUnorderedList;
    public JButton getInsertUnorderedListButton() {
        if (cmdInsertUnorderedList == null) {
            cmdInsertUnorderedList = new JButton();
            cmdInsertUnorderedList.setToolTipText("Insert list with bullets");
            OAButton.setup(cmdInsertUnorderedList);
            cmdInsertUnorderedList.setRequestFocusEnabled(false);
            cmdInsertUnorderedList.setFocusPainted(false);
            cmdInsertUnorderedList.setIcon(new ImageIcon(getImageURL("unorderedList.gif")));
            cmdInsertUnorderedList.addActionListener(getInsertUnorderedListAction());
        }
        return cmdInsertUnorderedList;
    }
    private JMenuItem miUnorderedList, pmiUnorderedList;
    public JMenuItem createUnorderedListMenuItem() {
        JMenuItem mi = new JMenuItem("Bulleted list <ul>");
        mi.setIcon(new ImageIcon(getImageURL("unorderedList.gif")));
        mi.addActionListener(getInsertUnorderedListAction());
        return mi;
    }
    public JMenuItem getUnorderedListMenuItem() {
        if (miUnorderedList == null) {
            miUnorderedList = createUnorderedListMenuItem();
        }
        return miUnorderedList;
    }
    public JMenuItem getPopupUnorderedListMenuItem() {
        if (pmiUnorderedList == null) {
            pmiUnorderedList = createUnorderedListMenuItem();
        }
        return pmiUnorderedList;
    }
    
    
/* ***********************************    
>>   COMMAND: Insert Ordered List
*************************************/
    // this has inconsistent issues, esp when inserting at begin or end of doc, or at the end of a line.
    // al = getAction("InsertUnorderedList");
    // getInsertUnorderedListButton().addActionListener(al);
   private Action actionInsertOrderedList;
    protected Action getInsertOrderedListAction() {
        if (actionInsertOrderedList == null) {
            actionInsertOrderedList = editorKit.getAction("InsertOrderedList");
            //was: actionInsertOrderedList = editorKit.getAction(OAHTMLEditorKit.insertOrderedListAction);
        }
        return actionInsertOrderedList;
    }
    
    private JButton cmdInsertOrderedList;
    public JButton getInsertOrderedListButton() {
        if (cmdInsertOrderedList == null) {
            cmdInsertOrderedList = new JButton();
            cmdInsertOrderedList.setToolTipText("Insert numbered list");
            OAButton.setup(cmdInsertOrderedList);
            cmdInsertOrderedList.setRequestFocusEnabled(false);
            cmdInsertOrderedList.setFocusPainted(false);
            cmdInsertOrderedList.setIcon(new ImageIcon(getImageURL("orderedList.gif")));
            cmdInsertOrderedList.addActionListener(getInsertOrderedListAction());
        }
        return cmdInsertOrderedList;
    }
    private JMenuItem miOrderedList, pmiOrderedList;
    public JMenuItem createOrderedListMenuItem() {
        JMenuItem mi = new JMenuItem("Numbered List <ol>");
        mi.setIcon(new ImageIcon(getImageURL("orderedList.gif")));
        mi.addActionListener(getInsertOrderedListAction());
        return mi;
    }
    public JMenuItem getOrderedListMenuItem() {
        if (miOrderedList == null) {
            miOrderedList = createOrderedListMenuItem();
        }
        return miOrderedList;
    }
    public JMenuItem getPopupOrderedListMenuItem() {
        if (pmiOrderedList == null) {
            pmiOrderedList = createOrderedListMenuItem();
        }
        return pmiOrderedList;
    }

    
/* ***************************    
>>   COMMAND: Follow Link href/mailto
*****************************/    
    protected KeyStroke getFollowLinkKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionFollowLink;
    protected Action getFollowLinkAction() {
        if (actionFollowLink == null) {
            actionFollowLink = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onFollowLink(currentLinkSource);
                }
            };
            String cmd = "FollowLink";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertImageKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionInsertImage);
        }
        return actionFollowLink;
    }
    private JMenuItem miFollowLink;
    private JMenuItem pmiFollowLink;
    
    protected JMenuItem createFollowLinkMenuItem() {
        JMenuItem mi = new JMenuItem("Follow link ...");
        mi.setMnemonic(KeyEvent.VK_F);
        mi.setToolTipText("Goto the selected link.");
        // mi.setIcon(new ImageIcon(getImageURL("table.gif")));
        mi.addActionListener(getFollowLinkAction());
        return mi;
    }   
    
    public JMenuItem getFollowLinkMenuItem() {
        if (miFollowLink == null) {
            miFollowLink = createFollowLinkMenuItem();
        }
        return miFollowLink;
    }   
    public JMenuItem getPopupFollowLinkMenuItem() {
        if (pmiFollowLink == null) {
            pmiFollowLink = createFollowLinkMenuItem();
        }
        return pmiFollowLink;
    }   

/* ***************************    
>>   COMMAND: InsertImageDiv
*****************************/    
    
    /* this is used to insert a <div> that has a background image, scaled to match DPI differences
         this is used for printing certificate like documents
         
        Example output:
        <div style="background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/Bytes?33&h=245&w=327); width:327; height:245; background-repeat:no-repeat">
        
        This can then be used by "convert to PDF" to be the hi-res background image
           see goHi5 project
    */
    protected KeyStroke getInsertImageDivKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertImageDiv;
    protected Action getInsertImageDivAction() {
        if (actionInsertImageDiv == null) {
            actionInsertImageDiv = editorKit.getAction(OAHTMLEditorKit.insertImageDivAction);
            String cmd = "InsertImageDiv";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getFixedSizeBackgroundImageKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionFixedSizeBackgroundImage);
        }
        return actionInsertImageDiv;
    }
    protected JMenuItem createInsertImageDivMenuItem() {
        JMenuItem miInsertImageDiv = new JMenuItem("Background Image <div> ...");
        miInsertImageDiv.setToolTipText("create a <div> that has a background image");
        miInsertImageDiv.setMnemonic(KeyEvent.VK_B);
        miInsertImageDiv.setIcon(new ImageIcon(getImageURL("image.png")));
        miInsertImageDiv.addActionListener(getInsertImageDivAction());
        // miInsertImageDiv.setAccelerator(getFixedSizeBackgroundImageKeyStroke());
        return miInsertImageDiv;
    }
    private JMenuItem miInsertImageDiv, pmiInsertImageDiv;
    public JMenuItem getInsertImageDivMenuItem() {
        if (miInsertImageDiv == null) {
            miInsertImageDiv = createInsertImageDivMenuItem();
        }
        return miInsertImageDiv;
    }
    public JMenuItem getPopupInsertImageDivMenuItem() {
        if (pmiInsertImageDiv == null) {
            pmiInsertImageDiv = createInsertImageDivMenuItem();
        }
        return pmiInsertImageDiv;
    }
    private JButton cmdInsertImageDiv;
    public JButton getInsertImageDivButton() {
        if (cmdInsertImageDiv == null) {
            cmdInsertImageDiv = new JButton("Background Image <div> ...");
            cmdInsertImageDiv.setToolTipText("create a <div> that has a background image");
            cmdInsertImageDiv.setRequestFocusEnabled(false);
            cmdInsertImageDiv.setFocusPainted(false);
            OAButton.setupButton(cmdInsertImageDiv);
            cmdInsertImageDiv.setIcon(new ImageIcon(getImageURL("image.png")));
            cmdInsertImageDiv.addActionListener(getInsertImageDivAction());
        }
        return cmdInsertImageDiv;
    }    

/* ***************************    
>>   COMMAND: EditBlock (paragraph)
*****************************/    
    protected KeyStroke getEditBlockKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionEditBlock;
    protected Action getEditBlockAction() {
        if (actionEditBlock == null) {
            actionEditBlock = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onEditBlockCode();
                }
            };
            String cmd = "EditBlock";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getEditBlockKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionEditBlock);
        }
        return actionEditBlock;
    }
    protected JMenuItem createEditBlockMenuItem() {
        JMenuItem miEditBlock = new JMenuItem("Paragraph properties ...");
        miEditBlock.setToolTipText("Edit HTML source code");
        miEditBlock.setMnemonic(KeyEvent.VK_P);
        
        miEditBlock.setIcon(new ImageIcon(getImageURL("paragraph.gif")));
        miEditBlock.addActionListener(getEditBlockAction());
        // miEditBlock.setAccelerator(getEditBlockKeyStroke());
        return miEditBlock;
    }
    private JMenuItem miEditBlock, pmiEditBlock;
    public JMenuItem getEditBlockMenuItem() {
        if (miEditBlock == null) {
            miEditBlock = createEditBlockMenuItem();
        }
        return miEditSource;
    }
    public JMenuItem getPopupEditBlockMenuItem() {
        if (pmiEditBlock == null) {
            pmiEditBlock = createEditBlockMenuItem();
        }
        return pmiEditBlock;
    }
    private JButton cmdEditBlock;
    public JButton getEditBlockButton() {
        if (cmdEditBlock == null) {
            cmdEditBlock = new JButton();
            cmdEditBlock.setToolTipText("Edit paragraph properties");
            cmdEditBlock.setRequestFocusEnabled(false);
            cmdEditBlock.setFocusPainted(false);
            OAButton.setupButton(cmdEditBlock);
            cmdEditBlock.setIcon(new ImageIcon(getImageURL("paragraph.gif")));
            cmdEditBlock.addActionListener(getEditBlockAction());
        }
        return cmdEditBlock;
    }    
    
/* **************************    
>>   COMMAND: InsertDialog
*****************************/    
    protected KeyStroke getInsertDialogKeyStroke() {
        return null; // KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionInsertDialog;
    protected Action getInsertDialogAction() {
        if (actionInsertDialog == null) {
            actionInsertDialog = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onInsertDialog();
                }
            };
            String cmd = "InsertDialog";
            // htmlEditor.getInputMap(JComponent.WHEN_FOCUSED).put(getInsertDialogKeyStroke(), cmd);
            // htmlEditor.getActionMap().put(cmd, actionInsertDialog);
        }
        return actionInsertDialog;
    }
    protected JMenuItem createInsertDialogMenuItem() {
        JMenuItem miInsertDialog = new JMenuItem("Insert break ...");
        miInsertDialog.setToolTipText("Insert break before/after/current position");
        miInsertDialog.setMnemonic(KeyEvent.VK_I);
        miInsertDialog.setIcon(new ImageIcon(getImageURL("insert.gif")));
        miInsertDialog.addActionListener(getInsertDialogAction());
        // miInsertDialog.setAccelerator(getInsertDialogKeyStroke());
        return miInsertDialog;
    }
    private JMenuItem miInsertDialog, pmiInsertDialog;
    public JMenuItem getInsertDialogMenuItem() {
        if (miInsertDialog == null) {
            miInsertDialog = createInsertDialogMenuItem();
        }
        return miEditSource;
    }
    public JMenuItem getPopupInsertDialogMenuItem() {
        if (pmiInsertDialog == null) {
            pmiInsertDialog = createInsertDialogMenuItem();
        }
        return pmiInsertDialog;
    }
    private JButton cmdInsertDialog;
    public JButton getInsertDialogButton() {
        if (cmdInsertDialog == null) {
            cmdInsertDialog = new JButton();
            cmdInsertDialog.setToolTipText("Insert break before/after/current position");
            cmdInsertDialog.setRequestFocusEnabled(false);
            cmdInsertDialog.setFocusPainted(false);
            OAButton.setupButton(cmdInsertDialog);
            cmdInsertDialog.setIcon(new ImageIcon(getImageURL("insert.gif")));
            cmdInsertDialog.addActionListener(getInsertDialogAction());
        }
        return cmdInsertDialog;
    }    
    
    
    
// ============ End of UI components     

    
    private JMenu menuEdit; 
// MenuBar    
    public JMenuBar getMenuBar() {
        if (menuBar != null) return menuBar;
        
        menuBar = new JMenuBar();
        JMenu menu;
        
        menu = new JMenu("Edit");
        menuEdit = menu;
        MenuListener ml = new MenuListener() {
            public void menuSelected(MenuEvent e) {
                if (htmlEditor != null) {
                    int p = htmlEditor.getCaretPosition();
                    int p2 = htmlEditor.getCaret().getMark();
                    boolean b = (p != p2);
                    
                    getUnselectMenuItem().setEnabled(b);
                    
                    boolean b2 = htmlEditor != null && htmlEditor.isEnabled() && htmlEditor.isEditable();
                    
                    getCutMenuItem().setEnabled(b && b2);
                    getCopyMenuItem().setEnabled(b);

                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Object objx = cb.getContents(null);

                    getPasteMenuItem().setEnabled(objx != null && b2);
                }
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        };

        menu.add(getUndoMenuItem());
        menu.add(getRedoMenuItem());

        menu.addSeparator();
        menu.add(getCutMenuItem());
        menu.add(getCopyMenuItem());
        menu.add(getPasteMenuItem());
        
        menu.addSeparator();
        menu.add(getFindMenuItem());
        menu.add(getReplaceMenuItem());

        menu.addSeparator();
        menu.add(getSpellCheckMenuItem());
        menu.add(getAutoCompleteMenuItem());

        menu.addSeparator();
        menu.add(getInsertImageDivMenuItem());
        menu.add(getEditImageMenuItem());
        
        menu.addSeparator();
        menu.add(getSelectLineMenuItem());
        menu.add(getSelectParagraphMenuItem());
        menu.add(getSelectAllMenuItem());
        menu.add(getUnselectMenuItem());
        
        
        menu.addSeparator();
        menu.add(getEditBlockMenuItem());
        menu.add(getEditSourceMenuItem());
        menuBar.add(menu);

        
        
        menu = new JMenu("Format");
        //Menu listeners to set the correct background color when menu is selected        
        ml = new MenuListener() {
            public void menuSelected(MenuEvent e) {
                if (htmlEditor != null) {
                    int p = htmlEditor.getCaretPosition();
                    AttributeSet attr = editorDocument.getCharacterElement(p).getAttributes();
                    Color c = StyleConstants.getBackground(attr);
                    if (menuBackgroundColor != null) menuBackgroundColor.setColor(c);

                    c = StyleConstants.getForeground(attr);
                    if (menuFontColor != null) menuFontColor.setColor(c);
                }
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        };
        menu.addMenuListener(ml);
        
        
        menu.add(getBoldMenuItem());
        menu.add(getUnderlineMenuItem());
        menu.add(getItalicMenuItem());
        menu.addSeparator();
        menu.add(getFontMenuItem());
        menu.addSeparator();
        menu.add(getFontColorMenu());
        menu.add(getBackgroundColorMenu());
        menu.addSeparator();
        menu.add(getAlignLeftMenuItem());
        menu.add(getAlignRightMenuItem());
        menu.add(getAlignCenterMenuItem());
        menuBar.add(menu);
        
        menu = new JMenu("Insert");
        menu.add(getParagraphBreakMenuItem());
        menu.add(getInsertDivMenuItem());
        menu.add(getInsertDialogMenuItem());
        menu.addSeparator();
        menu.add(getInsertImageMenuItem());
        menu.add(getInsertImageDivMenuItem());
        menu.addSeparator();
        menu.add(getOrderedListMenuItem());
        menu.add(getUnorderedListMenuItem());
        menu.addSeparator();
        menu.add(getInsertTableMenuItem());
        menu.add(getInsertTableRowMenuItem());
        menu.add(getInsertTableColumnMenuItem());
        menu.addSeparator();
        menu.add(getInsertHyperLinkMenuItem());
        menu.add(getInsertMailtoMenuItem());
        menu.add(getInsertFieldMenuItem());
        menuBar.add(menu);
        
        updateEnabled();
        
        return menuBar;
    }
    
// PopupMenu    
    private JMenu pmenuEdit; 
    protected JPopupMenu getPopupMenu() {
        if (editorKit == null) return null; // not yet initialized
        if (popupMenu != null) return popupMenu;
        popupMenu = new JPopupMenu();
        
        JMenu menu;
        
        menu = new JMenu("Edit");
        pmenuEdit = menu;
        MenuListener ml = new MenuListener() {
            public void menuSelected(MenuEvent e) {
                if (htmlEditor != null) {
                    int p = htmlEditor.getCaretPosition();
                    int p2 = htmlEditor.getCaret().getMark();
                    boolean b = (p != p2);
                    
                    getPopupUnselectMenuItem().setEnabled(b);

                    boolean b2 = htmlEditor != null && htmlEditor.isEnabled() && htmlEditor.isEditable();
                    
                    getPopupCutMenuItem().setEnabled(b && b2);
                    getPopupCopyMenuItem().setEnabled(b);

                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Object objx = cb.getContents(null);
                    
                    getPopupPasteMenuItem().setEnabled(objx != null && b2);
                }
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        };
        menu.addMenuListener(ml);
        
        
        menu.add(getPopupUndoMenuItem());
        menu.add(getPopupRedoMenuItem());

        menu.addSeparator();
        menu.add(getPopupCutMenuItem());
        menu.add(getPopupCopyMenuItem());
        menu.add(getPopupPasteMenuItem());
        
        menu.addSeparator();
        menu.add(getPopupFindMenuItem());
        menu.add(getPopupReplaceMenuItem());

        menu.addSeparator();
        menu.add(getPopupSpellCheckMenuItem());
        menu.add(getPopupAutoCompleteMenuItem());

        menu.addSeparator();
        menu.add(getPopupEditImageMenuItem());
        
        menu.addSeparator();
        menu.add(getPopupSelectLineMenuItem());
        menu.add(getPopupSelectParagraphMenuItem());
        menu.add(getPopupSelectAllMenuItem());
        menu.add(getPopupUnselectMenuItem());
        
        
        menu.addSeparator();
        menu.add(getPopupEditBlockMenuItem());
        menu.add(getPopupEditSourceMenuItem());
        popupMenu.add(menu);

        menu = new JMenu("Format");
        //Menu listeners to set the correct background color when menu is selected        
        ml = new MenuListener() {
            public void menuSelected(MenuEvent e) {
                if (htmlEditor != null) {
                    int p = htmlEditor.getCaretPosition();
                    AttributeSet attr = editorDocument.getCharacterElement(p).getAttributes();
                    Color c = StyleConstants.getBackground(attr);
                    if (pmenuBackgroundColor != null) pmenuBackgroundColor.setColor(c);

                    c = StyleConstants.getForeground(attr);
                    if (pmenuFontColor != null) pmenuFontColor.setColor(c);
                }
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        };
        menu.addMenuListener(ml);
        
        
        menu.add(getPopupBoldMenuItem());
        menu.add(getPopupUnderlineMenuItem());
        menu.add(getPopupItalicMenuItem());
        menu.addSeparator();
        menu.add(getPopupFontMenuItem());
        menu.addSeparator();
        menu.add(getPopupFontColorMenu());
        menu.add(getPopupBackgroundColorMenu());
        menu.addSeparator();
        menu.add(getPopupAlignLeftMenuItem());
        menu.add(getPopupAlignRightMenuItem());
        menu.add(getPopupAlignCenterMenuItem());
        popupMenu.add(menu);
        

        menu = new JMenu("Insert");
        menu.add(getPopupParagraphBreakMenuItem());
        menu.add(getPopupInsertDivMenuItem());
        menu.add(getPopupInsertDialogMenuItem());
        menu.addSeparator();
        menu.add(getPopupInsertImageMenuItem());
        menu.add(getPopupInsertImageDivMenuItem());
        menu.addSeparator();
        menu.add(getPopupOrderedListMenuItem());
        menu.add(getPopupUnorderedListMenuItem());
        menu.addSeparator();
        menu.add(getPopupInsertTableMenuItem());
        menu.add(getPopupInsertTableRowMenuItem());
        menu.add(getPopupInsertTableColumnMenuItem());
        menu.addSeparator();
        menu.add(getPopupInsertHyperLinkMenuItem());
        menu.add(getPopupInsertMailtoMenuItem());
        menu.add(getPopupInsertFieldMenuItem());
        popupMenu.add(menu);
    
        popupMenu.addSeparator();
        popupMenu.add(getPopupFollowLinkMenuItem());
        
        updateEnabled();
        
        return popupMenu;
    }
    
    
    /**
     * 
     * @see OAScroller to add scrolling to toolbar
     * 
     */
    public JToolBar getToolBar() {
        if (toolBar != null) return toolBar;
        toolBar = new JToolBar();

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        toolBar.add(getUndoButton());
        toolBar.add(getRedoButton());
        toolBar.addSeparator();
        
        
        toolBar.add(getFindButton());
        toolBar.add(getAutoCompleteButton());
        toolBar.add(getSpellCheckButton());
        toolBar.addSeparator();

        toolBar.add(getFontNameComboBox());
        toolBar.add(Box.createHorizontalStrut(2));
        toolBar.add(getFontSizeComboBox());

        toolBar.addSeparator();

        toolBar.add(getBoldToggleButton());
        toolBar.add(getItalicToggleButton());
        toolBar.add(getUnderlineToggleButton());
        toolBar.addSeparator();

        toolBar.add(getFontColorButton());
        toolBar.add(getBackgroundColorButton());
        toolBar.add(getSplitImageButton());

        toolBar.addSeparator();

        toolBar.add(getLeftToggleButton());
        toolBar.add(getCenterToggleButton());
        toolBar.add(getRightToggleButton());

        toolBar.addSeparator();
        toolBar.add(getInsertOrderedListButton());
        toolBar.add(getInsertUnorderedListButton());
        
        toolBar.addSeparator();
        
        toolBar.add(getInsertTableButton());
        toolBar.add(getSplitHyperLinkButton());        
        
        toolBar.addSeparator();
        toolBar.add(getEditSourceButton());
    
        
        updateEnabled();
        
        return toolBar;
    }
        


    public void onFontChange() {
        if (dlgFont == null) {
            dlgFont = new FontDialog(getWindow(), getFontNames(), fontSizes);
        }
        AttributeSet a = editorDocument.getCharacterElement(htmlEditor.getCaretPosition()).getAttributes();
        dlgFont.setAttributes(a);
        dlgFont.setVisible(true);
        if (dlgFont.succeeded()) {
            addAttributeSet(dlgFont.getAttributes());
            updateAttributeCommands();
        }
    }
    
    private InsertHyperlinkDialog dlgInsertHyperlink;
    protected InsertHyperlinkDialog getInsertHyperlinkDialog() {
        if (dlgInsertHyperlink == null) {
            dlgInsertHyperlink = new InsertHyperlinkDialog(SwingUtilities.windowForComponent(editor));
        }
        return dlgInsertHyperlink;
    }
    public void onInsertHyperLink() {
        getInsertHyperlinkDialog().setVisible(true);
        if (getInsertHyperlinkDialog().wasCanceled()) return;
        String link = getInsertHyperlinkDialog().getTextField().getText();
        if (OAString.isEmpty(link)) return;
        
        String s = link.toLowerCase();
        s = s.replace("\\", "/");
        if (s.startsWith("http://")) link = link.substring(7);
        
        Element ep = editorDocument.getCharacterElement(htmlEditor.getSelectionStart());
        try {
            editorDocument.insertAfterEnd(ep, "<a href='http://"+link+"'>"+link+"</a>");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
    private InsertMailtoDialog dlgInsertMailto;
    protected InsertMailtoDialog getInsertMailtoDialog() {
        if (dlgInsertMailto == null) {
            dlgInsertMailto = new InsertMailtoDialog(SwingUtilities.windowForComponent(editor));
        }
        return dlgInsertMailto;
    }
    public void onInsertMailto() {
        getInsertMailtoDialog().setVisible(true);
        if (getInsertMailtoDialog().wasCanceled()) return;
        String link = getInsertMailtoDialog().getTextField().getText();
        if (OAString.isEmpty(link)) return;
        
        Element ep = editorDocument.getCharacterElement(htmlEditor.getSelectionStart());
        try {
            editorDocument.insertAfterEnd(ep, "<a href='mailto:"+link+"'>"+link+"</a>");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Base/root class to use when inserting Field tags.
     * Set to non-OAObject class to to have it disabled.
     */
    public void setFieldClass(Class c) {
        HTMLTextPaneController cx = getBindController();
        if (cx == null) return;
        cx.setFieldClass(c);
    }
    /**
     * Get the class to use for the list of Fields that a user can insert into a document.
     * If fieldClass was not set by calling setFieldClass(..), then it will look for 
     * a method named "get"+propertyPath+"FieldClass" from the current Hub's class.
     * If not found then it will use the Hub's class.
     * @return
     */
    public Class getFieldClass() {
        HTMLTextPaneController cx = getBindController();
        if (cx == null) return null;
        return cx.getFieldClass();
    }    
    
    
    private InsertFieldDialog dlgInsertField;
    public InsertFieldDialog getInsertFieldDialog() {
        if (dlgInsertField == null) {
            dlgInsertField = new InsertFieldDialog(SwingUtilities.windowForComponent(editor), getObjectDefs(), getCustomFields(), getCustomCommands());
        }
        return dlgInsertField;
    }
    
    private Hub<ObjectDef> hubObjectDef;
    public Hub<ObjectDef> getObjectDefs() {
        if (hubObjectDef == null) hubObjectDef = new Hub<ObjectDef>(ObjectDef.class);
        return hubObjectDef;
    }
    private Hub<String> hubCustomField;
    public Hub<String> getCustomFields() {
        if (hubCustomField == null) {
            hubCustomField = new Hub<String>(String.class);
            addDefaultCustomFields();
        }
        return hubCustomField;
    }
    public void addDefaultCustomFields() {
        if (hubCustomField != null) {
            hubCustomField.add("Date");
            hubCustomField.add("Time");
            hubCustomField.add("Page");
        }
    }
    
    private Hub<String> hubCustomCommand;
    public Hub<String> getCustomCommands() {
        if (hubCustomCommand == null) {
            hubCustomCommand = new Hub<>(String.class);
            hubCustomCommand.add("format: <%=prop[,width||fmt]%>");
            hubCustomCommand.add("for each: <%=foreach [prop]%>..<%=end%>");
            hubCustomCommand.add("if statement: <%=if prop%>..<%=end%>");
            hubCustomCommand.add("ifnot statement: <%=ifnot prop%>..<%=end%>");
            hubCustomCommand.add("if equals statement: <%=ifequals prop \"value to match\"%>..<%=end%>");
            hubCustomCommand.add("format block: <%=format[X],'12 L'%>..<%=end%>");
            hubCustomCommand.add("include file: <%=include filename%>");
            hubCustomCommand.add("counter in foreach: <%=#counter, fmt%>");
            hubCustomCommand.add("sum: <%=#sum [prop] prop fmt%>");
            hubCustomCommand.add("count: <%=#count prop, fmt%>");
            //  hubCustomField.add("");
        }
        return hubCustomCommand;
    }
    
    
    // 20130223
    public void onInsertField() {
        
        if (!getInsertFieldButton().isEnabled()) return;
        
        getObjectDefs().clear();

        HTMLTextPaneController cx = getBindController();
        if (cx == null) return;

        Class c = cx.getFieldClass();
        if (c == null) return;

        ObjectDef od = ObjectDefDelegate.getObjectDef(hubObjectDef, c);
        hubObjectDef.setAO(od);
        
        // formatting

        InsertFieldDialog dlg = getInsertFieldDialog();
        
        dlg.getTextField().setText("");
        
        getCustomFields().setPos(-1);
        String[] sx = cx.getCustomFields();
        if (sx != null) {  // even if empty
            getCustomFields().clear();
            addDefaultCustomFields();
            for (String s : sx) {
                getCustomFields().add(s);
            }
        }

        getCustomCommands().setPos(-1);
        
        getInsertFieldDialog().setVisible(true);
        // getInsertFieldDialog().getPropertyPathTree().expand(od);  qqqq not working, since it's in a popup
        if (getInsertFieldDialog().wasCanceled()) return;
        String field = getInsertFieldDialog().getTextField().getText();
        
        if (OAString.isEmpty(field)) {
            field = getCustomFields().getAO();
            if (!OAString.isEmpty(field)) {
                field = "$"+field;
            }
        }
        
        String cmd = getCustomCommands().getAO();
        if (getCustomFields().getAO() == null && OAString.isNotEmpty(cmd)) {
            cmd = OAString.field(cmd, ":", 2, 99).trim();
            if (OAString.isNotEmpty(field)) {
                cmd = OAString.convert(cmd, "prop", field);
            }
            field = cmd;
        }
        else {
            if (OAString.isNotEmpty(field)) field = "<%="+field+"%>";
        }
        
        if (!OAString.isEmpty(field)) {
            htmlEditor.insertString(field);
        }
    }
    
    public void onInsertTable() {
        TableDialog dlg = new TableDialog(getWindow());
        dlg.setVisible(true);
        if (dlg.succeeded()) {
            String tableHtml = dlg.getHTML();
            Element ep = editorDocument.getParagraphElement(htmlEditor.getSelectionStart());

            try {
                editorDocument.insertAfterEnd(ep, tableHtml);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            documentChanged();
        }
    }
    
    protected JColorChooser getColorChooser() {
        if (colorChooser == null) {
            colorChooser = new JColorChooser();
            AbstractColorChooserPanel[] pans = colorChooser.getChooserPanels();
            AbstractColorChooserPanel[] pans2 = new AbstractColorChooserPanel[pans.length+1];
            pans2[0] = new ColorChooser();
            for (int i=0; i < pans.length; i++) {
                pans2[i+1] = pans[i];
            }
            colorChooser.setChooserPanels(pans2);
        }
        return colorChooser;
    }
    
    
    public void onFontColorChooser() {
        Color c = getFontColorButton().getColor();
        if (c == null) removeAttribute(StyleConstants.Foreground, false);
        else {
            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, c);
            addAttributeSet(attr);
        }
    }
    
    public void onBackgroundColorChooser() {
        Color c = getBackgroundColorButton().getColor();
        if (c == null) removeAttribute(StyleConstants.Background, false);
        else {
            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setBackground(attr, c);
            addAttributeSet(attr);
        }
    }
    
    public HtmlSourceDialog getHtmlSourceDialog() {
        if (dlgHtmlSource == null) {
            dlgHtmlSource = new HtmlSourceDialog(getWindow());
            JTextArea ta = dlgHtmlSource.getTextArea();
            textControlSourceDlgText = new OATextController(ta, spellChecker, true);
        }
        return dlgHtmlSource;
    }
    
    
    public BlockController getBlockController() {
        if (controlBlock == null) {
            controlBlock = new BlockController(htmlEditor); 
        }
        return controlBlock;
    }
    
    public void onEditBlockCode() {
        getBlockController().getBlockDialog().setVisible(true);
    }

    
    public InsertController getInsertController() {
        if (controlInsert == null) {
            controlInsert = new InsertController(htmlEditor); 
        }
        return controlInsert;
    }
    public void onInsertDialog() {
        getInsertController().getInsertDialog().setVisible(true);
    }
    
    public void onEditSourceCode() {
        try {
/*qqq
            StringWriter sw = new StringWriter();
            int x = editor.editorDocument.getLength();
            editor.editorKit.write(sw, editor.editorDocument, 0, x);
            sw.close();
****/
            String s = htmlEditor.getText();

            getHtmlSourceDialog();

//          dlgHtmlSource.setSource(sw.toString());
            dlgHtmlSource.setSource(s);

            boolean b = htmlEditor != null && htmlEditor.isEnabled() && htmlEditor.isEditable();
            dlgHtmlSource.getSaveButton().setEnabled(b);
            
            dlgHtmlSource.setVisible(true);
            if (!dlgHtmlSource.succeeded()) return;

            
            
            final String src = dlgHtmlSource.getSource();

/*
            StringReader sr = new StringReader(dlgHtmlSource.getSource());
            editor.editorDocument = (EditorDocument) editor.editorKit.createDocument();
            // m_context = m_doc.getStyleSheet();
            editor.editorKit.read(sr, editor.editorDocument, 0);
            sr.close();
            editor.setDocument(editor.editorDocument);
*/
            htmlEditor.requestFocusInWindow();
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    htmlEditor.setText(src);
                    documentChanged();
                }
            });
        
        }
        catch (Exception ex) {
            showError(ex, "Error: "+ex);
        }
    }
    
    public JDialog getImageEditorDialog() {
        if (dlgImageEditor != null) return dlgImageEditor; 
        dlgImageEditor = new JDialog(getWindow(), ModalityType.APPLICATION_MODAL);
        dlgImageEditor.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dlgImageEditor.setLayout(new BorderLayout());
        dlgImageEditor.setTitle("Image Editor");
        
        JToolBar tool = getImagePanelController().getToolBar();
        dlgImageEditor.add(tool, BorderLayout.NORTH);
        getImagePanelController().getOAImagePanel().setBorder(new LineBorder(Color.lightGray, 1));
        dlgImageEditor.add(new JScrollPane(getImagePanelController().getOAImagePanel()), BorderLayout.CENTER);
        dlgImageEditor.setSize(500, 500);
        
        tool.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,false), "esc");
        tool.getActionMap().put("esc", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getImageEditorDialog().setVisible(false);
            }
        });
        
        
        return dlgImageEditor;
    }
    
    public OAImagePanelController getImagePanelController() {
        if (contImagePanel == null) {
            contImagePanel = new OAImagePanelController() {
                @Override
                public void onSave() {
                    // no-op, since image will be saved in document when it is returned from dialog
                }
            };
        }
        return contImagePanel;
    }

    /**
     * Edit the selected image (MyImageView) and then call OAHTMLTextPane.updateImage(src, img) 
     */
    protected void onEditImage() {
        MyImageView view = getSelectedImageView();
        if (view == null) return;
        
        if (htmlEditor == null || !htmlEditor.isEnabled() || !htmlEditor.isEditable()) return;
        
        Image img = view.getImage();
        getImagePanelController().setImage(img);
        
        getImageEditorDialog().setVisible(true); // modal
 
        img = getImagePanelController().getBufferedImage();
        view.setImage(img);
        
        AttributeSet aset; // = view.getAttributes();  this only has Style attributes, not html, ex: SRC
        aset = view.getElement().getAttributes();
        String srcName = (String) aset.getAttribute(HTML.Attribute.SRC);
        htmlEditor.updateImage(srcName, img);
        htmlEditor.repaint();
    }
    
    private long maxImageSize = 4 * 1024 * 1024;
    public void setMaxImageSize(long size) {
        maxImageSize = size;
    }
    public long getMaxImageSize() {
        return maxImageSize;
    }
    /**
     * Have user select a file and then call OAHTMLTextPane.insertImage(src, img) 
     */
    protected void onInsertImage() {
        JFileChooser fc = getImagePanelController().getImageFileChooserController().getOpenImageFileChooser();
        
        int x = fc.showOpenDialog(getWindow());
        if (x != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();

        if (file.length() > getMaxImageSize()) {
            JOptionPane.showMessageDialog(getWindow(), "Image file over "+getMaxImageSize()+", please use smaller size", "Open image", JOptionPane.WARNING_MESSAGE);
        }

        String fname = file.getPath();
        BufferedImage bi;
        try {
           bi = OAImageUtil.loadImage(file);
           // bi = ImageIO.read(file);

/*        
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
            /*
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
*/       
            htmlEditor.insertImage(fname, bi);
        }
        catch (Exception ex) {
            System.out.println("Exception: "+ex);
            ex.printStackTrace();
            JOptionPane.showMessageDialog(getWindow(), "Error reading image \n"+fname, "OAHTMLTextPaneController",JOptionPane.WARNING_MESSAGE);
            return;
        }
        htmlEditor.requestFocus();  // so that lostFocus will be called, and text will be saved
    }
    
    private InsertImageDialog dlgInsertImage;
    protected InsertImageDialog getInsertImageDialog() {
        if (dlgInsertImage == null) {
            JFileChooser fc = getImagePanelController().getImageFileChooserController().getOpenImageFileChooser();
            dlgInsertImage = new InsertImageDialog(SwingUtilities.windowForComponent(editor), fc);
        }
        return dlgInsertImage;
    }
  
    
    protected String getInsertImageDivHtmlForEditorKitCallback() {
        getInsertImageDialog().setVisible(true);
        if (getInsertImageDialog().wasCanceled()) return null;
        String dpi = getInsertImageDialog().getImageDpiTextField().getText();
        String fname = getInsertImageDialog().getFileNameTextField().getText();
        if (OAString.isEmpty(fname)) return null;
        File file = new File(fname);
        if (!file.exists()) return null;
        
        if (file.length() > getMaxImageSize()) {
            JOptionPane.showMessageDialog(getWindow(), "Image file size greater then max limit of "+getMaxImageSize()+", please use smaller size", "Open image", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        
        String srcName = file.getPath();
        BufferedImage img = null;
        try {
            img = OAImageUtil.loadImage(srcName);
        }
        catch (Exception ex) {
            System.out.println("Exception: "+ex);
            ex.printStackTrace();
            JOptionPane.showMessageDialog(getWindow(), "Error reading image \n"+srcName, "OAHTMLTextPaneController",JOptionPane.WARNING_MESSAGE);
            return null;
        }
        
        ImageHandlerInterface imageHandler = htmlEditor.getImageHandler();
        if (imageHandler != null) {
            String s;
            try {
                s = imageHandler.onInsertImage(srcName, img);
            }
            catch (Exception e) {
                return null;
            }
            if (s != null) srcName = s;
        }
        else if (srcName.indexOf("://") < 0) {
            // convert file name to url compatible
            srcName = srcName.replace('\\', '/');
            // srcName = "file:///" + srcName;
            /*
            * There is a problem with some jpeg files, where a jdk decoder is not found.
            *    images for background-image are retrieved by CSS.getImage, 
            *        which is different then <img> which are controlled by imageHandler
            *        
            * oaimage protocol will fix this by using OAImageUtil to load the image       
            */
            srcName = "oaimage:///" + srcName;
        }
        OAImageUtil.loadImage(img);
        int w = img.getWidth(null);
        int h = img.getHeight(null);


        if (!OAString.isEmpty(dpi) && OAString.isNumber(dpi)) {
            int dpiImage = OAConv.toInt(dpi);
            
            int dpiScreen = Toolkit.getDefaultToolkit().getScreenResolution();
            
            double d = dpiScreen / ((double)dpiImage); 
            w = (int) (w * d);
            h = (int) (h * d);
            srcName += "&h="+h+"&w="+w;  // scale image
        }
        
        String style = "background-image:url(\"" + srcName + "\"); background-repeat:no-repeat;";
        style += " height:" + h + "; width:" + w + ";";  // set div size
        
        return "<div style='"+style+"'></div>";
    }

    
    // Editor support ==================================================

    protected void addAttributeSet(AttributeSet attr) {
        addAttributeSet(attr, false);
    }

    protected void addAttributeSet(AttributeSet attr, boolean bParagraph) {
        if (bUpdatingAttributes) return;

        int xStart = htmlEditor.getSelectionStart();
        int xFinish = htmlEditor.getSelectionEnd();

        if (bParagraph) {
            editorDocument.setParagraphAttributes(xStart,xFinish - xStart, attr, false);
        }
        else if (xStart != xFinish) {
            editorDocument.setCharacterAttributes(xStart, xFinish - xStart, attr, false);
        }
        else {
            MutableAttributeSet inputAttributes = editorKit.getInputAttributes();
            inputAttributes.addAttributes(attr);
        }
    }

    protected void removeAttribute(Object name, boolean bParagraph) {
        int xStart = htmlEditor.getSelectionStart();
        int xFinish = htmlEditor.getSelectionEnd();
    
        if (bParagraph) {
            AttributeSet as = htmlEditor.getParagraphAttributes();
            SimpleAttributeSet sas = new SimpleAttributeSet(as);
            sas.removeAttribute(name);
            htmlEditor.setParagraphAttributes(sas, true);
        }
        else if (xStart != xFinish) {
            AttributeSet ats = htmlEditor.getCharacterAttributes();
            SimpleAttributeSet sas = new SimpleAttributeSet(ats);
            sas.removeAttribute(name);
            htmlEditor.setCharacterAttributes(sas, true);
        }
        else {
            MutableAttributeSet inputAttributes = editorKit.getInputAttributes();
            inputAttributes.removeAttribute(name);
        }
    }
    


    protected AttributeSet getCurrentDocAttributeSet() {
        int xStart = htmlEditor.getSelectionStart();
        if (xStart == 0) xStart = 1;
        int xFinish = htmlEditor.getSelectionEnd();

        AttributeSet as;
        if (xStart != xFinish) {
            // int pos = htmlEditor.getCaret().getDot();
            as = editorDocument.getCharacterElement(xStart+1).getAttributes();
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
    protected View getCurrentView() {
        int pos = htmlEditor.getSelectionStart();
        if (pos == 0) pos = 1;  // 0 = first paragraph
        if (pos < 0) return null;
        return getView(pos);
    }

    protected View getView(int pos) {
        View view = htmlEditor.getUI().getRootView(htmlEditor);
        if (view == null) return null;

        Element ele = editorDocument.getParagraphElement(pos);
        boolean bParagraphStart = (ele.getStartOffset() == pos);

        for (int i=0; i < view.getViewCount(); i++) {
            View child = view.getView(i);
            if (child == null) continue;
            int p1 = child.getStartOffset();
            int p2 = child.getEndOffset();
            if (pos >= p1 && pos <= p2) {
                if (pos != p2 || !bParagraphStart) {
                    view = child;
                    i = -1;
                }
            }
        }
        return view;
    }
    protected AttributeSet getCurrentViewAttributeSet() {
        AttributeSet as = getCurrentView().getAttributes();
        return as;
    }
    
    protected void updateAttributeCommands() {
        if (SwingUtilities.isEventDispatchThread()) {
            _updateAttributeCommands();
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        _updateAttributeCommands();
                    }
                    catch (Exception e) {
                    }
                }
            });
        }
    }    
    protected void _updateAttributeCommands() {
        if (bUpdatingAttributes) return;
        bUpdatingAttributes = true;
        if (htmlEditor == null || !htmlEditor.isEnabled()) {
            getBoldToggleButton().setSelected(false);
            getItalicToggleButton().setSelected(false);
            getUnderlineToggleButton().setSelected(false);
            getFontNameComboBox().setSelectedIndex(-1);
            getFontSizeComboBox().setSelectedIndex(-1);
            getFontSizeTextField().setText("");
        }
        else {
            AttributeSet asDoc = getCurrentDocAttributeSet();
            Font fontDoc = ((HTMLDocument)htmlEditor.getDocument()).getFont(asDoc);//qqqqqqqqtttttttt
            getBoldToggleButton().setSelected(fontDoc.isBold());
            getItalicToggleButton().setSelected(fontDoc.isItalic());
            getUnderlineToggleButton().setSelected(StyleConstants.isUnderline(asDoc));

            AttributeSet asView = getCurrentViewAttributeSet();
            if (asView == null) return;
            Font fontView = ((HTMLDocument)htmlEditor.getDocument()).getFont(asView);
/*
System.out.println("\npos="+htmlEditor.getCaretPosition());//qqqqqqqqq
System.out.println("Doc Font="+fontDoc);//qqqqqqqqq
System.out.println("View Font="+fontView);//qqqqqqqqq
*/
            int fontSize = fontDoc.getSize();
            String fontName = fontDoc.getFamily();
            
            String s = fontDoc.toString();
            
            if (s.indexOf("family=SansSerif,name=SansSerif") >= 0) {  // hack: input attributes default if not set
                fontName = fontView.getFamily();
                if (s.indexOf("size=12") >= 0) {  
                    fontSize = fontView.getSize();
                }
            }
            
//  if (s.equalsIgnoreCase("java.awt.Font[family=SansSerif,name=SansSerif,style=plain,size=12]")) {  // hack, inputAttributes does not have font set correctly

            getFontNameComboBox().setSelectedItem(fontName);

            fontSize = (int) Math.round(OAPrintUtil.convertPixelsToPoints(fontSize));
            
            // 20121112
            getFontSizeTextField().setText(fontSize+"pt");

            int[] sizes = fontSizes;            
            int pos = 0;
            for ( ; pos<sizes.length; pos++) {
                if (sizes[pos] >= fontSize) break;
            }
            try {
                getFontSizeComboBox().setSelectedIndex(pos);
            }
            catch (Exception e) {}

            // set the "current color" for the fore/back ground colors
            getFontColorButton().setCurrentColor(StyleConstants.getForeground(asView));
            getBackgroundColorButton().setCurrentColor(StyleConstants.getBackground(asView));
            
            // see if current pos is a link
            AttributeSet anchor = (AttributeSet) asDoc.getAttribute(HTML.Tag.A);
            if (anchor != null) {
                currentLinkSource = (String) anchor.getAttribute(HTML.Attribute.HREF);
            }
            else currentLinkSource = null;

            getFollowLinkMenuItem().setEnabled(currentLinkSource != null);
            getPopupFollowLinkMenuItem().setEnabled(currentLinkSource != null);
            
            s = currentLinkSource == null ? "" : "go to link " + currentLinkSource;
            getPopupFollowLinkMenuItem().setToolTipText(s);
            getFollowLinkMenuItem().setToolTipText(s);
            
            
            AttributeSet as = this.htmlEditor.getParagraphAttributes();
            fontSize = StyleConstants.getAlignment(as);
            getLeftToggleButton().setSelected( (fontSize == StyleConstants.ALIGN_LEFT) );
            getCenterToggleButton().setSelected( (fontSize == StyleConstants.ALIGN_CENTER) );
            getRightToggleButton().setSelected( (fontSize == StyleConstants.ALIGN_RIGHT) );
            // getJustifiedToggleButton().setSelected( (x == StyleConstants.ALIGN_JUSTIFIED) );

            // getFontColorMenu().setColor(htmlEditor.getForeground());
            // getBackgroundColorMenu().setColor(htmlEditor.getBackground());
        }


        
        // see if image is selected
        int pos = htmlEditor==null ? -1 : htmlEditor.getSelectionStart();
        // if (pos == 0) pos = 1;  // 0 = first paragraph   20100928 removed because the doc might not have a paragraph (especially after using ^a+del)
        View view = getView(pos+1);  // view for images is after the cursor position
        boolean b;
        if (view instanceof MyImageView) {
            myImageView = (MyImageView) view;
            b = true;
        }
        else b = false;
        getPopupEditImageMenuItem().setEnabled(b);
        getEditImageMenuItem().setEnabled(b);
        getEditImageButton().setEnabled(b);
        
        bUpdatingAttributes = false;
    }

    public MyImageView getSelectedImageView() {
        return myImageView;
    }

    public void showError(Exception ex, String message) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(getWindow(), message, "Editor", JOptionPane.WARNING_MESSAGE);
    }


    JFileChooser chooser;
    protected String inputURL(String prompt, String initialValue) {
        if (chooser == null) chooser = new JFileChooser();
        if (chooser.showOpenDialog(getWindow()) != JFileChooser.APPROVE_OPTION) return null;

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
        if (chooser.showOpenDialog(getWindow()) != JFileChooser.APPROVE_OPTION) return null;
        return chooser.getSelectedFile();
    }


    public void documentChanged() {
        htmlEditor.revalidate();
        htmlEditor.repaint();
        updateAttributeCommands();
    }


    public void setSelection(int xStart, int xFinish, boolean moveUp) {
        if (htmlEditor != null) {
            if (moveUp) {
                htmlEditor.setCaretPosition(xFinish);
                htmlEditor.moveCaretPosition(xStart);
            }
            else htmlEditor.select(xStart, xFinish);
        }
    }
    
    
    /**
     * find the first occurrence of an <code>Element</code> in the
     * element tree above a given <code>Element</code>
     *
     * @param name the name of the <code>Element</code> to search for
     *
     * @return the found <code>Element</code> or null if none is found
     */
    public static Element findElementUp(String name, Element start) {
      Element elem = start;
      while((elem != null) && (!elem.getName().equalsIgnoreCase(name))) {
        elem = elem.getParentElement();
      }
      return elem;
    }

    /**
     * find the first occurrence of an <code>Element</code> in the
     * element tree below a given <code>Element</code>
     *
     * @param name the name of the <code>Element</code> to search for
     * @param parent the <code>Element</code> to start looking
     *
     * @return the found <code>Element</code> or null if none is found
     */
    public static Element findElementDown(String name, Element parent) {
      Element foundElement = null;
      ElementIterator eli = new ElementIterator(parent);
      Element thisElement = eli.first();
      while(thisElement != null && foundElement == null) {
        if(thisElement.getName().equalsIgnoreCase(name)) {
          foundElement = thisElement;
        }
        thisElement = eli.next();
      }
      return foundElement;
    }

    
}

class FontNameComboBoxCellRenderer implements ListCellRenderer {
    JLabel lbl1 = new JLabel("XXX");
    JLabel lbl2 = new JLabel("");
    JLabel lbl0 = new JLabel("");
    JComponent comp;
    int genericFontCount;
    Border border1, border2, border3;
 
    public FontNameComboBoxCellRenderer(int genericFontCount, int maxFontNameWidth) {
        this.genericFontCount = genericFontCount;
        lbl1.setOpaque(false);
        lbl2.setOpaque(false);
        comp = new JPanel();
        comp.setBorder(new EmptyBorder(2,4,2,4));
        comp.setOpaque(true);
        comp.setLayout(new GridLayout(1,2,0,0));
        
        border1 = new EmptyBorder(0,0,2,0);
        border2 = new EmptyBorder(0,4,2,0);
        
        border3 = new CustomLineBorder(0, 0, 2, 0, Color.BLACK);
        border3 = new CompoundBorder(border3, new EmptyBorder(0, 4, 0, 0));
        
        comp.add(lbl1);
        comp.add(lbl2);

        
        Dimension d = lbl1.getPreferredSize();
        d.width = maxFontNameWidth * 2;
        d.height += 4;
        comp.setMaximumSize(d);
        comp.setMinimumSize(d);
        comp.setPreferredSize(d);
    }
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
//System.out.println("-->index="+index+", isSelected="+isSelected+", cellHasFocus="+cellHasFocus+", value="+value);
        
        if (value instanceof String) {
            try {
                value = new String( ((String) value).getBytes(), "UTF-8");               
            }
            catch (Exception e) {}
        }
        
        
        if (index < 0) {
            if (value == null) lbl0.setText("");
            else {
                lbl0.setText(value.toString());
            }
            lbl2.setText("");
            return lbl0;
            //return comp;
        }

        Color fg, bg; 
        if (isSelected) {
            bg = list.getSelectionBackground();
            fg = list.getSelectionForeground();
        }
        else {
            bg = list.getBackground();
            fg = list.getForeground();
        }
        comp.setBackground(bg);
        lbl1.setForeground(fg);
        lbl2.setForeground(fg);
        
        lbl1.setText(value.toString());
        
        lbl2.setFont(new Font((String) value, 0, 12));
        lbl2.setText(value.toString());
        
        if (index < genericFontCount) {
            if (index+1 == genericFontCount) {
                lbl1.setBorder(border3);
                lbl2.setBorder(border3);
            }
            else {
                lbl1.setBorder(border2);
                lbl2.setBorder(border2);
            }
        }
        else {
            lbl1.setBorder(border1);
            lbl2.setBorder(border1);
        }
        
        return comp;
    }
}

