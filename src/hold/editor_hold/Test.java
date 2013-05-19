package com.dispatcher.editor;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;


public class Test {

    Editor editor;
    public Test() {

        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
        //EditorModule mod =  new EditorModule(frm);
        // mod.setSpellChecker(new SampleSpellChecker(mod));
        // mod.setSpellChecker(new VPSpellChecker(mod));
        
        editor = new Editor();
        editor.setEditable(true);
/*        
        HTMLDocument doc = (HTMLDocument) editor.editorDocument;
        String s = "<html><body><p>Vince <B>Via</B> <br> was here<p>new Paragraph ... Systems History<p>This is a new Paragraph</body></html>";
        editor.setText(s);
        mod.setEditor(editor);
*/
//        frm.setJMenuBar(mod.getMenuBar());
        frm.setLayout(new BorderLayout());
  //      frm.add(mod.getToolBar(), BorderLayout.NORTH);
        

HTMLEditorKit kitx = new HTMLEditorKit();
StyleSheet styles = kitx.getStyleSheet();
Enumeration rules = styles.getStyleNames();
while (rules.hasMoreElements()) {
    String name = (String) rules.nextElement();
    Style rule = styles.getStyle(name);
    System.out.println("EditorKit Style ==> "+rule.toString());
}


HTMLDocument doc = (HTMLDocument) editor.getDocument();


StyleSheet css = doc.getStyleSheet();

Enumeration enumx = css.getStyleNames();
while (enumx.hasMoreElements()) {
    String name = (String) enumx.nextElement();
    Style style = css.getStyle(name);
    System.out.println("Document Style ==> "+style.toString());
}




/*
Style style = editor.editorKit.getStyleSheet().getRule("body");

SimpleAttributeSet sas = new SimpleAttributeSet(style);



css = editor.editorKit.getStyleSheet();
css.addCSSAttribute(sas, CSS.Attribute.COLOR, "red");


// StyleSheet css = doc.getStyleSheet();
css.addRule("body { font-family: Arial; font-size: 18pt; color: blue; }");
css.addRule("td { padding-left: 1; padding-right: 1; padding-top: 0; padding-bottom: 0 }");
*/

/*
StyleConstants.setFontFamily(style, "Arial");
StyleConstants.setFontSize(style, 28);
StyleConstants.setForeground(style, Color.red);
*/

/*
//Style style = editor.editorDocument.getStyle("Test");
//editor.setLogicalStyle(style);
//editor.setFont(new Font("Arial", 0, 8));        
//Style style = editor.getStyledDocument().addStyle("Test", null);
Style style = doc.getStyle(StyleContext.DEFAULT_STYLE);
//style = addStyle("Test", style);
StyleConstants.setFontFamily(style, "Arial");
StyleConstants.setFontSize(style, 28);
StyleConstants.setForeground(style, Color.red);

editor.setLogicalStyle(style);
doc.setLogicalStyle(0, style);
*/
        


/****
editor.getCaret().addChangeListener(new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
        int pos = ((Caret)e.getSource()).getDot();
        System.out.println("==> "+pos);
        
try {
    StyledDocument doc = (StyledDocument) Test.this.editor.getDocument(); 
    System.out.println("getText ==> "+doc.getText(pos, 1));
    Element ele = doc.getCharacterElement(pos);
    Element elep = doc.getParagraphElement(pos);
    int offs = pos;

	Rectangle r = editor.modelToView(offs);
	if (r == null) return;
	int lastOffs = offs;
	int y = r.y;
	while ((r != null) && (y == r.y)) {
	    offs = lastOffs;
	    lastOffs -= 1;
	    r = (lastOffs >= 0) ? editor.modelToView(lastOffs) : null;
	}



    try {
        int begOffs = Utilities.getRowStart(editor, pos);
        System.out.println("Row Begin ==> "+ begOffs);
    } catch (BadLocationException bl) {
    }
    
    
}
catch (Exception ex) {
}
        
    }
}
);
**/

/*
try {
//editor.setPage("http://127.0.0.1/index.html");
editor.setPage("file://c:/projects/java/com/vetplan/wellness.html");
}
catch (Exception e) {
    System.out.println("error: "+e);
}
  

        mod.setEditor(editor); // set the active editor
*/
        
        
        frm.getContentPane().add(new JScrollPane(editor));
        frm.pack();
		frm.setVisible(true);
        
    }
    
    public static void main(String[] args) {
        Test test = new Test();
    }
}

