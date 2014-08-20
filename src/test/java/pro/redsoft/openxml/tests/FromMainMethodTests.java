package pro.redsoft.openxml.tests; /**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pro.redsoft.openxml.DigestMain;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author V.Solovjev
 */
public class FromMainMethodTests extends AbstractDigestTests {

  private DigestConfig digestConfig;

  @Before
  public void setUp() throws Exception {
    digestConfig = DigestConfig.fromConfig("config.properties");
    prepareWorkPath(digestConfig.getPath());
  }

  @After
  public void tearDown() throws Exception {
    checkResult(digestConfig.getPath());
  }

  @Test
  public void testEmptyArgs() throws Exception {
    String[] emptyArgs = new String[ 0 ];
    DigestMain.main(emptyArgs);
  }

  @Test
  public void testGetVersionArgs() throws Exception {
    noError = false;
    needCheckErrorFile = false;
    if(digestConfig.getPath() != null) {
      String[] getVersionArgs = new String[ 1 ];
      getVersionArgs[ 0 ] = digestConfig.getPath();
      DigestMain.main(getVersionArgs);
    }
  }

  @Test
  public void testFromDefaultConfig() throws Exception {
      String[] configArgs = new String[ 4 ];
      configArgs[ 0 ] = digestConfig.getOOpath();
      configArgs[ 1 ] = digestConfig.getPath();
      configArgs[ 2 ] = digestConfig.getPort();
      configArgs[ 3 ] = "true";
      DigestMain.main(configArgs);
  }

  @Test
  public void testWrongOOPath() throws Exception {
    noError = false;
      String[] configArgs = new String[ 4 ];
      configArgs[ 0 ] = digestConfig.getOOpath() + digestConfig.getPath();
      configArgs[ 1 ] = digestConfig.getPath();
      configArgs[ 2 ] = digestConfig.getPort();
      configArgs[ 3 ] = "true";
      DigestMain.main(configArgs);
  }

  @Test
  public void testEmptyWorkDir() throws Exception {
    noError = false;
    if(digestConfig.getPath() != null) {
      clearFiles(digestConfig.getPath());
      testFromDefaultConfig();
    }
  }

  @Test
  public void testEmptyFile1() throws Exception {
    noError = false;
    if(digestConfig.getPath() != null) {
      clearFiles(digestConfig.getPath());
      testWithOneFile("2");
    }
  }

  @Test
  public void testEmptyFile2() throws Exception {
    noError = false;
    if(digestConfig.getPath() != null) {
      clearFiles(digestConfig.getPath());
      testWithOneFile("1");
    }
  }

  private void testWithOneFile(String fileName) throws Exception {
    copyFilesFromResource(digestConfig.getPath(), fileName, fileName);
    testFromDefaultConfig();
  }

}
