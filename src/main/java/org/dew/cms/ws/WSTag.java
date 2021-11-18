package org.dew.cms.ws;

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
import org.dew.cms.Tag;
import org.dew.cms.common.ITag;
import org.dew.cms.util.CMSCache;
import org.dew.cms.util.ConnectionManager;
import org.dew.cms.util.QueryBuilder;

public 
class WSTag implements ITag 
{
  protected static Logger oLogger = Logger.getLogger(WSTag.class);
  
  public static
  Map<String, Object> read(String sCodice, int iIdLingua)
      throws Exception
  {
    WMap result = new WMap();
    
    if(sCodice == null || sCodice.length() == 0) {
      return result.toMapObject();
    }
    
    String sSQL = "SELECT T.ID_TAG,T.ANTEPRIMA,TD.ID_LINGUA,TD.DESCRIZIONE ";
    sSQL += "FROM CMS_TAG T,CMS_TAG_DESC TD ";
    sSQL += "WHERE T.ID_TAG=TD.ID_TAG ";
    sSQL += "AND T.CODICE=?";
    int iIdTag = 0;
    String sFlagAnteprima = null;
    String sDescrizione   = null;
    String sDescrizione0  = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setString(1, sCodice);
      rs = pstm.executeQuery();
      while(rs.next()) {
        iIdTag           = rs.getInt("ID_TAG");
        sFlagAnteprima   = rs.getString("ANTEPRIMA");
        int iTagIdLingua = rs.getInt("ID_LINGUA");
        String sTagDesc  = rs.getString("DESCRIZIONE");
        if(sTagDesc == null || sTagDesc.length() == 0) continue;
        if(iTagIdLingua == 0) sDescrizione0 = sTagDesc;
        if(iTagIdLingua == iIdLingua) {
          sDescrizione = sTagDesc;
          break;
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.read", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    if(sDescrizione == null || sDescrizione.length() == 0) {
      sDescrizione = sDescrizione0;
    }
    if(sDescrizione == null || sDescrizione.length() == 0) {
      sDescrizione = sCodice;
    }
    
    result.put(sID,               iIdTag);
    result.put(sCODICE,           sCodice);
    result.put(sDESCRIZIONE,      sDescrizione);
    result.putBoolean(sANTEPRIMA, sFlagAnteprima);
    
    return result.toMapObject();
  }
  
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
    qb.put("ID_TAG", sID);
    qb.put("CODICE", sCODICE + "%");
    String sAddClause = "ATTIVO='" + QueryBuilder.decodeBoolean(true) + "'";
    String sSQL = qb.select("CMS_TAG", mapFilter, sAddClause);
    sSQL += " ORDER BY CODICE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int    iId     = rs.getInt("ID_TAG");
        String sCodice = rs.getString("CODICE");
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iId);
        record.add(sCodice);
        record.add(sCodice);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.lookup(" + mapFilter + ")", ex);
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
    
    boolean boEscludiTagUsoInterno = WUtil.toBoolean(mapFilter.get(sESCL_USO_INT), false);
    
    QueryBuilder qb = new QueryBuilder();
    qb.put("ID_TAG",    sID);
    qb.put("CODICE",    sCODICE + "%");
    qb.put("ANTEPRIMA", sANTEPRIMA);
    qb.put("ORDINE",    sORDINE);
    qb.put("ATTIVO",    sATTIVO);
    String sAddClause = "ID_TAG > 0";
    String sSQL = qb.select("CMS_TAG", mapFilter, sAddClause);
    sSQL += " ORDER BY ORDINE";
    
    List<String> listGruppi = new ArrayList<String>();
    Map<String, List<Map<String, Object>>> mapGruppi = new HashMap<String, List<Map<String, Object>>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId           = rs.getInt("ID_TAG");
        String sCodice    = rs.getString("CODICE");
        String sAnteprima = rs.getString("ANTEPRIMA");
        int iOrdine       = rs.getInt("ORDINE");
        String sAttivo    = rs.getString("ATTIVO");
        if(sCodice == null) continue;
        if(boEscludiTagUsoInterno && sCodice.startsWith("#")) continue;
        int iSep = sCodice.indexOf('_');
        String sPrefix = iSep > 0 ? sCodice.substring(0, iSep+1) : sCodice;
        
        WMap record = new WMap();
        record.put(sID,               iId);
        record.put(sCODICE,           sCodice);
        record.putBoolean(sANTEPRIMA, sAnteprima);
        record.put(sORDINE,           iOrdine);
        record.putBoolean(sATTIVO,    sAttivo);
        
        List<Map<String, Object>> listGruppo = mapGruppi.get(sPrefix);
        if(listGruppo == null) {
          listGruppo = new ArrayList<Map<String,Object>>();
          mapGruppi.put(sPrefix, listGruppo);
        }
        
        listGruppo.add(record.toMapObject());
        
        if(!listGruppi.contains(sPrefix)) {
          listGruppi.add(sPrefix);
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.find(" + mapFilter + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();  } catch(Exception ex) {}
      if(stm  != null) try{ stm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    
    boolean boGruppo = true;
    for(int i = 0; i < listGruppi.size(); i++) {
      List<Map<String, Object>> listGruppo = mapGruppi.get(listGruppi.get(i));
      if(listGruppo == null) continue;
      
      boGruppo = !boGruppo;
      for(int g = 0; g < listGruppo.size(); g++) {
        Map<String, Object> mapGruppo = listGruppo.get(g);
        mapGruppo.put(sGRUPPO, boGruppo ? Boolean.TRUE : Boolean.FALSE);
        
        result.add(mapGruppo);
      }
    }
    
    return result;
  }
  
  public static
  List<Integer> find(int iIdLingua, String sDescrizione)
      throws Exception
  {
    Connection conn = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      return find(conn, iIdLingua, sDescrizione);
    }
    finally {
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public static
  List<Integer> find(Connection conn, int iIdLingua, String sDescrizione)
      throws Exception
  {
    List<Integer> result = new ArrayList<Integer>();
    
    if(sDescrizione == null || sDescrizione.length() == 0) {
      return result;
    }
    
    String sSQL = "SELECT ID_TAG FROM CMS_TAG_DESC WHERE ID_LINGUA=? AND DESCRIZIONE IN (?,?,?) GROUP BY ID_TAG ORDER BY ID_TAG";
    String s1 = sDescrizione.toLowerCase();
    String s2 = sDescrizione.toUpperCase();
    String s3 = null;
    if(sDescrizione.length() > 1) {
      s3 = s2.substring(0,1) + s1.substring(1);
    }
    else {
      s3 = sDescrizione;
    }
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iIdLingua);
      pstm.setString(2, s1);
      pstm.setString(3, s2);
      pstm.setString(4, s3);
      rs = pstm.executeQuery();
      while(rs.next()) {
        result.add(rs.getInt("ID_TAG"));
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.find(conn," + iIdLingua + "," + sDescrizione + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
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
    qb.add("ANTEPRIMA");
    qb.add("ORDINE");
    qb.add("ATTIVO");
    String sSQL = qb.select("CMS_TAG");
    sSQL += "WHERE ID_TAG=?";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        String sCodice    = rs.getString("CODICE");
        String sAnteprima = rs.getString("ANTEPRIMA");
        int iOrdine       = rs.getInt("ORDINE");
        String sAttivo    = rs.getString("ATTIVO");
        
        result.put(sID,               iId);
        result.put(sCODICE,           sCodice);
        result.putBoolean(sANTEPRIMA, sAnteprima);
        result.put(sORDINE,           iOrdine);
        result.putBoolean(sATTIVO,    sAttivo);
      }
      rs.close();
      pstm.close();
      
      Map<String, Object> mapDescrizioni = new HashMap<String, Object>();
      result.put(sDESCRIZIONE, mapDescrizioni);
      
      pstm = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_TAG_DESC WHERE ID_TAG=? ORDER BY ID_LINGUA");
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
      oLogger.error("Exception in WSTag.read(" + iId + ")", ex);
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
  int countArticles(int iId)
      throws Exception
  {
    int result = 0;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT COUNT(*) FROM CMS_ARTICOLI_TAG WHERE ID_TAG=?");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        result = rs.getInt(1);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.countArticles(" + iId + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  private static
  int getMaxOrdine(Connection conn, String sPrefix)
      throws Exception
  {
    if(sPrefix == null || sPrefix.length() == 0) return 0;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT MAX(ORDINE) FROM CMS_TAG WHERE CODICE LIKE ?");
      pstm.setString(1, sPrefix + "%");
      rs = pstm.executeQuery();
      if(rs.next()) return rs.getInt(1);
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.getMaxOrdine(conn," + sPrefix + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return 0;
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
      pstm = conn.prepareStatement("SELECT ID_TAG FROM CMS_TAG WHERE CODICE=?");
      pstm.setString(1, sCodice);
      rs   = pstm.executeQuery();
      return rs.next();
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSTag.exists(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public
  Map<String, Object> insert(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_TAG");
    qb.add("CODICE");
    qb.add("ANTEPRIMA");
    qb.add("ORDINE");
    qb.add("ATTIVO");
    String sSQL = qb.insert("CMS_TAG", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_TAG");
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap    wmValues    = new WMap(mapValues);
      String  sCodice     = wmValues.getString(sCODICE);
      boolean boAnteprima = wmValues.getBoolean(sANTEPRIMA);
      int     iOrdine     = wmValues.getInt(sORDINE);
      if(iOrdine == 0 && sCodice != null) {
        String sPrefix = null;
        int iSep = sCodice.indexOf("_");
        if(iSep > 0) {
          sPrefix = sCodice.substring(0, iSep + 1);
        }
        else {
          sPrefix = sCodice;
        }
        int iMaxOrdine = getMaxOrdine(conn, sPrefix);
        iOrdine = iMaxOrdine + 1;
      }
      
      int p = 0;
      pstm.setInt(++p,    iId);
      pstm.setString(++p, sCodice);
      pstm.setString(++p, QueryBuilder.decodeBoolean(boAnteprima));
      pstm.setInt(++p,    iOrdine);
      pstm.setString(++p, QueryBuilder.decodeBoolean(true));
      pstm.executeUpdate();
      
      Map<String, Object> mapDescrizioni = wmValues.getMapObject(sDESCRIZIONE);
      if(mapDescrizioni == null || mapDescrizioni.isEmpty()) {
        mapDescrizioni = new HashMap<String, Object>();
        mapDescrizioni.put("0", "&nbsp;");
      }
      insertDescrizioni(conn, iId, mapDescrizioni);
      
      ut.commit();
      
      CMSCache.mapCodeTag.clear();
      CMSCache.mapTags.clear();
      CMSCache.mapTagsPrev.clear();
      
      mapValues.put(sID,     iId);
      mapValues.put(sORDINE, iOrdine);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSTag.insert(" + mapValues + ")", ex);
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
    qb.add("ANTEPRIMA");
    qb.add("ORDINE");
    String sSQL = qb.update("CMS_TAG", true);
    sSQL += "WHERE ID_TAG=?";
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstm = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstm = conn.prepareStatement(sSQL);
      
      WMap    wmValues    = new WMap(mapValues);
      int     iId         = wmValues.getInt(sID);
      String  sCodice     = wmValues.getString(sCODICE);
      boolean boAnteprima = wmValues.getBoolean(sANTEPRIMA);
      int     iOrdine     = wmValues.getInt(sORDINE);
      if(iOrdine == 0 && sCodice != null) {
        String sPrefix = null;
        int iSep = sCodice.indexOf("_");
        if(iSep > 0) {
          sPrefix = sCodice.substring(0, iSep + 1);
        }
        else {
          sPrefix = sCodice;
        }
        int iMaxOrdine = getMaxOrdine(conn, sPrefix);
        iOrdine = iMaxOrdine + 1;
      }
      
      // SET
      int p = 0;
      pstm.setString(++p, sCodice);
      pstm.setString(++p, QueryBuilder.decodeBoolean(boAnteprima));
      pstm.setInt(++p,    iOrdine);
      // WHERE
      pstm.setInt(++p, iId);
      pstm.executeUpdate();
      
      deleteDescrizioni(conn, iId);
      Map<String, Object> mapDescrizioni = wmValues.getMapObject(sDESCRIZIONE);
      if(mapDescrizioni == null || mapDescrizioni.isEmpty()) {
        mapDescrizioni = new HashMap<String, Object>();
        mapDescrizioni.put("0", "&nbsp;");
      }
      insertDescrizioni(conn, iId, mapDescrizioni);
      
      ut.commit();
      
      CMSCache.mapCodeTag.clear();
      CMSCache.mapTags.clear();
      CMSCache.mapTagsPrev.clear();
      
      mapValues.put(sORDINE, iOrdine);
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSTag.update(" + mapValues + ")", ex);
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
    qb.add("ID_TAG");
    qb.add("ID_LINGUA");
    qb.add("DESCRIZIONE");
    String sSQL = qb.insert("CMS_TAG_DESC", true);
    
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
      pstm = conn.prepareStatement("DELETE FROM CMS_TAG_DESC WHERE ID_TAG=?");
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
      
      pstm = conn.prepareStatement("UPDATE CMS_TAG SET ATTIVO=? WHERE ID_TAG=?");
      // SET
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      // WHERE
      pstm.setInt(2, iId);
      pstm.executeUpdate();
      
      ut.commit();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSTag.setEnabled(" + iId + "," + boEnabled + ")", ex);
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
    PreparedStatement pstmA = null;
    PreparedStatement pstm  = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstmA = conn.prepareStatement("DELETE FROM CMS_ARTICOLI_TAG WHERE ID_TAG=?");
      pstmA.setInt(1, iId);
      pstmA.executeUpdate();
      
      deleteDescrizioni(conn, iId);
      
      pstm = conn.prepareStatement("DELETE FROM CMS_TAG WHERE ID_TAG=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
      
      CMSCache.mapCodeTag.clear();
      CMSCache.mapTags.clear();
      CMSCache.mapTagsPrev.clear();
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSTag.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstm  != null) try{ pstm.close();  } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  Tag toTag(Object oTag, int defIdTag)
      throws Exception
  {
    if(oTag == null) {
      return new Tag(defIdTag);
    }
    if(oTag instanceof Tag) {
      return (Tag) oTag;
    }
    if(oTag instanceof Map) {
      return new Tag(WUtil.toMapObject(oTag));
    }
    int idTag = WUtil.toInt(oTag, defIdTag);
    return new Tag(idTag);
  }
  
  public static
  int getIdTag(Object oTag, int defIdTag)
      throws Exception
  {
    if(oTag == null) {
      return defIdTag;
    }
    if(oTag instanceof Tag) {
      return ((Tag) oTag).getId();
    }
    if(oTag instanceof Map) {
      return WUtil.toInt(WUtil.toMapObject(oTag).get(sID), defIdTag);
    }
    return WUtil.toInt(oTag, defIdTag);
  }
}
