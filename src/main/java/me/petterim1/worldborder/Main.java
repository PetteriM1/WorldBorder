package me.petterim1.worldborder;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.vehicle.VehicleMoveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.Arrays;
import java.util.List;

public class Main extends PluginBase implements Listener {
    
    private long distance;
    private String message;
    private String messageTp;
    private String messageTpOp;
    private boolean teleportToSpawn;
    private boolean checkVehicleMovement;
    private boolean opBypass;
    private List<String> worlds;
    private static final Vector3 temp = new Vector3(0, 0, 0);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int ver = 4;
        int cfg = getConfig().getInt("configVersion", 99);
        if (cfg != ver) {
            if (cfg == 3) {
                Config c = getConfig();
                c.set("configVersion", ver);
                c.set("messageTpOp", "§6You have passed the world border");
                c.set("opBypass", false);
                c.save();
                getLogger().info("Config updated from version " + cfg);
            } else if (cfg < 3) {
                Config c = getConfig();
                c.set("configVersion", ver);
                c.set("messageTp", "§cYou cannot teleport outside the world border!");
                c.set("messageTpOp", "§6You have passed the world border");
                c.set("teleportToSpawn", false);
                c.set("checkVehicleMovement", false);
                c.set("opBypass", false);
                c.set("worlds", Arrays.asList("world"));
                c.save();
                getLogger().info("Config updated from version " + cfg);
            } else {
                getLogger().warning("Couldn't update the config file: unknown config version");
            }
        }
        distance = (long) Math.pow(getConfig().getInt("distance"), 2);
        message = getConfig().getString("message");
        messageTp = getConfig().getString("messageTp");
        messageTpOp = getConfig().getString("messageTpOp");
        teleportToSpawn = getConfig().getBoolean("teleportToSpawn");
        checkVehicleMovement = getConfig().getBoolean("checkVehicleMovement");
        opBypass = getConfig().getBoolean("opBypass");
        worlds = getConfig().getStringList("worlds");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (worlds.contains(p.getLevel().getName())) {
            Position spawn = p.getLevel().getSpawnLocation();
            if (p.distanceSquared(temp.setComponents(spawn.getX(), p.getY(), spawn.getZ())) > distance) {
                if (!(opBypass && p.isOp())) {
                    p.sendMessage(message);
                    e.setCancelled(true);
                    if (teleportToSpawn) {
                        p.teleport(p.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (worlds.contains(e.getTo().getLevel().getName())) {
            Position spawn = e.getTo().getLevel().getSpawnLocation();
            if (e.getTo().distanceSquared(temp.setComponents(spawn.getX(), e.getTo().getY(), spawn.getZ())) > distance) {
                if (!(opBypass && p.isOp())) {
                    p.sendMessage(messageTp);
                    e.setCancelled(true);
                } else {
                    p.sendMessage(messageTpOp);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent e) {
        if (checkVehicleMovement && worlds.contains(e.getTo().getLevel().getName())) {
            Position spawn = e.getTo().getLevel().getSpawnLocation();
            if (e.getTo().distanceSquared(temp.setComponents(spawn.getX(), e.getTo().getY(), spawn.getZ())) > distance) {
                e.getVehicle().kill();
            }
        }
    }
}
