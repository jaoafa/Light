package com.jaoafa.light;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import java.util.function.Function;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        final PaperCommandManager<CommandSender> manager;
        try {
            manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator(),
                Function.identity(), Function.identity());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        manager.command(manager
            .commandBuilder("addlight", "light")
            .senderType(Player.class)
            .literal("set", ArgumentDescription.of("WorldEditで選択している範囲にライトを設定します"), "lightlevel")
            .argument(IntegerArgument.<CommandSender>newBuilder("changeTo")
                .withMin(0)
                .withMax(15), ArgumentDescription.of("設定する値"))
            .permission("light.set")
            .handler(context -> {
                Player player = (Player) context.getSender();
                int changeTo = context.get("changeTo");
                WorldEditPlugin we = getWorldEdit();
                if(we == null){
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text("WorldEditと連携できないため、このコマンドを使用できません。", NamedTextColor.GOLD)
                    ));
                    return;
                }

                try {
                    World selectionWorld = we.getSession(player).getSelectionWorld();
                    Region region = we.getSession(player).getSelection(selectionWorld);
                    int changed = 0;
                    for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                        for (int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getBlockY(); y++) {
                            for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                                Location loc = new Location(player.getWorld(), x, y, z);
                                LightAPI.createLight(loc, LightType.BLOCK, changeTo, true);
                                for (ChunkInfo info : LightAPI.collectChunks(loc, LightType.BLOCK, changeTo)) {
                                    LightAPI.updateChunk(info, LightType.BLOCK);
                                }
                                changed++;
                            }
                        }
                    }
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text(changed + " ブロックを明るさ " + changeTo + " に変更しました。", NamedTextColor.GOLD)
                    ));
                } catch (IncompleteRegionException e) {
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text("WorldEditで範囲を指定してください。", NamedTextColor.GOLD)
                    ));
                }
            })
            .build());
        manager.command(manager
            .commandBuilder("addlight", "light")
            .senderType(Player.class)
            .literal("remove", ArgumentDescription.of("WorldEditで選択している範囲のライトを消します"), "del")
            .permission("light.del")
            .handler(context -> {
                Player player = (Player) context.getSender();
                WorldEditPlugin we = getWorldEdit();
                if(we == null){
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text("WorldEditと連携できないため、このコマンドを使用できません。", NamedTextColor.GOLD)
                    ));
                    return;
                }

                try {
                    World selectionWorld = we.getSession(player).getSelectionWorld();
                    Region region = we.getSession(player).getSelection(selectionWorld);
                    int changed = 0;
                    for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                        for (int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getBlockY(); y++) {
                            for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                                Location loc = new Location(player.getWorld(), x, y, z);
                                LightAPI.deleteLight(loc, LightType.BLOCK, true);
                                for (ChunkInfo info : LightAPI.collectChunks(loc, LightType.BLOCK, 15)) {
                                    LightAPI.updateChunk(info, LightType.BLOCK);
                                }
                                changed++;
                            }
                        }
                    }
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text(changed + " ブロックの明るさを削除しました。", NamedTextColor.GOLD)
                    ));
                } catch (IncompleteRegionException e) {
                    context.getSender().sendMessage(Component.text("[Light] ").append(
                        Component.text("WorldEditで範囲を指定してください。", NamedTextColor.GOLD)
                    ));
                }
            })
            .build());
    }

    public static WorldEditPlugin getWorldEdit() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(plugin instanceof WorldEditPlugin)) {
            return null;
        }

        return (WorldEditPlugin) plugin;
    }
}
