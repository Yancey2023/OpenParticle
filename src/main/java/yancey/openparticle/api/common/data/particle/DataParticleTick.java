package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.node.Node;
import yancey.openparticle.api.common.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

public class DataParticleTick extends DataParticle {

    public final DataParticle dataParticle;
    public final int tickStart;

    public DataParticleTick(DataParticle dataParticle, int tickStart) {
        super();
        if (tickStart < 0) {
            throw new RuntimeException("粒子的时间不可以是负数");
        }
        this.dataParticle = dataParticle;
        this.tickStart = tickStart;
    }

    public DataParticleTick(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
        this.dataParticle = dataParticleManager.getDataParticle(dataInputStream.readInt());
        this.tickStart = dataInputStream.readInt();
    }

    @Override
    public int getType() {
        return TICK;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(dataParticle.id);
        dataOutputStream.writeInt(tickStart);
    }

    @Override
    public Pair<Node, Stream<Node>> getNode(Node parentNode) {
        Pair<Node, Stream<Node>> result = dataParticle.getNode(parentNode);
        result.first.addTickAdd(tickStart);
        return result;
    }

    @Override
    public Stream<DataParticle> getChildren() {
        return Stream.of(dataParticle);
    }
}
