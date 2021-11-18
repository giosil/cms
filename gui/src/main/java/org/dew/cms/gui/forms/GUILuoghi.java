package org.dew.cms.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.ILuogo;
import org.dew.cms.gui.DecodifiableFactory;
import org.dew.cms.gui.MenuManager;
import org.dew.cms.gui.util.AppUtil;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ATableModelForSorter;
import org.dew.swingup.util.CodeAndDescription;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.GUIUtil;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;

import org.dew.util.WUtil;

public
class GUILuoghi extends AEntityEditor implements ILuogo
{
  private static final long serialVersionUID = 4632280942235846429L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  
  protected DPLuoghi dpLuoghi;
  
  protected JButton btnGeocoding;
  protected JButton btnGeoPaste;
  
  protected boolean boCanSelect = false;
  
  public
  GUILuoghi()
  {
    super();
  }
  
  public
  GUILuoghi(boolean boCanSelect)
  {
    super();
    this.boCanSelect = boCanSelect;
  }
  
  public
  void setDataPanel(DPLuoghi dpLuoghi)
  {
    this.dpLuoghi = dpLuoghi;
  }
  
  public
  void actionPerformed(ActionEvent e)
  {
    super.actionPerformed(e);
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null) return;
    try {
      if(sActionCommand.equals("geocoding"))      doGeocoding();
      else if(sActionCommand.equals("geo_paste")) doGeoPaste();
    }
    catch (Exception ex) {
      GUIMessage.showException(ex);
    }
  }
  
  protected
  void doGeocoding()
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    String sIndirizzo = (String) fpDetail.getValue(sINDIRIZZO);
    if(sIndirizzo == null || sIndirizzo.length() < 3) {
      GUIMessage.showWarning("Specificare l'indirizzo per la geocodifica.");
      return;
    }
    List<Object> listComune = WUtil.toList(fpDetail.getContent(sID_COMUNE), Object.class, null);
    if(listComune == null || listComune.size() < 2) {
      GUIMessage.showWarning("Specificare il comune per la geocodifica.");
      return;
    }
    String sComune = (String) listComune.get(listComune.size() - 1);
    String sAddress = sIndirizzo + " " + sComune;
    StringSelection oStringSelection = new StringSelection(sAddress);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(oStringSelection, null);
    try {
      ResourcesMgr.openBrowser("http://itouchmap.com/latlong.html");
    }
    catch(Exception ex) {
      GUIMessage.showException(ex);
    }
  }
  
  protected
  void doGeoPaste()
  {
    try {
      String sText = null;
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable transferable = clipboard.getContents(null);
      if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        sText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
      }
      if(sText == null) {
        GUIMessage.showWarning("Nessun testo contenuto nella clipboard.");
        return;
      }
      int iComma = sText.indexOf(',');
      if(iComma < 0) {
        GUIMessage.showWarning("Il testo copiato deve essere di questo formato: \"latitudine,longitudine\".");
        return;
      }
      double dLatitude = 0.0d;
      try {
        dLatitude = Double.parseDouble(sText.substring(0, iComma).trim());
      }
      catch(Exception ex) {
        ex.printStackTrace();
        GUIMessage.showWarning("Latitudine non corretta");
        return;
      }
      double dLongitude = 0.0d;
      try {
        dLongitude = Double.parseDouble(sText.substring(iComma+1).trim());
      }
      catch(Exception ex) {
        ex.printStackTrace();
        GUIMessage.showWarning("Longitudine non corretta");
        return;
      }
      FormPanel fpDetail = (FormPanel) getDetailContainer();
      fpDetail.setValue(sLATITUDINE,  new Double(dLatitude));
      fpDetail.setValue(sLONGITUDINE, new Double(dLongitude));
      GUIMessage.showInformation("Latitudine e longitudine riportate correttamente.");
    }
    catch(Exception ex) {
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
    Vector<CodeAndDescription> vTipiLuogo = Opzioni.getTipiLuogo(false);
    
    FormPanel fp = new FormPanel("Ricerca");
    fp.addTab("Luogo");
    fp.addRow();
    fp.addTextField(sRICERCA,    "Ricerca", 255);
    fp.addTextField(sCODICE,     "Codice",   50);
    fp.addRow();
    fp.addOptionsField(sID_TIPO, "Tipo",    vTipiLuogo);
    fp.addComponent(sID_COMUNE,  "Comune",  DecodifiableFactory.buildDCComune());
    fp.build();
    
    fp.setCase(sDESCRIZIONE, 0);
    
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
    Vector<CodeAndDescription> vTipiLuogo = Opzioni.getTipiLuogo(true);
    
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Luogo");
    fp.addRow();
    fp.addTextField(sCODICE,        "Codice",       50);
    fp.addTextField(sDESCRIZIONE,   "Descrizione", 255);
    fp.addRow();
    fp.addOptionsField(sID_TIPO,    "Tipo",        vTipiLuogo);
    fp.addComponent(sID_COMUNE,     "Comune",      DecodifiableFactory.buildDCComune());
    fp.addTextField(sINDIRIZZO,     "Indirizzo",   255);
    fp.addTextNumericField(sCAP,    "CAP",           5);
    fp.addRow();
    fp.addTextField(sSITO_WEB,      "Sito Web",    255);
    fp.addTextField(sEMAIL,         "Email",       255);
    fp.addTextField(sTEL_1,         "Tel. 1",       50);
    fp.addTextField(sTEL_2,         "Tel. 2",       50);
    fp.addRow();
    fp.addTextField(sFAX,           "Fax",          50);
    fp.addDoubleField(sLATITUDINE,  "Lat.");
    fp.addDoubleField(sLONGITUDINE, "Long.");
    fp.addTextField(sINFORMAZIONI,  "Info",        100);
    fp.addHiddenField(sID);
    fp.build();
    
    fp.setCase(sDESCRIZIONE, 0);
    fp.setCase(sINDIRIZZO,   0);
    fp.setCase(sSITO_WEB,    0);
    fp.setCase(sEMAIL,       0);
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sDESCRIZIONE);
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
    String[] asCOLUMNS   = {"Tipo",     "Codice", "Descrizione", "Indirizzo", "CAP", "Comune"};
    String[] asSYMBOLICS = {sDESC_TIPO, sCODICE,  sDESCRIZIONE,  sINDIRIZZO,  sCAP,  sDESC_COMUNE};
    
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS,	asSYMBOLICS);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    oTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int iRow, int iCol) {
        super.getTableCellRendererComponent(table,value, isSelected, hasFocus, iRow, iCol);
        Map<String, Object> oRecord = oRecords.get(iRow);
        boolean boAttivo = WUtil.toBoolean(oRecord.get(sATTIVO), true);
        if(boAttivo) {
          this.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
          this.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
        }
        else {
          this.setForeground(Color.gray);
          this.setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
        }
        return this;
      }
    });
    
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
  
  public
  void fireSelect()
      throws Exception
  {
    if(dpLuoghi != null && btnSelect != null && btnSelect.isEnabled()) {
      onChoiceMade();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FormPanel fpFilter = (FormPanel) getFilterContainer();
          fpFilter.setValue(sRICERCA, "");
          fpFilter.requestFocus(sRICERCA);
        }
      });
    }
    else {
      super.fireSelect();
    }
  }
  
  protected
  void onChoiceMade()
  {
    setChoice(oLastRecordReaded);
    if(dpLuoghi != null) dpLuoghi.addRecord(oLastRecordReaded);
  }
  
  protected
  void setFilterValues(Object oValues)
      throws Exception
  {
    if(oValues instanceof Map) {
      FormPanel fpFilter = (FormPanel) getFilterContainer();
      fpFilter.setValues(WUtil.toMapObject(oValues));
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
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("LUOGHI.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 luogo trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " luoghi trovati.");
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
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("LUOGHI.read", parameters));
    
    AppUtil.denormalizeText(mapRead);
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
    
    fpDetail.getComponent(sDESCRIZIONE).requestFocus();
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
    AppUtil.normalizeText(oNormalizedValues);
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oNormalizedValues);
    
    if(boNew) {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("LUOGHI.insert", parameters));
      
      fpDetail.setValues(mapResult);
      oRecords.add(mapResult);
      iRowToSelect = oRecords.size() - 1;
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("LUOGHI.update", parameters));
      
      mapResult.put(sATTIVO, oLastRecordReaded.get(sATTIVO));
      
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
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("LUOGHI.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Luogo non eliminabile.");
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
    Object oId  = oRecord.get(sID);
    Boolean oAttivo = WUtil.toBooleanObj(oRecord.get(sATTIVO), true);
    boolean boNuovoStato = false;
    if(oAttivo != null) boNuovoStato = !oAttivo.booleanValue();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    parameters.add(new Boolean(boNuovoStato));
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("LUOGHI.setEnabled", parameters), oAttivo);
    
    oRecord.put(sATTIVO, oResult);
    oLastRecordReaded.put(sATTIVO, oResult);
  }
  
  protected
  void checkActions(List<JButton> listDefActions, boolean boAllowEditing)
  {
    removeButtonByActionCommand(listDefActions, sACTION_PRINT);
    
    if(boCanSelect) {
      btnSelect = GUIUtil.buildActionButton(IConstants.sGUIDATA_SELECT, sACTION_SELECT);
      btnSelect.setEnabled(false);
      btnSelect.addActionListener(this);
      listDefActions.add(0, btnSelect);
    }
    
    btnGeocoding = GUIUtil.buildActionButton("Geocoding|Geocoding|" + MenuManager.sICON_LUOGHI, "geocoding");
    btnGeocoding.setEnabled(false);
    btnGeocoding.addActionListener(this);
    listDefActions.add(btnGeocoding);
    iMaxRowsActions++;
    
    btnGeoPaste = GUIUtil.buildActionButton(IConstants.sGUIDATA_PASTE, "geo_paste");
    btnGeoPaste.setEnabled(false);
    btnGeoPaste.addActionListener(this);
    listDefActions.add(btnGeoPaste);
    iMaxRowsActions++;
  }
  
  protected 
  void onChangeEditorStatus(int iStatus)
  {
    btnGeocoding.setEnabled(iStatus != iSTATUS_STARTUP);
    btnGeoPaste.setEnabled(iStatus == iSTATUS_EDITING);
  }
}
