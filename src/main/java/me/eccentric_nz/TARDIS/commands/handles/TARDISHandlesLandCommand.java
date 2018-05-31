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
package me.eccentric_nz.TARDIS.commands.handles;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitChecker;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitDamager;
import me.eccentric_nz.TARDIS.artron.TARDISArtronIndicator;
import me.eccentric_nz.TARDIS.artron.TARDISArtronLevels;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetControls;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.database.ResultSetTardis;
import me.eccentric_nz.TARDIS.database.data.Tardis;
import me.eccentric_nz.TARDIS.enumeration.DIFFICULTY;
import me.eccentric_nz.TARDIS.enumeration.DISK_CIRCUIT;
import me.eccentric_nz.TARDIS.enumeration.PRESET;
import me.eccentric_nz.TARDIS.flight.TARDISHandbrakeListener;
import me.eccentric_nz.TARDIS.flight.TARDISLand;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISSounds;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

import java.util.Collections;
import java.util.HashMap;

/**
 * @author eccentric_nz
 */
public class TARDISHandlesLandCommand {

    private final TARDIS plugin;

    public TARDISHandlesLandCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    public boolean exitVortex(Player player, int id, String uuid) {
        HashMap<String, Object> wherei = new HashMap<>();
        wherei.put("tardis_id", id);
        ResultSetTardis rs = new ResultSetTardis(plugin, wherei, "", false, 2);
        if (rs.resultSet()) {
            Tardis tardis = rs.getTardis();
            if (tardis.getPreset().equals(PRESET.JUNK)) {
                TARDISMessage.handlesSend(player, "HANDLES_JUNK");
                return true;
            }
            if (!tardis.isHandbrake_on()) {
                TARDISMessage.handlesSend(player, "HANDBRAKE_ON_ERR");
                return true;
            }
            // must have a destination
            if (!plugin.getTrackerKeeper().getHasDestination().containsKey(id)) {
                TARDISMessage.handlesSend(player, "TRAVEL_NEED_DEST");
                return true;
            }
            new TARDISLand(plugin, id, player).exitVortex();
            // delay setting handbrake
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                HashMap<String, Object> whereh = new HashMap<>();
                whereh.put("type", 0);
                whereh.put("tardis_id", id);
                ResultSetControls rsc = new ResultSetControls(plugin, whereh, false);
                if (rsc.resultSet()) {
                    Location location = plugin.getLocationUtils().getLocationFromBukkitString(rsc.getLocation());
                    BlockState state = location.getBlock().getState();
                    Lever lever = (Lever) state.getData();
                    TARDISSounds.playTARDISSound(location, "tardis_handbrake_engage");
                    // Changes the lever to on
                    lever.setPowered(true);
                    state.setData(lever);
                    state.update();
                    // Check if it's at a recharge point
                    TARDISArtronLevels tal = new TARDISArtronLevels(plugin);
                    tal.recharge(id);
                    HashMap<String, Object> wherek = new HashMap<>();
                    wherek.put("uuid", uuid);
                    ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, wherek);
                    boolean beac_on = true;
                    String beacon = tardis.getBeacon();
                    if (rsp.resultSet()) {
                        beac_on = rsp.isBeaconOn();
                    }
                    if (!beac_on && !beacon.isEmpty()) {
                        TARDISHandbrakeListener.toggleBeacon(beacon, false);
                    }
                    // Remove energy from TARDIS and sets database
                    TARDISMessage.send(player, "HANDBRAKE_ON");
                    int amount = plugin.getTrackerKeeper().getHasDestination().get(id) * -1;
                    QueryFactory qf = new QueryFactory(plugin);
                    HashMap<String, Object> wheret = new HashMap<>();
                    wheret.put("tardis_id", id);
                    qf.alterEnergyLevel("tardis", amount, wheret, player);
                    new TARDISArtronIndicator(plugin).showArtronLevel(player, id, Math.abs(amount));
                    plugin.getTrackerKeeper().getHasDestination().remove(id);
                    if (plugin.getTrackerKeeper().getHasRandomised().contains(id)) {
                        plugin.getTrackerKeeper().getHasRandomised().removeAll(Collections.singleton(id));
                    }
                    // damage the circuit if configured
                    TARDISCircuitChecker tcc = null;
                    if (!plugin.getDifficulty().equals(DIFFICULTY.EASY) && !plugin.getUtils().inGracePeriod(player, true)) {
                        tcc = new TARDISCircuitChecker(plugin, id);
                        tcc.getCircuits();
                    }
                    if (tcc != null && plugin.getConfig().getBoolean("circuits.damage") && plugin.getConfig().getInt("circuits.uses.materialisation") > 0) {
                        // decrement uses
                        int uses_left = tcc.getMaterialisationUses();
                        new TARDISCircuitDamager(plugin, DISK_CIRCUIT.MATERIALISATION, uses_left, id, player).damage();
                    }
                    HashMap<String, Object> set = new HashMap<>();
                    set.put("handbrake_on", 1);
                    HashMap<String, Object> whereb = new HashMap<>();
                    whereb.put("tardis_id", id);
                    qf.doUpdate("tardis", set, whereb);
                }
            }, 400L); // TODO check delay
        }
        return true;
    }
}
