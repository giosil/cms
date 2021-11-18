package org.dew.cms.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import org.util.WMap;
import org.dew.cms.common.IIstituto;
import org.dew.cms.util.ConnectionManager;
import org.dew.cms.util.QueryBuilder;

public 
class WSIstituti implements IIstituto 
{
  protected static Logger oLogger = Logger.getLogger(WSIstituti.class);
  
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
    qb.put("ID_ISTITUTO", sID);
    qb.put("CODICE",      sCODICE);
    qb.put("DESCRIZIONE", sDESCRIZIONE);
    String sAddClause = "ATTIVO='" + QueryBuilder.decodeBoolean(true) + "' AND ID_ISTITUTO<>0";
    String sSQL = qb.select("CMS_ISTITUTI", mapFilter, sAddClause);
    sSQL += " ORDER BY ID_ISTITUTO";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_ISTITUTO");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iId);
        record.add(sCodice);
        if(sDescrizione != null) {
          record.add(sDescrizione);
        }
        else {
          record.add("");
        }
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSIstituti.lookup(" + mapFilter + ")", ex);
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
    qb.put("ID_ISTITUTO", sID);
    qb.put("CODICE",      sCODICE);
    qb.put("DESCRIZIONE", sDESCRIZIONE);
    qb.put("ATTIVO",      sATTIVO);
    String sAddClause = "ID_ISTITUTO<>0";
    String sSQL = qb.select("CMS_ISTITUTI", mapFilter, sAddClause);
    sSQL += " ORDER BY ID_ISTITUTO";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_ISTITUTO");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sCODICE,        sCodice);
        record.put(sDESCRIZIONE,   sDescrizione);
        record.putBoolean(sATTIVO, sAttivo);
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSIstituti.find(" + mapFilter + ")", ex);
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
    qb.add("DESCRIZIONE");
    qb.add("ATTIVO");
    String sSQL = qb.select("CMS_ISTITUTI");
    sSQL += "WHERE ID_ISTITUTO=?";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        
        result.put(sID,            iId);
        result.put(sCODICE,        sCodice);
        result.put(sDESCRIZIONE,   sDescrizione);
        result.putBoolean(sATTIVO, sAttivo);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSIstituti.read(" + iId + ")", ex);
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
  Map<String, Object> insert(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_ISTITUTO");
    qb.add("CODICE");
    qb.add("DESCRIZIONE");
    qb.add("ATTIVO");
    String sSQL = qb.insert("CMS_ISTITUTI", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_ISTITUTI");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues = new WMap(mapValues);
      String sCodice      = wmValues.getString(sCODICE);
      String sDescrizione = wmValues.getString(sDESCRIZIONE);
      
      int p = 0;
      pstm.setInt(++p,    iId);
      pstm.setString(++p, sCodice);
      pstm.setString(++p, sDescrizione);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      
      pstm.executeUpdate();
      
      ut.commit();
      
      mapValues.put(sID, iId);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSIstituti.insert", ex);
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
    qb.add("DESCRIZIONE");
    String sSQL = qb.update("CMS_ISTITUTI", true);
    sSQL += "WHERE ID_ISTITUTO=?";
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues = new WMap(mapValues);
      int    iId          = wmValues.getInt(sID);
      String sCodice      = wmValues.getString(sCODICE);
      String sDescrizione = wmValues.getString(sDESCRIZIONE);
      
      // SET
      int p = 0;
      pstm.setString(++p, sCodice);
      pstm.setString(++p, sDescrizione);
      // WHERE
      pstm.setInt(++p, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSIstituti.update(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
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
      
      pstm = conn.prepareStatement("UPDATE CMS_ISTITUTI SET ATTIVO=? WHERE ID_ISTITUTO=?");
      // SET
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      // WHERE
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSIstituti.setEnabled(" + iId + "," + boEnabled + ")", ex);
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
      
      pstm = conn.prepareStatement("DELETE FROM CMS_ISTITUTI WHERE ID_ISTITUTO=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSIstituti.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
}

