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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
    public static String getSHAHash(String input){
        return getHash(input);
    }
    public static String getHash(String input){
        if (input == null) return null;
        MessageDigest md = null;
        
        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("No SHA-256");
            return null;
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

    public static Cipher getCipher() throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        return cipher;
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

    // same that is used for "FileZilla Server.xml"
    public static String getMD5Hash(String input){
        MessageDigest md = null;
        
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException("No MD5 available");
        }
        
        try {
          md.update(input.getBytes(), 0, input.length());
        }
        catch(Exception e)
        {
            System.out.println("Encoding error.");
        }
        
        byte raw[] = md.digest();
        
        String hash = new BigInteger(1, raw).toString(16);
   
        if (hash.length() == 31) hash = "0" + hash;
        return hash;
    }
    
    
    public static void main(String[] args) throws Exception {
        String s = "";
        
        
        String pw = getHash("00001201");
        String pw2 = getMD5Hash("00001201");
        String pw3 = OAEncryption.encrypt("00001201");
        
        String smd = getMD5Hash("emp3364");
        
        smd = getMD5Hash("vince1");
       
        
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

