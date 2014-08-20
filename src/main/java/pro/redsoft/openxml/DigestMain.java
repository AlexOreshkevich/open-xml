/**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package pro.redsoft.openxml;

import pro.redsoft.openxml.logging.FileOutLoggerFactory;
import pro.redsoft.openxml.logging.LoggingService;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author V.Solovjev
 */
public class DigestMain {

    private static String version = "36";

    public static void main(String[] args) {
        DigestServiceImpl dgst = new DigestServiceImpl();
        boolean noexit = false;
        if (args.length == 0) {
            try {
                dgst.loadFromConfig("config.properties");
                noexit = true;
            } catch (Exception e) {
                return;
            }
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

        if (args.length > 3 && !args[3].isEmpty()) {
            noexit = true;
        }
        try {
            LoggingService.init(new FileOutLoggerFactory(dgst.getPath()));
            dgst.processFromPath();
        } catch (DigestServiceException e) {
            try {
                OutputStream ff = new FileOutputStream(dgst.getPath() + "/5");
                PrintStream outStream=new PrintStream(ff);
                e.printStackTrace(outStream);
                ff.flush();
                ff.close();
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!noexit) {
            Runtime.getRuntime().exit(0);
        }
    }

    public static DigestService getDigestService(String OOPath) {
        DigestService digestService = new DigestServiceImpl(OOPath);
        return digestService;
    }

    public static DigestService getDigestService(String OOPath, String port) {
        DigestService digestService = new DigestServiceImpl(OOPath, port);
        return digestService;
    }

    public static String getVersion() {
        return version;
    }
}
