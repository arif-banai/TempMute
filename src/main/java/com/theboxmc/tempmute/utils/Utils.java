package com.theboxmc.tempmute.utils;

import com.theboxmc.tempmute.TempMute;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static void notifyPlayerOfBeingMuted(Player p, long timeUntilUnmuted) {

        Date dateUnmuted = new Date(timeUntilUnmuted);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy hh:mm:ss aa");

        String formatDate = sdf.format(dateUnmuted);
        String[] tokens = formatDate.split(" ");

        p.sendMessage(TempMute.PREFIX + ChatColor.RED + "You are muted until "
                    + ChatColor.WHITE + tokens[0] + " "
                    + ChatColor.GOLD + tokens[1] + " "
                    + ChatColor.GREEN + tokens[2] + ChatColor.RED + "!");
    }
}
