/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.utils;

import com.ky.jacon.api.Model.Email;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kys
 */
public class SendMail {
    private final static int SMTP_PORT = 25;

    InetAddress mailHost;

    InetAddress localhost;

    BufferedReader in;

    PrintWriter out;
    
    public SendMail(String host) throws UnknownHostException {
      try {
        mailHost = InetAddress.getByName(host);
        localhost = InetAddress.getLocalHost();
        System.out.println("mailhost = " + mailHost);
        System.out.println("localhost= " + localhost);
        System.out.println("SMTP constructor done\n");
      } catch (java.net.UnknownHostException ex) {
        Logger.getLogger(SendMail.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    public boolean send(Email email) throws IOException {
      Socket smtpPipe;
      InputStream inn;
      OutputStream outt;
      smtpPipe = new Socket("example.com", SMTP_PORT);
      if (smtpPipe == null) {
        return false;
      }
      inn = smtpPipe.getInputStream();
      outt = smtpPipe.getOutputStream();
      in = new BufferedReader(new InputStreamReader(inn));
      out = new PrintWriter(new OutputStreamWriter(outt), true);
      if (inn == null || outt == null) {
        System.out.println("Failed to open streams to socket.");
        return false;
      }
      // String initialID = in.readLine();
      // System.out.println(initialID);
      System.out.println("HELO " + localhost.getHostName());
      out.println("HELO " + localhost.getHostName());
      // String welcome = in.readLine();
      // System.out.println(welcome);
      System.out.println("MAIL From:<" + email.getFrom()+ ">");
      out.println("MAIL From:<" + email.getFrom() + ">");
      String senderOK = in.readLine();
      System.out.println(senderOK);
      System.out.println("RCPT TO:<" + email.getTo() + ">");
      out.println("RCPT TO:<" + email.getTo() + ">");
      String recipientOK = in.readLine();
      System.out.println(recipientOK);
      System.out.println("DATA");
      out.println("DATA");
      out.println(email.getContent());
      System.out.println(".");
      out.println(".");
      String acceptedOK = in.readLine();
      System.out.println(acceptedOK);
      System.out.println("QUIT");
      out.println("QUIT");
      return true;
    }
}
