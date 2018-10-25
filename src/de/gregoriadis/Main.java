package de.gregoriadis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Main extends Application {

    private static final Path loginFilePath = Paths.get(System.getProperty("user.home") + "/.hdmskripteupdater/login");
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
        } else {
            // Successful login
            LOGGER.info("Login successfull");
            scene = getMainGui();
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Scene getMainGui() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/de/gregoriadis/gui.css");

        return scene;
    }


    public static void main(String[] args) throws IOException {
        try {
            Files.createDirectories(loginFilePath.getParent());
            Files.createFile(loginFilePath);
            LOGGER.info("Login file created in " + loginFilePath.toString());
        } catch (FileAlreadyExistsException e) {
            LOGGER.info("Login file exists");
        }

        // Setting username and password from file
        String loginFile = new String(Files.readAllBytes(loginFilePath));
        String[] loginFileParts = loginFile.split(":");
        Login.setUsername(loginFileParts[0]);
        Login.setPassword(loginFileParts[1]);
        LOGGER.info("Using username: " + Login.getUsername() + ", pass: " + Login.getPassword());

        launch(args);
    }

    public static void downloadZips() {
        final WebScraper scraper = WebScraper.getInstance();

        try {
            Document document = scraper.getDocumentFromURL(baseURL);

            Elements downloads = document.select(".content h2 a");
            for (Element download : downloads) {
                scraper.download(
                        baseURL + download.attr("href"),
                        System.getProperty("user.home") + "/Downloads/download.zip"
                );

                System.out.println(download.attr("href"));
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveLogin(String username, String password) {
        System.out.println("save login " + username + " " + password);

        Login.setUsername(username);
        Login.setPassword(password);

        try {
            Files.write(loginFilePath, (username + ":" + password).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LoginController getLoginController() {
        return loginController;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

}
