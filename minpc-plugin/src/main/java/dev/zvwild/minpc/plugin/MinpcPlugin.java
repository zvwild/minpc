package dev.zvwild.minpc.plugin;

import dev.zvwild.minpc.api.MinpcRuntime;
import dev.zvwild.minpc.api.Npc;
import dev.zvwild.minpc.plugin.npc.MinpcRuntimeImpl;
import dev.zvwild.minpc.plugin.npc.OperationUpdateName;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class MinpcPlugin extends JavaPlugin implements Listener {

    private MinpcRuntimeImpl runtime;

    @Override
    public void onEnable() {
        runtime = new MinpcRuntimeImpl();
        Bukkit.getServicesManager().register(MinpcRuntime.class, runtime, this, ServicePriority.Normal);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void handlePlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Set<Npc> npcs = runtime.getNpcs(player);
        if (npcs != null) {
            World targetWorld = player.getLocation().getWorld();
            for (Npc npc : npcs) {
                if (!npc.getLocation().getWorld().equals(targetWorld)) {
                    npc.deSpawn(player);
                    continue;
                }

                double dSq = npc.getLocation().distanceSquared(player.getLocation());

                if (dSq < 50 * 50) {
                    npc.spawn(player);
                } else {
                    npc.deSpawn(player);
                }
            }
        }
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        CraftPlayer player = (CraftPlayer) event.getPlayer();
        player.getHandle().b.a.k.pipeline().addAfter("decoder", "minpc_sniffer", new PipelineSniffer(this, player.getUniqueId(), runtime));
    }

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        runtime.clear(player);
        OperationUpdateName.PLAYER_NAME_TAG_MAP.remove(player.getUniqueId());
    }

}
