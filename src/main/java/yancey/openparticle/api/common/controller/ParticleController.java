package yancey.openparticle.api.common.controller;


import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.ParticleState;

public interface ParticleController {

    boolean isStatic();

    void prepare(int tick);

    ParticleState getParticleState();

    int getTickStart();

    int getAge();

    Object getParticleSprites(OpenParticleAPI openParticleAPI);

}
