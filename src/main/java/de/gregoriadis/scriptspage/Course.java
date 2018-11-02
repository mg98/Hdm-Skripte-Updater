package de.gregoriadis.scriptspage;

import de.gregoriadis.Config;
import de.gregoriadis.Main;
import de.gregoriadis.Synchronizer;
import de.gregoriadis.WebScraper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Course {

    private String name;
    private List<Content> contents = new ArrayList<>();
    private String zipDownloadUrl;

    public List<Content> getContents() {
        return contents;
    }

    protected void setContents(List<Content> contents) {
        this.contents = contents;
    }

    protected void addContent(Content content) {
        contents.add(content);
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected String getZipDownloadUrl() {
        return zipDownloadUrl;
    }

    protected void setZipDownloadUrl(String zipDownloadUrl) {
        this.zipDownloadUrl = zipDownloadUrl;
    }

    public void download() {
        // Download zip
        String tempZipFile = Synchronizer.getTempDir() + "/" + name + ".zip";
        Main.getLogger().info("Downloading from " + zipDownloadUrl);
        WebScraper.getInstance().download(
                zipDownloadUrl,
                tempZipFile
        );

        // Extract file
        Main.getLogger().info("Extracting file to set destination");
        try {
            ZipFile zipFile = new ZipFile(tempZipFile);
            zipFile.extractAll(Config.getInstance().getDirectory() + "/" + name);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        // Delete temp file
        (new java.io.File(tempZipFile)).delete();
        Main.getLogger().info("Temp zip file deleted");
    }

    public boolean locallyExists() {
        return Files.exists(Paths.get(Config.getInstance().getDirectory() + "/" + name));
    }
}
