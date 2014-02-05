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
package com.viaoa.util;

import java.util.*;
import java.lang.reflect.*;

import com.viaoa.hub.Hub;


/**
    OAComparator is used to sort objects.  Hub.sort uses this when creating a HubSorter.
    @see Hub#sort
    @see HubSortListener
*/
public class OAComparator implements Comparator {
    Class clazz;
    String propertyPaths;
    boolean bAscending;
    Method[][] methodss;
    boolean[] bAscendings; 

    /**
     * @param clazz
     * @param propertyPaths, can include keywords "ASC" or "DESC" to determine ascending or descending.
     * @param bAscending default value for sorting, true=Ascending, false=Descending
     */
    public OAComparator(Class clazz, String propertyPaths, boolean bAscending) {
        this.clazz = clazz;
        this.propertyPaths = propertyPaths;
        this.bAscending = bAscending;
    }

    public int compare(Object o1, Object o2) {
        int x = preCheck(o1, o2);
        if (x < 5) return x;

        if (methodss == null) {
            init();
        }

        if (methodss.length == 0) {
            x = 0;
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                x = ((Comparable)o1).compareTo(((Comparable)o2));
            }
            if (!bAscending) {
                if (x < 0) return 1;
                if (x > 0) return -1;
            }
            return x;
        }
        
        for (int i=0; i<methodss.length; i++) {
        	boolean bAscend = bAscending;
        	if (bAscendings != null && i<bAscendings.length) bAscend = bAscendings[i];
            x = compare(o1, o2, methodss[i], bAscend);
            if (x != 0) return x;
        }
        
        return 0;
    }

    private int compare(Object o1, Object o2, Method[] methods, boolean bAscend) {
        if (methods != null && methods.length != 0) {
            o1 = OAReflect.getPropertyValue(o1, methods);
            o2 = OAReflect.getPropertyValue(o2, methods);
        }

        int x = 0;
        if (o1 == null || o2 == null) {
            if (o1 == o2) x = 0;
            else if (o1 == null) x = -1;
            else x = 1;
        }
        else {
            boolean bComparable = true;
            if (!(o1 instanceof Comparable)) bComparable = false;
            else if (!(o2 instanceof Comparable)) bComparable = false;
            
            if (!bComparable) {
                x = 0;
                if (o1 instanceof Boolean && o2 instanceof Boolean) {
                    boolean b1 = ((Boolean) o1).booleanValue();
                    boolean b2 = ((Boolean) o2).booleanValue();
                    if (b1 == b2) x = 0;
                    else if (b1) x = 1;
                    else x = -1;
                    if (!bAscend && x != 0) x = -x;
                }
                return x;
            }

            // Strings will use a case insensitive search
            if (o1 instanceof String) o1 = ((String) o1).toUpperCase();
            if (o2 instanceof String) o2 = ((String) o2).toUpperCase();

            Comparable c1 = (Comparable) o1;
            Comparable c2 = (Comparable) o2;

            try {
                x = c1.compareTo(c2);
            }
            catch (Exception e) {
                try {
                    x = -c2.compareTo(c1);
                }
                catch (Exception ex) {
                    x = -1;
                }
            }
        }
        if (bAscend || x == 0) return x;
        return -x;
    }

    protected int preCheck(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) {
            if (bAscending) return -1;
            return 1;
        }
        if (o2 == null) {
            if (bAscending) return 1;
            return -1;
        }
        return 5;
    }

    protected void init() {
        if (clazz == null) return;
        if (propertyPaths == null || propertyPaths.length() == 0) {
            // sort on object itself
            methodss = new Method[0][];
            return;
        }

        ArrayList al = new ArrayList(7);
        ArrayList alAsc = new ArrayList(7);
        StringTokenizer st = new StringTokenizer(propertyPaths, ", ", true);
        Method[] ms = null;
        boolean bAllowDesc = propertyPaths.equalsIgnoreCase("desc");
        for ( ; st.hasMoreElements() ; ) {
            String prop = (String) st.nextElement();

            if (prop.equals(" ")) {
                bAllowDesc = true;
                continue;
            }
            if (prop.equals(",")) {
                if (bAllowDesc) alAsc.add(new Boolean(bAscending));
                bAllowDesc = false;
                continue;
            }
            if (prop.equalsIgnoreCase("desc") && bAllowDesc) {
                bAllowDesc = false;
                alAsc.add(new Boolean(false));
                continue;
            }
            if (prop.equalsIgnoreCase("asc") && bAllowDesc) {
                bAllowDesc = false;
                alAsc.add(new Boolean(true));
                continue;
            }
            
            try {
                ms = OAReflect.getMethods(clazz, prop);
                bAllowDesc = true;
            }
            catch (Exception e) {
                if (prop.equalsIgnoreCase("by")) continue;
            	throw new RuntimeException(e);
            }
            al.add(ms);
        }
        if (bAllowDesc) alAsc.add(new Boolean(bAscending));
        methodss = new Method[al.size()][];

        al.toArray(methodss);
        
        // 2006/10/25
        int x = alAsc.size();
        bAscendings = new boolean[x];
        for (int i=0; i<x; i++) {
        	Boolean b = (Boolean) alAsc.get(i);
        	bAscendings[i] = b.booleanValue();
        }
        if (x == 1) bAscending = bAscendings[0];
    }

    public static void main(String[] args) {
        Hub<Double> hub = new Hub<Double>(Double.class);
        hub.add(1.3);
        hub.add(1.1);
        hub.add(1.0);
        hub.add(1.2);
        hub.add(1.02);
        
        hub.sort("", false);
        
        for (double d : hub) {
            System.out.println(""+d);
        }
        
        int x = 4;
        x++;

        
    }
}

