/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.services;

import com.ky.jacon.api.Model.Email;
import com.ky.jacon.api.Model.User;
import com.ky.jacon.api.services.GlobalService;
import com.ky.jacon.server.utils.DbConn;
import com.ky.jacon.server.utils.SendMail;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kys
 */
public class GlobalServiceImpl extends UnicastRemoteObject implements GlobalService {

  public GlobalServiceImpl() throws RemoteException {
  }
  
    @Override
    public boolean login(User user) throws RemoteException {
      String sql = "SELECT 0 FROM user WHERE username=? AND password=?";

      try (Connection conn = DbConn.getInstance().getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      }
      return false;
    }

  @Override
  public User addUser(User user) throws RemoteException {
    String sql = "SELECT 0 FROM user WHERE username=? OR email =?";    
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getEmail());
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        user = null;
      }
      else {
        sql = "INSERT INTO user(username,password,email)\n"
            + "VALUES\n"
            + "(?,?,?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        
        pstmt.executeUpdate();
        rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
          user.setUser_id(rs.getInt(1));
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    return user;
  }

  @Override
  public void updateUser(User user) throws RemoteException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void deleteUser(int id) throws RemoteException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void getUser(int id) throws RemoteException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<User> getUsers() throws RemoteException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean sendEmail(Email email) throws RemoteException {
    SendMail sm = new SendMail("localhost");
    if (sm != null) {
      try {
        return sm.send(email);
      } catch (IOException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return false;
  }
}
