package yancey.openparticle.api.run.logger;

public interface Logger {

    void info(String str);

    void warn(String str, Throwable throwable);

    void warn(String str);

}
