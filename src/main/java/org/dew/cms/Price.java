package org.dew.cms;

import org.dew.cms.common.IArticolo;
import org.util.WMap;

import java.io.Serializable;
import java.util.Map;

/**
 * Price bean.
 */
public 
class Price implements Serializable
{
  private static final long serialVersionUID = 3095327261228686151L;
  
  private String  code;
  private String  displayName;
  private double  price;
  private double  discountedPrice;
  private int     discount;
  private double  advance;
  private boolean promotion;
  
  public Price()
  {
  }
  
  public Price(Map<String, Object> map)
  {
    if(map == null || map.isEmpty()) return;
    
    WMap wmap = new WMap(map);
    
    code            = wmap.getString(IArticolo.sPREZZO_CODICE);
    displayName     = wmap.getString(IArticolo.sPREZZO_DESCR);
    price           = wmap.getDouble(IArticolo.sPREZZO_PREZZO);
    discountedPrice = wmap.getDouble(IArticolo.sPREZZO_SCONTATO);
    discount        = wmap.getInt(IArticolo.sPREZZO_SCONTO);
    advance         = wmap.getDouble(IArticolo.sPREZZO_ACCONTO);
    promotion       = wmap.getBoolean(IArticolo.sPREZZO_PROMOZ);
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
  
  public double getPrice() {
    return price;
  }
  
  public void setPrice(double price) {
    this.price = price;
  }
  
  public double getDiscountedPrice() {
    return discountedPrice;
  }
  
  public void setDiscountedPrice(double discountedPrice) {
    this.discountedPrice = discountedPrice;
  }
  
  public int getDiscount() {
    return discount;
  }
  
  public void setDiscount(int discount) {
    this.discount = discount;
  }
  
  public double getAdvance() {
    return advance;
  }
  
  public void setAdvance(double advance) {
    this.advance = advance;
  }
  
  public boolean isPromotion() {
    return promotion;
  }
  
  public void setPromotion(boolean promotion) {
    this.promotion = promotion;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Price)) return false;
    String objCode = ((Price) obj).getCode();
    return objCode != null && objCode.equals(code);
  }
  
  @Override
  public int hashCode() {
    return code != null ? code.hashCode() : 0;
  }
  
  @Override
  public String toString() {
    return displayName;
  }
}
