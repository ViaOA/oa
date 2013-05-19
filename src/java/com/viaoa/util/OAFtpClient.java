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

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
    connect()
    login(user, pw)
    setBinary()
    setAscii()
    
    get
    put
    append
*/
public class OAFtpClient {

    private Socket csock = null; // controll socket
    private Socket dsock = null; // data socket
    private BufferedReader reader; //qqqq dcis;
    private PrintWriter writer; //qqq  pos;

    public boolean connect(String server) throws IOException {
        csock = new Socket(server, 21);
        reader =  new BufferedReader(new InputStreamReader(csock.getInputStream()));
        writer = new PrintWriter(csock.getOutputStream(), true); // set auto flush true.
        return getResponse(null).substring(0,3).equals("220");
    }

    public void login(String user, String pass) throws IOException {
        ftpSendCmd("USER "+user);
        ftpSendCmd("PASS "+pass);
    }

    public void quit() { // logout, close streams
        try { 
            if (writer != null) {
                writer.print("BYE" + "\r\n" );
                writer.flush();
                writer.close();
            }
            if (reader != null)  reader.close();
            if (csock != null) csock.close();
        } 
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setBinary() throws IOException {
        ftpSendCmd("TYPE I");
    }
    public void setAscii() throws IOException {
        ftpSendCmd("TYPE A");
    }

    public String getFileAsString(String dir, String file) throws IOException {
        ftpSetDir(dir);
        dsock = ftpGetDataSock();
        InputStream is = dsock.getInputStream();
        ftpSendCmd("RETR "+file);
        String contents = getAsString(is);
        is.close();
        dsock.close();
        return contents;
    }

    public String getDirectory(String dir) throws IOException {
        ftpSetDir(dir);
        dsock = ftpGetDataSock();
        InputStream is = dsock.getInputStream();
        ftpSendCmd("LIST");
        String contents = getAsString(is);
        is.close();
        dsock.close();
        return contents;
    }
    
    
    public void append(String dir, String file, String what) throws IOException {
        ftpSetDir(dir);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("APPE "+file); 
        dos.writeBytes(what);
        dos.flush();
        dos.close();
        dsock.close();
    }

    public void put(String dir, String fileName, File file) throws IOException {
        ftpSetDir(dir);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("STOR "+fileName);
    
        InputStream is = new FileInputStream(file);
        int bufferSize = 1024 * 2;
        byte[] bs = new byte[bufferSize];
        for (int i=0; ;i++) {
            int x = is.read(bs, 0, bufferSize);
            if (x < 0) break;
            dos.write(bs, 0, x);
        }
        is.close();
        
        dos.flush();
        dos.close();
        dsock.close();
    }

    public void put(String dir, String file, String what) throws IOException {
        ftpSetDir(dir);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("STOR "+file);
        dos.writeBytes(what);    
        dos.flush();
        dos.close();
        dsock.close();
    }


    
    ///////////////// private fields ////////////////////
    private boolean pauser = false;  // it's a hack. We're going to 
          // stall (refuse further requests) till we get a reply back 
          // from server for the current request.

    private String getAsString(InputStream is) {
        int c=0;
        char lineBuffer[]=new char[128], buf[]=lineBuffer;
        int room= buf.length, offset=0;
        try {
          loop: while (true) {
              // read chars into a buffer which grows as needed
                  switch (c = is.read() ) {
                      case -1: break loop;

                      default: if (--room < 0) {
                                   buf = new char[offset + 128];
                                   room = buf.length - offset - 1;
                                   System.arraycopy(lineBuffer, 0, 
                                            buf, 0, offset);
                                   lineBuffer = buf;
                               }
                               buf[offset++] = (char) c;
                               break;
                  }
          }
        } catch(IOException ioe) {ioe.printStackTrace();}
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }


    private void ftpSetDir(String dir)
        throws IOException { 
        // cwd to dir
        ftpSendCmd("CWD "+dir);
    }


    private Socket ftpGetDataSock()
        throws IOException {
        // Go to PASV mode, capture server reply, parse for socket setup
        // V2.1: generalized port parsing, allows more server variations
        String reply = ftpSendCmd("PASV");

        // New technique: just find numbers before and after ","!
        StringTokenizer st = new StringTokenizer(reply, ",");
        String[] parts = new String[6]; // parts, incl. some garbage
        int i = 0; // put tokens into String array
        while(st.hasMoreElements()) {
            // stick pieces of host, port in String array
            try {
                parts[i] = st.nextToken();
                i++;
            } catch(NoSuchElementException nope){nope.printStackTrace();}
        } // end getting parts of host, port

        // Get rid of everything before first "," except digits
        String[] possNum = new String[3];
        for(int j = 0; j < 3; j++) {
            // Get 3 characters, inverse order, check if digit/character
            possNum[j] = parts[0].substring(parts[0].length() - (j + 1),
                parts[0].length() - j); // next: digit or character?
            if(!Character.isDigit(possNum[j].charAt(0)))
                possNum[j] = "";
        }
        parts[0] = possNum[2] + possNum[1] + possNum[0];
        // Get only the digits after the last ","
        String[] porties = new String[3];
        for(int k = 0; k < 3; k++) {
            // Get 3 characters, in order, check if digit/character
            // May be less than 3 characters
            if((k + 1) <= parts[5].length())
                porties[k] = parts[5].substring(k, k + 1);
            else porties[k] = "FOOBAR"; // definitely not a digit!
            // next: digit or character?
            if(!Character.isDigit(porties[k].charAt(0)))
                    porties[k] = "";
        } // Have to do this one in order, not inverse order
        parts[5] = porties[0] + porties[1] + porties[2];
        // Get dotted quad IP number first
        String ip = parts[0]+"."+parts[1]+"."+parts[2]+"."+parts[3];

        // Determine port
        int port = -1;
        try { // Get first part of port, shift by 8 bits.
            int big = Integer.parseInt(parts[4]) << 8;
            int small = Integer.parseInt(parts[5]);
            port = big + small; // port number
        } catch(NumberFormatException nfe) {nfe.printStackTrace();}
        if((ip != null) && (port != -1))

            dsock = new Socket(ip, port);
        else throw new IOException();
        return dsock;
    }

    private String ftpSendCmd(String cmd)
        throws IOException
    { // This sends a dialog string to the server, returns reply
      // V2.0 Updated to parse multi-string responses a la RFC 959
      // Prints out only last response string of the lot.
        if (pauser) // i.e. we already issued a request, and are
                  // waiting for server to reply to it.  
            {
                if (reader != null)
                {
                    String discard = reader.readLine(); // will block here
                    // preventing this further client request until server
                    // responds to the already outstanding one.
                    pauser = false;
                }
            }
        writer.print(cmd + "\r\n" );
        writer.flush(); 
        String response = getResponse(cmd);
        return response;
    }

     // new method to read multi-line responses
     // responseHandler: takes a String command or null and returns
     // just the last line of a possibly multi-line response
     private String getResponse(String cmd) 
         throws IOException
     { // handle more than one line returned
        String reply = this.responseParser(reader.readLine());
        String numerals = reply.substring(0, 3);
        String hyph_test = reply.substring(3, 4);
        String next = null;
        if(hyph_test.equals("-")) {
            // Create "tester", marks end of multi-line output
            String tester = numerals + " ";
            boolean done = false;
            while(!done) { // read lines til finds last line
                next = reader.readLine();
                // Read "over" blank line responses
                while (next.equals("") || next.equals("  ")) {
                    next = reader.readLine();
                }

                // If next starts with "tester", we're done
               if(next.substring(0,4).equals(tester))
                   done = true;
            }
            return next;

        } else // "if (hyph_test.equals("-")) not true"
            return reply;
    }

    // responseParser: check first digit of first line of response
    // and take action based on it; set up to read an extra line
    // if the response starts with "1"
    private String responseParser(String resp)
        throws IOException
    { // Check first digit of resp, take appropriate action.
        String digit1 = resp.substring(0, 1);
        if(digit1.equals("1")) {
            // server to act, then give response
            // set pauser
            pauser = true;
            return resp;
        }
        else if(digit1.equals("2")) { // do usual handling
            // reset pauser
            pauser = false;
            return resp;
        }
        else if(digit1.equals("3") || digit1.equals("4")
            || digit1.equals("5")) { // do usual handling
            return resp;
        }
        else { // not covered, so return null
            return null;
        }
    }

/****

    public static void main(String[] args) {
        try {
            FTPClient ftp = new FTPClient();
            ftp.connect("ftp.vetplan.net");
            ftp.login("vince", "via83");
            // String s = ftp.get("", "test");
            // System.out.println("==> "+s);
            // ftp.put("", "xx", "Vince Via test");
            ftp.put("", "OAFtp.java", new File("util\\OAFtp.java"));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: "+e);
        }
    }
*****/
}


/**
Peter van der Linden's Linlyn applet
http://www.afu.com/t.java
*/

//////////////////////////////////////////
//  At last!  Java code to read/write files on the server from an applet!
//  This is the famous Linlyn code.
//
//  Use:  
//    compile this file, and have your applet call it as below.
//
//    to upload a file:  
//          Linlyn ftp = new Linlyn( <servername>, <user>, <password> );
//          ftp.upload( <directory>, <filename>, <contents of file> );
//
//    to download a file:  
//          Linlyn ftp = new Linlyn( <servername>, <user>, <password> );
//          String contents = ftp.download( <directory>, <filename> );
//
//          the default is ASCII transfer, an overloaded method does bin.
//
//    All parameters and return values are Strings. E.g.
//          Linlyn ftp = new Linlyn( "rtfm.mit.edu", "anonymous", "linden@" );
//          String contents = ftp.download( 
//                        "/pub/usenet-by-group/comp.lang.java.programmer"
//                        "Java_Programmers_FAQ" );
//
//          [the actual values above are not generally valid, substitute
//           your own server for your first attempt, see note 1.]
//
//    Notes:
//      1.  Usual applet security rules apply: you can only get a file
//          from the server that served the applet.
//      2.  The applet server must also be an FTP server.  This is NOT true
//          for some ISPs, such as best.com.  They have separate FTP and
//          http machines.  This code may work on such a setup if you put
//          the classfiles into the ftp area, and in the HTML file say:
//            <applet  codebase="ftp:///home/linden/ftp"  code="t.class" 
//      3.  This code does not break Java security.
//          It uses FTP to transfer files.  If the author of the applet
//          has FTP disabled you are out of luck.
//          It breaks regular system security however, as it publishes
//          (effectively) your ftp password.  Only use on an Intranet and
//          with authorization.
//      4.  Compiling this causes some deprecation warnings.   We wanted to 
//          stick with code that would work in JDK 1.0 browsers.
//      5.  Each upload or download creates, uses, and terminates a new
//          ftp session.  This is intended for low volume transfer, such
//          as the ever popular high-score files.
//      6.  Look at the source for the methods for binary transfers.
//
//      7.  On the Windows platform (particularly an NT ftp server
//          accessed through the IE5 browser) we have noticed a Microsoft
//          bug.  Sometimes the file methods: STOR, DELE, APPE, etc. "stall".
//          The file operation starts, but for some reason never completes.
//
//          The workaround is to nudge the buggy Microsoft server by sending
//          a "NOOP" command after each file operation, e.g.
//              ...
//              ftpSendCmd("STOR "+file);
//              ftpSendCmd("NOOP");
//              ...
//          Your mileage may vary, just passing on a tip.
//
//      8.  FTP is specified in RFC 959 and 1123.
//          It is based on the telnet protocol which is RFC 854.
//          There are more FTP RFC's such as 1579, 1635, 1639 and 2228,
//          if you are interested in FTP development.
//          You can find RFCs online in many places, including
//                  http://www.ietf.org
//                  http://www.faqs.org/rfcs/rfc959.html
//                  ftp://ftp.isi.edu/in-notes/rfc959.txt
//
//    Version 1.0   May 6 1998.
//    Version 3.1   Jan 23 2000  -- submit and finish one request at a time.
//                                  Stall further requests, and swallow 
//                                  multiple responses from server
//    
//    Authors:
//          Robert Lynch
//          Peter van der Linden  (Author of "Just Java" book).
//   

