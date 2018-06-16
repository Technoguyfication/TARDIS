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
package me.eccentric_nz.TARDIS.move;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.api.event.TARDISEnterEvent;
import me.eccentric_nz.TARDIS.api.event.TARDISExitEvent;
import me.eccentric_nz.TARDIS.chatGUI.TARDISUpdateChatGUI;
import me.eccentric_nz.TARDIS.database.QueryFactory;
import me.eccentric_nz.TARDIS.database.ResultSetDoors;
import me.eccentric_nz.TARDIS.database.ResultSetPlayerPrefs;
import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import me.eccentric_nz.TARDIS.mobfarming.TARDISParrot;
import me.eccentric_nz.TARDIS.travel.TARDISDoorLocation;
import me.eccentric_nz.TARDIS.utility.TARDISItemRenamer;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISNumberParsers;
import me.eccentric_nz.TARDIS.utility.TARDISSounds;
import multiworld.MultiWorldPlugin;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import multiworld.api.flag.FlagName;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author eccentric_nz
 */
public class TARDISDoorListener {

    public final TARDIS plugin;
    public float[][] adjustYaw = new float[4][4];
    Random r = new Random();

    public TARDISDoorListener(TARDIS plugin) {
        this.plugin = plugin;
        // yaw adjustments if inner and outer door directions are different
        adjustYaw[0][0] = 0;
        adjustYaw[0][1] = 90;
        adjustYaw[0][2] = 180;
        adjustYaw[0][3] = -90;
        adjustYaw[1][0] = -90;
        adjustYaw[1][1] = 0;
        adjustYaw[1][2] = 90;
        adjustYaw[1][3] = 180;
        adjustYaw[2][0] = 180;
        adjustYaw[2][1] = -90;
        adjustYaw[2][2] = 0;
        adjustYaw[2][3] = 90;
        adjustYaw[3][0] = 90;
        adjustYaw[3][1] = 180;
        adjustYaw[3][2] = -90;
        adjustYaw[3][3] = 0;
    }

    /**
     * A method to teleport the player into and out of the TARDIS.
     *
     * @param p     the player to teleport
     * @param l     the location to teleport to
     * @param exit  whether the player is entering or exiting the TARDIS, if true they are exiting
     * @param from  the world they are teleporting from
     * @param q     whether the player will receive a TARDIS quote message
     * @param sound an integer representing the sound to play
     * @param m     whether to play the resource pack sound
     */
    public void movePlayer(Player p, Location l, boolean exit, World from, boolean q, int sound, boolean m) {
        int i = r.nextInt(plugin.getGeneralKeeper().getQuotes().size());
        World to = l.getWorld();
        boolean allowFlight = p.getAllowFlight();
        boolean crossWorlds = (from != to);
        boolean quotes = q;
        boolean isSurvival = checkSurvival(to);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.teleport(l);
            playDoorSound(p, sound, l, m);
        }, 5L);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.teleport(l);
            if (p.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds && !isSurvival)) {
                p.setAllowFlight(true);
            }
            if (quotes) {
                if (r.nextInt(100) < 3 && plugin.getPM().isPluginEnabled("ProtocolLib")) {
                    TARDISUpdateChatGUI.sendJSON(plugin.getJsonKeeper().getEgg(), p);
                } else {
                    p.sendMessage(plugin.getPluginName() + plugin.getGeneralKeeper().getQuotes().get(i));
                }
            }
            if (exit) {
                plugin.getPM().callEvent(new TARDISExitEvent(p, to));
                // give some artron energy
                QueryFactory qf = new QueryFactory(plugin);
                // add energy to player
                HashMap<String, Object> where = new HashMap<>();
                UUID uuid = p.getUniqueId();
                where.put("uuid", uuid.toString());
                if (plugin.getTrackerKeeper().getHasTravelled().contains(uuid)) {
                    int player_artron = plugin.getArtronConfig().getInt("player");
                    qf.alterEnergyLevel("player_prefs", player_artron, where, p);
                    plugin.getTrackerKeeper().getHasTravelled().remove(uuid);
                }
                if (plugin.getTrackerKeeper().getSetTime().containsKey(uuid)) {
                    setTemporalLocation(p, plugin.getTrackerKeeper().getSetTime().get(uuid));
                    plugin.getTrackerKeeper().getSetTime().remove(uuid);
                }
                plugin.getTrackerKeeper().getEjecting().remove(uuid);
            } else {
                plugin.getPM().callEvent(new TARDISEnterEvent(p, from));
                if (p.isPlayerTimeRelative()) {
                    setTemporalLocation(p, -1);
                }
                TARDISSounds.playTARDISHum(p);
            }
            // give a key
            giveKey(p);
        }, 10L);
    }

    /**
     * Checks if the world the player is teleporting to is a SURVIVAL world.
     *
     * @param w the world to check
     * @return true if the world is a SURVIVAL world, otherwise false
     */
    public boolean checkSurvival(World w) {
        boolean bool = false;
        if (plugin.isMVOnServer()) {
            bool = plugin.getMVHelper().isWorldSurvival(w);
        }
        if (plugin.getPM().isPluginEnabled("MultiWorld")) {
            MultiWorldAPI mw = ((MultiWorldPlugin) plugin.getPM().getPlugin("MultiWorld")).getApi();
            MultiWorldWorldData mww = mw.getWorld(w.getName());
            if (!mww.isOptionSet(FlagName.CREATIVEWORLD)) {
                bool = true;
            }
        }
        return bool;
    }

    /**
     * A method to transport player pets (tamed mobs) into and out of the TARDIS.
     *
     * @param p      a list of the player's pets found nearby
     * @param l      the location to teleport pets to
     * @param player the player who owns the pets
     * @param d      the direction of the police box
     * @param enter  whether the pets are entering (true) or exiting (false)
     */
    public void movePets(List<TARDISParrot> p, Location l, Player player, COMPASS d, boolean enter) {
        Location pl = l.clone();
        World w = l.getWorld();
        // will need to adjust this depending on direction Police Box is facing
        if (enter) {
            pl.setZ(l.getZ() + 1);
        } else {
            switch (d) {
                case NORTH:
                    pl.setX(l.getX() + 1);
                    pl.setZ(l.getZ() + 1);
                    break;
                case WEST:
                    pl.setX(l.getX() + 1);
                    pl.setZ(l.getZ() - 1);
                    break;
                case SOUTH:
                    pl.setX(l.getX() - 1);
                    pl.setZ(l.getZ() - 1);
                    break;
                default:
                    pl.setX(l.getX() - 1);
                    pl.setZ(l.getZ() + 1);
                    break;
            }
        }
        for (TARDISParrot pet : p) {
            plugin.setTardisSpawn(true);
            LivingEntity ent;
            ent = (LivingEntity) w.spawnEntity(pl, pet.getType());
            if (ent.isDead()) {
                ent.remove();
                plugin.debug("Entity is dead! Spawning again...");
                ent = (LivingEntity) w.spawnEntity(pl, pet.getType());
            }
            String pet_name = pet.getName();
            if (pet_name != null && !pet_name.isEmpty()) {
                ent.setCustomName(pet.getName());
            }
            ent.setHealth(pet.getHealth());
            ((Tameable) ent).setTamed(true);
            ((Tameable) ent).setOwner(player);
            switch (pet.getType()) {
                case WOLF:
                    Wolf wolf = (Wolf) ent;
                    wolf.setCollarColor(pet.getColour());
                    wolf.setSitting(pet.getSitting());
                    wolf.setAge(pet.getAge());
                    if (pet.isBaby()) {
                        wolf.setBaby();
                    }
                    break;
                case OCELOT:
                    Ocelot cat = (Ocelot) ent;
                    cat.setCatType(pet.getCatType());
                    cat.setSitting(pet.getSitting());
                    cat.setAge(pet.getAge());
                    if (pet.isBaby()) {
                        cat.setBaby();
                    }
                    break;
                case PARROT:
                    Parrot parrot = (Parrot) ent;
                    parrot.setSitting(pet.getSitting());
                    parrot.setAge(pet.getAge());
                    if (pet.isBaby()) {
                        parrot.setBaby();
                    }
                    parrot.setVariant(pet.getVariant());
                    if (pet.isOnLeftShoulder() || pet.isOnRightShoulder()) {
                        HumanEntity he = player;
                        if (pet.isOnLeftShoulder()) {
                            he.setShoulderEntityLeft(parrot);
                        }
                        if (pet.isOnRightShoulder()) {
                            he.setShoulderEntityRight(parrot);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        p.clear();
    }

    /**
     * A method to give the TARDIS key to a player if the server is using a multi-inventory plugin.
     *
     * @param p the player to give the key to
     */
    private void giveKey(Player p) {
        String key;
        HashMap<String, Object> where = new HashMap<>();
        where.put("uuid", p.getUniqueId().toString());
        ResultSetPlayerPrefs rsp = new ResultSetPlayerPrefs(plugin, where);
        if (rsp.resultSet()) {
            key = (!rsp.getKey().isEmpty()) ? rsp.getKey() : plugin.getConfig().getString("preferences.key");
        } else {
            key = plugin.getConfig().getString("preferences.key");
        }
        if (plugin.getConfig().getBoolean("travel.give_key") && !key.equals("AIR")) {
            PlayerInventory inv = p.getInventory();
            Material m = Material.valueOf(key);
            ItemStack oh = inv.getItemInOffHand();
            if (!inv.contains(m) && (oh != null && !oh.getType().equals(m))) {
                ItemStack is = new ItemStack(m, 1);
                TARDISItemRenamer ir = new TARDISItemRenamer(is);
                ir.setName("TARDIS Key", true);
                inv.addItem(is);
                p.updateInventory();
                TARDISMessage.send(p, "KEY_REMIND");
            }
        }
    }

    /**
     * Adjusts the direction the player is facing after a teleport.
     *
     * @param d1 the direction the first door is facing
     * @param d2 the direction the second door is facing
     * @return the angle needed to correct the yaw
     */
    public float adjustYaw(COMPASS d1, COMPASS d2) {
        switch (d1) {
            case EAST:
                return adjustYaw[0][d2.ordinal()];
            case SOUTH:
                return adjustYaw[1][d2.ordinal()];
            case WEST:
                return adjustYaw[2][d2.ordinal()];
            default:
                return adjustYaw[3][d2.ordinal()];
        }
    }

    /**
     * Get door location data for teleport entry and exit of the TARDIS.
     *
     * @param doortype a reference to the door_type field in the doors table
     * @param id       the unique TARDIS identifier i the database
     * @return an instance of the TARDISDoorLocation data class
     */
    public TARDISDoorLocation getDoor(int doortype, int id) {
        TARDISDoorLocation tdl = new TARDISDoorLocation();
        // get door location
        HashMap<String, Object> wherei = new HashMap<>();
        wherei.put("door_type", doortype);
        wherei.put("tardis_id", id);
        ResultSetDoors rsd = new ResultSetDoors(plugin, wherei, false);
        if (rsd.resultSet()) {
            COMPASS d = rsd.getDoor_direction();
            tdl.setD(d);
            String doorLocStr = rsd.getDoor_location();
            String[] split = doorLocStr.split(":");
            World cw = plugin.getServer().getWorld(split[0]);
            tdl.setW(cw);
            int cx = TARDISNumberParsers.parseInt(split[1]);
            int cy = TARDISNumberParsers.parseInt(split[2]);
            int cz = TARDISNumberParsers.parseInt(split[3]);
            Location tmp_loc = new Location(cw, cx, cy, cz);
            int getx = tmp_loc.getBlockX();
            int getz = tmp_loc.getBlockZ();
            switch (d) {
                case NORTH:
                    // z -ve
                    tmp_loc.setX(getx + 0.5);
                    tmp_loc.setZ(getz - 0.5);
                    break;
                case EAST:
                    // x +ve
                    tmp_loc.setX(getx + 1.5);
                    tmp_loc.setZ(getz + 0.5);
                    break;
                case SOUTH:
                    // z +ve
                    tmp_loc.setX(getx + 0.5);
                    tmp_loc.setZ(getz + 1.5);
                    break;
                case WEST:
                    // x -ve
                    tmp_loc.setX(getx - 0.5);
                    tmp_loc.setZ(getz + 0.5);
                    break;
            }
            tdl.setL(tmp_loc);
        }
        return tdl;
    }

    /**
     * Plays a door sound when the iron door is clicked.
     *
     * @param p     a player to play the sound for
     * @param sound the sound to play
     * @param l     a location to play the sound at
     * @param m     whether to play the TARDIS sound or a Minecraft substitute
     */
    public void playDoorSound(Player p, int sound, Location l, boolean m) {
        switch (sound) {
            case 1:
                if (!m) {
                    TARDISSounds.playTARDISSound(l, "tardis_door_open");
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0F, 1.0F);
                }
                break;
            case 2:
                if (!m) {
                    TARDISSounds.playTARDISSound(l, "tardis_door_close");
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0F, 1.0F);
                }
                break;
            case 3:
                if (!m) {
                    TARDISSounds.playTARDISSound(l, "tardis_enter");
                } else {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                }
                break;
            default:
                break;
        }
    }

    public boolean isDoorOpen(byte door_data, COMPASS d) {
        switch (d) {
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
     * Set a player's time relative to the server time. Based on Essentials /ptime command.
     *
     * @param p the player to set the time for
     * @param t the ticks to set the time to
     */
    private void setTemporalLocation(Player p, long t) {
        if (p.isOnline()) {
            if (t != -1) {
                long time = p.getPlayerTime();
                time -= time % 24000L;
                time += 24000L + t;
                long calculatedtime = time - p.getWorld().getTime();
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    p.setPlayerTime(calculatedtime, true);
                    if (plugin.getConfig().getBoolean("allow.perception_filter")) {
                        plugin.getFilter().addPerceptionFilter(p);
                    }
                    plugin.getTrackerKeeper().getTemporallyLocated().add(p.getUniqueId());
                }, 10L);
            } else {
                p.resetPlayerTime();
                boolean remove = true;
                Material m = Material.valueOf(plugin.getRecipesConfig().getString("shaped.Perception Filter.result"));
                for (ItemStack is : p.getInventory().getArmorContents()) {
                    if (is != null && is.getType().equals(m)) {
                        remove = false;
                    }
                }
                if (remove && plugin.getTrackerKeeper().getTemporallyLocated().contains(p.getUniqueId())) {
                    if (plugin.getConfig().getBoolean("allow.perception_filter")) {
                        plugin.getFilter().removePerceptionFilter(p);
                    }
                    plugin.getTrackerKeeper().getTemporallyLocated().remove(p.getUniqueId());
                }
            }
        }
    }

    /**
     * Remove player from the travellers table.
     *
     * @param u the UUID of the player to remove
     */
    public void removeTraveller(UUID u) {
        HashMap<String, Object> where = new HashMap<>();
        where.put("uuid", u.toString());
        new QueryFactory(plugin).doSyncDelete("travellers", where);
    }
}
