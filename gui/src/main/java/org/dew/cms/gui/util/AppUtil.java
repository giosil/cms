package org.dew.cms.gui.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dew.cms.gui.forms.GUIArticolo;

import org.dew.swingup.GUIMessage;
import org.dew.swingup.ResourcesMgr;
import org.dew.swingup.User;

import org.dew.util.WUtil;

public
class AppUtil
{
  public static
  String getUserGroup()
  {
    String sResult = null;
    User user = ResourcesMgr.getSessionManager().getUser();
    List<?> listGroups = user.getGroups();
    if(listGroups != null && listGroups.size() > 0) {
      Object oGroup = listGroups.get(0);
      sResult = oGroup != null ? oGroup.toString() : null;
    }
    return sResult;
  }
  
  public static
  int getPrefix(String sText)
  {
    if(sText == null || sText.length() == 0) return -1;
    int iResult = -1;
    StringBuffer sbPrefix = new StringBuffer();
    for(int i = 0; i < sText.length(); i++) {
      char c = sText.charAt(i);
      if(Character.isDigit(c)) {
        sbPrefix.append(c);
      }
      else {
        break;
      }
    }
    if(sbPrefix.length() == 0) return iResult;
    try{ iResult = Integer.parseInt(sbPrefix.toString()); } catch(Exception ex) {}
    return iResult;
  }
  
  public static
  String getHost(String sURL)
  {
    if(sURL == null) return "";
    int iSepHost = sURL.indexOf("://");
    if(iSepHost < 0) return "";
    int iStartHost = iSepHost + 3;
    String sHost = null;
    int iSepPort = sURL.indexOf(':', iStartHost);
    int iSepCtx  = sURL.indexOf('/', iStartHost);
    if(iSepPort > 0) {
      sHost = sURL.substring(iStartHost, iSepPort);
    }
    else {
      if(iSepCtx > 0) {
        sHost = sURL.substring(iStartHost, iSepCtx);
      }
      else {
        sHost = sURL.substring(iStartHost);
      }
    }
    if(sHost == null) sHost = "";
    return sHost;
  }
  
  public static String normalizeText(String text) {
    if(text == null || text.length() == 0) return text;
    int iLength = text.length();
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < iLength; i++) {
      char c = text.charAt(i);
      switch (c) {
        case '\300':   sb.append("&Agrave;"); break;
        case '\310':   sb.append("&Egrave;"); break;
        case '\314':   sb.append("&Igrave;"); break;
        case '\322':   sb.append("&Ograve;"); break;
        case '\331':   sb.append("&Ugrave;"); break;
        case '\301':   sb.append("&Aacute;"); break;
        case '\311':   sb.append("&Eacute;"); break;
        case '\315':   sb.append("&Iacute;"); break;
        case '\323':   sb.append("&Oacute;"); break;
        case '\332':   sb.append("&Uacute;"); break;
        case '\340':   sb.append("&agrave;"); break;
        case '\350':   sb.append("&egrave;"); break;
        case '\354':   sb.append("&igrave;"); break;
        case '\362':   sb.append("&ograve;"); break;
        case '\371':   sb.append("&ugrave;"); break;
        case '\341':   sb.append("&aacute;"); break;
        case '\351':   sb.append("&eacute;"); break;
        case '\355':   sb.append("&iacute;"); break;
        case '\363':   sb.append("&oacute;"); break;
        case '\372':   sb.append("&uacute;"); break;
        case '\347':   sb.append("&ccedil;"); break;
        case '\307':   sb.append("&Ccedil;"); break;
        case '\361':   sb.append("&ntilde;"); break;
        case '\342':   sb.append("&acirc;");  break;
        case '\352':   sb.append("&ecirc;");  break;
        case '\356':   sb.append("&icirc;");  break;
        case '\364':   sb.append("&ocirc;");  break;
        case '\373':   sb.append("&ucirc;");  break;
        case '\252':   sb.append("&ordf;");   break;
        case '\260':   sb.append("&deg;");    break;
        case '\241':   sb.append("&iexcl;");  break;
        case '\277':   sb.append("&iquest;"); break;
        case '\u20ac': sb.append("&euro;");   break;
        default: {
          if(c < 128) {
            sb.append(c);
          }
          else {
            int code = (int) c;
            sb.append("&#" + code + ";");
          }
        }
      }
    }
    return sb.toString();
  }
  
  public static void normalizeText(Map<String, Object> map) {
    if(map == null || map.isEmpty()) return;
    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      Object oValue = entry.getValue();
      if(oValue instanceof String) {
        entry.setValue(normalizeText((String) oValue));
      }
      else if(oValue instanceof List) {
        normalizeText(WUtil.toList(oValue, Object.class, null));
      }
      else if(oValue instanceof Map) {
        normalizeText(WUtil.toMapObject(oValue));
      }
    }
  }
  
  public static void normalizeText(List<Object> list) {
    if(list == null || list.isEmpty()) return;
    for(int i = 0; i < list.size(); i++) {
      Object oItem = list.get(i);
      if(oItem instanceof String) {
        list.set(i, normalizeText((String) oItem));
      }
      else if(oItem instanceof List) {
        normalizeText(WUtil.toList(oItem, Object.class, null));
      }
      else if(oItem instanceof Map) {
        normalizeText(WUtil.toMapObject(oItem));
      }
    }
  }
  
  public static String denormalizeText(String sText) {
    if(sText == null || sText.length() <= 1) return sText;
    sText = sText.replace("&agrave;", "\340");
    sText = sText.replace("&egrave;", "\350");
    sText = sText.replace("&igrave;", "\354");
    sText = sText.replace("&ograve;", "\362");
    sText = sText.replace("&ugrave;", "\371");
    sText = sText.replace("&aacute;", "\341");
    sText = sText.replace("&eacute;", "\351");
    sText = sText.replace("&iacute;", "\355");
    sText = sText.replace("&oacute;", "\363");
    sText = sText.replace("&uacute;", "\372");
    sText = sText.replace("&Agrave;", "\300");
    sText = sText.replace("&Egrave;", "\310");
    sText = sText.replace("&Igrave;", "\314");
    sText = sText.replace("&Ograve;", "\322");
    sText = sText.replace("&Ugrave;", "\331");
    sText = sText.replace("&Aacute;", "\301");
    sText = sText.replace("&Eacute;", "\311");
    sText = sText.replace("&Iacute;", "\315");
    sText = sText.replace("&Oacute;", "\323");
    sText = sText.replace("&Uacute;", "\332");
    return sText;
  }
  
  public static void denormalizeText(Map<String, Object> map) {
    if(map == null || map.isEmpty()) return;
    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      Object oValue = entry.getValue();
      if(oValue instanceof String) {
        entry.setValue(denormalizeText((String) oValue));
      }
      else if(oValue instanceof List) {
        denormalizeText(WUtil.toList(oValue, Object.class, null));
      }
      else if(oValue instanceof Map) {
        denormalizeText(WUtil.toMapObject(oValue));
      }
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void denormalizeText(List list) {
    if(list == null || list.isEmpty()) return;
    for(int i = 0; i < list.size(); i++) {
      Object oItem = list.get(i);
      if(oItem instanceof String) {
        list.set(i, denormalizeText((String) oItem));
      }
      else if(oItem instanceof List) {
        denormalizeText(WUtil.toList(oItem, Object.class, null));
      }
      else if(oItem instanceof Map) {
        denormalizeText(WUtil.toMapObject(oItem));
      }
    }
  }
  
  public static GUIArticolo getGUIArticolo() {
    String sGUIArticolo = ResourcesMgr.config.getProperty("cms.gui.article");
    if(sGUIArticolo == null || sGUIArticolo.length() == 0) {
      sGUIArticolo = System.getProperty("cms.gui.article");
    }
    Object oGUIArticolo = null;
    if(sGUIArticolo != null && sGUIArticolo.length() > 1) {
      try {
        Class<?> classGUIArticolo = Class.forName(sGUIArticolo);
        if(classGUIArticolo != null) {
          oGUIArticolo = classGUIArticolo.newInstance();
        }
        if(!(oGUIArticolo instanceof GUIArticolo)) {
          GUIMessage.showWarning(sGUIArticolo + " non \350 una istanza di GUIArticolo.");
          return null;
        }
      }
      catch(Exception ex) {
        ResourcesMgr.getLogger().error("Error during instancing " + sGUIArticolo);
      }
    }
    if(oGUIArticolo == null) oGUIArticolo = new GUIArticolo();
    return (GUIArticolo) oGUIArticolo;
  }
}
