package org.dew.cms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import org.util.WUtil;

public
class BEConfig
{
  public static Properties config = new Properties();
  
  private static boolean boConfigFileLoaded = false;
  private static String sResultLoading = "OK";
  
  public static
  boolean loadConfig(String sCfgFileName)
  {
    if(sCfgFileName == null || sCfgFileName.length() == 0) {
      sCfgFileName = "cms.cfg";
    }
    String sUserHome = System.getProperty("user.home");
    String sPathFile = sUserHome + File.separator + "cfg" + File.separator + sCfgFileName;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(sPathFile);
      config = new Properties();
      config.load(fis);
      boConfigFileLoaded = true;
      sResultLoading = "File " + sPathFile + " loaded.";
    }
    catch(FileNotFoundException ex) {
      sResultLoading = "File " + sPathFile + " not found.";
      return false;
    }
    catch (IOException ioex) {
      sResultLoading = "IOException during load " + sPathFile + ": " + ioex;
      return false;
    }
    finally {
      if(fis != null) try{ fis.close(); } catch(Exception ex) {}
    }
    return true;
  }
  
  public static
  boolean isConfigFileLoaded()
  {
    return boConfigFileLoaded;
  }
  
  public static
  String getResultLoading()
  {
    return sResultLoading;
  }
  
  public static
  String getProperty(String sKey)
  {
    return config.getProperty(sKey);
  }
  
  public static
  String getProperty(String sKey, String sDefault)
  {
    return config.getProperty(sKey, sDefault);
  }
  
  public static
  int getInt(String sKey, int iDefault)
  {
    String value = config.getProperty(sKey);
    return WUtil.toInt(value, iDefault);
  }
  
  public static
  boolean getBoolean(String sKey, boolean boDefault)
  {
    String value = config.getProperty(sKey);
    return WUtil.toBoolean(value, boDefault);
  }
  
  public static
  String getArticleFolder(int iIdArticle)
  {
    String sArticlesFolder = config.getProperty("cms.articles", "cms" + File.separator + "articles");
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + sArticlesFolder + File.separator + iIdArticle;
  }
  
  public static
  String getArticleRelFolder(int iIdArticle)
  {
    String sArticlesFolder = config.getProperty("cms.articles", "cms" + File.separator + "articles");
    return "${user.home}/" + sArticlesFolder + File.separator + iIdArticle;
  }
  
  public static
  String getImportFolder()
  {
    String sImportFolder = config.getProperty("cms.import", "cms" + File.separator + "import");
    String sUserHome = System.getProperty("user.home");
    String sMultFold = sUserHome + File.separator + sImportFolder;
    try {
      File folder = new File(sMultFold);
      if(!folder.exists()) folder.mkdirs();
    }
    catch(Exception ex) {
      return sUserHome;
    }
    return sMultFold;
  }
}
