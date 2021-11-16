package org.dew.cms.backend.ws;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dew.cms.common.IOpzioni;

public 
class WSOpzioni implements IOpzioni
{
  protected static Logger oLogger = Logger.getLogger(WSOpzioni.class);
  
  public static 
  Map<String, Object> read()
      throws Exception
  {
    Map<String, Object> mapResult = new HashMap<String, Object>();
    
    mapResult.put(sLINGUE,        WSLingue.lookup(null));
    mapResult.put(sTIPI_AUTORE,   WSAutori.getTipiAutore());
    mapResult.put(sTIPI_UTENTE,   WSUtenti.getTipiUtenti());
    mapResult.put(sRUOLI,         WSAutori.getRuoli());
    mapResult.put(sISTITUTI,      WSIstituti.lookup(null));
    mapResult.put(sLUOGHI,        WSLuoghi.lookup(null));
    mapResult.put(sCATEGORIE,     WSPortale.getCategorie(0));
    mapResult.put(sSOTTO_CAT,     WSPortale.getSottoCategorie(0));
    mapResult.put(sTIPOLOGIE,     WSPortale.getTipologie(0));
    mapResult.put(sIMPORT_FOLDER, WSArticoli.getImportFolder());
    
    return mapResult;
  }
}
