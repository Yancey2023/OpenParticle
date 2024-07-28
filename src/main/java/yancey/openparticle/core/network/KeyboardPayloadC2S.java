package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record KeyboardPayloadC2S(List<Integer> idList) implements CustomPayload {

    public static final CustomPayload.Id<KeyboardPayloadC2S> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "key_board"));
    public static final PacketCodec<RegistryByteBuf, KeyboardPayloadC2S> CODEC =
            PacketCodec.tuple(PacketCodecs.collection(ArrayList::new, PacketCodecs.VAR_INT),
                    KeyboardPayloadC2S::idList, KeyboardPayloadC2S::new);

    @Override
    public Id<KeyboardPayloadC2S> getId() {
        return ID;
    }

}
