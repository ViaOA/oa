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

import javax.swing.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Vector;
import java.io.*;

/**
 * Subclass of java.io.File that includes extra functionality.
 * <p>
 * Note: All file names separators will automatically be converted to match the
 * Operating System.
 * 
 * @see OAString#convertFileName
 */
public class OAFile extends java.io.File {
    static final long serialVersionUID = 1L;

    public static final String FS = File.separator;
    public static final String NL = System.getProperty("line.separator");

    public OAFile(String fname) {
        super(OAString.convertFileName(fname));
    }

    /**
     * Copy this file to another file.
     * 
     * @see #copy(String,String)
     */
    public boolean copyTo(String fileNameTo) {
        try {
            copy(this.getPath(), fileNameTo);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Copy this file to another file.
     * 
     * @see #copy(String,String)
     */
    public boolean copyTo(OAFile fileTo) {
        if (fileTo == null) return false;
        try {
            copy(this.getPath(), fileTo.getPath());
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Create all directories for fileName.
     */
    public void mkdirsForFile() {
        mkdirsForFile(getPath());
    }

    /**
     * Converts fileName path to correct system file.separator characters.
     * 
     * @return new String with corrected file path characters.
     */
    public static String convertFileName(String fileName) {
        return convertFileName(fileName, false);
    }

    /**
     * Converts fileName path to correct system file.separator characters.
     * 
     * @return new String with corrected file path characters.
     */
    public static String convertFileName(String fileName, boolean bEndWithSlashChar) {
        if (fileName == null) return null;
        fileName = fileName.replace('/', File.separatorChar);
        fileName = fileName.replace('\\', File.separatorChar);
        if (bEndWithSlashChar && !fileName.endsWith(FS)) fileName += File.separatorChar;
        return fileName;
    }

    public static String getFileName(String filePath) {
        filePath = filePath.replace('\\', '/');

        int x = filePath.lastIndexOf('/');
        if (x >= 0) {
            filePath = filePath.substring(x + 1);
        }
        filePath = convertFileName(filePath);
        return filePath;
    }

    public static String getDirectoryName(String filePath) {
        filePath = filePath.replace('\\', '/');
        String dir = ".";

        int x = filePath.lastIndexOf('/');
        if (x >= 0) {
            dir = filePath.substring(0, x);
        }
        dir = convertFileName(dir);
        return dir;
    }

    public static String getExtension(File file) {
        if (file == null) return null;
        return getExtension(file.getName());
    }
    public static String getExtension(String filePath) {
        String ext;
        int x = filePath.lastIndexOf('.');
        if (x >= 0) {
            ext = filePath.substring(x+1);
        }
        else ext = "";
        return ext;
    }
    
    /**
     * Create directories for fileName.<br>
     * Compared to the method in the File.class, "File.mkdirs()" which creates a
     * directory using the full fileName, where fileName itself will end up
     * being a directory.
     * 
     * This method assumes that the fileName is for a file and will then create
     * the directories needed so that the file can be saved.
     */
    public static void mkdirsForFile(String fileName) {
        if (fileName == null) return;
        fileName = OAString.convertFileName(fileName);
        int pos = fileName.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            File f = new File(fileName.substring(0, pos));
            f.mkdirs();
        }
    }

    public static void mkdirsForFile(File file) {
        if (file == null) return;
        String fileName = file.getAbsolutePath();
        fileName = OAString.convertFileName(fileName);
        int pos = fileName.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            File f = new File(fileName.substring(0, pos));
            f.mkdirs();
        }
    }

    /**
     * Renames this file to another name. This will create the required
     * directories for the new file.
     */
    public void renameTo(String fileName) {
        if (fileName != null) {
            File f1 = new File(OAString.convertFileName(this.getPath()));
            if (f1.exists()) {
                OAFile.mkdirsForFile(fileName);
                File f2 = new File(OAString.convertFileName(fileName));
                f1.renameTo(f2);
            }
        }
    }

    /**
     * Copy a file to another file. This will create the required directories
     * for the new file.
     * <p>
     * NOTE: if the fileNameTo already exists, it will be overwritten.
     */
    public static void copy(String fileNameFrom, String fileNameTo) throws Exception {
        if (fileNameFrom == null || fileNameTo == null) {
            return;
        }
        fileNameFrom = OAString.convertFileName(fileNameFrom);
        File fileFrom = new File(fileNameFrom);

        fileNameTo = OAString.convertFileName(fileNameTo);
        File fileTo = new File(fileNameTo);

        copy(fileFrom, fileTo);
    }

    public static void copy(File file, File fileTo) throws Exception {
        if (file == null || fileTo == null) return;

        if (!file.exists()) {
            throw new Exception("File " + file.getAbsolutePath() + " not found");
        }

        mkdirsForFile(fileTo);
        if (fileTo.exists()) {
            fileTo.delete();
        }

        InputStream is = new FileInputStream(file);
        OutputStream os = new FileOutputStream(fileTo);

        int bufferSize = 1024 * 8;
        byte[] bs = new byte[bufferSize];

        for (int i = 0;; i++) {
            int x = is.read(bs, 0, bufferSize);
            if (x < 0) break;
            os.write(bs, 0, x);
        }
        is.close();
        os.close();
    }

    /**
     * Copy a file from class/jar to file.
     * 
     * @param c
     * @param resourceName
     *            class path name for file to read
     * param fname
     *            file name to save as
     * param estimatedSize
     * @return true if successful, false if resource did not exist
     * @throws Exception
     */
    public static boolean copyResourceToFile(Class c, String resourceName, String fname) throws Exception {
        if (fname == null) return false;
        fname = OAString.convertFileName(fname);

        InputStream is = c.getResourceAsStream(resourceName);
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(resourceName);
            if (is == null) return false;
        }

        mkdirsForFile(fname);
        File fileTo = new File(fname);

        OutputStream os = new FileOutputStream(fileTo);

        int bufferSize = 1024 * 2;
        byte[] bs = new byte[bufferSize];

        for (int i = 0;; i++) {
            int x = is.read(bs, 0, bufferSize);
            if (x < 0) break;
            os.write(bs, 0, x);
        }
        is.close();
        os.close();
        return true;
    }

    /**
     * Read the contents of a text file from a specific class location. This
     * will read from a jar file.
     * 
     * param fname
     *            '/' seperated file name, located from the class. If fname
     *            begins with '/' then the file will go to the root directory.
     */
    public static String[] readResourceTextFile(Class c, String resourceName) throws Exception {
        InputStream is = c.getResourceAsStream(resourceName);
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(resourceName);
            if (is == null) return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        ArrayList<String> al = new ArrayList<String>(120);
        for (int i = 0;; i++) {
            String s = reader.readLine();
            if (s == null) break;
            al.add(s);
        }
        is.close();
        return al.toArray(new String[0]);
    }

    /**
     * Read the contents of a text file from a specific class location. This
     * will read from a jar file.
     * 
     * @param fname
     *            '/' seperated file name, located from the class. If fname
     *            begins with '/' then the file will go to the root directory.
     */
    public static String readTextFile(Class c, String fname, int estimatedSize) throws Exception {
        if (fname == null) return null;
        // fname = OAString.convertFileName(fname); // dont convert, this reads
        // from class and should be using '/'
        InputStream is = c.getResourceAsStream(fname);
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(fname);
            if (is == null) {
                return null;
            }
        }

        StringBuffer sb = new StringBuffer(estimatedSize);
        for (;;) {
            int x = is.read();
            if (x < 0) break;
            sb.append((char) x);
        }
        is.close();
        return new String(sb);
    }

    public static String readTextFile(File file, int estimatedSize) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer sb = new StringBuffer(estimatedSize);
        for (;;) {
            String line = reader.readLine();
            if (line == null) break;
            sb.append(line);
            sb.append(NL);
        }
        reader.close();
        return new String(sb);
    }

    public static String readTextFile(String fname, int estimatedSize) throws Exception {
        if (fname == null) return null;
        fname = OAString.convertFileName(fname);
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        if (estimatedSize < 100) estimatedSize = 1024 * 4;
        StringBuffer sb = new StringBuffer(estimatedSize);
        for (;;) {
            String line = reader.readLine();
            if (line == null) break;
            sb.append(line);
            sb.append(NL);
        }
        reader.close();
        return new String(sb);
    }

    public static boolean writeTextFile(String fname, String data) throws Exception {
        if (fname == null) return false;
        fname = OAString.convertFileName(fname);

        mkdirsForFile(fname);
        File fileTo = new File(fname);

        OutputStream os = new FileOutputStream(fileTo);

        if (data != null) os.write(data.getBytes());

        os.close();
        return true;
    }

    public static boolean writeTextFile(File file, String data) throws Exception {
        if (file == null) return false;

        mkdirsForFile(file);
        OutputStream os = new FileOutputStream(file);

        if (data != null) os.write(data.getBytes());

        os.close();
        return true;
    }

    /**
     * Remove directory and all children files.
     */
    public static void rmDir(File f) throws IOException {
        delTree(f);
    }
    public static void removeDir(File f) throws IOException {
        delTree(f);
    }
    public static void delTree(File f) throws IOException {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) {
                for (File c : fs) {
                    delTree(c);
                }
            }
        }
        f.delete();
    }
}


