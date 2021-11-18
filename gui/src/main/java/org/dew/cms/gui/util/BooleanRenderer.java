package org.dew.cms.gui.util;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

public 
class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource 
{
  private static final long serialVersionUID = 1328110546490227245L;
  
  private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
  
  public 
  BooleanRenderer() 
  {
    super();
    setHorizontalAlignment(JLabel.CENTER);
    setBorderPainted(true);
  }
  
  public Component 
  getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
  {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      super.setBackground(table.getSelectionBackground());
    }
    else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setSelected((value != null && ((Boolean)value).booleanValue()));
    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
    } else {
      setBorder(noFocusBorder);
    }
    return this;
  }
}
