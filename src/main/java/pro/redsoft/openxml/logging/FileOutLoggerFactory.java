package pro.redsoft.openxml.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by crzang.
 */
public class FileOutLoggerFactory implements LoggingFactory {

    private static final Logger LOG = Logger.getLogger(FileOutLoggerFactory.class.getName());
    private final String path;

    public FileOutLoggerFactory(String path) {
        this.path = path;
        setLogHandlers();
    }

    @Override
    public DigestLogger create(Class clazz) {
        return new FileOutLogger(clazz);
    }

    @Override
    public void dispose() {
        freeHandlers();
    }

    public void setLogHandlers() {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            Logger.getLogger("").removeHandler(h);
        }

        String logFile = path + "/digest.log";
        FileHandler handler = null;
        try {
            handler = new FileHandler(logFile, true);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error crate digest.log handler", e);
        }
        if (handler != null) {
            Formatter formater = new SimpleFormatter();
            handler.setFormatter(formater);
            Logger.getLogger("").addHandler(handler);
        }
        String errFile = path + "/5";
        if (new File(errFile + ".lck").exists()) {
            LOG.log(Level.SEVERE, "agent alreay running");
            Runtime.getRuntime().exit(0);
        }
        FileHandler errhandler = null;
        try {
            errhandler = new FileHandler(errFile, false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error crate 5.log handler", e);
        }
        if (errhandler != null) {
            Formatter errformater = new SimpleFormatter();
            errhandler.setFormatter(errformater);
            errhandler.setLevel(Level.SEVERE);
            Logger.getLogger("").addHandler(errhandler);
        }

    }


    public void freeHandlers() {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.close();
        }
    }
}
