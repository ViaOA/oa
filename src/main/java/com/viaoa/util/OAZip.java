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
