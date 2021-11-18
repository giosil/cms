package org.dew.cms.gui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dew.cms.common.IPagina;

import org.dew.cms.gui.util.AppUtil;
import org.dew.cms.gui.util.Opzioni;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.IConstants;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ADataPanel;
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
class GUIPagine extends AEntityEditor implements IPagina
{
  private static final long serialVersionUID = 2534830586551057699L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  protected JTabbedPane jtpResult;
  protected Object oIdSelectedOnTree;
  protected JTree  oTree;
  
  protected DPPagine dpPagine;
  
  protected boolean boCanSelect = false;
  
  public
  GUIPagine()
  {
    super();
  }
  
  public
  GUIPagine(boolean boCanSelect)
  {
    super();
    this.boCanSelect = boCanSelect;
  }
  
  public
  void setDataPanel(DPPagine dpPagine)
  {
    this.dpPagine = dpPagine;
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
    fp.addTab("Pagina");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 50);
    fp.addOptionsField(sID_TIPO_PAG,  "Tipo Pag.", Opzioni.getTipiPagina(true));
    fp.build();
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
    
    ADataPanel dpArticoli = new DPArticoli();
    dpArticoli.setPreferredSize(new Dimension(0, 140));
    
    ADataPanel dpPagine = new DPPagine();
    dpPagine.setPreferredSize(new Dimension(0, 140));
    
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Pagina");
    fp.addRow();
    fp.addTextField(sCODICE, "Codice", 50);
    fp.addOptionsField(sID_TIPO_PAG,  "Tipo Pag.", Opzioni.getTipiPagina(true));
    fp.addIntegerField(sORDINE, "Ordine");
    fp.addRow();
    fp.addDataPanel(sDESCRIZIONE, "Descrizioni",  dpDescrizioni);
    fp.addTab("Configurazione");
    fp.addRow();
    fp.addOptionsField(sID_CATEGORIA,  "Categoria",   Opzioni.getCategorie(true));
    fp.addBlankField();
    fp.addRow();
    fp.addOptionsField(sID_SOTTOCATEG, "Sottocateg.", new Vector<CodeAndDescription>());
    fp.addBlankField();
    fp.addRow();
    fp.addOptionsField(sID_TIPO_ART,   "Tipologia",   Opzioni.getTipologie(true));
    fp.addBlankField();
    fp.addRow();
    fp.addIntegerField(sRIGHE,   "Righe");
    fp.addRow();
    fp.addIntegerField(sCOLONNE, "Colonne");
    fp.addRow();
    fp.addOptionsField(sVISTA,   "Vista",   Opzioni.getViste(true));
    fp.addBlankField();
    fp.addTab("Articoli");
    fp.addRow();
    fp.addDataPanel(sARTICOLI, dpArticoli);
    fp.addTab("Componenti");
    fp.addRow();
    fp.addDataPanel(sCOMPONENTI, dpPagine);
    fp.addHiddenField(sID);
    fp.build();
    
    JComboBox<?> jcbCategorie = (JComboBox<?>) fp.getComponent(sID_CATEGORIA);
    jcbCategorie.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FormPanel fpDetail = (FormPanel) getDetailContainer();
        Object oCategoria  = fpDetail.getValue(sID_CATEGORIA);
        Vector<CodeAndDescription> vSottoCateg = Opzioni.getSottoCategorie(oCategoria, true);
        fpDetail.setOptionsItems(sID_SOTTOCATEG, vSottoCateg);
      }
    });
    
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
    jtpResult = new JTabbedPane();
    jtpResult.addTab("Ricerca", buildGUIResultTable());
    jtpResult.addTab("Mappa",   buildGUIResultTree());
    jtpResult.setIconAt(0, ResourcesMgr.getImageIcon("SheetLarge.gif"));
    jtpResult.setIconAt(1, ResourcesMgr.getImageIcon("DocumentDiagramLarge.gif"));
    jtpResult.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int iSelectedIndex = jtpResult.getSelectedIndex();
        if(iSelectedIndex == 0) {
          if(oRecords != null && oRecords.size() > 0) {
            try{ fireFind(); } catch(Exception ex) {}
          }
        }
        else 
          if(iSelectedIndex == 1) {
            reloadTree();
          }
      }
    });
    return jtpResult;
  }
  
  protected
  Container buildGUIResultTree()
  {
    DefaultMutableTreeNode dmtRoot = new DefaultMutableTreeNode("Struttura", true);
    DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode("Tipologia", false);
    dmtRoot.add(dmtn);
    
    oTree = new JTree(dmtRoot);
    oTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) oTree.getLastSelectedPathComponent();
        oIdSelectedOnTree = null;
        if(dmtn != null) {
          int iLevel = dmtn.getLevel();
          if(iLevel > 1) {
            Object oUserObject = dmtn.getUserObject();
            if(oUserObject instanceof CodeAndDescription) {
              oIdSelectedOnTree = ((CodeAndDescription) oUserObject).getCode();
            }
            else
              if(oUserObject != null) {
                String sUserObject = oUserObject.toString();
                int iStartId = sUserObject.indexOf('[');
                if(iStartId >= 0) {
                  int iEndId = sUserObject.indexOf(']', iStartId);
                  if(iEndId > 1) {
                    String sId = sUserObject.substring(iStartId+1, iEndId);
                    try { oIdSelectedOnTree = new Integer(sId); } catch(Throwable th) {}
                  }
                }
              }
          }
        }
        if(oIdSelectedOnTree == null) {
          FormPanel fpDetail = (FormPanel) getDetailContainer();
          fpDetail.reset();
          onLostSelection();
        }
        else {
          ListSelectionEvent lse = new ListSelectionEvent(oTree, 0, 0, false);
          GUIPagine.this.valueChanged(lse);
        }
      }
    });
    return new JScrollPane(oTree);
  }
  
  protected
  Container buildGUIResultTable()
  {
    String[] asCOLUMNS   = {"Tipo",         "Codice"};
    String[] asSYMBOLICS = {sDESC_TIPO_PAG, sCODICE};
    
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
    if(dpPagine != null && btnSelect != null && btnSelect.isEnabled()) {
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
    if(dpPagine != null) dpPagine.addRecord(oLastRecordReaded);
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
    jtpResult.setSelectedIndex(0);
    
    FormPanel fpFilter = (FormPanel) getFilterContainer();
    Map<String, Object> oFilterValues = fpFilter.getValues();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(DataNormalizer.normalize(oFilterValues));
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("PAGINE.find", parameters, true));
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 pagina trovata.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " pagine trovate.");
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
    
    jtpResult.setSelectedIndex(0);
  }
  
  protected
  boolean onSelection()
      throws Exception
  {
    Object oId = null;
    if(oIdSelectedOnTree != null) {
      oId = oIdSelectedOnTree;
      oIdSelectedOnTree = null;
    }
    else {
      int iRow = oTable.getSelectedRow();
      if(iRow < 0 || iRow >= oRecords.size()) return false;
      Map<String, Object> oRecord = oRecords.get(iRow);
      oId  = oRecord.get(sID);
    }
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("PAGINE.read", parameters));
    
    AppUtil.denormalizeText(mapRead);
    oLastRecordReaded = new HashMap<String, Object>(mapRead);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    fpDetail.setValues(mapRead);
    
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
    oTree.setEnabled(false);
  }
  
  protected
  void doOpen()
      throws Exception
  {
    oTable.setEnabled(false);
    oTree.setEnabled(false);
    
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
    
    int iTabSelected = jtpResult.getSelectedIndex();
    int iRowToSelect = -1;
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    Map<String, Object> oDetailValues = fpDetail.getValues();
    Map<String, Object> oNormalizedValues = WUtil.toMapObject(DataNormalizer.normalize(oDetailValues));
    AppUtil.normalizeText(oNormalizedValues);
    
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oNormalizedValues);
    if(boNew) {
      Boolean oExists = WUtil.toBooleanObj(oRPCClient.execute("PAGINE.exists", parameters), false);
      if(oExists != null && oExists.booleanValue()) {
        GUIMessage.showWarning("Codice pagina gi\340 utilizzato.");
        return false;
      }
      
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("PAGINE.insert", parameters));
      
      fpDetail.setValues(mapResult);
      if(iTabSelected == 0) {
        oRecords.add(mapResult);
        iRowToSelect = oRecords.size() - 1;
      }
    }
    else {
      Map<String, Object> mapResult = WUtil.toMapObject(oRPCClient.execute("PAGINE.update", parameters));
      
      mapResult.put(sATTIVO, oLastRecordReaded.get(sATTIVO));
      
      fpDetail.setValues(mapResult);
      if(iTabSelected == 0) {
        int iRow = oTable.getSelectedRow();
        if(iRow >= 0) {
          oRecords.set(iRow, mapResult);
          iRowToSelect = iRow;
        }
      }
    }
    
    oTable.setEnabled(true);
    oTree.setEnabled(true);
    if(iTabSelected == 0) {
      TableSorter.resetHeader(oTable);
      oTableModel.notifyUpdates();
      if(iRowToSelect >= 0) {
        oTable.setRowSelectionInterval(iRowToSelect, iRowToSelect);
      }
    }
    else {
      reloadTree();
    }
    return true;
  }
  
  protected
  void doCancel()
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    int iTabSelected = jtpResult.getSelectedIndex();
    if(iTabSelected == 0) {
      int iRow = oTable.getSelectedRow();
      if(iRow >= 0) {
        fpDetail.reset();
        fpDetail.setValues(oLastRecordReaded);
      }
    }
    else {
      if(oLastRecordReaded != null && !oLastRecordReaded.isEmpty()) {
        fpDetail.reset();
        fpDetail.setValues(oLastRecordReaded);
      }
    }
    oTable.setEnabled(true);
    oTree.setEnabled(true);
  }
  
  protected
  void doDelete()
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    Object oId = fpDetail.getValue(sID);
    if(oId == null) return;
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("PAGINE.delete", parameters), false);
    if(oResult == null || !oResult.booleanValue()) {
      GUIMessage.showWarning("Pagina non eliminabile.");
      return;
    }
    
    if(jtpResult.getSelectedIndex() == 0) {
      int iRow = oTable.getSelectedRow();
      if(iRow >= 0) {
        oRecords.remove(iRow);
        oTable.clearSelection();
        oTableModel.notifyUpdates();
      }
      fpDetail = (FormPanel) getDetailContainer();
      fpDetail.reset();
    }
    else {
      reloadTree();
    }
  }
  
  protected
  void doPrint()
      throws Exception
  {
  }
  
  protected
  boolean isElementEnabled()
  {
    if(jtpResult.getSelectedIndex() == 0) {
      int iRow = oTable.getSelectedRow();
      if(iRow < 0) return true;
      Map<String, Object> oRecord = oRecords.get(iRow);
      return WUtil.toBoolean(oRecord.get(sATTIVO), true);
    }
    else {
      return true;
    }
  }
  
  protected
  void doToggle()
      throws Exception
  {
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    Object oId = fpDetail.getValue(sID);
    if(oId == null) return;
    
    if(jtpResult.getSelectedIndex() == 0) {
      int iRow = oTable.getSelectedRow();
      if(iRow < 0) return;
      Map<String, Object> oRecord = oRecords.get(iRow);
      Boolean oAttivo = (Boolean) oRecord.get(sATTIVO);
      
      boolean boNuovoStato = false;
      if(oAttivo != null) boNuovoStato = !oAttivo.booleanValue();
      
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(oId);
      parameters.add(new Boolean(boNuovoStato));
      Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("PAGINE.setEnabled", parameters), oAttivo);
      
      oRecord.put(sATTIVO, oResult);
      oLastRecordReaded.put(sATTIVO, oResult);
    }
    else {
      IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
      List<Object> parameters = new ArrayList<Object>();
      parameters.add(oId);
      parameters.add(Boolean.FALSE);
      oRPCClient.execute("PAGINE.setEnabled", parameters);
      
      reloadTree();
    }
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
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
  }
  
  protected
  void reloadTree()
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        oTable.clearSelection();
        
        FormPanel fpDetail = (FormPanel) getDetailContainer();
        fpDetail = (FormPanel) getDetailContainer();
        fpDetail.reset();
        
        setEditorStatus(iSTATUS_STARTUP);
        
        try {
          IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
          List<Object> parameters = new ArrayList<Object>();
          Map<String, Object> mapPagine = WUtil.toMapObject(oRPCClient.execute("PAGINE.getMappa", parameters, true));
          
          DefaultMutableTreeNode root = new DefaultMutableTreeNode("Struttura");
          addLeaves(root, mapPagine);
          
          oTree.setModel(new DefaultTreeModel(root));
        }
        catch(Exception ex) {
          GUIMessage.showException("Errore nella lettura mappa pagine", ex);
        }
        
        for(int i= 0; i<oTree.getRowCount(); i++) oTree.expandRow(i);
      }
    });
  }
  
  protected static
  void addLeaves(DefaultMutableTreeNode oParent, Object oData)
  {
    if(oData == null) return;
    if(oData instanceof Map) {
      addMapLeaves(oParent, WUtil.toMapObject(oData));
    }
    else
      if(oData instanceof List) {
        addListLeaves(oParent, (List<?>) oData);
      }
      else {
        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(normalizeNodeData(oData));
        dmtn.setAllowsChildren(false);
        oParent.add(dmtn);
      }
  }
  
  protected static
  void addMapLeaves(DefaultMutableTreeNode oParent, Map<String, Object> map)
  {
    Object[] oKeys = map.keySet().toArray();
    Arrays.sort(oKeys);
    for(int i = 0; i < oKeys.length; i++) {
      Object oKey = oKeys[i];
      Object oValue = map.get(oKey);
      
      DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(normalizeNodeData(oKey));
      oParent.add(dmtn);
      addLeaves(dmtn, oValue);
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected static
  void addListLeaves(DefaultMutableTreeNode oParent, List list)
  {
    Collections.sort(list);
    for(int i = 0; i < list.size(); i++) {
      Object oValue = list.get(i);
      addLeaves(oParent, oValue);
    }
  }
  
  protected static
  Object normalizeNodeData(Object oData)
  {
    if(oData instanceof String) {
      String sData = (String) oData;
      int iSepOrd = sData.indexOf(')');
      if(iSepOrd > 0) sData = sData.substring(iSepOrd + 1).trim();
      int iStartId = sData.indexOf('[');
      if(iStartId >= 0) {
        int iEndId = sData.indexOf(']', iStartId);
        if(iEndId > 1) {
          String sId = sData.substring(iStartId+1, iEndId);
          try { 
            return new CodeAndDescription(new Integer(sId), sData.substring(iEndId + 1).trim());
          } 
          catch(Throwable th) {
          }
        }
      }
      return sData;
    }
    return oData;
  }
}