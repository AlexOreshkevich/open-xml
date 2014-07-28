/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.*;

/**
 * @author John
 */
public class dgst {

  static final Logger LOG = Logger.getLogger(dgst.class.getName());
  private static String version = "36";
  private static Timer timer;
  private static Object sync = null;

  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        return;
      }
      if (args.length == 1) {
        FileWriter ff = new FileWriter(args[0] + "/4");
        ff.write(version);
        ff.flush();
        ff.close();
        return;
      }
      String OOpath = "";
      String path = "";
      String port = "8113";
      if (args.length > 0) {
        OOpath = args[0];
      }
      if (args.length > 1) {
        path = args[1];
      }
      if (args.length > 2) {
        port = args[2];
      }
      try {
        for (Handler h : Logger.getLogger("").getHandlers()) {
          Logger.getLogger("").removeHandler(h);
        }
        String logFile = path.equals("") ? System.getProperty("java.io.tmpdir") : path + "/digest.log";
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
      } catch (Exception ex) {
        Logger.getLogger(dgst.class.getName()).log(Level.SEVERE, null, ex);
      }
      LOG.info("Start");
      LOG.info(OOpath);
      LOG.info(path);
      LOG.info(port);

      makeDiffDigest(path, OOpath, Integer.valueOf(port));
      FileWriter ff = new FileWriter(path + "/4");
      ff.write(version);
      ff.flush();
      ff.close();
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, "", ex);
    } finally {
    }
    LOG.info("Exit 1");
    Runtime.getRuntime().exit(0);
  }

  /*
   * @param etalon     - полный путь до исходного файла.
   * @param derivative - полный путь до файла сравнения.
   * @param outXml     - полный путь до результирующего файла дайджеста.
   * @throws Exception
   */
    /* public static void makeDiffDigest(String etalon, String derivative, String outXml) throws Exception {
     if (!ooload) {
     init();
     }
     if (dgst != null) {
     dgst.makeDiff(etalon, etalon, etalon);
     }

     }*/
  public static void printVersion() {
    LOG.info("diffDigest version :" + version + "16.07");
    LOG.info("OS name :" + System.getProperty("os.name"));
    LOG.info("user dir :" + System.getProperty("user.dir"));
  }

  public void setPath(String path, String ooPath) {
    printVersion();
  }

  static Digest dgst = null;

  private static void runDigest(String ooPath, String path, int port) {
    printVersion();

    dgst = init(ooPath, port);

    if (dgst != null) {

      String workPath = path;
      if (!path.endsWith("/") && !path.endsWith("\\")) {
        workPath += "/";
      }
      try {
        dgst.makeDiff(workPath + "1", workPath + "2", workPath + "3", port);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "", e);
      }
      try {
        dgst.dispose();
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "", e);
      }

    }
  }

  public static void makeDiffDigest(final String path, final String ooPath, final Integer port) throws Exception {

    try {

      sync = new Object();
      try {
      } catch (Exception e) {
      }
      dgst = null;
      timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            if (dgst != null) {
              dgst.dispose();
            }
            LOG.severe("Out off time. Kill soffice");
            sync = null;
            return;
          } catch (Exception ex) {
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
          dgst = null;
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

    } catch (Exception e) {
      LOG.log(Level.SEVERE, null, e);
    }
  }

  public void dispose() {
        /*try {
         LOG.info("try Dispose");
         dgst.dispose();
         } catch (Exception e) {
         e.printStackTrace();
         }
         ooload = false;
         dgst = null;
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
      } catch (Exception e) {
        // don't add this class path entry to the list of class loader
        // URLs
        LOG.log(Level.SEVERE, "", e);

      }
    }
  }

  private static void callUnoinfo(String path, Vector urls) {
    Process p;
    try {
      p = Runtime.getRuntime().exec(
          new String[]{new File(path, "unoinfo").getPath(), "java"});
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "", e);
      try {
        p = Runtime.getRuntime().exec(
            new String[]{new File(path, "unoinfo.exe").getPath(), "java"});
      } catch (IOException ex) {
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
      for (; ; ) {
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
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "", e);
      return;
    }
    int ev;
    try {
      ev = p.waitFor();
    } catch (InterruptedException e) {
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
    } else if (code == '1') {
      try {
        s = new String(buf, "UTF-16LE");
      } catch (UnsupportedEncodingException e) {
        LOG.log(Level.SEVERE, "", e);
        return;
      }
    } else {
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
      //sm = new SecurityManager();
      //System.setSecurityManager(sm);
      try {

        sm.checkAccept("127.0.0.1", port);
        LOG.info("checkAccept -done");
      } catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkConnect("127.0.0.1", port);
        LOG.info("checkConnect -done");
      } catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkListen(port);
        LOG.info("checkListen " + port + " -done");
      } catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkRead(path);
        LOG.info("checkRead -done");
      } catch (Exception e) {
        LOG.info(e.getMessage());
      }
      try {
        sm.checkExec(path + "/soffice");
        LOG.info("checkExec -done");
      } catch (Exception e) {
        LOG.info(e.getMessage());
      }
    } else {
      LOG.info("Security maager is OFF");
    }


    try {
      LOG.info("start Init");
      ret = new Digest();
      ret.init(path, port);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "", e);
    }
    return ret;
  }
}
