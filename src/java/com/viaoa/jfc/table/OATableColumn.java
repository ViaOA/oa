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
    String pathIntValue; // if using a LinkHub that is linked on hub position
                         // and need to get integer to use
    Method[] methods, methodsIntValue;
    private TableCellEditor comp;
    TableCellRenderer renderer;
    boolean bLinkOnPos;
    OATable table;
    HubMerger hubMerger;
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
    public boolean bIgnoreLink; // use property path only, dont try to resolve
                                // the hub link
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
    }

    public OATableComponent getOATableComponent() {
        return oaComp;
    }

    public TableCellEditor getTableCellEditor() {
        return comp;
    }

    public void setTable(OATable t) {
        this.table = t;
    }

    // 2006/10/12
    TableCellRenderer headerRenderer;

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

    // methods gets set to null whenever Hub or PropertyPath get changed
    public Method[] getMethods(Hub hub) {
        if (methods != null && hub == hubMethodHub) return methods;

        hubMethodHub = hub;
        pathIntValue = null;
        // changed so that it will only change the path when the component hub
        // is linked back to the table.hub
        if (oaComp != null && oaComp.getHub() != null && !bIgnoreLink) {
            path = origPath;
            String holdPath = path;
            // get path from any link Hub
            Hub h = oaComp.getHub();
            bLinkOnPos = HubLinkDelegate.getLinkedOnPos(h);
            // 20110116
            String fromProp = HubLinkDelegate.getLinkFromProperty(h);
            if (fromProp != null) {
                path = HubLinkDelegate.getLinkToProperty(h);
            }
            else {
                for (; h != hub;) {
                    Hub lh = h.getLinkHub();
                    if (lh == null) break;
                    if (path == null) path = "";
                    if (bLinkOnPos) {
                        if (pathIntValue != null) pathIntValue = "." + pathIntValue;
                        else pathIntValue = "";
                        pathIntValue = HubLinkDelegate.getLinkHubPath(h) + pathIntValue;
                    }
                    else {
                        if (path.length() == 0) path = HubLinkDelegate.getLinkHubPath(h);
                        else path = HubLinkDelegate.getLinkHubPath(h) + "." + path;
                    }
                    h = lh;
                }
                if (h != hub) {
                    // 20110515 see if this is a master/detail relationship
                    if (h.getMasterHub() == hub) {
                        path = HubDetailDelegate.getPropertyFromMasterToDetail(h) + "." + path;
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
        else {
            obj = OAReflect.getPropertyValue(obj, ms);
        }
        return obj;
    }

}
