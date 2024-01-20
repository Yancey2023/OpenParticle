package yancey.openparticle.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import yancey.openparticle.core.events.PerParticleState;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.util.OpenParticleUtil;

import java.util.List;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public class NetworkHandler {

    public static final Identifier ID_KEY_BOARD = new Identifier(MOD_ID, "key_board");
    public static final Identifier ID_SUMMON_PARTICLE = new Identifier(MOD_ID, "summon_particle");
    public static final Identifier LOAD_ON_CLIENT = new Identifier(MOD_ID, "load_on_client");
    public static final Identifier RUN_ON_CLIENT = new Identifier(MOD_ID, "run_on_client");
    public static final Identifier RUN_AND_RUN_ON_CLIENT = new Identifier(MOD_ID, "run_and_run_on_client");


    public static void initServer() {
        //执行按键的服务端事件
        ServerPlayNetworking.registerGlobalReceiver(ID_KEY_BOARD, (server, player, handler, buf, responseSender) -> {
            int[] idList = new int[buf.readInt()];
            for (int i = 0; i < idList.length; i++) {
                idList[i] = buf.readInt();
            }
            server.execute(() -> KeyboardManager.runInServe(idList, player));
        });
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        //生成粒子
        ClientPlayNetworking.registerGlobalReceiver(ID_SUMMON_PARTICLE, (client, handler, buf, responseSender) -> new PerParticleState(buf).run(handler.getWorld()));
        //加载并运行粒子文件
        ClientPlayNetworking.registerGlobalReceiver(RUN_AND_RUN_ON_CLIENT, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.loadAndRum(buf.readString(), handler.getWorld());
        });
        //加载粒子文件
        ClientPlayNetworking.registerGlobalReceiver(LOAD_ON_CLIENT, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.loadFile(buf.readString());
        });
        //运行粒子文件
        ClientPlayNetworking.registerGlobalReceiver(RUN_ON_CLIENT, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.run(handler.getWorld());
        });
    }

    @Environment(EnvType.CLIENT)
    public static void keyBoardToServer(List<Integer> idList) {
        //执行按键的服务端事件
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(idList.size());
        for (Integer id : idList) {
            buf.writeInt(id);
        }
        ClientPlayNetworking.send(ID_KEY_BOARD, buf);
    }

    public static void summonParticle(ServerWorld world, PerParticleState perParticleState) {
        //生成粒子
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        perParticleState.toBuf(packetByteBuf);
        for (ServerPlayerEntity serverPlayerEntity : world.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(serverPlayerEntity, ID_SUMMON_PARTICLE, packetByteBuf);
        }
    }
}
