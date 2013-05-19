package com.viaoa.jfc.text.spellcheck;

import java.util.*;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import com.viaoa.jfc.text.view.SpellCheckDialog;
import com.viaoa.util.*;

/**
 * USed to add a spellchecker to a text component.
 * @author vvia
 *
 */
public abstract class SpellChecker {
	protected static String SELECT_QUERY = "SELECT Data.word FROM Data WHERE Data.word = ";
	protected static String SOUNDEX_QUERY = "SELECT Data.word FROM Data WHERE Data.soundex = ";

	protected DocumentTokenizer m_tokenizer;
	protected Hashtable	hashIgnore;
	protected SpellCheckDialog dlgSpellCheck;
	
	protected JTextComponent editor;

	public SpellChecker() {
    }
    
	private JFrame getFrame() {
        JFrame frm = null;
        if (editor != null) {
            Object obj = SwingUtilities.getWindowAncestor(editor);
            if (obj instanceof JFrame) {
                frm = (JFrame) obj;
            }
        }
        return frm;
	}
	
	public SpellCheckDialog getSpellCheckDialog() {
	    if (dlgSpellCheck == null) {
	        dlgSpellCheck = new SpellCheckDialog(getFrame(), this);
	    }
        return dlgSpellCheck;
	}
	
	
	private int xStart;
	private int xFinish;

	public void spellCheck(JTextComponent editor) {
        this.editor = editor;
		hashIgnore = new Hashtable();
		boolean bFlag = true;
		try {
		    editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Document doc = editor.getDocument();
			int pos = editor.getCaretPosition();
			pos = 0; // 20101107
			m_tokenizer = new DocumentTokenizer(doc, pos);
		    String word, wordLowCase;

			
		    while (m_tokenizer.hasMoreTokens()) {
				word = m_tokenizer.nextToken();
                xStart = m_tokenizer.getStartPos();
                xFinish = m_tokenizer.getEndPos();
                if (xStart > xFinish) xFinish = xStart + word.length();
                
				if (word.equals(word.toUpperCase())) continue;
				if (word.length()<=1) continue;
				if (OAString.hasDigits(word)) continue;
				wordLowCase = word.toLowerCase();
				if (hashIgnore.get(wordLowCase) != null) continue;
                
                if (isWordFound(wordLowCase)) continue;
                
				editor.select(xStart, xFinish);
				
				if (!getSpellCheckDialog().suggest(word, getSuggestions(word), hashIgnore)) {
				    bFlag = false;
					break;
				}
			}
			editor.setCaretPosition(pos);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("SpellChecker error: "+ex.toString());
		}

		if (bFlag) JOptionPane.showMessageDialog(getFrame(), "The spelling check is complete", "",JOptionPane.INFORMATION_MESSAGE);
		editor.setCursor(Cursor.getDefaultCursor());
	}


	public void replaceSelection(String origWord, String replacement) {
//		int xStart = m_tokenizer.getStartPos();
//		int xFinish = m_tokenizer.getEndPos();

		editor.select(xStart, xFinish);
		editor.replaceSelection(replacement);
		xFinish = xStart+replacement.length();
		editor.select(xStart, xFinish);
		m_tokenizer.setPosition(xFinish);
	}

    
    private String[] getSuggestions(String word) {
        String[] ss = getMatches(word);
        ss = (String[]) OAArray.add(String.class, ss, getSoundexMatches(word));
        return ss;
    }

    
    public abstract void addNewWord(String word);

    public abstract boolean isWordFound(String word);
    
    /**
     * This should be overwritten to supply the words that start with the string in text.
     */
    public abstract String[] getMatches(String text);

    /**
     * This should be overwritten to supply the words that are Soundex the text.
     */
    public abstract String[] getSoundexMatches(String text);
    
    
}


class DocumentTokenizer
{
	protected Document m_doc;
	protected Segment	m_seg;
	protected int m_startPos;
	protected int m_endPos;
	protected int m_currentPos;
	protected boolean bSkipToFirst;  // go to first word 

	public DocumentTokenizer(Document doc, int offset) {
		m_doc = doc;
		m_seg = new Segment();
		setPosition(offset);
	}

	public boolean hasMoreTokens() {
		return (m_currentPos < m_doc.getLength());
	}

	public static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
		'\r', '\f', '.', ',', ':', '-', ';', '(', ')', '[', ']', '{',
		'}', '<', '>', '/', '|', '\\', '\'', '\"', '?', '!', ((char)160) };

	public static boolean isSeparator(char ch) {
		for (int k=0; k<WORD_SEPARATORS.length; k++)
			if (ch == WORD_SEPARATORS[k])
				return true;
		return false;
	}

	public String nextToken() {
		StringBuffer s = new StringBuffer();
		try {
			// Trim leading separators
			while (hasMoreTokens()) {
				m_doc.getText(m_currentPos, 1, m_seg);
				char ch = m_seg.array[m_seg.offset];
				if (!isSeparator(ch)) {
					m_startPos = m_currentPos;
					break;
				}
			    bSkipToFirst = false;
				m_currentPos++;
			}

			// Append characters
			while (hasMoreTokens()) {
				m_doc.getText(m_currentPos, 1, m_seg);
				char ch = m_seg.array[m_seg.offset];
				if (isSeparator(ch)) {
					m_endPos = m_currentPos;
					break;
				}
				s.append(ch);
				m_currentPos++;
			}
            if (bSkipToFirst) {
                bSkipToFirst = false;
                return nextToken();
            }
		}
		catch (BadLocationException ex) {
			System.err.println("nextToken: "+ex.toString());
			m_currentPos = m_doc.getLength();
		}
		return s.toString();
	}

	public int getStartPos() { return m_startPos; }

	public int getEndPos() { return m_endPos; }

	public void setPosition(int pos) {
	    if (pos != 0) {
	        pos--;
	        bSkipToFirst = true;
	    }
	    
		m_startPos = pos;
		m_endPos = pos;
		m_currentPos = pos;
	}
}


