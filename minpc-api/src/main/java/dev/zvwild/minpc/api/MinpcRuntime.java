package dev.zvwild.minpc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface MinpcRuntime {

    Npc createNpc(Location location, String tabListName);

    void spawnManaged(Npc npc, Player player);

    void deSpawnManaged(Npc npc, Player player);

}
