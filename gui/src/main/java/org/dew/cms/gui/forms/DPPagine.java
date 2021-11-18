package org.dew.cms.gui.forms;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.dew.cms.common.IPagina;
import org.dew.cms.gui.MenuManager;
import org.dew.swingup.*;
import org.dew.swingup.editors.*;
import org.dew.swingup.util.*;
import org.dew.util.WUtil;

public
class DPPagine extends ADataPanel implements IPagina, ActionListener, ListSelectionListener
{
  private static final long serialVersionUID = 1669762621491776097L;
  
  protected SimpleTableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords;
  
  protected JButton btnAdd;
  protected JButton btnRemove;
  protected JButton btnMoveUp;
  protected JButton btnMoveDown;
  
  public
  void setEnabled(boolean boEnabled)
  {
    super.setEnabled(boEnabled);
    oTable.setEnabled(boEnabled);
    btnAdd.setEnabled(boEnabled);
    btnRemove.setEnabled(false);
    btnMoveUp.setEnabled(false);
    btnMoveDown.setEnabled(false);
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
      if(sActionCommand.equals("add"))            addItem();
      else if(sActionCommand.equals("remove"))    removeItem(); 
      else if(sActionCommand.equals("move_up"))   moveUp();
      else if(sActionCommand.equals("move_down")) moveDown();
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
      btnMoveUp.setEnabled(false);
      btnMoveDown.setEnabled(false);
      return;
    }
    btnRemove.setEnabled(true);
    btnMoveUp.setEnabled(true);
    btnMoveDown.setEnabled(true);
  }
  
  protected
  void addRecord(Map<String, Object> oRecord)
  {
    if(oRecord == null || oRecord.isEmpty()) return;
    Object id = oRecord.get(sID);
    if(id == null) return;
    for(int i = 0; i < oRecords.size(); i++) {
      Map<String, Object> oRecord_i = oRecords.get(i);
      Object id_i = oRecord_i.get(sID);
      if(id.equals(id_i)) {
        GUIMessage.showWarning("Pagina gi\340 presente nella lista");
        return;
      }
    }
    oRecords.add(oRecord);
    oTableModel.notifyUpdates();
  }
  
  protected
  void addItem()
      throws Exception
  {
    GUIPagine guiPagine = new GUIPagine(true);
    guiPagine.setDataPanel(this);
    
    EntityDialog entityDialog = new EntityDialog();
    entityDialog.init(guiPagine, "Pagine", MenuManager.sICON_PAGINE, true);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    entityDialog.setSize(900, 700);
    entityDialog.setLocation(size.width/2 - entityDialog.getSize().width/2, size.height/2 - entityDialog.getSize().height/2);
    entityDialog.setVisible(true);
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
  void moveUp()
  {
    int iRow = oTable.getSelectedRow();
    if(iRow <= 0 || iRow >= oRecords.size()) return;
    
    Map<String, Object> oRow_1 = oRecords.get(iRow - 1);
    Map<String, Object> oRow   = oRecords.get(iRow);
    oRecords.set(iRow - 1, oRow);
    oRecords.set(iRow,   oRow_1);
    
    oTableModel.notifyUpdates();
    
    oTable.getSelectionModel().setSelectionInterval(iRow-1, iRow-1);
  }
  
  protected
  void moveDown()
  {
    int iRow = oTable.getSelectedRow();
    if(iRow < 0 || iRow >= oRecords.size() - 1) return;
    
    Map<String, Object> oRow_1 = oRecords.get(iRow + 1);
    Map<String, Object> oRow   = oRecords.get(iRow);
    oRecords.set(iRow + 1, oRow);
    oRecords.set(iRow,   oRow_1);
    
    oTableModel.notifyUpdates(); 
    
    oTable.getSelectionModel().setSelectionInterval(iRow+1, iRow+1);
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
    String[] asCOLUMNS   = {"Codice"};
    String[] asSYMBOLICS = {sCODICE};
    
    oRecords = new ArrayList<Map<String, Object>>();
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    oTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int iRow, int iCol) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, iRow, iCol);
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
    
    JScrollPane oScrollPane = new JScrollPane(oTable);
    TableColumnResizer.setResizeColumnsListeners(oTable);
    TableSorter.setSorterListener(oTable);
    
    oTable.getSelectionModel().addListSelectionListener(this);
    
    return oScrollPane;
  }
  
  protected
  Container buildButtonsPanel()
  {
    JPanel oButtonsPanel = new JPanel(new GridLayout(4, 1));
    btnAdd = GUIUtil.buildActionButton(IConstants.sGUIDATA_PLUS, "add");
    btnAdd.addActionListener(this);
    oButtonsPanel.add(btnAdd);
    btnRemove = GUIUtil.buildActionButton(IConstants.sGUIDATA_MINUS, "remove");
    btnRemove.setEnabled(false);
    btnRemove.addActionListener(this);
    oButtonsPanel.add(btnRemove);
    btnMoveUp = GUIUtil.buildActionButton("&Sposta Su|Sposta in alto|UpLarge.gif", "move_up");
    btnMoveUp.setEnabled(false);
    btnMoveUp.addActionListener(this);
    oButtonsPanel.add(btnMoveUp);
    btnMoveDown = GUIUtil.buildActionButton("Sposta &Gi\371|Sposta in basso|DownLarge.gif", "move_down");
    btnMoveDown.setEnabled(false);
    btnMoveDown.addActionListener(this);
    oButtonsPanel.add(btnMoveDown);
    
    JPanel oResult = new JPanel(new BorderLayout());
    oResult.add(oButtonsPanel, BorderLayout.NORTH);
    return oResult;
  }
}

