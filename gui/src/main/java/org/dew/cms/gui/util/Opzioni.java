package org.dew.cms.gui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.CodeAndDescription;

import org.dew.util.WUtil;

import org.dew.cms.common.IOpzioni;

public 
class Opzioni implements IOpzioni
{
  private static Vector<CodeAndDescription> vSesso;
  private static Vector<CodeAndDescription> vLingue;
  private static Vector<CodeAndDescription> vTipiAutore;
  private static Vector<CodeAndDescription> vRuoli;
  private static Vector<CodeAndDescription> vTipiUtente;
  private static Vector<CodeAndDescription> vIstituti;
  private static Vector<CodeAndDescription> vLuoghi;
  private static Vector<CodeAndDescription> vCategorie;
  private static Vector<CodeAndDescription> vTipologie;
  private static Vector<CodeAndDescription> vTipiLuogo;
  private static Vector<CodeAndDescription> vTipiPagina;
  private static Vector<CodeAndDescription> vViste;
  private static Map<String, Object> mapSottoCategorie;
  private static Map<Object, String> mapLingue;
  private static String sImportFolder;
  
  public static
  void load(String sExclude)
  {
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("OPZIONI.read", Collections.EMPTY_LIST, true));
      if(mapResult == null || mapResult.isEmpty()) return;
      vLingue     = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sLINGUE));
      vTipiAutore = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sTIPI_AUTORE));
      vTipiUtente = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sTIPI_UTENTE));
      vRuoli      = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sRUOLI));
      vIstituti   = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sISTITUTI));
      vCategorie  = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sCATEGORIE), null, " (Nessuna) ");
      vTipologie  = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sTIPOLOGIE), null, " (Nessuna) ");
      mapSottoCategorie = WUtil.toMapObject(mapResult.get(IOpzioni.sSOTTO_CAT));
      mapLingue   = toMapIdDesc(vLingue);
      if(sExclude == null || sExclude.indexOf(IOpzioni.sLUOGHI) < 0) {
        vLuoghi = toVectorOfCodeAndDescription(mapResult.get(IOpzioni.sLUOGHI));
      }
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la lettura delle opzioni", ex);
    }
  }
  
  public static
  String getImportFolder()
  {
    if(sImportFolder != null && sImportFolder.length() > 0) return sImportFolder;
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      sImportFolder = (String) oRPCClient.execute("ARTICOLI.getImportFolder", Collections.EMPTY_LIST);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(sImportFolder == null || sImportFolder.length() == 0) {
      sImportFolder = "DATI/import";
    }
    return sImportFolder;
  }
  
  public static
  Vector<CodeAndDescription> getSesso()
  {
    if(vSesso != null && vSesso.size() > 0) {
      return vSesso;
    }
    vSesso = new Vector<CodeAndDescription>();
    vSesso.add(new CodeAndDescription("",  ""));
    vSesso.add(new CodeAndDescription("M", "Maschio"));
    vSesso.add(new CodeAndDescription("F", "Femmina"));
    return vSesso;
  }	
  
  public static
  Vector<CodeAndDescription> getLingue(boolean boUseCache)
  {
    if(boUseCache && vLingue != null && vLingue.size() > 0) {
      return vLingue;
    }
    vLingue   = new Vector<CodeAndDescription>();
    mapLingue = new HashMap<Object, String>();
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(new HashMap<String, Object>());
      List<List<Object>> listResult = WUtil.toListOfListObject(oRPCClient.execute("LINGUE.lookup", parameters));
      for(int i = 0; i < listResult.size(); i++) {
        List<Object> listRecord = listResult.get(i);
        Object oId   = listRecord.get(0);
        String sDesc = listRecord.get(2).toString();
        sDesc = AppUtil.denormalizeText(sDesc);
        vLingue.add(new CodeAndDescription(oId, sDesc));
        mapLingue.put(oId, sDesc);
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vLingue == null || vLingue.size() == 0) {
      vLingue.add(new CodeAndDescription(new Integer(0), "PREDEFINITA"));
    }
    return vLingue;
  }
  
  public static
  String getDescLingua(Object oId)
  {
    Integer oKey = null;
    if(oId == null) oKey = new Integer(0);
    if(oId instanceof Integer) {
      oKey = (Integer) oId;
    }
    else {
      String sId = oId.toString();
      if(sId.length() == 0) {
        oKey = new Integer(0);
      }
      else {
        int iId = 0;
        try{ iId = Integer.parseInt(sId); } catch(Exception ex) {}
        oKey = new Integer(iId);
      }
    }
    if(mapLingue == null) getLingue(false);
    String sResult = (String) mapLingue.get(oKey);
    if(sResult == null) sResult = (String) mapLingue.get(new Integer(0));
    return sResult;
  }
  
  public static
  Vector<CodeAndDescription> getTipiAutore(boolean boUseCache)
  {
    if(boUseCache && vTipiAutore != null && vTipiAutore.size() > 0) {
      return vTipiAutore;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      Object result = oRPCClient.execute("AUTORI.getTipiAutore", Collections.EMPTY_LIST);
      
      vTipiAutore = toVectorOfCodeAndDescription(result, new Integer(0), "");
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vTipiAutore == null) vTipiAutore = new Vector<CodeAndDescription>();
    return vTipiAutore;
  }
  
  public static
  Vector<CodeAndDescription> getRuoli(boolean boUseCache)
  {
    if(boUseCache && vRuoli != null && vRuoli.size() > 0) {
      return vRuoli;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      Object result = oRPCClient.execute("AUTORI.getRuoli", Collections.EMPTY_LIST);
      
      vRuoli = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vRuoli == null) vRuoli = new Vector<CodeAndDescription>();
    return vRuoli;
  }
  
  public static
  Vector<CodeAndDescription> getTipiUtente(boolean boUseCache)
  {
    if(boUseCache && vTipiUtente != null && vTipiUtente.size() > 0) {
      return vTipiUtente;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      Object result = oRPCClient.execute("UTENTI.getTipiUtenti", Collections.EMPTY_LIST);
      
      vTipiUtente = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vTipiUtente == null) vTipiUtente = new Vector<CodeAndDescription>();
    return vTipiUtente;
  }
  
  public static
  Vector<CodeAndDescription> getIstituti(boolean boUseCache)
  {
    if(boUseCache && vIstituti != null && vIstituti.size() > 0) {
      return vIstituti;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(new HashMap<String, Object>());
      Object result = oRPCClient.execute("ISTITUTI.lookup", parameters);
      
      vIstituti = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vIstituti == null) vIstituti = new Vector<CodeAndDescription>();
    return vIstituti;
  }	
  
  public static
  Vector<CodeAndDescription> getTipiLuogo(boolean boUseCache)
  {
    if(boUseCache && vTipiLuogo != null && vTipiLuogo.size() > 0) {
      return vTipiLuogo;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(0);
      Object result = oRPCClient.execute("LUOGHI.getTipiLuogo", parameters);
      
      vTipiLuogo = toVectorOfCodeAndDescription(result, new Integer(0), "");
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vTipiLuogo == null) vTipiLuogo = new Vector<CodeAndDescription>();
    return vTipiLuogo;
  }
  
  public static
  Vector<CodeAndDescription> getLuoghi(boolean boUseCache)
  {
    if(boUseCache && vLuoghi != null && vLuoghi.size() > 0) {
      return vLuoghi;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(new HashMap<String, Object>());
      Object result = oRPCClient.execute("LUOGHI.lookup", parameters);
      
      vLuoghi = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vLuoghi == null) vLuoghi = new Vector<CodeAndDescription>();
    return vLuoghi;
  }	
  
  public static
  Vector<CodeAndDescription> getCategorie(boolean boUseCache)
  {
    if(boUseCache && vCategorie != null && vCategorie.size() > 0) {
      return vCategorie;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(0);
      Object result = oRPCClient.execute("PORTALE.getCategorie", parameters);
      
      vCategorie = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vCategorie == null) vCategorie = new Vector<CodeAndDescription>();
    return vCategorie;
  }	
  
  public static
  Vector<CodeAndDescription> getSottoCategorie(Object oCategoria, boolean boUseCache)
  {
    if(!boUseCache || mapSottoCategorie == null || mapSottoCategorie.isEmpty()) {
      loadSottoCategorie();
    }
    
    Vector<CodeAndDescription> vResult  = new Vector<CodeAndDescription>();
    vResult.add(new CodeAndDescription(null, " (Nessuna) "));
    if(mapSottoCategorie == null || mapSottoCategorie.isEmpty()) return vResult;
    String sKey = null;
    if(oCategoria == null) {
      sKey = "0";
    }
    else if(oCategoria instanceof CodeAndDescription) {
      Object oCode = ((CodeAndDescription) oCategoria).getCode();
      sKey = oCode != null ? oCode.toString() : "0";
    }
    else {
      sKey = oCategoria.toString();
    }
    List<List<Object>> listSottoCategorie = WUtil.toListOfListObject(mapSottoCategorie.get(sKey));
    if(listSottoCategorie == null) return vResult;
    
    List<Integer> listOfId = new ArrayList<Integer>();
    
    Map<Integer, CodeAndDescription> mapIdItem = new HashMap<Integer, CodeAndDescription>();
    for(int i = 0; i < listSottoCategorie.size(); i++) {
      List<Object> listRecord = listSottoCategorie.get(i);
      Integer oId  = WUtil.toInteger(listRecord.get(0), 0);
      String sDesc = WUtil.toString(listRecord.get(1), "");
      listOfId.add(oId);
      mapIdItem.put(oId, new CodeAndDescription(oId, sDesc));
    }
    
    Collections.sort(listOfId);
    for(int i = 0; i < listOfId.size(); i++) {
      vResult.add(mapIdItem.get(listOfId.get(i)));
    }
    return vResult;
  }
  
  public static
  Vector<CodeAndDescription> getTipologie(boolean boUseCache)
  {
    if(boUseCache && vTipologie != null && vTipologie.size() > 0) {
      return vTipologie;
    }
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(0);
      Object result = oRPCClient.execute("PORTALE.getTipologie", parameters);
      
      vTipologie = toVectorOfCodeAndDescription(result);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    if(vTipologie == null) vTipologie = new Vector<CodeAndDescription>();
    return vTipologie;
  }	
  
  public static
  Vector<CodeAndDescription> getTipiPagina(boolean boUseCache)
  {
    if(boUseCache && vTipiPagina != null && vTipiPagina.size() > 0) {
      return vTipiPagina;
    }
    vTipiPagina = new Vector<CodeAndDescription>();
    vTipiPagina.add(new CodeAndDescription(new Integer(0),  "  (Nessuna)  "));
    vTipiPagina.add(new CodeAndDescription(new Integer(1),  "Pagina pubblica"));
    vTipiPagina.add(new CodeAndDescription(new Integer(2),  "Pagina privata"));
    vTipiPagina.add(new CodeAndDescription(new Integer(3),  "Menu intestazione"));
    vTipiPagina.add(new CodeAndDescription(new Integer(4),  "Menu navigazione"));
    vTipiPagina.add(new CodeAndDescription(new Integer(5),  "Menu footer"));
    vTipiPagina.add(new CodeAndDescription(new Integer(6),  "Citazione"));
    vTipiPagina.add(new CodeAndDescription(new Integer(7),  "Elenco singolo"));
    vTipiPagina.add(new CodeAndDescription(new Integer(8),  "Elenco multiplo"));
    vTipiPagina.add(new CodeAndDescription(new Integer(9),  "Elenco principale"));
    vTipiPagina.add(new CodeAndDescription(new Integer(10), "Elenco in evidenza"));
    return vTipologie;
  }
  
  public static
  Vector<CodeAndDescription> getViste(boolean boUseCache)
  {
    if(boUseCache && vViste != null && vViste.size() > 0) {
      return vViste;
    }
    vViste = new Vector<CodeAndDescription>();
    vViste.add(new CodeAndDescription(new Integer(0), "predefinita"));
    vViste.add(new CodeAndDescription(new Integer(1), "a pagina casuale"));
    return vViste;
  }
  
  private static
  void loadSottoCategorie()
  {
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(0); // Lingua predefinita
      mapSottoCategorie = WUtil.toMapObject(oRPCClient.execute("PORTALE.getSottoCategorie", parameters));
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }	
  
  public static
  Vector<CodeAndDescription> toVectorOfCodeAndDescription(Object oItems)
  {
    Vector<CodeAndDescription> vResult = new Vector<CodeAndDescription>();
    if(oItems instanceof List) {
      List<?> listItems = (List<?>) oItems;
      for(int i = 0; i < listItems.size(); i++) {
        Object oRecord  = listItems.get(i);
        if(!(oRecord instanceof List)) continue;
        List<?> listRecord = (List<?>) oRecord;
        Object oId   = listRecord.get(0);
        String sDesc = listRecord.get(listRecord.size()-1).toString();
        sDesc = AppUtil.denormalizeText(sDesc);
        vResult.add(new CodeAndDescription(oId, sDesc));
      }
    }
    return vResult;
  }
  
  public static
  Vector<CodeAndDescription> toVectorOfCodeAndDescription(Object oItems, Object oEmptyCode, String sEmptyDesc)
  {
    Vector<CodeAndDescription> vResult = new Vector<CodeAndDescription>();
    vResult.add(new CodeAndDescription(oEmptyCode, sEmptyDesc));
    if(oItems instanceof List) {
      List<?> listItems = (List<?>) oItems;
      for(int i = 0; i < listItems.size(); i++) {
        Object oRecord  = listItems.get(i);
        if(!(oRecord instanceof List)) continue;
        List<?> listRecord = (List<?>) oRecord;
        Object oId   = listRecord.get(0);
        String sDesc = listRecord.get(listRecord.size()-1).toString();
        sDesc = AppUtil.denormalizeText(sDesc);
        vResult.add(new CodeAndDescription(oId, sDesc));
      }
    }
    return vResult;
  }
  
  public static
  Map<Object, String> toMapIdDesc(Vector<?> vItems)
  {
    Map<Object, String> mapResult = new HashMap<Object, String>();
    if(vItems == null || vItems.size() == 0) return mapResult;
    for(int i = 0; i < vItems.size(); i++) {
      Object oRecord  = vItems.get(i);
      Object oId   = null;
      String sDesc = null;
      if(oRecord instanceof List) {
        List<?> listRecord = (List<?>) oRecord;
        oId   = listRecord.get(0);
        sDesc = WUtil.toString(listRecord.get(listRecord.size()-1), "");
      }
      else if(oRecord instanceof CodeAndDescription) {
        CodeAndDescription cdRecord = (CodeAndDescription) oRecord;
        oId   = cdRecord.getCode();
        sDesc = cdRecord.getDescription();
      }
      if(oId == null) continue;
      if(sDesc == null) sDesc = "";
      mapResult.put(oId, sDesc);
    }
    return mapResult;
  }
}
