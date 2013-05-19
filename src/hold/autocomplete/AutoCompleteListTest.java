package com.viaoa.jfc.textfield;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

public class AutoCompleteListTest extends AutoCompleteList {
	
	private ArrayList<String> list;
    private static final String[] EmptyString = new String[0];

    public AutoCompleteListTest(ArrayList list, JTextField txt, JList jlist, boolean bExactMatchOnly){ 
        super(txt, jlist, bExactMatchOnly); 
        this.list = list;
    } 

    @Override
    protected String getClosetMatch(String value) {
    	if (list == null || value.length() == 0) return null;
    	for (String s : list) {
    		int x = value.compareTo(s);
    		if (x == 0) return value;
    		boolean b = s.startsWith(value);
    		if (b) return s;
    		if (x < 0) break;
    	}
    	return null;
    }
    
    
    @Override
    protected String[] getSearchData(String value, int offset) {
    	if (list == null || value.length() == 0) return EmptyString;
    	if (offset > value.length()) offset = value.length();
    	if (offset == 0) value = "";
    	else {
    		value = value.substring(0, offset);
    	}
    	if (value.length() == 0) return EmptyString;

    	int cnt = 0;
    	for (String s : list) {
    		if (s.startsWith(value)) cnt++;
    	}
    	String[] ss = new String[cnt];
    	int x = 0;
    	for (String s : list) {
    		if (s.startsWith(value)) ss[x++] = s;
    	}
    	return ss;
    } 
    
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i=0; i<26; i++) {
            char ch = (char) ('A' + i);
            for (int ii=0; ii<26; ii++) {
                char ch2 = (char) ('A' + ii);
                for (int j=0; j<5; j++) {
                    char chx = (char) ('A' + (26*Math.random()));
                    for (int k=0; k<3; k++) {
                        list.add(ch+""+ch2+""+chx+"Test."+k);
                    }
                }
            }
        }
        Collections.sort(list);

        JFrame frm = new JFrame();
		frm.setSize(new Dimension(500,400));
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setLayout(new FlowLayout());
		
		for (int i=0; i<3; i++) {
    		JTextField txt = new JTextField(25);
            JList jlist = new JList();
    		AutoCompleteListTest fac = new AutoCompleteListTest(list, txt, jlist, true);
    		frm.add(txt);
            txt = new JTextField(25);
            jlist = new JList();
            fac = new AutoCompleteListTest(list, txt, jlist, false);
            frm.add(txt);
		}
		
		frm.setVisible(true);
	}

}


