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

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 * This is used to search for a file from a specific directory and all files below it.
 * If the file is an archive file (zip|jar|war|ear), then it will search within the archive file.
 * Supports regular expressions for filename.  
 * @author vvia
 */
public class FindFile {
	static final String[] ZIP_EXTENSIONS = { ".zip", ".jar", ".war", ".ear" };

	private String strPattern;
	private Pattern pattern; // regexp
	private String findFileName;
	private ArrayList<String> list;

	public String[] findAll(String rootFile, String findFileName) throws IOException {
		if (rootFile == null || rootFile.trim().length() == 0) rootFile = ".";
		return findAll(new File(rootFile), findFileName);
	}
	public String[] findAll(File rootFile, String findFileName) throws IOException {
		if (findFileName == null || findFileName.trim().length() == 0) return new String[0];
		this.findFileName = findFileName;
		
findFileName = "OA[a-zA-Z0-9.]{6}\\.class"; // works		
findFileName = "OA[^$]{12,}\\.class";  // works		
findFileName = "OA.*\\.class";  // works		
		this.pattern = Pattern.compile(findFileName);
		
Matcher m = pattern.matcher("toolbar.html");
boolean b = m.matches();
		
		this.list = new ArrayList<String>();
		findFile(rootFile);
		String[] ss = new String[list.size()];
		this.list.toArray(ss);
		this.list = null;
		String s = "";
		return ss;
	}
	
	protected boolean doesMatch(String fileName) {
if (fileName.indexOf("toolbar") >= 0) {
	int xxx = 5;
}
		Matcher m = pattern.matcher(fileName);
		boolean b = m.matches();
		return b;
	}
	
	protected void findFile(File file) throws IOException {
		if (file.isDirectory()) {
			//System.out.println("Directory= "+file);			
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				findFile(files[i]);
			}
		} 
		else {
			// if ((cnt%100) == 0) System.out.println("Status: " + cnt+") "+file.getAbsolutePath());			
			String fileName = file.getName();
			if (doesMatch(fileName)) {
				list.add(file.getAbsolutePath());
				//System.out.println("Found #"+(list.size()) + " = " + file);		
			}
			else {
				for (int i = 0; i < ZIP_EXTENSIONS.length; i++) {
					if (fileName.toLowerCase().endsWith(ZIP_EXTENSIONS[i])) {
						//System.out.println("Compressed= "+file.getAbsolutePath());			
						findZip(file);
						break;
					}
				}
			}
		}
	}

	protected void findZip(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		ZipInputStream zin = new ZipInputStream(in);

		ZipEntry en;
		while ((en = zin.getNextEntry()) != null) {
			if (en.isDirectory()) continue;
			String fn = en.getName();
			int pos = fn.lastIndexOf('/');
			if (pos < 0) pos = fn.lastIndexOf('\\');
			if (pos >= 0) fn = fn.substring(pos+1);
			if (doesMatch(fn)) {
				String s = file.getAbsolutePath() + "!" + en.getName();
				list.add(s);
				// System.out.println("Found #"+(list.size()) + " = " + file + "!" + en);		
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			System.out.println("Usage: FindFile [fromDirectory|File] (SearchFileName|regexp)");
		}
		else {
			String s1;
			String s2;
			if (args.length == 1) {
				s1 = ".";
				s2 = args[0];
			}
			else {
				s1 = args[0];
				s2 = args[1];
			}

s2 = "tool*";
			
			FindFile ff = new FindFile();
			String[] fileNames = ff.findAll(s1, s2);
			
			for (int i=0; fileNames != null && i < fileNames.length; i++) {
				System.out.println((i+1)+") " + fileNames[i]);
			}
			System.out.println("FindFile done for " + s2);
		}
	}
	
}

