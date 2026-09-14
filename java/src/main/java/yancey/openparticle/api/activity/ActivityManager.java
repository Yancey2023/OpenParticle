package yancey.openparticle.api.activity;

import yancey.openparticle.api.run.OpenParticleAPI;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {

    private static final List<Activity> activityList = new ArrayList<>();

    public static void add(Class<? extends Activity> activityClass) {
        try {
            activityList.add(activityClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            System.err.println("找不到" + activityClass.getSimpleName() + "的无参构造");
        }
    }

    public static void output(OpenParticleAPI openParticleAPI) {
        activityList.forEach(activity -> activity.output(openParticleAPI, false, false));
    }

    public static void outputAndRun(OpenParticleAPI openParticleAPI, boolean isSaveFile) {
        activityList.forEach(activity -> activity.output(openParticleAPI, true, isSaveFile));
    }


}
