package org.dew.cms.backend;

import java.io.Serializable;

import java.util.Map;

import org.dew.cms.common.ILuogo;

import org.util.WMap;

/**
 * Place bean.
 */
public 
class Place implements Serializable
{
  private static final long serialVersionUID = 400287142763291428L;
  
  private int id;
  private int type;
  private String displayName;
  private String address;
  private String zipCode;
  private int idCity;
  private String city;
  private double latitude;
  private double longitude;
  private String info;
  private String webSite;
  private String email;
  private String phoneNumber;
  
  public Place()
  {
  }
  
  public Place(int id)
  {
    this.id = id;
  }
  
  public Place(Map<String, Object> map)
  {
    if(map == null || map.isEmpty()) return;
    
    WMap wmap = new WMap(map);
    
    id = wmap.getInt(ILuogo.sID);
    displayName = wmap.getString(ILuogo.sDESC_IN_ART);
    if(displayName == null || displayName.length() < 2) {
      displayName = wmap.getString(ILuogo.sDESCRIZIONE);
    }
    type        = wmap.getInt(ILuogo.sID_TIPO);
    address     = wmap.getString(ILuogo.sINDIRIZZO);
    zipCode     = wmap.getString(ILuogo.sCAP);
    idCity      = wmap.getInt(ILuogo.sID_COMUNE);
    city        = wmap.getString(ILuogo.sDESC_COMUNE);
    latitude    = wmap.getDouble(ILuogo.sLATITUDINE);
    longitude   = wmap.getDouble(ILuogo.sLONGITUDINE);
    info        = wmap.getString(ILuogo.sINFORMAZIONI);
    webSite     = wmap.getString(ILuogo.sSITO_WEB);
    email       = wmap.getLowerString(ILuogo.sEMAIL);
    phoneNumber = wmap.getString(ILuogo.sTEL_1);
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public String getAddress() {
    return address;
  }
  
  public void setAddress(String address) {
    this.address = address;
  }
  
  public String getZipCode() {
    return zipCode;
  }
  
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }
  
  public int getIdCity() {
    return idCity;
  }
  
  public void setIdCity(int idCity) {
    this.idCity = idCity;
  }
  
  public String getCity() {
    return city;
  }
  
  public void setCity(String city) {
    this.city = city;
  }
  
  public double getLatitude() {
    return latitude;
  }
  
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }
  
  public double getLongitude() {
    return longitude;
  }
  
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
  
  public int getType() {
    return type;
  }
  
  public void setType(int type) {
    this.type = type;
  }
  
  public String getInfo() {
    return info;
  }
  
  public void setInfo(String info) {
    this.info = info;
  }
  
  public String getWebSite() {
    if(webSite == null) return "";
    return webSite;
  }
  
  public void setWebSite(String webSite) {
    this.webSite = webSite;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getPhoneNumber() {
    return phoneNumber;
  }
  
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Place)) return false;
    int objId = ((Place) obj).getId();
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
}
