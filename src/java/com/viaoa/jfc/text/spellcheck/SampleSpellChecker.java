package com.viaoa.jfc.text.spellcheck;

import javax.swing.JFrame;

import com.viaoa.jfc.editor.html.*;

public class SampleSpellChecker extends SpellChecker {

	public SampleSpellChecker() {
    }
	
	@Override
	public void addNewWord(String word) {
		// TODO Auto-generated method stub

	}

    @Override
    public String[] getMatches(String text) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String[] getSoundexMatches(String text) {
        // TODO Auto-generated method stub
        return null;
    }
    
	@Override
	public boolean isWordFound(String word) {
		// TODO Auto-generated method stub
		return false;
	}

}
