package dev.zvwild.minpc.plugin.npc;

import dev.zvwild.minpc.api.Operation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

public abstract class OperationImpl implements Operation {

    protected final NpcImpl npc;

    public OperationImpl(NpcImpl npc) {
        this.npc = npc;
    }

    @Override
    public final void executeFor(Player... players) {
        for (Player player : players) {
            execute(player);
        }
    }

    @Override
    public final void executeForAll() {
        npc.getPlayersSpawnedTo().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .forEach(this::execute);
    }

    protected abstract void execute(Player player);

}
