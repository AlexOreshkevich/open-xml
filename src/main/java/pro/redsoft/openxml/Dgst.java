/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author John
 */
public class Dgst {

  static final Logger  LOG            = Logger.getLogger(Dgst.class.getName());
  private final String version        = "36";
  private Timer        timer;
  private Object       sync           = null;
  private String       OOpath;
  private String       path;
  private String       port;
  private Digest       digestInst     = null;
  private boolean      disableConsole = true;

  public Dgst() {
  }

  public Dgst(String OOpath, String path, String port) {
    this(OOpath,path);
    this.port = port;
  }

  public Dgst(String OOpath, String path) {
    this(OOpath);
    this.path = path;
  }

  public Dgst(String OOpath) {
    this.OOpath = OOpath;
  }

  public void setOOpath(String OOpath) {
    this.OOpath = OOpath;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public void enableConsole() {
    disableConsole = false;
  }

  public static void main(String[] args) {
    String OOpath = "";
    String path = "";
    String port = "8113";
    Dgst dgst = new Dgst();
    if (args.length == 0) {
      return;
    }
    if (args.length == 1) {
      dgst.setPath(args[0]);
      dgst.printVersion();
      return;
    }
    if (args.length > 2) {
      dgst.setOOpath(args[0]);
      dgst.setPath(args[1]);
      dgst.setPort(args[2]);
    }
    boolean noexit = false;
    if (args.length > 3 && !args[3].isEmpty()) {
      noexit = true;
    }
    dgst.process();
    if (!noexit) {
      Runtime.getRuntime().exit(0);
    }
  }

  public void printVersion() {
    FileWriter ff = null;
    try {
      ff = new FileWriter(path + "/4");
      ff.write(version);
      ff.flush();
      ff.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void process() {
    if (path == null || path.isEmpty()) {
      path = System.getProperty("java.io.tmpdir");
      LOG.info("Work path is null. Set work path :" + path);
    }
    assert OOpath != null;
    Integer portNum = 8113;
    try {
      portNum = Integer.valueOf(port);
    }
    catch (Exception e) {
      LOG.info("Port invalid. Set default port" + portNum);
    }

    setLogHandlers();
    showInformation();
    try {
      makeDiffDigest(path, OOpath, portNum);
      printVersion();
    }
    catch (Exception ex) {
      LOG.log(Level.SEVERE, "", ex);
    }
    finally {
      LOG.info("Exit 1");
    }
  }

  private void showInformation() {
    LOG.info("Start");
    LOG.info("diffDigest version :" + version);
    LOG.info("OS name :" + System.getProperty("os.name"));
    LOG.info("user dir :" + System.getProperty("user.dir"));
    LOG.info(OOpath);
    LOG.info(path);
    LOG.info(port);
  }

  public void setLogHandlers() {
    try {
      if (disableConsole) {
        for (Handler h : Logger.getLogger("").getHandlers()) {
          Logger.getLogger("").removeHandler(h);
        }
      }
      String logFile = path.equals("") ? System.getProperty("java.io.tmpdir") : path
          + "/digest.log";
      FileHandler handler = new FileHandler(logFile, true);

      Formatter formater = new SimpleFormatter();
      handler.setFormatter(formater);
      Logger.getLogger("").addHandler(handler);

      String errFile = path.equals("") ? System.getProperty("java.io.tmpdir") : path + "/5";
      if (new File(errFile + ".lck").exists()) {
        LOG.severe("agent alreay running");
        Runtime.getRuntime().exit(0);
      }
      FileHandler errhandler = new FileHandler(errFile, false);
      Formatter errformater = new SimpleFormatter();
      errhandler.setFormatter(errformater);
      errhandler.setLevel(Level.SEVERE);
      Logger.getLogger("").addHandler(errhandler);
    }
    catch (Exception ex) {
      Logger.getLogger(Dgst.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void runDigest(String ooPath, String path, int port) {
    digestInst = init(ooPath, port);

    if (digestInst != null) {

      String workPath = path;
      if (!path.endsWith("/") && !path.endsWith("\\")) {
        workPath += "/";
      }
      try {
        digestInst.makeDiff(workPath + "1", workPath + "2", workPath + "3", port);
      }
      catch (Exception e) {
        LOG.log(Level.SEVERE, "", e);
      }
      try {
        digestInst.dispose();
      }
      catch (Exception e) {
        LOG.log(Level.SEVERE, "", e);
      }

    }
  }

  public void makeDiffDigest(final String path, final String ooPath, final Integer port)
      throws Exception {

    try {

      sync = new Object();
      try {
      }
      catch (Exception e) {
      }
      digestInst = null;
      timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            if (digestInst != null) {
              digestInst.dispose();
            }
            LOG.severe("Out off time. Kill soffice");
            sync = null;
            return;
          }
          catch (Exception ex) {
            LOG.log(Level.SEVERE, "", ex);
          }

        }
      }, 1000 * 25);

      Thread t = new Thread() {
        @Override
        public void run() {
          LOG.info("run digest");
          runDigest(ooPath, path, port);
          LOG.info("run digest - done");
          digestInst = null;
          sync = null;
          return;
        }
      };
      t.start();

      while (sync != null) {
        Thread.sleep(100);
      }
      LOG.info("sync null");
      timer.cancel();
      LOG.info("timer.cancel();");
      timer.purge();
      LOG.info("timer.purge();");
      timer = null;
      if (t.isAlive()) {
        LOG.info("t.isAlive()");
        t.interrupt();
      }

    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, null, e);
    }
  }

  public void dispose() {
    /*try {
     LOG.info("try Dispose");
     digestInst.dispose();
     } catch (Exception e) {
     e.printStackTrace();
     }
     ooload = false;
     digestInst = null;
     */
  }

  private static void addUrls(Vector urls, String data, String delimiter) {
    StringTokenizer tokens = new StringTokenizer(data, delimiter);
    while (tokens.hasMoreTokens()) {
      try {
        File f = new File(tokens.nextToken());

        if (f != null) {
          urls.add(f.toURL());
        }
      }
      catch (Exception e) {
        // don't add this class path entry to the list of class loader
        // URLs
        LOG.log(Level.SEVERE, "", e);

      }
    }
  }

  private static void callUnoinfo(String path, Vector urls) {
    Process p;
    try {
      p = Runtime.getRuntime().exec(new String[] { new File(path, "unoinfo").getPath(), "java" });
    }
    catch (IOException e) {
      LOG.log(Level.SEVERE, "", e);
      try {
        p = Runtime.getRuntime().exec(
            new String[] { new File(path, "unoinfo.exe").getPath(), "java" });
      }
      catch (IOException ex) {
        LOG.log(Level.SEVERE, "", ex);
      }
      return;
    }
    new Drain(p.getErrorStream()).start();
    int code;
    byte[] buf = new byte[1000];
    int n = 0;
    try {
      InputStream s = p.getInputStream();
      code = s.read();
      for (;;) {
        if (n == buf.length) {
          if (n > Integer.MAX_VALUE / 2) {
            LOG.log(Level.SEVERE, "com.sun.star.lib.loader.Loader::getCustomLoader:"
                + " too much unoinfo output");

            return;
          }
          byte[] buf2 = new byte[2 * n];
          for (int i = 0; i < n; ++i) {
            buf2[i] = buf[i];
          }
          buf = buf2;
        }
        int k = s.read(buf, n, buf.length - n);
        if (k == -1) {
          break;
        }
        n += k;
      }
    }
    catch (IOException e) {
      LOG.log(Level.SEVERE, "", e);
      return;
    }
    int ev;
    try {
      ev = p.waitFor();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.log(Level.SEVERE, "", e);
      return;
    }
    if (ev != 0) {
      LOG.log(Level.SEVERE, "com.sun.star.lib.loader.Loader::getCustomLoader: unoinfo"
          + " exit value " + n);
      return;
    }
    String s;
    if (code == '0') {
      s = new String(buf);
    }
    else if (code == '1') {
      try {
        s = new String(buf, "UTF-16LE");
      }
      catch (UnsupportedEncodingException e) {
        LOG.log(Level.SEVERE, "", e);
        return;
      }
    }
    else {
      LOG.log(Level.SEVERE, "com.sun.star.lib.loader.Loader::getCustomLoader: bad unoinfo"
          + " output");

      return;
    }
    addUrls(urls, s, "\0");
  }

  private static Digest init(String path, Integer port) {
    Digest ret = null;

    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      LOG.info("Security maager is ON");
      // sm = new SecurityManager();
      // System.setSecurityManager(sm);
      try {

        sm.checkAccept("127.0.0.1", port);
        LOG.info("checkAccept -done");
      }
      catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkConnect("127.0.0.1", port);
        LOG.info("checkConnect -done");
      }
      catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkListen(port);
        LOG.info("checkListen " + port + " -done");
      }
      catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkRead(path);
        LOG.info("checkRead -done");
      }
      catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkExec(path + "/soffice");
        LOG.info("checkExec -done");
      }
      catch (Exception e) {
        LOG.info(e.getMessage());
      }
    }
    else {
      LOG.info("Security maager is OFF");
    }

    try {
      LOG.info("start Init");
      ret = new Digest();
      ret.init(path, port);
    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, "", e);
    }
    return ret;
  }

  public String getPath() {
    return path;
  }
}
