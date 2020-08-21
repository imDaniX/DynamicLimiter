package me.imdanix.limiter;

import me.imdanix.limiter.math.FormulaEvaluator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class Limiter {
    private final String world;
    private final UUID currentId;
    private final FormulaEvaluator formula;
    private final int minimal;
    private final boolean above;

    private int animal, ambient, monster, waterAnimal, waterAmbient;

    public Limiter(String worldName, ConfigurationSection cfg) {
        this.formula = new FormulaEvaluator(cfg.getString("formula"));
        this.minimal = Math.max(cfg.getInt("minimal-limit"), 0);
        this.above = cfg.getBoolean("allow-above");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("World " + worldName + " is not found.");
        }
        this.world = worldName;
        this.currentId = world.getUID();
        reloadInitials(world);
    }

    public boolean update(float tps) {
        World world = Bukkit.getWorld(currentId);
        if (world == null) {
            DynamicLimiter.warn("World " + this.world + " is not found by its UID. Will try to find it by its" +
                    " name and reload initial limits.");
            world = Bukkit.getWorld(this.world);
            if(world == null) {
                DynamicLimiter.warn("World " + this.world + " is not found by its name. Limiter for this world" +
                        " will be disabled until next config reload.");
                return false;
            }
            reloadInitials(world);
        }
        formula.setParameters(Bukkit.getOnlinePlayers().size(), world.getPlayerCount(), tps);
        world.setAnimalSpawnLimit(getLimit(animal));
        world.setAmbientSpawnLimit(getLimit(ambient));
        world.setMonsterSpawnLimit(getLimit(monster));
        world.setWaterAnimalSpawnLimit(getLimit(waterAnimal));
        world.setWaterAmbientSpawnLimit(getLimit(waterAmbient));
        return true;
    }

    private int getLimit(int initial) {
        return (int) Math.max(minimal, above ? formula.eval(initial) : Math.min(initial, formula.eval(initial)));
    }

    private void reloadInitials(World world) {
        this.animal = world.getAnimalSpawnLimit();
        this.ambient = world.getAmbientSpawnLimit();
        this.monster = world.getMonsterSpawnLimit();
        this.waterAnimal = world.getWaterAnimalSpawnLimit();
        this.waterAmbient = world.getWaterAmbientSpawnLimit();
    }
}
