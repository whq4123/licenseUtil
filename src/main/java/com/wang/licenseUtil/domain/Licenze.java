package com.wang.licenseUtil.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyuhan on 2019/5/20.
 */
public class Licenze {
  private String name;
  private boolean haveVersion;
  private String version = "";

  private static final Map<String, String> licenseChangeMap = new HashMap<>();
  static {
    licenseChangeMap.put("apache", "Apache");
    licenseChangeMap.put("cddl", "CDDL");
    licenseChangeMap.put("lgpl", "LGPL");
    licenseChangeMap.put("gpl", "GPL");
    licenseChangeMap.put("epl", "EPL");
    licenseChangeMap.put("bsd", "BSD");
    licenseChangeMap.put("mit", "MIT");
    licenseChangeMap.put("public", "PublicDomain");
    licenseChangeMap.put("bouncycastle", "BouncyCastle");
  }

  public Licenze() {
  }

  public Licenze(String name, boolean haveVersion) {
    this.name = name;
    this.haveVersion = haveVersion;
  }

  public Licenze(String name, boolean haveVersion, String version) {
    this.name = name;
    this.haveVersion = haveVersion;
    this.version = version;
  }

  @Override
  public String toString() {
    String realName = licenseChangeMap.containsKey(name) ? licenseChangeMap.get(name) : name;
    if (haveVersion) {
      return realName + "-" + version;
    } else {
      return realName;
    }
  }
}
