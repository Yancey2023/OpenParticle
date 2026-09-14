package activity;

import yancey.openparticle.api.activity.Activity;
import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.type.ParticleType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityTest extends Activity {


    @Override
    protected File getFile() {
        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\activity1.par");
    }

    @Override
    protected Vec3 getPosition() {
        return new Vec3(0, -55, 0);
//        return new Vec3(0, 0, 0);
    }

    @Override
    protected void createParticle() {
        addParticle(ParticleType.END_ROD.createParticle(20));
        addParticle(ParticleType.END_ROD.createParticle(20).offset(0, 1, 0).tick(20));
//        butterfly(80, 0.2F);
    }

    public void butterfly(int age, float rotateChangeRange) {
        AtomicInteger j = new AtomicInteger();
        addParticle(ParticleType.END_ROD
                        .createParticle(age)
                        //生成蝴蝶
                        .compound(particle -> {
                            List<Particle> particleList = new ArrayList<>();
                            double r = 1;
                            for (double theta = 0; theta < 2 * Math.PI; theta += 0.02) {
                                float r0 = (float) (2 * Math.abs(Math.sin(2 * theta)) + Math.abs(Math.sin(4 * theta)));
                                float x = (float) (r * r0 * Math.sin(theta));
                                float y = (float) (r * r0 * Math.cos(theta));
                                Vec3[] positions = new Vec3[age];
                                for (int i = 0; i < age; i++) {
//                            positions[i] = new Vec3(x, (float) (Math.abs(y) * Math.sin(0.5 * i)), y);
                                    positions[i] = new Vec3(x, (float) (0.1 * i), y);
//                            positions[i] = new Vec3(x, 0, y);
                                }
                                particleList.add(particle
                                        .offset(x, 0, y)
                                        .offset(positions)
                                );
                            }
                            return particleList;
                        })
                        //倾斜45度
                        .rotate(0, 0, (float) Math.PI / 4)
//                .apply(particle -> {
//                    //获得时间
//                    //随机生成每个时刻旋转的角度，获得旋转矩阵
//                    Vec3 rotateFree = getRandomVec3((float) Math.PI);
//                    Vec3 rotateChange = Vec3.ZERO;
//                    Vec3[] rotateList = new Vec3[age];
//                    Matrix[] rotateMatrixList = new Matrix[age];
//                    for (int i = 0; i < age; i++) {
//                        rotateList[i] = rotateFree;
//                        rotateMatrixList[i] = Matrix.rotateXYZ(rotateFree);
//                        if (i % 10 == 0) {
//                            rotateChange = getRandomVec3(rotateChangeRange);
//                        }
//                        rotateFree = rotateFree.add(rotateChange);
//                    }
//                    //算出每个时刻的位移
//                    Vec3 offset = Vec3.ZERO;
//                    Vec3[] offsetList = new Vec3[age];
//                    for (int i = 0; i < age; i++) {
//                        offsetList[i] = offset;
//                        offset = offset.add(rotateMatrixList[i].apply(new Vec3(0.5F, 0, 0)).add(0, 0.03F * i, 0));
//                    }
//                    return particle.rotateFree(rotateList)
//                            .offset(offsetList);
//                })
                        .apply(particle -> particle.offset(getRandomVec3(100)).tick((int) (0.1 * j.getAndIncrement())))
                        .repeat(1)
                        .offset(0, 10, 0)
        );
    }
}