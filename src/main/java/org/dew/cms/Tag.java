package org.dew.cms;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dew.cms.common.ITag;
import org.util.WMap;
import org.util.WUtil;

/**
 * Tag bean. 
 */
public 
class Tag implements Serializable, Cloneable
{
  private static final long serialVersionUID = 87209073028069799L;
  
  private int id;
  private String code;
  private String displayName;
  private boolean preview;
  
  public Tag()
  {
  }
  
  public Tag(int id)
  {
    this.id = id;
    this.displayName = String.valueOf(id);
  }
  
  public Tag(int id, String sDisplayName)
  {
    this.id   = id;
    this.displayName = sDisplayName;
  }
  
  public Tag(Map<String, Object> map)
  {
    if(map == null || map.isEmpty()) return;
    
    WMap wmap = new WMap(map);
    
    id          = wmap.getInt(ITag.sID);
    code        = wmap.getString(ITag.sCODICE);
    displayName = wmap.getString(ITag.sDESC_IN_ART);
    if(displayName == null || displayName.length() < 2) {
      displayName = wmap.getString(ITag.sDESCRIZIONE);
    }
    preview     = wmap.getBoolean(ITag.sANTEPRIMA);
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getCode() {
    return code;
  }
  
  public void setCode(String code) {
    this.code = code;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public boolean isPreview() {
    return preview;
  }
  
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Tag)) return false;
    int objId = ((Tag) obj).getId();
    return objId == id;
  }

  @Override
  public int hashCode() {
    return id;
  }
  
  @Override
  public String toString() {
    return displayName;
  }
  
  @Override
  public Object clone() {
    Tag tag = new Tag();
    tag.setId(id);
    tag.setCode(code);
    tag.setDisplayName(displayName);
    tag.setPreview(preview);
    return tag;
  }
  
  public static 
  List<Tag> toListOfTag(List<?> list, int idLang) 
  {
    int size = list != null ? list.size() : 0;
    List<Tag> listResult = new ArrayList<Tag>(size);
    if(size == 0) return listResult;
    for(int i = 0; i < size; i++) {
      Object item = list.get(i);
      if(item instanceof Map) {
        listResult.add(new Tag(WUtil.toMapObject(item)));
      }
      else if(item instanceof Number) {
        listResult.add(new Tag(((Number) item).intValue()));
      }
      else if(item instanceof String) {
        listResult.add(new Tag(WUtil.toInt(item, 0)));
      }
      else if(item instanceof Tag) {
        listResult.add((Tag) item);
      }
    }
    return listResult;
  }
  
  public static 
  List<Tag> toListOfTag(List<?> list, int idLang, String description) 
  {
    int size = list != null ? list.size() : 0;
    List<Tag> listResult = new ArrayList<Tag>(size);
    if(size == 0) return listResult;
    for(int i = 0; i < size; i++) {
      Object item = list.get(i);
      if(item instanceof Map) {
        listResult.add(new Tag(WUtil.toMapObject(item)));
      }
      else if(item instanceof Number) {
        listResult.add(new Tag(((Number) item).intValue(), description));
      }
      else if(item instanceof String) {
        listResult.add(new Tag(WUtil.toInt(item, 0), description));
      }
      else if(item instanceof Tag) {
        listResult.add((Tag) item);
      }
    }
    return listResult;
  }
}
