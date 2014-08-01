/**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import pro.redsoft.openxml.Dgst;

/**
 * @author V.Solovjev
 */
public class FullExecute {
  @Test
  public void testProdMode() throws Exception {
    testConfig("config.properties");

  }

  public void testConfig(String fileName) {
    Properties properties = loadProperties(fileName);
    String OOpath = properties.getProperty("OOPath");
    String path = properties.getProperty("workPath");
    String port = properties.getProperty("port");
    String noexit = properties.getProperty("noexit");
    String enableConsle=properties.getProperty("enableConsole");
    if (path != null && !path.isEmpty()) {
      copyTestFile(path);
    }
    Dgst dgst = new Dgst(OOpath, path, port);
    if("true".equals(enableConsle)){
      dgst.enableConsole();
    }
    dgst.process();
    checkResult(dgst.getPath());
  }

  private void copyTestFile(String path) {
    InputStream inFile1 = FullExecute.class.getClassLoader().getResourceAsStream("1");
    copyFile(inFile1, path + "/1");
    InputStream inFile2 = FullExecute.class.getClassLoader().getResourceAsStream("2");
    copyFile(inFile2, path + "/2");
  }

  private void copyFile(InputStream in, String dest) {
    try {
      OutputStream out = new FileOutputStream(dest);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void checkResult(String path) {
    File resultFile = new File(path + "/3");
    assertTrue(resultFile.exists());
    assertTrue(resultFile.length() > 0);
    analizeResultFile(resultFile);
    File errorFile = new File(path + "/5");
    assertTrue(errorFile.exists());
    assertTrue(errorFile.length() == 0);
  }

  private void analizeResultFile(File resultFile) {
    XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
    try {
      FileInputStream fileInputStream = new FileInputStream(resultFile);
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fileInputStream,
          "UTF-8");
      boolean isSafeToGetNextXmlElement = true;
      while (isSafeToGetNextXmlElement) {
        if (xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
          if ("BlockCount" == xmlStreamReader.getAttributeValue(null, "name")) {
            String val = xmlStreamReader.getAttributeValue(null, "value");
            assertEquals(val, "2");
            return;
          }
        }
        if (xmlStreamReader.hasNext()) {
          xmlStreamReader.next();
        }
        else {
          isSafeToGetNextXmlElement = false;
          break;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  private Properties loadProperties(String filename) {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = Dgst.class.getClassLoader().getResourceAsStream(filename);
      if (input == null) {
        System.out.println("Sorry, unable to find " + filename);
      }
      prop.load(input);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return prop;
  }
}
