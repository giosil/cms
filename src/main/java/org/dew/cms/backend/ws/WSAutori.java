package org.dew.cms.backend.ws;

import java.sql.Connection;
import java.sql.Date;
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

import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.IAutore;

public 
class WSAutori implements IAutore 
{
  protected static Logger oLogger = Logger.getLogger(WSAutori.class);
  
  public static
  List<List<Object>> getTipiAutore()
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery("SELECT ID_TIPO_AUTORE,DESCRIZIONE FROM CMS_TIPI_AUTORE WHERE ID_TIPO_AUTORE > 0 ORDER BY DESCRIZIONE");
      while(rs.next()) {
        int iId = rs.getInt("ID_TIPO_AUTORE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.getTipiAutore", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public static
  List<List<Object>> getRuoli()
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery("SELECT ID_RUOLO,DESCRIZIONE FROM CMS_RUOLI WHERE ATTIVO = '" + QueryBuilder.decodeBoolean(true) + "' ORDER BY ID_RUOLO");
      while(rs.next()) {
        int iId = rs.getInt("ID_RUOLO");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.getRuoli", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public
  List<List<Object>> lookup(Map<String, Object> mapFilter)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_AUTORE", sID);
    qb.put("COGNOME",   sCOGNOME + "%");
    qb.put("NOME",      sNOME + "%");
    String sAddClause = "ID_AUTORE > 0";
    String sSQL = qb.select("CMS_AUTORI", mapFilter, sAddClause);
    sSQL += " ORDER BY COGNOME,NOME";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId         = rs.getInt("ID_AUTORE");
        String sCognome = rs.getString("COGNOME");
        String sNome    = rs.getString("NOME");
        String sNominativo = sCognome;
        if(sNome != null && sNome.length() > 0) {
          sNominativo += " " + sNome;
        }
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        record.add(sNominativo);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.lookup(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public
  List<Map<String, Object>> find(Map<String, Object> mapFilter)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("A.ID_AUTORE",      sID);
    qb.put("A.COGNOME",        sCOGNOME + "%");
    qb.put("A.NOME",           sNOME + "%");
    qb.put("A.SESSO",          sSESSO);
    qb.put("A.DATA_NASCITA",   sDATA_NASCITA);
    qb.put("A.EMAIL",          sEMAIL);
    qb.put("A.ID_TIPO_AUTORE", sID_TIPO);
    qb.put("T.DESCRIZIONE",    sDESC_TIPO);
    String sAddClause = "A.ID_TIPO_AUTORE=T.ID_TIPO_AUTORE AND A.ID_AUTORE > 0";
    String sSQL = qb.select("CMS_AUTORI A,CMS_TIPI_AUTORE T", mapFilter, sAddClause);
    sSQL += " ORDER BY A.COGNOME,A.NOME";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId           = rs.getInt("ID_AUTORE");
        String sCognome   = rs.getString("COGNOME");
        String sNome      = rs.getString("NOME");
        String sSesso     = rs.getString("SESSO");
        Date dDataNascita = rs.getDate("DATA_NASCITA");
        String sEmail     = rs.getString("EMAIL");
        int iIdTipoAutore = rs.getInt("ID_TIPO_AUTORE");
        String sDescTipo  = rs.getString("DESCRIZIONE");
        
        Map<String, Object> record = new HashMap<String, Object>();
        record.put(sID,           iId);
        record.put(sCOGNOME,      sCognome);
        record.put(sNOME,         sNome);
        record.put(sSESSO,        sSesso);
        record.put(sDATA_NASCITA, dDataNascita);
        record.put(sEMAIL,        sEmail);
        record.put(sID_TIPO,      iIdTipoAutore);
        record.put(sDESC_TIPO,    sDescTipo);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.find(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, stm, conn);
    }
    return result;
  }
  
  public
  Map<String, Object> read(int iId)
      throws Exception
  {
    Map<String, Object> result = new HashMap<String, Object>();
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("A.ID_TIPO_AUTORE");
    qb.add("T.DESCRIZIONE");
    qb.add("A.COGNOME");
    qb.add("A.NOME");
    qb.add("A.SESSO");
    qb.add("A.DATA_NASCITA");
    qb.add("A.TITOLO");
    qb.add("A.TELEFONO");
    qb.add("A.CELLULARE");
    qb.add("A.EMAIL");
    qb.add("A.NOTE");
    String sSQL = qb.select("CMS_AUTORI A,CMS_TIPI_AUTORE T");
    sSQL += "WHERE A.ID_TIPO_AUTORE=T.ID_TIPO_AUTORE AND A.ID_AUTORE=?";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdTipoAutore   = rs.getInt("ID_TIPO_AUTORE");
        String sDescTipoAut = rs.getString("DESCRIZIONE");
        String sCognome     = rs.getString("COGNOME");
        String sNome        = rs.getString("NOME");
        String sSesso       = rs.getString("SESSO");
        Date dDataNascita   = rs.getDate("DATA_NASCITA");
        String sTitolo      = rs.getString("TITOLO");
        String sTelefono    = rs.getString("TELEFONO");
        String sCellulare   = rs.getString("CELLULARE");
        String sEmail       = rs.getString("EMAIL");
        String sNote        = rs.getString("NOTE");
        String sNominativo  = sCognome;
        if(sNome != null && sNome.length() > 0) {
          sNominativo += " " + sNome;
        }
        
        result.put(sID,           iId);
        result.put(sID_TIPO,      iIdTipoAutore);
        result.put(sDESC_TIPO,    sDescTipoAut);
        result.put(sCOGNOME,      sCognome);
        result.put(sNOME,         sNome);
        result.put(sNOMINATIVO,   sNominativo);
        result.put(sSESSO,        sSesso);
        result.put(sDATA_NASCITA, dDataNascita);
        result.put(sTITOLO,       sTitolo);
        result.put(sTELEFONO,     sTelefono);
        result.put(sCELLULARE,    sCellulare);
        result.put(sEMAIL,        sEmail);
        result.put(sNOTE,         sNote);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.read(" + iId + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm, conn);
    }
    return result;
  }
  
  public
  Map<String, Object> insert(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_AUTORE");
    qb.add("ID_TIPO_AUTORE");
    qb.add("COGNOME");
    qb.add("NOME");
    qb.add("SESSO");
    qb.add("DATA_NASCITA");
    qb.add("TITOLO");
    qb.add("TELEFONO");
    qb.add("CELLULARE");
    qb.add("EMAIL");
    qb.add("NOTE");
    String sSQL = qb.insert("CMS_AUTORI", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_AUTORI");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues  = new WMap(mapValues);
      int iIdTipoAutore = wmValues.getInt(sID_TIPO);
      String sCognome   = wmValues.getString(sCOGNOME);
      String sNome      = wmValues.getString(sNOME);
      String sSesso     = wmValues.getString(sSESSO);
      Date dDataNascita = wmValues.getSQLDate(sDATA_NASCITA);
      String sTitolo    = wmValues.getString(sTITOLO);
      String sTelefono  = wmValues.getString(sTELEFONO);
      String sCellulare = wmValues.getString(sCELLULARE);
      String sEmail     = wmValues.getLowerString(sEMAIL);
      String sNote      = wmValues.getString(sNOTE);
      
      int p = 0;
      pstm.setInt(++p,    iId);
      pstm.setInt(++p,    iIdTipoAutore);
      pstm.setString(++p, sCognome);
      pstm.setString(++p, sNome);
      pstm.setString(++p, sSesso);
      pstm.setDate(++p,   dDataNascita);
      pstm.setString(++p, sTitolo);
      pstm.setString(++p, sTelefono);
      pstm.setString(++p, sCellulare);
      pstm.setString(++p, sEmail);
      pstm.setString(++p, sNote);
      pstm.executeUpdate();
      
      ut.commit();
      
      mapValues.put(sID,        iId);
      mapValues.put(sDESC_TIPO, getDescTipo(conn, iIdTipoAutore));
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSAutori.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(pstm, conn);
    }
    return mapValues;
  }
  
  public
  Map<String, Object> update(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_TIPO_AUTORE");
    qb.add("COGNOME");
    qb.add("NOME");
    qb.add("SESSO");
    qb.add("DATA_NASCITA");
    qb.add("TITOLO");
    qb.add("TELEFONO");
    qb.add("CELLULARE");
    qb.add("EMAIL");
    qb.add("NOTE");
    String sSQL = qb.update("CMS_AUTORI", true);
    sSQL += "WHERE ID_AUTORE=?";
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap wmValues  = new WMap(mapValues);
      int iId           = wmValues.getInt(sID);
      int iIdTipoAutore = wmValues.getInt(sID_TIPO);
      String sCognome   = wmValues.getString(sCOGNOME);
      String sNome      = wmValues.getString(sNOME);
      String sSesso     = wmValues.getString(sSESSO);
      Date dDataNascita = wmValues.getSQLDate(sDATA_NASCITA);
      String sTitolo    = wmValues.getString(sTITOLO);
      String sTelefono  = wmValues.getString(sTELEFONO);
      String sCellulare = wmValues.getString(sCELLULARE);
      String sEmail     = wmValues.getLowerString(sEMAIL);
      String sNote      = wmValues.getString(sNOTE);
      
      // SET
      int p = 0;
      pstm.setInt(++p,    iIdTipoAutore);
      pstm.setString(++p, sCognome);
      pstm.setString(++p, sNome);
      pstm.setString(++p, sSesso);
      pstm.setDate(++p,   dDataNascita);
      pstm.setString(++p, sTitolo);
      pstm.setString(++p, sTelefono);
      pstm.setString(++p, sCellulare);
      pstm.setString(++p, sEmail);
      pstm.setString(++p, sNote);
      // WHERE
      pstm.setInt(++p, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
      
      mapValues.put(sDESC_TIPO, getDescTipo(conn, iIdTipoAutore));
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSAutori.update(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(pstm, conn);
    }
    return mapValues;
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
      
      pstm = conn.prepareStatement("DELETE FROM CMS_AUTORI WHERE ID_AUTORE=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSAutori.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      ConnectionManager.close(pstm, conn);
    }
    return true;
  }
  
  public static 
  String getDescTipo(Connection conn, int iIdTipoAutore)
      throws Exception
  {
    String result = "";
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT DESCRIZIONE FROM CMS_TIPI_AUTORE WHERE ID_TIPO_AUTORE=?");
      pstm.setInt(1, iIdTipoAutore);
      rs = pstm.executeQuery();
      if(rs.next()) result = rs.getString("DESCRIZIONE");
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSAutori.getDescTipo(conn," + iIdTipoAutore + ")", ex);
      throw ex;
    }
    finally {
      ConnectionManager.close(rs, pstm);
    }
    if(result == null) result = "";
    return result;
  }
}
