/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 

package com.viaoa.util;

/**
*  Provides encoding of raw bytes to base64-encoded characters, and
*  decoding of base64 characters to raw bytes.  
*/
public class Base64 {

    /**
        Static method to encode a String using Base64.
        @param str string to encode
        @return null if str is null, otherwise base64 encoded String.
        @see #decode
    */
    static public String encode(String str) {
        if (str == null) return null;
        return new String( encode(str.getBytes()) );   
    }

    /**
        returns an array of base64-encoded characters to represent the
        passed data array.
            
        @param data the array of bytes to encode
        @return base64-coded character array.
    */
    static public char[] encode(byte[] data)
    {
        char[] out = new char[((data.length + 2) / 3) * 4];

        //
        // 3 bytes encode to 4 chars.  Output is always an even
        // multiple of 4 characters.
        //
        for (int i=0, index=0; i<data.length; i+=3, index+=4) {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & (int) data[i]);
            val <<= 8;
            if ((i+1) < data.length) {
                val |= (0xFF & (int) data[i+1]);
                trip = true;
            }
            val <<= 8;
            if ((i+2) < data.length) {
                val |= (0xFF & (int) data[i+2]);
                quad = true;
            }
            out[index+3] = alphabet[(quad? (val & 0x3F): 64)];
            val >>= 6;
            out[index+2] = alphabet[(trip? (val & 0x3F): 64)];
            val >>= 6;
            out[index+1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index+0] = alphabet[val & 0x3F];
        }
        return out;
    }

    /**
        Decodes a String that is encoded using Base64 encoding.
        @param s is the encoded String that will be decoded.
    */
    static public String decode(String s) {
        if (s == null) return null;
        char[] c = new char[s.length()];
        s.getChars(0, s.length(),c, 0);

        return new String( decode(c) );   
    }

    /**
    * Returns an array of bytes which were encoded in the passed
    * character array.
    *
    * @param data the array of base64-encoded characters
    * @return decoded data array
    */
    static public byte[] decode(char[] data)
    {
        int len = ((data.length + 3) / 4) * 3;
        if (data.length>0 && data[data.length-1] == '=') --len;
        if (data.length>1 && data[data.length-2] == '=') --len;
        byte[] out = new byte[len];

        int shift = 0;   // # of excess bits stored in accum
        int accum = 0;   // excess bits
        int index = 0;

        for (int ix=0; ix<data.length; ix++)
        {
            int value = codes[ data[ix] & 0xFF ];   // ignore high byte of char
            if ( value >= 0 ) {                     // skip over non-code
                accum <<= 6;            // bits shift up by 6 each time thru
                shift += 6;             // loop, with new bits being put in
                accum |= value;         // at the bottom.
                if ( shift >= 8 ) {     // whenever there are 8 or more shifted in,
                    shift -= 8;         // write them out (from the top, leaving any
                    out[index++] =      // excess at the bottom for next iteration.
                        (byte) ((accum >> shift) & 0xff);
        }   }   }
        if (index != out.length)
            throw new Error("miscalculated data length!");

        return out;
    }

    //
    // code characters for values 0..63
    //
    static private char[] alphabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
            .toCharArray();

    //
    // lookup table for converting base64 characters to value in range 0..63
    //
    static private byte[] codes = new byte[256];
    static {
        for (int i=0; i<256; i++) codes[i] = -1;
        for (int i = 'A'; i <= 'Z'; i++) codes[i] = (byte)(     i - 'A');
        for (int i = 'a'; i <= 'z'; i++) codes[i] = (byte)(26 + i - 'a');
        for (int i = '0'; i <= '9'; i++) codes[i] = (byte)(52 + i - '0');
        codes['+'] = 62;
        codes['/'] = 63;
    }


    public static void main(String[] args) {
        String[] ss = {"Vince","Anthony","Via"};
        for (int i=0; i<ss.length; i++) {
            String s = ss[i];
            String s2 = com.viaoa.util.Base64.encode(s);
            String s3 = com.viaoa.util.Base64.decode(s2);
            System.out.println(s + " -> " + s2 + " -> " + s3);
        }
        
    }

}
