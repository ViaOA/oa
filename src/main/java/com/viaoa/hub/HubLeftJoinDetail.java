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
package com.viaoa.hub;

import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.object.OALeftJoin;
import com.viaoa.object.OALeftJoinDetail;
import com.viaoa.object.OAObject;

/**
 * Combines two hubs into a new single hub to create the equivalent of a database left join, where all
 * of the "left" side objects are in the list.
 * 
 * The combined Hub (see getCombinedHub) uses OAObject OALeftJoinDetail<A,B>, where A is the same class
 * as the left Hub and B is a Hub of the the same as the right Hub.
 * 
 * @see HubGroupBy#
 * @see HubLeftJoin#
 * 
 * @author vvia
 */
public class HubLeftJoinDetail<A extends OAObject, B extends OAObject> {
    private Hub<A> hubA;
    private Hub<B> hubB;
    private Hub<OALeftJoinDetail<A, B>> hubCombined;
    private String propertyPath;
    private String listenPropertyName;

    private final static AtomicInteger aiCnt = new AtomicInteger();

    /**
     * Combine a left and right hubs on a propertyPath to form Hub.
     * 
     * @param hubA
     *            left object
     * @param hubB
     *            right object
     * @param propertyPath
     *            pp of the property from the right object to get left object.
     */
    public HubLeftJoinDetail(Hub<A> hubA, Hub<B> hubB, String propertyPath) {
        this.hubA = hubA;
        this.hubB = hubB;
        this.propertyPath = propertyPath;
        setup();
    }

    /**
     * 
     * @return Hub of combined objects
     */
    public Hub<OALeftJoinDetail<A, B>> getCombinedHub() {
        if (hubCombined != null) return hubCombined;
        hubCombined = new Hub(OALeftJoinDetail.class);
        return hubCombined;
    }

    void setup() {
        getCombinedHub().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                // set the active object in hub A&B when hubCombine.AO is changed
                OALeftJoinDetail obj = (OALeftJoinDetail) e.getObject();
                if (obj == null) {
                    hubA.setAO(null);
                    hubB.setAO(null);
                }
                else {
                    hubA.setAO(obj.getA());
                    hubB.setAO(null);
                }
            }
        });

        hubA.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }

            @Override
            public void afterAdd(HubEvent e) {
                A a = (A) e.getObject();
                OALeftJoinDetail<A, B> c = new OALeftJoinDetail(a);
                hubCombined.add(c);
            }

            @Override
            public void afterRemove(HubEvent e) {
                A a = (A) e.getObject();
                for (;;) {
                    OALeftJoinDetail c = hubCombined.find(OALeftJoin.P_A, a);
                    if (c == null) break;
                    hubCombined.remove(c);
                }
            }

            @Override
            public void onNewList(HubEvent e) {
                hubCombined.clear();
                for (A a : hubA) {
                    hubCombined.add(new OALeftJoinDetail(a));
                }
                for (B b : hubB) {
                    add(b);
                }
            }
        });

        for (A a : hubA) {
            hubCombined.add(new OALeftJoinDetail(a));
        }

        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }

            @Override
            public void afterAdd(HubEvent e) {
                B b = (B) e.getObject();
                add(b);
            }

            @Override
            public void afterRemove(HubEvent e) {
                B b = (B) e.getObject();
                remove(b);
            }

            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (!listenPropertyName.equalsIgnoreCase(s)) return;

                remove((B) e.getObject());
                add((B) e.getObject());
            }

            @Override
            public void onNewList(HubEvent e) {
                hubCombined.clear();
                for (A a : hubA) {
                    hubCombined.add(new OALeftJoinDetail(a));
                }
                for (B b : hubB) {
                    add(b);
                }
            }

            @Override
            public void afterChangeActiveObject(HubEvent e) {
                B b = (B) e.getObject();
                if (b != null) {
                    for (OALeftJoinDetail lj : hubCombined) {
                        Hub h = lj.getHubB();
                        if (h.contains(b)) {
                            hubCombined.setAO(lj);
                            h.setAO(b);
                            return;
                        }
                    }
                }
                hubCombined.setAO(null);
            }
        };

        if (propertyPath == null || propertyPath.indexOf('.') < 0) {
            listenPropertyName = propertyPath;
            hubB.addHubListener(hl, propertyPath);
        }
        else {
            listenPropertyName = "hubCombinedDetail" + aiCnt.getAndIncrement();
            hubB.addHubListener(hl, listenPropertyName, new String[] { propertyPath });
        }

        for (B b : hubB) {
            add(b);
        }
    }

    private void add(B b) {
        if (b == null) return;
        Object valueA = b.getProperty(propertyPath);
        for (OALeftJoinDetail lj : hubCombined) {
            if (lj.getA() != valueA) continue;
            lj.getHubB().add(b);
        }
    }

    private void remove(B b) {
        for (OALeftJoinDetail lj : hubCombined) {
            Hub<B> h = lj.getHubB();
            if (h.contains(b)) {
                h.remove(b);
                return;
            }
        }
    }
}
