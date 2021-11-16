package org.dew.cms.backend.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.sql.DataSource;

import javax.transaction.UserTransaction;

public
class ConnectionManager
{
  public static String sJDBC_PATH = "jdbc/";
  
  public static final String sSYS_ONDEBUG   = "ondebug";
  public static final String sDEF_CONN_NAME = "db_cms";
  public static boolean boIsOnDebug = false;
  public static Boolean useSequece;
  
  public static String sJNDI_USER_TRANSACTION = "java:comp/UserTransaction";
  public static boolean boSimpleTransaction = true;
  
  static {
    String sOnDebug = System.getProperty(sSYS_ONDEBUG);
    boIsOnDebug = sOnDebug != null && !sOnDebug.equals("0");
    try {
      Context ctx = new InitialContext();
      sJNDI_USER_TRANSACTION = "java:comp/UserTransaction";
      boSimpleTransaction = ctx.lookup(sJNDI_USER_TRANSACTION) == null;
    }
    catch(Exception ex) {
      System.err.println("ctx.lookup(\"" + sJNDI_USER_TRANSACTION + "\") -> " + ex);
      try {
        Context ctx = new InitialContext();
        sJNDI_USER_TRANSACTION = "java:jboss/UserTransaction";
        boSimpleTransaction = ctx.lookup(sJNDI_USER_TRANSACTION) == null;
      }
      catch(Exception ex2) {
        boSimpleTransaction = true;
        System.err.println("ctx.lookup(\"" + sJNDI_USER_TRANSACTION + "\") -> " + ex2);
      }
    }
    if(boSimpleTransaction) {
      System.err.println("Use SimpleTransaction");
    }
    else {
      System.err.println("Use " + sJNDI_USER_TRANSACTION);
    }
  }
  
  public static
  Connection getDefaultConnection()
      throws Exception
  {
    return getConnection(sDEF_CONN_NAME);
  }
  
  public static
  Connection getConnection(String sName)
      throws Exception
  {
    if(boIsOnDebug) {
      return DebugDataSource.getConnection(sName);
    }
    Context ctx = new InitialContext();
    // Impostazione iniziale
    try {
      DataSource ds = (DataSource) ctx.lookup(sJDBC_PATH + sName);
      if(ds != null) return ds.getConnection();
    }
    catch(Exception ex) { ex.printStackTrace(); }
    // Altri possibili percorsi...
    sJDBC_PATH = "java:/";
    try {
      DataSource ds = (DataSource) ctx.lookup(sJDBC_PATH + sName);
      if(ds != null) return ds.getConnection();
    }
    catch(Exception ex) {}
    sJDBC_PATH = "java:/jdbc/";
    try {
      DataSource ds = (DataSource) ctx.lookup(sJDBC_PATH + sName);
      if(ds != null) return ds.getConnection();
    }
    catch(Exception ex) {}
    sJDBC_PATH = "java:/comp/env/jdbc/";
    try {
      DataSource ds = (DataSource) ctx.lookup(sJDBC_PATH + sName);
      if(ds != null) return ds.getConnection();
    }
    catch(Exception ex) {}
    sJDBC_PATH = "jdbc/";
    try {
      DataSource ds = (DataSource) ctx.lookup(sJDBC_PATH + sName);
      if(ds != null) return ds.getConnection();
    }
    catch(Exception ex) {}
    throw new Exception("DataSource " + sName + " not available.");
  }
  
  public static
  void closeConnection(Connection conn)
  {
    if(boIsOnDebug) {
      closeDebugConnection(conn);
      return;
    }
    
    try{
      conn.close();
    }
    catch(Exception ex) {
    }
  }
  
  public static
  void closeDebugConnection(Connection conn)
  {
    try{
      conn.commit();
      conn.close();
    }
    catch(Exception ex) {
    }
  }
  
  public static
  void close(AutoCloseable... arrayOfAutoCloseable)
  {
    if(arrayOfAutoCloseable == null || arrayOfAutoCloseable.length == 0) {
      return;
    }
    for(AutoCloseable autoCloseable : arrayOfAutoCloseable) {
      if(autoCloseable == null) continue;
      if(autoCloseable instanceof Connection) {
        closeConnection((Connection) autoCloseable);
        continue;
      }
      try {
        autoCloseable.close();
      }
      catch(Exception ignore) {
      }
    }
  }
  
  public static
  void rollback(UserTransaction ut)
  {
    if(ut == null) return;
    try {
      ut.rollback();
    }
    catch(Exception ignore) {
    }
  }
  
  public static
  UserTransaction getUserTransaction(Connection conn)
      throws Exception
  {
    if(boIsOnDebug || boSimpleTransaction) {
      return new SimpleUserTransaction(conn);
    }
    Context ctx = new InitialContext();
    return (UserTransaction) ctx.lookup(sJNDI_USER_TRANSACTION);
  }
  
  public static
  int nextVal(Connection conn, String sSequence)
      throws Exception
  {
    int iResult = 0;
    if(useSequence(conn)) {
      String sSQL = "SELECT " + sSequence + ".NEXTVAL FROM DUAL";
      Statement stm = null;
      ResultSet rs = null;
      try {
        stm = conn.createStatement();
        rs = stm.executeQuery(sSQL);
        if(rs.next()) {
          iResult = rs.getInt(1);
        }
      }
      finally {
        if(rs  != null) try{ rs.close();  } catch(Exception ex) {}
        if(stm != null) try{ stm.close(); } catch(Exception ex) {}
      }
    }
    else {
      String sSQL = "UPDATE CMS_PROGRESSIVI SET VALORE=VALORE+1 WHERE CODICE=?";
      PreparedStatement pstm = null;
      ResultSet rs = null;
      try {
        pstm = conn.prepareStatement(sSQL);
        pstm.setString(1, sSequence);
        int iRows = pstm.executeUpdate();
        pstm.close();
        
        if(iRows == 1) {
          sSQL = "SELECT VALORE FROM CMS_PROGRESSIVI WHERE CODICE=?";
          pstm = conn.prepareStatement(sSQL);
          pstm.setString(1, sSequence);
          rs = pstm.executeQuery();
          if(rs.next()) {
            iResult = rs.getInt(1);
          }
        }
        else {
          iResult = 1;
          sSQL = "INSERT INTO CMS_PROGRESSIVI(CODICE,VALORE) VALUES(?,?)";
          pstm = conn.prepareStatement(sSQL);
          pstm.setString(1, sSequence);
          pstm.setInt(2, iResult);
          pstm.executeUpdate();
        }
      }
      finally {
        if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
        if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      }
    }
    return iResult;
  }
  
  public static
  boolean useSequence()
  {
    if(useSequece != null) return useSequece.booleanValue();
    Connection conn = null;
    try {
      conn = getDefaultConnection();
      DatabaseMetaData bdbmd = conn.getMetaData();
      String sDatabaseProductName = bdbmd.getDatabaseProductName();
      if(sDatabaseProductName != null && sDatabaseProductName.toUpperCase().indexOf("ORACLE") >= 0) {
        useSequece = Boolean.TRUE;
      }
      else {
        useSequece = Boolean.FALSE;
      }
    }
    catch(Throwable ex) {
      return false;
    }
    finally {
      closeConnection(conn);
    }
    if(useSequece == null) return false;
    return useSequece.booleanValue();
  }
  
  public static
  boolean useSequence(Connection conn)
      throws Exception
  {
    if(useSequece != null) return useSequece.booleanValue();
    if(conn == null) return false;
    DatabaseMetaData bdbmd = conn.getMetaData();
    String sDatabaseProductName = bdbmd.getDatabaseProductName();
    if(sDatabaseProductName != null && sDatabaseProductName.toUpperCase().indexOf("ORACLE") >= 0) {
      useSequece = Boolean.TRUE;
    }
    else {
      useSequece = Boolean.FALSE;
    }
    return useSequece.booleanValue();
  }
}
