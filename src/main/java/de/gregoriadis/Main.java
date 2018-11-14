package de.gregoriadis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;

import java.io.FileInputStream;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String baseURL = "https://www.hdm-stuttgart.de/studierende/stundenplan/pers_stundenplan/skripte/";
    private static LoginController loginController;
    private static MainController mainController;
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static Stage primaryStage;
    private static Scene loginScene;
    private static Scene mainScene;
    private static Scene loadingScene;

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

        // Setup loading scene
        StackPane loadingPane = new StackPane();
        Image loadingImage = new Image(new FileInputStream("src/main/resources/img/loading.gif"));
        ImageView loadingImageView = new ImageView(loadingImage);
        loadingImageView.setFitHeight(150);
        loadingImageView.setFitWidth(150);
        loadingPane.getChildren().add(loadingImageView);
        loadingScene = new Scene(loadingPane, 600, 400);


        switchToLoginScene();

        if (!Config.getInstance().getUsername().equals("") && !Config.getInstance().getPassword().equals("")) {
            //loginController.login();
        }

        primaryStage.show();
    }

    public static void switchToMainScene() {
        primaryStage.setScene(mainScene);
    }

    public static void switchToLoginScene() {
        primaryStage.setScene(loginScene);
    }

    public static void switchToLoginSceneAndInitialize() {
        loginController.initialize();
        primaryStage.setScene(loginScene);
    }

    public static void switchToLoadingScene() {
        primaryStage.setScene(loadingScene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void saveLogin(String username, String password) {
        Config.getInstance().setUsername(username);
        Config.getInstance().setPassword(password);
        Config.getInstance().save();
    }

    public static void logout() {
        Config.getInstance().resetCredentials();
        switchToLoginSceneAndInitialize();
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
