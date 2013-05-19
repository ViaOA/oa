package com.viaoa.jfc.textfield;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class FileAutoComplete extends AutoComplete { 
    
    public FileAutoComplete(JTextComponent comp, boolean bExactMatchOnly){ 
        super(comp, bExactMatchOnly); 
    } 
 
    @Override
    protected String[] getSearchData(String value, int position) {
        int index1 = value.lastIndexOf('\\'); 
        int index2 = value.lastIndexOf('/'); 
        int index = Math.max(index1, index2); 
        if(index==-1) return new String[0]; 
        String dir = value.substring(0, index+1); 
        final String prefix = index==value.length()-1 ? null : value.substring(index + 1).toLowerCase(); 
        String[] files = new File(dir).list(new FilenameFilter(){ 
            public boolean accept(File dir, String name){ 
                return prefix!=null ? name.toLowerCase().startsWith(prefix) : true; 
            } 
        }); 
        if (files == null) return new String[0]; 
        if (files.length==1 && files[0].equalsIgnoreCase(prefix)) return new String[0]; 
        return files; 
    } 
 
    protected void acceptedListItem(String selected){ 
        if (selected==null) return; 
 
        String value = textComp.getText(); 
        int index1 = value.lastIndexOf('\\'); 
        int index2 = value.lastIndexOf('/'); 
        int index = Math.max(index1, index2); 
        if(index==-1) 
            return; 
        int prefixlen = textComp.getDocument().getLength()-index-1; 
        try{ 
            textComp.getDocument().insertString(textComp.getCaretPosition(), selected.substring(prefixlen), null); 
        } catch(BadLocationException e){ 
            e.printStackTrace(); 
        } 
    }
    
    @Override
    protected String getFirstMatch(String value) {
        return value; //qqqqqqqq ToDo
    }
    
    
    public static void main(String[] args) {
		JFrame frm = new JFrame();
		frm.setSize(new Dimension(500,200));
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setLayout(new FlowLayout());
		
		JTextField txt = new JTextField(25);
		FileAutoComplete fac = new FileAutoComplete(txt, false);
		
		frm.add(txt);
		
		frm.setVisible(true);
	}
    
} 


