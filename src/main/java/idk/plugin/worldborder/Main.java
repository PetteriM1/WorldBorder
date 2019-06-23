package idk.plugin.worldborder;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

public class Main extends PluginBase implements Listener {
    
    private int distance;
    private String message;
    private String messageTp;
    private boolean teleportToSpawn;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        if (getConfig().getInt("configVersion") != 2) {
            Config c = getConfig();
            c.set("configVersion", 2);
            c.set("messageTp", "§cYou cannot teleport outside world border!");
            c.set("teleportToSpawn", false);
            c.save();
        }

        distance = getConfig().getInt("distance");
        message = getConfig().getString("message");
        messageTp = getConfig().getString("messageTp");
        teleportToSpawn = getConfig().getBoolean("teleportToSpawn");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.distance(new Vector3(p.getLevel().getSpawnLocation().getX(), p.getY(), p.getLevel().getSpawnLocation().getZ())) > distance) {
            p.sendMessage(message);
            e.setCancelled(true);

            if (teleportToSpawn) {
                p.teleport(p.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getTo().distance(new Vector3(e.getTo().getLevel().getSpawnLocation().getX(), e.getTo().getY(), e.getTo().getLevel().getSpawnLocation().getZ())) > distance) {
            e.getPlayer().sendMessage(messageTp);
            e.setCancelled(true);
        }
    }
}
