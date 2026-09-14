package yancey.openparticle.api.run.data;

import yancey.openparticle.api.run.controller.ParticleController;
import yancey.openparticle.api.run.data.identifier.DataIdentifier;
import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.data.particle.DataParticleSingle;
import yancey.openparticle.api.run.node.Node;
import yancey.openparticle.api.run.node.NodeTicker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataParticleManager {

    public List<DataIdentifier> dataIdentifierList;
    public List<DataParticle> dataParticleList;
    public DataParticle root;

    public DataParticleManager(DataParticle root) {
        if (root.getType() == DataParticle.COMPOUND) {
            throw new IllegalArgumentException("root data particle cannot be compounded");
        }
        this.root = root;
        dataParticleList = new ArrayList<>();
        root.collect(dataParticleList);
        for (DataParticle dataParticle : dataParticleList) {
            dataParticle.optimize();
        }
        dataParticleList.clear();
        root.collect(dataParticleList);
        for (int i = 0; i < dataParticleList.size(); i++) {
            dataParticleList.get(i).setId(i);
        }
        dataIdentifierList = new ArrayList<>();
        for (DataParticle dataParticle : dataParticleList) {
            if (dataParticle instanceof DataParticleSingle dataParticleSingle) {
                if (!dataIdentifierList.contains(dataParticleSingle.dataIdentifier)) {
                    dataIdentifierList.add(dataParticleSingle.dataIdentifier);
                }
            }
        }
    }

    public DataParticleManager(DataInputStream dataInputStream) throws IOException {
        int size = dataInputStream.readInt();
        dataIdentifierList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dataIdentifierList.add(new DataIdentifier(dataInputStream));
        }
        size = dataInputStream.readInt();
        dataParticleList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dataParticleList.add(DataParticle.readFromFile(this, dataInputStream));
        }
        root = getDataParticle(dataInputStream.readInt());
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(dataIdentifierList.size());
        for (DataIdentifier dataIdentifier : dataIdentifierList) {
            dataIdentifier.writeToFile(dataOutputStream);
        }
        dataOutputStream.writeInt(dataParticleList.size());
        for (DataParticle dataParticle : dataParticleList) {
            dataParticle.writeToFile(this, dataOutputStream);
        }
        dataOutputStream.writeInt(root.id);
    }

    public DataParticle getDataParticle(int id) {
        return dataParticleList.get(id);
    }

    public long getParticleCount() {
        return root.getParticleCount();
    }

    public NodeTicker getNodeTicker() {
        List<DataRunningPerTick> dataRunningList = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        Node rootNode = new Node(root, nodes);
        rootNode.setTickStart(0);
        List<Node> singleNodeList = nodes.stream().filter(node -> !node.isTransformNode).toList();
        for (int i = 0; i < singleNodeList.size(); i++) {
            Node node = singleNodeList.get(i);
            node.id = i;
            ParticleController controller = new ParticleController(node);
            DataRunningPerTick.getFromList(controller.node.tickStart, dataRunningList).controllerList.add(controller);
        }
        dataRunningList.sort(Comparator.comparingInt(dataRunningPerTick -> dataRunningPerTick.tick));
        return new NodeTicker(rootNode, dataRunningList.toArray(new DataRunningPerTick[0]), singleNodeList.size());
    }

}
