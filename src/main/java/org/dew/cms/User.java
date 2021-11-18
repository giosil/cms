package org.dew.cms;

import java.io.Serializable;

import java.util.Calendar;
import java.util.Map;

import org.util.WMap;

import org.dew.cms.common.IUtente;

/**
 * User bean. 
 */
public 
class User implements Serializable
{
  private static final long serialVersionUID = -7127749844773935074L;
  
  private int id;
  private int type;
  private String userName;
  private String password;
  private String firstName;
  private String lastName;
  private String email;
  private String city;
  private String jobTitle;
  private String sex;
  private Calendar dateOfBirth;
  private boolean enabled = true;
  
  public User()
  {
  }
  
  public User(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
  }
  
  public User(String userName, String password, String email)
  {
    this.userName = userName;
    this.password = password;
    this.email    = email;
  }
  
  public User(Map<String, Object> map)
  {
    if(map == null || map.isEmpty()) return;
    
    WMap wmap = new WMap(map);
    
    id          = wmap.getInt(IUtente.sID);
    type        = wmap.getInt(IUtente.sID_TIPO);
    userName    = wmap.getString(IUtente.sUSERNAME);
    firstName   = wmap.getString(IUtente.sNOME);
    lastName    = wmap.getString(IUtente.sCOGNOME);
    email       = wmap.getString(IUtente.sEMAIL);
    city        = wmap.getString(IUtente.sCITTA);
    jobTitle    = wmap.getString(IUtente.sPROFESSIONE);
    sex         = wmap.getString(IUtente.sSESSO);
    dateOfBirth = wmap.getCalendar(IUtente.sDATA_NASCITA);
    enabled     = wmap.getBoolean(IUtente.sATTIVO, true);
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
  
  public String getUserName() {
    return userName;
  }
  
  public void setUserName(String userName) {
    this.userName = userName;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getCity() {
    return city;
  }
  
  public void setCity(String city) {
    this.city = city;
  }
  
  public String getJobTitle() {
    return jobTitle;
  }
  
  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }
  
  public String getSex() {
    return sex;
  }
  
  public void setSex(String sex) {
    this.sex = sex;
  }
  
  public Calendar getDateOfBirth() {
    return dateOfBirth;
  }
  
  public void setDateOfBirth(Calendar dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public String getDisplayName() {
    String result = "";
    if(firstName != null && firstName.length() > 0) {
      result += firstName;
      if(lastName != null && lastName.length() > 0) {
        result += " " + lastName;
      }
    }
    else {
      result = lastName;
    }
    if(result == null || result.length() == 0) {
      if(userName != null && userName.length() > 0) {
        return userName;
      }
      else if(email != null && email.length() > 0) {
        int iAt = email.indexOf('@');
        if(iAt > 0) {
          return email.substring(0, iAt);
        }
        else {
          return email;
        }
      }
    }
    return result;
  }
  
  public boolean isMale() {
    return sex == null || sex.length() == 0 || sex.startsWith("M") || sex.startsWith("m");
  }
  
  public boolean isFemale() {
    return sex != null && sex.length() > 0 && !sex.startsWith("M") && !sex.startsWith("m");
  }
  
  public boolean isBirthDay() {
    if(dateOfBirth == null) return false;
    Calendar cal = Calendar.getInstance();
    int iCurrMonth  = cal.get(Calendar.MONTH);
    int iCurrDate   = cal.get(Calendar.DATE);
    int iBirthMonth = dateOfBirth.get(Calendar.MONTH);
    int iBirthDate  = dateOfBirth.get(Calendar.DATE);
    return iCurrMonth == iBirthMonth && iCurrDate == iBirthDate;
  }
  
  public boolean isMinor() {
    int iAge = getAge();
    return iAge >= 0 && iAge < 18;
  }
  
  public int getAge() {
    if(dateOfBirth == null) return 0;
    int iY2 = dateOfBirth.get(Calendar.YEAR);
    int iM2 = dateOfBirth.get(Calendar.MONTH) + 1;
    int iD2 = dateOfBirth.get(Calendar.DAY_OF_MONTH);
    Calendar cal = Calendar.getInstance();
    int iY1 = cal.get(Calendar.YEAR);
    int iM1 = cal.get(Calendar.MONTH) + 1;
    int iD1 = cal.get(Calendar.DAY_OF_MONTH);
    int iResult = iY1 - iY2;
    if(iM2 < iM1) {
      return iResult;
    }
    else if(iM2 == iM1 && iD2 <= iD1) {
      return iResult;
    }
    return iResult - 1;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof User)) return false;
    int objId = ((User) obj).getId();
    return objId == id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return userName;
  }
}
