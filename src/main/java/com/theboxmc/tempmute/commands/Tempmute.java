package com.theboxmc.tempmute.commands;

import com.theboxmc.tempmute.TempMute;
import com.theboxmc.tempmute.exceptions.InvalidTimeUnitException;
import com.theboxmc.tempmute.interfaces.TempMuteCallback;
import com.theboxmc.tempmute.utils.TimeUtils;
import com.theboxmc.tempmute.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Tempmute implements CommandExecutor {

    private TempMute plugin;

    public Tempmute(final TempMute plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     * <br>
     * /tempmute <player> <time>
     * <br>
     * The <time> argument accepts a single character at the end to denote a time unit.
     * The acceptable time units are:
     * <br> d = day
     * <br> h = hour
     * <br> m = minutes
     * <br> s = seconds
     * <br>
     * If no time unit is used, it defaults to seconds
     *
     * @param sender  Source of the command
     * @param cmd Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("tempmute")) {

            if (!sender.hasPermission(Objects.requireNonNull(cmd.getPermission()))) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "You don't have permission!");
                return true;
            }

            if(args.length != 2) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "The proper syntax is "
                                 + ChatColor.DARK_RED + "/tempmute <player> <time>");
                return true;
            }

            // Can only mute an online player
            Player player = Bukkit.getPlayer(args[0]);

            if(player == null) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "Player not found!");
                return true;
            }

            if(player.hasPermission("tempmute.nomute")) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "This player cannot be muted!");
                return true;
            }

            if(plugin.getMuteManager().hasPlayer(player.getUniqueId())) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "This player is already muted!");
                return true;
            }

            String timeArg = args[1];

            try {
                char lastChar = timeArg.charAt(timeArg.length() - 1);

                TimeUnit timeUnit;
                long timeMuted;

                if(Character.isAlphabetic(lastChar)) {
                    timeUnit = TimeUtils.getTimeUnit(lastChar);
                    timeMuted = Long.parseLong(timeArg.substring(0, timeArg.length()-1));
                } else {
                    timeUnit = TimeUnit.SECONDS;
                    timeMuted = Long.parseLong(timeArg);
                }

                if(timeMuted <= 0) {
                    sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "Time cannot be less than or equal to 0.");
                    return true;
                }

                long timeUntilUnmuted = System.currentTimeMillis() + timeUnit.toMillis(timeMuted);

                plugin.getMuteManager().addPlayer(player.getUniqueId(), timeUntilUnmuted);

                plugin.getSqlManager().doAsyncAddPlayer(player.getUniqueId(), timeUntilUnmuted, new TempMuteCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        sender.sendMessage(plugin.PREFIX + ChatColor.GREEN + "Player "
                                + ChatColor.DARK_GREEN + player.getName()
                                + ChatColor.GREEN + " has been muted for "
                                + ChatColor.GOLD + timeMuted + " "
                                + ChatColor.BLUE + timeUnit.toString());

                        Utils.notifyPlayerOfBeingMuted(player, timeUntilUnmuted);
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        sender.sendMessage("An SQL Error occurred, notify admin.");
                        plugin.handleSqlError((SQLException) cause, "/tempmute");
                    }
                });
            } catch (NumberFormatException e) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "Invalid time");
                return true;
            } catch (InvalidTimeUnitException e) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "Invalid time or timecode.");
                return true;
            }

            return true;
        }

        return false;
    }
}
