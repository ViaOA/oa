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

import java.io.*;

public class OAClassLoader extends ClassLoader {

    private final String className;
    private Class<?> clazz;

    public OAClassLoader(String className) {
        this.className = className;
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (!this.className.equals(className)) {
            return findSystemClass(className);
        }
        if (clazz != null) return clazz;

        String cn = className.replace('.', '/');
        InputStream is = ClassLoader.getSystemResourceAsStream(cn+".class");
        if (is == null) {
            throw new ClassNotFoundException("could not load class as resource using OAClassLoader");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for ( ;; ) {
            int x;
            try {
                x = is.read();
            }
            catch (IOException e) {
                throw new ClassNotFoundException("IO exception while reading "+className+".class", e);
            }
            if (x < 0) break;
            baos.write(x);
        }
        byte[] bs = baos.toByteArray(); 
        
        clazz = super.defineClass(className, bs, 0, bs.length);

        return clazz;
    }

// test using Jar, or directory  qqqqqqqqqqqqqqq
    public static void main(String[] args) throws Exception {
        
        String cname = "com.viaoa.util.Test";
        
        OAClassLoader test = new OAClassLoader(cname);
        Class c = test.loadClass(cname);
        TestInterface t = (TestInterface) c.newInstance();
        t.test();
        System.out.println("Done");
    }
    
}
