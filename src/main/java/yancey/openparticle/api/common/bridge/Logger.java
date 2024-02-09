package yancey.openparticle.api.common.bridge;

public interface Logger {

    void info(String str);

    void warn(String str, Throwable throwable);

    void warn(String str);

}
