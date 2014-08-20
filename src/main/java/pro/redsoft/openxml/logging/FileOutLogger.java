package pro.redsoft.openxml.logging;


import pro.redsoft.openxml.DigestServiceException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by crzang.
 */
public class FileOutLogger implements DigestLogger {
    private final Logger log;

    public FileOutLogger(Class clazz) {
        this.log = Logger.getLogger(clazz.getName());
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void debug(String message) {
        log.fine(message);
    }

    @Override
    public void error(String message) throws DigestServiceException {
        log.severe(message);
        throw new DigestServiceException(message);
    }

    @Override
    public void error(String message, Exception e) throws DigestServiceException {
        log.log(Level.SEVERE,message,e);
        throw new DigestServiceException(message,e);
    }
}
