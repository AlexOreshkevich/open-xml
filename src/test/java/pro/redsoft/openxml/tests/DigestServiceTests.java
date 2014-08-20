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
import pro.redsoft.openxml.DigestService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

/**
 * @author V.Solovjev
 */
public class DigestServiceTests extends AbstractDigestTests {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testSimpleRun() throws Exception {
    DigestConfig digestConfig = DigestConfig.fromConfig("config_.properties");
    prepareWorkPath(digestConfig.getPath());
    DigestService digestService = DigestMain.getDigestService(digestConfig.getOOpath(),digestConfig.getPort());
    digestService.processFromPath(digestConfig.getPath());
    checkResult(digestConfig.getPath());
  }

  @Test
  public void testManyRun() throws Exception {
    DigestConfig digestConfig = DigestConfig.fromConfig("config_.properties");
    prepareWorkPath(digestConfig.getPath());
    DigestService digestService = DigestMain.getDigestService(digestConfig.getOOpath(),digestConfig.getPort());
    digestService.processFromPath(digestConfig.getPath());
    checkResult(digestConfig.getPath());

    digestConfig = DigestConfig.fromConfig("config2.properties");
    prepareWorkPath(digestConfig.getPath());
    digestService.processFromPath(digestConfig.getPath());
    checkResult(digestConfig.getPath());
  }

  @Test
  public void testWrongOOPath() throws Exception {
    noError=false;
    needCheckErrorFile=false;
    DigestConfig digestConfig = DigestConfig.fromConfig("config_.properties");
    prepareWorkPath(digestConfig.getPath());
    DigestService digestService = DigestMain.getDigestService(null, digestConfig.getPort());
    digestService.processFromPath(digestConfig.getPath());
    checkResult(digestConfig.getPath());
  }

  @Test
  public void testSimpleFromStream() throws Exception {
    DigestConfig digestConfig = DigestConfig.fromConfig("config_.properties");
    final StringBuilder sb=new StringBuilder();
    OutputStream outputStream=new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        sb.append((char)b);
      }
    };
    DigestService digestService = DigestMain.getDigestService(digestConfig.getOOpath(),digestConfig.getPort());
    digestService.processFromStream(getResourceAsStream("1"),getResourceAsStream("2"),outputStream);
    analizeResultStream(new StringBufferInputStream(sb.toString()));
  }

}
