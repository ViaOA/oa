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
package com.viaoa.jfc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.text.*;



/**
 * Base formated textfield, that allows for masking unformatted data using a mask. 
 * OAFormattedTextField ftxt = new OAFormattedTextField("(###) ###-####", "0123456789", true, true);
 */
class BaseFormattedTextField extends JFormattedTextField {
    private boolean bSettingValue;
    private boolean bRightJustified, bAllowSpaces;
    private String mask, validChars;
    private int maskWidth;
    private char placeHolder = '_';

    /**
     * This will convert the data into a none masked format, with only valid characters.
     * 
     * Values are stored in an internal (non-masked) format. 
     *  
     * @param mask input mask to use
     * @param validChars characters that user is able to enter.
     * @param bRightJustified should data be aligned right
     * @param bAllowSpaces if true, then spaces are permitted in the output data, otherwise, they will be removed.
     */
    public BaseFormattedTextField(String mask, String validChars, boolean bRightJustified, boolean bAllowSpaces) {
        super(new MaskFormatter());
        this.mask = mask;
        this.validChars = validChars;
        this.bRightJustified = bRightJustified;
        this.bAllowSpaces = bAllowSpaces;
        setup();
    }
    
    protected void setup() {
        MaskFormatter formatter = (MaskFormatter) getFormatter();
        if (mask == null) mask = "";
        if (validChars == null) validChars = "";
        
        mask = mask.replace('#', '*');
        try {
            formatter.setMask(mask);
        }
        catch (Exception e) {
            System.out.println("Error: "+e);
        }
        int x = mask.length();
        for (int i=0; i<x; i++) {
            if (mask.charAt(i) == '*') maskWidth++; 
        }
        
        formatter.setPlaceholderCharacter(placeHolder);
        if (validChars.length() > 0) formatter.setValidCharacters(validChars+"_");  // this will set allowable chars, plus '_' for placeholder for missing digits
        formatter.setValueContainsLiteralCharacters(false);  // have mask chars stripped
        
        
        // this is expected by FormatedTextFieldController, so that focusLost will cause the value to be updated - which will then call controller saveText()
        setFocusLostBehavior(JFormattedTextField.COMMIT); // no parse error

        
        addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (bSettingValue) return;
                String value = (String) evt.getNewValue();
//System.out.println("1 prop change --> " + value);
    
                // convert to output format - strips the mask chars
                int x = value.length();
                StringBuffer sb = new StringBuffer(x);
                for (int i=0; i<x; i++) {
                    char ch = value.charAt(i); 
                    if (ch == placeHolder) {
                        if (bAllowSpaces) sb.append(' '); 
                    }
                    else sb.append(ch);
                }
                value = new String(sb);
//System.out.println("2 prop change --> " + value);
                onValueChange(value);
            }
        });
    }

    @Override
    public void setValue(Object obj) {
        String value = obj == null ? "" : obj.toString();
        // System.out.println("1 setValue --> " + value);
        
        MaskFormatter fmt = (MaskFormatter) getFormatter();
        
        // convert to internal format needed by mask - can only be validCharacters
        if (bAllowSpaces) {
            int x = value.length();
            StringBuffer sb = new StringBuffer(x);
            for (int i=0; i<x; i++) {
                char ch = value.charAt(i);
                if (validChars.indexOf(ch) >= 0) {
                    sb.append(ch);
                }
                else {
                    if (bAllowSpaces && ch == ' ') sb.append(placeHolder);
                }
            }
            value = new String(sb);
        }
        
        int x = value.length();
        for (int i=x; i < maskWidth; i++) {
            if (bRightJustified) value = placeHolder + value; 
            else value = value + placeHolder; 
        }
        
        bSettingValue = true;
        super.setValue(value);
        bSettingValue = false;
    }

    protected void onValueChange(String value) {
        // set OA property here
    }
    
}
