package com.wang.licenseUtil;

import com.wang.licenseUtil.domain.Notice;
import com.wang.licenseUtil.syncTask.CleanAction;
import com.wang.licenseUtil.syncTask.LicenseAction;
import com.wang.licenseUtil.syncTask.XmlAction;
import com.wang.licenseUtil.util.LicenseUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Created by wangyuhan on 2019/5/6.
 */
public class LicenseUtilMain {

  private static final BlockingQueue<Notice> noticeQueue = new LinkedBlockingQueue<>();

  public static void main(String[] args)
      throws IOException, XmlPullParserException, NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
    String zipFilePath = "C:\\Users\\wangyuhan\\Desktop\\xresmgr_svn0build20190512802V1.1.0\\xresmgr_1.1.0.zip";
    File file = new File(zipFilePath);

//    //1、在当前目录建立隐藏文件夹temp，解压zip包到改隐藏文件夹中。
    String path = LicenseUtilMain.class.getResource("/").getPath();
    String tempDir = path + "temp/";
    File tempDirectory = new File(tempDir);
    tempDirectory.mkdir();
    String s = "attrib " + "+H" + " \"" + tempDirectory.getAbsolutePath() + "\"";
    Runtime.getRuntime().exec(s);

    ZipFile zipFile = new ZipFile(file);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry zipEntry = entries.nextElement();
      if (inLibFile(zipEntry)) {  //lib文件夹下的jar
        File entryFile = new File(tempDir + getJarName(zipEntry.getName()));
        boolean succeed = entryFile.createNewFile();
        if (succeed) {
          InputStream inputStream = zipFile.getInputStream(zipEntry);
          FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
          byte[] bytes = new byte[2048];  // 一次读写2048个字节
          int len;
          while ((len = inputStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, len);
          }
          IOUtils.closeQuietly(fileOutputStream);
          IOUtils.closeQuietly(inputStream);
        }
      }
    }

    //2、扫描temp文件夹中的各个路径，分析每一个jar文件。对第三方jar判断license。
    File[] jars = tempDirectory.listFiles();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    XmlAction xmlAction = new XmlAction(noticeQueue, path, countDownLatch);
    CleanAction cleanAction = new CleanAction(tempDirectory, jars, countDownLatch);
    LicenseAction licenseAction = new LicenseAction(jars, noticeQueue, xmlAction);
    ForkJoinPool forkJoinPool = new ForkJoinPool(5);
    forkJoinPool.execute(licenseAction);

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.execute(xmlAction);
    executorService.execute(cleanAction);

    //3、生成license.xml文件，对分析到的license信息及jar信息，写开源软件声明到license.xml中。
    //涉及文件写操作，是否多线程，速度，资源占用，什么方案， 怎么实现。


    //4、对于记录到的请求错误的文件，重新请求一遍。
    LicenseUtil.dealWithErrorPom(noticeQueue, LicenseUtil.errorSet);
    try {
      countDownLatch.await();
      System.out.println("success");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

//    new MyFrame().lauch();
  }


  //util
  private static boolean inLibFile(ZipEntry zipEntry) {
    String entryName = zipEntry.getName();
    return entryName.startsWith("bin/") && entryName.contains("/lib/") && entryName.endsWith(".jar");
  }


  //util
  private static String getJarName(String zipEntryName) {
    int i = zipEntryName.lastIndexOf("/");
    return zipEntryName.substring(i + 1);
  }

}
