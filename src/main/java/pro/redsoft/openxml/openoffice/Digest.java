/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.openoffice;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import pro.redsoft.openxml.DigestServiceException;
import pro.redsoft.openxml.connector.BootstrapPipeConnector;
import pro.redsoft.openxml.connector.BootstrapSocketConnector;
import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author crzang
 */
public class Digest {

    static final DigestLogger LOG = LoggingService.getLogger(Digest.class);
    BootstrapSocketConnector connector;
    BootstrapPipeConnector connector1;
    File f1;
    File f2;
    private OOApi oo = null;
    private XComponentContext xContext;
    private XMultiComponentFactory xMCF = null;
    private XComponentLoader xCLoader;
    private XDispatchHelper xDispather;
    private XDesktop xDesktop;
    private Object desktop;

    public void makeDiff(String file1, String file2, OutputStream file3, int port) throws DigestServiceException {
        try {
            long fileSize1 = (new File(file1)).length();
            long fileSize2 = (new File(file2)).length();
            LOG.info("makeDiff. Filesize1 =" + fileSize1 + ", filesize2 =" + fileSize2);
            if (fileSize1 == 0 || fileSize2 == 0) {
                LOG.error("File is empty or null");
                throw new DigestServiceException("File is empty or null");
            }
            if (xCLoader != null) {
                oo = new OOApi(this.xCLoader, this.xDispather);
                oo.load(file2);
                oo.mergeEtalon(file1);
                oo.exportDgst(file3);
            } else {
                LOG.info("xCloader is null");
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            if(ex instanceof DigestServiceException){
                throw (DigestServiceException)ex;
            }
        } finally {
            f1 = new File(file1);
            f2 = new File(file2);

            if (oo != null) {
                oo.close();
            }
        }
    }

    public void dispose() throws DigestServiceException {
        LOG.info("try dispose");

        if (connector != null) {
            connector.disconnect();
        }
        if (connector1 != null) {
            connector1.disconnect();
        }
    }

    private boolean tryInit() throws DigestServiceException {
        try {
            int countFail = 0;
            while (true) {
                //xContext = Bootstrap.defaultBootstrap_InitialComponentContext();
                if (xContext == null) {
                    LOG.error("ERROR: Could not bootstrap default Office.");
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
            LOG.error("", ex);
            return false;
        }
        return true;
    }

    public Object init(String OOPath, int port) throws DigestServiceException {

        try {
            LOG.info("new BootstrapSocketConnector");
            connector = new BootstrapSocketConnector(OOPath, port);
            LOG.info("connect");
            xContext = connector.connect();//BootstrapSocketConnector.bootstrap(OOPath);
            //   xContext=
        } catch (Exception ex) {
            throw new DigestServiceException("",ex);
        }
        LOG.info("init Office variable");
        tryInit();
        return null;
    }
}
