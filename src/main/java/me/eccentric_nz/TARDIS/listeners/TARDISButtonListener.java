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
package me.eccentric_nz.TARDIS.listeners;

import me.eccentric_nz.TARDIS.ARS.TARDISARSInventory;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.advanced.TARDISCircuitChecker;
import me.eccentric_nz.TARDIS.advanced.TARDISSerializeInventory;
import me.eccentric_nz.TARDIS.api.event.TARDISZeroRoomEnterEvent;
import me.eccentric_nz.TARDIS.api.event.TARDISZeroRoomExitEvent;
import me.eccentric_nz.TARDIS.control.*;
import me.eccentric_nz.TARDIS.database.*;
import me.eccentric_nz.TARDIS.database.data.Tardis;
import me.eccentric_nz.TARDIS.enumeration.COMPASS;
import me.eccentric_nz.TARDIS.enumeration.DIFFICULTY;
import me.eccentric_nz.TARDIS.enumeration.PRESET;
import me.eccentric_nz.TARDIS.enumeration.STORAGE;
import me.eccentric_nz.TARDIS.handles.TARDISHandlesProcessor;
import me.eccentric_nz.TARDIS.handles.TARDISHandlesProgramInventory;
import me.eccentric_nz.TARDIS.move.TARDISBlackWoolToggler;
import me.eccentric_nz.TARDIS.rooms.TARDISExteriorRenderer;
import me.eccentric_nz.TARDIS.travel.TARDISTemporalLocatorInventory;
import me.eccentric_nz.TARDIS.travel.TARDISTerminalInventory;
import me.eccentric_nz.TARDIS.utility.TARDISLocationGetters;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISNumberParsers;
import me.eccentric_nz.TARDIS.utility.TARDISSounds;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

/**
 * The various systems of the console room are fairly well-understood. According to one account, each of the six panels
 * controls a discrete function. The navigation panel contains a time and space forward/back control, directional
 * pointer, atom accelerator and the spatial location input.
 *
 * @author eccentric_nz
 */
public class TARDISButtonListener implements Listener {

    private final TARDIS plugin;
    private final List<Material> validBlocks = new ArrayList<>();
    private final List<Integer> onlythese = Arrays.asList(1, 8, 9, 10, 11, 12, 13, 14, 16, 17, 20, 21, 22, 26);
    private final List<Integer> allow_unpowered = Arrays.asList(13, 17, 22);
    private final List<Integer> no_siege = Arrays.asList(0, 10, 12, 16, 19, 20);

    public TARDISButtonListener(TARDIS plugin) {
        this.plugin = plugin;
        validBlocks.add(Material.WOOD_BUTTON);
        validBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        validBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        validBlocks.add(Material.STONE_BUTTON);
        validBlocks.add(Material.LEVER);
        validBlocks.add(Material.WALL_SIGN);
        validBlocks.add(Material.NOTE_BLOCK);
        validBlocks.add(Material.JUKEBOX);
        validBlocks.add(Material.STONE_PLATE);
        validBlocks.add(Material.WOOD_PLATE);
        validBlocks.add(Material.GRAY_GLAZED_TERRACOTTA);
    }

    /**
     * Listens for player interaction with the TARDIS console button. If the button is clicked it will return a random
     * destination based on the settings of the four TARDIS console repeaters.
     *
     * @param event the player clicking a block
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onButtonInteract(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null) {
            Material blockType = block.getType();
            Action action = event.getAction();
            // only proceed if they are clicking a type of a button or a lever!
            if (validBlocks.contains(blockType)) {
                // get clicked block location
                String buttonloc = block.getLocation().toString();
                // get tardis from saved button location
                HashMap<String, Object> where = new HashMap<>();
                where.put("location", buttonloc);
                ResultSetControls rsc = new ResultSetControls(plugin, where, false);
                if (rsc.resultSet()) {
                    int id = rsc.getTardis_id();
                    int type = rsc.getType();
                    if (plugin.getTrackerKeeper().getJohnSmith().containsKey(player.getUniqueId()) && type != 13) {
                        TARDISMessage.send(player, "ISO_HANDS_OFF");
                        return;
                    }
                    if (!onlythese.contains(type)) {
                        return;
                    }
                    HashMap<String, Object> whereid = new HashMap<>();
                    whereid.put("tardis_id", id);
                    ResultSetTardis rs = new ResultSetTardis(plugin, whereid, "", false, 0);
                    if (rs.resultSet()) {
                        Tardis tardis = rs.getTardis();
                        if (tardis.getPreset().equals(PRESET.JUNK)) {
                            return;
                        }
                        // check they initialised
                        if (!tardis.isTardis_init()) {
                            TARDISMessage.send(player, "ENERGY_NO_INIT");
                            return;
                        }
                        if (plugin.getConfig().getBoolean("allow.power_down") && !tardis.isPowered_on() && !allow_unpowered.contains(type)) {
                            TARDISMessage.send(player, "POWER_DOWN");
                            return;
                        }
                        if (plugin.getTrackerKeeper().getInSiegeMode().contains(id) && no_siege.contains(type)) {
                            TARDISMessage.send(player, "SIEGE_NO_CONTROL");
                            return;
                        }
                        boolean lights = tardis.isLights_on();
                        if (!lights && type == 12 && plugin.getConfig().getBoolean("allow.power_down") && !tardis.isPowered_on()) {
                            TARDISMessage.send(player, "POWER_DOWN");
                            return;
                        }
                        int level = tardis.getArtron_level();
                        boolean hb = tardis.isHandbrake_on();
                        UUID ownerUUID = tardis.getUuid();
                        TARDISCircuitChecker tcc = null;
                        if (!plugin.getDifficulty().equals(DIFFICULTY.EASY)) {
                            tcc = new TARDISCircuitChecker(plugin, id);
                            tcc.getCircuits();
                        }
                        QueryFactory qf = new QueryFactory(plugin);
                        if (action == Action.RIGHT_CLICK_BLOCK) {
                            switch (type) {
                                case 1: // random location button
                                    if (plugin.getTrackerKeeper().getMaterialising().contains(id) || plugin.getTrackerKeeper().getDematerialising().contains(id) || (!hb && !plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) || plugin.getTrackerKeeper().getHasRandomised().contains(id)) {
                                        TARDISMessage.send(player, "NOT_WHILE_TRAVELLING");
                                        return;
                                    }
                                    if (plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) {
                                        plugin.getTrackerKeeper().getHasRandomised().add(id);
                                    }
                                    new TARDISRandomButton(plugin, player, id, level, 0, tardis.getCompanions(), tardis.getUuid()).clickButton();
                                    break;
                                case 8: // fast return button
                                    if (plugin.getTrackerKeeper().getMaterialising().contains(id) || plugin.getTrackerKeeper().getDematerialising().contains(id) || (!hb && !plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) || plugin.getTrackerKeeper().getHasRandomised().contains(id)) {
                                        TARDISMessage.send(player, "NOT_WHILE_TRAVELLING");
                                        return;
                                    }
                                    if (plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) {
                                        plugin.getTrackerKeeper().getHasRandomised().add(id);
                                    }
                                    new TARDISFastReturnButton(plugin, player, id, level).clickButton();
                                    break;
                                case 9: // terminal sign
                                    if (plugin.getTrackerKeeper().getMaterialising().contains(id) || plugin.getTrackerKeeper().getDematerialising().contains(id) || (!hb && !plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) || plugin.getTrackerKeeper().getHasRandomised().contains(id)) {
                                        TARDISMessage.send(player, "NOT_WHILE_TRAVELLING");
                                        return;
                                    }
                                    if (plugin.getTrackerKeeper().getDestinationVortex().containsKey(id)) {
                                        plugin.getTrackerKeeper().getHasRandomised().add(id);
                                    }
                                    if (level < plugin.getArtronConfig().getInt("travel")) {
                                        TARDISMessage.send(player, "NOT_ENOUGH_ENERGY");
                                        return;
                                    }
                                    if (tcc != null && !tcc.hasInput() && !plugin.getUtils().inGracePeriod(player, false)) {
                                        TARDISMessage.send(player, "INPUT_MISSING");
                                        return;
                                    }
                                    ItemStack[] items = new TARDISTerminalInventory(plugin).getTerminal();
                                    Inventory aec = plugin.getServer().createInventory(player, 54, "§4Destination Terminal");
                                    aec.setContents(items);
                                    player.openInventory(aec);
                                    break;
                                case 10: // ARS sign
                                    if (!hb) {
                                        TARDISMessage.send(player, "ARS_NO_TRAVEL");
                                        return;
                                    }
                                    // check they're in a compatible world
                                    if (!plugin.getUtils().canGrowRooms(tardis.getChunk())) {
                                        TARDISMessage.send(player, "ROOM_OWN_WORLD");
                                        return;
                                    }
                                    if (player.isSneaking()) {
                                        // check they have permission to change the desktop
                                        if (!player.hasPermission("tardis.upgrade")) {
                                            TARDISMessage.send(player, "NO_PERM_UPGRADE");
                                            return;
                                        }
                                        if (tcc != null && !tcc.hasARS() && !plugin.getUtils().inGracePeriod(player, true)) {
                                            TARDISMessage.send(player, "ARS_MISSING");
                                            return;
                                        }
                                        // upgrade menu
                                        new TARDISThemeButton(plugin, player, tardis.getSchematic(), level, id).clickButton();
                                    } else {
                                        // check they have permission to grow rooms
                                        if (!player.hasPermission("tardis.architectural")) {
                                            TARDISMessage.send(player, "NO_PERM_ROOMS");
                                            return;
                                        }
                                        if (tcc != null && !tcc.hasARS() && !plugin.getUtils().inGracePeriod(player, true)) {
                                            TARDISMessage.send(player, "ARS_MISSING");
                                            return;
                                        }
                                        ItemStack[] tars = new TARDISARSInventory(plugin).getARS();
                                        Inventory ars = plugin.getServer().createInventory(player, 54, "§4Architectural Reconfiguration");
                                        ars.setContents(tars);
                                        player.openInventory(ars);
                                    }
                                    break;
                                case 11: // Temporal Locator sign
                                    if (!player.hasPermission("tardis.temporal")) {
                                        TARDISMessage.send(player, "NO_PERM_TEMPORAL");
                                        return;
                                    }
                                    if (tcc != null && !tcc.hasTemporal() && !plugin.getUtils().inGracePeriod(player, false)) {
                                        TARDISMessage.send(player, "TEMP_MISSING");
                                        return;
                                    }
                                    ItemStack[] clocks = new TARDISTemporalLocatorInventory(plugin).getTemporal();
                                    Inventory tmpl = plugin.getServer().createInventory(player, 27, "§4Temporal Locator");
                                    tmpl.setContents(clocks);
                                    player.openInventory(tmpl);
                                    break;
                                case 12: // Control room light switch
                                    new TARDISLightSwitch(plugin, id, lights, player, tardis.getSchematic().hasLanterns()).flickSwitch();
                                    break;
                                case 13: // TIS
                                    new TARDISInfoMenuButton(plugin, player).clickButton();
                                    break;
                                case 14: // Disk Storage
                                    UUID playerUUID = player.getUniqueId();
                                    // only the time lord of this tardis
                                    if (!ownerUUID.equals(playerUUID)) {
                                        TARDISMessage.send(player, "NOT_OWNER");
                                        return;
                                    }
                                    // do they have a storage record?
                                    HashMap<String, Object> wherestore = new HashMap<>();
                                    wherestore.put("uuid", playerUUID);
                                    ResultSetDiskStorage rsstore = new ResultSetDiskStorage(plugin, wherestore);
                                    ItemStack[] stack = new ItemStack[54];
                                    if (rsstore.resultSet()) {
                                        try {
                                            if (!rsstore.getSavesOne().isEmpty()) {
                                                stack = TARDISSerializeInventory.itemStacksFromString(rsstore.getSavesOne());
                                            } else {
                                                stack = TARDISSerializeInventory.itemStacksFromString(STORAGE.SAVE_1.getEmpty());
                                            }
                                        } catch (IOException ex) {
                                            plugin.debug("Could not get Storage Inventory: " + ex.getMessage());
                                        }
                                    } else {
                                        try {
                                            stack = TARDISSerializeInventory.itemStacksFromString(STORAGE.SAVE_1.getEmpty());
                                        } catch (IOException ex) {
                                            plugin.debug("Could not get default Storage Inventory: " + ex.getMessage());
                                        }
                                        // make a record
                                        HashMap<String, Object> setstore = new HashMap<>();
                                        setstore.put("uuid", player.getUniqueId().toString());
                                        setstore.put("tardis_id", id);
                                        qf.doInsert("storage", setstore);
                                    }
                                    Inventory inv = plugin.getServer().createInventory(player, 54, STORAGE.SAVE_1.getTitle());
                                    inv.setContents(stack);
                                    player.openInventory(inv);
                                    break;
                                case 16: // enter zero room
                                    doZero(level, player, tardis.getZero(), id, qf);
                                    break;
                                case 17:
                                    // exit zero room
                                    plugin.getTrackerKeeper().getZeroRoomOccupants().remove(player.getUniqueId());
                                    plugin.getGeneralKeeper().getRendererListener().transmat(player);
                                    plugin.getPM().callEvent(new TARDISZeroRoomExitEvent(player, id));
                                    break;
                                case 20:
                                    // toggle black wool blocks behind door
                                    new TARDISBlackWoolToggler(plugin).toggleBlocks(id, player);
                                    break;
                                case 21:
                                    // siege lever
                                    if (tcc != null && !tcc.hasMaterialisation()) {
                                        TARDISMessage.send(player, "NO_MAT_CIRCUIT");
                                        return;
                                    }
                                    new TARDISSiegeButton(plugin, player, tardis.isPowered_on(), id).clickButton();
                                    break;
                                case 22:
                                    if (player.isSneaking()) {
                                        // keyboard
                                        if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
                                            if (!plugin.getDifficulty().equals(DIFFICULTY.EASY) && !plugin.getUtils().inGracePeriod(player, false)) {
                                                tcc = new TARDISCircuitChecker(plugin, id);
                                                tcc.getCircuits();
                                            }
                                            if (tcc != null && !tcc.hasInput()) {
                                                TARDISMessage.send(player, "INPUT_MISSING");
                                                return;
                                            }
                                            Sign sign = (Sign) block.getState();
                                            sign.setLine(0, "");
                                            sign.setLine(1, "");
                                            sign.setLine(2, "");
                                            sign.setLine(3, "");
                                            sign.update();
                                            plugin.getTrackerKeeper().getSign().put(buttonloc, sign);
                                            plugin.getTrackerKeeper().getKeyboard().add(id);
                                            TARDISKeyboardPacketListener.displaySignEditor(player, block);
                                        }
                                    } else {
                                        // controls GUI
                                        ItemStack[] controls = new TARDISControlInventory(plugin, player.getUniqueId()).getControls();
                                        Inventory cgui = plugin.getServer().createInventory(player, 54, "§4TARDIS Control Menu");
                                        cgui.setContents(controls);
                                        player.openInventory(cgui);
                                    }
                                    break;
                                case 26:
                                    // Handles
                                    if (!player.hasPermission("tardis.handles.use")) {
                                        TARDISMessage.send(player, "NO_PERM");
                                        return;
                                    }
                                    TARDISSounds.playTARDISSound(player, "Handles");
                                    if (!player.hasPermission("tardis.handles.program")) {
                                        TARDISMessage.send(player, "NO_PERM");
                                        return;
                                    }
                                    if (player.isSneaking()) {
                                        // open programming GUI
                                        ItemStack[] handles = new TARDISHandlesProgramInventory(plugin, 0).getHandles();
                                        Inventory hgui = plugin.getServer().createInventory(player, 54, "§4Handles Program");
                                        hgui.setContents(handles);
                                        player.openInventory(hgui);
                                    } else {
                                        // check if item in hand is a Handles program disk
                                        ItemStack disk = player.getInventory().getItemInMainHand();
                                        if (disk != null && disk.getType().equals(Material.RECORD_10) && disk.hasItemMeta()) {
                                            ItemMeta dim = disk.getItemMeta();
                                            if (dim.hasDisplayName() && ChatColor.stripColor(dim.getDisplayName()).equals("Handles Program Disk")) {
                                                // get the program_id from the disk
                                                int pid = TARDISNumberParsers.parseInt(dim.getLore().get(1));
                                                // query the database
                                                ResultSetProgram rsp = new ResultSetProgram(plugin, pid);
                                                if (rsp.resultSet()) {
                                                    // send program to processor
                                                    new TARDISHandlesProcessor(plugin).processDisk(rsp.getProgram());
                                                    // check in the disk
                                                    HashMap<String, Object> set = new HashMap<>();
                                                    set.put("checked", 0);
                                                    HashMap<String, Object> wherep = new HashMap<>();
                                                    wherep.put("program_id", pid);
                                                    new QueryFactory(plugin).doUpdate("programs", set, wherep);
                                                    player.getInventory().setItemInMainHand(null);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (action.equals(Action.PHYSICAL) && type == 16) {
                            doZero(level, player, tardis.getZero(), id, qf);
                        }
                    }
                }
            }
        }
    }

    private void doZero(int level, Player player, String z, int id, QueryFactory qf) {
        int zero_amount = plugin.getArtronConfig().getInt("zero");
        if (level < zero_amount) {
            TARDISMessage.send(player, "NOT_ENOUGH_ZERO_ENERGY");
            return;
        }
        Location zero = TARDISLocationGetters.getLocationFromDB(z, 0.0F, 0.0F);
        if (zero != null) {
            TARDISMessage.send(player, "ZERO_READY");
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                new TARDISExteriorRenderer(plugin).transmat(player, COMPASS.SOUTH, zero);
                plugin.getPM().callEvent(new TARDISZeroRoomEnterEvent(player, id));
            }, 20L);
            plugin.getTrackerKeeper().getZeroRoomOccupants().add(player.getUniqueId());
            HashMap<String, Object> wherez = new HashMap<>();
            wherez.put("tardis_id", id);
            qf.alterEnergyLevel("tardis", -zero_amount, wherez, player);
        } else {
            TARDISMessage.send(player, "NO_ZERO");
        }
    }
}
