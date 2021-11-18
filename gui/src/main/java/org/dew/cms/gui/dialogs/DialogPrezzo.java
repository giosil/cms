package org.dew.cms.gui.dialogs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.Date;
import java.util.Map;

import org.dew.swingup.AJDialog;
import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.util.FormPanel;

import org.dew.util.WUtil;

import org.dew.cms.common.IArticolo;

public 
class DialogPrezzo extends AJDialog implements IArticolo
{
  private static final long serialVersionUID = -7176821017680584187L;
  
  protected FormPanel fp;
  protected Map<String, Object> mapData;
  protected FormPanel fpGenerale;
  protected Date startDate;
  protected Date endDate;
  
  public
  DialogPrezzo(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    setSize(400, 270);
  }
  
  public static
  Map<String, Object> showMe(Map<String, Object> mapValues)
  {
    DialogPrezzo dialog = new DialogPrezzo(ResourcesMgr.mainFrame, "Prezzo", true);
    if(mapValues != null) {
      dialog.setData(mapValues);
    }
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    return dialog.getData();
  }
  
  protected 
  Container buildGUI() 
      throws Exception 
  {
    fp = new FormPanel();
    fp.setCase(0);
    fp.addRow();
    fp.addTextField(sPREZZO_CODICE,       "Codice",       50);
    fp.addRow();
    fp.addTextField(sPREZZO_DESCR,        "Descrizione", 255);
    fp.addRow();
    fp.addCurrencyField(sPREZZO_PREZZO,   "Prezzo");
    fp.addRow();
    fp.addIntegerField(sPREZZO_SCONTO,    "Sconto (%)",    3);
    fp.addRow();
    fp.addCurrencyField(sPREZZO_SCONTATO, "Prezzo Scontato");
    fp.addRow();
    fp.addCurrencyField(sPREZZO_ACCONTO,  "Acconto");
    fp.addRow();
    fp.addBooleanField(sPREZZO_PROMOZ,    "Promozione");
    fp.build();
    
    Component cmpPrezzoScontato = fp.getComponent(sPREZZO_SCONTATO);
    if(cmpPrezzoScontato != null) {
      cmpPrezzoScontato.addFocusListener(new FocusListener() {
        public void focusLost(FocusEvent e) {
        }
        public void focusGained(FocusEvent e) {
          Double  oPrezzo = (Double)  fp.getValue(sPREZZO_PREZZO);
          Integer oSconto = (Integer) fp.getValue(sPREZZO_SCONTO);
          if(oPrezzo != null && oSconto != null) {
            double dPrezzo = oPrezzo.doubleValue();
            int iSconto    = oSconto.intValue();
            if(dPrezzo > 0.0d && iSconto != 0) {
              double dSconto = (dPrezzo * (double) iSconto) / 100.0d;
              double dPrezzoScontato = WUtil.round2(dPrezzo - dSconto);
              fp.setValue(sPREZZO_SCONTATO, dPrezzoScontato);
            }
          }
        }
      });
    }
    
    return fp;
  }
  
  public
  boolean doOk()
  {
    if(fp.isBlank(sPREZZO_CODICE) && fp.isBlank(sPREZZO_DESCR)) {
      Double oPrezzo  = (Double)  fp.getValue(sPREZZO_PREZZO);
      Boolean oPromoz = (Boolean) fp.getValue(sPREZZO_PROMOZ);
      boolean boPrezzo_Blank = oPrezzo == null || oPrezzo.doubleValue() == 0.0d;
      boolean boPromozione   = oPromoz != null && oPromoz.booleanValue();
      if(!boPromozione && boPrezzo_Blank) {
        GUIMessage.showWarning("Riportare il prezzo. Se pari a zero selezionare il flag Promozione.");
        return false;
      }
    }
    mapData = fp.getContents();
    return true;
  }
  
  public
  boolean doCancel()
  {
    mapData = null;
    return true;
  }
  
  public
  void setData(Map<String, Object> mapData)
  {
    fp.setValues(mapData);
  }
  
  public
  Map<String, Object> getData()
  {
    return mapData;
  }
}

