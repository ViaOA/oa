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

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/**
 * Class used to <i>wrap</i> a Table column to work with an OATableComponent.
 */
public class OATableColumn {
    private OATableComponent oaComp;
    public String origPath;
    public String path;
    public String pathIntValue; // if using a LinkHub that is linked on hub position
                         // and need to get integer to use
    Method[] methods, methodsIntValue;
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

    public String getToolTipText(int row, int col, String defaultValue) {
        if (oaComp != null) {
            return oaComp.getToolTipText(row, col, defaultValue);
        }
        return defaultValue;
    }

    public OATableColumn(OATable table, String path, TableCellEditor comp, TableCellRenderer rend, OATableComponent oaComp, String fmt) {
        this.table = table;
        this.path = this.origPath = path;
        this.comp = comp;
        this.renderer = rend;
        this.oaComp = oaComp;
        this.fmt = fmt;

///qqqqq 20131109 testing, so that it will verify that methods can be found when column is created.
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

    private Hub hubMethodHub; // 2006/12/11

/***qqqqqqq OLD    
    // methods gets set to null whenever Hub or PropertyPath get changed
    public Method[] getMethods_OLD(Hub hub) {
        if (methods != null && hub == hubMethodHub) return methods;

        hubMethodHub = hub;
        pathIntValue = null;
        // changed so that it will only change the path when the component hub
        // is linked back to the table.hub
        if (oaComp != null && oaComp.getHub() != null && !bIsAlreadyExpanded) {
            path = origPath;
            String holdPath = path;
            // get path from any link Hub
            Hub h = oaComp.getHub();

            bLinkOnPos = HubLinkDelegate.getLinkedOnPos(h, true);
            // 20110116
            String fromProp = HubLinkDelegate.getLinkFromProperty(h, true);
            if (fromProp != null) {
                path = fromProp;
            }
            else {
                // see if this is linked to table hub, and expand the path 
                for (; h != hub;) {
                    Hub lh = HubLinkDelegate.getLinkToHub(h, true);
                    if (lh == null) break;
                    if (path == null) path = "";
                    if (bLinkOnPos) {
                        if (pathIntValue != null) pathIntValue = "." + pathIntValue;
                        else pathIntValue = "";
                        pathIntValue = HubLinkDelegate.getLinkHubPath(h, true) + pathIntValue;
                    }
                    else {
                        if (path.length() == 0) path = HubLinkDelegate.getLinkHubPath(h, true);
                        else path = HubLinkDelegate.getLinkHubPath(h, true) + "." + path;
                    }
                    h = lh;
                    if (h == hub) break; // 20131109
                    if (hub.getMasterHub() == null) { // 20131109 could be a hub copy
                        if (h.getObjectClass().equals(hub.getObjectClass())) break;
                    }
                }
                
                if (h != hub && !bLinkOnPos) {
                    if (HubShareDelegate.isUsingSameSharedAO(hub, h, true)) h = hub;
                }
                
                if (h != hub && !bLinkOnPos) {  // 20131109 
                    Hub mh = HubDetailDelegate.getMasterHub(h);
                    if (mh != null) {
                        if (HubShareDelegate.isUsingSameSharedAO(mh, hub, true)) mh = hub;
                    }
                    
                    if (mh == hub) {
                        path = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + path;
                    }
                    else if (mh != null && mh.getObjectClass().equals(hub.getObjectClass()) && hub.getMasterHub() == null) {
                        // 20131026 this is when a hubCopy is used
                        path = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + path;
                    }
                    else if (hub.getMasterHub() == null) {
                        // 20131109  check to see if it is from a HubCopy
                    }
                    else {
                        path = holdPath; 
                        bLinkOnPos = false;
                    }
                }
            }
        }

        // if path == null then getMethods() will use "toString"
        if (bLinkOnPos) {
            OAPropertyPath opp = new OAPropertyPath(pathIntValue);
            try { // 20120809
                opp.setup(hub.getObjectClass());
            }
            catch (Exception e) {
                throw new RuntimeException("could not parse propertyPath", e);
            }
            methodsIntValue = opp.getMethods();
            // was: methodsIntValue = OAReflect.getMethods(hub.getObjectClass(), pathIntValue);
            
            opp = new OAPropertyPath(path);
            try { // 20120809
                opp.setup(oaComp.getHub().getObjectClass());
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("could not parse propertyPath=%s, hub=%s",path,hub), e);
            }
            methods = opp.getMethods();
            // was: methods = OAReflect.getMethods(oaComp.getHub().getObjectClass(), path);
        }
        else {
            OAPropertyPath opp = new OAPropertyPath(path);
            try { // 20120809
                opp.setup(hub.getObjectClass());
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("could not parse propertyPath=%s, hub=%s",path,hub), e);
            }
            methods = OAReflect.getMethods(hub.getObjectClass(), path);
            //was:methods = OAReflect.getMethods(hub.getObjectClass(), path);
        }

        // this will setup a Hub listener to listen for changes to columns that use propertyPaths
        // ?? might want this to be a setting
        if (methods != null && methods.length > 1 && path != null && path.indexOf('.') >= 0 && path.indexOf('.') != path.length() - 1) {
            // 20101219 create a "dummy" prop, with path as a dependent propPath
            final String propx = "TableColumn_" + path.replace('.', '_');
            hubListener = new HubListenerAdapter() {
                public @Override
                void afterPropertyChange(HubEvent e) {
                    String s = e.getPropertyName();
                    if (s != null && s.equalsIgnoreCase(propx)) {
                        table.repaint();
                    }
                }
            };
            table.getHub().addHubListener(hubListener, propx, new String[] { path });
        }

        return methods;
    }
*/

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
        Method[] ms = getMethods(hub);

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

    
    
    // 20140404 moved this to OAObjectReflectDelegate
    /** 20140211
     * This is used to expand a propertyPath from the Table hub, to the OAComp hub 
     * for a column, so that the value of the rows can be found.
     *
    public String expandPropertyPath(Hub hubTable, Hub hubComp, String path) {
        if (hubTable == null) return path;
        if (hubComp == null) return path;

        if (HubLinkDelegate.getLinkedOnPos(hubComp, true)) {
            String s = HubLinkDelegate.getLinkToProperty(hubComp, true);
            return s;
        }
        
        // check if there is a link "from" property used
        String fromProp = HubLinkDelegate.getLinkFromProperty(hubComp, true);
        if (fromProp != null) {
            return fromProp;
        }

        // see if there is a link path
        String hold = path;
        Hub h = hubComp;
        
        for ( ;; ) {
            Hub hx = HubLinkDelegate.getLinkToHub(h, true);
            if (hx == null) {
                path = hold;
                break;
            }

            if (path.length() == 0) path = HubLinkDelegate.getLinkHubPath(h, true);
            else path = HubLinkDelegate.getLinkHubPath(h, true) + "." + path;
            
            // found the links back to table hub
            if (hx == hubTable) {
                return path;
            }
            if (HubShareDelegate.isUsingSameSharedAO(hubTable, hx, true)) {
                return path;
            }
            if (hubTable.getMasterHub() == null) { // 20131109 could be a hub copy
                if (hx.getObjectClass().equals(hubTable.getObjectClass())) {
                    return path;
                }
            }
            h = hx;
        }

        // see if if there is a detail path using masterHub
        h = oaComp.getHub();
        for ( ;; ) {
            Hub hx = h.getMasterHub();
            if (hx == null) {
                path = hold;
                return path;
            }
            if (path.length() == 0) path = HubDetailDelegate.getPropertyFromMasterToDetail(h);
            else path = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + path;
            if (hx == hubTable) {
                return path;
            }
            if (HubShareDelegate.isUsingSameSharedAO(hubTable, hx, true)) {
                return path;
            }
            if (hubTable.getMasterHub() == null) { // 20131109 could be a hub copy
                if (hx.getObjectClass().equals(hubTable.getObjectClass())) {
                    return path;
                }
            }
            h = hx;
        }
    }
    */
    
    // 20140211
    public Method[] getMethods(Hub hubTable) {
        try {
            return _getMethods(hubTable);
        }
        catch (Exception e) {
//qqqqqqqqqqqq testing, to catch exceptions     
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
                // 20150303
                //was: String s = OAObjectReflectDelegate.getPropertyPathFromMaster(hubTable, oaComp.getHub());
                String s = OAObjectReflectDelegate.getPropertyPathBetweenHubs(hubTable, oaComp.getHub());
                if (s != null) { 
                    path = s + "." + path;
                }
                if (bLinkOnPos) {
                    pathIntValue = HubLinkDelegate.getLinkToProperty(oaComp.getHub());;
                    path = origPath;
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
                throw new RuntimeException(String.format("could not parse propertyPath=%s, hub=%s",path,hubTable), e);
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
        return methods;
    }

}
