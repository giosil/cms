package org.dew.cms.gui.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.IArticolo;
import org.dew.cms.gui.DecodifiableFactory;
import org.dew.cms.gui.util.AppUtil;
import org.dew.cms.gui.util.Opzioni;
import org.dew.cms.gui.util.PopUpRichText;
import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.IResourceMgr;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.components.JRichTextNote;
import org.dew.swingup.editors.EntityLookUpDialog;
import org.dew.swingup.fm.DownloadManager;
import org.dew.swingup.fm.FMEntry;
import org.dew.swingup.fm.FMViewer;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ADataPanel;
import org.dew.swingup.util.CodeAndDescription;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.GUIUtil;
import org.dew.util.WUtil;

public 
class GUIArticolo extends JPanel implements ActionListener, IArticolo
{
  private static final long serialVersionUID = 227610612243129058L;
  
  protected FormPanel fpHeader;
  protected JComboBox<?> jbCategory;
  protected JComboBox<?> jbTypology;
  protected FormPanel fpContent;
  protected FormPanel fpMultimedia;
  protected FormPanel fpMetaInfo;
  protected FormPanel fpComponents;
  protected FormPanel fpLuoghi;
  protected JTabbedPane jTabbedPane;
  protected JTextField jtfDescrizioneFile;
  protected boolean boUpdateDescrizioneFile = false;
  
  protected FMViewer fmArtFiles;
  protected FMViewer fmImpFiles;
  protected JButton btnDxMove;
  protected JButton btnSxMove;
  protected JButton btnDxCopy;
  protected JButton btnSxCopy;
  protected JButton btnAddURL;
  
  protected JButton btnFind;
  protected JButton btnReset;
  protected JButton btnNew;
  
  protected JComboBox<?> jcbLanguage;
  protected JButton btnSave;
  protected JButton btnCancel;
  protected JButton btnToggle;
  protected JButton btnDelete;
  protected JButton btnNavigation;
  protected JButton btnPreview;
  
  protected Map<String, Object> mapReadArt;
  protected Map<String, Object> mapCurrArt;
  protected String sLastLang;
  protected String sCurrLang;
  protected Map<String, Object> mapLinkFiles;
  
  public static final int iSTATUS_STARTUP = 0;
  public static final int iSTATUS_VIEW    = 1;
  public static final int iSTATUS_NEW     = 2;
  protected int iCurrentStatus = iSTATUS_STARTUP;
  
  public final static int iURL_PAGE_PORTAL     = 0;
  public final static int iURL_PAGE_PREVIEW    = 1;
  public final static int iURL_PAGE_NAVIGATION = 2;
  
  public GUIArticolo()
  {
    try {
      Opzioni.load(null);
      init();
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante l'inizializzazione di GUIArticolo", ex);
    }
  }
  
  public
  void actionPerformed(ActionEvent e)
  {
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null) return;
    try {
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      if(sActionCommand.equals("find"))            doFind();
      else if(sActionCommand.equals("reset"))      doReset();
      else if(sActionCommand.equals("new"))        doNew(null);
      else if(sActionCommand.equals("save"))       doSave();
      else if(sActionCommand.equals("cancel"))     doCancel();
      else if(sActionCommand.equals("delete"))     doDelete();
      else if(sActionCommand.equals("toggle"))     doToggle();
      else if(sActionCommand.equals("add_files"))  doAddFiles(false);
      else if(sActionCommand.equals("rem_files"))  doRemFiles(false);
      else if(sActionCommand.equals("adc_files"))  doAddFiles(true);
      else if(sActionCommand.equals("rec_files"))  doRemFiles(true);
      else if(sActionCommand.equals("add_url"))    doAddURL();
      else if(sActionCommand.equals("navigation")) doNavigation();
      else if(sActionCommand.equals("preview"))    doPreview();
    }
    catch (Exception ex) {
      GUIMessage.showException(ex);
    }
    finally {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
  }
  
  protected
  void doFind()
  {
    Map<String, Object> mapFilter = fpHeader.getValues();
    mapFilter.remove(sID);
    
    Date dDataArticolo  = (Date) mapFilter.remove(sDATA_ARTICOLO);
    if(dDataArticolo != null) {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.YEAR, -1);
      mapFilter.put(sDATA_INIZIO, cal.getTime());
      mapFilter.put(sDATA_FINE,   null);
    }
    
    GUIArticoli guiArticoli = new GUIArticoli();
    List<String> oKeys = new ArrayList<String>();
    oKeys.add(sID);
    oKeys.add(sDESCRIZIONE);
    oKeys.add(sDATA_ARTICOLO);
    EntityLookUpDialog oLookUpDialog = new EntityLookUpDialog(guiArticoli, oKeys, false);
    oLookUpDialog.setTitle("Ricerca Articolo");
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    oLookUpDialog.setSize(950, 700);
    try {
      guiArticoli.fireReset();
      guiArticoli.setFilterValues(mapFilter);
      if(!fpHeader.isBlank('%')) {
        guiArticoli.fireFind();
      }
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la preparazione della ricerca", ex);
    }
    oLookUpDialog.setLocation(size.width/2 - oLookUpDialog.getSize().width/2, size.height/2 - oLookUpDialog.getSize().height/2);
    oLookUpDialog.setVisible(true);
    
    List<?> listSelectedRecord = oLookUpDialog.getSelectedRecord();
    if(listSelectedRecord == null || listSelectedRecord.size() == 0) return;
    
    Integer oIdArticolo = (Integer) listSelectedRecord.get(0);
    if(oIdArticolo == null || oIdArticolo.intValue() == 0) return;
    
    read(oIdArticolo.intValue());
  }
  
  protected
  void doReset()
  {
    clear();
    fpHeader.setValue(sDATA_ARTICOLO, new Date());
    fpHeader.selectFirstTab();
    onChangeEditorStatus(iSTATUS_STARTUP);
  }
  
  protected
  void clear()
  {
    mapReadArt = null;
    mapCurrArt = null;
    fmArtFiles.clearSelection();
    fpHeader.clear();
    fpContent.clear();
    fpMultimedia.clear();
    if(fpMetaInfo   != null) fpMetaInfo.clear();
    if(fpLuoghi     != null) fpLuoghi.clear();
    if(fpComponents != null) fpComponents.clear();
    sLastLang  = "0";
    sCurrLang  = "0";
    jTabbedPane.setSelectedIndex(0);
    jcbLanguage.setSelectedIndex(0);
  }
  
  public
  void doNew(String sDescArticolo)
  {
    clear();
    
    Map<String, Object> mapResult = null;
    try{
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      mapResult = WUtil.toMapObject(oRPCClient.execute("ARTICOLI.getNewId", parameters));
      if(mapResult == null || mapResult.isEmpty()) {
        GUIMessage.showWarning("Generazione nuovo articolo non riuscita.");
        return;
      }
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la generazione del nuovo articolo", ex);
      return;
    }
    Object oFirstType = null;
    if(jbTypology != null && jbTypology.getItemCount() > 0) {
      oFirstType = jbTypology.getItemAt(0);
    }
    Object oFirstCategory = null;
    if(jbCategory != null && jbCategory.getItemCount() > 0) {
      oFirstCategory = jbCategory.getItemAt(0);
    }
    
    Integer oIdArticolo = (Integer) mapResult.get(sID);
    String sMultFolder  = (String)  mapResult.get(sMULT_FOLDER);
    mapCurrArt = new HashMap<String, Object>();
    mapCurrArt.put(sID,            oIdArticolo);
    mapCurrArt.put(sDATA_ARTICOLO, new Date());
    mapCurrArt.put(sID_TIPO_UTE,   new Integer(0));
    mapCurrArt.put(sID_TIPO_ART,   oFirstType);
    mapCurrArt.put(sID_CATEGORIA,  oFirstCategory);
    if(sDescArticolo != null && sDescArticolo.length() > 0) {
      mapCurrArt.put(sDESCRIZIONE, sDescArticolo);	
    }
    fmArtFiles.setRootDirectory(sMultFolder);
    fpHeader.setValues(mapCurrArt);
    
    onChangeEditorStatus(iSTATUS_NEW);
  }
  
  protected
  boolean checkBeforeSave(Map<String, Object> mapArticle)
  {
    return true;
  }
  
  protected
  void doSave()
  {
    Integer oId = WUtil.toInteger(fpHeader.getValue(sID), 0);
    if(oId == null || oId.intValue() == 0) return;
    if(fpHeader.isBlank(sDATA_ARTICOLO)) {
      GUIMessage.showWarning("Specificare la data dell'articolo.");
      fpHeader.requestFocus(sDATA_ARTICOLO);
      return;
    }
    if(mapCurrArt == null) mapCurrArt = new HashMap<String, Object>();
    String sDescrizione = null;
    if(fpHeader.isBlank(sDESCRIZIONE)) {
      Map<String, Object> mapContenuti = WUtil.toMapObject(mapCurrArt.get(sCONTENUTI));
      if(mapContenuti != null) {
        Map<String, Object> mapContenuti_DefLang = WUtil.toMapObject(mapContenuti.get("0"));
        if(mapContenuti_DefLang != null) {
          sDescrizione = WUtil.toString(mapContenuti_DefLang.get(sTITOLO), null);
          if(sDescrizione != null) sDescrizione = sDescrizione.toUpperCase();
        }
      }
      if(sDescrizione == null || sDescrizione.length() == 0) {
        sDescrizione = (String) fpContent.getValue(sTITOLO);
        if(sDescrizione != null) sDescrizione = sDescrizione.toUpperCase();
      }
      if(sDescrizione == null || sDescrizione.length() == 0) {
        sDescrizione = "ARTICOLO " + oId;
      }
      fpHeader.setValue(sDESCRIZIONE, sDescrizione);
    }
    else {
      sDescrizione = WUtil.toString(fpHeader.getValue(sDESCRIZIONE), "");
    }
    mapCurrArt.putAll(fpHeader.getValues());
    if(fpMetaInfo   != null) {
      Map<String, Object> mapMetaInfo = fpMetaInfo.getValues();
      if(mapMetaInfo != null) {
        mapMetaInfo.remove(sNOTE);
        mapMetaInfo.remove(sSPECIFICA);
        mapMetaInfo.remove(sPREZZI);
      }
      mapCurrArt.putAll(mapMetaInfo);
    }
    if(fpLuoghi     != null) mapCurrArt.putAll(fpLuoghi.getValues());
    if(fpComponents != null) mapCurrArt.putAll(fpComponents.getValues());
    
    updateContenuto();
    
    Object oDataIns = mapCurrArt.get(sDATA_INS);
    boolean boInserimento = oDataIns == null;
    if(boInserimento) {
      mapCurrArt.put(sUTENTE_INS, ResourcesMgr.getSessionManager().getUser().getUserName());
    }
    else {
      mapCurrArt.put(sUTENTE_AGG, ResourcesMgr.getSessionManager().getUser().getUserName());
    }
    
    if(!checkBeforeSave(mapCurrArt)) return;
    
    try{
      Map<String, Object> mapValues = WUtil.toMapObject(DataNormalizer.normalize(mapCurrArt));
      AppUtil.normalizeText(mapValues);
      
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      if(boInserimento && sDescrizione != null) {
        parameters.add(sDescrizione);
        Boolean oExists = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.exists", parameters, true), false);
        if(oExists != null && oExists.booleanValue()) {
          GUIMessage.showWarning("Esiste gi\340 un articolo con la stessa descrizione");
          fpHeader.requestFocus(sDESCRIZIONE);
          return;
        }
      }
      parameters.clear();
      parameters.add(mapValues);
      Boolean oResult = null;
      if(boInserimento) {
        oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.insert", parameters, true), false);
      }
      else {
        oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.update", parameters, true), false);
      }
      if(oResult == null || !oResult.booleanValue()) {
        if(boInserimento) {
          GUIMessage.showWarning("Inserimento articolo non riuscito.");
        }
        else {
          GUIMessage.showWarning("Aggiornamento articolo non riuscito.");
        }
        return;
      }
    }
    catch(Exception ex) {
      if(boInserimento) {
        GUIMessage.showException("Errore durante l'inserimento dell'articolo", ex);
      }
      else {
        GUIMessage.showException("Errore durante l'aggiornamento dell'articolo", ex);
      }
      return;
    }
    fpHeader.setValue(sDATA_INS, new Date());
    GUIMessage.showInformation("Articolo " + oId + " salvato con successo.");
    afterSave();
  }
  
  protected
  void afterSave()
  {
    doReset();
  }
  
  protected
  void doCancel()
  {
    if(mapCurrArt == null || mapCurrArt.isEmpty()) return;
    Integer oIdArticolo = (Integer) fpHeader.getValue(sID);
    if(oIdArticolo == null || oIdArticolo.intValue() == 0) return;
    boolean boConfirm = GUIMessage.getConfirmation("Si vogliono annullare le modifiche?");
    if(!boConfirm) return;
    if(mapReadArt == null || mapReadArt.isEmpty()) {
      mapCurrArt = null;
    }
    else {
      mapCurrArt = new HashMap<String, Object>(mapReadArt);
    }
    showArticolo();
  }
  
  protected
  void doToggle()
  {
    Integer oId = (Integer) fpHeader.getValue(sID);
    if(oId == null || oId.intValue() == 0) return;
    Boolean  oAttivo = WUtil.toBooleanObj(fpHeader.getValue(sATTIVO), true);
    boolean boAttivo = oAttivo != null ? oAttivo.booleanValue() : false;
    boolean boConfirm = false;
    if(boAttivo) {
      boConfirm = GUIMessage.getConfirmation("Si vuole rendere l'articolo " + oId + " NON pi\371 visibile.");
    }
    else {
      boConfirm = GUIMessage.getConfirmation("Si vuole rendere l'articolo " + oId + " visibile.");
    }
    if(!boConfirm) return;
    try{
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(oId);
      parameters.add(new Boolean(!boAttivo));
      Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.setEnabled", parameters), oAttivo);
      if(oResult == null) {
        GUIMessage.showWarning("Aggiornamento articolo non riuscito.");
        return;
      }
      if(oResult.booleanValue()) {
        GUIMessage.showInformation("L'articolo " + oId + " \350 stato reso visibile.");
      }
      else {
        GUIMessage.showInformation("L'articolo " + oId + " \350 stato reso NON visibile.");
      }
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante l'aggiornamento dell'articolo", ex);
      return;
    }
    fpHeader.setValue(sATTIVO, new Boolean(!boAttivo));
  }
  
  protected
  void doNavigation()
  {
    if(fpHeader == null) return;
    Integer oId = (Integer) fpHeader.getValue(sID);
    if(oId == null || oId.intValue() == 0) return;
    String sURLWebPage = getURLWebPage(iURL_PAGE_NAVIGATION);
    if(sURLWebPage == null || sURLWebPage.length() < 5) {
      String sURLBase = getURLBase();
      if(sURLBase == null || sURLBase.length() < 5) return;
      sURLWebPage = sURLBase + "/index.do?cat=";
    }
    int iIdCategoria = 1;
    Object oIdCategoria = fpHeader.getValue(sID_CATEGORIA);
    if(oIdCategoria instanceof CodeAndDescription) {
      oIdCategoria = ((CodeAndDescription) oIdCategoria).getCode();
    }
    if(oIdCategoria instanceof Number) {
      iIdCategoria = ((Number) oIdCategoria).intValue();
    }
    try {
      if(sURLWebPage.endsWith("=") || sURLWebPage.endsWith("/")) {
        ResourcesMgr.openBrowser(sURLWebPage + iIdCategoria);
      }
      else {
        ResourcesMgr.openBrowser(sURLWebPage);
      }
    }
    catch(Exception ex) {
      GUIMessage.showException(ex);
    }	
  }
  
  protected
  void doPreview()
  {
    if(fpHeader == null) return;
    Integer oId = (Integer) fpHeader.getValue(sID);
    if(oId == null || oId.intValue() == 0) return;
    String sURLWebPage = getURLWebPage(iURL_PAGE_PREVIEW);
    if(sURLWebPage == null || sURLWebPage.length() < 5) {
      String sURLBase = getURLBase();
      if(sURLBase == null || sURLBase.length() < 5) return;
      sURLWebPage = sURLBase + "/index.do?ida=";
    }
    try {
      if(sURLWebPage.endsWith("=") || sURLWebPage.endsWith("/")) {
        ResourcesMgr.openBrowser(sURLWebPage + oId);
      }
      else {
        ResourcesMgr.openBrowser(sURLWebPage);
      }
    }
    catch(Exception ex) {
      GUIMessage.showException(ex);
    }
  }
  
  protected
  void doAddFiles(boolean boCopy)
  {
    String sCurrentDirectory = fmArtFiles.getCurrentDirectory();
    if(sCurrentDirectory == null || sCurrentDirectory.length() == 0) {
      GUIMessage.showWarning("Directory di destinazione non specificata.");
      return;
    }
    FMEntry fmCurrentDirectory = null;
    if(boCopy) {
      fmCurrentDirectory = new FMEntry();
      fmCurrentDirectory.setPath(sCurrentDirectory);
      fmCurrentDirectory.setType("d");
    }
    FMEntry[] arrayOfEntry = fmImpFiles.getSelectedItems();
    if(arrayOfEntry == null || arrayOfEntry.length == 0) {
      GUIMessage.showWarning("Selezionare almeno un file per lo spostamento.");
      return;
    }
    for(int i = 0 ; i < arrayOfEntry.length; i++) {
      FMEntry fmEntry = arrayOfEntry[i];
      if(fmEntry == null || !fmEntry.isFile()) continue;
      if(boCopy) {
        fmArtFiles.doCopy(fmEntry, false);
        fmArtFiles.doPaste(fmCurrentDirectory, false, false);
      }
      else {
        fmArtFiles.doMove(fmEntry.getPath(), sCurrentDirectory);
      }
    }
    fmArtFiles.doRefresh();
    fmImpFiles.doRefresh();
  }
  
  protected
  void doRemFiles(boolean boCopy)
  {
    String sCurrentDirectory = fmImpFiles.getCurrentDirectory();
    if(sCurrentDirectory == null || sCurrentDirectory.length() == 0) {
      GUIMessage.showWarning("Directory di destinazione non specificata.");
      return;
    }
    FMEntry fmCurrentDirectory = null;
    if(boCopy) {
      fmCurrentDirectory = new FMEntry();
      fmCurrentDirectory.setPath(sCurrentDirectory);
      fmCurrentDirectory.setType("d");
    }
    FMEntry[] arrayOfEntry = fmArtFiles.getSelectedItems();
    if(arrayOfEntry == null || arrayOfEntry.length == 0) {
      GUIMessage.showWarning("Selezionare almeno un file per lo spostamento.");
      return;
    }
    for(int i = 0 ; i < arrayOfEntry.length; i++) {
      FMEntry fmEntry = arrayOfEntry[i];
      if(fmEntry == null || !fmEntry.isFile()) continue;
      if(boCopy) {
        fmImpFiles.doCopy(fmEntry, false);
        fmImpFiles.doPaste(fmCurrentDirectory, false, false);
      }
      else {
        fmImpFiles.doMove(fmEntry.getPath(), sCurrentDirectory);
      }
    }
    fmArtFiles.doRefresh();
    fmImpFiles.doRefresh();
  }
  
  protected
  void doAddURL()
  {
    String sURL = GUIMessage.getInput("Riportare l'indirizzo completo della pagina (ad esempio: http://www.test.com/test/index.html o mailto:indirizzo di posta elettronica)");
    if(sURL == null || sURL.length() == 0) return;
    String sURLLC = sURL.toLowerCase();
    if(!sURLLC.startsWith("http://") && !sURLLC.startsWith("https://") && !sURLLC.startsWith("mailto://") && !sURLLC.startsWith("ftp://")) {
      GUIMessage.showWarning("Indirizzo non valido");
      return;
    }
    if(sURLLC.length() < 8 || sURLLC.length() > 255) {
      GUIMessage.showWarning("Indirizzo non valido");
      return;
    }
    
    int iMaxProg = -1;
    Vector<FMEntry> vEntries = fmArtFiles.getEntries();
    if(vEntries != null && vEntries.size() > 0) {
      for(int i = 0; i < vEntries.size(); i++) {
        Object oEntry = vEntries.get(i);
        String sEntry = oEntry != null ? oEntry.toString() : "";
        if(!sEntry.endsWith(".link")) continue;
        int iProg = AppUtil.getPrefix(sEntry);
        if(iProg > iMaxProg) iMaxProg = iProg;
      }
    }
    iMaxProg++;
    String sPrefix   = WUtil.lpad(String.valueOf(iMaxProg), '0', 3);
    String sHost     = AppUtil.getHost(sURLLC);
    String sFileName = sPrefix + "_" + sHost + "_.link";
    
    String sLocalFolder = DownloadManager.getDefaultLocalFolder();
    if(sLocalFolder == null || sLocalFolder.length() == 0) {
      GUIMessage.showWarning("Non e' possibile creare il collegamento alla URL specificata.");
      return;
    }
    File fLocalFolder = new File(sLocalFolder);
    if(!fLocalFolder.exists()) {
      fLocalFolder.mkdirs();
    }
    File file = new File(sLocalFolder + File.separator + sFileName);
    PrintStream ps = null;
    try {
      ps = new PrintStream(file);
      ps.print(sURL);
      ps.flush();
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la creazione del file", ex);
      return;
    }
    finally {
      if(ps != null) try{ ps.close(); } catch(Exception ex) {}
    }
    if(mapLinkFiles == null) mapLinkFiles = new HashMap<String, Object>();
    mapLinkFiles.put(sFileName, sURL);
    List<File> listOfFile = new ArrayList<File>();
    listOfFile.add(file);
    fmArtFiles.doUpload(null, listOfFile);
  }
  
  protected
  void doDelete()
  {
    Integer oId = (Integer) fpHeader.getValue(sID);
    if(oId == null || oId.intValue() == 0) return;
    boolean boConfirm = GUIMessage.getConfirmation("Si vuole eliminare definitivamente l'articolo " + oId + "?");
    if(!boConfirm) return;
    try{
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(oId);
      Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.delete", parameters), false);
      if(oResult == null || !oResult.booleanValue()) {
        GUIMessage.showWarning("Cancellazione articolo non riuscita.");
        return;
      }
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la cancellazione dell'articolo", ex);
      return;
    }
    doReset();
  }
  
  public
  void read(int iIdArticolo)
  {
    clear();
    try {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(iIdArticolo);
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("ARTICOLI.read", parameters, true));
      if(mapResult == null || mapResult.isEmpty()) {
        GUIMessage.showWarning("Lettura dell'articolo " + iIdArticolo + " non riuscita");
        return;
      }
      AppUtil.denormalizeText(mapResult);
      mapReadArt = mapResult;
      mapCurrArt = new HashMap<String, Object>(mapReadArt);
      showArticolo();
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante la lettura dell'articolo", ex);
    }
  }
  
  protected
  Container buildHeader()
  {
    ADataPanel dpAutori = new DPAutori();
    dpAutori.setPreferredSize(new Dimension(0, 90));
    
    fpHeader = new FormPanel("Articolo");
    fpHeader.addTab("Dati principali");
    fpHeader.addRow();
    fpHeader.addDateField(sDATA_ARTICOLO,    "Data");
    fpHeader.addComponent(sID_AUTORE,        "Autore",    DecodifiableFactory.buildDCAutore());
    fpHeader.addIntegerField(sID, "Id");
    fpHeader.addRow();
    fpHeader.addTextField(sDESCRIZIONE,      "Descriz.",  255);
    fpHeader.addRow();
    fpHeader.addOptionsField(sID_ISTITUTO,   "Istituto",  Opzioni.getIstituti(true));
    fpHeader.addOptionsField(sID_LUOGO,      "Luogo",     Opzioni.getLuoghi(true));
    fpHeader.addBlankField();
    fpHeader.addRow();
    fpHeader.addOptionsField(sID_CATEGORIA,  "Categoria", Opzioni.getCategorie(true));
    fpHeader.addOptionsField(sID_SOTTOCATEG, "S.Categ.",  new Vector<CodeAndDescription>());
    fpHeader.addOptionsField(sID_TIPO_UTE,   "Tipo Ut.",  Opzioni.getTipiUtente(true));
    fpHeader.addTab("Altri autori");
    fpHeader.addDataPanel(sAUTORI, dpAutori);
    fpHeader.addHiddenField(sDATA_INS);
    fpHeader.addHiddenField(sUTENTE_INS);
    fpHeader.addHiddenField(sATTIVO);
    fpHeader.build();
    
    fpHeader.setValue(sDATA_ARTICOLO, new Date());
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sDATA_ARTICOLO);
    fpHeader.setMandatoryFields(listMandatoryFields);
    
    // Ricerca quando si preme invio sul campo
    KeyListener klFind = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          if(iCurrentStatus == iSTATUS_STARTUP) {
            try { doFind(); } catch(Exception ex) {}
          }
        }
      }
    };
    List<Component> listComponents = fpHeader.getListComponents();
    for(int i = 0; i < listComponents.size(); i++) {
      Object oComponent = listComponents.get(i);
      if(oComponent instanceof JTextComponent) {
        ((JTextComponent) oComponent).addKeyListener(klFind);
      }
    }
    
    fpHeader.getJLabel(sID).setForeground(Color.red);
    fpHeader.setEnabled(sID, false);
    
    jbCategory = (JComboBox<?>) fpHeader.getComponent(sID_CATEGORIA);
    jbCategory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object oCategoria  = fpHeader.getValue(sID_CATEGORIA);
        Vector<CodeAndDescription> vSottoCateg = Opzioni.getSottoCategorie(oCategoria, true);
        fpHeader.setOptionsItems(sID_SOTTOCATEG, vSottoCateg);
      }
    });
    return fpHeader;
  }
  
  protected
  Container buildDetail()
  {
    // Occorre costruire prima il container relativo ai contenuti multimediali: questo perche'
    // si possano creare gli oggetti PopUpRichText con riferimento al fmArtFiles
    Container ctnMultimedia = buildDetail_Mult();
    jTabbedPane = new JTabbedPane();
    jTabbedPane.addTab("Contenuto",  ResourcesMgr.getImageIcon("DocumentDrawLarge.gif"),    buildDetail_Cont());
    jTabbedPane.addTab("Multimedia", ResourcesMgr.getImageIcon("PaletteLarge.gif"),         ctnMultimedia);
    jTabbedPane.addTab("Meta Info",  ResourcesMgr.getImageIcon("DocumentDiagramLarge.gif"), buildDetail_Meta());
    jTabbedPane.addTab("Luoghi",     ResourcesMgr.getImageIcon("Search24.gif"),             buildDetail_Luoghi());
    jTabbedPane.addTab("Componenti", ResourcesMgr.getImageIcon("DocumentMagLarge.gif"),     buildDetail_Componenti());
    return jTabbedPane;
  }
  
  protected
  Container buildDetail_Cont()
  {
    fpContent = new FormPanel();
    fpContent.setCase(0);
    fpContent.addRow();
    fpContent.addTextField(sTITOLO,       "Titolo",        255);
    fpContent.addRow();
    fpContent.addTextField(sSPECIFICA,    "Specifica",     255);
    fpContent.addRow();
    fpContent.addRichNoteField(sABSTRACT, "Abstract",      3);
    fpContent.addRow();
    fpContent.addRichNoteField(sTESTO,    "Testo",         5);
    fpContent.addRow();
    fpContent.addRichNoteField(sNOTE,     "Note",          2);
    fpContent.addRow();
    fpContent.addNoteField(sRIFERIMENTI,  "Riferimenti",   2);
    fpContent.addRow();
    fpContent.addNoteField(sKEYWORDS,     "Parole Chiave", 3);
    fpContent.build();
    
    new PopUpRichText((JRichTextNote) fpContent.getComponent(sABSTRACT), fmArtFiles);
    new PopUpRichText((JRichTextNote) fpContent.getComponent(sTESTO),    fmArtFiles);
    
    return fpContent;
  }
  
  protected
  Container buildDetail_Mult()
  {
    fpMultimedia = new FormPanel();
    fpMultimedia.addRow();
    fpMultimedia.addTextField(sDESCRIZIONE, "Descrizione", 255);
    fpMultimedia.build();
    
    fpMultimedia.setCase(sDESCRIZIONE, 0);
    
    jtfDescrizioneFile = (JTextField) fpMultimedia.getComponent(sDESCRIZIONE);
    jtfDescrizioneFile.getDocument().addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
        if(boUpdateDescrizioneFile) updateDescrizioneSelectedFile();
      }
      public void insertUpdate(DocumentEvent e) {
        if(boUpdateDescrizioneFile) updateDescrizioneSelectedFile();
      }
      public void changedUpdate(DocumentEvent e) {
        if(boUpdateDescrizioneFile) updateDescrizioneSelectedFile();
      }
    });
    
    String sImportFolder = Opzioni.getImportFolder();
    
    fmArtFiles = new FMViewer("File articolo", ResourcesMgr.config.getProperty(IResourceMgr.sAPP_RPC_URL), sImportFolder, "*.*");
    fmArtFiles.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting()) return;
        FMEntry fmEntry = fmArtFiles.getSelectedItem();
        if(fmEntry == null) {
          jtfDescrizioneFile.setText("");
          return;
        }
        btnDxMove.setEnabled(fmEntry != null && fmEntry.isFile() && fpMultimedia != null && fpMultimedia.isEnabled());
        btnDxCopy.setEnabled(fmEntry != null && fmEntry.isFile() && fpMultimedia != null && fpMultimedia.isEnabled());
        showDescrizioneSelectedFile();
      }
    });
    fmImpFiles = new FMViewer("File caricati", ResourcesMgr.config.getProperty(IResourceMgr.sAPP_RPC_URL), sImportFolder, "*.*");
    fmImpFiles.doRefresh();
    fmImpFiles.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        FMEntry fmEntry = fmImpFiles.getSelectedItem();
        btnSxMove.setEnabled(fmEntry != null && fmEntry.isFile() && fpMultimedia != null && fpMultimedia.isEnabled());
        btnSxCopy.setEnabled(fmEntry != null && fmEntry.isFile() && fpMultimedia != null && fpMultimedia.isEnabled());
      }
    });
    
    JPanel jpRight = new JPanel(new BorderLayout());
    jpRight.add(buildMultButtons(), BorderLayout.WEST);
    jpRight.add(fmImpFiles,         BorderLayout.CENTER);
    
    JPanel jpCenter = new JPanel(new GridLayout(1, 2));
    jpCenter.add(fmArtFiles);
    jpCenter.add(jpRight);
    
    fpMultimedia.add(jpCenter, BorderLayout.CENTER);
    return fpMultimedia;
  }
  
  protected
  Container buildDetail_Meta()
  {
    ADataPanel dpTag = new DPTag();
    dpTag.setPreferredSize(new Dimension(0, 140));
    ADataPanel dpArticoli = new DPArticoli();
    dpArticoli.setPreferredSize(new Dimension(0, 140));
    ADataPanel dpPrezzi = new DPPrezzi();
    dpPrezzi.setPreferredSize(new Dimension(0, 160));
    
    fpMetaInfo = new FormPanel();
    fpMetaInfo.addRow();
    fpMetaInfo.addDataPanel(sTAG,       "Tag",            dpTag);
    fpMetaInfo.addRow();
    fpMetaInfo.addDataPanel(sCORRELATI, "Art. Correlati", dpArticoli);
    fpMetaInfo.addRow();
    fpMetaInfo.addDataPanel(sPREZZI,    "Prezzi",         dpPrezzi);
    fpMetaInfo.build();
    return new JScrollPane(fpMetaInfo);
  }
  
  protected
  Container buildDetail_Luoghi()
  {
    ADataPanel dpLuoghi = new DPLuoghi();
    dpLuoghi.setPreferredSize(new Dimension(0, 380));
    fpLuoghi = new FormPanel();
    fpLuoghi.addRow();
    fpLuoghi.addDataPanel(sLUOGHI, "Luoghi", dpLuoghi);
    fpLuoghi.build();
    return fpLuoghi;
  }
  
  protected
  Container buildDetail_Componenti()
  {
    ADataPanel dpArticoli = new DPArticoli();
    dpArticoli.setPreferredSize(new Dimension(0, 380));
    fpComponents = new FormPanel();
    fpComponents.addRow();
    fpComponents.addDataPanel(sCOMPONENTI, "Articoli", dpArticoli);
    fpComponents.build();
    return fpComponents;
  }
  
  protected
  Container buildButtons()
  {
    btnFind = GUIUtil.buildActionButton(IConstants.sGUIDATA_FIND,   "find");
    btnFind.addActionListener(this);
    btnFind.setFocusable(false);
    btnReset = GUIUtil.buildActionButton(IConstants.sGUIDATA_RESET, "reset");
    btnReset.addActionListener(this);
    btnReset.setFocusable(false);
    btnNew = GUIUtil.buildActionButton(IConstants.sGUIDATA_NEW,     "new");
    btnNew.addActionListener(this);
    btnNew.setFocusable(false);
    
    JPanel oButtonsPanel = new JPanel(new GridLayout(3, 1, 4, 4));
    oButtonsPanel.add(btnFind);
    oButtonsPanel.add(btnReset);
    oButtonsPanel.add(btnNew);
    
    JPanel oActionsPanel = new JPanel(new BorderLayout(4, 4));
    oActionsPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 8));
    oActionsPanel.add(oButtonsPanel, BorderLayout.NORTH);
    return oActionsPanel;
  }
  
  protected
  Container buildMultButtons()
  {
    btnDxMove = GUIUtil.buildActionButton("|Sposta il file selezionato di sinistra a destra|RightLarge.gif", "rem_files");
    btnDxMove.setHorizontalAlignment(SwingConstants.CENTER);
    btnDxMove.setEnabled(false);	    
    btnDxMove.addActionListener(this);
    
    btnSxMove = GUIUtil.buildActionButton("|Sposta il file selezionato di destra a sinistra|LeftLarge.gif",  "add_files");
    btnSxMove.setHorizontalAlignment(SwingConstants.CENTER);
    btnSxMove.setEnabled(false);
    btnSxMove.addActionListener(this);
    
    btnDxCopy = GUIUtil.buildActionButton("|Copia il file selezionato di sinistra a destra|EndLarge.gif", "rec_files");
    btnDxCopy.setHorizontalAlignment(SwingConstants.CENTER);
    btnDxCopy.setEnabled(false);	    
    btnDxCopy.addActionListener(this);
    
    btnSxCopy = GUIUtil.buildActionButton("|Copia il file selezionato di destra a sinistra|BeginLarge.gif",  "adc_files");
    btnSxCopy.setHorizontalAlignment(SwingConstants.CENTER);
    btnSxCopy.setEnabled(false);
    btnSxCopy.addActionListener(this);
    
    btnAddURL = GUIUtil.buildActionButton("|Aggiunge un URL alla sezione multimediale|WebComponent24.gif",  "add_url");
    btnAddURL.setHorizontalAlignment(SwingConstants.CENTER);
    btnAddURL.setEnabled(false);
    btnAddURL.addActionListener(this);
    
    JPanel oButtonsPanel = new JPanel(new GridLayout(5, 1));
    oButtonsPanel.add(btnDxMove);
    oButtonsPanel.add(btnSxMove);
    oButtonsPanel.add(btnDxCopy);
    oButtonsPanel.add(btnSxCopy);
    oButtonsPanel.add(btnAddURL);
    
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(oButtonsPanel, BorderLayout.NORTH);
    return oResult;
  }
  
  protected
  Container buildCommands()
  {
    jcbLanguage = new JComboBox<CodeAndDescription>(Opzioni.getLingue(true));
    jcbLanguage.setPreferredSize(new Dimension(300, 0));
    jcbLanguage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        sCurrLang = getSelectedLanguage();
        if(sLastLang == null || sLastLang.length() == 0) {
          sLastLang = "0";
        }
        try {
          updateContenuto();
          showContenuto();
          showDescrizioneSelectedFile();
        }
        finally {
          sLastLang = sCurrLang;
        }
      }
    });
    
    btnSave = GUIUtil.buildActionButton(IConstants.sGUIDATA_SAVE,      "save");
    btnSave.addActionListener(this);
    btnSave.setEnabled(true);
    btnCancel = GUIUtil.buildActionButton(IConstants.sGUIDATA_CANCEL,  "cancel");
    btnCancel.addActionListener(this);
    btnCancel.setEnabled(true);
    btnToggle = GUIUtil.buildActionButton(IConstants.sGUIDATA_DISABLE, "toggle");
    btnToggle.addActionListener(this);
    btnToggle.setEnabled(false);
    btnDelete = GUIUtil.buildActionButton(IConstants.sGUIDATA_DELETE,  "delete");
    btnDelete.addActionListener(this);
    btnDelete.setEnabled(false);
    btnNavigation = GUIUtil.buildActionButton("Na&vigazione|Navigazione|DocumentMagLarge.gif", "navigation");
    btnNavigation.addActionListener(this);
    btnNavigation.setEnabled(false);
    btnPreview = GUIUtil.buildActionButton("An&teprima|Anteprima|DocumentMagLarge.gif",  "preview");
    btnPreview.addActionListener(this);
    btnPreview.setEnabled(false);
    
    JPanel oPanel = new JPanel(new GridLayout(1, 6, 4, 4));
    oPanel.add(btnSave);
    oPanel.add(btnCancel);
    oPanel.add(btnToggle);
    oPanel.add(btnDelete);
    oPanel.add(btnNavigation);
    oPanel.add(btnPreview);
    
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(GUIUtil.buildLabelledComponent(jcbLanguage, "Lingua:", 50), BorderLayout.WEST);
    oResult.add(oPanel, BorderLayout.EAST);
    return oResult;
  }
  
  protected
  void onChangeEditorStatus(int iStatus)
  {
    switch (iStatus) {
    case iSTATUS_STARTUP:
      fpContent.setEnabled(false);
      fpMultimedia.setEnabled(false);
      fmArtFiles.setEnabled(false);
      if(fpMetaInfo   != null) fpMetaInfo.setEnabled(false);
      if(fpLuoghi     != null) fpLuoghi.setEnabled(false);
      if(fpComponents != null) fpComponents.setEnabled(false);
      
      btnFind.setEnabled(true);
      btnReset.setEnabled(true);
      btnNew.setEnabled(true);
      
      btnSave.setEnabled(false);
      btnCancel.setEnabled(false);
      btnToggle.setEnabled(false);
      btnDelete.setEnabled(false);
      btnNavigation.setEnabled(false);
      btnPreview.setEnabled(false);
      btnAddURL.setEnabled(false);
      break;
    case iSTATUS_VIEW:
      fpContent.setEnabled(true);
      fpMultimedia.setEnabled(true);
      fmArtFiles.setEnabled(true);
      if(fpMetaInfo   != null) fpMetaInfo.setEnabled(true);
      if(fpLuoghi     != null) fpLuoghi.setEnabled(true);
      if(fpComponents != null) fpComponents.setEnabled(true);
      
      btnFind.setEnabled(true);
      btnReset.setEnabled(true);
      btnNew.setEnabled(true);
      
      btnSave.setEnabled(true);
      btnCancel.setEnabled(true);
      btnToggle.setEnabled(true);
      btnDelete.setEnabled(true);
      btnNavigation.setEnabled(true);
      btnPreview.setEnabled(true);
      btnAddURL.setEnabled(true);
      break;
    case iSTATUS_NEW:
      fpContent.setEnabled(true);
      fpMultimedia.setEnabled(true);
      fmArtFiles.setEnabled(true);
      if(fpMetaInfo   != null) fpMetaInfo.setEnabled(true);
      if(fpLuoghi     != null) fpLuoghi.setEnabled(true);
      if(fpComponents != null) fpComponents.setEnabled(true);
      
      btnFind.setEnabled(true);
      btnReset.setEnabled(true);
      btnNew.setEnabled(false);
      
      btnSave.setEnabled(true);
      btnCancel.setEnabled(true);
      btnToggle.setEnabled(true);
      btnDelete.setEnabled(true);
      btnNavigation.setEnabled(false);
      btnPreview.setEnabled(false);
      btnAddURL.setEnabled(true);
      
      fpHeader.requestFocus();
      break;
    }
  }
  
  protected
  String getSelectedLanguage()
  {
    String sResult = null;
    Object oItem   = jcbLanguage.getSelectedItem();
    if(oItem == null) {
      sResult = "0";
    }
    else if(oItem instanceof CodeAndDescription) {
      Object oCode = ((CodeAndDescription) oItem).getCode();
      sResult = oCode != null ? oCode.toString() : "0";
    }
    else {
      sResult = oItem.toString();
    }
    if(sResult == null || sResult.length() == 0) sResult = "0";
    return sResult;
  }
  
  protected
  void showArticolo()
  {
    fpHeader.clear();
    fpContent.clear();
    fpMultimedia.clear();
    if(fpMetaInfo != null) fpMetaInfo.clear();
    if(fpLuoghi   != null) fpLuoghi.clear();
    if(mapCurrArt == null || mapCurrArt.isEmpty()) return;
    fpHeader.setValues(mapCurrArt);
    fpHeader.setValue(sID_SOTTOCATEG, mapCurrArt.get(sID_SOTTOCATEG));
    if(fpMetaInfo != null) {
      fpMetaInfo.setValue(sTAG,       mapCurrArt.get(sTAG));
      fpMetaInfo.setValue(sCORRELATI, mapCurrArt.get(sCORRELATI));
    }
    if(fpLuoghi != null) {
      fpLuoghi.setValue(sLUOGHI,      mapCurrArt.get(sLUOGHI));
    }
    if(fpComponents != null) {
      fpComponents.setValue(sCOMPONENTI, mapCurrArt.get(sCOMPONENTI));
    }
    String sMultimediaFolder = (String) mapCurrArt.get(sMULT_FOLDER);
    fmArtFiles.setRootDirectory(sMultimediaFolder);
    showContenuto();
    onChangeEditorStatus(iSTATUS_VIEW);
  }
  
  protected
  void updateContenuto()
  {
    if(mapCurrArt == null) return;
    Map<String, Object> mapContent = WUtil.toMapObject(mapCurrArt.get(sCONTENUTI));
    if(mapContent == null) {
      mapContent = new HashMap<String, Object>();
      mapCurrArt.put(sCONTENUTI, mapContent);
    }
    Map<String, Object> mapContent_LastLang   = fpContent.getValues();
    if(fpMetaInfo != null) {
      String  sNote      = WUtil.toString(fpMetaInfo.getValue(sNOTE),      null);
      String  sSpec      = WUtil.toString(fpMetaInfo.getValue(sSPECIFICA), null);
      List<?> listPrezzi = WUtil.toList(fpMetaInfo.getValue(sPREZZI),      null);
      if(sNote != null && sNote.length() > 0) {
        mapContent_LastLang.put(sNOTE, sNote);
      }
      if(sSpec != null && sSpec.length() > 0) {
        mapContent_LastLang.put(sSPECIFICA, sSpec);
      }
      if(listPrezzi != null && listPrezzi.size() > 0) {
        mapContent_LastLang.put(sPREZZI, listPrezzi);
      }
    }
    mapContent.put(sLastLang, mapContent_LastLang);
    mapCurrArt.put(sCONTENUTI, mapContent);
  }
  
  protected
  void showContenuto()
  {
    if(mapCurrArt == null) return;
    Map<String, Object> mapContent = WUtil.toMapObject(mapCurrArt.get(sCONTENUTI));
    if(mapContent == null) {
      mapContent = new HashMap<String, Object>();
      mapCurrArt.put(sCONTENUTI, mapContent);
    }
    Map<String, Object> mapContent_CurrLang = WUtil.toMapObject(mapContent.get(sCurrLang));
    fpContent.clear();
    fpContent.setValues(mapContent_CurrLang);
    if(fpMetaInfo != null) {
      fpMetaInfo.setValue(sNOTE,      null);
      fpMetaInfo.setValue(sSPECIFICA, null);
      fpMetaInfo.setValue(sPREZZI,    null);
    }
    if(mapContent_CurrLang != null) {
      String sNote       = WUtil.toString(mapContent_CurrLang.get(sNOTE),      null);
      String sSpec       = WUtil.toString(mapContent_CurrLang.get(sSPECIFICA), null);
      List<?> listPrezzi = WUtil.toList(mapContent_CurrLang.get(sPREZZI),      null);
      if(sNote != null && sNote.length() > 0) {
        fpMetaInfo.setValue(sNOTE, sNote);
      }
      if(sSpec != null && sSpec.length() > 0) {
        fpMetaInfo.setValue(sSPECIFICA, sSpec);
      }
      if(listPrezzi != null && listPrezzi.size() > 0) {
        fpMetaInfo.setValue(sPREZZI, listPrezzi);
      }
    }
  }
  
  protected
  void updateDescrizioneSelectedFile()
  {
    FMEntry fmEntry = fmArtFiles.getSelectedItem();
    if(fmEntry == null || !fmEntry.isFile()) return;
    
    if(mapCurrArt == null) mapCurrArt = new HashMap<String, Object>();
    Map<String, Object> mapMultimedia = WUtil.toMapObject(mapCurrArt.get(sMULTIMEDIA));
    if(mapMultimedia == null) {
      mapMultimedia = new HashMap<String, Object>();
      mapCurrArt.put(sMULTIMEDIA, mapMultimedia);
    }
    String sFilePath = fmEntry.getPath();
    Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sFilePath));
    if(mapDescrizioni == null) {
      mapDescrizioni = new HashMap<String, Object>();
      mapMultimedia.put(sFilePath, mapDescrizioni);
    }
    String sSelectedLanguage = getSelectedLanguage();
    mapDescrizioni.put(sSelectedLanguage, jtfDescrizioneFile.getText());
  }
  
  protected
  void showDescrizioneSelectedFile()
  {
    boUpdateDescrizioneFile = false;
    try {
      FMEntry fmEntry = fmArtFiles.getSelectedItem();
      if(fmEntry == null || !fmEntry.isFile()) return;
      
      if(mapCurrArt == null) mapCurrArt = new HashMap<String, Object>();
      Map<String, Object> mapMultimedia = WUtil.toMapObject(mapCurrArt.get(sMULTIMEDIA));
      if(mapMultimedia == null) {
        mapMultimedia = new HashMap<String, Object>();
        mapCurrArt.put(sMULTIMEDIA, mapMultimedia);
      }
      String sFilePath = fmEntry.getPath();
      Map<String, Object> mapDescrizioni = WUtil.toMapObject(mapMultimedia.get(sFilePath));
      if(mapDescrizioni == null) {
        mapDescrizioni = new HashMap<String, Object>();
        mapMultimedia.put(sFilePath, mapDescrizioni);
      }
      String sSelectedLanguage = getSelectedLanguage();
      String sDescrizione = WUtil.toString(mapDescrizioni.get(sSelectedLanguage), null);
      if(sDescrizione != null) {
        jtfDescrizioneFile.setText(sDescrizione);
      }
      else {
        sDescrizione = WUtil.toString(mapDescrizioni.get("0"), null);
        if(sDescrizione != null && sDescrizione.length() > 0) {
          jtfDescrizioneFile.setText(sDescrizione);
          mapDescrizioni.put(sSelectedLanguage, sDescrizione);
        }
        else {
          String sEntryName = fmEntry.getName();
          if(sEntryName != null && sEntryName.length() > 0) {
            String sURL = null;
            if(mapLinkFiles != null) {
              sURL = (String) mapLinkFiles.remove(sEntryName);
            }
            if(sURL != null && sURL.length() > 7) {
              jtfDescrizioneFile.setText(sURL);
              mapDescrizioni.put(sSelectedLanguage, sURL);
            }
            else {
              int iSepExt = sEntryName.lastIndexOf('.');
              if(iSepExt > 0) sEntryName = sEntryName.substring(0, iSepExt);
              jtfDescrizioneFile.setText(sEntryName.replace('_', ' '));
              mapDescrizioni.put(sSelectedLanguage, sEntryName);
            }
          }
        }
      }
    }
    finally {
      boUpdateDescrizioneFile = true;
    }
  }	
  
  protected
  String getURLBase()
  {
    String sResult = ResourcesMgr.config.getProperty("cms.url.base");
    if(sResult != null && sResult.length() > 0) return sResult;
    sResult = System.getProperty("cms.url.base");
    if(sResult != null && sResult.length() > 0) return sResult;
    sResult = System.getProperty("jnlp.cms.url.base");
    if(sResult != null && sResult.length() > 0) return sResult;
    sResult = ResourcesMgr.config.getProperty(IResourceMgr.sAPP_RPC_URL);
    if(sResult == null || sResult.length() == 0) return null;
    int iLastSlash = sResult.lastIndexOf("/");
    if(iLastSlash < 0) return null;
    return sResult.substring(0, iLastSlash);
  }
  
  protected
  String getURLWebPage(int iURLType)
  {
    String sResult = null;
    switch (iURLType) {
    case iURL_PAGE_PORTAL:
      sResult = ResourcesMgr.config.getProperty("cms.url.portal");
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("cms.url.portal");
      }
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("jnlp.cms.url.portal");
      }
      if(sResult != null && sResult.length() > 0) {
        if(sResult.startsWith("http://") || sResult.startsWith("https://")) {
          return sResult;
        }
        else {
          String sURLBase = getURLBase();
          if(sURLBase == null || sURLBase.length() == 0) return null;
          if(sResult.startsWith("/")) {
            return sURLBase + sResult;
          }
          else {
            return sURLBase + "/" + sResult;
          }
        }
      }
      break;
    case iURL_PAGE_PREVIEW:
      sResult = ResourcesMgr.config.getProperty("cms.url.preview");
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("cms.url.preview");
      }
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("jnlp.cms.url.preview");
      }
      if(sResult != null && sResult.length() > 0) {
        if(sResult.startsWith("http://") || sResult.startsWith("https://")) {
          return sResult;
        }
        else {
          String sURLBase = getURLBase();
          if(sURLBase == null || sURLBase.length() == 0) return null;
          if(sResult.startsWith("/")) {
            return sURLBase + sResult;
          }
          else {
            return sURLBase + "/" + sResult;
          }
        }
      }
      break;
    case iURL_PAGE_NAVIGATION:
      sResult = ResourcesMgr.config.getProperty("cms.url.navigation");
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("cms.url.navigation");
      }
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("jnlp.cms.url.navigation");
      }
      if(sResult != null && sResult.length() > 0) {
        if(sResult.startsWith("http://") || sResult.startsWith("https://")) {
          return sResult;
        }
        else {
          String sURLBase = getURLBase();
          if(sURLBase == null || sURLBase.length() == 0) return null;
          if(sResult.startsWith("/")) {
            return sURLBase + sResult;
          }
          else {
            return sURLBase + "/" + sResult;
          }
        }
      }
      break;
    default:
      sResult = ResourcesMgr.config.getProperty("cms.url.portal");
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("cms.url.portal");
      }
      if(sResult == null || sResult.length() == 0) {
        sResult = System.getProperty("jnlp.cms.url.portal");
      }
      if(sResult != null && sResult.length() > 0) {
        if(sResult.startsWith("http://") || sResult.startsWith("https://")) {
          return sResult;
        }
        else {
          String sURLBase = getURLBase();
          if(sURLBase == null || sURLBase.length() == 0) return null;
          if(sResult.startsWith("/")) {
            return sURLBase + sResult;
          }
          else {
            return sURLBase + "/" + sResult;
          }
        }
      }
      break;
    }
    return sResult;
  }
  
  protected
  void init()
      throws Exception
  {
    super.setLayout(new BorderLayout());
    
    JPanel jpNorth = new JPanel(new BorderLayout());
    jpNorth.add(buildHeader(),  BorderLayout.CENTER);
    jpNorth.add(buildButtons(), BorderLayout.EAST);
    
    this.add(jpNorth,         BorderLayout.NORTH);
    this.add(buildDetail(),   BorderLayout.CENTER);
    this.add(buildCommands(), BorderLayout.SOUTH);
    
    onChangeEditorStatus(iSTATUS_STARTUP);
  }
}
