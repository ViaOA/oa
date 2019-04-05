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
package com.viaoa.jfc.editor.html.oa;

import java.awt.Color;

import com.viaoa.annotation.OAClass;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/**
 * OAObject used for BlockDialog.
 * @author vvia
 *
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class Block extends OAObject {

    public static final String P_Width = "Width";
    public static final String P_Height = "Height";
    
    public static final String P_Margin = "Margin";
    public static final String P_MarginTop = "MarginTop";
    public static final String P_MarginBottom = "MarginBottom";
    public static final String P_MarginLeft = "MarginLeft";
    public static final String P_MarginRight = "MarginRight";
    
    public static final String P_Padding = "Padding";
    public static final String P_PaddingTop = "PaddingTop";
    public static final String P_PaddingBottom = "PaddingBottom";
    public static final String P_PaddingLeft = "PaddingLeft";
    public static final String P_PaddingRight = "PaddingRight";

    public static final String P_BackgroundColor = "BackgroundColor";


    public static final String P_BorderWidth = "BorderWidth";
    public static final String P_BorderTopWidth = "BorderTopWidth";
    public static final String P_BorderRightWidth = "BorderRightWidth";
    public static final String P_BorderBottomWidth = "BorderBottomWidth";
    public static final String P_BorderLeftWidth = "BorderLeftWidth";

    public static final String P_BorderColor = "BorderColor";
    public static final String P_BorderTopColor = "BorderTopColor";
    public static final String P_BorderRightColor = "BorderRightColor";
    public static final String P_BorderBottomColor = "BorderBottomColor";
    public static final String P_BorderLeftColor = "BorderLeftColor";
    
    public static final String P_BorderStyle = "BorderStyle";
    public static final String P_BorderTopStyle = "BorderTopStyle";
    public static final String P_BorderRightStyle = "BorderRightStyle";
    public static final String P_BorderBottomStyle = "BorderBottomStyle";
    public static final String P_BorderLeftStyle = "BorderLeftStyle";
    
    protected int width;
    protected int height;
    protected int margin;
    protected int marginTop;
    protected int marginBottom;
    protected int marginLeft;
    protected int marginRight;
    protected int padding;
    protected int paddingTop;
    protected int paddingBottom;
    protected int paddingLeft;
    protected int paddingRight;
    protected Color backgroundColor;
    protected int borderWidth;
    protected int borderTopWidth;
    protected int borderRightWidth;
    protected int borderBottomWidth;
    protected int borderLeftWidth;
    protected Color borderColor;
    protected Color borderTopColor;
    protected Color borderRightColor;
    protected Color borderBottomColor;
    protected Color borderLeftColor;

    protected String borderStyle;
    protected String borderTopStyle;
    protected String borderRightStyle;
    protected String borderBottomStyle;
    protected String borderLeftStyle;
    
    private static Hub<String> hubBorderStyles;
    public static Hub<String> getBorderStyles() {
        if (hubBorderStyles == null) {
            hubBorderStyles = new Hub<String>(String.class);
            hubBorderStyles.add("solid");
            hubBorderStyles.add("dashed");
            hubBorderStyles.add("none");
        }
        return hubBorderStyles.createSharedHub();
    }
    
    
    public int getBorderLeftWidth() {
        return borderLeftWidth;
    }
    public void setBorderLeftWidth(int newValue) {
        int old = this.borderLeftWidth;
        this.borderLeftWidth = newValue;
        firePropertyChange(P_BorderLeftWidth, old, this.borderLeftWidth);
    }
    public int getBorderBottomWidth() {
        return borderBottomWidth;
    }
    public void setBorderBottomWidth(int newValue) {
        int old = this.borderBottomWidth;
        this.borderBottomWidth = newValue;
        firePropertyChange(P_BorderBottomWidth, old, this.borderBottomWidth);
    }
    public int getBorderRightWidth() {
        return borderRightWidth;
    }
    public void setBorderRightWidth(int newValue) {
        int old = this.borderRightWidth;
        this.borderRightWidth = newValue;
        firePropertyChange(P_BorderRightWidth, old, this.borderRightWidth);
    }
    public int getBorderTopWidth() {
        return borderTopWidth;
    }
    public void setBorderTopWidth(int newValue) {
        int old = this.borderTopWidth;
        this.borderTopWidth = newValue;
        firePropertyChange(P_BorderTopWidth, old, this.borderTopWidth);
    }
    public int getBorderWidth() {
        return borderWidth;
    }
    public void setBorderWidth(int newValue) {
        int old = this.borderWidth;
        this.borderWidth = newValue;
        firePropertyChange(P_BorderWidth, old, this.borderWidth);
        if (old != newValue) {
            setBorderTopWidth(newValue);
            setBorderLeftWidth(newValue);
            setBorderBottomWidth(newValue);
            setBorderRightWidth(newValue);
        }
    }
    
    public String getBorderLeftStyle() {
        return borderLeftStyle;
    }
    public void setBorderLeftStyle(String newValue) {
        String old = this.borderLeftStyle;
        this.borderLeftStyle = newValue;
        firePropertyChange(P_BorderLeftStyle, old, this.borderLeftStyle);
    }
    public String getBorderBottomStyle() {
        return borderBottomStyle;
    }
    public void setBorderBottomStyle(String newValue) {
        String old = this.borderBottomStyle;
        this.borderBottomStyle = newValue;
        firePropertyChange(P_BorderBottomStyle, old, this.borderBottomStyle);
    }
    public String getBorderRightStyle() {
        return borderRightStyle;
    }
    public void setBorderRightStyle(String newValue) {
        String old = this.borderRightStyle;
        this.borderRightStyle = newValue;
        firePropertyChange(P_BorderRightStyle, old, this.borderRightStyle);
    }
    public String getBorderTopStyle() {
        return borderTopStyle;
    }
    public void setBorderTopStyle(String newValue) {
        String old = this.borderTopStyle;
        this.borderTopStyle = newValue;
        firePropertyChange(P_BorderTopStyle, old, this.borderTopStyle);
    }
    public String getBorderStyle() {
        return borderStyle;
    }
    public void setBorderStyle(String newValue) {
        String old = this.borderStyle;
        this.borderStyle = newValue;
        firePropertyChange(P_BorderStyle, old, this.borderStyle);
        if (old != newValue) {
            setBorderTopStyle(newValue);
            setBorderLeftStyle(newValue);
            setBorderBottomStyle(newValue);
            setBorderRightStyle(newValue);
        }
    }

    public Color getBorderLeftColor() {
        return borderLeftColor;
    }
    public void setBorderLeftColor(Color newValue) {
        Color old = this.borderLeftColor;
        this.borderLeftColor = newValue;
        firePropertyChange(P_BorderLeftColor, old, this.borderLeftColor);
    }
    public Color getBorderBottomColor() {
        return borderBottomColor;
    }
    public void setBorderBottomColor(Color newValue) {
        Color old = this.borderBottomColor;
        this.borderBottomColor = newValue;
        firePropertyChange(P_BorderBottomColor, old, this.borderBottomColor);
    }
    public Color getBorderRightColor() {
        return borderRightColor;
    }
    public void setBorderRightColor(Color newValue) {
        Color old = this.borderRightColor;
        this.borderRightColor = newValue;
        firePropertyChange(P_BorderRightColor, old, this.borderRightColor);
    }
    public Color getBorderTopColor() {
        return borderTopColor;
    }
    public void setBorderTopColor(Color newValue) {
        Color old = this.borderTopColor;
        this.borderTopColor = newValue;
        firePropertyChange(P_BorderTopColor, old, this.borderTopColor);
    }
    public Color getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(Color newValue) {
        Color old = this.borderColor;
        this.borderColor = newValue;
        firePropertyChange(P_BorderColor, old, this.borderColor);
        if (old != newValue) {
            setBorderTopColor(newValue);
            setBorderLeftColor(newValue);
            setBorderBottomColor(newValue);
            setBorderRightColor(newValue);
        }
    }
    
    
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(Color newValue) {
        Color old = this.backgroundColor;
        this.backgroundColor = newValue;
        firePropertyChange(P_BackgroundColor, old, this.backgroundColor);
    }
    
    public int getPaddingRight() {
        return paddingRight;
    }
    public void setPaddingRight(int newValue) {
        int old = this.paddingRight;
        this.paddingRight = newValue;
        firePropertyChange(P_PaddingRight, old, this.paddingRight);
    }
    
    public int getPaddingLeft() {
        return paddingLeft;
    }
    public void setPaddingLeft(int newValue) {
        int old = this.paddingLeft;
        this.paddingLeft = newValue;
        firePropertyChange(P_PaddingLeft, old, this.paddingLeft);
    }
    
    public int getPaddingBottom() {
        return paddingBottom;
    }
    public void setPaddingBottom(int newValue) {
        int old = this.paddingBottom;
        this.paddingBottom = newValue;
        firePropertyChange(P_PaddingBottom, old, this.paddingBottom);
    }
    
    public int getPaddingTop() {
        return paddingTop;
    }
    public void setPaddingTop(int newValue) {
        int old = this.paddingTop;
        this.paddingTop = newValue;
        firePropertyChange(P_PaddingTop, old, this.paddingTop);
    }
    
    public int getPadding() {
        return padding;
    }
    public void setPadding(int newValue) {
        int old = this.padding;
        this.padding = newValue;
        firePropertyChange(P_Padding, old, this.padding);
        if (old != newValue) {
            setPaddingTop(newValue);
            setPaddingLeft(newValue);
            setPaddingBottom(newValue);
            setPaddingRight(newValue);
        }
    }
    
    public int getMargin() {
        return margin;
    }
    public void setMargin(int newValue) {
        int old = this.margin;
        this.margin = newValue;
        firePropertyChange(P_Margin, old, this.margin);
        if (old != newValue) {
            setMarginTop(newValue);
            setMarginLeft(newValue);
            setMarginBottom(newValue);
            setMarginRight(newValue);
        }
    }
    
    public int getMarginRight() {
        return marginRight;
    }
    public void setMarginRight(int newValue) {
        int old = this.marginRight;
        this.marginRight = newValue;
        firePropertyChange(P_MarginRight, old, this.marginRight);
    }
    public int getMarginLeft() {

        return marginLeft;
    }
    public void setMarginLeft(int newValue) {
        int old = this.marginLeft;
        this.marginLeft = newValue;
        firePropertyChange(P_MarginLeft, old, this.marginLeft);
    }
    public int getMarginBottom() {
        return marginBottom;
    }
    public void setMarginBottom(int newValue) {
        int old = this.marginBottom;
        this.marginBottom = newValue;
        firePropertyChange(P_MarginBottom, old, this.marginBottom);
    }
    public int getMarginTop() {
        return marginTop;
    }
    public void setMarginTop(int newValue) {
        int old = this.marginTop;
        this.marginTop = newValue;
        firePropertyChange(P_MarginTop, old, this.marginTop);
    }
    
    
    
    
    public int getHeight() {
        return height;
    }
    public void setHeight(int newValue) {
        int old = this.height;
        this.height = newValue;
        firePropertyChange(P_Height, old, this.height);
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int newValue) {
        int old = this.width;
        this.width = newValue;
        firePropertyChange(P_Width, old, this.width);
    }

    
    
    
    
    
    
    
    
    
    public String getStyle() {
        // "border-style:solid; border-top-width:2;border-color:green;width:120px;");
        
        String style = "";
        if (width > 0) style += "width:"+width+";";
        if (height > 0) style += "height:"+height+";";

        if (marginTop > 0) style += "margin-top:"+marginTop+";";
        if (marginRight > 0) style += "margin-right:"+marginRight+";";
        if (marginBottom > 0) style += "margin-bottom:"+marginBottom+";";
        if (marginLeft > 0) style += "margin-left:"+marginLeft+";";
        
        if (paddingTop > 0) style += "padding-top:"+paddingTop+";";
        if (paddingRight > 0) style += "padding-right:"+paddingRight+";";
        if (paddingBottom > 0) style += "padding-bottom:"+paddingBottom+";";
        if (paddingLeft > 0) style += "padding-left:"+paddingLeft+";";
        
        if (backgroundColor != null) {
            style += "background-color: rgb("+ backgroundColor.getRed()+", " + backgroundColor.getGreen() + ", "+backgroundColor.getBlue()+");"; 
        }

        Color c = borderTopColor;
        if (c != null) {
            style += "border-top-color: rgb("+ c.getRed()+", " + c.getGreen() + ", "+c.getBlue()+");"; 
        }
        c = borderRightColor;
        if (c != null) {
            style += "border-right-color: rgb("+ c.getRed()+", " + c.getGreen() + ", "+c.getBlue()+");"; 
        }
        c = borderBottomColor;
        if (c != null) {
            style += "border-bottom-color: rgb("+ c.getRed()+", " + c.getGreen() + ", "+c.getBlue()+");"; 
        }
        c = borderLeftColor;
        if (c != null) {
            style += "border-left-color: rgb("+ c.getRed()+", " + c.getGreen() + ", "+c.getBlue()+");"; 
        }

        if (borderTopWidth > 0) {
            style += "border-top-width:"+borderTopWidth+";";
        }
        if (borderRightWidth > 0) {
            style += "border-right-width:"+borderRightWidth+";";
        }
        if (borderBottomWidth > 0) {
            style += "border-bottom-width:"+borderBottomWidth+";";
        }
        if (borderLeftWidth > 0) {
            style += "border-left-width:"+borderLeftWidth+";";
        }

        if (!OAString.isEmpty(borderTopStyle)) {
            style += "border-top-style:"+borderTopStyle+";";
        }
        if (!OAString.isEmpty(borderRightStyle)) {
            style += "border-right-style:"+borderRightStyle+";";
        }
        if (!OAString.isEmpty(borderBottomStyle)) {
            style += "border-bottom-style:"+borderBottomStyle+";";
        }
        if (!OAString.isEmpty(borderLeftStyle)) {
            style += "border-left-style:"+borderLeftStyle+";";
        }
        
        return style;
    }
}
