package com.wang.licenseUtil.util;


import static com.wang.licenseUtil.domain.factory.NoticeFactory.VERSION_PATTERN;
import static com.wang.licenseUtil.util.httpsUtil.HttpUtil.REQUEST_ERROR;

import com.wang.licenseUtil.domain.Licenze;
import com.wang.licenseUtil.domain.Notice;
import com.wang.licenseUtil.domain.factory.LicenzeFactory;
import com.wang.licenseUtil.domain.factory.NoticeFactory;
import com.wang.licenseUtil.util.httpsUtil.HttpUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Created by wangyuhan on 2019/5/10.
 *
 * 从jar文件中获取license信息的工具类。
 */
public class LicenseUtil {

  private static String LICENSE_TAG = "Bundle-License"; // manifest文件里的协议属性key值。
  public static final String UNKNOWN = "unknown";
  private static String pattern = "(apache)|(BSD.*?((\\s)|(-)[2-3]-clause)?)|(MIT)|(Zlib)|(Libpng)|(CDDL)|(LGPL.*?(-[2-3]\\.[0-1])?)|(GPL.*?(-[2-3]\\.0)?)|"
      + "(PostgresSQL)|(OpenSSL)|(EPL.*?([-\\s][1-2]\\.0)?)|(BouncyCastle)|(public)";   // 要匹配的开源协议正则集合。

  private static final int retryTime = 2; // 失败重试次数
  public static final Set<JarFile> errorSet = new HashSet<>();

  private LicenseUtil() {

  }

  public static String getFromManifest(JarFile jarFile) throws IOException {
    Manifest manifest = jarFile.getManifest();
    if (manifest == null) {
      return null;
    }
    String baseLicense = manifest.getMainAttributes().getValue(LICENSE_TAG);
    return changeLicense(baseLicense);
  }

  public static String getFromPom(JarFile jarFile) throws IOException, XmlPullParserException {
    Model pomModel = null;
    List<License> licenses = new LinkedList<>();
    try {
      pomModel = getPomModel(jarFile);
      licenses = pomModel.getLicenses();
    } catch (NullPointerException e) { // 空指针说明找不到pom文件，直接返回""
      return "";
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    }
    return changeLicense(licenses);
  }

  public static String getFromLicense(JarFile jarFile) throws IOException {
    ZipEntry license = getEntryFromJar(jarFile, "LICENSE");
    if (null == license) {
      return "";
    }
    InputStream inputStream = jarFile.getInputStream(license);
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < 4; i++) { // 读LICENSE文件的前4行，
      sb.append(bufferedReader.readLine());
    }
    return changeLicense(sb.toString());
  }

  public static String getFromWeb(JarFile jarFile) throws XmlPullParserException {
    String license;
    try {
      Model pomModel = getPomModel(jarFile);
      license = HttpUtil.getLicenseFromWeb(pomModel);
    } catch (NullPointerException e) {
      return "";
    } catch (NoSuchProviderException | KeyManagementException | NoSuchAlgorithmException e) {
      license = UNKNOWN;
    } catch (IOException e) {
      if (!errorSet.contains(jarFile)) {
        errorSet.add(jarFile);
      }
      return REQUEST_ERROR;
    }
    return changeLicense(license);
  }

  public static String getFromSearch(JarFile jarFile)
      throws KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
    String license;
    try {
      license = HttpUtil.getLicenseFromSearch(getJarName(jarFile));
      return changeLicense(license);
    } catch (IOException e) {
      errorSet.add(jarFile);
      return REQUEST_ERROR;
    }
  }

  public static void dealWithErrorPom(Queue<Notice> list, Set<JarFile> errorSet)
      throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException, IOException {
    int time = 0;
    for(;!CollectionUtils.isEmpty(errorSet) && time < retryTime; time++) {
      Iterator<JarFile> iterator = errorSet.iterator();
      while (iterator.hasNext()) {
        JarFile jarFile = iterator.next();
        String license = getFromSearch(jarFile);
        Notice notice = NoticeFactory.createNotice(jarFile, license);
        if (notice != null) {
          iterator.remove();
          list.add(notice);
        }
      }
    }
  }

  private static Model getPomModel(JarFile jarFile) throws IOException, XmlPullParserException {
    JarEntry jarEntry = getEntryFromJar(jarFile, "pom.xml");
    InputStream pomInputStream = jarFile.getInputStream(jarEntry);
    MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
    return mavenXpp3Reader.read(pomInputStream);
  }

  private static JarEntry getEntryFromJar(JarFile jarFile, String entryName) {
    Enumeration<JarEntry> entries = jarFile.entries();
    while(entries.hasMoreElements()){
      JarEntry jarEntry = entries.nextElement();
      if (jarEntry.getName().contains(entryName)) {
        return jarEntry;
      }
    }
    return null;
  }

  private static String changeLicense(String baseLicense) {
    if (StringUtils.isEmpty(baseLicense)) {
      return "";
    }
    Pattern compile = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    Matcher license = compile.matcher(baseLicense);
    if (license.find()) {
      Licenze licenze = LicenzeFactory.createLicenze(license.group());
      return licenze.toString();
    } else {
      return "";
    }
  }

  private static String changeLicense(List<License> licenses){
    if (!CollectionUtils.isEmpty(licenses)) {
      String name = licenses.get(0).getName();
      String url = licenses.get(0).getUrl();
      return changeLicense(url+ name);
    }
    return "";
  }

  public static String getJarName(JarFile jarFile) {
    String fullName = jarFile.getName();
    int i = fullName.lastIndexOf("\\");
    String jarFileName = fullName.substring(i + 1);
    String[] split = jarFileName.split(VERSION_PATTERN);
    return split[0].substring(0, split[0].length());
  }
}
