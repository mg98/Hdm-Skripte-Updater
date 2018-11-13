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

    /**
     * Name of course
     */
    private String name;
    /**
     * Content elements that are owned by this course
     */
    private List<Content> contents = new ArrayList<>();
    /**
     * Download url for a zip archive for all course contents
     */
    private String zipDownloadUrl;

    /**
     * @return contents
     */
    public List<Content> getContents() {
        return contents;
    }

    /**
     * Set contents
     *
     * @param contents
     */
    protected void setContents(List<Content> contents) {
        this.contents = contents;
    }

    /**
     * Add content
     *
     * @param content
     */
    protected void addContent(Content content) {
        contents.add(content);
    }

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
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @return zipDownloadUrl
     */
    protected String getZipDownloadUrl() {
        return zipDownloadUrl;
    }

    /**
     * Set zip download url
     *
     * @param zipDownloadUrl
     */
    protected void setZipDownloadUrl(String zipDownloadUrl) {
        this.zipDownloadUrl = zipDownloadUrl;
    }

    /**
     * Downloads course as zip archive and extracts all files
     * to the sync directory
     */
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
            zipFile.extractAll(Config.getInstance().getSyncDirectory() + "/" + name);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        // Delete temp file
        (new java.io.File(tempZipFile)).delete();
        Main.getLogger().info("Temp zip file deleted");
    }

    /**
     * @return If course directory locally exists
     */
    public boolean locallyExists() {
        return Files.exists(Paths.get(Config.getInstance().getSyncDirectory() + "/" + name));
    }
}
