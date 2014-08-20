package pro.redsoft.openxml.tests; /**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */

import pro.redsoft.openxml.Utils;

import java.util.Properties;

/**
 * @author V.Solovjev
 */
public class DigestConfig {
  private String OOpath;
  private String path;
  private String port;

  private DigestConfig() {
  }

  public static DigestConfig fromConfig(String filename){
    final DigestConfig digestConfig = new DigestConfig();
    try{
    Properties properties= Utils.loadProperties(filename);
      digestConfig.OOpath=properties.getProperty("OOPath");
      digestConfig.path=properties.getProperty("workPath");
      digestConfig.port=properties.getProperty("port");
    }
    catch(Exception e){

    }
    return digestConfig;
  };

  public String getOOpath() {
    return OOpath;
  }

  public String getPath() {
    return path;
  }

  public String getPort() {
    return port;
  }
}
