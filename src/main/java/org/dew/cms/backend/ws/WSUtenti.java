package org.dew.cms.backend.ws;

import java.security.Principal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import org.util.WMap;
import org.util.WUtil;

import org.dew.cms.backend.User;
import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IUtente;

import org.rpc.util.RPCContext;

public 
class WSUtenti implements IUtente 
{
  protected static Logger oLogger = Logger.getLogger(WSUtenti.class);
  
  public static
  String getCurrentUser()
    throws Exception
  {
    Principal userPrincipal = RPCContext.getUserPrincipal();
    if(userPrincipal == null) return "";
    
    String result = userPrincipal.getName();
    return result;
  }
  
  public static
  List<List<Object>> getTipiUtenti()
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery("SELECT ID_TIPO_UTENTE,DESCRIZIONE FROM CMS_TIPI_UTENTE WHERE ATTIVO='" + QueryBuilder.decodeBoolean(true) + "' ORDER BY ID_TIPO_UTENTE");
      while(rs.next()) {
        int    iId          = rs.getInt("ID_TIPO_UTENTE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iId);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.getTipiUtenti()", ex);
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
    
    if(mapFilter == null) mapFilter = new HashMap<String, Object>();
    Date dDal = WUtil.toDate(mapFilter.get(sREG_DAL), null);
    Date dAl  = WUtil.toDate(mapFilter.get(sREG_AL),  null);
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("U.ID_UTENTE",      sID);
    qb.put("U.ID_TIPO_UTENTE", sID_TIPO);
    qb.put("U.USERNAME",       sUSERNAME);
    qb.put("U.COGNOME",        sCOGNOME + "%");
    qb.put("U.NOME",           sNOME    + "%");
    qb.put("U.EMAIL",          "%" + sEMAIL + "%");
    qb.put("U.ATTIVO",         sATTIVO);
    qb.put("U.DT_INS",         sDATA_REG);
    qb.put("T.DESCRIZIONE",    sDESC_TIPO);
    String sAddClause = "U.ID_TIPO_UTENTE=T.ID_TIPO_UTENTE";
    if(dDal != null) {
      sAddClause += " AND DT_INS>=" + QueryBuilder.toString(dDal);
    }
    if(dAl != null) {
      sAddClause += " AND DT_INS<=" + QueryBuilder.toString(dAl);
    }
    String sSQL = qb.select("CMS_UTENTI U,CMS_TIPI_UTENTE T", mapFilter, sAddClause);
    sSQL += " ORDER BY U.COGNOME,U.NOME";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId          = rs.getInt("ID_UTENTE");
        String sUserName = rs.getString("USERNAME");
        String sCognome  = rs.getString("COGNOME");
        String sNome     = rs.getString("NOME");
        String sEmail    = rs.getString("EMAIL");
        String sAttivo   = rs.getString("ATTIVO");
        String sDescTipo = rs.getString("DESCRIZIONE");
        Timestamp tsDtRe = rs.getTimestamp("DT_INS");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sUSERNAME,      sUserName);
        record.put(sCOGNOME,       sCognome);
        record.put(sNOME,          sNome);
        record.put(sEMAIL,         sEmail);
        record.put(sDESC_TIPO,     sDescTipo);
        record.putBoolean(sATTIVO, sAttivo);
        record.putDate(sDATA_REG,  tsDtRe);
        record.putTime(sORA_REG,   tsDtRe);
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.find(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();  } catch(Exception ex) {}
      if(stm  != null) try{ stm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  public static
  Map<String, Object> read(int iId)
      throws Exception
  {
    Map<String, Object> mapResult = null;
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      mapResult = read(conn, iId);
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.read(" + iId + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(mapResult == null) mapResult = new HashMap<String, Object>();
    return mapResult;
  }
  
  public
  List<Map<String, Object>> getReadArticles(int iId)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    String sSQL = "SELECT A.ID_ARTICOLO,A.DESCRIZIONE,L.DT_VISITA ";
    sSQL += "FROM CMS_LOG_VISITE L,CMS_ARTICOLI A ";
    sSQL += "WHERE L.ID_ARTICOLO=A.ID_ARTICOLO AND ID_UTENTE=? ";
    sSQL += "ORDER BY L.DT_VISITA DESC";
    
    int iRows = 0;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdArticolo     = rs.getInt("ID_ARTICOLO");
        String sDescrizione = rs.getString("DESCRIZIONE");
        Timestamp tsVisita  = rs.getTimestamp("DT_VISITA");
        
        WMap record = new WMap();
        record.put(IArticolo.sID,              iIdArticolo);
        record.put(IArticolo.sDESCRIZIONE,     sDescrizione);
        record.putDate(IArticolo.sDATA_VISITA, tsVisita);
        record.putTime(IArticolo.sORA_VISITA,  tsVisita);
        
        result.add(record.toMapObject());
        iRows++;
        if(iRows >= 1000) break;
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.getReadArticles(" + iId + ")", ex);
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
  int register(User user)
      throws Exception
  {
    if(user == null) return -1;
    String sUsername = user.getUserName();
    if(sUsername == null || sUsername.length() == 0) return -1;
    
    Map<String, Object> mapValues = new HashMap<String, Object>();
    mapValues.put(sUSERNAME,     user.getUserName());
    mapValues.put(sPASSWORD,     user.getPassword());
    mapValues.put(sID_TIPO,      user.getType());
    mapValues.put(sEMAIL,        user.getEmail());
    mapValues.put(sNOME,         user.getFirstName());
    mapValues.put(sCOGNOME,      user.getLastName());
    mapValues.put(sSESSO,        user.getSex());
    mapValues.put(sDATA_NASCITA, user.getDateOfBirth());
    mapValues.put(sCITTA,        user.getCity());
    mapValues.put(sPROFESSIONE,  user.getJobTitle());
    
    int iResult = 0;
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      if(exists(conn, sUsername)) {
        return 0;
      }
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      iResult = insert(conn, mapValues);
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.register(" + user + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return iResult;
  }
  
  public static
  int update(User user)
      throws Exception
  {
    if(user == null) return -1;
    String sUsername = user.getUserName();
    if(sUsername == null || sUsername.length() == 0) return -1;
    
    Map<String, Object> mapValues = new HashMap<String, Object>();
    mapValues.put(sUSERNAME,     user.getUserName());
    mapValues.put(sPASSWORD,     user.getPassword());
    mapValues.put(sID_TIPO,      user.getType());
    mapValues.put(sEMAIL,        user.getEmail());
    mapValues.put(sNOME,         user.getFirstName());
    mapValues.put(sCOGNOME,      user.getLastName());
    mapValues.put(sSESSO,        user.getSex());
    mapValues.put(sDATA_NASCITA, user.getDateOfBirth());
    mapValues.put(sCITTA,        user.getCity());
    mapValues.put(sPROFESSIONE,  user.getJobTitle());
    
    int iResult = 0;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      pstm = conn.prepareStatement("SELECT ID_UTENTE FROM CMS_UTENTI WHERE USERNAME=?");
      pstm.setString(1, sUsername);
      rs = pstm.executeQuery();
      if(rs.next()) {
        iResult = rs.getInt("ID_UTENTE");
      }
      rs.close();
      if(iResult == 0) return iResult;
      mapValues.put(sID, iResult);
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      update(conn, mapValues);
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.update(" + user + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return iResult;
  }
  
  public
  Map<String, Object> insert(Map<String, Object> mapValues)
      throws Exception
  {
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      insert(conn, mapValues);
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public
  Map<String, Object> update(Map<String, Object> mapValues)
      throws Exception
  {
    Connection conn = null;
    UserTransaction ut = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      update(conn, mapValues);
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.update(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public static
  Map<String, Object> update(Connection conn, Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null) mapValues = new HashMap<String, Object>();
    String sPassword = WUtil.toString(mapValues.get(sPASSWORD), null);
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("USERNAME");
    if(sPassword != null && sPassword.length() > 0) {
      qb.add("PASSWORD");
    }
    qb.add("ID_TIPO_UTENTE");
    qb.add("COGNOME");
    qb.add("NOME");
    qb.add("SESSO");
    qb.add("DATA_NASCITA");
    qb.add("PROFESSIONE");
    qb.add("CITTA");
    qb.add("EMAIL");
    String sSQL = qb.update("CMS_UTENTI", true);
    sSQL += "WHERE ID_UTENTE=?";
    
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      
      WMap   wmValues = new WMap(mapValues);
      int    iId           = wmValues.getInt(sID);
      String sUsername     = wmValues.getString(sUSERNAME);
      int    iIdTipoUtente = wmValues.getInt(sID_TIPO);
      String sCognome      = wmValues.getUpperString(sCOGNOME);
      String sNome         = wmValues.getUpperString(sNOME);
      String sSesso        = wmValues.getUpperString(sSESSO);
      java.sql.Date dDtNas = wmValues.getSQLDate(sDATA_NASCITA);
      String sProfessione  = wmValues.getUpperString(sPROFESSIONE);
      String sCitta        = wmValues.getUpperString(sCITTA);
      String sEmail        = wmValues.getLowerString(sEMAIL);
      if(sUsername != null) sUsername = sUsername.trim();
      if(sPassword != null) sPassword = sPassword.trim();
      if(sEmail    != null) sEmail = sEmail.trim().replace(' ', '_');
      
      // SET
      int p = 0;
      pstm.setString(++p, sUsername);
      if(sPassword != null && sPassword.length() > 0) {
        pstm.setString(++p, WSFM.encrypt(sPassword));
      }
      pstm.setInt(++p, iIdTipoUtente);
      pstm.setString(++p, sCognome);
      pstm.setString(++p, sNome);
      pstm.setString(++p, sSesso);
      pstm.setDate(++p, dDtNas);
      pstm.setString(++p, sProfessione);
      pstm.setString(++p, sCitta);
      pstm.setString(++p, sEmail);
      // WHERE
      pstm.setInt(++p, iId);
      pstm.executeUpdate();
      
      mapValues.put(sDESC_TIPO, getDescTipo(conn, iIdTipoUtente));
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
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
      
      pstm = conn.prepareStatement("UPDATE CMS_UTENTI SET ATTIVO=? WHERE ID_UTENTE=?");
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.setEnabled(" + iId + "," + boEnabled + ")", ex);
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
      
      pstm = conn.prepareStatement("DELETE FROM CMS_UTENTI WHERE ID_UTENTE=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSUtenti.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  Map<String, Object> check(String sUsername, String sPassword)
      throws Exception
  {
    if(sUsername == null || sUsername.length() == 0) return new HashMap<String, Object>();
    if(sPassword == null || sPassword.length() == 0) return new HashMap<String, Object>();
    
    Map<String, Object> mapResult = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_UTENTE FROM CMS_UTENTI WHERE USERNAME=? AND PASSWORD=? AND ATTIVO=?");
      pstm.setString(1, sUsername);
      pstm.setString(2, WSFM.encrypt(sPassword));
      pstm.setString(3, QueryBuilder.decodeBoolean(true));
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdUtente = rs.getInt("ID_UTENTE");
        mapResult = read(conn, iIdUtente);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.check(" + sUsername + ",*)", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(mapResult == null) mapResult = new HashMap<String, Object>();
    return mapResult;
  }
  
  public static
  Map<String, Object> check(String sUsername, String sPassword, int iIdTipo)
      throws Exception
  {
    if(sUsername == null || sUsername.length() == 0) return new HashMap<String, Object>();
    if(sPassword == null || sPassword.length() == 0) return new HashMap<String, Object>();
    
    Map<String, Object> mapResult = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_UTENTE FROM CMS_UTENTI WHERE USERNAME=? AND PASSWORD=? AND ID_TIPO_UTENTE=? AND ATTIVO=?");
      pstm.setString(1, sUsername);
      pstm.setString(2, WSFM.encrypt(sPassword));
      pstm.setInt(3,    iIdTipo);
      pstm.setString(4, QueryBuilder.decodeBoolean(true));
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdUtente = rs.getInt("ID_UTENTE");
        mapResult = read(conn, iIdUtente);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.check(" + sUsername + ",*," + iIdTipo + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(mapResult == null) mapResult = new HashMap<String, Object>();
    return mapResult;
  }
  
  public static
  Map<String, Object> read(String sUsername)
      throws Exception
  {
    if(sUsername == null || sUsername.length() == 0) {
      return new HashMap<String, Object>();
    }
    
    Map<String, Object> result = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_UTENTE FROM CMS_UTENTI WHERE USERNAME=?");
      pstm.setString(1, sUsername);
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdUtente = rs.getInt("ID_UTENTE");
        result = read(conn, iIdUtente);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.read(" + sUsername + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new HashMap<String, Object>();
    return result;
  }	
  
  public static
  Map<String, Object> readByEmail(String sEmail)
      throws Exception
  {
    if(sEmail == null || sEmail.length() == 0) {
      return new HashMap<String, Object>();
    }
    
    Map<String, Object> result = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_UTENTE FROM CMS_UTENTI WHERE EMAIL=?");
      pstm.setString(1, sEmail);
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdUtente = rs.getInt("ID_UTENTE");
        result = read(conn, iIdUtente);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.readByEmail(" + sEmail + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(result == null) result = new HashMap<String, Object>();
    return result;
  }	
  
  public static
  Map<String, Object> read(Connection conn, int iId)
      throws Exception
  {
    WMap result = new WMap();
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("USERNAME");
    qb.add("ID_TIPO_UTENTE");
    qb.add("COGNOME");
    qb.add("NOME");
    qb.add("SESSO");
    qb.add("DATA_NASCITA");
    qb.add("PROFESSIONE");
    qb.add("CITTA");
    qb.add("EMAIL");
    qb.add("ATTIVO");
    qb.add("DT_INS");
    String sSQL = qb.select("CMS_UTENTI");
    sSQL += "WHERE ID_UTENTE=?";
    
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sUserName      = rs.getString("USERNAME");
        int iIdTipoUtente     = rs.getInt("ID_TIPO_UTENTE");
        String sCognome       = rs.getString("COGNOME");
        String sNome          = rs.getString("NOME");
        String sSesso         = rs.getString("SESSO");
        java.sql.Date dDtNasc = rs.getDate("DATA_NASCITA");
        String sProfessione   = rs.getString("PROFESSIONE");
        String sCitta         = rs.getString("CITTA");
        String sEmail         = rs.getString("EMAIL");
        String sAttivo        = rs.getString("ATTIVO");
        Timestamp tsDtRe      = rs.getTimestamp("DT_INS");
        
        result.put(sID,               iId);
        result.put(sUSERNAME,         sUserName);
        result.put(sID_TIPO,          iIdTipoUtente);
        result.put(sCOGNOME,          sCognome);
        result.put(sNOME,             sNome);
        result.put(sSESSO,            sSesso);
        result.putDate(sDATA_NASCITA, dDtNasc);
        result.put(sPROFESSIONE,      sProfessione);
        result.put(sCITTA,            sCitta);
        result.put(sEMAIL,            sEmail);
        result.putBoolean(sATTIVO,    sAttivo);
        result.putDate(sDATA_REG,     tsDtRe);
        result.putTime(sORA_REG,      tsDtRe);
      }
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return result.toMapObject();
  }
  
  public static
  int insert(Connection conn, Map<String, Object> mapValues)
      throws Exception
  {
    int iId = 0;
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_UTENTE");
    qb.add("USERNAME");
    qb.add("PASSWORD");
    qb.add("ID_TIPO_UTENTE");
    qb.add("COGNOME");
    qb.add("NOME");
    qb.add("SESSO");
    qb.add("DATA_NASCITA");
    qb.add("PROFESSIONE");
    qb.add("CITTA");
    qb.add("EMAIL");
    qb.add("ATTIVO");
    qb.add("DT_INS");
    String sSQL = qb.insert("CMS_UTENTI", true);
    
    PreparedStatement pstm = null;
    try {
      iId = ConnectionManager.nextVal(conn, "SEQ_CMS_UTENTI");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap   wmValues      = new WMap(mapValues);
      String sUsername     = wmValues.getString(sUSERNAME);
      String sPassword     = wmValues.getString(sPASSWORD);
      int    iIdTipoUtente = wmValues.getInt(sID_TIPO, 0);
      String sCognome      = wmValues.getUpperString(sCOGNOME);
      String sNome         = wmValues.getUpperString(sNOME);
      String sSesso        = wmValues.getUpperString(sSESSO);
      java.sql.Date dDtNas = wmValues.getSQLDate(sDATA_NASCITA);
      String sProfessione  = wmValues.getUpperString(sPROFESSIONE);
      String sCitta        = wmValues.getUpperString(sCITTA);
      String sEmail        = wmValues.getLowerString(sEMAIL);
      if(sUsername != null) sUsername = sUsername.trim();
      if(sPassword != null) sPassword = sPassword.trim();
      if(sEmail    != null) sEmail = sEmail.trim().replace(' ', '_');
      
      Timestamp tsDtIns = new Timestamp(System.currentTimeMillis());
      
      int p = 0;
      pstm.setInt(++p,       iId);
      pstm.setString(++p,    sUsername);
      pstm.setString(++p,    WSFM.encrypt(sPassword));
      pstm.setInt(++p,       iIdTipoUtente);
      pstm.setString(++p,    sCognome);
      pstm.setString(++p,    sNome);
      pstm.setString(++p,    sSesso);
      pstm.setDate(++p,      dDtNas);
      pstm.setString(++p,    sProfessione);
      pstm.setString(++p,    sCitta);
      pstm.setString(++p,    sEmail);
      pstm.setString(++p,    QueryBuilder.decodeBoolean(true));
      pstm.setTimestamp(++p, tsDtIns);
      pstm.executeUpdate();
      
      mapValues.put(sID,        iId);
      mapValues.put(sDESC_TIPO, getDescTipo(conn, iIdTipoUtente));
      mapValues.put(sDATA_REG,  new Date(tsDtIns.getTime()));
      mapValues.put(sORA_REG,   new Date(tsDtIns.getTime()));
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return iId;
  }
  
  public static
  boolean exists(Connection conn, String sUsername)
      throws Exception
  {
    if(sUsername == null || sUsername.length() == 0) return false;
    String sSQL = "SELECT ID_UTENTE FROM CMS_UTENTI WHERE USERNAME=?";
    int iResult = 0;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(1, sUsername);
      rs = pstm.executeQuery();
      if(rs.next()) iResult = rs.getInt("ID_UTENTE");
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.exist(conn," + sUsername + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return iResult != 0;
  }
  
  public static 
  String getDescTipo(Connection conn, int iIdTipoUtente)
      throws Exception
  {
    String sResult = "";
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT DESCRIZIONE FROM CMS_TIPI_UTENTE WHERE ID_TIPO_UTENTE=?");
      pstm.setInt(1, iIdTipoUtente);
      rs = pstm.executeQuery();
      if(rs.next()) sResult = rs.getString("DESCRIZIONE");
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSUtenti.getDescTipo(conn," + iIdTipoUtente + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return sResult;
  }
}