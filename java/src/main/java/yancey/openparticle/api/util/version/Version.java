package yancey.openparticle.api.util.version;

public record Version(String version) {
    public static final Version Java1_12 = new Version("Java 1.12");
    public static final Version Java1_16 = new Version("Java 1.16");
}
