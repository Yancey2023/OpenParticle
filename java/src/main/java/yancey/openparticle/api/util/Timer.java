package yancey.openparticle.api.util;

public class Timer {

    private final long timeStart;

    public Timer() {
        this.timeStart = System.currentTimeMillis();
    }

    public long now() {
        return System.currentTimeMillis() - timeStart;
    }

}
