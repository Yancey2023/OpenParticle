package yancey.openparticle.core.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import yancey.openparticle.api.activity.ActivityBase;
import yancey.openparticle.api.context.Context;
import yancey.openparticle.api.controller.Controller;
import yancey.openparticle.api.math.Vec3;
import yancey.openparticle.core.events.RunningHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Environment(EnvType.CLIENT)
public class OpenParticleUtil {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static Controller[] cache = null;
    private static ActivityBase activity = null;

    private OpenParticleUtil() {

    }

    public static boolean loadFile(String path) {
        cache = null;
        activity = null;
        try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(path)})) {
            Class<?> initClass = urlClassLoader.loadClass("yancey.openparticle.Main");
            ActivityBase activityBase = (ActivityBase) initClass.getDeclaredConstructor().newInstance();
            Method method = initClass.getMethod("run");
            activity = activityBase;
            return true;
        } catch (MalformedURLException e) {
            LOGGER.warn("找不到该粒子文件 : MalformedURLException", e);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("粒子文件找不到对应的class : ClassNotFoundException", e);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("粒子文件的class找不到method : NoSuchMethodException", e);
        } catch (InvocationTargetException e) {
            LOGGER.warn("粒子文件的class找不到新建示例的方法 : InvocationTargetException", e);
        } catch (InstantiationException e) {
            LOGGER.warn("粒子文件的class不能新建实例 : InstantiationException", e);
        } catch (IllegalAccessException e) {
            LOGGER.warn("粒子文件的class不能新建实例 : IllegalAccessException", e);
        } catch (ClassCastException e) {
            LOGGER.warn("粒子文件的class没有继承ActivityBase : ClassCastException", e);
        } catch (Exception e) {
            LOGGER.warn("粒子文件的加载失败，原因未知", e);
        }
        return false;
    }

    public static void load(ActivityBase activity) {
        cache = null;
        OpenParticleUtil.activity = activity;
    }

    public static void run(Vec3 center, ClientWorld clientWorld) {
        if (activity != null) {
            if(cache == null){
                cache = activity.run(center);
            }
            new RunningHandler(new Context(clientWorld), cache).run();
        }
    }

    public static boolean loadAndRum(Vec3 center, String path, ClientWorld clientWorld) {
        if (loadFile(path)) {
            run(center, clientWorld);
            return true;
        } else {
            return false;
        }
    }

    public static void loadAndRum(Vec3 center, ActivityBase activity, ClientWorld clientWorld) {
        load(activity);
        run(center, clientWorld);
    }

}
