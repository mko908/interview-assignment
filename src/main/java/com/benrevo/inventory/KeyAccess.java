package com.benrevo.inventory;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by snap on 10/14/17.
 */
public class KeyAccess {

    private static final String PROTECTED_FILE_PATH = "./protected";

    private static final char DB_CONNECTION = 1;

    /**
     * Method to access the db's connection string from a private file.
     *
     * @return                      The Database's connection string
     * @throws IOException          Throws if protected file cannot be opened or accessed
     */
    static String dbConnectionString() throws IOException {
        return lineScanner(DB_CONNECTION).getString("key");
    }

    /**
     * Helper method for retrieving a line in the protected file of a certain type
     *
     * @param type          Type of information to retrieve
     * @return              JSONObject containing desired information
     * @throws IOException  Throws if file is inaccessible for any reason
     */
    private static JSONObject lineScanner(char type) throws IOException {
        Scanner input = null;
        try {
            input = openFile();
            JSONObject rObj;
            while (input.hasNextLine()) {
                rObj = new JSONObject(input.nextLine());
                if (rObj.getInt("type") == type) {
                    return rObj;
                }
            }
            throw new IOException("Error retrieving credentials");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Helper method returning a scanner to the protected file
     *
     * @return              A Scanner with the protected file as input
     * @throws IOException  Throws if the file cannot be opened.
     */
    private static Scanner openFile() throws IOException {
        File file = new File(PROTECTED_FILE_PATH);
        return new Scanner(file);
    }
}
