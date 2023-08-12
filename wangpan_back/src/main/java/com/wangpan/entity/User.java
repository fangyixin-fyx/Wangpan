package com.wangpan.entity;


public class User {

  private String id;
  private String username;
  private String password;
  private String email;
  private String qqOpenId;
  private String qqIcon;
  private java.sql.Timestamp registrationTime;
  private java.sql.Timestamp lastLoginTime;
  private int state;
  private int useSpace;
  private int totalSpace;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public String getQqOpenId() {
    return qqOpenId;
  }

  public void setQqOpenId(String qqOpenId) {
    this.qqOpenId = qqOpenId;
  }


  public String getQqIcon() {
    return qqIcon;
  }

  public void setQqIcon(String qqIcon) {
    this.qqIcon = qqIcon;
  }


  public java.sql.Timestamp getRegistrationTime() {
    return registrationTime;
  }

  public void setRegistrationTime(java.sql.Timestamp registrationTime) {
    this.registrationTime = registrationTime;
  }


  public java.sql.Timestamp getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(java.sql.Timestamp lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }


  public long getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }


  public long getUseSpace() {
    return useSpace;
  }

  public void setUseSpace(int useSpace) {
    this.useSpace = useSpace;
  }


  public long getTotalSpace() {
    return totalSpace;
  }

  public void setTotalSpace(int totalSpace) {
    this.totalSpace = totalSpace;
  }

}
