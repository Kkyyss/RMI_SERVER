/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.services;

import com.ky.jacon.api.Model.Email;
import com.ky.jacon.api.Model.Book;
import com.ky.jacon.api.Model.Issue;
import com.ky.jacon.api.Model.Role;
import com.ky.jacon.api.Model.Status;
import com.ky.jacon.api.Model.Student;
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
import java.util.UUID;
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
      String sql = "SELECT [user].*, [role].role_name"
              + " FROM [user] \n"
              + "INNER JOIN [role] ON [role].role_id = [user].role_id \n"
              + "WHERE username LIKE ? AND password=?";

      try (Connection conn = DbConn.getInstance().getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
         return getUserFromRs(rs);
        }
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      } 
      return null;
    }

  @Override
  public User addUser(User user) throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection()) {
          conn.setAutoCommit(false);
          String sql = "INSERT INTO [user](user_id,username,password,email)\n"
              + "VALUES\n"
              + "(?,?,?,?)";
          PreparedStatement pstmt = conn.prepareStatement(sql);      
        try {
          pstmt.setString(1, UUID.randomUUID().toString());
          pstmt.setString(2, user.getUsername());
          pstmt.setString(3, user.getPassword());
          pstmt.setString(4, user.getEmail());

          int affectedRows = pstmt.executeUpdate();
          
          conn.commit();
        } catch (SQLException ex) {
          Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
          conn.rollback();
        } finally {
          if (pstmt != null) {
            pstmt.close();
          }
        }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return user;
  }

  @Override
  public String deleteUser(String id) throws RemoteException {
    String sql = "DELETE FROM [user] WHERE user_id = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      conn.setAutoCommit(false);
      
      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      try {
        pstmt.setString(1, id);
        
        int rs = pstmt.executeUpdate();
        
        if (rs <= 0) return "No such user!";
        
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null)
          pstmt.close();
      }
    
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    return null;
  }

  @Override
  public List<User> getUsers() throws RemoteException {
    String sql = "SELECT [user].*, [role].role_name from [user] \n"
            + "INNER JOIN [role] ON [role].role_id = [user].role_id \n"
            + "WHERE [user].role_id <> 3";
    try (Connection conn = DbConn.getInstance().getConnection();
            Statement stmt = conn.createStatement()) {

      ResultSet rs = stmt.executeQuery(sql);
      List<User> users = new ArrayList<>();
      while (rs.next()) {
        users.add(getUserFromRs(rs));
      }
      return users;
  } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public boolean sendEmail(Email email) throws RemoteException {
    SendMail sm = new SendMail();
    if (sm != null) {
      try {
        return sm.proc(email);
      } catch (IOException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        return false;
      }
    }
    return false;
  }

  @Override
  public List<Book> getBooks() throws RemoteException {
    List<Book> books = new ArrayList<>();
    String sql = "SELECT * FROM [book]";
    
    try (Connection conn = DbConn.getInstance().getConnection();
            Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(sql);
      
      
      while (rs.next()) {
        books.add(getBookFromRs(rs));
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return books;
  }
  
  private Book getBookFromRs(ResultSet rs) {
    try {
      Book book = new Book();
      book.setBook_id(rs.getString("book_id"));
      book.setBook_author(rs.getString("book_author"));
      book.setBook_name(rs.getString("book_name"));
      book.setBook_quantity(rs.getInt("book_quantity"));
      book.setBook_isbn(rs.getString("book_isbn"));
      book.setBook_publisher(rs.getString("book_publisher"));
      book.setBook_subject(rs.getString("book_subject"));
      return book; 
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
  
  private Status getStatusFromRs(ResultSet rs) {
    try {
      Status status = new Status();
      status.setStatus_id(rs.getInt("status_id"));
      status.setStatus_name(rs.getString("status_name"));
      return status;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public List<Issue> getIssuesByUserId(String id) throws RemoteException {
      List<Issue> trs = new ArrayList<>();
    String sql = ""
            + "SELECT [issue].tr_id, [issue].tr_date, [issue].user_id, [issue].tr_returned_date, "
            + "[book].*, [status].*  FROM [issue]\n"
            + "INNER JOIN [book] ON [book].book_id = [issue].book_id\n"
            + "INNER JOIN [status] ON [status].status_id = [issue].status_id\n"
            + "WHERE [issue].user_id = ? AND [issue].status_id != 2";
    
    try (Connection conn = DbConn.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);) {
      
      pstmt.setString(1, id);
      
      ResultSet rs = pstmt.executeQuery();
      
      
      while (rs.next()) {
        Issue tr = new Issue();
        tr.setTr_id(rs.getString("tr_id"));
        tr.setTr_date(rs.getString("tr_date"));
        String returned_date = rs.getString("tr_returned_date");
        tr.setTr_returned_date(
                (returned_date == null || returned_date.isEmpty()) ?
                        "---" : returned_date);        
        tr.setTr_book(getBookFromRs(rs));
        tr.setTr_status(getStatusFromRs(rs));
        trs.add(tr);
      }
      return trs;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public List<Issue> getIssues() throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection();
            Statement stmt = conn.createStatement();) {
      List<Issue> trs = new ArrayList<>();
      String sql = ""
              + "SELECT [issue].tr_id, [issue].tr_date, [issue].tr_returned_date, "
              + "[book].*, [status].*, "
              + "[student].*, "
              + "[user].* FROM [issue]\n"
              + "INNER JOIN [book] ON [book].book_id = [issue].book_id\n"
              + "INNER JOIN [status] ON [status].status_id = [issue].status_id\n"
              + "INNER JOIN [student] ON [student].user_id = [issue].user_id\n"
              + "INNER JOIN [user] ON [user].user_id = [student].user_id";      
      ResultSet rs = stmt.executeQuery(sql);
      
      while (rs.next()) {
        Issue tr = new Issue();
        tr.setTr_id(rs.getString("tr_id"));
        tr.setTr_date(rs.getString("tr_date"));
        String returned_date = rs.getString("tr_returned_date");
        tr.setTr_student(getStudentFromRs(rs));
        tr.setTr_returned_date(
                (returned_date == null || returned_date.isEmpty()) ?
                        "---" : returned_date);
        tr.setTr_book(getBookFromRs(rs));
        tr.setTr_status(getStatusFromRs(rs));
        trs.add(tr);
      }
      return trs;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
  
  private Student getStudentFromRs(ResultSet rs) {
    try {
        Student student = new Student();
        student.setEmail(rs.getString("email"));
        student.setUser_id(rs.getString("user_id"));
        student.setStudent_id(rs.getString("student_id"));
        student.setStudent_intake(rs.getString("student_intake"));
        student.setStudent_name(rs.getString("student_name"));
        student.setStudent_no(rs.getString("student_no"));
        student.setStudent_ic(rs.getString("student_ic"));
        return student;
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public Book addBook(Book book) throws RemoteException {
    String sql = "SELECT 0 FROM [book] WHERE book_isbn = ?";    
    
    try {
      Connection conn = DbConn.getInstance().getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, book.getBook_isbn());
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        return null;
      }
      else {
        try {
          conn.setAutoCommit(false);
          sql = "INSERT INTO [book]("
                  + "book_id,book_name,book_author,"
                  + "book_quantity,book_subject,book_publisher,"
                  + "book_isbn)\n"
              + "VALUES\n"
              + "(?,?,?,?,?,?,?)";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, UUID.randomUUID().toString());
          pstmt.setString(2, book.getBook_name());
          pstmt.setString(3, book.getBook_author());
          pstmt.setInt(4, book.getBook_quantity());
          pstmt.setString(5, book.getBook_subject());
          pstmt.setString(6, book.getBook_publisher());
          pstmt.setString(7, book.getBook_isbn());

          int affectedRows = pstmt.executeUpdate();
          conn.commit();
          
          return book;
        } catch (SQLException ex) {
          Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
          conn.rollback();
        } finally {
          if (pstmt != null) {
            pstmt.close();
          }
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
  }

  @Override
  public String addIssue(Issue issue) throws RemoteException {
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      String sql = "SELECT 0 from [issue] \n"
              + "WHERE user_id = ? AND book_Id = ? AND status_id = 1";
      
      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      pstmt.setString(1, issue.getTr_student().getUser_id());
      pstmt.setString(2, issue.getTr_book().getBook_id());
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        return "This book already issued to the student!";
      }
      
      sql = "SELECT book_quantity from [book]\n"
              + "WHERE book_isbn = ?";
      
      pstmt = conn.prepareStatement(sql);
      
      pstmt.setString(1, issue.getTr_book().getBook_isbn());
      
      rs = pstmt.executeQuery();
      
      if (rs.next()) {
        if(rs.getInt("book_quantity") <= 0) {
          return "Out of stock!";
        }
      } else {
        return "Book not available!";
      }
      
      sql = "INSERT INTO [issue](tr_id,tr_date,user_id,book_id,status_id)\n"
            + "VALUES\n"
            + "(?,DATETIME('now', 'localtime'),?,?,1)";      

      pstmt = conn.prepareStatement(sql);      
      try {
        conn.setAutoCommit(false);
        pstmt.setString(1, UUID.randomUUID().toString());
        pstmt.setString(2, issue.getTr_student().getUser_id());
        pstmt.setString(3, issue.getTr_book().getBook_id());

        int result = pstmt.executeUpdate();
        System.out.println("Number of rows inserted: "+ result);
        
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }
      }
    } catch (Exception ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    
    return updateBookQuantity(-1, issue.getTr_book().getBook_isbn());
  }

  @Override
  public String updateBookQuantity(int num, String ISBN) throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection()) {
      String sql = "UPDATE [book]\n"
              + "SET book_quantity = book_quantity + ?\n"
              + "WHERE book_isbn = ?";

      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      try {
        conn.setAutoCommit(false);
        pstmt.setInt(1, num);
        pstmt.setString(2, ISBN);

        int rs = pstmt.executeUpdate();
        System.out.println("Number of rows updated: "+ rs);
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }        
      }

    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    return null;
  }

  @Override
  public String returnBook(Issue issue) throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection()) {
      String sql = "UPDATE [issue]\n"
              + "SET status_id = 2,\n"
              + "tr_returned_date = DATETIME('now', 'localtime') "
              + "WHERE user_id = ? AND book_id = ? AND status_id = 1";

      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      try {
        conn.setAutoCommit(false);
        pstmt.setString(1, issue.getTr_student().getUser_id());
        pstmt.setString(2, issue.getTr_book().getBook_id());

        int rs = pstmt.executeUpdate();
        System.out.println("Number of rows updated: "+ rs);
        if (rs == 0)
          return "No such issue/Issue already returned!";
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }        
      }

    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    return updateBookQuantity(1, issue.getTr_book().getBook_isbn());
  }

  @Override
  public Student getStudent(String id) throws RemoteException {
    String sql = "SELECT [student].student_id, "
            + "[student].student_no, [student].student_name, [student].student_intake, [student].student_ic, "
            + "[user].* "
            + "from [student]\n"
            + "INNER JOIN [user] ON [user].user_id = [student].user_id\n"
            + "WHERE [student].student_no LIKE ?";
    
    try (Connection conn = DbConn.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, id);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
          return getStudentFromRs(rs);
        }    
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
  }

  @Override
  public Book getBook(String id) throws RemoteException {
    String sql = "SELECT * from [book]\n"
            + "WHERE book_isbn = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, id);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
          return getBookFromRs(rs);
        }   
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }   
    return null;
  }

  @Override
  public Student addStudent(Student student) throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection()) {
        conn.setAutoCommit(false);
        String sql = "INSERT INTO [user](user_id,username,password,role_id,email)\n"
            + "VALUES\n"
            + "(?,?,?,3,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
      try {
        student.setUser_id(UUID.randomUUID().toString());
        pstmt.setString(1, student.getUser_id());
        pstmt.setString(2, student.getStudent_no());
        pstmt.setString(3, student.getStudent_ic());
        pstmt.setString(4, student.getEmail());

        int affectedRows = pstmt.executeUpdate();

        if (affectedRows <= 0)
          return null;

        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return addStudentProfile(student);  
  }

  @Override
  public User getUserByEmail(String email) throws RemoteException {
    String sql = "SELECT [user].*, [role].role_name from [user] \n"
            + "INNER JOIN [role] ON [role].role_id = [user].role_id \n"
            + "WHERE email = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, email);
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        return getUserFromRs(rs);
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
  }
  
  private User getUserFromRs(ResultSet rs) {
    try {
      User user = new User();
      user.setEmail(rs.getString("email"));
      user.setPassword(rs.getString("password"));
      user.setUsername(rs.getString("username"));
      user.setUser_id(rs.getString("user_id"));
      Role role = new Role();
      role.setRole_id(rs.getInt("role_id"));
      role.setRole_name(rs.getString("role_name"));
      user.setRole(role);
      return user;
    } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        return null;
    }
  }

  @Override
  public User getUserByUsername(String username) throws RemoteException {
    String sql = "SELECT [user].*, [role].role_name from [user] \n"
            + "INNER JOIN [role] ON [role].role_id = [user].role_id \n"
            + "WHERE username = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, username);
      
      ResultSet rs = pstmt.executeQuery();
      
      if (rs.next()) {
        return getUserFromRs(rs);
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;
  }

  @Override
  public Student addStudentProfile(Student student) throws RemoteException {
    try (Connection conn = DbConn.getInstance().getConnection()) {
        conn.setAutoCommit(false);
        String sql = "INSERT INTO [student](student_id,user_id,student_no,student_name,student_intake,student_ic)\n"
            + "VALUES\n"
            + "(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
      try {
        pstmt.setString(1, UUID.randomUUID().toString());
        pstmt.setString(2, student.getUser_id());
        pstmt.setString(3, student.getStudent_no());
        pstmt.setString(4, student.getStudent_name());
        pstmt.setString(5, student.getStudent_intake());
        pstmt.setString(6, student.getStudent_ic());

        int affectedRows = pstmt.executeUpdate();

        if (affectedRows <= 0)
          return null;

        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return student;
  }

  @Override
  public List<Student> getStudents() throws RemoteException {
    String sql = "SELECT [student].*, [user].email from [student] \n"
            + "INNER JOIN [user] ON [user].user_id = [student].user_id";
    try (Connection conn = DbConn.getInstance().getConnection();
            Statement stmt = conn.createStatement()) {

      ResultSet rs = stmt.executeQuery(sql);
      List<Student> students = new ArrayList<>();
      while (rs.next()) {
        students.add(getStudentFromRs(rs));
      }
      return students;
  } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }  
  }

  @Override
  public String saveUserPwd(User u) throws RemoteException {
    String sql = "UPDATE [user] \n"
            + "SET password = ? \n"
            + "WHERE user_id = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      conn.setAutoCommit(false);
      PreparedStatement pstmt = conn.prepareStatement(sql);

      
      try {
      pstmt.setString(1, u.getPassword());
      pstmt.setString(2, u.getUser_id());
        int rs = pstmt.executeUpdate();
        
        if (rs <= 0) return "No such user!";
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    return null;
  }

  @Override
  public String saveUserInfo(User u) throws RemoteException {
    String sql = "UPDATE [user] \n"
            + "SET username = ?, \n"
            + "email = ? \n"
            + "WHERE user_id = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection()) {
      conn.setAutoCommit(false);
       PreparedStatement pstmt = conn.prepareStatement(sql);
       
      try {
      pstmt.setString(1, u.getUsername());
      pstmt.setString(2, u.getEmail());
      pstmt.setString(3, u.getUser_id());        
        int rs = pstmt.executeUpdate();
        
        if (rs <= 0) return "No such user!";
        conn.commit();
      } catch (SQLException ex) {
        Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        conn.rollback();
      } finally {
        if (pstmt != null) {
          pstmt.close();
        }
      }
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return ex.toString();
    }
    return null;
  }

  @Override
  public Student getStudentProfile(String uid) throws RemoteException {
    String sql = "SELECT [student].student_id, "
            + "[student].student_no, [student].student_name, [student].student_intake, [student].student_ic, "
            + "[user].* "
            + "from [student]\n"
            + "INNER JOIN [user] ON [user].user_id = [student].user_id\n"
            + "WHERE [student].user_id = ?";
    
    try (Connection conn = DbConn.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, uid);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
          return getStudentFromRs(rs);
        }    
    } catch (SQLException ex) {
      Logger.getLogger(GlobalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
    return null;  
  }
}
