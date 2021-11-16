package org.dew.cms.backend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;

/**
 * Classe per la pubblicazione di un feed RSS 2.0.<br>
 * Alcune opzioni sulle quali prestare attenzione: <br>
 * <br>
 * portBaseURL = URL base del portale (http://host:port/context). Serve per determinare la URL completa dei contenuti multimediali.<br>
 * artViewURL = URL base per la visualizzazione di un articolo (es. http://host:port/context/index.do?ida=). Potrebbe essere anche il primo "pezzo" da anteporre alla URL dell'articolo.
 */
public 
class RSS20 
{
  // Canale RSS
  private String title;
  private String link;
  private String description;
  private String language;
  private String copyright;
  private String managingEditor;
  private String webMaster;
  private String category;
  private String domain;
  private String docs;
  private Date lastBuildDate;
  private int ttl;
  // opzioni visualizzazione articolo
  private String  portalBaseURL;
  private String  artViewURL;
  private int     artMaxLength;
  private boolean artShowPreview      = false;
  private boolean artShowFirstPhoto   = true;
  private boolean artShowImagesInText = false;
  private boolean artRemoveBR         = true;
  
  private DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
  
  public RSS20()
  {
  }
  
  public RSS20(String title)
  {
    this.title = title;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String getCopyright() {
    return copyright;
  }
  
  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }
  
  public String getManagingEditor() {
    return managingEditor;
  }
  
  public void setManagingEditor(String managingEditor) {
    this.managingEditor = managingEditor;
  }
  
  public String getWebMaster() {
    return webMaster;
  }
  
  public void setWebMaster(String webMaster) {
    this.webMaster = webMaster;
  }
  
  public String getCategory() {
    return category;
  }
  
  public void setCategory(String category) {
    this.category = category;
  }
  
  public String getDomain() {
    return domain;
  }
  
  public void setDomain(String domain) {
    this.domain = domain;
  }
  
  public String getDocs() {
    return docs;
  }
  
  public void setDocs(String docs) {
    this.docs = docs;
  }
  
  public Date getLastBuildDate() {
    return lastBuildDate;
  }
  
  public void setLastBuildDate(Date lastBuildDate) {
    this.lastBuildDate = lastBuildDate;
  }
  
  public int getTtl() {
    return ttl;
  }
  
  public void setTtl(int ttl) {
    this.ttl = ttl;
  }
  
  public String getPortalBaseURL() {
    return portalBaseURL;
  }
  
  public void setPortalBaseURL(String portalBaseURL) {
    this.portalBaseURL = portalBaseURL;
  }
  
  public String getArtViewURL() {
    return artViewURL;
  }
  
  public void setArtViewURL(String artViewURL) {
    this.artViewURL = artViewURL;
  }
  
  public int getArtMaxLength() {
    return artMaxLength;
  }
  
  public void setArtMaxLength(int artMaxLength) {
    this.artMaxLength = artMaxLength;
  }
  
  public boolean isArtShowPreview() {
    return artShowPreview;
  }
  
  public void setArtShowPreview(boolean artShowPreview) {
    this.artShowPreview = artShowPreview;
  }
  
  public boolean isArtShowImagesInText() {
    return artShowImagesInText;
  }
  
  public void setArtShowFirstPhoto(boolean artShowFirstPhoto) {
    this.artShowFirstPhoto = artShowFirstPhoto;
  }
  
  public boolean isArtShowFirstPhoto() {
    return artShowFirstPhoto;
  }
  
  public void setArtShowImagesInText(boolean artShowImagesInText) {
    this.artShowImagesInText = artShowImagesInText;
  }
  
  public boolean isArtRemoveBR() {
    return artRemoveBR;
  }
  
  public void setArtRemoveBR(boolean artRemoveBR) {
    this.artRemoveBR = artRemoveBR;
  }
  
  public
  String toXML(Collection<Article> colArticles)
  {
    String sResult = "<?xml version=\"1.0\"?>";
    sResult += "<rss version=\"2.0\">";
    sResult += "<channel>";
    if(title != null && title.length() > 0) {
      sResult += "<title><![CDATA[" + title + "]]></title>";
    }
    else {
      sResult += "<title>RSS</title>";
    }
    if(link != null && link.length() > 0) {
      sResult += "<link>" + link + "</link>";
    }
    else {
      sResult += "<link>" + getURLPortal() + "</link>";
    }
    if(description != null && description.length() > 0) {
      sResult += "<description><![CDATA[" + description + "]]></description>";
    }
    else {
      sResult += "<description>Articles</description>";
    }
    if(language != null && language.length() > 0) {
      sResult += "<language>" + language + "</language>";
    }
    else {
      sResult += "<language>it</language>";
    }
    if(copyright != null && copyright.length() > 0) {
      sResult += "<copyright>" + copyright + "</copyright>";
    }
    if(managingEditor != null && managingEditor.length() > 0) {
      sResult += "<managingEditor>" + managingEditor + "</managingEditor>";
    }		
    if(webMaster != null && webMaster.length() > 0) {
      sResult += "<webMaster>" + webMaster + "</webMaster>";
    }
    if(lastBuildDate != null) {
      sResult += "<lastBuildDate>" + df.format(lastBuildDate) + "</lastBuildDate>";
    }
    if(category != null && category.length() > 0) {
      if(domain != null && domain.length() > 0) {
        sResult += "<category domain=\"" + domain + "\"><![CDATA[" + category + "]]></category>";
      }
      else {
        sResult += "<category><![CDATA[" + category + "]]></category>";
      }
    }
    else {
      sResult += "<category>Articles</category>";
    }
    if(docs != null && docs.length() > 0) {
      sResult += "<docs>" + docs + "</docs>";
    }
    else {
      sResult += "<docs>http://blogs.law.harvard.edu/tech/rss</docs>";
    }
    if(managingEditor != null && managingEditor.length() > 0) {
      sResult += "<managingEditor>" + managingEditor + "</managingEditor>";
    }
    if(ttl > 0) {
      sResult += "<ttl>" + ttl + "</ttl>";
    }
    else {
      sResult += "<ttl>30</ttl>";
    }
    if(colArticles != null) {
      Iterator<Article> iterator = colArticles.iterator();
      while(iterator.hasNext()) {
        Article article = iterator.next();
        sResult += toXML(article);
      }
    }
    sResult += "</channel>";
    sResult += "</rss>";
    return sResult;
  }
  
  public
  String toXML(Article article)
  {
    if(article == null) return "";
    String sTitle    = article.getTitle();
    String sAbstract = null;
    if(artShowImagesInText) {
      sAbstract = article.getAbstractText(artMaxLength, "...", artRemoveBR, null, "<img src=\"#\" />", "<img src=\"#\" />", true);
    }
    else {
      sAbstract = article.getAbstractText(artMaxLength, "...", artRemoveBR, null, null, null, false);
    }
    String sArtURL   = article.getURL();
    Calendar calDate = article.getDateCalendar();
    String sSubCat   = article.getSubcategory();
    String sAuthor   = article.getAuthor();
    String sAutEmail = article.getAuthorEmail();
    String sPreview  = "";
    if(artShowFirstPhoto && article.hasPhotos()) {
      int[] photos = article.getPhotos();
      if(photos != null && photos.length > 0) {
        String sURLPreview = article.getURLPreview(photos[0]);
        if(sURLPreview != null && sURLPreview.length() > 0) {
          if(!sURLPreview.endsWith("/_0.jpg")) {
            if(sURLPreview.startsWith("http:") || sURLPreview.startsWith("https:")) {
              sPreview = "<img src=\"" + sURLPreview + "\" align=\"left\" hspace=\"10\">";
            }
            else {
              String sURLPortal = getURLPortal();
              if(sURLPortal != null && sURLPortal.length() > 0) {
                String sSrcImage = sURLPortal + "/" + sURLPreview;
                sPreview = "<img src=\"" + sSrcImage + "\" align=\"left\" hspace=\"10\">";
              }
            }
          }
        }
      }
    }
    else
      if(artShowPreview) {
        String sURLPreview = article.getURLPreview();
        if(sURLPreview != null && sURLPreview.length() > 0) {
          if(!sURLPreview.endsWith("/_0.jpg")) {
            if(sURLPreview.startsWith("http:") || sURLPreview.startsWith("https:")) {
              sPreview = "<img src=\"" + sURLPreview + "\" align=\"left\" hspace=\"10\">";
            }
            else {
              String sURLPortal = getURLPortal();
              if(sURLPortal != null && sURLPortal.length() > 0) {
                String sSrcImage = sURLPortal + "/" + sURLPreview;
                sPreview = "<img src=\"" + sSrcImage + "\" align=\"left\" hspace=\"10\">";
              }
            }
          }
        }
      }
    if(sTitle    == null) sTitle = "";
    if(sAbstract == null) sAbstract = sTitle;
    if(calDate   == null) calDate = new GregorianCalendar();
    String sLinkArt = null;
    if(sArtURL != null && sArtURL.length() > 0) {
      if(sArtURL.startsWith("http:") || sArtURL.startsWith("https:")) {
        sLinkArt = sArtURL;
      }
      else
        if(artViewURL != null && artViewURL.length() > 0) {
          sLinkArt = artViewURL + sArtURL;
        }
        else {
          sLinkArt = sArtURL;
        }
    }
    else {
      if(artViewURL != null && artViewURL.length() > 0) {
        sLinkArt = artViewURL + article.getId();
      }
    }
    String sResult = "<item>";
    sResult += "<title><![CDATA[" + sTitle + "]]></title>";
    if(sLinkArt != null && sLinkArt.length() > 0) {
      sResult += "<link>" + sLinkArt + "</link>";
    }
    if(sAuthor != null && sAuthor.length() > 0) {
      if(sAutEmail != null && sAutEmail.length() > 0) {
        sResult += "<author><![CDATA[" + sAutEmail + " (" + sAuthor + ")]]></author>";
      }
      else {
        sResult += "<author><![CDATA[" + sAuthor + "]]></author>";
      }
    }
    sResult += "<description><![CDATA[" + sPreview + sAbstract + "]]></description>";
    if(sSubCat != null && sSubCat.length() > 0) {
      if(domain != null && domain.length() > 0) {
        sResult += "<category domain=\"" + domain + "\"><![CDATA[" + sSubCat + "]]></category>";
      }
      else {
        sResult += "<category><![CDATA[" + sSubCat + "]]></category>";
      }
    }
    if(calDate != null) {
      sResult += "<pubDate>" + df.format(calDate.getTime()) + "</pubDate>";
    }
    sResult += "</item>";
    return sResult;
  }
  
  public
  String getURLPortal()
  {
    if(portalBaseURL != null && portalBaseURL.length() > 4) {
      return portalBaseURL;
    }
    if(artViewURL != null && artViewURL.length() > 4) {
      int iLastSep = artViewURL.lastIndexOf('/');
      if(iLastSep > 0) {
        return artViewURL.substring(0, iLastSep);
      }
    }
    if(link != null && link.length() > 0) {
      return link;
    }
    if(domain != null && domain.length() > 0) {
      int iLastSep = domain.lastIndexOf('/');
      if(iLastSep > 0) {
        return domain.substring(0, iLastSep);
      }
    }
    return null;
  }
}
