package yancey.openparticle.api.math;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Vec3 {

    public double x;
    public double y;
    public double z;

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

    public static Vec3 ofInt(int x, int y, int z) {
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }

    public static Vec3 zero() {
        return new Vec3(0, 0, 0);
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeDouble(x);
        dataOutputStream.writeDouble(y);
        dataOutputStream.writeDouble(z);
    }

    public Vec3 add(double dx, double dy, double dz) {
        return new Vec3(x + dx, y + dy, z + dz);
    }

    public Vec3 add(Vec3 vec3) {
        return add(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 remove(double dx, double dy, double dz) {
        return new Vec3(x - dx, y - dy, z - dz);
    }

    public Vec3 remove(Vec3 vec3) {
        return remove(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 multiply(double num) {
        return multiply(num, num, num);
    }

    public Vec3 multiply(double dx, double dy, double dz) {
        return new Vec3(x * dx, y * dy, z * dz);
    }

    public Vec3 multiply(Vec3 vec3) {
        return multiply(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 multiplySelf(double num) {
        return multiplySelf(num, num, num);
    }

    public Vec3 multiplySelf(double dx, double dy, double dz) {
        x *= dx;
        y *= dy;
        z *= dz;
        return this;
    }

    public Vec3 multiplySelf(Vec3 vec3) {
        return multiplySelf(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 addSelf(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vec3 addSelf(Vec3 vec3) {
        return addSelf(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 removeSelf(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vec3 removeSelf(Vec3 vec3) {
        return removeSelf(vec3.x, vec3.y, vec3.z);
    }

    public double distanceToZero() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double distance(Vec3 vec3) {
        return remove(vec3).distanceToZero();
    }

    public Vec3 copy() {
        return new Vec3(x, y, z);
    }

    public Vec3 rotate(Vec3 rotate) {
        return rotateRx(rotate.x).rotateRy(rotate.y).rotateRz(rotate.z);
    }

    //xz
    public Vec3 rotateRx(double rx) {
        double sinRx = Math.sin(rx);
        double cosRx = Math.cos(rx);
        return new Vec3(x * cosRx - z * sinRx, y, z * cosRx + x * sinRx);
    }

    //xy
    public Vec3 rotateRy(double ry) {
        double sinRy = Math.sin(ry);
        double cosRy = Math.cos(ry);
        return new Vec3(x * cosRy - y * sinRy, y * cosRy + x * sinRy, z);
    }

    //yz
    public Vec3 rotateRz(double rz) {
        double sinRz = Math.sin(rz);
        double cosRz = Math.cos(rz);
        return new Vec3(x, y * cosRz + z * sinRz, z * cosRz - y * sinRz);
    }

    public Vec3 radiansToDegrees() {
        return new Vec3(Math.toDegrees(x), Math.toDegrees(y), Math.toDegrees(z));
    }

    public Vec3 degreesToRadians() {
        return new Vec3(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }
}
