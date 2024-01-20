package yancey.openparticle.core.util;

import net.minecraft.network.PacketByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Vec3 {

    public double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(DataInputStream dataInputStream) throws IOException {
        x = dataInputStream.readDouble();
        y = dataInputStream.readDouble();
        z = dataInputStream.readDouble();
    }

    public Vec3(PacketByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    public static Vec3 zero() {
        return new Vec3(0,0,0);
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeDouble(x);
        dataOutputStream.writeDouble(y);
        dataOutputStream.writeDouble(z);
    }

    public void toBuf(PacketByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public Vec3 minus(Vec3 vec3) {
        return new Vec3(x - vec3.x, y - vec3.y, z - vec3.z);
    }

}
