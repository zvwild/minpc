package dev.zvwild.minpc.plugin.npc;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class OperationLookAt extends OperationImpl {

    private final Packet<?> lookPacket;
    private final Packet<?> headRotPacket;

    public OperationLookAt(NpcImpl npc, Packet<?> lookPacket, Packet<?> headRotPacket) {
        super(npc);
        this.lookPacket = lookPacket;
        this.headRotPacket = headRotPacket;
    }

    @Override
    protected void execute(Player player) {
        PlayerConnection c = ((CraftPlayer) player).getHandle().b;
        c.a(lookPacket);
        c.a(headRotPacket);
    }

}
