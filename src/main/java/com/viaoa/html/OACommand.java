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


// sends as:  oacommand_nameLength_name:[ID]


/** Includes built in commands and the option to create a passthru command to a url (.jsp).
    If a passthru command, then OAForm setLastCommand() will be set to value of command 
    and setLastObject() will be set (if needed).

<pre>
    [Java Code]
    OACommand cmd = new OACommand(hub,OACommand.SAVE);
    cmd.setInvisibleImageName("invisible.gif");
    cmd.setDisabledImageName("saveOff.gif");
    form.add("cmdTest", cmd);
    ....
    [HTML Code]
    &lt;input type="image" &lt;%=form.getCommand("cmdTest").getTags()%&gt;"&gt;
    output =&gt;
    &lt;input type="image" name="oacommand_7_cmdTest" SRC="save.gif" src="test.gif"&gt;

    <br>-or-
    &lt;INPUT TYPE="SUBMIT" &lt;%=form.getCommand("cmdTest").getTags()%&gt;" VALUE="OK"&gt;
    output =&gt;
    &lt;input type="SUBMIT" name="oacommand_7_cmdTest" VALUE="OK" &gt;
    

</pre>
    @see OATable#addCommand
*/

public class OACommand extends OAImage {
    private static final long serialVersionUID = 1L;
    
    public OACommand() {
    }
    public OACommand(Hub hub, int cmd) {
       setHub(hub);
       setCommand(cmd);
    }
    public OACommand(int cmd) {
       setCommand(cmd);
    }

    public OACommand(String url) {
       setUrl(url);
    }

    
    public String getHtml(String htmlTags) {
// <input type="IMAGE" name="<%=form.getCommand("cmdSave").getName()%>" SRC="<%=form.getCommand("cmdSave").getSource()%>" ALT="Save" BORDER="0">
// <input type="submit" name="<%=form.getCommand("cmdSave").getName()%>" VALUE="OK">
        if (!getVisible()) return "";
        
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s = "<INPUT TYPE=\"";
        if (text != null && text.length() > 0) {
            s += "SUBMIT\"";
            s += " VALUE=\""+text+"\"";
        }
        else {
            s += "IMAGE\"";
            s += " SRC=\""+getSource()+"\" BORDER=\"0\"";
        }
        s += " NAME=\""+getName()+"\"";
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        
        
        OAImage img;
        try {
            img = getTargetImage();
        }
        catch (Exception e) {
            return e.getMessage();
        }
        if (!img.isValid()) s += " DISABLED";
        else  s += " "+getScript();
        
        s += ">";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }

    
    
    
/**    
    public String getHtml() {

 String line = "<IMG NAME=\""+getName()+"\" SRC=\""+getSource()+"\" BORDER=\""+getBorder()+"\"";
 line += " ALIGN=\""+ALIGN[getAlign()]+"\"";
 line += " VALIGN=\""+VALIGN[getValign()]+"\"";
 String s = getAlt();
 if (s != null && s.length() > 0) line += " ALT=\""+s+"\"";
 int i = getHeight();
 if (i > 0) line += " HEIGHT=\""+i+"\"";
 i = getWidth();
 if (i > 0) line += " WIDTH=\""+i+"\"";

 String s = getScript();
 
\" ALT=\""+getAlt()+"\" ALIGN=\""+getAlignAsString()+"\">";

    }
*/




}

