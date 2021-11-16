package org.dew.cms.backend.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.dew.cms.backend.util.BEConfig;
import org.dew.cms.backend.ws.WSArticoli;
import org.dew.cms.backend.ws.WSAutori;
import org.dew.cms.backend.ws.WSCategorie;
import org.dew.cms.backend.ws.WSComuni;
import org.dew.cms.backend.ws.WSFM;
import org.dew.cms.backend.ws.WSIstituti;
import org.dew.cms.backend.ws.WSLingue;
import org.dew.cms.backend.ws.WSLuoghi;
import org.dew.cms.backend.ws.WSOpzioni;
import org.dew.cms.backend.ws.WSPagine;
import org.dew.cms.backend.ws.WSPortale;
import org.dew.cms.backend.ws.WSSottocategorie;
import org.dew.cms.backend.ws.WSTag;
import org.dew.cms.backend.ws.WSTipiArticolo;
import org.dew.cms.backend.ws.WSUtenti;

public
class WebServices extends org.rpc.server.RpcServlet
{
  private static final long serialVersionUID = -8713948362629677462L;
  
  @Override
  public
  void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    
    if(!BEConfig.isConfigFileLoaded()) {
      String cfgFileName = config.getInitParameter("cms.cfg");
      BEConfig.loadConfig(cfgFileName);
    }
  }
  
  @Override
  public
  void init()
    throws ServletException
  {
    rpcExecutor      = new org.rpc.server.MultiRpcExecutor();
    restAudit        = null;
    restTracer       = null;
    
    legacy           = false;
    createRpcContex  = false;
    checkSession     = false;
    checkSessionREST = false;
    restful          = true;
    basicAuth        = false;
    
    addWebService(new WSLingue(),         "LINGUE",         "Servizio gestione linuge");
    addWebService(new WSTag(),            "TAG",            "Servizio gestione tag");
    addWebService(new WSIstituti(),       "ISTITUTI",       "Servizio gestione istituti");
    addWebService(new WSComuni(),         "COMUNI",         "Servizio gestione comuni");
    addWebService(new WSLuoghi(),         "LUOGHI",         "Servizio gestione luoghi");
    addWebService(new WSAutori(),         "AUTORI",         "Servizio gestione autori");
    addWebService(new WSArticoli(),       "ARTICOLI",       "Servizio gestione articoli");
    addWebService(new WSPortale(),        "PORTALE",        "Servizio gestione portale");
    addWebService(new WSOpzioni(),        "OPZIONI",        "Servizio gestione opzioni");
    addWebService(new WSCategorie(),      "CATEGORIE",      "Servizio gestione categorie");
    addWebService(new WSSottocategorie(), "SOTTOCATEGORIE", "Servizio gestione sottocategorie");
    addWebService(new WSTipiArticolo(),   "TIPOLOGIE",      "Servizio gestione tipologie");
    addWebService(new WSPagine(),         "PAGINE",         "Servizio gestione pagine");
    addWebService(new WSUtenti(),         "UTENTI",         "Servizio gestione utenti");
    addWebService(new WSFM(),             "FM",             "Servizio File Manager");
  }
}
