package me.eccentric_nz.TARDIS.chemistry.block;

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.chemistry.compound.CompoundInventory;
import me.eccentric_nz.TARDIS.chemistry.constructor.ConstructorInventory;
import me.eccentric_nz.TARDIS.chemistry.element.ElementInventory;
import me.eccentric_nz.TARDIS.chemistry.lab.LabInventory;
import me.eccentric_nz.TARDIS.chemistry.product.ProductInventory;
import me.eccentric_nz.TARDIS.chemistry.reducer.ReducerInventory;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class ChemistryBlockListener implements Listener {

    private final TARDIS plugin;
    private final HashMap<String, String> blocks = new HashMap<>();
    private final HashMap<String, Integer> models = new HashMap<>();

    public ChemistryBlockListener(TARDIS plugin) {
        this.plugin = plugin;
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=false,up=false,west=false]", "Atomic elements");
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=false,up=false,west=true]", "Chemical compounds");
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=false,up=true,west=false]", "Material reducer");
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=false,up=true,west=true]", "Element constructor");
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=true,up=false,west=false]", "Lab table");
        blocks.put("minecraft:red_mushroom_block[down=true,east=true,north=true,south=true,up=false,west=true]", "Product crafting");
        models.put("Atomic elements", 40);
        models.put("Chemical compounds", 41);
        models.put("Material reducer", 42);
        models.put("Element constructor", 43);
        models.put("Lab table", 44);
        models.put("Product crafting", 45);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChemistryBlockInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            Material material = block.getType();
            if (!material.equals(Material.RED_MUSHROOM_BLOCK)) {
                return;
            }
            String name = blocks.get(block.getBlockData().getAsString());
            if (name != null) {
                Player player = event.getPlayer();
                ItemStack[] menu;
                Inventory inventory;
                switch (name) {
                    case "Atomic elements":
                        // elements
                        if (player.hasPermission("tardis.chemistry.creative")) {
                            menu = new ElementInventory().getMenu();
                        } else {
                            TARDISMessage.send(player, "NO_PERM_CHEM");
                            return;
                        }
                        break;
                    case "Chemical compounds":
                        // compound
                        menu = new CompoundInventory().getMenu();
                        break;
                    case "Material reducer":
                        // reducer
                        menu = new ReducerInventory().getMenu();
                        break;
                    case "Element constructor":
                        // constructor
                        menu = new ConstructorInventory().getMenu();
                        break;
                    case "Lab table":
                        // lab
                        menu = new LabInventory().getMenu();
                        break;
                    default:
                        // product
                        menu = new ProductInventory().getMenu();
                        break;
                }
                inventory = plugin.getServer().createInventory(player, (name.equals("Atomic elements") ? 54 : 27), ChatColor.DARK_RED + name);
                inventory.setContents(menu);
                player.openInventory(inventory);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChemistryBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        Block block = event.getBlock();
        if (!event.getBlock().getType().equals(Material.RED_MUSHROOM_BLOCK)) {
            return;
        }
        String name = blocks.get(block.getBlockData().getAsString());
        if (name != null) {
            ItemStack is = new ItemStack(Material.RED_MUSHROOM_BLOCK, 1);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(blocks.get(block.getType()));
            int cmd = models.get(name);
            im.setCustomModelData(10000000 + cmd);
            im.getPersistentDataContainer().set(plugin.getCustomBlockKey(), PersistentDataType.INTEGER, cmd);
            is.setItemMeta(im);
            block.setBlockData(Material.AIR.createBlockData());
            block.getWorld().dropItemNaturally(event.getPlayer().getLocation(), is);
        }
    }
}