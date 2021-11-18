package org.dew.cms.gui;

import java.util.HashMap;
import java.util.Map;

import org.dew.swingup.ASimpleMenuManager;
import org.dew.swingup.AWorkPanel;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.EntityInternalFrame;
import org.dew.swingup.editors.IEntityMgr;

import org.dew.cms.gui.forms.GUIArticolo;
import org.dew.cms.gui.forms.GUIAutori;
import org.dew.cms.gui.forms.GUICategorie;
import org.dew.cms.gui.forms.GUIImport;
import org.dew.cms.gui.forms.GUIIstituti;
import org.dew.cms.gui.forms.GUILingue;
import org.dew.cms.gui.forms.GUILuoghi;
import org.dew.cms.gui.forms.GUIPagine;
import org.dew.cms.gui.forms.GUISottocategorie;
import org.dew.cms.gui.forms.GUITag;
import org.dew.cms.gui.forms.GUITipiArticolo;
import org.dew.cms.gui.forms.GUIUtenti;

import org.dew.cms.gui.util.AppUtil;

public
class MenuManager extends ASimpleMenuManager
{
  public final static String sICON_LINGUE     = "GoalFlagLarge.gif";
  public final static String sICON_TAG        = "ObjectLarge.gif";
  public final static String sICON_CATEGORIE  = "Bookmarks24.gif";
  public final static String sICON_SOTTO_CAT  = "FlowGraphLarge.gif";
  public final static String sICON_TIPOLOGIE  = "OpenProjectLarge.gif";
  public final static String sICON_ISTITUTI   = "HomeLarge.gif";
  public final static String sICON_LUOGHI     = "Search24.gif";
  public final static String sICON_AUTORI     = "UserLarge.gif";
  public final static String sICON_ARTICOLI   = "DocumentDrawLarge.gif";
  public final static String sICON_SEZIONI    = "FlowGraphLarge.gif";
  public final static String sICON_MULTIMEDIA = "PaletteLarge.gif";
  public final static String sICON_PAGINE     = "DocumentDiagramLarge.gif";
  public final static String sICON_UTENTI     = "UsersLarge.gif";
  public final static String sICON_SERVER     = "WebComponent24.gif";
  
  public
  void enable(String userRole)
  {
    Map<String, Object> enablings = new HashMap<String, Object>();
    enablings.put("gestione.articoli",       true);
    enablings.put("gestione.multimedia",     true);
    enablings.put("gestione.utenti",         true);
    enablings.put("gestione.tag",            true);
    enablings.put("gestione.categorie",      true);
    enablings.put("gestione.sottocategorie", true);
    enablings.put("gestione.tipologie",      true);
    enablings.put("gestione.istituti",       true);
    enablings.put("gestione.luoghi",         true);
    enablings.put("gestione.autori",         true);
    enablings.put("gestione.pagine",         true);
    enablings.put("gestione.lingue",         true);
    
    super.setEnabled(enablings);
  }
  
  protected
  void onClick(String idItem)
  {
    AWorkPanel workPanel = ResourcesMgr.getWorkPanel();
    
    if(idItem.equals("gestione.articoli")) {
      GUIArticolo guiArticolo = AppUtil.getGUIArticolo();
      if(guiArticolo == null) return;
      workPanel.show(guiArticolo, "Articoli", sICON_ARTICOLI);
    }
    else if(idItem.equals("gestione.multimedia")) {
      if(!workPanel.selectTab("Multimedia")) {
        workPanel.show(new GUIImport(), "Multimedia", sICON_MULTIMEDIA);
      }
    }
    else if(idItem.equals("gestione.utenti")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUIUtenti(false), "Utenti", sICON_UTENTI);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.tag")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUITag(), "Tag", sICON_TAG);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.categorie")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUICategorie(), "Categorie", sICON_CATEGORIE);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.sottocategorie")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUISottocategorie(), "Sottocategorie", sICON_SOTTO_CAT);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.tipologie")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUITipiArticolo(), "Tipologie", sICON_TIPOLOGIE);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.istituti")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUIIstituti(), "Istituti", sICON_ISTITUTI);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.luoghi")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUILuoghi(), "Luoghi", sICON_LUOGHI);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.autori")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUIAutori(false), "Autori", sICON_AUTORI);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.pagine")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUIPagine(), "Pagine", sICON_PAGINE);
      workPanel.show(oEntityMgr);
    }
    else if(idItem.equals("gestione.lingue")) {
      IEntityMgr oEntityMgr = new EntityInternalFrame();
      oEntityMgr.init(new GUILingue(), "Lingue", sICON_LINGUE);
      workPanel.show(oEntityMgr);
    }
  }
  
  protected
  void initMenu()
  {
    addMenu("gestione", "&Gestione", "Menu Gestione", false);
  }
  
  protected
  void initItems()
  {
    // Gap tra le voci del menu laterale.
    iGapItems = 12;
    addMenuItem("gestione", // Id Menu
        "articoli",         // Id Item
        "Articoli",         // Testo
        "Apre la form gestione articoli", // Descrizione
        sICON_ARTICOLI,     // Small Icon
        sICON_ARTICOLI,     // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "multimedia",       // Id Item
        "Multimedia",       // Testo
        "Apre la form gestione dei contenuti multimediali", // Descrizione
        sICON_MULTIMEDIA,   // Small Icon
        sICON_MULTIMEDIA,   // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "luoghi",           // Id Item
        "Luoghi",           // Testo
        "Apre la form gestione Luoghi",   // Descrizione
        sICON_LUOGHI,       // Small Icon
        sICON_LUOGHI,       // Large Icon
        true);              // Enabled
    
    addSeparator("gestione");
    
    addMenuItem("gestione", // Id Menu
        "categorie",        // Id Item
        "Categorie",        // Testo
        "Apre la form gestione categorie", // Descrizione
        sICON_CATEGORIE,    // Small Icon
        sICON_CATEGORIE,    // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "sottocategorie",   // Id Item
        "Sottocateg.",      // Testo
        "Apre la form gestione sottocategorie", // Descrizione
        sICON_SOTTO_CAT,    // Small Icon
        sICON_SOTTO_CAT,    // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "tipologie",        // Id Item
        "Tipologie",        // Testo
        "Apre la form gestione tipologie", // Descrizione
        sICON_TIPOLOGIE,    // Small Icon
        sICON_TIPOLOGIE,    // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "tag",              // Id Item
        "Tag",              // Testo
        "Apre la form gestione tag",       // Descrizione
        sICON_TAG,          // Small Icon
        sICON_TAG,          // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "pagine",           // Id Item
        "Pagine",           // Testo
        "Apre la form gestione pagine",    // Descrizione
        sICON_PAGINE,       // Small Icon
        sICON_PAGINE,       // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "utenti",           // Id Item
        "Utenti",           // Testo
        "Apre la form gestione utenti",   // Descrizione
        sICON_UTENTI,       // Small Icon
        sICON_UTENTI,       // Large Icon
        true);              // Enabled
    
    addSeparator("gestione");
    
    addMenuItem("gestione", // Id Menu
        "istituti",         // Id Item
        "Istituti",         // Testo
        "Apre la form gestione istituti", // Descrizione
        sICON_ISTITUTI,     // Small Icon
        sICON_ISTITUTI,     // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "autori",           // Id Item
        "Autori",           // Testo
        "Apre la form gestione autori",   // Descrizione
        sICON_AUTORI,       // Small Icon
        sICON_AUTORI,       // Large Icon
        true);              // Enabled
    
    addMenuItem("gestione", // Id Menu
        "lingue",           // Id Item
        "Lingue",           // Testo
        "Apre la form gestione lingue",  // Descrizione
        sICON_LINGUE,       // Small Icon
        sICON_LINGUE,       // Large Icon
        true);              // Enabled
  }
}
