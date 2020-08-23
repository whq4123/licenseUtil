package com.wang.licenseUtil.util.httpsUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.apache.maven.model.Model;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Created by wangyuhan on 2019/5/12.
 */
public class HttpUtil {

  private static final String context = "https://mvnrepository.com/";
  private static final String SPLIT = "/";

  public static final String REQUEST_ERROR = "request error";


  public static String getLicenseFromWeb(Model pomModel)
      throws IOException, NoSuchProviderException, NoSuchAlgorithmException, KeyManagementException {
    URL serverUrl = generateGetUrl(pomModel);
    HttpsURLConnection conn = getConnectionFromURL(serverUrl);
    conn.connect();
    return parseHtml(conn);
  }

  public static String getLicenseFromSearch(String jarName)
      throws IOException, NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
    URL url = new URL(context + "search?q=" + jarName);
    HttpsURLConnection conn = getConnectionFromURL(url);
    conn.connect();
    return parseSearchHtml(conn);

  }

  private static URL generateGetUrl(Model pomModel) throws MalformedURLException {
    String groupId = StringUtils.isEmpty(pomModel.getGroupId()) ? pomModel.getParent().getGroupId() : pomModel.getGroupId();
    String artifactId = pomModel.getArtifactId();
    String version = StringUtils.isEmpty(pomModel.getVersion()) ? pomModel.getParent().getVersion() : pomModel.getVersion();
    return new URL(context + "artifact/" + groupId + SPLIT + artifactId + SPLIT + version);
  }

  private static String parseHtml(HttpsURLConnection connection) throws IOException {
    StringBuffer buffer = new StringBuffer();
    //将返回的输入流转换成字符串
    try (InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      String str = null;
      while ((str = bufferedReader.readLine()) != null) {
        buffer.append(str);
      }
      String result = buffer.toString();
      Document document = Jsoup.parse(result);
      Element page = document.getElementById("page");
      Element maincontent = page.getElementById("maincontent");
      Elements grid = maincontent.getElementsByClass("grid");
      Element span = grid.get(0).getElementsByTag("span").get(0);
      String text = span.text();
      return text;
    } catch (FileNotFoundException e) {
      return "cant find in web";
    }
  }

  private static String parseSearchHtml(HttpsURLConnection connection) throws IOException {
    String license;
    try (InputStream inputStream = connection.getInputStream()) {
      Document document = Jsoup.parse(inputStream, "utf-8", "");
      Elements im = document.getElementsByClass("im");
      if (!CollectionUtils.isEmpty(im)) {
        license = im.get(0).getElementsByClass("b lic im-lic").text();
      } else {
        return "cant find in search";
      }
    }
    return license;
  }

  private static HttpsURLConnection getConnectionFromURL(URL url)
      throws IOException, KeyManagementException, NoSuchProviderException, NoSuchAlgorithmException {
    //创建SSLContext对象，并使用我们指定的信任管理器初始化
    TrustManager[] tm = {new MyX509TrustManager()};
    SSLContext sslContext = SSLContext.getInstance("SSL","SunJSSE");
    sslContext.init(null, tm, new java.security.SecureRandom());

    //从上述SSLContext对象中得到SSLSocketFactory对象
    SSLSocketFactory ssf = sslContext.getSocketFactory();

    //创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setSSLSocketFactory(ssf);


    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-type", "application/json");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36 OPR/58.0.3135.90");

    //必须设置false，否则会自动redirect到重定向后的地址
    conn.setInstanceFollowRedirects(false);
    return conn;
  }
}
