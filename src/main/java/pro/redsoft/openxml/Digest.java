/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author crzang
 */
public class Digest {

  static final Logger LOG = Logger.getLogger(Digest.class.getName());
  //private static Digest dgst = null;
  //private static boolean ooInit = false;
  private String oopath = "";
  private OOApi oo = null;
  private XComponentContext xContext;
  private XMultiComponentFactory xMCF = null;
  private XComponentLoader xCLoader;
  private XDispatchHelper xDispather;
  private XDesktop xDesktop;
  private Object desktop;
  BootstrapSocketConnector connector;
  BootstrapPipeConnector connector1;
  File f1;
  File f2;

  public void makeDiff(String file1, String file2, String file3, int port) {
    try {
      long fileSize1 = (new File(file1)).length();
      long fileSize2 = (new File(file2)).length();
      LOG.info("makeDiff. Filesize1 =" + fileSize1 + ", filesize2 =" + fileSize2);
      if (xCLoader != null) {
        oo = new OOApi(this.xCLoader, this.xDispather);
        oo.load(file2);
        oo.mergeEtalon(file1);
        (new File(file3)).createNewFile();
        oo.exportDgst(file3);
      } else {
        LOG.info("xCloader is null");
      }
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, "", ex);
    } finally {
      f1 = new File(file1);
      f2 = new File(file2);

      if (oo != null) {
        oo.close();
      }
    }
  }

  public void dispose() {
    LOG.info("try dispose");

    if (connector != null) {
      connector.disconnect();
    }
    if (connector1 != null) {
      connector1.disconnect();
    }
  }

  public void test() {
    if (tryInit()) {
      LOG.info("OpenOffice work");
    } else {
      LOG.info("OpenOffice not work");
    }
  }

  private boolean tryInit() {
    try {
      int countFail = 0;
      while (true) {
        //xContext = Bootstrap.defaultBootstrap_InitialComponentContext();
        if (xContext == null) {
          LOG.log(Level.SEVERE, "ERROR: Could not bootstrap default Office.");
        }
        xMCF = xContext.getServiceManager();
        desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        xCLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);
        xDesktop = (XDesktop) UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, desktop);
        Object dispatchHelper = xMCF.createInstanceWithContext("com.sun.star.frame.DispatchHelper", xContext);
        xDispather = (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, dispatchHelper);
        PropertyValue[] szArgs = new PropertyValue[1];
        szArgs[0] = new PropertyValue();
        szArgs[0].Name = "Hidden";
        szArgs[0].Value = Boolean.TRUE;
        String strDoc = "private:factory/swriter";
        XComponent xComp = xCLoader.loadComponentFromURL(strDoc, "_blank", 0, szArgs);
        XTextDocument xDoc = UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class, xComp);
        if (xDoc != null) {
          LOG.info("tryInit xDoc!=null");
          xComp.dispose();
          break;
        } else {
          countFail++;
          if (countFail > 20) {
            throw new RuntimeException("Unable to create XTextDocument");
          }
          try {
            Thread.sleep(500);
          } catch (Exception e) {
          }
        }
      }
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, "", ex);
      return false;
    }
    return true;
  }

  Object init(String OOPath, int port) {

    try {
      LOG.info("new BootstrapSocketConnector");
      connector = new BootstrapSocketConnector(OOPath, port);
      LOG.info("connect");
      xContext = connector.connect();//BootstrapSocketConnector.bootstrap(OOPath);
      //   xContext=
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, "", ex);
    }
    LOG.info("init Office variable");
    tryInit();
    return null;
  }
}
