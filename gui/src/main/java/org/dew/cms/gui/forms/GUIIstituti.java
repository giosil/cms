package org.dew.cms.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.IIstituto;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ATableModelForSorter;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;

import org.dew.util.WUtil;

public
class GUIIstituti extends AEntityEditor implements IIstituto
{
  private static final long serialVersionUID = 2786682970196922377L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  
  public
  GUIIstituti()
  {
    super();
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
    fp.addTab("Istituto");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 10);
    fp.addRow();
    fp.addTextField(sDESCRIZIONE, "Descrizione", 50);
    fp.build();
    
    fp.setCase(sDESCRIZIONE, 0);
    
    return fp;
  }
  
  protected
  Container buildGUIDetail()
  {
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Istituto");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 10);
    fp.addRow();
    fp.addTextField(sDESCRIZIONE, "Descrizione", 50);
    fp.addHiddenField(sID);
    fp.build();
    
    fp.setCase(sDESCRIZIONE, 0);
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sCODICE);
    listMandatoryFields.add(sDESCRIZIONE);
    fp.setMandatoryFields(listMandatoryFields);
    
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
    String[] asCOLUMNS   = {"Codice", "Descrizione"};
    String[] asSYMBOLICS = {sCODICE,  sDESCRIZIONE};
    
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS);
    
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
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("ISTITUTI.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 istituto trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " istituti trovati.");
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
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("ISTITUTI.read", parameters));
    
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
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oNormalizedValues);
    
    if(boNew) {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("ISTITUTI.insert", parameters));
      
      fpDetail.setValues(mapResult);
      oRecords.add(mapResult);
      iRowToSelect = oRecords.size() - 1;
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("ISTITUTI.update", parameters));
      
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
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ISTITUTI.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Istituto non eliminabile.");
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
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ISTITUTI.setEnabled", parameters), oAttivo);
    
    oRecord.put(sATTIVO, oResult);
    oLastRecordReaded.put(sATTIVO, oResult);
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
  }
}

