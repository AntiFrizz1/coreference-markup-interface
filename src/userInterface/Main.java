package userInterface;

import client.User;
import document.Data;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static userInterface.ScreenUtils.generateErrorScreen;
import static userInterface.ScreenUtils.makeColFromPercent;
import static userInterface.ScreenUtils.makeRowFromPercent;

public class Main extends Application {

    private User user;
    private ControllerImpl controller;
    private Stage primaryStage;
    private static String address = "62.109.13.129";


    @Override
    public void start(Stage primaryStage) {
        Stage primaryStage1 = new Stage();  // not using primaryStage because of showAndWait if a judge connects
        controller = new ControllerImpl(primaryStage1);
        this.primaryStage = primaryStage1;
        userLoginScreen();
    }


    public static void main(String[] args) {
        if (args.length == 1) {
            address = args[0];
        }

        launch(args);
    }

    /**
     * Generates the UI for user login screen, which prompts the user to enter an ID.
     */
    private void userLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите ID пользователя");


        TextField id = new TextField();
        id.setPromptText("Введите ID пользователя...");
        Button enterOffline = new Button("Войти оффлайн");
        Button enterOnline = new Button("Войти онлайн");
        Text error = new Text("");


        enterOffline.setOnAction(event -> {
            controller.offlineMode();
            UserInterface ui = new UserInterface(primaryStage, user, controller);
            ui.genScene();
            stage.getScene().getWindow().hide();
        });

        enterOnline.setOnAction(event -> {
            String login = id.getText();
            id.clear();
            user = new User(login, 3334, address);
            int ans = user.joinOnline();
            if (ans == 2) {
                generateErrorScreen(stage, "Пользователь с таким ID уже авторизован!");
            } else if (ans == 3) {
                generateErrorScreen(stage, "Не удалось подключиться к серверу");
            } else {
                stage.getScene().getWindow().hide();
                Platform.runLater(() -> {
                    controller.onlineMode();
                    UserInterface ui = new UserInterface(primaryStage, user, controller);
                    ui.genScene();
                    if (ans == 0) {
                        ui.setText(user.getText());
                    } else if (ans == 1) {
                        Data data = user.getData();
                        ui.restoreState(data.getText(), data.getActions());
                    }
                });
            }
        });

        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");

        GridPane root = new GridPane();
        root.getRowConstraints().addAll(
                makeRowFromPercent(35),
                makeRowFromPercent(30),
                makeRowFromPercent(35)
        );

        root.getColumnConstraints().addAll(
                makeColFromPercent(100)
        );

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(enterOffline, enterOnline);
        GridPane.setFillHeight(box, true);
        GridPane.setHalignment(box, HPos.CENTER);
        GridPane.setValignment(box, VPos.CENTER);
        GridPane.setFillHeight(box, true);

        GridPane.setValignment(error, VPos.CENTER);
        GridPane.setHalignment(error, HPos.CENTER);
        GridPane.setFillHeight(error, true);
        GridPane.setFillWidth(error, true);

        GridPane.setFillWidth(id, true);
        GridPane.setFillHeight(id, true);
        GridPane.setHalignment(id, HPos.CENTER);
        GridPane.setValignment(id, VPos.CENTER);
        GridPane.setMargin(id, new Insets(10, 60, 10, 60));

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");
        enterOffline.getStyleClass().add("button-font");
        enterOnline.getStyleClass().add("button-font");
        //root.setGridLinesVisible(true);

        root.add(id, 0, 0);
        root.add(box, 0, 1);
        root.add(error, 0, 2);

        stage.setScene(new Scene(root, 300, 150));
        stage.setOnCloseRequest(event -> {
            stage.getScene().getWindow().hide();
        });

        stage.showAndWait();

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterOnline.fire();
                event.consume();
            }
        });


    }

}