package yancey.openparticle.core.util;

import java.lang.reflect.Method;

public class CommonUtil {

    private static boolean checkIrisOn() {
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstance = irisApi.getMethod("getInstance");
            getInstance.setAccessible(true);
            Method isShaderPackInUse = irisApi.getMethod("isShaderPackInUse");
            isShaderPackInUse.setAccessible(true);
            return (boolean) isShaderPackInUse.invoke(getInstance.invoke(null));
        } catch (Exception ignored) {
            return false;
        }
    }

}
