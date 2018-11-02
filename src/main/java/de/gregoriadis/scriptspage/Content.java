package de.gregoriadis.scriptspage;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.Synchronizer;
import de.gregoriadis.WebScraper;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstraction of content elements (basically files or directories)
 * fetched from the hdm website
 */
public abstract class Content {

    /**
     * File name with extension
     */
    private String name;
    /**
     * Date of last update/ modification
     */
    private DateTime updatedAt;
    /**
     * Fully qualified remote download url
     */
    private String url;
    /**
     * Local path relative to sync directory to the file
     */
    private String localPath;


    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return name
     */
    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set updated at
     *
     * @param updatedAt
     */
    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set url
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return localPath
     */
    public String getLocalPath() {
        return localPath;
    }

    /**
     * @return file object of local file
     */
    public File getLocalFile() {
        return new java.io.File(localPath);
    }

    /**
     * Set local path
     *
     * @param localPath
     */
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    /**
     * @return If file locally exists
     */
    public boolean locallyExists() {
        return Files.exists(Paths.get(Config.getInstance().getDirectory() + "/" + localPath));
    }

    /**
     * Downloads content into the specified directory
     */
    public void download() {
        Main.getLogger().info("Downloading from " + url);
        String location = Config.getInstance().getDirectory() + "/" + localPath;
        try {
            System.out.println(Paths.get(location).getParent());
            Path p = Paths.get(location);
            p = p.getParent();
            Files.createDirectories(Paths.get(location).getParent());
            WebScraper.getInstance().download(url, location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
