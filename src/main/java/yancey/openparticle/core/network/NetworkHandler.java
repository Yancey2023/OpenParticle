package yancey.openparticle.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import yancey.openparticle.api.math.Vec3;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.util.OpenParticleUtil;

import java.util.List;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public class NetworkHandler {

    public static final Identifier ID_KEY_BOARD = new Identifier(MOD_ID, "key_board");
    public static final Identifier ID_LOAD = new Identifier(MOD_ID, "load");
    public static final Identifier ID_RUN = new Identifier(MOD_ID, "run");
    public static final Identifier ID_LOAD_AND_RUN = new Identifier(MOD_ID, "load_and_run");


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
        //加载粒子文件
        ClientPlayNetworking.registerGlobalReceiver(ID_LOAD, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.loadFile(buf.readString());
        });
        //运行粒子文件
        ClientPlayNetworking.registerGlobalReceiver(ID_RUN, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.run(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), handler.getWorld());
        });
        //加载并运行粒子文件
        ClientPlayNetworking.registerGlobalReceiver(ID_LOAD_AND_RUN, (client, handler, buf, responseSender) -> {
            OpenParticleUtil.loadAndRum(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readString(), handler.getWorld());
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
}
