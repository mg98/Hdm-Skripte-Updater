package de.gregoriadis;

import javafx.application.Platform;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

public class Synchronizer {

    private final WebScraper scraper = WebScraper.getInstance();

    public void sync() {


    }

    public void downloadEverything() {

        try {
            String tempDir = System.getProperty("user.home") + "/.hdmskripteupdater/tmp";
            FileUtils.deleteDirectory(new File(tempDir));
            (new File(tempDir)).mkdir();

            Document document = scraper.getDocumentFromURL(Main.baseURL);

            Elements downloads = document.select(".content h2 a");

            for (Element download : downloads) {
                String name = download.parent().ownText();
                name = name.substring(8, name.length() - 2);

                // Download zip
                String tempZipFile = tempDir + "/" + name + ".zip";
                Main.getLogger().info("Downloading from " + Main.baseURL + download.attr("href"));
                scraper.download(
                        Main.baseURL + download.attr("href"),
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

                // Delete file
                (new File(tempZipFile)).delete();
                Main.getLogger().info("Temp zip file deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
