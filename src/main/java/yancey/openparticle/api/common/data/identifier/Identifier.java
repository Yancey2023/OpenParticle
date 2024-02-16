package yancey.openparticle.api.common.data.identifier;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.common.OpenParticleAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Identifier {

    @NotNull
    private final String namespace, value;
    private Object particleSprites;

    public Identifier(@NotNull String namespace, @NotNull String value) {
        this.namespace = namespace;
        this.value = value;
    }

    public Identifier(@NotNull String value) {
        this("minecraft", value);
    }

    public Identifier(@NotNull DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readBoolean() ? "minecraft" : dataInputStream.readUTF(), dataInputStream.readUTF());
    }

    public @NotNull String getNamespace() {
        return namespace;
    }

    public @NotNull String getValue() {
        return value;
    }

    public Object getParticleSprites(OpenParticleAPI openParticleAPI) {
        if (particleSprites == null) {
            particleSprites = openParticleAPI.bridge().getParticleSprites(this);
        }
        return particleSprites;
    }

    public void writeToFile(@NotNull DataOutputStream dataOutputStream) throws IOException {
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
