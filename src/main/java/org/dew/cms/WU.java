package org.dew.cms;

import java.net.URLEncoder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dew.cms.backend.Article;
import org.dew.cms.backend.Multimedia;
import org.dew.cms.backend.Page;
import org.dew.cms.backend.Place;
import org.dew.cms.backend.Tag;
import org.dew.cms.backend.User;

import org.util.WUtil;

/**
 * Classe che raccoglie una serie di utilit&agrave;.<br>
 * I metodi che iniziano per getAttr consentono il recupero degli attributi dell'oggetto request.<br>
 * I metodi che iniziano per getParam consentono il recupero dei parametri.<br>
 * I metodi che iniziano per getSess consentono il recupero dei dati in sessione.<br>
 */
public 
class WU 
{
  public static
  int getLanguage(HttpServletRequest request)
  {
    return CMS.getLanguage(request);
  }
  
  public static
  void setLanguage(HttpServletRequest request, int idLang)
  {
    CMS.setLanguage(request, idLang);
  }
  
  public static
  void setLastArticle(HttpServletRequest request, int idArticle)
  {
    HttpSession httpSession = request.getSession(false);
    if(httpSession != null) {
      httpSession.setAttribute(CMS.SESS_LAST_ARTICLE, idArticle);
    }
  }
  
  public static
  void setNoCacheHeaders(HttpServletResponse response)
  {
    response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
    response.setHeader("Pragma","no-cache");        // HTTP 1.0
    response.setDateHeader ("Expires", 0);          // prevents caching at the proxy server
  }
  
  // Attributi ---------------------------------------------------------------- //
  
  public static
  int getAttrInt(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toInt(value, 0);
  }
  
  public static
  int getAttrInt(HttpServletRequest request, String sAttributeName, int iDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toInt(value, iDefault);
  }
  
  public static
  long getAttrLong(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toLong(value, 0);
  }
  
  public static
  long getAttrLong(HttpServletRequest request, String sAttributeName, long lDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toLong(value, lDefault);
  }
  
  public static
  double getAttrDouble(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toDouble(value, 0.0d);
  }
  
  public static
  double getAttrDouble(HttpServletRequest request, String sAttributeName, double dDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toDouble(value, dDefault);
  }
  
  public static
  boolean getAttrBoolean(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toBoolean(value, false);
  }
  
  public static
  boolean getAttrBoolean(HttpServletRequest request, String sAttributeName, boolean bDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toBoolean(value, bDefault);
  }
  
  public static
  String getAttrString(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toString(value, null);
  }
  
  public static
  String getAttrString(HttpServletRequest request, String sAttributeName, String sDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toString(value, sDefault);
  }
  
  public static
  Date getAttrDate(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toDate(value, null);
  }
  
  public static
  Date getAttrDate(HttpServletRequest request, String sAttributeName, Date dDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toDate(value, dDefault);
  }
  
  public static
  Calendar getAttrCalendar(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toCalendar(value, null);
  }
  
  public static
  Calendar getAttrCalendar(HttpServletRequest request, String sAttributeName, Calendar calDefault)
  {
    Object value = request.getAttribute(sAttributeName);
    return toCalendar(value, calDefault);
  }
  
  public static
  List<?> getAttrList(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return WUtil.toList(value, false);
  }
  
  public static
  int getAttrListSize(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    if(value == null) return 0;
    if(value instanceof List) {
      return ((List<?>) value).size();
    }
    return 1;
  }
  
  public static
  Article getAttrArticle(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toArticle(value, CMS.getLanguage(request), false);
  }
  
  public static
  Article getAttrArticle(HttpServletRequest request, String sAttributeName, boolean boEmptyArticle)
  {
    Object value = request.getAttribute(sAttributeName);
    return toArticle(value, CMS.getLanguage(request), boEmptyArticle);
  }
  
  public static
  Article getAttrArticle(HttpServletRequest request, String sAttributeName, int iIndex)
  {
    Object value = request.getAttribute(sAttributeName);
    return toArticle(value, CMS.getLanguage(request), iIndex, false);
  }
  
  public static
  Article getAttrArticle(HttpServletRequest request, String sAttributeName, int iIndex, boolean boEmptyArticle)
  {
    Object value = request.getAttribute(sAttributeName);
    return toArticle(value, CMS.getLanguage(request), iIndex, boEmptyArticle);
  }
  
  public static
  Multimedia getAttrMultimedia(HttpServletRequest request, String sAttributeName)
  {
    Object value = request.getAttribute(sAttributeName);
    return toMultimedia(value, false);
  }
  
  public static
  Multimedia getAttrMultimedia(HttpServletRequest request, String sAttributeName, boolean boEmptyMultimedia)
  {
    Object value = request.getAttribute(sAttributeName);
    return toMultimedia(value, boEmptyMultimedia);
  }
  
  public static
  Multimedia getAttrMultimedia(HttpServletRequest request, String sAttributeName, int iIndex)
  {
    Object value = request.getAttribute(sAttributeName);
    return toMultimedia(value, iIndex, false);
  }
  
  public static
  Multimedia getAttrMultimedia(HttpServletRequest request, String sAttributeName, int iIndex, boolean boEmptyMultimedia)
  {
    Object value = request.getAttribute(sAttributeName);
    return toMultimedia(value, iIndex, boEmptyMultimedia);
  }
  
  // Parametri ---------------------------------------------------------------- //
  
  public static
  int getParamInt(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toInt(value, 0);
  }
  
  public static
  int getParamInt(HttpServletRequest request, String sParamName, int iDefault)
  {
    Object value = request.getParameter(sParamName);
    return toInt(value, iDefault);
  }
  
  public static
  long getParamLong(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toLong(value, 0);
  }
  
  public static
  long getParamLong(HttpServletRequest request, String sParamName, long lDefault)
  {
    Object value = request.getParameter(sParamName);
    return toLong(value, lDefault);
  }
  
  public static
  double getParamDouble(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toDouble(value, 0.0d);
  }
  
  public static
  double getParamDouble(HttpServletRequest request, String sParamName, double dDefault)
  {
    Object value = request.getParameter(sParamName);
    return toDouble(value, dDefault);
  }
  
  public static
  boolean getParamBoolean(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toBoolean(value, false);
  }
  
  public static
  boolean getParamBoolean(HttpServletRequest request, String sParamName, boolean bDefault)
  {
    Object value = request.getParameter(sParamName);
    return toBoolean(value, bDefault);
  }
  
  public static
  String getParamString(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toString(value, null);
  }
  
  public static
  String getParamString(HttpServletRequest request, String sParamName, String sDefault)
  {
    Object value = request.getParameter(sParamName);
    return toString(value, sDefault);
  }
  
  public static
  Date getParamDate(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toDate(value, null);
  }
  
  public static
  Date getParamDate(HttpServletRequest request, String sParamName, Date dDefault)
  {
    Object value = request.getParameter(sParamName);
    return toDate(value, dDefault);
  }
  
  public static
  Calendar getParamCalendar(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toCalendar(value, null);
  }
  
  public static
  Calendar getParamCalendar(HttpServletRequest request, String sParamName, Calendar calDefault)
  {
    Object value = request.getParameter(sParamName);
    return toCalendar(value, calDefault);
  }
  
  public static
  List<?> getParamList(HttpServletRequest request, String sParamName)
  {
    String sValue = request.getParameter(sParamName);
    return WUtil.toList(sValue, false);
  }
  
  public static
  Article getParamArticle(HttpServletRequest request, String sParamName)
  {
    Object value = request.getParameter(sParamName);
    return toArticle(value, CMS.getLanguage(request), false);
  }
  
  public static
  Article getParamArticle(HttpServletRequest request, String sParamName, boolean boEmptyArticle)
  {
    Object value = request.getParameter(sParamName);
    return toArticle(value, CMS.getLanguage(request), boEmptyArticle);
  }
  
  // Sessione ---------------------------------------------------------------- //
  
  public static
  int getSessInt(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toInt(value, 0);
  }
  
  public static
  int getSessInt(HttpServletRequest request, String sAttributeName, int iDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toInt(value, iDefault);
  }
  
  public static
  long getSessLong(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toLong(value, 0);
  }
  
  public static
  long getSessLong(HttpServletRequest request, String sAttributeName, long lDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toLong(value, lDefault);
  }
  
  public static
  double getSessDouble(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toDouble(value, 0.0d);
  }
  
  public static
  double getSessDouble(HttpServletRequest request, String sAttributeName, double dDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toDouble(value, dDefault);
  }
  
  public static
  boolean getSessBoolean(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toBoolean(value, false);
  }
  
  public static
  boolean getSessBoolean(HttpServletRequest request, String sAttributeName, boolean bDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toBoolean(value, bDefault);
  }
  
  public static
  String getSessString(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toString(value, null);
  }
  
  public static
  String getSessString(HttpServletRequest request, String sAttributeName, String sDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toString(value, sDefault);
  }
  
  public static
  Date getSessDate(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toDate(value, null);
  }
  
  public static
  Date getSessDate(HttpServletRequest request, String sAttributeName, Date dDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toDate(value, dDefault);
  }
  
  public static
  Calendar getSessCalendar(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toCalendar(value, null);
  }
  
  public static
  Calendar getSessCalendar(HttpServletRequest request, String sAttributeName, Calendar calDefault)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toCalendar(value, calDefault);
  }
  
  public static
  List<?> getSessList(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return WUtil.toList(value, false);
  }
  
  public static
  int getSessListSize(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    if(value == null) return 0;
    if(value instanceof List) {
      return ((List<?>) value).size();
    }
    return 1;
  }
  
  public static
  Article getSessArticle(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toArticle(value, CMS.getLanguage(request), false);
  }
  
  public static
  Article getSessArticle(HttpServletRequest request, String sAttributeName, boolean boEmptyArticle)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toArticle(value, CMS.getLanguage(request), boEmptyArticle);
  }
  
  public static
  Article getSessArticle(HttpServletRequest request, String sAttributeName, int iIndex)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toArticle(value, CMS.getLanguage(request), iIndex, false);
  }
  
  public static
  Article getSessArticle(HttpServletRequest request, String sAttributeName, int iIndex, boolean boEmptyArticle)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toArticle(value, CMS.getLanguage(request), iIndex, boEmptyArticle);
  }
  
  public static
  Article getSessLastArticle(HttpServletRequest request, boolean boEmptyArticle)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(CMS.SESS_LAST_ARTICLE) : null;
    return toArticle(value, CMS.getLanguage(request), boEmptyArticle);
  }
  
  public static
  Multimedia getSessMultimedia(HttpServletRequest request, String sAttributeName)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toMultimedia(value, false);
  }
  
  public static
  Multimedia getSessMultimedia(HttpServletRequest request, String sAttributeName, boolean boEmptyMultimedia)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toMultimedia(value, boEmptyMultimedia);
  }
  
  public static
  Multimedia getSessMultimedia(HttpServletRequest request, String sAttributeName, int iIndex)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toMultimedia(value, iIndex, false);
  }
  
  public static
  Multimedia getSessMultimedia(HttpServletRequest request, String sAttributeName, int iIndex, boolean boEmptyMultimedia)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(sAttributeName) : null;
    return toMultimedia(value, iIndex, boEmptyMultimedia);
  }
  
  public static
  User getSessUserLogged(HttpServletRequest request)
  {
    HttpSession httpSession = request.getSession(false);
    Object value = httpSession != null ? httpSession.getAttribute(CMS.SESS_USER_LOGGED) : null;
    if(value instanceof User) {
      return (User) value;
    }
    else if(value instanceof Number) {
      try { return CMS.readUser(((Number) value).intValue()); } catch(Throwable th) { return null; }
    }
    else if(value instanceof String) {
      try { return CMS.readUser((String) value); } catch(Throwable th) { return null; }
    }
    return null;
  }
  
  // Altro
  
  public static
  boolean checkAttr(HttpServletRequest request, String sAttributeName)
  {
    Object oAttributeValue = request.getAttribute(sAttributeName);
    if(oAttributeValue == null) return false;
    if(oAttributeValue instanceof List) {
      return ((List<?>) oAttributeValue).size() > 0;
    }
    String sAttributeValue = oAttributeValue.toString();
    return sAttributeValue.length() != 0 && !sAttributeValue.equals("0");
  }
  
  public static
  boolean checkAttr(HttpServletRequest request, String... asAttributes)
  {
    if(asAttributes == null || asAttributes.length == 0) return false;
    for(int i = 0; i < asAttributes.length; i++) {
      boolean boCheck = checkAttr(request, asAttributes[i]);
      if(boCheck) return true;
    }
    return false;
  }
  
  public static
  String getParValue(String sURL, String sParName)
  {
    if(sURL == null || sURL.length() == 0) return null;
    int iStartPar = sURL.indexOf(sParName + "=");
    if(iStartPar < 0) return null;
    int iEndPar = sURL.indexOf('&', iStartPar);
    if(iEndPar < 0) iEndPar = sURL.length();
    int iStartValue = iStartPar + (sParName + "=").length();
    String sValue = sURL.substring(iStartValue, iEndPar);
    return sValue;
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName, int iValue)
  {
    return setPar(request, sParName, String.valueOf(iValue));
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName1, int iValue1, String sParName2, int iValue2)
  {
    String sQueryString = request.getQueryString();
    sQueryString = setPar(sQueryString, sParName1, String.valueOf(iValue1));
    sQueryString = setPar(sQueryString, sParName2, String.valueOf(iValue2));
    return sQueryString;
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName1, int iValue1, String sParName2, int iValue2, String sParName3, int iValue3)
  {
    String sQueryString = request.getQueryString();
    sQueryString = setPar(sQueryString, sParName1, String.valueOf(iValue1));
    sQueryString = setPar(sQueryString, sParName2, String.valueOf(iValue2));
    sQueryString = setPar(sQueryString, sParName3, String.valueOf(iValue3));
    return sQueryString;
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName1, int iValue1, String sParName2, String sValue2)
  {
    String sQueryString = request.getQueryString();
    sQueryString = setPar(sQueryString, sParName1, String.valueOf(iValue1));
    sQueryString = setPar(sQueryString, sParName2, sValue2);
    return sQueryString;
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName1, String sValue1, String sParName2, String sValue2)
  {
    String sQueryString = request.getQueryString();
    sQueryString = setPar(sQueryString, sParName1, sValue1);
    sQueryString = setPar(sQueryString, sParName2, sValue2);
    return sQueryString;
  }
  
  public static
  String setPar(HttpServletRequest request, String sParName, String sValue)
  {
    return setPar(request.getQueryString(), sParName, sValue);
  }
  
  public static
  String setPar(String sQueryString, String sParName, String sValue)
  {
    if(sQueryString == null) sQueryString = "";
    String sToSearch = sParName + "=";
    int iPar = sQueryString.indexOf(sToSearch);
    if(iPar >= 0) {
      int iEnd = sQueryString.indexOf('&', iPar);
      if(iEnd > 0) {
        String sSx = sQueryString.substring(0, iPar + sToSearch.length());
        String sDx = sQueryString.substring(iEnd);
        try {
          sQueryString = sSx + URLEncoder.encode(sValue, "UTF-8") + sDx;
        }
        catch(Exception ex) {
          sQueryString = sSx + sValue + sDx;
        }
      }
      else {
        String sSx = sQueryString.substring(0, iPar + sToSearch.length());
        try {
          sQueryString = sSx + URLEncoder.encode(sValue, "UTF-8");
        }
        catch(Exception ex) {
          sQueryString = sSx + sValue;
        }
      }
    }
    else {
      if(sQueryString.length() == 0) {
        try {
          sQueryString += sParName + "=" + URLEncoder.encode(sValue, "UTF-8");
        }
        catch(Exception ex) {
          sQueryString += sParName + "=" + sValue;
        }
      }
      else {
        try {
          sQueryString += "&" + sParName + "=" + URLEncoder.encode(sValue, "UTF-8");
        }
        catch(Exception ex) {
          sQueryString += "&" + sParName + "=" + sValue;
        }
      }
    }
    return sQueryString;
  }
  
  public static
  int toInt(Object object, int iDefault)
  {
    if(object == null) return iDefault;
    if(object instanceof Number) {
      return ((Number) object).intValue();
    }
    else if(object instanceof Article) {
      return ((Article) object).getId();
    }
    else if(object instanceof Tag) {
      return ((Tag) object).getId();
    }
    else if(object instanceof Multimedia) {
      return ((Multimedia) object).getId();
    }
    else if(object instanceof Page) {
      return ((Page) object).getId();
    }
    else if(object instanceof Place) {
      return ((Place) object).getId();
    }
    else if(object instanceof User) {
      return ((User) object).getId();
    }
    return WUtil.toInt(object, iDefault);
  }
  
  public static
  long toLong(Object object, long lDefault)
  {
    if(object == null) return lDefault;
    if(object instanceof Number) {
      return ((Number) object).longValue();
    }
    else if(object instanceof Date) {
      return ((Date) object).getTime();
    }
    else if(object instanceof Calendar) {
      return ((Calendar) object).getTimeInMillis();
    }
    return WUtil.toLong(object, lDefault);
  }
  
  public static
  double toDouble(Object object, double dDefault)
  {
    if(object == null) return dDefault;
    if(object instanceof Number) {
      return ((Number) object).doubleValue();
    }
    return WUtil.toDouble(object, dDefault);
  }
  
  public static
  boolean toBoolean(Object object, boolean bDefault)
  {
    if(object == null) return bDefault;
    if(object instanceof Boolean) {
      return ((Boolean) object).booleanValue();
    }
    if(object instanceof Number) {
      return ((Number) object).intValue() != 0;
    }
    return WUtil.toBoolean(object, bDefault);
  }
  
  public static
  String toString(Object object, String sDefault)
  {
    if(object == null) return sDefault;
    if(object instanceof String) {
      return (String) object;
    }
    else if(object instanceof Article) {
      String sResult = ((Article) object).getTitle();
      return sResult != null ? sResult : sDefault;
    }
    else if(object instanceof User) {
      String sResult = ((User) object).getDisplayName();
      return sResult != null ? sResult : sDefault;
    }
    return object.toString();
  }
  
  public static
  Date toDate(Object object, Date dDefault)
  {
    if(object == null) return dDefault;
    if(object instanceof Date) {
      return (Date) object;
    }
    else if(object instanceof Calendar) {
      return ((Calendar) object).getTime();
    }
    else if(object instanceof Article) {
      Calendar cal = ((Article) object).getDateCalendar();
      if(cal == null) return dDefault;
      return cal.getTime();
    }
    return WUtil.toDate(object, dDefault);
  }
  
  public static
  Calendar toCalendar(Object object, Calendar cDefault)
  {
    if(object == null) return cDefault;
    if(object instanceof Calendar) {
      return ((Calendar) object);
    }
    else if(object instanceof Date) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(((Date) object).getTime());
      return cal;
    }
    else if(object instanceof Article) {
      Calendar cal = ((Article) object).getDateCalendar();
      if(cal == null) return cDefault;
      return cal;
    }
    return WUtil.toCalendar(object, cDefault);
  }
  
  public static
  Article toArticle(Object object, int iIdLanguage, boolean boEmptyArticle)
  {
    return toArticle(object, iIdLanguage, 0, boEmptyArticle);
  }
  
  public static
  Article toArticle(Object object, int iIdLanguage, int iIndex, boolean boEmptyArticle)
  {
    if(object == null || iIndex < 0) return boEmptyArticle ? new Article() : null;
    if(object instanceof Article) {
      return ((Article) object);
    }
    else if(object instanceof Number) {
      int iId = ((Number) object).intValue();
      if(iId == 0) return boEmptyArticle ? new Article() : null;
      Article result = null;
      try { result = CMS.read(iId, iIdLanguage); } catch(Throwable th) {}
      if(result == null || result.getId() == 0) return boEmptyArticle ? new Article() : null;
      return result;
    }
    else if(object instanceof List) {
      Object item = WUtil.getValueAt(object, iIndex);
      if(item == null) return boEmptyArticle ? new Article() : null;
      return toArticle(item, iIdLanguage, 0, boEmptyArticle);
    }
    int id = WUtil.toInt(object, 0);
    if(id == 0) return boEmptyArticle ? new Article() : null;
    Article result = null;
    try { result = CMS.read(id, iIdLanguage); } catch(Throwable th) {}
    if(result == null || result.getId() == 0) return boEmptyArticle ? new Article() : null;
    return result;
  }
  
  public static
  Multimedia toMultimedia(Object object, boolean boEmptyMultimedia)
  {
    return toMultimedia(object, 0, boEmptyMultimedia);
  }
  
  public static
  Multimedia toMultimedia(Object object, int iIndex, boolean boEmptyMultimedia)
  {
    if(object == null || iIndex < 0) {
      return boEmptyMultimedia ? new Multimedia() : null;
    }
    if(object instanceof Multimedia) {
      return ((Multimedia) object);
    }
    else if(object instanceof Article) {
      Article article = (Article) object;
      Multimedia[] arrayOfMultimedia = article.getMultimedia();
      if(arrayOfMultimedia == null || arrayOfMultimedia.length <= iIndex) {
        return boEmptyMultimedia ? new Multimedia() : null;
      }
      return arrayOfMultimedia[iIndex];
    }
    else if(object instanceof Number) {
      int iId = ((Number) object).intValue();
      if(iId == 0) return boEmptyMultimedia ? new Multimedia() : null;
      return new Multimedia(iId);
    }
    else if(object instanceof List) {
      Object item = WUtil.getValueAt(object, iIndex);
      if(item == null) return boEmptyMultimedia ? new Multimedia() : null;
      return toMultimedia(item, 0, boEmptyMultimedia);
    }
    String sValue = object.toString();
    if(sValue.length() == 0) {
      return boEmptyMultimedia ? new Multimedia() : null;
    }
    int iId = 0;
    try{ iId = Integer.parseInt(sValue); } catch(Exception ex) {}
    if(iId == 0) return boEmptyMultimedia ? new Multimedia() : null;
    return new Multimedia(iId);
  }
}
