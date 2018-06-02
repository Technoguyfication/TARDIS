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

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants;
import me.eccentric_nz.TARDIS.enumeration.CONSOLES;
import me.eccentric_nz.TARDIS.enumeration.DISK_CIRCUIT;
import me.eccentric_nz.TARDIS.enumeration.SCHEMATIC;
import me.eccentric_nz.TARDIS.rooms.TARDISWallsLookup;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;
import java.util.logging.Level;

/**
 * @author eccentric_nz
 */
public class TARDISCraftListener implements Listener {

    private final TARDIS plugin;
    private final HashMap<Material, String> t = new HashMap<>();
    private final TARDISWallsLookup twl;
    private final List<UUID> crafters = new ArrayList<>();
    private final List<Integer> spaces = Arrays.asList(1, 4, 7, 6, 9);

    public TARDISCraftListener(TARDIS plugin) {
        this.plugin = plugin;
        // DELUXE, ELEVENTH, TWELFTH, ARS & REDSTONE schematics designed by Lord_Rahl and killeratnight at mcnovus.net
        t.put(Material.BOOKSHELF, "PLANK"); // plank
        t.put(Material.COAL_BLOCK, "STEAMPUNK"); // steampunk
        t.put(Material.DIAMOND_BLOCK, "DELUXE"); // deluxe
        t.put(Material.EMERALD_BLOCK, "ELEVENTH"); // eleventh
        t.put(Material.GOLD_BLOCK, "BIGGER"); // bigger
        t.put(Material.IRON_BLOCK, "BUDGET"); // budget
        t.put(Material.LAPIS_BLOCK, "TOM"); // tom baker
        t.put(Material.NETHER_BRICK, "MASTER"); // master schematic designed by ShadowAssociate
        t.put(Material.NETHER_WART_BLOCK, "CORAL"); // coral schematic designed by vistaero
        t.put(Material.PRISMARINE, "TWELFTH"); // twelfth
        t.put(Material.PURPUR_BLOCK, "ENDER"); // ender schematic designed by ToppanaFIN (player at thatsnotacreeper.com)
        t.put(Material.QUARTZ_BLOCK, "ARS"); // ARS
        t.put(Material.REDSTONE_BLOCK, "REDSTONE"); // redstone
        t.put(Material.SANDSTONE_STAIRS, "PYRAMID"); // pyramid schematic designed by airomis (player at thatsnotacreeper.com)
        t.put(Material.STAINED_CLAY, "WAR"); // war doctor
        t.put(Material.CYAN_GLAZED_TERRACOTTA, "LEGACY_ELEVENTH"); // legacy_eleventh
        t.put(Material.LIME_GLAZED_TERRACOTTA, "LEGACY_DELUXE"); // legacy_deluxe
        t.put(Material.ORANGE_GLAZED_TERRACOTTA, "LEGACY_BIGGER"); // legacy_bigger
        t.put(Material.RED_GLAZED_TERRACOTTA, "LEGACY_REDSTONE"); // legacy_redstone
        t.put(Material.SILVER_GLAZED_TERRACOTTA, "LEGACY_BUDGET"); // legacy_budget
        // custom seeds
        plugin.getCustomConsolesConfig().getKeys(false).forEach((console) -> {
            if (plugin.getCustomConsolesConfig().getBoolean(console + ".enabled")) {
                if (plugin.getArtronConfig().contains("upgrades." + console.toLowerCase(Locale.ENGLISH))) {
                    Material cmat = Material.valueOf(plugin.getCustomConsolesConfig().getString(console + ".seed"));
                    t.put(cmat, console.toUpperCase(Locale.ENGLISH));
                } else {
                    plugin.getLogger().log(Level.WARNING, "The custom console {0} does not have a corresponding upgrade value in artron.", console);
                }
            }
        });
        twl = new TARDISWallsLookup(plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player p = (Player) event.getPlayer();
        UUID uuid = p.getUniqueId();
        Inventory inv = event.getInventory();
        if (crafters.contains(uuid) && inv.getType().equals(InventoryType.WORKBENCH)) {
            // remove dropped items around workbench
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                p.getNearbyEntities(6, 6, 6).forEach((e) -> {
                    if (e instanceof Item) {
                        e.remove();
                    }
                });
            }, 1L);
            crafters.remove(uuid);
        }
    }

    /**
     * Places a configured TARDIS Seed block in the crafting result slot.
     *
     * @param event the player clicking the crafting result slot.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSeedBlockCraft(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        if (inv.getType().equals(InventoryType.WORKBENCH) && slot < 10) {
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (checkSlots(inv)) {
                    if (!crafters.contains(uuid)) {
                        crafters.add(uuid);
                    }
                    if (slot == 0) {
                        event.setCancelled(true);
                    }
                    // get the materials in crafting slots
                    Material m7 = inv.getItem(7).getType(); // tardis type
                    ItemStack is = new ItemStack(m7, 1);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName("§6TARDIS Seed Block");
                    List<String> lore = new ArrayList<>();
                    lore.add(t.get(m7));
                    lore.add("Walls: " + twl.wall_lookup.get(inv.getItem(6).getType().toString() + ":" + inv.getItem(6).getData().getData()));
                    lore.add("Floors: " + twl.wall_lookup.get(inv.getItem(9).getType().toString() + ":" + inv.getItem(9).getData().getData()));
                    im.setLore(lore);
                    is.setItemMeta(im);
                    if (checkPerms(player, m7)) {
                        TARDISMessage.send(player, "SEED_VALID");
                        inv.setItem(0, is);
                        player.updateInventory();
                        if (slot == 0) {
                            event.setCancelled(true);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                // clear the other slots
                                for (int i = 1; i < 10; i++) {
                                    inv.setItem(i, null);
                                }
                                if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                                    player.setItemOnCursor(is);
                                    crafters.remove(uuid);
                                }
                            }, 2L);
                        }
                    } else {
                        TARDISMessage.send(player, "NO_PERMS");
                    }
                }
            }, 2L);
        }
    }

    /**
     * Checks the craft inventory slots contain the correct materials to craft a TARDIS Seed block.
     *
     * @param inv
     * @return whether it is a valid seed block
     */
    private boolean checkSlots(Inventory inv) {
        for (int s : spaces) {
            ItemStack is = inv.getItem(s);
            if (is == null) {
                return false;
            }
            Material m = is.getType();
            switch (s) {
                case 1:
                    if (!m.equals(Material.REDSTONE_TORCH_ON)) {
                        return false;
                    }
                    break;
                case 4:
                    // must be lapis block
                    if (!m.equals(Material.LAPIS_BLOCK)) {
                        return false;
                    }
                    break;
                case 7:
                    // must be a TARDIS block
                    if (!t.containsKey(m)) {
                        return false;
                    }
                    break;
                default: // 6, 9
                    // must be a valid wall / floor block
                    if (!twl.wall_lookup.containsKey(m.toString() + ":" + is.getData().getData())) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private boolean checkPerms(Player p, Material m) {
        SCHEMATIC schm = CONSOLES.getBY_MATERIALS().get(m.toString());
        if (schm != null) {
            String perm = schm.getPermission();
            return (perm.equals("budget")) ? true : p.hasPermission("tardis." + perm);
        } else {
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftTARDISItem(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe != null) {
            ItemStack is = recipe.getResult();
            if (is.getType().equals(Material.AIR)) {
                CraftingInventory ci = event.getInventory();
                // get first map
                int slot = ci.first(Material.MAP);
                if (slot != -1) {
                    ItemStack map = ci.getItem(slot);
                    if (map.hasItemMeta() && map.getItemMeta().hasDisplayName() && TARDISConstants.CIRCUITS.contains(map.getItemMeta().getDisplayName())) {
                        // disallow cloning
                        if (ci.first(Material.EMPTY_MAP) != -1) {
                            ci.setResult(null);
                            return;
                        }
                    }
                }
            }
            if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
                String dn = is.getItemMeta().getDisplayName();
                CraftingInventory ci = event.getInventory();
                if (is.getType().equals(Material.MAP)) {
                    if (DISK_CIRCUIT.getCircuitNames().contains(dn)) {
                        // which circuit is it?
                        String[] split = dn.split(" ");
                        String which = split[1].toLowerCase(Locale.ENGLISH);
                        // set the second line of lore
                        ItemMeta im = is.getItemMeta();
                        List<String> lore;
                        String uses = (plugin.getConfig().getString("circuits.uses." + which).equals("0") || !plugin.getConfig().getBoolean("circuits.damage")) ? ChatColor.YELLOW + "unlimited" : ChatColor.YELLOW + plugin.getConfig().getString("circuits.uses." + which);
                        if (im.hasLore()) {
                            lore = im.getLore();
                            lore.set(1, uses);
                        } else {
                            lore = Arrays.asList("Uses left", uses);
                        }
                        im.setLore(lore);
                        is.setItemMeta(im);
                        ci.setResult(is);
                    }
                } else if (is.getType().equals(Material.NETHER_BRICK_ITEM) && dn.equals("Acid Battery")) {
                    for (int i = 2; i < 9; i += 2) {
                        ItemStack water = ci.getItem(i);
                        if (!water.hasItemMeta() || !water.getItemMeta().hasDisplayName() || !water.getItemMeta().getDisplayName().equals("Acid Bucket")) {
                            ci.setResult(null);
                            break;
                        }
                    }
                } else if (is.getType().equals(Material.BEACON) && dn.equals("Rift Manipulator")) {
                    for (int i = 2; i < 9; i += 2) {
                        ItemStack acid = ci.getItem(i);
                        if (!acid.hasItemMeta() || !acid.getItemMeta().hasDisplayName() || !acid.getItemMeta().getDisplayName().equals("Acid Battery")) {
                            ci.setResult(null);
                            break;
                        }
                    }
                } else if (is.getType().equals(Material.IRON_SWORD) && dn.equals("Rust Plague Sword")) {
                    // enchant the result
                    is.addEnchantment(Enchantment.DAMAGE_UNDEAD, 2);
                    ci.setResult(is);
                    List<Integer> slots = Arrays.asList(1, 3, 4, 6);
                    for (int i : slots) {
                        ItemStack rust = ci.getItem(i);
                        if (!rust.hasItemMeta() || !rust.getItemMeta().hasDisplayName() || !rust.getItemMeta().getDisplayName().equals("Rust Bucket")) {
                            ci.setResult(null);
                            break;
                        }
                    }
                } else if (is.getType().equals(Material.LEATHER_HELMET) && (dn.equals("3-D Glasses") || dn.equals("TARDIS Communicator"))) {
                    LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
                    lam.setColor(Color.WHITE);
                    is.setItemMeta(lam);
                    ci.setResult(is);
                }
            }
        }
    }
}
