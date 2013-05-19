package com.viaoa.model;

import java.util.Vector;

import com.viaoa.hub.*;
import com.viaoa.util.OAString;

public class ChangeListener {
	
	private Model model;
    private boolean bIgnoreChange;
    private boolean bIgnorePropertyAdd;

    private transient HubListener hlObjectDef;
    private transient HubListener hlIndex;
    private transient HubListener hlIndexProperty;

    protected HubMerger hmProperty;  // used to combine all PropertyDefs
    protected Hub hubProperty;
    private transient HubListener hlProperty;

    protected HubMerger hmLinkProperty;  // used to combine all PropertyDefs
    protected Hub hubLinkProperty;
    private transient HubListener hlLinkProperty;
    
	public ChangeListener(Model model) {
		this.model = model;
    	
		setupObjectDefListener();
    	setupPropertyListener();
    	setupLinkPropertyListener();
    	setupLinkFKeyListener();
    	setupIndexListener();

    	setupLinkListener();
	}
    
    public void setIgnoreChange(boolean b) {
        bIgnoreChange = b;
    }

    private boolean bIgnoreModelChanges;
    public void addChange(Change mc) {
    	if (!bIgnoreModelChanges) model.getChanges().add(mc);
    }
    
    private void setupObjectDefListener() {
        hlObjectDef = (new HubListenerAdapter() {
            private Vector vecHoldPkeys; 
            public @Override void beforePropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
                if (s.equalsIgnoreCase(ObjectDef.PROPERTY_ParentObjectDef)) {
                    ObjectDef objectDef = (ObjectDef) e.getObject();
                    Property[] defs = ObjectDefDelegate.getAllProperties(objectDef);
                    vecHoldPkeys = new Vector(10,10);
                    for (int i=0; i<defs.length; i++) {
                        if (defs[i].getKey() && !defs[i].getTransient()) vecHoldPkeys.add(defs[i]);
                    }
                }
            }

            public @Override void afterPropertyChange(HubEvent e) {
                if (!(e.getObject() instanceof ObjectDef)) return;
                String s = e.getPropertyName();
                if (s == null) return;
                if (!s.equalsIgnoreCase(ObjectDef.PROPERTY_Name)) {
	                if (!s.equalsIgnoreCase(ObjectDef.PROPERTY_DataSourceName)) {
	                    if (!s.equalsIgnoreCase(ObjectDef.PROPERTY_Transient)) {
	                        if (!s.equalsIgnoreCase(ObjectDef.PROPERTY_ParentObjectDef)) {
	                            return;
	                        }
                        }
                    }
                }
                
                if (s.equalsIgnoreCase(ObjectDef.PROPERTY_ParentObjectDef)) {
                    ObjectDef objectDef = (ObjectDef) e.getObject();
                    if (e.getNewValue() == null) {     
                        if (!(e.getOldValue() instanceof ObjectDef)) return;
                        ObjectDef od = (ObjectDef) e.getOldValue();
                        if (od == null) return;
                        
                        // give SubClass OD its own unique properties that match the Parents
                        //   this way, the Links for the subClass will not have to be changed, they 
                        //   will still have same fkey names.  
                        bIgnorePropertyAdd = true; // The subClass table already has the correct columns
                        Property[] defs = ObjectDefDelegate.getAllProperties(od);
                        int pos = 0;
                        for (int ii=0; ii < defs.length; ii++) {
                            Property property = defs[ii];
                            if (!property.getKey()) continue;
                            if (property.getTransient()) continue;
                            Property pd = new Property();
                            property.copyInto(pd, new String[] { Property.PROPERTY_ObjectDef} );  // using exclude option
                            objectDef.getProperties().insert(pd, pos++);
                        }
                        bIgnorePropertyAdd = false;
                    }
                }
                
                Change mc = getAdd((ObjectDef) e.getObject());
                if (mc != null) {
                    generateCode(mc); // regenerate
                }
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_OBJECTCHANGE;
                    mc.property = e.getPropertyName();
                    mc.object = e.getObject();
                    mc.oldValue = e.getOldValue();
                    mc.newValue = e.getNewValue();
                    mc.temp = vecHoldPkeys;
                    vecHoldPkeys = null;
                    addChange(mc);
                }

                // this must be done after MC is sent
                if (s.equalsIgnoreCase(ObjectDef.PROPERTY_ParentObjectDef)) {
                    ObjectDef objectDef = (ObjectDef) e.getObject();
                    if (e.getNewValue() != null) {
                        // remove ObjectId properties
                        Hub hub = objectDef.getProperties();
                        for (int i=0; ; i++) {
                            Property property = (Property) hub.elementAt(i);
                            if (property == null) break;
                            if (property.getKey()) {
                                hub.remove(property);
                                i--;
                            }
                        }
                    }
                }

                ObjectDef od = (ObjectDef) e.getObject();
                Model md = null;
                if (od != null) md = od.getModel();

                if (s.equalsIgnoreCase(ObjectDef.PROPERTY_Name)) {
	                Hub h = od.getLinkProperties();
	                for (int i=0; ;i++) {
	                    LinkProperty lp = (LinkProperty) h.elementAt(i);
	                    if (lp == null) break;
	                    s = (String) e.getOldValue();
	                    lp.createDefaultName(s);
	                }
	                if (od != null && md != null) {
	                    h = od.getIndexes();
	                    Index id = null;
	                    for (int i=0; ;i++) {
	                    	id = (Index) h.elementAt(i);
	                    	if (id == null) break;
	                    	Hub h2 = id.getIndexProperties();
	                    	if (h2.getSize() != 1) continue;
	                    	IndexProperty ipd = (IndexProperty) h2.elementAt(0);
	                    	Property pd = ipd.getProperty();
							s = md.getDefaultIndexName((String) e.getOldValue(), pd.getName());
	                    	if (s.equalsIgnoreCase(id.getDataSourceName())) id.setDataSourceName(md.getDefaultIndexName(od.getName(), pd.getName()));
	                    }
	                }
                }
            }

            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            // 2004/08/23 see if a OBJECTADD can be found that has not had any changes affect it
            Change getAdd(ObjectDef object) {
                int x = model.getChanges().getSize();
                for (int i=x-1; i>=0; i--) {
                    Change mc = (Change) model.getChanges().elementAt(i);
                    Object obj = mc.getObject();
                    
                    if (obj instanceof ObjectDef) {
                        if (obj == object) {
                            if (mc.getType() == mc.TYPE_OBJECTADD) return mc;
                            break;
                        }
                        
                        ObjectDef od = (ObjectDef) obj;
                        
                        if (od.getParentObjectDef() == object) return null;
                        
                        // see if this od has same link as object
                        Hub hub = od.getLinkProperties();
                        for (int j=0; ;j++) {
                            LinkProperty lpd = (LinkProperty) hub.elementAt(j);
                            if (lpd == null) break;
                            if (lpd.getObjectDef() == object) return null;
                            // cant be reference to any of this objects subclasses either
                            Hub h = object.getChildrenObjectDefs();
                            for (int k=0; ;k++) {
                                ObjectDef sod = (ObjectDef) h.elementAt(k);
                                if (sod == null) break;
                                if (lpd.getObjectDef() == sod) return null;
                            }
                        }
                    }
                    else if (obj instanceof Property) {
                        Property pd = (Property) obj;
                        ObjectDef od = pd.getObjectDef();
                        if (od == null) continue;
                        if (od == object) break;
                        
                        if (!pd.getKey()) {
                            // see if this could be an objectId prop for a linkTo object
                            if (mc.getType() != mc.TYPE_PROPERTYCHANGE) continue;
                            if (!mc.getProperty().equalsIgnoreCase(Property.PROPERTY_Key)) continue;
                        }
                        Hub hub = od.getLinkProperties();
                        for (int j=0; ;j++) {
                            LinkProperty lpd = (LinkProperty) hub.elementAt(j);
                            if (lpd == null) break;
                            if (lpd.getObjectDef() == object) return null;
                            // cant be reference to any of this objects subclasses either
                            Hub h = object.getChildrenObjectDefs();
                            for (int k=0; ;k++) {
                                ObjectDef sod = (ObjectDef) h.elementAt(k);
                                if (sod == null) break;
                                if (lpd.getObjectDef() == sod) return null;
                            }
                        }
                    }
                    else if (obj instanceof Link) {
                        Link link = (Link) obj;
                        if (link.getFromLinkProperty().getObjectDef() == object) break;
                        if (link.getToLinkProperty().getObjectDef() == object) break;

                        // cant be reference to any of this objects subclasses either
                        Hub h = object.getChildrenObjectDefs();
                        for (int k=0; ;k++) {
                            ObjectDef sod = (ObjectDef) h.elementAt(k);
                            if (sod == null) break;
                            if (link.getFromLinkProperty().getObjectDef() == sod) break;
                            if (link.getToLinkProperty().getObjectDef() == sod) break;
                        }
                    }
                }
                return null;
            }
            
            public void hubAdd(HubEvent e) {
                if (model.isLoading()) return;
                Object object = e.getObject();
                Change mc = new Change();
                mc.type = mc.TYPE_OBJECTADD;
                mc.object = object;
                addChange(mc);
            }

            public void hubRemove(HubEvent e) {
                ObjectDef od = (ObjectDef) e.getObject();
                Change mc = getAdd(od);
                if (mc != null) {
                    model.getChanges().remove(mc);
                }

                // remove Link that this ObjectDef uses
                Hub h = od.getLinkProperties();
                Vector vec = new Vector(5,5);
                for (int i=0; ;i++) {
                    LinkProperty old = (LinkProperty) h.elementAt(i);
                    if (old == null) break;
                    Link ld = old.getLink();
                    vec.add(ld);
                }
                int x = vec.size();
                for (int i=0; i<x; i++) {
                    Link ld = (Link) vec.elementAt(i);
                    model.getLinks().remove(ld);
                }

                if (mc == null) {
                    mc = new Change();
                    mc.type = mc.TYPE_OBJECTREMOVE;
                    mc.object = od;
                    addChange(mc);
                }
            

                // need to detach subClasses
                h = od.getChildrenObjectDefs();
                for ( ; ;) {
                    ObjectDef sod = (ObjectDef) h.elementAt(0);
                    if (sod == null) break;
                    sod.setParentObjectDef(od.getParentObjectDef());
                    if (sod.getParentObjectDef() != null) continue;
                }
            
            }
        });
        Hub hub = model.getObjectDefs();
        hub.addHubListener(hlObjectDef);
    }


    private void setupPropertyListener() {
        hlProperty = new HubListenerAdapter() {
            Change mcRemove;
            Change mcChange;
            
            boolean isValid(HubEvent e) {
                // make sure that this propertyDef is from this modelDef
                if (!(e.getObject() instanceof Property)) return false;
                Property pd = (Property) e.getObject();
                ObjectDef od = pd.getObjectDef();
                if (od == null || model.getObjectDefs().get(od) == null) return false;
                return true;
            }
            public void hubBeforeRemove(HubEvent e) {
                Property pd = (Property) e.getObject();
                ObjectDef od = pd.getObjectDef();

                Change mc = new Change();
                mc.type = mc.TYPE_PROPERTYREMOVE;
                mc.object = pd;
                mc.oldValue = od;  // ObjectDef to remove Property from
                model.getChanges().add(mc);
			}            
            public void hubPropertyChange(HubEvent e) {
                if (bIgnoreChange) return;
                if (!isValid(e)) return;
                String s = e.getPropertyName();
                if (s == null) return;
                
                Property pd = (Property) e.getObject();
                
                if (s.equalsIgnoreCase(Property.PROPERTY_ObjectDef)) {
                    ObjectDef od = pd.getObjectDef();
                    if (od == null) return;

                    ObjectDef odOld = (ObjectDef) e.getOldValue();
                    if (odOld == null) return;

                    // 1: see if data pre-exists
                    Change mc = getAdd(pd, odOld);
                    if (mc != null) return;  // dont need to transfer data

                    /*
                        OAObject.firePropertyChange to reference properties that then call updateLinks()
                        1: property change event
                        2: remove event
                        3: add event
                        
                        For modelChanges, we need to move existing data from old table to new table.
                        For this, the column (from add event) needs to be done first.
                    */
                    
                    // PropertyChange and Remove need to be added after ADD is done
                    // 2: send propChange so that data is copied to new Table column
                    mc = new Change();
                    mc.type = mc.TYPE_PROPERTYCHANGE;
                    mc.property = e.getPropertyName();
                    mc.object = pd;
                    mc.oldValue = odOld;
                    mc.newValue = od;
                    mcChange = mc;  // added after ADD event

                    // 3: remove
                    mc = new Change();
                    mc.type = mc.TYPE_PROPERTYREMOVE;
                    mc.object = pd;
                    mc.oldValue = odOld;  // ObjectDef to remove Property from
                    mcRemove = mc; // added after ADD event
                
                    return;
                }
                else {
                    // if (!s.equalsIgnoreCase(Property.PROPERTY_Name)) {
                    if (!s.equalsIgnoreCase(Property.PROPERTY_Key)) {
                        if (!s.equalsIgnoreCase(Property.PROPERTY_Index)) {
                            if (!s.equalsIgnoreCase(Property.PROPERTY_Name)) {
	                            if (!s.equalsIgnoreCase(Property.PROPERTY_DataSourceName)) {
	                                if (!s.equalsIgnoreCase(Property.PROPERTY_Type)) {
	                                    if (!s.equalsIgnoreCase(Property.PROPERTY_MaxLength)) {
	                                        if (!s.equalsIgnoreCase(Property.PROPERTY_MinLength)) {
	                                            return;
	                                        }
	                                    }
	                                }
	                            }
                            }                    
                        }
                    }
                    // }
                }
                Change mc;
                mc = getAdd(pd, e);
                if (mc != null) {
                    generateCode(mc);
                }
                else {
	                mc = new Change();
	                mc.type = mc.TYPE_PROPERTYCHANGE;
	                mc.property = e.getPropertyName();
	                mc.object = pd;
	                mc.oldValue = e.getOldValue();
	                mc.newValue = e.getNewValue();
	                model.getChanges().add(mc);
                }
                
                s = e.getPropertyName();
                if (s.equalsIgnoreCase(Property.PROPERTY_Key)) {
                	updateFKeys(pd);
                }
                else if (s.equalsIgnoreCase(Property.PROPERTY_DataSourceName)) {
                    ObjectDef od = pd.getObjectDef();
                	Model md = null;
                	if (od != null) md = od.getModel();
                	if (md == null || (!md.getLockDataSourceAutoUpdate() && !md.getDataSourceExpandPkeyName())) {
                		updateFKeyNames(pd, (String) e.getOldValue());
                	}
                	updateIndexes(e,pd);
                }
                else if (s.equalsIgnoreCase(Property.PROPERTY_Index)) {
                	updateIndexes(e,pd);
                }
            }
            
            protected void updateIndexes(HubEvent e, Property pd) {
				boolean bNameChange = e.getPropertyName().equalsIgnoreCase(Property.PROPERTY_DataSourceName);
            	
            	ObjectDef od = pd.getObjectDef();
                if (od == null) return;
                Hub h = od.getIndexes();
                Index id = null;
                boolean bFound = false;
                for (int i=0; ;i++) {
                	id = (Index) h.elementAt(i);
                	if (id == null) break;
                	Hub h2 = id.getIndexProperties();
                	for (int j=0; ;j++) {
                		IndexProperty ipd = (IndexProperty) h2.elementAt(j);
                		if (ipd == null) break;
                		if (ipd.getProperty() != pd) continue;
            			if (bNameChange) {
            				// try to find existing MC
            				Change mc = ChangeListener.this.getAdd(ipd);
            				if (mc != null) {
            					generateCode(mc);
            				}
            				else {
                				mc = new Change();
        	                    mc.type = mc.TYPE_INDEXPROPERTYCHANGE;
        	                    mc.property = Property.PROPERTY_DataSourceName;
        	                    mc.object = ipd;
        	                    mc.oldValue = e.getOldValue();
        	                    mc.newValue = e.getNewValue();
        	                    addChange(mc);
            				}
            			}
            			else {
                			if (!pd.getIndex()) ipd.delete();
                			else {
                    			if (h2.getSize() == 1) bFound = true; // index for this property already exists
                			}
            			}
            			break;
            		}
                	if (h2.getSize() == 0) {
                		id.delete();
                		i--;
                	}
                }
                if (pd.getIndex() && !bFound && !bNameChange) {
                	id = new Index();
                	IndexProperty ipd = new IndexProperty();
                	ipd.setProperty(pd);
                	id.getIndexProperties().add(ipd);
                	Model md = od.getModel();
                	if (md != null) {
                		id.setDataSourceName(md.getDefaultIndexName(od.getName(), pd.getName()));
                	}
                	else id.setDataSourceName("Index"+od.getName()+OAString.getTitle(pd.getName()));
                	h.add(id);
                	h.setActiveObject(id);
                }
            }
            
            
            protected void updateFKeyNames(Property pd, String oldPropertyName) {
            	ObjectDef od = pd.getObjectDef();
        		if (od == null) return;
        		Hub h = od.getLinkProperties();
        		for (int i=0; ;i++) {
        			LinkProperty lpd = (LinkProperty) h.elementAt(i);
        			if (lpd == null) break;
        			Hub h2 = lpd.getLinkFKeys();
        			for (int j=0; ;j++) {
        				LinkFKey fk = (LinkFKey) h2.elementAt(j);
        				if (fk == null) break;
        				if (fk.getProperty() == pd) {
        					String s = fk.getDefaultDataSourceName(lpd.getName(), oldPropertyName);
        					if (s == null || s.equalsIgnoreCase(fk.getDataSourceName())) fk.updateDefaultName();
        				}
        			}
        			h2 = lpd.getLinkFKeys();
        			for (int j=0; ;j++) {
        				LinkFKey fk = (LinkFKey) h2.elementAt(j);
        				if (fk == null) break;
        				if (fk.getProperty() == pd) {
        					String s = fk.getDefaultDataSourceName(lpd.getName(), oldPropertyName);
        					if (s == null || s.equalsIgnoreCase(fk.getDataSourceName())) fk.updateDefaultName();
        				}
        			}
        		}
            }    

            protected void updateFKeys(Property pd) {
        	    ObjectDef od = pd.getObjectDef();
        		if (od == null) return;
        		Hub h = od.getLinkProperties();
        		for (int i=0; ;i++) {
        			LinkProperty lpd = (LinkProperty) h.elementAt(i);
        			if (lpd == null) break;
        			lpd.getLink().updateFkeys();
        		}
            }    
            
            // 2004/08/23 see if a OBJECTADD can be found that has not had any changes affect it
            Change getAdd(Property pd) {
                if (bIgnoreChange) return null;
                if (pd == null) return null;
                ObjectDef od = pd.getObjectDef();
                return getAdd(pd, od, null);
            }
            Change getAdd(Property pd, HubEvent e) {
                if (bIgnoreChange) return null;
                if (pd == null) return null;
                ObjectDef od = pd.getObjectDef();
                return getAdd(pd, od, e);
            }
            Change getAdd(Property pd, ObjectDef od) {
                if (bIgnoreChange) return null;
                if (pd == null) return null;
                return getAdd(pd, od, null);
            }
            Change getAdd(Property pd, ObjectDef od, HubEvent e) {
                if (bIgnoreChange) return null;
                boolean bWasId = false;
                if (e != null && e.getPropertyName() != null) {
                    if (e.getPropertyName().equalsIgnoreCase(Property.PROPERTY_Key)) {
                        Object obj = e.getOldValue();
                        if (obj instanceof Boolean) bWasId = ((Boolean) obj).booleanValue();
                    }
                }
                int x = model.getChanges().getSize();
                for (int i=x-1; i>=0; i--) {
                    Change mc = (Change) model.getChanges().elementAt(i);
                    Object obj = mc.getObject();
                    
                    if (obj instanceof ObjectDef) {
                        if (obj == od) {
                            if (mc.getType() == mc.TYPE_OBJECTADD) return mc;
                            return null;
                        }
                        if (!pd.getKey() && !bWasId) continue;
                        if (mc.getType() == mc.TYPE_OBJECTADD) {
                            if (obj instanceof ObjectDef) {
                                if ( ((ObjectDef) obj).getParentObjectDef() == od ) return null;
                            }
                        }
                        ObjectDef odx = (ObjectDef) obj;
                        // see if this odx has same link as object
                        Hub hub = odx.getLinkProperties();
                        boolean b = false;
                        for (int j=0; !b; j++) {
                            LinkProperty lpd = (LinkProperty) hub.elementAt(j);
                            if (lpd == null) break;
                            if (lpd.getObjectDef() == od) {
                                b = true;
                                break;
                            }
                            // cant be reference to any of this objects subclasses either
                            Hub h = od.getChildrenObjectDefs();
                            for (int k=0; ;k++) {
                                ObjectDef sod = (ObjectDef) h.elementAt(k);
                                if (sod == null) break;
                                if (lpd.getObjectDef() == sod) {
                                    b = true;
                                    break;
                                }
                            }
                        }
                        if (b) break;
                    }
                    else if (obj instanceof Property) {
                        if (obj == pd) {
                            if (mc.getType() == mc.TYPE_PROPERTYADD) return mc; // found
                            return null;  // another type of change
                        }
                        if (!pd.getKey() && !bWasId) continue;
                        
                        Property pdx = (Property) obj;
                        ObjectDef odx = pdx.getObjectDef();
                        if (odx == null) continue;
                        if (!pdx.getKey()) {
                            // see if this could be an objectId prop for a linkTo object
                            if (mc.getType() != mc.TYPE_PROPERTYCHANGE) continue;
                            if (!mc.getProperty().equalsIgnoreCase(Property.PROPERTY_Key)) continue;
                        }
                        if (odx == od) break; 
                        Hub hub = odx.getLinkProperties();
                        for (int j=0; ;j++) {
                            LinkProperty lpd = (LinkProperty) hub.elementAt(j);
                            if (lpd == null) break;
                            if (lpd.getObjectDef() == od) return null;
                            // cant be reference to any of this objects subclasses either
                            Hub h = od.getChildrenObjectDefs();
                            for (int k=0; ;k++) {
                                ObjectDef sod = (ObjectDef) h.elementAt(k);
                                if (sod == null) break;
                                if (lpd.getObjectDef() == sod) return null;
                            }
                        }
                    }
                    else if (obj instanceof Link && mc.type == mc.TYPE_LINKADD) {
                        if (pd.getKey()) {
                            Link ld = (Link) obj;
                            if (ld.getToLinkProperty().getObjectDef() == od) break;
                            if (ld.getFromLinkProperty().getObjectDef() == od) break;

                            // cant be reference to any of this objects subclasses either
                            Hub h = od.getChildrenObjectDefs();
                            for (int k=0; ;k++) {
                                ObjectDef sod = (ObjectDef) h.elementAt(k);
                                if (sod == null) break;
                                if (ld.getToLinkProperty().getObjectDef() == sod) break;
                                if (ld.getFromLinkProperty().getObjectDef() == sod) break;
                            }
                        }
                    }
                }
                return null;
            }

            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            public void hubAdd(HubEvent e) {
                if (!isValid(e)) return;
                if (bIgnoreChange) return;
                if (bIgnorePropertyAdd) return; // set when ParentObjectDef is changed
                if (model.isLoading()) return;
                
                Property pd = (Property) e.getObject();
                if (pd == null) return;
                ObjectDef od = pd.getObjectDef();
                if (od == null) return; // must be new

                Change mc = getAdd(pd);
                if (mc != null) generateCode(mc);
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_PROPERTYADD;
                    mc.object = e.getObject();
                    addChange(mc);
                }                
                if (mcChange != null) addChange(mcChange);
                if (mcRemove != null)  addChange(mcRemove);
                mcChange = null;
                mcRemove = null;
            	if (pd.getKey()) updateFKeys(pd);
            }

            private ObjectDef holdObjectDef;
            public void hubBeforeDelete(HubEvent e) {
                Property pd = (Property) e.getObject();
                holdObjectDef = pd.getObjectDef();
            }
            public void hubAfterDelete(HubEvent e) {
                if (!isValid(e)) return;
                if (bIgnoreChange) return;
                Property pd = (Property) e.getObject();
                if (mcRemove != null && mcRemove.getObject() == pd) return;
                ObjectDef od = holdObjectDef;

                Change mc = getAdd(pd);
                if (mc != null) {
                    if (mc.type == mc.TYPE_PROPERTYADD) model.getChanges().remove(mc);
                    else generateCode(mc);
                }
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_PROPERTYREMOVE;
                    mc.object = pd;
                    mc.temp = od;
                    addChange(mc);
                }
            	if (pd.getKey()) updateFKeys(pd);
            	// update indexes
            	Hub h = od.getIndexes();
            	for (int i=0; ;i++) {
            		Index id = (Index) h.elementAt(i);
            		if (id == null) break;
            		Hub h2 = id.getIndexProperties();
            		for (int j=0; ;j++) {
            			IndexProperty ipd = (IndexProperty) h2.elementAt(j);
            			if (ipd == null) {
            				if (j == 0) {
            					id.delete();
            					i--;
            				}
            				break;
            			}
            			if (ipd.getProperty() == pd) {
            				ipd.delete();
            				j--;
            			}
            		}
            	}
            }
        };
        hubProperty = new Hub(Property.class);
        hmProperty = new HubMerger(model.getObjectDefs(), hubProperty, ObjectDef.PROPERTY_Properties, false, null, true);
        hubProperty.addHubListener(hlProperty);
    }


    private void setupLinkListener() {
        Hub hub = model.getLinks();
        hub.addHubListener(new HubListenerAdapter() {
            public void hubPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
    			if (!s.equalsIgnoreCase(Link.PROPERTY_LinkDataSourceName)) {
    				return;
    			}
                Link link = (Link) e.getObject();
    			//qqqqqqqqqqqqqqq TODo: .........
            }
        });
    }    
    
    
    private void setupLinkPropertyListener() {
        hlLinkProperty = new HubListenerAdapter() {
            public void hubPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
                if (!s.equalsIgnoreCase(LinkProperty.PROPERTY_Type)) {
                	if (!s.equalsIgnoreCase(LinkProperty.PROPERTY_Name)) {
                		if (!s.equalsIgnoreCase(LinkProperty.PROPERTY_CreateMethod)) {
            				if (!s.equalsIgnoreCase(LinkProperty.PROPERTY_ObjectDef)) {
            					return;
                            }
                        }
                    }
                }
                LinkProperty lp = (LinkProperty) e.getObject();

                // if From/ToObjectDef is changed, then need to update ObjectDefs.linkProperties
                if (s.equalsIgnoreCase(LinkProperty.PROPERTY_ObjectDef) && (e.getOldValue() instanceof ObjectDef)) {
                    ObjectDef od = (ObjectDef) e.getOldValue();
                    Hub h = od.getLinkProperties();

                    od = link.getFromObjectDef();
                    if (od.find("linkProperties.linkDef", link) == null) {
                        od.getLinkProperties().add(new LinkProperty(link, false));
                    }
                }
                if (s.equalsIgnoreCase(Link.PROPERTY_ToObjectDef) && (e.getOldValue() instanceof ObjectDef)) {
                    ObjectDef od = (ObjectDef) e.getOldValue();
                    Hub h = od.getLinkProperties();
                    LinkProperty lpd = (LinkProperty) h.find("Link", link);
                    if (lpd != null) h.remove(lpd);

                    od = link.getToObjectDef();
                    if (od.find("linkProperties.linkDef", link) == null) {
                        od.getLinkProperties().add(new LinkProperty(link, true));
                    }
                }

                // see if it can go back to LINKADD
                Model mc = getAdd(link);
                if (mc != null) {
                    generateCode(mc);
                    return;
                }

                if (s.equalsIgnoreCase(Link.PROPERTY_FromType) || s.equalsIgnoreCase(Link.PROPERTY_ToType)) {
                	if (link.getFromType() == link.MANY && link.getToType() == link.MANY) {
                		// need to have LinkDataSourceName and FKeys all setup first
	                	bIgnoreModelChanges = true;
	                	link.setDefaults();
	                	bIgnoreModelChanges = false;
                	}
                }
                
                mc = new Change();
                mc.type = mc.TYPE_LINKCHANGE;
                mc.property = e.getPropertyName();
                mc.object = link;
                mc.oldValue = e.getOldValue();
                mc.newValue = e.getNewValue();
                addModelChange(mc);

                s = e.getPropertyName();
                if (s.equalsIgnoreCase(Link.PROPERTY_FromType) || s.equalsIgnoreCase(Link.PROPERTY_ToType)) {
                	// link.updateFkeys(); // this is done in Link.java
                }
            }    

            Model getM2MTypeChange(Link link) {
                int x = hubModelChange.size();
                for (int i=x-1; i>=0; i--) {
                    Change mc = (Change) model.getChanges().elementAt(i);
                    if (mc == null) break;
                    Object obj = mc.getObject();
                    if (obj == link) {
                    	if (mc.getType() == mc.TYPE_LINKCHANGE) {
                    		if (mc.getProperty().equals(Link.PROPERTY_FromType) || mc.getProperty().equals(Link.PROPERTY_ToType)) {
                    			return mc;
                    		}
                    	}
                    }
                }
                return null;
            }
            
            // 2004/08/23 see if a OBJECTADD can be found that has not had any changes affect it
            Model getAdd(Link link) {
                int x = hubModelChange.size();
                for (int i=x-1; i>=0; i--) {
                    Change mc = (Change) model.getChanges().elementAt(i);
                    Object obj = mc.getObject();
                    
                    if (obj instanceof ObjectDef) {
                        ObjectDef od = (ObjectDef) obj;
                        if (link.getFromObjectDef() == od || link.getToObjectDef() == od) return null;
                    }
                    else if (obj instanceof Property) {
                        Property pd = (Property) obj;
                        ObjectDef od = pd.getObjectDef();
                        if (link.getFromObjectDef() != od && link.getToObjectDef() != od) continue;
                        
                        if (pd.getKey()) return null;
                        if (mc.getType() == mc.TYPE_PROPERTYCHANGE) {
                            if (mc.getProperty().equalsIgnoreCase(Property.PROPERTY_Key)) return null;
                        }
                    }
                    else if (obj instanceof Link) {
                        Link ld = (Link) obj;
                        if (ld == link && mc.getType() == mc.TYPE_LINKADD) return mc;
                    }
                }
                return null;
            }



            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            public void hubAdd(HubEvent e) {
                if (isLoading()) return;
                Object object = e.getObject();

                // create/add LinkProperty for ObjectDef.from/to
                Link ld = (Link) object;
                // make sure that it does not already exist

                ObjectDef od = ld.getFromObjectDef();
                boolean b = false;
                Hub hub = od.getLinkProperties();
                for (int i=0; ;i++) {
                    LinkProperty lpd = (LinkProperty) hub.elementAt(i);
                    if (lpd == null) break;
                    if (lpd.getLink() == ld) {
                        if (!lpd.getUseTo()) {
                            b = true;
                            break;
                        }
                    }
                }
                if (!b) od.getLinkProperties().add(new LinkProperty(ld, false));

                od = ld.getToObjectDef();
                b = false;
                hub = od.getLinkProperties();
                for (int i=0; ;i++) {
                    LinkProperty lpd = (LinkProperty) hub.elementAt(i);
                    if (lpd == null) break;
                    if (lpd.getLink() == ld) {
                        if (lpd.getUseTo()) {
                            b = true;
                            break;
                        }
                    }
                }
                if (!b) od.getLinkProperties().add(new LinkProperty(ld, true));
               
                Change mc = new Change();
                mc.type = mc.TYPE_LINKADD;
                mc.object = object;
                addModelChange(mc);
            }

            public void hubRemove(HubEvent e) {
                Link ld = (Link) e.getObject();
            
                Change mc = getAdd(ld);
                if (mc != null) {
                    model.getChanges().remove(mc);
                }
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_LINKREMOVE;
                    mc.object = ld;
                    addChange(mc);
                }
                
                // remove LinkProperty from ObjectDef.from/to, do after ModelChange

                Hub h = ld.getFromObjectDef().getLinkProperties();
                LinkProperty lpd = (LinkProperty) h.find("Link", ld);
                if (lpd != null) h.remove(lpd);

                h = ld.getToObjectDef().getLinkProperties();
                lpd = (LinkProperty) h.find("Link", ld);
                if (lpd != null) h.remove(lpd);
            }
        };
        
        hubLinkProperty = new Hub(LinkProperty.class);
        hmLinkProperty = new HubMerger(model.getObjectDefs(), hubLinkProperty, ObjectDef.PROPERTY_LinkProperties, false, null, true);
        hubLinkProperty.addHubListener(hlProperty);
        
    }


    private void setupLinkFKeyListener() {
        hlLinkFKey = new HubListenerAdapter() {
            public void hubPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
                
                Object obj = e.getObject();
                if (!(obj instanceof LinkFKey)) return;
                LinkFKey fk = (LinkFKey) obj;
                
                if (s.equalsIgnoreCase(LinkFKey.PROPERTY_DataSourceName)) {
                    Model mc;
                    mc = getAdd(fk);
                    if (mc != null) {
                    	generateCode(mc);
                    	return;
                    }
                    mc = new Change();
                    mc.type = mc.TYPE_LINKFKEYCHANGE;
                    mc.property = e.getPropertyName();
                    mc.object = fk;
                    mc.oldValue = e.getOldValue();
                    mc.newValue = e.getNewValue();
                    addChange(mc);
                }
            }

            Model getAdd(LinkFKey fk) {
            	Link ld = fk.getLink();
                for (int i=0; ;i++) {
                    Change mc = (Change) model.getChanges().elementAt(i);
                    if (mc == null) break;
                    Object obj = mc.getObject();
                    if (obj == fk) {
                    	if (mc.getType() == Model.TYPE_LINKFKEYADD) return mc;
                    }
                    if (obj == ld) {
                    	if (mc.getType() == Model.TYPE_LINKADD) return mc;
                    }
                }
                return null;
            }
            
            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            public void hubAdd(HubEvent e) {
                if (isLoading()) return;
            	Change mc = getAdd((LinkFKey)e.getObject());
            	if (mc != null) generateCode(mc);
            	else {
	            	mc = new Change();
	                mc.type = mc.TYPE_LINKFKEYADD;
	                mc.object = e.getObject();
	                addChange(mc);
            	}
            }

            private Hub holdHub;
            public void hubBeforeDelete(HubEvent e) {
                LinkFKey fk = (LinkFKey) e.getObject();
                holdHub = fk.getLink().getFromLinkFKeys();
                if (!holdHub.contains(fk)) holdHub = fk.getLink().getToLinkFKeys();
            }
            public void hubAfterDelete(HubEvent e) {
                LinkFKey fk = (LinkFKey) e.getObject();
            	Change mc = getAdd(fk);
                if (mc != null) {
                	if (mc.getObject() != fk) generateCode(mc);
                	else hubModelChange.remove(mc);
                }
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_LINKFKEYREMOVE;
                    mc.object = fk;
                    mc.temp = holdHub;
                    addChange(mc);
                }
            }
        };
        hubLinkFKeyFrom = new Hub(LinkFKey.class);
        hmLinkFKeyFrom = new HubMerger(getLinks(), hubLinkFKeyFrom, Link.PROPERTY_FromLinkFKeys, false, null, true);
        hubLinkFKeyFrom.addListener(hlLinkFKey);
    	
        hubLinkFKeyTo = new Hub(LinkFKey.class);
        hmLinkFKeyTo = new HubMerger(getLinks(), hubLinkFKeyTo, Link.PROPERTY_ToLinkFKeys, false, null, true);
        hubLinkFKeyTo.addListener(hlLinkFKey);
    }
    
    private Model getAdd(IndexProperty ipd) {
    	return getAdd(ipd.getIndex());
    }
    private Model getAdd(Index id) {
        ObjectDef od = id.getObjectDef();
    	for (int i=0; ;i++) {
            Change mc = (Change) model.getChanges().elementAt(i);
            if (mc == null) break;
            Object obj = mc.getObject();
            if (obj == id) {
                if (mc.getType() == Model.TYPE_INDEXADD) return mc;
            }
            else {
            	if (obj == od && mc.getType() == mc.TYPE_OBJECTADD) return mc;
            }
        }
        return null;
    }
    private void setupIndexListener() {
        hlIndex = new HubListenerAdapter() {
            
            public void hubPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
                
                Object obj = e.getObject();
                if (!(obj instanceof Index)) return;
                Index id = (Index) obj;
                if (s.equalsIgnoreCase(Index.PROPERTY_DataSourceName)) {
                    Change mc = getAdd(id);
                    if (mc != null) generateCode(mc);
                    else {
	                    mc = new Change();
	                    mc.type = mc.TYPE_INDEXCHANGE;
	                    mc.property = e.getPropertyName();
	                    mc.object = id;
	                    mc.oldValue = e.getOldValue();
	                    mc.newValue = e.getNewValue();
	                    addModelChange(mc);
                    }
                }
            }

            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            public void hubAdd(HubEvent e) {
                if (isLoading()) return;
                Object obj = e.getObject();
                if (!(obj instanceof Index)) return;

                Change mc = getAdd((Index)obj);
                if (mc != null) generateCode(mc);
                else {
	                mc = new Change();
	                mc.type = mc.TYPE_INDEXADD;
	                mc.object = obj;
	                addModelChange(mc);
                }
            }

            public void hubBeforeDelete(HubEvent e) {
                Index id = (Index) e.getObject();
                Change mc = getAdd(id);
                if (mc != null) {
                	if (mc.type == mc.TYPE_INDEXADD) hubModelChange.remove(mc);
                	else generateCode(mc);
                }
                else {
                    // remove any previous changes
            		for (int i=0; ;i++) {
                        mc = (Change) model.getChanges().elementAt(i);
                        if (mc == null) break;
                        if (mc.getObject() != id) continue;
                    	hubModelChange.remove(mc);
                    	i--;
                    }
            		mc = new Change();
                    mc.type = mc.TYPE_INDEXREMOVE;
                    mc.object = id;
                    addModelChange(mc);
                }
            }
        };
        hubIndex = new Hub(Index.class);
        hmIndex = new HubMerger(getObjectDefs(), hubIndex, ObjectDef.PROPERTY_Indexs, false, null, true);
        hubIndex.addListener(hlIndex);

        
        hlIndexProperty = new HubListenerAdapter() {
            Model getAdd(Index id, IndexProperty pd) {
            	ObjectDef od = id.getObjectDef();
            	for (int i=0; ;i++) {
                    Change mc = (Change) hubModelChange.elementAt(i);
                    if (mc == null) break;
                    Object obj = mc.getObject();
                    if (mc.getType() == mc.TYPE_OBJECTADD && mc.getObject() == od) return mc;
                    if (mc.getType() == mc.TYPE_INDEXADD && mc.getObject() == id) return mc;
                    if (mc.getType() == mc.TYPE_INDEXPROPERTYADD) {
                    	if (pd == mc.object) return mc;
                    }
                }
                return null;
            }
            
            public void hubPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (s == null) return;
                
                Object obj = e.getObject();
                if (!(obj instanceof IndexProperty)) return;
                IndexProperty ipd = (IndexProperty) obj;
                
                if (s.equalsIgnoreCase(IndexProperty.PROPERTY_Desc)) {
                    Change mc = getAdd(ipd.getIndex(), ipd);
                    if (mc != null) {
                    	generateCode(mc);
                    }
                    else {
	                    mc = new Change();
	                    mc.type = mc.TYPE_INDEXPROPERTYCHANGE;
	                    mc.property = e.getPropertyName();
	                    mc.object = ipd;
	                    mc.oldValue = e.getOldValue();
	                    mc.newValue = e.getNewValue();
	                    addModelChange(mc);
                    }
                }
            }
            
            
            public void hubInsert(HubEvent e) {
                hubAdd(e);
            }

            public void hubAdd(HubEvent e) {
                if (isLoading()) return;
            	IndexProperty ipd = (IndexProperty) e.getObject();
           		Index id = ipd.getIndex();
        		Change mc = getAdd(id, null);
                if (mc != null) {
                    generateCode(mc);
                }
                else {
	            	mc = new Change();
	                mc.type = mc.TYPE_INDEXPROPERTYADD;
	                mc.object = ipd;
	                addModelChange(mc);
                }
            }

            private Index holdIndex;
            public void hubBeforeDelete(HubEvent e) {
            	IndexProperty ipd = (IndexProperty) e.getObject();
            	holdIndex = ipd.getIndex();
            }

            public void hubAfterDelete(HubEvent e) {
            	IndexProperty ipd = (IndexProperty) e.getObject();
            	Index id = holdIndex;
        		Change mc = getAdd(id, ipd);
        		if (mc != null) {
                	if (mc.getObject() == ipd) hubModelChange.remove(mc);
                	else generateCode(mc);
                }
                else {
                    mc = new Change();
                    mc.type = mc.TYPE_INDEXPROPERTYREMOVE;
                    mc.object = ipd;
                    mc.temp = id;
                    addChange(mc);
                }
            }
        };
        hubIndexProperty = new Hub(IndexProperty.class);
        hmIndexProperty = new HubMerger(hubIndex, hubIndexProperty, Index.PROPERTY_IndexProperties, false, null, true);
        hubIndexProperty.addListener(hlIndexProperty);
    }

    
    public void generateCode(Change change) {
    	//qqqqqqqqqqqqqqqqqq
    }
    
}



