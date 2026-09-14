package yancey.openparticle.api.type;

import yancey.openparticle.api.identifier.Identifier;
import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.particle.ParticleDefault;
import yancey.openparticle.api.run.data.identifier.DataIdentifier;
import yancey.openparticle.api.util.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticleType {

    private static final List<ParticleType> particleTypeList = new ArrayList<>();

    public static final ParticleType END_ROD = new ParticleType(Map.of(
            Version.Java1_12, new Identifier("endRod"),
            Version.Java1_16, new Identifier("end_rod")
    ));

    private final Map<Version, Identifier> identifierMap;

    public ParticleType(Map<Version, Identifier> identifierMap) {
        particleTypeList.add(this);
        this.identifierMap = identifierMap;
    }

    public DataIdentifier getIdentifier(Version version) {
        return identifierMap.get(version).getDataIdentifier();
    }

    public void clearCache() {
        identifierMap.forEach((version, identifier) -> identifier.clearCache());
    }

    public static void clearAllCache() {
        particleTypeList.forEach(ParticleType::clearCache);
    }

    public void addVersion(Version version, Identifier name) {
        identifierMap.put(version, name);
    }

    public Particle createParticle(int age) {
        return new ParticleDefault(this, age).useCache(true);
    }
}
