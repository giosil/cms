package org.dew.cms.backend.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import org.util.WMap;
import org.util.WUtil;

import org.dew.cms.backend.Place;
import org.dew.cms.backend.util.ConnectionManager;
import org.dew.cms.backend.util.QueryBuilder;

import org.dew.cms.common.ILuogo;

public 
class WSLuoghi implements ILuogo 
{
  protected static Logger oLogger = Logger.getLogger(WSLuoghi.class);
  
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
    qb.put("ID_LUOGO",   sID);
    qb.put("CODICE",      sCODICE);
    qb.put("DESCRIZIONE", sDESCRIZIONE);
    String sAddClause = "ATTIVO = '" + QueryBuilder.decodeBoolean(true) + "' AND ID_LUOGO<>0";
    String sSQL = qb.select("CMS_LUOGHI", mapFilter, sAddClause);
    sSQL += " ORDER BY ID_LUOGO";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_LUOGO");
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
      oLogger.error("Exception in WSLuoghi.lookup(" + mapFilter + ")", ex);
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
  List<List<Object>> getTipiLuogo(int iIdLingua)
      throws Exception
  {
    return getTipiLuogo(iIdLingua, false);
  }
  
  public static
  List<List<Object>> getTipiLuogo(int iIdLingua, boolean boSoloAttive)
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    String sSQL = "SELECT T.ID_TIPO_LUOGO,TD.DESCRIZIONE,T.ATTIVO ";
    sSQL += "FROM CMS_TIPI_LUOGO T,CMS_TIPI_LUOGO_DESC TD ";
    sSQL += "WHERE T.ID_TIPO_LUOGO=TD.ID_TIPO_LUOGO ";
    sSQL += "AND TD.ID_LINGUA=? ";
    if(boSoloAttive) sSQL += "AND T.ATTIVO = ? ";
    sSQL += "ORDER BY TD.DESCRIZIONE";
    
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
        int iId             = rs.getInt("ID_TIPO_LUOGO");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sAttivo      = rs.getString("ATTIVO");
        boolean boAttivo    = sAttivo != null ? sAttivo.equalsIgnoreCase("S") : false;
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iId);
        if(boAttivo) {
          record.add(sDescrizione);
        }
        else {
          record.add("(" + sDescrizione + ")");
        }
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSLuoghi.getTipiLuogo(" + iIdLingua + "," + boSoloAttive + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
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
    String search = WUtil.toString(mapFilter.remove(sRICERCA), null);
    if(search != null && search.length() > 0) {
      mapFilter.put(sRICERCA, getLikeFilter(search));
    }
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("L.ID_LUOGO",              sID);
    qb.put("L.ID_TIPO_LUOGO",         sID_TIPO);
    qb.put("TD.DESCRIZIONE DESC_TIP", sDESC_TIPO);
    qb.put("L.CODICE",                sCODICE);
    qb.put("L.DESCRIZIONE",           sDESCRIZIONE);
    qb.put("L.INDIRIZZO",             sINDIRIZZO);
    qb.put("C.DESCRIZIONE DESC_COM",  sDESC_COMUNE);
    qb.put("L.CAP",                   sCAP);
    qb.put("L.RICERCA",               sRICERCA);
    qb.put("L.ATTIVO",                sATTIVO);
    String sAddClause = "L.ID_TIPO_LUOGO=T.ID_TIPO_LUOGO AND T.ID_TIPO_LUOGO=TD.ID_TIPO_LUOGO AND TD.ID_LINGUA=0 AND L.ID_COMUNE=C.ID_COMUNE AND L.ID_LUOGO<>0";
    String sSQL = qb.select("CMS_LUOGHI L,CMS_TIPI_LUOGO T,CMS_TIPI_LUOGO_DESC TD,CMS_COMUNI C", mapFilter, sAddClause);
    sSQL += " ORDER BY L.ID_LUOGO";
    
    int iRows = 0;
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_LUOGO");
        int iIdTipoLuogo    = rs.getInt("ID_TIPO_LUOGO");
        String sTipoLuogo   = rs.getString("DESC_TIP");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        String sIndirizzo   = rs.getString("INDIRIZZO");
        String sDescComune  = rs.getString("DESC_COM");
        String sCap         = rs.getString("CAP");
        String sAttivo      = rs.getString("ATTIVO");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sID_TIPO,       iIdTipoLuogo);
        record.put(sDESC_TIPO,     sTipoLuogo);
        record.put(sCODICE,        sCodice);
        record.put(sDESCRIZIONE,   sDescrizione);
        record.put(sINDIRIZZO,     sIndirizzo);
        record.put(sDESC_COMUNE,   sDescComune);
        record.put(sCAP,           sCap);
        record.putBoolean(sATTIVO, sAttivo);
        
        result.add(record.toMapObject());
        iRows++;
        if(iRows > 2000) break;
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSLuoghi.find(" + mapFilter + ")", ex);
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
    qb.add("L.ID_TIPO_LUOGO");
    qb.add("L.CODICE");
    qb.add("L.DESCRIZIONE");
    qb.add("L.ID_COMUNE");
    qb.add("C.COD_ISTAT COD_COM");
    qb.add("C.DESCRIZIONE DESC_COM");
    qb.add("L.INDIRIZZO");
    qb.add("L.CAP");
    qb.add("L.SITO_WEB");
    qb.add("L.EMAIL");
    qb.add("L.TEL_1");
    qb.add("L.TEL_2");
    qb.add("L.FAX");
    qb.add("L.LATITUDINE");
    qb.add("L.LONGITUDINE");
    qb.add("L.ATTIVO");
    String sSQL = qb.select("CMS_LUOGHI L,CMS_COMUNI C");
    sSQL += "WHERE L.ID_COMUNE=C.ID_COMUNE AND L.ID_LUOGO=?";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdTipoLuogo    = rs.getInt("ID_TIPO_LUOGO");
        String sCodice      = rs.getString("CODICE");
        String sDescrizione = rs.getString("DESCRIZIONE");
        int iIdComune       = rs.getInt("ID_COMUNE");
        String sCodComune   = rs.getString("COD_COM");
        String sDescComune  = rs.getString("DESC_COM");
        String sIndirizzo   = rs.getString("INDIRIZZO");
        String sCap         = rs.getString("CAP");
        String sSitoWeb     = rs.getString("SITO_WEB");
        String sEmail       = rs.getString("EMAIL");
        String sTel1        = rs.getString("TEL_1");
        String sTel2        = rs.getString("TEL_2");
        String sFax         = rs.getString("FAX");
        double dLatitudine  = rs.getDouble("LATITUDINE");
        double dLongitudine = rs.getDouble("LONGITUDINE");
        String sAttivo      = rs.getString("ATTIVO");
        String sDescTipo    = getDescTipo(conn, iIdTipoLuogo);
        
        result.put(sID,            iId);
        result.put(sID_TIPO,       iIdTipoLuogo);
        result.put(sDESC_TIPO,     sDescTipo);
        result.put(sCODICE,        sCodice);
        result.put(sDESCRIZIONE,   sDescrizione);
        result.putList(sID_COMUNE, iIdComune, sCodComune, sDescComune);
        result.put(sDESC_COMUNE,   sDescComune);
        result.put(sINDIRIZZO,     sIndirizzo);
        result.put(sCAP,           sCap);
        result.put(sSITO_WEB,      sSitoWeb);
        result.put(sEMAIL,         sEmail);
        result.put(sTEL_1,         sTel1);
        result.put(sTEL_2,         sTel2);
        result.put(sFAX,           sFax);
        result.put(sLATITUDINE,    dLatitudine);
        result.put(sLONGITUDINE,   dLongitudine);
        result.putBoolean(sATTIVO, sAttivo);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSLuoghi.read(" + iId + ")", ex);
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
    qb.add("ID_LUOGO");
    qb.add("ID_TIPO_LUOGO");
    qb.add("CODICE");
    qb.add("DESCRIZIONE");
    qb.add("ID_COMUNE");
    qb.add("INDIRIZZO");
    qb.add("CAP");
    qb.add("SITO_WEB");
    qb.add("EMAIL");
    qb.add("TEL_1");
    qb.add("TEL_2");
    qb.add("FAX");
    qb.add("INFORMAZIONI");
    qb.add("LATITUDINE");
    qb.add("LONGITUDINE");
    qb.add("RICERCA");
    qb.add("ATTIVO");
    String sSQL = qb.insert("CMS_LUOGHI", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_LUOGHI");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap dmValues = new WMap(mapValues);
      int iIdTipoLuogo     = dmValues.getInt(sID_TIPO);
      String sCodice       = dmValues.getString(sCODICE);
      String sDescrizione  = dmValues.getString(sDESCRIZIONE);
      int iIdComune        = dmValues.getInt(sID_COMUNE);
      String sIndirizzo    = dmValues.getString(sINDIRIZZO);
      String sCap          = dmValues.getString(sCAP);
      String sSitoWeb      = dmValues.getString(sSITO_WEB);
      String sEmail        = dmValues.getString(sEMAIL);
      String sTel_1        = dmValues.getString(sTEL_1);
      String sTel_2        = dmValues.getString(sTEL_2);
      String sFax          = dmValues.getString(sFAX);
      String sInformazioni = dmValues.getString(sINFORMAZIONI);
      double dLatitudine   = dmValues.getDouble(sLATITUDINE);
      double dLongitudine  = dmValues.getDouble(sLONGITUDINE);
      String sDescTipo     = getDescTipo(conn, iIdTipoLuogo);
      String sDescComune   = getDescComune(conn, iIdComune);
      String sTextRicerca  = sDescrizione;
      if(sDescTipo   != null && sDescTipo.length()   > 1) sTextRicerca += " " + sDescTipo;
      if(sIndirizzo  != null && sIndirizzo.length()  > 1) sTextRicerca += " " + sIndirizzo;
      if(sDescComune != null && sDescComune.length() > 1) sTextRicerca += " " + sDescComune;
      String sRicerca      = getRicerca(sTextRicerca);
      if(sCodice == null || sCodice.length() == 0) {
        sCodice = "L-" + WUtil.lpad(String.valueOf(iId), '0', 5);
      }
      
      int p = 0;
      pstm.setInt(++p,    iId);
      pstm.setInt(++p,    iIdTipoLuogo);
      pstm.setString(++p, sCodice);
      pstm.setString(++p, sDescrizione);
      pstm.setInt(++p,    iIdComune);
      pstm.setString(++p, sIndirizzo);
      pstm.setString(++p, sCap);
      pstm.setString(++p, sSitoWeb);
      pstm.setString(++p, sEmail);
      pstm.setString(++p, sTel_1);
      pstm.setString(++p, sTel_2);
      pstm.setString(++p, sFax);
      pstm.setString(++p, sInformazioni);
      pstm.setDouble(++p, dLatitudine);
      pstm.setDouble(++p, dLongitudine);
      pstm.setString(++p, sRicerca);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      
      pstm.executeUpdate();
      
      ut.commit();
      
      mapValues.put(sID,          iId);
      mapValues.put(sCODICE,      sCodice);
      mapValues.put(sDESC_COMUNE, sDescComune);
      mapValues.put(sDESC_TIPO,   sDescTipo);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSLuoghi.insert(" + mapValues + ")", ex);
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
    qb.add("ID_TIPO_LUOGO");
    qb.add("CODICE");
    qb.add("DESCRIZIONE");
    qb.add("ID_COMUNE");
    qb.add("INDIRIZZO");
    qb.add("CAP");
    qb.add("SITO_WEB");
    qb.add("EMAIL");
    qb.add("TEL_1");
    qb.add("TEL_2");
    qb.add("FAX");
    qb.add("INFORMAZIONI");
    qb.add("LATITUDINE");
    qb.add("LONGITUDINE");
    qb.add("RICERCA");
    String sSQL = qb.update("CMS_LUOGHI", true);
    sSQL += "WHERE ID_LUOGO=?";
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap dmValues = new WMap(mapValues);
      int iId              = dmValues.getInt(sID);
      int iIdTipoLuogo     = dmValues.getInt(sID_TIPO);
      String sCodice       = dmValues.getString(sCODICE);
      String sDescrizione  = dmValues.getString(sDESCRIZIONE);
      int iIdComune        = dmValues.getInt(sID_COMUNE);
      String sIndirizzo    = dmValues.getString(sINDIRIZZO);
      String sCap          = dmValues.getString(sCAP);
      String sSitoWeb      = dmValues.getString(sSITO_WEB);
      String sEmail        = dmValues.getString(sEMAIL);
      String sTel_1        = dmValues.getString(sTEL_1);
      String sTel_2        = dmValues.getString(sTEL_2);
      String sFax          = dmValues.getString(sFAX);
      String sInformazioni = dmValues.getString(sINFORMAZIONI);
      double dLatitudine   = dmValues.getDouble(sLATITUDINE);
      double dLongitudine  = dmValues.getDouble(sLONGITUDINE);
      String sDescTipo     = getDescTipo(conn, iIdTipoLuogo);
      String sDescComune   = getDescComune(conn, iIdComune);
      String sTextRicerca  = sDescrizione;
      if(sDescTipo   != null && sDescTipo.length()   > 1) sTextRicerca += " " + sDescTipo;
      if(sIndirizzo  != null && sIndirizzo.length()  > 1) sTextRicerca += " " + sIndirizzo;
      if(sDescComune != null && sDescComune.length() > 1) sTextRicerca += " " + sDescComune;
      String sRicerca      = getRicerca(sTextRicerca);
      if(sCodice == null || sCodice.length() == 0) {
        sCodice = "L-" + WUtil.lpad(String.valueOf(iId), '0', 5);
      }
      
      // SET
      int p = 0;
      pstm.setInt(++p,    iIdTipoLuogo);
      pstm.setString(++p, sCodice);
      pstm.setString(++p, sDescrizione);
      pstm.setInt(++p,    iIdComune);
      pstm.setString(++p, sIndirizzo);
      pstm.setString(++p, sCap);
      pstm.setString(++p, sSitoWeb);
      pstm.setString(++p, sEmail);
      pstm.setString(++p, sTel_1);
      pstm.setString(++p, sTel_2);
      pstm.setString(++p, sFax);
      pstm.setString(++p, sInformazioni);
      pstm.setDouble(++p, dLatitudine);
      pstm.setDouble(++p, dLongitudine);
      pstm.setString(++p, sRicerca);
      // WHERE
      pstm.setInt(++p, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
      
      mapValues.put(sCODICE,      sCodice);
      mapValues.put(sDESC_COMUNE, sDescComune);
      mapValues.put(sDESC_TIPO,   sDescTipo);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSLuoghi.update(" + mapValues + ")", ex);
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
      
      pstm = conn.prepareStatement("UPDATE CMS_LUOGHI SET ATTIVO=? WHERE ID_LUOGO=?");
      // SET
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      // WHERE
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSLuoghi.setEnabled(" + iId + "," + boEnabled + ")", ex);
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
      
      pstm = conn.prepareStatement("DELETE FROM CMS_LUOGHI WHERE ID_LUOGO=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSLuoghi.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  String getRicerca(String sText) 
  {
    String result = "";
    if(sText == null || sText.trim().length() == 0) return result;
    StringBuilder sb = new StringBuilder(sText.length());
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c)) sb.append(c); else sb.append(' ');
    }
    List<String> listTokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(sb.toString(), " ");
    while(st.hasMoreTokens()) {
      String sToken = st.nextToken().toLowerCase().trim();
      if(sToken.length() > 1) {
        if(!listTokens.contains(sToken)) listTokens.add(sToken);
      }
    }
    if(listTokens.size() == 0) return "";
    Collections.sort(listTokens);
    for(int i = 0; i < listTokens.size(); i++) {
      result += "," + listTokens.get(i);
    }
    return result + ",";
  }	
  
  public static
  String getLikeFilter(String sText) 
  {
    String result = "";
    if(sText == null || sText.trim().length() == 0) return result;
    StringBuilder sb = new StringBuilder(sText.length());
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isLetter(c) || Character.isDigit(c)) sb.append(c); else sb.append(' ');
    }
    List<String> listTokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(sb.toString(), " ");
    while(st.hasMoreTokens()) {
      String sToken = st.nextToken().toLowerCase().trim();
      if(sToken.length() > 1) {
        if(!listTokens.contains(sToken)) listTokens.add(sToken);
      }
    }
    if(listTokens.size() == 0) return "";
    Collections.sort(listTokens);
    for(int i = 0; i < listTokens.size(); i++) {
      String sToken = listTokens.get(i);
      sToken = sToken.substring(0, sToken.length()-1) + "%";
      result += "," + sToken;
    }
    return "%" + result;
  }
  
  public static 
  String getDescComune(Connection conn, int iIdComune)
      throws Exception
  {
    if(iIdComune == 0) return "";
    String result = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT DESCRIZIONE FROM CMS_COMUNI WHERE ID_COMUNE=?");
      pstm.setInt(1, iIdComune);
      rs = pstm.executeQuery();
      if(rs.next()) {
        result = rs.getString("DESCRIZIONE");
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSLuoghi.getDescComune(conn," + iIdComune + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    if(result == null) result = "";
    return result;
  }
  
  public static 
  String getDescTipo(Connection conn, int iIdTipoLuogo)
      throws Exception
  {
    if(iIdTipoLuogo == 0) return "";
    
    String result = null;
    
    String sSQL = "SELECT TD.DESCRIZIONE ";
    sSQL += "FROM CMS_TIPI_LUOGO T,CMS_TIPI_LUOGO_DESC TD ";
    sSQL += "WHERE T.ID_TIPO_LUOGO=TD.ID_TIPO_LUOGO ";
    sSQL += "AND T.ID_TIPO_LUOGO=? AND TD.ID_LINGUA=? ";
    sSQL += "ORDER BY TD.DESCRIZIONE";
    
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdTipoLuogo);
      pstm.setInt(2, 0);
      rs = pstm.executeQuery();
      if(rs.next()) {
        result = rs.getString("DESCRIZIONE");
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSLuoghi.getDescTipo(conn," + iIdTipoLuogo + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    if(result == null) result = "";
    return result;
  }
  
  public static
  Place toPlace(Object oPlace, int defIdPlace)
      throws Exception
  {
    if(oPlace == null) {
      return new Place(defIdPlace);
    }
    if(oPlace instanceof Place) {
      return (Place) oPlace;
    }
    if(oPlace instanceof Map) {
      return new Place(WUtil.toMapObject(oPlace));
    }
    int id = WUtil.toInt(oPlace, defIdPlace);
    return new Place(id);
  }
}
