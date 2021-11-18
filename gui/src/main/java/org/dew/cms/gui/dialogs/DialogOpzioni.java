package org.dew.cms.gui.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.dew.swingup.*;

public
class DialogOpzioni extends AJDialog
{
  private static final long serialVersionUID = -4745471698810808922L;
  
  protected JList<Object> jList;
  protected JCheckBox[] arrayOfCheckBox;
  protected Map<Integer, Object> mapItems;
  protected List<Object> oValues;
  protected Object oSelectedValue;
  protected String sBorderTitle;
  protected ImageIcon icon;
  protected boolean boSceltaMultipla;
  protected int iIndexToSelect = -1;
  
  public DialogOpzioni(Frame parent, String sTitle, String sBorderTitle, ImageIcon icon, int iWidth, int iHeight, List<Object> listValues, boolean boSceltaMultipla, int iIndexToSelect)
  {
    setTitle(sTitle);
    setModal(true);
    this.sBorderTitle = sBorderTitle;
    this.oValues = listValues;
    this.icon = icon;
    this.boSceltaMultipla = boSceltaMultipla;
    this.iIndexToSelect = iIndexToSelect;
    try {
      init(false);
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante l'inizializzazione di DialogOpzioni", ex);
    }  
    this.setSize(iWidth, iHeight);
  }
  
  public static
  Object showMe(Frame parent, String sTitle, String sBorderTitle, ImageIcon icon, int iWidth, int iHeight, List<Object> listValues)
  {
    DialogOpzioni dialog = new DialogOpzioni(parent, sTitle, sBorderTitle,  icon, iWidth, iHeight, listValues, false, 0);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    
    return dialog.getSelectedValue();
  }
  
  public static
  Object showMe(Frame parent, String sTitle, String sBorderTitle, ImageIcon icon, int iWidth, int iHeight, List<Object> listValues, boolean boSceltaMultipla)
  {
    DialogOpzioni dialog = new DialogOpzioni(parent, sTitle, sBorderTitle, icon, iWidth, iHeight, listValues, boSceltaMultipla, 0);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    
    return dialog.getSelectedValue();
  }
  
  public static
  Object showMe(Frame parent, String sTitle, String sBorderTitle, int iWidth, int iHeight, List<Object> listValues, boolean boSceltaMultipla)
  {
    DialogOpzioni dialog = new DialogOpzioni(parent, sTitle, sBorderTitle, null, iWidth, iHeight, listValues, boSceltaMultipla, -1);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    
    return dialog.getSelectedValue();
  }
  
  public static
  Object showMe(Frame parent, String sTitle, String sBorderTitle, int iWidth, int iHeight, List<Object> listValues, boolean boSceltaMultipla, int iIndexToSelect)
  {
    DialogOpzioni dialog = new DialogOpzioni(parent, sTitle, sBorderTitle, null, iWidth, iHeight, listValues, boSceltaMultipla, iIndexToSelect);
    
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    
    return dialog.getSelectedValue();
  }
  
  public
  Object getSelectedValue()
  {
    return oSelectedValue;
  }
  
  protected
  Container buildGUI()
      throws Exception
  {
    if(boSceltaMultipla) {
      return buildArrayOfCheckBox();
    }
    else {
      return buildJList();
    }
  }
  
  protected
  Container buildJList()
      throws Exception
  {
    if(oValues == null || oValues.size() == 0) {
      return new JLabel("Nessuna opzione disponibile");
    }
    jList = new JList<Object>(new Vector<Object>(oValues));
    jList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2 && !e.isControlDown()) {
          fireOk();
        }
      }
    });
    jList.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
          fireOk();
        }
      }
    });
    if(icon != null) {
      jList.setCellRenderer(new DefaultListCellRenderer() {
        private static final long serialVersionUID = 9187131928417995867L;
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          setIcon(icon);
          return this;
        }
      });
    }
    if(oValues != null && oValues.size() > 0) {
      if(iIndexToSelect >= 0 && iIndexToSelect < oValues.size()) {
        jList.setSelectedIndex(iIndexToSelect);
      }
      else {
        jList.setSelectedIndex(0);
      }
    }
    JScrollPane jScrollPane = new JScrollPane(jList);
    jScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), sBorderTitle));
    return jScrollPane;
  }
  
  protected
  Container buildArrayOfCheckBox()
      throws Exception
  {
    if(oValues == null || oValues.size() == 0) {
      return new JLabel("Nessuna opzione disponibile");
    }
    arrayOfCheckBox = new JCheckBox[oValues.size()];
    mapItems = new HashMap<Integer, Object>();
    JPanel jpResult = new JPanel(new GridLayout(oValues.size(), 1, 4, 4));
    jpResult.setOpaque(true);
    jpResult.setBackground(Color.white);
    for(int i = 0; i < oValues.size(); i++) {
      Object oItem = oValues.get(i);
      if(oItem == null) continue;
      boolean boChecked = iIndexToSelect >= 0 ? i == iIndexToSelect : true;
      arrayOfCheckBox[i] = new JCheckBox(oItem.toString(), boChecked);
      arrayOfCheckBox[i].setOpaque(true);
      arrayOfCheckBox[i].setBackground(Color.white);
      jpResult.add(arrayOfCheckBox[i]);
      mapItems.put(i, oItem);
    }
    JScrollPane jScrollPane = new JScrollPane(jpResult);
    jScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), sBorderTitle));
    return jScrollPane;
  }
  
  public
  boolean doCancel()
  {
    oSelectedValue = null;
    return true;
  }
  
  public
  void onActivated()
  {
  }
  
  public
  void onOpened()
  {
  }
  
  public
  boolean doOk()
  {
    if(jList != null) {
      oSelectedValue = jList.getSelectedValue();
      return true;
    }
    if(arrayOfCheckBox != null && mapItems != null) {
      List<Object> listSelectedValue = new ArrayList<Object>();
      for(int i = 0; i < arrayOfCheckBox.length; i++) {
        JCheckBox jCheckBox = arrayOfCheckBox[i];
        if(jCheckBox.isSelected()) {
          listSelectedValue.add(mapItems.get(i));
        }
      }
      this.oSelectedValue = listSelectedValue;
    }
    return true;
  }
}

