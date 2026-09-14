package yancey.openparticle.api.util;

import yancey.openparticle.api.run.logger.Logger;

public class MyLogger implements Logger {

    @Override
    public void info(String str) {
        System.out.print(str);
    }

    @Override
    public void warn(String str, Throwable throwable) {
        System.out.print(str + '\n');
        throwable.printStackTrace();
    }

    @Override
    public void warn(String str) {
        System.err.print(str + '\n');
    }

}
