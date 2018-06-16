/*
 * Copyright (C) 2016 eccentric_nz
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
package me.eccentric_nz.TARDIS.ARS;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.builders.TARDISInteriorPostioning;
import me.eccentric_nz.TARDIS.builders.TARDISTIPSData;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.data.Tardis;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

/**
 * The architectural reconfiguration system is a component of the Doctor's TARDIS in the shape of a tree that, according
 * to the Eleventh Doctor, "reconstructs the particles according to your needs." It is basically "a machine that makes
 * machines," perhaps somewhat like a 3D printer. It is, according to Gregor Van Baalen's scanner, "more valuable than
 * the total sum of any currency.
 *
 * @author eccentric_nz
 */
public class TARDISARSMapListener extends TARDISARSMethods implements Listener {

    public TARDISARSMapListener(TARDIS plugin) {
        super(plugin);
    }

    /**
     * Listens for player clicking inside an inventory. If the inventory is a TARDIS GUI, then the click is processed
     * accordingly.
     *
     * @param event a player clicking an inventory slot
     */
    @EventHandler(ignoreCancelled = true)
    public void onARSMapClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        String name = inv.getTitle();
        if (name.equals("§4TARDIS Map")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            ids.put(uuid, getTardisId(player.getUniqueId().toString()));
            int slot = event.getRawSlot();
            if (slot != 10 && slot != 45 && !hasLoadedMap.contains(uuid)) {
                TARDISMessage.send(player, "ARS_LOAD");
                return;
            }
            if (slot >= 0 && slot < 54) {
                switch (slot) {
                    case 1:
                    case 9:
                    case 11:
                    case 19:
                        // up
                        moveMap(uuid, inv, slot);
                        break;
                    case 10:
                        // load map
                        loadMap(inv, uuid);
                        break;
                    case 45:
                        // close
                        close(player);
                        break;
                    case 47:
                        // where am I?
                        findPlayer(player, inv);
                        break;
                    case 27:
                    case 28:
                    case 29:
                        // change levels
                        if (map_data.containsKey(uuid)) {
                            switchLevel(inv, slot, uuid);
                            TARDISARSMapData md = map_data.get(uuid);
                            setMap(md.getY(), md.getE(), md.getS(), uuid, inv);
                            setLore(inv, slot, null);
                        } else {
                            setLore(inv, slot, plugin.getLanguage().getString("ARS_LOAD"));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void findPlayer(Player p, Inventory inv) {
        UUID uuid = p.getUniqueId();
        int id = ids.get(uuid);
        // need to get the console location - will be different for non-TIPS TARDISes
        HashMap<String, Object> where = new HashMap<>();
        where.put("tardis_id", id);
        ResultSetTardis rs = new ResultSetTardis(plugin, where, "", false, 0);
        if (rs.resultSet()) {
            Tardis tardis = rs.getTardis();
            int pos = tardis.getTIPS();
            int tx = 0, tz = 0;
            if (pos != -1) {
                // tips slot
                TARDISInteriorPostioning tips = new TARDISInteriorPostioning(plugin);
                TARDISTIPSData coords = tips.getTIPSData(pos);
                tx = coords.getCentreX();
                tz = coords.getCentreZ();
            }
            Location loc = p.getLocation();
            int px = loc.getBlockX();
            int pz = loc.getBlockZ();
            // determine row and col
            int col = (int) (4 + (Math.floor((px - tx) / 16.0d)));
            int row = (int) (4 + (Math.floor((pz - tz) / 16.0d)));
            if (col < 0 || col > 8 || row < 0 || row > 8) {
                // outside ARS grid
                setLore(inv, 47, plugin.getLanguage().getString("ARS_MAP_OUTSIDE"));
                return;
            }
            int east = getOffset(col);
            int south = getOffset(row);
            int py = loc.getBlockY();
            int level = 28;
            if (py >= 48 && py < 64) {
                level = 27;
            }
            if (py >= 80 && py < 96) {
                level = 29;
            }
            // set map
            switchLevel(inv, level, uuid);
            TARDISARSMapData md = map_data.get(uuid);
            md.setY(level - 27);
            md.setE(east);
            md.setS(south);
            setMap(level - 27, east, south, uuid, inv);
            setLore(inv, level, null);
            map_data.put(uuid, md);
            // get itemstack to enchant and change lore
            int slot = ((row - south) * 9) + 4 + (col - east);
            ItemStack is = inv.getItem(slot);
            is.setType(Material.ARROW);
            setLore(inv, slot, plugin.getLanguage().getString("ARS_MAP_HERE"));
        }
    }

    private int getOffset(double d) {
        int offset = 2;
        if (d >= 6) {
            offset = 4;
        }
        if (d == 5) {
            offset = 3;
        }
        if (d == 3) {
            offset = 1;
        }
        if (d <= 2) {
            offset = 0;
        }
        return offset;
    }

    @Override
    public void close(Player p) {
        UUID uuid = p.getUniqueId();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            hasLoadedMap.remove(uuid);
            map_data.remove(uuid);
            ids.remove(uuid);
            p.closeInventory();
        }, 1L);
    }
}
