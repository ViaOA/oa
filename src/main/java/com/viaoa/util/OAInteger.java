/*  Copyright 1999-2017 Vince Via vvia@viaoa.com
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

public class OAInteger {
    private int x;
    
    public OAInteger() {
    }
    public OAInteger(int x) {
        this.x = x;
    }

    public int get() {
        return x;
    }
    public void set(int x) {
        this.x = x;
    }
    
    
    public int add(int x) {
        this.x += x;
        return this.x;
    }
    public int add() {
        return this.add(1);
    }
    public int subtract(int x) {
        this.x -= x;
        return this.x;
    }
    public int subtract() {
        return this.subtract(1);
    }
    
    public static void viewBytes(byte i) {
        int[] ints = new int[1];
        ints[0] = i & 0xFF;
        ints = null;
    }

    public static void viewBytes(int i) {
        int[] ints = new int[4];
        ints[0] = (i >>> 24);
        ints[1] = (i >> 16) & 0xFF;
        ints[2] = (i >> 8) & 0xFF;
        ints[3] = i & 0xFF;
        ints = null;
    }

    public static void viewBytes(long i) {
        int[] ints = new int[8];
        ints[0] = (int) (i >>> 56);
        ints[1] = (int) (i >> 48) & 0xFF;
        ints[2] = (int) (i >> 40) & 0xFF;
        ints[3] = (int) (i >> 32) & 0xFF;
        ints[4] = (int) (i >> 24) & 0xFF;
        ints[5] = (int) (i >> 16) & 0xFF;
        ints[6] = (int) (i >> 8) & 0xFF;
        ints[7] = (int) (i & 0xFF);
        ints = null;
    }

}
