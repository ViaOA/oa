package com.dispatcher.editor;

import javax.swing.JFrame;

public class SampleSpellChecker extends SpellChecker {

	public SampleSpellChecker(JFrame frm, Editor ed) {
	    super(frm, ed);
    }
	
	@Override
	public void addNewWord(String word) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getSuggestions(String word) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWordFound(String word) {
		// TODO Auto-generated method stub
		return false;
	}

}
