package com.wang.licenseUtil.syncTask;

import static com.wang.licenseUtil.util.LicenseUtil.UNKNOWN;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VENDOR_ID;

import com.wang.licenseUtil.domain.Notice;
import com.wang.licenseUtil.domain.factory.NoticeFactory;
import com.wang.licenseUtil.util.LicenseUtil;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.helper.StringUtil;
import org.springframework.util.StringUtils;

/**
 * 一个ForkJoinTask 去生成notice队列， 没有返回值，直接将结果写进一个同步队列中，相当于生产者
 * Created by wangyuhan on 2019/5/20.
 */
public class LicenseAction extends RecursiveAction {

  private File[] jars;
  private BlockingQueue<Notice> noticeQueue;
  private int low;
  private int high;
  private XmlAction xmlAction;

  private static final int THRESHOLD = 10;

  public LicenseAction(File[] jars, BlockingQueue<Notice> noticeQueue, XmlAction xmlAction) {
    this(jars, noticeQueue, 0, jars.length, xmlAction);
  }

  public LicenseAction(File[] jars, BlockingQueue<Notice> noticeQueue, int low, int high) {
    this.jars = jars;
    this.noticeQueue = noticeQueue;
    this.low = low;
    this.high = high;
  }

  public LicenseAction(File[] jars, BlockingQueue<Notice> noticeQueue, int low, int high,
      XmlAction xmlAction) {
    this.jars = jars;
    this.noticeQueue = noticeQueue;
    this.low = low;
    this.high = high;
    this.xmlAction = xmlAction;
  }

  @Override
  protected void compute() {
    if (high - low < THRESHOLD) {
      for (int i = low; i < high; i++) {
        try {
          JarFile jarFile = new JarFile(jars[i]);
          if (thirdPartyJAR(jarFile)) {
            noticeQueue.put(scanFile(jarFile));
          }
        } catch (IOException | XmlPullParserException | KeyManagementException | NoSuchAlgorithmException | NoSuchProviderException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    } else {
      int mid = (low + high) >>> 1;
      invokeAll(new LicenseAction(jars, noticeQueue, low, mid),
          new LicenseAction(jars, noticeQueue, mid, high));
    }
    if (xmlAction != null) {
      xmlAction.setScanAll(true);
    }
  }

  private boolean thirdPartyJAR(JarFile jarFile) throws IOException {
    String jarFileName = jarFile.getName();
    if (jarFileName.contains("\\bic-") || jarFileName.contains("\\hdm-feign-api")|| jarFileName.contains("\\vehicle-main")) {//有几个jar包没有hik标识，但是海康开发的，需要过滤
      return false;
    }

    Manifest manifest = jarFile.getManifest();
    if (null == manifest) {
      return true;
    }
    String vender = (String) manifest.getMainAttributes().get(IMPLEMENTATION_VENDOR_ID);
    return StringUtils.isEmpty(vender) || !vender.contains("hikvision");
  }

  public static Notice scanFile(JarFile jarFile)
      throws IOException, XmlPullParserException, NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
    String licenseId;
    if (!StringUtil.isBlank(licenseId = LicenseUtil.getFromManifest(jarFile))) { //如果能从MANIFEST.MF文件中获取
      return NoticeFactory.createNotice(jarFile, licenseId);
    } else if (!StringUtil.isBlank(licenseId = LicenseUtil.getFromPom(jarFile))) {//如果能从POM文件中获取
      return NoticeFactory.createNotice(jarFile, licenseId);
    } else if (!StringUtil.isBlank(licenseId = LicenseUtil.getFromLicense(jarFile))) {//如果能从license文件中获取
      return NoticeFactory.createNotice(jarFile, licenseId);
    } else if (!StringUtil.isBlank(licenseId = LicenseUtil.getFromWeb(jarFile))) {//如果能从web中获取。
      return NoticeFactory.createNotice(jarFile, licenseId);
    } else if (!StringUtil.isBlank(licenseId = LicenseUtil.getFromSearch(jarFile))) {
      return NoticeFactory.createNotice(jarFile, licenseId);
    } else {
      return NoticeFactory.createNotice(jarFile, UNKNOWN);
    }
  }
}
