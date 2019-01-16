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
package com.viaoa.jfc.table;

import java.awt.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.control.OAJfcController;

/**
 * Class used to <i>wrap</i> a Table column to work with an OATableComponent.
 */
public class OATableColumn {
    private static Logger LOG = Logger.getLogger(OATableColumn.class.getName());
    private OATableComponent oaComp;
    public String origPath;
    public String path;
    public String pathIntValue; // if using a LinkHub that is linked on hub position
                         // and need to get integer to use
    Method[] methods, methodsIntValue;
    Method[] methodsAsString;
    private TableCellEditor comp;
    TableCellRenderer renderer;
    boolean bLinkOnPos;
    OATable table;
//    HubMerger hubMerger;
    Hub hubCombined;
    boolean useHubGetObjectForLink;
    String fmt;
    public int sortOrder; // 2006/10/12
    public boolean sortDesc; // 2006/10/12
    public TableColumn tc;
    public boolean bVisible = true; // 2006/12/28
    public boolean bDefault = true; // 2006/12/28
    public int defaultWidth; // 2006/12/28
    public int currentWidth; // 2006/12/28
    boolean allowSorting=true;
    protected OATableColumnCustomizer columnCustomizer;
    public OATemplate templateToolTip;
    public String format;
    
    public boolean getAllowSorting() {
        return allowSorting;
    }
    public void setAllowSorting(boolean b) {
        allowSorting = b;
    }

    public void setTable(OATable t) {
        this.table = t;
    }

    
    public OATableFilterComponent compFilter;
    public void setFilterComponent(OATableFilterComponent comp) {
        compFilter = comp;
    }
    public OATableFilterComponent getFilterComponent() {
        return compFilter;
    }
    
    // flag to know if the propertyPath needs to be expanded to include any
    //    additional path from the component's Hub to the Table's hub.
    public boolean bIsAlreadyExpanded; 
                                
    public HubListener hubListener; // 20101219 for columns that use a
                                    // propertyPath

    
    public void setFormat(String fmt) {
        if (oaComp instanceof OAJfcComponent) ((OAJfcComponent)oaComp).getController().setFormat(fmt);
        this.format = fmt;
    }
    public String getFormat() {
        return this.format;
    }
    
    
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        if (oaComp != null) {
            return oaComp.getTableToolTipText(table, row, col, defaultValue);
        }
        return defaultValue;
    }
    private String toolTipText;
    public void setToolTipText(String tt) {
        toolTipText = tt;
    }
    public String getToolTipText() {
        return toolTipText;
    }
    
    public boolean allowEdit() {
        if (oaComp == null) return true;
        return oaComp.allowEdit();
    }
    
    public OATableColumn(OATable table, String path, TableCellEditor comp, TableCellRenderer rend, OATableComponent oaComp, String fmt) {
        this.table = table;
        this.path = this.origPath = path;
        this.comp = comp;
        this.renderer = rend;
        this.oaComp = oaComp;
        this.fmt = fmt;

        // verify that methods can be found when column is created.
        Method[] ms = getMethods(table.getHub());
        int xx = 4;
        xx++;
    }

    public OATableComponent getOATableComponent() {
        return oaComp;
    }

    public TableCellEditor getTableCellEditor() {
        return comp;
    }


    // 2006/10/12
    public TableCellRenderer headerRenderer;

    public TableColumn getTableColumn() {
        return tc;
    }
    
    public void setupTableColumn() {
        if (tc != null && headerRenderer == null) {
            headerRenderer = tc.getHeaderRenderer();
            tc.setHeaderRenderer(new TableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component comp = headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    return comp;
                }
            });
        }
    }

    public OATableColumnCustomizer getCustomizer() {
        return columnCustomizer;
    }
    public void setCustomizer(OATableColumnCustomizer tcc) {
        this.columnCustomizer = tcc;
    }
    
    private Hub hubMethodHub; // 2006/12/11

    public void setMethods(Method[] m) {
        methods = m;
    }

    // 2006/02/09
    public Object getValue(Hub hub, Object obj) {
        if (obj == null) return null;
        /* 20111213 removed, since getMethods(..) handles this        
        if (oaComp != null && oaComp.getHub() != null && !bIgnoreLink) {
            // 20110116 if link has a linkFromProperty, then dont get
            // refProperty. ex: Breed.name linked to Pet.breed (String)
            String fromProp = HubLinkDelegate.getLinkFromProperty(oaComp.getHub());
            if (fromProp == null) {
                if (oaComp.getHub().getLinkHub() == hub) {
                    Object obj2 = HubLinkDelegate.getPropertyValueInLinkedToHub(oaComp.getHub(), obj);
                    if (obj2 != obj) {
                        obj = obj2;
                        hub = oaComp.getHub();
                    }
                }
            }
        }
        */        
        Method[] ms;
        if (methodsAsString != null) ms = methodsAsString;
        else ms = getMethods(hub);

        if (bLinkOnPos) {
            Method[] m2 = methodsIntValue;
            obj = OAReflect.getPropertyValue(obj, m2);
            if (obj instanceof Number) {
                obj = oaComp.getHub().elementAt(((Number) obj).intValue());
                obj = OAReflect.getPropertyValue(obj, ms);
            }
            else {
                // obj = "Invalid";
            }
        }
        else if ((path == null || path.length()==0) && table.getSelectHub() != null) {
            // see if it is in the select hub
            Hub h = table.getSelectHub();
            obj = h.contains(obj); 
        }
        else {
            obj = OAReflect.getPropertyValue(obj, ms);
        }
        return obj;
    }


    /** 20180620
     * get the last OAObject in the property path, for case where a column uses a property path instead of just a property.
     * 20181004
     * changed to get object from table.hub in the component.hub 
     */
    public Object getObjectForTableObject(Object obj) {
        if (obj == null) return null;
        
        if (methodsIntValue != null) {
            Object objx = OAReflect.getPropertyValue(obj, methodsIntValue);
            int x = OAConv.toInt(objx);
            objx = getOATableComponent().getHub().getAt(x);
            return objx;
        }
        
        Method[] ms = methods;
        
        if (ms == null || ms.length < 2) return obj;
        
        Class clazz = oaComp.getHub().getObjectClass();
        int x = ms.length;
        for (int i=0; i<(x-1); i++) {
            if (obj.getClass().equals(clazz)) break;
            obj = OAReflect.getPropertyValue(obj, ms[i]);
            if (obj == null) break;
        }
        return obj;
    }
    

    public String getPathFromTableHub(Hub hubTable) {
        getMethods(hubTable);
        return path;
    }
    
    // 20140211
    public Method[] getMethods(Hub hubTable) {
        try {
            return _getMethods(hubTable);
        }
        catch (Exception e) {
            //qqq testing, to catch exceptions     
            e.printStackTrace();
            System.out.println("error: "+e);
        }
        return _getMethods(hubTable);
    }    
    public Method[] _getMethods(Hub hubTable) {
        if (methods != null && hubTable == hubMethodHub) return methods;
        hubMethodHub = hubTable;
        pathIntValue = null;
        bLinkOnPos = false;

        // changed so that it will only change the path when the component hub
        //    is linked back to the table.hub
        if (oaComp != null && oaComp.getHub() != null && !bIsAlreadyExpanded) {
            bLinkOnPos = HubLinkDelegate.getLinkedOnPos(oaComp.getHub(), true);
            path = origPath;
            if (!bIsAlreadyExpanded) {
                String s = OAObjectReflectDelegate.getPropertyPathBetweenHubs(hubTable, oaComp.getHub());
                if (!bLinkOnPos) {
                    if (s != null) {
                        if (path == null) path = s;
                        else path = s + "." + path;
                    }
                }
                else {
                    if (s == null) s = "";
                    else s += ".";
                    pathIntValue = s + HubLinkDelegate.getLinkToProperty(oaComp.getHub());
                }
            }
        }

        // if path == null then getMethods() will use "toString"
        if (bLinkOnPos) {
            OAPropertyPath opp = new OAPropertyPath(pathIntValue);
            try {
                opp.setup(hubTable.getObjectClass());
            }
            catch (Exception e) {
                throw new RuntimeException("could not parse propertyPath", e);
            }
            methodsIntValue = opp.getMethods();
            
            opp = new OAPropertyPath(path);
            try { 
                opp.setup(oaComp.getHub().getObjectClass());
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("could not parse propertyPath=%s, hub=%s",path,hubTable), e);
            }
            methods = opp.getMethods();
        }
        else {
            OAPropertyPath opp = new OAPropertyPath(path);
            try { 
                opp.setup(hubTable.getObjectClass());
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, String.format("could not parse propertyPath=%s, hub=%s, will use prop=id",path,hubTable), e);
                path = "id";
            }
            methods = OAReflect.getMethods(hubTable.getObjectClass(), path);
        }

        
        // this will setup a Hub listener to listen for changes to columns that use propertyPaths
        if (methods != null && methods.length > 1 && path != null && path.indexOf('.') >= 0 && path.indexOf('.') != path.length() - 1) {
            // 20101219 create a "dummy" prop, with path as a dependent propPath
            final String propx = "TableColumn_" + path.replace('.', '_');
            hubListener = new HubListenerAdapter() {
                public @Override
                void afterPropertyChange(HubEvent e) {
                    String s = e.getPropertyName();
                    if (s == null || !s.equalsIgnoreCase(propx)) {
                        return;
                    }
                    table.repaint();
                    
                    // 20150315
                    Object objx = e.getObject();
                    if (!(objx instanceof OAObject)) return;
                    Object val = ((OAObject) objx).getProperty(path);
                    
                    int col = table.getColumnIndex(oaComp);
                    int row = e.getHub().getPos(objx);
                    if (row >= 0) table.setChanged(row, col, val);
                }
            };
            
            // 20160613 have it run in background
            // 20160722 only listen to viewed rows
            table.getViewableHub().addHubListener(hubListener, propx, new String[] { path }, false, true);
        }
        else if (methods != null && !OAString.isEmpty(path)) {
            // 20150315
            hubListener = new HubListenerAdapter() {
                public @Override
                void afterPropertyChange(HubEvent e) {
                    String s = e.getPropertyName();
                    if (s != null && s.equalsIgnoreCase(path)) {
                        int col = table.getColumnIndex(oaComp);
                        table.setChanged(e.getHub().getPos(e.getObject()), col);
                    }
                }
            };
            table.getHub().addHubListener(hubListener);
        }
        
        // 20180626 see if methodName"AsString" exists
        methodsAsString = null;
        if (methods != null && methods.length > 0) {
            Method m = methods[methods.length-1];
            
            Method mx = OAReflect.getMethod(m.getDeclaringClass(), m.getName()+"AsString", 0, null);
            if (mx != null) {
                methodsAsString = new Method[methods.length];
                System.arraycopy(methods, 0, methodsAsString, 0, methods.length-1);
                methodsAsString[methods.length-1] = mx;
            }
        }
        
        return methods;
    }

}
