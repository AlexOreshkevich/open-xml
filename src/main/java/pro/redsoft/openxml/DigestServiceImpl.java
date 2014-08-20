/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;


import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.Log4JLoggingFactory;
import pro.redsoft.openxml.logging.LoggingService;
import pro.redsoft.openxml.openoffice.Digest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author V.Solovjev
 */
public class DigestServiceImpl implements DigestService {

    private static final DigestLogger LOG = LoggingService.getLogger(DigestServiceImpl.class);
    private Timer timer;
    private Object sync = null;
    private String OOpath;
    private String path;
    private String port;
    private Digest digestInst = null;
    private int delay = 1000 * 2500;
    private Integer portNum;
    private OutputStream out;
    private DigestServiceException digestError;

    DigestServiceImpl() {
    }

    DigestServiceImpl(String OOpath) {
        this.OOpath = OOpath;
    }

    public DigestServiceImpl(String ooPath, String port) {
        this(ooPath);
        setPort(port);
    }

    void setOOpath(String OOpath) {
        this.OOpath = OOpath;
    }

    void processFromPath() throws DigestServiceException {
        processFromPath(path);
        LoggingService.dispose();
        dispose();
    }

    @Override
    public void processFromPath(String path) throws DigestServiceException {
        Exception errExceprion=null;
        LoggingService.init(new Log4JLoggingFactory(path));
        setPath(path);
        File outFile = new File(path + "/3");
        try {
            outFile.createNewFile();
            out = new FileOutputStream(outFile);
            process();
        } catch (Exception e) {
            errExceprion=e;
        }
        finally {
            printVersion();
        }
        if(errExceprion!=null){
            throw new DigestServiceException("Error creating output file.", errExceprion);
        }
    }

    @Override
    public void processFromStream(InputStream source, InputStream der, OutputStream out) throws DigestServiceException {
        LoggingService.init(new Log4JLoggingFactory(path));
        prepareWorkPath();
        createFile(path + "/1", source);
        createFile(path + "/2", der);
        this.out = out;
        process();
    }

    private void createFile(String dest, InputStream in) {
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

    private void prepareWorkPath() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        if (!tmpPath.endsWith("/") && !tmpPath.endsWith("\\")) {
            tmpPath += "/";
        }
        tmpPath += "digest";
        File digestPath = new File(tmpPath);
        if (!digestPath.exists()) {
            digestPath.mkdirs();
        } else if (!digestPath.isDirectory()) {
            digestPath.delete();
            digestPath.mkdirs();
        }
        Random r = new Random();
        while (true) {
            String rndPath = String.valueOf(r.nextLong());
            File targetPath = new File(tmpPath + "/" + rndPath);
            if (!targetPath.exists()) {
                targetPath.mkdirs();
                path = tmpPath + "/" + rndPath;
                break;
            }
        }
    }

    void setPort(String port) {
        this.port = port;
    }

    public void printVersion() {
        FileWriter ff = null;
        try {
            ff = new FileWriter(path + "/4");
            ff.write(DigestMain.getVersion());
            ff.flush();
            ff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void process() throws DigestServiceException {
        if (!checkSettings()) {
            return;
        }
        showInformation();
        try {
            makeDiffDigest();
        } catch (Exception e) {
            throw new DigestServiceException("",e);
        }

    }

    private boolean checkSettings() throws DigestServiceException {
        if (path == null || path.isEmpty()) {
            path = System.getProperty("java.io.tmpdir");
            LOG.info("Work path is null. Set work path :" + path);
        }
        if (OOpath == null) {
            LOG.error("OOPath is empty");
            return false;
        }
        if (out == null) {
            LOG.error("Output is null");
            return false;
        }
        try {
            portNum = Integer.valueOf(port);
        } catch (Exception e) {
            LOG.info("Port invalid. Set default port" + portNum);
        }
        return true;
    }

    private void showInformation() {
        LOG.info("Start");
        LOG.info("diffDigest version :" + DigestMain.getVersion());
        LOG.info("OS name :" + System.getProperty("os.name"));
        LOG.info("user dir :" + System.getProperty("user.dir"));
        LOG.info(OOpath);
        LOG.info(path);
        LOG.info(port);
    }

    public void makeDiffDigest() throws Exception {
        try {
            sync = new Object();
            enableTimer();

            Thread t = new DigestThread();
            t.start();

            while (sync != null) {
                Thread.sleep(100);
            }

            LOG.info("sync null");
            timer.cancel();
            timer.purge();
            timer = null;
            if (t.isAlive()) {
                LOG.info("t.isAlive()");
                t.interrupt();
            }

        } catch (Exception e) {
            LOG.error("", e);
        }
        if (digestError != null) {
            throw digestError;
        }
    }

    private void enableTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (digestInst != null) {
                        digestInst.dispose();
                    }
                    LOG.error("Out off time. Kill soffice");
                    sync = null;
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }, delay);
    }

    private Digest init(String path, Integer port) throws DigestServiceException {
        Digest ret = null;

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            LOG.info("Security maager is ON");
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
            throw new DigestServiceException("", e);
        }
        return ret;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    public void loadFromConfig(String filename) {
        Properties properties = Utils.loadProperties(filename);
        setOOpath(properties.getProperty("OOPath"));
        setPath(properties.getProperty("workPath"));
        setPort(properties.getProperty("port"));
    }

    private void dispose() throws DigestServiceException {

        try {
            digestInst.dispose();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private class DigestThread extends Thread {
        @Override
        public void run() {
            LOG.info("run digest");
            try {
                if (digestInst == null) {

                    digestInst = init(OOpath, portNum);

                }
                if (digestInst != null) {
                    if (!path.endsWith("/") && !path.endsWith("\\")) {
                        path += "/";
                    }
                    digestInst.makeDiff(path + "1", path + "2", out, portNum);

                }
            } catch (DigestServiceException e) {
                digestError = e;
                e.printStackTrace();
            }
            LOG.info("run digest - done");
            sync = null;
            return;
        }
    }
}
