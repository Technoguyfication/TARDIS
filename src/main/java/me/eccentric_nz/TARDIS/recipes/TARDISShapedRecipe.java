/*
 *  Copyright 2013 eccentric_nz.
 */
package me.eccentric_nz.TARDIS.recipes;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.enumeration.DIFFICULTY;
import me.eccentric_nz.TARDIS.utility.TARDISNumberParsers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * @author eccentric_nz
 */
public class TARDISShapedRecipe {

    private final TARDIS plugin;
    private final HashMap<String, ShapedRecipe> shapedRecipes;
    private ChatColor keyDisplay;
    private ChatColor sonicDisplay;
    private final HashMap<String, ChatColor> sonic_colour_lookup = new HashMap<>();
    private final HashMap<String, ChatColor> key_colour_lookup = new HashMap<>();

    public TARDISShapedRecipe(TARDIS plugin) {
        this.plugin = plugin;
        shapedRecipes = new HashMap<>();
        sonic_colour_lookup.put("mark_1", ChatColor.DARK_GRAY);
        sonic_colour_lookup.put("mark_2", ChatColor.YELLOW);
        sonic_colour_lookup.put("mark_3", ChatColor.DARK_PURPLE);
        sonic_colour_lookup.put("mark_4", ChatColor.GRAY);
        sonic_colour_lookup.put("eighth", ChatColor.BLUE);
        sonic_colour_lookup.put("ninth", ChatColor.GREEN);
        sonic_colour_lookup.put("ninth_open", ChatColor.DARK_GREEN);
        sonic_colour_lookup.put("tenth", ChatColor.AQUA);
        sonic_colour_lookup.put("tenth_open", ChatColor.DARK_AQUA);
        sonic_colour_lookup.put("eleventh", null);
        sonic_colour_lookup.put("eleventh_open", ChatColor.LIGHT_PURPLE);
        sonic_colour_lookup.put("master", ChatColor.DARK_BLUE);
        sonic_colour_lookup.put("sarah_jane", ChatColor.RED);
        sonic_colour_lookup.put("river_song", ChatColor.GOLD);
        sonic_colour_lookup.put("twelfth", ChatColor.UNDERLINE);
        sonic_colour_lookup.put("war", ChatColor.DARK_RED);
        key_colour_lookup.put("first", ChatColor.AQUA);
        key_colour_lookup.put("second", ChatColor.DARK_BLUE);
        key_colour_lookup.put("third", ChatColor.LIGHT_PURPLE);
        key_colour_lookup.put("fifth", ChatColor.DARK_RED);
        key_colour_lookup.put("seventh", ChatColor.GRAY);
        key_colour_lookup.put("ninth", ChatColor.DARK_PURPLE);
        key_colour_lookup.put("tenth", ChatColor.GREEN);
        key_colour_lookup.put("eleventh", null);
        key_colour_lookup.put("susan", ChatColor.YELLOW);
        key_colour_lookup.put("rose", ChatColor.RED);
        key_colour_lookup.put("sally", ChatColor.DARK_AQUA);
        key_colour_lookup.put("perception", ChatColor.BLUE);
        key_colour_lookup.put("gold", ChatColor.GOLD);
    }

    public void addShapedRecipes() {
        keyDisplay = key_colour_lookup.get(plugin.getConfig().getString("preferences.default_key").toLowerCase(Locale.ENGLISH));
        sonicDisplay = sonic_colour_lookup.get(plugin.getConfig().getString("preferences.default_sonic").toLowerCase(Locale.ENGLISH));
        Set<String> shaped = plugin.getRecipesConfig().getConfigurationSection("shaped").getKeys(false);
        shaped.forEach((s) -> {
            plugin.getServer().addRecipe(makeRecipe(s));
        });
    }

    private ShapedRecipe makeRecipe(String s) {
        /*
         * shape: A-A,BBB,CDC ingredients: A: 1 B: 2 C: '5:2' D: 57 result: 276
         * amount: 1 lore: "The vorpal blade\ngoes snicker-snack!" enchantment:
         * FIRE_ASPECT strength: 3
         */
        String[] result_iddata = plugin.getRecipesConfig().getString("shaped." + s + ".result").split(":");
        Material mat = Material.valueOf(result_iddata[0]);
        int amount = plugin.getRecipesConfig().getInt("shaped." + s + ".amount");
        ItemStack is;
        if (result_iddata.length == 2) {
            short result_data = TARDISNumberParsers.parseShort(result_iddata[1]);
            is = new ItemStack(mat, amount, result_data);
        } else {
            is = new ItemStack(mat, amount);
        }
        ItemMeta im = is.getItemMeta();
        if (s.equals("TARDIS Key") && keyDisplay != null) {
            im.setDisplayName(keyDisplay + s);
        } else if (s.equals("Sonic Screwdriver") && sonicDisplay != null) {
            im.setDisplayName(sonicDisplay + s);
        } else {
            im.setDisplayName(s);
        }
        if (s.endsWith("Bow Tie")) {
            is.setDurability((short) 75);
        }
        if (s.equals("3-D Glasses")) {
            is.setDurability((short) 50);
        }
        if (!plugin.getRecipesConfig().getString("shaped." + s + ".lore").equals("")) {
            im.setLore(Arrays.asList(plugin.getRecipesConfig().getString("shaped." + s + ".lore").split("~")));
        }
        is.setItemMeta(im);
        NamespacedKey key = new NamespacedKey(plugin, s.replace(" ", "_").toLowerCase(Locale.ENGLISH));
        ShapedRecipe r = new ShapedRecipe(key, is);
        // get shape
        String difficulty = (plugin.getDifficulty().equals(DIFFICULTY.MEDIUM)) ? "easy" : plugin.getConfig().getString("preferences.difficulty").toLowerCase(Locale.ENGLISH);
        try {
            String[] shape_tmp = plugin.getRecipesConfig().getString("shaped." + s + "." + difficulty + "_shape").split(",");
            String[] shape = new String[3];
            for (int i = 0; i < 3; i++) {
                shape[i] = shape_tmp[i].replaceAll("-", " ");
            }
            r.shape(shape[0], shape[1], shape[2]);
            Set<String> ingredients = plugin.getRecipesConfig().getConfigurationSection("shaped." + s + "." + difficulty + "_ingredients").getKeys(false);
            ingredients.forEach((g) -> {
                char c = g.charAt(0);
                String[] recipe_iddata = plugin.getRecipesConfig().getString("shaped." + s + "." + difficulty + "_ingredients." + g).split(":");
                Material m = Material.valueOf(recipe_iddata[0]);
                if (recipe_iddata.length == 2) {
                    int recipe_data = TARDISNumberParsers.parseInt(recipe_iddata[1]);
                    r.setIngredient(c, m, recipe_data);
                } else {
                    r.setIngredient(c, m);
                }
            });
        } catch (IllegalArgumentException e) {
            plugin.getConsole().sendMessage(plugin.getPluginName() + ChatColor.RED + s + " recipe failed! " + ChatColor.RESET + "Check the recipe config file!");
        }
        shapedRecipes.put(s, r);
        return r;
    }

    public HashMap<String, ShapedRecipe> getShapedRecipes() {
        return shapedRecipes;
    }
}
