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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitChecker;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitDamager;
import me.eccentric_nz.TARDIS.database.ResultSetCurrentLocation;
import me.eccentric_nz.TARDIS.database.ResultSetNextLocation;
import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import me.eccentric_nz.TARDIS.enumeration.DIFFICULTY;
import me.eccentric_nz.TARDIS.enumeration.DISK_CIRCUIT;
import static me.eccentric_nz.TARDIS.listeners.TARDISScannerListener.getNearbyEntities;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISSounds;
import me.eccentric_nz.TARDIS.utility.TARDISStaticUtils;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author eccentric_nz
 */
public class TARDISHandlesScanCommand {

    private final TARDIS plugin;
    private final Player player;
    private final int id;
    private boolean inTARDIS = false;

    public TARDISHandlesScanCommand(TARDIS plugin, Player player, int id) {
        this.plugin = plugin;
        this.player = player;
        this.id = id;
        inTARDIS = plugin.getUtils().inTARDISWorld(this.player);
    }

    public boolean sayScan() {
        TARDISSounds.playTARDISSound(player.getLocation(), "handles_scanner");
        final Location scan_loc;
        String whereisit;
        final COMPASS tardisDirection;
        HashMap<String, Object> wherenl = new HashMap<>();
        wherenl.put("tardis_id", id);
        if (inTARDIS) {
            if (plugin.getTrackerKeeper().getHasDestination().containsKey(id)) {
                ResultSetNextLocation rsn = new ResultSetNextLocation(plugin, wherenl);
                if (!rsn.resultSet()) {
                    TARDISMessage.handlesSend(player, "NEXT_NOT_FOUND");
                    return true;
                }
                scan_loc = new Location(rsn.getWorld(), rsn.getX(), rsn.getY(), rsn.getZ());
                tardisDirection = rsn.getDirection();
                whereisit = plugin.getLanguage().getString("SCAN_NEXT");
            } else {
                ResultSetCurrentLocation rsc = new ResultSetCurrentLocation(plugin, wherenl);
                if (!rsc.resultSet()) {
                    TARDISMessage.handlesSend(player, "CURRENT_NOT_FOUND");
                    return true;
                }
                scan_loc = new Location(rsc.getWorld(), rsc.getX(), rsc.getY(), rsc.getZ());
                tardisDirection = rsc.getDirection();
                whereisit = plugin.getLanguage().getString("SCAN_CURRENT");
            }
        } else {
            scan_loc = player.getLocation();
            tardisDirection = COMPASS.valueOf(TARDISStaticUtils.getPlayersDirection(player, false));
            whereisit = plugin.getLanguage().getString("SCAN_PLAYER");
        }
        // record nearby entities
        final HashMap<EntityType, Integer> scannedentities = new HashMap<>();
        final List<String> playernames = new ArrayList<>();
        for (Entity k : getNearbyEntities(scan_loc, 16)) {
            EntityType et = k.getType();
            if (TARDISConstants.ENTITY_TYPES.contains(et)) {
                boolean visible = true;
                if (et.equals(EntityType.PLAYER)) {
                    Player entPlayer = (Player) k;
                    if (player.canSee(entPlayer)) {
                        playernames.add(entPlayer.getName());
                    } else {
                        visible = false;
                    }
                }
                if (plugin.getPM().isPluginEnabled("TARDISWeepingAngels") && (et.equals(EntityType.SKELETON) || et.equals(EntityType.ZOMBIE) || et.equals(EntityType.PIG_ZOMBIE))) {
                    EntityEquipment ee = ((LivingEntity) k).getEquipment();
                    if (ee.getHelmet() != null) {
                        switch (ee.getHelmet().getType()) {
                            case VINE:
                                // dalek
                                et = EntityType.COMPLEX_PART;
                                break;
                            case IRON_HELMET:
                            case GOLD_HELMET:
                            case CHAINMAIL_HELMET:
                                if (ee.getHelmet().hasItemMeta() && ee.getHelmet().getItemMeta().hasDisplayName()) {
                                    String dn = ee.getHelmet().getItemMeta().getDisplayName();
                                    if (plugin.getBuildKeeper().getTWA_Heads().containsKey(dn)) {
                                        et = plugin.getBuildKeeper().getTWA_Heads().get(dn);
                                    }
                                }
                                break;
                            case STONE_BUTTON:
                                // weeping angel
                                et = EntityType.DRAGON_FIREBALL;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (et.equals(EntityType.ENDERMAN) && k.getPassengers().size() > 0 && k.getPassengers().get(0) != null && k.getPassengers().get(0).getType().equals(EntityType.GUARDIAN)) {
                    // silent
                    et = EntityType.SPLASH_POTION;
                }
                Integer entity_count = (scannedentities.containsKey(et)) ? scannedentities.get(et) : 0;
                if (visible) {
                    scannedentities.put(et, entity_count + 1);
                }
            }
        }
        final long time = scan_loc.getWorld().getTime();
        final String daynight = TARDISStaticUtils.getTime(time);
        // message the player
        if (inTARDIS) {
            TARDISMessage.handlesSend(player, "SCAN_RESULT", whereisit);
        } else {
            TARDISMessage.handlesSend(player, "SCAN_PLAYER");
        }
        String worldname;
        if (plugin.isMVOnServer()) {
            worldname = plugin.getMVHelper().getAlias(scan_loc.getWorld());
        } else {
            worldname = scan_loc.getWorld().getName();
        }
        TARDISMessage.handlesSend(player, "SCAN_WORLD", worldname);
        TARDISMessage.handlesSend(player, "SONIC_COORDS", scan_loc.getBlockX() + ":" + scan_loc.getBlockY() + ":" + scan_loc.getBlockZ());
        BukkitScheduler bsched = plugin.getServer().getScheduler();
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "SCAN_DIRECTION", tardisDirection.toString());
        }, 20L);
        // get biome
        Biome tmb;
        if (whereisit.equals(plugin.getLanguage().getString("SCAN_CURRENT"))) {
            // adjsut for current location as it will always return SKY if set_biome is true
            switch (tardisDirection) {
                case NORTH:
                    tmb = scan_loc.getBlock().getRelative(BlockFace.SOUTH, 2).getBiome();
                    break;
                case WEST:
                    tmb = scan_loc.getBlock().getRelative(BlockFace.EAST, 2).getBiome();
                    break;
                case SOUTH:
                    tmb = scan_loc.getBlock().getRelative(BlockFace.NORTH, 2).getBiome();
                    break;
                default:
                    tmb = scan_loc.getBlock().getRelative(BlockFace.WEST, 2).getBiome();
                    break;
            }
        } else {
            tmb = scan_loc.getBlock().getBiome();
        }
        final Biome biome = tmb;
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "BIOME_TYPE", biome.toString());
        }, 40L);
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "SCAN_TIME", daynight + " / " + time);
        }, 60L);
        // get weather
        final String weather;
        switch (biome) {
            case DESERT:
            case DESERT_HILLS:
            case MUTATED_DESERT:
            case SAVANNA:
            case SAVANNA_ROCK:
            case MUTATED_SAVANNA:
            case MUTATED_SAVANNA_ROCK:
            case MESA:
            case MUTATED_MESA:
            case MUTATED_MESA_CLEAR_ROCK:
            case MUTATED_MESA_ROCK:
            case MESA_ROCK:
            case MESA_CLEAR_ROCK:
                weather = plugin.getLanguage().getString("WEATHER_DRY");
                break;
            case ICE_FLATS:
            case MUTATED_ICE_FLATS:
            case FROZEN_OCEAN:
            case FROZEN_RIVER:
            case COLD_BEACH:
            case TAIGA_COLD:
            case TAIGA_COLD_HILLS:
            case MUTATED_TAIGA_COLD:
                weather = (scan_loc.getWorld().hasStorm()) ? plugin.getLanguage().getString("WEATHER_SNOW") : plugin.getLanguage().getString("WEATHER_COLD");
                break;
            default:
                weather = (scan_loc.getWorld().hasStorm()) ? plugin.getLanguage().getString("WEATHER_RAIN") : plugin.getLanguage().getString("WEATHER_CLEAR");
                break;
        }
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "SCAN_WEATHER", weather);
        }, 80L);
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "SCAN_HUMIDITY", String.format("%.2f", scan_loc.getBlock().getHumidity()));
        }, 100L);
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            TARDISMessage.handlesSend(player, "SCAN_TEMP", String.format("%.2f", scan_loc.getBlock().getTemperature()));
        }, 120L);
        bsched.scheduleSyncDelayedTask(plugin, () -> {
            if (scannedentities.size() > 0) {
                TARDISMessage.handlesSend(player, "SCAN_ENTS");
                for (Map.Entry<EntityType, Integer> entry : scannedentities.entrySet()) {
                    String message = "";
                    StringBuilder buf = new StringBuilder();
                    if (entry.getKey().equals(EntityType.PLAYER) && playernames.size() > 0) {
                        playernames.forEach((p) -> {
                            buf.append(", ").append(p);
                        });
                        message = " (" + buf.toString().substring(2) + ")";
                    }
                    switch (entry.getKey()) {
                        case AREA_EFFECT_CLOUD:
                            player.sendMessage("    Cyberman: " + entry.getValue());
                            break;
                        case COMPLEX_PART:
                            player.sendMessage("    Dalek: " + entry.getValue());
                            break;
                        case ARMOR_STAND:
                            player.sendMessage("    Empty Child: " + entry.getValue());
                            break;
                        case ARROW:
                            player.sendMessage("    Ice Warrior: " + entry.getValue());
                            break;
                        case SPLASH_POTION:
                            player.sendMessage("    Silent: " + entry.getValue());
                            break;
                        case BOAT:
                            player.sendMessage("    Silurian: " + entry.getValue());
                            break;
                        case FIREWORK:
                            player.sendMessage("    Sontaran: " + entry.getValue());
                            break;
                        case EGG:
                            player.sendMessage("    Strax: " + entry.getValue());
                            break;
                        case ENDER_CRYSTAL:
                            player.sendMessage("    Vashta Nerada: " + entry.getValue());
                            break;
                        case DRAGON_FIREBALL:
                            player.sendMessage("    Weeping Angel: " + entry.getValue());
                            break;
                        case FISHING_HOOK:
                            player.sendMessage("    Zygon: " + entry.getValue());
                            break;
                        default:
                            player.sendMessage("    " + entry.getKey() + ": " + entry.getValue() + message);
                            break;
                    }
                }
                scannedentities.clear();
            } else {
                TARDISMessage.handlesSend(player, "SCAN_NONE");
            }
            // damage the circuit if configured
            if (plugin.getConfig().getBoolean("circuits.damage") && !plugin.getDifficulty().equals(DIFFICULTY.EASY) && plugin.getConfig().getInt("circuits.uses.scanner") > 0) {
                TARDISCircuitChecker tcc = new TARDISCircuitChecker(plugin, id);
                tcc.getCircuits();
                // decrement uses
                int uses_left = tcc.getScannerUses();
                new TARDISCircuitDamager(plugin, DISK_CIRCUIT.SCANNER, uses_left, id, player).damage();
            }
        }, 140L);
        return true;
    }
}
