/*
 * Copyright (C) 2015 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.utility;

import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * @author eccentric_nz
 */
public class TARDISStaticUtils {

    /**
     * Get the direction a player is facing.
     *
     * @param p    the player
     * @param swap whether to swap the direction E &lt;-&gt; W, S &lt;-&gt; N
     * @return the direction the player is facing
     */
    public static String getPlayersDirection(Player p, boolean swap) {
        // get player direction
        float pyaw = p.getLocation().getYaw();
        if (pyaw >= 0) {
            pyaw = (pyaw % 360);
        } else {
            pyaw = (360 + (pyaw % 360));
        }
        // determine direction player is facing
        String d = "";
        if (pyaw >= 315 || pyaw < 45) {
            d = (swap) ? "NORTH" : "SOUTH";
        }
        if (pyaw >= 225 && pyaw < 315) {
            d = (swap) ? "WEST" : "EAST";
        }
        if (pyaw >= 135 && pyaw < 225) {
            d = (swap) ? "SOUTH" : "NORTH";
        }
        if (pyaw >= 45 && pyaw < 135) {
            d = (swap) ? "EAST" : "WEST";
        }
        return d;
    }

    /**
     * Get whether the biome is ocean.
     *
     * @param b the biome to check
     * @return true if it is ocean
     */
    public static boolean isOceanBiome(Biome b) {
        return (b.equals(Biome.OCEAN) || b.equals(Biome.DEEP_OCEAN) || b.equals(Biome.FROZEN_OCEAN));
    }

    /**
     * Get a human readable time from server ticks.
     *
     * @param t the time in ticks
     * @return a string representation of the time
     */
    public static String getTime(long t) {
        if (t > 0 && t <= 2000) {
            return "early morning";
        }
        if (t > 2000 && t <= 3500) {
            return "mid morning";
        }
        if (t > 3500 && t <= 5500) {
            return "late morning";
        }
        if (t > 5500 && t <= 6500) {
            return "around noon";
        }
        if (t > 6500 && t <= 8000) {
            return "afternoon";
        }
        if (t > 8000 && t <= 10000) {
            return "mid afternoon";
        }
        if (t > 10000 && t <= 12000) {
            return "late afternoon";
        }
        if (t > 12000 && t <= 14000) {
            return "twilight";
        }
        if (t > 14000 && t <= 16000) {
            return "evening";
        }
        if (t > 16000 && t <= 17500) {
            return "late evening";
        }
        if (t > 17500 && t <= 18500) {
            return "around midnight";
        }
        if (t > 18500 && t <= 20000) {
            return "the small hours";
        }
        if (t > 20000 && t <= 22000) {
            return "the wee hours";
        } else {
            return "pre-dawn";
        }
    }

    /**
     * Checks whether a door is open.
     *
     * @param door_bottom the bottom door block
     * @param dd          the direction the door is facing
     * @return true or false
     */
    public static boolean isOpen(Block door_bottom, COMPASS dd) {
        byte door_data = door_bottom.getData();
        switch (dd) {
            case NORTH:
                if (door_data == 7) {
                    return true;
                }
                break;
            case WEST:
                if (door_data == 6) {
                    return true;
                }
                break;
            case SOUTH:
                if (door_data == 5) {
                    return true;
                }
                break;
            default:
                if (door_data == 4) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Gets the column to set the Police box sign in if CTM is on in the player's preferences.
     *
     * @param d the direction of the Police Box
     * @return the column
     */
    public static int getCol(COMPASS d) {
        switch (d) {
            case NORTH:
                return 6;
            case WEST:
                return 4;
            case SOUTH:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Sets the Chameleon Sign text or messages the player.
     *
     * @param loc  the location string retrieved from the database
     * @param line the line number to set
     * @param text the text to write
     * @param p    the player to message (if the Chameleon control is not a sign)
     */
    public static void setSign(String loc, int line, String text, Player p) {
        // get sign block so we can update it
        Location l = TARDISLocationGetters.getLocationFromDB(loc, 0, 0);
        if (l != null) {
            Chunk chunk = l.getChunk();
            while (!chunk.isLoaded()) {
                chunk.load();
            }
            Block cc = l.getBlock();
            if (cc.getType() == Material.WALL_SIGN || cc.getType() == Material.SIGN_POST) {
                Sign sign = (Sign) cc.getState();
                sign.setLine(line, text);
                sign.update();
            } else {
                TARDISMessage.send(p, "CHAM", " " + text);
            }
        }
    }

    /**
     * Gets the Chameleon Sign preset text.
     *
     * @param loc the location string retrieved from the database
     * @return the last line of the sign
     */
    public static String getLastLine(String loc) {
        // get sign block so we can read it
        String str = "";
        Location l = TARDISLocationGetters.getLocationFromDB(loc, 0, 0);
        if (l != null) {
            Block cc = l.getBlock();
            if (cc.getType() == Material.WALL_SIGN || cc.getType() == Material.SIGN_POST) {
                Sign sign = (Sign) cc.getState();
                str = sign.getLine(3);
            }
        }
        return str;
    }
}
