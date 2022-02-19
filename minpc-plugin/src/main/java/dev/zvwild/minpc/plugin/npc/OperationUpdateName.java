package dev.zvwild.minpc.plugin.npc;

import dev.zvwild.minpc.plugin.armorstand.PacketArmorStand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OperationUpdateName extends OperationImpl {

    public static final Map<UUID, Map<UUID, PacketArmorStand>> PLAYER_NAME_TAG_MAP = new HashMap<>();

    private final PacketArmorStand newArmorStand;

    public OperationUpdateName(NpcImpl npc, PacketArmorStand newArmorStand) {
        super(npc);
        this.newArmorStand = newArmorStand;
    }

    @Override
    protected void execute(Player player) {
        Map<UUID, PacketArmorStand> nameTags = PLAYER_NAME_TAG_MAP.get(player.getUniqueId());

        if (nameTags != null) {
            PacketArmorStand oldArmorStand = nameTags.get(npc.getUuid());
            if (oldArmorStand != null) {
                oldArmorStand.deSpawn(player);
            }
        } else {
            nameTags = new HashMap<>();
            npc.getNameTag().deSpawn(player);
            PLAYER_NAME_TAG_MAP.put(player.getUniqueId(), nameTags);
        }

        nameTags.put(npc.getUuid(), newArmorStand);
        newArmorStand.spawn(player);
    }

}
