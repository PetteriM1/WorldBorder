package petterim1.worldborder;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.vehicle.VehicleMoveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
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
    private boolean middlePointAtZero;
    private boolean allWorlds;
    private boolean teleportToSpawn;
    private boolean checkVehicleMovement;
    private boolean opBypass;
    private Set<String> worlds;

    private static final Vector3 ZERO = new Vector3(0, 0, 0);

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Config c = getConfig();
        int currentVersion = 6;
        int configVersion = c.getInt("configVersion", 99);

        if (configVersion != currentVersion) {
            boolean changed = true;

            if (configVersion == 5) {
                c.set("middlePointAtZero", false);
                c.set("allWorlds", false);
            } else if (configVersion == 4) {
                c.set("square", false);
                c.set("middlePointAtZero", false);
                c.set("allWorlds", false);
            } else if (configVersion == 3) {
                c.set("square", false);
                c.set("middlePointAtZero", false);
                c.set("allWorlds", false);
                c.set("messageTpOp", "§6You have passed the world border");
                c.set("opBypass", false);
            } else if (configVersion < 3) {
                c.set("square", false);
                c.set("middlePointAtZero", false);
                c.set("allWorlds", true);
                c.set("messageTp", "§cYou cannot teleport outside the world border!");
                c.set("messageTpOp", "§6You have passed the world border");
                c.set("teleportToSpawn", false);
                c.set("checkVehicleMovement", false);
                c.set("opBypass", false);
                c.set("worlds", Arrays.asList("world"));
            } else {
                changed = false;
                getLogger().warning("Couldn't update the config file: unknown config version");
            }

            if (changed) {
                c.set("configVersion", currentVersion);
                c.save();
                getLogger().info("Config updated from version " + configVersion);
            }
        }

        distance = getConfig().getInt("distance");
        message = getConfig().getString("message");
        messageTp = getConfig().getString("messageTp");
        messageTpOp = getConfig().getString("messageTpOp");
        square = getConfig().getBoolean("square");
        middlePointAtZero = getConfig().getBoolean("middlePointAtZero");
        allWorlds = getConfig().getBoolean("allWorlds");
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
        if (!allWorlds && !worlds.contains(level.getName())) {
            return false;
        }

        Vector3 spawn = middlePointAtZero ? ZERO : level.getSpawnLocation();
        return (square && outsideSquare(spawn, to)) || (!square && outsideCircle(spawn, to));
    }

    private boolean outsideCircle(Vector3 a, Vector3 b) {
        double x = a.x - b.x;
        double z = a.z - b.z;
        return ((x * x) + (z * z)) > (distance * distance);
    }

    private boolean outsideSquare(Vector3 a, Vector3 b) {
        return Math.abs(a.x - b.x) > distance || Math.abs(a.z - b.z) > distance;
    }
}
