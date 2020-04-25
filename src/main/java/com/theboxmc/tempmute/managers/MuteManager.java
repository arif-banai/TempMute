package com.theboxmc.tempmute.managers;

import com.theboxmc.tempmute.TempMute;
import com.theboxmc.tempmute.exceptions.PlayerNotMutedException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

/**
 * All online muted players are stored in the HashMap contained
 * in this class.
 * <br>
 * Players are removed from the map when <method>isMuted(Player player)</method>
 * returns a negative number.
 *
 * @author Arif Banai
 */
public class MuteManager {

    final TempMute plugin;

    HashMap<UUID, Long> mutedOnlinePlayers;

    public MuteManager(final TempMute plugin) {
        this.plugin = plugin;
        mutedOnlinePlayers = new HashMap<>();
    }

    public void addPlayer(UUID uuid, long timeMuted) {
        mutedOnlinePlayers.put(uuid, timeMuted);
    }

    public void removePlayer(UUID uuid) {
        mutedOnlinePlayers.remove(uuid);
    }

    /**
     * Whether or not a player is on the mute list.
     * <br>
     * This does not necessairily mean the player is muted.
     * @param uuid The UUID of a player who may be on the mute list.
     * @return Whether or not the player is on the mute list
     */
    public boolean hasPlayer(UUID uuid) {
        return mutedOnlinePlayers.containsKey(uuid);
    }

    //Checks if a player is in the mute hashmap, and if the timestamp in the map is later than the current time,
    //the return the time. If not, make sure to remove the key-value pair from the hashmap and return -1L;
    public long getTimeMutedUntil(UUID uuid) throws PlayerNotMutedException, SQLException {
        if(hasPlayer(uuid)) {
            Long timeMutedUntilMilli = mutedOnlinePlayers.get(uuid);

            long timeLeft = timeMutedUntilMilli - System.currentTimeMillis();

            if(timeLeft > 0) {
                return timeMutedUntilMilli;
            } else {
                //The mute timer is over.
                mutedOnlinePlayers.remove(uuid);
                plugin.getSqlManager().deletePlayer(uuid);

                throw new PlayerNotMutedException();
            }
        }

        throw new PlayerNotMutedException();
    }
}
