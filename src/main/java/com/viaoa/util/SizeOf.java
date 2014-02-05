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


import java.lang.instrument.Instrumentation; 
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;


/**
 * Used to get the actual object size, similar to C sizeof method.
 *
 * Create manifest file "sizeof.mf", with this line: (no begin/ending spaces)
 *     Premain-Class: com.viaoa.util.SizeOf
 * 
 *
 * create jar file with this class:
 *     
    <target name="JarSizeOf" description="Jar SizeOf">
        <jar basedir="bin" destfile="dist/sizeof.jar"  update="false" includes="** /SizeOf*.class" manifest="sizeof.mf"/>
    </target>
 
 * To run, add command line option to runtime:
 *     java -javaagent:sizeof.jar ...
 * 
 * @author vincevia
 *
 */
public class SizeOf { 
    private static Instrumentation instrumentation;
    private static ArrayList<Class<?>> alExclude = new ArrayList<Class<?>>(12);
    private static boolean bHasExcludes;
    private static int defaultSize;

    private IdentityHashMap<Object, Object> hashMap = new IdentityHashMap<Object, Object>(413);
 
    public static void premain(String args, Instrumentation instrumentation) { 
        SizeOf.instrumentation = instrumentation; 
    } 

    /**
     * Classes to exclude from sizeof.  These are usually classes that are know to be reused.
     * Note: "Class.class" is already included. 
     * @param clazz
     */
    public static void excludeClass(Class clazz) {
        if (clazz == null) return;
        bHasExcludes = true;
        if (!alExclude.contains(clazz)) {
            alExclude.add(clazz);
        }
    }
    
    /**
     * Get the runtime size of an object, not including the size of any references.
     */
    public static long sizeOf(Object objx) {
        return sizeOf(objx, false);
    }

    private static boolean bSingleError;
    
    /**
     * Get the runtime size of an object, with/out including the size of references.
     * @param objx
     * @param bIncludeReferences if true then all references are also included.
     */
    public static long sizeOf(Object objx, boolean bIncludeReferences) {
        if (instrumentation == null) {
            if (!bSingleError) {
                System.out.println("Instrumentation has not been set, see SizeOf.java JavaDoc for instructions, will return -1");
                bSingleError = true;
            }
            return -1;
        }
        
        if (objx == null) return 0;
        if (!bIncludeReferences) {
            // if (instrumentation == null) return 0;  // used when debugging
            return instrumentation.getObjectSize(objx);
        }
    
        if (defaultSize == 0) {
            defaultSize = (int) instrumentation.getObjectSize(new Object());
        }
        SizeOf sizeOf = new SizeOf();
        return sizeOf._sizeOf(objx);
    }

    
    /**
     * Recursive method to get sizeOf an object and reference objects.
     * 
     * @param objx
     * @return
     */
static long cnt;
static long fcnt;
    private long _sizeOf(Object objx) {
        if (objx == null) return 0;
        if (hashMap.containsKey(objx)) {
            fcnt++;
//if (fcnt % 500 == 0) System.out.println("fcnt="+fcnt);            
            return 0;
        }
        hashMap.put(objx, null);
        
        Class clazz = objx.getClass();
        if (clazz.isPrimitive()) return 0;

        long size = SizeOf.sizeOf(objx);
//System.out.println((cnt++)+" "+objx+" = "+size);        

cnt++;
//if (cnt % 2500 == 0) System.out.println("cnt="+cnt+", fcnt="+fcnt);            
        
        if (clazz.isArray()) {
            if (objx instanceof Object[]) {
                for (Object obj : (Object[]) objx) {
                    size += _sizeOf(obj);
                }
            }
            else {
                // primitive, already added by sizeOf
            }
            return size;
        } 

        
        for ( ; clazz != null ; clazz = clazz.getSuperclass() ) {
            for (Field field : clazz.getDeclaredFields()) {
                
                Class c = field.getType();
                
                if (c.isPrimitive()) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isEnumConstant()) continue;
    
                if (c.equals(Class.class)) continue;
                if (c.equals(Enum.class)) continue;
                if (bHasExcludes && alExclude.contains(c)) continue;
                
                Object obj;
                try {
                    field.setAccessible(true);
                    obj = field.get(objx);
                } 
                catch (Exception e) {
                    //System.out.println("SizeOf exception for class="+clazz+", field="+field.getName()+", exception:"+e);
                    continue;
                }
                if (obj == null) continue;
                
                size += _sizeOf(obj) - defaultSize; // "-defaultSize" is to remove the amount for pointer (already added into parent object)
            }        
        }
        return size;
    }

    
    
    
    public static void main(String[] args) {
        String s;
        long x = SizeOf.sizeOf("abv", true);
        System.out.println("done =>>> "+x);
    }
    
} 
    
