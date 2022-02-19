package dev.zvwild.minpc.plugin.npc;

import dev.zvwild.minpc.plugin.util.NmsUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.level.EnumGamemode;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public final class NpcPacketFactory {

    private final EnumMap<NpcPacket, Packet<?>> cache = new EnumMap<>(NpcPacket.class);
    private final NpcImpl caller;

    public NpcPacketFactory(NpcImpl caller) {
        this.caller = caller;
    }

    public Packet<?> getPacket(NpcPacket packet) {
        Packet<?> result = cache.get(packet);

        if (result == null) {
            updatePackets();
            return cache.get(packet);
        } else {
            return result;
        }
    }

    public void updatePackets(NpcPacket... packets) {
        if (packets == null || packets.length == 0) {
            packets = NpcPacket.values();
        }

        Arrays.stream(packets).forEach(this::update);
    }

    private void update(NpcPacket packetType) {
        switch (packetType) {
            case SCOREBOARD_TEAM -> {
                PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                packetDataSerializer.a("999" + caller.getName());
                packetDataSerializer.writeByte(0);

                // b data
                packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a(String.format("{\"text\": \"%s\"}", caller.getName())));
                packetDataSerializer.writeByte(0);
                packetDataSerializer.a("never");
                packetDataSerializer.a("never");
                packetDataSerializer.a(EnumChatFormat.v);
                packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}"));
                packetDataSerializer.a(IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}"));

                // entries
                packetDataSerializer.a(List.of(caller.getName()), PacketDataSerializer::a);

                PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(packetDataSerializer);
                cache.put(packetType, packet);
            }

            case ADD_TO_PLAYER_INFO, REMOVE_FROM_PLAYER_INFO -> {
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction nmsAction = packetType == NpcPacket.ADD_TO_PLAYER_INFO ?
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a : PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e;

                PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(nmsAction);
                packet.b().add(new PacketPlayOutPlayerInfo.PlayerInfoData(
                        caller.getGameProfile(),
                        1,
                        EnumGamemode.a,
                        IChatBaseComponent.ChatSerializer.a(String.format("{\"text\": \"%s\"}", caller.getName()))
                ));

                cache.put(packetType, packet);
            }

            case SPAWN -> {
                PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                packetDataSerializer.d(caller.getNmsId());
                packetDataSerializer.a(caller.getGameProfile().getId());
                packetDataSerializer.writeDouble(caller.getLocation().getX());
                packetDataSerializer.writeDouble(caller.getLocation().getY());
                packetDataSerializer.writeDouble(caller.getLocation().getZ());
                packetDataSerializer.writeByte(NmsUtils.convertToAngle(caller.getLocation().getYaw()));
                packetDataSerializer.writeByte(NmsUtils.convertToAngle(caller.getLocation().getPitch()));

                PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(packetDataSerializer);
                cache.put(packetType, packet);
            }

            case META_DATA -> {
                PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                packetDataSerializer.d(caller.getNmsId());

                DataWatcher.a(List.of(
                                new DataWatcher.Item<>(new DataWatcherObject<>(17, DataWatcherRegistry.a), (byte) 0xFF)),
                        packetDataSerializer);

                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(packetDataSerializer);
                cache.put(packetType, packet);
            }

            case DE_SPAWN -> {
                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(caller.getNmsId());
                cache.put(packetType, packet);
            }

            case LOOK -> {
                PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        caller.getNmsId(),
                        NmsUtils.convertToAngle(caller.getLocation().getYaw()),
                        NmsUtils.convertToAngle(caller.getLocation().getPitch()),
                        false
                );
                cache.put(packetType, packet);
            }

            case HEAD_ROTATION -> {
                PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
                packetDataSerializer.d(caller.getNmsId());
                packetDataSerializer.writeByte(NmsUtils.convertToAngle(caller.getLocation().getYaw()));

                PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation(packetDataSerializer);
                cache.put(packetType, packet);
            }

            default -> throw new RuntimeException("Somehow got here (Type: " + packetType + ")");
        }
    }

}
