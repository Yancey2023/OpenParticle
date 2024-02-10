package yancey.openparticle.api.common.controller;


import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.ParticleState;

public interface ParticleController {

    ParticleState getParticleState(int tick);

    int getTickStart();

    int getAge();

    Object getParticleSprites(OpenParticleAPI openParticleAPI);

}
