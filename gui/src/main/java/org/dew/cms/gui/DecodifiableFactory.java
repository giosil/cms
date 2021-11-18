package org.dew.cms.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.util.ArrayList;
import java.util.List;

import org.dew.swingup.components.ADecodifiableComponent;
import org.dew.swingup.components.JTextDecodifiable;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.editors.EntityLookUpDialog;
import org.dew.swingup.impl.SimpleLookUpDialog;
import org.dew.swingup.util.JComboDecodifiableExt;

import org.dew.cms.common.IAutore;

import org.dew.cms.gui.dialogs.AutoriLookUpFinder;
import org.dew.cms.gui.dialogs.ComuniLookUpFinder;
import org.dew.cms.gui.dialogs.IstitutiLookUpFinder;

import org.dew.cms.gui.forms.GUIAutori;

public 
class DecodifiableFactory 
{
  public static
  ADecodifiableComponent buildDCIstituto()
  {
    JTextDecodifiable oResult = new JTextDecodifiable("Istituto");
    oResult.setLookUpFinder(new IstitutiLookUpFinder());
    oResult.setLookUpDialog(new SimpleLookUpDialog("Ricerca Istituto"));
    return oResult;
  }
  
  public static
  ADecodifiableComponent buildDCComune()
  {
    JComboDecodifiableExt oResult = new JComboDecodifiableExt("Comune");
      oResult.setLookUpFinder(new ComuniLookUpFinder());
      oResult.setLookUpDialog(new SimpleLookUpDialog("Ricerca Comune"));
      return oResult;
  } 
  
  public static
  ADecodifiableComponent buildDCAutore()
  {
    JTextDecodifiable oResult = new JTextDecodifiable("Autore", 1);
    oResult.setFindByPressingEnter(true);
    oResult.setLookUpFinder(new AutoriLookUpFinder(true));
    
    AEntityEditor guiAnagrafe = new GUIAutori(true);
    List<String> oKeys = new ArrayList<String>();
    oKeys.add(IAutore.sID);
    oKeys.add(IAutore.sNOMINATIVO);
    
    EntityLookUpDialog oLookUpDialog = new EntityLookUpDialog(guiAnagrafe, oKeys, true);
    oLookUpDialog.setTitle("Ricerca Autore");
      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      if(size.height > 768) {
        oLookUpDialog.setSize(980, 720);
      }
      else {
      oLookUpDialog.setSize(850, 650);
      }
    oResult.setLookUpDialog(oLookUpDialog);
    return oResult;
  }
}
