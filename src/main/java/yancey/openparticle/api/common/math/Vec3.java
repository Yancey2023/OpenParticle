package yancey.openparticle.api.common.math;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Vec3 {

    public static Vec3 ZERO = new Vec3(0, 0, 0);

    public final float x;
    public final float y;
    public final float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(DataInputStream dataInputStream) throws IOException {
        x = dataInputStream.readFloat();
        y = dataInputStream.readFloat();
        z = dataInputStream.readFloat();
    }

    public static Vec3 ofInt(int x, int y, int z) {
        return new Vec3(x + 0.5F, y + 0.5F, z + 0.5F);
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeFloat(x);
        dataOutputStream.writeFloat(y);
        dataOutputStream.writeFloat(z);
    }

    public Vec3 add(float dx, float dy, float dz) {
        return new Vec3(x + dx, y + dy, z + dz);
    }

    public Vec3 add(Vec3 vec3) {
        return add(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 remove(float dx, float dy, float dz) {
        return new Vec3(x - dx, y - dy, z - dz);
    }

    public Vec3 remove(Vec3 vec3) {
        return remove(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 multiply(float num) {
        return multiply(num, num, num);
    }

    public Vec3 multiply(float dx, float dy, float dz) {
        return new Vec3(x * dx, y * dy, z * dz);
    }

    public Vec3 multiply(Vec3 vec3) {
        return multiply(vec3.x, vec3.y, vec3.z);
    }

    public float distanceToZero() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float distance(Vec3 vec3) {
        return remove(vec3).distanceToZero();
    }

    public Vec3 getRadian() {
        return new Vec3((float) 0, (float) Math.atan2(z, MathUtil.distance(x, y)), (float) Math.atan2(y, x));
    }

    public Vec3 toRadians() {
        return new Vec3((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    public boolean isZero(){
        return x == 0 && y == 0 && z == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3 vec3 = (Vec3) o;
        return Float.compare(x, vec3.x) == 0 && Float.compare(y, vec3.y) == 0 && Float.compare(z, vec3.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("\"%.2f %.2f %.2f\"", x, y, z);
    }
}
