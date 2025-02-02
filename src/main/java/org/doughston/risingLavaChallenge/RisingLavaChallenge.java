package org.doughston.risingLavaChallenge;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RisingLavaChallenge extends JavaPlugin {

    private int currentLavaLevel = 0;
    private BukkitRunnable lavaTask;
    private int worldSizeX = 200;
    private int worldSizeZ = 200;
    private long intervalTicks = 600L; // Default to 30 seconds
    private int centerX;
    private int centerZ;
    private boolean borderSet = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(this.getCommand("startlava")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("stoplava")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("setborder")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("pauselava")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("resumelava")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("help")).setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        stopLavaRisingTask();
    }

    private void startLavaRisingTask(Player player, int startLayer, long startDelayTicks) {
        if (lavaTask != null) {
            lavaTask.cancel();
        }
        currentLavaLevel = player.getLocation().getBlockY() + startLayer; // Start at the specified layer
        if (!borderSet) {
            centerX = player.getLocation().getBlockX();
            centerZ = player.getLocation().getBlockZ();
        }
        World world = player.getWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(Math.max(worldSizeX, worldSizeZ));

        lavaTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("Lava will rise in 10 seconds!");
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int x = centerX - worldSizeX / 2; x <= centerX + worldSizeX / 2; x++) {
                            for (int z = centerZ - worldSizeZ / 2; z <= centerZ + worldSizeZ / 2; z++) {
                                world.getBlockAt(x, currentLavaLevel, z).setType(Material.LAVA);
                            }
                        }
                        currentLavaLevel++;
                    }
                }.runTaskLater(RisingLavaChallenge.this, 200L); // 10 seconds later
            }
        };
        lavaTask.runTaskTimer(this, startDelayTicks, intervalTicks); // Run task at the specified interval
    }

    private void stopLavaRisingTask() {
        if (lavaTask != null) {
            lavaTask.cancel();
            lavaTask = null;
        }
    }

    private void setWorldBorder(@NotNull Player player, int sizeX, int sizeZ) {
        World world = player.getWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        centerX = player.getLocation().getBlockX();
        centerZ = player.getLocation().getBlockZ();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(Math.max(sizeX, sizeZ));
        worldSizeX = sizeX;
        worldSizeZ = sizeZ;
        borderSet = true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (command.getName().equalsIgnoreCase("startlava")) {
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                if (args.length == 4 || args.length == 5) {
                    try {
                        int timeInSeconds = Integer.parseInt(args[0]);
                        worldSizeX = Integer.parseInt(args[1]);
                        worldSizeZ = Integer.parseInt(args[2]);
                        intervalTicks = timeInSeconds * 20L; // Convert seconds to ticks
                        long startDelayTicks = Long.parseLong(args[3]) * 20L; // Convert seconds to ticks
                        int startLayer = (args.length == 5) ? Integer.parseInt(args[4]) : -3;
                        if (sender instanceof Player) {
                            startLavaRisingTask((Player) sender, startLayer, startDelayTicks);
                        }
                        sender.sendMessage("Lava rising challenge started with world size " + worldSizeX + "x" + worldSizeZ + ", interval " + (intervalTicks / 20) + " seconds, start delay " + (startDelayTicks / 20) + " seconds, and starting layer " + startLayer + "!");
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid arguments. Please provide four or five integers.");
                        return false;
                    }
                } else {
                    sender.sendMessage("Invalid number of arguments. Usage: /startlava <timeInSeconds> <worldSizeX> <worldSizeZ> <startDelayInSeconds> [startLayer]");
                    return false;
                }
            }
        } else if (command.getName().equalsIgnoreCase("stoplava")) {
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                stopLavaRisingTask();
                sender.sendMessage("Lava rising challenge stopped!");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("setborder")) {
            if (sender instanceof Player) {
                if (args.length == 2) {
                    try {
                        int sizeX = Integer.parseInt(args[0]);
                        int sizeZ = Integer.parseInt(args[1]);
                        setWorldBorder((Player) sender, sizeX, sizeZ);
                        sender.sendMessage("World border set to " + sizeX + "x" + sizeZ + " centered at your current location.");
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid arguments. Please provide two integers.");
                        return false;
                    }
                } else {
                    sender.sendMessage("Invalid number of arguments. Usage: /setborder <worldSizeX> <worldSizeZ>");
                    return false;
                }
            }
        } else if (command.getName().equalsIgnoreCase("help")) {
            sender.sendMessage("""
                    Available commands:
                    /startlava <timeInSeconds> <worldSizeX> <worldSizeZ> <startDelayInSeconds> [startLayer] - Starts the lava rising challenge
                    /stoplava - Stops the lava rising challenge
                    /setborder <worldSizeX> <worldSizeZ> - Sets the world border
                    /pauselava - Pauses the lava rising challenge
                    /resumelava - Resumes the lava rising challenge
                    /help - Displays this help message""");
            return true;
        }
        return false;
    }
}