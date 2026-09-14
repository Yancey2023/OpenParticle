package activity;

import yancey.openparticle.api.activity.Activity;
import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.run.math.Matrix;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.shape3d.Cuboid;
import yancey.openparticle.api.type.ParticleType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ActivityTest2 extends Activity {


    @Override
    protected File getFile() {
        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\1.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\3150.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\315000.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\1008000.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\3150000.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\10080000.par");
//        return new File("D:\\IDEA\\project\\others\\OpenParticleAPI\\run\\particles.par");
    }

    @Override
    protected Vec3 getPosition() {
//        return new Vec3(0, -30, 0);
        return new Vec3(0, 100, 0);
    }

    @Override
    protected void createParticle() {
        addParticle(ParticleType.END_ROD.createParticle(20).offsetSpeed(Vec3.ZERO, new Vec3(0, 0.1F, 0)));
//        staticTest();
        //3150
//        butterfly(10, 80, 0.2F);
        //315000
//        butterfly(1000, 80, 0.2F);
        //1008000
//        butterfly(3200, 80, 0.2F);
        //3150000
//        butterfly(10000, 80, 0.2F);
        //10080000
//        butterfly(32000, 80, 0.2F);
    }

    public void staticTest() {
        addParticle(ParticleType.END_ROD
                .createParticle(200)
//                .compound(Cuboid.create().shape(50).count(5).type(Cuboid.Type.SOLID).build()));
                .compound(Cuboid.create().shape(50).count(100000).type(Cuboid.Type.SOLID).build()));
//                .compound(Cuboid.create().shape(50).count(300000).type(Cuboid.Type.SOLID).build()));
    }

    public void butterfly(int count, int age, float rotateChangeRange) {
        AtomicReference<Float> tick = new AtomicReference<>(0F);
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
                            positions[i] = new Vec3(x, (float) (Math.abs(y) * Math.sin(0.5 * i)), y);
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
                .apply(particle -> {
                    //随机生成每个时刻旋转的角度，获得旋转矩阵
                    Vec3 rotate = getRandomVec3((float) Math.PI);
                    Vec3 rotateChange = Vec3.ZERO;
                    Vec3[] rotateList = new Vec3[age];
                    Matrix[] rotateMatrixList = new Matrix[age];
                    for (int i = 0; i < age; i++) {
                        rotateList[i] = rotate;
                        rotateMatrixList[i] = Matrix.rotateXYZ(rotate);
                        if (i % 10 == 0) {
                            rotateChange = getRandomVec3(rotateChangeRange);
                        }
                        rotate = rotate.add(rotateChange);
                    }
                    //算出每个时刻的位移
                    Vec3 offset = Vec3.ZERO;
                    Vec3[] offsetList = new Vec3[age];
                    for (int i = 0; i < age; i++) {
                        offsetList[i] = offset;
                        offset = offset.add(rotateMatrixList[i].apply(new Vec3(0.5F, 0, 0)));
                    }
                    return particle
                            .rotateFree(rotateList)
                            .offset(offsetList);
                })
                .apply(particle -> particle.tick(tick.getAndUpdate(v -> v + (age - 1) / count).intValue()))
                .repeat(count)
                .offset(0, 10, 0)
        );
    }
}