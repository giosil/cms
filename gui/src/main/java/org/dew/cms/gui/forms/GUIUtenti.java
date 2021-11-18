package org.dew.cms.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IUtente;

import org.dew.cms.gui.dialogs.AutoriLookUpFinder;
import org.dew.cms.gui.dialogs.DialogHTMLPage;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.GUIUtil;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;

import org.dew.util.WUtil;

public
class GUIUtenti extends AEntityEditor implements IUtente
{
  private static final long serialVersionUID = 2625231788550508355L;
  
  protected SimpleTableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  protected boolean boCanSelect = false;
  
  protected JButton btnReadArticles;
  protected JButton btnCopyEmail;
  
  public
  GUIUtenti(boolean boCanSelect)
  {
    super();
    this.boCanSelect = boCanSelect;
  }
  
  public
  void actionPerformed(ActionEvent e)
  {
    super.actionPerformed(e);
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null) return;
    try {
      if(sActionCommand.equals("articles"))        doViewReadArticles();
      else if(sActionCommand.equals("copy_email")) doCopyEmail();
    }
    catch (Exception ex) {
      GUIMessage.showException(ex);
    }
  }  
  
  public
  Object getCurrentSelection()
      throws Exception
  {
    return oLastRecordReaded;
  }
  
  protected
  Container buildGUIFilter()
  {
    FormPanel fp = new FormPanel("Ricerca");
    fp.addTab("Utente");
    fp.addRow();
    fp.addTextField(sCOGNOME,      "Cognome",  50);
    fp.addTextField(sNOME,         "Nome",     50);
    fp.addRow();
    fp.addOptionsField(sID_TIPO,   "Tipo",     Opzioni.getTipiUtente(true));
    fp.addTextField(sEMAIL,        "Email", 0,  0);
    fp.addRow();
    fp.addDateField(sREG_DAL,      "Reg. Dal");
    fp.addDateField(sREG_AL,       "Reg. Al");
    fp.build();
    
    fp.setCase(sEMAIL, 0);
    
    // Ricerca quando si preme invio sul campo
    KeyListener klFind = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) try { fireFind(); } catch(Exception ex) {}
      }
    };
    List<Component> listComponents = fp.getListComponents();
    for(int i = 0; i < listComponents.size(); i++) {
      Object oComponent = listComponents.get(i);
      if(oComponent instanceof JTextComponent) {
        ((JTextComponent) oComponent).addKeyListener(klFind);
      }
    }
    return fp;
  }
  
  protected
  Container buildGUIDetail()
  {
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Utente");
    fp.addRow();
    fp.addTextField(sCOGNOME,      "Cognome",     50);
    fp.addTextField(sNOME,         "Nome",        50);
    fp.addRow();
    fp.addOptionsField(sID_TIPO,   "Tipo",        Opzioni.getTipiUtente(true));
    fp.addOptionsField(sSESSO,     "Sesso",       Opzioni.getSesso());
    fp.addDateField(sDATA_NASCITA, "Data Nascita");
    fp.addTextField(sCITTA,        "Indirizzo",   50);
    fp.addRow();
    fp.addTextField(sUSERNAME,     "Username",    50);
    fp.addTextField(sPASSWORD,     "Password",    50);
    fp.addRow();
    fp.addTextField(sPROFESSIONE,  "Professione", 50);
    fp.addTextField(sEMAIL,        "Email",    0,  0);
    fp.addRow();
    fp.addDateField(sDATA_REG,     "Data Reg.");
    fp.addTimeField(sORA_REG,      "Ora Reg.");
    fp.addHiddenField(sID);
    fp.build();
    
    fp.setCase(sUSERNAME, 0);
    fp.setCase(sPASSWORD, 0);
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sCOGNOME);
    listMandatoryFields.add(sNOME);
    listMandatoryFields.add(sID_TIPO);
    listMandatoryFields.add(sUSERNAME);
    listMandatoryFields.add(sPASSWORD);
    fp.setMandatoryFields(listMandatoryFields);
    return fp;
  }
  
  protected
  Container buildGUIBigDetail()
  {
    return null;
  }
  
  protected
  Container buildGUIOtherDetail()
  {
    return null;
  }
  
  protected
  Container buildGUIResult()
  {
    String[] asCOLUMNS   = {"Username", "Cognome", "Nome", "Email", "Tipo",     "Data Reg.", "Ora Reg."};
    String[] asSYMBOLICS = {sUSERNAME,  sCOGNOME,  sNOME,  sEMAIL,  sDESC_TIPO, sDATA_REG,   sORA_REG  };
    
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS,	asSYMBOLICS);
    oTableModel.addTimeField(sORA_REG);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    oTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int iRow, int iCol) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, iRow, iCol);
        Map<String, Object> oRecord = oRecords.get(iRow);
        boolean boAttivo = WUtil.toBoolean(oRecord.get(sATTIVO), true);
        if(boAttivo) {
          this.setForeground(Color.black);
          this.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
        }
        else {
          this.setForeground(Color.gray);
          this.setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
        }
        return this;
      }
    });
    
    TableUtils.setLinkField(oTable, 3);
    TableUtils.setMonospacedFont(oTable);
    
    oTable.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() >= 2 && !e.isControlDown()) {
          try {
            fireSelect();
          }
          catch (Exception ex) {
            GUIMessage.showException(ex);
          }
        }
      }
    });
    
    JScrollPane oScrollPane = new JScrollPane(oTable);
    
    TableColumnResizer.setResizeColumnsListeners(oTable);
    TableSorter.setSorterListener(oTable);
    
    oTable.getSelectionModel().addListSelectionListener(this);
    
    return oScrollPane;
  }
  
  protected
  void onChoiceMade()
  {
    setChoice(oLastRecordReaded);
  }
  
  protected
  void setFilterValues(Object oValues)
      throws Exception
  {
    if(oValues instanceof Map) {
      Map<String, Object> mapValues = WUtil.toMapObject(oValues);
      String sNominativo = (String) mapValues.get(sNOMINATIVO);
      if(sNominativo != null && sNominativo.length() > 0) {
        if(AutoriLookUpFinder.isEmail(sNominativo)) {
          mapValues.put(sEMAIL, sNominativo.toLowerCase());
        }
        else {
          String sCognome = AutoriLookUpFinder.getCognome(sNominativo);
          String sNome    = AutoriLookUpFinder.getNome(sNominativo);
          if(sCognome != null && sCognome.length() > 0) {
            mapValues.put(sCOGNOME, sCognome);
          }
          if(sNome != null && sNome.length() > 0) {
            mapValues.put(sNOME, sNome);
          }
        }
      }
      
      FormPanel fpFilter = (FormPanel) getFilterContainer();
      fpFilter.setValues(mapValues);
    }
  }
  
  protected
  void doFind()
      throws Exception
  {
    FormPanel fpFilter = (FormPanel) getFilterContainer();
    Map<String, Object> oFilterValues = fpFilter.getValues();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(DataNormalizer.normalize(oFilterValues));
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("UTENTI.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 utente trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " utenti trovati.");
    }
  }
  
  protected
  void doReset()
      throws Exception
  {
    FormPanel fpFilter = (FormPanel) getFilterContainer();
    fpFilter.reset();
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    oRecords = new ArrayList<Map<String, Object>>();
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
  }
  
  protected
  boolean onSelection()
      throws Exception
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0 || iRow >= oRecords.size()) return false;
    
    Map<String, Object> oRecord = oRecords.get(iRow);
    Object oId = oRecord.get(sID);
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("UTENTI.read", parameters));
    
    oLastRecordReaded = new HashMap<String, Object>(mapRead);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    fpDetail.setValues(mapRead);
    fpDetail.selectFirstTab();
    
    return true;
  }
  
  protected
  void doNew()
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    fpDetail.setEnabled(sDATA_REG, false);
    fpDetail.setEnabled(sORA_REG,  false);
    fpDetail.requestFocus();
    
    oTable.clearSelection();
    oTable.setEnabled(false);
  }
  
  protected
  void doOpen()
      throws Exception
  {
    oTable.setEnabled(false);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.setEnabled(sDATA_REG, false);
    fpDetail.setEnabled(sORA_REG,  false);
  }
  
  protected
  boolean doSave(boolean boNew)
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    
    String sCheckMandatory = fpDetail.getStringCheckMandatories();
    if(sCheckMandatory.length() > 0) {
      GUIMessage.showWarning("Occorre valorizzare i seguenti campi:\n" + sCheckMandatory);
      return false;
    }
    
    int iRowToSelect = 0;
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    Map<String, Object> oDetailValues = fpDetail.getValues();
    Map<String, Object> oNormalizedValues = WUtil.toMapObject(DataNormalizer.normalize(oDetailValues));
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oNormalizedValues);
    
    if(boNew) {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("UTENTI.insert", parameters));
      
      fpDetail.setValues(mapResult);
      oRecords.add(mapResult);
      iRowToSelect = oRecords.size() - 1;
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("UTENTI.update", parameters));
      
      fpDetail.setValues(mapResult);
      int iRow = oTable.getSelectedRow();
      oRecords.set(iRow, mapResult);
      iRowToSelect = iRow;
    }
    
    oTable.setEnabled(true);
    TableSorter.resetHeader(oTable);
    oTableModel.notifyUpdates();
    oTable.setRowSelectionInterval(iRowToSelect, iRowToSelect);
    
    return true;
  }
  
  protected
  void doCancel()
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    
    int iRow = oTable.getSelectedRow();
    fpDetail.reset();
    if(iRow >= 0) {
      fpDetail.setValues(oLastRecordReaded);
    }
    
    oTable.setEnabled(true);
  }
  
  protected
  void doDelete()
      throws Exception
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0) return;
    
    Map<String, Object> oRecord = oRecords.get(iRow);
    Object oId = oRecord.get(sID);
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("UTENTI.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Utente non eliminabile.");
      return;
    }
    
    oRecords.remove(iRow);
    oTable.clearSelection();
    oTableModel.notifyUpdates();
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
  }
  
  protected
  void doPrint()
      throws Exception
  {
    if(oRecords == null || oRecords.size() == 0) {
      GUIMessage.showWarning("Effettuare una ricerca per poter esportare gli utenti.");
      return;
    }
    TableUtils.exportTableToFile(oTable);
  }
  
  protected
  boolean isElementEnabled()
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0) return true;
    Map<String, Object> oRecord = oRecords.get(iRow);
    return WUtil.toBoolean(oRecord.get(sATTIVO), true);
  }
  
  protected
  void doToggle()
      throws Exception
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0) return;
    
    Map<String, Object> oRecord = oRecords.get(iRow);
    Object  oId     = oRecord.get(sID);
    Boolean oAttivo = WUtil.toBooleanObj(oRecord.get(sATTIVO), true);
    boolean boNuovoStato = false;
    if(oAttivo != null) boNuovoStato = !oAttivo.booleanValue();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    parameters.add(new Boolean(boNuovoStato));
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("UTENTI.setEnabled", parameters), oAttivo);
    
    oRecord.put(sATTIVO, oResult);
    oLastRecordReaded.put(sATTIVO, oResult);
  }
  
  protected
  void doViewReadArticles()
      throws Exception
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0) return;
    
    Map<String, Object> oRecord = oRecords.get(iRow);
    Object oId = oRecord.get(sID);
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    
    List<Map<String, Object>> listResult = WUtil.toListOfMapObject(oRPCClient.execute("UTENTI.getReadArticles", parameters));
    if(listResult == null || listResult.size() == 0) {
      GUIMessage.showWarning("Non vi sono visite registrate per l'utente.");
      return;
    }
    
    String sUserName = (String) oRecord.get(sUSERNAME);
    String sEmail    = (String) oRecord.get(sEMAIL);
    if(sEmail == null) sEmail = "";
    if(sUserName == null) sUserName = sEmail;
    String sTitolo   = "Articoli letti da " + sUserName + ".";
    
    String sHTML = "<html><body>";
    sHTML += "<h3 align=\"center\">" + sTitolo + "</h3>";
    sHTML += "<table align=\"center\" border=\"1\">";
    sHTML += "<tr bgcolor=\"#eeeeee\"><th>Data Visita</th><th>Ora</th><th>Articolo</th></tr>";
    for(int i = 0; i < listResult.size(); i++) {
      Map<String, Object> mapRecord = listResult.get(i);
      Object oDataVisita   = mapRecord.get(IArticolo.sDATA_VISITA);
      Object oOraVisita    = mapRecord.get(IArticolo.sORA_VISITA);
      Object oDescArticolo = mapRecord.get(IArticolo.sDESCRIZIONE);
      sHTML += "<tr><td>" + WUtil.formatDate(oDataVisita, "-") + "</td><td>" + WUtil.formatTime(oOraVisita, false, false) + "</td><td>" + oDescArticolo + "</td></tr>";
    }
    sHTML += "</table>";
    sHTML += "</body></html>";
    
    DialogHTMLPage.showMe(sTitolo, sHTML);
  }
  
  protected
  void doCopyEmail()
      throws Exception
  {
    if(oRecords == null || oRecords.size() == 0) {
      GUIMessage.showWarning("Per copiare gli indirizzi di posta eseguire una ricerca.");
      return;
    }
    
    List<String> listEmail = new ArrayList<String>();
    for(int i = 0; i < oRecords.size(); i++) {
      Map<String, Object> mapRecord = oRecords.get(i);
      String sEmail = (String) mapRecord.get(sEMAIL);
      if(sEmail == null || sEmail.length() < 5) continue;
      int iAt = sEmail.indexOf('@');
      if(iAt <= 0) continue;
      int iDot = sEmail.lastIndexOf('.');
      if(iDot < iAt) continue;
      if(iDot == sEmail.length() - 1) continue;
      if(!listEmail.contains(sEmail)) {
        listEmail.add(sEmail.trim().toLowerCase());
      }
    }
    Collections.sort(listEmail);
    if(listEmail.size() == 0) {
      GUIMessage.showWarning("Non vi sono indirizzi di posta elettronica validi.");
      return;
    }
    
    String sText = "";
    for(int i = 0; i < listEmail.size(); i++) {
      sText += ";" + listEmail.get(i);
    }
    if(sText.length() > 0) sText = sText.substring(1);
    
    StringSelection oStringSelection = new StringSelection(sText);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(oStringSelection, null);
    GUIMessage.showInformation("Sono stati copiati " + listEmail.size() + " indirizzi di posta elettronica.");
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
    switch (iStatus) {
    case iSTATUS_STARTUP:
      btnReadArticles.setEnabled(false);
      btnCopyEmail.setEnabled(true);
      break;
    case iSTATUS_VIEW:
      btnReadArticles.setEnabled(true);
      btnCopyEmail.setEnabled(true);
      break;
    case iSTATUS_EDITING:
      btnReadArticles.setEnabled(false);
      btnCopyEmail.setEnabled(false);
      break;
    }
  }
  
  protected
  void checkActions(List<JButton> listDefActions, boolean boAllowEditing)
  {
    if(btnPrint != null) GUIUtil.setGUIData(btnPrint, IConstants.sGUIDATA_EXPORT);
    if(boAllowEditing && boCanSelect) {
      btnSelect = GUIUtil.buildActionButton(IConstants.sGUIDATA_SELECT, sACTION_SELECT);
      btnSelect.addActionListener(this);
      listDefActions.add(0, btnSelect);
    }
    btnReadArticles = GUIUtil.buildActionButton("Ar&ticoli|Articoli visti|WebComponent24.gif", "articles");
    btnReadArticles.addActionListener(this);
    btnReadArticles.setEnabled(false);
    listDefActions.add(btnReadArticles);
    iMaxRowsActions++;
    btnCopyEmail = GUIUtil.buildActionButton("Copia|Copia indirizzi email|" + IConstants.sICON_COPY, "copy_email");
    btnCopyEmail.addActionListener(this);
    btnCopyEmail.setEnabled(false);
    listDefActions.add(btnCopyEmail);
    iMaxRowsActions++;
    // Il pulsante di chiusura si riporta all'ultimo posto
    listDefActions.add(removeButtonByActionCommand(listDefActions, sACTION_EXIT));
  }
}