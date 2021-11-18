package org.dew.cms.ws;

import java.io.File;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import org.util.WMap;
import org.util.WUtil;
import org.dew.cms.Multimedia;
import org.dew.cms.Place;
import org.dew.cms.Tag;
import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IAutore;
import org.dew.cms.common.ILuogo;
import org.dew.cms.common.ITag;
import org.dew.cms.util.BEConfig;
import org.dew.cms.util.CMSCache;
import org.dew.cms.util.ConnectionManager;
import org.dew.cms.util.DB;
import org.dew.cms.util.QueryBuilder;
import org.dew.cms.web.WebServices;

public 
class WSArticoli implements IArticolo 
{
  protected static Logger oLogger = Logger.getLogger(WebServices.class);
  
  private static final int iMAX_TEXT_LENGTH = 3980;
  
  protected static String _sWordsToExclude   = "|";
  static {
    // Italiano
    _sWordsToExclude += "agli|alla|allo|anche|aveva|avevano|come|dall|dalla|degli|dell|della|delle|dello|deve|domani|dopo|dovevano|";
    _sWordsToExclude += "ebbero|essa|essere|essi|esso|furono|hanno|ieri|loro|negli|nell|nella|nelle|nello|oggi|prima|";
    _sWordsToExclude += "quali|quando|quanto|quei|quella|quelli|quello|questa|questi|questo|sono|stati|stato|suoi|tutta|tutti|tutto|";
    // English
    _sWordsToExclude += "above|abstract|after|been|before|between|does|each|from|just|like|named|same|such|text|that|then|there|they|this|today|which|with|were|your|";
    // HTML
    _sWordsToExclude += "agrave|egrave|igrave|ograve|ugrave|aacute|eacute|iacute|oacute|uacute|";
  }
  
  protected static String _sWordsMultimedia = "|video|videos|photo|photos|foto|audio|audios|doc|docs|pdf|file|files|";
  
  // Query invocate nel metodo read
  private static final String sSQL_ART  = "SELECT A.DESCRIZIONE,A.DATA_ARTICOLO,A.ID_CATEGORIA,A.ID_SOTTOCATEGORIA,A.ID_TIPO_ARTICOLO,A.ID_ISTITUTO,I.CODICE,I.DESCRIZIONE," +
      "A.ID_LUOGO,L.DESCRIZIONE DESC_LUOGO,A.ID_TIPO_UTENTE,A.ATTIVO,A.DT_INS,A.UTE_INS,A.DT_AGG,A.UTE_AGG,A.PREF_POS,A.PREF_NEG " +
      "FROM CMS_ARTICOLI A,CMS_LUOGHI L,CMS_ISTITUTI I WHERE A.ID_LUOGO=L.ID_LUOGO AND A.ID_ISTITUTO=I.ID_ISTITUTO AND A.ID_ARTICOLO=?";
  private static final String sSQL_SOTT = "SELECT ID_LINGUA,DESCRIZIONE FROM CMS_SOTTOCATEGORIE_DESC WHERE ID_SOTTOCATEGORIA=?";
  private static final String sSQL_CONT = "SELECT ID_LINGUA,TITOLO,SPECIFICA,ABSTRACT,TESTO,TESTO2,TESTO3,NOTE,RIFERIMENTI,KEYWORDS FROM CMS_ARTICOLI_CONT WHERE ID_ARTICOLO=?";
  private static final String sSQL_AUT  = "SELECT AR.ID_AUTORE,AU.ID_TIPO_AUTORE,TA.DESCRIZIONE DESC_TIPO,AU.COGNOME,AU.NOME,AU.EMAIL,AR.ID_RUOLO,RU.DESCRIZIONE DESC_RUO " +
      "FROM CMS_ARTICOLI_AUT AR,CMS_AUTORI AU,CMS_TIPI_AUTORE TA,CMS_RUOLI RU " +
      "WHERE AR.ID_AUTORE=AU.ID_AUTORE AND AU.ID_TIPO_AUTORE=TA.ID_TIPO_AUTORE AND AR.ID_RUOLO=RU.ID_RUOLO AND AR.ID_ARTICOLO=? " +
      "ORDER BY AR.ID_RUOLO,AU.COGNOME,AU.NOME";
  private static final String sSQL_MULT = "SELECT ID_MULTIMEDIA,ID_LINGUA,URL_FILE,DESCRIZIONE FROM CMS_ARTICOLI_MULT WHERE ID_ARTICOLO=?";
  private static final String sSQL_TAG  = "SELECT AR.ID_TAG,TA.CODICE,AR.DESCRIZIONE FROM CMS_ARTICOLI_TAG AR,CMS_TAG TA " +
      "WHERE AR.ID_TAG=TA.ID_TAG AND AR.ID_ARTICOLO=? ORDER BY TA.CODICE";
  private static final String sSQL_COMP = "SELECT AC.ID_ARTICOLO_COMP,AR.DESCRIZIONE,AR.DATA_ARTICOLO,AC.ORDINE FROM CMS_ARTICOLI_COMP AC,CMS_ARTICOLI AR " +
      "WHERE AC.ID_ARTICOLO_COMP=AR.ID_ARTICOLO AND AC.ID_ARTICOLO=? ORDER BY AC.ORDINE";
  private static final String sSQL_LUO = "SELECT AL.ORDINE,AL.ID_LUOGO,AL.DESCRIZIONE DESC_ART,L.ID_TIPO_LUOGO,L.CODICE,L.DESCRIZIONE DESC_LUO," +
      "L.ID_COMUNE,C.DESCRIZIONE DESC_COM,L.INDIRIZZO,L.CAP,L.SITO_WEB,L.EMAIL,L.TEL_1,L.LATITUDINE,L.LONGITUDINE,L.INFORMAZIONI " +
      "FROM CMS_ARTICOLI_LUOGHI AL,CMS_LUOGHI L,CMS_COMUNI C " +
      "WHERE AL.ID_LUOGO=L.ID_LUOGO AND L.ID_COMUNE=C.ID_COMUNE AND AL.ID_ARTICOLO=? ORDER BY AL.ORDINE,L.DESCRIZIONE";
  private static final String sSQL_CORR = "SELECT AC.ID_ARTICOLO_CORR,AR.DESCRIZIONE,AR.DATA_ARTICOLO,AC.ORDINE " +
      "FROM CMS_ARTICOLI_CORR AC,CMS_ARTICOLI AR WHERE AC.ID_ARTICOLO_CORR=AR.ID_ARTICOLO AND AC.ID_ARTICOLO=? ORDER BY AC.ORDINE";
  private static final String sSQL_PRE = "SELECT ID_LINGUA,ORDINE,CODICE,DESCRIZIONE,PREZZO,SCONTO,SCONTATO,ACCONTO,PROMOZIONE " +
      "FROM CMS_ARTICOLI_PREZZI WHERE ID_ARTICOLO=? ORDER BY ID_LINGUA,ORDINE,CODICE";
  
  public static
  String getImportFolder()
  {
    return BEConfig.getImportFolder();
  }
  
  public
  List<List<Object>> lookup(Map<String, Object> mapFilter)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    if(mapFilter == null) mapFilter = new HashMap<String, Object>();
    String sFDescrizione = WUtil.toString(mapFilter.get(sDESCRIZIONE), "");
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_ARTICOLO", sID);
    qb.put("DESCRIZIONE", sDESCRIZIONE + "%");
    String sSQL = qb.select("CMS_ARTICOLI", mapFilter, "ATTIVO='" + QueryBuilder.decodeBoolean(true) + "'");
    sSQL += " ORDER BY DESCRIZIONE";
    
    int iRows = 0;
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_ARTICOLO");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        record.add(sDescrizione);
        
        if(sFDescrizione != null && sFDescrizione.equalsIgnoreCase(sDescrizione)) {
          result.add(0, record);
        }
        else {
          result.add(record);
        }
        
        iRows++;
        if(iRows >= 1000) break;
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.lookup(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public
  boolean exists(String sDescrizione)
      throws Exception
  {
    if(sDescrizione == null || sDescrizione.length() == 0) {
      return false;
    }
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_ARTICOLO FROM CMS_ARTICOLI WHERE DESCRIZIONE=?");
      pstm.setString(1, sDescrizione);
      rs   = pstm.executeQuery();
      return rs.next();
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.exists(" + sDescrizione + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
  }
  
  public static
  List<Integer> getYears(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    String sSQL = "SELECT DISTINCT EXTRACT(YEAR FROM DATA_ARTICOLO) FROM CMS_ARTICOLI ";
    sSQL += "WHERE ATTIVO = ? ";
    if(iIdCategoria > 0) {
      sSQL += "AND ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND ID_CATEGORIA <> ? AND ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND ID_SOTTOCATEGORIA <> ? AND ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND ID_TIPO_ARTICOLO <> ? AND ID_TIPO_ARTICOLO > 0 ";
    }
    if(iIdTipoUtente > 0) {
      sSQL += "AND ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND ID_TIPO_UTENTE < ? ";
    }
    sSQL += "ORDER BY 1";
    
    int p = 0;
    List<Integer> result = new ArrayList<Integer>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      rs = pstm.executeQuery();
      while(rs.next()) {
        result.add(rs.getInt(1));
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.getYears(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return result;
  }
  
  public static
  List<Integer> getMonths(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    String sSQL = "SELECT DISTINCT EXTRACT(YEAR FROM DATA_ARTICOLO),EXTRACT(MONTH FROM DATA_ARTICOLO) FROM CMS_ARTICOLI ";
    sSQL += "WHERE ATTIVO = ? ";
    if(iIdCategoria > 0) {
      sSQL += "AND ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND ID_CATEGORIA <> ? AND ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND ID_SOTTOCATEGORIA <> ? AND ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND ID_TIPO_ARTICOLO <> ? AND ID_TIPO_ARTICOLO > 0 ";
    }
    if(iIdTipoUtente > 0) {
      sSQL += "AND ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND ID_TIPO_UTENTE < ? ";
    }
    sSQL += "ORDER BY 1,2";
    
    int p = 0;
    List<Integer> result = new ArrayList<Integer>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iYear  = rs.getInt(1);
        int iMonth = rs.getInt(2);
        result.add(iYear*100 + iMonth);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.getMonths(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return result;
  }
  
  public static
  List<Map<String, Object>> find(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iAnnoMese, boolean boComposti)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    Calendar cal = null;
    java.sql.Date dtInizio = null;
    java.sql.Date dtFine   = null;
    if(iAnnoMese > 9999) {
      cal = new GregorianCalendar(iAnnoMese/100, (iAnnoMese%100)-1, 1, 0, 0, 0);
      dtInizio = new java.sql.Date(cal.getTimeInMillis());
      cal.add(Calendar.MONTH, 1);
      dtFine = new java.sql.Date(cal.getTimeInMillis());
    }
    else {
      cal = new GregorianCalendar(iAnnoMese, 0, 1, 0, 0, 0);
      dtInizio = new java.sql.Date(cal.getTimeInMillis());
      cal.add(Calendar.YEAR, 1);
      dtFine = new java.sql.Date(cal.getTimeInMillis());
    }
    
    String sSQL = "SELECT DISTINCT A.ID_ARTICOLO,A.DATA_ARTICOLO ";
    if(boComposti) {
      sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_COMP AC ";
      sSQL += "WHERE A.ID_ARTICOLO = AC.ID_ARTICOLO AND A.ATTIVO = ? ";
    }
    else {
      sSQL += "FROM CMS_ARTICOLI A ";
      sSQL += "WHERE A.ATTIVO = ? ";
    }
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    sSQL += "AND A.DATA_ARTICOLO >= ? AND A.DATA_ARTICOLO < ? ";
    sSQL += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC";
    
    int p = 0;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      pstm.setDate(++p, dtInizio);
      pstm.setDate(++p, dtFine);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iId = rs.getInt("ID_ARTICOLO");
        Map<String, Object> record = read(conn, iId, false, false);
        if(record == null || record.isEmpty()) continue;
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iAnnoMese + "," + boComposti + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    result.get(0).put(sCOUNT, result.size());
    return result;
  }
  
  public static
  List<Map<String, Object>> find(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, List<?> listTags)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    String sSQL = "SELECT DISTINCT A.ID_ARTICOLO,A.DATA_ARTICOLO ";
    sSQL += "FROM CMS_ARTICOLI A ";
    if(listTags != null && listTags.size() > 0) {
      sSQL += ",CMS_ARTICOLI_TAG AT ";
    }
    sSQL += "WHERE A.ATTIVO = ? ";
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    if(listTags != null && listTags.size() > 0) {
      sSQL += "AND A.ID_ARTICOLO = AT.ID_ARTICOLO ";
      sSQL += "AND AT.ID_TAG IN (" + DB.buildInSet(listTags.size()) + ") ";
    }
    sSQL += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC";
    
    int p = 0;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(listTags != null && listTags.size() > 0) {
        for(int t = 0; t < listTags.size(); t++) {
          int idTag = WUtil.toInt(listTags.get(t), 0);
          pstm.setInt(++p, idTag);
        }
      }
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iId = rs.getInt("ID_ARTICOLO");
        Map<String, Object> record = read(conn, iId, false, false);
        if(record == null || record.isEmpty()) continue;
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + listTags + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    result.get(0).put(sCOUNT, result.size());
    return result;
  }
  
  public static
  List<Map<String, Object>> find(String sSQL, int iPage, int iArticlesPerPage, int iArticlesPagePrev, boolean boDoNotCount)
      throws Exception
  {
    if(sSQL == null || sSQL.length() == 0) {
      return new ArrayList<Map<String, Object>>();
    }
    if(sSQL.length() < 15) {
      String sDescrizione = sSQL.toUpperCase();
      sSQL = "SELECT ID_ARTICOLO FROM CMS_ARTICOLI ";
      sSQL += "WHERE DESCRIZIONE LIKE '" + sDescrizione.replace("'", "''") + "' ";
      sSQL += "AND ATTIVO = '" + QueryBuilder.decodeBoolean(true) + "' ";
      sSQL += "ORDER BY DESCRIZIONE";
    }
    else {
      String sStart = sSQL.substring(0, 7).toUpperCase();
      if(!sStart.startsWith("SELECT ")) {
        String sDescrizione = sSQL.toUpperCase();
        sSQL = "SELECT ID_ARTICOLO FROM CMS_ARTICOLI ";
        sSQL += "WHERE DESCRIZIONE LIKE '" + sDescrizione.replace("'", "''") + "' ";
        sSQL += "AND ATTIVO = '" + QueryBuilder.decodeBoolean(true) + "' ";
        sSQL += "ORDER BY DESCRIZIONE";
      }
    }
    
    if(iPage < 1) iPage = 1;
    if(iArticlesPerPage < 1) {
      iArticlesPerPage = 1000;
    }
    int iMinIndex = (iPage - 1) * iArticlesPerPage;
    int iMaxIndex = iMinIndex + iArticlesPerPage - 1;
    if(iArticlesPagePrev > 0) {
      if(iMinIndex > iArticlesPagePrev) {
        iMinIndex = iMinIndex - iArticlesPagePrev;
      }
      else {
        iMinIndex = 0;
      }
    }
    
    int i = -1;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        i++;
        if(i < iMinIndex) continue;
        if(i > iMaxIndex) {
          if(boDoNotCount) break;
          continue;
        }
        
        int iId = rs.getInt(1);
        Map<String, Object> record = read(conn, iId, false, false);
        if(record == null || record.isEmpty()) continue;
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + sSQL + "," + iPage + "," + iArticlesPerPage + "," + iArticlesPagePrev + "," + boDoNotCount + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    if(result != null && result.size() > 0) {
      int iModPages = (i + 1) % iArticlesPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = (i + 1) / iArticlesPerPage;
      }
      else {
        iPages = (i + 1) / iArticlesPerPage + 1;
      }
      Map<String, Object> map0 = result.get(0);
      map0.put(sPAGINA, iPage);
      map0.put(sPAGINE, iPages);
      map0.put(sCOUNT,  i + 1);
    }
    return result;
  }
  
  public static
  List<Map<String, Object>> find(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente, int iIdTag, int iPage, int iArticlesPerPage, int iArticlesPagePrev, boolean boComposti, boolean boDoNotCount)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    if(iPage < 1) iPage = 1;
    boolean boBreakAtYearChange = false;
    if(iArticlesPerPage < 1) {
      iArticlesPerPage = 1000;
      boBreakAtYearChange = true;
    }
    int iMinIndex = (iPage - 1) * iArticlesPerPage;
    int iMaxIndex = iMinIndex + iArticlesPerPage - 1;
    if(iArticlesPagePrev > 0) {
      if(iMinIndex > iArticlesPagePrev) {
        iMinIndex = iMinIndex - iArticlesPagePrev;
      }
      else {
        iMinIndex = 0;
      }
    }
    
    String sSQL = "SELECT DISTINCT A.ID_ARTICOLO,A.DATA_ARTICOLO ";
    if(boComposti) {
      if(iIdTag != 0) {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_COMP AC,CMS_ARTICOLI_TAG T ";
        sSQL += "WHERE A.ID_ARTICOLO = AC.ID_ARTICOLO AND A.ID_ARTICOLO = T.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
      else {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_COMP AC ";
        sSQL += "WHERE A.ID_ARTICOLO = AC.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
    }
    else {
      if(iIdTag != 0) {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_TAG T ";
        sSQL += "WHERE A.ID_ARTICOLO = T.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
      else {
        sSQL += "FROM CMS_ARTICOLI A ";
        sSQL += "WHERE A.ATTIVO = ? ";
      }
    }
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    if(iIdTipoUtente > 0) {
      sSQL += "AND A.ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND A.ID_TIPO_UTENTE < ? ";
    }
    if(iIdTag > 0) {
      sSQL += "AND T.ID_TAG = ? ";
    }
    else if(iIdTag < 0) {
      iIdTag = iIdTag * -1;
      sSQL += "AND T.ID_TAG <> ? AND T.ID_TAG > 0 ";
    }
    sSQL += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC";
    
    Calendar cal = Calendar.getInstance();
    int iLastYear = 0;
    int i = -1;
    int p = 0;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      if(iIdTag            != 0) pstm.setInt(++p, iIdTag);
      rs = pstm.executeQuery();
      while(rs.next()) {
        i++;
        if(i < iMinIndex) continue;
        if(i > iMaxIndex) {
          if(boDoNotCount) break;
          continue;
        }
        if(boBreakAtYearChange) {
          java.sql.Date dtArt = rs.getDate("DATA_ARTICOLO");
          if(dtArt != null) {
            cal.setTimeInMillis(dtArt.getTime());
            int iYear = cal.get(Calendar.YEAR);
            if(iLastYear != 0 && iLastYear != iYear) break;
            iLastYear = iYear;
          }
        }
        int iId = rs.getInt("ID_ARTICOLO");
        Map<String, Object> map = read(conn, iId, false, false);
        if(map == null || map.isEmpty()) continue;
        result.add(map);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + "," + iIdTag + "," + iPage + "," + iArticlesPerPage + "," + iArticlesPagePrev + "," + boComposti + "," + boDoNotCount + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    if(result != null && result.size() > 0) {
      int iModPages = (i + 1) % iArticlesPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = (i + 1) / iArticlesPerPage;
      }
      else {
        iPages = (i + 1) / iArticlesPerPage + 1;
      }
      Map<String, Object> map0 = result.get(0);
      map0.put(sPAGINA, iPage);
      map0.put(sPAGINE, iPages);
      map0.put(sCOUNT,  i + 1);
    }
    return result;
  }
  
  public static
  List<Map<String, Object>> find(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdComune, String sCap, int iPage, int iArticlesPerPage, int iArticlesPagePrev, boolean boDoNotCount)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    if(iPage < 1) iPage = 1;
    boolean boBreakAtYearChange = false;
    if(iArticlesPerPage < 1) {
      iArticlesPerPage = 1000;
      boBreakAtYearChange = true;
    }
    int iMinIndex = (iPage - 1) * iArticlesPerPage;
    int iMaxIndex = iMinIndex + iArticlesPerPage - 1;
    if(iArticlesPagePrev > 0) {
      if(iMinIndex > iArticlesPagePrev) {
        iMinIndex = iMinIndex - iArticlesPagePrev;
      }
      else {
        iMinIndex = 0;
      }
    }
    
    String sSQL = "SELECT A.ID_ARTICOLO,A.DATA_ARTICOLO FROM CMS_ARTICOLI A,CMS_ARTICOLI_LUOGHI AL,CMS_LUOGHI L ";
    sSQL += "WHERE A.ID_ARTICOLO = AL.ID_ARTICOLO AND AL.ID_LUOGO = L.ID_LUOGO AND A.ATTIVO = ? ";
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    if(iIdComune > 0) {
      sSQL += "AND L.ID_COMUNE = ? ";
    }
    else if(iIdComune < 0) {
      iIdComune = iIdComune * -1;
      sSQL += "AND L.ID_COMUNE <> ? ";
    }
    if(sCap != null && sCap.length() > 1) {
      sSQL += "AND L.CAP LIKE ? ";
    }
    sSQL += "GROUP BY A.ID_ARTICOLO,A.DATA_ARTICOLO ";
    sSQL += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC";
    
    Calendar cal = Calendar.getInstance();
    int iLastYear = 0;
    int i = -1;
    int p = 0;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdComune         != 0) pstm.setInt(++p, iIdComune);
      if(sCap != null && sCap.length() > 1) pstm.setString(++p, sCap);
      rs = pstm.executeQuery();
      while(rs.next()) {
        i++;
        if(i < iMinIndex) continue;
        if(i > iMaxIndex) {
          if(boDoNotCount) break;
          continue;
        }
        if(boBreakAtYearChange) {
          java.sql.Date dtArt = rs.getDate("DATA_ARTICOLO");
          if(dtArt != null) {
            cal.setTimeInMillis(dtArt.getTime());
            int iYear = cal.get(Calendar.YEAR);
            if(iLastYear != 0 && iLastYear != iYear) break;
            iLastYear = iYear;
          }
        }
        int iId = rs.getInt("ID_ARTICOLO");
        Map<String, Object> map = read(conn, iId, false, false);
        if(map == null || map.isEmpty()) continue;
        result.add(map);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdComune + "," + sCap + "," + iPage + "," + iArticlesPerPage + "," + iArticlesPagePrev + "," + boDoNotCount + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    if(result != null && result.size() > 0) {
      int iModPages = (i + 1) % iArticlesPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = (i + 1) / iArticlesPerPage;
      }
      else {
        iPages = (i + 1) / iArticlesPerPage + 1;
      }
      Map<String, Object> map0 = result.get(0);
      map0.put(sPAGINA, iPage);
      map0.put(sPAGINE, iPages);
      map0.put(sCOUNT,  i + 1);
    }
    return result;
  }
  
  public static
  int count(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente, int iIdTag)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    String sSQL = "SELECT COUNT(*) ";
    if(iIdTag != 0) {
      sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_TAG T ";
      sSQL += "WHERE A.ID_ARTICOLO=T.ID_ARTICOLO AND A.ATTIVO = ? ";
    }
    else {
      sSQL += "FROM CMS_ARTICOLI A ";
      sSQL += "WHERE A.ATTIVO = ? ";
    }
    // Categoria
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    // Sottocategoria
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    // Tipo articolo
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    // Tipo utente
    if(iIdTipoUtente > 0) {
      sSQL += "AND A.ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND A.ID_TIPO_UTENTE < ? ";
    }
    // Tag
    if(iIdTag > 0) {
      sSQL += "AND T.ID_TAG = ? ";
    }
    else if(iIdTag < 0) {
      iIdTag = iIdTag * -1;
      sSQL += "AND T.ID_TAG <> ? AND T.ID_TAG > 0 ";
    }
    
    int iCount = 0;
    int p = 0;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      if(iIdTag            != 0) pstm.setInt(++p, iIdTag);
      rs = pstm.executeQuery();
      if(rs.next()) {
        iCount = rs.getInt(1);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.count(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + "," + iIdTag + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return iCount;
  }
  
  public static
  int count(int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente, int iIdComune, String sCap)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    String sSQL = "SELECT COUNT(*) FROM (SELECT A.ID_ARTICOLO,A.DATA_ARTICOLO FROM CMS_ARTICOLI A,CMS_ARTICOLI_LUOGHI AL,CMS_LUOGHI L ";
    sSQL += "WHERE A.ID_ARTICOLO=AL.ID_ARTICOLO AND AL.ID_LUOGO=L.ID_LUOGO AND A.ATTIVO = ? ";
    // Categoria
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    // Sottocategoria
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    // Tipo articolo
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    // Tipo utente
    if(iIdTipoUtente > 0) {
      sSQL += "AND A.ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND A.ID_TIPO_UTENTE < ? ";
    }
    // Comune
    if(iIdComune > 0) {
      sSQL += "AND L.ID_COMUNE = ? ";
    }
    else if(iIdComune < 0) {
      iIdComune = iIdComune * -1;
      sSQL += "AND L.ID_COMUNE <> ? ";
    }
    // CAP
    if(sCap != null && sCap.length() > 1) {
      sSQL += "AND L.CAP LIKE ? ";
    }
    sSQL += "GROUP BY A.ID_ARTICOLO,A.DATA_ARTICOLO) T";
    
    int iCount = 0;
    int p = 0;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      if(iIdComune         != 0) pstm.setInt(++p, iIdComune);
      if(sCap != null && sCap.length() > 1) pstm.setString(++p, sCap);
      rs = pstm.executeQuery();
      if(rs.next()) {
        iCount = rs.getInt(1);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.count(" + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + "," + iIdComune + "," + sCap + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return iCount;
  }
  
  public static
  List<Map<String, Object>> find(String sText, int iIdTipoArticolo1, int iIdTipoArticolo2, int iPage, int iItemsPerPage, int iIdLingua)
      throws Exception
  {
    if(sText == null || sText.length() == 0) {
      return new ArrayList<Map<String,Object>>();
    }
    List<Map<String, Object>> result = null;
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, iIdLingua, null);
      if((result == null || result.size() == 0) && iIdLingua != 0) {
        result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, 0, null);
      }
      if(result == null || result.size() == 0) {
        List<Integer> listTags = WSTag.find(conn, iIdLingua, sText);
        if(listTags != null && listTags.size() > 0) {
          result = find(conn, "", iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, 0, listTags);
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + sText + "," + iIdTipoArticolo1 + "," + iIdTipoArticolo2 + "," + iPage + "," + iItemsPerPage + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new ArrayList<Map<String, Object>>();
    return result;
  }
  
  public static
  List<Map<String, Object>> find(String sText, List<?> vTags, int iIdTipoArticolo1, int iIdTipoArticolo2, int iPage, int iItemsPerPage, int iIdLingua)
      throws Exception
  {
    if((sText == null || sText.length() == 0) && (vTags == null || vTags.size() == 0)) {
      return new ArrayList<Map<String,Object>>();
    }
    List<Map<String, Object>> result = null;
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, iIdLingua, vTags);
      if((result == null || result.size() == 0) && iIdLingua != 0) {
        result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, 0, vTags);
      }
      if((result == null || result.size() == 0) && (vTags == null || vTags.size() == 0)) {
        vTags = WSTag.find(conn, iIdLingua, sText);
        if(vTags != null && vTags.size() > 0) {
          result = find(conn, "", iIdTipoArticolo1, iIdTipoArticolo2, 0, 0, 0, iPage, iItemsPerPage, 0, vTags);
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + sText + "," + vTags + "," + iIdTipoArticolo1 + "," + iIdTipoArticolo2 + "," + iPage + "," + iItemsPerPage + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new ArrayList<Map<String, Object>>();
    return result;
  }
  
  public static
  List<Map<String, Object>> find(String sText, List<?> vTags, int iIdTipoArticolo1, int iIdTipoArticolo2, int iIdCategoria, int iIdSottocategoria, int iIdTipoUtente, int iPage, int iItemsPerPage, int iIdLingua)
      throws Exception
  {
    if((sText == null || sText.length() == 0) && (vTags == null || vTags.size() == 0) && (iIdCategoria == 0 && iIdSottocategoria == 0)) {
      return new ArrayList<Map<String,Object>>();
    }
    List<Map<String, Object>> result = null;
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, iIdCategoria, iIdSottocategoria, iIdTipoUtente, iPage, iItemsPerPage, iIdLingua, vTags);
      if((result == null || result.size() == 0) && iIdLingua != 0) {
        result = find(conn, sText, iIdTipoArticolo1, iIdTipoArticolo2, iIdCategoria, iIdSottocategoria, iIdTipoUtente, iPage, iItemsPerPage, 0, vTags);
      }
      if((result == null || result.size() == 0) && (vTags == null || vTags.size() == 0)) {
        vTags = WSTag.find(conn, iIdLingua, sText);
        if(vTags != null && vTags.size() > 0) {
          result = find(conn, "", iIdTipoArticolo1, iIdTipoArticolo2, iIdCategoria, iIdSottocategoria, iIdTipoUtente, iPage, iItemsPerPage, 0, vTags);
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + sText + "," + vTags + "," + iIdTipoArticolo1 + "," + iIdTipoArticolo2 + "," + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoUtente + "," + iPage + "," + iItemsPerPage + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new ArrayList<Map<String, Object>>();
    return result;
  }
  
  public static
  List<Map<String, Object>> find(Map<String, Object> mapFilter)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    String sAddClause = "";
    String sTables    = "CMS_ARTICOLI A";
    
    if(mapFilter == null) mapFilter = new HashMap<String, Object>();
    java.util.Date dDataInizio = WUtil.toDate(mapFilter.get(sDATA_INIZIO),  null);
    java.util.Date dDataFine   = WUtil.toDate(mapFilter.get(sDATA_FINE),    null);
    String sText = WUtil.toString(mapFilter.remove(sKEYWORDS), null);
    if(sText != null && sText.length() > 1) {
      StringBuffer sbTextWithoutDigits = new StringBuffer();
      Date[] adStartEnd = getDateFilter(sText, sbTextWithoutDigits);
      if(adStartEnd != null && adStartEnd.length > 0) {
        dDataInizio = adStartEnd[0];
        dDataFine   = adStartEnd.length > 1 ? adStartEnd[1] : null;
        sText       = sbTextWithoutDigits.toString().trim();
      }
      sText = sText.toLowerCase();
      boolean boSearchMultimedia = _sWordsMultimedia.indexOf("|" + sText + "|") >= 0;
      int iTipoContenuto = 0;
      if(boSearchMultimedia) {
        if("|video|videos|film|films|filmato|filmati|".indexOf("|" + sText + "|")  >= 0) iTipoContenuto = 1;
        else if("|photo|photos|foto|immagine|immagini|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 2;
        else if("|audio|audios|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 3;
        else if("|doc|docs|pdf|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 4;
        sTables   += ",CMS_ARTICOLI_MULT M";
        sAddClause = "A.ID_ARTICOLO=M.ID_ARTICOLO";
        if(iTipoContenuto > 0) sAddClause += " AND M.ID_TIPO_CONTENUTO=" + iTipoContenuto;
      }
      else {
        sTables   += ",CMS_ARTICOLI_CONT C";
        sAddClause = "A.ID_ARTICOLO = C.ID_ARTICOLO";
        if(sText != null && sText.length() > 2) {
          String sLikeFilter = getLikeFilter(sText);
          sAddClause += " AND C.ID_LINGUA=0 AND C.KEYWORDS LIKE '" + sLikeFilter.replace("'", "''") + "'";
        }
      }
    }
    if(dDataInizio != null) {
      if(sAddClause != null && sAddClause.length() > 0) sAddClause += " AND ";
      sAddClause += "DATA_ARTICOLO >= " + QueryBuilder.toString(dDataInizio);
    }
    if(dDataFine   != null) {
      if(sAddClause != null && sAddClause.length() > 0) sAddClause += " AND ";
      sAddClause += "DATA_ARTICOLO <= " + QueryBuilder.toString(dDataFine);
    }
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("A.ID_ARTICOLO",       sID);
    qb.put("A.DESCRIZIONE",       "%" + sDESCRIZIONE + "%");
    qb.put("A.DATA_ARTICOLO",     sDATA_ARTICOLO);
    qb.put("A.ID_CATEGORIA",      sID_CATEGORIA);
    qb.put("A.ID_SOTTOCATEGORIA", sID_SOTTOCATEG);
    qb.put("A.ID_TIPO_ARTICOLO",  sID_TIPO_ART);
    qb.put("A.ID_ISTITUTO",       sID_ISTITUTO);
    qb.put("A.ID_LUOGO",          sID_LUOGO);
    qb.put("A.ID_TIPO_UTENTE",    sID_TIPO_UTE);
    qb.put("A.ATTIVO",            sATTIVO);
    String sSQL = qb.select(sTables, mapFilter, sAddClause);
    sSQL += " ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC";
    
    int iCount  = 0;
    int iLastId = 0;
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId               = rs.getInt("ID_ARTICOLO");
        String sDescrizione   = rs.getString("DESCRIZIONE");
        Date dDataArticolo    = rs.getDate("DATA_ARTICOLO");
        String sAttivo        = rs.getString("ATTIVO");
        
        if(iId == iLastId) continue;
        iLastId = iId;
        
        WMap record = new WMap();
        record.put(sID,                iId);
        record.put(sDESCRIZIONE,       sDescrizione);
        record.putDate(sDATA_ARTICOLO, dDataArticolo);
        record.putBoolean(sATTIVO,     sAttivo);
        
        result.add(record.toMapObject());
        iCount++;
        if(iCount >= 1000) break;
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public static
  Map<String, Object> read(int iId)
      throws Exception
  {
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      return read(conn, iId, true, true);
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.read(" + iId + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public static
  Map<String, Object> read(int iId, boolean boCreateMultFolder)
      throws Exception
  {
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      return read(conn, iId, true, boCreateMultFolder);
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.read(" + iId + "," + boCreateMultFolder + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public static
  Map<String, Object> read(int iId, int iIdUtente)
      throws Exception
  {
    Map<String, Object> result = null;
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      result = read(conn, iId, true, false);
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      WSPortale.insertLog(conn, iId, iIdUtente);
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.read(" + iId + "," + iIdUtente + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new HashMap<String, Object>();
    return result;
  }
  
  public
  boolean insert(Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty() || mapValues.get(sID) == null) {
      return false;
    }
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      insert(conn, mapValues, null);
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public
  boolean update(Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty() || mapValues.get(sID) == null) {
      return false;
    }
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int id = WUtil.toInt(mapValues.get(sID), 0);
      
      // In questo modo si preservano gli id precedenti
      Map<String, Integer> mapIdMultimedia = getIdMultimedia(conn, id);
      
      clean(conn, id);
      
      insert(conn, mapValues, mapIdMultimedia);
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  Map<String, Integer> getIdMultimedia(Connection conn, int idArticolo)
      throws Exception
  {
    Map<String, Integer> result = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT ID_MULTIMEDIA,ID_LINGUA,URL_FILE FROM CMS_ARTICOLI_MULT WHERE ID_ARTICOLO=?");
      pstm.setInt(1, idArticolo);
      rs = pstm.executeQuery();
      while(rs.next()) {
        if(result == null) result = new HashMap<String, Integer>();
        int iIdMultimedia = rs.getInt(1);
        int iIdLingua     = rs.getInt(2);
        String sURLFile   = rs.getString(3);
        if(sURLFile == null || sURLFile.length() == 0) continue;
        result.put(iIdLingua + "#" + sURLFile, iIdMultimedia);
      }
    }
    finally {
      ConnectionManager.close(rs, pstm);
    }
    return result;
  }
  
  public static
  List<Map<String, Object>> find(Connection conn, String sText, int iIdTipoArticolo1, int iIdTipoArticolo2, int iIdCategoria, int iIdSottocategoria, int iIdTipoUtente, int iPage, int iArticlesPerPage, int iIdLingua, List<?> listTags)
      throws Exception
  {
    if(sText == null || sText.length() == 0) sText = "";
    if(listTags == null) listTags = new ArrayList<Integer>(0);
    
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    boolean bovTags_is_ListOfString = false;
    if(listTags.size() > 0) {
      Object oTag0 = listTags.get(0);
      if(oTag0 instanceof String) {
        bovTags_is_ListOfString = true;
      }
    }
    
    if(iPage < 1) iPage = 1;
    if(iArticlesPerPage < 1) iArticlesPerPage = 1000;
    int iMinIndex = (iPage - 1) * iArticlesPerPage;
    int iMaxIndex = iMinIndex + iArticlesPerPage - 1;
    
    StringBuffer sbTextWithoutDigits = new StringBuffer();
    Date[] adStartEnd = getDateFilter(sText, sbTextWithoutDigits);
    if(adStartEnd != null && adStartEnd.length > 0) {
      sText = sbTextWithoutDigits.toString().trim();
    }
    sText = sText.toLowerCase();
    boolean boSearchMultimedia = _sWordsMultimedia.indexOf("|" + sText + "|") >= 0;
    int iTipoContenuto = 0;
    String sSQL = null;
    if(boSearchMultimedia) {
      if("|video|videos|film|films|filmato|filmati|".indexOf("|" + sText + "|")  >= 0) iTipoContenuto = 1;
      else if("|photo|photos|foto|immagine|immagini|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 2;
      else if("|audio|audios|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 3;
      else if("|doc|docs|pdf|".indexOf("|" + sText + "|") >= 0) iTipoContenuto = 4;
      sSQL = "SELECT A.ID_ARTICOLO,A.DATA_ARTICOLO FROM CMS_ARTICOLI A,CMS_ARTICOLI_MULT M ";
      if(listTags.size() > 0) {
        sSQL += ",CMS_ARTICOLI_TAG AT ";
      }
      sSQL += "WHERE A.ID_ARTICOLO = M.ID_ARTICOLO AND A.ATTIVO = ? ";
      if(iIdTipoArticolo1 != 0 && iIdTipoArticolo2 != 0) {
        if(iIdTipoArticolo1 < 0 && iIdTipoArticolo2 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO > ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
        }
        else if(iIdTipoArticolo1 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO > ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO <= ? ";
        }
        else if(iIdTipoArticolo2 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO >= ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
        }
        else {
          sSQL += "AND A.ID_TIPO_ARTICOLO IN (?, ?) ";
        }
      }
      else if(iIdTipoArticolo1 < 0) {
        sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
      }
      else if(iIdTipoArticolo1 > 0) {
        sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
      }
      if(iIdCategoria > 0) {
        sSQL += "AND A.ID_CATEGORIA = ? ";
      }
      else if(iIdCategoria < 0) {
        iIdCategoria = iIdCategoria * -1;
        sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
      }
      if(iIdSottocategoria > 0) {
        sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
      }
      else if(iIdSottocategoria < 0) {
        iIdSottocategoria = iIdSottocategoria * -1;
        sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
      }
      if(iIdTipoUtente > 0) {
        sSQL += "AND A.ID_TIPO_UTENTE = ? ";
      }
      else if(iIdTipoUtente < 0) {
        iIdTipoUtente = iIdTipoUtente * -1;
        sSQL += "AND A.ID_TIPO_UTENTE < ? ";
      }
      if(iTipoContenuto > 0) sSQL += "AND M.ID_TIPO_CONTENUTO = ? ";
      if(adStartEnd != null && adStartEnd.length == 2) {
        sSQL += "AND A.DATA_ARTICOLO >= ? AND A.DATA_ARTICOLO <= ? ";
      }
      
      if(bovTags_is_ListOfString) {
        sSQL += "AND A.ID_ARTICOLO=AT.ID_ARTICOLO ";
        sSQL += "AND AT.DESCRIZIONE IN (" + DB.buildInSet(listTags.size()) + ") ";
      }
      else {
        sSQL += "AND A.ID_ARTICOLO=AT.ID_ARTICOLO ";
        sSQL += "AND AT.ID_TAG IN (" + DB.buildInSet(listTags.size()) + ") ";
      }
      
      sSQL += "GROUP BY A.ID_ARTICOLO,A.DATA_ARTICOLO ";
      sSQL += "ORDER BY A.DATA_ARTICOLO,A.ID_ARTICOLO DESC";
    }
    else {
      sSQL = "SELECT A.ID_ARTICOLO,A.DATA_ARTICOLO FROM CMS_ARTICOLI A,CMS_ARTICOLI_CONT C ";
      if(listTags.size() > 0) {
        sSQL += ",CMS_ARTICOLI_TAG AT ";
      }
      sSQL += "WHERE A.ID_ARTICOLO = C.ID_ARTICOLO AND A.ATTIVO = ? ";
      if(iIdTipoArticolo1 != 0 && iIdTipoArticolo2 != 0) {
        if(iIdTipoArticolo1 < 0 && iIdTipoArticolo2 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO > ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
        }
        else if(iIdTipoArticolo1 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO > ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO <= ? ";
        }
        else if(iIdTipoArticolo2 < 0) {
          sSQL += "AND A.ID_TIPO_ARTICOLO >= ? ";
          sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
        }
        else {
          sSQL += "AND A.ID_TIPO_ARTICOLO IN (?, ?) ";
        }
      }
      else if(iIdTipoArticolo1 < 0) {
        sSQL += "AND A.ID_TIPO_ARTICOLO < ? ";
      }
      else if(iIdTipoArticolo1 > 0) {
        sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
      }
      if(iIdCategoria > 0) {
        sSQL += "AND A.ID_CATEGORIA = ? ";
      }
      else if(iIdCategoria < 0) {
        iIdCategoria = iIdCategoria * -1;
        sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
      }
      if(iIdSottocategoria > 0) {
        sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
      }
      else if(iIdSottocategoria < 0) {
        iIdSottocategoria = iIdSottocategoria * -1;
        sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
      }
      if(iIdTipoUtente > 0) {
        sSQL += "AND A.ID_TIPO_UTENTE = ? ";
      }
      else if(iIdTipoUtente < 0) {
        iIdTipoUtente = iIdTipoUtente * -1;
        sSQL += "AND A.ID_TIPO_UTENTE < ? ";
      }
      if(sText != null && sText.length() > 2) {
        sSQL += "AND C.ID_LINGUA = ? AND C.KEYWORDS LIKE ? ";
      }
      if(adStartEnd != null && adStartEnd.length == 2) {
        sSQL += "AND A.DATA_ARTICOLO >= ? AND A.DATA_ARTICOLO <= ? ";
      }
      
      if(bovTags_is_ListOfString) {
        sSQL += "AND A.ID_ARTICOLO=AT.ID_ARTICOLO ";
        sSQL += "AND AT.DESCRIZIONE IN (" + DB.buildInSet(listTags.size()) + ") ";
      }
      else {
        sSQL += "AND A.ID_ARTICOLO=AT.ID_ARTICOLO ";
        sSQL += "AND AT.ID_TAG IN (" + DB.buildInSet(listTags.size()) + ") ";
      }
      
      sSQL += "GROUP BY A.ID_ARTICOLO,A.DATA_ARTICOLO ";
      sSQL += "ORDER BY A.DATA_ARTICOLO,A.ID_ARTICOLO DESC";
    }
    if(iIdTipoArticolo1 < 0) iIdTipoArticolo1 = iIdTipoArticolo1 * -1;
    if(iIdTipoArticolo2 < 0) iIdTipoArticolo2 = iIdTipoArticolo2 * -1;
    int i = -1;
    int p =  0;
    
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdTipoArticolo1 != 0) {
        pstm.setInt(++p, iIdTipoArticolo1);
      }
      if(iIdTipoArticolo1 != 0 && iIdTipoArticolo2 != 0) {
        pstm.setInt(++p, iIdTipoArticolo2);
      }
      if(iIdCategoria != 0) {
        pstm.setInt(++p, iIdCategoria < Integer.MAX_VALUE ? iIdCategoria : 0);
      }
      if(iIdSottocategoria != 0) {
        pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      }
      if(iIdTipoUtente != 0) {
        pstm.setInt(++p, iIdTipoUtente);
      }
      if(boSearchMultimedia) {
        if(iTipoContenuto > 0) {
          pstm.setInt(++p, iTipoContenuto);
        }
      }
      else {
        if(sText != null && sText.length() > 2) {
          pstm.setInt(++p, iIdLingua);
          pstm.setString(++p, getLikeFilter(sText));
        }
      }
      if(adStartEnd != null && adStartEnd.length == 2) {
        pstm.setDate(++p, adStartEnd[0]);
        pstm.setDate(++p, adStartEnd[1]);
      }
      
      if(bovTags_is_ListOfString) {
        for(int t = 0; t < listTags.size(); t++) {
          pstm.setString(++p, WUtil.toString(listTags.get(t), ""));
        }
      }
      else {
        for(int t = 0; t < listTags.size(); t++) {
          pstm.setInt(++p, WSTag.getIdTag(listTags.get(t), 0));
        }
      }
      
      rs = pstm.executeQuery();
      while(rs.next()) {
        i++;
        if(i < iMinIndex) continue;
        if(i > iMaxIndex) continue;
        
        int iId = rs.getInt("ID_ARTICOLO");
        Map<String, Object> map = read(conn, iId, false, false);
        if(map == null || map.isEmpty()) continue;
        result.add(map);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.find(conn," + sText + "," + iIdTipoArticolo1 + "," + iIdTipoArticolo2 + "," + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoUtente + "," + iPage + "," + iArticlesPerPage + "," + iIdLingua + "," + listTags + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm);
    }
    if(result != null && result.size() > 0) {
      int iModPages = (i + 1) % iArticlesPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = (i + 1) / iArticlesPerPage;
      }
      else {
        iPages = (i + 1) / iArticlesPerPage + 1;
      }
      Map<String, Object> map0 = result.get(0);
      map0.put(sPAGINA, iPage);
      map0.put(sPAGINE, iPages);
      map0.put(sCOUNT,  i + 1);
    }
    return result;
  }
  
  public static
  Map<String, Object> read(Connection conn, int iId, boolean boComplete, boolean boCreateMultFolder)
      throws Exception
  {
    WMap result = new WMap();
    
    int iIdSottocategoria = 0;
    boolean boFound = false;
    PreparedStatement pstm  = null;
    PreparedStatement pstmC = null;
    ResultSet rs  = null;
    ResultSet rsC = null;
    try {
      pstm = conn.prepareStatement(sSQL_ART);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sDescrizione   = rs.getString(1);
        Date dDataArticolo    = rs.getDate(2);
        int iIdCategoria      = rs.getInt(3);
        iIdSottocategoria     = rs.getInt(4);
        int iIdTipoArticolo   = rs.getInt(5);
        int iIdIstituto       = rs.getInt(6);
        String sCodIstituto   = rs.getString(7);
        String sDesIstituto   = rs.getString(8);
        int iIdLuogo          = rs.getInt(9);
        String sDescLuogo     = rs.getString(10);
        int iIdTipoUtente     = rs.getInt(11);
        String sAttivo        = rs.getString(12);
        Timestamp tsDataIns   = rs.getTimestamp(13);
        String sUteIns        = rs.getString(14);
        Timestamp tsDataAgg   = rs.getTimestamp(15);
        String sUteAgg        = rs.getString(16);
        int iPrefPositive     = rs.getInt(17);
        int iPrefNegative     = rs.getInt(18);
        
        String sArticleFolder = BEConfig.getArticleFolder(iId);
        File fMultimedia = new File(sArticleFolder);
        
        result.put(sID,                   iId);
        result.put(sDESCRIZIONE,          sDescrizione);
        result.putDate(sDATA_ARTICOLO,    dDataArticolo);
        result.putNotZero(sID_CATEGORIA,  iIdCategoria);
        result.putNotZero(sID_SOTTOCATEG, iIdSottocategoria);
        result.putNotZero(sID_TIPO_ART,   iIdTipoArticolo);
        result.putNotZero(sID_ISTITUTO,   iIdIstituto);
        result.put(sCOD_ISTITUTO,         sCodIstituto);
        result.put(sDES_ISTITUTO,         sDesIstituto);
        result.putNotZero(sID_LUOGO,      iIdLuogo);
        result.put(sDESC_LUOGO,           sDescLuogo);
        result.put(sID_TIPO_UTE,          iIdTipoUtente);
        result.putBoolean(sATTIVO,        sAttivo);
        result.putDate(sDATA_INS,         tsDataIns);
        result.put(sUTENTE_INS,           sUteIns);
        result.putDate(sDATA_AGG,         tsDataAgg);
        result.put(sUTENTE_AGG,           sUteAgg);
        if(fMultimedia.exists()) {
          result.put(sMULT_FOLDER,   fMultimedia.getAbsolutePath());
        }
        else {
          if(boCreateMultFolder) {
            fMultimedia.mkdirs();
            result.put(sMULT_FOLDER, fMultimedia.getAbsolutePath());
          }
          else {
            result.put(sMULT_FOLDER, sArticleFolder);
          }
        }
        result.put(sPREF_POSITIVE, iPrefPositive);
        result.put(sPREF_NEGATIVE, iPrefNegative);
        
        boFound = true;
      }
      rs.close();
      pstm.close();
      if(!boFound) return result.toMapObject();
      
      if(iIdSottocategoria != 0) {
        Map<String, Object> mapSottocategoria = new HashMap<String, Object>();
        result.put(sDESC_SOTTOCAT, mapSottocategoria);
        pstm = conn.prepareStatement(sSQL_SOTT);
        pstm.setInt(1, iIdSottocategoria);
        rs = pstm.executeQuery();
        while(rs.next()) {
          int iIdLingua       = rs.getInt(1);
          String sDescrizione = rs.getString(2);
          if(sDescrizione == null || sDescrizione.length() == 0) continue;
          mapSottocategoria.put(String.valueOf(iIdLingua), sDescrizione);
        }
        rs.close();
        pstm.close();
      }
      
      List<Integer> listIdAutori = new ArrayList<Integer>();
      
      List<Map<String, Object>> listAutori   = new ArrayList<Map<String, Object>>();
      result.put(sAUTORI, listAutori);
      
      pstm = conn.prepareStatement(sSQL_AUT);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdAutore       = rs.getInt(1);
        int iIdTipoAutore   = rs.getInt(2);
        String sDescTipoAut = rs.getString(3);
        String sCognome     = rs.getString(4);
        String sNome        = rs.getString(5);
        String sEmail       = rs.getString(6);
        int iIdRuolo        = rs.getInt(7);
        String sDescRuolo   = rs.getString(8);
        if(iIdAutore == 0) continue;
        
        Map<String, Object> mapAutore = new HashMap<String, Object>();
        mapAutore.put(IAutore.sID,        iIdAutore);
        mapAutore.put(IAutore.sID_TIPO,   iIdTipoAutore);
        mapAutore.put(IAutore.sDESC_TIPO, sDescTipoAut);
        mapAutore.put(IAutore.sCOGNOME,   sCognome);
        mapAutore.put(IAutore.sNOME,      sNome);
        mapAutore.put(IAutore.sEMAIL,     sEmail);
        mapAutore.put(sID_RUOLO,          iIdRuolo);
        mapAutore.put(sDESC_RUOLO,        sDescRuolo);
        
        listAutori.add(mapAutore);
        if(result.isBlank(sID_AUTORE) && iIdRuolo == 0 && iIdAutore != 0) {
          result.putList(sID_AUTORE, iIdAutore, sCognome + " " + sNome);
        }
        if(iIdRuolo == 0 && iIdAutore != 0) listIdAutori.add(iIdAutore);
      }
      rs.close();
      pstm.close();
      
      Map<String, Object> mapContenuti = new HashMap<String, Object>();
      result.put(sCONTENUTI, mapContenuti);
      
      pstmC = conn.prepareStatement(sSQL_CONT);
      pstmC.setInt(1, iId);
      rsC = pstmC.executeQuery();
      while(rsC.next()) {
        int iIdLingua       = rsC.getInt(1);
        String sTitolo      = rsC.getString(2);
        String sSpecifica   = rsC.getString(3);
        String sAbstract    = rsC.getString(4);
        String sTesto       = rsC.getString(5);
        String sTesto2      = rsC.getString(6);
        String sTesto3      = rsC.getString(7);
        String sNote        = rsC.getString(8);
        String sRiferimenti = rsC.getString(9);
        if(sTesto != null && sTesto.length() > 0) {
          if(sTesto2 != null && sTesto2.length() > 0) sTesto += sTesto2;
          if(sTesto3 != null && sTesto3.length() > 0) sTesto += sTesto3;
        }
        
        Map<String, Object> mapContenuto = new HashMap<String, Object>();
        mapContenuto.put(sTITOLO,      sTitolo);
        mapContenuto.put(sSPECIFICA,   sSpecifica);
        mapContenuto.put(sABSTRACT,    sAbstract);
        mapContenuto.put(sTESTO,       sTesto);
        mapContenuto.put(sNOTE,        sNote);
        mapContenuto.put(sRIFERIMENTI, sRiferimenti);
        
        if(boComplete) {
          String sKeywords = rsC.getString(10);
          mapContenuto.put(sKEYWORDS, sKeywords);
        }
        
        mapContenuti.put(String.valueOf(iIdLingua), mapContenuto);
      }
      rsC.close();
      // pstmC non si chiude qui poiche' serve dopo
      
      String sUserHome = System.getProperty("user.home");
      
      Map<String, Object>  mapMultimedia   = new HashMap<String, Object>();
      Map<String, Integer> mapIdMultimedia = new HashMap<String, Integer>();
      mapMultimedia.put("#", mapIdMultimedia);
      result.put(sMULTIMEDIA, mapMultimedia);
      
      pstm = conn.prepareStatement(sSQL_MULT);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdMultimedia   = rs.getInt(1);
        int iIdLingua       = rs.getInt(2);
        String sURLFile     = rs.getString(3);
        String sDescrizione = rs.getString(4);
        if(sDescrizione == null) sDescrizione = "";
        if(sURLFile == null) continue;
        if(sURLFile.startsWith("${user.home}/") || sURLFile.startsWith("${user.home}\\")) {
          sURLFile = sUserHome + File.separator + sURLFile.substring(13).replace('\\', File.separatorChar);
        }
        
        mapIdMultimedia.put(sURLFile + "#" + iIdLingua, iIdMultimedia);
        Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sURLFile));
        if(mapDescrizioni == null) {
          mapDescrizioni = new HashMap<String, Object>();
          mapMultimedia.put(sURLFile, mapDescrizioni);
        }
        mapDescrizioni.put(String.valueOf(iIdLingua), sDescrizione);
      }
      ConnectionManager.close(rs, pstm);
      
      List<Map<String, Object>> listTag = new ArrayList<Map<String, Object>>();
      result.put(sTAG, listTag);
      
      pstm = conn.prepareStatement(sSQL_TAG);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdTag     = rs.getInt(1);
        String sCodice = rs.getString(2);
        String sDesc   = rs.getString(3);
        
        Map<String, Object> mapTag = new HashMap<String, Object>();
        mapTag.put(ITag.sID,          iIdTag);
        mapTag.put(ITag.sCODICE,      sCodice);
        mapTag.put(ITag.sDESC_IN_ART, sDesc);
        
        listTag.add(mapTag);
      }
      ConnectionManager.close(rs, pstm);
      
      List<Map<String, Object>> listArtComponenti = new ArrayList<Map<String, Object>>();
      result.put(sCOMPONENTI, listArtComponenti);
      
      pstm = conn.prepareStatement(sSQL_COMP);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdArtComp      = rs.getInt(1);
        String sDescrizione = rs.getString(2);
        Date dDataArticolo  = rs.getDate(3);
        if(iIdArtComp == iId) continue;
        
        Map<String, Object> mapArtComp = new HashMap<String, Object>();
        mapArtComp.put(sID,            iIdArtComp);
        mapArtComp.put(sDESCRIZIONE,   sDescrizione);
        mapArtComp.put(sDATA_ARTICOLO, dDataArticolo);
        
        Map<String, String> mapTitoli = new HashMap<String, String>();
        pstmC.setInt(1, iIdArtComp);
        rsC = pstmC.executeQuery();
        while(rsC.next()) {
          int iIdLingua  = rsC.getInt(1);
          String sTitolo = rsC.getString(2);
          if(sTitolo == null) continue;
          mapTitoli.put(String.valueOf(iIdLingua), sTitolo);
        }
        rsC.close();
        
        mapArtComp.put(sTITOLO, mapTitoli);
        
        listArtComponenti.add(mapArtComp);
      }
      ConnectionManager.close(rs, pstm);
      
      pstm = conn.prepareStatement(sSQL_PRE);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdLingua       = rs.getInt(1);
        String sCodice      = rs.getString(3);
        String sDescrizione = rs.getString(4);
        double dPrezzo      = rs.getDouble(5);
        int iSconto         = rs.getInt(6);
        double dScontato    = rs.getDouble(7);
        double dAcconto     = rs.getDouble(8);
        String sFlagPromoz  = rs.getString(9);
        
        WMap wmPrezzo = new WMap();
        wmPrezzo.put(sPREZZO_CODICE,        sCodice);
        wmPrezzo.put(sPREZZO_DESCR,         sDescrizione);
        wmPrezzo.put(sPREZZO_PREZZO,        dPrezzo);
        wmPrezzo.put(sPREZZO_SCONTO,        iSconto);
        wmPrezzo.put(sPREZZO_SCONTATO,      dScontato);
        wmPrezzo.put(sPREZZO_ACCONTO,       dAcconto);
        wmPrezzo.putBoolean(sPREZZO_PROMOZ, sFlagPromoz);
        
        String sIdLingua = String.valueOf(iIdLingua);
        Map<String, Object> mapContenuto = WUtil.toMapObject(mapContenuti.get(sIdLingua));
        if(mapContenuto == null) {
          mapContenuto = new HashMap<String, Object>();
          mapContenuti.put(sIdLingua, mapContenuto);
        }
        List<Map<String, Object>> listPrezzi = WUtil.toListOfMapObject(mapContenuto.get(sPREZZI));
        if(listPrezzi == null) {
          listPrezzi = new ArrayList<Map<String, Object>>();
          mapContenuto.put(sPREZZI, listPrezzi);
        }
        listPrezzi.add(wmPrezzo.toMapObject());
      }
      ConnectionManager.close(rs, pstm);
      
      List<Map<String, Object>> listLuoghi = null;
      pstm = conn.prepareStatement(sSQL_LUO);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        if(listLuoghi == null) {
          listLuoghi = new ArrayList<Map<String,Object>>();
          result.put(sLUOGHI, listLuoghi);
        }
        int iOrdine          = rs.getInt(1);
        int iIdLuogo         = rs.getInt(2);
        String sDescInArt    = rs.getString(3);
        int iIdTipoLuogo     = rs.getInt(4);
        String sCodLuogo     = rs.getString(5);
        String sDescLuogo    = rs.getString(6);
        int iIdComune        = rs.getInt(7);
        String sDescComune   = rs.getString(8);
        String sIndirizzo    = rs.getString(9);
        String sCap          = rs.getString(10);
        String sSitoWeb      = rs.getString(11);
        String sEmail        = rs.getString(12);
        String sTelefono1    = rs.getString(13);
        double dLatitudine   = rs.getDouble(14);
        double dLongitudine  = rs.getDouble(15);
        String sInformazioni = rs.getString(16);
        if(iIdLuogo == 0) continue;
        
        WMap wmLuogo = new WMap();
        wmLuogo.putBoolean(ILuogo.sFL_PERCORSO, iOrdine >= 0);
        wmLuogo.put(ILuogo.sID,            iIdLuogo);
        wmLuogo.put(ILuogo.sDESC_IN_ART,   sDescInArt);
        wmLuogo.put(ILuogo.sID_TIPO,       iIdTipoLuogo);
        wmLuogo.put(ILuogo.sCODICE,        sCodLuogo);
        wmLuogo.put(ILuogo.sDESCRIZIONE,   sDescLuogo);
        if(iIdComune != 0) {
          wmLuogo.put(ILuogo.sID_COMUNE,   iIdComune);
          wmLuogo.put(ILuogo.sDESC_COMUNE, sDescComune);
        }
        wmLuogo.put(ILuogo.sINDIRIZZO,     sIndirizzo);
        wmLuogo.put(ILuogo.sCAP,           sCap);
        wmLuogo.put(ILuogo.sSITO_WEB,      sSitoWeb);
        wmLuogo.put(ILuogo.sEMAIL,         sEmail);
        wmLuogo.put(ILuogo.sTEL_1,         sTelefono1);
        wmLuogo.put(ILuogo.sLATITUDINE,    dLatitudine);
        wmLuogo.put(ILuogo.sLONGITUDINE,   dLongitudine);
        wmLuogo.put(ILuogo.sINFORMAZIONI,  sInformazioni);
        
        listLuoghi.add(wmLuogo.toMapObject());
        if(result.isBlank(sID_LUOGO) && iIdLuogo != 0) {
          result.put(sID_LUOGO, iIdLuogo);
        }
      }
      ConnectionManager.close(rs, pstm);
      
      if(!boComplete) return result.toMapObject();
      
      List<Map<String, Object>> listArtCorrelati = null;
      pstm = conn.prepareStatement(sSQL_CORR);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        if(listArtCorrelati == null) {
          listArtCorrelati = new ArrayList<Map<String,Object>>();
          result.put(sCORRELATI, listArtCorrelati);
        }
        int iIdArtCorr      = rs.getInt(1);
        String sDescrizione = rs.getString(2);
        Date dDataArticolo  = rs.getDate(3);
        if(iIdArtCorr == iId) continue;
        
        WMap wmArtCorr = new WMap();
        wmArtCorr.put(sID,                iIdArtCorr);
        wmArtCorr.put(sDESCRIZIONE,       sDescrizione);
        wmArtCorr.putDate(sDATA_ARTICOLO, dDataArticolo);
        
        Map<String, Object> mapTitoli = new HashMap<String, Object>();
        pstmC.setInt(1, iIdArtCorr);
        rsC = pstmC.executeQuery();
        while(rsC.next()) {
          int iIdLingua  = rsC.getInt(1);
          String sTitolo = rsC.getString(2);
          if(sTitolo == null) continue;
          mapTitoli.put(String.valueOf(iIdLingua), sTitolo);
        }
        rsC.close();
        wmArtCorr.put(sTITOLO, mapTitoli);
        
        listArtCorrelati.add(wmArtCorr.toMapObject());
      }
      ConnectionManager.close(rs, pstm);
      
      if(listIdAutori != null && listIdAutori.size() > 0) {
        List<Map<String, Object>> listArtStessoAutore = new ArrayList<Map<String,Object>>();
        result.put(sSTESSO_AUTORE, listArtStessoAutore);
        
        String sSQL_SA = "SELECT A.ID_ARTICOLO,A.DESCRIZIONE,A.DATA_ARTICOLO FROM CMS_ARTICOLI A,CMS_ARTICOLI_AUT AU ";
        sSQL_SA += "WHERE A.ID_ARTICOLO=AU.ID_ARTICOLO AND AU.ID_RUOLO=0 ";
        if(listIdAutori.size() == 1) {
          sSQL_SA += "AND AU.ID_AUTORE=? ";
        }
        else {
          sSQL_SA += "AND AU.ID_AUTORE IN (" + DB.buildInSet(listIdAutori.size()) + ") ";
        }
        sSQL_SA += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO";
        pstm = conn.prepareStatement(sSQL_SA);
        for(int i = 0; i < listIdAutori.size(); i++) {
          Integer oIdAutore = listIdAutori.get(i);
          if(oIdAutore == null) oIdAutore = new Integer(0);
          pstm.setInt(i+1, oIdAutore.intValue());
        }
        rs = pstm.executeQuery();
        int iLastIdArticolo = 0;
        int iCount = 0;
        while(rs.next()) {
          int iIdArticolo     = rs.getInt(1);
          String sDescrizione = rs.getString(2);
          Date dDataArticolo  = rs.getDate(3);
          if(iIdArticolo == iId) continue;
          if(iIdArticolo == iLastIdArticolo) continue;
          
          Map<String, Object> mapArtStessoAut = new HashMap<String, Object>();
          mapArtStessoAut.put(sID,            iIdArticolo);
          mapArtStessoAut.put(sDESCRIZIONE,   sDescrizione);
          mapArtStessoAut.put(sDATA_ARTICOLO, dDataArticolo);
          
          Map<String, Object> mapTitoli = new HashMap<String, Object>();
          pstmC.setInt(1, iIdArticolo);
          rsC = pstmC.executeQuery();
          while(rsC.next()) {
            int iIdLingua  = rsC.getInt(1);
            String sTitolo = rsC.getString(2);
            if(sTitolo == null) continue;
            mapTitoli.put(String.valueOf(iIdLingua), sTitolo);
          }
          rsC.close();
          mapArtStessoAut.put(sTITOLO, mapTitoli);
          
          listArtStessoAutore.add(mapArtStessoAut);
          iCount++;
          iLastIdArticolo = iIdArticolo;
          if(iCount >= 10) break;
        }
      }
    }
    catch(Exception ex) {
      oLogger.error("Exception in WSArticoli.read(conn," + iId + "," + boComplete + "," + boCreateMultFolder + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, rsC, pstm, pstmC);
    }
    return result.toMapObject();
  }
  
  public static
  List<Map<String, Object>> findMult(int iIdTipoContenuto, int iIdCategoria, int iIdSottocategoria, int iIdTipoArticolo, int iIdTipoUtente, int iIdTag, int iIdComune, int iPage, int iMultPerPage, int iMultPagePrev, boolean boDoNotCount)
      throws Exception
  {
    if(iIdCategoria      == Integer.MIN_VALUE) iIdCategoria      = Integer.MAX_VALUE;
    if(iIdSottocategoria == Integer.MIN_VALUE) iIdSottocategoria = Integer.MAX_VALUE;
    
    if(iPage < 1) iPage = 1;
    boolean boBreakAtYearChange = false;
    if(iMultPerPage < 1) {
      iMultPerPage = 1000;
      boBreakAtYearChange = true;
    }
    int iMinIndex = (iPage - 1) * iMultPerPage;
    int iMaxIndex = iMinIndex + iMultPerPage - 1;
    if(iMultPagePrev > 0) {
      if(iMinIndex > iMultPagePrev) {
        iMinIndex = iMinIndex - iMultPagePrev;
      }
      else {
        iMinIndex = 0;
      }
    }
    
    String sSQL = "SELECT DISTINCT M.ID_MULTIMEDIA,A.ID_ARTICOLO,A.DATA_ARTICOLO ";
    if(iIdComune != 0) {
      if(iIdTag != 0) {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_MULT M,CMS_ARTICOLI_LUOGHI AL,CMS_LUOGHI L,CMS_ARTICOLI_TAG T ";
        sSQL += "WHERE A.ID_ARTICOLO=M.ID_ARTICOLO AND A.ID_ARTICOLO=AL.ID_ARTICOLO AND AL.ID_LUOGO=L.ID_LUOGO AND A.ID_ARTICOLO=T.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
      else {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_MULT M,CMS_ARTICOLI_LUOGHI AL,CMS_LUOGHI L ";
        sSQL += "WHERE A.ID_ARTICOLO=M.ID_ARTICOLO AND A.ID_ARTICOLO=AL.ID_ARTICOLO AND AL.ID_LUOGO=L.ID_LUOGO AND A.ATTIVO = ? ";
      }
    }
    else {
      if(iIdTag != 0) {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_MULT M,CMS_ARTICOLI_TAG T ";
        sSQL += "WHERE A.ID_ARTICOLO=M.ID_ARTICOLO AND A.ID_ARTICOLO=T.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
      else {
        sSQL += "FROM CMS_ARTICOLI A,CMS_ARTICOLI_MULT M ";
        sSQL += "WHERE A.ID_ARTICOLO=M.ID_ARTICOLO AND A.ATTIVO = ? ";
      }
    }
    if(iIdTipoContenuto > 0) {
      sSQL += "AND M.ID_TIPO_CONTENUTO = ? ";
    }
    else if(iIdTipoContenuto < 0) {
      iIdTipoContenuto = iIdTipoContenuto * -1;
      sSQL += "AND M.ID_TIPO_CONTENUTO <> ? AND M.ID_TIPO_CONTENUTO > 0 ";
    }
    if(iIdCategoria > 0) {
      sSQL += "AND A.ID_CATEGORIA = ? ";
    }
    else if(iIdCategoria < 0) {
      iIdCategoria = iIdCategoria * -1;
      sSQL += "AND A.ID_CATEGORIA <> ? AND A.ID_CATEGORIA > 0 ";
    }
    if(iIdSottocategoria > 0) {
      sSQL += "AND A.ID_SOTTOCATEGORIA = ? ";
    }
    else if(iIdSottocategoria < 0) {
      iIdSottocategoria = iIdSottocategoria * -1;
      sSQL += "AND A.ID_SOTTOCATEGORIA <> ? AND A.ID_SOTTOCATEGORIA > 0 ";
    }
    if(iIdTipoArticolo > 0) {
      sSQL += "AND A.ID_TIPO_ARTICOLO = ? ";
    }
    else if(iIdTipoArticolo < 0) {
      iIdTipoArticolo = iIdTipoArticolo * -1;
      sSQL += "AND A.ID_TIPO_ARTICOLO <> ? AND A.ID_TIPO_ARTICOLO > 0 ";
    }
    if(iIdTipoUtente > 0) {
      sSQL += "AND A.ID_TIPO_UTENTE = ? ";
    }
    else if(iIdTipoUtente < 0) {
      iIdTipoUtente = iIdTipoUtente * -1;
      sSQL += "AND A.ID_TIPO_UTENTE < ? ";
    }
    if(iIdTag > 0) {
      sSQL += "AND T.ID_TAG = ? ";
    }
    else if(iIdTag < 0) {
      iIdTag = iIdTag * -1;
      sSQL += "AND T.ID_TAG <> ? AND T.ID_TAG > 0 ";
    }
    if(iIdComune > 0) {
      sSQL += "AND L.ID_COMUNE = ? ";
    }
    else if(iIdComune < 0) {
      iIdComune = iIdComune * -1;
      sSQL += "AND L.ID_COMUNE <> ? ";
    }
    sSQL += "ORDER BY A.DATA_ARTICOLO DESC,A.ID_ARTICOLO DESC,M.ID_MULTIMEDIA";
    
    Calendar cal = Calendar.getInstance();
    int iLastYear = 0;
    int i = -1;
    int p = 0;
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    Connection conn = null;
    PreparedStatement pstm  = null;
    PreparedStatement pstmM = null;
    ResultSet rsM = null;
    ResultSet rs  = null;
    try {
      conn  = ConnectionManager.getDefaultConnection();
      pstm  = conn.prepareStatement(sSQL);
      pstmM = conn.prepareStatement("SELECT ID_LINGUA,URL_FILE,DESCRIZIONE FROM CMS_ARTICOLI_MULT WHERE ID_MULTIMEDIA=?");
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      if(iIdTipoContenuto  != 0) pstm.setInt(++p, iIdTipoContenuto);
      if(iIdCategoria      != 0) pstm.setInt(++p, iIdCategoria      < Integer.MAX_VALUE ? iIdCategoria      : 0);
      if(iIdSottocategoria != 0) pstm.setInt(++p, iIdSottocategoria < Integer.MAX_VALUE ? iIdSottocategoria : 0);
      if(iIdTipoArticolo   != 0) pstm.setInt(++p, iIdTipoArticolo);
      if(iIdTipoUtente     != 0) pstm.setInt(++p, iIdTipoUtente);
      if(iIdTag            != 0) pstm.setInt(++p, iIdTag);
      if(iIdComune         != 0) pstm.setInt(++p, iIdComune);
      rs = pstm.executeQuery();
      while(rs.next()) {
        i++;
        if(i < iMinIndex) continue;
        if(i > iMaxIndex) {
          if(boDoNotCount) break;
          continue;
        }
        if(boBreakAtYearChange) {
          java.sql.Date dtArt = rs.getDate("DATA_ARTICOLO");
          if(dtArt != null) {
            cal.setTimeInMillis(dtArt.getTime());
            int iYear = cal.get(Calendar.YEAR);
            if(iLastYear != 0 && iLastYear != iYear) break;
            iLastYear = iYear;
          }
        }
        int iIdMul = rs.getInt("ID_MULTIMEDIA");
        int iIdArt = rs.getInt("ID_ARTICOLO");
        
        Map<String, Object> mapMultimedia = new HashMap<String, Object>();
        pstmM.setInt(1, iIdMul);
        rsM = pstmM.executeQuery();
        while(rsM.next()) {
          int iIdLingua       = rsM.getInt("ID_LINGUA");
          String sURLFile     = rsM.getString("URL_FILE");
          String sDescrizione = rsM.getString("DESCRIZIONE");
          if(sURLFile == null || sDescrizione == null) continue;
          if(sURLFile.startsWith("${user.home}/") || sURLFile.startsWith("${user.home}\\")) {
            String sUserHome = System.getProperty("user.home");
            sURLFile = sUserHome + File.separator + sURLFile.substring(13).replace('\\', File.separatorChar);
          }
          mapMultimedia.put(sID_MULTIMEDIA, iIdMul);
          mapMultimedia.put(sURL_FILE,      sURLFile);
          Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sDESCRIZIONE));
          if(mapDescrizioni == null) {
            mapDescrizioni = new HashMap<String, Object>();
            mapMultimedia.put(sDESCRIZIONE, mapDescrizioni);
          }
          mapDescrizioni.put(String.valueOf(iIdLingua), sDescrizione);
        }
        rsM.close();
        if(mapMultimedia.isEmpty()) continue;
        
        mapMultimedia.put(sID, iIdArt);
        result.add(mapMultimedia);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.findMult(" + iIdTipoContenuto + "," + iIdCategoria + "," + iIdSottocategoria + "," + iIdTipoArticolo + "," + iIdTipoUtente + "," + iIdTag + "," + iIdComune + "," + iPage + "," + iMultPerPage + "," + iMultPagePrev + "," + boDoNotCount + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, rsM, pstm, pstmM, conn);
    }
    if(result != null && result.size() > 0) {
      int iModPages = (i + 1) % iMultPerPage;
      int iPages = 0;
      if(iModPages == 0) {
        iPages = (i + 1) / iMultPerPage;
      }
      else {
        iPages = (i + 1) / iMultPerPage + 1;
      }
      Map<String, Object> map0 = result.get(0);
      map0.put(sPAGINA, iPage);
      map0.put(sPAGINE, iPages);
      map0.put(sCOUNT,  i + 1);
    }
    return result;
  }
  
  public static
  Map<String, Object> insert(Connection conn, Map<String, Object> htValues, Map<String, Integer> htIdMultimedia)
      throws Exception
  {
    if(htValues == null || htValues.isEmpty() || htValues.get(sID) == null) {
      return new HashMap<String, Object>();
    }
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_ARTICOLO");
    qb.add("DESCRIZIONE");
    qb.add("DATA_ARTICOLO");
    qb.add("ID_CATEGORIA");
    qb.add("ID_SOTTOCATEGORIA");
    qb.add("ID_TIPO_ARTICOLO");
    qb.add("ID_ISTITUTO");
    qb.add("ID_LUOGO");
    qb.add("ID_TIPO_UTENTE");
    qb.add("ATTIVO");
    qb.add("DT_INS");
    qb.add("UTE_INS");
    qb.add("DT_AGG");
    qb.add("UTE_AGG");
    String sSQL_INS_ART = qb.insert("CMS_ARTICOLI", true);
    qb.init();
    qb.add("DESCRIZIONE");
    qb.add("DATA_ARTICOLO");
    qb.add("ID_CATEGORIA");
    qb.add("ID_SOTTOCATEGORIA");
    qb.add("ID_TIPO_ARTICOLO");
    qb.add("ID_ISTITUTO");
    qb.add("ID_LUOGO");
    qb.add("ID_TIPO_UTENTE");
    qb.add("DT_AGG");
    qb.add("UTE_AGG");
    String sSQL_UPD_ART = qb.update("CMS_ARTICOLI", true);
    sSQL_UPD_ART += " WHERE ID_ARTICOLO=?";
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_AUTORE");
    qb.add("ID_RUOLO");
    String sSQL_AUT = qb.insert("CMS_ARTICOLI_AUT", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_LUOGO");
    qb.add("ORDINE");
    qb.add("DESCRIZIONE");
    String sSQL_LUO = qb.insert("CMS_ARTICOLI_LUOGHI", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_LINGUA");
    qb.add("TITOLO");
    qb.add("SPECIFICA");
    qb.add("ABSTRACT");
    qb.add("TESTO");
    qb.add("TESTO2");
    qb.add("TESTO3");
    qb.add("NOTE");
    qb.add("RIFERIMENTI");
    qb.add("KEYWORDS");
    String sSQL_CONT = qb.insert("CMS_ARTICOLI_CONT", true);
    qb.init();
    qb.add("ID_MULTIMEDIA");
    qb.add("ID_LINGUA");
    qb.add("ID_TIPO_CONTENUTO");
    qb.add("URL_FILE");
    qb.add("ID_ARTICOLO");
    qb.add("ORDINE");
    qb.add("DESCRIZIONE");
    String sSQL_MULT = qb.insert("CMS_ARTICOLI_MULT", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_TAG");
    qb.add("DESCRIZIONE");
    String sSQL_TAG = qb.insert("CMS_ARTICOLI_TAG", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_ARTICOLO_CORR");
    qb.add("ORDINE");
    String sSQL_CORR = qb.insert("CMS_ARTICOLI_CORR", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_ARTICOLO_COMP");
    qb.add("ORDINE");
    String sSQL_COMP = qb.insert("CMS_ARTICOLI_COMP", true);
    qb.init();
    qb.add("ID_ARTICOLO");
    qb.add("ID_LINGUA");
    qb.add("ORDINE");
    qb.add("CODICE");
    qb.add("DESCRIZIONE");
    qb.add("PREZZO");
    qb.add("SCONTO");
    qb.add("SCONTATO");
    qb.add("ACCONTO");
    qb.add("PROMOZIONE");
    String sSQL_PRE = qb.insert("CMS_ARTICOLI_PREZZI", true);
    
    long lCurrentTimeMillis = System.currentTimeMillis();
    
    PreparedStatement pstm_IArt = null;
    PreparedStatement pstm_UArt = null;
    PreparedStatement pstm_Aut  = null;
    PreparedStatement pstm_Luo  = null;
    PreparedStatement pstm_Cont = null;
    PreparedStatement pstm_Mult = null;
    PreparedStatement pstm_Tag  = null;
    PreparedStatement pstm_DTag = null;
    PreparedStatement pstm_Corr = null;
    PreparedStatement pstm_Comp = null;
    PreparedStatement pstm_Pre  = null;
    try {
      pstm_IArt = conn.prepareStatement(sSQL_INS_ART);
      pstm_UArt = conn.prepareStatement(sSQL_UPD_ART);
      pstm_Aut  = conn.prepareStatement(sSQL_AUT);
      
      WMap dmArticolo  = new WMap(htValues);
      int iIdArticolo     = dmArticolo.getInt(sID);
      String sDescrizione = dmArticolo.getString(sDESCRIZIONE, String.valueOf(iIdArticolo));
      Date dDataArticolo  = dmArticolo.getSQLDate(sDATA_ARTICOLO, new java.sql.Date(lCurrentTimeMillis));
      int iIdCategoria    = dmArticolo.getInt(sID_CATEGORIA);
      int iIdSottoCateg   = dmArticolo.getInt(sID_SOTTOCATEG);
      int iIdTipoArticolo = dmArticolo.getInt(sID_TIPO_ART);
      int iIdIstituto     = dmArticolo.getInt(sID_ISTITUTO);
      int iIdLuogo        = dmArticolo.getInt(sID_LUOGO);
      int iIdTipoUtente   = dmArticolo.getInt(sID_TIPO_UTE);
      String sAttivo      = QueryBuilder.decodeBoolean(true);
      Timestamp tsDtIns   = dmArticolo.getSQLTimestamp(sDATA_INS);
      if(tsDtIns == null) tsDtIns = new Timestamp(lCurrentTimeMillis);
      String sUteIns      = dmArticolo.getString(sUTENTE_INS, "admin");
      Timestamp tsDtAgg   = new Timestamp(lCurrentTimeMillis);
      String sUteAgg      = dmArticolo.getString(sUTENTE_AGG, sUteIns);
      
      List<?> listLuoghi  = dmArticolo.getList(sLUOGHI);
      if(iIdLuogo == 0 && listLuoghi != null && listLuoghi.size() > 0) {
        Object oLuogo = listLuoghi.get(0);
        if(oLuogo != null) {
          Object oIdLuogo = null;
          if(oLuogo instanceof  Map) {
            Map<String, Object> mapLuogo = WUtil.toMapObject(oLuogo);
            iIdLuogo = WUtil.toInt(mapLuogo.get(ILuogo.sID), 0);
          }
          else if(oLuogo instanceof List) {
            iIdLuogo = WUtil.toInt(WUtil.getFirst(oLuogo), 0);
          }
          else {
            iIdLuogo = WUtil.toInt(oIdLuogo, 0);
          }
        }
      }
      
      int p = 0;
      pstm_UArt.setString(++p,    sDescrizione);
      pstm_UArt.setDate(++p,      dDataArticolo);
      pstm_UArt.setInt(++p,       iIdCategoria);
      pstm_UArt.setInt(++p,       iIdSottoCateg);
      pstm_UArt.setInt(++p,       iIdTipoArticolo);
      pstm_UArt.setInt(++p,       iIdIstituto);
      pstm_UArt.setInt(++p,       iIdLuogo);
      pstm_UArt.setInt(++p,       iIdTipoUtente);
      pstm_UArt.setTimestamp(++p, tsDtAgg);
      pstm_UArt.setString(++p,    sUteAgg);
      pstm_UArt.setInt(++p,       iIdArticolo);
      int iRows = pstm_UArt.executeUpdate();
      if(iRows == 0) {
        p = 0;
        pstm_IArt.setInt(++p,       iIdArticolo);
        pstm_IArt.setString(++p,    sDescrizione);
        pstm_IArt.setDate(++p,      dDataArticolo);
        pstm_IArt.setInt(++p,       iIdCategoria);
        pstm_IArt.setInt(++p,       iIdSottoCateg);
        pstm_IArt.setInt(++p,       iIdTipoArticolo);
        pstm_IArt.setInt(++p,       iIdIstituto);
        pstm_IArt.setInt(++p,       iIdLuogo);
        pstm_IArt.setInt(++p,       iIdTipoUtente);
        pstm_IArt.setString(++p,    sAttivo);
        pstm_IArt.setTimestamp(++p, tsDtIns);
        pstm_IArt.setString(++p,    sUteIns);
        pstm_IArt.setTimestamp(++p, tsDtAgg);
        pstm_IArt.setString(++p,    sUteAgg);
        pstm_IArt.executeUpdate();
      }
      
      int iIdAutorePrincipale = dmArticolo.getInt(sID_AUTORE);
      int iIdRuoloPrincipale  = 0;
      if(iIdAutorePrincipale != 0) {
        pstm_Aut.setInt(1, iIdArticolo);
        pstm_Aut.setInt(2, iIdAutorePrincipale);
        pstm_Aut.setInt(3, iIdRuoloPrincipale);
        pstm_Aut.executeUpdate();
      }
      
      List<?> listAutori = dmArticolo.getList(sAUTORI);
      if(listAutori != null && listAutori.size() > 0) {
        for(int i = 0; i < listAutori.size(); i++) {
          Object oAutore  = listAutori.get(i);
          if(oAutore == null) continue;
          int iIdAutore = 0;
          int iIdRuolo  = 2;
          if(oAutore instanceof  Map) {
            Map<String, Object> mapAutore = WUtil.toMapObject(oAutore);
            iIdAutore = WUtil.toInt(mapAutore.get(IAutore.sID),       0);
            iIdRuolo  = WUtil.toInt(mapAutore.get(IAutore.sID_RUOLO), 2);
          }
          else if(oAutore instanceof List) {
            iIdAutore = WUtil.toInt(WUtil.getFirst(oAutore), 0);
          }
          else {
            iIdAutore = WUtil.toInt(oAutore, 0);
          }
          pstm_Aut.setInt(1, iIdArticolo);
          pstm_Aut.setInt(2, iIdAutore);
          pstm_Aut.setInt(3, iIdRuolo);
          pstm_Aut.executeUpdate();
        }
      }
      
      if(listLuoghi != null && listLuoghi.size() > 0) {
        boolean boFlagPercorso = false;
        Object oLuogo0  = listLuoghi.get(0);
        if(oLuogo0 instanceof Map) {
          boFlagPercorso = WUtil.toBoolean(((Map<?, ?>) oLuogo0).get(ILuogo.sFL_PERCORSO), false);
        }
        int iOrdine = boFlagPercorso ? 0 : (listLuoghi.size() + 1) * -1;
        pstm_Luo  = conn.prepareStatement(sSQL_LUO);
        for(int i = 0; i < listLuoghi.size(); i++) {
          Place place = WSLuoghi.toPlace(listLuoghi.get(i), 0);
          if(place == null || place.getId() == 0) continue;
          
          iOrdine++;
          pstm_Luo.setInt(1,    iIdArticolo);
          pstm_Luo.setInt(2,    place.getId());
          pstm_Luo.setInt(3,    iOrdine);
          pstm_Luo.setString(4, place.getDisplayName());
          pstm_Luo.executeUpdate();
        }
      }
      
      String sTags = "";
      Map<Integer, List<String>> mapDescTags = new HashMap<Integer, List<String>>();
      List<Integer> listIdTag = new ArrayList<Integer>();
      List<?> listTag = dmArticolo.getList(sTAG);
      if(listTag != null && listTag.size() > 0) {
        pstm_DTag = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_TAG_DESC WHERE ID_TAG=?");
        pstm_Tag  = conn.prepareStatement(sSQL_TAG);
        for(int i = 0; i < listTag.size(); i++) {
          Tag tag = WSTag.toTag(listTag.get(i), 0);
          if(tag == null || tag.getId() == 0) continue;
          
          pstm_Tag.setInt(1,    iIdArticolo);
          pstm_Tag.setInt(2,    tag.getId());
          pstm_Tag.setString(3, tag.getDisplayName());
          pstm_Tag.executeUpdate();
          
          listIdTag.add(tag.getId());
          
          pstm_DTag.setInt(1, tag.getId());
          ResultSet rs = pstm_DTag.executeQuery();
          while(rs.next()) {
            int iIdLingua = rs.getInt(1);
            List<String> listDescTags = mapDescTags.get(new Integer(iIdLingua));
            if(listDescTags == null) {
              listDescTags = new ArrayList<String>();
              mapDescTags.put(new Integer(iIdLingua), listDescTags);
            }
            String tagDisplayName = tag.getDisplayName();
            if(iIdLingua == 0 && tagDisplayName != null && tagDisplayName.length() > 0) {
              listDescTags.addAll(getWords(tagDisplayName));
            }
            else {
              String sDescTag = rs.getString(2);
              if(sDescTag != null && sDescTag.length() > 0) {
                listDescTags.addAll(getWords(sDescTag));
              }
            }
          }
          rs.close();
        }
        Collections.sort(listIdTag);
        for(int i = 0; i < listIdTag.size(); i++) {
          sTags += ",#" + listIdTag.get(i);
        }
      }
      
      Map<String, Object> mapContenuti = dmArticolo.getMapObject(sCONTENUTI);
      if(mapContenuti != null && !mapContenuti.isEmpty()) {
        pstm_Cont = conn.prepareStatement(sSQL_CONT);
        pstm_Pre  = conn.prepareStatement(sSQL_PRE);
        Iterator<Map.Entry<String, Object>> iterator = mapContenuti.entrySet().iterator();
        while(iterator.hasNext()) {
          Map.Entry<String, Object> entry  = iterator.next();
          int iIdLingua = WUtil.toInt(entry.getKey(), 0);
          
          WMap wmContenuto    = new WMap(WUtil.toMapObject(entry.getValue()));
          String sTitolo      = wmContenuto.getString(sTITOLO);
          String sSpecifica   = wmContenuto.getString(sSPECIFICA);
          String sAbstract    = wmContenuto.getString(sABSTRACT);
          String sTesto       = wmContenuto.getString(sTESTO);
          String sNote        = wmContenuto.getString(sNOTE);
          String sRiferimenti = wmContenuto.getString(sRIFERIMENTI);
          String sKeywords    = wmContenuto.getLowerString(sKEYWORDS);
          String sTesto2      = null;
          String sTesto3      = null;
          if(sKeywords != null && sKeywords.length() > 3 && sKeywords.charAt(0) != '.') {
            sKeywords = normalizeKeywords(sKeywords);
          }
          else {
            if(sTesto != null && sTesto.length() > 5) {
              sKeywords = getKeywords(sTesto, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else if(sAbstract != null && sAbstract.length() > 5) {
              sKeywords = getKeywords(sAbstract, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else if(sSpecifica != null && sSpecifica.length() > 1) {
              sKeywords = getKeywords(sTitolo + " " + sSpecifica, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else {
              sKeywords = getKeywords(sTitolo, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
          }
          if(sAbstract != null && sAbstract.length() > iMAX_TEXT_LENGTH) {
            sAbstract = sAbstract.substring(0, iMAX_TEXT_LENGTH);
          }
          if(sTesto != null && sTesto.length() >= iMAX_TEXT_LENGTH) {
            sTesto2 = sTesto.substring(iMAX_TEXT_LENGTH);
            sTesto  = sTesto.substring(0, iMAX_TEXT_LENGTH);
          }
          if(sTesto2 != null && sTesto2.length() >= iMAX_TEXT_LENGTH) {
            sTesto3 = sTesto2.substring(iMAX_TEXT_LENGTH);
            sTesto2 = sTesto2.substring(0, iMAX_TEXT_LENGTH);
          }
          if(sTesto3 != null && sTesto3.length() >= iMAX_TEXT_LENGTH) {
            sTesto3 = sTesto3.substring(iMAX_TEXT_LENGTH);
          }
          if(sNote != null && sNote.length() > iMAX_TEXT_LENGTH) {
            sNote = sNote.substring(0, iMAX_TEXT_LENGTH);
          }
          if(sRiferimenti != null && sRiferimenti.length() > iMAX_TEXT_LENGTH) {
            sRiferimenti = sRiferimenti.substring(0, iMAX_TEXT_LENGTH);
          }
          if(sKeywords != null && sKeywords.length() > iMAX_TEXT_LENGTH) {
            sKeywords = sKeywords.substring(0, iMAX_TEXT_LENGTH - 1) + ",";
          }
          p = 0;
          pstm_Cont.setInt(++p, iIdArticolo);
          pstm_Cont.setInt(++p, iIdLingua);
          pstm_Cont.setString(++p, sTitolo);
          pstm_Cont.setString(++p, sSpecifica);
          pstm_Cont.setString(++p, sAbstract);
          pstm_Cont.setString(++p, sTesto);
          pstm_Cont.setString(++p, sTesto2);
          pstm_Cont.setString(++p, sTesto3);
          pstm_Cont.setString(++p, sNote);
          pstm_Cont.setString(++p, sRiferimenti);
          pstm_Cont.setString(++p, sKeywords);
          pstm_Cont.executeUpdate();
          
          List<Map<String, Object>> listPrezzi = wmContenuto.getListOfMapObject(sPREZZI);
          if(listPrezzi != null && listPrezzi.size() > 0) {
            for(int i = 0; i < listPrezzi.size(); i++) {
              WMap    wmPrezzo   = new WMap(listPrezzi.get(i));
              String  sPreCodice = wmPrezzo.getString(sPREZZO_CODICE);
              String  sPreDescr  = wmPrezzo.getString(sPREZZO_DESCR);
              double  dPrezzo    = wmPrezzo.getDouble(sPREZZO_PREZZO);
              int     iSconto    = wmPrezzo.getInt(sPREZZO_SCONTO);
              double  dScontato  = wmPrezzo.getDouble(sPREZZO_SCONTATO);
              double  dAcconto   = wmPrezzo.getInt(sPREZZO_ACCONTO);
              boolean boPromoz   = wmPrezzo.getBoolean(sPREZZO_PROMOZ);
              
              p = 0;
              pstm_Pre.setInt(++p,    iIdArticolo);
              pstm_Pre.setInt(++p,    iIdLingua);
              pstm_Pre.setInt(++p,    i + 1);
              pstm_Pre.setString(++p, sPreCodice);
              pstm_Pre.setString(++p, sPreDescr);
              pstm_Pre.setDouble(++p, dPrezzo);
              pstm_Pre.setInt(++p,    iSconto);
              pstm_Pre.setDouble(++p, dScontato);
              pstm_Pre.setDouble(++p, dAcconto);
              pstm_Pre.setString(++p, QueryBuilder.decodeBoolean(boPromoz));
              pstm_Pre.executeUpdate();
            }
          }
        }
      }
      
      List<String> vMultimedia = getContenutiMultimediali(iIdArticolo);
      if(vMultimedia != null && !vMultimedia.isEmpty()) {
        pstm_Mult = conn.prepareStatement(sSQL_MULT);
        
        Map<String, Object> mapMultimedia = dmArticolo.getMapObject(sMULTIMEDIA);
        if(mapMultimedia == null) mapMultimedia = new HashMap<String, Object>();
        
        String sArticleRelFolder = BEConfig.getArticleRelFolder(iIdArticolo);
        for(int i = 0; i < vMultimedia.size(); i++) {
          String sURLFile  = vMultimedia.get(i);
          String sFileName = getFileName(sURLFile);
          
          Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sURLFile));
          if(mapDescrizioni == null || mapDescrizioni.isEmpty()) {
            String sDescFile = sFileName;
            int iSepExt = sDescFile.lastIndexOf('.');
            if(iSepExt > 0) sDescFile = sDescFile.substring(0, iSepExt);
            mapDescrizioni = new HashMap<String, Object>();
            mapDescrizioni.put("0", sDescFile.replace('_', ' '));
          }
          
          Iterator<Map.Entry<String, Object>> iterator = mapDescrizioni.entrySet().iterator();
          while(iterator.hasNext()) {
            Map.Entry<String, Object> entry  = iterator.next();
            Object oIdLingua = entry.getKey();
            int iIdLingua = 0;
            try{ iIdLingua = Integer.parseInt(oIdLingua.toString()); } catch(Exception ex) {}
            Object oDescrizione = entry.getValue();
            String sDescMultimedia = oDescrizione != null ? oDescrizione.toString() : "";
            
            String sRelURLFile = sArticleRelFolder + File.separator + sFileName;
            Integer oIdMultimedia = htIdMultimedia != null ? (Integer) htIdMultimedia.get(iIdLingua + "#" + sRelURLFile) : null;
            int iIdMultimedia = 0;
            if(oIdMultimedia != null && oIdMultimedia.intValue() != 0) {
              iIdMultimedia = oIdMultimedia.intValue();
            }
            else {
              iIdMultimedia = ConnectionManager.nextVal(conn, "SEQ_CMS_ARTICOLI_MULT");
            }
            
            p = 0;
            pstm_Mult.setInt(++p, iIdMultimedia);
            pstm_Mult.setInt(++p, iIdLingua);
            pstm_Mult.setInt(++p, getIdTipoContenuto(sURLFile));
            pstm_Mult.setString(++p, sRelURLFile);
            pstm_Mult.setInt(++p, iIdArticolo);
            pstm_Mult.setInt(++p, 0);
            pstm_Mult.setString(++p, sDescMultimedia);
            pstm_Mult.executeUpdate();
          }
        }
      }
      
      List<?> listArtCorrelati = dmArticolo.getList(sCORRELATI);
      if(listArtCorrelati != null && listArtCorrelati.size() > 0) {
        pstm_Corr = conn.prepareStatement(sSQL_CORR);
        for(int i = 0; i < listArtCorrelati.size(); i++) {
          Object oArticolo = listArtCorrelati.get(i);
          if(oArticolo == null) continue;
          int iIdArticoloCorr = 0;
          if(oArticolo instanceof  Map) {
            Map<String, Object> mapArticolo = WUtil.toMapObject(oArticolo);
            iIdArticoloCorr = WUtil.toInt(mapArticolo.get(IArticolo.sID), 0);
          }
          else if(oArticolo instanceof List) {
            iIdArticoloCorr = WUtil.toInt(WUtil.getFirst(oArticolo), 0);
          }
          else {
            iIdArticoloCorr = WUtil.toInt(oArticolo, 0);
          }
          if(iIdArticoloCorr == 0) continue;
          pstm_Corr.setInt(1, iIdArticolo);
          pstm_Corr.setInt(2, iIdArticoloCorr);
          pstm_Corr.setInt(3, i+1);
          pstm_Corr.executeUpdate();
        }
      }
      
      List<?> listArtComponenti = dmArticolo.getList(sCOMPONENTI);
      if(listArtComponenti != null && listArtComponenti.size() > 0) {
        pstm_Comp = conn.prepareStatement(sSQL_COMP);
        for(int i = 0; i < listArtComponenti.size(); i++) {
          Object oArticolo = listArtComponenti.get(i);
          if(oArticolo == null) continue;
          int iIdArticoloComp = 0;
          if(oArticolo instanceof  Map) {
            Map<String, Object> mapArticolo = WUtil.toMapObject(oArticolo);
            iIdArticoloComp = WUtil.toInt(mapArticolo.get(IArticolo.sID), 0);
          }
          else if(oArticolo instanceof List) {
            iIdArticoloComp = WUtil.toInt(WUtil.getFirst(oArticolo), 0);
          }
          else {
            iIdArticoloComp = WUtil.toInt(oArticolo, 0);
          }
          if(iIdArticoloComp == 0) continue;
          pstm_Comp.setInt(1, iIdArticolo);
          pstm_Comp.setInt(2, iIdArticoloComp);
          pstm_Comp.setInt(3, i+1);
          pstm_Comp.executeUpdate();
        }
      }
    }
    finally {
      ConnectionManager.close(pstm_IArt, pstm_UArt, pstm_Aut, pstm_Luo, pstm_Cont, pstm_Mult, pstm_Tag, pstm_DTag, pstm_Corr, pstm_Comp, pstm_Pre);
    }
    CMSCache.mapCount.clear();
    CMSCache.mapYears.clear();
    CMSCache.mapMonths.clear();
    return htValues;
  }
  
  public static
  boolean updateKeywords(int iId)
      throws Exception
  {
    boolean boResult = false;
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      Map<String, Object> mapValues = read(conn, iId, true, false);
      if(mapValues != null && !mapValues.isEmpty()) {
        boResult = updateKeywords(conn, mapValues);
      }
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.updateKeywords(" + iId + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return boResult;
  }
  
  public static
  boolean updateKeywords(Connection conn, Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty() || mapValues.get(sID) == null) {
      return false;
    }
    
    PreparedStatement pstm_Cont = null;
    PreparedStatement pstm_DTag = null;
    try {
      pstm_Cont = conn.prepareStatement("UPDATE CMS_ARTICOLI_CONT SET KEYWORDS=? WHERE ID_ARTICOLO=? AND ID_LINGUA=?");
      
      WMap wmArticolo = new WMap(mapValues);
      int iIdArticolo = wmArticolo.getInt(sID);
      
      String sTags = "";
      Map<Integer, List<String>> mapDescTags = new HashMap<Integer, List<String>>();
      List<Integer> listIdTag = new ArrayList<Integer>();
      List<?> listTag = wmArticolo.getList(sTAG);
      if(listTag != null && listTag.size() > 0) {
        pstm_DTag = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_TAG_DESC WHERE ID_TAG=?");
        for(int i = 0; i < listTag.size(); i++) {
          Tag tag = WSTag.toTag(listTag, 0);
          if(tag == null || tag.getId() == 0) continue;
          
          listIdTag.add(tag.getId());
          
          pstm_DTag.setInt(1, tag.getId());
          ResultSet rs = pstm_DTag.executeQuery();
          while(rs.next()) {
            int iIdLingua = rs.getInt(1);
            List<String> listDescTags = mapDescTags.get(new Integer(iIdLingua));
            if(listDescTags == null) {
              listDescTags = new ArrayList<String>();
              mapDescTags.put(new Integer(iIdLingua), listDescTags);
            }
            String tagDisplayName = tag.getDisplayName();
            if(iIdLingua == 0 && tagDisplayName != null && tagDisplayName.length() > 0) {
              listDescTags.addAll(getWords(tagDisplayName));
            }
            else {
              String sDescTag = rs.getString(2);
              if(sDescTag != null && sDescTag.length() > 0) {
                listDescTags.addAll(getWords(sDescTag));
              }
            }
          }
          rs.close();
        }
        Collections.sort(listIdTag);
        for(int i = 0; i < listIdTag.size(); i++) {
          sTags += ",#" + listIdTag.get(i);
        }
      }
      
      Map<String, Object> mapContenuti = wmArticolo.getMapObject(sCONTENUTI);
      if(mapContenuti != null && !mapContenuti.isEmpty()) {
        Iterator<Map.Entry<String, Object>> iterator = mapContenuti.entrySet().iterator();
        while(iterator.hasNext()) {
          Map.Entry<String, Object> entry  = iterator.next();
          
          int iIdLingua = WUtil.toInt(entry.getKey(),0);
          
          WMap wmContenuto  = new WMap(WUtil.toMapObject(entry.getValue()));
          String sTitolo    = wmContenuto.getString(sTITOLO);
          String sSpecifica = wmContenuto.getString(sSPECIFICA);
          String sAbstract  = wmContenuto.getString(sABSTRACT);
          String sTesto     = wmContenuto.getString(sTESTO);
          String sKeywords  = wmContenuto.getLowerString(sKEYWORDS);
          if(sKeywords != null && sKeywords.length() > 3 && sKeywords.charAt(0) != '.') {
            sKeywords = normalizeKeywords(sKeywords);
          }
          else {
            if(sTesto != null && sTesto.length() > 5) {
              sKeywords = getKeywords(sTesto, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else if(sAbstract != null && sAbstract.length() > 5) {
              sKeywords = getKeywords(sAbstract, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else if(sSpecifica != null && sSpecifica.length() > 1) {
              sKeywords = getKeywords(sTitolo + " " + sSpecifica, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
            else {
              sKeywords = getKeywords(sTitolo, sTags, mapDescTags.get(new Integer(iIdLingua)));
            }
          }
          if(sKeywords != null && sKeywords.length() > iMAX_TEXT_LENGTH) {
            sKeywords = sKeywords.substring(0, iMAX_TEXT_LENGTH - 1) + ",";
          }
          pstm_Cont.setString(1, sKeywords);
          pstm_Cont.setInt(2,    iIdArticolo);
          pstm_Cont.setInt(3,    iIdLingua);
          pstm_Cont.executeUpdate();
        }
      }
    }
    finally {
      if(pstm_Cont != null) try{ pstm_Cont.close(); } catch(Exception ex) {}
      if(pstm_DTag != null) try{ pstm_DTag.close(); } catch(Exception ex) {}
    }
    return true;
  }
  
  public
  Map<String, Object> getNewId()
      throws Exception
  {
    Map<String, Object> result = new HashMap<String, Object>();
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_ARTICOLI");
      
      String sArticleFolder = BEConfig.getArticleFolder(iId);
      File fMultimedia = new File(sArticleFolder);
      if(!fMultimedia.exists()) fMultimedia.mkdirs();
      
      result.put(sID, new Integer(iId));
      result.put(sMULT_FOLDER, sArticleFolder);
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSArticoli.getNewId", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public
  boolean setEnabled(int iId, boolean boEnabled)
      throws Exception
  {
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement("UPDATE CMS_ARTICOLI SET ATTIVO=? WHERE ID_ARTICOLO=?");
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.setEnabled(" + iId + "," + boEnabled + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(pstm, conn);
    }
    return boEnabled;
  }	
  
  public
  boolean delete(int iId)
      throws Exception
  {
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      delete(conn, iId);
      
      CMSCache.mapCount.clear();
      CMSCache.mapYears.clear();
      CMSCache.mapMonths.clear();
      
      ut.commit();
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  int addLike(int iId)
      throws Exception
  {
    int iResult = 0;
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement("UPDATE CMS_ARTICOLI SET PREF_POS=PREF_POS+1,DT_POS=? WHERE ID_ARTICOLO=?");
      pstm.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
      pstm.setInt(2, iId);
      pstm.executeUpdate();
      
      ut.commit();
      
      pstm.close();
      pstm = conn.prepareStatement("SELECT PREF_POS FROM CMS_ARTICOLI WHERE ID_ARTICOLO=?");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) iResult = rs.getInt(1);
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.addLike(" + iId + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return iResult;
  }	
  
  public static
  int addDontLike(int iId)
      throws Exception
  {
    int iResult = 0;
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement("UPDATE CMS_ARTICOLI SET PREF_NEG=PREF_NEG+1,DT_NEG=? WHERE ID_ARTICOLO=?");
      pstm.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
      pstm.setInt(2, iId);
      pstm.executeUpdate();
      
      ut.commit();
      
      pstm.close();
      pstm = conn.prepareStatement("SELECT PREF_NEG FROM CMS_ARTICOLI WHERE ID_ARTICOLO=?");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) iResult = rs.getInt(1);
    }
    catch (Exception ex) {
      ConnectionManager.rollback(ut);
      oLogger.error("Exception in WSArticoli.addDontLike(" + iId + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return iResult;
  }	
  
  public static
  boolean delete(Connection conn, int iId)
      throws Exception
  {
    Statement stm = null;
    try {
      stm = conn.createStatement();
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_COMP WHERE ID_ARTICOLO="   + iId + " OR ID_ARTICOLO_COMP=" + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_CORR WHERE ID_ARTICOLO="   + iId + " OR ID_ARTICOLO_CORR=" + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_TAG WHERE ID_ARTICOLO="    + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_MULT WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_CONT WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_LUOGHI WHERE ID_ARTICOLO=" + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_AUT WHERE ID_ARTICOLO="    + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_PREZZI WHERE ID_ARTICOLO=" + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI WHERE ID_ARTICOLO="        + iId);
    }
    finally {
      ConnectionManager.close(stm);
    }
    return true;
  }
  
  public static
  boolean clean(Connection conn, int iId)
      throws Exception
  {
    // Non si elimina il record in CMS_ARTICOLI
    Statement stm = null;
    try {
      stm = conn.createStatement();
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_COMP WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_CORR WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_TAG WHERE ID_ARTICOLO="    + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_MULT WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_CONT WHERE ID_ARTICOLO="   + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_LUOGHI WHERE ID_ARTICOLO=" + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_AUT WHERE ID_ARTICOLO="    + iId);
      stm.executeUpdate("DELETE FROM CMS_ARTICOLI_PREZZI WHERE ID_ARTICOLO=" + iId);
    }
    finally {
      ConnectionManager.close(stm);
    }
    return true;
  }
  
  public static
  File getFile(int iIdMultimedia)
  {
    File file = null;
    if(iIdMultimedia == 0) return null;
    if(iIdMultimedia < 0) {
      String sDefFilePath = BEConfig.getImportFolder() + File.separator + iIdMultimedia + ".jpg";
      file = new File(sDefFilePath);
      if(!file.exists()) return null;
      return file;
    }
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT URL_FILE FROM CMS_ARTICOLI_MULT WHERE ID_MULTIMEDIA=?");
      pstm.setInt(1, iIdMultimedia);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sURLFile = rs.getString(1);
        if(sURLFile == null || sURLFile.length() == 0) return null;
        if(sURLFile.startsWith("${user.home}/") || sURLFile.startsWith("${user.home}\\")) {
          String sUserHome = System.getProperty("user.home");
          sURLFile = sUserHome + File.separator + sURLFile.substring(13).replace('\\', File.separatorChar);
        }
        file = new File(sURLFile);
        if(!file.exists()) return null;
      }
    }
    catch(Exception ex) {
      oLogger.error("Exception in WSArticoli.getFile(" + iIdMultimedia + ")", ex);
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return file;
  }
  
  public static
  String getKeywords(String sText, String sTags, List<String> listDescTags) 
  {
    String sResult = "";
    if(sText == null || sText.trim().length() == 0) return sResult;
    // Si rimuovono eventuali tag html
    sText = removeHtmlTags(sText);
    // Si rimuovono caratteri non alfanumerici
    StringBuffer sb = new StringBuffer(sText.length());
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c)) sb.append(c); else sb.append(' ');
    }
    // Si costruisce la lista di parole
    List<String> listTokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(sb.toString(), " ");
    while(st.hasMoreTokens()) {
      String sToken = st.nextToken().toLowerCase().trim();
      if(sToken.length() > 3) {
        if(_sWordsToExclude.indexOf("|" + sToken + "|") >= 0) continue;
        if(!listTokens.contains(sToken)) listTokens.add(sToken);
      }
    }
    if(listTokens.size() == 0 && (listDescTags == null || listDescTags.size() == 0)) return "";
    if(listDescTags != null && listDescTags.size() > 0) listTokens.addAll(listDescTags);
    // Si ordina la lista di parole
    Collections.sort(listTokens);
    // Si costruisce la stringa utilizzata per le ricerche
    for(int i = 0; i < listTokens.size(); i++) {
      sResult += "," + listTokens.get(i);
    }
    // Il punto all'inizio denota la generazione automatica delle keywords
    if(sTags != null && sTags.length() > 2) {
      return "." + sTags + sResult + ",";
    }
    return "." + sResult + ",";
  }
  
  public static
  List<String> getWords(String sText) 
  {
    List<String> listResult = new ArrayList<String>();
    if(sText == null || sText.trim().length() == 0) return listResult;
    StringBuilder sb = new StringBuilder(sText.length());
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c)) sb.append(c); else sb.append(' ');
    }
    StringTokenizer st = new StringTokenizer(sb.toString(), " ");
    while(st.hasMoreTokens()) {
      String sToken = st.nextToken().toLowerCase().trim();
      if(sToken.length() > 3) {
        if(_sWordsToExclude.indexOf("|" + sToken + "|") >= 0) continue;
        if(!listResult.contains(sToken)) listResult.add(sToken);
      }
    }
    return listResult;
  }
  
  public static
  String getLikeFilter(String sText) 
  {
    if(sText == null || sText.trim().length() == 0) return "%";
    if(sText.length() > 2 && sText.startsWith("%")) return sText;
    if(sText.length() > 2 && sText.startsWith(".")) return sText;
    StringBuffer sb = new StringBuffer(sText.length());
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c)) sb.append(c); else sb.append(' ');
    }
    List<String> listTokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(sb.toString(), " ");
    while(st.hasMoreTokens()) {
      String sToken = st.nextToken().toLowerCase().trim();
      if(sToken.length() > 3) {
        if(_sWordsToExclude.indexOf("|" + sToken + "|") >= 0) continue;
        if(!listTokens.contains(sToken)) listTokens.add(sToken);
      }
    }
    if(listTokens.size() == 0) return "";
    Collections.sort(listTokens);
    String sResult = "";
    for(int i = 0; i < listTokens.size(); i++) {
      String sToken = listTokens.get(i);
      sToken = sToken.substring(0, sToken.length()-1) + "%";
      sResult += "," + sToken;
    }
    return "%" + sResult;
  }
  
  public static
  String normalizeKeywords(String sKeywords)
  {
    if(sKeywords == null || sKeywords.length() < 2) return sKeywords;
    String result = "";
    try {
      sKeywords = sKeywords.trim();
      if(sKeywords.length() > 0 && sKeywords.charAt(0) == '.') {
        sKeywords = sKeywords.substring(1);
      }
      if(sKeywords.length() > 0 && sKeywords.charAt(0) == ',') {
        sKeywords = sKeywords.substring(1);
      }
      if(sKeywords.length() > 0 && sKeywords.charAt(sKeywords.length()-1) == ',') {
        sKeywords = sKeywords.substring(0, sKeywords.length()-1);
      }
      if(sKeywords.length() == 0) return sKeywords;
      List<String> listKeywords = WUtil.toListOfString(sKeywords);
      Collections.sort(listKeywords);
      for(int i = 0; i < listKeywords.size(); i++) {
        String sKeyword = listKeywords.get(i);
        if(sKeyword == null) continue;
        sKeyword = sKeyword.trim();
        if(sKeyword.length() == 0) continue;
        result += "," + sKeyword.toLowerCase();
      }
      if(result != null && result.length() > 0) result += ",";
    }
    catch(Exception ex) {
      return sKeywords;
    }
    return result;
  }
  
  private static
  String getExtension(String sURLFile)
  {
    if(sURLFile == null || sURLFile.length() == 0) return "";
    String sResult = "";
    int iDot = sURLFile.lastIndexOf('.');
    if(iDot >= 0 && iDot < sURLFile.length() - 1) {
      sResult = sURLFile.substring(iDot + 1).toLowerCase();
    }
    return sResult;
  }	
  
  private static
  int getIdTipoContenuto(String sURLFile)
  {
    String ext = getExtension(sURLFile);
    if(ext == null || ext.length() == 0) return 0;
    else if(isVideoFile(ext)) return Multimedia.iTYPE_VIDEO;
    else if(isImageFile(ext)) return Multimedia.iTYPE_PHOTO;
    else if(isAudioFile(ext)) return Multimedia.iTYPE_AUDIO;
    return Multimedia.iTYPE_DOCUM;
  }
  
  public static
  boolean isVideoFile(String ext)
  {
    if(ext == null) return false;
    if(ext.equals("flv") || ext.equals("f4v"))  return true;
    if(ext.equals("avi") || ext.equals("wmv"))  return true;
    if(ext.equals("mpg") || ext.equals("mp4"))  return true;
    if(ext.equals("mpe") || ext.equals("mpeg")) return true;
    if(ext.equals("mov") || ext.equals("qt"))   return true;
    if(ext.equals("rm")  || ext.equals("rv"))   return true;
    if(ext.equals("dv")  || ext.equals("divx")) return true;
    if(ext.equals("3gp") || ext.equals("3gpp")) return true;
    if(ext.equals("3g2") || ext.equals("3gp2")) return true;
    return false;
  }
  
  public static
  boolean isAudioFile(String ext)
  {
    if(ext == null) return false;
    if(ext.equals("mp3") || ext.equals("m4a"))  return true;
    if(ext.equals("wav") || ext.equals("wma"))  return true;
    if(ext.equals("aac") || ext.equals("au"))   return true;
    if(ext.equals("aif") || ext.equals("aiff")) return true;
    if(ext.equals("ra")  || ext.equals("ram"))  return true;
    return false;
  }
  
  public static
  boolean isImageFile(String ext)
  {
    if(ext == null) return false;
    if(ext.equals("jpg") || ext.equals("jpeg")) return true;
    if(ext.equals("gif") || ext.equals("bmp"))  return true;
    if(ext.equals("jpe") || ext.equals("png"))  return true;
    if(ext.equals("tif") || ext.equals("tiff")) return true;
    if(ext.equals("ico") || ext.equals("dib"))  return true;
    if(ext.equals("xcf") || ext.equals("psd"))  return true;
    return false;
  }
  
  public static
  boolean isLinkFile(String ext)
  {
    if(ext == null) return false;
    if(ext.equals("url") || ext.equals("link")) return true;
    return false;
  }
  
  public static
  boolean isDocFile(String ext)
  {
    if(ext == null) return false;
    if(ext.equals("pdf") || ext.equals("ps"))    return true;
    if(ext.equals("xps") || ext.equals("rtf"))   return true;
    if(ext.equals("doc") || ext.equals("docx"))  return true;
    if(ext.equals("odt") || ext.equals("sdw"))   return true;
    if(ext.equals("txt") || ext.equals("text"))  return true;
    if(ext.equals("htm") || ext.equals("html"))  return true;
    return false;
  }
  
  public static
  List<String> getContenutiMultimediali(int iIdArticolo)
  {
    File fMultimedia = new File(BEConfig.getArticleFolder(iIdArticolo));
    if(!fMultimedia.exists())      return new ArrayList<String>(0);
    if(!fMultimedia.isDirectory()) return new ArrayList<String>(0);
    return getFiles(fMultimedia);
  }
  
  public static
  List<String> getFiles(File fDirectory)
  {
    File[] afFiles  = fDirectory.listFiles();
    List<String> result = new ArrayList<String>();
    if(afFiles != null && afFiles.length > 0) {
      for(int i = 0; i < afFiles.length; i++) {
        File file = afFiles[i];
        if(file.isDirectory()) {
          result.addAll(getFiles(file));
          continue;
        }
        String sFileName = file.getName();
        // Si scartano le anteprime
        if(sFileName.startsWith("_")) continue;
        result.add(file.getPath());
      }
    }
    return result;
  }
  
  public static
  String getFileName(String sURL)
  {
    String sResult = "";
    int iSep = sURL.lastIndexOf('/');
    if(iSep >= 0 && iSep < sURL.length() - 1) {
      sResult = sURL.substring(iSep + 1);
    }
    else {
      iSep = sURL.lastIndexOf('\\');
      if(iSep >= 0 && iSep < sURL.length() - 1) {
        sResult = sURL.substring(iSep + 1);
      }
      else {
        sResult = sURL;
      }
    }
    return sResult;
  }
  
  public static
  java.sql.Date[] getDateFilter(String sText, StringBuffer sbTextWithoutDigits)
  {
    if(sText == null || sText.length() == 0) return null;
    if(sText.startsWith(".") || sText.startsWith("%")) return null;
    boolean boLastIsDigit = false;
    StringBuffer sbField = null;
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < sText.length(); i++) {
      Character c = sText.charAt(i);
      if(Character.isDigit(c)) {
        if(!boLastIsDigit) sbField = new StringBuffer();
        sbField.append(c);
        boLastIsDigit = true;
      }
      else {
        if(boLastIsDigit) {
          if(sbField.length() == 1) sb.append("0" + sbField); else sb.append(sbField);
        }
        if(sbTextWithoutDigits != null) {
          if(c != '.' && c != '-' && c != '/') {
            sbTextWithoutDigits.append(c);
          }
        }
        boLastIsDigit = false;
      }
    }
    if(boLastIsDigit) {
      if(sbField.length() == 1) sb.append("0"+sbField); else sb.append(sbField);
    }
    if(sb.length() < 4) return null;
    Calendar cal = null;
    if(sb.length() == 4) {
      int iYear = 0;
      try{ iYear = Integer.parseInt(sb.toString()); } catch(Exception ex) {}
      if(iYear < 1000) return null;
      
      cal = new GregorianCalendar(iYear, 0, 1, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      java.sql.Date[] adResult = new java.sql.Date[2];
      adResult[0] = new java.sql.Date(cal.getTimeInMillis());
      cal = new GregorianCalendar(iYear, 11, 31, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      adResult[1] = new java.sql.Date(cal.getTimeInMillis());
      return adResult;
    }
    else if(sb.length() > 4 && sb.length() <  7) {
      String sYear  = sb.toString().substring(sb.length()-4,sb.length());
      String sMonth = sb.toString().substring(0, sb.length()-4);
      int iYear = 0;
      try{ iYear = Integer.parseInt(sYear);   } catch(Exception ex) {}
      if(iYear < 1000) return null;
      int iMonth = 0;
      try{ iMonth = Integer.parseInt(sMonth); } catch(Exception ex) {}
      if(iMonth < 1 || iMonth > 12) return null;
      
      cal = new GregorianCalendar(iYear, iMonth-1, 1, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      java.sql.Date[] adResult = new java.sql.Date[2];
      adResult[0] = new java.sql.Date(cal.getTimeInMillis());
      cal.add(Calendar.MONTH, 1);
      cal.add(Calendar.DATE, -1);
      adResult[1] = new java.sql.Date(cal.getTimeInMillis());
      return adResult;
    }
    else if(sb.length() > 6 && sb.length() < 9) {
      String sYear  = sb.toString().substring(sb.length()-4,sb.length());
      String sMonth = sb.toString().substring(sb.length()-6,sb.length()-4);
      String sDay   = sb.toString().substring(0,sb.length()-6);
      int iYear = 0;
      try{ iYear = Integer.parseInt(sYear);   } catch(Exception ex) {}
      if(iYear < 1000) return null;
      int iMonth = 0;
      try{ iMonth = Integer.parseInt(sMonth); } catch(Exception ex) {}
      if(iMonth < 1 || iMonth > 12) return null;
      int iDay = 0;
      try{ iDay = Integer.parseInt(sDay); } catch(Exception ex) {}
      if(iDay < 1 || iDay > 31) return null;
      
      cal = new GregorianCalendar(iYear, iMonth-1, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      java.sql.Date[] adResult = new java.sql.Date[2];
      adResult[0] = new java.sql.Date(cal.getTimeInMillis());
      adResult[1] = new java.sql.Date(cal.getTimeInMillis());
      return adResult;
    }
    return null;
  }
  
  public static
  String removeHtmlTags(String sText)
  {
    StringBuilder sbResult = new StringBuilder();
    if(sText == null || sText.length() == 0) {
      return sbResult.toString();
    }
    boolean boIsTag = false;
    int iTextLength = sText.length();
    int iStartTag = -1;
    for(int i = 0; i < iTextLength; i++) {
      char c = sText.charAt(i);
      if(c == '<' && i < iTextLength - 1) {
        char c1 = sText.charAt(i + 1);
        boIsTag = (Character.isLetter(c1) || c1 == '/') && sText.indexOf('>', i) > 0;
        if(boIsTag) iStartTag = i;
      }
      if(c == '>') {
        boIsTag = false;
        if(iStartTag >= 0) {
          String sTag = sText.substring(iStartTag, i + 1);
          if(sTag.startsWith("<br") || sTag.startsWith("<BR")) sbResult.append('\n'); else
          if(sTag.equalsIgnoreCase("</P>")) sbResult.append('\n');
        }
      }
      if(!boIsTag && c != '>') {
        if(c == '&') {
          int iEnd = sText.indexOf(';', i);
          if(iEnd > 0 && iEnd - i <= 8) {
            String sSeq = sText.substring(i, iEnd + 1);
            if(sSeq.equalsIgnoreCase("&nbsp;")) sbResult.append(' ');
            else if(sSeq.equalsIgnoreCase("&Agrave;")) sbResult.append('\300');
            else if(sSeq.equalsIgnoreCase("&Egrave;")) sbResult.append('\310');
            else if(sSeq.equalsIgnoreCase("&Igrave;")) sbResult.append('\314');
            else if(sSeq.equalsIgnoreCase("&Ograve;")) sbResult.append('\322');
            else if(sSeq.equalsIgnoreCase("&Ugrave;")) sbResult.append('\331');
            else if(sSeq.equalsIgnoreCase("&Aacute;")) sbResult.append('\301');
            else if(sSeq.equalsIgnoreCase("&Eacute;")) sbResult.append('\311');
            else if(sSeq.equalsIgnoreCase("&Iacute;")) sbResult.append('\315');
            else if(sSeq.equalsIgnoreCase("&Oacute;")) sbResult.append('\323');
            else if(sSeq.equalsIgnoreCase("&Uacute;")) sbResult.append('\332');
            else if(sSeq.equalsIgnoreCase("&agrave;")) sbResult.append('\340');
            else if(sSeq.equalsIgnoreCase("&egrave;")) sbResult.append('\350');
            else if(sSeq.equalsIgnoreCase("&igrave;")) sbResult.append('\354');
            else if(sSeq.equalsIgnoreCase("&ograve;")) sbResult.append('\362');
            else if(sSeq.equalsIgnoreCase("&ugrave;")) sbResult.append('\371');
            else if(sSeq.equalsIgnoreCase("&aacute;")) sbResult.append('\341');
            else if(sSeq.equalsIgnoreCase("&eacute;")) sbResult.append('\351');
            else if(sSeq.equalsIgnoreCase("&iacute;")) sbResult.append('\355');
            else if(sSeq.equalsIgnoreCase("&oacute;")) sbResult.append('\363');
            else if(sSeq.equalsIgnoreCase("&uacute;")) sbResult.append('\372');
            else if(sSeq.equalsIgnoreCase("&acirc;"))  sbResult.append('\342');
            else if(sSeq.equalsIgnoreCase("&ecirc;"))  sbResult.append('\352');
            else if(sSeq.equalsIgnoreCase("&icirc;"))  sbResult.append('\356');
            else if(sSeq.equalsIgnoreCase("&ocirc;"))  sbResult.append('\364');
            else if(sSeq.equalsIgnoreCase("&ucirc;"))  sbResult.append('\373');
            else if(sSeq.equalsIgnoreCase("&ccedil;")) sbResult.append('\347');
            else if(sSeq.equalsIgnoreCase("&Ccedil;")) sbResult.append('\307');
            else if(sSeq.equalsIgnoreCase("&ntilde;")) sbResult.append('\361');
            else if(sSeq.equalsIgnoreCase("&iexcl;"))  sbResult.append('\241');
            else if(sSeq.equalsIgnoreCase("&iquest;")) sbResult.append('\277');
            else if(sSeq.equalsIgnoreCase("&ordf;"))   sbResult.append('\252');
            else if(sSeq.equalsIgnoreCase("&deg;"))    sbResult.append('\260');
            else if(sSeq.equalsIgnoreCase("&euro;"))   sbResult.append('\u20ac');
            else if(sSeq.equalsIgnoreCase("&gt;"))     sbResult.append('>');
            else if(sSeq.equalsIgnoreCase("&lt;"))     sbResult.append('<');
            i = iEnd;
          }
          else {
            sbResult.append(c);
          }
        }
        else {
          sbResult.append(c);
        }
      }
    }
    return sbResult.toString();
  }
}

