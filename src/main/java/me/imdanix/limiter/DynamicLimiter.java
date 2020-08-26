package me.imdanix.limiter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public final class DynamicLimiter extends JavaPlugin {
    private static Logger logger;
    private List<Limiter> limiters;
    private BukkitTask task;

    @Override
    public void onEnable() {
        logger = getLogger();
        saveDefaultConfig();
        reloadConfig();
        limiters = new ArrayList<>();
        FileConfiguration cfg = getConfig();

        List<String> failed = new ArrayList<>();
        reloadLimiters(cfg.getConfigurationSection("worlds").getKeys(false), (w,e)-> failed.add(w));
        if(!failed.isEmpty()) {
            warn("Worlds " + String.join(", ", failed) + " cannot be found. Will try again after full server load.");
            Bukkit.getScheduler().runTask(this, () -> reloadLimiters(failed, (w, e) -> warn(e.getMessage())));
        }

        task = Bukkit.getScheduler().runTaskTimer(this, this::updateLimits, 1, cfg.getInt("scheduler"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(clr("&a/" + label + "help&7 - show this help."));
            sender.sendMessage(clr("&a/" + label + "info [world]&7- check limits in&e world&7."));
            sender.sendMessage(clr( "&a/" + label + "reload&7 - reload plugin configuration."));
            return true;
        }
        if(args[0].equalsIgnoreCase("info")) {
            String worldName;
            if(args.length < 2) {
                if(sender instanceof Player) {
                    worldName = ((Player) sender).getWorld().getName();
                } else {
                    sender.sendMessage(clr("&cYou can't use this command from the console without the &eworld&c argument!"));
                    return true;
                }
            } else {
                worldName = args[1];
            }
            World world = Bukkit.getWorld(worldName);
            if(world == null) {
                sender.sendMessage(clr("&cThere's no world called &e" + worldName + "&c!"));
                return true;
            }
            sender.sendMessage("Animal: " + world.getAnimalSpawnLimit());
            sender.sendMessage("Ambient: " + world.getAmbientSpawnLimit());
            sender.sendMessage("Monster: " + world.getMonsterSpawnLimit());
            sender.sendMessage("Water Animal: " + world.getWaterAnimalSpawnLimit());
            sender.sendMessage("Water Ambient: " + world.getWaterAmbientSpawnLimit());
        } else if(args[0].equalsIgnoreCase("reload")) {
            task.cancel();
            reloadConfig();
            FileConfiguration cfg = getConfig();
            reloadLimiters(cfg.getConfigurationSection("worlds").getKeys(false), (w,e) -> sender.sendMessage(ChatColor.RED + e.getMessage()));
            task = Bukkit.getScheduler().runTaskTimer(this, this::updateLimits, 1, cfg.getInt("scheduler"));
        }
        return true;
    }

    private void reloadLimiters(Collection<String> worlds, BiConsumer<String, Exception> onFail) {
        FileConfiguration cfg = getConfig();
        for(String worldName : worlds) {
            try {
                limiters.add(new Limiter(worldName, cfg.getConfigurationSection("worlds." + worldName)));
            } catch (IllegalStateException e) {
                onFail.accept(worldName, e);
            }
        }
    }

    public void updateLimits() {
        Iterator<Limiter> iter = limiters.iterator();
        float tps = (float) Bukkit.getTPS()[0];
        while(iter.hasNext()) {
            Limiter limiter = iter.next();
            if(!limiter.update(tps)) {
                iter.remove();
            }
        }
    }

    // I'm lazy

    public static void warn(String text) {
        logger.warning(text);
    }

    private static String clr(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
