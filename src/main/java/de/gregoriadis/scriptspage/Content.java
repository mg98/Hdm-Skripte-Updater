package de.gregoriadis.scriptspage;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.Synchronizer;
import de.gregoriadis.WebScraper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class Content {

    private String name;
    private DateTime updatedAt;
    private String url;
    private String localPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public File getLocalFile() {
        return new java.io.File(localPath);
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean locallyExists() {
        return Files.exists(Paths.get(Config.getInstance().getDirectory() + "/" + localPath));
    }

    public void download() {
        Main.getLogger().info("Downloading from " + url);
        WebScraper.getInstance().download(url, Config.getInstance().getDirectory() + "/" + localPath);
    }

}
