package userInterface;

import client.ConflictImpl;
import client.Judge;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class JudgeInterface extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;

    private volatile Judge judge;

    private boolean isFinish;

    private JudgeController controller = new JudgeController();


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        judgeLoginScreen(primaryStage);
        //startScene().show();
    }

    private void work(Stage primaryStage) {
        isFinish = false;

        while (!isFinish) {
            Stage mainScene = startScene();
            mainScene.show();
            ConflictImpl conflict = (ConflictImpl) judge.getInfo();
            controller.getInfo(conflict.wordList, conflict.firstWordsLocation, conflict.secondWordsLocation, conflict.firstBlanksLocation, conflict.secondBlanksLocation, conflict.firstLast, conflict.secondLast);
            controller.getChains(conflict.firstChain, conflict.secondChain);
            mainScene.getScene().getWindow().hide();
            judgeScene();
        }
    }


    private Stage startScene() {
        Stage stage = new Stage();
        stage.setTitle("Ожидание конфликта.");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        Random random = new Random();
        BorderPane pane = new BorderPane();
        pane.setMinSize(800, 600);
        pane.setId("waiting" + (Math.abs(random.nextInt()) % 7 + 1));
        Scene scene = new Scene(pane, 800, 600);
        scene.getStylesheets().add("styles.css");

        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            isFinish = true;
            scene.getWindow().hide();
        });
        return stage;
    }

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

    private FlowPane genChoices(Stage stage) {
        ToggleGroup group = new ToggleGroup();

        RadioButton first = new RadioButton("First");
        RadioButton second = new RadioButton("Second");
        RadioButton both = new RadioButton("Both");
        RadioButton nobody = new RadioButton("Nobody");
        Button confirm = new Button("Подтвердить выбор");
        confirm.getStyleClass().add("button-font");

        first.setOnAction(event -> {
            controller.setDecision(1);
        });

        second.setOnAction(event -> {
            controller.setDecision(2);
        });

        both.setOnAction(event -> {
            controller.setDecision(3);
        });

        nobody.setOnAction(event -> {
            controller.setDecision(0);
        });

        first.setToggleGroup(group);
        second.setToggleGroup(group);
        both.setToggleGroup(group);
        nobody.setToggleGroup(group);
        nobody.setText("Оба не правы.");
        both.setText("Обав правы.");

        FlowPane res = new FlowPane(Orientation.VERTICAL, 10, 10);
        res.setAlignment(Pos.CENTER_LEFT);

        switch (controller.getConflType()) {
            case NEW_SAME:
                both.setText("Подтвердить создание цепочки.");
                nobody.setText("Отклониь создание цепочки.");
                res.getChildren().addAll(both, nobody);
                break;
            case NEWCHAIN_EMPTY:
                if (controller.isFirstEmpty()) {
                    second.setText("Подтвердить создание цепочки(Принять решение второго участника).");
                    first.setText("Отклонить создание цепочки(Принять решение первого участника).");
                } else {
                    first.setText("Подтвердить создание цепочки(Принять решение первого участника).");
                    second.setText("Отклонить создание цепочки(Принять решение второго участника).");
                }
                res.getChildren().addAll(first, second);
                break;
            case ADD_SAME:
                first.setText("Подтвердить решение первого участника.");
                second.setText("Подтердить решение второго участника.");
                res.getChildren().addAll(first, second, nobody);
                break;
            case ADD_EMPTY_SAME:
            case CONTCHAIN_EMPTY:
                if (controller.isFirstEmpty()) {
                    second.setText("Подтвердить добавление элемента в цепочку(Принять решение второго участника).");
                    first.setText("Отклонить добавление элемента в цепочку(Принять решение первого участника).");
                } else {
                    first.setText("Подтвердить добавление элемента в цепочку(Принять решение первого участника).");
                    second.setText("Отклонить добавление элемента в цепочку(Принять решение второго участника).");
                }
                res.getChildren().addAll(first, second);
            case NEW_ADD:
                if (controller.isFirstEmpty()) {
                    second.setText("Подтвердить добавление элемента в цепочку(Принять решение второго участника).");
                    first.setText("Подтвердить создание новой цепочки(Принять решение первого участника).");
                } else {
                    first.setText("Подтвердить добавление элемента в цепочку(Принять решение первого участника).");
                    second.setText("Подтвердить создание новой цепочки(Принять решение второго участника).");
                }
                res.getChildren().addAll(first, second, nobody);
                break;
        }

        confirm.setOnAction(event -> {
            if (group.getSelectedToggle() != null) {
                confirmDecision(stage, ((RadioButton) group.getSelectedToggle()).getText());
            } else {
                errorScene(stage, "Вы ничего не выбрали.");
            }
        });
        res.getChildren().add(confirm);

        return res;
    }

    private void judgeLoginScreen(Stage primaryStage) {
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
                judge = new Judge("228", 3334, "localhost");

                if (judge.joinOnline() != 0) {
                    error.setText("Не удалось подключиться к серверу.");
                } else {
                    stage.getScene().getWindow().hide();
                    work(primaryStage);
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


        stage.setOnCloseRequest(event -> {
            stage.getScene().getWindow().hide();
        });

        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    ScrollPane genInfo(int labelWidth, String content) {

        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(labelWidth);
        GridPane.setMargin(label, new Insets(10));
        GridPane.setValignment(label, VPos.CENTER);
        GridPane.setHalignment(label, HPos.CENTER);

        GridPane pane = new GridPane();

        pane.getRowConstraints().addAll(
                makeRowFromPercent(100)
        );

        pane.getColumnConstraints().addAll(
                makeColFromPercent(100)
        );

        pane.add(label, 0, 0);

        ScrollPane scroll = new ScrollPane(pane);

        return scroll;
    }


    public void infoScene() {
        Stage stage = new Stage();
        stage.setTitle("Информация о конфликте");

        GridPane pane = new GridPane();

        pane.getColumnConstraints().addAll(
                makeColFromPercent(100)
        );

        pane.getRowConstraints().addAll(
                makeRowFromPercent(100)
        );

        ScrollPane scroll = genInfo(500, controller.getInfo());
        GridPane.setMargin(scroll, new Insets(15));

        pane.add(scroll, 0, 0);

        Scene scene = new Scene(pane, 400, 150);
        scene.getStylesheets().add("styles.css");
        pane.getStyleClass().add("default-background");
        scroll.getStyleClass().add("default-outline");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }


    public void judgeScene() {
        Stage stage = new Stage();
        stage.setTitle("Решение конфликта");

        GridPane main = new GridPane();
        main.getStylesheets().add("styles.css");
        main.getStyleClass().add("default-background");
        main.setMinSize(0.8 * APP_WIDTH, 0.8 * APP_HEIGHT);

        RowConstraints row1 = new RowConstraints(0.3 * APP_HEIGHT);
        row1.setFillHeight(true);
        row1.setVgrow(Priority.NEVER);
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        main.getRowConstraints().addAll(
                row1,
                row2
        );

        ColumnConstraints col1 = makeColFromPercent(50);
        ColumnConstraints col2 = makeColFromPercent(50);

        main.getColumnConstraints().addAll(
                col1,
                col2
        );


        ScrollPane textWrapper1 = new ScrollPane();
        textWrapper1.setFitToWidth(true);
        textWrapper1.setFitToHeight(true);
        FlowPane text1 = new FlowPane();
        textWrapper1.setContent(text1);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text1, controller.getFirstHighlights(), controller.getFirstWhite(), controller.getFirstSingle());
        GridPane.setHalignment(textWrapper1, HPos.CENTER);
        GridPane.setValignment(textWrapper1, VPos.CENTER);
        GridPane.setMargin(textWrapper1, new Insets(15));


        ScrollPane textWrapper2 = new ScrollPane();
        textWrapper2.setFitToWidth(true);
        textWrapper2.setFitToHeight(true);
        FlowPane text2 = new FlowPane();
        textWrapper2.setContent(text2);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text2, controller.getSecondHighlights(), controller.getSecondWhite(), controller.getSecondSingle());
        GridPane.setValignment(textWrapper2, VPos.CENTER);
        GridPane.setHalignment(textWrapper2, HPos.CENTER);
        GridPane.setMargin(textWrapper2, new Insets(15));


        FlowPane choices = genChoices(stage);
        GridPane.setMargin(choices, new Insets(15));
        GridPane.setValignment(choices, VPos.CENTER);
        GridPane.setHalignment(choices, HPos.LEFT);
        GridPane.setFillHeight(choices, false);

        ScrollPane scroll = genInfo(600, controller.getInfo());
        GridPane.setHalignment(scroll, HPos.CENTER);
        GridPane.setValignment(scroll, VPos.CENTER);
        GridPane.setFillWidth(scroll, true);
        GridPane.setMargin(scroll, new Insets(15));

        main.add(choices, 1, 0);
        main.add(scroll, 0, 0);
        main.add(textWrapper1, 0, 1);
        main.add(textWrapper2, 1, 1);


        textWrapper1.getStyleClass().add("default-outline");
        textWrapper2.getStyleClass().add("default-outline");
        scroll.getStyleClass().add("default-outline");
        Scene sc = new Scene(main, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        stage.setMinWidth(0.8 * APP_WIDTH);
        stage.setMinHeight(0.8 * APP_HEIGHT);
        stage.setScene(sc);
        stage.initModality(Modality.WINDOW_MODAL);
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
            textPane.getChildren().add(space);
            if (whereSingle.contains(-i - 1)) {
                space.setText("@");
                space.getStyleClass().add("chain-selected-judge-highlight");
            } else {
                if (whereBlank.contains(i)) {
                    space.setText("@");
                    space.getStyleClass().add("chain-selected-judge");
                }
            }
        }
    }

    private void errorScene(Stage mainStage, String error) {
        Stage stage = new Stage();
        stage.setTitle("Ошибка");
        GridPane root = new GridPane();
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");

        root.getRowConstraints().addAll(
                makeRowFromPercent(70),
                makeRowFromPercent(30)
        );

        root.getColumnConstraints().addAll(
                makeColFromPercent(100)
        );

        Text text = new Text(error);
        GridPane.setValignment(text, VPos.CENTER);
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setFillWidth(text, true);
        GridPane.setFillHeight(text, true);
        GridPane.setMargin(text, new Insets(10, 50, 10, 50));

        Button ok = new Button("OK");
        ok.getStyleClass().add("button-font");

        GridPane.setHalignment(ok, HPos.CENTER);
        GridPane.setValignment(ok, VPos.CENTER);

        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });

        root.add(text, 0, 0);
        root.add(ok, 0, 1);

        stage.initOwner(mainStage);
        stage.setScene(new Scene(root, 400, 150));
        stage.showAndWait();
    }


    private void confirmDecision(Stage mainStage, String decision) {
        Stage stage = new Stage();
        stage.setTitle("Подтвердите выбор решения конфликта");
        GridPane root = new GridPane();

        root.getColumnConstraints().addAll(
                makeColFromPercent(100)
        );

        root.getRowConstraints().addAll(
                makeRowFromPercent(70),
                makeRowFromPercent(30)
        );

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");

        ScrollPane scroll = genInfo(600, "Вы выбрали: ".concat(decision).concat("\nПодтвердите ваш выбор"));
        HBox box = new HBox(20);

        Button ok = new Button("OK");
        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            mainStage.getScene().getWindow().hide();
            judge.sendDecision(controller.getDecision());
        });
        Button cancel = new Button("CANCEL");
        cancel.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        ok.getStyleClass().add("button-font");
        cancel.getStyleClass().add("button-font");

        box.getChildren().addAll(ok, cancel);
        box.setAlignment(Pos.CENTER);
        GridPane.setHalignment(box, HPos.CENTER);
        GridPane.setValignment(box, VPos.CENTER);
        GridPane.setFillHeight(box, true);
        GridPane.setFillWidth(box, true);

        GridPane.setFillWidth(scroll, true);
        GridPane.setValignment(scroll, VPos.CENTER);
        GridPane.setHalignment(scroll, HPos.CENTER);
        GridPane.setMargin(scroll, new Insets(30, 50, 10, 50));

        root.add(box, 0, 1);
        root.add(scroll, 0, 0);

        stage.setScene(new Scene(root, 600, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            stage.getScene().getWindow().hide();
        });
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(mainStage);
        stage.showAndWait();
    }
}
