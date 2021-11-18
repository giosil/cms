package org.dew.cms.gui.forms;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dew.cms.gui.dialogs.DialogDescrizione;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.util.ADataPanel;
import org.dew.swingup.util.GUIUtil;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;
import org.dew.util.WUtil;

public
class DPDescrizioni extends ADataPanel implements ActionListener, ListSelectionListener
{
  private static final long serialVersionUID = -7894985489874146632L;
  
  protected SimpleTableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords;
  
  protected JButton btnAdd;
  protected JButton btnModify;
  protected JButton btnRemove;
  
  public
  void setEnabled(boolean boEnabled)
  {
    super.setEnabled(boEnabled);
    oTable.setEnabled(boEnabled);
    btnAdd.setEnabled(boEnabled);
    btnModify.setEnabled(false);
    btnRemove.setEnabled(false);
  }
  
  public
  void setData(Object oData)
  {
    oRecords = new ArrayList<Map<String, Object>>();
    if(oData instanceof List) {
      oRecords.addAll(WUtil.toListOfMapObject(oData));
    }
    else if(oData instanceof Map) {
      Map<?, ?> mapData = (Map<?, ?>) oData;
      
      List<Integer> listOfId  = new ArrayList<Integer>();
      Map<Integer, Object> mapIdKey   = new HashMap<Integer, Object>();
      Iterator<?> iterator = mapData.keySet().iterator();
      while(iterator.hasNext()) {
        Object oKey = iterator.next();
        Integer oId = WUtil.toInteger(oKey, 0);
        listOfId.add(oId);
        mapIdKey.put(oId, oKey);
      }
      
      Collections.sort(listOfId);
      
      for(int i = 0; i < listOfId.size(); i++) {
        Integer oId = listOfId.get(i);
        Object oKey = mapIdKey.get(oId);
        String sDescrizione = WUtil.toString(mapData.get(oKey), "");
        String sDescLingua  = Opzioni.getDescLingua(oId);
        
        Map<String, Object> mapRecord = new HashMap<String, Object>();
        mapRecord.put(DialogDescrizione.sID_LINGUA,   oId);
        mapRecord.put(DialogDescrizione.sDESC_LINGUA, sDescLingua);
        mapRecord.put(DialogDescrizione.sDESCRIZIONE, sDescrizione);
        
        oRecords.add(mapRecord);
      }
    }
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
  }
  
  public
  Object getData()
  {
    Map<String, Object> mapResult = new HashMap<String, Object>();
    if(oRecords == null || oRecords.size() == 0) return mapResult;
    for(int i = 0; i < oRecords.size(); i++) {
      Map<String, Object> mapRecord = oRecords.get(i);
      Object oIdLingua    = mapRecord.get(DialogDescrizione.sID_LINGUA);
      String sDescrizione = (String) mapRecord.get(DialogDescrizione.sDESCRIZIONE);
      if(oIdLingua == null) oIdLingua = new Integer("0");
      mapResult.put(oIdLingua.toString(), sDescrizione);
    }
    return mapResult;
  }
  
  public
  void actionPerformed(ActionEvent e)
  {
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null) return;
    try {
      if(sActionCommand.equals("add"))         addItem();
      else if(sActionCommand.equals("modify")) modifyItem();
      else if(sActionCommand.equals("remove")) removeItem();
    }
    catch (Exception ex) {
      GUIMessage.showException(ex);
    }
  }
  
  public
  void valueChanged(ListSelectionEvent e)
  {
    if(e.getValueIsAdjusting()) return;
    int iRow = oTable.getSelectedRow();
    if(iRow < 0 || iRow >= oRecords.size()) {
      btnModify.setEnabled(false);
      btnRemove.setEnabled(false);
      return;
    }
    btnModify.setEnabled(true);
    btnRemove.setEnabled(true);
  }
  
  protected
  void addItem()
      throws Exception
  {
    Map<String, Object> mapDescrizione = DialogDescrizione.showMe();
    if(mapDescrizione == null || mapDescrizione.isEmpty()) return;
    while(exists(mapDescrizione)) {
      GUIMessage.showWarning("La lingua specificata \350 stata gi\340 aggiunta.");
      mapDescrizione = DialogDescrizione.showMe(mapDescrizione, true);
      if(mapDescrizione == null || mapDescrizione.isEmpty()) return;
    }
    oRecords.add(mapDescrizione);
    oTableModel.notifyUpdates();
  }
  
  protected
  void modifyItem()
      throws Exception
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0 || iRow >= oRecords.size()) return;
    Map<String, Object> mapRecord      = oRecords.get(iRow);
    Map<String, Object> mapDescrizione = DialogDescrizione.showMe(mapRecord, false);
    if(mapDescrizione == null || mapDescrizione.isEmpty()) return;
    oRecords.set(iRow, mapDescrizione);
    oTableModel.notifyUpdates();
  }
  
  protected
  void removeItem()
  {
    int[] iRows = oTable.getSelectedRows();
    if(iRows.length == 0) return;
    List<Map<String, Object>> oRecordsToRemove = new ArrayList<Map<String, Object>>();
    for(int i = 0; i < iRows.length; i++) {
      oRecordsToRemove.add(oRecords.get(iRows[i]));
    }
    for(int i = 0; i < oRecordsToRemove.size(); i++) {
      oRecords.remove(oRecordsToRemove.get(i));
    }
    oTableModel.notifyUpdates();
  }
  
  protected
  boolean exists(Map<String, Object> mapDescrizione)
  {
    if(mapDescrizione == null || mapDescrizione.isEmpty()) return false;
    if(oRecords == null) return false;
    Object oIdLingua = mapDescrizione.get(DialogDescrizione.sID_LINGUA);
    if(oIdLingua == null) oIdLingua = new Integer(0);
    String sIdLingua = oIdLingua.toString();
    for(int i = 0; i < oRecords.size(); i++) {
      Map<String, Object> mapRecord = oRecords.get(i);
      Object oRecIdLingua = mapRecord.get(DialogDescrizione.sID_LINGUA);
      if(oRecIdLingua == null) oRecIdLingua = new Integer(0);
      String sRecIdLingua = oRecIdLingua.toString();
      if(sIdLingua.equals(sRecIdLingua)) return true;
    }
    return false;
  }
  
  protected
  Container buildGUI()
      throws Exception
  {
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(buildTablePanel(),   BorderLayout.CENTER);
    oResult.add(buildButtonsPanel(), BorderLayout.EAST);
    return oResult;
  }
  
  protected
  Container buildTablePanel()
  {
    String[] asCOLUMNS   = {"Lingua",                       "Descrizione"};
    String[] asSYMBOLICS = {DialogDescrizione.sDESC_LINGUA, DialogDescrizione.sDESCRIZIONE};
    
    oRecords = new ArrayList<Map<String, Object>>();
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    TableUtils.setMonospacedFont(oTable);
    
    oTable.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() >= 2 && !e.isControlDown()) {
          try {
            modifyItem();
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
  Container buildButtonsPanel()
  {
    JPanel oButtonsPanel = new JPanel(new GridLayout(3, 1));
    btnAdd = GUIUtil.buildActionButton(IConstants.sGUIDATA_PLUS, "add");
    btnAdd.addActionListener(this);
    oButtonsPanel.add(btnAdd);
    btnModify = GUIUtil.buildActionButton(IConstants.sGUIDATA_OPEN,  "modify");
    btnModify.setEnabled(false);
    btnModify.addActionListener(this);
    oButtonsPanel.add(btnModify);
    btnRemove = GUIUtil.buildActionButton(IConstants.sGUIDATA_MINUS, "remove");
    btnRemove.setEnabled(false);
    btnRemove.addActionListener(this);
    oButtonsPanel.add(btnRemove);
    
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(oButtonsPanel, BorderLayout.NORTH);
    return oResult;
  }
}
