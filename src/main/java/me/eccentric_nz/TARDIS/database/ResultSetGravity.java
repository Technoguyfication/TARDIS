/*
 * Copyright (C) 2012 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.eccentric_nz.TARDIS.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import me.eccentric_nz.TARDIS.TARDIS;
import org.bukkit.World;

/**
 * Anti-gravity was the process of creating objects free from or releasing
 * objects from the force of gravity. The Time Lords' Matrix held many advanced
 * scientific techniques, including the secret of anti-gravity power.
 *
 * Air corridors projected by TARDISes had the option to use anti-gravity,
 * allowing the occupant of the corridor to float through the corridor instead
 * of walk.
 *
 * @author eccentric_nz
 */
public class ResultSetGravity {

    private TARDISDatabase service = TARDISDatabase.getInstance();
    private Connection connection = service.getConnection();
    private TARDIS plugin;
    private HashMap<String, Object> where;
    private boolean multiple;
    private int gravity_id;
    private int tardis_id;
    private World world;
    private int upx;
    private int upz;
    private int downx;
    private int downz;
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

    /**
     * Creates a class instance that can be used to retrieve an SQL ResultSet
     * from the gravity table.
     *
     * @param plugin an instance of the main class.
     * @param where a HashMap<String, Object> of table fields and values to
     * refine the search.
     */
    public ResultSetGravity(TARDIS plugin, HashMap<String, Object> where, boolean multiple) {
        this.plugin = plugin;
        this.where = where;
        this.multiple = multiple;
    }

    /**
     * Retrieves an SQL ResultSet from the doors table. This method builds an
     * SQL query string from the parameters supplied and then executes the
     * query. Use the getters to retrieve the results.
     */
    public boolean resultSet() {
        PreparedStatement statement = null;
        ResultSet rs = null;
        String wheres = "";
        if (where != null) {
            StringBuilder sbw = new StringBuilder();
            for (Map.Entry<String, Object> entry : where.entrySet()) {
                sbw.append(entry.getKey()).append(" = ? AND ");
            }
            wheres = " WHERE " + sbw.toString().substring(0, sbw.length() - 5);
        }
        String query = "SELECT * FROM gravity" + wheres;
        //plugin.debug(query);
        try {
            statement = connection.prepareStatement(query);
            if (where != null) {
                int s = 1;
                for (Map.Entry<String, Object> entry : where.entrySet()) {
                    if (entry.getValue().getClass().equals(String.class)) {
                        statement.setString(s, entry.getValue().toString());
                    } else {
                        statement.setInt(s, plugin.utils.parseNum(entry.getValue().toString()));
                    }
                    s++;
                }
                where.clear();
            }
            rs = statement.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    if (multiple) {
                        HashMap<String, String> row = new HashMap<String, String>();
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columns = rsmd.getColumnCount();
                        for (int i = 1; i < columns + 1; i++) {
                            row.put(rsmd.getColumnName(i).toLowerCase(Locale.ENGLISH), rs.getString(i));
                        }
                        data.add(row);
                    }
                    this.gravity_id = rs.getInt("g_id");
                    this.tardis_id = rs.getInt("tardis_id");
                    this.world = plugin.getServer().getWorld(rs.getString("world"));
                    this.upx = rs.getInt("upx");
                    this.upz = rs.getInt("upz");
                    this.downx = rs.getInt("downx");
                    this.downz = rs.getInt("downz");
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.debug("ResultSet error for doors table! " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                plugin.debug("Error closing doors table! " + e.getMessage());
            }
        }
        return true;
    }

    public int getGravity_id() {
        return gravity_id;
    }

    public int getTardis_id() {
        return tardis_id;
    }

    public World getWorld() {
        return world;
    }

    public int getUpx() {
        return upx;
    }

    public int getUpz() {
        return upz;
    }

    public int getDownx() {
        return downx;
    }

    public int getDownz() {
        return downz;
    }

    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }
}
