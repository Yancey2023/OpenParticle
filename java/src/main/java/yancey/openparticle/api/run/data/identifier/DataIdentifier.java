package yancey.openparticle.api.run.data.identifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class DataIdentifier {

    public final String namespace, value;
    private Object particleSprites;

    public DataIdentifier(String namespace, String value) {
        this.namespace = namespace;
        this.value = value;
    }

    public DataIdentifier(String value) {
        this("minecraft", value);
    }

    public DataIdentifier(DataInputStream dataInputStream) throws IOException {
        this.namespace = dataInputStream.readBoolean() ? "minecraft" : dataInputStream.readUTF();
        this.value = dataInputStream.readUTF();
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        if (Objects.equals(namespace, "minecraft")) {
            dataOutputStream.writeBoolean(true);
        } else {
            dataOutputStream.writeBoolean(false);
            dataOutputStream.writeUTF(namespace);
        }
        dataOutputStream.writeUTF(value);
    }

    @Override
    public String toString() {
        return namespace + ':' + value;
    }
}
