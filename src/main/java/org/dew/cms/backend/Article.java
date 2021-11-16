package org.dew.cms.backend;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.util.WMap;
import org.util.WUtil;

import org.dew.cms.backend.ws.WSArticoli;

import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IAutore;
import org.dew.cms.common.ILuogo;
import org.dew.cms.common.ITag;

/**
 * Article bean. 
 */
public 
class Article implements Serializable
{
  private static final long serialVersionUID = 973504664190202862L;

  private int id;
  private String title;
  private String specific;
  private String abstractText;
  private String text;
  private String note;
  private String references;
  private int category;
  private int type;
  private int idSubcategory;
  private String subcategory;
  private String place;
  private String author;
  private String authorEmail;
  private int institute;
  private String codInstitute;
  private String desInstitute;
  private Calendar date;
  private int language;
  private int userType;
  private String uRL;
  private int page;
  private int pages;
  private int count;
  private int countLike;
  private int countDontLike;
  private int[] audios;
  private int[] photos;
  private int[] videos;
  private int[] docs;
  private int[] links;
  private HashSet<String> tags;
  private Map<String, String>  mapTagsDesc;
  private int defMultimedia = 0;
  private Article[] related;
  private Article[] sameAuthor;
  private Article[] components;
  private Place[] places;
  private boolean route;
  private Map<Integer, String> mapMultimedia_Id_Ext;
  private Map<Integer, String> mapMultimedia_Id_Tit;
  private Map<Integer, String> mapMultimedia_Id_URL;
  private Price[] prices;
  
  public Article()
  {
  }
  
  public Article(int defMultimedia)
  {
    this.defMultimedia = defMultimedia;
  }
  
  public Article(Map<String, Object> map)
  {
    this(map, 0);
  }
  
  public Article(Map<String, Object> map, int idLang)
  {
    if(map == null || map.isEmpty()) return;
    
    String lang = String.valueOf(idLang);
    
    WMap wmap = new WMap(map);
    
    id            = wmap.getInt(IArticolo.sID);
    language      = idLang;
    setDate(wmap.get(IArticolo.sDATA_ARTICOLO), wmap.get(IArticolo.sDATA_INS));
    place         = wmap.getString(IArticolo.sDESC_LUOGO);
    category      = wmap.getInt(IArticolo.sID_CATEGORIA);
    idSubcategory = wmap.getInt(IArticolo.sID_SOTTOCATEG);
    type          = wmap.getInt(IArticolo.sID_TIPO_ART);
    defMultimedia = type * -1;
    userType      = wmap.getInt(IArticolo.sID_TIPO_UTE);
    page          = wmap.getInt(IArticolo.sPAGINA);
    pages         = wmap.getInt(IArticolo.sPAGINE);
    count         = wmap.getInt(IArticolo.sCOUNT);
    countLike     = wmap.getInt(IArticolo.sPREF_POSITIVE);
    countDontLike = wmap.getInt(IArticolo.sPREF_NEGATIVE);
    institute     = wmap.getInt(IArticolo.sID_ISTITUTO);
    codInstitute  = wmap.getString(IArticolo.sCOD_ISTITUTO);
    desInstitute  = wmap.getString(IArticolo.sDES_ISTITUTO);
    
    Map<String, Object> mapContenuti = wmap.getMapObject(IArticolo.sCONTENUTI);
    if(mapContenuti != null) {
      Map<String, Object> mapArtL = WUtil.toMapObject(mapContenuti.get(lang), false);
      Map<String, Object> mapArt0 = WUtil.toMapObject(mapContenuti.get("0"),  false);
      Map<String, Object> mapArtC = mapArtL;
      if(mapArtC == null || mapArtC.isEmpty()) mapArtC = mapArt0;
      
      WMap wmapArtC = new WMap(mapArtC);
      WMap wmapArt0 = new WMap(mapArt0);
      
      title        = wmapArtC.getString(IArticolo.sTITOLO);
      specific     = wmapArtC.getString(IArticolo.sSPECIFICA);
      abstractText = wmapArtC.getString(IArticolo.sABSTRACT);
      text         = wmapArtC.getString(IArticolo.sTESTO);
      note         = wmapArtC.getString(IArticolo.sNOTE);
      references   = wmapArtC.getString(IArticolo.sRIFERIMENTI);
      
      List<Map<String, Object>> prezzi = wmapArtC.getListOfMapObject(IArticolo.sPREZZI);
      
      if(prezzi == null || prezzi.size() == 0) {
        prezzi = wmapArt0.getListOfMapObject(IArticolo.sPREZZI);
      }
      
      if(prezzi != null && prezzi.size() > 0) {
        prices = new Price[prezzi.size()];
        for(int i = 0; i < prezzi.size(); i++) {
          prices[i] = new Price(prezzi.get(i));
        }
      }
    }
    
    Map<String, Object> mapDescSottoCategoria = wmap.getMapObject(IArticolo.sDESC_SOTTOCAT);
    if(mapDescSottoCategoria != null) {
      subcategory = WUtil.toString(mapDescSottoCategoria.get(lang), null);
      if(subcategory == null || subcategory.length() == 0) {
        subcategory = WUtil.toString(mapDescSottoCategoria.get("0"), null);
      }
    }
    
    Map<String, Object> mapMultimedia = wmap.getMapObject(IArticolo.sMULTIMEDIA);
    if(mapMultimedia != null) {
      Map<String, Object> mapId = WUtil.toMapObject(mapMultimedia.get("#"), false);
      
      Iterator<String> iterator = mapMultimedia.keySet().iterator();
      List<String> listURLFile = new ArrayList<String>();
      while(iterator.hasNext()) listURLFile.add(iterator.next());
      Collections.sort(listURLFile);
      
      List<Integer> listAudios = new ArrayList<Integer>();
      List<Integer> listPhotos = new ArrayList<Integer>();
      List<Integer> listVideos = new ArrayList<Integer>();
      List<Integer> listDocs   = new ArrayList<Integer>();
      List<Integer> listLinks  = new ArrayList<Integer>();
      
      mapMultimedia_Id_Ext = new HashMap<Integer, String>();
      mapMultimedia_Id_Tit = new HashMap<Integer, String>();
      mapMultimedia_Id_URL = new HashMap<Integer, String>();
      
      for(int i = 0; i < listURLFile.size(); i++) {
        String sURLFile = listURLFile.get(i);
        Integer oIdMultimedia = null;
        if(mapId != null) {
          oIdMultimedia = WUtil.toInteger(mapId.get(sURLFile + "#" + language), null);
          if(oIdMultimedia == null) oIdMultimedia = WUtil.toInteger(mapId.get(sURLFile + "#0"), null);
        }
        if(oIdMultimedia == null) continue;
        
        String ext = getExtension(sURLFile);
        mapMultimedia_Id_URL.put(oIdMultimedia, sURLFile);
        mapMultimedia_Id_Ext.put(oIdMultimedia, ext);
        
        Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sURLFile), false);
        if(mapDescrizioni != null) {
          String sDescrizione = WUtil.toString(mapDescrizioni.get(lang), null);
          if(sDescrizione == null || sDescrizione.length() == 0) {
            sDescrizione = WUtil.toString(mapDescrizioni.get("0"), null);
          }
          if(sDescrizione != null && sDescrizione.length() > 0) {
            mapMultimedia_Id_Tit.put(oIdMultimedia, sDescrizione);
          }
        }
        
        if(WSArticoli.isAudioFile(ext))      listAudios.add(oIdMultimedia);
        else if(WSArticoli.isImageFile(ext)) listPhotos.add(oIdMultimedia);
        else if(WSArticoli.isVideoFile(ext)) listVideos.add(oIdMultimedia);
        else if(WSArticoli.isLinkFile(ext))  listLinks.add(oIdMultimedia); 
        else {
          listDocs.add(oIdMultimedia);
        }
      }
      
      audios = WUtil.toArrayOfInt(listAudios, true);
      photos = WUtil.toArrayOfInt(listPhotos, true);
      videos = WUtil.toArrayOfInt(listVideos, true);
      docs   = WUtil.toArrayOfInt(listDocs,   true);
      links  = WUtil.toArrayOfInt(listLinks,  true);
    }
    
    List<Map<String, Object>> listCorrelati = wmap.getListOfMapObject(IArticolo.sCORRELATI);
    if(listCorrelati != null && listCorrelati.size() > 0) {
      related = new Article[listCorrelati.size()];
      for(int i = 0; i < listCorrelati.size(); i++) {
        Map<String, Object> mapArt = listCorrelati.get(i);
        
        Article article = new Article();
        article.setId(WUtil.toInt(mapArt.get(IArticolo.sID), 0));
        article.setTitle(getTitleByLang(mapArt.get(IArticolo.sTITOLO), language, ""));
        article.setDate(mapArt.get(IArticolo.sDATA_ARTICOLO));
        article.setLanguage(language);
        
        related[i] = article;
      }
    }
    
    List<Map<String, Object>> listStessoAutore = wmap.getListOfMapObject(IArticolo.sSTESSO_AUTORE);
    if(listStessoAutore != null && listStessoAutore.size() > 0) {
      sameAuthor = new Article[listStessoAutore.size()];
      for(int i = 0; i < listStessoAutore.size(); i++) {
        Map<String, Object> mapArt = listStessoAutore.get(i);
        
        Article article = new Article();
        article.setId(WUtil.toInt(mapArt.get(IArticolo.sID), 0));
        article.setTitle(getTitleByLang(mapArt.get(IArticolo.sTITOLO), language, ""));
        article.setDate(mapArt.get(IArticolo.sDATA_ARTICOLO));
        article.setLanguage(language);
        
        sameAuthor[i] = article;
      }
    }
    
    List<Map<String, Object>> listComponenti = wmap.getListOfMapObject(IArticolo.sCOMPONENTI);
    if(listComponenti != null && listComponenti.size() > 0) {
      components = new Article[listComponenti.size()];
      for(int i = 0; i < listComponenti.size(); i++) {
        Map<String, Object> mapArt = listComponenti.get(i);
        
        Article article = new Article();
        article.setId(WUtil.toInt(mapArt.get(IArticolo.sID), 0));
        article.setTitle(getTitleByLang(mapArt.get(IArticolo.sTITOLO), language, ""));
        article.setDate(mapArt.get(IArticolo.sDATA_ARTICOLO));
        article.setLanguage(language);
        
        components[i] = article;
      }
    }
    
    List<Map<String, Object>> listLuoghi = wmap.getListOfMapObject(IArticolo.sLUOGHI);
    if(listLuoghi != null && listLuoghi.size() > 0) {
      places = new Place[listLuoghi.size()];
      for(int i = 0; i < listLuoghi.size(); i++) {
        Map<String, Object> mapLuogo = listLuoghi.get(i);
        places[i] = new Place(mapLuogo);
        
        route = WUtil.toBoolean(mapLuogo.get(ILuogo.sFL_PERCORSO), false);
      }
    }
    
    List<Map<String, Object>> listAutori = wmap.getListOfMapObject(IArticolo.sAUTORI);
    if(listAutori != null && listAutori.size() > 0) {
      Map<String, Object> mapFirstAutore = listAutori.get(0);
      if(mapFirstAutore != null) {
        String sAutCognome = WUtil.toString(mapFirstAutore.get(IAutore.sCOGNOME), null);
        String sAutNome    = WUtil.toString(mapFirstAutore.get(IAutore.sNOME),    null);
        if(sAutCognome != null && sAutCognome.length() > 0) {
          if(sAutNome != null && sAutNome.length() > 0) {
            this.author = sAutNome + " " + sAutCognome;
          }
          else {
            this.author = sAutCognome;
          }
        }
        String sAutEmail = WUtil.toLowerString(mapFirstAutore.get(IAutore.sEMAIL), null);
        if(sAutEmail != null && sAutEmail.length() > 4 && sAutEmail.indexOf('@') > 0) {
          this.authorEmail = sAutEmail.trim();
        }
      }
    }
    
    List<Map<String, Object>> listTag = wmap.getListOfMapObject(IArticolo.sTAG);
    if(listTag != null && listTag.size() > 0) {
      tags        = new HashSet<String>();
      mapTagsDesc = new HashMap<String,String>();
      for(int i = 0; i < listTag.size(); i++) {
        Map<String, Object> mapTag = listTag.get(i);
        String sCodice = WUtil.toString(mapTag.get(ITag.sCODICE), null);
        if(sCodice != null && sCodice.length() > 0) {
          tags.add(sCodice);
          String sDesc = WUtil.toString(mapTag.get(ITag.sDESC_IN_ART), null);
          if(sDesc != null && sDesc.length() > 0) {
            mapTagsDesc.put(sCodice, sDesc);
          }
        }
      }
    }
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getTitle() {
    if(title == null) return "";
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getSpecific() {
    if(specific == null) return "";
    return specific;
  }
  
  public void setSpecific(String specific) {
    this.specific = specific;
  }
  
  public String getAbstractText() {
    if(abstractText == null || abstractText.length() == 0) {
      if(text != null && text.length() > 0) {
        return text;
      }
      else {
        return "";
      }
    }
    return abstractText;
  }
  
  public String getAbstractText(boolean boGetTextIfAbstractIsNull) {
    if(abstractText == null || abstractText.length() == 0) {
      if(boGetTextIfAbstractIsNull && (text != null && text.length() > 0)) {
        return text;
      }
      else {
        return "";
      }
    }
    return abstractText;
  }
  
  public String getAbstractText(int iMaxLength, String sAfterCut, boolean boRemoveBR, String sClassAnchor, String sTagImgSx, String sTagImgDx, boolean boPreview) {
    if(abstractText == null || abstractText.length() == 0) {
      if(text != null && text.length() > 0) {
        return format(text, iMaxLength, sAfterCut, boRemoveBR, sClassAnchor, sTagImgSx, sTagImgDx, boPreview);
      }
      else {
        return "";
      }
    }
    return format(abstractText, iMaxLength, sAfterCut, boRemoveBR, sClassAnchor, sTagImgSx, sTagImgDx, boPreview);
  }
  
  public String getAbstractText(String sClassAnchor, String sTagImgSx, String sTagImgDx) {
    if(abstractText == null || abstractText.length() == 0) {
      if(text != null && text.length() > 0) {
        return format(text, 0, null, false, sClassAnchor, sTagImgSx, sTagImgDx, false);
      }
      else {
        return "";
      }
    }
    return format(abstractText, 0, null, false, sClassAnchor, sTagImgSx, sTagImgDx, false);
  }
  
  public void setAbstractText(String abstractText) {
    this.abstractText = abstractText;
  }
  
  public String getText() {
    if(text == null) return "";
    return text;
  }
  
  public String getText(int iMaxLength, String sAfterCut, boolean boRemoveBR, String sClassAnchor, String sTagImgSx, String sTagImgDx, boolean boPreview) {
    if(text == null) return "";
    return format(text, iMaxLength, sAfterCut, boRemoveBR, sClassAnchor, sTagImgSx, sTagImgDx, boPreview);
  }
  
  public String getText(String sClassAnchor, String sTagImgSx, String sTagImgDx) {
    if(text == null) return "";
    return format(text, 0, null, false, sClassAnchor, sTagImgSx, sTagImgDx, false);
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  public String getNote() {
    if(note == null) return "";
    return note;
  }
  
  public String getNote(int iMaxLength, String sAfterCut, boolean boRemoveBR, String sClassAnchor, String sTagImgSx, String sTagImgDx, boolean boPreview) {
    if(note == null) return "";
    return format(note, iMaxLength, sAfterCut, boRemoveBR, sClassAnchor, sTagImgSx, sTagImgDx, boPreview);
  }
  
  public String getNote(String sClassAnchor, String sTagImgSx, String sTagImgDx) {
    if(note == null) return "";
    return format(note, 0, null, false, sClassAnchor, sTagImgSx, sTagImgDx, false);
  }
  
  public void setNote(String note) {
    this.note = note;
  }
  
  public String getReferences() {
    if(references == null) return "";
    return references;
  }
  
  public String getReferences(int iMaxLength, String sAfterCut, boolean boRemoveBR, String sClassAnchor, String sTagImgSx, String sTagImgDx, boolean boPreview) {
    if(references == null) return "";
    return format(references, iMaxLength, sAfterCut, boRemoveBR, sClassAnchor, sTagImgSx, sTagImgDx, boPreview);
  }
  
  public String getReferences(String sClassAnchor, String sTagImgSx, String sTagImgDx) {
    if(references == null) return "";
    return format(references, 0, null, false, sClassAnchor, sTagImgSx, sTagImgDx, false);
  }
  
  public void setReferences(String references) {
    this.references = references;
  }
  
  public int getCategory() {
    return category;
  }
  
  public void setCategory(int category) {
    this.category = category;
  }
  
  public int getIdSubcategory() {
    return idSubcategory;
  }
  
  public void setIdSubcategory(int idSubcategory) {
    this.idSubcategory = idSubcategory;
  }
  
  public int getType() {
    return type;
  }
  
  public void setType(int type) {
    this.type = type;
  }
  
  public String getSubcategory() {
    if(subcategory == null) return "";
    return subcategory;
  }
  
  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }
  
  public String getPlace() {
    if(place == null) return "";
    return place;
  }
  
  public void setPlace(String place) {
    this.place = place;
  }
  
  public String getAuthor() {
    if(author == null) return "";
    return author;
  }
  
  public void setAuthor(String author) {
    this.author = author;
  }
  
  public String getAuthorEmail() {
    if(authorEmail == null) return "";
    return authorEmail;
  }
  
  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }
  
  public int getInstitute() {
    return institute;
  }
  
  public void setInstitute(int institute) {
    this.institute = institute;
  }
  
  public String getCodInstitute() {
    return codInstitute;
  }
  
  public void setCodInstitute(String codInstitute) {
    this.codInstitute = codInstitute;
  }
  
  public String getDesInstitute() {
    return desInstitute;
  }
  
  public void setDesInstitute(String desInstitute) {
    this.desInstitute = desInstitute;
  }
  
  public void setDate(Object date) {
    this.date = WUtil.toCalendar(date, null);
  }
  
  public void setDate(Object date, Object time) {
    this.date = WUtil.setTime(WUtil.toCalendar(date, null), time);
  }
  
  public Calendar getDateCalendar() {
    return date;
  }
  
  public String getDate() {
    if(date == null) return "";
    return WUtil.formatDate(date, "IT");
  }
  
  public String getDate(Object locale) {
    if(date == null) return "";
    return WUtil.formatDate(date, locale);
  }
  
  public int getLanguage() {
    return language;
  }
  
  public void setLanguage(int language) {
    this.language = language;
  }
  
  public int getUserType() {
    return userType;
  }
  
  public void setUserType(int userType) {
    this.userType = userType;
  }
  
  public String getURL() {
    if(uRL == null) return "";
    return uRL;
  }
  
  public void setURL(String uRL) {
    this.uRL = uRL;
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
  
  public void setCount(int count) {
    this.count = count;
  }
  
  public int getCountLike() {
    return countLike;
  }
  
  public void setCountLike(int countLike) {
    this.countLike = countLike;
  }
  
  public int getCountDontLike() {
    return countDontLike;
  }
  
  public void setCountDontLike(int countDontLike) {
    this.countDontLike = countDontLike;
  }
  
  public Article[] getRelated() {
    return related;
  }
  
  public void setRelated(Article[] related) {
    this.related = related;
  }
  
  public Article[] getSameAuthor() {
    return sameAuthor;
  }
  
  public void setSameAuthor(Article[] sameAuthor) {
    this.sameAuthor = sameAuthor;
  }
  
  public Article[] getComponents() {
    return components;
  }
  
  public void setComponents(Article[] components) {
    this.components = components;
  }
  
  public int[] getAudios() {
    return audios;
  }
  
  public void setAudios(int[] audios) {
    this.audios = audios;
  }
  
  public int[] getPhotos() {
    return photos;
  }
  
  public void setPhotos(int[] photos) {
    this.photos = photos;
  }
  
  public int[] getVideos() {
    return videos;
  }
  
  public void setVideos(int[] videos) {
    this.videos = videos;
  }
  
  public int[] getDocs() {
    return docs;
  }
  
  public void setDocs(int[] docs) {
    this.docs = docs;
  }
  
  public int[] getLinks() {
    return links;
  }
  
  public void setLinks(int[] links) {
    this.links = links;
  }
  
  public HashSet<String> getTags() {
    return tags;
  }
  
  public void setTags(HashSet<String> tags) {
    this.tags = tags;
  }
  
  public boolean containsTag(String sTag) {
    if(tags == null) return false;
    return tags.contains(sTag);
  }
  
  public String firstTagStartWith(String sPrefix) {
    if(tags == null) return null;
    Iterator<String> iterator = tags.iterator();
    while(iterator.hasNext()) {
      String sTag = iterator.next();
      if(sTag.startsWith(sPrefix)) return sTag;
    }
    return null;
  }
  
  public List<String> getTagsStartWith(String sPrefix) {
    List<String> listResult = new ArrayList<String>();
    if(tags == null) return listResult;
    Iterator<String> iterator = tags.iterator();
    while(iterator.hasNext()) {
      String sTag = iterator.next();
      if(sTag.startsWith(sPrefix)) listResult.add(sTag);
    }
    Collections.sort(listResult);
    return listResult;
  }
  
  public List<String> getListTags() {
    List<String> listResult = new ArrayList<String>();
    if(tags == null) return listResult;
    Iterator<String> iterator = tags.iterator();
    while(iterator.hasNext()) {
      String sTag = iterator.next();
      listResult.add(sTag);
    }
    Collections.sort(listResult);
    return listResult;
  }
  
  public String getDescTag(String sTag) {
    if(mapTagsDesc == null || mapTagsDesc.isEmpty()) return null;
    return mapTagsDesc.get(sTag);
  }
  
  public String getDescTag(String sTag, String sDefault) {
    if(mapTagsDesc == null || mapTagsDesc.isEmpty()) return sDefault;
    String sResult = mapTagsDesc.get(sTag);
    if(sResult == null || sResult.length() == 0) return sDefault;
    return sResult;
  }
  
  public Place[] getPlaces() {
    return places;
  }
  
  public void setPlaces(Place[] places) {
    this.places = places;
  }
  
  public Multimedia[] getMultimedia() {
    int iLenPho = photos != null ? photos.length : 0;
    int iLenVid = videos != null ? videos.length : 0;
    int iLenAud = audios != null ? audios.length : 0;
    int iLenDoc = docs   != null ? docs.length   : 0;
    int iLenLnk = links  != null ? links.length  : 0;
    int iLenRes = iLenPho + iLenVid + iLenAud + iLenDoc + iLenLnk;
    Multimedia[] result = new Multimedia[iLenRes];
    int r = 0; 
    for(int i = 0; i < iLenPho; i++) result[r++] = getMultimedia(photos[i]);
    for(int i = 0; i < iLenVid; i++) result[r++] = getMultimedia(videos[i]);
    for(int i = 0; i < iLenAud; i++) result[r++] = getMultimedia(audios[i]);
    for(int i = 0; i < iLenDoc; i++) result[r++] = getMultimedia(docs[i]);
    for(int i = 0; i < iLenLnk; i++) result[r++] = getMultimedia(links[i]);
    return result;
  }
  
  /**
   * Restituisce i multimedia filtrati per tipo (estensione).
   * Sono accettati anche pi&ugrave; tipi separati dal pipe: ad esempio jpg|png|gif.
   * 
   * @param sFilterType Estensione dei file multimediali da selezionare
   * @return array di Multimedia
   */
  public Multimedia[] getMultimedia(String sFilterType) {
    if(sFilterType == null || sFilterType.length() == 0 || sFilterType.equals("*")) {
      return getMultimedia();
    }
    sFilterType = "|" + sFilterType.toLowerCase() + "|";
    int iLenPho = photos != null ? photos.length : 0;
    int iLenVid = videos != null ? videos.length : 0;
    int iLenAud = audios != null ? audios.length : 0;
    int iLenDoc = docs   != null ? docs.length   : 0;
    int iLenLnk = links  != null ? links.length  : 0;
    List<Integer> listOfId = new ArrayList<Integer>();
    for(int i = 0; i < iLenPho; i++) {
      String sType = getExtensionFile(photos[i]);
      if(sFilterType.indexOf("|" + sType + "|") >= 0) {
        listOfId.add(photos[i]);
      }
    }
    for(int i = 0; i < iLenVid; i++) {
      String sType = getExtensionFile(videos[i]);
      if(sFilterType.indexOf("|" + sType + "|") >= 0) {
        listOfId.add(videos[i]);
      }
    }
    for(int i = 0; i < iLenAud; i++) {
      String sType = getExtensionFile(audios[i]);
      if(sFilterType.indexOf("|" + sType + "|") >= 0) {
        listOfId.add(audios[i]);
      }
    }
    for(int i = 0; i < iLenDoc; i++) {
      String sType = getExtensionFile(docs[i]);
      if(sFilterType.indexOf("|" + sType + "|") >= 0) {
        listOfId.add(docs[i]);
      }
    }
    for(int i = 0; i < iLenLnk; i++) {
      String sType = getExtensionFile(links[i]);
      if(sFilterType.indexOf("|" + sType + "|") >= 0) {
        listOfId.add(links[i]);
      }
    }
    Multimedia[] result = new Multimedia[listOfId.size()];
    for(int i = 0; i < listOfId.size(); i++) {
      result[i] = getMultimedia(listOfId.get(i));
    }
    return result;
  }
  
  public Multimedia getMultimedia(int iIdMultimedia) {
    Multimedia multimedia = new Multimedia();
    multimedia.setId(iIdMultimedia);
    multimedia.setTitle(getTitleFile(iIdMultimedia));
    multimedia.setType(getExtensionFile(iIdMultimedia));
    multimedia.setLink(getURLLinkFile(iIdMultimedia));
    multimedia.setURL(getURLFile(iIdMultimedia));
    multimedia.setURLNormalizedPreview(getURLNormalizedPreview(iIdMultimedia));
    multimedia.setURLPreview(getURLPreview(iIdMultimedia));
    multimedia.setURLSmallPreview(getURLSmallPreview(iIdMultimedia));
    multimedia.setFilePath(getFilePath(iIdMultimedia));
    multimedia.setIdArticle(id);
    return multimedia;
  }
  
  public File getFile(int iIdMultimedia) {
    File file = null;
    String sFilePath = getFilePath(iIdMultimedia);
    if(sFilePath != null && sFilePath.length() > 0) {
      file = new File(sFilePath);
      if(!file.exists()) file = null;
    }
    if(file != null) return file;
    return WSArticoli.getFile(iIdMultimedia);
  }
  
  public String getDimension(int iIdMultimedia) {
    File file = getFile(iIdMultimedia);
    return Multimedia.getDimension(file);
  }
  
  public boolean isRoute() {
    return route;
  }
  
  public void setRoute(boolean route) {
    this.route = route;
  }
  
  public boolean hasVideos() {
    return videos != null && videos.length > 0;
  }
  
  public boolean hasAudios() {
    return audios != null && audios.length > 0;
  }
  
  public boolean hasPhotos() {
    return photos != null && photos.length > 0;
  }
  
  public boolean hasDocs() {
    return docs != null && docs.length > 0;
  }
  
  public boolean hasLinks() {
    return links != null && links.length > 0;
  }
  
  public Price[] getPrices() {
    return prices;
  }
  
  public double getPrice(int iIndex) {
    if(prices == null || prices.length <= iIndex) return 0.0d;
    Price price = prices[iIndex];
    if(price == null) return 0.0d;
    double dPrice    = price.getPrice();
    int    iDiscount = price.getDiscount();
    double dDisPrice = price.getDiscountedPrice();
    if(dDisPrice > 0.001d) return dDisPrice;
    if(iDiscount > 0) return WUtil.round2(dPrice-(dPrice*(double)iDiscount)/100.0d);
    return dPrice;
  }
  
  public String getCity() {
    if(places == null || places.length == 0) return "";
    Place place0 = places[0];
    if(place0 == null) return "";
    String sResult = place0.getCity();
    if(sResult == null) sResult = "";
    return sResult;
  }
  
  public String getCity(String sDefault) {
    if(places == null || places.length == 0) return sDefault;
    Place place0 = places[0];
    if(place0 == null) return sDefault;
    String sResult = place0.getCity();
    if(sResult == null) sResult = sDefault;
    return sResult;
  }
  
  public String getURLPreview() {
    if(photos != null && photos.length > 0) {
      return getURLPreview(photos[0]);
    }
    if(videos != null && videos.length > 0) {
      return getURLPreview(videos[0]);
    }
    if(audios != null && audios.length > 0) {
      return getURLPreview(audios[0]);
    }
    if(docs != null && docs.length > 0) {
      return getURLPreview(docs[0]);
    }
    return getURLPreview(defMultimedia);
  }
  
  public String getURLSmallPreview() {
    if(photos != null && photos.length > 0) {
      return getURLSmallPreview(photos[0]);
    }
    if(videos != null && videos.length > 0) {
      return getURLSmallPreview(videos[0]);
    }
    if(audios != null && audios.length > 0) {
      return getURLSmallPreview(audios[0]);
    }
    if(docs != null && docs.length > 0) {
      return getURLSmallPreview(docs[0]);
    }
    return getURLSmallPreview(defMultimedia);
  }
  
  public String getURLNormalizedPreview() {
    if(photos != null && photos.length > 0) {
      return getURLNormalizedPreview(photos[0]);
    }
    if(videos != null && videos.length > 0) {
      return getURLNormalizedPreview(videos[0]);
    }
    if(audios != null && audios.length > 0) {
      return getURLNormalizedPreview(audios[0]);
    }
    if(docs != null && docs.length > 0) {
      return getURLNormalizedPreview(docs[0]);
    }
    return getURLNormalizedPreview(defMultimedia);
  }
  
  public String getURLFile() {
    if(photos != null && photos.length > 0) {
      return getURLFile(photos[0]);
    }
    if(videos != null && videos.length > 0) {
      return getURLFile(videos[0]);
    }
    if(audios != null && audios.length > 0) {
      return getURLFile(audios[0]);
    }
    if(docs != null && docs.length > 0) {
      return getURLFile(docs[0]);
    }
    return getURLFile(defMultimedia);
  }
  
  public String getURLNormalizedPreview(int iIdMultimedia) {
    return Multimedia.sURL_MULTIMEDIA + "/___" + iIdMultimedia + ".jpg";
  }
  
  public String getURLSmallPreview(int iIdMultimedia) {
    return Multimedia.sURL_MULTIMEDIA + "/__" + iIdMultimedia + ".jpg";
  }
  
  public String getURLPreview(int iIdMultimedia) {
    return Multimedia.sURL_MULTIMEDIA + "/_" + iIdMultimedia + ".jpg";
  }
  
  public 
  String getURLFile(int iIdMultimedia) 
  {
    if(mapMultimedia_Id_Ext != null) {
      String sExt = (String) mapMultimedia_Id_Ext.get(new Integer(iIdMultimedia));
      if(sExt != null && sExt.length() > 0) {
        return Multimedia.sURL_MULTIMEDIA + "/" + iIdMultimedia + "." + sExt;
      }
    }
    return Multimedia.sURL_MULTIMEDIA + "/" + iIdMultimedia;
  }
  
  public 
  String getFilePath(int iIdMultimedia) 
  {
    if(mapMultimedia_Id_URL != null) {
      String sURLFile = (String) mapMultimedia_Id_URL.get(new Integer(iIdMultimedia));
      if(sURLFile == null || sURLFile.length() == 0) return null;
      if(sURLFile.startsWith("${user.home}/") || sURLFile.startsWith("${user.home}\\")) {
        String sUserHome = System.getProperty("user.home");
        sURLFile = sUserHome + File.separator + sURLFile.substring(13).replace('\\', File.separatorChar);
      }
      return sURLFile;
    }
    return null;
  }
  
  public 
  String getExtensionFile(int iIdMultimedia) 
  {
    if(mapMultimedia_Id_Ext != null) {
      String sExt = (String) mapMultimedia_Id_Ext.get(new Integer(iIdMultimedia));
      if(sExt != null && sExt.length() > 0) {
        return sExt.toLowerCase();
      }
    }
    return "";
  }
  
  public 
  String getTitleFile(int iIdMultimedia) 
  {
    if(mapMultimedia_Id_Tit != null) {
      String sTitle = (String) mapMultimedia_Id_Tit.get(new Integer(iIdMultimedia));
      if(sTitle != null && sTitle.length() > 0) {
        int iSep = sTitle.indexOf('|');
        if(iSep == 0) return "";
        if(iSep > 0) {
          return sTitle.substring(0, iSep);
        }
        else {
          return sTitle;
        }
      }
    }
    return "";
  }
  
  public 
  String getURLLinkFile(int iIdMultimedia) 
  {
    if(mapMultimedia_Id_Tit != null) {
      String sTitle = (String) mapMultimedia_Id_Tit.get(new Integer(iIdMultimedia));
      if(sTitle != null && sTitle.length() > 0) {
        if(sTitle.startsWith("http://")  || sTitle.startsWith("https://") || 
            sTitle.startsWith("ftp://")   || sTitle.startsWith("mailto://")) {
          return sTitle;
        }
        int iSep = sTitle.indexOf('|');
        if(iSep >= 0 && iSep < sTitle.length() - 1) {
          return sTitle.substring(iSep + 1);
        }
        else {
          return null;
        }
      }
    }
    return null;
  }
  
  @Override
  public int hashCode() {
    return id;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Article)) return false;
    int objId = ((Article) obj).getId();
    return objId == id;
  }
  
  @Override
  public String toString() {
    return String.valueOf(id);
  }
  
  // Utilities
  
  public static 
  List<Article> toListOfArticle(List<Map<String, Object>> list, int idLang) 
  {
    int size = list != null ? list.size() : 0;
    List<Article> listResult = new ArrayList<Article>(size);
    if(size == 0) return listResult;
    for(int i = 0; i < size; i++) {
      listResult.add(new Article(list.get(i), idLang));
    }
    return listResult;
  }
  
  public static
  String getExtension(String filePath)
  {
    if(filePath == null || filePath.length() == 0) {
      return "";
    }
    String result = "";
    int sep = filePath.lastIndexOf('.');
    if(sep >= 0 && sep < filePath.length() - 1) {
      result = filePath.substring(sep + 1).toLowerCase();
    }
    return result;
  }
  
  public static
  String getTitleByLang(Object titles, int language, String defaultTitle)
  {
    String result = null;
    Map<String, Object> mapTitles = WUtil.toMapObject(titles, false);
    if(mapTitles != null) {
      result = WUtil.toString(mapTitles.get(String.valueOf(language)), null);
      if(result == null || result.length() == 0) {
        result = WUtil.toString(mapTitles.get("0"), null);
      }
    }
    if(result == null || result.length() == 0) {
      return defaultTitle;
    }
    return result;
  }
  
  protected
  String format(String sText, int iMaxLength, String sAfterCut, boolean boRemoveBR, String sClassAnchor, String sTagImgSx, String sTagImgDx, boolean boPreview)
  {
    if(sText == null || sText.length() == 0) return "";
    if(sClassAnchor != null && sClassAnchor.length() > 0) {
      sText = sText.replace("<a ", "<a class=\"" + sClassAnchor + "\" ");
    }
    
    List<String> listImgSx = new ArrayList<String>();
    List<String> listImgDx = new ArrayList<String>();
    List<String> listTag   = new ArrayList<String>();
    boolean boStartImg   = false;
    boolean boStartTag   = false;
    boolean boStartImgSx = false;
    boolean boStartImgDx = false;
    boolean boStartUL    = false;
    boolean boStartLI    = false;
    
    StringBuilder sbImg = null;
    StringBuilder sbTag = null;
    int iActualLength = 0;
    int iCutAt = 0;
    int iTextLength = sText.length();
    Map<Integer,String> mapTagToInsert = new HashMap<Integer,String>();
    for(int i = 0; i < iTextLength; i++) {
      char c = sText.charAt(i);
      if(c == '<') {
        boStartTag   = true;
        sbTag = new StringBuilder();
      }
      else if(c == '{') {
        boStartImg   = true;
        sbImg = new StringBuilder();
      }
      else if(boStartTag && c == '>') {
        boStartTag  = false;
        if(iCutAt == 0) {
          String sTag = sbTag.toString();
          int iSepAttr = sTag.indexOf(' ');
          if(iSepAttr > 0) sTag = sTag.substring(0, iSepAttr);
          if(sTag.length() > 0 && sTag.charAt(0) == '/') {
            listTag.remove(sTag.substring(1));
          }
          else {
            if(sTag.equals("br")) {
              if(i < iTextLength - 1) {
                char c1 = sText.charAt(i + 1);
                if(c1 == '*') {
                  if(boStartUL) {
                    mapTagToInsert.put(i + 1, "</li><li>");
                  }
                  else {
                    boStartUL = true;
                    mapTagToInsert.put(i + 1, "<ul class=\"list\"><li>");
                  }
                }
                else {
                  if(boStartUL) {
                    boStartUL = false;
                    mapTagToInsert.put(i, "</li></ul>");
                  }
                }
              }
              else {
                if(boStartUL) {
                  boStartUL = false;
                  mapTagToInsert.put(i, "</li></ul>");
                }
              }
            }
            else {
              listTag.add(sTag);
            }
          }
        }
      }
      else
        if(boStartImg && c == '$') {
          boStartImgSx = true;
        }
        else if(boStartImg && c == '#') {
          boStartImgDx = true;
        }
        else if(boStartImg && c == '}') {
          if(boStartImgSx && sbImg.length() > 0) {
            listImgSx.add(sbImg.toString());
          }
          if(boStartImgDx && sbImg.length() > 0) {
            listImgDx.add(sbImg.toString());
          }
          boStartImg   = false;
          boStartImgSx = false;
          boStartImgDx = false;
        }
        else if(boStartTag) {
          sbTag.append(c);
        }
        else if(boStartImg) {
          sbImg.append(c);
        }
        else {
          iActualLength++;
          if(iMaxLength > 0 && iCutAt == 0 && iActualLength >= iMaxLength) {
            if(!Character.isLetter(c) && c != '&') {
              iCutAt = i;
            }
          }
        }
    }
    if(boStartUL) {
      boStartUL = false;
      mapTagToInsert.put(new Integer(iTextLength-1), "</li></ul>");
    }
    if(iCutAt > 0) {
      sText = sText.substring(0, iCutAt+1);
      int iTagSize = listTag.size();
      if(iTagSize > 0) {
        for(int i = iTagSize-1; i >= 0; i--) {
          String sTag = (String) listTag.get(i);
          sText += "</" + sTag + ">";
        }
      }
      if(sAfterCut != null && sAfterCut.length() > 0) {
        sText += " " + sAfterCut;
      }
    }
    if(mapTagToInsert != null && !mapTagToInsert.isEmpty()) {
      iTextLength = sText.length();
      int iEnd = iCutAt > 0 ? iCutAt : iTextLength;
      Set<Integer> keySet = mapTagToInsert.keySet();
      boStartUL = false;
      boStartLI = false;
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < iTextLength; i++) {
        if(i < iEnd) {
          Integer oIndex = new Integer(i);
          if(keySet.contains(oIndex)) {
            String sToInsert = mapTagToInsert.get(oIndex);
            char c = sText.charAt(i);
            if(c == '*') {
              if(i > 4) {
                String sSub = sb.substring(sb.length()-4, sb.length());
                if(sSub.equals("<br>")) {
                  sb.delete(sb.length()-4, sb.length());
                }
              }
              sb.append(sToInsert);
            }
            else {
              sb.append(c);
              sb.append(sToInsert);
            }
            if(sToInsert.indexOf("<ul")  >= 0) boStartUL = true;
            else if(sToInsert.indexOf("<li")  >= 0) boStartLI = true;
            else if(sToInsert.indexOf("</ul") >= 0) boStartUL = false;
            else if(sToInsert.indexOf("</li") >= 0) boStartLI = false;
          }
          else {
            sb.append(sText.charAt(i));
          }
        }
        else {
          sb.append(sText.charAt(i));
        }
      }
      if(boStartLI) sb.append("</li>");
      if(boStartUL) sb.append("</ul>");
      sText = sb.toString();
    }
    for(int i = 0; i < listImgSx.size(); i++) {
      String sImgSx = (String) listImgSx.get(i);
      int iImg = 0;
      try{ iImg = Integer.parseInt(sImgSx); } catch(Exception ex) {}
      if(iImg < 1 || photos == null || iImg > photos.length) {
        sText = sText.replace("{$" + sImgSx + "}", "");
        continue;
      }
      if(sTagImgSx == null || sTagImgSx.length() < 3) {
        sText = sText.replace("{$" + sImgSx + "}", "");
        continue;
      }
      String sTag = null;
      if(boPreview) {
        sTag = sTagImgSx.replace("#", getURLPreview(photos[iImg-1]));
      }
      else {
        sTag = sTagImgSx.replace("#", getURLFile(photos[iImg-1]));
      }
      sText = sText.replace("{$" + sImgSx + "}", sTag);
    }
    for(int i = 0; i < listImgDx.size(); i++) {
      String sImgDx = (String) listImgDx.get(i);
      int iImg = 0;
      try{ iImg = Integer.parseInt(sImgDx); } catch(Exception ex) {}
      if(iImg < 1 || photos == null || iImg > photos.length) {
        sText = sText.replace("{#" + sImgDx + "}", "");
        continue;
      }
      if(sTagImgSx == null || sTagImgSx.length() < 3) {
        sText = sText.replace("{#" + sImgDx + "}", "");
        continue;
      }
      String sTag = null;
      if(boPreview) {
        sTag = sTagImgDx.replace("#", getURLPreview(photos[iImg-1]));
      }
      else {
        sTag = sTagImgDx.replace("#", getURLFile(photos[iImg-1]));
      }
      sText = sText.replace("{#" + iImg + "}", sTag);
    }
    if(boRemoveBR) {
      sText = sText.replace("<br>", " ");
    }
    return sText;
  }
}
