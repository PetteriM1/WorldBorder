package petterim1.worldborder;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.vehicle.VehicleMoveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.utils.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main extends PluginBase implements Listener {

    public static Main instance;
    private long distance;
    private String message;
    private String messageTp;
    private String messageTpOp;
    private boolean square;
    private boolean teleportToSpawn;
    private boolean checkVehicleMovement;
    private boolean opBypass;
    private Set<String> worlds;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        int ver = 5;
        int cfg = getConfig().getInt("configVersion", 99);
        if (cfg != ver) {
            if (cfg == 4) {
                Config c = getConfig();
                c.set("square", false);
                c.save();
                getLogger().info("Config updated from version " + cfg);
            } else if (cfg == 3) {
                Config c = getConfig();
                c.set("configVersion", ver);
                c.set("square", false);
                c.set("messageTpOp", "§6You have passed the world border");
                c.set("opBypass", false);
                c.save();
                getLogger().info("Config updated from version " + cfg);
            } else if (cfg < 3) {
                Config c = getConfig();
                c.set("configVersion", ver);
                c.set("square", false);
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

        distance = getConfig().getInt("distance");
        message = getConfig().getString("message");
        messageTp = getConfig().getString("messageTp");
        messageTpOp = getConfig().getString("messageTpOp");
        square = getConfig().getBoolean("square");
        teleportToSpawn = getConfig().getBoolean("teleportToSpawn");
        checkVehicleMovement = getConfig().getBoolean("checkVehicleMovement");
        opBypass = getConfig().getBoolean("opBypass");
        worlds = new HashSet<>(getConfig().getStringList("worlds"));

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (outsideBorder(e.getTo())) {
            if (!(opBypass && p.isOp())) {
                p.sendMessage(message);
                e.setCancelled(true);
                if (teleportToSpawn) {
                    p.teleport(p.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (outsideBorder(e.getTo())) {
            if (!(opBypass && p.isOp())) {
                p.sendMessage(messageTp);
                e.setCancelled(true);
            } else {
                p.sendMessage(messageTpOp);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent e) {
        if (checkVehicleMovement) {
            if (outsideBorder(e.getTo())) {
                e.getVehicle().kill();
            }
        }
    }

    public boolean outsideBorder(Position to) {
        Level level = to.getLevel();
        if (!worlds.contains(level.getName())) {
            return false;
        }

        Position spawn = level.getSpawnLocation();
        return (square && outsideSquare(spawn, to)) || (!square && outsideCircle(spawn, to));
    }

    private boolean outsideCircle(Position a, Position b) {
        double x = a.x - b.x;
        double z = a.z - b.z;
        return ((x * x) + (z * z)) > (distance * distance);
    }

    private boolean outsideSquare(Position a, Position b) {
        return Math.abs(a.x - b.x) > distance || Math.abs(a.z - b.z) > distance;
    }
}
