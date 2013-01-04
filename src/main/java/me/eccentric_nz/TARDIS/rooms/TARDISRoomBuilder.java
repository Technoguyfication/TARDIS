/*
 * Copyright (C) 2012 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.rooms;

import java.util.HashMap;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants.COMPASS;
import me.eccentric_nz.TARDIS.TARDISConstants.ROOM;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.TARDISDatabase;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author eccentric_nz
 */
public class TARDISRoomBuilder {

    private final TARDIS plugin;
    TARDISDatabase service = TARDISDatabase.getInstance();
    private String r;
    private Location l;
    private COMPASS d;
    private Player p;

    public TARDISRoomBuilder(TARDIS plugin, String r, Location l, COMPASS d, Player p) {
        this.plugin = plugin;
        this.r = r;
        this.l = l;
        this.d = d;
        this.p = p;
    }

    public boolean build() {
        HashMap<String, Object> where = new HashMap<String, Object>();
        where.put("owner", p.getName());
        ResultSetTardis rs = new ResultSetTardis(plugin, where, "", false);
        if (rs.resultSet()) {
            TARDISRoomData roomData = new TARDISRoomData();
            roomData.setTardis_id(rs.getTardis_id());
            // get middle data, default to orange wool if not set
            int middle_id = (rs.getMiddle_id() != 0) ? rs.getMiddle_id() : 35;
            roomData.setMiddle_id(middle_id);
            byte middle_data = (rs.getMiddle_data() != 0) ? rs.getMiddle_data() : 1;
            roomData.setMiddle_data(middle_data);
            plugin.debug(middle_id + ":" + middle_data);
            // get start locations
            Block b = l.getBlock();
            roomData.setBlock(b);
            roomData.setDirection(d);
            roomData.setLocation(l);
            switch (d) {
                case NORTH:
                    if (r.equalsIgnoreCase("PASSAGE")) {
                        l.setX(l.getX() + 4);
                    } else {
                        l.setX(l.getX() + 6);
                    }
                    break;
                case WEST:
                    if (r.equalsIgnoreCase("PASSAGE")) {
                        l.setZ(l.getZ() + 4);
                    } else {
                        l.setZ(l.getZ() + 6);
                    }
                    break;
                case SOUTH:
                    if (r.equalsIgnoreCase("PASSAGE")) {
                        l.setX(l.getX() - 4);
                    } else {
                        l.setX(l.getX() - 6);
                    }
                    break;
                default:
                    if (r.equalsIgnoreCase("PASSAGE")) {
                        l.setZ(l.getZ() - 4);
                    } else {
                        l.setZ(l.getZ() - 6);
                    }
                    break;
            }
            switch (ROOM.valueOf(r)) {
                case PASSAGE:
                    l.setY(l.getY() - 2);
                    break;
                case POOL:
                    l.setY(l.getY() - 3);
                    break;
                case ARBORETUM:
                    l.setY(l.getY() - 4);
                    break;
                default:
                    l.setY(l.getY() - 1);
                    break;
            }
            if (d.equals(COMPASS.EAST) || d.equals(COMPASS.SOUTH)) {
                roomData.setX(1);
                roomData.setZ(1);
            } else {
                roomData.setX(-1);
                roomData.setZ(-1);
            }
            String[][][] s;
            short[] dimensions;
            ROOM room = ROOM.valueOf(r);
            roomData.setRoom(room);
            switch (room) {
                case ARBORETUM:
                    s = plugin.arboretumschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case BEDROOM:
                    s = plugin.bedroomschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case KITCHEN:
                    s = plugin.kitchenschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case LIBRARY:
                    s = plugin.libraryschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case POOL:
                    s = plugin.poolschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case VAULT:
                    s = plugin.vaultschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                case EMPTY:
                    s = plugin.emptyschematic;
                    dimensions = plugin.roomdimensions;
                    break;
                default:
                    // PASSAGE
                    s = plugin.passageschematic;
                    dimensions = plugin.passagedimensions;
                    break;
            }
            roomData.setSchematic(s);
            roomData.setDimensions(dimensions);

            // set door space to air
            b.setTypeId(0);
            b.getRelative(BlockFace.UP).setTypeId(0);

            TARDISRoomRunnable runnable = new TARDISRoomRunnable(plugin, roomData);
            int taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 5L, 5L);
            runnable.setTask(taskID);
        }
        return true;
    }
}