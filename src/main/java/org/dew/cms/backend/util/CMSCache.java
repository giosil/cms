package org.dew.cms.backend.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dew.cms.backend.Page;
import org.dew.cms.backend.Tag;

public 
class CMSCache 
{
  public static Map<String, String>  mapDescCat = new HashMap<String, String>();
  public static Map<String, String>  mapDescSot = new HashMap<String, String>();
  public static Map<String, String>  mapDescTip = new HashMap<String, String>();
  public static Map<String, String>  mapDescPag = new HashMap<String, String>();
  public static Map<String, Tag>     mapCodeTag = new HashMap<String, Tag>();
  public static Map<String, Integer> mapCount   = new HashMap<String, Integer>();
  
  public static Map<String, List<Integer>> mapYears   = new HashMap<String, List<Integer>>();
  public static Map<String, List<Integer>> mapMonths  = new HashMap<String, List<Integer>>();
  
  public static Page home;
  public static Page view;
  public static Map<String, List<Tag>> mapTags     = new HashMap<String, List<Tag>>();
  public static Map<String, List<Tag>> mapTagsPrev = new HashMap<String, List<Tag>>();
  
  public static
  void clear()
  {
    mapDescCat.clear();
    mapDescSot.clear();
    mapDescTip.clear();
    mapDescPag.clear();
    mapCodeTag.clear();
    mapCount.clear();
    
    mapYears.clear();
    mapMonths.clear();
    
    home = null;
    view = null;
    mapTags.clear();
    mapTagsPrev.clear();
  }
}
