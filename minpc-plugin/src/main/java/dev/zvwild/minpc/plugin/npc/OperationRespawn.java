package dev.zvwild.minpc.plugin.npc;

import org.bukkit.entity.Player;

public final class OperationRespawn extends OperationImpl {

    public OperationRespawn(NpcImpl npc) {
        super(npc);
    }

    @Override
    protected void execute(Player player) {
        npc.deSpawn(player);
        npc.spawn(player);
    }

}
