package dev.zvwild.minpc.plugin.npc;

import dev.zvwild.minpc.api.MinpcRuntime;
import dev.zvwild.minpc.api.Npc;
import dev.zvwild.minpc.plugin.util.NmsUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MinpcRuntimeImpl implements MinpcRuntime {

    private static final Map<UUID, Set<Npc>> playerNpcMap = new HashMap<>();
    private static final ConcurrentMap<Integer, Npc> npcMap = new ConcurrentHashMap<>();

    @Override
    public Npc createNpc(Location location, String tabListName) {
        int id = NmsUtils.getNewEntityId();
        Npc result = new NpcImpl(id, location, tabListName);
        npcMap.put(id, result);
        return result;
    }

    @Override
    public void spawnManaged(Npc npc, Player player) {
        Set<Npc> npcs = playerNpcMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (npcs.add(npc)) {
            npc.spawn(player);
        }
    }

    @Override
    public void deSpawnManaged(Npc npc, Player player) {
        playerNpcMap.computeIfPresent(player.getUniqueId(), (k, v) -> {
            if (v.remove(npc)) {
                npc.deSpawn(player);
            }

            return v;
        });
    }

    public void clear(Player player) {
        Set<Npc> npcs = playerNpcMap.remove(player.getUniqueId());
        if (npcs != null) {
            npcs.forEach(npc -> npc.deSpawn(player));
        }
    }

    public Set<Npc> getNpcs(Player player) {
        return playerNpcMap.get(player.getUniqueId());
    }

    public Npc getById(int id) {
        return npcMap.get(id);
    }

}
