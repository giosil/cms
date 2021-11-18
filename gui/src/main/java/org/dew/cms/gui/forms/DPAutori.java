package org.dew.cms.gui.forms;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dew.cms.common.IAutore;

import org.dew.cms.gui.MenuManager;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.editors.EntityDialog;
import org.dew.swingup.util.ADataPanel;
import org.dew.swingup.util.CodeAndDescription;
import org.dew.swingup.util.GUIUtil;
import org.dew.swingup.util.OptionsCellEditor;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;

import org.dew.util.WUtil;

public
class DPAutori extends ADataPanel implements IAutore, ActionListener, ListSelectionListener
{
  private static final long serialVersionUID = 6075758113854924485L;
  
  protected SimpleTableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords;
  
  protected JButton btnAdd;
  protected JButton btnRemove;
  protected Vector<String> vDescRuoli;
  protected Map<String, Object> mapDescRuoliId;
  
  public
  void setEnabled(boolean boEnabled)
  {
    super.setEnabled(boEnabled);
    oTable.setEnabled(boEnabled);
    btnAdd.setEnabled(boEnabled);
    btnRemove.setEnabled(false);
  }
  
  public
  void setData(Object oData)
  {
    oRecords = new ArrayList<Map<String, Object>>();
    if(oData instanceof List) {
      oRecords.addAll(WUtil.toListOfMapObject(oData));
    }
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
  }
  
  public
  Object getData()
  {
    return oRecords;
  }
  
  public
  void actionPerformed(ActionEvent e)
  {
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null) return;
    try {
      if(sActionCommand.equals("add"))         addItem();
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
      btnRemove.setEnabled(false);
      return;
    }
    btnRemove.setEnabled(true);
  }
  
  protected
  void addItem()
      throws Exception
  {
    AEntityEditor aEntityEditor = new GUIAutori(true);
    
    EntityDialog entityDialog = new EntityDialog();
    entityDialog.init(aEntityEditor, "Autori", MenuManager.sICON_AUTORI);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    entityDialog.setSize(850, 650);
    entityDialog.setLocation(size.width/2 - entityDialog.getSize().width/2, size.height/2 - entityDialog.getSize().height/2);
    entityDialog.setVisible(true);
    
    Object oChoice = entityDialog.getChoice();
    if(oChoice instanceof List) {
      List<Map<String, Object>> listChoice = WUtil.toListOfMapObject(oChoice);
      for(int i = 0; i < listChoice.size(); i++) {
        Map<String, Object> oRecord = listChoice.get(i);
        if(oRecords.contains(oRecord)) continue;
        oRecords.add(oRecord);
      }
      oTableModel.notifyUpdates();
    }
    else if(oChoice instanceof Map) {
      Map<String, Object> mapChoice = WUtil.toMapObject(oChoice);
      if(oRecords.contains(mapChoice)) {
        GUIMessage.showWarning("Autore gi\340 presente nella lista");
        return;
      }
      oRecords.add(mapChoice);
      oTableModel.notifyUpdates();
    }
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
    String[] asCOLUMNS   = {"Ruolo",     "Cognome", "Nome", "Tipo",     "Email"};
    String[] asSYMBOLICS = {sDESC_RUOLO, sCOGNOME,  sNOME,  sDESC_TIPO, sEMAIL };
    String[] asEDITABLE_SYMBOLICS = {sDESC_RUOLO};
    
    oRecords = new ArrayList<Map<String, Object>>();
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS) {
      private static final long serialVersionUID = 1L;
      public void setValueAt(Object oVal, int iRow, int iCol) {
        super.setValueAt(oVal, iRow, iCol);
        if(iCol != 0) return;
        String sDescRuolo = WUtil.toString(oVal, null);
        if(sDescRuolo != null && sDescRuolo.length() > 0) {
          Object oIdRuolo = mapDescRuoliId.get(sDescRuolo);
          if(oIdRuolo == null) oIdRuolo = new Integer(0);
          Map<String, Object> mapRecord = oRecords.get(iRow);
          mapRecord.put(sID_RUOLO, oIdRuolo);
        }
      }
    };
    oTableModel.setEditableColumns(asEDITABLE_SYMBOLICS);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    oTable.setRowHeight(20);
    TableUtils.setLinkField(oTable, 4);
    
    Vector<CodeAndDescription> vRuoli = Opzioni.getRuoli(true);
    vDescRuoli     = new Vector<String>(vRuoli.size());
    mapDescRuoliId = new HashMap<String, Object>();
    for(int i = 0; i < vRuoli.size(); i++) {
      CodeAndDescription cdRuolo = (CodeAndDescription) vRuoli.get(i);
      String sDescription = cdRuolo.getDescription();
      vDescRuoli.add(sDescription);
      mapDescRuoliId.put(sDescription, cdRuolo.getCode());
    }
    oTable.setDefaultEditor(String.class, new OptionsCellEditor(vDescRuoli));
    
    TableUtils.setMonospacedFont(oTable);
    
    JScrollPane oScrollPane = new JScrollPane(oTable);
    TableSorter.setSorterListener(oTable);
    
    oTable.getColumnModel().getColumn(0).setPreferredWidth(110);
    oTable.getColumnModel().getColumn(1).setPreferredWidth(135);
    oTable.getColumnModel().getColumn(2).setPreferredWidth(135);
    oTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    oTable.getColumnModel().getColumn(4).setPreferredWidth(140);
    
    oTable.getSelectionModel().addListSelectionListener(this);
    
    return oScrollPane;
  }
  
  protected
  Container buildButtonsPanel()
  {
    JPanel oButtonsPanel = new JPanel(new GridLayout(2, 1));
    btnAdd = GUIUtil.buildActionButton(IConstants.sGUIDATA_PLUS, "add");
    btnAdd.addActionListener(this);
    oButtonsPanel.add(btnAdd);
    btnRemove = GUIUtil.buildActionButton(IConstants.sGUIDATA_MINUS, "remove");
    btnRemove.setEnabled(false);
    btnRemove.addActionListener(this);
    oButtonsPanel.add(btnRemove);
    
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(oButtonsPanel, BorderLayout.NORTH);
    return oResult;
  }
}
