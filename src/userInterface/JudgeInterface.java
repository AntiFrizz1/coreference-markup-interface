package userInterface;

import client.ConflictImpl;
import client.Judge;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class JudgeInterface extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;
    final private int MIN_APP_WIDTH = 700;
    final private int MIN_APP_HEIGHT = 300;

    private volatile Judge judge;

    private JudgeController controller = new JudgeController();
    private List<String> decisions = Arrays.asList("Не принимать ничье решение", "Принять решение первого", "Принять решение второго", "Принять решения обоих");

    private ColumnConstraints makeColFromPercent(int value) {
        ColumnConstraints res = new ColumnConstraints();
        res.setPercentWidth(value);
        return res;
    }

    private RowConstraints makeRowFromPercent(int value) {
        RowConstraints res = new RowConstraints();
        res.setPercentHeight(value);
        return res;
    }

    private GridPane baseUserPart() {
        GridPane root = new GridPane();

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");

        root.getColumnConstraints().addAll(
                makeColFromPercent(30),
                makeColFromPercent(40),
                makeColFromPercent(30)
        );

        root.getRowConstraints().addAll(
                makeRowFromPercent(10),
                makeRowFromPercent(25),
                makeRowFromPercent(10),
                makeRowFromPercent(25),
                makeRowFromPercent(20)
        );

        return root;
    }

    private GridPane baseUserSubPart() {
        GridPane subRoot = new GridPane();

        subRoot.getColumnConstraints().addAll(
                makeColFromPercent(10),
                makeColFromPercent(35),
                makeColFromPercent(10),
                makeColFromPercent(35),
                makeColFromPercent(10)
        );

        subRoot.getRowConstraints().addAll(
                makeRowFromPercent(20),
                makeRowFromPercent(60),
                makeRowFromPercent(20)
        );
        return subRoot;
    }

    private void judgeLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите пароль");

        GridPane root = baseUserPart();
        GridPane subRoot = baseUserSubPart();

        PasswordField password = new PasswordField();
        password.setPromptText("Введите пароль судьи...");
        GridPane.setValignment(password, VPos.CENTER);
        GridPane.setHalignment(password, HPos.CENTER);

        Button enter = new Button("Войти");
        enter.getStyleClass().add("button-font");

        Button back = new Button("Назад");
        back.getStyleClass().add("button-font");

        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);

        Text error = new Text("");

        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");

        enter.setOnAction(event -> {
            if (password.getText().equals("1234")) {
                judge = new Judge("1234", 3334, "192.168.43.126");
                if (judge.joinOnline() == 0) {
                    stage.getScene().getWindow().hide();
                } else {
                    System.out.println("SOSESH'");
                }
            } else {
                error.setText("Неверный пароль!");
            }
        });
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enter.fire();
                event.consume();
            }
        });

        back.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        subRoot.add(enter, 0, 1, 2, 1);
        subRoot.add(back, 3, 1, 2, 1);

        root.add(password, 1, 1);
        root.add(error, 1, 2);
        root.add(subRoot, 1, 3);

        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);

        stage.setOnCloseRequest(event -> {
            stage.getScene().getWindow().hide();
        });

        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }


    public void start(Stage primaryStage) {
        judgeLoginScreen();
        primaryStage.setTitle("Ожидание чуда");
        primaryStage.setMinWidth(MIN_APP_WIDTH);
        primaryStage.setMinHeight(MIN_APP_HEIGHT);
        primaryStage.setScene(startScene(primaryStage));
        primaryStage.show();
        while (true) {
            ConflictImpl conflict = (ConflictImpl) judge.getInfo();
            if (conflict == null) {
                //
                break;
            }


            controller.getInfo(conflict.wordList, conflict.firstWordsLocation, conflict.secondWordsLocation,
                    conflict.firstBlanksLocation, conflict.secondBlanksLocation, conflict.firstLast, conflict.secondLast);
            judgeScene();
            judge.sendDecision(controller.getDecision());
        }
    }

    public static void main(String[] args){
        launch(args);
    }


    public void setJudge(Judge judge) {
        this.judge = judge;
    }

    public JudgeController getController() {
        return controller;
    }

    private Scene startScene(Stage primaryStage) {
        //TODO something cool
        return new Scene(new BorderPane());
    }

    public void judgeScene() {
        Stage stage = new Stage();
        BorderPane leftSide = new BorderPane();

        GridPane texts = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        texts.getColumnConstraints().addAll(col1, col2);

        ScrollPane textWrapper1 = new ScrollPane();
        textWrapper1.setFitToWidth(true);
        FlowPane text1 = new FlowPane();
        textWrapper1.setContent(text1);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text1, controller.getFirstHighlights(), controller.getFirstWhite(), controller.getFirstSingle());


        ScrollPane textWrapper2 = new ScrollPane();
        textWrapper2.setFitToWidth(true);
        FlowPane text2 = new FlowPane();
        textWrapper2.setContent(text2);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text2, controller.getSecondHighlights(), controller.getSecondWhite(), controller.getSecondSingle());

        texts.add(textWrapper1, 0, 0);
        texts.add(textWrapper2, 1, 0);
        leftSide.setCenter(texts);

        HBox box = new HBox(5);

        Button b1 = new Button(decisions.get(1));
        b1.setOnAction(event -> {
            controller.setDecision(1);
            confirmDecision(stage);
        });

        Button b2 = new Button(decisions.get(2));
        b2.setOnAction(event -> {
            controller.setDecision(2);
            confirmDecision(stage);
        });

        Button b3 = new Button(decisions.get(3));
        b3.setOnAction(event -> {
            controller.setDecision(3);
            confirmDecision(stage);
        });

        Button b4 = new Button(decisions.get(0));
        b4.setOnAction(event -> {
            controller.setDecision(0);
            confirmDecision(stage);
        });

        box.getChildren().addAll(b1, b2, b3, b4);
        leftSide.setTop(box);

        Scene sc = new Scene(leftSide, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        stage.setOnCloseRequest(Event::consume);
        stage.setMinWidth(MIN_APP_WIDTH);
        stage.setMinWidth(MIN_APP_HEIGHT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(sc);
        stage.showAndWait();
    }

    private void generateText(List<String> words, FlowPane textPane, Set<Integer> where, Set<Integer> whereBlank, Set<Integer> whereSingle) {
        textPane.getChildren().clear();
        // TODO: should probably remove punctuation from buttons into separate TextAreas

        for (int i = 0; i < words.size(); i++) {
            Button word = new Button(words.get(i));
            word.getStyleClass().add("word");
            word.getStyleClass().add("search");
            word.setStyle("-fx-background-color: rgba(0,0,0,0);");

            if (whereSingle.contains(i)) {
                word.getStyleClass().add("chain-selected-judge-highlight");
            } else {
                if (where.contains(i)) {
                    word.getStyleClass().add("chain-selected-judge");
                }
            }

            textPane.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            if (whereSingle.contains(-i - 1)) {
                space.setText("@");
                space.getStyleClass().add("chain-selected-judge-highlight");
            } else {
                if (whereBlank.contains(-i-1)) {
                    space.setText("@");
                    space.getStyleClass().add("chain-selected-judge");
                }
            }
            textPane.getChildren().add(space);
        }
    }


    private void confirmDecision(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Подтвердите выбор решения конфликта");
        GridPane root = new GridPane();

        root.add(new Text("Вы выбрали: ".concat(decisions.get(controller.getDecision()))), 0, 0);

        /*BorderPane buttons = new BorderPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(20);
        buttons.getColumnConstraints().addAll(col1, col2, col3);*/

        HBox box = new HBox(190);

        Button ok = new Button("OK");
        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            primaryStage.getScene().getWindow().hide();
            //TODO disable buttons or make empty Scene
        });
        Button cancel = new Button("CANCEL");
        cancel.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        box.getChildren().addAll(ok, cancel);
        root.add(box, 0, 1);

        stage.setScene(new Scene(root, 300, 70));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }
}
