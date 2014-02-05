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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class OAZip {

    public void test() throws Exception {
        File file = new File("c:\\temp\\job.zip");
        
        // check if Zipped
        ZipEntry zipEntry = null;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(file));
            for ( ;; ) {
                zipEntry = zis.getNextEntry();
                if (zipEntry == null) break;
                System.out.println(zipEntry.getName());
                // zis.closeEntry();
                break;
            }
        }
        finally {
            if (zis != null) {
                zis.close();
            }
        }

        // check if XML
        BufferedReader br;
        if (zipEntry != null) {
            ZipFile zip = new ZipFile(file);
            InputStream inputStream = zip.getInputStream(zipEntry);
            br = new BufferedReader(new InputStreamReader(inputStream));
        }
        else {
            br = new BufferedReader(new FileReader(file));
        }
        
        char[] chars = new char[255];
        int x = br.read(chars);
        String s = new String(chars);
        s = s.toLowerCase();
        boolean bIsXML = (s.indexOf("?xml") >= 0) || (s.indexOf("<jobs>") >= 0) || (s.indexOf("<job") >= 0);

        
        br.close();
        if (zipEntry != null) {
            ZipFile zip = new ZipFile(file);
            InputStream inputStream = zip.getInputStream(zipEntry);
            br = new BufferedReader(new InputStreamReader(inputStream));
        }
        else {
            br = new BufferedReader(new FileReader(file));
        }
        
        x = br.read(chars);
        System.out.println("==> "+(new String(chars)));
    }
    
    public static void main(String[] args) throws Exception {
        OAZip z = new OAZip();
        z.test();
    }
}
