package de.gregoriadis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import javafx.scene.control.MenuBar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String baseURL = "https://www.hdm-stuttgart.de/studierende/stundenplan/pers_stundenplan/skripte/";
    private static LoginController loginController;
    private static MainController mainController;
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static Stage primaryStage;



    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setting static AES key
        AES.setKey("zE;=vveFr8GBQWBR;=z4F877MJyifG8y");

        this.primaryStage = primaryStage;
        primaryStage.setTitle("HdM Skripte Updater");
        //set icon of the application
        Image applicationIcon = new Image(getClass().getResourceAsStream("/img/favicon.png"));
        primaryStage.getIcons().add(applicationIcon);

        // Setup login gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        loginController = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/css/gui.css");

        primaryStage.setScene(scene);

        if (!Config.getInstance().getUsername().equals("")) {
            // Try to login
            Document doc = WebScraper.getInstance().getDocumentFromURL(Main.baseURL);
            if (doc == null) {
                LOGGER.info("Could not login with configured credentials");
            } else {
                // Successful login
                LOGGER.info("Config successfull");
                switchToMainGui();
            }
        }

        primaryStage.show();
    }

    public static void switchToMainGui() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/css/gui.css");
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void saveLogin(String username, String password) {
        System.out.println("save login " + username + " " + password);

        Config.getInstance().setUsername(username);
        Config.getInstance().setPassword(password);
        Config.getInstance().save();
    }

    public static boolean firstStartup() {
        return !Config.fileExists();
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
