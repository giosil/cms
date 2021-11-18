package org.dew.cms.gui.forms;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dew.cms.common.IArticolo;

import org.dew.cms.gui.dialogs.DialogPrezzo;
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
class DPPrezzi extends ADataPanel implements IArticolo, ActionListener, ListSelectionListener
{
  private static final long serialVersionUID = -3955750490567128949L;
  
  protected SimpleTableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords;
  
  protected JButton btnAdd;
  protected JButton btnModify;
  protected JButton btnRemove;
  protected JButton btnMoveUp;
  protected JButton btnMoveDown;
  
  public
  void setEnabled(boolean boEnabled)
  {
    super.setEnabled(boEnabled);
    oTable.setEnabled(boEnabled);
    btnAdd.setEnabled(boEnabled);
    btnModify.setEnabled(false);
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
      else if(sActionCommand.equals("modify"))    modifyItem();
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
      btnModify.setEnabled(false);
      btnRemove.setEnabled(false);
      btnMoveUp.setEnabled(false);
      btnMoveDown.setEnabled(false);
      return;
    }
    btnModify.setEnabled(true);
    btnRemove.setEnabled(true);
    btnMoveUp.setEnabled(true);
    btnMoveDown.setEnabled(true);
  }
  
  protected
  void addItem()
      throws Exception
  {
    Map<String, Object> mapPrezzo = DialogPrezzo.showMe(null);
    if(mapPrezzo == null || mapPrezzo.isEmpty()) {
      return;
    }
    oRecords.add(mapPrezzo);
    oTableModel.notifyUpdates();
  }
  
  protected
  void removeItem()
  {
    int[] aiRows = oTable.getSelectedRows();
    if(aiRows.length == 0) return;
    List<Map<String, Object>> oRecordsToRemove = new ArrayList<Map<String, Object>>();
    for(int i = 0; i < aiRows.length; i++) {
      oRecordsToRemove.add(oRecords.get(aiRows[i]));
    }
    for(int i = 0; i < oRecordsToRemove.size(); i++) {
      oRecords.remove(oRecordsToRemove.get(i));
    }
    oTableModel.notifyUpdates();
  }
  
  protected
  void modifyItem()
  {
    int[] aiRows = oTable.getSelectedRows();
    if(aiRows.length == 0) return;
    if(aiRows.length > 1) {
      GUIMessage.showWarning("Per la modifica del prezzo selezionare un record alla volta.");
      return;
    }
    Map<String, Object> mapRecord = oRecords.get(aiRows[0]);
    Map<String, Object> mapPrezzo = DialogPrezzo.showMe(mapRecord);
    if(mapPrezzo == null || mapPrezzo.isEmpty()) {
      return;
    }
    oRecords.set(aiRows[0], mapPrezzo);
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
    String[] asCOLUMNS    = {"Codice",       "Descrizione", "Prezzo",       "Sconto",       "Prezzo Scontato", "Acconto",       "Promozione"  , ""};
    String[] asSYMBOLICS  = {sPREZZO_CODICE, sPREZZO_DESCR, sPREZZO_PREZZO, sPREZZO_SCONTO, sPREZZO_SCONTATO,  sPREZZO_ACCONTO, sPREZZO_PROMOZ, ""};
    Class<?>[]  acCLASSES = {String.class,   String.class,  Double.class,   Integer.class,  Double.class,      Double.class,    Boolean.class,  String.class};
    
    oRecords    = new ArrayList<Map<String, Object>>();
    oTableModel = new SimpleTableModelForSorter(oRecords, asCOLUMNS, asSYMBOLICS, acCLASSES);
    
    oTable = new JTable(oTableModel);
    oTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    oTable.setColumnSelectionAllowed(false);
    oTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
    TableUtils.setMonospacedFont(oTable);
    
    JScrollPane oScrollPane = new JScrollPane(oTable);
    TableColumnResizer.setResizeColumnsListeners(oTable);
    TableSorter.setSorterListener(oTable);
    
    oTable.getSelectionModel().addListSelectionListener(this);
    
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
    
    return oScrollPane;
  }
  
  protected
  Container buildButtonsPanel()
  {
    JPanel oButtonsPanel = new JPanel(new GridLayout(5, 1));
    btnAdd = GUIUtil.buildActionButton(IConstants.sGUIDATA_PLUS, "add");
    btnAdd.addActionListener(this);
    oButtonsPanel.add(btnAdd);
    btnModify = GUIUtil.buildActionButton(IConstants.sGUIDATA_OPEN, "modify");
    btnModify.setEnabled(false);
    btnModify.addActionListener(this);
    oButtonsPanel.add(btnModify);
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

