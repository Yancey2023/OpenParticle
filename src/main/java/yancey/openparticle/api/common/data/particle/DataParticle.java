package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.math.Matrix;
import yancey.openparticle.api.common.node.Node;
import yancey.openparticle.api.common.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public abstract class DataParticle {

    public static final int SINGLE = 0;
    public static final int COMPOUND = 1;
    public static final int TICK = 2;
    public static final int OFFSET = 3;
    public static final int ROTATE = 4;
    public static final int COLOR = 5;
    public static int nextId = 0;
    public final int id;

    public DataParticle() {
        this.id = nextId++;
        DataParticleManager.dataParticleList.add(this);
    }

    public DataParticle(DataInputStream dataInputStream) throws IOException {
        this.id = dataInputStream.readInt();
    }

    public static DataParticle readFromFile(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        return switch (type) {
            case SINGLE -> new DataParticleSingle(dataInputStream);
            case COMPOUND -> new DataParticleCompound(dataParticleManager, dataInputStream);
            case TICK -> new DataParticleTick(dataParticleManager, dataInputStream);
            case OFFSET -> new DataParticleOffset(dataParticleManager, dataInputStream);
            case ROTATE -> new DataParticleRotate(dataParticleManager, dataInputStream);
            default -> throw new IllegalStateException("未知的粒子类型: " + type);
        };
    }

    public abstract int getType();

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(getType());
        dataOutputStream.writeInt(id);
    }

    public abstract Stream<DataParticle> getChildren();

    public Stream<DataParticle> streamParticleSingle() {
        return getChildren().flatMap(DataParticle::streamParticleSingle);
    }

    public void collect(List<DataParticle> dataParticleList) {
        dataParticleList.add(this);
        getChildren().forEach(dataParticle -> dataParticle.collect(dataParticleList));
    }

    /**
     * 左边是当前的节点，右边是最底层的节点
     */
    public Pair<Node, Stream<Node>> getNode(Node parentNode) {
        Node node = new Node(this);
        node.setParent(parentNode);
        return new Pair<>(node, getChildren()
                .map(dataParticle -> dataParticle.getNode(node))
                .peek(pair -> pair.first.setParent(node))
                .flatMap(pair -> pair.second));
    }

    public Matrix getPositionMatrix(int tick, int age) {
        return Matrix.ZERO;
    }

    public Integer getColor(int tick, int age) {
        return 0;
    }

    public Matrix getCurrentStaticPosition() {
        return Matrix.ZERO;
    }

    public Integer getCurrentStaticColor() {
        return 0;
    }
}
