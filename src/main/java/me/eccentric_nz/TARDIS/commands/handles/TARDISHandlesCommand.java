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

import me.eccentric_nz.TARDIS.TARDIS;
import me.eccentric_nz.TARDIS.utility.TARDISMessage;
import me.eccentric_nz.TARDIS.utility.TARDISNumberParsers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author eccentric_nz
 */
public class TARDISHandlesCommand implements CommandExecutor {

    private final TARDIS plugin;

    public TARDISHandlesCommand(TARDIS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("tardis.admin")) {
            TARDISMessage.send(sender, "NO_PERM_AREA");
            return true;
        }
        UUID uuid = UUID.fromString(args[1]);
        Player player = plugin.getServer().getPlayer(uuid);
        if (args[0].equals("land")) {
            return new TARDISHandlesTakeoffCommand(plugin).enterVortex(player, args);
        }
        if (args[0].equals("lock") || args[0].equals("unlock")) {
            return new TARDISHandlesLockUnlockCommand(plugin).toggleLock(player, TARDISNumberParsers.parseInt(args[2]), Boolean.valueOf(args[3]));
        }
        if (args[0].equals("name")) {
            TARDISMessage.handlesSend(player, "HANDLES_NAME", player.getName());
            return true;
        }
        if (args[0].equals("remind")) {
            return new TARDISHandlesRemindCommand(plugin).doReminder(player, args);
        }
        if (args[0].equals("say")) {
            return new TARDISHandlesSayCommand(plugin).say(player, args);
        }
        if (args[0].equals("scan")) {
            return new TARDISHandlesScanCommand(plugin, player, TARDISNumberParsers.parseInt(args[2])).sayScan();
        }
        if (args[0].equals("takeoff")) {
            return new TARDISHandlesTakeoffCommand(plugin).enterVortex(player, args);
        }
        if (args[0].equals("time")) {
            return new TARDISHandlesTimeCommand().sayTime(player);
        }
        return false;
    }
}
