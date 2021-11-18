package org.dew.cms.util;

import java.io.InputStream;

import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Properties;

public
class DebugDataSource
{
  public static Properties config = new Properties();

  static {
    InputStream oIn = null;
    try {
      URL urlCfg = Thread.currentThread().getContextClassLoader().getResource("jdbc_debug.cfg");
      if(urlCfg != null) {
          oIn = urlCfg.openStream();
          config.load(oIn);
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    finally {
      try { oIn.close(); } catch (Exception oEx) {}
    }
    
    System.out.println("File jdbc_debug.cfg loaded.");
  }

  public static
  Connection getConnection(String sName)
    throws Exception
  {
    String sDriver = config.getProperty(sName + ".driver");
    Class.forName(sDriver);
    
    String sURL  = config.getProperty(sName + ".url");
    String sUser = config.getProperty(sName + ".user");
    String sPass = config.getProperty(sName + ".password");
    
    Connection conn = DriverManager.getConnection(sURL, sUser, sPass);
    conn.setAutoCommit(false);
    
    return conn;
  }
}
