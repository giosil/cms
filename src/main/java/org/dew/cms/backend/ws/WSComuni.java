package org.dew.cms.backend.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.IComune;

public 
class WSComuni implements IComune
{
  protected static Logger oLogger = Logger.getLogger(WSComuni.class);
  
  public
  List<List<Object>> lookup(Map<String, Object> mapFilter)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_COMUNE",   sID);
    qb.put("COD_ISTAT",   sCOD_ISTAT);
    qb.put("COD_FISCALE", sCOD_FISCALE);
    qb.put("DESCRIZIONE", sDESCRIZIONE + "%");
    qb.put("PROVINCIA",   sPROVINCIA);
    String sSQL = qb.select("CMS_COMUNI", mapFilter);
    sSQL += " ORDER BY DESCRIZIONE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iIdComune       = rs.getInt("ID_COMUNE");
        String sCodIstat    = rs.getString("COD_ISTAT");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(sCodIstat    == null) sCodIstat = "";
        if(sDescrizione == null) continue;
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iIdComune);
        record.add(sCodIstat);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSComuni.lookup(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public static
  List<List<Object>> getComuni()
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery("SELECT ID_COMUNE,DESCRIZIONE FROM CMS_COMUNI ORDER BY DESCRIZIONE");
      while(rs.next()) {
        int iIdComune       = rs.getInt("ID_COMUNE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(iIdComune    == 0)    continue;
        if(sDescrizione == null) continue;
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iIdComune);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSComuni.getComuni()", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public static
  List<List<Object>> getComuni(String sProvincia)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    String sSQL = "SELECT ID_COMUNE,DESCRIZIONE FROM CMS_COMUNI ";
    if(sProvincia != null && sProvincia.length() > 0) {
      sSQL += "WHERE PROVINCIA=? ";
    }
    sSQL += "ORDER BY DESCRIZIONE";
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      if(sProvincia != null && sProvincia.length() > 0) {
        pstm.setString(1, sProvincia);
      }
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdComune       = rs.getInt("ID_COMUNE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(iIdComune    == 0)    continue;
        if(sDescrizione == null) continue;
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iIdComune);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSComuni.getComuni(" + sProvincia + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return result;
  }
}
