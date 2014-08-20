package pro.redsoft.openxml.logging;

import pro.redsoft.openxml.DigestServiceException;

/**
 * Created by crzang
 */
public interface DigestLogger {

    void info(String message);

    void debug(String message);

    void error(String message) throws DigestServiceException;

    void error(String message, Exception e) throws DigestServiceException ;
}
