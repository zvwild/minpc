package dev.zvwild.minpc.plugin.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.zvwild.minpc.api.Npc;
import dev.zvwild.minpc.api.Operation;
import dev.zvwild.minpc.plugin.MinpcPlugin;
import dev.zvwild.minpc.plugin.armorstand.PacketArmorStand;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class NpcImpl implements Npc {

    private final int nmsId;
    private final String name;
    private final NpcPacketFactory npcPacketFactory;
    private final Set<UUID> playersSpawnedTo;

    private Location location;
    private GameProfile gameProfile;
    private PacketArmorStand nameTag;

    public NpcImpl(int nmsId, Location location, String tabListName) {
        this.nmsId = nmsId;
        this.npcPacketFactory = new NpcPacketFactory(this);
        this.playersSpawnedTo = new HashSet<>();

        this.location = location;

        UUID uuid = new UUID(ThreadLocalRandom.current().nextLong(), 0);
        this.gameProfile = new GameProfile(uuid, name = tabListName.substring(0, Math.min(tabListName.length(), 13)));
        this.nameTag = PacketArmorStand.create(name, location);

        npcPacketFactory.updatePackets();
    }

    @Override
    public void spawn(Player player) {
        if (playersSpawnedTo.add(player.getUniqueId())) {
            PlayerConnection c = ((CraftPlayer) player).getHandle().b;

            new OperationUpdateName(this, nameTag).executeFor(player);

            c.a(npcPacketFactory.getPacket(NpcPacket.SCOREBOARD_TEAM));
            c.a(npcPacketFactory.getPacket(NpcPacket.ADD_TO_PLAYER_INFO));
            c.a(npcPacketFactory.getPacket(NpcPacket.SPAWN));
            c.a(npcPacketFactory.getPacket(NpcPacket.META_DATA));
            c.a(npcPacketFactory.getPacket(NpcPacket.LOOK));
            c.a(npcPacketFactory.getPacket(NpcPacket.HEAD_ROTATION));
            Bukkit.getScheduler().runTaskLater(MinpcPlugin.getPlugin(MinpcPlugin.class), () ->
                    c.a(npcPacketFactory.getPacket(NpcPacket.REMOVE_FROM_PLAYER_INFO)), 60);
        }

    }

    @Override
    public void deSpawn(Player player) {
        if (playersSpawnedTo.remove(player.getUniqueId())) {
            PlayerConnection c = ((CraftPlayer) player).getHandle().b;
            c.a(npcPacketFactory.getPacket(NpcPacket.DE_SPAWN));
            c.a(npcPacketFactory.getPacket(NpcPacket.REMOVE_FROM_PLAYER_INFO));
            nameTag.deSpawn(player);

            Map<UUID, PacketArmorStand> nameTags = OperationUpdateName.PLAYER_NAME_TAG_MAP.get(player.getUniqueId());

            if (nameTags != null) {
                PacketArmorStand oldArmorStand = nameTags.get(getUuid());
                if (oldArmorStand != null) {
                    oldArmorStand.deSpawn(player);
                }
            }
        }
    }

    @Override
    public Operation lookAt(Location target, boolean updateState) {
        Vector t = target.toVector();
        Vector h = location.toVector();
        Vector d = t.subtract(h);

        Location cloned = !updateState ? location.clone() : null;

        location.setDirection(d);
        npcPacketFactory.updatePackets(NpcPacket.LOOK, NpcPacket.HEAD_ROTATION);

        Packet<?> lookPacket = npcPacketFactory.getPacket(NpcPacket.LOOK);
        Packet<?> headRotPacket = npcPacketFactory.getPacket(NpcPacket.HEAD_ROTATION);

        if (!updateState) {
            location = cloned;
            npcPacketFactory.updatePackets(NpcPacket.LOOK, NpcPacket.HEAD_ROTATION);
        }

        return new OperationLookAt(this, lookPacket, headRotPacket);
    }

    @Override
    public Operation updateSkin(String skinValue, String skinSignature, boolean updateState) {
        GameProfile cloned = !updateState ? cloneProfile(gameProfile) : null;

        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

        npcPacketFactory.updatePackets(NpcPacket.ADD_TO_PLAYER_INFO, NpcPacket.REMOVE_FROM_PLAYER_INFO);

        if (!updateState) {
            gameProfile = cloned;
            npcPacketFactory.updatePackets(NpcPacket.ADD_TO_PLAYER_INFO, NpcPacket.REMOVE_FROM_PLAYER_INFO);
        }

        return new OperationRespawn(this);
    }

    @Override
    public Operation updateName(String name, boolean updateState) {
        PacketArmorStand newNameTag = PacketArmorStand.create(name, location);

        if (updateState) {
            nameTag = newNameTag;
        }

        return new OperationUpdateName(this, newNameTag);
    }

    @Override
    public boolean isVisibleTo(Player player) {
        return playersSpawnedTo.contains(player.getUniqueId());
    }

    @Override
    public UUID getUuid() {
        return gameProfile.getId();
    }

    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    private static GameProfile cloneProfile(GameProfile reference) {
        GameProfile result = new GameProfile(reference.getId(), reference.getName());
        result.getProperties().putAll(reference.getProperties());
        return result;
    }

    PacketArmorStand getNameTag() {
        return nameTag;
    }

    public int getNmsId() {
        return nmsId;
    }

    @Override
    public int hashCode() {
        return nmsId;
    }

    GameProfile getGameProfile() {
        return gameProfile;
    }

    Collection<UUID> getPlayersSpawnedTo() {
        return playersSpawnedTo;
    }

    NpcPacketFactory getNpcPacketFactory() {
        return npcPacketFactory;
    }

}
