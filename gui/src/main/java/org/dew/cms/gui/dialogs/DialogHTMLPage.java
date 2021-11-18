package org.dew.cms.gui.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.dew.swingup.AJDialog;
import org.dew.swingup.GUIMessage;

public 
class DialogHTMLPage extends AJDialog
{
  private static final long serialVersionUID = 8115874174955272199L;
  
  protected JButton btnPrintRed;
  protected JButton btnPrint;
  protected JButton btnCancel;
  
  protected JEditorPane jEditorPane;
  protected HyperlinkListener oDefaultHyperlinkListener;
  protected boolean boCancel = true;
  
  public
  DialogHTMLPage(String sTitle)
  {
    super(sTitle);
    setSize(800, 600);
  }
  
  public static
  void showMe(String sTitle, String sText)
  {
    DialogHTMLPage dialog = new DialogHTMLPage(sTitle);
    try {
      dialog.setText(sText);
    }
    catch(Exception ex) {
      GUIMessage.showException("Errore durante l'apertura di DialogHTMLPage", ex);
      return;
    }
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(size.width/2 - dialog.getSize().width/2, size.height/2 - dialog.getSize().height/2);
    dialog.setVisible(true);
  }
  
  public
  void setHomePage(URL urlPage)
      throws Exception
  {
    jEditorPane.setPage(urlPage);
    jEditorPane.setCaretPosition(0);
  }
  
  public
  void setText(String sText)
      throws Exception
  {
    jEditorPane.setText(sText);
    jEditorPane.setCaretPosition(0);
  }
  
  protected 
  Container buildGUI() 
  {
    jEditorPane = new JEditorPane();
    jEditorPane.setEditable(false);
    jEditorPane.setContentType("text/html");
    oDefaultHyperlinkListener = createHyperLinkListener();
    jEditorPane.addHyperlinkListener(oDefaultHyperlinkListener);
    JScrollPane oScrollPane = new JScrollPane(jEditorPane);
    return oScrollPane;
  }
  
  public
  boolean doOk()
  {
    return true;
  }
  
  public
  boolean doCancel()
  {
    return true;
  }
  
  public HyperlinkListener createHyperLinkListener() {
    return new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (e instanceof HTMLFrameHyperlinkEvent) {
            ((HTMLDocument) jEditorPane.getDocument()).processHTMLFrameHyperlinkEvent(
                (HTMLFrameHyperlinkEvent)e);
          } else {
            try {
              URL url = e.getURL();
              if(url == null) return;
              String sURL = url.toString();
              if(sURL.indexOf(' ') >= 0) {
                StringBuffer sbURLEncoded = new StringBuffer();
                for(int i = 0; i < sURL.length(); i++) {
                  char c = sURL.charAt(i);
                  if(c == ' ') {
                    sbURLEncoded.append("%20");
                  }
                  else {
                    sbURLEncoded.append(c);
                  }
                }
                url = new URL(sbURLEncoded.toString());
              }
              jEditorPane.setPage(url);
              jEditorPane.setCaretPosition(0);
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      }
    };
  }
}
