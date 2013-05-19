package com.dispatcher.editor;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.sql.*;

import com.viaoa.util.*;

public class AccessSpellChecker extends SpellChecker {
	protected static String SELECT_QUERY = "SELECT Data.word FROM Data WHERE Data.word = ";
	protected static String SOUNDEX_QUERY = "SELECT Data.word FROM Data WHERE Data.soundex = ";

	protected Connection m_conn;
	protected Statement selStmt;

	public AccessSpellChecker(JFrame frm, Editor ed) {
	    super(frm, ed);
	    setup();
    }

    protected void setup() {
		try {
			// Load the JDBC-ODBC bridge driver
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            String jdbcUrl = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=Shakespeare.mdb";
            //was:  jdbcUrl = "jdbc:odbc:Shakespeare";			

			m_conn = DriverManager.getConnection(jdbcUrl, "admin", "");
		    selStmt = m_conn.createStatement();
            // m_conn.close();
            // System.gc();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("SpellChecker error: "+ex.toString());
		}
	}

    public void addNewWord(String word) {
        if (word == null) return;
        word = word.toLowerCase();
		String sdx = OAString.soundex(word);
		try {
			Statement stmt = m_conn.createStatement();
			stmt.executeUpdate("INSERT INTO DATA (Word, Soundex) VALUES ('"+word+"', '"+sdx+"')");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("SpellChecker error: "+ex.toString());
		}
    }

    public boolean isWordFound(String word) {
        if (word == null) return true;
		String wordLowCase = word.toLowerCase();
        try {
    		ResultSet results = selStmt.executeQuery(SELECT_QUERY+"'"+wordLowCase+"'");
	    	boolean b = results.next();
	    	results.close();
	    	return b;
	    }
	    catch (Exception e) {
	        System.out.println("Error: "+e);
	    }
	    return false;
    }

    public String[] getSuggestions(String word) {
        if (word == null) return null;
		String wordLowCase = word.toLowerCase();
		Vector vec = new Vector(10,10);
		try {
    		ResultSet results = selStmt.executeQuery(SOUNDEX_QUERY+"'"+OAString.soundex(wordLowCase)+"'");
			while (results.next()) {
				String str = results.getString(1);
				str = Utils.titleCase(str);
				vec.addElement(str);
			}
		}
		catch (SQLException ex) {
			System.err.println("getSuggestions(): "+ex.toString());
		}
		String[] ss = new String[vec.size()];
		vec.copyInto(ss);
		return ss;
    }

}
