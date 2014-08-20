/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.connector;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.XComponentContext;
import pro.redsoft.openxml.openoffice.OOoServer;

/**
 * A Bootstrap Connector which uses a socket to connect to an OOo server.
 */
public class BootstrapSocketConnector extends BootstrapConnector {
  private int port = 8113;

  /**
   * Constructs a bootstrap socket connector which uses the folder of the OOo
   * installation containing the soffice executable.
   *
   * @param oooExecFolder The folder of the OOo installation containing the
   *                      soffice executable
   */
  public BootstrapSocketConnector(String oooExecFolder, int port) {
    super(oooExecFolder);
    this.port = port;
  }

  /**
   * Constructs a bootstrap socket connector which connects to the specified
   * OOo server.
   *
   * @param oooServer The OOo server
   */
  public BootstrapSocketConnector(OOoServer oooServer) {
    super(oooServer);
  }

  /**
   * Connects to an OOo server using a default socket and returns a component
   * context for using the connection to the OOo server.
   *
   * @return The component context
   */
  public XComponentContext connect() throws BootstrapException, Exception {

    // create random pipe name
    String host = "127.0.0.1";
    //int port = 8113;

    return connect(host, port);

  }

  /**
   * Connects to an OOo server using the specified host and port for the
   * socket and returns a component context for using the connection to the
   * OOo server.
   *
   * @param host The host
   * @param port The port
   * @return The component context
   */
  public XComponentContext connect(String host, int port) throws BootstrapException, Exception {

    // host and port
    String hostAndPort = "host=" + host + ",port=" + port;

    // accept option
    String oooAcceptOption = "--accept=socket," + hostAndPort + ";urp;";
    //  if (System.getProperty("os.name").startsWith("Windows")) {
    oooAcceptOption = "-accept=socket," + hostAndPort + ";urp;";
    //   }

    // connection string
    String unoConnectString = "uno:socket," + hostAndPort + ";urp;StarOffice.ComponentContext";

    return connect(oooAcceptOption, unoConnectString, port);
  }

  /**
   * Bootstraps a connection to an OOo server in the specified soffice
   * executable folder of the OOo installation using a default socket and
   * returns a component context for using the connection to the OOo server.
   *
   * @param oooExecFolder The folder of the OOo installation containing the
   *                      soffice executable
   * @return The component context
   */
  public static final XComponentContext bootstrap(String oooExecFolder, int port) throws BootstrapException, Exception {

    BootstrapSocketConnector bootstrapSocketConnector = new BootstrapSocketConnector(oooExecFolder, port);
    return bootstrapSocketConnector.connect();
  }

  /**
   * Bootstraps a connection to an OOo server in the specified soffice
   * executable folder of the OOo installation using the specified host and
   * port for the socket and returns a component context for using the
   * connection to the OOo server.
   *
   * @param oooExecFolder The folder of the OOo installation containing the
   *                      soffice executable
   * @param host          The host
   * @param port          The port
   * @return The component context
   */
  public static final XComponentContext bootstrap(String oooExecFolder, String host, int port) throws BootstrapException, Exception {

    BootstrapSocketConnector bootstrapSocketConnector = new BootstrapSocketConnector(oooExecFolder, port);
    return bootstrapSocketConnector.connect(host, port);
  }
}