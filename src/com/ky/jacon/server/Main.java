/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ky.jacon.server;

import com.ky.jacon.server.services.GlobalServiceImpl;
import com.ky.jacon.api.services.GlobalService;
import com.ky.jacon.server.utils.DbConn;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author kys
 */
public class Main extends Application { 

  @Override
  public void start(Stage primaryStage) throws Exception {
      try {
        DbConn.getInstance();
        
        Registry registry = LocateRegistry.createRegistry(3344);
          
        GlobalServiceImpl globalServiceImpl = new GlobalServiceImpl();

        // GlobalService globalService = (GlobalService) UnicastRemoteObject.exportObject(globalServiceImpl, 0);

        registry.rebind("JACON", globalServiceImpl);
        
        System.out.println("Server is running"); 
      } catch (RemoteException e) { 
        System.out.println("Server exception: " + e.toString()); 
      }   
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}
