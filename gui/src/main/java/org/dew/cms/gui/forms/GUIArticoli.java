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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.dew.cms.common.IArticolo;
import org.dew.cms.gui.util.AppUtil;
import org.dew.cms.gui.util.Opzioni;
import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.rpc.DataNormalizer;
import org.dew.swingup.rpc.IRPCClient;
import org.dew.swingup.util.ADataPanel;
import org.dew.swingup.util.ATableModelForSorter;
import org.dew.swingup.util.CodeAndDescription;
import org.dew.swingup.util.FormPanel;
import org.dew.swingup.util.SimpleTableModelForSorter;
import org.dew.swingup.util.TableColumnResizer;
import org.dew.swingup.util.TableSorter;
import org.dew.swingup.util.TableUtils;
import org.dew.util.WUtil;

public
class GUIArticoli extends AEntityEditor implements IArticolo
{
  private static final long serialVersionUID = -5274197056327426097L;
  
  protected ATableModelForSorter oTableModel;
  protected JTable oTable;
  protected List<Map<String, Object>> oRecords = new ArrayList<Map<String, Object>>();
  protected Map<String, Object> oLastRecordReaded;
  
  protected DPArticoli dpArticoli;
  
  public
  GUIArticoli()
  {
    super();
  }
  
  public
  void setDataPanel(DPArticoli dpArticoli)
  {
    this.dpArticoli = dpArticoli;
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
    fp.addTab("Articolo");
    fp.addRow();
    fp.addTextField(sKEYWORDS,         "Ricerca",     255);
    fp.addTextField(sDESCRIZIONE,      "Descrizione", 255);
    fp.addRow();
    fp.addDateField(sDATA_INIZIO,      "Data Inizio");
    fp.addDateField(sDATA_FINE,        "Data Fine");
    fp.addRow();
    fp.addOptionsField(sID_TIPO_UTE,   "Tipo Utente", Opzioni.getTipiUtente(true));
    fp.addOptionsField(sID_TIPO_ART,   "Tipologia",   Opzioni.getTipologie(true));
    fp.addRow();
    fp.addOptionsField(sID_CATEGORIA,  "Categoria",   Opzioni.getCategorie(true));
    fp.addOptionsField(sID_SOTTOCATEG, "S.Categ.",    new Vector<CodeAndDescription>());
    fp.addRow();
    fp.build();
    
    fp.setCase(sKEYWORDS, 0);
    
    JComboBox<?> jcbCategorie = (JComboBox<?>) fp.getComponent(sID_CATEGORIA);
    jcbCategorie.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FormPanel fpFilter = (FormPanel) getFilterContainer();
        Object oCategoria  = fpFilter.getValue(sID_CATEGORIA);
        Vector<CodeAndDescription> vSottoCateg = Opzioni.getSottoCategorie(oCategoria, true);
        fpFilter.setOptionsItems(sID_SOTTOCATEG, vSottoCateg);
      }
    });
    // Ricerca quando si preme invio sul campo
    KeyListener klFind = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) try { fireFind(); } catch(Exception ex) {}
      }
    };
    List<Component> listComponents = fp.getListComponents();
    for(int i = 0; i < listComponents.size(); i++) {
      Component oComponent = listComponents.get(i);
      if(oComponent instanceof JTextComponent) {
        ((JTextComponent) oComponent).addKeyListener(klFind);
      }
    }
    
    return fp;
  }
  
  protected
  Container buildGUIDetail()
  {
    ADataPanel dpAutori = new DPAutori();
    dpAutori.setPreferredSize(new Dimension(0, 120));
    
    ADataPanel dpTag = new DPTag();
    dpTag.setPreferredSize(new Dimension(0, 120));
    
    ADataPanel dpLuoghi = new DPLuoghi();
    dpLuoghi.setPreferredSize(new Dimension(0, 120));
    
    FormPanel fp = new FormPanel("Attributi");
    fp.addTab("Articolo");
    fp.addRow();
    fp.addDateField(sDATA_ARTICOLO,    "Data Articolo");
    fp.addBlankField();
    fp.addRow();
    fp.addTextField(sDESCRIZIONE,      "Descrizione", 50);
    fp.addRow();
    fp.addOptionsField(sID_CATEGORIA,  "Categoria",   Opzioni.getCategorie(true));
    fp.addBlankField();
    fp.addRow();
    fp.addOptionsField(sID_SOTTOCATEG, "Sottocateg.", new Vector<CodeAndDescription>());
    fp.addBlankField();
    fp.addRow();
    fp.addOptionsField(sID_TIPO_ART,   "Tipologia",   Opzioni.getTipologie(true));
    fp.addBlankField();
    fp.addTab("Autori");
    fp.addRow();
    fp.addDataPanel(sAUTORI, dpAutori);
    fp.addTab("Tag");
    fp.addRow();
    fp.addDataPanel(sTAG, dpTag);
    fp.addTab("Luoghi");
    fp.addRow();
    fp.addDataPanel(sLUOGHI, dpLuoghi);
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
    String[] asCOLUMNS   = {"Id", "Data Art.",    "Descrizione"};
    String[] asSYMBOLICS = {sID,  sDATA_ARTICOLO, sDESCRIZIONE};
    
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
    if(dpArticoli != null && btnSelect != null && btnSelect.isEnabled()) {
      onChoiceMade();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FormPanel fpFilter = (FormPanel) getFilterContainer();
          fpFilter.setValue(sKEYWORDS, "");
          fpFilter.requestFocus(sKEYWORDS);
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
    if(dpArticoli != null) dpArticoli.addRecord(oLastRecordReaded);
  }
  
  protected
  void setFilterValues(Object oValues)
      throws Exception
  {
    if(oValues instanceof Map) {
      FormPanel fpFilter = (FormPanel) getFilterContainer();
      fpFilter.setValues((Map<?, ?>) oValues);
    }
  }
  
  protected
  void doFind()
      throws Exception
  {
    FormPanel fpFilter = (FormPanel) getFilterContainer();
    Map<String, Object> oFilterValues  = fpFilter.getValues();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new Vector<Object>();
    parameters.add(DataNormalizer.normalize(oFilterValues));
    
    oRecords = WUtil.toListOfMapObject(oRPCClient.execute("ARTICOLI.find", parameters, true));
    
    AppUtil.denormalizeText(oRecords);
    
    oTableModel.setData(oRecords);
    TableSorter.resetHeader(oTable);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    
    if(oRecords.size() == 1) {
      ResourcesMgr.getStatusBar().setText("1 articolo trovato.");
    }
    else {
      ResourcesMgr.getStatusBar().setText(oRecords.size() + " articoli trovati.");
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
    
    fpFilter.requestFocus();
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
    Map<String, Object> mapRead = WUtil.toMapObject(oRPCClient.execute("ARTICOLI.read", parameters));
    
    oLastRecordReaded = new HashMap<String, Object>(mapRead);
    
    AppUtil.denormalizeText(oLastRecordReaded);
    
    FormPanel fpDetail = (FormPanel) getDetailContainer();
    fpDetail.reset();
    fpDetail.setValues(mapRead);
    fpDetail.setValue(sID_SOTTOCATEG, mapRead.get(sID_SOTTOCATEG));
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
    
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.delete", parameters), false);
    if(!oResult.booleanValue()) {
      GUIMessage.showWarning("Articolo non eliminabile.");
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
    Boolean oAttivo = (Boolean) oRecord.get(sATTIVO);
    boolean boNuovoStato = false;
    if(oAttivo != null) boNuovoStato = !oAttivo.booleanValue();
    
    IRPCClient oRPCClient = ResourcesMgr.getDefaultRPCClient();
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(oId);
    parameters.add(new Boolean(boNuovoStato));
    Boolean oResult = WUtil.toBooleanObj(oRPCClient.execute("ARTICOLI.setEnabled", parameters), oAttivo);
    
    oRecord.put(sATTIVO, oResult);
    oLastRecordReaded.put(sATTIVO, oResult);
  }
  
  protected 
  void onChangeEditorStatus(int iStatus) 
  {
  }
}
