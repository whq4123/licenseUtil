package com.wang.licenseUtil.syncTask;

import com.wang.licenseUtil.domain.Notice;
import com.wang.licenseUtil.util.myWriter.MyXMLWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * 将写入到队列中的值输出到文件中，相当于消费者
 * Created by wangyuhan on 2019/5/20.
 */
public class XmlAction extends Thread{

  private BlockingQueue<Notice> noticeQueue;
  private String path;
  private volatile boolean scanAll = false;
  private CountDownLatch countDownLatch; // 用来同步删除temp文件夹的线程。

  public XmlAction(BlockingQueue<Notice> noticeQueue, String path, CountDownLatch countDownLatch) {
    this.noticeQueue = noticeQueue;
    this.path = path;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    Document document = DocumentHelper.createDocument();
    Element licenseNotice = document.addElement("licensenotice");
    licenseNotice.addNamespace("", "http://www.hikvision.com/compomentModel/0.5.0/licensenotice");
    licenseNotice.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    licenseNotice.addAttribute("xsi:schemaLocation", "http://www.hikvision.com/compomentModel/0.5.0/licensenotice ../../schema/0.5.0/licensenotice.xsd");
    licenseNotice.addAttribute("docVersion", "0.5.0");
    Element notices = licenseNotice.addElement("notices");
    XMLWriter xmlWriter = null;
    try {
      while (!scanAll || !noticeQueue.isEmpty()) {
        Notice notice = noticeQueue.poll(1, TimeUnit.SECONDS);
        if (notice == null) {
          if (scanAll) {
            break;
          }else {
            continue;
          }
        }
        Element noticeElement = notices.addElement("notice");
        noticeElement.addAttribute("licenseId", notice.getLicenseId());
        noticeElement.addAttribute("softwareName", notice.getSoftwareName());
        noticeElement.addAttribute("softwareVersion", notice.getSoftwareVersion());
      }
      OutputFormat outputFormat = configOutputFormat();
      xmlWriter = new MyXMLWriter(new FileOutputStream(path + "licensenotice.xml"), outputFormat);
      xmlWriter.setEscapeText(false);
      xmlWriter.write(document);
      countDownLatch.countDown();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private OutputFormat configOutputFormat() {
    OutputFormat outputFormat = OutputFormat.createPrettyPrint();
    // 设置XML编码方式,即是用指定的编码方式保存XML文档到字符串(String),这里也可以指定为GBK或是ISO8859-1  
    outputFormat.setEncoding("UTF-8");
    outputFormat.setSuppressDeclaration(false); //是否生产xml头
    outputFormat.setIndent(true); //设置是否缩进
    outputFormat.setNewlines(true); //设置是否换行
    return outputFormat;
  }

  public boolean isScanAll() {
    return scanAll;
  }

  public void setScanAll(boolean scanAll) {
    this.scanAll = scanAll;
  }
}
