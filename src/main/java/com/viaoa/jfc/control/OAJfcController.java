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
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.*;
import com.viaoa.hub.HubChangeListener.HubProp;
import com.viaoa.util.*;
import com.viaoa.jfc.image.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.table.*;
import com.viaoa.object.OAObjectEditQuery;
import com.viaoa.object.OAObjectEditQueryDelegate;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.object.OAThreadLocalDelegate;


/**
    Base controller class for OA JFC/Swing components.

    Implements the HubListener and provides most of the methods required for creating
    controller Classes (Model/View/Controller) for UI components.  
*/
public class OAJfcController extends HubListenerAdapter {
    private static Logger LOG = Logger.getLogger(OAJfcController.class.getName());
    
    public boolean DEBUG;  // used for debugging a single component. ex: ((OALabel)lbl).setDebug(true)    

    protected final JComponent component;
    
    protected Hub hub;
    protected String propertyPath;
    protected OAPropertyPath oaPropertyPath;

    protected Class endPropertyFromClass;  // oaObj class (same as hub, or class for pp end)
    protected String endPropertyName;
    protected Class endPropertyClass;
    
    
    protected String hubListenerPropertyName;
    
    protected Object hubObject;  // single object, that will be put in temp hub
    protected Hub hubTemp;

    protected Hub hubLink; // link hub for Hub
    protected String linkPropertyName; 
    
    protected boolean bUseLinkHub;
    protected final boolean bUseEditQuery;

    protected HubChangeListener.Type hubChangeListenerType;
    
    protected boolean bIsHubCalc;
    protected boolean bEnableUndo=true;
    protected String undoDescription;
    
    protected Hub hubSelect;
    
    protected String format;
    protected Font font;
    protected String fontPropertyPath;
    protected Color colorBackground;
    protected String backgroundColorPropertyPath;
    protected Color colorForeground;
    protected String foregroundColorPropertyPath;
    protected Color colorIcon;
    protected String iconColorPropertyPath;

    private String confirmMessage;

    // Image sizing
    protected int maxImageHeight, maxImageWidth;
    
    protected Image image;
    protected String imageDirectory;
    protected String imageClassPath;
    protected Class rootImageClassPath;;
    protected String imagePropertyPath;

    protected String toolTipTextPropertyPath;
    protected String nullDescription = "";

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
    // should label always match comp.enabled? Default=false: will not disable label if AO!=null (view only mode)
    private boolean bLabelAlwaysMatchesComponentEnabled;
    private Hub hubForLabel; // hub that controls label.enabled

    
    private ColorIcon myColorIcon;
    private MultiIcon myMultiIcon;

    private MyHubChangeListener changeListener; // listens for any/all hub+propPaths needed for component
    private MyHubChangeListener changeListenerEnabled;
    private MyHubChangeListener changeListenerVisible;


    
    /** HTML used for displaying in some components (label, combo, list, autocomplete), and used for table cell rendering */
    protected String displayTemplate;
    private OATemplate templateDisplay;

    protected String toolTipTextTemplate;
    private OATemplate templateToolTipText;
    
    
    public OAJfcController(Hub hub, Object object, String propertyPath, JComponent comp, HubChangeListener.Type type, final boolean bUseLinkHub, final boolean bUseEditQuery) {
        this.hub = hub;
        this.hubObject = object;
        this.propertyPath = propertyPath;
        this.component = comp;
        this.bUseLinkHub = bUseLinkHub && (hub != null) && (hub.getLinkHub() != null);
        this.bUseEditQuery = bUseEditQuery;
        this.hubChangeListenerType = type;
        
        reset();
    }

    // used to track the last values used by reset 
    private Hub hubLast;
    private Object hubObjectLast; 
    private HubChangeListener.HubProp hubChangeListenerTypeLast; 

    
    // called when hub, property, etc is changed.
    // does not include resetting HubChangeListeners (changeListener, visibleChangeListener, enabledChangeListener)
    protected void reset() {
        // note: dont call close, want to keep visibleChangeListener, enabledChangeListener
        if (hubLast != null) {
            hubLast.removeHubListener(this);
        }
        if (hubObjectLast != null) {
            HubTemp.deleteHub(hubObjectLast);
        }
        if (changeListenerEnabled != null && hubChangeListenerTypeLast != null) {
            changeListenerEnabled.remove(hubChangeListenerTypeLast);
            hubChangeListenerTypeLast = null;
        }
        if (changeListener != null) {
            changeListener.close();
            changeListener = null;
        }
        
        if (hub != null) {
            this.hubTemp = null;
            this.hubObject = null;
        }
        else {
            if (hubObject == null) {
                this.hub = null;
                this.hubTemp = null;
            }
            else {
                this.hub = this.hubTemp = HubTemp.createHub(hubObject);
            }
        }

        hubObjectLast = hubObject;
        hubLast = this.hub;
        
        if (this.hub == null) return;
        
        if (propertyPath != null && propertyPath.indexOf('.') >= 0) {
            hubListenerPropertyName = propertyPath.replace('.', '_');
            hub.addHubListener(this, hubListenerPropertyName, new String[] {propertyPath}, true);
        }
        else {
            hubListenerPropertyName = propertyPath; 
            if (OAString.isNotEmpty(hubListenerPropertyName)) hub.addHubListener(this, hubListenerPropertyName, true);
            else hub.addHubListener(this);
        }

        oaPropertyPath = new OAPropertyPath(hub.getObjectClass(), propertyPath);
        final String[] properties = oaPropertyPath.getProperties();
        endPropertyName = (properties == null || properties.length == 0) ? null : properties[properties.length-1];
        
        if (hubChangeListenerType != null) { // else: this class already is listening to hub
            hubChangeListenerTypeLast = getEnabledChangeListener().add(hub, hubChangeListenerType);
        }
        
        Method[] ms = oaPropertyPath.getMethods();
        endPropertyFromClass = hub.getObjectClass();
        if (ms != null && ms.length > 0) {
            Class[] cs = ms[ms.length-1].getParameterTypes();
            bIsHubCalc = cs.length == 1 && cs[0].equals(Hub.class);
            endPropertyClass = ms[ms.length-1].getReturnType();
            
            if (ms.length > 1) {
                endPropertyFromClass = ms[ms.length-2].getReturnType();
            }
        }
        else {
            bIsHubCalc = false;
            endPropertyClass = String.class;
        }
        bDefaultFormat = false;
        
        if (!bUseLinkHub) {
            if (bUseEditQuery) {
                Class cz = hub.getObjectClass();
                String ppPrefix = "";
                int cnt = 0;
                for (String prop : properties) {
                    if (cnt == 0) {
                        addEnabledEditQueryCheck(hub, prop); 
                        addVisibleEditQueryCheck(hub, prop);
                    }
                    else {
                        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, cz, prop, ppPrefix, getEnabledChangeListener(), true);
                        OAObjectEditQueryDelegate.addEditQueryChangeListeners(hub, cz, prop, ppPrefix, getVisibleChangeListener(), false);
                    }
                    ppPrefix += prop + ".";
                    cz = oaPropertyPath.getClasses()[cnt++];
                }

                if (cnt == 0) {
                    addEnabledEditQueryCheck(hub, null);
                    addVisibleEditQueryCheck(hub, null);
                }
            }
        }
        else {
            hubLink = hub.getLinkHub();
            linkPropertyName = hub.getLinkPath();
            if (hubLink != null) {
                getEnabledChangeListener().add(hubLink, HubChangeListener.Type.AoNotNull);
                if (bUseEditQuery) {
                    // check to see if linkToHub needs to be added to changeListener
                    addEnabledEditQueryCheck(hubLink, linkPropertyName);
                    // addVisibleEditQueryCheck(hubLink, linkPropertyName);
                }
            }
        }
        update();
    }

    
    
    public void bind(Hub hub, String propertyPath, boolean bUseLinkHub) {
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.bUseLinkHub = bUseLinkHub;
        reset();
    }
    public void bind(Hub hub, String propertyPath) {
        this.hub = hub;
        this.propertyPath = propertyPath;
        reset();
    }
    
    
    public Hub getHub() {
        return hub;
    }
    public void setHub(Hub hub) {
        this.hub = hub;
        reset();
    }
    public Object getObject() {
        return hubObject;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propPath) {
        propertyPath = propPath;
        reset();
    }

    public Component getComponent() {
        return component;
    }
    
    
    public String getEndPropertyName() {
        return endPropertyName;
    }
    public Class getEndPropertyClass() {
        return endPropertyClass;
    }
    public Class getEndPropertyFromClass() {
        return endPropertyFromClass;
    }

    public String getHubListenerPropertyName() {
        return hubListenerPropertyName;
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        if (hubObject != null) {
            HubTemp.deleteHub(hubObject);
        }
        if (changeListener != null) {
            changeListener.close();
            changeListener = null;
        }
        if (changeListenerEnabled != null) {
            changeListenerEnabled.close();
            changeListenerEnabled = null;
        }
        if (changeListenerVisible != null) {
            changeListenerVisible.close();
            changeListenerVisible = null;
        }
        if (hub != null) hub.removeHubListener(this);
    }
    

    /**
        Returns the Hub that this component will work with. 
    */
    public Hub getSelectHub() {
        return hubSelect;
    }
    /**
        Sets the MultiSelect that this component will work with. 
    */
    public void setSelectHub(Hub newHub) {
        if (hubSelect != null) {
            getChangeListener().remove(hubSelect);
        }
        this.hubSelect = newHub;
        if (hubSelect != null) {
            getChangeListener().add(hubSelect);
        }
    }
    
    /**
     * This will find the real object in this hub use, in cases where a comp is added to
     * a table, and the table.hub is different then the comp.hub, which could be
     * a detail or link type relationship to the table.hub
     */
    private Class fromParentClass;
    private String fromParentPropertyPath;
    protected Object getRealObject(Object fromObject) {
        if (fromObject == null || hub == null) return fromObject;
        Class c = hub.getObjectClass();
        if (c == null || c.isAssignableFrom(fromObject.getClass())) return fromObject;
        if (!(fromObject instanceof OAObject)) return fromObject;
        
        if (fromParentClass == null || !fromParentClass.equals(fromObject.getClass())) {
            fromParentClass = fromObject.getClass();
            fromParentPropertyPath = OAObjectReflectDelegate.getPropertyPathFromMaster((OAObject)fromObject, getHub());
        }
        return OAObjectReflectDelegate.getProperty((OAObject)fromObject, fromParentPropertyPath);
    }
    
    public Object getValue(Object obj) {
        obj = getRealObject(obj);
        if (obj == null) return null;

        if (OAString.isEmpty(propertyPath) && hubSelect != null) {
            return hubSelect.contains(obj);
        }

        if (bIsHubCalc) {
            obj = OAObjectReflectDelegate.getProperty(getHub(), propertyPath);
        }
        else {
            if (obj == null) return null;
            if (OAString.isEmpty(propertyPath)) return obj;
            if (!(obj instanceof OAObject)) return obj;
            obj = ((OAObject) obj).getProperty(propertyPath);
        }
        return obj;
    }
    public String getValueAsString(Object obj) {
        return getValueAsString(obj, getFormat());
    }
    public String getValueAsString(Object obj, String fmt) {
        obj = getValue(obj);
        String s = OAConv.toString(obj, fmt);
        return s;
    }
    
    // calls the set method on the actualHub.ao
    public void setValue(Object value) {
        String fmt = getFormat();
        Object obj = getHub().getAO();
        setValue(obj, value, fmt);
    }
    public void setValue(Object obj, Object value) {
        String fmt = getFormat();
        setValue(obj, value, fmt);
    }
    public void setValue(Object obj, Object value, String fmt) {
        if (obj == null) return;
        if (obj instanceof OAObject) {
            ((OAObject) obj).setProperty(propertyPath, value, fmt);
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

    /**
        Popup message used to confirm button click before running code.
    */
    public void setConfirmMessage(String msg) {
        confirmMessage = msg;
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return confirmMessage;
    }

    @Override
    public void beforePropertyChange(HubEvent e) {
        // TODO Auto-generated method stub
        super.beforePropertyChange(e);
    }
    
    
    
    /**
     * confirm a new change.
     */
    protected boolean confirmPropertyChange(final Object obj, Object newValue) {
        String confirmMessage = getConfirmMessage();
        String confirmTitle = "Confirm";

        if (obj instanceof OAObject) {
            Object objx = obj;
            String prop;
            if (bUseLinkHub && hubLink != null) prop = linkPropertyName;
            else {
                if (oaPropertyPath != null && oaPropertyPath.hasLinks()) {
                    prop = endPropertyName;
                    objx = oaPropertyPath.getLastLinkValue(obj);
                }
                else prop = propertyPath;
            }
            OAObjectEditQuery em = OAObjectEditQueryDelegate.getConfirmPropertyChangeEditQuery((OAObject)objx, prop, newValue, confirmMessage, confirmTitle);
            confirmMessage = em.getConfirmMessage();
            confirmTitle = em.getConfirmTitle();
        }
        
        boolean result = true;
        if (OAString.isNotEmpty(confirmMessage)) {
            if (OAString.isEmpty(confirmTitle)) confirmTitle = "Confirmation";
                     
            if (confirmMessage != null && confirmMessage.indexOf("<%=") >= 0 && obj instanceof OAObject) {
                OATemplate temp = new OATemplate(confirmMessage);
                temp.setProperty("newValue", newValue); // used by <%=$newValue%>
                confirmMessage = temp.process((OAObject) obj);
                if (confirmMessage != null && confirmMessage.indexOf('<') >=0 && confirmMessage.toLowerCase().indexOf("<html>") < 0) confirmMessage = "<html>" + confirmMessage; 
            }
            
            int x = JOptionPane.showOptionDialog(getWindow(), confirmMessage, confirmTitle, 0, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, "Yes");
            result = (x == 0);
        }
        return result;
    }

    protected Window getWindow() {
        return OAJfcUtil.getWindow(component);
    }
    
    /**
     * Used to confirm changing AO when hub is link to another hub.
     */
    protected boolean confirmHubChangeAO(final Object objNew) {
        if (!bUseLinkHub || hubLink == null) return true;
        if (!(objNew instanceof OAObject)) return true;
        return confirmPropertyChange(hubLink.getAO(), objNew);
    }
    
    /**
     * Used to verify a property change.
     * @return null if no errors, else error message
     */
    protected String isValidHubChangeAO(final Object objNew) {
        if (!bUseLinkHub || hubLink == null) return null;
        if (!(objNew instanceof OAObject)) return null;
        
        Object obj = hubLink.getAO();
        if (!(obj instanceof OAObject)) return null;
        
        OAObject oaObj = (OAObject) obj;
        OAObjectEditQuery em = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery(oaObj, linkPropertyName, null, objNew);

        String result = null;
        if (!em.getAllowed()) {
            result = em.getResponse();
            Throwable t = em.getThrowable();
            if (OAString.isEmpty(result)) {
                if (t != null) {
                    for (; t!=null; t=t.getCause()) {
                        result = t.getMessage();
                        if (OAString.isNotEmpty(result)) break;
                    }
                    if (OAString.isEmpty(result)) result = em.getThrowable().toString();
                }
                else result = "invalid value";
            }
        }
        return result;
    }
    
    
    
    /**
     * Converts a value to correct type needed for setMethod
     */
    public Object getConvertedValue(Object value, String fmt) {
        if (hubLink == null && !bUseLinkHub) {
            value = OAConv.convert(endPropertyClass, value, fmt);
        }
        return value;
    }
    
    /**
     * Used to verify a property change.
     * @return null if no errors, else error message
     */
    public String isValid(final Object obj, Object newValue) {
        if (!bUseEditQuery) return null;
        if (!(obj instanceof OAObject)) return null;
        OAObject oaObj = (OAObject) obj;
        
        String fmt = getFormat();
        newValue = getConvertedValue(newValue, fmt);

        Object objx = obj;
        String prop;
        if (hubLink != null) prop = linkPropertyName;
        else {
            if (oaPropertyPath != null && oaPropertyPath.hasLinks()) {
                prop = endPropertyName;
                objx = oaPropertyPath.getLastLinkValue(obj);
            }
            else prop = propertyPath;
        }
        OAObjectEditQuery em = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery((OAObject)objx, prop, null, newValue);

        String result = null;
        if (!em.getAllowed()) {
            result = em.getResponse();
            Throwable t = em.getThrowable();
            if (OAString.isEmpty(result)) {
                if (t != null) {
                    for (; t!=null; t=t.getCause()) {
                        result = t.getMessage();
                        if (OAString.isNotEmpty(result)) break;
                    }
                    if (OAString.isEmpty(result)) result = em.getThrowable().toString();
                }
                else result = "invalid value";
            }
        }
        return result;
    }
    
    private boolean bDefaultFormat;
    private String defaultFormat;
    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format != null) return format;
        if (!bDefaultFormat) {
            bDefaultFormat = true;
            if (oaPropertyPath != null) {
                defaultFormat = oaPropertyPath.getFormat();
            }
            if (defaultFormat == null) {
                defaultFormat = OAConverter.getFormat(endPropertyClass);
            }
        }
        
        if (bUseEditQuery) {
            Object objx = hub.getAO();
            if (objx instanceof OAObject) {
                String prop;
                if (oaPropertyPath != null && oaPropertyPath.hasLinks()) {
                    objx = oaPropertyPath.getLastLinkValue(objx);
                }
                return OAObjectEditQueryDelegate.getFormat((OAObject)objx, endPropertyName, defaultFormat);
            }
        }
        return defaultFormat;
    }

    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        set to "" (blank) for no formatting.  If null, then the default format will be used.
        @see OADate#OADate
        see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        this.format = fmt;
        bDefaultFormat = true;
        defaultFormat = null;
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

    
    public void setFont(Font font) {
        this.font = font;
        update();
    }
    public Font getFont() {
        return this.font;
    }
    public void setFontPropertyPath(String pp) {
        fontPropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getFontProperty() {
        return fontPropertyPath;
    }
    public Font getFont(Object obj) {
        if (OAString.isEmpty(fontPropertyPath)) return this.font;
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.font;
        if (!(obj instanceof OAObject)) return this.font;
        if (hub == null) return this.font;

        Object objx = ((OAObject) obj).getProperty(fontPropertyPath);
        Font font = (Font) OAConv.convert(Font.class, objx);
        return font;
    }

    public void setForegroundColor(Color c) {
        this.colorForeground = c;
        update();
    }
    public Color getForegroundColor() {
        return this.colorForeground;
    }
    public void setForegroundColorPropertyPath(String pp) {
        foregroundColorPropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getForegroundColorPropertyPath() {
        return foregroundColorPropertyPath;
    }
    public Color getForegroundColor(Object obj) {
        if (OAString.isEmpty(foregroundColorPropertyPath)) return this.colorForeground;
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.colorForeground;
        if (!(obj instanceof OAObject)) return this.colorForeground;
        if (hub == null) return this.colorForeground;

        Object objx = ((OAObject) obj).getProperty(foregroundColorPropertyPath);
        Color color = (Color) OAConv.convert(Color.class, objx);
        return color;
    }
    
    public void setBackgroundColor(Color c) {
        this.colorBackground = c;
        update();
    }
    public Color getBackgroundColor() {
        return this.colorBackground;
    }
    public void setBackgroundColorPropertyPath(String pp) {
        backgroundColorPropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getBackgroundColorPropertyPath() {
        return backgroundColorPropertyPath;
    }
    public Color getBackgroundColor(Object obj) {
        if (OAString.isEmpty(backgroundColorPropertyPath)) return this.colorBackground;
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.colorBackground;
        if (!(obj instanceof OAObject)) return this.colorBackground;
        if (hub == null) return this.colorBackground;

        Object objx = ((OAObject) obj).getProperty(backgroundColorPropertyPath);
        Color color = (Color) OAConv.convert(Color.class, objx);
        return color;
    }

    
    public void setIconColor(Color c) {
        this.colorIcon = c;
        update();
    }
    public Color getIconColor() {
        return this.colorIcon;
    }
    public void setIconColorPropertyPath(String pp) {
        iconColorPropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getIconColorPropertyPath() {
        return iconColorPropertyPath;
    }
    public Color getIconColor(Object obj) {
        if (OAString.isEmpty(iconColorPropertyPath)) return this.colorIcon;
        obj = getRealObject(obj);
        if (obj == null || obj instanceof OANullObject) return this.colorIcon;
        if (!(obj instanceof OAObject)) return this.colorIcon;
        if (hub == null) return this.colorIcon;

        Object objx = ((OAObject) obj).getProperty(iconColorPropertyPath);
        Color color = (Color) OAConv.convert(Color.class, objx);
        return color;
    }
    
    public void setToolTipTextPropertyPath(String pp) {
        toolTipTextPropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getToolTipTextPropertyPath() {
        return toolTipTextPropertyPath;
    }
    
    /**
        Root directory path where images are stored.
    */
    public void setImageDirectory(String s) {
        if (s != null) {
            s += "/";
            s = OAString.convert(s, "\\", "/");
            s = OAString.convert(s, "//", "/");
        }
        this.imageDirectory = s;
        update();
    }
    /**
        Root directory path where images are stored.
    */
    public String getImageDirectory() {
        return imageDirectory;
    }
    /**
        Class path where images are stored.
    */
    public void setImageClassPath(Class root, String path) {
        this.rootImageClassPath = root;
        this.imageClassPath = path;
        update();
    }
    public void setImage(Image img) {
        this.image = img;
        update();
    }
    public Image getImage() {
        return this.image;
    }
    public void setImagePropertyPath(String pp) {
        imagePropertyPath = pp;
        if (OAString.isNotEmpty(pp)) getChangeListener().add(hub, pp);
        update();
    }
    public String getImagePropertyPath() {
        return imagePropertyPath;
    }
    
    
    public Icon getIcon() {
        if (this.image == null) {
            ImageIcon ii = new ImageIcon(this.image);
            return ii;
        }
        Color color = getIconColor();
        if (color == null) return null;
        
        ColorIcon ci = new ColorIcon();
        ci.setColor(color);
        return ci;
    }
    public Icon getIcon(Object obj) {
        Icon icon = _getIcon(obj);
        if (icon != null && (maxImageWidth > 0 || maxImageHeight > 0)) {
            icon = new ScaledImageIcon(icon, maxImageWidth, maxImageHeight);
        }
        return icon;
    }    
    private Icon _getIcon(Object object) {
        object = getRealObject(object);
        if (object == null || object instanceof OANullObject) return null;
        if (!(object instanceof OAObject)) return null;
        if (hub == null) return null;
        
        OAObject obj = (OAObject) object;
        
        Icon icon = null;
        if (iconColorPropertyPath != null) {
            Color color = getIconColor(obj);
            if (color == null) color = Color.white;
            if (myColorIcon == null) myColorIcon = new ColorIcon();
            myColorIcon.setColor(color);
            icon = myColorIcon;
        }
        
        Icon icon2 = null;
    
        Object objx = obj.getProperty(propertyPath);
        
        if (objx instanceof Icon) {
            icon2 = (Icon) objx;
        }
        else if (objx instanceof byte[]) {
            byte[] bs = (byte[]) objx;
            try {
                Image img = OAImageUtil.convertToBufferedImage(bs);
                if (img != null) icon2 = new ImageIcon(img);
            }
            catch (IOException ex) {
            }
        }
        else if (OAString.isNotEmpty(getImageDirectory()) && objx instanceof String && ((String) objx).length() > 0) {
            String s = (String) objx;
            if (OAString.isNotEmpty(getImageDirectory())) {
                s = getImageDirectory() + "/" + s;
            }
            URL url = OAJfcController.class.getResource(s);
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
        else if (icon2 != null) {
            if (myMultiIcon == null) myMultiIcon = new MultiIcon();
            myMultiIcon.setIcon1(icon);
            myMultiIcon.setIcon2(icon2);
            icon = myMultiIcon;
        }
        return icon;
    }
    
    /** 
    The "word(s)" to use for the empty slot (null value).  
    Example: "none of the above".
    Default: "" (blank).  Set to null if none should be used
    */
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
        update();
    }

    
    
    
    
    /**
     * Used to listen to additional changes that will then call this.update()
     */
    public HubChangeListener getChangeListener() {
        if (changeListener != null) return changeListener;
        changeListener = new MyHubChangeListener() {
            @Override
            protected void onChange() {
                OAJfcController.this.update();
            }
        };
        return changeListener;
    }
    
    public HubChangeListener getEnabledChangeListener() {
        if (changeListenerEnabled != null) return changeListenerEnabled;
        changeListenerEnabled = new MyHubChangeListener() {
            @Override
            protected void onChange() {
                OAJfcController.this.update();
            }
        };
        return changeListenerEnabled;
    }
    
    public HubChangeListener getVisibleChangeListener() {
        if (changeListenerVisible != null) return changeListenerVisible;
        changeListenerVisible = new MyHubChangeListener() {
            @Override
            protected void onChange() {
                OAJfcController.this.update();
            }
        };
        return changeListenerVisible;
    }
    
    
    
    
    public HubProp addEnabledCheck(Hub hub, String pp) {
        return getEnabledChangeListener().add(hub, pp);
    }
    public HubProp addEnabledCheck(Hub hub, String pp, Object value) {
        return getEnabledChangeListener().add(hub, pp, value);
    }
    public HubProp addEnabledCheck(Hub hub, String property, HubChangeListener.Type type) {
        return getEnabledChangeListener().add(hub, property, type);
    }
    public HubProp addEnabledCheck(Hub hub, HubChangeListener.Type type) {
        return getEnabledChangeListener().add(hub, type);
    }
    public HubProp addEnabledEditQueryCheck(Hub hub, String propertyName) {
        return getEnabledChangeListener().addEditQueryEnabled(hub, propertyName);
    }
    
    
    public HubProp addVisibleCheck(Hub hub, String pp) {
        return getVisibleChangeListener().add(hub, pp);
    }
    public HubProp addVisibleCheck(Hub hub, String pp, Object value) {
        return getVisibleChangeListener().add(hub, pp, value);
    }
    public HubProp addVisibleCheck(Hub hub, String property, HubChangeListener.Type type) {
        return getVisibleChangeListener().add(hub, property, type);
    }
    public HubProp addVisibleEditQueryCheck(Hub hub, String propertyName) {
        return getVisibleChangeListener().addEditQueryVisible(hub, propertyName);
    }
    
    
    
    private HubEvent lastUpdateHubEvent;
//qqq Test
//int cntUpdate;    
    
    /**
     *  Called to have component update itself.  
     */
    public void update() {
/*qqq Test        
    System.out.printf((++cntUpdate)+") %s %s %s\n", 
        hub!=null ? hub.getObjectClass().getSimpleName() : "", 
        propertyPath, 
        component != null ? component.getClass().getSimpleName() : ""
    );
*/
        final HubEvent he = OAThreadLocalDelegate.getCurrentHubEvent();
        if (lastUpdateHubEvent != null &&  (he == lastUpdateHubEvent)) {
            return;
        }
        lastUpdateHubEvent = he;
        
        if (component == null) return;
        Object obj;
        if (hub != null) obj = hub.getAO();
        else obj = null;
        update(component, obj, true);
        updateEnabled();        
        updateVisible();      
        updateLabel(component, obj);
    }

    public void updateLabel(final JComponent comp, Object object) {
        JLabel lbl = getLabel();
        if (lbl != null) {
            OAObject oaobj = null;
            if (object instanceof OAObject) oaobj = (OAObject) object;
            String s = bUseLinkHub ? linkPropertyName : propertyPath;
            OAObjectEditQueryDelegate.updateLabel(oaobj, s, lbl);
        }
    }
    
    
    /**
     * @param comp can be used for this.component, or another, ex: an OAList renderer (label)
     */
    public void update(final JComponent comp, Object object, boolean bIncudeToolTip) {
        if (comp == null) return;
        object = getRealObject(object);
        Font font = getFont(object);
        if (font != null) comp.setFont(font);

        JLabel lblThis;
        if (comp instanceof JLabel && !(comp instanceof OAFunctionLabel)) {
            lblThis = (JLabel) comp;
        }
        else lblThis = null;
        
        if (lblThis != null) {
            lblThis.setIcon(getIcon(object));
        }
        Color c = getBackgroundColor(object);
        if (c != null) comp.setBackground(c);
        c = getForegroundColor(object);
        
        if (c != null) comp.setForeground(c);

        // tooltip
        if (bIncudeToolTip) {
            String tt = component.getToolTipText();
            //was: String tt = comp.getToolTipText();
            tt = getToolTipText(object, tt);
            component.setToolTipText(tt);
            //was: comp.setToolTipText(tt);
            if (comp == component && label != null) label.setToolTipText(tt);
        }
        
        if (lblThis != null && (getPropertyPath() != null || object instanceof String)) {
            String text;
            if (object == null) text = "";
            else {
                OATemplate temp = getTemplateForDisplay();
                if (temp != null && (object instanceof OAObject)) {
                    text = templateDisplay.process((OAObject) object);
                    if (text != null && text.indexOf('<') >=0 && text.toLowerCase().indexOf("<html>") < 0) text = "<html>" + text; 
                }
                else {
                    Object obj = getValue(object);
                    text = OAConv.toString(obj, getFormat());
                }
                if (text == null) {
                    String s = getFormat();
                    if (OAString.isNotEmpty(s)) text = OAConv.toString(null, s);
                }
            }
            lblThis.setText(text);

            if (object != null) {
                if (bUseEditQuery) {
                    try {
                        if (object instanceof OAObject) {
                            Object objx = object;
                            if (oaPropertyPath != null && oaPropertyPath.hasLinks()) {
                                objx = oaPropertyPath.getLastLinkValue(objx);
                            }
                            OAObjectEditQueryDelegate.renderLabel((OAObject)objx, endPropertyName, (JLabel)  comp);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("OAJfcController.update exception: "+e);
                    }
                }
                if (lblThis instanceof OAJfcComponent) {
                    int pos = getHub().getPos(object);
                    ((OAJfcComponent) lblThis).customizeRenderer(lblThis, object, object, false, false, pos, false, false);
                }
            }
        }
    }

    private HubEvent lastUpdateEnabledHubEvent;
    private boolean bLastUpdateEnabled;
    public boolean updateEnabled() {
        final HubEvent he = OAThreadLocalDelegate.getCurrentHubEvent(); 
        if (he == null || he != lastUpdateEnabledHubEvent) {
            lastUpdateEnabledHubEvent = he;
            bLastUpdateEnabled = updateEnabled(component, hub==null ? null : hub.getAO());
        }
        return bLastUpdateEnabled;
    }
    public boolean updateEnabled(final JComponent comp, final Object object) {
        if (comp == null) return false;
        boolean bEnabled = getEnabledChangeListener().getValue();
        bEnabled = isEnabled(bEnabled);

        final boolean bEnabledOrig = bEnabled;
        if (comp instanceof JTextComponent) {
            JTextComponent txt = (JTextComponent) comp;
            if (!bEnabled) {
                // need to see if it should call setEditable(b) instead
                if (getHub().getAO() != null) {
                    txt.setEditable(false);
                    bEnabled = true;
                }
            }
            else {
                if (!txt.isEditable()) txt.setEditable(true);
            }
        }
        if (comp.isEnabled() != bEnabled) {
            if (!(comp instanceof OALabel)) {
                comp.setEnabled(bEnabled);
            }
        }
        if (comp instanceof OAJfcComponent) {
            OAJfcController jc = ((OAJfcComponent) comp).getController();
            if (jc != null) {
                JLabel lbl = jc.getLabel();
                boolean b = bEnabledOrig;
                if (!b && !bLabelAlwaysMatchesComponentEnabled && ((hubChangeListenerType == null || hubChangeListenerType == HubChangeListener.Type.AoNotNull) || (hubChangeListenerType == HubChangeListener.Type.HubValid))) {
                    Hub h = getHub();
                    if (h != null && h.isValid() && h.getAO() != null) b = true; 
                }
                if (!b && hubForLabel != null) b = hubForLabel.isValid() && hubForLabel.getAO() != null;
                if (lbl != null && lbl.isEnabled() != b) {
                    lbl.setEnabled(b);
                }
            }
        }
        return bEnabledOrig;
    }
    // called by updateEnabled to allow it to be overwritten
    protected boolean isEnabled(boolean defaultValue) {
        return defaultValue;
    }

    private HubEvent lastUpdateVisibleHubEvent;
    private boolean bLastUpdateVisible;
    public boolean updateVisible() {
        final HubEvent he = OAThreadLocalDelegate.getCurrentHubEvent(); 
        if (he == null || he != lastUpdateVisibleHubEvent) {
            lastUpdateVisibleHubEvent = he;
            Object obj;
            if (bUseLinkHub && hubLink != null) obj = hubLink.getAO();
            else if (hub != null) obj = hub.getAO();
            else obj = null;
            bLastUpdateVisible = updateVisible(component, obj);
        }
        return bLastUpdateVisible;
    }
    public boolean updateVisible(final JComponent comp, final Object object) {
        if (comp == null) return false;
        boolean bVisible = getVisibleChangeListener().getValue();
        bVisible = isVisible(bVisible);
        
        if (comp.isVisible() != bVisible) {
            comp.setVisible(bVisible);
        }
        if (comp instanceof OAJfcComponent) {
            OAJfcController jc = ((OAJfcComponent) comp).getController();
            if (jc != null) {
                JLabel lbl = jc.getLabel();
                if (lbl != null && lbl.isVisible() != bVisible) {
                    lbl.setVisible(bVisible);
                }
            }
        }
        return bVisible;
    }

    // called by updateVisible to allow it to be overwritten
    protected boolean isVisible(boolean defaultValue) {
        return defaultValue;
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
            Hub h = getHub();
            if (h == null) return propertyInfoMaxColumns;
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(h.getObjectClass());
            OAPropertyInfo pi = oi.getPropertyInfo(endPropertyName);
            
            propertyInfoMaxColumns = (pi == null) ? -1 : pi.getMaxLength();
            propertyInfoDisplayColumns  = (pi == null) ? -1 : pi.getDisplayLength();

            if (endPropertyClass != null) {
                if (endPropertyClass.equals(String.class)) {
                    if (propertyInfoMaxColumns > 254) propertyInfoMaxColumns = 254;
                }
                else propertyInfoMaxColumns = -1;
            }
        }
        return propertyInfoMaxColumns;
    }

    public int getDataSourceMaxColumns() {
        if (dataSourceMaxColumns == -2) {
            
            // annotation OAColumn has ds max length
            int x = getPropertyInfoMaxColumns();
            if (x > 0) {
                dataSourceMaxColumns = x;
                return x;
            }
            
            Hub h = getHub();
            if (h == null) return dataSourceMaxColumns;
            OADataSource ds = OADataSource.getDataSource(h.getObjectClass());
            if (ds != null) {
                dataSourceMaxColumns = -1;
                dataSourceMaxColumns = ds.getMaxLength(h.getObjectClass(), endPropertyName);
                if (endPropertyClass != null) {
                    if (endPropertyClass.equals(String.class)) {
                        if (dataSourceMaxColumns > 254) dataSourceMaxColumns = -1;
                    }
                    else dataSourceMaxColumns = -1;
                }
            }
        }
        return dataSourceMaxColumns;
    }

    
    /**
     * Label that is used with component, so that enabled and visible will be applied.
     */
    public void setLabel(JLabel lbl) {
        setLabel(lbl, false, null);
    }
    public void setLabel(JLabel lbl, boolean bAlwaysMatchEnabled) {
        setLabel(lbl, bAlwaysMatchEnabled, null);
    }
    public void setLabel(JLabel lbl, Hub hubForLabel) {
        setLabel(lbl, false, hubForLabel);
    }
    public void setLabel(JLabel lbl, boolean bAlwaysMatchEnabled, Hub hubForLabel) {
        this.label = lbl;
        lbl.setLabelFor(component);
        this.hubForLabel = hubForLabel;
        this.bLabelAlwaysMatchesComponentEnabled = bAlwaysMatchEnabled;
        update();
    }
    public JLabel getLabel() {
        return this.label;
    }

    private Border borderFocus;
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!isSelected && !hasFocus) {
            label.setForeground( UIManager.getColor(table.getForeground()) );
            label.setBackground( UIManager.getColor(table.getBackground()) );
        }

        Object obj = value;

        // label.setHorizontalTextPosition(label.getHorizontalTextPosition());
        label.setIcon( getIcon(getHub().elementAt(row)) );

        Hub h = getHub();  // could be a link hub
        if (table instanceof OATable) {
            h = ((OATable) table).getHub();
        }
        
        obj = h.elementAt(row);
        update(label, obj, false);

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


    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        undoDescription = s;
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return undoDescription;
    }


    @Override
    public void afterAdd(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void afterRemove(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void afterRemoveAll(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void onNewList(HubEvent e) {
        update();
    }
    @Override
    public void afterInsert(HubEvent e) {
        if (bIsHubCalc) update();
    }
    @Override
    public void afterChangeActiveObject(HubEvent e) {
        afterChangeActiveObject();
    }
    @Override
    public void afterNewList(HubEvent e) {
        update();
    }
    @Override
    public void afterPropertyChange(HubEvent e) {
        Object ao = getHub().getAO();
        if (ao != null && e.getObject() == ao) {
            boolean b = false;
            String prop = e.getPropertyName();
            if (prop != null && prop.equalsIgnoreCase(OAJfcController.this.getHubListenerPropertyName())) b = true;
            else {
                final MyHubChangeListener[] mcls = new MyHubChangeListener[] {changeListener, changeListenerEnabled, changeListenerVisible};
                for (MyHubChangeListener mcl : mcls) {
                    if (mcl == null) continue;
                    if (mcl.isListeningTo(hub, prop)) {
                        b = true;
                        break;
                    }
                }
            }
            if (b) {
                OAJfcController.this.afterPropertyChange();
                update();
            }
        }
    }

    // called if the actual property is changed in the actualHub.activeObject
    protected void afterPropertyChange() {
    }
    protected void afterChangeActiveObject() {
        update();  
    }

    public void setDisplayTemplate(String s) {
        this.displayTemplate = s;
        templateDisplay = null;
    }
    public String getDisplayTemplate() {
        return displayTemplate;
    }
    public OATemplate getTemplateForDisplay() {
        if (OAString.isNotEmpty(getDisplayTemplate())) {
            if (templateDisplay == null) templateDisplay = new OATemplate<>(getDisplayTemplate());
        }
        return templateDisplay;
    }

    /**
     * Used to display values, uses display template if defined.
     */
    public String getDisplayText(Object obj, String defaultText) {
        obj = getRealObject(obj);
        if (!(obj instanceof OAObject)) return defaultText;
    
        String s = getDisplayTemplate();
        if (OAString.isEmpty(s)) return defaultText;
        
        defaultText = getTemplateForDisplay().process((OAObject) obj);
        if (defaultText != null && defaultText.indexOf('<') >=0 && defaultText.toLowerCase().indexOf("<html>") < 0) defaultText = "<html>" + defaultText;
    
        return defaultText;
    }


    public void setToolTipTextTemplate(String s) {
        this.toolTipTextTemplate = s;
        templateToolTipText = null;
    }
    public String getToolTipTextTemplate() {
        return toolTipTextTemplate;
    }
    public OATemplate getTemplateForToolTipText() {
        if (OAString.isNotEmpty(getToolTipTextTemplate())) {
            if (templateToolTipText == null) templateToolTipText = new OATemplate<>(getToolTipTextTemplate());
        }
        return templateToolTipText;
    }


    public String getToolTipText(Object obj, String ttDefault) {
        obj = getRealObject(obj);

        Component comp = getComponent();
        if (OAString.isEmpty(ttDefault) && comp instanceof JComponent) {
            String s = ((JComponent) comp).getToolTipText();
            if (OAString.isNotEmpty(s)) ttDefault = s;
        }
        
        if (obj instanceof OAObject) {
            if (OAString.isNotEmpty(toolTipTextPropertyPath)) {
                ttDefault = ((OAObject) obj).getPropertyAsString(toolTipTextPropertyPath);
            }
            
            String s = getToolTipTextTemplate();
            if (OAString.isNotEmpty(s)) ttDefault = s;
            
            Object objx = obj;
            String prop;
            if (oaPropertyPath != null && oaPropertyPath.hasLinks()) {
                objx = oaPropertyPath.getLastLinkValue(objx);
            }
            ttDefault = OAObjectEditQueryDelegate.getToolTip((OAObject) objx, endPropertyName, ttDefault);
        }
        else {
            if (OAString.isNotEmpty(toolTipTextPropertyPath) || OAString.isNotEmpty(getToolTipTextTemplate())) {
                ttDefault = null;
            }
        }

        if (ttDefault != null && ttDefault.indexOf("<%=") >= 0 && obj instanceof OAObject) {
            if (templateToolTipText == null || !ttDefault.equals(templateToolTipText.getTemplate())) {
                templateToolTipText = new OATemplate(ttDefault);
            }
            ttDefault = templateToolTipText.process((OAObject) obj);
        }
        
        if (ttDefault != null && ttDefault.indexOf('<') >=0 && ttDefault.toLowerCase().indexOf("<html>") < 0) ttDefault = "<html>" + ttDefault;

        return ttDefault;
    }


    // Shares hubListeners between hub and all 3 hubChangeListeners
    protected abstract class MyHubChangeListener extends HubChangeListener {
        @Override
        public void remove(Hub hub, String prop) {
            final MyHubChangeListener[] mcls = new MyHubChangeListener[] {changeListener, changeListenerEnabled, changeListenerVisible};

            HubProp hp = null;
            for (MyHubChangeListener mcl : mcls) {
                if (mcl == null) continue;

                for (HubProp hpx : mcl.hubProps) {
                    if (hpx.hub != hub) continue;
                    if (hpx.hubListener == null) continue;
                    if (!OAString.equals(prop, hpx.propertyPath)) continue;
                    hp = hpx;
                    break;
                }
            }
            if (hp == null) return;
            
            int cnt = 0;
            for (MyHubChangeListener mcl : mcls) {
                if (mcl == null) continue;
                for (HubProp hpx : mcl.hubProps) {
                    if (hpx.hubListener == hp.hubListener) cnt++;
                }
            }
            
            if (cnt == 1) hp.hub.removeHubListener(hp.hubListener);
            hp.hubListener = null;
        }

        @Override
        public void close() {
            final MyHubChangeListener[] mcls = new MyHubChangeListener[] {changeListener, changeListenerEnabled, changeListenerVisible};
            
            for (HubProp hp : hubProps) {
                if (hp.bIgnore) continue;
                if (hp.hubListener == null) continue;
                if (hp.hub == OAJfcController.this.hub) {
                    hp.hubListener = null;
                    continue;
                }
                
                for (MyHubChangeListener mcl : mcls) {
                    if (mcl == null) continue;
                    if (mcl == this) continue;
                    boolean b = false;
                    for (HubProp hpx : mcl.hubProps) {
                        if (hpx.hubListener == hp.hubListener) {
                            b = true;
                            break;
                        }
                    }
                    if (!b) hp.hub.removeHubListener(hp.hubListener);
                    for (HubProp hpx : hubProps) {
                        if (hpx.hubListener == hp.hubListener) hpx.hubListener = null;
                    }
                    hp.hubListener = null;
                }
            }
        }
        
        @Override
        protected void assignHubListener(HubProp newHubProp) {
            if (!_assignHubListener(newHubProp)) {
                super.assignHubListener(newHubProp);
            }
        }
        protected boolean _assignHubListener(HubProp newHubProp) {
            if (OAJfcController.this.hub == newHubProp.hub) {
                if (newHubProp.propertyPath == null) return true;
                if (newHubProp.propertyPath.indexOf('.') < 0) return true;
                if (newHubProp.propertyPath.equalsIgnoreCase(OAJfcController.this.propertyPath)) {
                    return true;
                }
            }

            Hub h = OAJfcController.this.hub;
            if (h != null) {
                h = h.getLinkHub();
                if (h != null && h == newHubProp.hub)  {
                    if (newHubProp.propertyPath == null) return true;
                }
            }
            
            final MyHubChangeListener[] mcls = new MyHubChangeListener[] {changeListener, changeListenerEnabled, changeListenerVisible};
            for (MyHubChangeListener mcl : mcls) {
                if (mcl == null) continue;
                for (HubProp hp : mcl.hubProps) {
                    if (hp.bIgnore) continue;
                    if (hp.hub != newHubProp.hub) continue;
                    if (hp.hubListener == null) continue;
                    if (newHubProp.propertyPath == null) {
                        newHubProp.hubListener = hp.hubListener;
                        return true;
                    }
                    if (newHubProp.propertyPath.indexOf('.') < 0) {
                        newHubProp.hubListener = hp.hubListener;
                        return true;
                    }
                    if (!newHubProp.propertyPath.equalsIgnoreCase(hp.propertyPath)) continue; 
                    newHubProp.hubListener = hp.hubListener;
                    return true;
                }
            }            
            return false;
        }
        public boolean isListeningTo(Hub hub, String prop) {
            if (hub == null) return false;
            for (HubProp hp : hubProps) {
                if (hp.bIgnore) continue;
                if (hp.hub != hub) continue;
                if (OAString.isEqual(hp.propertyPath, prop, true)) return true;
            }
            return false;
        }
    }
}

