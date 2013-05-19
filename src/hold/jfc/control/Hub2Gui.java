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
package com.viaoa.jfc.control;

import java.io.IOException;
import java.lang.reflect.*;
import java.awt.*;
import java.net.*;

import javax.swing.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.jfc.image.ScaledImageIcon;
import com.viaoa.jfc.*;
import com.viaoa.jfc.table.*;

/* Todo:
 * 
 * review Editable and readonly - based on parent?
 * 
 */


/**
    Implements the HubListener and provides most of the methods required for creating
    controller Classes (Model/View/Controller) for UI components.  
    <p>
    Used by the OA "Hub2XXX" to be able to bind a component to a Hub, so that both the Hub and the 
    component automatically work together.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class Hub2Gui extends HubListenerAdapter {
    protected Component component;
    protected Hub hub;  // hub assigned
    protected Hub actualHub;  // hub that is closest to property (different then hub if propertyPath has more then one property)
    protected Object hubObject;
    protected Hub hubTemp;
    

    protected String propertyName;
    protected String propertyPath;
    protected Method getMethod, setMethod, enableMethod, validMethod;  // from ActualHub and propertyName
    protected Method[] getMethods;  // from propertyPath

    protected boolean readOnly;
    protected String format;
    protected boolean bEnableUndo=true;

    // FONT
    protected Font font;
    protected String fontProperty;
    protected Method[] methodsToFont;
    
    // COLORS
    protected Color colorBackground;
    protected String backgroundColorProperty;
    Method[] methodsToBackgroundColor;
    protected Color colorForeground;
    protected String foregroundColorProperty;
    protected Method[] methodsToForegroundColor;

    // Image sizing
    protected int maxImageHeight, maxImageWidth;
    
    
    protected String imagePath;
    protected String imageProperty;
    protected Method[] methodsToImage;

    protected String iconColorProperty;
    protected Method[] methodsToIconColor;

    protected String toolTipTextProperty;
    protected Method[] methodsToToolTipText;
    
    protected String nullDescription = "";
    public boolean bDebug;  // used for debugging a single component. ex: ((OALabel)lbl).setDebug(true)    
    
    /**
        Create a component that is not bound to a Hub.
    */  
    public Hub2Gui() {
    }

    /**
        Bind a component to a Hub.
    */  
    public Hub2Gui(Hub hub, Component comp) {
        this.component = comp;
        setHub(hub);
        setPropertyPath("");
    }
    
    /**
        Bind a component to a property path in the active object of a Hub.
    */  
    public Hub2Gui(Hub hub, String propertyPath, Component comp) {
        this.propertyPath = propertyPath;
        this.component = comp;
        setHub(hub);
    }

    /**
        Bind a component to an Object.
    */  
    public Hub2Gui(Object hubObject, Component comp) {
        this.hubObject = hubObject;
        this.component = comp;
        this.propertyPath = "";
        setObject(hubObject);
    }

    /**
        Bind a component to a property path in an Object.
    */  
    public Hub2Gui(Object hubObject, String propertyPath, Component comp) {
        this.hubObject = hubObject;
        this.component = comp;
        this.propertyPath = propertyPath;
        setObject(hubObject);
    }

    
    public int getMaxImageHeight() {
        return maxImageHeight;
    }
    public void setMaxImageHeight(int maxImageHeight) {
        this.maxImageHeight = maxImageHeight;
    }

    public int getMaxImageWidth() {
        return maxImageWidth;
    }
    public void setMaxImageWidth(int maxImageWidth) {
        this.maxImageWidth = maxImageWidth;
    }
    
    protected void resetHubOrProperty() {
        close();
        getMethod = null;
        getMethods = null;
        setMethod = null;
        validMethod = null;
        enableMethod = null;
        propertyName = "";
        actualHub = null;

        if (hub != null && propertyPath != null) {
            actualHub = getPathHub(hub,propertyPath);  // this could create detailHubs, use hub.removeHubDetail(actualHub)
            propertyName = getPropertyName(propertyPath);
            actualHub.addHubListener(this, propertyName);

            getGetMethods();
            
            //added 2/5/99  need to also get events for hub to
            //      know when object are added or deleted
            if (hub != actualHub) hub.addHubListener(this);
        }
        else {
            actualHub = hub;
        }
    }

    /**
        Removes Hub listener.
    */
    public void close() {
        // called by finalize, setPropertyPath, setHub
        if (actualHub != null) {
            actualHub.removeHubListener(this, propertyName);
            //not needed -> if (hub != actualHub) hub.removeHubLink(hub);
            if (actualHub != null && hub != actualHub) {
                HubDetailDelegate.removeDetailHub(hub, actualHub);
            }
            actualHub = null;
        }
    }

    /**
        Flag to enable undo, default is true.
    */
    public void setEnableUndo(boolean b) {
        bEnableUndo = b;
    }
    public boolean getEnableUndo() {
        return bEnableUndo;
    }

    protected void finalize() throws Throwable {
        close();
        if (hubObject != null) {
            HubTemp.deleteHub(hubObject);
            hubObject = null;
        }
        super.finalize();
    }

    /**
        Sets the Hub that this component will work with. 
    */
    public void setHub(Hub newHub) {
        close();
        this.hub = newHub;
        resetHubOrProperty();
    }
    /**
        Returns the Hub that this component will work with. 
    */
    public Hub getHub() {
        return hub;
    }

    /** 
        Returns the Hub that this component is working with. 
        If property path has reference properties (links), then
        a Hub will be returned that matches the Class for the last property.
        <p>
        Example:<br>
        Hub with ObjectClass = Order.class <br>
        property path "order.cust.salesrep.name" <br>
        will have a property name = "name" and actualHub with ObjectClass = SalesRep.class
    */
    public Hub getActualHub() {
        return actualHub;
    }

    /**
        Returns the Object that is being used.  Internally, it will be "wrapped" in a temporary Hub and
        made the active object for that temporary Hub.
    */
    public Object getObject() {
        return hubObject;
    }

    /**
        The Object that is being used.  It will be "wrapped" in a temporary Hub.
    */
    public void setObject(Object obj) {
        HubTemp.deleteHub(hubObject);
        hubObject = obj;
        hubTemp = HubTemp.createHub(obj);
        setHub(hubTemp);
    }

    /**
        A dot (".") separated list of property names that are used to navigate from 
        an object to a property value.  OA uses <i>Java Reflection</i> to dynamically 
        access the value.
        <p>
        Example:<br>
        A property path to retrieve an employees department manager's last name:
        "department.manager.lastName"
    */
    public void setPropertyPath(String s) {
        if (propertyPath == null || !s.equals(propertyPath)) {
            Object obj = hubObject;  // fake out close
            hubObject = null;
            close();
            hubObject = obj;
            propertyPath = s;
            resetHubOrProperty();
        }
    }
    /**
        A dot (".") separated list of property names.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyPath() {
        return propertyPath;
    }
    /**
        Get the property name that is bound.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyName() {
        return propertyName;
    }

    
    /**
        Root directory path where images are stored.
    */
    public void setImagePath(String s) {
        if (s != null) {
            s += "/";
            s = OAString.convert(s, "\\", "/");
            s = OAString.convert(s, "//", "/");
        }
        this.imagePath = s;
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePath() {
        return imagePath;
    }

    private String imageClassPath;
    private Class rootImageClassPath;;
    /**
        Class path where images are stored.
    */
    public void setImageClassPath(Class root, String path) {
        this.rootImageClassPath = root;
        this.imageClassPath = path;
    }

    
    
    /**
        Get the property name used for displaying an image with component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setImageProperty(String prop) {
        this.imageProperty = prop;
        methodsToImage = null;
        getMethodsToImage(); // this will verify
    }
    /**
        Get the property name used for displaying an image with component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getImageProperty() {
        return imageProperty;
    }

    /**
        Get the methods used to retrieve the image name from image property.
        @see #getImageProperty
    */
    public Method[] getMethodsToImage() {
        if (imageProperty == null) return null;
        if (methodsToImage == null && hub != null) {
            methodsToImage = OAReflect.getMethods(hub.getObjectClass(), imageProperty);
        }
        return methodsToImage;
    }

    /**
    Returns the icon to use for the current object.
    */
/** 2007/09/17 qqqqqqqqqqqqvvvvvvvvvv    
    public ImageIcon getImageIcon() {
        if (getHub() == null) return null;
        if (getHub().getAO() == null) return null;
        return getImageIcon(getHub().getAO());
    }

    public ImageIcon getImageIcon(Object obj) {
        if (obj == null || obj instanceof OANullObject) return null;
        getMethodsToImage();
        if (methodsToImage == null) return null;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return null;

        String s = ClassModifier.getPropertyValueAsString(obj, methodsToImage);
        if (s == null || s.length() == 0) return null;
        if (getImagePath() != null && getImagePath().length() > 0) {
            s = getImagePath() + s;
        }
        URL url = OATreeNode.class.getResource(s);
        if (url != null) return new ImageIcon(url);
        
        s = OAString.convertFileName(s);
        ImageIcon ii = new ImageIcon(s);

        if (maxImageWidth > 0 || maxImageHeight > 0) {
            ScaledImageIcon sii = new ScaledImageIcon(ii, maxImageWidth, maxImageHeight);
            ii = new ImageIcon(sii.getImage());
        }
        return ii;
    }
**/ 
    
    
    private MyColorIcon myColorIcon;
    private MyMultiIcon myMultiIcon;
    public Icon getIcon() {
        if (getHub() == null) return null;
        if (getHub().getAO() == null) return null;
        return getIcon(getHub().getAO());
    }
    public Icon getIcon(Object obj) {
        Icon icon = _getIcon(obj);
        if (icon != null && (maxImageWidth > 0 || maxImageHeight > 0)) {
            icon = new ScaledImageIcon(icon, maxImageWidth, maxImageHeight);
        }
        return icon;
    }    
    
    /**
        Returns the icon to use for current object.
    */
    private Icon _getIcon(Object obj) {
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return null;
        Icon icon = null;
        if (iconColorProperty != null) {
            Color color = getIconColor(obj);
            if (color == null) color = Color.white;
            if (myColorIcon == null) myColorIcon = new MyColorIcon();
            myColorIcon.color = color;
            icon = myColorIcon;
        }
        
        getMethodsToImage();
        if (methodsToImage == null) return icon;

        Icon icon2 = null;
        
        Class returnClass = methodsToImage[methodsToImage.length-1].getReturnType();
        if (Icon.class.isAssignableFrom(returnClass)) {
            icon2 = (Icon) OAReflect.getPropertyValue(obj, methodsToImage);
        }
        else if (returnClass.equals(byte[].class)) {
            byte[] bs = (byte[]) OAReflect.getPropertyValue(obj, methodsToImage);
            try {
                if (bs != null) {
                    Image img = OAImageUtil.convertToBufferedImage(bs);
                    if (img != null) icon2 = new ImageIcon(img);
                }
            }
            catch (IOException ex) {
            }
        }
        else {
            String s = OAReflect.getPropertyValueAsString(obj, methodsToImage);
            if (s == null || s.length() == 0) return icon;
            if (getImagePath() != null && getImagePath().length() > 0) {
                s = getImagePath() + s;
            }
            URL url = Hub2Gui.class.getResource(s);
            if (url != null) icon2 = new ImageIcon(url);
            else {
                s = OAString.convertFileName(s);
                icon2 = new ImageIcon(s);
            }
        }        
        
        if (icon == null) {
            icon = icon2;
            icon2 = null;
        }
        if (icon2 != null) {
            if (myMultiIcon == null) myMultiIcon = new MyMultiIcon();
            myMultiIcon.icon1 = icon;
            myMultiIcon.icon2 = icon2;
            icon = myMultiIcon;
        }
        return icon;
    }
        

    protected Hub getPathHub(Hub base, String propertyPath) {
        int pos = propertyPath.lastIndexOf('.');
        if (pos < 0) return base;
        return base.getDetailHub(propertyPath.substring(0,pos));
    }
    protected String getPropertyName(String propertyPath) {
        if (propertyPath.indexOf('.') >= 0) {
            int pos = propertyPath.lastIndexOf('.');
            propertyPath = propertyPath.substring(pos+1);
        }
        return propertyPath;
    }

    /**
        Returns methods used to get value using property path.
    */
    public Method[] getGetMethods() {
        if (getMethods == null) {
            getMethods = OAReflect.getMethods(hub.getObjectClass(), propertyPath);
        }
        return getMethods;
    }

    
    
    
    /**
        Returns method to get value from last property in property path.
    */
    public Method getGetMethod() {
        if (getMethod == null) {
            String methodName = null;
            if (propertyName != null && propertyName.length() > 0) methodName = "get" + propertyName;
            else {
                if (actualHub == null || !actualHub.isOAObject()) methodName = "toString";
            }
            if (methodName != null) {
                if (actualHub != null) getMethod = OAReflect.getMethod(actualHub.getObjectClass(), methodName, 0);
                if (getMethod == null) {
                    Class c = null;
                    if (actualHub != null) c = actualHub.getObjectClass();
                    else if (hub != null) c = hub.getObjectClass();
                    else if (hubObject != null) c = hubObject.getClass();
                    throw new RuntimeException("method="+methodName + " class="+c); 
                }
            }
        }
        return getMethod;
    }
    /**
        Returns method used to set value.
    */
    public Method getSetMethod() {
        if (setMethod == null && !readOnly) {
            String methodName;
            if (propertyName != null && propertyName.length() > 0) {
                methodName = "set" + propertyName;
                setMethod = OAReflect.getMethod(actualHub.getObjectClass(), methodName, 1);
                if (setMethod == null) readOnly = true;
            }
            else readOnly = true;
        }
        return setMethod;
    }

    public Method getValidMethod() {
        if (validMethod == null) {
            String methodName;
            if (propertyName != null && propertyName.length() > 0) {
                methodName = "isValid" + propertyName;
                validMethod = OAReflect.getMethod(actualHub.getObjectClass(), methodName, 2);
                if (validMethod != null) {
                    Class[] cs = validMethod.getParameterTypes();
                    if (cs == null || cs.length != 2 || !cs[1].equals(OAEditMessage.class)) validMethod = null;
                    
                }
            }
        }
        return validMethod;
    }

    private boolean bEnableMethodFlag;
    public Method getEnableMethod() {
        if (enableMethod == null && !bEnableMethodFlag) {
            bEnableMethodFlag = true;
            String methodName;
            if (propertyName != null && propertyName.length() > 0) {
                methodName = "isEnabled" + propertyName;
                enableMethod = OAReflect.getMethod(actualHub.getObjectClass(), methodName, 1);
            }
        }
        return enableMethod;
    }

    /**
        Returns if component is read only.
    */
    public boolean getReadOnly() {
        if (!readOnly && setMethod == null) getSetMethod();
        return readOnly;
    }

    /**
        Make the component read only.
    */
    public void setReadOnly(boolean b) {
        this.readOnly = b;
    }


    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format == null) {
            return OAConverter.getFormat( OAReflect.getClass(getGetMethod()) );
        }
        return format;
    }

    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    protected String getFormat(Method method) {
        if (format == null) {
            return OAConverter.getFormat( OAReflect.getClass(method) );
        }
        return format;
    }

    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        format = fmt;
    }

    /**
        Utility used to "see" if this component or any of its parent containers are disabled.
    */
    public static boolean isParentEnabled(Component comp) {
        // if any parent is disabled, then this must be disabled
        if (comp == null) return false;
        Container cont = comp.getParent();
        for ( ;cont != null; ) {
            if (!cont.isEnabled()) return false;
            cont = cont.getParent();
        }

        if (comp instanceof OATableComponent) {
            OATable tab =  ((OATableComponent) comp).getTable();
            if (tab != null) return isParentEnabled(tab);
        }
        
        return true;
    }
    
    // 2004/09/05
    /**
        Checks the following to see if component should be enabled for an Object.
        <ol>
        <li>If object has an "isEnabledXXX" method, then it will be called.
        <li>Checks to see if any of the components parents are disabled.
        <li>Checks to see if is readOnly, ex: a setXX method does not exist.
        </ol>
    */
    public boolean isEnabled(Component comp, Object obj) {
        boolean b = true;
        Method m = getEnableMethod();
        if (m != null) {
            Object[] objs = new Object[] { null };
            try {
                Boolean B = (Boolean) m.invoke(obj, objs);
                if (B != null) b = B.booleanValue();
            }
            catch (Exception ex) {
            }
        }
        if (b) {
            b = isParentEnabled(comp);
            if (b) b = !getReadOnly();
        }
        return b;
    }

    public void setFont(Font font) {
        this.font = font;
    }
    public Font getFont() {
        return this.font;
    }
    public void setFontProperty(String s) {
        fontProperty = s;
        methodsToFont = null;
        getMethodsToFont();
    }
    public String getFontProperty() {
        return fontProperty;
    }
    public Method[] getMethodsToFont() {
        if (methodsToFont == null && hub != null) {
            methodsToFont = OAReflect.getMethods(hub.getObjectClass(), fontProperty);
        }
        return methodsToFont;
    }
    public Font getFont(Object obj) {
        if (obj == null || obj instanceof OANullObject) return this.font;
        if (hub == null) return this.font;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return this.font;
        Font font = null;
        if (fontProperty != null && obj != null && getMethodsToFont() != null) {
            font = (Font) com.viaoa.util.OAConv.convert(Font.class, OAReflect.getPropertyValue(obj, methodsToFont));
        }
        if (font == null) font = this.font;
        return font;
    }

    public void setBackground(Color c) {
        this.colorBackground = c;
    }
    public Color getBackground() {
        return this.colorBackground;
    }
    public void setForeground(Color c) {
        this.colorForeground = c;
    }
    public Color getForeground() {
        return this.colorForeground;
    }

    public void setBackgroundColorProperty(String s) {
        backgroundColorProperty = s;
        methodsToBackgroundColor = null;
        getMethodsToBackgroundColor();
    }
    public String getBackgroundColorProperty() {
        return backgroundColorProperty;
    }

    public void setForegroundColorProperty(String s) {
        foregroundColorProperty = s;
        methodsToForegroundColor = null;
        getMethodsToForegroundColor();
    }
    public String getForegroundColorProperty() {
        return foregroundColorProperty;
    }
    
    public Method[] getMethodsToForegroundColor() {
        if (methodsToForegroundColor == null && hub != null) {
            methodsToForegroundColor = OAReflect.getMethods(hub.getObjectClass(), foregroundColorProperty);
        }
        return methodsToForegroundColor;
    }
    public Color getForegroundColor(Object obj) {
        if (obj == null || obj instanceof OANullObject) return this.colorForeground;
        if (hub == null) return this.colorForeground;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return this.colorForeground;
        Color color = null;
        if (foregroundColorProperty != null && obj != null && getMethodsToForegroundColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToForegroundColor));
        }
        if (color == null) color = this.colorForeground;
        return color;
    }

    public Method[] getMethodsToBackgroundColor() {
        if (methodsToBackgroundColor == null && hub != null) {
            methodsToBackgroundColor = OAReflect.getMethods(hub.getObjectClass(), backgroundColorProperty);
        }
        return methodsToBackgroundColor;
    }
    public Color getBackgroundColor(Object obj) {
        if (obj == null || obj instanceof OANullObject) return this.colorBackground;
        if (hub == null) return this.colorBackground;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return this.colorBackground;
        Color color = null;
        if (backgroundColorProperty != null && obj != null && getMethodsToBackgroundColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToBackgroundColor));
        }
        if (color == null) color = this.colorBackground;
        return color;
    }

    public void setIconColorProperty(String s) {
        iconColorProperty = s;
        methodsToIconColor = null;
        getMethodsToIconColor();
    }
    public String getIconColorProperty() {
        return iconColorProperty;
    }
    public Method[] getMethodsToIconColor() {
        if (methodsToIconColor == null && hub != null) {
            methodsToIconColor = OAReflect.getMethods(hub.getObjectClass(), iconColorProperty);
        }
        return methodsToIconColor;
    }
    public Color getIconColor(Object obj) {
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return null;
        Color color = null;
        if (iconColorProperty != null && obj != null && getMethodsToIconColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToIconColor));
        }
        return color;
    }
    
    public void setToolTipTextProperty(String s) {
        toolTipTextProperty = s;
        methodsToToolTipText = null;
    }
    public String getToolTipTextProperty() {
        return toolTipTextProperty;
    }
    public Method[] getMethodsToToolTipText() {
        if (methodsToToolTipText == null && hub != null) {
            methodsToToolTipText = OAReflect.getMethods(hub.getObjectClass(), toolTipTextProperty);
        }
        return methodsToToolTipText;
    }
    public String getToolTipText(Object obj) {
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
        Class c = hub.getObjectClass();
        if (c == null || !c.isAssignableFrom(obj.getClass())) return null;
        String tt = null;
        if (toolTipTextProperty != null && obj != null && getMethodsToToolTipText() != null) {
            tt = OAReflect.getPropertyValueAsString(obj, methodsToToolTipText);
        }
        return tt;
    }

    public String getNullDescription() {
        return nullDescription;
    }
    /** 
        The "word(s)" to use for the empty slot (null value).  
        <p>
        Example: "none of the above".
        <p>
        Default: "" (blank).  Set to null if none should be used
    */
    public void setNullDescription(String s) {
        nullDescription = s;
    }

    public void updateComponent(final Component comp, final Object obj, final String text) {
        if (SwingUtilities.isEventDispatchThread()) {
            _updateComponent(comp, obj, text);
        }
        else {
//qqqqqqqqqqqqqqqqqq            
_updateComponent(comp, obj, text);
/**qqqqqqqqqqq
            try {
            SwingUtilities.invokeAndWait( new Runnable() { // this could be in the startup thread, which needs to set renderer components to get sizing 
                public void run() {
                    _updateComponent(comp, obj, text);
                }
            });
            }
            catch (Exception e) {
                
            }
**/         
        }
    }    
    
    protected void _updateComponent(Component comp, Object obj, String text) {
        Font font = getFont(obj);
        if (font != null && comp != null) comp.setFont(font);
        if (comp instanceof JLabel) {
            if (text == null) text = "";
            // text = OAString.format(text, getFormat());   20081013 text should already be formatted, otherwise it could use the wrong type of format, since it is being used with String and not the property class type
            JLabel lbl = (JLabel) comp;
            if (obj == null || obj instanceof OANullObject) {
                text = getNullDescription();
                if (text == null) text = "";
                else if (text.length() == 0) text = " ";  // 2007/08/31 this needs to use a space, otherwise it will have a size of 0.
            }
            lbl.setText(text);
            lbl.setIcon(getIcon(obj));
            Color c = getBackgroundColor(obj);
            if (c != null) lbl.setBackground(c);
            c = getForegroundColor(obj);
            if (c != null) lbl.setForeground(c);
            if (getToolTipTextProperty() != null) {
                String s = getToolTipText(obj);
                if (s == null) s = "";
                lbl.setToolTipText(s);
            }
        }       
        else if (comp instanceof JRadioButton) {
            if (text == null) text = "";
            // text = OAString.format(text, getFormat());   20081013 text should already be formatted, otherwise it could use the wrong type of format, since it is being used with String and not the property class type
            JRadioButton rad = (JRadioButton) comp;
            if (obj == null || obj instanceof OANullObject) text = getNullDescription();
            if (text == null) text = "";
            rad.setText(text);
            Color c = getBackgroundColor(obj);
            if (c != null) rad.setBackground(c);
            c = getForegroundColor(obj);
            if (c != null) rad.setForeground(c);
            if (getToolTipTextProperty() != null) {
                String s = getToolTipText(obj);
                if (s == null) s = "";
                rad.setToolTipText(s);
            }
        }       
        else if (comp instanceof JTextArea) {
            if (text == null) text = "";
            // text = OAString.format(text, getFormat());   20081013 text should already be formatted, otherwise it could use the wrong type of format, since it is being used with String and not the property class type
            JTextArea txt = (JTextArea) comp;
            if (obj == null || obj instanceof OANullObject) text = getNullDescription();
            if (text == null) text = "";
            txt.setText(text);
            Color c = getBackgroundColor(obj);
            if (c != null) txt.setBackground(c);
            c = getForegroundColor(obj);
            if (c != null) txt.setForeground(c);
            if (getToolTipTextProperty() != null) {
                String s = getToolTipText(obj);
                if (s == null) s = "";
                txt.setToolTipText(s);
            }
        }       
        else if (comp instanceof JFormattedTextField) {
            if (text == null) text = "";
            JFormattedTextField txt = (JFormattedTextField) comp;
            txt.setValue(text);
        }
        else if (comp instanceof JTextField) {
            if (text == null) text = "";
            // text = OAString.format(text, getFormat());   20081013 text should already be formatted, otherwise it could use the wrong type of format, since it is being used with String and not the property class type
            JTextField txt = (JTextField) comp;
            if (obj == null || obj instanceof OANullObject) text = getNullDescription();
            if (text == null) text = "";
            if (comp instanceof OATextField) {
                ((OATextField)txt).setSelectionStart(0);
                ((OATextField)txt).setSelectionEnd(0);
                ((OATextField)txt).setText(text, false);
            }
            else txt.setText(text);
            Color c = getBackgroundColor(obj);
            if (c != null) txt.setBackground(c);
            c = getForegroundColor(obj);
            if (c != null) txt.setForeground(c);
            if (getToolTipTextProperty() != null) {
                String s = getToolTipText(obj);
                if (s == null) s = "";
                txt.setToolTipText(s);
            }
        }       
        else if (comp instanceof JPasswordField) {
            if (text == null) text = "";
            text = OAString.format(text, getFormat());
            JPasswordField txt = (JPasswordField) comp;
            if (obj == null || obj instanceof OANullObject) text = getNullDescription();
            if (text == null) text = "";
            txt.setText(text);
            Color c = getBackgroundColor(obj);
            if (c != null) txt.setBackground(c);
            c = getForegroundColor(obj);
            if (c != null) txt.setForeground(c);
            if (getToolTipTextProperty() != null) {
                String s = getToolTipText(obj);
                if (s == null) s = "";
                txt.setToolTipText(s);
            }
        }       
        // qqq Support for other components.  Ex: checkbox, textfield, etc.
    }

    protected boolean bManuallyDisabled, bInternallyCallingEnabled; // flags for changing "enabled"
    protected void setInternalEnabled(boolean b) {
        if (component == null) return;
        if (bManuallyDisabled) b = false;
        bInternallyCallingEnabled = true;
        changeEnabled(b);
        bInternallyCallingEnabled = false;
    }

    protected void changeEnabled(boolean b) {
        component.setEnabled(b);
    }
    
    
    public void setEnabled(boolean b) {
        if (!bInternallyCallingEnabled) {
            bManuallyDisabled = !b;
        }
    }
}


class MyColorIcon implements Icon {
    Color color;        
    public int getIconHeight() {
        return 17;
    }
    public int getIconWidth() {
        return 12;
    }

    public void paintIcon(Component c,Graphics g,int x,int y) {
        g.setColor(color==null?Color.white:color);
        g.fillRoundRect(x+1,y+3,11,11,2,2);
        // g.fillOval(2,5,8,8);
    }
}

class MyMultiIcon implements Icon {
    Icon icon1, icon2;
    int gap = 0;
    public int getIconHeight() {
        int x = 0;
        if (icon1 != null) {
            x = icon1.getIconHeight();
        }
        if (icon2 != null) x = Math.max(x, icon2.getIconHeight());
        return x;
    }
    public int getIconWidth() {
        int x = 0;
        if (icon1 != null) {
            x = icon1.getIconWidth();
            if (icon2 != null) x += gap;
        }
        if (icon2 != null) x += icon2.getIconWidth();
        return x;
    }

    public void paintIcon(Component c,Graphics g,int x,int y) {
        if (icon1 != null) {
            icon1.paintIcon(c, g, x, y);
            x += icon1.getIconWidth();
            if (icon2 != null) x += gap;
        }
        if (icon2 != null) {
            icon2.paintIcon(c, g, x, y);
        }
    }
}




