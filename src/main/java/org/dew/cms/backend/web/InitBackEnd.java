package org.dew.cms.backend.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.sql.Connection;

import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

import org.dew.cms.backend.util.BEConfig;
import org.dew.cms.backend.util.ConnectionManager;

public
class InitBackEnd extends HttpServlet
{
  private static final long serialVersionUID = 8672911931992635071L;

  private final static String sLOGGER_CFG = "logger.cfg";
  
  private Properties oLoggerCfg;
  private String sCheckInit;
  private String sCheckConfig;
  private String sImportFolder;
  
  public
  void init(ServletConfig config)
      throws ServletException
  {
    super.init(config);
    
    try {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sLOGGER_CFG);
      oLoggerCfg = new Properties();
      oLoggerCfg.load(is);
      changeLogFilePath(oLoggerCfg);
      PropertyConfigurator.configure(oLoggerCfg);
    }
    catch (IOException ex) {
      sCheckInit = ex.toString();
      return;
    }
    
    String sCfgFileName = config.getInitParameter("nam.cfg");
    BEConfig.loadConfig(sCfgFileName);
    
    sImportFolder = BEConfig.getImportFolder();
    
    sCheckInit = "OK";
    
    if(BEConfig.isConfigFileLoaded()) {
      sCheckConfig = "OK (" + BEConfig.getResultLoading() + ")";
    }
    else {
      sCheckConfig = BEConfig.getResultLoading();
    }
  }
  
  protected
  void changeLogFilePath(Properties properties)
  {
    if(properties == null) return;
    
    String sUserHome = System.getProperty("user.home");
    String sLogFilePath = sUserHome + File.separator + "log" + File.separator;
    
    Iterator<Object> iterator = properties.keySet().iterator();
    while(iterator.hasNext()){
      String key = iterator.next().toString();
      String val = properties.getProperty(key);
      
      if(key.endsWith(".File") && val != null) {
        if(!val.startsWith(".") && !val.startsWith("/")) {
          properties.put(key, sLogFilePath + val);
        }
      }
    }
  }
  
  public
  void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    String sCheckConn = "OK";
    try {
      checkConnection();
    }
    catch(Exception ex) {
      sCheckConn = ex.toString();
      sCheckConn += "<br>";
      sCheckConn += "if server run on a developer environment set VM parameter: <i>-Dondebug=1</i>";
    }
    
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<body>");
    out.println("<b>Logger initialization: " + sCheckInit + "</b>");
    out.println("<br><br>");
    out.println("<b>BackEnd configuration: " + sCheckConfig + "</b>");
    out.println("<br><br>");
    out.println("<b>Check DBMS Connection: " + sCheckConn + "<b>");
    out.println("<br><br>");
    out.println("<b>Import folder: " + sImportFolder + "<b>");
    out.println("</body>");
    out.println("</html>");
  }
  
  public
  void checkConnection()
      throws Exception
  {
    Connection conn = ConnectionManager.getDefaultConnection();
    
    conn.close();
  }
}
