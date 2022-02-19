package dev.zvwild.minpc.plugin.armorstand;

import dev.zvwild.minpc.plugin.util.NmsUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.IRegistry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PacketArmorStand {

    private final PacketPlayOutSpawnEntityLiving spawnPacket;
    private final PacketPlayOutEntityMetadata metadataPacket;
    private final PacketPlayOutEntityDestroy deSpawnPacket;

    private PacketArmorStand(PacketPlayOutSpawnEntityLiving spawnPacket, PacketPlayOutEntityMetadata metadataPacket, PacketPlayOutEntityDestroy deSpawnPacket) {
        this.spawnPacket = spawnPacket;
        this.metadataPacket = metadataPacket;
        this.deSpawnPacket = deSpawnPacket;
    }

    public void spawn(Player player) {
        ((CraftPlayer) player).getHandle().b.sendPacket(spawnPacket);
        ((CraftPlayer) player).getHandle().b.sendPacket(metadataPacket);
    }

    public void deSpawn(Player player) {
        ((CraftPlayer) player).getHandle().b.sendPacket(deSpawnPacket);
    }

    public static PacketArmorStand create(String text, Location location) {
        int id = NmsUtils.getNewEntityId();
        UUID uuid = UUID.randomUUID();

        PacketPlayOutSpawnEntityLiving spawnPacket = createSpawnPacket(id, uuid, location);

        return new PacketArmorStand(spawnPacket, createMetadataPacket(id, text), new PacketPlayOutEntityDestroy(id));
    }

    private static PacketPlayOutSpawnEntityLiving createSpawnPacket(int id, UUID uuid, Location location) {
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
        packetDataSerializer.d(id);
        packetDataSerializer.a(uuid);
        packetDataSerializer.d(IRegistry.Y.getId(EntityTypes.c));
        packetDataSerializer.writeDouble(location.getX());
        packetDataSerializer.writeDouble(location.getY());
        packetDataSerializer.writeDouble(location.getZ());
        packetDataSerializer.writeByte(0);
        packetDataSerializer.writeByte(0);
        packetDataSerializer.writeByte(0);
        packetDataSerializer.writeShort(0);
        packetDataSerializer.writeShort(0);
        packetDataSerializer.writeShort(0);

        return new PacketPlayOutSpawnEntityLiving(packetDataSerializer);
    }

    private static PacketPlayOutEntityMetadata createMetadataPacket(int id, String text) {
        PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
        packetDataSerializer.d(id);

        DataWatcher.a(
                List.of(new DataWatcher.Item<>(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0x20),
                        new DataWatcher.Item<>(new DataWatcherObject<>(5, DataWatcherRegistry.i), true),
                        new DataWatcher.Item<>(new DataWatcherObject<>(2, DataWatcherRegistry.f), Optional.of(IChatBaseComponent.ChatSerializer.a(String.format("{\"text\": \"%s\"}", text)))),
                        new DataWatcher.Item<>(new DataWatcherObject<>(3, DataWatcherRegistry.i), true)
                ),
                packetDataSerializer);

        return new PacketPlayOutEntityMetadata(packetDataSerializer);
    }

}
