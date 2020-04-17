package com.theboxmc.tempmute.commands;

import com.theboxmc.tempmute.TempMute;
import me.arifbanai.idLogger.exceptions.PlayerNotIDLoggedException;
import me.arifbanai.idLogger.interfaces.IDLoggerCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.UUID;

public class Unmute implements CommandExecutor {

    private TempMute plugin;

    public Unmute(final TempMute plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param cmd Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("unmute")) {
            if (!sender.hasPermission(cmd.getPermission())) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "You don't have permission!");
                return true;
            }

            if(args.length != 1) {
                sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "The proper syntax is "
                        + ChatColor.DARK_RED + "/unmute <player>");
                return true;
            }

            plugin.getIdLogger().doAsyncUUIDLookup(args[0], new IDLoggerCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    UUID playerUUID = UUID.fromString(s);
                    if(plugin.getMuteManager().hasPlayer(playerUUID)) {
                        plugin.getMuteManager().removePlayer(playerUUID);
                        try {
                            plugin.getSqlManager().deletePlayer(playerUUID);
                        } catch (SQLException e) {
                            plugin.handleSqlError(e, "Unmute command");
                        }

                        sender.sendMessage(TempMute.PREFIX + ChatColor.DARK_GREEN + args[0]
                                                           + ChatColor.GREEN + " has been unmuted!");

                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                        if(offlinePlayer.isOnline()) {
                            Player player = offlinePlayer.getPlayer();
                            player.sendMessage(TempMute.PREFIX + ChatColor.GREEN + "You have been unmuted!");
                        }
                    } else {
                        sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "This player is not muted!");
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    if(throwable instanceof PlayerNotIDLoggedException) {
                        sender.sendMessage(TempMute.PREFIX + ChatColor.RED + "Player "
                                        + ChatColor.GREEN + args[0]
                                        + ChatColor.RED + " not found");
                        return;
                    }
                }
            });

            return true;
        }

        return false;
    }
}
