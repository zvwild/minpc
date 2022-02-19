package dev.zvwild.minpc.api.event;

import dev.zvwild.minpc.api.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class NpcInteractEvent extends PlayerEvent {

    private static HandlerList handlers = new HandlerList();

    private final Npc npc;
    private final Action action;

    public NpcInteractEvent(Player who, Npc npc, Action action) {
        super(who);
        this.npc = npc;
        this.action = action;
    }

    public Npc getNpc() {
        return npc;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Action {
        ATTACK,
        INTERACT,
    }

}
