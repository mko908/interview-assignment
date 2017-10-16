package com.benrevo.inventory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for ease of access ot the data layer. Provides CRUD for inventory, and Create and Read for users.
 *
 * @author John Hurst
 */
public class DataAccess {
    private Connection conn;

    /**
     * Initiate connection to database
     */
    public DataAccess() {
        setConnection();
        activatePersistence();
    }

    /**
     * Check if connection is valid
     * @return boolean indicating valid connection
     * @throws SQLException Throws if conn is not a SQL connection
     */
    private boolean isValid() throws SQLException {
        return conn.isValid(10);
    }

    /**
     * Set the database connection to a predefined connection string
     */
    private void setConnection() {
        try {
            String connString = KeyAccess.dbConnectionString();
            conn = DriverManager.getConnection(connString);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start a connection checker
     */
    private void activatePersistence() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!isValid()) {
                        setConnection();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setConnection();
                }
            }
        }, 5000, 5000);
    }

    /**
     * Register user with Postgres instance
     * @param username Requested username
     * @param password Requested password
     * @throws SQLException Throws if username already exists, SQL is malformed, or the connection is invalid
     */
    public void addUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    /**
     * Get a user
     * @param username      Username of user to be retrieved
     * @return              JSON Object representing the requested user
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public JSONObject getUser(String username) throws SQLException {
        String sql = "SELECT username, password FROM users WHERE username = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        JSONObject user = new JSONObject();
        if (rs.next()) {
            user.put("username", rs.getString("username"));
            user.put("password", rs.getString("password"));
        }
        return user;
    }

    /**
     * Retrieve login info for user
     * @param username Username of requester
     * @param password Password of requester
     * @return True if username matches with password
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public boolean login(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) == 1;
    }

    /**
     * Retrieve inventory for user
     * @param username Username of requester
     * @return JSONArray representing inventory
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public JSONArray getInventory(String username) throws SQLException {
        String sql = "SELECT id, productname, amount FROM inventory WHERE userid IN " +
                "(SELECT id FROM users WHERE username = ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        JSONArray array = new JSONArray();
        while (rs.next()) {
            JSONObject tempObj = new JSONObject();
            tempObj.put("id", rs.getInt("id"));
            tempObj.put("name", rs.getString("productname"));
            tempObj.put("amount", rs.getInt("amount"));
            array.put(tempObj);
        }
        return array;
    }

    /**
     * Update inventory items
     * @param items JSON Array containing items to update
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public void updateItem(JSONArray items) throws SQLException {
        String sql = "UPDATE inventory SET productname = ?, amount = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        int count = 1;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            ps.setString(1, item.getString("name"));
            ps.setInt(2, item.getInt("amount"));
            ps.setInt(3, item.getInt("id"));
            ps.addBatch();
            count++;
            if (count % 10 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
    }

    /**
     * Delete inventory items
     * @param id id of item to delete
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public void deleteItem(int id) throws SQLException {
        String sql = "DELETE FROM inventory WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    /**
     * Add item into inventory table
     * @param name          Name of inventory item
     * @param amount        Amount of inventory item
     * @param username      User that inventory item corresponds to
     * @throws SQLException Throws if SQL is malformed or if connection is invalid
     */
    public Integer addItem(String name, int amount, String username) throws SQLException {
        String sql = "INSERT INTO inventory (productname, amount, userid) " +
                "SELECT ?, ?, id FROM users WHERE username = ?";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setInt(2, amount);
        ps.setString(3, username);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public void cleanDatabase() throws SQLException {
        cleanInventory();
        cleanUsers();
    }

    public void cleanInventory() throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        String deleteInventory = "DELETE FROM inventory";
        String alterSeq = "ALTER SEQUENCE inventory_id_seq RESTART WITH 1";
        PreparedStatement ps = conn.prepareStatement(deleteInventory);
        ps.execute();
        ps = conn.prepareStatement(alterSeq);
        ps.execute();
        conn.commit();
        conn.setAutoCommit(autoCommit);
    }

    public void cleanUsers() throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        String deleteUsers = "DELETE FROM users";
        String alterSeq = "ALTER SEQUENCE users_id_seq RESTART WITH 1";
        PreparedStatement ps = conn.prepareStatement(deleteUsers);
        ps.execute();
        ps = conn.prepareStatement(alterSeq);
        ps.execute();
        conn.commit();
        conn.setAutoCommit(autoCommit);
    }
}
