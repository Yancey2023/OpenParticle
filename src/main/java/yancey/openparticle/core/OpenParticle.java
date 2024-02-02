package yancey.openparticle.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yancey.openparticle.core.command.CommandPar;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;

public class OpenParticle implements ModInitializer {

    public static final String MOD_ID = "openparticle";

    @Override
    public void onInitialize() {
        NetworkHandler.initServer();
        KeyboardManager.init(false);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "better_particle"), FabricParticleTypes.simple());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CommandPar.init(dispatcher, true));
    }
}
