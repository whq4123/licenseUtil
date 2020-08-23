package com.wang.licenseUtil.domain.factory;

import com.wang.licenseUtil.domain.Licenze;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Created by wangyuhan on 2019/5/20.
 */
public class LicenzeFactory {

  private static String LICENSE = "(apache)|(BSD)|(MIT)|(Zlib)|(Libpng)|(CDDL)|(LGPL)|(GPL)|"
      + "(PostgresSQL)|(OpenSSL)|(EPL)|(BouncyCastle)|(Public)";
  private static String SPLIT = "^(-)|(\\s)";

  static public Licenze createLicenze(String license) {
    Pattern licensePattern = Pattern.compile(LICENSE, Pattern.CASE_INSENSITIVE);
    Matcher matcher = licensePattern.matcher(license);
    String version = matcher.replaceFirst("");
    license = license.toLowerCase();
    if (StringUtils.isEmpty(version)) {
      if (license.equalsIgnoreCase("apache")) {  //  这两种情况下有默认版本。
        return new Licenze(license, true, "2.0");
      } else if (license.equalsIgnoreCase("cddl")) {
        return new Licenze(license, true, "1.0");
      } else if (license.equalsIgnoreCase("bsd")) {
        return new Licenze(license, true, "2-Clause");
      } else if (license.equalsIgnoreCase("epl")) {
        return new Licenze(license, true, "1.0");
      }
      return new Licenze(license, false);
    } else{
      Pattern split = Pattern.compile(SPLIT);
      Matcher versionWithSplit = split.matcher(version);
      return new Licenze(matcher.group().toLowerCase(), true, versionWithSplit.replaceAll(""));
    }
  }

}
