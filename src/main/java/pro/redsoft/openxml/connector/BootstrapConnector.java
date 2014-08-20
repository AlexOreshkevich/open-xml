/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.connector;

import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.NoConnectException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import pro.redsoft.openxml.DigestServiceException;
import pro.redsoft.openxml.openoffice.OOoServer;
import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A bootstrap connector which establishes a connection to an OOo server.
 * <p/>
 * Most of the source code in this class has been taken from the Java class
 * "Bootstrap.java" (Revision: 1.15) from the UDK projekt (Uno Software Develop-
 * ment Kit) from OpenOffice.org (http://udk.openoffice.org/). The source code
 * is available for example through a browser based online version control
 * access at http://udk.openoffice.org/source/browse/udk/. The Java class
 * "Bootstrap.java" is there available at
 * http://udk.openoffice.org/source/browse/udk/javaunohelper/com/sun/star/comp/helper/Bootstrap.java?view=markup
 * <p/>
 * The idea to develop this BootstrapConnector comes from the blog "Getting
 * started with the OpenOffice.org API part III : starting OpenOffice.org with
 * jars not in the OOo install dir by Wouter van Reeven"
 * (http://technology.amis.nl/blog/?p=1284) and from various posts in the
 * "(Unofficial) OpenOffice.org Forum" at http://www.oooforum.org/ and the
 * "OpenOffice.org Community Forum" at http://user.services.openoffice.org/
 * complaining about "no office executable found!".
 */
public class BootstrapConnector {

    private static final DigestLogger LOG = LoggingService.getLogger(BootstrapConnector.class);
    /**
     * The OOo server.
     */
    private OOoServer oooServer;
    /**
     * The connection string which has ben used to establish the connection.
     */
    private String oooConnectionString;

    /**
     * Constructs a bootstrap connector which uses the folder of the OOo
     * installation containing the soffice executable.
     *
     * @param oooExecFolder The folder of the OOo installation containing the
     *                      soffice executable
     */
    public BootstrapConnector(String oooExecFolder) {

        this.oooServer = new OOoServer(oooExecFolder);
        this.oooConnectionString = null;
    }

    /**
     * Constructs a bootstrap connector which connects to the specified OOo
     * server.
     *
     * @param oooServer The OOo server
     */
    public BootstrapConnector(OOoServer oooServer) {

        this.oooServer = oooServer;
        this.oooConnectionString = null;
    }

    /**
     * Bootstraps a connection to an OOo server in the specified soffice
     * executable folder of the OOo installation using the specified accept
     * option and connection string and returns a component context for using
     * the connection to the OOo server.
     * <p/>
     * The accept option and the connection string should match in connection
     * type and pipe name or host and port to get a connection.
     *
     * @param oooExecFolder       The folder of the OOo installation containing the
     *                            soffice executable
     * @param oooAcceptOption     The accept option
     * @param oooConnectionString The connection string
     * @return The component context
     */
    public static final XComponentContext bootstrap(String oooExecFolder, String oooAcceptOption, String oooConnectionString) throws BootstrapException, Exception {

        BootstrapConnector bootstrapConnector = new BootstrapConnector(oooExecFolder);
        return bootstrapConnector.connect(oooAcceptOption, oooConnectionString, 8113);
    }

    /**
     * Connects to an OOo server using the specified accept option and
     * connection string and returns a component context for using the
     * connection to the OOo server.
     * <p/>
     * The accept option and the connection string should match to get a
     * connection. OOo provides to different types of connections: 1) The socket
     * connection 2) The named pipe connection
     * <p/>
     * To create a socket connection a host and port must be provided. For
     * example using the host "localhost" and the port "8100" the accept option
     * and connection string looks like this: - accept option :
     * -accept=socket,host=localhost,port=8100;urp; - connection string:
     * uno:socket,host=localhost,port=8100;urp;StarOffice.ComponentContext
     * <p/>
     * To create a named pipe a pipe name must be provided. For example using
     * the pipe name "oooPipe" the accept option and connection string looks
     * like this: - accept option : -accept=pipe,name=oooPipe;urp; - connection
     * string: uno:pipe,name=oooPipe;urp;StarOffice.ComponentContext
     *
     * @param oooAcceptOption     The accept option
     * @param oooConnectionString The connection string
     * @return The component context
     */
    public XComponentContext connect(String oooAcceptOption, String oooConnectionString, int port) throws BootstrapException, Exception {

        this.oooConnectionString = oooConnectionString;
        LOG.info("connect :" + oooAcceptOption + "," + oooConnectionString);
        XComponentContext xContext = null;
        XComponentContext xLocalContext = null;
        try {
            // get local context
            xLocalContext = getLocalContext();
            for (int i = 0; i < 10; ++i) {
                int count = 0;
                Process p1 = Runtime.getRuntime().exec("netstat -na");
                InputStream stdout1 = p1.getInputStream();
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(stdout1));
                String line1;
                while ((line1 = reader1.readLine()) != null) {
                    if (line1.contains(String.valueOf(port))
                            && line1.contains("TIME_WAIT")) {
                        LOG.info("Stdout: " + line1);
                        count++;
                    }
                }
                if (count > 1) {
                    Thread.sleep(1000);
                } else {
                    LOG.info("not exist");
                    break;
                }
            }
            oooServer.start(oooAcceptOption);

            LOG.info("initial service manager: ");
            // initial service manager
            XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
            if (xLocalServiceManager == null) {
                throw new BootstrapException("no initial service manager!");
            }

            LOG.info("create a URL resolver: ");
            // create a URL resolver
            XUnoUrlResolver xUrlResolver = UnoUrlResolver.create(xLocalContext);

            // wait until office is started
            LOG.info("wait until office is started: ");
            boolean first = true;

            for (int i = 0; ; ++i) {
                try {
                    //Thread.sleep(50000);
                    Process p = Runtime.getRuntime().exec("netstat -na");
                    InputStream stdout = p.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                    String line;
                    while ((line = reader.readLine()) != null) {

                        if (line.contains(String.valueOf(port))
                                && line.contains("LISTEN")) {
                            LOG.info("Stdout: " + line);
                        }
                    }

                    xContext = getRemoteContext(xUrlResolver);
                    break;
                } catch (com.sun.star.connection.NoConnectException ex) {
                    // Wait 500 ms, then try to connect again, but do not wait
                    // longer than 5 min (= 600 * 500 ms) total:
                    if (first) {
                        //   ex.printStackTrace();
                        first = false;
                    }
                    if (i == 50) {
                        throw new BootstrapException(ex.toString());
                    }
                    Thread.sleep(500);
                }
            }
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            throw e;
        }
        return xContext;
    }

    /**
     * Disconnects from an OOo server using the connection string from the
     * previous connect.
     * <p/>
     * If there has been no previous connect, the disconnects does nothing.
     * <p/>
     * If there has been a previous connect, disconnect tries to terminate the
     * OOo server and kills the OOo server process the connect started.
     */
    public void disconnect() throws DigestServiceException {

        if (oooConnectionString == null) {
            return;
        }

        // call office to terminate itself
        try {
            // get local context
         /*   XComponentContext xLocalContext = getLocalContext();

             // create a URL resolver
             XUnoUrlResolver xUrlResolver = UnoUrlResolver.create(xLocalContext);

             // get remote context
             XComponentContext xRemoteContext = getRemoteContext(xUrlResolver);

             // get desktop to terminate office
             Object desktop = xRemoteContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
             XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
             xDesktop.terminate();*/
        } catch (Exception e) {
            e.printStackTrace();
            // Bad luck, unable to terminate office
        }

        oooServer.kill();
        oooConnectionString = null;
    }

    /**
     * Create default local component context.
     *
     * @return The default local component context
     */
    private XComponentContext getLocalContext() throws BootstrapException, Exception {


        XComponentContext xLocalContext = Bootstrap.createInitialComponentContext(null);
        if (xLocalContext == null) {
            throw new BootstrapException("no local component context!");
        }
        return xLocalContext;
    }

    /**
     * Try to connect to office.
     *
     * @return The remote component context
     */
    private XComponentContext getRemoteContext(XUnoUrlResolver xUrlResolver) throws BootstrapException, ConnectionSetupException, IllegalArgumentException, NoConnectException {

        Object context = xUrlResolver.resolve(oooConnectionString);
        XComponentContext xContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, context);
        if (xContext == null) {
            throw new BootstrapException("no component context!");
        }
        return xContext;
    }
}