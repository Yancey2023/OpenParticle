package yancey.openparticle.core.util;

import yancey.openparticle.api.common.bridge.Logger;

public class MyLogger implements Logger {

    private final org.slf4j.Logger logger;

    public MyLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String str) {
        logger.info(str);
    }

    @Override
    public void warn(String str, Throwable throwable) {
        logger.warn(str, throwable);
    }

    @Override
    public void warn(String str) {
        logger.warn(str);
    }
}
