package yancey.openparticle.api.getter;

public abstract class TimeGetter {

    public static TimeGetter get(int time){
        return new TimeGetter() {
            @Override
            public int getTime() {
                return time;
            }
        };
    }

    public abstract int getTime();

}
