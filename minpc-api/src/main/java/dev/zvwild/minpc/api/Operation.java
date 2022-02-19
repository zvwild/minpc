package dev.zvwild.minpc.api;

import org.bukkit.entity.Player;

public interface Operation {

    void executeFor(Player... players);

    void executeForAll();

}
