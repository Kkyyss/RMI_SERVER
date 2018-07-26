/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author kys
 * 
 * Singleton implementation
 */

public class DbConn {
    private static DbConn instance;
    private Connection conn;
    private String url = "jdbc:sqlite:" + new File("src/com/ky/jacon/server/datastore.db").getAbsolutePath();

    private DbConn() throws SQLException {
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
            }
        } catch (SQLException ex) {
            System.out.println("Database Connection Failed : " + ex.getMessage());
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public static DbConn getInstance() throws SQLException {
        if (instance == null) {
            instance = new DbConn();
        } else if (instance.getConnection().isClosed()) {
            instance = new DbConn();
        }
        return instance;
    }
}
