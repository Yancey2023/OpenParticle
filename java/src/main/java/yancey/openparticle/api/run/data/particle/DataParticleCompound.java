package yancey.openparticle.api.run.data.particle;

import yancey.openparticle.api.run.data.DataParticleManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataParticleCompound extends DataParticle {

    public DataParticle[] children;

    public DataParticleCompound(DataParticle[] children) {
        this.children = children;
        if (children.length < 1) {
            throw new IllegalArgumentException("children must contain at least one particle");
        }
    }

    public DataParticleCompound(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        this.children = new DataParticle[dataInputStream.readInt()];
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = dataParticleManager.getDataParticle(dataInputStream.readInt());
        }
    }

    @Override
    public int getType() {
        return COMPOUND;
    }

    @Override
    public void writeToFile(DataParticleManager dataParticleManager, DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataParticleManager, dataOutputStream);
        dataOutputStream.writeInt(children.length);
        for (DataParticle dataParticle : children) {
            dataOutputStream.writeInt(dataParticle.id);
        }
    }

    @Override
    public void collect(List<DataParticle> dataParticleList) {
        if (!dataParticleList.contains(this)) {
            Arrays.stream(children).forEach(dataParticle -> dataParticle.collect(dataParticleList));
            dataParticleList.add(this);
        }
    }

    @Override
    public void optimize() {
        List<DataParticle> tasks = new ArrayList<>(List.of(children));
        List<DataParticle> newChildren = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            DataParticle child = tasks.get(i);
            if (child instanceof DataParticleCompound compoundChild) {
                Collections.addAll(tasks, compoundChild.children);
                continue;
            } else if (child instanceof DataParticleTransform transformChild) {
                if (transformChild.tickAdd == 0 && transformChild.dataMatrix == null && transformChild.dataColor == null) {
                    tasks.add(transformChild.child);
                    continue;
                }
            }
            newChildren.add(child);
        }
        children = newChildren.toArray(new DataParticle[0]);
    }

    @Override
    public int getParticleCount() {
        return Arrays.stream(children).mapToInt(DataParticle::getParticleCount).sum();
    }

    @Override
    public void setId(int id) {
        this.id = id;
        for (DataParticle child : children) {
            if (child.id > id) {
                throw new RuntimeException("child id is greater than id");
            }
            if (child.id == -1) {
                throw new RuntimeException("child id is less than id");
            }
        }
    }

}
