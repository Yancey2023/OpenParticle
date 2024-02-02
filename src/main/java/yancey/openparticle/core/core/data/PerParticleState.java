package yancey.openparticle.core.core.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.particle.BetterParticle;
import yancey.openparticle.core.util.CommonUtil;
import yancey.openparticle.core.util.IdentifierCache;
import yancey.openparticle.core.util.Vec3;

import java.io.DataInputStream;
import java.io.IOException;

public class PerParticleState {

    public int particleTypeRawId;
    public Vec3[] positions;
    public int[] colors;

    public PerParticleState(DataInputStream dataInputStream) throws IOException {
        particleTypeRawId = Registries.PARTICLE_TYPE.getRawId(Registries.PARTICLE_TYPE.get(IdentifierCache.getIdentifier(dataInputStream.readInt())));
        int liveTick = dataInputStream.readInt();
        positions = new Vec3[liveTick];
        colors = new int[liveTick];
        for (int i = 0; i < liveTick; i++) {
            positions[i] = new Vec3(dataInputStream);
            colors[i] = dataInputStream.readInt();
        }
    }

    public PerParticleState(PacketByteBuf buf) {
        particleTypeRawId = buf.readInt();
        int liveTick = buf.readInt();
        positions = new Vec3[liveTick];
        colors = new int[liveTick];
        for (int i = 0; i < liveTick; i++) {
            positions[i] = new Vec3(buf);
            colors[i] = buf.readInt();
        }
    }

    public void toBuf(PacketByteBuf buf) {
        buf.writeInt(particleTypeRawId);
        buf.writeInt(positions.length);
        for (int i = 0; i < positions.length; i++) {
            positions[i].toBuf(buf);
            buf.writeInt(colors[i]);
        }
    }

    public void run(World world) {
        if (positions.length == 0) {
            return;
        }
//        if (world.isClient) {
            Registries.PARTICLE_TYPE.getEntry(particleTypeRawId)
                    .ifPresent(particleTypeReference -> CommonUtil.mc.particleManager.addParticle(BetterParticle.create(
                            (ClientWorld) world,
                            positions,
                            colors,
                            ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager)
                                    .getSpriteAwareFactories().get(Registries.PARTICLE_TYPE.getId(particleTypeReference.value()))
                    )));
//        } else {
//            NetworkHandler.summonParticle((ServerWorld) world, );
//        }
    }
}
