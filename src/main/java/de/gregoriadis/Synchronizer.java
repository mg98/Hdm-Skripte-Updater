package de.gregoriadis;

import de.gregoriadis.scriptspage.*;
import de.gregoriadis.Config.FileUpdateHandling;
import javafx.scene.control.Alert;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.HttpStatusException;
import java.io.IOException;
import java.net.UnknownHostException;

public class Synchronizer {

    /**
     * Temp directory to handle immature files
     */
    private final static String tempDir = System.getProperty("user.home") + "/.hdmskripteupdater/tmp";
    /**
     * Stores contents added or updated during the last sync
     */
    private List<Content> lastAdded = new ArrayList<>();
    /**
     * How to handle updates on existing files
     */
    private FileUpdateHandling mode;

    public Synchronizer() {
        mode = Config.getInstance().getFileUpdateHandling();
    }

    /**
     * Initiates sync process
     */
    public void sync() {
        Main.getLogger().info("Synchronyzing begins...");

        lastAdded.clear();
        IhreSkripte scripts = new IhreSkripte();
        List<Course> courses = scripts.getCourses();

        try {
            for (Course course : courses) {
                Main.getLogger().info("Syncing course " + course.getName());

                if (course.locallyExists()) {
                    recursiveContents(course.getContents());
                } else {
                    course.download();
                }
            }
        }
        catch (UnknownHostException e) {
            Main.getLogger().severe("Sync process cancelled because of lost internet connection.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Keine Internetverbindung");
            alert.setContentText("Die Synchronisierung konnte nicht durchgeführt werden. Bitte überprüfe deine Internetverbindung und versuche es erneut.");
            alert.show();

            e.printStackTrace();
        } catch (IOException e) {
            Main.getLogger().severe("Sync process cancelled for unknown reason (IOException).");
            e.printStackTrace();
        }

        Main.getLogger().info("Synchronizing done! " + lastAdded.size() + " items synced.");
    }

    /**
     * @return Added or updated contents in the last sync
     */
    public List<Content> getLastAdded() {
        return lastAdded;
    }

    /**
     * Recursively scans files, directories and sub-directories for synchronization
     *
     * @param contents
     */
    private void recursiveContents(List<Content> contents) throws IOException {
        for (Content content : contents) {

            System.out.println(content.getUrl());

            if (Config.getInstance().isDeepSync() && content.getClass() == Directory.class) {
                Directory dir = (Directory) content;
                recursiveContents(dir.getContents());
            } else  if (content.locallyExists()) {
                    try {
                        // Get local updated at date in string
                        FileTime fileTime = Files.getLastModifiedTime(Paths.get(Config.getInstance().getSyncDirectory() + "/" + content.getLocalPath()));

                        long difference = content.getUpdatedAt().getMillis() - fileTime.toMillis();
                        System.out.println(content.getLocalPath() + " - " + difference);
                        if (difference > 60000) {
                            if (content.getClass() == File.class) {
                                // Download file
                                Main.getLogger().info("Updating local file (old: " + fileTime.toString() + ", new: " + content.getUpdatedAt().toString() + ")");
                                content.download(true);
                                lastAdded.add(content);
                            } else {
                                // Directory
                                Directory dir = (Directory) content;
                                recursiveContents(dir.getContents());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            } else {
                if (content.getClass() == File.class) {
                    // Download file
                    switch (mode) {
                        case OVERWRITE:
                            content.download(true);
                            break;
                        case RENAME:
                            content.download(false);
                            break;
                        case NOTIFY:
                            if (!content.locallyExists()) {
                                content.download(true);
                            }
                            break;
                    }
                    lastAdded.add(content);
                } else {
                    // Directory
                    Directory dir = (Directory) content;
                    recursiveContents(dir.getContents());
                }

            }
        }
    }

    /**
     * @return Path to temp directory
     */
    public static String getTempDir() {
        return tempDir;
    }

}
