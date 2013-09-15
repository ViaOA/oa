/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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

    public static final String PROPERTY_Width = "Width";
    public static final String PROPERTY_Height = "Height";
    
    public static final String PROPERTY_Margin = "Margin";
    public static final String PROPERTY_MarginTop = "MarginTop";
    public static final String PROPERTY_MarginBottom = "MarginBottom";
    public static final String PROPERTY_MarginLeft = "MarginLeft";
    public static final String PROPERTY_MarginRight = "MarginRight";
    
    public static final String PROPERTY_Padding = "Padding";
    public static final String PROPERTY_PaddingTop = "PaddingTop";
    public static final String PROPERTY_PaddingBottom = "PaddingBottom";
    public static final String PROPERTY_PaddingLeft = "PaddingLeft";
    public static final String PROPERTY_PaddingRight = "PaddingRight";

    public static final String PROPERTY_BackgroundColor = "BackgroundColor";


    public static final String PROPERTY_BorderWidth = "BorderWidth";
    public static final String PROPERTY_BorderTopWidth = "BorderTopWidth";
    public static final String PROPERTY_BorderRightWidth = "BorderRightWidth";
    public static final String PROPERTY_BorderBottomWidth = "BorderBottomWidth";
    public static final String PROPERTY_BorderLeftWidth = "BorderLeftWidth";

    public static final String PROPERTY_BorderColor = "BorderColor";
    public static final String PROPERTY_BorderTopColor = "BorderTopColor";
    public static final String PROPERTY_BorderRightColor = "BorderRightColor";
    public static final String PROPERTY_BorderBottomColor = "BorderBottomColor";
    public static final String PROPERTY_BorderLeftColor = "BorderLeftColor";
    
    public static final String PROPERTY_BorderStyle = "BorderStyle";
    public static final String PROPERTY_BorderTopStyle = "BorderTopStyle";
    public static final String PROPERTY_BorderRightStyle = "BorderRightStyle";
    public static final String PROPERTY_BorderBottomStyle = "BorderBottomStyle";
    public static final String PROPERTY_BorderLeftStyle = "BorderLeftStyle";
    
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
        firePropertyChange(PROPERTY_BorderLeftWidth, old, this.borderLeftWidth);
    }
    public int getBorderBottomWidth() {
        return borderBottomWidth;
    }
    public void setBorderBottomWidth(int newValue) {
        int old = this.borderBottomWidth;
        this.borderBottomWidth = newValue;
        firePropertyChange(PROPERTY_BorderBottomWidth, old, this.borderBottomWidth);
    }
    public int getBorderRightWidth() {
        return borderRightWidth;
    }
    public void setBorderRightWidth(int newValue) {
        int old = this.borderRightWidth;
        this.borderRightWidth = newValue;
        firePropertyChange(PROPERTY_BorderRightWidth, old, this.borderRightWidth);
    }
    public int getBorderTopWidth() {
        return borderTopWidth;
    }
    public void setBorderTopWidth(int newValue) {
        int old = this.borderTopWidth;
        this.borderTopWidth = newValue;
        firePropertyChange(PROPERTY_BorderTopWidth, old, this.borderTopWidth);
    }
    public int getBorderWidth() {
        return borderWidth;
    }
    public void setBorderWidth(int newValue) {
        int old = this.borderWidth;
        this.borderWidth = newValue;
        firePropertyChange(PROPERTY_BorderWidth, old, this.borderWidth);
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
        firePropertyChange(PROPERTY_BorderLeftStyle, old, this.borderLeftStyle);
    }
    public String getBorderBottomStyle() {
        return borderBottomStyle;
    }
    public void setBorderBottomStyle(String newValue) {
        String old = this.borderBottomStyle;
        this.borderBottomStyle = newValue;
        firePropertyChange(PROPERTY_BorderBottomStyle, old, this.borderBottomStyle);
    }
    public String getBorderRightStyle() {
        return borderRightStyle;
    }
    public void setBorderRightStyle(String newValue) {
        String old = this.borderRightStyle;
        this.borderRightStyle = newValue;
        firePropertyChange(PROPERTY_BorderRightStyle, old, this.borderRightStyle);
    }
    public String getBorderTopStyle() {
        return borderTopStyle;
    }
    public void setBorderTopStyle(String newValue) {
        String old = this.borderTopStyle;
        this.borderTopStyle = newValue;
        firePropertyChange(PROPERTY_BorderTopStyle, old, this.borderTopStyle);
    }
    public String getBorderStyle() {
        return borderStyle;
    }
    public void setBorderStyle(String newValue) {
        String old = this.borderStyle;
        this.borderStyle = newValue;
        firePropertyChange(PROPERTY_BorderStyle, old, this.borderStyle);
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
        firePropertyChange(PROPERTY_BorderLeftColor, old, this.borderLeftColor);
    }
    public Color getBorderBottomColor() {
        return borderBottomColor;
    }
    public void setBorderBottomColor(Color newValue) {
        Color old = this.borderBottomColor;
        this.borderBottomColor = newValue;
        firePropertyChange(PROPERTY_BorderBottomColor, old, this.borderBottomColor);
    }
    public Color getBorderRightColor() {
        return borderRightColor;
    }
    public void setBorderRightColor(Color newValue) {
        Color old = this.borderRightColor;
        this.borderRightColor = newValue;
        firePropertyChange(PROPERTY_BorderRightColor, old, this.borderRightColor);
    }
    public Color getBorderTopColor() {
        return borderTopColor;
    }
    public void setBorderTopColor(Color newValue) {
        Color old = this.borderTopColor;
        this.borderTopColor = newValue;
        firePropertyChange(PROPERTY_BorderTopColor, old, this.borderTopColor);
    }
    public Color getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(Color newValue) {
        Color old = this.borderColor;
        this.borderColor = newValue;
        firePropertyChange(PROPERTY_BorderColor, old, this.borderColor);
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
        firePropertyChange(PROPERTY_BackgroundColor, old, this.backgroundColor);
    }
    
    public int getPaddingRight() {
        return paddingRight;
    }
    public void setPaddingRight(int newValue) {
        int old = this.paddingRight;
        this.paddingRight = newValue;
        firePropertyChange(PROPERTY_PaddingRight, old, this.paddingRight);
    }
    
    public int getPaddingLeft() {
        return paddingLeft;
    }
    public void setPaddingLeft(int newValue) {
        int old = this.paddingLeft;
        this.paddingLeft = newValue;
        firePropertyChange(PROPERTY_PaddingLeft, old, this.paddingLeft);
    }
    
    public int getPaddingBottom() {
        return paddingBottom;
    }
    public void setPaddingBottom(int newValue) {
        int old = this.paddingBottom;
        this.paddingBottom = newValue;
        firePropertyChange(PROPERTY_PaddingBottom, old, this.paddingBottom);
    }
    
    public int getPaddingTop() {
        return paddingTop;
    }
    public void setPaddingTop(int newValue) {
        int old = this.paddingTop;
        this.paddingTop = newValue;
        firePropertyChange(PROPERTY_PaddingTop, old, this.paddingTop);
    }
    
    public int getPadding() {
        return padding;
    }
    public void setPadding(int newValue) {
        int old = this.padding;
        this.padding = newValue;
        firePropertyChange(PROPERTY_Padding, old, this.padding);
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
        firePropertyChange(PROPERTY_Margin, old, this.margin);
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
        firePropertyChange(PROPERTY_MarginRight, old, this.marginRight);
    }
    public int getMarginLeft() {

        return marginLeft;
    }
    public void setMarginLeft(int newValue) {
        int old = this.marginLeft;
        this.marginLeft = newValue;
        firePropertyChange(PROPERTY_MarginLeft, old, this.marginLeft);
    }
    public int getMarginBottom() {
        return marginBottom;
    }
    public void setMarginBottom(int newValue) {
        int old = this.marginBottom;
        this.marginBottom = newValue;
        firePropertyChange(PROPERTY_MarginBottom, old, this.marginBottom);
    }
    public int getMarginTop() {
        return marginTop;
    }
    public void setMarginTop(int newValue) {
        int old = this.marginTop;
        this.marginTop = newValue;
        firePropertyChange(PROPERTY_MarginTop, old, this.marginTop);
    }
    
    
    
    
    public int getHeight() {
        return height;
    }
    public void setHeight(int newValue) {
        int old = this.height;
        this.height = newValue;
        firePropertyChange(PROPERTY_Height, old, this.height);
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int newValue) {
        int old = this.width;
        this.width = newValue;
        firePropertyChange(PROPERTY_Width, old, this.width);
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
