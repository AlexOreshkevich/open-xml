package pro.redsoft.openxml.logging;

/**
 * Created by crzang.
 */
public interface LoggingFactory {

    public DigestLogger create(Class clazz);

    void dispose();
}
