package dev.zvwild.minpc.plugin;

import dev.zvwild.minpc.api.Npc;
import dev.zvwild.minpc.api.event.NpcInteractEvent;
import dev.zvwild.minpc.plugin.npc.MinpcRuntimeImpl;
import dev.zvwild.minpc.plugin.util.ReflectionUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public final class PipelineSniffer extends MessageToMessageDecoder<Packet<?>> {

    private final Plugin plugin;
    private final UUID owner;
    private final MinpcRuntimeImpl runtime;

    private int c = -1;

    public PipelineSniffer(Plugin plugin, UUID owner, MinpcRuntimeImpl runtime) {
        this.plugin = plugin;
        this.owner = owner;
        this.runtime = runtime;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) {
        if (packet instanceof PacketPlayInUseEntity usePacket) {
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
            usePacket.a(packetDataSerializer);
            int id = packetDataSerializer.j();
            Npc npc = runtime.getById(id);

            if (npc != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String actionClassName = ReflectionUtils.readField("b", usePacket).getClass().getSimpleName();

                    NpcInteractEvent.Action action;

                    if (actionClassName.isEmpty()) {
                        action = NpcInteractEvent.Action.ATTACK;
                    } else {
                        action = NpcInteractEvent.Action.INTERACT;

                        c += 1;

                        if (c % 4 != 0) {
                            return;
                        }
                    }

                    Player player = Bukkit.getPlayer(owner);
                    NpcInteractEvent event = new NpcInteractEvent(player, npc, action);
                    Bukkit.getPluginManager().callEvent(event);
                });

                return;
            }
        }

        list.add(packet);
    }

}
