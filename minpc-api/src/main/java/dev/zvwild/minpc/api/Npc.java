package dev.zvwild.minpc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Npc {

    void spawn(Player player);

    void deSpawn(Player player);

    Operation lookAt(Location location, boolean updateState);

    Operation updateSkin(String skinValue, String skinSignature, boolean updateState);

    Operation updateName(String name, boolean updateState);

    boolean isVisibleTo(Player player);

    UUID getUuid();

    Location getLocation();

}
