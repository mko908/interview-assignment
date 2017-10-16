package com.benrevo.inventory;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Class to manage the REST api
 * @author John Hurst
 */
@org.springframework.web.bind.annotation.RestController
public class RestController {

    private final ResponseEntity SERVER_ERROR = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(null);

    private final ResponseEntity UNAUTHORIZED = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(null);

    private final ResponseEntity BAD_REQUEST = ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Username is already taken");

    private final String USERNAME = "username";
    private final String PASSWORD = "password";

    /**
     * Check that the user's hash matches the local one
     * @param user          Username
     * @param hash          Hash to be matched against database
     * @return              Boolean indicating validity of username and hash
     * @throws SQLException Throws if DataAccess runs into an error
     */
    private boolean checkAuth(String user, String hash) throws SQLException {
        JSONObject userObj = InventoryManagementApplication.dataAccess.getUser(user);
        String userHash = Base64.getEncoder().encodeToString(
                DigestUtils.getSha512Digest().digest(
                        (user + userObj.getString(PASSWORD)).getBytes()));
        return hash.equals(userHash);
    }

    /**
     * Register a new user
     * @param body  Body object for the request
     * @return      Response Entity indicating success or failure
     */
    @CrossOrigin
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody String body) {
        try {
            JSONObject bodyObj = new JSONObject(URLDecoder.decode(body, "UTF-8"));
            InventoryManagementApplication.dataAccess.addUser(bodyObj.getString(USERNAME),
                    bodyObj.getString(PASSWORD));
            JSONArray inventory = InventoryManagementApplication.dataAccess.getInventory(
                    bodyObj.getString(USERNAME));
            JSONObject rObj = new JSONObject();
            rObj.put("data", inventory);
            rObj.put("auth", Base64.getEncoder().encodeToString(
                    DigestUtils.getSha512Digest().digest(
                            (bodyObj.getString(USERNAME) + bodyObj.getString(PASSWORD)).getBytes())));
            return ResponseEntity.ok(rObj.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return BAD_REQUEST;
        } catch (Exception e) {
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }

    /**
     * Login with username and password
     * @param body  Body object for the request
     * @return      Response Entity indicating success or failure
     */
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody String body) {
        try {
            JSONObject bodyObj = new JSONObject(URLDecoder.decode(body, "UTF-8"));
            if (InventoryManagementApplication.dataAccess.login(bodyObj.getString(USERNAME),
                    bodyObj.getString(PASSWORD))) {
                JSONArray inventory = InventoryManagementApplication.dataAccess.getInventory(
                        bodyObj.getString(USERNAME));
                JSONObject rObj = new JSONObject();
                rObj.put("data", inventory);
                rObj.put("auth", Base64.getEncoder().encodeToString(
                        DigestUtils.getSha512Digest().digest(
                                (bodyObj.getString(USERNAME) + bodyObj.getString(PASSWORD)).getBytes())));
                return ResponseEntity.ok(rObj.toString());
            } else {
                return UNAUTHORIZED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }

    /**
     * Add an item to the user's inventory
     * @param auth  Authentication header containing the hashed username and password
     * @param body  Body object for the request
     * @return      Response Entity indicating success or failure
     */
    @CrossOrigin
    @RequestMapping(value = "/inventory/add", method = RequestMethod.POST)
    public ResponseEntity addInventory(@RequestHeader(value = "Authentication") String auth,
                                       @RequestBody String body) {
        try {
            JSONObject bodyObj = new JSONObject(URLDecoder.decode(body, "UTF-8"));
            if (checkAuth(bodyObj.getString(USERNAME), auth)) {
                int id = InventoryManagementApplication.dataAccess.addItem(
                        bodyObj.getString("name"), bodyObj.getInt("amount"), bodyObj.getString(USERNAME));
                JSONObject rObj = new JSONObject();
                rObj.put("id", id);
                return ResponseEntity.ok(rObj.toString());
            } else {
                return UNAUTHORIZED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }

    /**
     * Update a set of items in the user's inventory
     * @param auth  Authentication header containing the hashed username and password
     * @param body  Body object for the request
     * @return      Response Entity indicating success or failure
     */
    @CrossOrigin
    @RequestMapping(value = "inventory/update", method = RequestMethod.PUT)
    public ResponseEntity updateInventory(@RequestHeader(value = "Authentication") String auth,
                                          @RequestBody String body) {
        try {
            JSONObject bodyObj = new JSONObject(URLDecoder.decode(body, "UTF-8"));
            if (checkAuth(bodyObj.getString(USERNAME), auth)) {
                InventoryManagementApplication.dataAccess.updateItem(bodyObj.getJSONArray("data"));
                return ResponseEntity.noContent().build();
            } else {
                return UNAUTHORIZED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }

    /**
     * Delete an item from the user's inventory
     * @param auth  Authentication header containing the hashed username and password
     * @param body  Body object for the request
     * @return      Response Entity indicating success or failure
     */
    @CrossOrigin
    @RequestMapping(value = "inventory/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteInventory(@RequestHeader(value = "Authentication") String auth,
                                          @RequestBody String body) {
        try {
            JSONObject bodyObj = new JSONObject(URLDecoder.decode(body, "UTF-8"));
            if (checkAuth(bodyObj.getString(USERNAME), auth)) {
                InventoryManagementApplication.dataAccess.deleteItem(bodyObj.getInt("id"));
                return ResponseEntity.noContent().build();
            } else {
                return UNAUTHORIZED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }
}
