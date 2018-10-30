package de.gregoriadis;

import de.gregoriadis.scriptspage.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Synchronizer {

    private final static String tempDir = System.getProperty("user.home") + "/.hdmskripteupdater/tmp";
    private List<Content> lastAdded = new ArrayList<>();

    public void sync() {
        lastAdded.clear();
        IhreSkripte scripts = new IhreSkripte();
        List<Course> courses = scripts.getCourses();
        for (Course course : courses) {
            if (course.locallyExists()) {
                recursiveContents(course.getContents());
            } else {
                course.download();
            }
        }

    }

    public List<Content> getLastAdded() {
        return lastAdded;
    }

    private void recursiveContents(List<Content> contents) {
        for (Content content : contents) {
            if (content.locallyExists()) {
                if (content.locallyExists()) {
                    try {
                        // Get local updated at date in string
                        FileTime fileTime = Files.getLastModifiedTime(Paths.get(Config.getInstance().getDirectory() + "/" + content.getLocalPath()));

                        long difference = Math.abs(fileTime.toMillis() - content.getUpdatedAt().getMillis());

                        if (difference > 60000) {
                            if (content.getClass() == File.class) {
                                // Download file
                                content.download();
                                lastAdded.add(content);
                            } else {
                                // Directory aleeert
                                Directory dir = (Directory) content;
                                recursiveContents(dir.getContents());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                System.out.println(content.getLocalPath());
            }
        }
    }

    public static String getTempDir() {
        return tempDir;
    }

}
