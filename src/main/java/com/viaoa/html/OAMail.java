/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA.

ViaOA MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.html;

import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;


public class OAMail implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param host
     * @param to
     * @param from
     * @param subject
     * @param text
     * @param contentType "text/html", "text/plain", "text/richtext", "text/css", "image/gif"
     */
    public void send(String host, String user, String pw, String to, String from, String subject, String text, String contentType) throws Exception {
        send(host,user, pw, new String[] {to}, null, from,subject,text, contentType);
    }

    /* 
        @param host smtp mail server
        @param "mail to" list of addresses to send message to
        @param "mail cc" list of addresses to send message to
        @param subject mail subject
        @parma text mail message
    */
    public void send(String host, String user, String pw, String[] to, String[] cc, String from, String subject, String text, String contentType) throws Exception {
        String msg = "";
        if (contentType == null || contentType.length() == 0) {
            // contentType = "text/html; charset=iso-8859-1";
            contentType = "text/html; charset=UTF-8";
        }
        Session session = Session.getInstance(System.getProperties(), null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        for (int i=0; to != null && i<to.length; i++) {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to[i]));
        }
        for (int i=0; cc != null && i<cc.length; i++) {
            message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc[i]));
        }
        
        // message.setHeader("X-Mailer", "OAMail");
        message.setSubject(subject);
        message.setText(text);
        if (contentType != null) message.setContent(text, contentType);
        
        Transport transport = session.getTransport("smtp");
        transport.connect(host,user,pw);
        transport.sendMessage(message,message.getAllRecipients());
        transport.close();
    }

    
// OLDer code --------------------------------------------------------
    
    public void send(String host, String to, String from, String subject, String text) {
        send(host,new String[] {to}, null, from,subject,text, null);
    }
    public void send(String host, String to, String from, String subject, String text, String fileName) {
        send(host,new String[] {to}, null, from,subject,text, new String[] { fileName } );
    }

    public void send(String host, String to, String from, String subject, String text, String[] fileNames) {
        send(host,new String[] {to}, null, from,subject,text, fileNames);
    }

    /* 
        @param host smtp mail server
        @param "mail to" list of addresses to send message to
        @param "mail cc" list of addresses to send message to
        @param subject mail subject
        @parma text mail message
    */
    public void send(String host, String[] to, String[] cc, String from, String subject, String text, String[] fileNames) {
        String msg = "";
        if (from == null) from = "";
               
        try {
	        // create some properties and get the default Session
	        Properties props = System.getProperties();
	        //props.put("mail.smtp.host", host);

            Session session = Session.getInstance(System.getProperties(),null);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            for (int i=0; to != null && i<to.length; i++) {
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to[i]));
            }
            for (int i=0; cc != null && i<cc.length; i++) {
                message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc[i]));
            }
            message.setSubject(subject);
            message.setSentDate(new java.util.Date());
            //message.setContent(text+host,"text/html; charset=iso-8859-1"); // HTML ??
            
            // create and fill the first message part

            MimeBodyPart mbp1 = new MimeBodyPart();
            //mbp1.setText(text);
            mbp1.setContent( text, "text/html; charset=iso-8859-1" );
            
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);


            if (fileNames != null) {
                for (int i=0; i<fileNames.length; i++) {
                    String filename = fileNames[i];
                    // attach the file to the message
   	                FileDataSource fds = new FileDataSource(filename);
                    MimeBodyPart mbp2 = new MimeBodyPart();
	                mbp2.setDataHandler(new DataHandler(fds));
	                mbp2.setFileName(fds.getName());
                    mp.addBodyPart(mbp2);    // add the Multipart to the message
                }
            }
            message.setContent(mp);

                    
            Transport.send(message);
        }
        catch(Exception e) {
            System.out.println("Exception "+e);            
        }
    }

    public static void mainX(String[] args) {
        OAMail m = new OAMail();
        // m.send("smtp-auth.no-ip.com", "vvia@viaoa.com", "info@viaoa.com", "huh", "who dat ...", new String[] { "c:\\v.html", "c:\\Insight.zip"} );
        try {
            m.send("smtp-auth.no-ip.com", "viaoa.com@noip-smtp", "vince7", "vvia@viaoa.com", "vvia@viaoa.com", "Test OAMail", "Message goes here", "" );
        }
        catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    public static void mainXXX(String[] args) throws Exception {
        OAMail m = new OAMail();
        m.send("mail.atl.bellsouth.net", "uId", "pw", "dkatz@norcron.com", "bigdaddie@microsoft.com", "huh", "who dat ...", null);
    }

    
 // qqqqqqqqqqqq this is for testing no-ip.com  - which I was not able to get to work :(    
    
    public static void main(String[] args) throws Exception {
        String msg = "";
        String contentType = "text/html; charset=UTF-8";
        
                
        try {
sendX("smtp-auth.no-ip.com", "cvia@viaoa.com", "vvia@viaoa.com", "subject" , "body");

            Session session = Session.getInstance(System.getProperties(), null);
            session.setDebug(true);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("vvia@viaoa.com"));
            
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse("vvia@viaoa.com"));
            message.addRecipients(Message.RecipientType.CC, InternetAddress.parse("vvia@viaoa.com"));
            
            //message.setHeader("X-Mailer", "OAMailer");
            message.setSubject("subject");
            message.setText("text", contentType);
            //message.setContent("text", contentType);


            Transport transport = session.getTransport("smtp");
            transport.connect("smtp-auth.no-ip.com", "viaoa.com@noip-smtp", "vince7");
            transport.sendMessage(message,message.getAllRecipients());
            transport.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    
    public static void sendX(String smtpServer, String to, String from, String subject, String body) throws Exception {
       Properties props = System.getProperties();
       // -- Attaching to default Session, or we could start a new one --
       props.put("mail.transport.protocol", "smtp");
       props.put("mail.smtp.host", smtpServer);
       
       props.put("mail.smtp.auth", true);
       
       props.put("mail.smtp.port", "3325");
       props.put("mail.smtp.user", "viaoa.com@noip-smtp");
       props.put("mail.smtp.password", "vince7");
       props.put("mail.user", "viaoa.com@noip-smtp");
       props.put("mail.password", "vince7");
       
       Authenticator auth= new Authenticator() {
           public PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication("viaoa.com@noip-smtp", "vince7");
           }
       };       
       
       
       
       Session session = Session.getDefaultInstance(props, null);
       session.setDebug(true);

// Try to fake out SMTPTransport.java and get working EHLO:
       String lh = InetAddress.getLocalHost().getHostName();
lh = "192.168.1.101";       
props.put("mail.smtp.localhost", lh);
       
       // -- Create a new message --
       Message msg = new MimeMessage(session);
       // -- Set the FROM and TO fields --
       msg.setFrom(new InternetAddress(from));
       msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
       // -- We could include CC recipients too --
       // if (cc != null)
       // msg.setRecipients(Message.RecipientType.CC
       // ,InternetAddress.parse(cc, false));
       // -- Set the subject and body text --
       msg.setSubject(subject);
       msg.setText(body);
       // -- Set some other header information --
       // msg.setHeader("X-Mailer", "LOTONtechEmail");
       // msg.setSentDate(new Date());
       // -- Send the message --
       
msg.saveChanges();

//Transport.send(msg, InternetAddress.parse(to));
URLName un = new URLName("smtp-auth.no-ip.com");
Transport transport = session.getTransport("smtp");//trid: un); error no provider
//transport.connect(smtpServer, "viaoa.com@noip-smtp", "vince7"); // host, user, password
transport.connect("viaoa.com@noip-smtp", "vince7"); // host, user, password

//transport.sendMessage(msg, msg.getAllRecipients());
//transport.close();

Transport.send(msg);



       System.out.println("Message sent OK.");
   }    

}





