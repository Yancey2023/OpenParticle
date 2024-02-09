package yancey.openparticle.api.common.data;

public class ParticleState {
    public float x, y, z;
    public byte r, g, b, a;
    public int bright;

    @Override
    public String toString() {
        return String.format("(x, y, z) = (%.2f, %.2f, %.2f) | (r, g, b, a) = (%d, %d, %d, %d) | bright = %d", x, y, z, r, g, b, a, bright);
    }
}
