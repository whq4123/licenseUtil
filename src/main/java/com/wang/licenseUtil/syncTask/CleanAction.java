package com.wang.licenseUtil.syncTask;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * 删除temp文件夹，释放资源，完成.
 * Created by wangyuhan on 2019/5/21.
 */
public class CleanAction extends Thread {

  private File path;
  private File[] childrenFile;
  private CountDownLatch countDownLatch;

  public CleanAction(File path, File[] childrenFile,
      CountDownLatch countDownLatch) {
    this.path = path;
    this.childrenFile = childrenFile;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for(int i = 0; i < childrenFile.length; i++) {
      childrenFile[i].delete();
    }
    path.delete();
  }

}
