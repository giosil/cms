package org.dew.cms.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledEditorKit;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.components.JRichTextNote;
import org.dew.swingup.editors.AEntityEditor;
import org.dew.swingup.editors.EntityDialog;
import org.dew.swingup.fm.FMEntry;
import org.dew.swingup.fm.FMViewer;
import org.dew.util.WUtil;
import org.dew.cms.common.IArticolo;

import org.dew.cms.gui.MenuManager;
import org.dew.cms.gui.dialogs.DialogHTMLPage;
import org.dew.cms.gui.dialogs.DialogOpzioni;
import org.dew.cms.gui.forms.GUIArticoli;

public 
class PopUpRichText extends JPopupMenu implements ActionListener
{
  private static final long serialVersionUID = -1495666055445926151L;
  
  protected JMenuItem jmiLinkInterno;
  protected JMenuItem jmiLinkEsterno;
  protected JMenuItem jmiRimuoviLink;
  protected JMenuItem jmiVediTuttiLink;
  protected JMenuItem jmiMultSinistra;
  protected JMenuItem jmiMultDestra;
  
  protected int iCount = 0;
  protected Map<Integer, String> mapLinkAttributes;
  protected JRichTextNote jRichTextNote;
  protected FMViewer fmViewer;
  protected Action actionCopyToClipboard;
  protected Action actionPasteFromClipboard;
  
  public PopUpRichText(JRichTextNote component, FMViewer fmViewer)
  {
    this.jRichTextNote = component;
    this.fmViewer      = fmViewer;
    
    mapLinkAttributes = new HashMap<Integer, String>();
    jRichTextNote.setMapLinkAttributes(mapLinkAttributes);
    
    jmiLinkInterno = new JMenuItem("Link Interno", ResourcesMgr.getSmallImageIcon("DocumentMagLarge.gif"));
    jmiLinkInterno.setActionCommand("int");
    jmiLinkInterno.addActionListener(this);
    jmiLinkEsterno = new JMenuItem("Link Esterno", ResourcesMgr.getSmallImageIcon("DocumentOutLarge.gif"));
    jmiLinkEsterno.setActionCommand("ext");
    jmiLinkEsterno.addActionListener(this);
    jmiRimuoviLink = new JMenuItem("Rimuovi Link", ResourcesMgr.getSmallImageIcon("DeleteLarge.gif"));
    jmiRimuoviLink.setActionCommand("rem");
    jmiRimuoviLink.addActionListener(this);
    jmiVediTuttiLink = new JMenuItem("Vedi tutti i link", ResourcesMgr.getSmallImageIcon("DocumentDiagramLarge.gif"));
    jmiVediTuttiLink.setActionCommand("show");
    jmiVediTuttiLink.addActionListener(this);
    
    this.add(jmiLinkInterno);
    this.add(jmiLinkEsterno);
    this.add(jmiRimuoviLink);
    this.add(jmiVediTuttiLink);
    
    if(fmViewer != null) {
      jmiMultSinistra = new JMenuItem("Multimedia sinistra", ResourcesMgr.getSmallImageIcon("LeftLarge.gif"));
      jmiMultSinistra.setActionCommand("msx");
      jmiMultSinistra.addActionListener(this);
      jmiMultDestra = new JMenuItem("Multimedia destra", ResourcesMgr.getSmallImageIcon("RightLarge.gif"));
      jmiMultDestra.setActionCommand("mdx");
      jmiMultDestra.addActionListener(this);
      this.addSeparator();
      this.add(jmiMultSinistra);
      this.add(jmiMultDestra);
    }
    
    jRichTextNote.getJEditorPane().addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger() || e.getButton() != MouseEvent.BUTTON1) {
          show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });
    Action[] actions = jRichTextNote.getJEditorPane().getEditorKit().getActions();
    for(int i = 0; i < actions.length; i++) {
      Action action = actions[i];
      String sActionName = (String) action.getValue(Action.NAME);
      if(sActionName.equalsIgnoreCase("copy-to-clipboard")) {
        actionCopyToClipboard = action;
      }
      else
        if(sActionName.equalsIgnoreCase("paste-from-clipboard")) {
          actionPasteFromClipboard = action;
        }	        
    }
    jRichTextNote.getJEditorPane().getDocument().addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
      }
      public void insertUpdate(DocumentEvent e) {
      }
      public void changedUpdate(DocumentEvent e) {
        String sText   = jRichTextNote.getJEditorPane().getText();
        String sTextLC = sText.toLowerCase();
        int iBody = sTextLC.indexOf("<body");
        if(iBody > 0) {
          int iStartBody = sTextLC.indexOf('>', iBody);
          if(iStartBody > 0) {
            int iEndBody = sTextLC.indexOf("</body>");
            if(iEndBody > 0) {
              String sBody = sTextLC.substring(iStartBody+1,iEndBody).trim();
              if(sBody.length() == 0 || sBody.equals("<br>") || sBody.equals("&nbsp;")) {
                iCount = 0;
                mapLinkAttributes.clear();
              }
            }
          }
        }
        else 
          if(sText == null || sText.length() == 0 || sText.equals("<br>") || sText.equals("&nbsp;")) {
            iCount = 0;
            mapLinkAttributes.clear();
          }
      }
    });
  }
  
  public 
  void actionPerformed(ActionEvent e) 
  {
    String sActionCommand = e.getActionCommand();
    if(sActionCommand == null || sActionCommand.length() == 0) return;
    if(jRichTextNote == null) return;
    if(sActionCommand.equals("int")) {
      int iIdArticolo = getIdArticolo();
      if(iIdArticolo != 0) {
        String sURLPreview = ResourcesMgr.config.getProperty("nam.url.preview");
        if(sURLPreview == null || sURLPreview.length() == 0) {
          sURLPreview = System.getProperty("nam.url.preview");
        }
        if(sURLPreview == null || sURLPreview.length() == 0) {
          sURLPreview = System.getProperty("jnlp.nam.url.preview");
        }
        if(sURLPreview != null && sURLPreview.length() > 0) {
          mapLinkAttributes.put(new Integer(iCount), "href=\"" + sURLPreview + iIdArticolo + "\"");
        }
        else {
          mapLinkAttributes.put(new Integer(iCount), "href=\"index.do?ida=" + iIdArticolo + "\"");
        }
        Action a = new StyledEditorKit.ForegroundAction("set-color", new Color(0, 0, 255 - iCount));
        a.actionPerformed(new ActionEvent(jRichTextNote.getJEditorPane(), ActionEvent.ACTION_PERFORMED, "set-color"));
        iCount++;
      }
    }
    else if(sActionCommand.equals("ext")) {
      int SelectionEnd = jRichTextNote.getJEditorPane().getSelectionEnd();
      if(SelectionEnd < 1) return;
      String sURL = GUIMessage.getInput("Riportare l'indirizzo completo della pagina (ad esempio: http://www.test.com/test/index.html o mailto:indirizzo di posta elettronica)");
      if(sURL != null && sURL.length() > 0) {
        int iSep = sURL.indexOf('@');
        if (iSep > 0) {
          mapLinkAttributes.put(new Integer(iCount), "href=\"" + sURL + "\"");
        } else {
          mapLinkAttributes.put(new Integer(iCount), "href=\"" + sURL + "\" target=\"_blank\"");
        }
        Action a = new StyledEditorKit.ForegroundAction("set-color", new Color(0, 0, 255 - iCount));
        a.actionPerformed(new ActionEvent(jRichTextNote.getJEditorPane(), ActionEvent.ACTION_PERFORMED, "set-color"));
        iCount++;
      }
    }
    else if(sActionCommand.equals("rem")) {
      Action a = new StyledEditorKit.ForegroundAction("set-color", new Color(0, 0, 0));
      a.actionPerformed(new ActionEvent(jRichTextNote.getJEditorPane(), ActionEvent.ACTION_PERFORMED, "set-color"));
      if(actionCopyToClipboard != null) {
        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copy-to-clipboard");
        actionCopyToClipboard.actionPerformed(actionEvent);
        try {
          String sSelectedText = null;
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable transferable = clipboard.getContents(null);
          if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            sSelectedText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
          }
          if(sSelectedText != null && sSelectedText.length() > 0) {
            String sText = jRichTextNote.getText();
            String sTextToSearch = sSelectedText + "</a>";
            int iIndexOf = sText.indexOf(sTextToSearch);
            if(iIndexOf <= 0) return;
            int iLenTextToSearch = sTextToSearch.length();
            String sLeft   = sText.substring(0, iIndexOf);
            int iStartLink = sLeft.lastIndexOf("<a");
            if(iStartLink < 0) iStartLink = sLeft.length() - 3;
            sLeft = sLeft.substring(0, iStartLink);
            String sRight  = iIndexOf + iLenTextToSearch < sText.length() ? sText.substring(iIndexOf + iLenTextToSearch) : "";
            jRichTextNote.setText(sLeft + sSelectedText + sRight);
          }
          else {
            GUIMessage.showWarning("Selezionare la parola o l'intera frase linkata.");
            return;
          }
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    }
    else if(sActionCommand.equals("show")) {
      String sText = jRichTextNote.getText();
      List<String> listLinks = getLinks(sText);
      if(listLinks == null || listLinks.size() == 0) {
        GUIMessage.showWarning("Non vi sono link nel testo analizzato.");
        return;
      }
      String sHTML = "<html><body>";
      sHTML += "<h2 align=\"center\">Link contenuti nel testo</h2>";
      for(int i = 0; i < listLinks.size(); i++) {
        String sLink = (String) listLinks.get(i);
        String sDesc = getDescLink(sLink);
        String sURL  = getURL(sLink);
        if(sURL != null && sURL.startsWith("http")) {
          sHTML += sDesc + " --&gt; <a href=\"" + sURL + "\">" + sURL + "</a><br>";
        }
        else {
          String sValue = getParValue(sURL, "ida");
          if(sValue != null && sValue.length() > 0) {
            sHTML += sDesc + " --&gt; Articolo <b>" + sValue + "</b><br>";
          }
          else {
            sHTML += sDesc + " --&gt; Articolo <b>" + sURL + "</b><br>";
          }
        }
      }
      sHTML += "</body></html>";
      DialogHTMLPage.showMe("Link contenuti nel testo", sHTML);
    }
    else if(sActionCommand.equals("msx")) {
      if(actionPasteFromClipboard == null) {
        GUIMessage.showWarning("Operazione non consentita.");
        return;
      }
      int iMultimedia = getMultimedia();
      if(iMultimedia == 0) return;
      StringSelection oStringSelection = new StringSelection("{$" + iMultimedia + "}");
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(oStringSelection, null);
      ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "paste-from-clipboard");
      actionPasteFromClipboard.actionPerformed(actionEvent);
    }
    else if(sActionCommand.equals("mdx")) {
      if(actionPasteFromClipboard == null) {
        GUIMessage.showWarning("Operazione non consentita.");
        return;
      }
      int iMultimedia = getMultimedia();
      if(iMultimedia == 0) return;
      StringSelection oStringSelection = new StringSelection("{#" + iMultimedia + "}");
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(oStringSelection, null);
      ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "paste-from-clipboard");
      actionPasteFromClipboard.actionPerformed(actionEvent);
    }
  }
  
  protected
  int getIdArticolo()
  {
    AEntityEditor aEntityEditor = new GUIArticoli();
    
    EntityDialog entityDialog = new EntityDialog();
    entityDialog.init(aEntityEditor, "Articoli", MenuManager.sICON_ARTICOLI);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    entityDialog.setSize(950, 700);
    entityDialog.setLocation(screenSize.width/2  - entityDialog.getSize().width/2,
        screenSize.height/2 - entityDialog.getSize().height/2);
    entityDialog.setVisible(true);
    
    Map<String, Object> mapArticolo = null;
    Object oChoice  = entityDialog.getChoice();
    if(oChoice instanceof List) {
      List<?> listRecords = (List<?>) oChoice;
      if(listRecords.size() > 0) {
        mapArticolo = WUtil.toMapObject(listRecords.get(0));
      }
    }
    else if(oChoice instanceof Map) {
      mapArticolo = WUtil.toMapObject(oChoice);
    }
    
    if(mapArticolo == null || mapArticolo.isEmpty()) return 0;
    return WUtil.toInt(mapArticolo.get(IArticolo.sID), 0);
  }
  
  protected
  int getMultimedia()
  {
    Vector<FMEntry> vItems = fmViewer.getEntries();
    if(vItems == null || vItems.size() == 0) {
      GUIMessage.showWarning("Non vi sono file multimediali.");
      return 0;
    }
    Vector<Object> vValues = new Vector<Object>();
    for(int i = 0; i < vItems.size(); i++) {
      FMEntry fmEntry = vItems.get(i);
      if(fmEntry.isDirectory()) continue;
      vValues.add(fmEntry);
    }
    if(vValues == null || vValues.size() == 0) {
      GUIMessage.showWarning("Non vi sono file multimediali.");
      return 0;
    }
    Object oItem = DialogOpzioni.showMe(ResourcesMgr.mainFrame, "Multimedia", "Scegli il file",  ResourcesMgr.getImageIcon(MenuManager.sICON_MULTIMEDIA), 500, 600, vValues);
    if(oItem == null) return 0;
    return vValues.indexOf(oItem) + 1;
  }
  
  public
  List<String> getLinks(String sText)
  {
    List<String> listResult = new ArrayList<String>();
    if(sText == null) return listResult;
    int iStartLink = sText.indexOf("<a ");
    while(iStartLink >= 0) {
      int iEndLink = sText.indexOf("</a>", iStartLink);
      if(iEndLink > 0) {
        String sLink = sText.substring(iStartLink, iEndLink + 4);
        listResult.add(sLink);
      }
      iStartLink = sText.indexOf("<a ", iStartLink + 1);
    }
    return listResult;
  }
  
  public
  String getDescLink(String sLink)
  {
    if(sLink == null) return "";
    int iStartDesc = sLink.indexOf('>');
    int iEndDesc   = sLink.lastIndexOf('<');
    if(iStartDesc > 0 && iEndDesc > 0) {
      return sLink.substring(iStartDesc + 1, iEndDesc);
    }
    return sLink;
  }
  
  public
  String getURL(String sLink)
  {
    if(sLink == null) return "";
    int iStart = sLink.indexOf("href=\"");
    if(iStart > 0) {
      int iEnd = sLink.indexOf('"', iStart + 6);
      if(iEnd > 0) {
        return sLink.substring(iStart + 6, iEnd);
      }
      else {
        return sLink.substring(iStart + 6);
      }
    }
    iStart = sLink.indexOf("rel=\"");
    if(iStart > 0) {
      int iEnd = sLink.indexOf('"', iStart + 5);
      if(iEnd > 0) {
        return sLink.substring(iStart + 5, iEnd);
      }
      else {
        return sLink.substring(iStart + 5);
      }
    }
    return sLink;
  }
  
  public static
  String getParValue(String sURL, String sParName)
  {
    if(sURL == null) return null;
    int iStartPar = sURL.indexOf(sParName + "=");
    if(iStartPar < 0) return null;
    int iEndPar = sURL.indexOf('&', iStartPar);
    if(iEndPar < 0) iEndPar = sURL.length();
    int iStartValue = iStartPar + (sParName + "=").length();
    String sValue = sURL.substring(iStartValue, iEndPar);
    return sValue;
  }
}
