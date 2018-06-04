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
package me.eccentric_nz.TARDIS.enumeration;

import org.bukkit.ChatColor;

/**
 * @author eccentric_nz
 */
public enum ADAPTION {

    OFF(ChatColor.RED),
    BIOME(ChatColor.GREEN),
    BLOCK(ChatColor.AQUA);

    public ChatColor colour;

    ADAPTION(ChatColor colour) {
        this.colour = colour;
    }

    public ChatColor getColour() {
        return colour;
    }
}
