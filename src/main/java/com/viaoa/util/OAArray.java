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

import java.lang.reflect.Array;
import java.util.Arrays;


public class OAArray {

    public static boolean contains(Object[] array, Object searchValue) {
    	if (array == null || array.length == 0) return false;
    	for (int i=0; i<array.length; i++) {
    		if (array[i] == searchValue) return true;
    		if (array[i] != null && array[i].equals(searchValue)) return true;
    	}
    	return false;
    }
    public static boolean containsExact(Object[] array, Object searchValue) {
        if (array == null || array.length == 0) return false;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return true;
        }
        return false;
    }
    public static boolean contains(int[] array, int searchValue) {
        if (array == null || array.length == 0) return false;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return true;
        }
        return false;
    }
    public static boolean contains(double[] array, double searchValue) {
        if (array == null || array.length == 0) return false;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return true;
        }
        return false;
    }
    public static boolean contains(String[] array, String searchValue, boolean bCaseSensitive) {
        if (array == null || array.length == 0) return false;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return true;
            if (array[i] != null) {
                if (array[i].equalsIgnoreCase(searchValue)) return true;
            }
        }
        return false;
    }

    
    
    
    public static int indexOf(Object[] array, Object searchValue) {
        if (array == null || array.length == 0) return -1;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return i;
            if (array[i] != null && array[i].equals(searchValue)) return i;
        }
        return -1;
    }
    
    public static int indexOf(Object[] array, Object searchValue, int startPos) {
        if (array == null || array.length == 0) return -1;
        if (startPos < 0 || startPos >= array.length) return -1;
        
        for (int i=startPos; i<array.length; i++) {
            if (array[i] == searchValue) return i;
            if (array[i] != null && array[i].equals(searchValue)) return i;
        }
        return -1;
    }
    
    
    public static int indexOf(int[] array, int searchValue) {
        if (array
                == null || array.length == 0) return -1;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return i;
        }
        return -1;
    }
    public static int indexOf(double[] array, double searchValue) {
        if (array == null || array.length == 0) return -1;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return i;
        }
        return -1;
    }
    public static int indexOf(String[] array, String searchValue, boolean bCaseSensitive) {
        if (array == null || array.length == 0) return -1;
        for (int i=0; i<array.length; i++) {
            if (array[i] == searchValue) return i;
            if (array[i] != null) {
                if (array[i].equalsIgnoreCase(searchValue)) return i;
            }
        }
        return -1;
    }
    
    
    /**
     * 
     * Example: 
     * String[] ss = null;
     * ss = (String[]) OAArray.add(String.class, ss, s); 
     * @param c
     * @param array
     * @param searchValue
     * @return
     */
	public static Object[] add(Class c, Object[] array, Object addValue) {
		int x = (array == null) ? 0 : array.length;
		Object[] newArray;
		if (array == null) {
	        newArray = (Object[]) Array.newInstance(c, 1);
		}
		else {
		    newArray = Arrays.copyOf(array, x+1);
		}
    	newArray[x] = addValue;
    	return newArray;
    }
    public static Object[] add(Class c, Object[] array, Object[] addValues) {
        if (addValues == null || addValues.length == 0) return array;
        int x = (array == null) ? 0 : array.length;
        int x2 = addValues.length;
        
        Object[] newArray;
        
        if (array == null) {
            newArray = (Object[]) Array.newInstance(c, x2);
        }
        else {
            newArray = Arrays.copyOf(array, x+x2);
        }
        for (int i=0; i<x2; i++) {
            newArray[x+i] = addValues[i];
        }
        return newArray;
    }
    public static int[] add(int[] array, int searchValue) {
        int x = (array == null) ? 0 : array.length;
        
        int[] newArray;
        if (array == null) {
            newArray = new int[1];
        }
        else {
            newArray = Arrays.copyOf(array, x+1);
        }
        newArray[x] = searchValue;
        return newArray;
    }
    public static boolean[] add(boolean[] array, boolean bAdd) {
        int x = (array == null) ? 0 : array.length;
        
        boolean[] newArray;
        if (array == null) {
            newArray = new boolean[1];
        }
        else {
            newArray = Arrays.copyOf(array, x+1);
        }
        newArray[x] = bAdd;
        return newArray;
    }
    public static double[] add(double[] array, double searchValue) {
        int x = (array == null) ? 0 : array.length;
        
        double[] newArray;
        if (array == null) {
            newArray = new double[1];
        }
        else {
            newArray = Arrays.copyOf(array, x+1);
        }
        newArray[x] = searchValue;
        return newArray;
    }

    public static String[] add(String[] array, String value) {
        int x = (array == null) ? 0 : array.length;
        
        String[] newArray;
        if (array == null) {
            newArray = new String[1];
        }
        else {
            newArray = Arrays.copyOf(array, x+1);
        }
        newArray[x] = value;
        return newArray;
    }
    
    
    /**
     * Removes the first searchValue found in an array - it does not search and try
     * to remove multiple copies of the same value.
     */
	public static Object[] removeValue(Class c, Object[] array, Object searchValue) {
    	if (array == null || array.length == 0) return array;
    	if (searchValue == null) return array;

    	int x = array.length;
    	int pos = -1;
    	for (int i=0; pos<0 && i<x; i++) {
            if (searchValue == array[i]) {
                pos = i;
                break;  // exact match
            }
    		if (searchValue.equals(array[i])) {
    		    pos = i;
    		}
    	}
    	if (pos<0) {
    	    return array;
    	}
        return removeAt(c, array, pos);
    }
    public static Object[] removeAt(final Class c, final Object[] array, final int pos) {
        if (array == null) return null;
        final int x = array.length;
        if (x == 0) return array;
        if (pos < 0 || pos >= x) return array;

        if (x == 1) {
            return (Object[]) Array.newInstance(c, 0);
        }
        
        if (pos == x-1) {
            // remove last element
            Object[] newArray = (Object[]) Arrays.copyOf(array, x-1);
            return newArray;
        }
        
        Object[] newArray = (Object[]) Array.newInstance(c, x-1);
        if (pos == 0) {
            System.arraycopy(array, 1, newArray, 0, x-1);
        }
        else {
            System.arraycopy(array, 0, newArray, 0, pos);
            System.arraycopy(array, pos+1, newArray, pos, (x-pos)-1);
        }
        return newArray;
    }


    public static Object[] insert(Class c, Object[] array, Object value, int atPos) {
        int x = (array == null) ? 0 : array.length;
        
        if (atPos >= x) {
            return add(c, array, value);
        }
        
        Object[] newArray = (Object[]) Array.newInstance(c, x+1);

        if (atPos == 0) {
            System.arraycopy(array, 0, newArray, 1, x);
        }
        else {
            System.arraycopy(array, 0, newArray, 0, atPos);
            System.arraycopy(array, atPos, newArray, atPos+1, x-atPos);
        }
        newArray[atPos] = value;
        return newArray;
    }
    
    
    
    public static int[] removeValue(int[] array, int searchValue) {
        if (array == null || array.length == 0) return array;

        int x = array.length;
        int pos = -1;
        for (int i=0; pos<0 && i<x; i++) {
            if (searchValue == array[i]) break;
        }
        if (pos<0) return array;
        return removeAt(array, pos);
    }
    public static int[] removeAt(int[] array, int pos) {
        if (array == null || array.length == 0) return array;
        if (pos < 0 || pos >= array.length) return array;

        int x = array.length;
        if (x == 1) {
            return new int[0];
        }
        
        if (pos == x-1) {
            // remove last element
            int[] newArray = (int[]) Arrays.copyOf(array, x-1);
            return newArray;
        }
        
        int[] newArray = new int[x-1];
        if (pos == 0) {
            System.arraycopy(array, 1, newArray, 0, x-1);
        }
        else {
            System.arraycopy(array, 0, newArray, 0, pos);
            System.arraycopy(array, pos+1, newArray, pos, (x-pos)-1);
        }
        return newArray;
    }

    public static double[] removeValue(double[] array, double searchValue) {
        if (array == null || array.length == 0) return array;

        int x = array.length;
        int pos = -1;
        for (int i=0; pos<0 && i<x; i++) {
            if (searchValue == array[i]) break;
        }
        if (pos<0) return array;
        return removeAt(array, pos);
    }
    public static double[] removeAt(double[] array, int pos) {
        if (array == null || array.length == 0) return array;
        if (pos < 0 || pos >= array.length) return array;

        int x = array.length;
        if (x == 1) {
            return new double[0];
        }
        
        if (pos == x-1) {
            // remove last element
            double[] newArray = (double[]) Arrays.copyOf(array, x-1);
            return newArray;
        }
        
        double[] newArray = new double[x-1];
        if (pos == 0) {
            System.arraycopy(array, 1, newArray, 0, x-1);
        }
        else {
            System.arraycopy(array, 0, newArray, 0, pos);
            System.arraycopy(array, pos+1, newArray, pos, (x-pos)-1);
        }
        return newArray;
    }
    
    /**
     * reorders one array to match a second, if possible.
     */
    public static void reorderToMatch(Object[] obja, Object[] objb) {
        if (obja == null) return;
        int x = obja.length;
        if (objb == null || objb.length != x) return;

        Object[] objNew = new Object[x];
        for (int i=0; i<x; i++) {
            boolean b = false;
            for (int j=0; j<x; j++) {
                if (obja[i].equals(objb[j])) {
                    b = true;
                    objNew[j] = obja[i];
                    break;
                }
            }
            if (!b) return;
        }
        for (int i=0; i<x; i++) {
            obja[i] = objNew[i];
        }
    }
}	
	
	

