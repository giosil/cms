package org.dew.cms.backend.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import org.util.WMap;
import org.util.WUtil;

import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.CMSCache;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.ICategoria;

public 
class WSCategorie implements ICategoria 
{
  protected static Logger oLogger = Logger.getLogger(WSCategorie.class);
  
  public static
  List<List<Object>> lookup()
      throws Exception
  {
    return lookup(new HashMap<String, Object>());
  }
  
  public static
  List<List<Object>> lookup(Map<String, Object> mapFilter)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_CATEGORIA", sID);
    qb.put("CODICE",       sCODICE + "%");
    String sAddClause = "ATTIVO='" + QueryBuilder.decodeBoolean(true) + "'";
    String sSQL = qb.select("CMS_CATEGORIE", mapFilter, sAddClause);
    sSQL += " ORDER BY CODICE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId        = rs.getInt("ID_CATEGORIA");
        String sCodice = rs.getString("CODICE");
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iId);
        record.add(sCodice);
        record.add(sCodice);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSCategorie.lookup(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();  } catch(Exception ex) {}
      if(stm  != null) try{ stm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public
  List<Map<String, Object>> find(Map<String, Object> mapFilter)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_CATEGORIA", sID);
    qb.put("CODICE", sCODICE + "%");
    qb.put("ATTIVO", sATTIVO);
    String sAddClause = "ID_CATEGORIA > 0";
    String sSQL = qb.select("CMS_CATEGORIE", mapFilter, sAddClause);
    sSQL += " ORDER BY CODICE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId        = rs.getInt("ID_CATEGORIA");
        String sCodice = rs.getString("CODICE");
        String sAttivo = rs.getString("ATTIVO");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sCODICE,        sCodice);
        record.putBoolean(sATTIVO, sAttivo);
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSCategorie.find(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();  } catch(Exception ex) {}
      if(stm  != null) try{ stm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public
  Map<String, Object> read(int iId)
      throws Exception
  {
    WMap result = new WMap();
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("CODICE");
    qb.add("ATTIVO");
    String sSQL = qb.select("CMS_CATEGORIE");
    sSQL += "WHERE ID_CATEGORIA = ?";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sCodice = rs.getString("CODICE");
        String sAttivo = rs.getString("ATTIVO");
        
        result.put(sID, iId);
        result.put(sCODICE, sCodice);
        result.putBoolean(sATTIVO, sAttivo);
      }
      rs.close();
      pstm.close();
      
      Map<String, Object> mapDescrizioni = new HashMap<String, Object>();
      result.put(sDESCRIZIONE, mapDescrizioni);
      
      pstm = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_CATEGORIE_DESC WHERE ID_CATEGORIA=? ORDER BY ID_LINGUA");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        String sIdLingua    = rs.getString("ID_LINGUA");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(sIdLingua == null || sDescrizione == null) continue;
        mapDescrizioni.put(sIdLingua, sDescrizione);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSCategorie.read(" + iId + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result.toMapObject();
  }
  
  public
  boolean exists(Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty()) return false;
    
    String sCodice = WUtil.toString(mapValues.get(sCODICE), null);
    if(sCodice == null || sCodice.length() == 0) return false;
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_CATEGORIA FROM CMS_CATEGORIE WHERE CODICE=?");
      pstm.setString(1, sCodice);
      rs   = pstm.executeQuery();
      return rs.next();
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSCategorie.exists(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public static
  String getDescrizione(int iId, int iIdLingua)
      throws Exception
  {
    String sResult = null;
    String sDescrizione0 = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_CATEGORIE_DESC WHERE ID_CATEGORIA=? ORDER BY ID_LINGUA");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iCatIdLingua    = rs.getInt("ID_LINGUA");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(sDescrizione == null || sDescrizione.length() == 0) continue;
        if(iCatIdLingua == 0) sDescrizione0 = sDescrizione;
        if(iCatIdLingua == iIdLingua) {
          sResult = sDescrizione;
          break;
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSCategorie.getDescrizione(" + iId + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(sResult == null) sResult = sDescrizione0;
    return sResult;
  }
  
  public
  Map<String, Object> insert(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_CATEGORIA");
    qb.add("CODICE");
    qb.add("ATTIVO");
    String sSQL = qb.insert("CMS_CATEGORIE", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_CATEGORIE");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues  = new WMap(mapValues);
      String sCodice = wmValues.getString(sCODICE);
      
      int p = 0;
      pstm.setInt(++p, iId);
      pstm.setString(++p, sCodice);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      pstm.executeUpdate();
      
      insertDescrizioni(conn, iId, wmValues.getMapObject(sDESCRIZIONE));
      
      ut.commit();
      
      CMSCache.mapDescCat.clear();
      
      mapValues.put(sID, iId);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSCategorie.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public
  Map<String, Object> update(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("CODICE");
    String sSQL = qb.update("CMS_CATEGORIE", true);
    sSQL += "WHERE ID_CATEGORIA=?";
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues  = new WMap(mapValues);
      int iId        = wmValues.getInt(sID);
      String sCodice = wmValues.getString(sCODICE);
      
      // SET
      int p = 0;
      pstm.setString(++p, sCodice);
      // WHERE
      pstm.setInt(++p, iId);
      pstm.executeUpdate();
      
      deleteDescrizioni(conn, iId);
      insertDescrizioni(conn, iId, wmValues.getMapObject(sDESCRIZIONE));
      
      ut.commit();
      
      CMSCache.mapDescCat.clear();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSCategorie.update(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public
  void insertDescrizioni(Connection conn, int iId, Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty()) return;
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_CATEGORIA");
    qb.add("ID_LINGUA");
    qb.add("DESCRIZIONE");
    String sSQL = qb.insert("CMS_CATEGORIE_DESC", true);
    
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      
      Iterator<Map.Entry<String, Object>> iterator = mapValues.entrySet().iterator();
      while(iterator.hasNext()) {
        Map.Entry<String, Object> entry = iterator.next();
        String key = entry.getKey();
        Object val = entry.getValue();
        
        pstm.setInt(1,    iId);
        pstm.setInt(2,    WUtil.toInt(key, 0));
        pstm.setString(3, WUtil.toString(val, ""));
        pstm.executeUpdate();
      }
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return;
  }
  
  public
  void deleteDescrizioni(Connection conn, int iId)
      throws Exception
  {
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement("DELETE FROM CMS_CATEGORIE_DESC WHERE ID_CATEGORIA=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return;
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
      
      pstm = conn.prepareStatement("UPDATE CMS_CATEGORIE SET ATTIVO = ? WHERE ID_CATEGORIA = ?");
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSCategorie.setEnabled(" + iId + "," + boEnabled + ")", ex);
      throw ex;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return boEnabled;
  }
  
  public
  boolean delete(int iId)
      throws Exception
  {
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      deleteDescrizioni(conn, iId);
      
      pstm = conn.prepareStatement("DELETE FROM CMS_CATEGORIE WHERE ID_CATEGORIA = ?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
      
      CMSCache.mapDescCat.clear();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSCategorie.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
}

