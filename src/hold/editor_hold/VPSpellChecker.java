package com.viaoa.scheduler.editor;


import com.viaoa.hub.*;
import com.viaoa.ds.*;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.*;
import com.vetplan.*;

public class VPSpellChecker extends SpellChecker {
	protected static String SELECT_QUERY = "word = ";
	protected static String SOUNDEX_QUERY = "soundex = ";
	
	static final String[] skipWords = new String[] { 
	    "a", "an", "and", "is", "was", "they", "he", "she", 
	    "it" 
	};
	
    Hub hub;

	public VPSpellChecker(JFrame frm, Editor ed) {
	    super(frm, ed);
	    hub = new Hub(Dictionary.class);
    }

    public void addNewWord(String word) {
        if (word == null) return;
        word = com.dispatcher.Dictionary.convertWord(word);

		try {
    		hub.select(SELECT_QUERY+"'"+word+"'");
    	    com.dispatcher.Dictionary dict = (com.dispatcher.Dictionary) hub.elementAt(0);
		    if (dict == null) {
		        dict = (Dictionary) OAObjectReflectDelegate.createNewObject(Dictionary.class);
    		    dict.setWord(word);
    		    dict.save();
    		}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("SpellChecker error: "+ex.toString());
		}
    }

	protected void replaceSelection(String word, String replacement) {
	    super.replaceSelection(word, replacement);
        try {
            word = com.dispatcher.Dictionary.convertWord(word);
    		hub.select(SELECT_QUERY+"'"+word+"'");
    	    com.dispatcher.Dictionary dict = (com.dispatcher.Dictionary) hub.elementAt(0);
            
            replacement = com.dispatcher.Dictionary.convertWord(replacement);
    		hub.select(SELECT_QUERY+"'"+replacement+"'");
    	    com.dispatcher.Dictionary dict2 = (com.dispatcher.Dictionary) hub.elementAt(0);

    	    if (dict2 == null) {
	    	    dict2 = new com.dispatcher.Dictionary();
	    	    dict2.setWord(replacement);
	    	    dict2.save();
    	    }
    	    
    	    if (dict == null) {
	    	    dict = new com.dispatcher.Dictionary();
	    	    dict.setWord(word);
            }
	    	dict.setValidDictionary(dict2);
	    	dict.save();
	    }
	    catch (Exception e) {
	        System.out.println("Error: "+e);
	        e.printStackTrace();
	    }
    
    }

    public boolean isWordFound(String word) {
        if (word == null) return true;
        word = word.trim();
        if (word.length() < 2) return true;
		word = word.toLowerCase();
		for (int i=0; i<skipWords.length; i++) if (word.equals(skipWords[i])) return true;

        String orig = word;
        word = com.dispatcher.Dictionary.convertWord(word);

        try {
    		hub.select(SELECT_QUERY+"'"+word+"'");
    	    com.dispatcher.Dictionary dict = (com.vetplan.Dictionary) hub.elementAt(0);
    	    if (dict == null) return false;
	    	// could be an invalid word
    	    if (!dict.getValid()) return false;
            if (dict.getValidDictionary() == null) return true;
    	    return false;
	    }
	    catch (Exception e) {
	        System.out.println("Error: "+e);
	    }
	    return false;
    }

    public String[] getSuggestions(String word) {
        if (word == null) return null;

        String orig = word;
        word = com.dispatcher.Dictionary.convertWord(word);
    	
    	hub.select(SOUNDEX_QUERY+"'"+OAString.soundex(word)+"'");
    	hub.loadAllData();
		String[] ss = new String[hub.getSize()];
		for (int i=0;;i++) {
			Dictionary dict = (Dictionary) hub.elementAt(i);
			if (dict == null) break;
			String s = dict.getWord();
			int pos = i;
    	    if (dict.getValidDictionary() != null) {
			    if (s != null && s.equals(word)) {
			        ss[pos] = ss[0];
			        pos = 0; // put at top of list
			    }
    	        dict = dict.getValidDictionary();
    	    }
    		ss[pos] = com.dispatcher.Dictionary.convertWord(orig, dict);
		}
		return ss;
    }
}
