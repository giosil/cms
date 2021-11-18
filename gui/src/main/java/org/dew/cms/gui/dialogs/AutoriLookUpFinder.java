package org.dew.cms.gui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.components.ILookUpFinder;
import org.dew.swingup.rpc.IRPCClient;

import org.dew.util.WUtil;

import org.dew.cms.common.IAutore;

public 
class AutoriLookUpFinder implements ILookUpFinder, IAutore
{
  protected boolean boSkipSearchWithLooseFilter = false;
  
  public AutoriLookUpFinder(boolean boSkipSearchWithLooseFilter)
  {
    this.boSkipSearchWithLooseFilter = boSkipSearchWithLooseFilter;
  }
  
  public
  List<List<Object>> find(String sEntity, List<Object> oFilter)
      throws Exception
  {
    Map<String, Object> mapFilter = new HashMap<String, Object>();
    
    boolean boLooseFilter = false;
    boolean boTestFilter = false;
    String sFilter = (String) oFilter.get(0);
    if(sFilter != null) {
      if(isEmail(sFilter)) {
        mapFilter.put(sEMAIL, sFilter);
      }
      else {
        String sCognome = getCognome(sFilter);
        if(sCognome != null) {
          mapFilter.put(sCOGNOME, sCognome);
        }
        else {
          boLooseFilter = true;
        }
        String sNome = getNome(sFilter);
        if(sNome != null) {
          mapFilter.put(sNOME, sNome);
        }
        else {
          boLooseFilter = true;
        }
      }
      boTestFilter = sFilter.length() > 0 && sFilter.charAt(0) == '\\'; 
    }
    
    if(boSkipSearchWithLooseFilter && boLooseFilter && !boTestFilter) {
      // Se il lookup restituisce pi� di un record, viene ripetuta
      // la ricerca nella dialog EntityLookUpDialog.
      // A fronte di un filtro meno stringente dal quale ci si aspetta
      // sicuramente pi� record si restituisce una lista vuota.
      // In questo modo parte soltanto la ricerca in EntityLookUpDialog.
      // ATTENZIONE: valido soltanto se si usa EntityLookUpDialog.
      return new ArrayList<List<Object>>();
    }
    
    IRPCClient rpcClient = ResourcesMgr.getDefaultRPCClient();
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(mapFilter);
    
    Object result = rpcClient.execute("AUTORI.lookup", parameters);
    
    return WUtil.toListOfListObject(result);
  }
  
  public static
  String getCognome(String sCognomeNome)
  {
    if(sCognomeNome == null) return null;
    int iSep = sCognomeNome.indexOf("-");
    if(iSep > 0) {
      return sCognomeNome.substring(0, iSep);
    }
    int iFirstSep = sCognomeNome.indexOf(' ');
    int iNextSep  = sCognomeNome.indexOf(' ', iFirstSep + 1);
    if(iFirstSep > 0 && iNextSep < 0 && iFirstSep < 4) {
      // C'e' uno spazio soltanto tuttavia e' ravvicinato: probabile cognome (ad es. DE ROSSI)
      return sCognomeNome;
    }
    else if(iFirstSep > 0 && iNextSep < 0 && iFirstSep >= 4) {
      // C'e' uno spazio soltanto ed e' lontano
      return sCognomeNome.substring(0, iFirstSep);
    }
    else if(iFirstSep > 0 && iNextSep > 0 && iFirstSep < 4) {
      // Ci sono due spazi tuttavia il primo e' ravvicinato: probabile cognome (ad es. DE ROSSI)
      return sCognomeNome.substring(0, iNextSep);
    }
    else if(iFirstSep > 0 && iNextSep > 0 && iFirstSep >= 4) {
      // Ci sono due spazi tuttavia il primo e' lontano.
      return sCognomeNome.substring(0, iFirstSep);
    }
    return sCognomeNome;
  }
  
  public static
  String getNome(String sCognomeNome)
  {
    if(sCognomeNome == null) return null;
    int iSep = sCognomeNome.indexOf("-");
    if(iSep > 0) {
      return sCognomeNome.substring(iSep + 1).trim();
    }
    int iFirstSep = sCognomeNome.indexOf(' ');
    int iNextSep  = sCognomeNome.indexOf(' ', iFirstSep + 1);
    if(iFirstSep > 0 && iNextSep < 0 && iFirstSep < 4) {
      // C'e' uno spazio soltanto tuttavia e' ravvicinato: probabile cognome (ad es. DE ROSSI)
      return null;
    }
    else if(iFirstSep > 0 && iNextSep < 0 && iFirstSep >= 4) {
      // C'e' uno spazio soltanto ed e' lontano
      return sCognomeNome.substring(iFirstSep).trim();
    }
    else if(iFirstSep > 0 && iNextSep > 0 && iFirstSep < 4) {
      // Ci sono due spazi tutta via il primo � ravvicinato: probabile cognome (ad es. DE ROSSI)
      return sCognomeNome.substring(iNextSep).trim();
    }
    else if(iFirstSep > 0 && iNextSep > 0 && iFirstSep >= 4) {
      // Ci sono due spazi tutta via il primo � lontano.
      return sCognomeNome.substring(iFirstSep).trim();
    }
    return null;
  }
  
  public static
  String getCognomeNome(String sCognome, String sNome)
  {
    if(sNome == null) return sCognome;
    return sCognome + "-" + sNome;
  }
  
  public static
  boolean isEmail(String sText)
  {
    if(sText == null || sText.length() < 4) return false;
    if(sText.indexOf('@') < 0) return false;
    return true;
  }
}
