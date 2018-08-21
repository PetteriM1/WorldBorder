package idk.plugin.worldborder;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.math.Vector3;

public class Main extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.getConfig();
    }

    @EventHandler(ignoreCancelled=true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().distance(new Vector3(e.getPlayer().getLevel().getSpawnLocation().getX(), e.getPlayer().getY(), e.getPlayer().getLevel().getSpawnLocation().getZ())) > this.getConfig().getInt("distance")) {
            e.getPlayer().sendMessage(this.getConfig().getString("message").replace("§", "\u00A7"));
            e.setCancelled(true);
        }
    }
}
