package yancey.openparticle.core.events;

import yancey.openparticle.api.context.Context;
import yancey.openparticle.api.controller.Controller;

public class RunningHandler {
    public final Context context;
    public final Controller[] controllers;
    public boolean isRunning = false;
    private int tick = 0;
    private int which = 0;

    public RunningHandler(Context context, Controller[] controllers) {
        this.controllers = controllers;
        this.context = context;
    }

    public void run() {
        if (controllers != null && controllers.length != 0) {
            isRunning = true;
            tick = Math.min(controllers[0].getTime(), 0);
            which = 0;
            RunningEventManager.INSTANCE.run(this);
        } else if (isRunning) {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        tick = 0;
        which = 0;
        RunningEventManager.INSTANCE.stop(this);
    }

    public void runPerTick() {
        if (controllers == null) {
            stop();
            return;
        }
        while (which < controllers.length) {
            if (tick != controllers[which].getTime()) {
                tick++;
                return;
            }
            controllers[which++].run(context);
        }
        stop();
    }
}
