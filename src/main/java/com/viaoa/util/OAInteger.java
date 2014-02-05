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

public class OAInteger {

    
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
