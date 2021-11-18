package org.dew.cms.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.ITag;

import org.dew.cms.gui.util.BooleanRenderer;
import org.dew.cms.gui.util.AppUtil;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ADataPanel;
import org.dew.swingup.util.ATableModelForSorter;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;
import org.dew.util.WUtil;

public
class GUITag extends AEntityEditor implements ITag
{
  private static final long serialVersionUID = -4776035700771920743L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  
  protected DPTag dpTag;
  protected Color colorChangeGruoup = new Color(200, 255, 200);
  protected boolean boEscludiTagUsoInterno = false;
  
  public
  GUITag()
  {
    super();
  }
  
  public
  GUITag(boolean boEscludiTagUsoInterno)
  {
    super();
    this.boEscludiTagUsoInterno = boEscludiTagUsoInterno; 
  }
  
  public
  void setDataPanel(DPTag dpTag)
  {
    this.dpTag = dpTag;
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
    Map<String, Object> mapDefatulValues = new HashMap<String, Object>();
    mapDefatulValues.put(sESCL_USO_INT, new Boolean(boEscludiTagUsoInterno));
    
    FormPanel fp = new FormPanel("Ricerca");
    fp.addTab("Tag");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 50);
    fp.addHiddenField(sESCL_USO_INT);
    fp.build();
    
    fp.setDefaultValues(mapDefatulValues);
    
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
    ADataPanel dpDescrizioni = new DPDescrizioni();
    dpDescrizioni.setPreferredSize(new Dimension(0, 140));
    
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Tag");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 50);
    fp.addBooleanField(sANTEPRIMA, "Vis. anteprima");
    fp.addIntegerField(sORDINE, "Ordine");
    fp.addRow();
    fp.addDataPanel(sDESCRIZIONE, "Descrizioni", dpDescrizioni);
    fp.addHiddenField(sID);
    fp.build();
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sCODICE);
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
    String[] asCOLUMNS    = {"Codice",     "Visibile in anteprima", "Ordine"};
    String[] asSYMBOLICS  = {sCODICE,      sANTEPRIMA,              sORDINE};
    Class<?>[]  asCLASSES = {String.class, Boolean.class,           String.class};
    
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS, asCLASSES);
    
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
        boolean boGruppo = WUtil.toBoolean(oRecord.get(sGRUPPO), true);
        if(boAttivo) {
          this.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
          this.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
          if(boGruppo) {
            this.setBackground(isSelected ? table.getSelectionBackground() : colorChangeGruoup);
          }
          else {
            this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
          }
        }
        else {
          this.setForeground(Color.gray);
          this.setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
          if(boGruppo) {
            this.setBackground(isSelected ? table.getSelectionBackground() : colorChangeGruoup);
          }
          else {
            this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
          }
        }
        return this;
      }
    });
    oTable.setDefaultRenderer(Boolean.class, new BooleanRenderer() {
      private static final long serialVersionUID = 1L;
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int iRow, int iCol) {
        super.getTableCellRendererComponent(table,value, isSelected, hasFocus, iRow, iCol);
        Map<String, Object> oRecord = oRecords.get(iRow);
        boolean boGruppo = WUtil.toBoolean(oRecord.get(sGRUPPO), true);
        if(boGruppo) {
          this.setBackground(isSelected ? table.getSelectionBackground() : colorChangeGruoup);
        }
        else {
          this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
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
    if(dpTag != null && btnSelect != null && btnSelect.isEnabled()) {
      onChoiceMade();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FormPanel fpFilter = (FormPanel) getFilterContainer();
          fpFilter.setValue(sCODICE, "");
          fpFilter.requestFocus(sCODICE);
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
    if(dpTag != null) dpTag.addRecord(oLastRecordReaded);
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
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("TAG.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 tag trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " tag trovati.");
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
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("TAG.read", parameters));
    
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
      Boolean oExists = WUtil.toBooleanObj(oRPCClient.execute("TAG.exists", parameters), false);
      if(oExists != null && oExists.booleanValue()) {
        GUIMessage.showWarning("Codice tag gi\340 utilizzato.");
        return false;
      }
      
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("TAG.insert", parameters));
      
      fpDetail.setValues(mapResult);
      oRecords.add(mapResult);
      iRowToSelect = oRecords.size() - 1;
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("TAG.update", parameters));
      
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
    
    int iCount = WUtil.toInt(oRPCClient.execute("TAG.countArticles", parameters), 0);
    if(iCount > 0) {
      boolean boConfirm = GUIMessage.getConfirmation("Vi sono " + iCount + " articoli a cui \350 stato associato il tag. Si vuole proseguire con la cancellazione?");
      if(!boConfirm) return;
    }
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("TAG.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Tag non eliminabile.");
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
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("TAG.setEnabled", parameters), oAttivo);
    
    oRecord.put(sATTIVO, oResult);
    oLastRecordReaded.put(sATTIVO, oResult);
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
  }
}
