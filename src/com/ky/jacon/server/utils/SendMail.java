/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.utils;

import com.ky.jacon.api.Model.Email;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author kys
 */
public class SendMail {
    private final String SENDER_USERNAME = "kymailservices@gmail.com";
    private final String NO_REPLY_SENDER = "no-reply@gmail.com";
    private final String SENDER_PASSWORD = "kymail1234";
    
    private final String SMTP_HOST = "smtp.gmail.com";
    private final int SMTP_PORT = 465;
    
    private final int DELAY = 1;
    
    private DataOutputStream dos;
        
    public boolean proc(Email email) throws IOException {
      SSLSocket socket = (SSLSocket)((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(SMTP_HOST, SMTP_PORT);

      final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      (new Thread(() -> {
        try
        {
          String line;
          while((line = br.readLine()) != null)
            System.out.println("SERVER: " + line);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      })).start();  
      
      dos = new DataOutputStream(socket.getOutputStream());
      
      try {
        // String initialID = in.readLine();
        // System.out.println(initialID);
        send("EHLO " + SMTP_HOST + "\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("AUTH LOGIN\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send(DatatypeConverter.printBase64Binary(SENDER_USERNAME.getBytes()) + "\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send(DatatypeConverter.printBase64Binary(SENDER_PASSWORD.getBytes()) + "\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("MAIL From:<" + SENDER_USERNAME + ">\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("RCPT TO:<" + email.getTo() + ">\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("DATA\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("Subject: " + email.getSubject() + "\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send(email.getContent() + "\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send(".\r\n");
        TimeUnit.SECONDS.sleep(DELAY);
        send("QUIT\r\n");
        return true;
      } catch (Exception ex) {
        Logger.getLogger(SendMail.class.getName()).log(Level.SEVERE, null, ex);
        return false;
      }
      // String welcome = in.readLine();
      // System.out.println(welcome);
    }
    
    private void send(String s) throws Exception {
      dos.writeBytes(s);
      System.out.println("CLIENT: " + s);
    }
}
