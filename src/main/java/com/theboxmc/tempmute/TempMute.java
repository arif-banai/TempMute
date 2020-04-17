package com.theboxmc.tempmute;

import com.theboxmc.tempmute.commands.Tempmute;
import com.theboxmc.tempmute.commands.Unmute;
import com.theboxmc.tempmute.exceptions.PlayerNotFoundException;
import com.theboxmc.tempmute.exceptions.PlayerNotMutedException;
import com.theboxmc.tempmute.interfaces.TempMuteCallback;
import com.theboxmc.tempmute.managers.MuteManager;
import com.theboxmc.tempmute.managers.SQLiteManager;
import com.theboxmc.tempmute.utils.Utils;
import me.arifbanai.idLogger.IDLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class TempMute extends JavaPlugin implements Listener {

    public static final String PREFIX = "[" + ChatColor.GREEN + "TempMute" + ChatColor.RESET + "] ";

    private IDLogger idLogger;
    private SQLiteManager sqlManager;
    private MuteManager muteManager;

    @Override
    public void onEnable() {
        idLogger = (IDLogger) Bukkit.getPluginManager().getPlugin("IDLogger");
        muteManager = new MuteManager(this);

        try {
            sqlManager = new SQLiteManager(this);

            sqlManager.setupDb();
        } catch (SQLException | ClassNotFoundException e) {
            this.getLogger().log(Level.SEVERE, "Problem setting up SQLite. Shutting down...");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }

        setupCommands();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((JavaPlugin) this);
    }

    public SQLiteManager getSqlManager() {
        return sqlManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public IDLogger getIdLogger() {
        return idLogger;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();

        try {
            long timeLeftMutedMilli = muteManager.getTimeMutedUntil(p.getUniqueId());

            if(timeLeftMutedMilli > 0) {
                System.out.println("Muted");
                event.setCancelled(true);
                Utils.notifyPlayerOfBeingMuted(p, timeLeftMutedMilli);
            }
        } catch (PlayerNotMutedException | SQLException e) {
            //Do nothing
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        sqlManager.doAsyncGetMuteTime(p.getUniqueId(), new TempMuteCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                long timeMutedUntil = result;

                if(timeMutedUntil -  System.currentTimeMillis() > 0) {
                    //Player is on mute list, add to hashmap
                    muteManager.addPlayer(p.getUniqueId(), timeMutedUntil);
                } else {
                    //Player is on mute list but the time has expired
                    muteManager.removePlayer(p.getUniqueId());
                    sqlManager.doAsyncDeletePlayer(p.getUniqueId(), new TempMuteCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            //Nothing
                        }

                        @Override
                        public void onFailure(Throwable cause) {
                            //Nothing
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable cause) {
                if(cause instanceof PlayerNotFoundException) {
                    return;
                }

                handleSqlError((SQLException) cause, "doAsyncGetMuteTime");
            }
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        muteManager.removePlayer(p.getUniqueId());
        System.out.println("Player removed");
    }

    // Prints the stack trace and says where the SQLException occurred
    public void handleSqlError(SQLException e, String methodOccurred) {
        e.printStackTrace();
        System.err.println("An SQLException occurred during " + methodOccurred);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private void setupCommands() {
        getCommand("tempmute").setExecutor(new Tempmute(this));
        getCommand("unmute").setExecutor(new Unmute(this));
    }
}
