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
    
    private int distance;
    private String message;
    private String messageTp;
    private boolean teleportToSpawn;
    private boolean checkVehicleMovement;
    private List<String> worlds;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        if (getConfig().getInt("configVersion") != 3) {
            if (getConfig().getInt("configVersion") == 2) {
                Config c = getConfig();
                c.set("configVersion", 3);
                c.set("messageTp", "§cYou cannot teleport outside world border!");
                c.set("teleportToSpawn", false);
                c.set("checkVehicleMovement", false);
                c.set("worlds", Arrays.asList("world"));
                c.save();
            } else {
                Config c = getConfig();
                c.set("configVersion", 3);
                c.set("checkVehicleMovement", false);
                c.set("worlds", Arrays.asList("world"));
                c.save();
            }
        }
        distance = (int) Math.pow(getConfig().getInt("distance"), 2);
        message = getConfig().getString("message");
        messageTp = getConfig().getString("messageTp");
        teleportToSpawn = getConfig().getBoolean("teleportToSpawn");
        checkVehicleMovement = getConfig().getBoolean("checkVehicleMovement");
        worlds = getConfig().getStringList("worlds");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!worlds.contains(p.getLevel().getName())) return;
        Position spawn = p.getLevel().getSpawnLocation();
        if (p.distanceSquared(new Vector3(spawn.getX(), p.getY(), spawn.getZ())) > distance) {
            p.sendMessage(message);
            e.setCancelled(true);
            if (teleportToSpawn) {
                p.teleport(p.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!worlds.contains(e.getTo().getLevel().getName())) return;
        Position spawn = e.getTo().getLevel().getSpawnLocation();
        if (e.getTo().distanceSquared(new Vector3(spawn.getX(), e.getTo().getY(), spawn.getZ())) > distance) {
            e.getPlayer().sendMessage(messageTp);
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent e) {
        if (!worlds.contains(e.getTo().getLevel().getName())) return;
        if (checkVehicleMovement) {
            Position spawn = e.getTo().getLevel().getSpawnLocation();
            if (e.getTo().distanceSquared(new Vector3(spawn.getX(), e.getTo().getY(), spawn.getZ())) > distance) {
                e.getVehicle().kill();
            }
        }
    }
}
