package org.dew.cms.backend.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.CMSCache;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.ITag;
import org.util.WMap;
import org.util.WUtil;

public 
class WSPortale 
{
  protected static Logger oLogger = Logger.getLogger(WSPortale.class);
  
  public static
  boolean clearCache()
  {
    CMSCache.clear();
    return true;
  }
  
  public static
  List<List<Object>> getCategorie(int iIdLingua)
      throws Exception
  {
    return getCategorie(iIdLingua, false);
  }
  
  public static
  List<List<Object>> getCategorie(int iIdLingua, boolean boSoloAttive)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    String sSQL = "SELECT C.ID_CATEGORIA,C.CODICE,CD.DESCRIZIONE,C.ATTIVO ";
    sSQL += "FROM CMS_CATEGORIE C,CMS_CATEGORIE_DESC CD ";
    sSQL += "WHERE C.ID_CATEGORIA=CD.ID_CATEGORIA ";
    sSQL += "AND CD.ID_LINGUA=? ";
    if(boSoloAttive) sSQL += "AND C.ATTIVO=? ";
    if(iIdLingua >= 0) {
      sSQL += "ORDER BY CD.DESCRIZIONE";
    }
    else {
      sSQL += "ORDER BY C.CODICE";
    }
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      if(boSoloAttive) pstm.setString(2, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int iId             = rs.getInt("ID_CATEGORIA");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        boolean boAttivo    = sAttivo != null ? sAttivo.equalsIgnoreCase("S") : false;
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        if(boAttivo) {
          if(iIdLingua >= 0) {
            record.add(sDescrizione);
          }
          else {
            record.add(sCodice);
          }
        }
        else {
          if(iIdLingua >= 0) {
            record.add("(" + sDescrizione + ")");
          }
          else {
            record.add("(" + sCodice + ")");
          }
        }
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getCategorie(" + iIdLingua + "," + boSoloAttive + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  Map<String, Integer> getMapCategorie()
      throws Exception
  {
    Map<String, Integer> result = new HashMap<String, Integer>();
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_CATEGORIA,CODICE FROM CMS_CATEGORIE WHERE ATTIVO=?");
      pstm.setString(1, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int iId        = rs.getInt("ID_CATEGORIA");
        String sCodice = rs.getString("CODICE");
        result.put(sCodice, new Integer(iId));
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getMapCategorie()", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  Map<String, List<Object>> getSottoCategorie(int iIdLingua)
      throws Exception
  {
    return getSottoCategorie(iIdLingua, false);
  }
  
  public static
  Map<String, List<Object>> getSottoCategorie(int iIdLingua, boolean boSoloAttive)
      throws Exception
  {
    Map<String, List<Object>> result = new HashMap<String, List<Object>>();
    
    String sSQL = "SELECT S.ID_SOTTOCATEGORIA,S.ID_CATEGORIA,S.CODICE,SD.DESCRIZIONE,S.ATTIVO ";
    sSQL += "FROM CMS_SOTTOCATEGORIE S,CMS_SOTTOCATEGORIE_DESC SD ";
    sSQL += "WHERE S.ID_SOTTOCATEGORIA = SD.ID_SOTTOCATEGORIA ";
    sSQL += "AND SD.ID_LINGUA = ? ";
    if(boSoloAttive) sSQL += "AND S.ATTIVO = ? ";
    if(iIdLingua >= 0) {
      sSQL += "ORDER BY S.ID_CATEGORIA,SD.DESCRIZIONE";
    }
    else {
      sSQL += "ORDER BY S.ID_CATEGORIA,S.CODICE";
    }
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      if(boSoloAttive) pstm.setString(2, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int iId             = rs.getInt("ID_SOTTOCATEGORIA");
        int iIdCategoria    = rs.getInt("ID_CATEGORIA");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        boolean boAttivo    = sAttivo != null ? sAttivo.equalsIgnoreCase("S") : false;
        
        String sIdCategoria = String.valueOf(iIdCategoria);
        
        List<Object> listSottoCategorie = result.get(sIdCategoria);
        if(listSottoCategorie == null) {
          listSottoCategorie = new ArrayList<Object>();
          
          result.put(sIdCategoria, listSottoCategorie);
        }
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        if(boAttivo) {
          if(iIdLingua >= 0) {
            record.add(sDescrizione);
          }
          else {
            record.add(sCodice);
          }
        }
        else {
          if(iIdLingua >= 0) {
            record.add("(" + sDescrizione + ")");
          }
          else {
            record.add("(" + sCodice + ")");
          }
        }
        
        listSottoCategorie.add(record);
      }
    }
    catch(Exception ex) {
      oLogger.error("Exception in WSPortale.getSottoCategorie(" + iIdLingua + "," + boSoloAttive + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  List<List<Object>> getSottoCategorie(int iIdLingua, int iIdCategoria, boolean boSoloAttive)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    String sSQL = "SELECT S.ID_SOTTOCATEGORIA,S.CODICE,SD.DESCRIZIONE ";
    sSQL += "FROM CMS_SOTTOCATEGORIE S,CMS_SOTTOCATEGORIE_DESC SD ";
    sSQL += "WHERE S.ID_SOTTOCATEGORIA=SD.ID_SOTTOCATEGORIA ";
    sSQL += "AND SD.ID_LINGUA=? AND S.ID_CATEGORIA=? ";
    if(boSoloAttive) sSQL += "AND S.ATTIVO=? ";
    if(iIdLingua >= 0) {
      sSQL += "ORDER BY S.ID_CATEGORIA,S.ID_SOTTOCATEGORIA";
    }
    else {
      sSQL += "ORDER BY S.ID_CATEGORIA,S.CODICE";
    }
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      pstm.setInt(2, iIdCategoria);
      if(boSoloAttive) pstm.setString(3, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int    iId          = rs.getInt("ID_SOTTOCATEGORIA");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>();
        record.add(iId);
        if(iIdLingua >= 0) {
          record.add(sDescrizione);
        }
        else {
          record.add(sCodice);
        }
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getSottoCategorie(" + iIdLingua + "," + iIdCategoria + "," + boSoloAttive + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  List<List<Object>> getTipologie(int iIdLingua)
      throws Exception
  {
    return getTipologie(iIdLingua, false);
  }
  
  public static
  List<List<Object>> getTipologie(int iIdLingua, boolean boSoloAttive)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    String sSQL = "SELECT T.ID_TIPO_ARTICOLO,T.CODICE,TD.DESCRIZIONE,T.ATTIVO ";
    sSQL += "FROM CMS_TIPI_ARTICOLO T,CMS_TIPI_ARTICOLO_DESC TD ";
    sSQL += "WHERE T.ID_TIPO_ARTICOLO=TD.ID_TIPO_ARTICOLO ";
    sSQL += "AND TD.ID_LINGUA=? ";
    if(boSoloAttive) sSQL += "AND T.ATTIVO=? ";
    if(iIdLingua >= 0) {
      sSQL += "ORDER BY TD.DESCRIZIONE";
    }
    else {
      sSQL += "ORDER BY T.CODICE";
    }
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      if(boSoloAttive) pstm.setString(2, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int iId             = rs.getInt("ID_TIPO_ARTICOLO");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        boolean boAttivo    = sAttivo != null ? sAttivo.equalsIgnoreCase("S") : false;
        
        List<Object> record = new ArrayList<Object>();
        record.add(iId);
        if(boAttivo) {
          if(iIdLingua >= 0) {
            record.add(sDescrizione);
          }
          else {
            record.add(sCodice);
          }
        }
        else {
          if(iIdLingua >= 0) {
            record.add("(" + sDescrizione + ")");
          }
          else {
            record.add("(" + sCodice + ")");
          }
        }
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getTipologie(" + iIdLingua + "," + boSoloAttive + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  List<Map<String, Object>> getTags(int iIdLingua)
      throws Exception
  {
    return getTags(iIdLingua, false, false);
  }
  
  public static
  List<Map<String, Object>> getTags(int iIdLingua, boolean boSoloAttivi, boolean boSoloAnteprima)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    String sSQL = "SELECT T.ID_TAG,T.CODICE,TD.DESCRIZIONE,T.ANTEPRIMA,T.ORDINE,T.ATTIVO ";
    sSQL += "FROM CMS_TAG T,CMS_TAG_DESC TD ";
    sSQL += "WHERE T.ID_TAG=TD.ID_TAG ";
    sSQL += "AND TD.ID_LINGUA=? ";
    if(boSoloAttivi)    sSQL += "AND T.ATTIVO=? ";
    if(boSoloAnteprima) sSQL += "AND T.ANTEPRIMA=? ";
    sSQL += "ORDER BY T.ORDINE,T.CODICE,TD.DESCRIZIONE";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      if(boSoloAttivi)    pstm.setString(2, QueryBuilder.decodeBoolean(true));
      if(boSoloAnteprima) pstm.setString(3, QueryBuilder.decodeBoolean(true));
      rs   = pstm.executeQuery();
      while(rs.next()) {
        int iId             = rs.getInt("ID_TAG");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAnteprima   = rs.getString("ANTEPRIMA");
        String sAttivo      = rs.getString("ATTIVO");
        boolean boAnteprima = WUtil.toBoolean(sAnteprima, false);
        boolean boAttivo    = WUtil.toBoolean(sAttivo,    false);
        if(!boAttivo) sDescrizione = "(" + sDescrizione + ")";
        
        WMap record = new WMap();
        record.put(ITag.sID,               iId);
        record.put(ITag.sCODICE,           sCodice);
        record.put(ITag.sDESCRIZIONE,      sDescrizione);
        record.putBoolean(ITag.sANTEPRIMA, boAnteprima);
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getTags(" + iIdLingua + "," + boSoloAttivi + "," + boSoloAnteprima + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  List<String> getTagOptions(int iIdTag, int iIdCategoria, int iIdLingua)
      throws Exception
  {
    List<String> result = new ArrayList<String>();
    
    String sSQL = "SELECT AT.DESCRIZIONE FROM CMS_ARTICOLI A,CMS_ARTICOLI_TAG AT ";
    sSQL += "WHERE A.ID_ARTICOLO=AT.ID_ARTICOLO AND AT.DESCRIZIONE IS NOT NULL ";
    sSQL += "AND A.ATTIVO=? AND AT.ID_TAG=? ";
    if(iIdCategoria != 0) {
      sSQL += "AND A.ID_CATEGORIA=? ";
    }
    sSQL += "GROUP BY AT.DESCRIZIONE ORDER BY AT.DESCRIZIONE";
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(1, QueryBuilder.decodeBoolean(true));
      pstm.setInt(2, iIdTag);
      if(iIdCategoria != 0) {
        pstm.setInt(3, iIdCategoria);
      }
      rs   = pstm.executeQuery();
      while(rs.next()) {
        result.add(rs.getString("DESCRIZIONE"));
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPortale.getTagOptions(" + iIdTag + "," + iIdCategoria + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  void insertLog(Connection conn, int iIdArticolo, int iIdUtente)
      throws Exception
  {
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement("INSERT INTO CMS_LOG_VISITE(ID_ARTICOLO,DT_VISITA,ID_UTENTE) VALUES(?,?,?)");
      pstm.setInt(1,       iIdArticolo);
      pstm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
      pstm.setInt(3,       iIdUtente);
      pstm.executeUpdate();
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
  }
}
