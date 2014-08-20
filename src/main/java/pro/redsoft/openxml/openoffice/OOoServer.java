/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.openoffice;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.lib.util.NativeLibraryLoader;
import pro.redsoft.openxml.DigestServiceException;
import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts and stops an OOo server.
 * <p>
 * Most of the source code in this class has been taken from the Java class
 * "Bootstrap.java" (Revision: 1.15) from the UDK projekt (Uno Software Develop-
 * ment Kit) from OpenOffice.org (http://udk.openoffice.org/). The source code
 * is available for example through a browser based online version control
 * access at http://udk.openoffice.org/source/browse/udk/. The Java class
 * "Bootstrap.java" is there available at
 * http://udk.openoffice.org/source/browse/udk/javaunohelper/com/sun/star/comp/helper/Bootstrap.java?view=markup
 */
public class OOoServer {

  static final DigestLogger LOG = LoggingService.getLogger(OOoServer.class);
  /**
   * The OOo server process.
   */
  private Process oooProcess;
  /**
   * The folder of the OOo installation containing the soffice executable.
   */
  private String oooExecFolder;
  /**
   * The options for starting the OOo server.
   */
  private List oooOptions;

  /**
   * Constructs an OOo server which uses the folder of the OOo installation
   * containing the soffice executable and a list of default options to start
   * OOo.
   *
   * @param oooExecFolder The folder of the OOo installation containing the
   *                      soffice executable
   */
  public OOoServer(String oooExecFolder) {

    this.oooProcess = null;
    this.oooExecFolder = oooExecFolder;
    this.oooOptions = getDefaultOOoOptions();
  }

  /**
   * Constructs an OOo server which uses the folder of the OOo installation
   * containing the soffice executable and a given list of options to start
   * OOo.
   *
   * @param oooExecFolder The folder of the OOo installation containing the
   *                      soffice executable
   * @param oooOptions    The list of options
   */
  public OOoServer(String oooExecFolder, List oooOptions) {

    this.oooProcess = null;
    this.oooExecFolder = oooExecFolder;
    this.oooOptions = oooOptions;
  }

  /**
   * Starts an OOo server which uses the specified accept option.
   * <p>
   * The accept option can be used for two different types of connections: 1)
   * The socket connection 2) The named pipe connection
   * <p>
   * To create a socket connection a host and port must be provided. For
   * example using the host "localhost" and the port "8100" the accept option
   * looks like this: - accept option :
   * -accept=socket,host=localhost,port=8100;urp;
   * <p>
   * To create a named pipe a pipe name must be provided. For example using
   * the pipe name "oooPipe" the accept option looks like this: - accept
   * option : -accept=pipe,name=oooPipe;urp;
   *
   * @param oooAcceptOption The accept option
   */
  public void start(String oooAcceptOption) throws BootstrapException, IOException,  DigestServiceException {
    try {
      // find office executable relative to this class's class loader
      String sOffice = System.getProperty("os.name").startsWith("Windows") ? "soffice.exe" : "soffice";

      File fOffice = null;
      URL[] oooExecFolderURL = new URL[]{new File(oooExecFolder).toURI().toURL()};
      URLClassLoader loader = new URLClassLoader(oooExecFolderURL);
      fOffice = NativeLibraryLoader.getResource(loader, sOffice);
      if (fOffice == null) {
        LOG.info("no office executable found. Try append 'program' to path");
        oooExecFolder = oooExecFolder.concat("/program");
        oooExecFolderURL = new URL[]{new File(oooExecFolder).toURI().toURL()};
        loader = new URLClassLoader(oooExecFolderURL);
        fOffice = NativeLibraryLoader.getResource(loader, sOffice);
        if (fOffice == null) {
          throw new DigestServiceException("no office executable found!");
        }
      }

      // create call with arguments
      int arguments = (oooOptions != null) ? oooOptions.size() + 1 : 1;
      if (oooAcceptOption != null) {
        arguments++;
      }

      String[] oooCommand = new String[arguments];
      oooCommand[0] = fOffice.getPath();

      for (int i = 0; i < oooOptions.size(); i++) {
        oooCommand[i + 1] = (String) oooOptions.get(i);
      }

      if (oooAcceptOption != null) {
        oooCommand[arguments - 1] = oooAcceptOption;
      }
      String comm = "";
      for (String ss : oooCommand) {
        comm += ss;
      }

      LOG.info("Start soffice : " + comm);
      oooProcess = Runtime.getRuntime().exec(oooCommand);

      pipe(oooProcess.getInputStream(), "CO> ");
      pipe(oooProcess.getErrorStream(), "CE> ");


      LOG.info("done Start soffice : ");
    } catch (Exception ex) {
        throw new DigestServiceException(oooExecFolder,ex);
    }
  }

  /**
   * Kills the OOo server process from the previous start.
   * <p>
   * If there has been no previous start of the OOo server, the kill does
   * nothing.
   * <p>
   * If there has been a previous start, kill destroys the process.
   */
  public void kill() throws DigestServiceException {
    try {
      LOG.info("KILL");
      if (oooProcess != null) {
        LOG.info("ooProcess exist");
        oooProcess.destroy();
        LOG.info("destroy");
        boolean ex = false;
        for (int i = 0; i < 10; ++i) {
          try {
            oooProcess.exitValue();
            ex = true;
            break;
          } catch (Exception e) {
            LOG.info("error exitValue :" + i);
            Thread.sleep(100);
          }
        }
        LOG.info("exitValue");

        oooProcess = null;
      }
    } catch (Exception ex) {
      LOG.error("", ex);
    }
  }

  private static void pipe(final InputStream in, final String prefix) {
    new Thread("Pipe: " + prefix) {
      @Override
      public void run() {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
          for (; ; ) {
            String s = r.readLine();
            if (s == null) {
              break;
            }
            LOG.info(prefix + s);
          }
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Returns the list of default options.
   *
   * @return The list of default options
   */
  public static List getDefaultOOoOptions() {
    ArrayList options = new ArrayList();
    options.add("-nologo");
    options.add("-nodefault");
    options.add("-norestore");
    options.add("-nocrashreport");
    options.add("-nolockcheck");
    options.add("-invisible");
    return options;
  }
}