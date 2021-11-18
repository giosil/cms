package org.dew.cms.web;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.dew.cms.CMS;
import org.dew.cms.User;
import org.dew.cms.util.BEConfig;
import org.dew.cms.util.ConnectionManager;

public
class WebMultimedia extends HttpServlet
{
  private static final long serialVersionUID = -8031976267202067984L;

  protected static Logger oLogger = Logger.getLogger(WebMultimedia.class);	
  
  public static int small_preview_width     =  150;
  public static int small_preview_height    =  114;
  public static int large_preview_width     =  164;
  public static int large_preview_height    =  108;
  public static int normalized_img_width    = 1000;
  public static int normalized_img_height   =  700;
  public static String default_img_id0      = "blank.jpg";
  public static String default_img_404      = "default.jpg";
  public static boolean create_preview_file = true;
  public static boolean check_user_logged   = false;
  
  public
  void init(ServletConfig config)
      throws ServletException
  {
    super.init(config);
    if(!BEConfig.isConfigFileLoaded()) {
      String sCfgFileName = config.getInitParameter("nam.cfg");
      BEConfig.loadConfig(sCfgFileName);
    }
    small_preview_width   = BEConfig.getInt("multimedia.small_preview_width",     small_preview_width);
    small_preview_height  = BEConfig.getInt("multimedia.small_preview_height",    small_preview_height);
    large_preview_width   = BEConfig.getInt("multimedia.large_preview_width",     large_preview_width);
    large_preview_height  = BEConfig.getInt("multimedia.large_preview_height",    large_preview_height);
    normalized_img_width  = BEConfig.getInt("multimedia.normalized_img_width",    normalized_img_width);
    normalized_img_height = BEConfig.getInt("multimedia.normalized_img_height",   normalized_img_height);
    default_img_id0       = BEConfig.getProperty("multimedia.default_img_id0",    default_img_id0);
    default_img_404       = BEConfig.getProperty("multimedia.default_img_404",    default_img_404);
    create_preview_file   = BEConfig.getBoolean("multimedia.create_preview_file", create_preview_file);
    check_user_logged     = BEConfig.getBoolean("multimedia.check_user_logged",   check_user_logged);
  }
  
  public
  void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    doGet(request, response);
  }
  
  public
  void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    String sPathInfo = request.getPathInfo();
    if(sPathInfo == null || sPathInfo.length() == 0) {
      response.sendError(404); // Not Found
      return;
    }
    if(sPathInfo.charAt(0) == '/') sPathInfo = sPathInfo.substring(1);
    int iBegin = sPathInfo.indexOf('/');
    if(iBegin >= 0) sPathInfo = sPathInfo.substring(0, iBegin);
    boolean boLargePreview = sPathInfo.startsWith("_");
    if(boLargePreview) sPathInfo = sPathInfo.substring(1);
    boolean boSmallPreview = sPathInfo.startsWith("_");
    if(boSmallPreview) sPathInfo = sPathInfo.substring(1);
    boolean boNormalized   = sPathInfo.startsWith("_");
    if(boNormalized) {
      sPathInfo = sPathInfo.substring(1);
      boSmallPreview = false;
      boLargePreview = false;
    }
    int iDot = sPathInfo.lastIndexOf('.');
    int iIdMultimedia = 0;
    if(iDot < 0) {
      try{ iIdMultimedia = Integer.parseInt(sPathInfo); } catch(Exception ex) {}
    }
    else {
      try{ iIdMultimedia = Integer.parseInt(sPathInfo.substring(0, iDot)); } catch(Exception ex) {}
    }
    File file = getFile(iIdMultimedia, request);
    if(file == null) {
      response.sendError(404); // Not Found
      return;
    }
    boLargePreview = boLargePreview && (large_preview_width  > 0 && large_preview_height  > 0);
    boSmallPreview = boSmallPreview && (small_preview_width  > 0 && small_preview_height  > 0);
    boNormalized   = boNormalized   && (normalized_img_width > 0 && normalized_img_height > 0);
    if(boLargePreview || boSmallPreview || boNormalized) {
      int iPrevWidth  = large_preview_width;
      int iPrevHeight = large_preview_height;
      if(boSmallPreview) {
        iPrevWidth  = small_preview_width;
        iPrevHeight = small_preview_height;
      }
      if(boNormalized) {
        iPrevWidth  = normalized_img_width;
        iPrevHeight = normalized_img_height;
      }		  
      String sExt = "";
      String sFilePath = file.getPath();
      iDot = sFilePath.lastIndexOf('.');
      if(iDot >= 0 && iDot < sFilePath.length() - 1) {
        sExt = sFilePath.substring(iDot + 1).toLowerCase();
      }
      if("jpg|jpeg|png|bmp|gif".indexOf(sExt) >= 0) {
        // Anteprima di una immagine: ridimensionamento
        String sParent   = file.getParent();
        String sFileName = file.getName();
        File filePreview = null;
        if(boSmallPreview) {
          filePreview = new File(sParent + File.separator + "__" + sFileName + ".jpg");
        }
        else 
          if(boLargePreview) {
            filePreview = new File(sParent + File.separator + "_" + sFileName + ".jpg");
          }
        if(filePreview != null && filePreview.exists()) {
          // Invio contenuto file preview
          String sContentType = getContentType(filePreview);
          response.setContentLength((int) filePreview.length());
          response.setContentType(sContentType);
          OutputStream out = response.getOutputStream();
          FileInputStream fis = new FileInputStream(filePreview);
          int iBytesReaded = 0;
          byte[] abBuffer = new byte[1024];
          while((iBytesReaded = fis.read(abBuffer)) > 0) {
            out.write(abBuffer, 0, iBytesReaded);
          }
          fis.close();
        }
        else {
          try {
            Image image = new ImageIcon(sFilePath).getImage();
            int iWidth  = image.getWidth(null);
            int iHeight = image.getHeight(null);
            if(iHeight > iPrevWidth && iHeight > iWidth) {
              // Immagine verticale
              int iH = (iWidth * iPrevHeight) / iPrevWidth;
              int iY = (iHeight - iH) / 2;
              Toolkit toolkit = Toolkit.getDefaultToolkit();
              image = toolkit.createImage(new FilteredImageSource(image.getSource(), new CropImageFilter(0, iY, iWidth, iH)));
            }
            Image imageScaled = null;
            if(boNormalized) {
              if(iWidth > iPrevWidth) {
                imageScaled = image.getScaledInstance(iPrevWidth, iPrevHeight, Image.SCALE_FAST);
              }
              else {
                // Invio contenuto file preview
                String sContentType = getContentType(file);
                response.setContentLength((int) file.length());
                response.setContentType(sContentType);
                OutputStream out = response.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                int iBytesReaded = 0;
                byte[] abBuffer = new byte[1024];
                while((iBytesReaded = fis.read(abBuffer)) > 0) {
                  out.write(abBuffer, 0, iBytesReaded);
                }
                fis.close();
                return;
              }
            }
            else {
              imageScaled = image.getScaledInstance(iPrevWidth, iPrevHeight, Image.SCALE_FAST);
            }
            FileOutputStream fos = null;
            if(filePreview != null && create_preview_file) fos = new FileOutputStream(filePreview);
            try {
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              
              BufferedImage bufferedImage = new BufferedImage(imageScaled.getWidth(null), imageScaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
              Graphics2D bGr = bufferedImage.createGraphics();
              bGr.drawImage(imageScaled, 0, 0, null);
              bGr.dispose();
              
              ImageIO.write(bufferedImage, "jpg", baos);
              
              byte[] arrayOfBytes = baos.toByteArray();
              response.setContentLength(arrayOfBytes.length);
              response.setContentType("image/jpeg");
              OutputStream out = response.getOutputStream();
              out.write(arrayOfBytes);
              if(fos != null) fos.write(arrayOfBytes);
            }
            finally {
              if(fos != null) try{ fos.close(); } catch(Exception ex) {}
            }
          }
          catch(Exception ex) {
            oLogger.error("Error in WebMultimedia.sendPreview(" + file.getPath() + ",response)", ex);
            response.sendError(404); // Not Found
          }
        }
      }
      else {
        // Anteprima di un altro contenuto multimediale
        String sParent   = file.getParent();
        String sFileName = file.getName();
        File filePreview = null;
        if(boSmallPreview) {
          filePreview = new File(sParent + File.separator + "__" + sFileName + ".jpg");
        }
        else {
          filePreview = new File(sParent + File.separator + "_" + sFileName + ".jpg");
        }
        if(filePreview != null && filePreview.exists()) {
          // Invio contenuto file preview
          String sContentType = getContentType(filePreview);
          response.setContentLength((int) filePreview.length());
          response.setContentType(sContentType);
          OutputStream out = response.getOutputStream();
          FileInputStream fis = new FileInputStream(filePreview);
          int iBytesReaded = 0;
          byte[] abBuffer = new byte[1024];
          while((iBytesReaded = fis.read(abBuffer)) > 0) {
            out.write(abBuffer, 0, iBytesReaded);
          }
          fis.close();
        }
        else {
          // Immagine fittizia
          try {
            BufferedImage bufferedImage = new BufferedImage(iPrevWidth, iPrevHeight, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] arrayOfBytes = baos.toByteArray();
            response.setContentLength(arrayOfBytes.length);
            response.setContentType("image/jpeg");
            OutputStream out = response.getOutputStream();
            out.write(arrayOfBytes);
          }
          catch(Exception ex) {
            oLogger.error("Error in WebMultimedia.sendPreview(" + file.getPath() + ", response)", ex);
            response.sendError(404); // Not Found
          }
        }
      }
      return;
    }
    else {
      // Invio contenuto file
      long lFileLength = file.length();
      int iFileLength  = 0;
      if(lFileLength > Integer.MAX_VALUE) {
        iFileLength = Integer.MAX_VALUE;
      }
      else {
        iFileLength = (int) lFileLength;
      }
      String sContentType = getContentType(file);
      response.setContentLength(iFileLength);
      response.setContentType(sContentType);
      OutputStream out = response.getOutputStream();
      FileInputStream fis = new FileInputStream(file);
      int iBytesReaded = 0;
      byte[] abBuffer = new byte[1024];
      while((iBytesReaded = fis.read(abBuffer)) > 0) {
        out.write(abBuffer, 0, iBytesReaded);
      }
      fis.close();
    }
  }
  
  private static
  File getFile(int iIdMultimedia, HttpServletRequest request)
  {
    File file = null;
    if(iIdMultimedia == 0 && default_img_id0 != null && default_img_id0.length() > 0) {
      String sDefFilePath = BEConfig.getImportFolder() + File.separator + default_img_id0;
      file = new File(sDefFilePath);
      if(!file.exists()) return null;
      return file;
    }
    if(iIdMultimedia < 0) {
      String sDefFilePath = BEConfig.getImportFolder() + File.separator + iIdMultimedia + ".jpg";
      file = new File(sDefFilePath);
      if(!file.exists()) {
        if(default_img_404 != null && default_img_404.length() > 0) {
          sDefFilePath = BEConfig.getImportFolder() + File.separator + default_img_404;
          file = new File(sDefFilePath);
          if(!file.exists()) return null;
          return file;
        }
      }
      return file;
    }
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      if(check_user_logged) {
        pstm = conn.prepareStatement("SELECT M.URL_FILE,A.ID_TIPO_UTENTE FROM NAM_ARTICOLI_MULT M,NAM_ARTICOLI A WHERE M.ID_ARTICOLO=A.ID_ARTICOLO AND M.ID_MULTIMEDIA=?");
      }
      else {
        pstm = conn.prepareStatement("SELECT URL_FILE FROM NAM_ARTICOLI_MULT WHERE ID_MULTIMEDIA=?");
      }
      pstm.setInt(1, iIdMultimedia);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sURLFile = rs.getString(1);
        
        if(check_user_logged) {
          int iIdTipoUtente = rs.getInt(2);
          if(iIdTipoUtente != 0) {
            HttpSession httpSession = request.getSession(false);
            Object oValue = httpSession != null ? httpSession.getAttribute(CMS.SESS_USER_LOGGED) : null;
            if(oValue instanceof User) {
              int iUserType = ((User) oValue).getType();
              if(iIdTipoUtente > iUserType) return null;
            }
            else {
              return null;
            }
          }
        }
        
        if(sURLFile == null || sURLFile.length() == 0) return null;
        if(sURLFile.startsWith("${user.home}/") || sURLFile.startsWith("${user.home}\\")) {
          String sUserHome = System.getProperty("user.home");
          sURLFile = sUserHome + File.separator + sURLFile.substring(13).replace('\\', File.separatorChar);
        }
        file = new File(sURLFile);
        if(!file.exists()) {
          if(default_img_404 != null && default_img_404.length() > 0) {
            String sDefFilePath = BEConfig.getImportFolder() + File.separator + default_img_404;
            file = new File(sDefFilePath);
            if(!file.exists()) return null;
            return file;
          }
        }
      }
    }
    catch(Exception ex) {
      oLogger.error("Error in WebMultimedia.getFile(" + iIdMultimedia + ")", ex);
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return file;
  }
  
  private static
  String getContentType(File file)
  {
    if(file == null) return "text/plain";
    String fileName = file.getName();
    String ext = "";
    int sep = fileName.lastIndexOf('.');
    if(sep >= 0 && sep < fileName.length() - 1) {
      ext = fileName.substring(sep + 1).toLowerCase();
    }
    if(ext.equals("txt"))        return "text/plain";
    else if(ext.equals("dat"))   return "text/plain";
    else if(ext.equals("csv"))   return "text/plain";
    else if(ext.equals("html"))  return "text/html";
    else if(ext.equals("htm"))   return "text/html";
    else if(ext.equals("xml"))   return "text/xml";
    else if(ext.equals("log"))   return "text/plain";
    else if(ext.equals("rtf"))   return "application/rtf";
    else if(ext.equals("doc"))   return "application/msword";
    else if(ext.equals("docx"))  return "application/msword";
    else if(ext.equals("xls"))   return "application/x-msexcel";
    else if(ext.equals("xlsx"))  return "application/x-msexcel";
    else if(ext.equals("pdf"))   return "application/pdf"; 
    else if(ext.equals("gif"))   return "image/gif";
    else if(ext.equals("bmp"))   return "image/bmp";
    else if(ext.equals("jpg"))   return "image/jpeg";
    else if(ext.equals("jpeg"))  return "image/jpeg";
    else if(ext.equals("tif"))   return "image/tiff";
    else if(ext.equals("tiff"))  return "image/tiff";
    else if(ext.equals("png"))   return "image/png";
    else if(ext.equals("mpg"))   return "video/mpeg";
    else if(ext.equals("mpeg"))  return "video/mpeg";
    else if(ext.equals("mpeg4")) return "video/mpeg";
    else if(ext.equals("mp4"))   return "video/mpeg";
    else if(ext.equals("flv"))   return "video/x-flv";
    else if(ext.equals("mp3"))   return "audio/mp3";
    else if(ext.equals("wav"))   return "audio/wav";
    else if(ext.equals("wma"))   return "audio/wma";
    else if(ext.equals("mov"))   return "video/quicktime";
    else if(ext.equals("tar"))   return "application/x-tar";
    else if(ext.equals("zip"))   return "application/x-zip-compressed";
    return "application/" + ext;
  }
}

