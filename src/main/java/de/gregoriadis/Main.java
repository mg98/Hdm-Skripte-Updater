package de.gregoriadis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String baseURL = "https://www.hdm-stuttgart.de/studierende/stundenplan/pers_stundenplan/skripte/";
    private static LoginController loginController;
    private static MainController mainController;
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static Stage primaryStage;
    private static Scene loginScene;
    private static Scene mainScene;



    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setting static AES key
        AES.setKey("zE;=vveFr8GBQWBR;=z4F877MJyifG8y");

        this.primaryStage = primaryStage;
        primaryStage.setTitle("HdM Skripte Updater");
        //set icon of the application
        Image applicationIcon = new Image(getClass().getResourceAsStream("/img/favicon.png"));
        primaryStage.getIcons().add(applicationIcon);

        // Setup login scene
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loginLoader.load();
        loginController = loginLoader.getController();
        loginScene = new Scene(loginRoot);
        loginScene.getStylesheets().add("/css/gui.css");

        // Setup main scene
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent mainRoot = mainLoader.load();
        mainController = mainLoader.getController();
        mainScene = new Scene(mainRoot);
        mainScene.getStylesheets().add("/css/gui.css");

        switchToLoginScene();

        if (!Config.getInstance().getUsername().equals("")) {
            // Try to login
            Document doc = WebScraper.getInstance().getDocumentFromURL(Main.baseURL);
            if (doc == null) {
                LOGGER.info("Could not login with configured credentials");
            } else {
                // Successful login
                LOGGER.info("Config successfull");
                switchToMainScene();
            }
        }

        primaryStage.show();
    }

    public static void switchToMainScene() {
        mainController.initialize();
        primaryStage.setScene(mainScene);
    }

    public static void switchToLoginScene() {
        loginController.initialize();
        primaryStage.setScene(loginScene);
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
