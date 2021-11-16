package org.dew.cms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dew.cms.backend.Article;
import org.dew.cms.backend.Multimedia;
import org.dew.cms.backend.Page;
import org.dew.cms.backend.Tag;
import org.dew.cms.backend.User;

import org.dew.cms.backend.util.CMSCache;

import org.dew.cms.backend.ws.WSArticoli;
import org.dew.cms.backend.ws.WSCategorie;
import org.dew.cms.backend.ws.WSComuni;
import org.dew.cms.backend.ws.WSPagine;
import org.dew.cms.backend.ws.WSPortale;
import org.dew.cms.backend.ws.WSSottocategorie;
import org.dew.cms.backend.ws.WSTag;
import org.dew.cms.backend.ws.WSTipiArticolo;
import org.dew.cms.backend.ws.WSUtenti;

/**
 * CMS proxy. 
 */
public 
class CMS 
{
  public final static String SESS_USER_LOGGED  = "org.dew.cms.userLogged";
  public final static String SESS_LAST_ARTICLE = "org.dew.cms.lastArticle";
  public final static String SESS_LANGUAGE     = "org.dew.cms.language";
  
  public static String PAGE_INDEX      = "index.jsp";
  public static String PAR_PAGE        = "p";
  public static String PAR_CATEGORY    = "c";
  public static String PAR_SUBCATEGORY = "s";
  public static String PAR_TYPE        = "t";
  public static String PAR_ARTICLE     = "a";
  public static String PAR_CAP         = "z";
  public static String PAR_TAG         = "g";
  public static String PAR_TEXT        = "x";
  
  /**
   * Svuota la cache.
   */
  public static
  void clearCache()
  {
    CMSCache.clear();
  }
  
  /**
   * Restituisce l'identificativo CMS della lingua secondo il parametro specificato.
   * 
   * @param sLanguage Lingua
   * @return int
   */
  public static
  int getLanguage(String sLanguage)
  {
    if(sLanguage == null || sLanguage.length() == 0) return 0;
    if(sLanguage.equalsIgnoreCase("it")) return 0;
    if(sLanguage.equalsIgnoreCase("en")) return 1;
    if(sLanguage.equalsIgnoreCase("fr")) return 2;
    if(sLanguage.equalsIgnoreCase("de")) return 3;
    if(sLanguage.equalsIgnoreCase("es")) return 4;
    return 0;
  }
  
  /**
   * Restituisce l'identificativo della lingua secondo il parametro specificato.
   * 
   * @param locale Oggetto Locale
   * @return Identificativo della lingua
   */
  public static
  int getLanguage(Locale locale)
  {
    if(locale == null) return 0;
    String sLanguage = locale.getLanguage();
    if(sLanguage == null || sLanguage.length() == 0) return 0;
    if(sLanguage.equalsIgnoreCase("it")) return 0;
    if(sLanguage.equalsIgnoreCase("en")) return 1;
    if(sLanguage.equalsIgnoreCase("fr")) return 2;
    if(sLanguage.equalsIgnoreCase("de")) return 3;
    if(sLanguage.equalsIgnoreCase("es")) return 4;
    return 0;
  }
  
  /**
   * Restituisce la rappresentazione stringa del Locale corrispondente alla lingua specificata.
   * 
   * @param iIdLanguage Lingua
   * @return Locale
   */
  public static
  String getLocale(int iIdLanguage)
  {
    switch (iIdLanguage) {
    case 0: return "it_IT";
    case 1: return "en_GB";
    case 2: return "fr_FR";
    case 3: return "de_DE";
    case 4: return "es_ES";
    }
    return "it_IT";
  }
  
  /**
   * Restituisce l'identificativo della lingua secondo la richiesta descritta da HttpServletRequest.
   * Viene restituito prima l'identificativo della lingua riportato in sessione (CMS.sSESS_LANGUAGE).
   * 
   * @param request Oggetto HttpServletRequest
   * @return Identificativo della lingua
   */
  public static
  int getLanguage(HttpServletRequest request)
  {
    HttpSession httpSession = request.getSession(false);
    Object oLanguage = null;
    if(httpSession != null) {
      oLanguage = httpSession.getAttribute(CMS.SESS_LANGUAGE);
    }
    if(oLanguage == null) {
      return getLanguage(request.getLocale());
    }
    else if(oLanguage instanceof Number) {
      return ((Number) oLanguage).intValue();
    }
    else if(oLanguage instanceof Locale) {
        return getLanguage((Locale) oLanguage);
    }
    return getLanguage(oLanguage.toString());
  }
  
  /**
   * Restituisce l'identificativo memorizzato in HttpSession.
   * 
   * @param httpSession Oggetto HttpSession
   * @return Identificativo della lingua
   */
  public static
  int getLanguage(HttpSession httpSession)
  {
    Object oLanguage = null;
    if(httpSession != null) {
      oLanguage = httpSession.getAttribute(CMS.SESS_LANGUAGE);
    }
    if(oLanguage == null) {
      return 0;
    }
    else if(oLanguage instanceof Number) {
      return ((Number) oLanguage).intValue();
    }
    else if(oLanguage instanceof Locale) {
      return getLanguage((Locale) oLanguage);
    }
    return getLanguage(oLanguage.toString());
  }
  
  /**
   * Imposta la variabile di sessione CMS.sSESS_LANGUAGE per la memorizzazione della lingua.
   * 
   * @param request Oggetto HttpServletRequest
   * @param iIdLanguage Identificativo della lingua
   */
  public static
  void setLanguage(HttpServletRequest request, int iIdLanguage)
  {
    HttpSession httpSession = request.getSession(false);
    if(httpSession != null) {
      httpSession.setAttribute(CMS.SESS_LANGUAGE, new Integer(iIdLanguage));
    }
  }
  
  /**
   * Verifica le credenziali di accesso.
   * 
   * @param sUsername Utente
   * @param sPassword Passowrd
   * @return User (null se il controllo non e' passato)
   * @throws Exception
   */
  public static
  User check(String sUsername, String sPassword)
      throws Exception
  {
    Map<String, Object> map = WSUtenti.check(sUsername, sPassword);
    if(map == null || map.isEmpty()) return null;
    return new User(map);
  }
  
  /**
   * Legge le informazioni dell'utente identificato dallo username.
   * 
   * @param iId Identificativo utente
   * @return User (null se l'Id non esiste)
   * @throws Exception
   */
  public static
  User readUser(int iId)
      throws Exception
  {
    if(iId == 0) return null;
    Map<String, Object> map = WSUtenti.read(iId);
    if(map == null || map.isEmpty()) return null;
    return new User(map);
  }
  
  /**
   * Legge le informazioni dell'utente identificato dallo username.
   * 
   * @param sUsername Utente
   * @return User (null se lo username non esiste)
   * @throws Exception
   */
  public static
  User readUser(String sUsername)
      throws Exception
  {
    if(sUsername == null || sUsername.length() == 0) return null;
    Map<String, Object> map = WSUtenti.read(sUsername);
    if(map == null || map.isEmpty()) return null;
    return new User(map);
  }
  
  /**
   * Legge le informazioni dell'utente identificato dalla email.
   * 
   * @param sEmail Email dell'utente
   * @return User (null se lo username non esiste)
   * @throws Exception
   */
  public static
  User readUserByEmail(String sEmail)
      throws Exception
  {
    Map<String, Object> map = WSUtenti.readByEmail(sEmail);
    if(map == null || map.isEmpty()) return null;
    return new User(map);
  }
  
  /**
   * Registra un nuovo utente.
   * 
   * @param user Utente
   * @return Id Utente (0 se la Username esiste, -1 registrazione rifiutata) 
   * @throws Exception
   */
  public static
  int register(User user)
      throws Exception
  {
    return WSUtenti.register(user);
  }
  
  /**
   * Aggiorna il profilo di un utente.
   *
   * @param user Utente
   * @return Id Utente (0 se la Username non esiste, -1 aggiornamento rifiutato) 
   * @throws Exception
   */
  public static
  int update(User user)
      throws Exception
  {
    return WSUtenti.update(user);
  }
  
  /**
   * Legge la pagina identificata dal primo parametro.
   * 
   * @param iIdPage Identificativo della pagina
   * @param iIdLanguage Lingua
   * @return oggetto Page
   * @throws Exception
   */ 
  public static
  Page getPage(int iIdPage, int iIdLanguage)
      throws Exception
  {
    Map<String, Object> mapResult = WSPagine.getPagina(iIdPage, iIdLanguage);
    return new Page(mapResult, iIdLanguage);
  }
  
  /**
   * Legge la pagina identificata dal codice mnemonico.
   * 
   * @param sCode Codice mnemonico della pagina
   * @param iIdLanguage Lingua
   * @return oggetto Page
   * @throws Exception
   */ 
  public static
  Page getPage(String sCode, int iIdLanguage)
      throws Exception
  {
    Map<String, Object> mapResult = WSPagine.getPagina(sCode, iIdLanguage);
    return new Page(mapResult, iIdLanguage);
  }
  
  /**
   * Legge la pagina Home.
   * 
   * @param iIdLanguage Lingua
   * @return oggetto Page
   * @throws Exception
   */
  public static
  Page getHomePage(int iIdLanguage)
      throws Exception
  {
    if(CMSCache.home != null) return CMSCache.home;
    Map<String, Object> mapResult = WSPagine.getPagina(1, iIdLanguage);
    Page page = new Page(mapResult, iIdLanguage);
    CMSCache.home = page;
    return page;
  }
  
  /**
   * Legge la pagina View.
   * 
   * @param iIdLanguage Lingua
   * @return oggetto Page
   * @throws Exception
   */
  public static
  Page getViewPage(int iIdLanguage)
      throws Exception
  {
    if(CMSCache.view != null) return CMSCache.view;
    Map<String, Object> mapResult = WSPagine.getPagina(2, iIdLanguage);
    Page page = new Page(mapResult, iIdLanguage);
    CMSCache.view = page;
    return page;
  }
  
  /**
   * Restituisce le pagine pubbliche.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getPublicPages(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_PAGINA_PUBBLICA, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce le pagine private.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getPrivatePages(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_PAGINA_PRIVATA, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce il menu intestazione.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getMenuHeader(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_MENU_INTESTAZIONE, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce il menu di navigazione.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getMenuNavigation(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_MENU_NAVIGAZIONE, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce il menu a pie' pagina.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getMenuFooter(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_MENU_FOOTER, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce le citazioni.
   * 
   * @param iIdLanguage Lingua
   * @return lista di Page
   * @throws Exception
   */
  public static
  List<Page> getQuotePages(int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSPagine.getPagine(WSPagine.iTIPO_CITAZIONE, iIdLanguage);
    return Page.toListOfPage(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce tutti i comuni interessati nel portale.
   * 
   * @return Comuni List di List con 0=Id,1=Descrizione
   * @throws Exception
   */
  public static
  List<List<Object>> getCities()
      throws Exception
  {
    return WSComuni.getComuni();
  }
  
  /**
   * Restituisce tutti i comuni interessati nel portale della provincia specificata.
   * 
   * @param sProv Sigla della provincia
   * @return Comuni List di List con 0=Id,1=Descrizione
   * @throws Exception
   */
  public static
  List<List<Object>> getCities(String sProv)
      throws Exception
  {
    return WSComuni.getComuni(sProv);
  }
  
  /**
   * Restituisce tutte le categorie.
   * 
   * @return Categorie List di List con 0=Id,1=Codice
   * @throws Exception
   */
  public static
  List<List<Object>> getCategories()
      throws Exception
  {
    return WSPortale.getCategorie(-1, true);
  }
  
  /**
   * Restituisce gli identificativi delle categorie specificate rispettando l'ordine.
   * 
   * @param asCodes Codici categoria
   * @return array di Id Categoria
   * @throws Exception
   */
  public static
  int[] getCategories(String... asCodes)
      throws Exception
  {
    if(asCodes == null || asCodes.length == 0) return new int[0];
    Map<String, Integer> mapCategorie = WSPortale.getMapCategorie();
    List<Number> listOfId = new ArrayList<Number>(asCodes.length);
    for(int i = 0; i < asCodes.length; i++) {
      Number oId = (Number) mapCategorie.get(asCodes[i]);
      if(oId == null) continue;
      listOfId.add(oId);
    }
    int[] aiResult = new int[listOfId.size()];
    for(int i = 0; i < listOfId.size(); i++) {
      aiResult[i] = listOfId.get(i).intValue();
    }
    return aiResult;
  }
  
  /**
   * Restituisce tutte le categorie con la descrizione in lingua.
   * 
   * @param iIdLanguage Lingua
   * @return Categorie List di List con 0=Id,1=Descrizione
   * @throws Exception
   */
  public static
  List<List<Object>> getCategories(int iIdLanguage)
      throws Exception
  {
    return WSPortale.getCategorie(iIdLanguage, true);
  }
  
  /**
   * Restituisce tutte le sottocategorie sotto forma di Mappa dove la chiave e' la Categoria (Rappresentazione String dell'Id).
   * 
   * @return Sottocategorie Map<String,List> key=IdCategoria.toString(),value=List di List con 0=Id,1=Codice
   * @throws Exception
   */
  public static
  Map<String, List<Object>> getSubcategories()
      throws Exception
  {
    return WSPortale.getSottoCategorie(-1, true);
  }	
  
  /**
   * Restituisce tutti tipi articolo.
   * 
   * @return Tipi articolo List di List con 0=Id,1=Codice
   * @throws Exception
   */
  public static
  List<List<Object>> getTypes()
      throws Exception
  {
    return WSPortale.getTipologie(-1, true);
  }
  
  /**
   * Restituisce tutti tipi articolo.
   * 
   * @param iIdLanguage Lingua
   * @return Tipi articolo List di List con 0=Id,1=Descrizione
   * @throws Exception
   */
  public static
  List<List<Object>> getTypes(int iIdLanguage)
      throws Exception
  {
    return WSPortale.getTipologie(iIdLanguage, true);
  }
  
  /**
   * Restituisce tutte le sottocategorie per Categoria.
   * 
   * @param iIdCategory Categoria
   * @return Sottocategorie List di List con 0=Id,1=Codice
   * @throws Exception
   */
  public static
  List<List<Object>> getSubcategories(int iIdCategory)
      throws Exception
  {
    return WSPortale.getSottoCategorie(-1, iIdCategory, true);
  }	
  
  /**
   * Restituisce tutte le sottocategorie per Categoria.
   * 
   * @param iIdLanguage Lingua
   * @param iIdCategory Categoria
   * @return Sottocategorie List di List con 0=Id,1=Descrizione
   * @throws Exception
   */
  public static
  List<List<Object>> getSubcategories(int iIdCategory, int iIdLanguage)
      throws Exception
  {
    return WSPortale.getSottoCategorie(iIdLanguage, iIdCategory, true);
  }	
  
  /**
   * Restituisce la descrizione della categoria. Utilizza la cache.
   * 
   * @param iIdCategory Categoria
   * @param iIdLanguage Lingua
   * @return Descrizione della categoria
   * @throws Exception
   */
  public static
  String getCategory(int iIdCategory, int iIdLanguage)
      throws Exception
  {
    switch(iIdCategory) {
    case  0: return "";
    case -1: return iIdLanguage == 0 ? "CMS"       : "CMS";
    case -2: return iIdLanguage == 0 ? "contatti"  : "contacts";
    case -3: return iIdLanguage == 0 ? "crediti"   : "credits";
    case -4: return iIdLanguage == 0 ? "chi siamo" : "about us";
    case -5: return iIdLanguage == 0 ? "progetto"  : "project";
    }
    String sResult = null;
    if(CMSCache.mapDescCat != null) {
      sResult = CMSCache.mapDescCat.get(iIdCategory + ":" + iIdLanguage);
    }
    if(sResult == null) {
      sResult = WSCategorie.getDescrizione(iIdCategory, iIdLanguage);
      CMSCache.mapDescCat.put(iIdCategory + ":" + iIdLanguage, sResult);
    }
    return sResult;
  }
  
  /**
   * Restituisce la descrizione della sottocategoria. Utilizza la cache.
   * 
   * @param iIdSubcategory Sottocategoria
   * @param iIdLanguage Lingua
   * @return Descrizione della categoria
   * @throws Exception
   */
  public static
  String getSubcategory(int iIdSubcategory, int iIdLanguage)
      throws Exception
  {
    String sResult = null;
    if(CMSCache.mapDescSot != null) {
      sResult = CMSCache.mapDescSot.get(iIdSubcategory + ":" + iIdLanguage);
    }
    if(sResult == null) {
      sResult = WSSottocategorie.getDescrizione(iIdSubcategory, iIdLanguage);
      CMSCache.mapDescSot.put(iIdSubcategory + ":" + iIdLanguage, sResult);
    }
    return sResult;
  }
  
  /**
   * Restituisce la descrizione del tipo articolo. Utilizza la cache.
   * 
   * @param iIdType Tipo articolo
   * @param iIdLanguage Lingua
   * @return Descrizione del tipo articolo
   * @throws Exception
   */
  public static
  String getType(int iIdType, int iIdLanguage)
      throws Exception
  {
    if(iIdType == 0) return "";
    String sResult = null;
    if(CMSCache.mapDescTip != null) {
      sResult = CMSCache.mapDescTip.get(iIdType + ":" + iIdLanguage);
    }
    if(sResult == null) {
      sResult = WSTipiArticolo.getDescription(iIdType, iIdLanguage);
      CMSCache.mapDescTip.put(iIdType + ":" + iIdLanguage, sResult);
    }
    return sResult;
  }
  
  /**
   * Restituisce tutti i tag. Utilizza la cache.
   * 
   * @param iIdLanguage Lingua
   * @return List of Tag
   * @throws Exception
   */
  public static
  List<Tag> getTags(int iIdLanguage)
      throws Exception
  {
    List<Tag> listResult = null;
    if(CMSCache.mapTags != null) {
      listResult = CMSCache.mapTags.get(String.valueOf(iIdLanguage));
    }
    if(listResult == null) {
      listResult = Tag.toListOfTag(WSPortale.getTags(iIdLanguage, true, false), iIdLanguage);
      CMSCache.mapTags.put(String.valueOf(iIdLanguage), listResult);
    }
    return listResult;
  }
  
  /**
   * Restituisce i tag per descrizione
   * 
   * @param iIdLanguage Lingua
   * @param sDescription Descrizione
   * @return List of Tag
   * @throws Exception
   */
  public static
  List<Tag> getTags(int iIdLanguage, String sDescription)
      throws Exception
  {
    List<Integer> listResult = WSTag.find(iIdLanguage, sDescription);
    return Tag.toListOfTag(listResult, iIdLanguage, sDescription);
  }
  
  /**
   * Restituisce tutte le descrizioni negli articoli corrispondenti al tag specificato.
   * 
   * @param iIdTag Identificativo tag
   * @param iIdLanguage Lingua
   * @return List of String
   * @throws Exception
   */
  public static
  List<String> getTagOptions(int iIdTag, int iIdLanguage)
      throws Exception
  {
    return WSPortale.getTagOptions(iIdTag, 0, iIdLanguage);
  }
  
  /**
   * Restituisce tutte le descrizioni negli articoli corrispondenti al tag specificato.
   * 
   * @param iIdTag Identificativo tag
   * @param iIdCategory Categoria
   * @param iIdLanguage Lingua
   * @return List of String
   * @throws Exception
   */
  public static
  List<String> getTagOptions(int iIdTag, int iIdCategory, int iIdLanguage)
      throws Exception
  {
    return WSPortale.getTagOptions(iIdTag, iIdCategory, iIdLanguage);
  }
  
  /**
   * Restituisce tutti i tag con visibilit&agrave; in anteprima. Utilizza la cache.
   * 
   * @param iIdLanguage Lingua
   * @return List of Tag
   * @throws Exception
   */
  public static
  List<Tag> getTagsInPreview(int iIdLanguage)
      throws Exception
  {
    List<Tag> listResult = null;
    if(CMSCache.mapTagsPrev != null) {
      listResult = CMSCache.mapTagsPrev.get(String.valueOf(iIdLanguage));
    }
    if(listResult == null) {
      listResult = Tag.toListOfTag(WSPortale.getTags(iIdLanguage, true, true), iIdLanguage);
      CMSCache.mapTagsPrev.put(String.valueOf(iIdLanguage), listResult);
    }
    return listResult;
  }
  
  /**
   * Restituisce tutti i tag completi associati all'articolo ordinati per codice.
   * 
   * @param article Articolo
   * @return List of Tag
   * @throws Exception
   */
  public static
  List<Tag> getTags(Article article)
      throws Exception
  {
    List<Tag> listResult = new ArrayList<Tag>();
    if(article == null) return listResult;
    List<String> listTags = article.getListTags();
    if(listTags == null) return listResult;
    for(int i = 0; i < listTags.size(); i++) {
      String sTag = listTags.get(i);
      Tag tag = getTag(sTag, article.getLanguage());
      String sDescTagInArt = article.getDescTag(sTag);
      if(sDescTagInArt != null && sDescTagInArt.length() > 0) {
        tag = (Tag) tag.clone();
        tag.setDisplayName(sDescTagInArt);
      }
      listResult.add(tag);
    }
    return listResult;
  }
  
  /**
   * Restituisce le informazioni relative al tag avente il codice specificato. Utilizza la cache.
   * 
   * @param sTag Codice mnemonico del tag
   * @param iIdLanguage Lingua
   * @return Oggetto Tag
   * @throws Exception
   */
  public static
  Tag getTag(String sTag, int iIdLanguage)
      throws Exception
  {
    if(sTag == null || sTag.length() == 0) return null;
    Tag tag = null;
    if(CMSCache.mapCodeTag != null) {
      tag = CMSCache.mapCodeTag.get(sTag + ":" + iIdLanguage);
    }
    if(tag == null) {
      Map<String, Object> mapResult = WSTag.read(sTag, iIdLanguage);
      if(mapResult == null || mapResult.isEmpty()) return null;
      tag = new Tag(mapResult);
      CMSCache.mapCodeTag.put(sTag + ":" + iIdLanguage, tag);
    }
    return tag;
  }
  
  /**
   * Restituisce gli anni disponibili relativi agli articoli della sottocategoria specificata. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @return Array contenente gli anni
   * @throws Exception
   */
  public static
  int[] getYears(int iIdCategory, int iIdSubcategory)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapYears != null) {
      listResult = CMSCache.mapYears.get(iIdCategory + ":" + iIdSubcategory + ":0:0");
    }
    if(listResult == null) {
      listResult = WSArticoli.getYears(iIdCategory, iIdSubcategory, 0, 0);
      CMSCache.mapYears.put(iIdCategory + ":" + iIdSubcategory + ":0:0", listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYear = number != null ? number.intValue() : 0;
      aiResult[i] = iYear;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce gli anni disponibili relativi agli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @return Array contenente gli anni
   * @throws Exception
   */
  public static
  int[] getYears(int iIdCategory, int iIdSubcategory, int iIdType)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapYears != null) {
      listResult = CMSCache.mapYears.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0");
    }
    if(listResult == null) {
      listResult = WSArticoli.getYears(iIdCategory, iIdSubcategory, iIdType, 0);
      CMSCache.mapYears.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0", listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYear = number != null ? number.intValue() : 0;
      aiResult[i] = iYear;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce gli anni disponibili relativi agli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @return Array contenente gli anni
   * @throws Exception
   */
  public static
  int[] getYears(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapYears != null) {
      listResult = CMSCache.mapYears.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType);
    }
    if(listResult == null) {
      listResult = WSArticoli.getYears(iIdCategory, iIdSubcategory, iIdType, iIdUserType);
      CMSCache.mapYears.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType, listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYear = number != null ? number.intValue() : 0;
      aiResult[i] = iYear;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce i mesi disponibili relativi agli articoli della sottocategoria specificata. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @return Array contenente i mesi (comprensivi di anno: 201301, 201302, ...)
   * @throws Exception
   */
  public static
  int[] getMonths(int iIdCategory, int iIdSubcategory)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapMonths != null) {
      listResult = CMSCache.mapMonths.get(iIdCategory + ":" + iIdSubcategory + ":0:0");
    }
    if(listResult == null) {
      listResult = WSArticoli.getMonths(iIdCategory, iIdSubcategory, 0, 0);
      CMSCache.mapMonths.put(iIdCategory + ":" + iIdSubcategory + ":0:0", listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYearMonth = number != null ? number.intValue() : 0;
      aiResult[i] = iYearMonth;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce i mesi disponibili relativi agli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @return Array contenente i mesi (comprensivi di anno)
   * @throws Exception
   */
  public static
  int[] getMonths(int iIdCategory, int iIdSubcategory, int iIdType)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapMonths != null) {
      listResult = CMSCache.mapMonths.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0");
    }
    if(listResult == null) {
      listResult = WSArticoli.getMonths(iIdCategory, iIdSubcategory, iIdType, 0);
      CMSCache.mapMonths.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0", listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYearMonth = number != null ? number.intValue() : 0;
      aiResult[i] = iYearMonth;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce i mesi disponibili relativi agli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @return Array contenente i mesi (comprensivi di anno)
   * @throws Exception
   */
  public static
  int[] getMonths(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType)
      throws Exception
  {
    List<Integer> listResult = null;
    if(CMSCache.mapMonths != null) {
      listResult = CMSCache.mapMonths.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType);
    }
    if(listResult == null) {
      listResult = WSArticoli.getMonths(iIdCategory, iIdSubcategory, iIdType, iIdUserType);
      CMSCache.mapMonths.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType, listResult);
    }
    if(listResult == null || listResult.size() == 0) return new int[0];
    int[] aiResult = new int[listResult.size()];
    for(int i = 0; i < listResult.size(); i++) {
      Number number = (Number) listResult.get(i);
      int iYearMonth = number != null ? number.intValue() : 0;
      aiResult[i] = iYearMonth;
    }
    return aiResult;
  }	
  
  /**
   * Restituisce gli articoli della categoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, 0, 0, 1, 1000, 0, false, true);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, 0, 0, 1, 1000, 0, false, true);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e dell'anno specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iYear Anno 
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByYear(int iIdCategory, int iIdSubcategory, int iYear, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYear, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e dell'anno specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iYear Anno
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByYear(int iIdCategory, int iIdSubcategory, int iYear, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYear, boCompounds);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e dell'anno specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo Articolo (valori negativi sono interpretati come "tranne")
   * @param iYear Anno
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByYear(int iIdCategory, int iIdSubcategory, int iIdType, int iYear, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iYear, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e dell'anno specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo Articolo (valori negativi sono interpretati come "tranne")
   * @param iYear Anno
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByYear(int iIdCategory, int iIdSubcategory, int iIdType, int iYear, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYear, boCompounds);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del mese (AAAAMM) specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iYearMonth AnnoMese
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByMonth(int iIdCategory, int iIdSubcategory, int iYearMonth, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYearMonth, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del mese (AAAAMM) specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iYearMonth AnnoMese
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByMonth(int iIdCategory, int iIdSubcategory, int iYearMonth, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYearMonth, boCompounds);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del mese (AAAAMM) specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo Articolo (valori negativi sono interpretati come "tranne")
   * @param iYearMonth AnnoMese
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByMonth(int iIdCategory, int iIdSubcategory, int iIdType, int iYearMonth, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iYearMonth, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del mese (AAAAMM) specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo Articolo (valori negativi sono interpretati come "tranne")
   * @param iYearMonth AnnoMese
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByMonth(int iIdCategory, int iIdSubcategory, int iIdType, int iYearMonth, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iYearMonth, boCompounds);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria specificata.
   * 
   * @param iIdCategory Categoria
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, false, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, false, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, false, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria e sottocategoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, false, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria e sottocategoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria e sottocategoria specificata.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria e del tipo specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria e del tipo specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, 0, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria, del tipo articolo e del tipo utente specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iIdUserType, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria, del tipo articolo e del tipo utente specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iIdUserType, 0, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria, del tipo articolo, del tipo utente e del tag specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iIdTag, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iIdUserType, iIdTag, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria, della sottocategoria, del tipo articolo, del tipo utente e del tag specificati.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iIdTag, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iIdUserType, iIdTag, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli del tag e del tipo utente specificati.
   * 
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param boCompounds true se solo articoli composti, false tutti
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByTag(int iIdTag, int iIdUserType, int iPage, int iArticlesPerPage, int iArticlesPrevPage, boolean boCompounds, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(0, 0, 0, iIdUserType, iIdTag, iPage, iArticlesPerPage, iArticlesPrevPage, boCompounds, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli individuati dalla query SQL specificata o dalla descrizione dell'articolo.
   * 
   * @param sDesc_Or_SQL Select SQL oppure ricerca in LIKE sulla descrizione ordinata per descrizione.
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (valori negativi non gestiti)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(String sDesc_Or_SQL, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sDesc_Or_SQL, iPage, iArticlesPerPage, iArticlesPrevPage, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli individuati dalla query SQL specificata o dalla descrizione dell'articolo.
   * 
   * @param sDesc_Or_SQL Select SQL oppure ricerca in LIKE sulla descrizione ordinata per descrizione.
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (valori negativi non gestiti)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> find(String sDesc_Or_SQL, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sDesc_Or_SQL, iPage, iArticlesPerPage, iArticlesPrevPage, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce i contenuti multimediali della categoria, della sottocategoria, del tipo articolo, del tipo utente specificati e del comune specificati.
   * 
   * @param iIdMultType Tipo contenuto multimediale (valori negativi sono interpretati come "tranne")
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iMultPerPage Contenuti multimediali per pagina
   * @param iMultPrevPage Contenuti multimediali della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di contenuti multimediali
   * @throws Exception
   */
  public static
  List<Multimedia> findMult(int iIdMultType, int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iIdTag, int iIdCity, int iPage, int iMultPerPage, int iMultPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.findMult(iIdMultType, iIdCategory, iIdSubcategory, iIdType, iIdUserType, iIdTag, iIdCity, iPage, iMultPerPage, iMultPrevPage, boDoNotCount);
    return Multimedia.toListOfMultimedia(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce i contenuti multimediali del tag e del tipo utente specificati e del comune specificati.
   * 
   * @param iIdMultType Tipo contenuto multimediale (valori negativi sono interpretati come "tranne")
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iPage Pagina
   * @param iMultPerPage Contenuti multimediali per pagina
   * @param iMultPrevPage Contenuti multimediali della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di contenuti multimediali
   * @throws Exception
   */
  public static
  List<Multimedia> findMultByTag(int iIdMultType, int iIdTag, int iIdUserType, int iPage, int iMultPerPage, int iMultPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.findMult(iIdMultType, 0, 0, 0, iIdUserType, iIdTag, 0, iPage, iMultPerPage, iMultPrevPage, boDoNotCount);
    return Multimedia.toListOfMultimedia(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della categoria e del comune specificato.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param sZipCode Zip Code
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByCity(int iIdCategory, int iIdCity, String sZipCode, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, iIdCity, sZipCode, iPage, iArticlesPerPage, iArticlesPrevPage, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }	
  
  /**
   * Restituisce gli articoli della categoria e del comune specificato.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param sZipCode Zip Code
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByCity(int iIdCategory, int iIdCity, String sZipCode, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, 0, 0, iIdCity, sZipCode, iPage, iArticlesPerPage, iArticlesPrevPage, boDoNotCount);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del comune specificato.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param sZipCode Zip Code
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByCity(int iIdCategory, int iIdSubcategory, int iIdCity, String sZipCode, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iIdCity, sZipCode, iPage, iArticlesPerPage, iArticlesPrevPage, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria e del comune specificato.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param sZipCode Zip Code
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByCity(int iIdCategory, int iIdSubcategory, int iIdCity, String sZipCode, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, 0, iIdCity, sZipCode, iPage, iArticlesPerPage, iArticlesPrevPage, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce gli articoli della sottocategoria, del tipo e del comune specificato.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdCity Identificativo citt&agrave; (valori negativi sono interpretati come "tranne")
   * @param sZipCode Zip Code
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina (se negativo si va per rottura di anno)
   * @param iArticlesPrevPage Articoli della pagina precedente
   * @param iIdLanguage Lingua
   * @param boDoNotCount Flag di ottimizzazione: non si effettua il conteggio
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> findByCity(int iIdCategory, int iIdSubcategory, int iIdType, int iIdCity, String sZipCode, int iPage, int iArticlesPerPage, int iArticlesPrevPage, int iIdLanguage, boolean boDoNotCount)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(iIdCategory, iIdSubcategory, iIdType, iIdCity, sZipCode, iPage, iArticlesPerPage, iArticlesPrevPage, false);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo.
   * 
   * @param sText Testo di ricerca
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, 0, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo.
   * 
   * @param sText Testo di ricerca
   * @param iPage Pagina
   * @param iIdType Tipo articolo
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, int iIdType, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, iIdType, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo.
   * 
   * @param sText Testo di ricerca
   * @param iPage Pagina
   * @param iIdType Tipo articolo
   * @param iIdType2 Tipo articolo 2
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, int iIdType, int iIdType2, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, iIdType, iIdType2, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo e dagli altri parametri.
   * 
   * @param sText Testo di ricerca
   * @param iPage Pagina
   * @param iIdType Tipo articolo
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, int iIdType, int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, null, iIdType, 0, iIdCategory, iIdSubcategory, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo e dagli altri parametri.
   * 
   * @param sText Testo di ricerca
   * @param iPage Pagina
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "da... tranne")
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo Utente (valori negativi sono interpretati come "fino a... tranne")
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, int iIdType, int iIdCategory, int iIdSubcategory, int iIdUserType, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, null, iIdType, 0, iIdCategory, iIdSubcategory, iIdUserType, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo e dai tag.
   * 
   * @param sText Testo di ricerca
   * @param vTags Elenco tag (Lista di Tag, Number o String)
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "da... tranne")
   * @param iIdType2 Tipo articolo 2 (valori negativi sono interpretati come "fino a... tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, List<?> vTags, int iIdType, int iIdType2, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, vTags, iIdType, iIdType2, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo e dai tag.
   * 
   * @param sText Testo di ricerca
   * @param vTags Elenco tag (List di Tag, Number o String)
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "da... tranne")
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, List<?> vTags, int iIdType, int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, vTags, iIdType, 0, iIdCategory, iIdSubcategory, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo e dai tag.
   * 
   * @param sText Testo di ricerca
   * @param vTags Elenco tag (Lista di Tag, Number o String)
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "da... tranne")
   * @param iIdType2 Tipo articolo 2 (valori negativi sono interpretati come "fino a... tranne")
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, List<?> vTags, int iIdType, int iIdType2, int iIdCategory, int iIdSubcategory, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, vTags, iIdType, iIdType2, iIdCategory, iIdSubcategory, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Cerca gli articoli secondo il criterio specificato dal testo, dai tag e appartenenti ad una categoria.
   * 
   * @param sText Testo di ricerca
   * @param vTags Elenco tag (Lista di Tag, Number o String)
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @param iIdLanguage Lingua
   * @return Lista di articoli
   * @throws Exception
   */
  public static
  List<Article> search(String sText, List<?> vTags, int iIdCategory, int iPage, int iArticlesPerPage, int iIdLanguage)
      throws Exception
  {
    List<Map<String, Object>> vResult = WSArticoli.find(sText, vTags, 0, 0, iIdCategory, 0, 0, iPage, iArticlesPerPage, iIdLanguage);
    return Article.toListOfArticle(vResult, iIdLanguage);
  }
  
  /**
   * Restituisce i contenuti multimediali contenuti nella lista degli articoli specificata.
   * 
   * @param listOfArticles Lista articoli
   * @param sMultFilter Filtro contenuti multimediali
   * @param iMaxPerArticle Numero massimo di contenuti per articolo
   * @return List di Multimedia
   */
  public static
  List<Multimedia> getMultimedia(List<Article> listOfArticles, String sMultFilter, int iMaxPerArticle)
  {
    List<Multimedia> listResult = new ArrayList<Multimedia>();
    if(listOfArticles == null || listOfArticles.size() == 0) {
      return listResult;
    }
    for(int i = 0; i < listOfArticles.size(); i++) {
      Article article = listOfArticles.get(i);
      Multimedia[] arrayOfMultimedia = article.getMultimedia(sMultFilter);
      if(arrayOfMultimedia == null || arrayOfMultimedia.length == 0) continue;
      int iMaxM = 0;
      if(iMaxPerArticle > 0) {
        iMaxM = iMaxPerArticle < arrayOfMultimedia.length ? iMaxPerArticle : arrayOfMultimedia.length;
      }
      else {
        iMaxM = arrayOfMultimedia.length;
      }
      for(int m = 0; m < iMaxM; m++) {
        listResult.add(arrayOfMultimedia[m]);
      }
    }
    return listResult;
  }
  
  /**
   * Legge l'articolo identificato dal parametro iId.
   * 
   * @param iId Identificativo
   * @param iIdLanguage Lingua
   * @return Articolo
   */
  public static
  Article read(int iId, int iIdLanguage)
      throws Exception
  {
    Map<String, Object> mapArticolo = WSArticoli.read(iId, false);
    if(mapArticolo == null || mapArticolo.isEmpty()) return null;
    return new Article(mapArticolo, iIdLanguage);
  }
  
  /**
   * Legge l'articolo identificato dal parametro iId e traccia nei log la visita.
   * 
   * @param iId Identificativo
   * @param iIdLanguage Lingua
   * @param iIdUser Identificativo utente
   * @return Articolo
   */
  public static
  Article read(int iId, int iIdLanguage, int iIdUser)
      throws Exception
  {
    Map<String, Object> mapArticolo = WSArticoli.read(iId, iIdUser);
    if(mapArticolo == null || mapArticolo.isEmpty()) return null;
    return new Article(mapArticolo, iIdLanguage);
  }
  
  /**
   * Aggiunge una preferenza positiva riguardo l'articolo specificato.
   * 
   * @param iId Identificativo dell'articolo
   * @throws Exception
   */
  public static
  int addLike(int iId)
      throws Exception
  {
    return WSArticoli.addLike(iId);
  }
  
  /**
   * Aggiunge una preferenza negativa riguardo l'articolo specificato.
   * 
   * @param iId Identificativo dell'articolo
   * @throws Exception
   */
  public static
  int addDontLike(int iId)
      throws Exception
  {
    return WSArticoli.addDontLike(iId);
  }
  
  /**
   * Restituisce il numero degli articoli della categoria specificata. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":0:0:0:0:");
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, 0, 0, 0, 0);
    CMSCache.mapCount.put(iIdCategory + ":0:0:0:0:", new Integer(iResult));
    return iResult;
  }
  
  /**
   * Restituisce il numero degli articoli della sottocategoria specificata. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory, int iIdSubcategory)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":" + iIdSubcategory + ":0:0:0:");
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, iIdSubcategory, 0, 0, 0);
    CMSCache.mapCount.put(iIdCategory + ":" + iIdSubcategory + ":0:0:0:", new Integer(iResult));
    return iResult;
  }
  
  /**
   * Restituisce il numero degli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory, int iIdSubcategory, int iIdType)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0:0:");
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, iIdSubcategory, iIdType, 0, 0);
    CMSCache.mapCount.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":0:0:", new Integer(iResult));
    return iResult;
  }
  
  /**
   * Restituisce il numero degli articoli della sottocategoria e del tipo specificati. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fina a... tranne")
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":0:");
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, iIdSubcategory, iIdType, iIdUserType, 0);
    CMSCache.mapCount.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":0:", new Integer(iResult));
    return iResult;
  }
  
  /**
   * Restituisce il numero degli articoli della sottocategoria, del tipo e aventi il tag specificato. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fina a... tranne")
   * @param iIdTag Tag (valori negativi sono interpretati come "tranne")
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iIdTag)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":#" + iIdTag);
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, iIdSubcategory, iIdType, iIdUserType, iIdTag);
    CMSCache.mapCount.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":#" + iIdTag, new Integer(iResult));
    return iResult;
  }
  
  /**
   * Restituisce il numero degli articoli della sottocategoria e della zona specificata. Utilizza la cache.
   * 
   * @param iIdCategory Categoria (valori negativi sono interpretati come "tranne")
   * @param iIdSubcategory Sottocategoria (valori negativi sono interpretati come "tranne")
   * @param iIdType Tipo articolo (valori negativi sono interpretati come "tranne")
   * @param iIdUserType Tipo utente (valori negativi sono interpretati come "fina a... tranne")
   * @param iIdCity Identificativo comune (valori negativi sono interpretati come "tranne")
   * @param sCap CAP
   * @return Conteggio articoli
   * @throws Exception
   */
  public static
  int count(int iIdCategory, int iIdSubcategory, int iIdType, int iIdUserType, int iIdCity, String sCap)
      throws Exception
  {
    int iResult = 0;
    if(CMSCache.mapCount != null) {
      if(sCap == null) sCap = "";
      Integer oCount = CMSCache.mapCount.get(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":" + iIdCity + ":" + sCap);
      if(oCount != null) return oCount.intValue();
    }
    iResult = WSArticoli.count(iIdCategory, iIdSubcategory, iIdType, iIdUserType, iIdCity, sCap);
    CMSCache.mapCount.put(iIdCategory + ":" + iIdSubcategory + ":" + iIdType + ":" + iIdUserType + ":" + iIdCity + ":" + sCap, new Integer(iResult));
    return iResult;
  }
  
  /**
   * Calcola il numero di pagine.
   * 
   * @param iCount Totale articoli
   * @param iArticlesPerPage Articoli per pagina
   * @return Numero di pagine
   */
  public static
  int computePages(int iCount, int iArticlesPerPage)
  {
    if(iCount < 1) return 0;
    if(iArticlesPerPage < 1) iArticlesPerPage = 1;
    int iModPages = iCount % iArticlesPerPage;
    int iPages = 0;
    if(iModPages == 0) {
      iPages = iCount / iArticlesPerPage;
    }
    else {
      iPages = iCount / iArticlesPerPage + 1;
    }
    return iPages;
  }
  
  /**
   * Filtra i tag passati per il prefisso specificato.
   * 
   * @param tags Lista tag
   * @param sPrefix Prefisso
   * @return Lista tag filtrati
   */
  public static
  List<Tag> filterTagsByPrefix(List<Tag> tags, String sPrefix)
  {
    if(tags == null || tags.size() == 0) return tags;
    List<Tag> listResult = new ArrayList<Tag>();
    for(int i = 0; i < tags.size(); i++) {
      Tag tag = tags.get(i);
      String sCode = tag.getCode();
      if(sCode == null || !sCode.startsWith(sPrefix)) continue;
      listResult.add(tag);
    }
    return listResult;
  }
  
  /**
   * Costruisce un oggetto HashSet con i prefissi dei tag passati.
   * 
   * @param tags Lista di tag
   * @return HashSet di prefissi
   */
  public static
  List<String> getPrefixOf(List<Tag> tags)
  {
    List<String> listResult = new ArrayList<String>();
    if(tags == null || tags.size() == 0) return listResult;
    for(int i = 0; i < tags.size(); i++) {
      Tag tag = tags.get(i);
      String sCode = tag.getCode();
      if(sCode == null) continue;
      int iSep = sCode.indexOf('_');
      String sPrefix = null;
      if(iSep > 0) {
        sPrefix = sCode.substring(0, iSep + 1);
      }
      else {
        sPrefix = sCode;
      }
      if(!listResult.contains(sPrefix)) {
        listResult.add(sPrefix);
      }
    }		
    return listResult;
  }
  
  /**
   * Combina i criteri di ricerca
   * 
   * @param sTagFilter Filtro tags
   * @param sText Testo di ricerca
   * @return Filtro di ricerca combinato.
   */
  public static
  String mergeSearchFilter(String sTagFilter, String sText)
  {
    String sResult = "";
    if(sTagFilter != null && sTagFilter.length() > 0) {
      sResult += sTagFilter;
    }
    if(sText != null && sText.length() > 0) {
      sResult += WSArticoli.getLikeFilter(sText);
    }
    return sResult;
  }
  
  /**
   * Di una lista di articoli seleziona una pagina.
   * 
   * @param articles Lista articoli
   * @param iPage Pagina
   * @param iArticlesPerPage Articoli per pagina
   * @return Lista di articoli selezionata
   */
  public static
  List<Article> getPage(List<Article> articles, int iPage, int iArticlesPerPage)
  {
    if(articles == null || articles.size() == 0) return articles;
    if(iPage < 1) iPage = 1;
    if(iArticlesPerPage < 1) iArticlesPerPage = 1000;
    int iMinIndex = (iPage - 1) * iArticlesPerPage;
    int iMaxIndex = iMinIndex + iArticlesPerPage - 1;
    List<Article> listResult = new ArrayList<Article>();
    int i = 0;
    for(i = 0; i < articles.size(); i++) {
      if(i < iMinIndex) continue;
      if(i > iMaxIndex) continue;
      listResult.add(articles.get(i));
    }
    if(listResult != null && listResult.size() > 0) {
      int iModPages = articles.size() % iArticlesPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = articles.size() / iArticlesPerPage;
      }
      else {
        iPages = articles.size() / iArticlesPerPage + 1;
      }
      Article a0 = (Article) listResult.get(0);
      a0.setPage(iPage);
      a0.setPages(iPages);
      a0.setCount(articles.size());
    }
    return listResult;
  }
  
  /**
   * Restituisce il primo articolo della pagina specificata.
   * Se la pagina &egrave; inesistente o non contiene articoli, viene restituito un articolo vuoto.
   * 
   * @param iIdPage Pagina
   * @param iIdLanguage Lingua
   * @return Articolo
   * @throws Exception
   */
  public static
  Article readFirstArticle(int iIdPage, int iIdLanguage) 
      throws Exception 
  {
    Page page = getPage(iIdPage, iIdLanguage);
    if(page != null) {
      int iFirstArticle = page.getFirstArticle();
      if(iFirstArticle == 0) return new Article();
      return read(iFirstArticle, iIdLanguage);
    }
    return new Article();
  }
  
  /**
   * Restituisce l'ultimo articolo della pagina specificata.
   * Se la pagina &egrave; inesistente o non contiene articoli, viene restituito un articolo vuoto.
   * 
   * @param iIdPage Pagina
   * @param iIdLanguage Lingua
   * @return Articolo
   * @throws Exception
   */
  public static
  Article readLastArticle(int iIdPage, int iIdLanguage) 
      throws Exception 
  {
    Page page = getPage(iIdPage, iIdLanguage);
    if(page != null) {
      int iLastArticle = page.getLastArticle();
      if(iLastArticle == 0) return new Article();
      return read(iLastArticle, iIdLanguage);
    }
    return new Article();
  }
}
