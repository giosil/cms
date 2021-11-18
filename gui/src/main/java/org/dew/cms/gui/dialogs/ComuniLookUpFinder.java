package org.dew.cms.gui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.components.ILookUpFinder;
import org.dew.swingup.rpc.IRPCClient;

import org.dew.util.WUtil;

import org.dew.cms.common.IComune;

public
class ComuniLookUpFinder implements ILookUpFinder, IComune
{
  public
  List<List<Object>> find(String sEntity, List<Object> oFilter)
      throws Exception
  {
    Map<String, Object> mapFilter = new HashMap<String, Object>();
    
    String sCodice = WUtil.toString(oFilter.get(0), null);
    if(sCodice != null && sCodice.length() > 0) {
      char c0 = sCodice.charAt(0);
      if(Character.isDigit(c0)) {
        mapFilter.put(sCOD_ISTAT, sCodice);
      }
      else {
        mapFilter.put(sCOD_FISCALE, sCodice);
      }
    }
    
    String sDescrizione = WUtil.toString(oFilter.get(1), null);
    String sProvincia   = null;
    int iSepProv = sDescrizione.indexOf('(');
    if(iSepProv > 0) {
      int iEndProv = sDescrizione.indexOf(')', iSepProv);
      if(iEndProv > 0) {
        sProvincia = sDescrizione.substring(iSepProv + 1, iEndProv);
      }
      else {
        sProvincia = sDescrizione.substring(iSepProv + 1);
      }
      sDescrizione = sDescrizione.substring(0, iSepProv);
    }
    if(sDescrizione != null) {
      mapFilter.put(sDESCRIZIONE, sDescrizione.trim());
    }
    if(sProvincia != null && sProvincia.length() > 0) {
      mapFilter.put(sPROVINCIA, sProvincia.trim());
    }
    
    IRPCClient rpcClient = ResourcesMgr.getDefaultRPCClient();
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(mapFilter);
    
    Object result = rpcClient.execute("COMUNI.lookup", parameters);
    
    return WUtil.toListOfListObject(result);
  }
}