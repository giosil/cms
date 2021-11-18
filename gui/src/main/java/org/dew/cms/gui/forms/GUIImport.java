package org.dew.cms.gui.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.dew.cms.gui.util.Opzioni;
import org.dew.swingup.GUIMessage;
import org.dew.swingup.IResourceMgr;
import org.dew.swingup.IWorkObject;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.fm.FMViewer;

public
class GUIImport extends JPanel implements IWorkObject
{
  private static final long serialVersionUID = -5378154947630019476L;
  
  protected FMViewer fmViewer;
  
  public
  GUIImport()
  {
    try {
      init();
    }
    catch (Exception ex) {
      GUIMessage.showException("Errore durante l'inizializzazione di GUIImport", ex);
    }
  }
  
  protected
  void init()
      throws Exception
  {
    this.setLayout(new BorderLayout());
    
    fmViewer = new FMViewer("Import", ResourcesMgr.config.getProperty(IResourceMgr.sAPP_RPC_URL), Opzioni.getImportFolder(), "*.*");
    
    String sNote = "<html>\n<head>\n</head>\n<body>\n";
    sNote += "<h2>Cartella contenuti multimediali.</h2>";
    sNote += "<font size=\"+1\">";
    sNote += "Attraverso questo pannello &egrave; possibile caricare i contenuti multimediali sul CMS.<br>";
    sNote += "Si possono trasferire file tramite l'area sottostante trascinandoli o gestendoli con il tasto destro.<br>";
    sNote += "Tutti i file trasferiti saranno subito visibili agli articolisti.<br>";
    sNote += "Prima di caricare i contenuti, si ricordi di convertire i video in flv, gli audio in mp3 e le immagini ad alta risoluzione in jpg.<br>";
    sNote += "</font></body></html>";
    
    JEditorPane jEditorPane = new JEditorPane();
    jEditorPane.setContentType("text/html");
    jEditorPane.setEditable(false);
    jEditorPane.setText(sNote);
    jEditorPane.setCaretPosition(0);
    jEditorPane.setPreferredSize(new Dimension(0, 160));
    
    JPanel jpNorth = new JPanel(new BorderLayout());
    jpNorth.add(jEditorPane, BorderLayout.CENTER);
    jpNorth.setBorder(BorderFactory.createTitledBorder("Note"));
    
    this.add(jpNorth,  BorderLayout.NORTH);
    this.add(fmViewer, BorderLayout.CENTER);
  }
  
  public
  void onActivated()
  {
  }
  
  public
  boolean onClosing()
  {
    return true;
  }
  
  public
  void onOpened()
  {
    if(fmViewer != null) {
      fmViewer.doRefresh();
    }
  }
}
