package org.dew.cms;

import org.dew.cms.common.IArticolo;
import org.dew.cms.common.IPagina;
import org.dew.cms.ws.WSPagine;
import org.util.WMap;
import org.util.WUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Page bean. 
 */
public 
class Page implements Serializable
{
  private static final long serialVersionUID = 896859304668191260L;

  private int id;
  private int type;
  private int category;
  private int subcategory;
  private int typeArticle;
  private String displayName;
  private String uRL;
  private int rows;
  private int cols;
  private int view;
  private int[] articles;
  private Page[] components;
  
  private String parameters;
  private String link;
  
  public Page()
  {
  }
  
  public Page(Map<String, Object> map, int idLang)
  {
    if(map == null || map.isEmpty()) return;
    
    WMap wmap = new WMap(map);
    
    id          = wmap.getInt(IPagina.sID);
    type        = wmap.getInt(IPagina.sID_TIPO_PAG);
    category    = wmap.getInt(IPagina.sID_CATEGORIA);
    subcategory = wmap.getInt(IPagina.sID_SOTTOCATEG);
    typeArticle = wmap.getInt(IPagina.sID_TIPO_ART);
    rows        = wmap.getInt(IPagina.sRIGHE);
    cols        = wmap.getInt(IPagina.sCOLONNE);
    view        = wmap.getInt(IPagina.sVISTA);
    displayName = wmap.getString(IPagina.sDESCRIZIONE);
    
    List<?> listArticoli = wmap.getList(IPagina.sARTICOLI);
    if(listArticoli != null && listArticoli.size() > 0) {
      articles = new int[listArticoli.size()];
      for(int i = 0; i < listArticoli.size(); i++) {
        Object item = listArticoli.get(i);
        if(item instanceof Map) {
          articles[i] = WUtil.toInt(((Map<?, ?>) item).get(IArticolo.sID), 0);
        }
        else if(item instanceof Number) {
          articles[i] = ((Number) item).intValue();
        }
        else if(item instanceof String) {
          articles[i] = WUtil.toInt(item, 0);
        }
        else if(item instanceof Article) {
          articles[i] = ((Article) item).getId();
        }
      }
    }
    else {
      articles = new int[0];
    }
    
    String sParams = CMS.PAR_PAGE + "=" + id;
    if(category != 0) {
      sParams += "&" + CMS.PAR_CATEGORY + "=" + category;
      if(subcategory != 0) sParams += "&" + CMS.PAR_SUBCATEGORY + "=" + subcategory;
    }
    if(typeArticle != 0) {
      sParams += "&" + CMS.PAR_TYPE + "=" + typeArticle;
    }
    if(articles != null && articles.length > 0) {
      sParams += "&" + CMS.PAR_ARTICLE + "=" + articles[0];
    }
    uRL = CMS.PAGE_INDEX + "?" + sParams;
    if(displayName != null) {
      int iSep = displayName.indexOf('|');
      if(iSep > 0) {
        link        = displayName.substring(iSep + 1).trim();
        displayName = displayName.substring(0, iSep).trim();
        if(link != null && link.length() > 0 && link.indexOf('@') > 0) {
          link = "mailto:" + link;
        }
      }
    }
    
    List<Map<String, Object>> listPagine = wmap.getListOfMapObject(IPagina.sCOMPONENTI);
    if(listPagine != null && listPagine.size() > 0) {
      components = new Page[listPagine.size()];
      for(int i = 0; i < listPagine.size(); i++) {
        components[i] = new Page(listPagine.get(i), idLang);
      }
    }
    else {
      components = new Page[0];
    }
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public int getType() {
    return type;
  }
  
  public void setType(int type) {
    this.type = type;
  }
  
  public String getDisplayName() {
    if(displayName == null) return "";
    return displayName;
  }
  
  public String getDisplayName(String sDefault) {
    if(displayName == null || displayName.length() == 0) return sDefault;
    return displayName;
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public int getCategory() {
    return category;
  }
  
  public void setCategory(int category) {
    this.category = category;
  }
  
  public int getSubcategory() {
    return subcategory;
  }
  
  public void setSubcategory(int subcategory) {
    this.subcategory = subcategory;
  }
  
  public int getTypeArticle() {
    return typeArticle;
  }
  
  public int getTypeArticle(int defValue) {
    if(typeArticle == 0) return defValue;
    return typeArticle;
  }
  
  public void setTypeArticle(int typeArticle) {
    this.typeArticle = typeArticle;
  }
  
  public int getRows() {
    return rows;
  }
  
  public int getRows(int iDefault) {
    if(rows < 1) return iDefault;
    return rows;
  }
  
  public void setRows(int rows) {
    this.rows = rows;
  }
  
  public int getCols() {
    return cols;
  }
  
  public int getCols(int iDefault) {
    if(cols < 1) return iDefault;
    return cols;
  }
  
  public void setCols(int cols) {
    this.cols = cols;
  }
  
  public int getView() {
    return view;
  }
  
  public void setView(int view) {
    this.view = view;
  }
  
  public String getURL() {
    if(link != null && link.length() > 0) {
      return link;
    }
    if(parameters != null && parameters.length() > 0) {
      if(uRL != null && uRL.length() > 0) {
        if(uRL.indexOf('?') >= 0) {
          return uRL + "&" + parameters;
        }
        else {
          return uRL + "?" + parameters;
        }
      }
    }
    return uRL;
  }
  
  public void setURL(String url) {
    this.uRL = url;
  }
  
  public int[] getArticles() {
    return articles;
  }
  
  public void setArticles(int[] articles) {
    this.articles = articles;
  }
  
  public Page[] getComponents() {
    return components;
  }
  
  public void setComponents(Page[] components) {
    this.components = components;
  }
  
  public Page getComponent(int iIndex) {
    if(components == null || components.length <= iIndex) return null;
    return components[iIndex];
  }
  
  public boolean hasPageListSingle() {
    if(components == null || components.length == 0) return false;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_SINGOLO) continue;
      return true;
    }
    return false;
  }
  
  public Page getPageListSingle(int iIndex, Page defPage) {
    if(components == null || components.length == 0) return defPage;
    int iPage = -1;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_SINGOLO) continue;
      iPage++;
      if(iPage == iIndex) return page;
    }
    return defPage;
  }
  
  public boolean hasPageListMult() {
    if(components == null || components.length == 0) return false;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_MULTIPLO) continue;
      return true;
    }
    return false;
  }
  
  public Page getPageListMult(int iIndex, Page defPage) {
    if(components == null || components.length == 0) return defPage;
    int iPage = -1;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_MULTIPLO) continue;
      iPage++;
      if(iPage == iIndex) return page;
    }
    return defPage;
  }
  
  public boolean hasPageListMain() {
    if(components == null || components.length == 0) return false;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_PRINCIPALE) continue;
      return true;
    }
    return false;
  }
  
  public Page getPageListMain(int iIndex, Page defPage) {
    if(components == null || components.length == 0) return defPage;
    int iPage = -1;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_PRINCIPALE) continue;
      iPage++;
      if(iPage == iIndex) return page;
    }
    return defPage;
  }
  
  public boolean hasPageListHighlights() {
    if(components == null || components.length == 0) return false;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_IN_EVIDENZA) continue;
      return true;
    }
    return false;
  }
  
  public Page getPageListHighlights(int iIndex, Page defPage) {
    if(components == null || components.length == 0) return defPage;
    int iPage = -1;
    for(int i = 0; i < components.length; i++) {
      Page page = components[i];
      if(page.getType() != WSPagine.iTIPO_ELENCO_IN_EVIDENZA) continue;
      iPage++;
      if(iPage == iIndex) return page;
    }
    return defPage;
  }
  
  public int getFirstArticle() {
    if(articles == null || articles.length == 0) return 0;
    return articles[0];
  }
  
  public int getLastArticle() {
    if(articles == null || articles.length == 0) return 0;
    return articles[articles.length - 1];
  }
  
  public String getParameters() {
    return parameters;
  }
  
  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Page)) return false;
    int objId = ((Page) obj).getId();
    return objId == id;
  }
  
  @Override
  public int hashCode() {
    return id;
  }
  
  @Override
  public String toString() {
    if(displayName == null) return "";
    return displayName;
  }
  
  public static 
  List<Page> toListOfPage(List<Map<String, Object>> list, int idLang) 
  {
    int size = list != null ? list.size() : 0;
    List<Page> listResult = new ArrayList<Page>(size);
    if(size == 0) return listResult;
    for(int i = 0; i < size; i++) {
      listResult.add(new Page(list.get(i), idLang));
    }
    return listResult;
  }	
}
