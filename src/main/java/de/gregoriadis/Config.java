package de.gregoriadis;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class Config {

    private static Config instance;
    private final static String fileDirectory = System.getProperty("user.home") + "/.hdmskripteupdater";
    private final static String configFile = fileDirectory + "/config.json";
    private String username = "";
    private String password = "";
    private String directory = MainController.filesRootDirectory;

    public Config() {
        try {
            // Create working directory to store config and temp files
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

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(configFile));
            JSONObject jsonObject = (JSONObject) obj;

            username = (String) jsonObject.get("username");
            password = (String) jsonObject.get("password");
            directory = (String) jsonObject.get("directory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(toJSONString());
            Main.getLogger().info("Config saved");
        } catch (IOException e) {
            Main.getLogger().severe("Could not save to configuration file");
            e.printStackTrace();
        }
    }

    private String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("password", password);
        obj.put("directory", directory);

        return obj.toJSONString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public static Config getInstance() {
        if (instance == null) instance = new Config();

        return instance;
    }

    public static boolean fileExists() {
        File f = new File(configFile);
        return f.exists() && !f.isDirectory();
    }

}
