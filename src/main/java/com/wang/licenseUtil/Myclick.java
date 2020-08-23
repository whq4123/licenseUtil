package com.wang.licenseUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by wangyuhan on 2019/5/6.
 */
public class Myclick implements ActionListener{
  MyFrame tf ;
  public  Myclick(MyFrame tf )
  {
    this.tf=tf;
  }
  @Override
  public void actionPerformed(ActionEvent e)
  {
    int  d = Integer.valueOf(tf.tf1.getText())+Integer.valueOf( tf.tf2.getText());
    tf.tf3.setText(String.valueOf(d));
  }

}
