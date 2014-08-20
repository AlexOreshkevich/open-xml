package pro.redsoft.openxml.logging;

/**
 * Created by crzang.
 */
public class Log4JLoggingFactory implements LoggingFactory {
    public Log4JLoggingFactory(String path) {

    }

    @Override
    public DigestLogger create(Class clazz) {
        return new Log4JLogger(clazz);
    }

    @Override
    public void dispose() {

    }
}
