/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.services;

import com.ky.jacon.api.Model.Email;
import com.ky.jacon.api.Model.Food;
import com.ky.jacon.api.Model.Transaction;
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
import java.sql.Statement;
import java.util.ArrayList;
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
    public User login(User user) throws RemoteException {
      String sql = "SELECT * FROM [user] WHERE username=? AND password=?";

      try (Connection conn = DbConn.getInstance().getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
         user.setUser_id(rs.getInt("user_id"));
         return user;
        }
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      } 
      return null;
    }

  @Override
  public User addUser(User user) throws RemoteException {
    String sql = "SELECT 0 FROM [user] WHERE username=? OR email =?";    
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, user.getUsername());
      pstmt.setString(2, user.getEmail());
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        return null;
      }
      else {
        sql = "INSERT INTO [user](username,password,email)\n"
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
          return user;
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
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
        return false;
      }
    }
    return false;
  }

  @Override
  public List<Food> getFoods() throws RemoteException {
    String sql = "SELECT * FROM [food]";
    
    try (Connection conn = DbConn.getInstance().getConnection();
            Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(sql);
      
      List<Food> foods = new ArrayList<>();
      while (rs.next()) {
        Food food = new Food();
        food.setFood_id(rs.getInt("food_id"));
        food.setFood_name(rs.getString("food_name"));
        food.setFood_price(rs.getDouble("food_price"));
        food.setFood_styles(rs.getString("food_styles"));
        
        foods.add(food);
      }
      return foods;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public List<Transaction> getTransactionsByUserId(int id) throws RemoteException {
    String sql = "SELECT * FROM [transaction]\n"
            + "WHERE user_id = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);) {
      
      pstmt.setInt(1, id);
      
      ResultSet rs = pstmt.executeQuery();
      
      List<Transaction> trs = new ArrayList<>();
      
      while (rs.next()) {
        Transaction tr = new Transaction();
        tr.setTr_id(rs.getInt("tr_id"));
        tr.setTr_date(rs.getString("tr_date"));
        tr.setTr_total(rs.getString("tr_total"));
        tr.setTr_food(rs.getString("tr_food"));
        
        trs.add(tr);
      }
      return trs;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public Transaction addTransaction(Transaction transaction) throws RemoteException {
    String sql = "INSERT INTO [transaction](tr_date,user_id,tr_food,tr_total)\n"
            + "VALUES\n"
            + "(DATETIME('now', 'localtime'),?,?,?)";
    try (Connection conn = DbConn.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, transaction.getUser_id());
      pstmt.setString(2, transaction.getTr_food());
      pstmt.setString(3, transaction.getTr_total());
      
      pstmt.executeUpdate();
      ResultSet rs = pstmt.getGeneratedKeys();
      if (rs.next()) {
        transaction.setTr_id(rs.getInt(1));
        return transaction;
      }
      
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
  }
}
