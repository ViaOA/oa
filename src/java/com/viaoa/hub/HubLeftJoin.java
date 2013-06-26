package com.viaoa.hub;

import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.object.OALeftJoin;
import com.viaoa.object.OAObject;
import com.viaoa.util.OANullObject;
import com.viaoa.util.OAString;

/**
 * Combines two hubs into a new single hub to create the equivalent of
 * a database left join, where all of the "left" side objects are in the list.
 * 
 * The combined Hub (see getCombinedHub) uses OAObject OALeftJoin<A,B>, where A is the 
 * same class as the left Hub and B is the same as the right Hub.
 * 
 * A property path that uses A or B will need to use casting.  
 * Example:  LeftHub=hubDepartments, RightHub=hubEmployees with last name "Jones" 
 *    the combined Hub A=Dept ref, B=Employee ref, can use hubCombined with properties
 *    from A or B, with casting: 
 *       hubCombined, "(com.xxx.Department)A.manager.fullName"
 *         or a better solution: OAString.cpp(Departement.class, OALeftJoin.PROPERTY_A, Department.PROPERTY_Manager, Employee.PROPERTY_FullName)
 *  
 * @author vvia
 */
public class HubLeftJoin<A extends OAObject, B extends OAObject> {
    
    private Hub<A> hubA;
    private Hub<B> hubB;
    private Hub<OALeftJoin<A,B>> hubCombined;
    private String propertyPath;
    private String listenPropertyName;

    private final static AtomicInteger aiCnt = new AtomicInteger();
    
    /**
     * Combine a left and right hubs on a propertyPath to form Hub.  
     * @param hubA left object
     * @param hubB right object
     * @param propertyPath pp of the property from the right object to get left object.
     */
    public HubLeftJoin(Hub<A> hubA, Hub<B> hubB, String propertyPath) {
        this.hubA = hubA;
        this.hubB = hubB;
        this.propertyPath = propertyPath;
        setup();
    }
    
    /**
     * 
     * @return Hub of combined objects
     */
    public Hub<OALeftJoin<A,B>> getCombinedHub() {
        if (hubCombined != null) return hubCombined;
        hubCombined = new Hub(OALeftJoin.class);
        return hubCombined;
    }

    void setup() {
        getCombinedHub().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                // set the active object in hub A&B when hubCombine.AO is changed
                OALeftJoin obj = (OALeftJoin) e.getObject();
                hubA.setAO( obj.getA() );
                hubB.setAO( obj.getB() );
            }
        });
        
        hubA.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                OALeftJoin<A,B> c = new OALeftJoin();
                A a = (A) e.getObject();
                c.setA(a);
                hubCombined.add(c);
            }
            @Override
            public void afterRemove(HubEvent e) {
                A a = (A) e.getObject();
                OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, a);
                hubCombined.remove(c);
            }
            @Override
            public void onNewList(HubEvent e) {
                hubCombined.clear();
                for (A a : hubA) {
                    hubCombined.add(new OALeftJoin(a, null));
                }
                for (B b : hubB) {
                    Object valueA = b.getProperty(propertyPath);

                    if (valueA != null) {
                        OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, valueA);
                        if (c != null) c.setB(b);
                    }
                }
            }
        });
        
        for (A a : hubA) {
            hubCombined.add(new OALeftJoin(a, null));
        }
        
        
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                B b = (B) e.getObject(); 
                Object value = b.getProperty(propertyPath);
                
                if (value != null) {
                    OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, value);
                    if (c != null) c.setB((B) e.getObject());
                }
            }
            @Override
            public void afterRemove(HubEvent e) {
                B b = (B) e.getObject(); 
                Object value = b.getProperty(propertyPath);

                if (value != null) {
                    OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, value);
                    if (c != null) c.setB(null);
                }
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (!listenPropertyName.equalsIgnoreCase(s)) return;
                
                Object objx = e.getOldValue();
                if (objx instanceof OANullObject) objx = null;
                A a = (A) objx;
                if (a != null) {
                    OALeftJoin c = (OALeftJoin) hubCombined.find(OALeftJoin.PROPERTY_A, a);
                    if (c != null) c.setB(null);
                }

                a = (A) e.getNewValue(); 
                if (a != null) {
                    OALeftJoin c = (OALeftJoin) hubCombined.find(OALeftJoin.PROPERTY_A, a);
                    if (c != null) c.setB((B) e.getObject());
                }
            }
            @Override
            public void onNewList(HubEvent e) {
                for (OALeftJoin lj : hubCombined) {
                    lj.setB(null);
                }
                for (B b : hubB) {
                    Object value = b.getProperty(propertyPath);

                    if (value != null) {
                        OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, value);
                        if (c != null) c.setB(b);
                    }
                }
            }
        };
        if (propertyPath == null || propertyPath.indexOf('.') < 0) {
            hubB.addHubListener(hl, propertyPath);
            listenPropertyName = propertyPath;
        }
        else {
            listenPropertyName = "hubCombined"+aiCnt.getAndIncrement();
            hubB.addHubListener(hl, listenPropertyName, new String[] {propertyPath});
        }

        for (B b : hubB) {
            Object value = b.getProperty(propertyPath);

            if (value != null) {
                OALeftJoin c = hubCombined.find(OALeftJoin.PROPERTY_A, value);
                if (c != null) c.setB(b);
            }
        }
    }
}
