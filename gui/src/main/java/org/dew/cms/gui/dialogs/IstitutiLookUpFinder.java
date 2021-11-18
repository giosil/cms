package org.dew.cms.gui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.components.ILookUpFinder;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.util.WUtil;
import org.dew.cms.common.IIstituto;

public
class IstitutiLookUpFinder implements ILookUpFinder, IIstituto
{
  public
  List<List<Object>> find(String sEntity, List<Object> oFilter)
      throws Exception
  {
    Map<String, Object> mapFilter = new HashMap<String, Object>();
    
    String sCodEsenzione = (String) oFilter.get(0);
    if(sCodEsenzione != null) {
      mapFilter.put(sCODICE, sCodEsenzione);
    }
    
    String sDescrizione = (String) oFilter.get(1);
    if(sDescrizione != null) {
      mapFilter.put(sDESCRIZIONE, sDescrizione.trim());
    }
    
    IRPCClient rpcClient = ResourcesMgr.getDefaultRPCClient();
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(mapFilter);
    
    Object result = rpcClient.execute("ISTITUTI.lookup", parameters);
    
    return WUtil.toListOfListObject(result);
  }
}