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

import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;


/*

    Sun:
    http://java.sun.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html    

    Sample programs:
    http://www.owasp.org/index.php/Digital_Signature_Implementation_in_Java
    http://www.rgagnon.com/javadetails/java-0400.html    

*/

public class OAEncryption {

    /**
     * Generates a SHA-256 hash code base64 string for a given input.
     * This is a one way function (irreversible).
     * 
     * Example: used when the real password is not stored.  Instead
     * the hash is stored and is used to compare the hash of user input.
     */
    public static String getHash(String input){
        MessageDigest md = null;
        
        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("No SHA-256");
        }
        try {
          md.update(input.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            System.out.println("Encoding error.");
        }
        
        byte raw[] = md.digest();
        String hash = new String(Base64.encode(raw));
        
        return hash;
    }

    
    /**
     * Encrypt bytes into a new byte array.
     * @see #decrypt(byte[])
     */
    public static byte[] encrypt(byte[] bs) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        
        bs = cipher.doFinal(bs);
        return bs;
    }
    public static byte[] encrypt(byte[] bs, String password) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        
        SecretKey key;
        if (OAString.isEmpty(password)) key = getSecretKey();
        else key = getSecretKey(password);
        
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        bs = cipher.doFinal(bs);
        return bs;
    }
    
    /**
     * Decrypt bytes into a new byte array.
     * @see #encrypt(byte[])
     */
    public static byte[] decrypt(byte[] bs) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        bs = cipher.doFinal(bs);
        return bs;
    }
    public static byte[] decrypt(byte[] bs, String password) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");

        SecretKey key;
        if (OAString.isEmpty(password)) key = getSecretKey();
        else key = getSecretKey(password);

        cipher.init(Cipher.DECRYPT_MODE, key);
        bs = cipher.doFinal(bs);
        return bs;
    }

    private static SecretKey _secretKey;
    /**
     * DES secret key used for encrypting data.
     */
    public static SecretKey getSecretKey() throws Exception {
        if (_secretKey == null) {
            byte[] bs = new byte[DESKeySpec.DES_KEY_LEN];
            for (int i=0; i<bs.length; i++) {
                bs[i] = (byte) i;
            }
            DESKeySpec desKeySpec = new DESKeySpec(bs);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            _secretKey = keyFactory.generateSecret(desKeySpec);
        }
        return _secretKey;
    }    
    public static SecretKey getSecretKey(String password) throws Exception {
        if (_secretKey == null) {
            byte[] bs = new byte[DESKeySpec.DES_KEY_LEN];
            
            int x = password == null ? 0 : password.length();
            
            for (int i=0; i<bs.length; i++) {
                if (i < x) bs[i] = (byte) password.charAt(i); 
                else bs[i] = (byte) i;
            }
            DESKeySpec desKeySpec = new DESKeySpec(bs);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            _secretKey = keyFactory.generateSecret(desKeySpec);
        }
        return _secretKey;
    }    
    
    
    /**
     * Encrypt a string to a Base64 string.
     */
    public static String encrypt(String input) throws Exception {
        byte[] bs = encrypt(input.getBytes());
        char[] cs = Base64.encode(bs);
        String s = new String(cs);
        return s;
    }
    public static String encrypt(String input, String password) throws Exception {
        byte[] bs = encrypt(input.getBytes(), password);
        char[] cs = Base64.encode(bs);
        String s = new String(cs);
        return s;
    }

    /**
     * Decrypt a base64 string to a string.
     */
    public static String decrypt(String inputBase64) throws Exception {
        char[] cs = new char[inputBase64.length()];
        inputBase64.getChars(0, inputBase64.length(), cs, 0);
        
        byte[] bs = Base64.decode(cs);
        
        bs = decrypt(bs);

        return new String(bs);
        
    }
    public static String decrypt(String inputBase64, String password) throws Exception {
        char[] cs = new char[inputBase64.length()];
        inputBase64.getChars(0, inputBase64.length(), cs, 0);
        
        byte[] bs = Base64.decode(cs);
        
        bs = decrypt(bs, password);

        return new String(bs);
        
    }

    
    public static void main(String[] args) throws Exception {
        String s = "";
        
        for (int i=0; args != null && i < args.length; i++) {
            s += args[i];
        }
        
        System.out.println("Original \""+s+"\"");
        
        String s2 = encrypt(s, "password");
        System.out.println("Encrypted ==> \""+s2+"\"");

        
        String s3 = decrypt(s2, "password");
        System.out.println("Decrypted ==> \""+s3+"\"");

        String s4 = getHash(s);
        System.out.println("Hashed ==> \""+s4+"\"");
    }
    
}

