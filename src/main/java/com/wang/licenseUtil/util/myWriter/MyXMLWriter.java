package com.wang.licenseUtil.util.myWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Created by wangyuhan on 2019/5/21.
 */
public class MyXMLWriter extends XMLWriter {

  public MyXMLWriter(FileOutputStream fileOutputStream, OutputFormat outputFormat)
      throws UnsupportedEncodingException {
    super(fileOutputStream, outputFormat);
  }

  @Override
  protected void writeDeclaration() throws IOException {
    OutputFormat format = getOutputFormat();
    String encoding = format.getEncoding();

    // Only print of declaration is not suppressed
    if (!format.isSuppressDeclaration()) {
      // Assume 1.0 version
      if (encoding.equals("UTF8")) {
        writer.write("<?xml version=\"1.0\"");

        if (!format.isOmitEncoding()) {
          writer.write(" encoding=\"UTF-8\"");
        }
        writer.write(" standalone=\"yes\"");
        writer.write("?>");
      } else {
        writer.write("<?xml version=\"1.0\"");

        if (!format.isOmitEncoding()) {
          writer.write(" encoding=\"" + encoding + "\"");
        }
        writer.write(" standalone=\"yes\"");
        writer.write("?>");
      }

      if (format.isNewLineAfterDeclaration()) {
        println();
      }
    }
  }
}
