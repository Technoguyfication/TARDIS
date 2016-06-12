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
package me.eccentric_nz.TARDIS.planets;

import java.util.Random;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.tardisweepingangels.TARDISWeepingAngelsAPI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author eccentric_nz
 */
public class TARDISSkaroSpawnListener implements Listener {

    private final TARDIS plugin;
    private final TARDISWeepingAngelsAPI twaAPI;
    private final Random r = new Random();

    public TARDISSkaroSpawnListener(TARDIS plugin) {
        this.plugin = plugin;
        this.twaAPI = TARDISAngelsAPI.getAPI(this.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDalekSpawn(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(SpawnReason.SPAWNER)) {
            return;
        }
        if (!event.getLocation().getWorld().getName().equals("Skaro")) {
            return;
        }
        if (!event.getEntity().getType().equals(EntityType.SKELETON)) {
            return;
        }
        final LivingEntity le = event.getEntity();
        // it's a Dalek - disguise it!
        twaAPI.setDalekEquipment(le);
        if (plugin.getPlanetsConfig().getBoolean("planets.Skaro.flying_daleks") && r.nextInt(100) < 10) {
            // make the Dalek fly
            EntityEquipment ee = le.getEquipment();
            ee.setChestplate(new ItemStack(Material.ELYTRA, 1));
            // teleport them straight up
            le.teleport(le.getLocation().add(0.0d, 20.0d, 0.0d));
            plugin.getTardisHelper().setFallFlyingTag(le);
        }
    }
}