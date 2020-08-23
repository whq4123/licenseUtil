package com.wang.licenseUtil.domain.factory;


import static com.wang.licenseUtil.util.LicenseUtil.UNKNOWN;
import static com.wang.licenseUtil.util.httpsUtil.HttpUtil.REQUEST_ERROR;

import com.wang.licenseUtil.domain.Notice;
import com.wang.licenseUtil.util.LicenseUtil;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.model.Model;
import org.springframework.util.StringUtils;

/**
 * Created by wangyuhan on 2019/5/9.
 */
public class NoticeFactory {

  // TODO wyh: 2019/5/12 确定一下正则表达式的斜线方向。
  public static final String VERSION_PATTERN = "-(\\d){1,4}(\\.(\\d){1,2})*[\\.-]";
  private NoticeFactory() {

  }

  public static Notice createNotice(JarFile jarFile, String licenceId) {
    if (REQUEST_ERROR == licenceId) {
      return null;
    }
    String fullName = jarFile.getName();
    int i = fullName.lastIndexOf("\\");
    String jarFileName = fullName.substring(i + 1);
    String version = getVersion(jarFileName);
    String jarName = LicenseUtil.getJarName(jarFile);
    return new Notice(jarName, version, licenceId);
  }

  public static Notice createNotice(Model pom, String licenceId) {
    if (REQUEST_ERROR == licenceId) {
      return null;
    }
    String jarName = pom.getArtifactId();
    String version =
        StringUtils.isEmpty(pom.getVersion()) ? pom.getParent().getVersion() : pom.getVersion();
    return new Notice(jarName, version, licenceId);
  }


  private static String getVersion(String jarFileName) {
    Pattern pattern = Pattern.compile(VERSION_PATTERN);
    Matcher matcher = pattern.matcher(jarFileName);
    if (matcher.find()) {
      return matcher.group().replaceFirst("^-","").replaceFirst("[\\.-]$","");
    }
    return UNKNOWN;
  }
}
