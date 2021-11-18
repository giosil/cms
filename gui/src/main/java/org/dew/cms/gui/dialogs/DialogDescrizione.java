package org.dew.cms.gui.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dew.swingup.AJDialog;
import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.util.CodeAndDescription;
import org.dew.swingup.util.FormPanel;
import org.dew.cms.gui.util.Opzioni;

public 
class DialogDescrizione extends AJDialog 
{
  private static final long serialVersionUID = 9044546985409186427L;
  
  public final static String sDESCRIZIONE =  "d";
  public final static String sID_LINGUA   = "il";
  public final static String sDESC_LINGUA = "dl";
  
  protected FormPanel fp;
  protected Map<String, Object> mapData;
  
  public DialogDescrizione(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    setSize(700, 200);
  }
  
  public static
  Map<String, Object> showMe()
  {
    DialogDescrizione dialog = new DialogDescrizione(ResourcesMgr.mainFrame, "Descrizione Localizzata", true);
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    return dialog.getData();
  }
  
  public static
  Map<String, Object> showMe(Map<String, Object> mapDefaultValues, boolean boLangEnabled)
  {
    DialogDescrizione dialog = new DialogDescrizione(ResourcesMgr.mainFrame, "Descrizione Localizzata", true);
    if(mapDefaultValues != null) {
      dialog.setData(mapDefaultValues);
    }
    dialog.setLanguageEnabled(boLangEnabled);
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
    return dialog.getData();
  }
  
  protected 
  Container buildGUI() 
      throws Exception 
  {
    fp = new FormPanel("Descrizione Localizzata");
    fp.addRow();
    fp.addOptionsField(sID_LINGUA, "Lingua", Opzioni.getLingue(true));
    fp.addRow();
    fp.addTextField(sDESCRIZIONE,  "Descrizione", 255);
    fp.build();
    
    fp.setCase(sDESCRIZIONE, 0);
    
    List<String> listMandatoryFields = new ArrayList<String>();
    listMandatoryFields.add(sID_LINGUA);
    listMandatoryFields.add(sDESCRIZIONE);
    fp.setMandatoryFields(listMandatoryFields);
    return fp;
  }
  
  public
  boolean doOk()
  {
    String sCheckMandatory = fp.getStringCheckMandatories();
    if(sCheckMandatory.length() > 0) {
      GUIMessage.showWarning("Occorre valorizzare i seguenti campi:\n" + sCheckMandatory);
      return false;
    }
    mapData = fp.getValues();
    Object oIdLingua = mapData.get(sID_LINGUA);
    if(oIdLingua instanceof CodeAndDescription) {
      CodeAndDescription cdLingua = (CodeAndDescription) oIdLingua;
      mapData.put(sID_LINGUA,   cdLingua.getCode());
      mapData.put(sDESC_LINGUA, cdLingua.getDescription());
    }
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
  void setLanguageEnabled(boolean boEnabled)
  {
    if(boEnabled) {
      fp.setEnabled(sID_LINGUA, boEnabled);
      fp.requestFocus(sID_LINGUA);
    }
    else {
      fp.requestFocus(sDESCRIZIONE);
    }
  }
  
  public
  Map<String, Object> getData()
  {
    return mapData;
  }
}
