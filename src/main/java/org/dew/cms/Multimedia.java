package org.dew.cms;

import org.dew.cms.common.IArticolo;
import org.dew.cms.ws.WSArticoli;
import org.util.WMap;
import org.util.WUtil;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Multimedia bean. 
 */
public 
class Multimedia implements Serializable
{
  private static final long serialVersionUID = 8985262586289291030L;

  private int id;
  private String type;
  private String title;
  private String uRL;
  private String uRLNormalizedPreview;
  private String uRLPreview;
  private String uRLSmallPreview;
  private String link;
  private String filePath;
  private int page;
  private int pages;
  private int count;
  private int idArticle;
  
  public static final int iTYPE_VIDEO = 1;
  public static final int iTYPE_PHOTO = 2;
  public static final int iTYPE_AUDIO = 3;
  public static final int iTYPE_DOCUM = 4;
  
  public static String sURL_MULTIMEDIA = "multimedia";
  
  public Multimedia()
  {
  }
  
  public Multimedia(int id)
  {
    this.id = id;
  }
  
  public Multimedia(Map<String, Object> map, int idLang)
  {
    if(map == null || map.isEmpty()) return;
    
    String lang = String.valueOf(idLang);
    
    WMap wmap = new WMap(map);
    
    id        = wmap.getInt(IArticolo.sID_MULTIMEDIA);
    idArticle = wmap.getInt(IArticolo.sID);
    filePath  = wmap.getString(IArticolo.sURL_FILE);
    type      = Article.getExtension(filePath);
    page      = wmap.getInt(IArticolo.sPAGINA);
    pages     = wmap.getInt(IArticolo.sPAGINE);
    count     = wmap.getInt(IArticolo.sCOUNT);
    
    Map<String, Object> mapDescrizioni = wmap.getMapObject(IArticolo.sDESCRIZIONE);
    if(mapDescrizioni != null) {
      title = WUtil.toString(mapDescrizioni.get(lang), null);
      if(title == null || title.length() == 0) {
        title = WUtil.toString(mapDescrizioni.get("0"), null);
      }
    }
    if(title != null && title.length() > 0) {
      if(title.indexOf("://") > 0) {
        link = title;
      }
      int iSep = title.indexOf('|');
      if(iSep >= 0 && iSep < title.length() - 1) {
        link = title.substring(iSep + 1);
      }
    }
    if(type != null && type.length() > 0) {
      uRL = sURL_MULTIMEDIA + "/" + id + "." + type;
    }
    else {
      uRL = sURL_MULTIMEDIA + "/" + id;
    }
  }
  
  public static void setURLMultimedia(String sURLMultimedia) {
    sURL_MULTIMEDIA = sURLMultimedia;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getURL() {
    if(uRL == null) {
      if(type != null && type.length() > 0) {
        return sURL_MULTIMEDIA + "/" + id + "." + type;
      }
      else {
        return sURL_MULTIMEDIA + "/" + id;
      }
    }
    return uRL;
  }
  
  public void setURL(String uRL) {
    this.uRL = uRL;
  }
  
  public String getURLNormalizedPreview() {
    if(uRLNormalizedPreview == null) return sURL_MULTIMEDIA + "/___" + id + ".jpg";
    return uRLNormalizedPreview;
  }
  
  public void setURLNormalizedPreview(String uRLNormalizedPreview) {
    this.uRLNormalizedPreview = uRLNormalizedPreview;
  }
  
  public String getURLPreview() {
    if(uRLPreview == null) return sURL_MULTIMEDIA + "/_" + id + ".jpg";
    return uRLPreview;
  }
  
  public void setURLPreview(String uRLPreview) {
    this.uRLPreview = uRLPreview;
  }
  
  public String getURLSmallPreview() {
    if(uRLSmallPreview == null) return sURL_MULTIMEDIA + "/__" + id + ".jpg";
    return uRLSmallPreview;
  }
  
  public void setURLSmallPreview(String uRLSmallPreview) {
    this.uRLSmallPreview = uRLSmallPreview;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }
  
  public String getFilePath() {
    return filePath;
  }
  
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
  
  public int getPage() {
    return page;
  }
  
  public void setPage(int page) {
    this.page = page;
  }
  
  public int getPages() {
    return pages;
  }
  
  public void setPages(int pages) {
    this.pages = pages;
  }
  
  public int getCount() {
    return count;
  }
  
  public int getIdArticle() {
    return idArticle;
  }
  
  public void setIdArticle(int idArticle) {
    this.idArticle = idArticle;
  }
  
  public File getFile() {
    File file = null;
    if(filePath != null && filePath.length() > 0) {
      file = new File(filePath);
      if(!file.exists()) file = null;
    }
    if(file != null) return file;
    return WSArticoli.getFile(id);
  }
  
  public String getDimension() {
    File file = getFile();
    return getDimension(file);
  }
  
  public boolean isVideo() {
    return WSArticoli.isVideoFile(type);
  }
  
  public boolean isAudio() {
    return WSArticoli.isAudioFile(type);
  }
  
  public boolean isImage() {
    return WSArticoli.isImageFile(type);
  }
  
  public boolean isDoc() {
    return WSArticoli.isDocFile(type);
  }
  
  public
  boolean isLink()
  {
    return WSArticoli.isLinkFile(type);
  }
  
  @Override
  public int hashCode() {
    return id;
  }
  
  @Override
  public boolean equals(Object o) {
    if(!(o instanceof Multimedia)) return false;
    int iId = ((Multimedia) o).getId();
    return iId == id;
  }
  
  @Override
  public String toString() {
    if(title != null) return title;
    return "";
  }
  
  /**
   * Resituisce la dimensione del file in un formato leggibile.
   * 
   * @param file File
   * @return Dimensione del file
   */
  public static
  String getDimension(File file)
  {
    if(file == null) return "";
    long lLength = file.length();
    if(lLength < 1024) {
      return lLength + " bytes";
    }
    else if(lLength < 1048576) { // 1 M
      long lKB = lLength / 1024l;
      long lDecimal = (lLength % 1024l) * 10 / 1024l;
      if(lDecimal > 0) {
        return lKB + "." + lDecimal + " KB";
      }
      return lKB + " KB";
    }
    else if(lLength < 1073741824) { // 1 G
      long lMB = lLength / 1048576l;
      long lDecimal = (lLength % 1048576l) * 10 / 1048576l;
      if(lDecimal > 0) {
        return lMB + "." + lDecimal + " MB";
      }
      return lMB + " MB";
    }
    else {
      long lGB = lLength / 1073741824l;
      long lDecimal = (lLength % 1073741824l) * 10 / 1073741824l;
      if(lDecimal > 0) {
        return lGB + "." + lDecimal + " GB";
      }
      return lGB + " GB";
    }
  }
  
  public static 
  List<Multimedia> toListOfMultimedia(List<Map<String, Object>> list, int idLang) 
  {
    int size = list != null ? list.size() : 0;
    List<Multimedia> listResult = new ArrayList<Multimedia>(size);
    if(size == 0) return listResult;
    for(int i = 0; i < size; i++) {
      listResult.add(new Multimedia(list.get(i), idLang));
    }
    return listResult;
  }	
}
