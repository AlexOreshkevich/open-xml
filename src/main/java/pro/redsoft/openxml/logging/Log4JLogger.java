package pro.redsoft.openxml.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by crzang.
 */
public class Log4JLogger implements DigestLogger {
    private final Logger logger;

    public Log4JLogger(Class clazz) {
        logger= LogManager.getLogger(clazz);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void error(String message) {
        logger.fatal(message);
    }

    @Override
    public void error(String message, Exception object) {
        logger.fatal(message,object);
    }
}
