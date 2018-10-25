package de.gregoriadis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.io.FileUtils;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String baseURL = "https://www.hdm-stuttgart.de/studierende/stundenplan/pers_stundenplan/skripte/";
    private static LoginController loginController;
    private static MainController mainController;
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("HdM Skripte Updater");

        // Setup login gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        loginController = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/de/gregoriadis/gui.css");

        // Try to login
        Document doc = WebScraper.getInstance().getDocumentFromURL(Main.baseURL);
        if (doc == null) {
            LOGGER.info("Could not login with configured credentials");
            primaryStage.setScene(scene);
        } else {
            // Successful login
            LOGGER.info("Config successfull");
            switchToMainGui();
        }

        primaryStage.show();
    }

    public static void switchToMainGui() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("main.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/de/gregoriadis/gui.css");
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void downloadEverything() {
        final WebScraper scraper = WebScraper.getInstance();

        try {
            String tempDir = System.getProperty("user.home") + "/.hdmskripteupdater/tmp";
            FileUtils.deleteDirectory(new File(tempDir));
            (new File(tempDir)).mkdir();

            Document document = scraper.getDocumentFromURL(baseURL);

            Elements downloads = document.select(".content h2 a");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    getMainController().setSyncInfo("0/" + downloads.size());
                }
            });

            int i = 0;
            for (Element download : downloads) {
                String name = download.parent().ownText();
                name = name.substring(8, name.length() - 2);

                // Download zip
                String tempZipFile = tempDir + "/" + name + ".zip";
                LOGGER.info("Downloading from " + baseURL + download.attr("href"));
                scraper.download(
                        baseURL + download.attr("href"),
                        tempZipFile
                );

                // Extract file
                LOGGER.info("Extracting file to set destination");
                try {
                    ZipFile zipFile = new ZipFile(tempZipFile);
                    zipFile.extractAll(Config.getInstance().getDirectory() + "/" + name);
                } catch (ZipException e) {
                    e.printStackTrace();
                }

                // Delete file
                (new File(tempZipFile)).delete();
                LOGGER.info("Temp zip file deleted");

                final int finalI = i++;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        getMainController().setSyncInfo(finalI + "/" + downloads.size());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveLogin(String username, String password) {
        System.out.println("save login " + username + " " + password);

        Config.getInstance().setUsername(username);
        Config.getInstance().setPassword(password);
        Config.getInstance().save();
    }

    public static LoginController getLoginController() {
        return loginController;
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
