/**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package pro.redsoft.openxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author crzang
 */
public class Utils {
  public static Properties loadProperties(String filename) {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = DigestServiceImpl.class.getClassLoader().getResourceAsStream(filename);
      if(input == null) {
        System.out.println("Sorry, unable to find " + filename);
      }
      prop.load(input);
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
    finally {
      if(input != null) {
        try {
          input.close();
        }
        catch(IOException e) {
          e.printStackTrace();
        }
      }
    }
    return prop;
  }
}
