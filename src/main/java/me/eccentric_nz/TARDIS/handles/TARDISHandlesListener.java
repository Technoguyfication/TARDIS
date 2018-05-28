/*
 * Copyright (C) 2018 eccentric_nz
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
package me.eccentric_nz.TARDIS.handles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetControls;
import me.eccentric_nz.TARDIS.database.ResultSetTardisID;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author eccentric_nz
 */
public class TARDISHandlesListener implements Listener {

    private final TARDIS plugin;

    public TARDISHandlesListener(TARDIS plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onHandlesBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (!b.getType().equals(Material.SILVER_GLAZED_TERRACOTTA)) {
            return;
        }
        // check location
        HashMap<String, Object> where = new HashMap<>();
        where.put("type", 26);
        where.put("location", b.getLocation().toString());
        ResultSetControls rsc = new ResultSetControls(plugin, where, false);
        if (!rsc.resultSet()) {
            return;
        }
        event.setCancelled(true);
        // set block to AIR
        b.setType(Material.AIR);
        // drop a custom SILVER_GLAZED_TERRACOTTA
        ItemStack is = new ItemStack(Material.SILVER_GLAZED_TERRACOTTA, 1);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("Handles");
        im.setLore(Arrays.asList("Cyberhead from the", "Maldovarium Market"));
        is.setItemMeta(im);
        b.getWorld().dropItemNaturally(b.getLocation(), is);
        // remove control record
        HashMap<String, Object> wherec = new HashMap<>();
        wherec.put("c_id", rsc.getC_id());
        new QueryFactory(plugin).doDelete("controls", wherec);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHandlesPlace(BlockPlaceEvent event) {
        ItemStack is = event.getItemInHand();
        if (!is.getType().equals(Material.SILVER_GLAZED_TERRACOTTA) || !is.hasItemMeta()) {
            return;
        }
        ItemMeta im = is.getItemMeta();
        if (im.hasDisplayName() && im.getDisplayName().equals("Handles")) {
            // cannot place unless inside the TARDIS
            if (!plugin.getUtils().inTARDISWorld(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            // must have a TARDIS
            HashMap<String, Object> where = new HashMap<>();
            where.put("uuid", uuid.toString());
            ResultSetTardisID rs = new ResultSetTardisID(plugin);
            if (rs.fromUUID(uuid.toString())) {
                // check if they have a handles block
                HashMap<String, Object> wherec = new HashMap<>();
                wherec.put("tardis_id", rs.getTardis_id());
                wherec.put("type", 26);
                ResultSetControls rsc = new ResultSetControls(plugin, wherec, false);
                if (!rsc.resultSet()) {
                    String l = event.getBlock().getLocation().toString();
                    new QueryFactory(plugin).insertControl(rs.getTardis_id(), 26, l, 0);
                } else {
                    event.setCancelled(true);
                    TARDISMessage.send(event.getPlayer(), "HANDLES_PLACED");
                }
            }
        }
    }
}
