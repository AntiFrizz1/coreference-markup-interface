package userInterface;

import chain.Action;
import chain.Blank;
import chain.Location;
import chain.Phrase;
import client.ConflictImpl;
import client.Judge;
import document.UpdateDocument;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class JudgeInterface extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;

    private volatile Judge judge;

    private boolean isFinish;

    private static String host = "178.128.38.25";

    private JudgeController controller = new JudgeController();

    private BooleanProperty existConflict = new SimpleBooleanProperty(false);

    private Stage mainScene;

    public static void main(String[] args) {
        if (args.length > 0) {
            host = args[0];
        }
        launch(args);
    }

    public void start(Stage primaryStage) {
        judgeLoginScreen(primaryStage);
    }

    private void work(Stage primaryStage) {
        isFinish = false;

        while (!isFinish) {
            ConflictImpl conflict = (ConflictImpl) judge.getInfo();
            if (conflict == null || !judge.isServerWork) {
                judge.kill();
                Platform.runLater(() -> mainScene.close());
                break;
            }

            controller.getInfo(conflict.wordList, conflict.firstWordsLocation, conflict.secondWordsLocation, conflict.firstBlanksLocation, conflict.secondBlanksLocation, conflict.firstLast, conflict.secondLast);
            controller.getChains(conflict.firstChain, conflict.secondChain);
            controller.setTextId(conflict.textId);
            controller.setFirstActionsList(conflict.first);
            controller.setSecondActionsList(conflict.second);

            Platform.runLater(() -> existConflict.setValue(!existConflict.getValue()));

            /*mainScene.getScene().getWindow().hide();
            judgeScene();*/
        }
    }


    private void startScene() {
        Stage stage = new Stage();
        stage.setTitle("Ожидание конфликта");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        Random random = new Random();
        BorderPane pane = new BorderPane();
        pane.setMinSize(800, 600);
        pane.setId("waiting" + (Math.abs(random.nextInt()) % 7 + 1));
        Scene scene = new Scene(pane, 800, 600);
        scene.getStylesheets().add("styles.css");
        pane.getStyleClass().add("default-background");

        stage.setResizable(false);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            isFinish = true;
            if (judge != null) {
                judge.kill();
            }
            scene.getWindow().hide();
        });

        existConflict.addListener((observable, oldValue, newValue) -> {
            stage.getScene().getWindow().hide();
            judgeScene();
        });
        mainScene = stage;

        mainScene.show();
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
        Button confirm = new Button("Подтвердить выбор");
        confirm.getStyleClass().add("button-font");

        ComboBox<String> comboBox = new ComboBox<>();

        FlowPane res = new FlowPane(Orientation.VERTICAL, 10, 10);
        res.setAlignment(Pos.CENTER_LEFT);
        JudgeController.Confl a = controller.getConflType();
        System.out.println(a.toString());
        switch (controller.getConflType()) {
            case NEW_SAME:
                comboBox.setItems(FXCollections.observableArrayList("Подтвердить создание цепочки", "Отклонить создание цепочки"));
                comboBox.setOnAction(event -> {
                    switch (comboBox.getItems().indexOf(comboBox.getValue())) {
                        case 0:
                            controller.setDecision(3);
                            break;
                        case 1:
                            controller.setDecision(0);
                            break;
                    }
                });
                break;
            case NEWCHAIN_EMPTY:
                if (controller.isFirstEmpty()) {
                    comboBox.setItems(FXCollections.observableArrayList("Отклонить создание цепочки (Принять решение первого участника)", "Подтвердить создание цепочки (Принять решение второго участника)"));
                    } else {
                    comboBox.setItems(FXCollections.observableArrayList("Подтвердить создание цепочки (Принять решение первого участника)", "Отклонить создание цепочки (Принять решение второго участника)"));
                        }
                comboBox.getItems().add("Оба не правы (Предложить свой вариант)");
                comboBox.setOnAction(event -> {
                    switch (comboBox.getItems().indexOf(comboBox.getValue())) {
                        case 0:
                            controller.setDecision(1);
                            break;
                        case 1:
                            controller.setDecision(2);
                            break;
                        case 2:
                            controller.setDecision(-1);
                            break;
                    }
                });
                break;
            case ADD_EMPTY_SAME:
            case CONTCHAIN_EMPTY:
                if (controller.isFirstEmpty()) {
                    comboBox.setItems(FXCollections.observableArrayList("Отклонить добавление элемента в цепочку (Принять решение первого участника)",
                            "Подтвердить добавление элемента в цепочку (Принять решение второго участника)"
                    ));
                } else {
                    comboBox.setItems(FXCollections.observableArrayList("Подтвердить добавление элемента в цепочку (Принять решение первого участника)",
                            "Отклонить добавление элемента в цепочку (Принять решение второго участника)"
                    ));
                    }
                comboBox.getItems().add("Оба не правы (Предложить свой вариант)");
                comboBox.setOnAction(event -> {
                    switch (comboBox.getItems().indexOf(comboBox.getValue())) {
                        case 0:
                            controller.setDecision(1);
                            break;
                        case 1:
                            controller.setDecision(2);
                            break;
                        case 2:
                            controller.setDecision(-1);
                            break;
                    }
                });
                break;
            case NEW_ADD:
                if (controller.isFirstEmpty()) {
                    comboBox.setItems(FXCollections.observableArrayList("Подтвердить создание новой цепочки (Принять решение первого участника)",
                            "Подтвердить добавление элемента в цепочку (Принять решение второго участника)"
                    ));
                } else {
                    comboBox.setItems(FXCollections.observableArrayList("Подтвердить добавление элемента в цепочку (Принять решение первого участника)",
                            "Подтвердить создание новой цепочки (Принять решение второго участника)"
                    ));
                }
                comboBox.getItems().addAll("Оба не правы", "Оба не правы (Предложить свой вариант)");

                comboBox.setOnAction(event -> {
                    switch (comboBox.getItems().indexOf(comboBox.getValue())) {
                        case 0:
                            controller.setDecision(1);
                            break;
                        case 1:
                            controller.setDecision(2);
                            break;
                        case 2:
                            controller.setDecision(0);
                            break;
                        case 3:
                            controller.setDecision(-1);
                            break;
                    }
                });
                break;
            default:
                comboBox.setItems(FXCollections.observableArrayList("Подтвердить решение первого участника",
                        "Подтвердить решение второго участника",
                        "Оба правы (Объединить цепочки)",
                        "Оба правы (Но должны находиться в разных цепочках)",
                        "Оба не правы",
                        "Оба не правы (Предложить свой вариант)"
                ));
                comboBox.setOnAction(event -> {
                    switch (comboBox.getItems().indexOf(comboBox.getValue())) {
                        case 0:
                            controller.setDecision(1);
                            break;
                        case 1:
                            controller.setDecision(2);
                            break;
                        case 2:
                            controller.setDecision(3);
                            break;
                        case 3:
                            controller.setDecision(4);
                            break;
                        case 4:
                            controller.setDecision(0);
                            break;
                        case 5:
                            controller.setDecision(-1);
                            break;
                    }
                });
        }

        comboBox.setMaxWidth(500);
        confirm.setOnAction(event -> {
            if (comboBox.getValue() != null) {
                if (controller.getDecision() == -1) {
                    makeAnswer(stage);
                } else {
                    confirmDecision(stage, comboBox.getValue());
                }
            } else {
                errorScene(stage, "Вы ничего не выбрали");
            }
        });
        res.getChildren().addAll(comboBox, confirm);

        return res;
    }

    private void judgeLoginScreen(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Введите пароль");


        GridPane root = baseUserPart();
        GridPane subRoot = baseUserSubPart();


        TextField password = new TextField();
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
            judge = new Judge(password.getText(), 3333, host);
            int ans = judge.joinOnline();
            if (ans == 2) {
                error.setText("Неверный пароль!");
            } else if (ans == 0) {
                error.setText("");
                stage.getScene().getWindow().hide();
                startScene();
                Thread worker = new Thread(() -> work(primaryStage));
                worker.start();
            } else{
                error.setText("Не удалось подключиться к серверу");
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

        stage.setOnCloseRequest(event -> {
            isFinish = true;
            if (judge != null) {
                judge.kill();
            }
            stage.hide();
        });

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
                if (whereBlank.contains(-i - 1)) {
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


    private List<Action> getActionsFromWindow(List<Action> oldActions, int startFrom, String windowName) {
        Stage stage = new Stage();
        ControllerImpl cont = new ControllerImpl(stage);
        cont.loginJudge();
        UserInterface ui = new UserInterface(stage, null, cont, windowName);
        ui.restoreState(judge.getTextByIndex(controller.getTextId()), oldActions, startFrom);
        ui.genScene();
        return ui.getActions();
    }

    private List<Action> prepareActions(int id, String name, List<Action> newActions, List<Action> oldActions) {
        if (newActions.size() == 0) {
            return new ArrayList<>();
        }
        int i = 0;
        while (oldActions.size() != 0 && newActions.size() != 0 && oldActions.get(i).getLocation().equals(newActions.get(0).getLocation())) {
            newActions.remove(0);
            i++;
        }

        if (newActions.size() == 0) {
            return new ArrayList<>();
        }

        List<Action> answer = new ArrayList<>();

        for (i = 0; i < newActions.size(); i++) {
            Action action = newActions.get(i);
            answer.add(new Action(action.getAction(), id, action.getLocation(), name));
        }
        return answer;
    }

    private int getLastLocation(List<Action> actions) {
        Action lastAction = actions.get(actions.size() - 1);
        Location location = actions.get(actions.size() - 1).getLocation();

        if (lastAction.isEmpty()) {
            return -1;
        }

        if (location instanceof Blank) {
            return ((Blank) location).getPosition();
        } else {
            return ((Phrase) location).getPositions().stream().min(Integer::compareTo).get();
        }
    }

    private void makeAnswer(Stage mainStage) {
        List<Action> oldFirstActions = controller.getPreparedFirstActionsList();
        List<Action> oldSecondActions = controller.getPreparedSecondActionsList();

        int firstChainId = controller.getFirstActionsList().get(0).getChainId();
        String firstChainName = controller.getFirstActionsList().get(0).getName();

        int secondChainId = controller.getSecondActionsList().get(0).getChainId();
        String secondChainName = controller.getSecondActionsList().get(0).getName();

        int startFromFirst = getLastLocation(controller.getFirstActionsList());
        int startFromSecond = getLastLocation(controller.getSecondActionsList());

        if (startFromFirst < 0 && startFromSecond > 0) {
            startFromFirst = startFromSecond;
        } else if (startFromFirst > 0 && startFromSecond < 0) {
            startFromSecond = startFromFirst;
        }

        List<Action> newFirstActions = getActionsFromWindow(oldFirstActions, startFromFirst, "Выполните разметку для первого участника");

        if (newFirstActions.isEmpty()) {
            return;
        }

        List<Action> newSecondActions = getActionsFromWindow(oldSecondActions, startFromSecond, "Выполните разметку для второго участника");

        if (newSecondActions.isEmpty()) {
            return;
        }

        List<Action> firstActionsToServer = prepareActions(firstChainId, firstChainName, newFirstActions, oldFirstActions);
        List<Action> secondActionsToServer = prepareActions(secondChainId, secondChainName, newSecondActions, oldSecondActions);

        if (firstActionsToServer.isEmpty() || secondActionsToServer.isEmpty()) {
            return;
        }

        mainStage.getScene().getWindow().hide();
        mainScene.show();
        UpdateDocument firstDoc = new UpdateDocument(firstActionsToServer);
        UpdateDocument secondDoc = new UpdateDocument(secondActionsToServer);

        judge.sendDecisionWithActionList(-1, firstDoc.pack(), secondDoc.pack());
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

        Button ok = new Button("ОК");
        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            mainStage.getScene().getWindow().hide();
            judge.sendDecision(controller.getDecision());
            mainScene.show();
        });

        Button cancel = new Button("Отмена");
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
