package yancey.openparticle.api.controller;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.context.Context;
import yancey.openparticle.api.getter.ColorGetter;
import yancey.openparticle.api.getter.PosGetter;
import yancey.openparticle.api.getter.TimeGetter;

public abstract class Controller {

    public int maxAge;
    @NotNull
    public PosGetter posGetter;
    @NotNull
    public ColorGetter colorGetter;
    @NotNull
    public TimeGetter timeGetter;
    private boolean isHaveTime = false;
    private int time = 0;

    public Controller(int maxAge,@NotNull PosGetter posGetter,@NotNull ColorGetter colorGetter,@NotNull TimeGetter timeGetter) {
        this.maxAge = maxAge;
        this.posGetter = posGetter;
        this.colorGetter = colorGetter;
        this.timeGetter = timeGetter;
    }

    public static Controller fromParticle(int particleType,TimeGetter startAge,int maxAge,@NotNull PosGetter posGetter,@NotNull ColorGetter colorGetter){
        return new Controller(maxAge, posGetter, colorGetter, startAge) {
            @Override
            public void run(Context context) {
                context.addParticle(particleType,this);
            }
        };
    }

        public abstract void run(Context context);;

public int getTime(){
        if(!isHaveTime){
            isHaveTime = true;
            time = timeGetter.getTime();
        }
        return time;
    }

}
