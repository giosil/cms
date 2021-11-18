package org.dew.cms.gui.forms;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.IAutore;

import org.dew.cms.gui.dialogs.AutoriLookUpFinder;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ATableModelForSorter;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.GUIUtil;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;

import org.dew.util.WUtil;

public
class GUIAutori extends AEntityEditor implements IAutore
{
  private static final long serialVersionUID = 3208963862213850120L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  protected boolean boCanSelect = false;
  
  public
  GUIAutori(boolean boCanSelect)
  {
    super();
    this.boCanSelect = boCanSelect;
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
    fp.addTab("Autore");
    fp.addRow();
    fp.addTextField(sCOGNOME,    "Cognome", 50);
    fp.addTextField(sNOME,       "Nome",    50);
    fp.addRow();
    fp.addOptionsField(sID_TIPO, "Tipo",    Opzioni.getTipiAutore(true));
    fp.addTextField(sEMAIL,      "Email",   100);
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
    fp.addTab("Autore");
    fp.addRow();
    fp.addTextField(sCOGNOME,      "Cognome",   50);
    fp.addTextField(sNOME,         "Nome",      50);
    fp.addRow();
    fp.addOptionsField(sID_TIPO,   "Tipo",      Opzioni.getTipiAutore(true));
    fp.addOptionsField(sSESSO,     "Sesso",     Opzioni.getSesso());
    fp.addDateField(sDATA_NASCITA, "Data Nascita");
    fp.addBlankField();
    fp.addRow();
    fp.addTextField(sTITOLO,       "Titolo",    50);
    fp.addTextField(sTELEFONO,     "Telefono",  50);
    fp.addRow();
    fp.addTextField(sCELLULARE,    "Cellulare", 50);
    fp.addTextField(sEMAIL,        "Email",     100);
    fp.addTab("Note");
    fp.addNoteField(sNOTE,         "Note",      5);
    fp.addHiddenField(sID);
    fp.addHiddenField(sDESC_TIPO);
    fp.build();
    
    fp.setCase(sEMAIL, 0);
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sCOGNOME);
    listMandatoryFields.add(sNOME);
    listMandatoryFields.add(sID_TIPO);
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
    String[] asCOLUMNS   = {"Cognome", "Nome", "Tipo",     "Email"};
    String[] asSYMBOLICS = { sCOGNOME, sNOME,  sDESC_TIPO, sEMAIL };
    
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS,	asSYMBOLICS);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
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
      String sNominativo = WUtil.toString(mapValues.get(sNOMINATIVO), null);
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
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("AUTORI.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 autore trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " autori trovati.");
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
    Object oId  = oRecord.get(sID);
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("AUTORI.read", parameters));
    
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
    
    fpDetail.setEnabled(sID, false);
    
    fpDetail.getComponent(sNOTE).requestFocus();
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
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("AUTORI.insert", parameters));
      
      fpDetail.setValues(mapResult);
      oRecords.add(mapResult);
      iRowToSelect = oRecords.size() - 1;
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("AUTORI.update", parameters));
      
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
    Object oId  = oRecord.get(sID);
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("AUTORI.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Autore non eliminabile.");
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
      GUIMessage.showWarning("Effettuare una ricerca per poter esportare gli autori.");
      return;
    }
    TableUtils.exportTableToFile(oTable);
  }
  
  protected
  boolean isElementEnabled()
  {
    return true;
  }
  
  protected
  void doToggle()
      throws Exception
  {
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
  }
  
  protected
  void checkActions(List<JButton> listDefActions, boolean boAllowEditing)
  {
    removeButtonByActionCommand(listDefActions, sACTION_TOGGLE);
    if(btnPrint != null) GUIUtil.setGUIData(btnPrint, IConstants.sGUIDATA_EXPORT);
    if(boAllowEditing && boCanSelect) {
      btnSelect = GUIUtil.buildActionButton(IConstants.sGUIDATA_SELECT, sACTION_SELECT);
      btnSelect.addActionListener(this);
      listDefActions.add(0, btnSelect);
    }
  }
}
