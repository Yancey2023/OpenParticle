package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.identifier.Identifier;
import yancey.openparticle.api.common.data.identifier.IdentifierCache;
import yancey.openparticle.api.common.node.Node;
import yancey.openparticle.api.common.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

public class DataParticleSingle extends DataParticle {

    public Identifier identifier;
    public int age;

    public DataParticleSingle(Identifier identifier, int age) {
        super();
        this.identifier = identifier;
        this.age = age;
    }

    public DataParticleSingle(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
        this.identifier = IdentifierCache.getIdentifier(dataInputStream.readInt());
        this.age = dataInputStream.readInt();
    }

    @Override
    public int getType() {
        return SINGLE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(IdentifierCache.getId(identifier));
        dataOutputStream.writeInt(age);
    }

    @Override
    public Stream<DataParticle> getChildren() {
        return Stream.of();
    }

    @Override
    public Stream<DataParticle> streamParticleSingle() {
        return Stream.of(this);
    }

    @Override
    public Pair<Node, Stream<Node>> getNode() {
        Node node = new Node(this);
        node.setAge(age);
        return new Pair<>(node, Stream.of(node));
    }

    @Override
    public Integer getColor(int tick, int age) {
        return 0xFFFFFFFF;
    }
}
