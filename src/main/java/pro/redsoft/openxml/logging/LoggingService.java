package pro.redsoft.openxml.logging;


import pro.redsoft.openxml.DigestServiceException;

/**
 * Created by crzang.
 */
public class LoggingService {

    private static LoggingFactory factory;
    private static LoggingFactory proxyFactory = new ProxyLoggingFactory();

    public static void init(LoggingFactory factory) {
        if(LoggingService.factory==null) {
            LoggingService.factory = factory;
        }
    }

    public static DigestLogger getLogger(Class clazz) {
        return getFactory().create(clazz);
    }

    private static LoggingFactory getFactory() {
        return factory != null ? factory : proxyFactory;
    }

    public static void dispose() {
        factory.dispose();
    }

    private static class ProxyLoggingFactory implements LoggingFactory {

        @Override
        public DigestLogger create(Class clazz) {
            return createLogger(clazz);
        }

        private DigestLogger createLogger(Class clazz) {
            if (factory != null) {
                return factory.create(clazz);
            }
            return new ProxyDigestLogger(clazz);
        }

        @Override
        public void dispose() {
            factory.dispose();
        }

        private static class ProxyDigestLogger implements DigestLogger {
            private final Class clazz;
            private DigestLogger logger;

            public ProxyDigestLogger(Class clazz) {
                this.clazz = clazz;
            }

            @Override
            public void info(String message) {
                if (getLogger() != null) {
                    getLogger().info(message);
                }
            }

            private DigestLogger getLogger() {
                if (factory == null) {
                    return null;
                }
                if (logger == null) {
                    logger = factory.create(clazz);
                }
                return logger;
            }

            @Override
            public void debug(String message) {
                if (getLogger() != null) {
                    getLogger().debug(message);
                }
            }

            @Override
            public void error(String message) throws DigestServiceException {
                if (getLogger() != null) {
                    getLogger().error(message);
                }
            }

            @Override
            public void error(String message, Exception object) throws DigestServiceException {
                if (getLogger() != null) {
                    getLogger().error(message, object);
                }
            }
        }
    }
}
