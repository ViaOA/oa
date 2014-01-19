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
package com.viaoa.hub;

import java.util.HashSet;

import com.viaoa.object.OACascade;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

public class OAFinder<F,T> implements OAFilter<T>{
    private Hub<F> hubFrom;
    private Hub<T> hubTo;
    private OAPropertyPath<?> propertyPathNavTo, propertyPathMatch;

    private OALinkInfo[] liNavTo;
    private OALinkInfo[] liNavToRecursive;
    private OALinkInfo[] liMatch;
    private OALinkInfo[] liMatchRecursive;
    private OALinkInfo liRecursiveRoot;
    private OACascade navCascade;
    private boolean bNavRequiresCasade;
    private boolean bMatchRequiresCasade;

    /**
     * 
     * @param hubRoot hub to begin searching from
     * @param propertyPathNav path to find objects to then match
     * @param propertyPathMatch path of value to match
     */
    public OAFinder(Hub<F> hubFrom, String propPathNavTo, Hub<T> hubTo, String propPathMatch) {
        if (hubFrom == null) {
            throw new IllegalArgumentException("Root hub can not be null");
        }
        this.hubFrom = hubFrom;
        this.hubTo = hubTo;

        propertyPathNavTo = new OAPropertyPath(hubFrom.getObjectClass(), propPathNavTo);
        
        liNavTo = propertyPathNavTo.getLinkInfos();
        liNavToRecursive = propertyPathNavTo.getRecursiveLinkInfos();
        
        Class c;
        Class[] cs = propertyPathNavTo.getClasses();
        if (cs == null || cs.length == 0) c = hubFrom.getObjectClass();
        else c = cs[cs.length-1];
        if (!c.equals(hubTo.getObjectClass())) {
            throw new RuntimeException("hubTo is expected to be for class="+hubTo.getObjectClass()+", but class="+c);
        }
        propertyPathMatch = new OAPropertyPath(hubTo.getObjectClass(), propPathMatch);
        liMatch = propertyPathMatch.getLinkInfos();
        liMatchRecursive = propertyPathMatch.getRecursiveLinkInfos();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hubFrom.getObjectClass());
        liRecursiveRoot = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        
        if (liNavTo != null) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : liNavTo) {
                if (hs.contains(li.getToClass())) {
                    bNavRequiresCasade = true;
                    break;
                }
                hs.add(li.getToClass());
            }
        }
        if (liMatch != null) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : liMatch) {
                if (hs.contains(li.getToClass())) {
                    bMatchRequiresCasade = true;
                    break;
                }
                hs.add(li.getToClass());
            }
        }
    }

    public void find(Object findObj) {
        if (bNavRequiresCasade) navCascade = new OACascade();
        try {
            this._find(hubFrom, findObj);
        }
        finally {
            navCascade = null;
        }
    }

    private void _find(Hub<F> hub, Object findObj) {
        for (Object obj : hub) {
            _find(obj, findObj, 0);
            if (liRecursiveRoot != null) {
                Hub h = (Hub) liRecursiveRoot.getValue(obj);
                _find(h, findObj);
            }
        }
    }
    
//qqqqqqqq option to supply filters
    // OAFilter getFilter(String name)
    //   get/set FilterPackageName
    // 
    
    private void _find(Object obj, Object findObj, int pos) {
        if (obj == null) return;
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _find(objx, findObj, pos);
            }
            return;
        }
        if (!(obj instanceof OAObject)) return;
        if (navCascade != null && navCascade.wasCascaded((OAObject)obj, true)) return;
        
        if (liNavTo == null || pos >= liNavTo.length) {
            onFindValue( (T) obj, findObj);
        }
        
        // check if recursive
        if (pos > 0 && liNavToRecursive != null && pos <= liNavToRecursive.length) {
            if (liNavToRecursive[pos-1] != null) {
                Object objx = liNavToRecursive[pos-1].getValue(obj);
                _find(objx, findObj, pos-1); // go up a level to then go through hub
            }
        }

        if (liNavTo != null && pos < liNavTo.length) {
            Object objx = liNavTo[pos].getValue(obj);
            _find(objx, findObj, pos+1);
        }
    }

    
    /**
     * This is called when an object is found using the propPathNavTo.  
     * @param obj object object found in propPathNavTo
     */
    protected void onFindValue(T obj, Object findObj) {
        System.out.println("=======> "+obj+", isUsed-"+isUsed(obj));
        
    }
    

    @Override
    public boolean isUsed(T obj) {
        // TODO Auto-generated method stub
        return false;
    }
    
    
}




