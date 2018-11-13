package de.gregoriadis;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton for interactions with the configuration file, saving & serving persisted information
 */
public class Config {

    /**
     * Enum declaring options for when an update for an existing file is available
     *
     * OVERWRITE - File will be downloaded and overwritten
     * RENAME - File will be downloaded and saved with a naming suffix
     * NOTIFY - User will only be notified about an update
     */
    public enum FileUpdateHandling {
        OVERWRITE(0),
        RENAME(1),
        NOTIFY(2);

        private final int radioIndex;

        FileUpdateHandling(int radioIndex) {
            this.radioIndex = radioIndex;
        }

        public int getRadioIndex() {
            return this.radioIndex;
        }
    }

    /**
     * Singleton instance
     */
    private static Config instance;
    /**
     * Hidden syncDirectory on user's machine to persist data
     */
    private final static String fileDirectory = System.getProperty("user.home") + "/.hdmskripteupdater";
    /**
     * Absolute path to config json file
     */
    private final static String configFile = fileDirectory + "/config.json";
    /**
     * HdM login username
     */
    private String username = "";
    /**
     * HdM login password
     */
    private String password = "";
    /**
     * File update handling
     */
    private FileUpdateHandling fileUpdateHandling = FileUpdateHandling.OVERWRITE;
    /**
     * User specified path for synchronization
     */
    private String syncDirectory = MainController.filesRootDirectory;

    /**
     * Setting up syncDirectory and config file
     */
    private Config() {
        try {
            // Create working syncDirectory to store config and temp files
            Files.createDirectories(Paths.get(fileDirectory + "/tmp"));

            // Create config json
            final Path configFilePath = Paths.get(configFile);
            Files.createFile(configFilePath);

            // Write json to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(toJSONString());
            writer.close();

            Main.getLogger().info("Config file created in " + configFilePath.toString());
        } catch (FileAlreadyExistsException e) {
            Main.getLogger().info("Config file exists");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Writing config json file
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(configFile));
            JSONObject jsonObject = (JSONObject) obj;

            username = (String) jsonObject.get("username");
            password = (String) jsonObject.get("password");
            syncDirectory = (String) jsonObject.get("syncDirectory");
            fileUpdateHandling = FileUpdateHandling.valueOf((String) jsonObject.get("fileUpdateHandling"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist settings to file
     */
    public void save() {
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(toJSONString());
            Main.getLogger().info("Config saved");
        } catch (IOException e) {
            Main.getLogger().severe("Could not save to configuration file");
            e.printStackTrace();
        }
    }

    /**
     * Reset hdm login credentials
     */
    public void resetCredentials() {
        username = "";
        password = "";
        save();
    }

    /**
     * Generates a JSON string from the settings
     *
     * @return JSON string
     */
    private String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("password", password);
        obj.put("syncDirectory", syncDirectory);
        obj.put("fileUpdateHandling", fileUpdateHandling.name());

        return obj.toJSONString();
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return password
     */
    protected String getPassword() {
        return AES.decrypt(password);
    }

    /**
     * @param password
     */
    protected void setPassword(String password) {
        this.password = AES.encrypt(password);
    }

    /**
     * @return syncDirectory
     */
    public String getSyncDirectory() {
        return syncDirectory;
    }

    /**
     * @return file syncDirectory
     */
    public static String getFileDirectory() {
        return fileDirectory;
    }

    /**
     * @param syncDirectory
     */
    public void setSyncDirectory(String syncDirectory) {
        this.syncDirectory = syncDirectory;
    }


    public FileUpdateHandling getFileUpdateHandling() {
        return fileUpdateHandling;
    }

    public void setFileUpdateHandling(FileUpdateHandling fileUpdateHandling) {
        this.fileUpdateHandling = fileUpdateHandling;
    }

    /**
     * @return Singleton instance
     */
    public static Config getInstance() {
        if (instance == null) instance = new Config();

        return instance;
    }

    /**
     * @return If config file exists
     */
    public static boolean fileExists() {
        File f = new File(configFile);
        return f.exists() && !f.isDirectory();
    }

}
