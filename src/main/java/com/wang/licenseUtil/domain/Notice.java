package com.wang.licenseUtil.domain;


import static com.wang.licenseUtil.util.LicenseUtil.UNKNOWN;

/**
 * Created by wangyuhan on 2019/5/7.
 */
public class Notice {

  private String softwareName;
  private String softwareVersion;
  private String licenseId = UNKNOWN;

  public Notice() {
  }

  public Notice(String softwareName, String softwareVersion, String licenseId) {
    this.softwareName = softwareName;
    this.softwareVersion = softwareVersion;
    this.licenseId = licenseId;
  }

  public String getSoftwareName() {
    return softwareName;
  }

  public void setSoftwareName(String softwareName) {
    this.softwareName = softwareName;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }

  public String getLicenseId() {
    return licenseId;
  }

  public void setLicenseId(String licenseId) {
    this.licenseId = licenseId;
  }
}
