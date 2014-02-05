/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.html;


import java.lang.reflect.*;
import java.util.Vector;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/** Button that has two states: Selected and Not Selected. Can be used in a OAButtonGroup so that
    only one button in the group is selected at a time.

<pre>
    [Java Code]
    OAToggleButton tog = new OAToggleButton("testUp.gif","testDown.gif");
    tog.setSelected(true);
    buttonGroup.add(tog);
    form.add("togTest", tog);
    ....
    [HTML Code]
    &lt;input type="image" name="&lt;%=form.getToggleButton("togUser").getName()%&gt;" &lt;%=form.getToggleButton("togUser").getSource()%&gt; SRC="testUp.gif" ALT="User Info" border="0"&gt;
    output =&gt;
    &lt;input type="image" name="oacommand_7_togUser" SRC="testUp.gif" src="testUp.gif"&gt;
</pre>
    @see OAButtonGroup
*/
public class OAToggleButton extends OACommand {
    private static final long serialVersionUID = 1L;
    protected OAButtonGroup buttonGroup;
    protected String tooltip;
    
    public OAToggleButton() {
        bOnClickScript = false; // dont include onclick event handler
    }

    public OAToggleButton(String imageName) {
        this();
        setImageName(imageName);
    }
    public OAToggleButton(String imageName, String selectedImageName) {
        this();
        setImageName(imageName);
        setSelectedImageName(selectedImageName);
    }
    public OAToggleButton(String imageName, String selectedImageName, String mouseOverImage) {
        this();
        setImageName(imageName);
        setSelectedImageName(selectedImageName);
        setMouseOverImageName(mouseOverImage);
    }

    public void setSelected(boolean b) {
        if (getSelected() != b) {
            super.setSelected(b);
            if (b && buttonGroup != null) buttonGroup.setSelected(this);
        }
    }


    public OAButtonGroup getButtonGroup() {
        return buttonGroup;
    }
    
    public void setButtonGroup(OAButtonGroup bgrp) {
        if (buttonGroup != null) {
            if (buttonGroup == bgrp) return;
            bgrp.remove(this);
        }
        buttonGroup = bgrp;
        if (bgrp != null) bgrp.add(this);
        
    }
    
    /************************** OAHtmlComponent ************************/
    protected String processCommand(OASession session, OAForm form, String command) { 
        setSelected(true);
        return super.processCommand(session, form, command);
    }

}

