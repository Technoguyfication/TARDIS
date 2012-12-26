package me.eccentric_nz.TARDIS.builders;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.TARDISConstants;
import me.eccentric_nz.TARDIS.database.TARDISDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author eccentric_nz
 */
public class TARDISBuilderInner {

    private final TARDIS plugin;
    TARDISDatabase service = TARDISDatabase.getInstance();
    Statement statement;

    public TARDISBuilderInner(TARDIS plugin) {
        this.plugin = plugin;
    }

    public void buildInner(TARDISConstants.SCHEMATIC schm, World world, TARDISConstants.COMPASS d, int dbID, Player p, int middle_id, byte middle_data) {
        String[][][] s;
        short h, w, l;
        switch (schm) {
            case BIGGER:
                s = plugin.biggerschematic;
                h = plugin.biggerdimensions[0];
                w = plugin.biggerdimensions[1];
                l = plugin.biggerdimensions[2];
                break;
            // Deluxe TARDIS schematic supplied by ewized http://dev.bukkit.org/profiles/ewized/
            case DELUXE:
                s = plugin.deluxeschematic;
                h = plugin.deluxedimensions[0];
                w = plugin.deluxedimensions[1];
                l = plugin.deluxedimensions[2];
                break;
            default:
                s = plugin.budgetschematic;
                h = plugin.budgetdimensions[0];
                w = plugin.budgetdimensions[1];
                l = plugin.budgetdimensions[2];
                break;
        }
        int level, row, col, id, x, y, z, startx, starty = 15, startz, resetx, resetz, cx, cy, cz, rid, multiplier = 1, tx = 0, ty = 0, tz = 0, j = 0;
        byte data;
        short damage = 0;
        String tmp, replacedBlocks;
        HashMap<Block, Byte> postDoorBlocks = new HashMap<Block, Byte>();
        HashMap<Block, Byte> postTorchBlocks = new HashMap<Block, Byte>();
        HashMap<Block, Byte> postSignBlocks = new HashMap<Block, Byte>();
        // calculate startx, starty, startz
        int gsl[] = plugin.utils.getStartLocation(dbID, d);
        startx = gsl[0];
        resetx = gsl[1];
        startz = gsl[2];
        resetz = gsl[3];
        x = gsl[4];
        z = gsl[5];
        Location wg1 = new Location(world, startx, starty, startz);
        // need to set TARDIS space to air first otherwise torches may be placed askew
        // also getting and storing block ids for bonus chest if configured
        StringBuilder sb = new StringBuilder();
        List<Chunk> chunkList = new ArrayList<Chunk>();
        for (level = 0; level < h; level++) {
            for (row = 0; row < w; row++) {
                for (col = 0; col < l; col++) {
                    Location replaceLoc = new Location(world, startx, starty, startz);
                    // get list of used chunks
                    Chunk thisChunk = world.getChunkAt(replaceLoc);
                    if (!chunkList.contains(thisChunk)) {
                        chunkList.add(thisChunk);
                    }
                    if (plugin.getConfig().getBoolean("bonus_chest")) {
                        // get block at location
                        int replacedMaterialId = replaceLoc.getBlock().getTypeId();
                        if (replacedMaterialId != 8 && replacedMaterialId != 9 && replacedMaterialId != 10 && replacedMaterialId != 11) {
                            sb.append(replacedMaterialId).append(":");
                        }
                    }
                    plugin.utils.setBlock(world, startx, starty, startz, 0, (byte) 0);
                    startx += x;
                }
                startx = resetx;
                startz += z;
            }
            startz = resetz;
            starty += 1;
        }
        Location wg2 = new Location(world, startx + (w - 1), starty, startz + (l - 1));
        // update chunks list in DB
        try {
            for (Chunk c : chunkList) {
                int chunkx = c.getX();
                int chunkz = c.getZ();
                statement.executeUpdate("INSERT INTO chunks (tardis_id,world,x,z) VALUES (" + dbID + ", '" + world.getName() + "'," + chunkx + "," + chunkz + ")");
            }
        } catch (SQLException e) {
            plugin.console.sendMessage(plugin.pluginName + " Could not insert reserved chunks into DB!");
        }
        // reset start positions and do over
        startx = resetx;
        starty = 15;
        startz = resetz;
        try {
            Connection connection = service.getConnection();
            statement = connection.createStatement();

            for (level = 0; level < h; level++) {
                for (row = 0; row < w; row++) {
                    for (col = 0; col < l; col++) {
                        tmp = s[level][row][col];
                        if (!tmp.equals("-")) {
                            if (tmp.contains(":")) {
                                String[] iddata = tmp.split(":");
                                id = plugin.utils.parseNum(iddata[0]);
                                data = Byte.parseByte(iddata[1]);
                                if (id == 54) { // chest
                                    // remember the location of this chest
                                    String chest = world.getName() + ":" + startx + ":" + starty + ":" + startz;
                                    String queryChest = "UPDATE tardis SET chest = '" + chest + "' WHERE tardis_id = " + dbID;
                                    statement.executeUpdate(queryChest);
                                }
                                if (id == 77) { // stone button
                                    // remember the location of this button
                                    String button = world.getName() + ":" + startx + ":" + starty + ":" + startz;
                                    String queryButton = "UPDATE tardis SET button = '" + button + "' WHERE tardis_id = " + dbID;
                                    statement.executeUpdate(queryButton);
                                }
                                if (id == 93) { // remember the location of this redstone repeater
                                    // save repeater location
                                    String repeater = world.getName() + ":" + startx + ":" + starty + ":" + startz;
                                    String queryRepeater = "UPDATE tardis SET repeater" + j + " = '" + repeater + "' WHERE tardis_id = " + dbID;
                                    statement.executeUpdate(queryRepeater);
                                    j++;
                                }
                                if (id == 71 && data < (byte) 8) { // iron door bottom
                                    String doorloc = world.getName() + ":" + startx + ":" + starty + ":" + startz;
                                    String queryDoor = "INSERT INTO doors (tardis_id, door_type, door_location, door_direction) VALUES (" + dbID + ", 1, '" + doorloc + "', 'SOUTH')";
                                    statement.executeUpdate(queryDoor);
                                }
                                if (id == 68) { // chameleon circuit sign
                                    String chameleonloc = world.getName() + ":" + startx + ":" + starty + ":" + startz;
                                    String queryChameleon = "UPDATE tardis SET chameleon = '" + chameleonloc + "', chamele_on = 0 WHERE tardis_id = " + dbID;
                                    statement.executeUpdate(queryChameleon);
                                }
                                if (id == 35 && data == 1) {
                                    switch (middle_id) {
                                        case 22:
                                            break;
                                        default:
                                            id = middle_id;
                                            data = middle_data;
                                    }
                                }
                            } else {
                                id = plugin.utils.parseNum(tmp);
                                data = 0;
                            }
                            //plugin.utils.setBlock(World w, int x, int y, int z, int m, byte d)
                            // if its the door, don't set it just remember its block then do it at the end
                            if (id == 71) {
                                postDoorBlocks.put(world.getBlockAt(startx, starty, startz), data);
                            } else if (id == 76) {
                                postTorchBlocks.put(world.getBlockAt(startx, starty, startz), data);
                            } else if (id == 68) {
                                postSignBlocks.put(world.getBlockAt(startx, starty, startz), data);
                            } else {
                                plugin.utils.setBlock(world, startx, starty, startz, id, data);
                            }
                        }
                        startx += x;
                    }
                    startx = resetx;
                    startz += z;
                }
                startz = resetz;
                starty += 1;
            }
        } catch (SQLException e) {
            plugin.console.sendMessage(plugin.pluginName + " Save Block Locations Error: " + e);
        }
        // put on the door and the redstone torches
        for (Map.Entry<Block, Byte> entry : postDoorBlocks.entrySet()) {
            Block pdb = entry.getKey();
            byte pddata = Byte.valueOf(entry.getValue());
            pdb.setTypeIdAndData(71, pddata, true);
        }
        for (Map.Entry<Block, Byte> entry : postTorchBlocks.entrySet()) {
            Block ptb = entry.getKey();
            byte ptdata = Byte.valueOf(entry.getValue());
            ptb.setTypeIdAndData(76, ptdata, true);
        }
        for (Map.Entry<Block, Byte> entry : postSignBlocks.entrySet()) {
            Block psb = entry.getKey();
            byte psdata = Byte.valueOf(entry.getValue());
            psb.setTypeIdAndData(68, psdata, true);
            Sign cs = (Sign) psb.getState();
            cs.setLine(0, "Chameleon");
            cs.setLine(1, "Circuit");
            cs.setLine(3, ChatColor.RED + "OFF");
            cs.update();
        }
        if (plugin.getConfig().getBoolean("bonus_chest")) {
            // get rid of last ":" and assign ids to an array
            String rb = sb.toString();
            replacedBlocks = rb.substring(0, rb.length() - 1);
            String[] replaceddata = replacedBlocks.split(":");
            // get saved chest location
            try {
                String queryGetChest = "SELECT chest FROM tardis WHERE tardis_id = " + dbID;
                ResultSet chestRS = statement.executeQuery(queryGetChest);
                if (chestRS.next()) {
                    String saved_chestloc = chestRS.getString("chest");
                    String[] cdata = saved_chestloc.split(":");
                    World cw = plugin.getServer().getWorld(cdata[0]);
                    cx = plugin.utils.parseNum(cdata[1]);
                    cy = plugin.utils.parseNum(cdata[2]);
                    cz = plugin.utils.parseNum(cdata[3]);
                    Location chest_loc = new Location(cw, cx, cy, cz);
                    Block bonus_chest = chest_loc.getBlock();
                    Chest chest = (Chest) bonus_chest.getState();
                    // get chest inventory
                    Inventory chestInv = chest.getInventory();
                    // convert non-smeltable ores to items
                    for (String i : replaceddata) {
                        rid = plugin.utils.parseNum(i);
                        switch (rid) {
                            case 1: // stone to cobblestone
                                rid = 4;
                                break;
                            case 16: // coal ore to coal
                                rid = 263;
                                break;
                            case 21: // lapis ore to lapis dye
                                rid = 351;
                                multiplier = 4;
                                damage = 4;
                                break;
                            case 56: // diamond ore to diamonds
                                rid = 264;
                                break;
                            case 73: // redstone ore to redstone dust
                                rid = 331;
                                multiplier = 4;
                                break;
                            case 129: // emerald ore to emerald
                                rid = 388;
                                break;
                        }
                        // add items to chest
                        chestInv.addItem(new ItemStack(rid, multiplier, damage));
                        multiplier = 1; // reset multiplier
                        damage = 0; // reset damage
                    }
                } else {
                    System.err.append(plugin.pluginName + " Could not find chest location in DB!");
                }
                chestRS.close();
                statement.close();
            } catch (SQLException e) {
                plugin.console.sendMessage(plugin.pluginName + " Could not get chest location from DB!" + e);
            }
        }
        if (plugin.worldGuardOnServer && plugin.getConfig().getBoolean("use_worldguard")) {
            plugin.wgchk.addWGProtection(p, wg1, wg2);
        }
    }
}