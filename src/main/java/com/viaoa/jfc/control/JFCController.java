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
package com.viaoa.jfc.control;

import java.io.IOException;
import java.lang.reflect.*;
import java.awt.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.image.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.table.*;
import com.viaoa.object.OAEditMessage;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAPropertyInfo;


/*
      201208    reworked to use OAPropertyPath for parsing the propertyPath
      20120824  using OAProperty annotations
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
public class JFCController extends HubListenerAdapter {
    protected JComponent component;
    
    // main Hub and property
    protected Hub hub;  // hub assigned
    protected Hub hubMultiSelect;
    protected Hub actualHub;  // hub that is closest to property (different then hub if propertyPath has more then one property, one that is Hub)
    protected Object hubObject;  // single object, that will be put in temp hub
    protected Hub hubTemp;
    protected String hubListenerPropertyName; // property name used by hubListener
    
    protected String propertyPath;    // full/original propertyPath
    protected Class setMethodClass;   // type of class at the end of path

    
    private OAPropertyPath oaPropertyPath;
    private String propertyPathToActualHub;
    private String propertyPathFromActualHub;
    private Method[] methodsToActualHub;
    private Method[] methodsFromActualHub;
    private Method methodSet;
    private Method methodValidate;   // in OAObject isValidXxx(newValue)
    
    protected boolean bIsHubCalc;
    
    protected EnabledController controlEnabled;
    protected VisibleController controlVisible;
    
    
    protected Method methodDelegateValidate;  // static method (usually in delegate object), used to validate changes
    
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

    // display collumn width
    private int columns;
    private int propertyInfoDisplayColumns = -2;

    // mini column/chars
    private int miniColumns;
    
    // max column/chars
    private int maxColumns;
    private int propertyInfoMaxColumns = -2;
    private int dataSourceMaxColumns = -2;
    
    private JLabel label;
    
    /**
        Create a component that is not bound to a Hub.
    */  
    public JFCController() {
    }

    public JFCController(JComponent comp) {
        this.component = comp;
    }
    
    /**
        Bind a component to a Hub.
    */  
    public JFCController(Hub hub, JComponent comp) {
        this.component = comp;
        setHub(hub);
        setPropertyPath("");
    }
    
    /**
        Bind a component to a property path in the active object of a Hub.
    */  
    public JFCController(Hub hub, String propertyPath, JComponent comp) {
        this.propertyPath = propertyPath;
        this.component = comp;
        setHub(hub);
    }

    public void setLabel(JLabel lbl) {
        this.label = lbl;
        lbl.setLabelFor(component);
    }
    public JLabel getLabel() {
        return this.label;
    }
    
    
    /**
        Bind a component to an Object.
    */  
    public JFCController(Object hubObject, JComponent comp) {
        this.hubObject = hubObject;
        this.component = comp;
        this.propertyPath = "";
        setObject(hubObject);
    }

    /**
        Bind a component to a property path in an Object.
    */  
    public JFCController(Object hubObject, String propertyPath, JComponent comp) {
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
        update();
    }

    public int getMaxImageWidth() {
        return maxImageWidth;
    }
    public void setMaxImageWidth(int maxImageWidth) {
        this.maxImageWidth = maxImageWidth;
        update();
    }
    
    protected void resetHubOrProperty() {
        closeMain();
        resetActualHub();
        resetHubListener();
        update();
    }

    public String getHubListenerPropertyName() {
        return hubListenerPropertyName;
    }
    
    // have hub listener include other properties
    protected void resetHubListener() {
        if (actualHub == null || propertyPathFromActualHub == null) return;
        
        actualHub.removeHubListener(this);

        String temp = null;
        if (propertyPathFromActualHub != null && propertyPathFromActualHub.indexOf('.') >= 0) {
            temp = propertyPathFromActualHub;
        }
        String[] props = new String[] {  // dependent properties - relative to hub (not actualHub)
            fontProperty,
            backgroundColorProperty,
            foregroundColorProperty,
            imageProperty,
            iconColorProperty,
            toolTipTextProperty,
            temp
        };
        hubListenerPropertyName = propertyPathFromActualHub;
        int pos = hubListenerPropertyName==null ? -1 : hubListenerPropertyName.lastIndexOf('.');
        if (pos >= 0) {
            hubListenerPropertyName = hubListenerPropertyName.substring(pos+1);
        }
        if (actualHub == hub) {
            actualHub.addHubListener(this, hubListenerPropertyName, props, true);
        }
        else {
            // listener for property change
            actualHub.addHubListener(this, hubListenerPropertyName);
            
            // listener for dependent properties (if any)
            for (int i=0; i<props.length; i++) {
                if (props[i] == null || props[i].length() == 0) continue;
                hub.addHubListener(this, hubListenerPropertyName, props);
                break;
            }
        }
    }

    @Override
    public void afterAdd(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void afterRemove(HubEvent e) {
        if (bIsHubCalc) update();
    }
    public void afterRemoveAll(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void afterInsert(HubEvent e) {
        if (bIsHubCalc) update();
    }
    
    public @Override void afterChangeActiveObject(HubEvent e) {
        update();
    }
    
    public void close() {
        closeMain();
    }

    private void closeMain() {
        if (actualHub != null) {
            actualHub.removeHubListener(this);

            if (actualHub != null && hub != actualHub) {
                HubDetailDelegate.removeDetailHub(hub, actualHub);
            }
            actualHub = null;
        }
        if (hub != null) hub.removeHubListener(this);
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
        if (OACompare.isEqual(this.hub, newHub)) return;
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
        Sets the MultiSelect that this component will work with. 
    */
    public void setMultiSelectHub(Hub newHub) {
        this.hubMultiSelect = newHub;
    }
    /**
        Returns the Hub that this component will work with. 
    */
    public Hub getMultiSelectHub() {
        return hubMultiSelect;
    }
    
    /** 
        Returns the Hub that this component is working with. 
        If property path has reference properties (links), then
        a Hub will be returned that matches the Class for the last property.
        <p>k
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
        if (OAString.isEqual(propertyPath, s, true)) return;
        Object obj = hubObject;  // fake out close
        hubObject = null;
        closeMain();
        hubObject = obj;
        propertyPath = s;
        resetHubOrProperty();
    }
    /**
        A dot (".") separated list of property names.

    */
    public String getPropertyPath() {
        return propertyPath;
    }
    /**
        Get the property name that is bound.
        @see HubGuiAdapter#setPropertyPath(String)
    
    public String getPropertyName() {
        return propertyName;
    }
    */
    
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
        update();
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
        update();
    }

    /**
        Get the property name used for displaying an image with component.

    */
    public void setImageProperty(String prop) {
        if (OAString.isEqual(prop, imageProperty, true)) return;;
        this.imageProperty = prop;
        methodsToImage = null;
        getMethodsToImage(); // this will verify
        resetHubListener();
        update();
    }
    /**
        Get the property name used for displaying an image with component.

    */
    public String getImageProperty() {
        return imageProperty;
    }

    /**
        Get the methods used to retrieve the image name from image property.
        @see #getImageProperty
    */
    public Method[] getMethodsToImage() {
        if (imageProperty == null || imageProperty.length() == 0) return null;
        if (methodsToImage == null && hub != null) {
            methodsToImage = OAReflect.getMethods(hub.getObjectClass(), imageProperty);
        }
        return methodsToImage;
    }

    
    private ColorIcon myColorIcon;
    private MultiIcon myMultiIcon;

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

    // 20140404      
    private Class fromParentClass;
    private String fromParentPropertyPath;
    /**
     * This will find the real object to use, in cases where a comp is added to
     * a table, and the table.hub is different then the comp.hub, which could be
     * a detail or link type relationship to the table.hub
     */
    protected Object getRealObject(Object object) {
        if (object == null || hub == null) return object;
        Class c = hub.getObjectClass();
        if (c == null || c.isAssignableFrom(object.getClass())) return object;
        if (!(object instanceof OAObject)) return object;
        
        if (fromParentClass == null || !fromParentClass.equals(object.getClass())) {
            fromParentClass = object.getClass();
            fromParentPropertyPath = OAObjectReflectDelegate.getPropertyPathFromMaster((OAObject)object, getHub());
//            if (fromParentPropertyPath == null) fromParentPropertyPath = propertyPath;
//            else fromParentPropertyPath += "." + propertyPath;
        }
        return OAObjectReflectDelegate.getProperty((OAObject)object, fromParentPropertyPath);
    }
    
    /**
        Returns the icon to use for current object.
    */
    private Icon _getIcon(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
        
        Icon icon = null;
        if (iconColorProperty != null) {
            Color color = getIconColor(obj);
            if (color == null) color = Color.white;
            if (myColorIcon == null) myColorIcon = new ColorIcon();
            myColorIcon.setColor(color);
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
            URL url = JFCController.class.getResource(s);
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
            if (myMultiIcon == null) myMultiIcon = new MultiIcon();
            myMultiIcon.setIcon1(icon);
            myMultiIcon.setIcon2(icon2);
            icon = myMultiIcon;
        }
        return icon;
    }
        
    /** 20120821
     * Find any hub in the propertyPath to use as the actualHub.
     * This is when a propertyPath is used to get a hub from another reference object.
     * Example:  from Employee -&gt; dept.employees.fullName
     */
    protected void resetActualHub() {
        actualHub = this.hub;
        propertyPathToActualHub = null;
        propertyPathFromActualHub = propertyPath;
        methodsToActualHub = null;
        methodsFromActualHub = null;
        methodSet = null;

        
        if (propertyPath == null || hub == null) return;

        // see if there is a hub in the path
        oaPropertyPath = new OAPropertyPath(hub.getObjectClass(), propertyPath);
        Method[] ms = oaPropertyPath.getMethods();
        String[] ss = oaPropertyPath.getProperties();
        
        String ppDetail = null;

        // 20140409
        bIsHubCalc = false;
        if (ms != null && ms.length > 0) {
            Class[] cs = ms[ms.length-1].getParameterTypes();
            bIsHubCalc = cs.length > 0;
        }
        
        for (int i=0; ms != null && i<ms.length; i++) {
            if (ms[i] == null) continue;
            if (ppDetail == null) ppDetail = ss[i];
            else ppDetail += "." + ss[i];
            if (!Hub.class.equals(ms[i].getReturnType())) continue;
            
            propertyPathToActualHub = ppDetail;
            actualHub = hub.getDetailHub(ppDetail);
            methodsToActualHub = new Method[i+1];
            System.arraycopy(ms, 0, methodsToActualHub, 0, i+1);
            
            int x = ms.length-i-1;
            methodsFromActualHub = new Method[x];
            if (x > 0) System.arraycopy(ms, i+1, methodsFromActualHub, 0, ms.length-i-1);
            
            propertyPathFromActualHub = "";
            for (i++; ms != null && i<ms.length; i++) {
                if (propertyPathFromActualHub.length() > 0) propertyPathFromActualHub += ".";
                propertyPathFromActualHub += ss[i];
            }
            break;
        }
        if (methodsFromActualHub == null) methodsFromActualHub = ms;
        
        if (ss != null && ss.length > 0) {
            Class[] cs = oaPropertyPath.getClasses();
            Class c = oaPropertyPath.getFromClass();
            if (cs != null && cs.length > 1) {
                c = cs[cs.length-2];
            }
            String methodName = "set" + ss[ss.length-1];
            methodSet = OAReflect.getMethod(c, methodName, 1);
            if (cs != null) setMethodClass = cs[cs.length-1];
            
            methodName = "isValid" + ss[ss.length-1];
            methodValidate = OAReflect.getMethod(c, methodName, 2);
        }
    }
    public String getPropertyPathToActualHub() {
        return propertyPathToActualHub;
    }
    public String getPropertyPathFromActualHub() {
        return propertyPathFromActualHub;
    }
    
    public Method[] getMethodsToActualHub() {
        return methodsToActualHub;
    }
    public Hub getActualHub(Object objFrom) {
        if (objFrom == null) return null;
        if (methodsFromActualHub == null) return null;
        Object obj = OAReflect.getPropertyValue(objFrom, methodsToActualHub);  // also checks OAObject.isNull
        if (obj instanceof Hub) return (Hub) obj;
        return null;
    }
    public Method[] getMethodsFromActualHub() {
        return methodsFromActualHub;
    }
    
    
    /**
        Returns methods used to get value using property path.
    public Method[] getGetMethods() {
        if (getMethods == null) {
            getMethods = OAReflect.getMethods(hub.getObjectClass(), propertyPath);
        }
        return getMethods;
    }
    */

    public Method getLastMethod() {
        if (methodsFromActualHub == null || methodsFromActualHub.length == 0) return null;
        return methodsFromActualHub[methodsFromActualHub.length-1];
    }
    
  
    /**
     * @param obj object from actual hub
     */
    public Object getPropertyPathValue(Object obj) {

        // 20150424 if using hubSelect only (no propPath), checkBox select column for table
        if (methodsFromActualHub == null || methodsFromActualHub.length == 0) {
            if (hubMultiSelect != null) {
                return hubMultiSelect.contains(obj);
            }
        }
        
        // 20140409
        if (bIsHubCalc) {
            obj = OAObjectReflectDelegate.getProperty(getHub(), propertyPath);
        }
        else {
            if (obj == null) return null;
            if (methodsFromActualHub == null || methodsFromActualHub.length == 0) return obj;
            obj = OAReflect.getPropertyValue(obj, methodsFromActualHub);  // also checks OAObject.isNull
        }
        return obj;
    }
    public String getPropertyPathValueAsString(Object obj, String fmt) {
        if (obj == null) return null;
        if (methodsFromActualHub == null || methodsFromActualHub.length == 0) {
            // 20150424 if using hubSelect only (no propPath), checkBox select column for table
            if (hubMultiSelect != null) {
                return hubMultiSelect.contains(obj)?"True":"False";
            }
            
            return obj.toString();
        }
        String s;
        // 20140409
        if (bIsHubCalc) {
            Object objx = OAObjectReflectDelegate.getProperty(getHub(), propertyPath);
            s = OAConverter.toString(objx, fmt);
        }
        else {
            s = OAReflect.getPropertyValueAsString(obj, methodsFromActualHub, fmt);
        }
        return s;
    }
    public boolean isPropertyPathValueNull(Object obj) {
        if (obj == null) return false;
        if (methodsFromActualHub == null || methodsFromActualHub.length == 0) return false;

        if (!(obj instanceof OAObject)) return false;
        
        if (methodsFromActualHub.length == 1) {
            return OAObjectReflectDelegate.getPrimitiveNull((OAObject)obj, hubListenerPropertyName);
        }
        
        obj = OAReflect.getPropertyValue(obj, methodsFromActualHub, methodsFromActualHub.length-1);
        if (!(obj instanceof OAObject)) return false;
        return OAObjectReflectDelegate.getPrimitiveNull((OAObject)obj, hubListenerPropertyName);
    }
    
    
    /*
    // Returns method used to set value.
    public Method getSetMethod() {
        if (setMethod == null) {
            String methodName;
            if (propertyName != null && propertyName.length() > 0) {
                methodName = "set" + propertyName;
                setMethod = OAReflect.getMethod(actualHub.getObjectClass(), methodName, 1);
            }
        }
        return setMethod;
    }
    */
    
    /**
     * This will validate using isValid(..) callback, isValidXxx property method, and then
     * methodValidate (static delegate method)
     * @param em used to capture invalid message and exception.
     * @param obj object to set
     * @param value new value to validate.
     * @return true if valid, else false
     */
    public boolean isValid(Object obj, Object value, OAEditMessage em) {
        boolean b = true;
        String s = isValid(obj, value);
        if (s != null) {
            if (em != null) em.setMessage(s);
            b = false;
        }
        
        if (b && methodValidate != null) {
            try {
                Class[] cs = methodValidate.getParameterTypes();
                Object result;
                boolean bSentEm = false;
                if (cs.length == 2) {
                    if (OAEditMessage.class.equals(cs[1])) {
                        bSentEm = true;
                        result = methodValidate.invoke(obj, value, em);
                    }
                    else {
                        bSentEm = true;
                        result = methodValidate.invoke(obj, em, value);
                    }
                }
                else {
                    result = methodValidate.invoke(obj, value);
                }

                if (result instanceof Boolean) {
                    b = ((Boolean) result).booleanValue();
                }
                else if (result instanceof String) {
                    if (em != null && !bSentEm) em.setMessage((String) result);
                    b = false;
                }
                else if (result != null) {
                    if (em != null && !bSentEm) em.setMessage( result.toString());
                    b = false;
                }
            }
            catch (Exception e) {
                if (em != null) em.setThrowable(e);
                b = false;
            }
        }
        if (b && methodDelegateValidate != null) {
            try {
                Class[] cs = methodDelegateValidate.getParameterTypes();
                Object result = null;
                
                boolean bSentEm = false;
                if (cs.length == 3) {
                    if (OAEditMessage.class.equals(cs[2])) {
                        bSentEm = true;
                        result = methodDelegateValidate.invoke(null, obj, value, em);
                    }
                    else if (OAEditMessage.class.equals(cs[1])) {
                        bSentEm = true;
                        result = methodDelegateValidate.invoke(null, obj, em, value);
                    }
                }
                else if (cs.length == 2) {
                    result = methodDelegateValidate.invoke(null, obj, value);
                }
                
                if (result instanceof Boolean) b = ((Boolean) result).booleanValue();
                else if (result instanceof String) {
                    if (!bSentEm && em != null) em.setMessage((String) result);
                    b = false;
                }
                else if (result != null) {
                    if (!bSentEm && em != null) em.setMessage( result.toString());
                    b = false;
                }
            }
            catch (Exception e) {
                if (em != null) {
                    em.setThrowable(e);
                }
                b = false;
            }
        }
        return b;
    }
    
    // 20120822
    public void setPropertyPathValue(Object obj, Object value) {
        setPropertyPathValue(obj, value, null);
    }
    public void setPropertyPathValue(Object obj, Object value, String fmt) {
        if (obj == null) return;
        int x = methodsFromActualHub == null ? 0 : methodsFromActualHub.length;
        if (x > 1) {
            obj = OAReflect.getPropertyValue(obj, methodsFromActualHub, x-1);
            if (obj == null) return;
        }
        boolean bWasNull = value == null && (fmt == null || fmt.length() == 0);
        
        value = OAConv.convert(setMethodClass, value, fmt);
        OAReflect.setPropertyValue(obj, methodSet, value);
        
        // 20140815
        if ((value == null) || bWasNull) {
        // was: if (value == null) {
            Class c = OAReflect.getClass(getLastMethod());
            if (c.isPrimitive() && obj instanceof OAObject) {
                ((OAObject) obj).setNull(hubListenerPropertyName);
            }
        }
    }
    /**
     * Converts a value to correct type needed for setMethod
     */
    public Object getConvertedValue(Object value, String fmt) {
        value = OAConv.convert(setMethodClass, value, fmt);
        return value;
    }
    
    
    /**
     * Sets up the validation delegate class to use for validating changes.
     * 
     * @param delegteClass
     * @param methodName must be for a method that params for: 
     * object, new value, and OAEditMessage, that returns null or boolean.  
     * If it returns a String, then it will be used as the error message text.
     * @return true if method was found, else false.
     */
    public boolean setValidationMethod(Class delegteClass, String methodName) {
        if (methodName == null || methodName.length() == 0) {
            methodDelegateValidate = null;
            return true;
        }
        methodDelegateValidate = OAReflect.getMethod(delegteClass, methodName, 2);
        return (methodDelegateValidate != null);
    }

    public String validateNewValue(Object obj, Object newValue) {
        if (methodDelegateValidate == null) return null;
        
        Object result;
        try {
            newValue = getConvertedValue(newValue, getFormat());
            /* was
            if (getMethod != null) {
                 newValue = OAConv.convert(getMethod.getReturnType(), newValue);
            }
            */
            OAEditMessage em = new OAEditMessage();
            isValid(obj, newValue, em);
            
            result = methodDelegateValidate.invoke(null, obj, newValue);
        }
        catch (Exception e) {
            result = e.getMessage();
        }
        
        if (result == null) return null;
        if (result instanceof String) return (String) result;
        if (result instanceof Boolean) {
            if ( ((Boolean) result).booleanValue()) return null; // valid
            else return "invalid value";
        }
        return result+"";
    }
    

    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format == null && oaPropertyPath != null) {
            format = oaPropertyPath.getFormat();
        }
        return format;
    }

    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
/*qqqqqq is this being used    
    protected String getFormat(Method method) {
        if (format == null) {
            return OAConverter.getFormat( OAReflect.getClass(method) );
        }
        return format;
    }
*/
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        this.format = fmt;
        update();
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

    public EnabledController getEnabledController() {
        if (controlEnabled == null) {
            controlEnabled = new EnabledController(this.component) {
                @Override
                protected boolean isValid(boolean bIsCurrentlyEnabled) {
                    return JFCController.this.isEnabled(bIsCurrentlyEnabled);
                }
            };
            controlEnabled.update();
        }
        return controlEnabled;
    }
    
    public void setEnabled(Hub hub, String prop) {
        getEnabledController().add(hub, prop);
    }
    // this is called by enabledController, so that it can be overwritten.
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }

    public VisibleController getVisibleController() {
        if (controlVisible == null) {
            controlVisible = new VisibleController(this.component) {
                @Override
                protected boolean isValid(boolean bIsCurrentlyVisible) {
                    return JFCController.this.isVisible(bIsCurrentlyVisible);
                }
            };
            controlVisible.update();
        }
        return controlVisible;
    }
    public void setVisible(Hub hub, String prop) {
        getVisibleController().add(hub, prop);
    }
    // this is called by VisibleController, so that it can be overwritten.
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    
    
    
    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    protected String isValid(Object object, Object value) {
        return null;
    }
    
    public void setFont(Font font) {
        this.font = font;
        update();
    }
    public Font getFont() {
        return this.font;
    }
    public void setFontProperty(String s) {
        if (OAString.isEqual(s, fontProperty, true)) return;;
        fontProperty = s;
        methodsToFont = null;
        getMethodsToFont();
        resetHubListener();
        update();
    }
    public String getFontProperty() {
        return fontProperty;
    }
    public Method[] getMethodsToFont() {
        if (methodsToFont == null && hub != null) {
            if (fontProperty != null && fontProperty.length() > 0) {
                methodsToFont = OAReflect.getMethods(hub.getObjectClass(), fontProperty);
            }
        }
        return methodsToFont;
    }
    public Font getFont(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.font;
        if (hub == null) return this.font;
        Font font = null;
        if (fontProperty != null && obj != null && getMethodsToFont() != null) {
            font = (Font) com.viaoa.util.OAConv.convert(Font.class, OAReflect.getPropertyValue(obj, methodsToFont));
        }
        if (font == null) font = this.font;
        return font;
    }

    public void setBackground(Color c) {
        this.colorBackground = c;
        update();
    }
    public Color getBackground() {
        return this.colorBackground;
    }
    public void setForeground(Color c) {
        this.colorForeground = c;
        update();
    }
    public Color getForeground() {
        return this.colorForeground;
    }

    public void setBackgroundColorProperty(String s) {
        if (OAString.isEqual(s, backgroundColorProperty, true)) return;;
        backgroundColorProperty = s;
        methodsToBackgroundColor = null;
        getMethodsToBackgroundColor();
        resetHubListener();
        update();
    }
    public String getBackgroundColorProperty() {
        return backgroundColorProperty;
    }

    public void setForegroundColorProperty(String s) {
        if (OAString.isEqual(s, foregroundColorProperty, true)) return;;
        foregroundColorProperty = s;
        methodsToForegroundColor = null;
        getMethodsToForegroundColor();
        resetHubListener();
        update();
    }
    public String getForegroundColorProperty() {
        return foregroundColorProperty;
    }
    
    public Method[] getMethodsToForegroundColor() {
        if (methodsToForegroundColor == null && hub != null) {
            if (foregroundColorProperty != null && foregroundColorProperty.length() > 0) {
                methodsToForegroundColor = OAReflect.getMethods(hub.getObjectClass(), foregroundColorProperty);
            }
        }
        return methodsToForegroundColor;
    }
    public Color getForegroundColor(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.colorForeground;
        if (hub == null) return this.colorForeground;
        Color color = null;
        if (foregroundColorProperty != null && obj != null && getMethodsToForegroundColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToForegroundColor));
        }
        if (color == null) color = this.colorForeground;
        return color;
    }

    public Method[] getMethodsToBackgroundColor() {
        if (methodsToBackgroundColor == null && hub != null) {
            if (backgroundColorProperty != null && backgroundColorProperty.length() > 0) {
                methodsToBackgroundColor = OAReflect.getMethods(hub.getObjectClass(), backgroundColorProperty);
            }
        }
        return methodsToBackgroundColor;
    }
    public Color getBackgroundColor(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.colorBackground;
        if (hub == null) return this.colorBackground;
        Color color = null;
        if (backgroundColorProperty != null && obj != null && getMethodsToBackgroundColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToBackgroundColor));
        }
        if (color == null) color = this.colorBackground;
        return color;
    }

    public void setIconColorProperty(String s) {
        if (OAString.isEqual(s, iconColorProperty, true)) return;;
        iconColorProperty = s;
        methodsToIconColor = null;
        getMethodsToIconColor();
        resetHubListener();
        update();
    }
    public String getIconColorProperty() {
        return iconColorProperty;
    }
    public Method[] getMethodsToIconColor() {
        if (methodsToIconColor == null && hub != null) {
            if (iconColorProperty != null && iconColorProperty.length() > 0) {
                methodsToIconColor = OAReflect.getMethods(hub.getObjectClass(), iconColorProperty);
            }
        }
        return methodsToIconColor;
    }
    public Color getIconColor(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
        Color color = null;
        if (iconColorProperty != null && obj != null && getMethodsToIconColor() != null) {
            color = (Color) com.viaoa.util.OAConv.convert(Color.class, OAReflect.getPropertyValue(obj, methodsToIconColor));
        }
        return color;
    }
    
    public void setToolTipTextProperty(String s) {
        if (OAString.isEqual(s, toolTipTextProperty, true)) return;;
        toolTipTextProperty = s;
        methodsToToolTipText = null;
        resetHubListener();
        update();
    }
    public String getToolTipTextProperty() {
        return toolTipTextProperty;
    }
    public Method[] getMethodsToToolTipText() {
        if (methodsToToolTipText == null && hub != null) {
            if (toolTipTextProperty != null && toolTipTextProperty.length() > 0) {
                methodsToToolTipText = OAReflect.getMethods(hub.getObjectClass(), toolTipTextProperty);
            }
        }
        return methodsToToolTipText;
    }
    public String getToolTipText(Object obj) {
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return null;
        if (hub == null) return null;
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
        update();
    }

    
    private Border borderFocus;
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!isSelected && !hasFocus) {
            label.setForeground( UIManager.getColor(table.getForeground()) );
            label.setBackground( UIManager.getColor(table.getBackground()) );
        }

        Object obj = value;
        if (getHub() != null) {
            label.setHorizontalTextPosition(label.getHorizontalTextPosition());
            label.setIcon( getIcon(getHub().elementAt(row)) );

            Hub h = getHub();  // could be a link hub
            if (table instanceof OATable) {
                h = ((OATable) table).getHub();
            }
            obj = h.elementAt(row);
        }
        update(label, obj);

        if (isSelected || hasFocus) {
            label.setForeground( UIManager.getColor("Table.selectionForeground") );
            label.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        if (hasFocus) {
            if (borderFocus == null) {
                borderFocus = new CompoundBorder(UIManager.getBorder("Table.focusCellHighlightBorder"), new LineBorder(UIManager.getColor("Table.focusCellBackground"),1));
            }
            label.setBorder( borderFocus );
        }
        else label.setBorder(null);
        return label;
    }

    
    
    public void update(JComponent comp, Object value) {
        // note: renderer.text is already set.
        Font font = getFont(value);
        if (font != null) comp.setFont(font);

        if (comp instanceof JLabel) {
            ((JLabel)comp).setIcon(getIcon(value));
        }
        Color c = getBackgroundColor(value);
        if (c != null) comp.setBackground(c);
        c = getForegroundColor(value);
        if (c != null) comp.setForeground(c);
        if (getToolTipTextProperty() != null) {
            String s = getToolTipText(value);
            if (s == null) s = "";
            comp.setToolTipText(s);
        }
        
        getEnabledController().update();
        getVisibleController().update();
    }

    /**
     *  Called to have component update itself.  
     *  By default, should call updateEnabled, updateVisible
     *  Most subclasses will then call update(comp, value) to update component icon/colors/font,etc
     */
    protected void update() {
    }


    public void setColumns(int x) {
        this.columns = x;
    }
    public int getColumns() {
        return this.columns;
    }
    public void setMinimumColumns(int x) {
        this.miniColumns = x;
    }
    public int getMinimumColumns() {
        return this.miniColumns;
    }

    public void setMaximumColumns(int x) {
        maxColumns = x;
    }
    public int getMaximumColumns() {
        return maxColumns;
    }

    public int getPropertyInfoDisplayColumns() {
        if (propertyInfoDisplayColumns == -2) {
            getPropertyInfoMaxColumns();
        }
        return propertyInfoDisplayColumns;
    }
    
    public int getPropertyInfoMaxColumns() {
        if (propertyInfoMaxColumns == -2) {
            Hub h = getActualHub();
            if (h == null) return propertyInfoMaxColumns;
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(h.getObjectClass());
            OAPropertyInfo pi = oi.getPropertyInfo(getPropertyPathFromActualHub());
            
            propertyInfoMaxColumns = (pi == null) ? -1 : pi.getMaxLength();
            propertyInfoDisplayColumns  = (pi == null) ? -1 : pi.getDisplayLength();

            Method method = getLastMethod();
            if (method != null) {
                if (method.getReturnType().equals(String.class)) {
                    if (propertyInfoMaxColumns > 254) propertyInfoMaxColumns = 254;
                }
                else propertyInfoMaxColumns = -1;
            }
        }
        return propertyInfoMaxColumns;
    }

    
    public int getDataSourceMaxColumns() {
        if (dataSourceMaxColumns == -2) {
            
            // 20151023
            // annotation OAColumn has ds max length
            int x = getPropertyInfoMaxColumns();
            if (x > 0) {
                dataSourceMaxColumns = x;
                return x;
            }
            
            Hub h = getActualHub();
            if (h == null) return dataSourceMaxColumns;
            OADataSource ds = OADataSource.getDataSource(h.getObjectClass());
            if (ds != null) {
                dataSourceMaxColumns = -1;
                dataSourceMaxColumns = ds.getMaxLength(h.getObjectClass(), getPropertyPathFromActualHub());
                Method method = getLastMethod();
                if (method != null) {
                    if (method.getReturnType().equals(String.class)) {
                        if (dataSourceMaxColumns > 254) dataSourceMaxColumns = -1;
                    }
                    else dataSourceMaxColumns = -1;
                }
            }
        }
        return dataSourceMaxColumns;
    }
    
}


