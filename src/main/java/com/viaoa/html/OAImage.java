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


/** Object to display dynamic images: 
    normal, select, disabled, invisible, mouseOver, mouseDown.  <br>
    Built-in javascript 1: to execute command on another frame, 2: display URL in another frame.

<pre>
    [Java Code]
    OAImage img = new OAImage("normal.gif", "mouseOver.gif");
    img.setUrl("newPage.jsp");  // to display a page
    img.setTarget("frameName");  // to display URL in another frame
    img.setTargetCommand("formName.jsp", "cmdName"); // to execute a command in another form
    form.add("imgTest", img);
    ....
    [HTML Code]
    &lt;img name="&lt;%=form.getImage("imgTest").getName()%&gt;" src="&lt;%=form.getImage("imgTest").getSource()%&gt;" &lt;%=form.getImage("imgTest").getScript()%&gt; border="0" alt="mess goes here" &gt;

    if within a form ...
    &lt;input type="image" name="&lt;%=form.getImage("imgTest").getName()%&gt;" src="&lt;%=form.getImage("imgTest").getSource()%&gt;" &lt;%=form.getImage("imgTest").getScript()%&gt; border="0" alt="mess goes here" &gt;
</pre>
*/

public class OAImage extends OALink {
    private static final long serialVersionUID = 1L;
    protected String imageName;  // "gif"
    protected String selectName;  // "gif"
    protected String disabledImageName = "invisible.gif"; 
    protected String invisibleImageName = "invisible.gif";
    protected String mouseOverImageName;
    protected String mouseDownImageName;
    protected boolean bSelected;

    public OAImage() {
    }
    public OAImage(String imageName) {
        setImageName(imageName);
    }
    public OAImage(String imageName, String mouseOver) {
        setImageNames(imageName,mouseOver);
    }
    public OAImage(String imageName, String mouseOver, String mouseDown) {
        setImageNames(imageName,mouseOver,mouseDown);
    }

    public void setImageNames(String imageName, String mouseOver, String mouseDown) {
        setImageName(imageName);
        setMouseOverImageName(mouseOver);
        setMouseDownImageName(mouseDown);
    }
    public void setImageNames(String imageName, String mouseOver) {
        setImageName(imageName);
        setMouseOverImageName(mouseOver);
    }

    public void setCommand(int cmd) {
        super.setCommand(cmd);
        if (imageName == null && cmd >= 0 && cmd < defaultImageNames.length) setImageName(defaultImageNames[cmd]+".gif");
    }

    public String getImageName() {
        return imageName;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getSelectedImageName() {
        return selectName;
    }
    public void setSelectedImageName(String selectImageName) {
        this.selectName = selectImageName;
    }

    public String getMouseOverImageName() {
        return mouseOverImageName;
    }
    public void setMouseOverImageName(String mouseOverImageName) {
        this.mouseOverImageName = mouseOverImageName;
    }
    public String getMouseDownImageName() {
        return mouseDownImageName;
    }
    public void setMouseDownImageName(String mouseDownImageName) {
        this.mouseDownImageName = mouseDownImageName;
    }
    
    public String getDisabledImageName() {
        return disabledImageName;
    }
    public void setDisabledImageName(String disabledImageName) {
        this.disabledImageName = disabledImageName;
    }

    /** default: "invisible.gif" */
    public String getInvisibleImageName() {
        return invisibleImageName;
    }
    public void setInvisibleImageName(String invisibleImageName) {
        this.invisibleImageName = invisibleImageName;
    }

    public boolean getSelected() {
        return bSelected;
    }
    public boolean isSelected() {
        return bSelected;
    }
    /** if false then the invisible image will be used and inputs will be ignored.*/
    public void setSelected(boolean b) {
        bSelected = b;
    }

    /** @return full string for name.  ex: NAME="imgTest" */
    public String getNameTag() {
        return "NAME=\""+getName()+"\"";
    }    
    
    
    /** @return full string for image source.  ex: SRC="image.gif" */
    public String getSourceTag() {
        return "SRC=\""+getSource()+"\"";
    }    

    protected OAImage getTargetImage() throws Exception {
        OAImage img = this;
        if (targetForm != null) {
            if (form != null) {
                OASession sess = form.getSession();
                if (sess != null) {
                    OAForm f = sess.getForm(targetForm, true);
                    if (f == null) throw new Exception("Error: targetForm for command \""+name+"\" not found");
                    OAHtmlComponent c = f.getComponent(targetCommand);
                    if (c == null) throw new Exception("Error: targetCommand for command \""+name+"\" not found");
                    if ( !(c instanceof OAImage) ) throw new Exception("Error: targetCommand for command \""+name+"\" is not an OACommand");
                    img = (OAImage) c;
                }
            }
        }
        return img;
    }
    

    private String lastSource;
    /** @return image source.  ex: "image.gif" 
        @see OAImage#getSourceTag
    */
    public String getSource() {
        OAImage img;
        try {
            img = getTargetImage();
        }
        catch (Exception e) {
            return e.getMessage();
        }

        boolean b = img.isValid();

        String src = imageName;
        if (img.bSelected && img.selectName != null) {
            src = selectName;
        }
        if (!b) {
            src = disabledImageName;
        }
        if (!bVisible) {
            src = invisibleImageName;
        }
        if (src == null) src = "";
        lastSource = src;
        
        // 2008
        if (src.length() > 0) src = "src='" + src+"'"; 
        return src;
    }


    /** @return javascript needed for initialization. */
    public String getInitScript() {
        String line = null;

        line = name + " = new Image();";
        line += name + ".src = \""+imageName+"\";";
        boolean b = false;
        if (mouseOverImageName != null) {
            b = true;
            line += name + "MouseOver = new Image();";
            line += name + "MouseOver.src = \""+mouseOverImageName+"\";";
        }    
        if (mouseDownImageName != null) {
            b = true;
            line += name + "MouseDown = new Image();";
            line += name + "MouseDown.src = \""+mouseDownImageName+"\";";
        }
        if (!b) return null;

        return line;
    }

    /** @return JavaScript or "". */
    public String getScript() {
        String line = "";
        String s = getSource();
        if (s.equals(imageName)) {
            if (mouseOverImageName != null) {
                line += " onmouseover=\"this.src = "+name+"MouseOver.src;\"";
                line += " onmouseout=\"this.src = "+name+".src;\"";
//                line += " onmouseover=\"document.images['"+getName()+"'].src = "+name+"MouseOver.src;\"";
//                line += " onmouseout=\"document.images['"+getName()+"'].src = "+name+".src;\"";
            }    
            if (mouseDownImageName != null) {
                line += " onmousedown=\"this.src = "+name+"MouseDown.src;\"";
                line += " onmouseup=\"this.src = "+name+".src;\"";
            }
            s = getOnClickJavaScript();
            if (s != null) {
                line += " "+s;
            }
        }
        return line;
    }    

    public boolean needsRefreshed() {
        if (lastSource == null) return false;
        String hold = lastSource;
        String s = getSource();
        lastSource = hold;
        return !s.equals(lastSource);
    }
}

