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
package me.eccentric_nz.TARDIS.commands;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.enumeration.INVENTORY_MANAGER;
import me.eccentric_nz.TARDIS.enumeration.MAP;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * A Time Control Unit is a golden sphere about the size of a Cricket ball. It is stored in the Secondary Control Room.
 * All TARDISes have one of these devices, which can be used to remotely control a TARDIS by broadcasting Stattenheim
 * signals that travel along the time contours in the Space/Time Vortex.
 *
 * @author eccentric_nz
 */
public class TARDISRecipeCommands implements CommandExecutor {

    private final TARDIS plugin;
    private final HashMap<String, String> recipeItems = new HashMap<>();
    private final HashMap<String, Material> t = new HashMap<>();

    public TARDISRecipeCommands(TARDIS plugin) {
        this.plugin = plugin;
        recipeItems.put("a-circuit", "Server Admin Circuit");
        recipeItems.put("acid-battery", "Acid Battery");
        recipeItems.put("ars-circuit", "TARDIS ARS Circuit");
        recipeItems.put("bio-circuit", "Bio-scanner Circuit");
        recipeItems.put("biome-disk", "Biome Storage Disk");
        recipeItems.put("blank", "Blank Storage Disk");
        recipeItems.put("battery", "Blaster Battery");
        recipeItems.put("blaster", "Sonic Blaster");
        recipeItems.put("bow-tie", "Red Bow Tie");
        recipeItems.put("c-circuit", "TARDIS Chameleon Circuit");
        recipeItems.put("cell", "Artron Energy Cell");
        recipeItems.put("communicator", "TARDIS Communicator");
        recipeItems.put("custard", "Bowl of Custard");
        recipeItems.put("d-circuit", "Diamond Disruptor Circuit");
        recipeItems.put("e-circuit", "Emerald Environment Circuit");
        recipeItems.put("filter", "Perception Filter");
        recipeItems.put("fish-finger", "Fish Finger");
        recipeItems.put("furnace", "TARDIS Artron Furnace");
        recipeItems.put("generator", "Sonic Generator");
        recipeItems.put("glasses", "3-D Glasses");
        recipeItems.put("handles", "Handles");
        recipeItems.put("i-circuit", "TARDIS Input Circuit");
        recipeItems.put("ignite-circuit", "Ignite Circuit");
        recipeItems.put("invisible", "TARDIS Invisibility Circuit");
        recipeItems.put("key", "TARDIS Key");
        recipeItems.put("jammy-dodger", "Jammy Dodger");
        recipeItems.put("jelly-baby", "Orange Jelly Baby");
        recipeItems.put("l-circuit", "TARDIS Locator Circuit");
        recipeItems.put("locator", "TARDIS Locator");
        recipeItems.put("m-circuit", "TARDIS Materialisation Circuit");
        recipeItems.put("memory-circuit", "TARDIS Memory Circuit");
        recipeItems.put("oscillator", "Sonic Oscillator");
        recipeItems.put("pad", "Landing Pad");
        recipeItems.put("painter", "Painter Circuit");
        recipeItems.put("p-circuit", "Perception Circuit");
        recipeItems.put("player-disk", "Player Storage Disk");
        recipeItems.put("preset-disk", "Preset Storage Disk");
        recipeItems.put("r-circuit", "Redstone Activator Circuit");
        recipeItems.put("r-key", "TARDIS Remote Key");
        recipeItems.put("randomiser-circuit", "TARDIS Randomiser Circuit");
        recipeItems.put("reader", "TARDIS Biome Reader");
        recipeItems.put("remote", "Stattenheim Remote");
        recipeItems.put("rift-circuit", "Rift Circuit");
        recipeItems.put("rift-manipulator", "Rift Manipulator");
        recipeItems.put("rust", "Rust Plague Sword");
        recipeItems.put("s-circuit", "TARDIS Stattenheim Circuit");
        recipeItems.put("save-disk", "Save Storage Disk");
        recipeItems.put("scanner-circuit", "TARDIS Scanner Circuit");
        recipeItems.put("sonic", "Sonic Screwdriver");
        recipeItems.put("t-circuit", "TARDIS Temporal Circuit");
        recipeItems.put("tardis", "");
        recipeItems.put("telepathic", "TARDIS Telepathic Circuit");
        recipeItems.put("vortex", "Vortex Manipulator");
        recipeItems.put("watch", "Fob Watch");
        // DELUXE, ELEVENTH, TWELFTH, ARS & REDSTONE schematics designed by Lord_Rahl and killeratnight at mcnovus.net
        t.put("ARS", Material.QUARTZ_BLOCK); // ARS
        t.put("BIGGER", Material.GOLD_BLOCK); // bigger
        t.put("BUDGET", Material.IRON_BLOCK); // budget
        t.put("CORAL", Material.NETHER_WART_BLOCK); // coral schematic designed by vistaero
        t.put("DELUXE", Material.DIAMOND_BLOCK); // deluxe
        t.put("ELEVENTH", Material.EMERALD_BLOCK); // eleventh
        t.put("ENDER", Material.PURPUR_BLOCK); // ender schematic designed by ToppanaFIN (player at thatsnotacreeper.com)
        t.put("PLANK", Material.BOOKSHELF); // plank
        t.put("REDSTONE", Material.REDSTONE_BLOCK); // redstone
        t.put("STEAMPUNK", Material.COAL_BLOCK); // steampunk
        t.put("TOM", Material.LAPIS_BLOCK); // tom baker
        t.put("TWELFTH", Material.PRISMARINE); // twelfth
        t.put("WAR", Material.STAINED_CLAY); // war doctor
        t.put("PYRAMID", Material.SANDSTONE_STAIRS); // pyramid schematic designed by airomis (player at thatsnotacreeper.com)
        t.put("MASTER", Material.NETHER_BRICK); // master schematic designed by ShadowAssociate
        t.put("LEGACY_BIGGER", Material.ORANGE_GLAZED_TERRACOTTA);
        t.put("LEGACY_BUDGET", Material.SILVER_GLAZED_TERRACOTTA);
        t.put("LEGACY_DELUXE", Material.LIME_GLAZED_TERRACOTTA);
        t.put("LEGACY_ELEVENTH", Material.CYAN_GLAZED_TERRACOTTA);
        t.put("LEGACY_REDSTONE", Material.RED_GLAZED_TERRACOTTA);
        // custom seeds
        plugin.getCustomConsolesConfig().getKeys(false).forEach((console) -> {
            if (plugin.getCustomConsolesConfig().getBoolean(console + ".enabled")) {
                Material cmat = Material.valueOf(plugin.getCustomConsolesConfig().getString(console + ".seed"));
                t.put(console.toUpperCase(Locale.ENGLISH), cmat);
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tardisrecipe")) {
            if (!sender.hasPermission("tardis.help")) {
                TARDISMessage.send(sender, "NO_PERMS");
                return false;
            }
            Player player = null;
            if (player == null) {
                TARDISMessage.send(sender, "CMD_PLAYER");
                return true;
            }
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            if (args.length == 0 || !recipeItems.containsKey(args[0].toLowerCase(Locale.ENGLISH))) {
                new TARDISRecipeLister(plugin, sender).list();
                return true;
            }
            if (args.length < 1) {
                TARDISMessage.send(player, "TOO_FEW_ARGS");
                return false;
            }
            if (args[0].equalsIgnoreCase("tardis") && args.length < 2) {
                TARDISMessage.send(player, "TOO_FEW_ARGS");
                return true;
            }
            if (args[0].equalsIgnoreCase("tardis") && args.length == 2) {
                if (!t.containsKey(args[1].toUpperCase(Locale.ENGLISH))) {
                    TARDISMessage.send(player, "ARG_NOT_VALID");
                    return true;
                }
                showTARDISRecipe(player, args[1]);
                return true;
            }
            String which = args[0].toLowerCase();
            switch (which) {
                case "a-circuit":
                case "acid-battery":
                case "ars-circuit":
                case "bio-circuit":
                case "blank":
                case "bow-tie":
                case "c-circuit":
                case "cell":
                case "communicator":
                case "d-circuit":
                case "e-circuit":
                case "filter":
                case "fish-finger":
                case "furnace":
                case "generator":
                case "glasses":
                case "handles":
                case "i-circuit":
                case "ignite-circuit":
                case "invisible":
                case "key":
                case "jammy-dodger":
                case "l-circuit":
                case "locator":
                case "m-circuit":
                case "memory-circuit":
                case "oscillator":
                case "p-circuit":
                case "painter":
                case "r-circuit":
                case "r-key":
                case "randomiser-circuit":
                case "reader":
                case "remote":
                case "rift-circuit":
                case "rift-manipulator":
                case "rust":
                case "s-circuit":
                case "scanner-circuit":
                case "sonic":
                case "t-circuit":
                case "telepathic":
                case "watch":
                    showShapedRecipe(player, recipeItems.get(which));
                    return true;
                case "vortex":
                    if (!plugin.getPM().isPluginEnabled("TARDISVortexManipulator")) {
                        TARDISMessage.send(sender, "RECIPE_VORTEX");
                        return true;
                    }
                    showShapedRecipe(player, "Vortex Manipulator");
                    return true;
                case "battery":
                case "blaster":
                case "pad":
                    if (!plugin.getPM().isPluginEnabled("TARDISSonicBlaster")) {
                        TARDISMessage.send(sender, "RECIPE_BLASTER");
                        return true;
                    }
                    showShapedRecipe(player, recipeItems.get(which));
                    return true;
                case "biome-disk":
                case "custard":
                case "jelly-baby":
                case "player-disk":
                case "preset-disk":
                case "save-disk":
                    showShapelessRecipe(player, recipeItems.get(which));
                    return true;
            }
        }
        return false;
    }

    public void showShapedRecipe(Player p, String str) {
        ShapedRecipe recipe = plugin.getFigura().getShapedRecipes().get(str);
        p.closeInventory();
        plugin.getTrackerKeeper().getRecipeView().add(p.getUniqueId());
        Inventory inv = plugin.getServer().createInventory(p, 27, "§4" + str + " recipe");
        String[] recipeShape = recipe.getShape();
        Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();
        for (int j = 0; j < recipeShape.length; j++) {
            for (int k = 0; k < recipeShape[j].length(); k++) {
                ItemStack item = ingredientMap.get(recipeShape[j].toCharArray()[k]);
                if (item == null) {
                    continue;
                }
                if (item.getType().equals(Material.MAP)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName(getDisplayName(item.getData().getData()));
                    item.setItemMeta(im);
                }
                if (str.equals("TARDIS Remote Key") && item.getType().equals(Material.GOLD_NUGGET)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName("TARDIS Key");
                    item.setItemMeta(im);
                }
                if (str.equals("Sonic Blaster") && item.getType().equals(Material.BUCKET)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName("Blaster Battery");
                    item.setItemMeta(im);
                }
                if (str.equals("Acid Battery") && item.getType().equals(Material.WATER_BUCKET)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName("Acid Bucket");
                    item.setItemMeta(im);
                }
                if (str.equals("Rift Manipulator") && item.getType().equals(Material.NETHER_BRICK_ITEM)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName("Acid Battery");
                    item.setItemMeta(im);
                }
                if (str.equals("Rift Manipulator") && item.getType().equals(Material.MAP)) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName("Rift Circuit");
                    item.setItemMeta(im);
                }
                inv.setItem(j * 9 + k, item);
            }
        }
        ItemStack result = recipe.getResult();
        ItemMeta im = result.getItemMeta();
        im.setDisplayName(str);
        if (str.equals("TARDIS Invisibility Circuit")) {
            // set the second line of lore
            List<String> lore = im.getLore();
            String uses = (plugin.getConfig().getString("circuits.uses.invisibility").equals("0") || !plugin.getConfig().getBoolean("circuits.damage")) ? ChatColor.YELLOW + "unlimited" : ChatColor.YELLOW + plugin.getConfig().getString("circuits.uses.invisibility");
            lore.set(1, uses);
            im.setLore(lore);
        }
        if ((str.equals("Save Storage Disk") || str.equals("Preset Storage Disk") || str.equals("Biome Storage Disk") || str.equals("Player Storage Disk") || str.equals("Sonic Blaster")) && !plugin.getInvManager().equals(INVENTORY_MANAGER.MULTIVERSE)) {
            im.addItemFlags(ItemFlag.values());
        }
        result.setAmount(1);
        result.setItemMeta(im);
        inv.setItem(17, result);
        p.openInventory(inv);
    }

    public void showShapelessRecipe(Player player, String str) {
        ShapelessRecipe recipe = plugin.getIncomposita().getShapelessRecipes().get(str);
        List<ItemStack> ingredients = recipe.getIngredientList();
        plugin.getTrackerKeeper().getRecipeView().add(player.getUniqueId());
        Inventory inv = plugin.getServer().createInventory(player, 27, "§4" + str + " recipe");
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).getType().equals(Material.MAP)) {
                ItemMeta im = ingredients.get(i).getItemMeta();
                im.setDisplayName(getDisplayName(ingredients.get(i).getData().getData()));
                ingredients.get(i).setItemMeta(im);
            }
            if (ingredients.get(i).getType().equals(Material.RECORD_9)) {
                ItemMeta im = ingredients.get(i).getItemMeta();
                im.setDisplayName("Blank Storage Disk");
                ingredients.get(i).setItemMeta(im);
            }
            inv.setItem(i * 9, ingredients.get(i));
        }
        ItemStack result = recipe.getResult();
        ItemMeta im = result.getItemMeta();
        im.setDisplayName(str);
        if ((str.equals("Save Storage Disk") || str.equals("Preset Storage Disk") || str.equals("Biome Storage Disk") || str.equals("Player Storage Disk") || str.equals("Sonic Blaster")) && !plugin.getInvManager().equals(INVENTORY_MANAGER.MULTIVERSE)) {
            im.addItemFlags(ItemFlag.values());
        }
        result.setAmount(1);
        result.setItemMeta(im);
        inv.setItem(17, result);
        player.openInventory(inv);
    }

    public void showTARDISRecipe(Player player, String type) {
        plugin.getTrackerKeeper().getRecipeView().add(player.getUniqueId());
        Inventory inv = plugin.getServer().createInventory(player, 27, "§4TARDIS " + type.toUpperCase(Locale.ENGLISH) + " seed recipe");
        // redstone torch
        ItemStack red = new ItemStack(Material.REDSTONE_TORCH_ON, 1);
        // lapis block
        ItemStack lapis = new ItemStack(Material.LAPIS_BLOCK, 1);
        // interior wall
        ItemStack in_wall = new ItemStack(Material.WOOL, 1, (byte) 1);
        ItemMeta in_meta = in_wall.getItemMeta();
        in_meta.setDisplayName("Interior walls");
        in_meta.setLore(Arrays.asList("Any valid Wall/Floor block"));
        in_wall.setItemMeta(in_meta);
        // interior floor
        ItemStack in_floor = new ItemStack(Material.WOOL, 1, (byte) 8);
        ItemMeta fl_meta = in_floor.getItemMeta();
        fl_meta.setDisplayName("Interior floors");
        fl_meta.setLore(Arrays.asList("Any valid Wall/Floor block"));
        in_floor.setItemMeta(fl_meta);
        // seed block
        ItemStack block = new ItemStack(t.get(type.toUpperCase(Locale.ENGLISH)), 1);
        // tardis type
        ItemStack tardis = new ItemStack(t.get(type.toUpperCase(Locale.ENGLISH)), 1);
        ItemMeta seed = tardis.getItemMeta();
        // set display name
        seed.setDisplayName("§6TARDIS Seed Block");
        List<String> lore = new ArrayList<>();
        lore.add(type);
        lore.add("Walls: ORANGE_WOOL");
        lore.add("Floors: LIGHT_GRAY_WOOL");
        lore.add("Chameleon: FACTORY");
        seed.setLore(lore);
        tardis.setItemMeta(seed);
        inv.setItem(0, red);
        inv.setItem(9, lapis);
        inv.setItem(11, in_wall);
        inv.setItem(17, tardis);
        inv.setItem(18, block);
        inv.setItem(20, in_floor);
        player.openInventory(inv);
    }

    private String getDisplayName(byte data) {
        MAP map = MAP.getMap(data);
        if (map != null) {
            return map.getDisplayName();
        } else {
            return "Map #" + data;
        }
    }
}
