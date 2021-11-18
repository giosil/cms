package org.dew.cms.ws;

import java.sql.Connection;
import java.sql.Date;
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
import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IPagina;
import org.dew.cms.util.CMSCache;
import org.dew.cms.util.ConnectionManager;
import org.dew.cms.util.QueryBuilder;

public 
class WSPagine implements IPagina 
{
  protected static Logger oLogger = Logger.getLogger(WSPagine.class);
  
  public final static int iTIPO_PAGINA_PUBBLICA    =  1;
  public final static int iTIPO_PAGINA_PRIVATA     =  2;
  public final static int iTIPO_MENU_INTESTAZIONE  =  3;
  public final static int iTIPO_MENU_NAVIGAZIONE   =  4;
  public final static int iTIPO_MENU_FOOTER        =  5;
  public final static int iTIPO_CITAZIONE          =  6;
  public final static int iTIPO_ELENCO_SINGOLO     =  7;
  public final static int iTIPO_ELENCO_MULTIPLO    =  8;
  public final static int iTIPO_ELENCO_PRINCIPALE  =  9;
  public final static int iTIPO_ELENCO_IN_EVIDENZA = 10;
  
  public static
  List<List<Object>> getTipiPagine()
      throws Exception
  {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery("SELECT ID_TIPO_PAGINA,DESCRIZIONE FROM CMS_TIPI_PAGINA WHERE ID_TIPO_PAGINA<>0 ORDER BY ID_TIPO_PAGINA");
      while(rs.next()) {
        int iIdTipoPagina   = rs.getInt("ID_TIPO_PAGINA");
        String sDescrizione = rs.getString("DESCRIZIONE");
        
        List<Object> record = new ArrayList<Object>(2);
        record.add(iIdTipoPagina);
        record.add(sDescrizione);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getTipiPagine()", ex);
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
  Map<String, Object> getPagina(int iId, int iIdLingua)
      throws Exception
  {
    WMap result = new WMap();
    
    String sSQL_P = "SELECT ID_TIPO_PAGINA,CODICE,ID_CATEGORIA,ID_SOTTOCATEGORIA,ID_TIPO_ARTICOLO,ORDINE,RIGHE,COLONNE,VISTA ";
    sSQL_P += "FROM CMS_PAGINE WHERE ID_PAGINA=?";
    
    String sSQL_A = "SELECT ID_ARTICOLO FROM CMS_PAGINE_ARTICOLI WHERE ID_PAGINA=? ORDER BY ORDINE";
    
    String sSQL_L = "SELECT ID_LINGUA,DESCRIZIONE FROM CMS_PAGINE_DESC WHERE ID_PAGINA=? ORDER BY ID_LINGUA";
    
    String sSQL_C = "SELECT C.ID_PAGINA_COMP,P.ID_TIPO_PAGINA,P.CODICE,P.ID_CATEGORIA,P.ID_SOTTOCATEGORIA,P.ID_TIPO_ARTICOLO,P.ORDINE,P.RIGHE,P.COLONNE,P.VISTA ";
    sSQL_C += "FROM CMS_PAGINE_COMP C,CMS_PAGINE P ";
    sSQL_C += "WHERE C.ID_PAGINA_COMP = P.ID_PAGINA ";
    sSQL_C += "AND C.ID_PAGINA=? AND P.ATTIVO=? ";
    sSQL_C += "ORDER BY C.ORDINE";
    
    Connection conn = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmA = null;
    PreparedStatement pstmL = null;
    PreparedStatement pstmC = null;
    ResultSet rsP = null;
    ResultSet rsA = null;
    ResultSet rsL = null;
    ResultSet rsC = null;
    try {
      conn  = ConnectionManager.getDefaultConnection();
      pstmP = conn.prepareStatement(sSQL_P);
      pstmA = conn.prepareStatement(sSQL_A);
      pstmL = conn.prepareStatement(sSQL_L);
      pstmC = conn.prepareStatement(sSQL_C);
      
      pstmP.setInt(1, iId);
      rsP   = pstmP.executeQuery();
      if(rsP.next()) {
        int iIdTipoPagina     = rsP.getInt("ID_TIPO_PAGINA");
        String sCodice        = rsP.getString("CODICE");
        int iIdCategoria      = rsP.getInt("ID_CATEGORIA");
        int iIdSottoCategoria = rsP.getInt("ID_SOTTOCATEGORIA");
        int iIdTipoArticolo   = rsP.getInt("ID_TIPO_ARTICOLO");
        int iRighe            = rsP.getInt("RIGHE");
        int iColonne          = rsP.getInt("COLONNE");
        int iVista            = rsP.getInt("VISTA");
        
        result.put(sID,            iId);
        result.put(sCODICE,        sCodice);
        result.put(sID_TIPO_PAG,   iIdTipoPagina);
        result.put(sID_CATEGORIA,  iIdCategoria);
        result.put(sID_SOTTOCATEG, iIdSottoCategoria);
        result.put(sID_TIPO_ART,   iIdTipoArticolo);
        result.put(sRIGHE,         iRighe);
        result.put(sCOLONNE,       iColonne);
        result.put(sVISTA,         iVista);
        
        List<Integer> listArticoli = new ArrayList<Integer>();
        result.put(sARTICOLI,      listArticoli);
        
        pstmA.setInt(1, iId);
        rsA = pstmA.executeQuery();
        while(rsA.next()) {
          listArticoli.add(rsA.getInt(1));
        }
        rsA.close();
        
        String sDescrizione0 = null;
        String sDescrizione  = null;
        pstmL.setInt(1, iId);
        rsL = pstmL.executeQuery();
        while(rsL.next()) {
          int iPIdLingua = rsL.getInt("ID_LINGUA");
          String sPDesc  = rsL.getString("DESCRIZIONE");
          if(iPIdLingua == 0) sDescrizione0 = sPDesc;
          if(iPIdLingua == iIdLingua) {
            sDescrizione = sPDesc;
            break;
          }
        }
        rsL.close();
        if(sDescrizione != null && sDescrizione.length() > 0) {
          result.put(sDESCRIZIONE, sDescrizione);
        }
        else if(sDescrizione0 != null && sDescrizione0.length() > 0) {
          result.put(sDESCRIZIONE, sDescrizione0);
        }
        
        List<Map<String, Object>> listComponenti = new ArrayList<Map<String,Object>>();
        result.put(sCOMPONENTI, listComponenti);
        
        pstmC.setInt(1, iId);
        pstmC.setString(2, QueryBuilder.decodeBoolean(true));
        rsC = pstmC.executeQuery();
        while(rsC.next()) {
          int iCIdPagina         = rsC.getInt("ID_PAGINA_COMP");
          int iCIdTipoPagina     = rsC.getInt("ID_TIPO_PAGINA");
          String sCCodice        = rsC.getString("CODICE");
          int iCIdCategoria      = rsC.getInt("ID_CATEGORIA");
          int iCIdSottoCategoria = rsC.getInt("ID_SOTTOCATEGORIA");
          int iCIdTipoArticolo   = rsC.getInt("ID_TIPO_ARTICOLO");
          int iCRighe            = rsC.getInt("RIGHE");
          int iCColonne          = rsC.getInt("COLONNE");
          int iCVista            = rsC.getInt("VISTA");
          
          WMap wmComp = new WMap();
          wmComp.put(sID,            iCIdPagina);
          wmComp.put(sCODICE,        sCCodice);
          wmComp.put(sID_TIPO_PAG,   iCIdTipoPagina);
          wmComp.put(sID_CATEGORIA,  iCIdCategoria);
          wmComp.put(sID_SOTTOCATEG, iCIdSottoCategoria);
          wmComp.put(sID_TIPO_ART,   iCIdTipoArticolo);
          wmComp.put(sRIGHE,         iCRighe);
          wmComp.put(sCOLONNE,       iCColonne);
          wmComp.put(sVISTA,         iCVista);
          String sPDescrizione0 = null;
          String sPDescrizione  = null;
          pstmL.setInt(1, iCIdPagina);
          rsL = pstmL.executeQuery();
          while(rsL.next()) {
            int iPIdLingua = rsL.getInt("ID_LINGUA");
            String sPDesc  = rsL.getString("DESCRIZIONE");
            if(iPIdLingua == 0) sPDescrizione0 = sPDesc;
            if(iPIdLingua == iIdLingua) {
              sPDescrizione = sPDesc;
              break;
            }
          }
          rsL.close();
          if(sPDescrizione != null && sPDescrizione.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sPDescrizione);
          }
          else if(sPDescrizione0 != null && sPDescrizione0.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sPDescrizione0);
          }
          
          List<Integer> listCompArticoli = new ArrayList<Integer>();
          wmComp.put(sARTICOLI, listCompArticoli);
          
          pstmA.setInt(1, iCIdPagina);
          rsA = pstmA.executeQuery();
          while(rsA.next()) {
            listCompArticoli.add(rsA.getInt(1));
          }
          rsA.close();
          
          listComponenti.add(wmComp.toMapObject());
        }
        rsC.close();
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getPagina(" + iId + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(rsP   != null) try{ rsP.close();   } catch(Exception ex) {}
      if(rsA   != null) try{ rsA.close();   } catch(Exception ex) {}
      if(rsL   != null) try{ rsL.close();   } catch(Exception ex) {}
      if(rsC   != null) try{ rsC.close();   } catch(Exception ex) {}
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstmL != null) try{ pstmL.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return result.toMapObject();
  }
  
  public static
  Map<String, Object> getPagina(String sCodice, int iIdLingua)
      throws Exception
  {
    WMap result = new WMap();
    
    String sSQL_P = "SELECT ID_PAGINA,ID_TIPO_PAGINA,ID_CATEGORIA,ID_SOTTOCATEGORIA,ID_TIPO_ARTICOLO,ORDINE,RIGHE,COLONNE,VISTA ";
    sSQL_P += "FROM CMS_PAGINE WHERE CODICE=?";
    
    String sSQL_A = "SELECT ID_ARTICOLO FROM CMS_PAGINE_ARTICOLI WHERE ID_PAGINA = ? ORDER BY ORDINE";
    
    String sSQL_L = "SELECT ID_LINGUA,DESCRIZIONE FROM CMS_PAGINE_DESC WHERE ID_PAGINA = ? ORDER BY ID_LINGUA";
    
    String sSQL_C = "SELECT C.ID_PAGINA_COMP,P.ID_TIPO_PAGINA,P.ID_CATEGORIA,P.ID_SOTTOCATEGORIA,P.ID_TIPO_ARTICOLO,P.ORDINE,P.RIGHE,P.COLONNE,P.VISTA ";
    sSQL_C += "FROM CMS_PAGINE_COMP C,CMS_PAGINE P ";
    sSQL_C += "WHERE C.ID_PAGINA_COMP=P.ID_PAGINA ";
    sSQL_C += "AND C.ID_PAGINA=? AND P.ATTIVO=? ";
    sSQL_C += "ORDER BY C.ORDINE";
    
    int iId = 0;
    Connection conn = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmA = null;
    PreparedStatement pstmL = null;
    PreparedStatement pstmC = null;
    ResultSet rsP = null;
    ResultSet rsA = null;
    ResultSet rsL = null;
    ResultSet rsC = null;
    try {
      conn  = ConnectionManager.getDefaultConnection();
      pstmP = conn.prepareStatement(sSQL_P);
      pstmA = conn.prepareStatement(sSQL_A);
      pstmL = conn.prepareStatement(sSQL_L);
      pstmC = conn.prepareStatement(sSQL_C);
      
      pstmP.setString(1, sCodice);
      rsP   = pstmP.executeQuery();
      if(rsP.next()) {
        iId                   = rsP.getInt("ID_PAGINA");
        int iIdTipoPagina     = rsP.getInt("ID_TIPO_PAGINA");
        int iIdCategoria      = rsP.getInt("ID_CATEGORIA");
        int iIdSottoCategoria = rsP.getInt("ID_SOTTOCATEGORIA");
        int iIdTipoArticolo   = rsP.getInt("ID_TIPO_ARTICOLO");
        int iRighe            = rsP.getInt("RIGHE");
        int iColonne          = rsP.getInt("COLONNE");
        int iVista            = rsP.getInt("VISTA");
        
        result.put(sID,            iId);
        result.put(sID_TIPO_PAG,   iIdTipoPagina);
        result.put(sID_CATEGORIA,  iIdCategoria);
        result.put(sID_SOTTOCATEG, iIdSottoCategoria);
        result.put(sID_TIPO_ART,   iIdTipoArticolo);
        result.put(sRIGHE,         iRighe);
        result.put(sCOLONNE,       iColonne);
        result.put(sVISTA,         iVista);
        
        List<Integer> listArticoli = new ArrayList<Integer>();
        result.put(sARTICOLI, listArticoli);
        
        pstmA.setInt(1, iId);
        rsA = pstmA.executeQuery();
        while(rsA.next()) {
          listArticoli.add(rsA.getInt(1));
        }
        rsA.close();
        
        String sDescrizione0 = null;
        String sDescrizione  = null;
        pstmL.setInt(1, iId);
        rsL = pstmL.executeQuery();
        while(rsL.next()) {
          int iPIdLingua = rsL.getInt("ID_LINGUA");
          String sPDesc  = rsL.getString("DESCRIZIONE");
          if(iPIdLingua == 0) sDescrizione0 = sPDesc;
          if(iPIdLingua == iIdLingua) {
            sDescrizione = sPDesc;
            break;
          }
        }
        rsL.close();
        if(sDescrizione != null && sDescrizione.length() > 0) {
          result.put(sDESCRIZIONE, sDescrizione);
        }
        else if(sDescrizione0 != null && sDescrizione0.length() > 0) {
          result.put(sDESCRIZIONE,  sDescrizione0);
        }
        
        List<Map<String, Object>> listComponenti = new ArrayList<Map<String,Object>>();
        result.put(sCOMPONENTI, listComponenti);
        
        pstmC.setInt(1, iId);
        pstmC.setString(2, QueryBuilder.decodeBoolean(true));
        rsC = pstmC.executeQuery();
        while(rsC.next()) {
          int iPIdPagina         = rsC.getInt("ID_PAGINA_COMP");
          int iPIdTipoPagina     = rsC.getInt("ID_TIPO_PAGINA");
          int iPIdCategoria      = rsC.getInt("ID_CATEGORIA");
          int iPIdSottoCategoria = rsC.getInt("ID_SOTTOCATEGORIA");
          int iPIdTipoArticolo   = rsC.getInt("ID_TIPO_ARTICOLO");
          int iPRighe            = rsC.getInt("RIGHE");
          int iPColonne          = rsC.getInt("COLONNE");
          int iPVista            = rsC.getInt("VISTA");
          
          WMap wmComp = new WMap();
          wmComp.put(sID,            iPIdPagina);
          wmComp.put(sID_TIPO_PAG,   iPIdTipoPagina);
          wmComp.put(sID_CATEGORIA,  iPIdCategoria);
          wmComp.put(sID_SOTTOCATEG, iPIdSottoCategoria);
          wmComp.put(sID_TIPO_ART,   iPIdTipoArticolo);
          wmComp.put(sRIGHE,         iPRighe);
          wmComp.put(sCOLONNE,       iPColonne);
          wmComp.put(sVISTA,         iPVista);
          
          String sPDescrizione0 = null;
          String sPDescrizione  = null;
          pstmL.setInt(1, iPIdPagina);
          rsL = pstmL.executeQuery();
          while(rsL.next()) {
            int iPIdLingua = rsL.getInt("ID_LINGUA");
            String sPDesc  = rsL.getString("DESCRIZIONE");
            if(iPIdLingua == 0) sPDescrizione0 = sPDesc;
            if(iPIdLingua == iIdLingua) {
              sPDescrizione = sPDesc;
              break;
            }
          }
          rsL.close();
          if(sPDescrizione != null && sPDescrizione.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sPDescrizione);
          }
          else if(sPDescrizione0 != null && sPDescrizione0.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sPDescrizione0);
          }
          
          List<Integer> listCompArticoli = new ArrayList<Integer>();
          wmComp.put(sARTICOLI, listCompArticoli);
          
          pstmA.setInt(1, iPIdPagina);
          rsA = pstmA.executeQuery();
          while(rsA.next()) {
            listCompArticoli.add(rsA.getInt(1));
          }
          rsA.close();
          
          listComponenti.add(wmComp.toMapObject());
        }
        rsC.close();
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getPagina(" + sCodice + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(rsP   != null) try{ rsP.close();   } catch(Exception ex) {}
      if(rsA   != null) try{ rsA.close();   } catch(Exception ex) {}
      if(rsL   != null) try{ rsL.close();   } catch(Exception ex) {}
      if(rsC   != null) try{ rsC.close();   } catch(Exception ex) {}
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstmL != null) try{ pstmL.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return result.toMapObject();
  }
  
  public static
  List<Map<String, Object>> getPagine(int iIdTipoPagina, int iIdLingua)
      throws Exception
  {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    
    String sSQL_P = "SELECT ID_PAGINA,CODICE,ID_CATEGORIA,ID_SOTTOCATEGORIA,ID_TIPO_ARTICOLO,ORDINE,RIGHE,COLONNE,VISTA ";
    sSQL_P += "FROM CMS_PAGINE WHERE ID_TIPO_PAGINA=? AND ATTIVO=? ORDER BY ORDINE";
    
    String sSQL_A = "SELECT ID_ARTICOLO FROM CMS_PAGINE_ARTICOLI WHERE ID_PAGINA=? ORDER BY ORDINE";
    
    String sSQL_L = "SELECT ID_LINGUA,DESCRIZIONE FROM CMS_PAGINE_DESC WHERE ID_PAGINA=? ORDER BY ID_LINGUA";
    
    String sSQL_C = "SELECT C.ID_PAGINA_COMP,P.ID_TIPO_PAGINA,P.CODICE,P.ID_CATEGORIA,P.ID_SOTTOCATEGORIA,P.ID_TIPO_ARTICOLO,P.ORDINE,P.RIGHE,P.COLONNE,P.VISTA ";
    sSQL_C += "FROM CMS_PAGINE_COMP C,CMS_PAGINE P WHERE C.ID_PAGINA_COMP=P.ID_PAGINA AND C.ID_PAGINA=? AND P.ATTIVO=? ORDER BY C.ORDINE";
    
    Connection conn = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmA = null;
    PreparedStatement pstmL = null;
    PreparedStatement pstmC = null;
    ResultSet rsP = null;
    ResultSet rsA = null;
    ResultSet rsL = null;
    ResultSet rsC = null;
    try {
      conn  = ConnectionManager.getDefaultConnection();
      
      pstmP = conn.prepareStatement(sSQL_P);
      pstmA = conn.prepareStatement(sSQL_A);
      pstmL = conn.prepareStatement(sSQL_L);
      pstmC = conn.prepareStatement(sSQL_C);
      
      pstmP.setInt(1, iIdTipoPagina);
      pstmP.setString(2, QueryBuilder.decodeBoolean(true));
      rsP   = pstmP.executeQuery();
      while(rsP.next()) {
        int iId               = rsP.getInt("ID_PAGINA");
        String sCodice        = rsP.getString("CODICE");
        int iIdCategoria      = rsP.getInt("ID_CATEGORIA");
        int iIdSottoCategoria = rsP.getInt("ID_SOTTOCATEGORIA");
        int iIdTipoArticolo   = rsP.getInt("ID_TIPO_ARTICOLO");
        int iRighe            = rsP.getInt("RIGHE");
        int iColonne          = rsP.getInt("COLONNE");
        int iVista            = rsP.getInt("VISTA");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sID_TIPO_PAG,   iIdTipoPagina);
        record.put(sCODICE,        sCodice);
        record.put(sID_CATEGORIA,  iIdCategoria);
        record.put(sID_SOTTOCATEG, iIdSottoCategoria);
        record.put(sID_TIPO_ART,   iIdTipoArticolo);
        record.put(sRIGHE,         iRighe);
        record.put(sCOLONNE,       iColonne);
        record.put(sVISTA,         iVista);
        
        List<Integer> listArticoli = new ArrayList<Integer>();
        record.put(sARTICOLI, listArticoli);
        
        pstmA.setInt(1, iId);
        rsA = pstmA.executeQuery();
        while(rsA.next()) {
          listArticoli.add(rsA.getInt(1));
        }
        rsA.close();
        
        String sDescrizione0 = null;
        String sDescrizione  = null;
        pstmL.setInt(1, iId);
        rsL = pstmL.executeQuery();
        while(rsL.next()) {
          int iPIdLingua = rsL.getInt("ID_LINGUA");
          String sPDesc  = rsL.getString("DESCRIZIONE");
          if(iPIdLingua == 0) sDescrizione0 = sPDesc;
          if(iPIdLingua == iIdLingua) {
            sDescrizione = sPDesc;
            break;
          }
        }
        rsL.close();
        if(sDescrizione != null && sDescrizione.length() > 0) {
          record.put(sDESCRIZIONE, sDescrizione);
        }
        else if(sDescrizione0 != null && sDescrizione0.length() > 0) {
          record.put(sDESCRIZIONE, sDescrizione0);
        }
        
        List<Map<String, Object>> listComponenti = new ArrayList<Map<String,Object>>();
        record.put(sCOMPONENTI, listComponenti);
        
        pstmC.setInt(1, iId);
        pstmC.setString(2, QueryBuilder.decodeBoolean(true));
        rsC = pstmC.executeQuery();
        while(rsC.next()) {
          int iCIdPagina         = rsC.getInt("ID_PAGINA_COMP");
          int iCIdTipoPagina     = rsC.getInt("ID_TIPO_PAGINA");
          String sCCodice        = rsC.getString("CODICE");
          int iCIdCategoria      = rsC.getInt("ID_CATEGORIA");
          int iCIdSottoCategoria = rsC.getInt("ID_SOTTOCATEGORIA");
          int iCIdTipoArticolo   = rsC.getInt("ID_TIPO_ARTICOLO");
          int iCRighe            = rsC.getInt("RIGHE");
          int iCColonne          = rsC.getInt("COLONNE");
          int iCVista            = rsC.getInt("VISTA");
          
          WMap wmComp = new WMap();
          wmComp.put(sID,            iCIdPagina);
          wmComp.put(sID_TIPO_PAG,   iCIdTipoPagina);
          wmComp.put(sCODICE,        sCCodice);
          wmComp.put(sID_CATEGORIA,  iCIdCategoria);
          wmComp.put(sID_SOTTOCATEG, iCIdSottoCategoria);
          wmComp.put(sID_TIPO_ART,   iCIdTipoArticolo);
          wmComp.put(sRIGHE,         iCRighe);
          wmComp.put(sCOLONNE,       iCColonne);
          wmComp.put(sVISTA,         iCVista);
          
          String sCDescrizione0 = null;
          String sCDescrizione  = null;
          pstmL.setInt(1, iCIdPagina);
          rsL = pstmL.executeQuery();
          while(rsL.next()) {
            int iPIdLingua = rsL.getInt("ID_LINGUA");
            String sPDesc  = rsL.getString("DESCRIZIONE");
            if(iPIdLingua == 0) sCDescrizione0 = sPDesc;
            if(iPIdLingua == iIdLingua) {
              sCDescrizione = sPDesc;
              break;
            }
          }
          rsL.close();
          if(sCDescrizione != null && sCDescrizione.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sCDescrizione);
          }
          else if(sCDescrizione0 != null && sCDescrizione0.length() > 0) {
            wmComp.put(sDESCRIZIONE,  sCDescrizione0);
          }
          
          listComponenti.add(wmComp.toMapObject());
        }
        rsC.close();
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getPagine(" + iIdTipoPagina + "," + iIdLingua + ")", ex);
      throw ex;
    }
    finally {
      if(rsP   != null) try{ rsP.close();   } catch(Exception ex) {}
      if(rsA   != null) try{ rsA.close();   } catch(Exception ex) {}
      if(rsL   != null) try{ rsL.close();   } catch(Exception ex) {}
      if(rsC   != null) try{ rsC.close();   } catch(Exception ex) {}
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstmL != null) try{ pstmL.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return result;
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
    qb.put("ID_PAGINA", sID);
    qb.put("CODICE",    sCODICE + "%");
    String sAddClause = "ATTIVO = '" + QueryBuilder.decodeBoolean(true) + "'";
    String sSQL = qb.select("CMS_PAGINE", mapFilter, sAddClause);
    sSQL += " ORDER BY CODICE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int    iId     = rs.getInt("ID_PAGINA");
        String sCodice = rs.getString("CODICE");
        
        List<Object> record = new ArrayList<Object>(3);
        record.add(iId);
        record.add(sCodice);
        record.add(sCodice);
        
        result.add(record);
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.lookup(" + mapFilter + ")", ex);
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
    qb.put("P.ID_PAGINA",      sID);
    qb.put("P.ID_TIPO_PAGINA", sID_TIPO_PAG);
    qb.put("T.DESCRIZIONE",    sDESC_TIPO_PAG);
    qb.put("P.CODICE",         sCODICE + "%");
    qb.put("P.ORDINE",         sORDINE);
    qb.put("P.ATTIVO",         sATTIVO);
    String sAddClause = "P.ID_TIPO_PAGINA=T.ID_TIPO_PAGINA AND ID_PAGINA<>0";
    String sSQL = qb.select("CMS_PAGINE P,CMS_TIPI_PAGINA T", mapFilter, sAddClause);
    sSQL += " ORDER BY P.ID_TIPO_PAGINA,P.ORDINE,P.CODICE";
    
    Connection conn = null;
    Statement stm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      stm  = conn.createStatement();
      rs   = stm.executeQuery(sSQL);
      while(rs.next()) {
        int iId             = rs.getInt("ID_PAGINA");
        int iIdTipoPagina   = rs.getInt("ID_TIPO_PAGINA");
        String sDescTipoPag = rs.getString("DESCRIZIONE");
        String sCodice      = rs.getString("CODICE");
        String sAttivo      = rs.getString("ATTIVO");
        
        WMap record = new WMap();
        record.put(sID,            iId);
        record.put(sID_TIPO_PAG,   iIdTipoPagina);
        record.put(sDESC_TIPO_PAG, sDescTipoPag);
        record.put(sCODICE,        sCodice);
        record.putBoolean(sATTIVO, sAttivo);
        
        result.add(record.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.find(" + mapFilter + ")", ex);
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
  Map<String, Object> getMappa()
      throws Exception
  {
    Map<String, Object> result = new HashMap<String, Object>();
    
    String sSQL_P = "SELECT P.ID_TIPO_PAGINA,T.DESCRIZIONE,P.ID_PAGINA,P.CODICE,P.ORDINE,P.ATTIVO ";
    sSQL_P += "FROM CMS_PAGINE P,CMS_TIPI_PAGINA T ";
    sSQL_P += "WHERE P.ID_TIPO_PAGINA=T.ID_TIPO_PAGINA AND P.ID_PAGINA > 0 AND P.ATTIVO=? ";
    sSQL_P += "ORDER BY P.ID_TIPO_PAGINA,P.ORDINE,P.CODICE";
    
    String sSQL_C = "SELECT ID_PAGINA FROM CMS_PAGINE_COMP WHERE ID_PAGINA_COMP=?";
    
    String sSQL_F = "SELECT P.ID_PAGINA,P.CODICE,P.ATTIVO,C.ORDINE ";
    sSQL_F += "FROM CMS_PAGINE_COMP C,CMS_PAGINE P ";
    sSQL_F += "WHERE C.ID_PAGINA_COMP=P.ID_PAGINA AND C.ID_PAGINA=? AND P.ATTIVO=? ";
    sSQL_F += "ORDER BY C.ORDINE";
    
    String sSQL_A = "SELECT COUNT(*) FROM CMS_PAGINE_ARTICOLI WHERE ID_PAGINA=?";
    
    Object o0Elements = new ArrayList<Map<String, Object>>(0);
    
    Connection conn = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmC = null;
    PreparedStatement pstmF = null;
    PreparedStatement pstmA = null;
    ResultSet rsP = null;
    ResultSet rsC = null;
    ResultSet rsA = null;
    try {
      conn  = ConnectionManager.getDefaultConnection();
      
      pstmP = conn.prepareStatement(sSQL_P);
      pstmC = conn.prepareStatement(sSQL_C);
      pstmF = conn.prepareStatement(sSQL_F);
      pstmA = conn.prepareStatement(sSQL_A);
      
      pstmP.setString(1, QueryBuilder.decodeBoolean(true));
      rsP   = pstmP.executeQuery();
      while(rsP.next()) {
        int iIdTipoPagina   = rsP.getInt("ID_TIPO_PAGINA");
        String sDescTipoPag = rsP.getString("DESCRIZIONE");
        int iId             = rsP.getInt("ID_PAGINA");
        String sCodice      = rsP.getString("CODICE");
        int iOrdine         = rsP.getInt("ORDINE");
        
        // Se la pagina e' una componente di un'altra pagina
        // non viene restituita come foglia della tipologia
        boolean boIsAComponent = false;
        pstmC.setInt(1, iId);
        rsC = pstmC.executeQuery();
        boIsAComponent = rsC.next();
        rsC.close();
        if(boIsAComponent) continue;
        
        int iCountArt = 0;
        pstmA.setInt(1, iId);
        rsA = pstmA.executeQuery();
        if(rsA.next()) iCountArt = rsA.getInt(1);
        rsA.close();
        
        String sTipoPagina  = WUtil.lpad(String.valueOf(iIdTipoPagina), '0', 3) + ") " + sDescTipoPag;
        Map<String, Object> mapPagine = WUtil.toMapObject(result.get(sTipoPagina));
        if(mapPagine == null) {
          mapPagine = new HashMap<String, Object>();
          result.put(sTipoPagina, mapPagine);
        }
        
        String sOrd  = WUtil.lpad(String.valueOf(iOrdine), '0', 3);
        String sItem = sOrd + ")   [" + iId + "]   " + sCodice;
        if(iCountArt > 0) sItem += " (" + iCountArt + " articoli)";
        
        Object oFoglie = getFoglie(pstmF, pstmA, iId, o0Elements);
        if(oFoglie != null) {
          mapPagine.put(sItem, oFoglie);
        }
        else {
          mapPagine.put(sItem, o0Elements);
        }
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getMappa()", ex);
      throw ex;
    }
    finally {
      if(rsP   != null) try{ rsP.close();   } catch(Exception ex) {}
      if(rsC   != null) try{ rsC.close();   } catch(Exception ex) {}
      if(rsA   != null) try{ rsA.close();   } catch(Exception ex) {}
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(pstmF != null) try{ pstmF.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return result;
  }
  
  protected static
  Object getFoglie(PreparedStatement pstmF, PreparedStatement pstmA, int iIdPagina, Object o0Elements)
      throws Exception
  {
    ResultSet rsF = null;
    ResultSet rsA = null;
    try {
      List<Integer> vId    = null;
      List<String>  vItems = null;
      pstmF.setInt(1,    iIdPagina);
      pstmF.setString(2, QueryBuilder.decodeBoolean(true));
      rsF = pstmF.executeQuery();
      while(rsF.next()) {
        if(vId == null) {
          vId    = new ArrayList<Integer>();
          vItems = new ArrayList<String>();
        }
        int iId        = rsF.getInt("ID_PAGINA");
        String sCodice = rsF.getString("CODICE");
        int iOrdine    = rsF.getInt("ORDINE");
        
        int iCountArt = 0;
        pstmA.setInt(1, iId);
        rsA = pstmA.executeQuery();
        if(rsA.next()) iCountArt = rsA.getInt(1);
        rsA.close();
        
        String sOrd  = WUtil.lpad(String.valueOf(iOrdine), '0', 3);
        String sItem = sOrd + ")   [" + iId + "]   " + sCodice;
        if(iCountArt > 0) sItem += " (" + iCountArt + " articoli)";
        
        vId.add(new Integer(iId));
        vItems.add(sItem);
      }
      rsF.close();
      if(vId == null) return null;
      
      Map<String, Object> htFoglie = new HashMap<String, Object>();
      boolean boAltreFoglie = false;
      for(int i = 0; i < vItems.size(); i++) {
        Integer oId = (Integer) vId.get(i);
        Object oFoglie = getFoglie(pstmF, pstmA, oId.intValue(), o0Elements);
        if(oFoglie != null) {
          boAltreFoglie = true;
          htFoglie.put(vItems.get(i), oFoglie);
        }
        else {
          htFoglie.put(vItems.get(i), o0Elements);
        }
      }
      if(boAltreFoglie) {
        return htFoglie;
      }
      else {
        return vItems;
      }
    }
    finally {
      if(rsF != null) try{ rsF.close(); } catch(Exception ex) {}
      if(rsA != null) try{ rsA.close(); } catch(Exception ex) {}
    }
  }
  
  public
  Map<String, Object> read(int iId)
      throws Exception
  {
    WMap result = new WMap();
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("P.ID_TIPO_PAGINA");
    qb.add("T.DESCRIZIONE");
    qb.add("P.CODICE");
    qb.add("P.ID_CATEGORIA");
    qb.add("P.ID_SOTTOCATEGORIA");
    qb.add("P.ID_TIPO_ARTICOLO");
    qb.add("P.ORDINE");
    qb.add("P.RIGHE");
    qb.add("P.COLONNE");
    qb.add("P.VISTA");
    qb.add("P.ATTIVO");
    String sSQL = qb.select("CMS_PAGINE P,CMS_TIPI_PAGINA T");
    sSQL += "WHERE P.ID_TIPO_PAGINA=T.ID_TIPO_PAGINA AND P.ID_PAGINA=?";
    
    qb.init();
    qb.add("PA.ID_ARTICOLO");
    qb.add("AR.DESCRIZIONE");
    qb.add("AR.DATA_ARTICOLO");
    qb.add("PA.ORDINE");
    String sSQL_ART = qb.select("CMS_PAGINE_ARTICOLI PA,CMS_ARTICOLI AR");
    sSQL_ART += "WHERE PA.ID_ARTICOLO=AR.ID_ARTICOLO ";
    sSQL_ART += "AND PA.ID_PAGINA=? ";
    sSQL_ART += "ORDER BY PA.ORDINE";
    
    qb.init();
    qb.add("C.ID_PAGINA_COMP");
    qb.add("P.ID_TIPO_PAGINA");
    qb.add("T.DESCRIZIONE");
    qb.add("P.CODICE");
    qb.add("P.ID_CATEGORIA");
    qb.add("P.ID_SOTTOCATEGORIA");
    qb.add("P.ID_TIPO_ARTICOLO");
    qb.add("P.ORDINE");
    qb.add("P.RIGHE");
    qb.add("P.COLONNE");
    qb.add("P.VISTA");
    qb.add("P.ATTIVO");
    String sSQL_COMP = qb.select("CMS_PAGINE_COMP C,CMS_PAGINE P,CMS_TIPI_PAGINA T");
    sSQL_COMP += "WHERE C.ID_PAGINA_COMP=P.ID_PAGINA ";
    sSQL_COMP += "AND P.ID_TIPO_PAGINA=T.ID_TIPO_PAGINA ";
    sSQL_COMP += "AND C.ID_PAGINA=? ";
    sSQL_COMP += "ORDER BY C.ORDINE";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement(sSQL);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      if(rs.next()) {
        int iIdTipoPagina     = rs.getInt("ID_TIPO_PAGINA"); 
        String sDescTipoPag   = rs.getString("DESCRIZIONE");
        String sCodice        = rs.getString("CODICE");
        int iIdCategoria      = rs.getInt("ID_CATEGORIA"); 
        int iIdSottoCategoria = rs.getInt("ID_SOTTOCATEGORIA");
        int iIdTipoArticolo   = rs.getInt("ID_TIPO_ARTICOLO");
        int iOrdine           = rs.getInt("ORDINE");
        int iRighe            = rs.getInt("RIGHE");
        int iColonne          = rs.getInt("COLONNE");
        int iVista            = rs.getInt("VISTA");
        String sAttivo        = rs.getString("ATTIVO");
        
        result.put(sID,                   iId);
        result.put(sID_TIPO_PAG,          iIdTipoPagina);
        result.put(sDESC_TIPO_PAG,        sDescTipoPag);
        result.put(sCODICE,               sCodice);
        result.putNotZero(sID_CATEGORIA,  iIdCategoria);
        result.putNotZero(sID_SOTTOCATEG, iIdSottoCategoria);
        result.putNotZero(sID_TIPO_ART,   iIdTipoArticolo);
        result.put(sORDINE,               iOrdine);
        result.put(sRIGHE,                iRighe);
        result.put(sCOLONNE,              iColonne);
        result.put(sVISTA,                iVista);
        result.putBoolean(sATTIVO,        sAttivo);
      }
      rs.close();
      pstm.close();
      
      Map<String, Object> mapDescrizione = new HashMap<String, Object>();
      result.put(sDESCRIZIONE, mapDescrizione);
      
      pstm = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_PAGINE_DESC WHERE ID_PAGINA=? ORDER BY ID_LINGUA");
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        String sIdLingua    = rs.getString("ID_LINGUA");
        String sDescrizione = rs.getString("DESCRIZIONE");
        if(sIdLingua == null || sDescrizione == null) continue;
        mapDescrizione.put(sIdLingua, sDescrizione);
      }
      rs.close();
      pstm.close();
      
      List<Map<String, Object>> listArticoli = new ArrayList<Map<String, Object>>();
      result.put(sARTICOLI, listArticoli);
      
      pstm = conn.prepareStatement(sSQL_ART);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdArticolo      = rs.getInt("ID_ARTICOLO"); 
        String sDescArticolo = rs.getString("DESCRIZIONE");
        Date dDataArticolo   = rs.getDate("DATA_ARTICOLO");
        
        WMap dmArticolo = new WMap();
        dmArticolo.put(IArticolo.sID,                iIdArticolo);
        dmArticolo.put(IArticolo.sDESCRIZIONE,       sDescArticolo);
        dmArticolo.putDate(IArticolo.sDATA_ARTICOLO, dDataArticolo);
        
        listArticoli.add(dmArticolo.toMapObject());
      }
      rs.close();
      pstm.close();
      
      List<Map<String, Object>> listComponenti = new ArrayList<Map<String, Object>>();
      result.put(sCOMPONENTI, listComponenti);
      
      pstm = conn.prepareStatement(sSQL_COMP);
      pstm.setInt(1, iId);
      rs = pstm.executeQuery();
      while(rs.next()) {
        int iIdPagina         = rs.getInt("ID_PAGINA_COMP");
        int iIdTipoPagina     = rs.getInt("ID_TIPO_PAGINA"); 
        String sDescTipoPag   = rs.getString("DESCRIZIONE");
        String sCodice        = rs.getString("CODICE");
        int iIdCategoria      = rs.getInt("ID_CATEGORIA"); 
        int iIdSottoCategoria = rs.getInt("ID_SOTTOCATEGORIA");
        int iIdTipoArticolo   = rs.getInt("ID_TIPO_ARTICOLO");
        int iOrdine           = rs.getInt("ORDINE");
        int iRighe            = rs.getInt("RIGHE");
        int iColonne          = rs.getInt("COLONNE");
        int iVista            = rs.getInt("VISTA");
        String sAttivo        = rs.getString("ATTIVO");
        
        WMap wmComp = new WMap();
        wmComp.put(sID,            iIdPagina);
        wmComp.put(sID_TIPO_PAG,   iIdTipoPagina);
        wmComp.put(sDESC_TIPO_PAG, sDescTipoPag);
        wmComp.put(sCODICE,        sCodice);
        wmComp.put(sID_CATEGORIA,  iIdCategoria);
        wmComp.put(sID_SOTTOCATEG, iIdSottoCategoria);
        wmComp.put(sID_TIPO_ART,   iIdTipoArticolo);
        wmComp.put(sORDINE,        iOrdine);
        wmComp.put(sRIGHE,         iRighe);
        wmComp.put(sCOLONNE,       iColonne);
        wmComp.put(sVISTA,         iVista);
        wmComp.putBoolean(sATTIVO, sAttivo);
        
        listComponenti.add(wmComp.toMapObject());
      }
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.read(" + iId + ")", ex);
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
      pstm = conn.prepareStatement("SELECT ID_PAGINA FROM CMS_PAGINE WHERE CODICE=?");
      pstm.setString(1, sCodice);
      rs = pstm.executeQuery();
      return rs.next();
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.exists(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();   } catch(Exception ex) {}
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
  }
  
  public static
  String getDescription(int iId, int iIdLingua)
      throws Exception
  {
    String sResult = null;
    String sDescrizione0 = null;
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      pstm = conn.prepareStatement("SELECT ID_LINGUA,DESCRIZIONE FROM CMS_PAGINE_DESC WHERE ID_PAGINA=? ORDER BY ID_LINGUA");
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
      oLogger.error("Exception in WSPagine.getDescription(" + iId + "," + iIdLingua + ")", ex);
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
    qb.add("ID_PAGINA");
    qb.add("ID_TIPO_PAGINA");
    qb.add("CODICE");
    qb.add("ID_CATEGORIA");
    qb.add("ID_SOTTOCATEGORIA");
    qb.add("ID_TIPO_ARTICOLO");
    qb.add("ORDINE");
    qb.add("RIGHE");
    qb.add("COLONNE");
    qb.add("VISTA");
    qb.add("ATTIVO");
    String sSQL = qb.insert("CMS_PAGINE", true);
    
    qb.init();
    qb.add("ID_PAGINA");
    qb.add("ID_ARTICOLO");
    qb.add("ORDINE");
    String sSQL_ART = qb.insert("CMS_PAGINE_ARTICOLI", true);
    
    qb.init();
    qb.add("ID_PAGINA");
    qb.add("ID_PAGINA_COMP");
    qb.add("ORDINE");
    String sSQL_COMP = qb.insert("CMS_PAGINE_COMP", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmA = null;
    PreparedStatement pstmC = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      int iId = ConnectionManager.nextVal(conn, "SEQ_CMS_PAGINE");
      
      pstmP = conn.prepareStatement(sSQL);
      
      WMap wmValues = new WMap(mapValues);
      int iIdTipoPagina      = wmValues.getInt(sID_TIPO_PAG);
      String sCodice         = wmValues.getString(sCODICE);
      int iIdCategoria       = wmValues.getInt(sID_CATEGORIA);
      int iIdSottoCategoria  = wmValues.getInt(sID_SOTTOCATEG);
      int iIdTipoArticolo    = wmValues.getInt(sID_TIPO_ART);
      int iOrdine            = wmValues.getInt(sORDINE);
      int iRighe             = wmValues.getInt(sRIGHE);
      int iColonne           = wmValues.getInt(sCOLONNE);
      int iVista             = wmValues.getInt(sVISTA);
      List<?> listArticoli   = wmValues.getList(sARTICOLI);
      List<?> listComponenti = wmValues.getList(sCOMPONENTI);
      
      int p = 0;
      pstmP.setInt(++p,    iId);
      pstmP.setInt(++p,    iIdTipoPagina);
      pstmP.setString(++p, sCodice);
      pstmP.setInt(++p,    iIdCategoria);
      pstmP.setInt(++p,    iIdSottoCategoria);
      pstmP.setInt(++p,    iIdTipoArticolo);
      pstmP.setInt(++p,    iOrdine);
      pstmP.setInt(++p,    iRighe);
      pstmP.setInt(++p,    iColonne);
      pstmP.setInt(++p,    iVista);
      pstmP.setString(++p, QueryBuilder.decodeBoolean(true));
      pstmP.executeUpdate();
      
      insertDescrizioni(conn, iId, wmValues.getMapObject(sDESCRIZIONE));
      
      if(listArticoli != null && listArticoli.size() > 0) {
        pstmA = conn.prepareStatement(sSQL_ART);
        for(int i = 0; i < listArticoli.size(); i++) {
          int iIdArticolo = getIdArticolo(listArticoli.get(i));
          if(iIdArticolo == 0) continue;
          
          pstmA.setInt(1, iId);
          pstmA.setInt(2, iIdArticolo);
          pstmA.setInt(3, i+1);
          pstmA.executeUpdate();
        }
      }
      
      if(listComponenti != null && listComponenti.size() > 0) {
        pstmC = conn.prepareStatement(sSQL_COMP);
        for(int i = 0; i < listComponenti.size(); i++) {
          int iIdPagina = getIdPagina(listComponenti.get(i));
          if(iIdPagina == 0) continue;
          
          pstmC.setInt(1, iId);
          pstmC.setInt(2, iIdPagina);
          pstmC.setInt(3, i+1);
          pstmC.executeUpdate();
        }
      }
      
      ut.commit();
      
      CMSCache.mapDescPag.clear();
      CMSCache.home = null;
      CMSCache.view = null;
      
      mapValues.put(sID,            iId);
      mapValues.put(sDESC_TIPO_PAG, getDescTipoPagina(conn, iIdTipoPagina));
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSPagine.insert(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public
  Map<String, Object> update(Map<String, Object> mapValues)
      throws Exception
  {
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_TIPO_PAGINA");
    qb.add("CODICE");
    qb.add("ID_CATEGORIA");
    qb.add("ID_SOTTOCATEGORIA");
    qb.add("ID_TIPO_ARTICOLO");
    qb.add("ORDINE");
    qb.add("RIGHE");
    qb.add("COLONNE");
    qb.add("VISTA");
    String sSQL = qb.update("CMS_PAGINE", true);
    sSQL += "WHERE ID_PAGINA=?";
    
    qb.init();
    qb.add("ID_PAGINA");
    qb.add("ID_ARTICOLO");
    qb.add("ORDINE");
    String sSQL_ART = qb.insert("CMS_PAGINE_ARTICOLI", true);
    
    qb.init();
    qb.add("ID_PAGINA");
    qb.add("ID_PAGINA_COMP");
    qb.add("ORDINE");
    String sSQL_COMP = qb.insert("CMS_PAGINE_COMP", true);
    
    Connection conn = null;
    UserTransaction ut = null;
    PreparedStatement pstmP = null;
    PreparedStatement pstmA = null;
    PreparedStatement pstmC = null;
    try {
      conn = ConnectionManager.getDefaultConnection();
      
      ut = ConnectionManager.getUserTransaction(conn);
      ut.begin();
      
      pstmP = conn.prepareStatement(sSQL);
      
      WMap wmValues = new WMap(mapValues);
      int iId                = wmValues.getInt(sID);
      int iIdTipoPagina      = wmValues.getInt(sID_TIPO_PAG);
      String sCodice         = wmValues.getString(sCODICE);
      int iIdCategoria       = wmValues.getInt(sID_CATEGORIA);
      int iIdSottoCategoria  = wmValues.getInt(sID_SOTTOCATEG);
      int iIdTipoArticolo    = wmValues.getInt(sID_TIPO_ART);
      int iOrdine            = wmValues.getInt(sORDINE);
      int iRighe             = wmValues.getInt(sRIGHE);
      int iColonne           = wmValues.getInt(sCOLONNE);
      int iVista             = wmValues.getInt(sVISTA);
      List<?> listArticoli   = wmValues.getList(sARTICOLI);
      List<?> listComponenti = wmValues.getList(sCOMPONENTI);
      
      // SET
      int p = 0;
      pstmP.setInt(++p,    iIdTipoPagina);
      pstmP.setString(++p, sCodice);
      pstmP.setInt(++p,    iIdCategoria);
      pstmP.setInt(++p,    iIdSottoCategoria);
      pstmP.setInt(++p,    iIdTipoArticolo);
      pstmP.setInt(++p,    iOrdine);
      pstmP.setInt(++p,    iRighe);
      pstmP.setInt(++p,    iColonne);
      pstmP.setInt(++p,    iVista);
      // WHERE
      pstmP.setInt(++p,    iId);
      pstmP.executeUpdate();
      
      deleteDescrizioni(conn, iId);
      insertDescrizioni(conn, iId, wmValues.getMapObject(sDESCRIZIONE));
      
      deleteArticoli(conn, iId);
      if(listArticoli != null && listArticoli.size() > 0) {
        pstmA = conn.prepareStatement(sSQL_ART);
        for(int i = 0; i < listArticoli.size(); i++) {
          int iIdArticolo = getIdArticolo(listArticoli.get(i));
          if(iIdArticolo == 0) continue;
          
          pstmA.setInt(1, iId);
          pstmA.setInt(2, iIdArticolo);
          pstmA.setInt(3, i+1);
          pstmA.executeUpdate();
        }
      }
      
      deleteComponenti(conn, iId);
      if(listComponenti != null && listComponenti.size() > 0) {
        pstmC = conn.prepareStatement(sSQL_COMP);
        for(int i = 0; i < listComponenti.size(); i++) {
          int iIdPagina = getIdPagina(listComponenti.get(i));
          if(iIdPagina == 0) continue;
          
          pstmC.setInt(1, iId);
          pstmC.setInt(2, iIdPagina);
          pstmC.setInt(3, i+1);
          pstmC.executeUpdate();
        }
      }
      
      ut.commit();
      
      CMSCache.mapDescPag.clear();
      CMSCache.home = null;
      CMSCache.view = null;
      
      mapValues.put(sDESC_TIPO_PAG, getDescTipoPagina(conn, iIdTipoPagina));
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSPagine.update(" + mapValues + ")", ex);
      throw ex;
    }
    finally {
      if(pstmP != null) try{ pstmP.close(); } catch(Exception ex) {}
      if(pstmA != null) try{ pstmA.close(); } catch(Exception ex) {}
      if(pstmC != null) try{ pstmC.close(); } catch(Exception ex) {}
      if(conn  != null) ConnectionManager.closeConnection(conn);
    }
    return mapValues;
  }
  
  public
  void insertDescrizioni(Connection conn, int iId, Map<String, Object> mapValues)
      throws Exception
  {
    if(mapValues == null || mapValues.isEmpty()) return;
    
    QueryBuilder qb = new QueryBuilder();
    qb.add("ID_PAGINA");
    qb.add("ID_LINGUA");
    qb.add("DESCRIZIONE");
    String sSQL = qb.insert("CMS_PAGINE_DESC", true);
    
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
  void deleteArticoli(Connection conn, int iId)
      throws Exception
  {
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement("DELETE FROM CMS_PAGINE_ARTICOLI WHERE ID_PAGINA=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return;
  }
  
  public
  void deleteComponenti(Connection conn, int iId)
      throws Exception
  {
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement("DELETE FROM CMS_PAGINE_COMP WHERE ID_PAGINA=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
    }
    return;
  }
  
  public
  void removeFromComponenti(Connection conn, int iId)
      throws Exception
  {
    PreparedStatement pstm = null;
    try {
      pstm = conn.prepareStatement("DELETE FROM CMS_PAGINE_COMP WHERE ID_PAGINA_COMP=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
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
      pstm = conn.prepareStatement("DELETE FROM CMS_PAGINE_DESC WHERE ID_PAGINA=?");
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
      
      pstm = conn.prepareStatement("UPDATE CMS_PAGINE SET ATTIVO=? WHERE ID_PAGINA=?");
      // SET
      pstm.setString(1, QueryBuilder.decodeBoolean(boEnabled));
      // WHERE
      pstm.setInt(2, iId);
      
      pstm.executeUpdate();
      
      ut.commit();
      
      CMSCache.home = null;
      CMSCache.view = null;
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSPagine.setEnabled(" + iId + "," + boEnabled + ")", ex);
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
      
      deleteDescrizioni(conn,    iId);
      deleteArticoli(conn,       iId);
      deleteComponenti(conn,     iId);
      removeFromComponenti(conn, iId);
      
      pstm = conn.prepareStatement("DELETE FROM CMS_PAGINE WHERE ID_PAGINA=?");
      pstm.setInt(1, iId);
      pstm.executeUpdate();
      
      ut.commit();
      
      CMSCache.mapDescPag.clear();
      CMSCache.home = null;
      CMSCache.view = null;
    }
    catch (Exception ex) {
      if(ut != null) try{ ut.rollback(); } catch(Exception exut) {}
      oLogger.error("Exception in WSPagine.delete(" + iId + ")", ex);
      return false;
    }
    finally {
      if(pstm != null) try{ pstm.close(); } catch(Exception ex) {}
      if(conn != null) ConnectionManager.closeConnection(conn);
    }
    return true;
  }
  
  public static
  String getDescTipoPagina(Connection conn, int iIdTipoPagina)
      throws Exception
  {
    String result = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      pstm = conn.prepareStatement("SELECT DESCRIZIONE FROM CMS_TIPI_PAGINA WHERE ID_TIPO_PAGINA=?");
      pstm.setInt(1, iIdTipoPagina);
      rs = pstm.executeQuery();
      if(rs.next()) result = rs.getString("DESCRIZIONE");
    }
    catch (Exception ex) {
      oLogger.error("Exception in WSPagine.getDescTipoPagina(conn," + iIdTipoPagina + ")", ex);
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
  int getIdArticolo(Object object)
  {
    if(object == null) return 0;
    if(object instanceof Number) {
      return ((Number) object).intValue();
    }
    else if(object instanceof Map) {
      Map<String, Object> map = WUtil.toMapObject(object);
      return WUtil.toInt(map.get(IArticolo.sID), 0);
      
    }
    else if(object instanceof List) {
      return WUtil.toInt(WUtil.getFirst(object), 0);
    }
    return WUtil.toInt(object, 0);
  }
  
  public static
  int getIdPagina(Object object)
  {
    if(object == null) return 0;
    if(object instanceof Number) {
      return ((Number) object).intValue();
    }
    else if(object instanceof Map) {
      Map<String, Object> map = WUtil.toMapObject(object);
      return WUtil.toInt(map.get(IPagina.sID), 0);
      
    }
    else if(object instanceof List) {
      return WUtil.toInt(WUtil.getFirst(object), 0);
    }
    return WUtil.toInt(object, 0);
  }
}