package de.gregoriadis.scriptspage;

import de.gregoriadis.Main;
import de.gregoriadis.Synchronizer;
import de.gregoriadis.WebScraper;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private String name;
    private List<Content> contents = new ArrayList<>();
    private String zipDownloadUrl;

    protected List<Content> getContents() {
        return contents;
    }

    protected void setContents(List<Content> contents) {
        this.contents = contents;
    }

    protected void addContent(Content content) {
        contents.add(content);
    }

    protected String getName() {
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

    public void downloadZip() {
        // Download zip
        String tempZipFile = Synchronizer.getTempDir() + "/" + name + ".zip";
        Main.getLogger().info("Downloading from " + Main.baseURL + zipDownloadUrl);
        WebScraper.getInstance().download(
                Main.baseURL + zipDownloadUrl,
                tempZipFile
        );
    }
}
