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
package com.viaoa.jfc.text.autocomplete;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.viaoa.util.OAString;

/**
 * Base class for building an autocomplete lookup.
 * 
 * This class does not know the component used in the popup, so it will manage the textField keys to call
 * abstract methods for changing the selected value.
 * 
 * The only selection that is captured is when the popup menu is displayed and [enter] is used - see: onSelection() 
 * 
 * 
 */
public abstract class AutoCompleteBase { 
    protected JTextField textComp; 
    protected JComponent popupComponent; 
    protected JPopupMenu popup; 
    protected boolean bExactMatchOnly;
    protected JScrollPane scroll;
    protected boolean bShowOne;  // show popup, even if only 1 value to display
    protected boolean bIgnorePopup;
    
    
    /**
     * Create an autoComplete that uses a text and popup component. 
     * @param txt user input component that is used to work with popupMenu.
     * @param popupComponent list/table/etc that is displayed with list of choices
     * @param bExactMatchOnly if true, then closest match is always selected.
     */
    public AutoCompleteBase(JTextField txt, JComponent popupComponent, boolean bExactMatchOnly){ 
        this.textComp = txt;
        this.popupComponent = popupComponent;
        this.bExactMatchOnly = bExactMatchOnly;

        textComp.addKeyListener(new KeyAdapter() {
            boolean bIgnore = false;
            @Override
            public void keyPressed(KeyEvent e) {
                bIgnore = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (popup.isVisible()) {
                            onDownArrow();
                            bIgnore = true;
                        }
                        else {
                            showPopup();
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (popup.isVisible()) {
                            onUpArrow();
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        if (popup.isVisible()) {
                            onPageDown();
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        if (popup.isVisible()) {
                            onPageUp();
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (popup.isVisible()) {
                            popup.setVisible(false);
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        if (popup.isVisible()) {
                            bIgnorePopup = true;
                            if (onSelection()) {
                                popup.setVisible(false);
                            }
                            bIgnorePopup = false;
                        }
                        else {
                            if (textComp != null && OAString.isEmpty(textComp.getText())) {
                                onSelection();
                            }
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        if (e.getModifiers() == KeyEvent.CTRL_DOWN_MASK) {
                            showPopup();
                            bIgnore = true;
                        }
                        break;
                }
                if (!bIgnore) super.keyPressed(e);
                else e.consume();  // so that parent table does not use it
            }  
        });
        
        textComp.getDocument().addDocumentListener(
            new DocumentListener() {
                public void insertUpdate(DocumentEvent e){
                    if (!bIgnorePopup && textComp.hasFocus()) {
                        showPopup(e.getOffset()+e.getLength()); 
                    }
                } 
                public void removeUpdate(DocumentEvent e){
                    if (!bIgnorePopup && textComp.hasFocus()) {
                        showPopup(e.getOffset());
                    }
                } 
                public void changedUpdate(DocumentEvent e) {
                    if (!bIgnorePopup && textComp.hasFocus()) {
                        showPopup(e.getOffset());
                    }
                } 
        }); 
                    
        
        textComp.addCaretListener(new CaretListener() {
        	@Override
        	public void caretUpdate(CaretEvent e) {
                if (textComp.isEnabled() && textComp.hasFocus()) { 
                    showPopup(); 
                } 
        	}
        });

       	if (bExactMatchOnly) {
       		AbstractDocument doc = (AbstractDocument) textComp.getDocument();
       		doc.setDocumentFilter(new DocumentExactMatchFilter());       		
       	}
        
        textComp.addFocusListener(new FocusAdapter() {
        	@Override
        	public void focusGained(FocusEvent e) {
        		super.focusGained(e);
    			if (!popup.isVisible()) showPopup();
        	}
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (popup.isVisible()) popup.setVisible(false);
            }
        });
        
        scroll = new JScrollPane(popupComponent); 
        scroll.setBorder(null); 
        scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false ); 
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
        popup = new JPopupMenu();
        popup.setFocusable(false); 
        popup.setRequestFocusEnabled(false);
        popup.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2), BorderFactory.createLineBorder(Color.black)) ); 
        popup.add(scroll);
    } 

    /** show popup, even if only 1 value to display
     */
    public void setShowOne(boolean b) {
        this.bShowOne = b;
    }
    public boolean getShowOne() {
        return this.bShowOne;
    }
 
    
    private void showPopup() {
        if (!textComp.isEnabled()) return;
        showPopup(textComp.getCaretPosition());
    }
    
    protected void showPopup(int offset) {
        if (bIgnorePopup) return;
        if (popup.isVisible()) popup.setVisible(false); 
        if (!textComp.isEnabled()) return;

        searchAndDisplay(textComp.getText(), offset);
        /**qqqqqqqq was
        Dimension d = updateSelectionList(textComp.getText(), offset);
        if (d == null) return; // dont show
        d.width += 7;  // include popup borders
        d.width = Math.max(textComp.getSize().width-6, d.width);
        
        d.height += 7; // include popup borders
        popup.setPopupSize(d);
        
        try {
            popup.show(textComp, 3, textComp.getHeight());
        }
        catch (Exception e) {
            
        }
        textComp.requestFocusInWindow();
        */         
    } 
 
 


    class DocumentExactMatchFilter extends DocumentFilter {
    	@Override
    	public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    		// p("insert(offset="+offset+", text="+text);//qqqqqq
    		
			Document doc = fb.getDocument();
			int lenDoc = doc.getLength();
			String updatedText = doc.getText(0, lenDoc);
			StringBuffer sb = new StringBuffer(updatedText);
			sb.insert(offset, text);
			updatedText = sb.toString();
    		
			// try to match the whole document/text
			boolean bFound = false;
			String newText = getClosestMatch(updatedText);
			if (newText != null) {
				if (newText.equals(updatedText)) super.insertString(fb, offset, text, attr);
				else fb.replace(offset, lenDoc-offset, newText.substring(offset), attr);
				bFound = true;
			}
			else {
				// try to match the from 0 to offset + text
				updatedText = updatedText.substring(0, offset);
				newText = getClosestMatch(updatedText);
				if (newText != null) {
					fb.replace(offset, lenDoc-offset, newText.substring(offset), null);
					bFound = true;
				}
			}
			if (bFound) {
				int x = textComp.getCaretPosition();
				textComp.setCaretPosition(offset+text.length()); 
			}
			// else ignore, no matches for text change
    	}
    	   
    	@Override
    	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
    		// p("replace(offset="+offset+", length="+length+", text="+text);//qqqqqq
			if (text == null) text = "";
    	    Document doc = fb.getDocument();
			int lenDoc = doc.getLength();
			String updatedText = doc.getText(0, lenDoc);
			StringBuffer sb = new StringBuffer(updatedText);
			sb.replace(offset, offset+length, text);
			updatedText = sb.toString();

			// try to match the whole doc
            int posCaret=-1;
			String newText = getClosestMatch(updatedText);
			if (newText != null) {
				if (newText.equals(updatedText)) {
				    super.replace(fb, offset, length, text, attr);
				    posCaret = offset+text.length();
				}
				else {
				    super.replace(fb, offset, lenDoc-offset, newText.substring(offset), attr);
                    posCaret = offset+text.length();
				}
			}
			else {
				// try to match the from 0 to offset + text
				updatedText = updatedText.substring(0, offset+text.length());
				newText = getClosestMatch(updatedText);
				if (newText != null) {
					super.replace(fb, offset, lenDoc-offset, newText.substring(offset), attr);
					posCaret = offset+text.length();
				}
				else {
				    if (text.length() == 0) {
	                    super.replace(fb, 0, lenDoc, "", attr);
	                    posCaret = 0;
				    }
				    // else change is not allowed, it will be ignored
				}
			}
			if (posCaret>=0) {
				textComp.setCaretPosition(posCaret); 
			}
			// else ignore, no matches for text change
    	}
    	
    	@Override
    	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    		// p("remove(offset="+offset+", length="+length);//qqqqqq
			
			Document doc = fb.getDocument();
			int lenDoc = doc.getLength();
			String updatedText = doc.getText(0, lenDoc);
			StringBuffer sb = new StringBuffer(updatedText);
			sb.delete(offset, offset+length);
			updatedText = sb.toString();

			if (updatedText.length() == 0 || offset == 0) {
				super.remove(fb, offset, lenDoc);
				return;
			}
			
			// try to match the whole doc
			boolean bFound = false;
			String newText = getClosestMatch(updatedText);
			if (newText != null) {
				if (newText.equals(updatedText)) super.remove(fb, offset, length);
				else {
					fb.replace(offset, lenDoc-offset, newText.substring(offset), null);
				}
				bFound = true;
			}
			else {
				// try to match the from 0 to offset + text
				updatedText = updatedText.substring(0, offset);
				newText = getClosestMatch(updatedText);
				if (newText != null) {
					fb.replace(offset, lenDoc-offset, newText.substring(offset), null);
					bFound = true;
				}
			}
			if (bFound) {
				int x = textComp.getCaretPosition();
				textComp.setCaretPosition(offset); 
			}
			// else ignore, no matches for text change
    	}
    	
    	int q = 0;
    	void p(String s) {
    		System.out.println((q++)+" "+s);
    	}
    }

    /**
     * Called when text has changed and list needs to be updated.
     * @param offset caret position within the textField 
     * @return perferred size to make the popupMenu
     */
    protected abstract Dimension updateSelectionList(String text, int offset);

    protected abstract void onDownArrow();
    protected abstract void onUpArrow();
    protected abstract void onPageDown();
    protected abstract void onPageUp();

    /**
     * Used for exactMatches, to find the closest match to text.
     */
    protected abstract String getClosestMatch(String value);
    
    /**
     * Called when user has pressed the "Enter" key, while the popup is visible.
     * The popup component needs to replace the textField text with the selected value from the popup component.
     * @return true if something was selected and popup can be closed.
     */
    protected abstract boolean onSelection();


    // 20140408 use swingworker for search and display
    private AtomicInteger aiCnt = new AtomicInteger();
    protected void searchAndDisplay(final String search, final int offset) {
        if (!textComp.isEnabled()) {
            return;
        }
        if (!textComp.isEditable()) {
            return;
        }
        
        final int cnt = aiCnt.incrementAndGet();
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            volatile Dimension dim;
            @Override
            protected Void doInBackground() throws Exception {
                for (int i=0; i<80; i++) {
                    if (cnt != aiCnt.get()) return null;
                    try {
                        Thread.sleep(1);
                    }
                    catch (Exception e) {
                    }
                }
                try {
                    dim = updateSelectionList(textComp.getText(), offset);
                }
                catch (Exception e) {
                    System.out.println("Error in AutoCompleteBase, calling updateSelectionList, ex="+e);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (dim == null) return; // dont show
                
                for (int i=0; i<55; i++) {
                    if (cnt != aiCnt.get()) return;
                    try {
                        Thread.sleep(1);
                    }
                    catch (Exception e) {
                    }
                }
                
                dim.width += 7;  // include popup borders
                dim.width = Math.max(textComp.getSize().width-6, dim.width);
                
                dim.height += 7; // include popup borders
                popup.setPopupSize(dim);
                
                try {
                    Point pt1 = textComp.getLocationOnScreen();
                    popup.show(textComp, 3, textComp.getHeight());
                    Point pt2 = popup.getLocationOnScreen();
                    if (pt1.y > pt2.y) {  // if popup is above (and overlapping) the component
                        Dimension d = popup.getSize();
                        int y = pt2.y - (d.height - (pt1.y-pt2.y)) ;
                        popup.setLocation(pt2.x, y);
                    }
                }
                catch (Exception e) {
                }
                textComp.requestFocusInWindow(); 
            }
        };
        sw.execute();
    }

}


