package me.eccentric_nz.TARDIS.commands.admin;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class TARDISMushroomCommand {

    private final TARDIS plugin;
    private final List<String> brownBlockNames = Arrays.asList("", "The Moment", "Siege Cube", "Police Box Blue", "Police Box White", "Police Box Orange", "Police Box Magenta", "Police Box Light Blue", "Police Box Yellow", "Police Box Lime", "Police Box Pink", "Police Box Gray", "Police Box Light Gray", "Police Box Cyan", "Police Box Purple", "Police Box Brown", "Police Box Green", "Police Box Red", "Police Box Black", "Police Box Blue South", "Police Box White South", "Police Box Orange South", "Police Box Magenta South", "Police Box Light Blue South", "Police Box Yellow South", "Police Box Lime South", "Police Box Pink South", "Police Box Gray South", "Police Box Light Gray South", "Police Box Cyan South", "Police Box Purple South", "Police Box Brown South", "Police Box Green South", "Police Box Red South", "Police Box Black South", "Police Box Blue West", "Police Box White West", "Police Box Orange West", "Police Box Magenta West", "Police Box Light Blue West", "Police Box Yellow West", "Police Box Lime West", "Police Box Pink West", "Police Box Gray West", "Police Box Light Gray West", "Police Box Cyan West", "Police Box Purple West", "Police Box Brown West", "Police Box Green West", "Police Box Red West", "Police Box Black West", "Police Box Blue North", "Police Box White North", "Police Box Orange North");
    private final List<String> brownSubs = Arrays.asList("", "the_moment", "siege_cube", "tardis_blue", "tardis_white", "tardis_orange", "tardis_magenta", "tardis_light_blue", "tardis_yellow", "tardis_lime", "tardis_pink", "tardis_gray", "tardis_light_gray", "tardis_cyan", "tardis_purple", "tardis_brown", "tardis_green", "tardis_red", "tardis_black", "tardis_blue_south", "tardis_white_south", "tardis_orange_south", "tardis_magenta_south", "tardis_light_blue_south", "tardis_yellow_south", "tardis_lime_south", "tardis_pink_south", "tardis_gray_south", "tardis_light_gray_south", "tardis_cyan_south", "tardis_purple_south", "tardis_brown_south", "tardis_green_south", "tardis_red_south", "tardis_black_south", "tardis_blue_west", "tardis_white_west", "tardis_orange_west", "tardis_magenta_west", "tardis_light_blue_west", "tardis_yellow_west", "tardis_lime_west", "tardis_pink_west", "tardis_gray_west", "tardis_light_gray_west", "tardis_cyan_west", "tardis_purple_west", "tardis_brown_west", "tardis_green_west", "tardis_red_west", "tardis_black_west", "tardis_blue_north", "tardis_white_north", "tardis_orange_north");
    private final List<String> redBlockNames = Arrays.asList("", "Police Box Magenta North", "Police Box Light Blue North", "Police Box Yellow North", "Police Box Lime North", "Police Box Pink North", "Police Box Gray North", "Police Box Light Gray North", "Police Box Cyan North", "Police Box Purple North", "Police Box Brown North", "Police Box Green North", "Police Box Red North", "Police Box Black North", "Ars", "Bigger", "Budget", "Coral", "Deluxe", "Eleventh", "Ender", "Plank", "Pyramid", "Redstone", "Steampunk", "Thirteenth", "Factory", "Tom", "Twelfth", "War", "Small", "Medium", "Tall", "Legacy Bigger", "Legacy Budget", "Legacy Deluxe", "Legacy Eleventh", "Legacy Redstone", "Pandorica", "Master", "Atomic elements", "Chemical compounds", "Material reducer", "Element constructor", "Lab table", "Product crafting");
    private final List<String> redSubs = Arrays.asList("", "tardis_magenta_north", "tardis_light_blue_north", "tardis_yellow_north", "tardis_lime_north", "tardis_pink_north", "tardis_gray_north", "tardis_light_gray_north", "tardis_cyan_north", "tardis_purple_north", "tardis_brown_north", "tardis_green_north", "tardis_red_north", "tardis_black_north", "ars", "bigger", "budget", "coral", "deluxe", "eleventh", "ender", "plank", "pyramid", "redstone", "steampunk", "thirteenth", "factory", "tom", "twelfth", "war", "small", "medium", "tall", "legacy_bigger", "legacy_budget", "legacy_deluxe", "legacy_eleventh", "legacy_redstone", "pandorica", "master", "creative", "compound", "reducer", "constructor", "lab", "product");
    private final List<String> stemBlockNames = Arrays.asList("", "Weeping Angel Head", "Cyberman Head", "Zygon Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Dalek Head", "Silurian Head", "White Bowtie", "Orange Bowtie", "Magenta Bowtie", "Light_Blue Bowtie", "Yellow Bowtie", "Lime Bowtie", "Pink Bowtie", "Gray Bowtie", "Light_Gray Bowtie", "Cyan Bowtie", "Purple Bowtie", "Blue Bowtie", "Brown Bowtie", "Green Bowtie", "Red Bowtie", "Black Bowtie", "3D Glasses", "TARDIS Communicator", "Blue Torch", "Green Torch", "Purple Torch", "Red Torch", "Custom", "Hexagon", "Roundel", "Roundel Offset", "Cog");
    private final List<String> stemSubs = Arrays.asList("", "angel", "cyberman", "zygon", "dalek", "dalek_brass", "dalek_white", "dalek_orange", "dalek_magenta", "dalek_light_blue", "dalek_yellow", "dalek_lime", "dalek_pink", "dalek_gray", "dalek_light_gray", "dalek_cyan", "dalek_purple", "dalek_blue", "dalek_brown", "dalek_green", "dalek_red", "dalek_black", "silurian", "bowtie_white", "bowtie_orange", "bowtie_magenta", "bowtie_light_blue", "bowtie_yellow", "bowtie_lime", "bowtie_pink", "bowtie_gray", "bowtie_light_gray", "bowtie_cyan", "bowtie_purple", "bowtie_blue", "bowtie_brown", "bowtie_green", "bowtie_red", "bowtie_black", "3d_glasses", "communicator", "blue_torch", "green_torch", "purple_torch", "red_torch", "custom", "hexagon", "roundel", "roundel_offset", "cog");

    public TARDISMushroomCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    public boolean give(CommandSender sender, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
            int which;
            Material mushroom;
            String displayName;
            boolean stem = false;
            if (redSubs.contains(args[1])) {
                which = redSubs.indexOf(args[1]);
                mushroom = Material.RED_MUSHROOM_BLOCK;
                displayName = redBlockNames.get(which);
            } else if (brownSubs.contains(args[1])) {
                which = brownSubs.indexOf(args[1]);
                mushroom = Material.BROWN_MUSHROOM_BLOCK;
                displayName = brownBlockNames.get(which);
            } else if (stemSubs.contains(args[1])) {
                which = stemSubs.indexOf(args[1]);
                mushroom = Material.MUSHROOM_STEM;
                displayName = stemBlockNames.get(which);
                stem = (args.length == 3 && args[2].equalsIgnoreCase("true"));
            } else {
                try {
                    which = Integer.parseInt(args[1]);
                    // spawn an invisible skeleton with a dalek helmet
                    Location location = player.getTargetBlock(plugin.getGeneralKeeper().getTransparent(), 50).getLocation().add(0.0d, 1.25d, 0.0d);
                    location.setYaw(TARDISConstants.RANDOM.nextInt(360));
                    ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                    ItemStack head = new ItemStack(org.bukkit.Material.MUSHROOM_STEM, 1);
                    ItemMeta im = head.getItemMeta();
                    im.setCustomModelData(10000005 + which);
                    im.setDisplayName("Dalek Head");
                    head.setItemMeta(im);
                    stand.setHelmet(head);
                    stand.setArms(false);
                    stand.setBasePlate(false);
                    stand.setVisible(false);
                } catch (NumberFormatException nfe) {
                    TARDISMessage.message(sender, "Invalid TARDIS mushroom block state!");
                }
                return true;
            }
            if (which != 0) {
                ItemStack is = new ItemStack(mushroom, 1);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(displayName);
                im.getPersistentDataContainer().set(plugin.getCustomBlockKey(), PersistentDataType.INTEGER, which);
                im.setCustomModelData(10000000 + which);
                is.setItemMeta(im);
                if (stem) {
                    player.getInventory().setHelmet(is);
                } else {
                    player.getInventory().addItem(is);
                }
                player.updateInventory();
            }
        } else {
            TARDISMessage.send(sender, "CMD_PLAYER");
        }
        return true;
    }
}