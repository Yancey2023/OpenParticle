package yancey.openparticle.api.context;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import yancey.openparticle.api.controller.Controller;
import yancey.openparticle.core.particle.BetterParticle;
import yancey.openparticle.core.util.SpriteProviderUtil;

public class Context {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ClientWorld clientWorld;

    public Context(ClientWorld clientWorld) {
        this.clientWorld = clientWorld;
    }

    public void addParticle(int particleType, Controller controller) {
        SpriteProvider spriteProvider = SpriteProviderUtil.getSpriteProvider(particleType);
        if (spriteProvider == null) {
            LOGGER.error("unable to find spriteProvider by id -> {}", particleType);
        }
        MinecraftClient.getInstance().particleManager.addParticle(BetterParticle.create(clientWorld, spriteProvider, controller));
    }

}
