package pro.redsoft.openxml.tests; /**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */

import pro.redsoft.openxml.DigestMain;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author V.Solovjev
 */
public class AbstractDigestTests {
    protected boolean noError = true;
    protected boolean needCheckErrorFile = true;

    protected void prepareWorkPath(String workPath) {
        if (workPath != null) {
            clearFiles(workPath);
            copyTestFile(workPath);
        }
    }

    protected void clearFiles(String workPath) {
        File targetPath = new File(workPath);
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }
        assertTrue(targetPath.isDirectory());
        for (File f : targetPath.listFiles()) {
            f.delete();
        }
        assertTrue(targetPath.listFiles().length == 0);
    }

    private void checkVersion(String workPath, String version) {
        File resultFile = new File(workPath + "/4");
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0);
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(resultFile));
            int data = reader.read();
            StringBuilder sb = new StringBuilder();
            while (data != -1) {
                sb.append((char) data);
                data = reader.read();
            }
            assertEquals(version, sb.toString());
        } catch (Exception e) {
            fail();
        }
    }

    protected void copyTestFile(String path) {
        final String file1 = "1";
        final String file2 = "2";
        copyFilesFromResource(path, file1, file1);
        copyFilesFromResource(path, file2, file2);
    }

    protected void copyFilesFromResource(String path, String sourceFileName, String targetFileName) {
        InputStream inFile1 = getResourceAsStream(sourceFileName);
        copyFile(inFile1, path + "/" + targetFileName);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkResult(String path) {
        if (path != null) {
            if (noError) {
                checkResultFile(path);
            }
            if (needCheckErrorFile) {
                checkErrorFile(path);
            }
            checkVersion(path, DigestMain.getVersion());
        }
    }

    private void checkErrorFile(String path) {
        File errorFile = new File(path + "/5");
        assertTrue(noError ? errorFile.length() == 0 : errorFile.length() != 0);
    }

    private void checkResultFile(String path) {
        File resultFile = new File(path + "/3");
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0);
        analizeResultFile(resultFile);
    }

    private void analizeResultFile(File resultFile) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(resultFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        analizeResultStream(fileInputStream);
    }

    protected void analizeResultStream(InputStream fileInputStream) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        try {

            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fileInputStream, "UTF-8");
            boolean isSafeToGetNextXmlElement = true;
            while (isSafeToGetNextXmlElement) {
                if (xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if ("BlockCount" == xmlStreamReader.getAttributeValue(null, "name")) {
                        String val = xmlStreamReader.getAttributeValue(null, "value");
                        assertEquals(val, "2");
                    }
                }
                if (xmlStreamReader.hasNext()) {
                    xmlStreamReader.next();
                } else {
                    isSafeToGetNextXmlElement = false;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected InputStream getResourceAsStream(String f1) {
        return AbstractDigestTests.class.getClassLoader().getResourceAsStream(f1);
    }
}
