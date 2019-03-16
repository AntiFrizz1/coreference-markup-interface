package userInterface;

import client.Judge;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sun.nio.ch.sctp.SctpNet;

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


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        judgeLoginScreen();
        infoScene();
        controller.setJudgeText(Arrays.asList(
                "Первое", "предложение", "из", "пяти", "слов.", "Второе", "предложение", "уже", "из", "шести слов."
        ));
        judgeScene();
        primaryStage.setTitle("Ожидание чуда");
        primaryStage.setMinWidth(MIN_APP_WIDTH);
        primaryStage.setMinHeight(MIN_APP_HEIGHT);
        primaryStage.setScene(startScene(primaryStage));
        primaryStage.show();
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

    private FlowPane genChoices() {
        ToggleGroup group = new ToggleGroup();

        RadioButton first = new RadioButton("First");
        RadioButton second = new RadioButton("Second");
        RadioButton both = new RadioButton("Both");
        RadioButton nobody = new RadioButton("Nobody");
        Button except = new Button("Пдтвердить выбор");

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

        FlowPane res = new FlowPane(Orientation.VERTICAL, 10, 10);
        res.setAlignment(Pos.CENTER_LEFT);

        switch (controller.getConflType()) {
            case NEW_SAME:
                both.setText("Подтвердить создание цепочки.");
                nobody.setText("Отклониь создание цепочки.");
                res.getChildren().addAll(both, nobody);
                break;
            case NEWCHAIN_EMPTY:
                first.setText("Подтвердить создание цепочки.");
                second.setText("Отклонить создание цепочки.");
                res.getChildren().addAll(first, second);
                break;
            case ADD_SAME:
                first.setText("Подтвердить решение первого участника.");
                second.setText("Подтердить решение второго участника.");
                nobody.setText("Оба не правы.");
                res.getChildren().addAll(first, second, nobody);
                break;
            case ADD_EMPTY_SAME:
            case CONTCHAIN_EMPTY:
                if (controller.isFirstEmpty()) {
                    second.setText("Подтвердить добавление элемента в цепочку(Принять решение второго участника).");
                    first.setText("Отклонить добавление элемента в цепочку(Принять решение первого участника).");
                    res.getChildren().addAll(second, first);
                } else {
                    first.setText("Подтвердить добавление элемента в цепочку(Принять решение первого участника).");
                    second.setText("Отклонить добавление элемента в цепочку(Принять решение второго участника).");
                    res.getChildren().addAll(first, second);
                }
        }
        res.getChildren().add(except);

        return res;
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
                stage.getScene().getWindow().hide();
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

    ScrollPane genInfo(int labelWidth) {

        Label label = new Label(controller.getInfo());
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

        ScrollPane scroll = genInfo(500);
        GridPane.setMargin(scroll, new Insets(15));

        pane.add(scroll, 0, 0);

        Scene scene = new Scene(pane, 400, 150);
        scene.getStylesheets().add("styles.css");
        pane.getStyleClass().add("default-background");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }


    public void judgeScene() {
        Stage stage = new Stage();
        stage.setTitle("Решение конфликта");

        GridPane main = new GridPane();
        main.getStylesheets().add("styles.css");
        main.getStyleClass().add("default-background");
        main.setGridLinesVisible(true);

        RowConstraints row1 = new RowConstraints(0.2 * APP_HEIGHT, 0.2 * APP_HEIGHT, 0.2 * APP_HEIGHT);
        row1.setFillHeight(true);
        row1.setVgrow(Priority.NEVER);
        main.getRowConstraints().addAll(
                row1,
                makeRowFromPercent(80)
        );

        main.getColumnConstraints().addAll(
                makeColFromPercent(50),
                makeColFromPercent(50)
        );


        ScrollPane textWrapper1 = new ScrollPane();
        textWrapper1.setFitToWidth(true);
        FlowPane text1 = new FlowPane();
        textWrapper1.setContent(text1);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text1, controller.getFirstHighlights(), controller.getFirstWhite(), controller.getFirstSingle());
        GridPane.setHalignment(textWrapper1, HPos.CENTER);
        GridPane.setValignment(textWrapper1, VPos.CENTER);
        GridPane.setMargin(textWrapper1, new Insets(15));


        ScrollPane textWrapper2 = new ScrollPane();
        textWrapper2.setFitToWidth(true);
        FlowPane text2 = new FlowPane();
        textWrapper2.setContent(text2);
        text1.setPadding(new Insets(5));
        generateText(controller.getJudgeText(), text2, controller.getSecondHighlights(), controller.getSecondWhite(), controller.getSecondSingle());
        GridPane.setValignment(textWrapper2, VPos.CENTER);
        GridPane.setHalignment(textWrapper2, HPos.CENTER);
        GridPane.setMargin(textWrapper2, new Insets(15));


        FlowPane choices = genChoices();
        choices.getStyleClass().add("default-outline");
        GridPane.setMargin(choices, new Insets(15));
        GridPane.setValignment(choices, VPos.CENTER);
        GridPane.setHalignment(choices, HPos.LEFT);
        GridPane.setFillHeight(choices, false);

        ScrollPane scroll = genInfo(600);
        GridPane.setHalignment(scroll, HPos.CENTER);
        GridPane.setValignment(scroll, VPos.CENTER);
        GridPane.setFillWidth(scroll, true);
        GridPane.setMargin(scroll, new Insets(15));

        main.add(choices, 1, 0);
        main.add(scroll, 0, 0);
        main.add(textWrapper1, 0, 1);
        main.add(textWrapper2, 1, 1);
        main.setMinWidth(APP_WIDTH);
        main.setMinHeight(APP_HEIGHT);

        Scene sc = new Scene(main, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        stage.setMinWidth(MIN_APP_WIDTH);
        stage.setMinWidth(MIN_APP_HEIGHT);
        stage.setScene(sc);
//        stage.initModality(Modality.WINDOW_MODAL);
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

            /*if (whereSingle.contains(i)) {
                word.getStyleClass().add("chain-selected-judge-highlight");
            } else {
                if (where.contains(i)) {
                    word.getStyleClass().add("chain-selected-judge");
                }
            }*/

            textPane.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            /*if (whereSingle.contains(-i - 1)) {
                space.setText("@");
                space.getStyleClass().add("chain-selected-judge-highlight");
            } else {
                if (whereBlank.contains(i)) {
                    space.setText("@");
                    space.getStyleClass().add("chain-selected-judge");
                }
            }*/
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
