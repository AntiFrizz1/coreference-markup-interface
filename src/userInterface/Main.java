package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import client.Judge;
import client.User;
import document.Data;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main extends Application {

    private User user;
    final private int APP_WIDTH = 800;
    final private int APP_HEIGHT = 600;
    final private int MIN_APP_WIDTH = 700;
    final private int MIN_APP_HEIGHT = 300;
    final private int RGB_BLACK = -16777216;
    private ControllerImpl controller;
    private String judgePassword = "qkordkfr";
    private List<String> logins = Arrays.asList("benzrady",
            "nmrctygk",
            "mclkxvsj",
            "odvxfuci",
            "dfubtwqg",
            "camamhnk",
            "iowczict",
            "ajngwijk",
            "uffawdre",
            "qggghbug",
            "ncixnfkx",
            "gczwfxtx",
            "irynigde",
            "yfzkjkes",
            "pffcpfkv",
            "mfdmnwum",
            "uotqbhvv",
            "ffurtemf",
            "mhkpwmun",
            "vkwljtir",
            "fefxzvzg",
            "nozerbzp",
            "pujwjwvg",
            "pbytbkok",
            "wujjjagi",
            "obumepcz",
            "gxldygao",
            "hyadyobq",
            "ioqpyjxb",
            "mmxmyyot",
            "umbkejwb",
            "kddtnqob",
            "msgqyfrz",
            "bqkyogqt",
            "bvvuverw",
            "jtdyjkck",
            "weahvkkv",
            "kfhbgtsx",
            "hnpxahwe",
            "hucedyvq",
            "cxeixlte",
            "izarunno",
            "nmdvtkqy",
            "xwcghmmh",
            "hnfcjwmg",
            "wiffdwgp",
            "ugosjxop",
            "wcoaftgi",
            "mhvlclsh",
            "ihsqnrpw",
            "qkbmdtvy",
            "rnahpxue",
            "ytmrrels",
            "xpgcsccm",
            "qzszwksw",
            "gisoxhzp",
            "evhqggxb",
            "povbkyln",
            "hkihogra",
            "mbqfjlmv",
            "hzjwrsbw",
            "ygqrzbby",
            "afjicwrg",
            "ldfknpnh",
            "sfsnetgw",
            "invmaxqd",
            "yizrkyrk",
            "rspgqlzf",
            "qbmxhwkl",
            "gpkfybeg",
            "bvjjxtwy",
            "cywzrkcu",
            "rndnibnl",
            "knyghfbn",
            "ahjntsuf",
            "wizhcvmi",
            "rsleqtom",
            "lwzphawv",
            "pzhdmvgg",
            "jwcyrccd",
            "xrdunttf",
            "egvqfunb",
            "pohqhspv",
            "yuzcwcsh",
            "wskeofoe",
            "nsaqwlyr",
            "viomipbt",
            "ufpigoqa",
            "ajasswvn",
            "ytgazwdv",
            "tjhpaawo",
            "ugeqaaow",
            "gehujxkx",
            "gqksnopt",
            "pbzpdhtd",
            "vwgdhsnf",
            "yecygdql",
            "hwpgqdyh",
            "nhlqqxin",
            "urmnmysy"
    );

    private int selectedSentenceStart = 0, selectedSentenceEnd = 0, textSizeInWords, displayedIndex;
    /**
     * A search criteria for chains. Only works on separate links in chain (i.e. a chain link
     * must contain the whole string).
     */
    private String chainFilter = "";

    private JudgeInterface judgeInterface = new JudgeInterface();

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        controller = new ControllerImpl(primaryStage);

//        loginUser();

        if (controller.isJudge()) {
            judgeInterface.start(primaryStage);
        } else {
            primaryStage.setTitle("Разметка кореференсов");
            Scene sc = genScene(primaryStage);
            primaryStage.setMinWidth(MIN_APP_WIDTH);
            primaryStage.setMinHeight(MIN_APP_HEIGHT);
            primaryStage.setScene(sc);
            //System.out.println("AAA");
            primaryStage.show();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void loginUser() {
        Stage stage = new Stage();
        stage.setTitle("Выберите роль");
        GridPane root = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();
        ColumnConstraints col5 = new ColumnConstraints();
        col1.setPercentWidth(10);
        col2.setPercentWidth(35);
        col3.setPercentWidth(10);
        col4.setPercentWidth(35);
        col5.setPercentWidth(10);

        root.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        row1.setPercentHeight(30);
        row2.setPercentHeight(40);
        row3.setPercentHeight(30);

        root.getRowConstraints().addAll(row1, row2, row3);

        Button judge = new Button("Войти как судья");
        Button user = new Button("Войти как пользователь");
        judge.setPadding(new Insets(5));
        user.setPadding(new Insets(5));
        GridPane.setFillHeight(judge, true);
        GridPane.setFillHeight(user, true);
        judge.setOnAction(event -> {
            judgeLoginScreen();
            if (controller.isJudge() || controller.isLoggedUser()) stage.getScene().getWindow().hide();
        });
        user.setOnAction(event -> {
            userLoginScreen();
            if (controller.isOfflineMode() || controller.isJudge() || controller.isLoggedUser()) {
                stage.getScene().getWindow().hide();
            }
        });
        root.add(judge, 1, 1);
        root.add(user, 3, 1);
        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
//        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    private void judgeLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите пароль");
        GridPane root = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(30);
        col2.setPercentWidth(40);
        col3.setPercentWidth(30);

        root.getColumnConstraints().addAll(col1, col2, col3);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();
        row1.setPercentHeight(10);
        row2.setPercentHeight(25);
        row3.setPercentHeight(20);
        row4.setPercentHeight(25);
        row5.setPercentHeight(10);


        root.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        GridPane subRoot = new GridPane();

        ColumnConstraints ccol1 = new ColumnConstraints();
        ColumnConstraints ccol2 = new ColumnConstraints();
        ColumnConstraints ccol3 = new ColumnConstraints();
        ColumnConstraints ccol4 = new ColumnConstraints();
        ColumnConstraints ccol5 = new ColumnConstraints();

        ccol1.setPercentWidth(10);
        ccol2.setPercentWidth(35);
        ccol3.setPercentWidth(10);
        ccol4.setPercentWidth(35);
        ccol5.setPercentWidth(10);

        subRoot.getColumnConstraints().addAll(ccol1, ccol2, ccol3, ccol4, ccol5);

        RowConstraints rrow1 = new RowConstraints();
        RowConstraints rrow2 = new RowConstraints();
        RowConstraints rrow3 = new RowConstraints();

        rrow1.setPercentHeight(20);
        rrow2.setPercentHeight(60);
        rrow3.setPercentHeight(20);

        subRoot.getRowConstraints().addAll(rrow1, rrow2, rrow3);

        PasswordField password = new PasswordField();
        password.setPromptText("Введите пароль судьи...");
        GridPane.setValignment(password, VPos.CENTER);
        GridPane.setHalignment(password, HPos.CENTER);
        Button enter = new Button("Войти");
        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);
        Text error = new Text("");
        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");
        enter.setOnAction(event -> {
            if (password.getText().equals(judgePassword)) {
                stage.getScene().getWindow().hide();
                controller.loginJudge();
                Judge judge = new Judge(1337, 3333, "62.109.13.129", judgeInterface.getController());
                judgeInterface.setJudge(judge);
                if (judge.joinOnline() != 0) {
                    error.setText("Не удалось подключиться. Проверьте подключение к интернету.");
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
        Button back = new Button("Назад");
        back.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        subRoot.add(enter, 1, 1);
        subRoot.add(back, 3, 1);
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

    /**
     * Generates the UI for user login screen, which prompts the user to enter an ID.
     */
    private void userLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите ID пользователя");
        GridPane root = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(30);
        col2.setPercentWidth(40);
        col3.setPercentWidth(30);

        root.getColumnConstraints().addAll(col1, col2, col3);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();
        row1.setPercentHeight(10);
        row2.setPercentHeight(25);
        row3.setPercentHeight(20);
        row4.setPercentHeight(25);
        row5.setPercentHeight(10);


        root.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        GridPane subRoot = new GridPane();

        ColumnConstraints ccol1 = new ColumnConstraints();
        ColumnConstraints ccol2 = new ColumnConstraints();
        ColumnConstraints ccol3 = new ColumnConstraints();
        ColumnConstraints ccol4 = new ColumnConstraints();
        ColumnConstraints ccol5 = new ColumnConstraints();

        ccol1.setPercentWidth(10);
        ccol2.setPercentWidth(35);
        ccol3.setPercentWidth(10);
        ccol4.setPercentWidth(35);
        ccol5.setPercentWidth(10);

        subRoot.getColumnConstraints().addAll(ccol1, ccol2, ccol3, ccol4, ccol5);

        RowConstraints rrow1 = new RowConstraints();
        RowConstraints rrow2 = new RowConstraints();
        RowConstraints rrow3 = new RowConstraints();

        rrow1.setPercentHeight(20);
        rrow2.setPercentHeight(60);
        rrow3.setPercentHeight(20);

        subRoot.getRowConstraints().addAll(rrow1, rrow2, rrow3);

        TextField id = new TextField();
        id.setPromptText("Введите ID пользователя...");
        GridPane.setValignment(id, VPos.CENTER);
        GridPane.setHalignment(id, HPos.CENTER);
        Button enter = new Button("Войти");
        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);
        Button enterOffline = new Button("Войти оффлайн");
        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);
        enterOffline.setOnAction(event -> {
            controller.offlineMode();
            stage.getScene().getWindow().hide();
        });
        Text error = new Text("");
        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");
        enter.setOnAction(event -> {
            Integer ID = 0;
            ID = logins.indexOf(id.getText());
            if (ID == -1) {
                error.setText("Неправильный ID!");
                return;
            }
            user = new User(ID, 3333, "62.109.13.129");
            int out = user.joinOnline();
            if (out == 0) {
                stage.getScene().getWindow().hide();
                Platform.runLater(() -> {
                    controller.setText(user.getText());
                });
                controller.loginUser(ID);
            } else if (out == 1) {
                stage.getScene().getWindow().hide();
                Platform.runLater(() -> {
                    Data data = user.getData();
                    controller.restoreState(data.getText(), data.getActions());
                });
                controller.loginUser(ID);
            } else {
                if (out == 2) {
                    error.setText("Пользователь с таким id уже авторизовался!");
                } else {
                    error.setText("Не удалось подключиться к серверу");
                }
            }
        });
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enter.fire();
                event.consume();
            }
        });

        Button back = new Button("Назад");
        back.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });


        subRoot.add(enter, 1, 1);
        subRoot.add(back, 3, 1);
//        subRoot.add(enterOffline, 5, 1);
        root.add(id, 1, 1);
        root.add(error, 0, 2);
        root.add(subRoot, 1, 3);
        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            stage.getScene().getWindow().hide();
        });
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    /**
     * Generates the main UI window for the app.
     *
     * @param primaryStage stage to bind the scene to
     * @return the scene
     */
    private Scene genScene(Stage primaryStage) {
        /*
        General layout: two columns, the left one with text/buttons and the right one with chains.
         */
        GridPane overall = new GridPane();
        {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
            ColumnConstraints col2 = new ColumnConstraints();
            overall.getColumnConstraints().addAll(col1, col2);
        }

        /*
        A pane containing the selected text in form of buttons.
         */
        ScrollPane textWrapper = new ScrollPane();
        textWrapper.setFitToWidth(true);
        FlowPane text = new FlowPane();
        textWrapper.setContent(text);
        text.setPadding(new Insets(5));
        generateText(text, textWrapper);

        GridPane rightSide = new GridPane();

        /*
        A scrollpane with a list of chains.
         */
        ScrollPane pane = new ScrollPane();
        pane.prefWidthProperty().bind(primaryStage.widthProperty().divide(4));
        GridPane chainsList = new GridPane();
        pane.setContent(chainsList);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);

        /*
        An input text field from which chainFilter is parsed.
         */
        TextField field = new TextField();
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            chainFilter = newValue;
            genChainsList(chainsList, text, controller.getChains());
        });
        field.setPromptText("Введите текст для поиска в цепочках...");

        rightSide.getRowConstraints().addAll(row1, row2);
        rightSide.add(field, 0, 0);
        rightSide.add(pane, 0, 1);

        BorderPane leftSide = new BorderPane();

        leftSide.setCenter(textWrapper);

        /*
        The event handler used to generate the text when restoring the state of the program.
         */
        primaryStage.addEventHandler(ControllerImpl.RefreshEvent.REFRESH_TEXT, event -> {
            generateText(text, textWrapper);
            event.consume();
        });

        /*
        The event handler used to generate the text when restoring the state of the program.
         */
        primaryStage.addEventHandler(ControllerImpl.RefreshChainEvent.REFRESH_CHAIN, event -> {
            genChainsList(chainsList, text, controller.getChains());
            event.consume();
        });

        /*
        A box that contains the buttons at the top.
         */
        HBox box = new HBox();

        Button b4 = new Button("Отменить");
        b4.setOnAction(event -> {
            Action ac = controller.cancel();
            genChainsList(chainsList, text, controller.getChains());
            undoAction(ac, text, controller.getChains());
            int remaining = controller.getPrevStatesSize();
            if (remaining == 0) b4.setDisable(true);
        });
        b4.setDisable(true);

        Button b1 = new Button("Продолжить цепочку");
        b1.setOnAction(event -> {
            Set<Integer> selected = controller.getSelected();
            if (controller.isSelectedAlreadyBound()) {
                generateErrorScreen(primaryStage, "Выбранные вами слова уже добавлены в другую цепочку!");
                controller.clearSelected();
                removeSelectionFromText(selected, text);
                return;
            }
            if (selected.isEmpty()) {
                generateErrorScreen(primaryStage, "Не выбрано ни одно слово!");
            } else if (controller.getSelectedChain() == -1) {
                generateErrorScreen(primaryStage, "Не выбрана ни одна цепочка!");
            }
            Action ac = controller.addToChain();
            if (ac != null) {
                List<Chain> chains = controller.getChains();
                genChainsList(chainsList, text, chains);
                updateColoring(ac, chains.get(0), text);
                removeSelectionFromText(selected, text);
                b4.setDisable(false);
            }
        });

        Button b2 = new Button("Новая цепочка");
        b2.setOnAction(event -> {
            Set<Integer> selected = controller.getSelected();
            if (controller.isSelectedAlreadyBound()) {
                generateErrorScreen(primaryStage, "Выбранные вами слова уже добавлены в другую цепочку!");
                controller.clearSelected();
                removeSelectionFromText(selected, text);
                return;
            }
            if (!selected.isEmpty()) {
                openChainNameDialogue(primaryStage);
            } else {
                generateErrorScreen(primaryStage, "Не выбрано ни одно слово!");
            }
            Action ac = controller.addNewChain();
            if (ac != null) {
                List<Chain> chains = controller.getChains();
                genChainsList(chainsList, text, chains);
                updateColoring(ac, chains.get(0), text);
                removeSelectionFromText(selected, text);
                b4.setDisable(false);
            }
        });

        Button b3 = new Button("Добавить нулевую анафору");
        b3.setOnAction(event -> {
            int selectedBlank = controller.getSelectedBlank();
            if (controller.isSelectedBlankAlreadyBound()) {
                generateErrorScreen(primaryStage, "Выбранные вами слова уже добавлены в другую цепочку!");
                controller.pressedButton(" ", selectedBlank);  // sets selectedBlank to -1
                toggleSelected((Button) text.getChildren().get(2 * (selectedBlank - displayedIndex) + 1), "word");
                return;
            }
            if (selectedBlank == -1) {
                generateErrorScreen(primaryStage, "Не выбран ни один пробел!");
            } else if (controller.getSelectedChain() == -1) {
                generateErrorScreen(primaryStage, "Не выбрана ни одна цепочка!");
            }
            Action ac = controller.addAnaphoraToChain();
            if (ac != null) {
                List<Chain> chains = controller.getChains();
                genChainsList(chainsList, text, chains);
                Blank b = (Blank) ac.getLocation();
                toggleSelected((Button) text.getChildren().get(2 * (b.getPosition() - displayedIndex) + 1), "word");
                ((Button) text.getChildren().get(2 * (b.getPosition() - displayedIndex) + 1)).setText("@");
                updateColoring(ac, chains.get(0), text);
                b4.setDisable(false);
            }
        });

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button fileSelect = new Button("Выбрать файл для разметки");
        fileSelect.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    String txt = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8)).lines().collect(Collectors.joining(". "));
                    txt = txt.replaceAll("\\s+", " ").replaceAll("\\.+", ".").replaceAll("(\\. )+", ". ");
                    controller.setText(txt);
                    controller.setTextPath(file.getName());
                    File file1 = Paths.get("dump" + controller.textPath).toFile();
                    try {
                        if (file1.exists()) {
                            controller.restoreFromDump(file1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            /*FileChooser сhooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));*/
        });
        /*Button dump = new Button("Восстановить разметку из дампа");
        dump.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    controller.restoreFromDump(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        if (controller.isOnline()) fileSelect.setVisible(false);
        box.getChildren().addAll(b1, b2, b3, b4, spacer, fileSelect/*, dump*/);
        leftSide.setTop(box);

        /*
        A bottom pane with sentence navigation and text search.
         */
        GridPane bottom = new GridPane();

        TextField textField = new TextField();
        textField.setPromptText("Введите слово для поиска...");
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.length() >= 3) {  // remove highlight from old query
                for (Node button : text.getChildren()) {
                    if (((Button) button).getText().toLowerCase().contains(oldValue.toLowerCase()))
                        toggleSelected((Button) button, "search");
                }
            }
            if (newValue.length() >= 3) {  // highlight all found words
                for (Node button : text.getChildren()) {
                    if (((Button) button).getText().toLowerCase().contains(newValue.toLowerCase()))
                        toggleSelected((Button) button, "search");
                }
            }
        });

        /*
        The following action basically searches for the next occurrence of a sentence start and shifts the
        indices accordingly, while toggling the buttons styles.
         */
        Button right = new Button(">");
        right.setOnAction(event -> {
            controller.saveStateOffline();
            controller.clearActions();
            controller.clearSelected();
            controller.pressedButton(" ", controller.getSelectedBlank());
            if (selectedSentenceEnd != textSizeInWords - 1) {
                selectedSentenceEnd++;
                selectedSentenceStart = selectedSentenceEnd;
                generateText(text, textWrapper);
//                if (!controller.getActions().isEmpty() && controller.isOnline())
//                    if (user.sendUpdates(controller.getActions()) == 0) {
//                        controller.clearActions();
//                    }
                b4.setDisable(true);
            } else {
                if (controller.isOnline()) {
                    user.close(controller.getActions());
                    controller.clearActions();
                }
            }
        });

        /*
        The event handler used to generate the text when restoring the state of the program.
         */
        primaryStage.addEventHandler(ControllerImpl.MoveSelectedSentenceEvent.MOVE_SELECTED_SENTENCE,
                event -> {
                    while (selectedSentenceStart < event.id) right.fire();
                    event.consume();
                });

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        bottom.getColumnConstraints().addAll(col1, col2);
        bottom.add(textField, 0, 0);
        bottom.add(right, 1, 0);

        leftSide.setBottom(bottom);

        leftSide.prefHeightProperty().bind(primaryStage.heightProperty());
        overall.add(leftSide, 0, 0);
        overall.add(rightSide, 1, 0);
        Scene sc = new Scene(overall, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        return sc;
    }

    /**
     * Generates a list of buttons containing the text and puts them into a chosen pane.
     *
     * @param textPane a pane to put the text buttons into
     */
    private void generateText(FlowPane textPane, ScrollPane outer) {
        String text = controller.getText();
        List<Chain> chains = controller.getChains();

        /*
        A clone pane used to determine whether we should stop adding new buttons.
         */
        Pane pane = new Pane();
        ScrollPane textWrapper = new ScrollPane();
        pane.getChildren().add(textWrapper);
        textWrapper.setMaxHeight(outer.getHeight());
        textWrapper.setMaxWidth(outer.getWidth());
        textWrapper.setMinHeight(outer.getHeight());
        textWrapper.setMinWidth(outer.getWidth());
        textWrapper.setFitToWidth(true);
        FlowPane flow = new FlowPane();
        textWrapper.setContent(flow);
        flow.setPadding(new Insets(5));
        Scene s = new Scene(pane);
        s.getStylesheets().add("styles.css");

        textPane.getChildren().clear();
        // TODO: should probably remove punctuation from buttons into separate TextAreas
        String[] words = text.split(" ");
        textSizeInWords = words.length;
        displayedIndex = Math.max(0, selectedSentenceStart - 15);
        boolean sentence = true;
        boolean toSetSentence = true;
        for (int i = displayedIndex; i < words.length; i++) {
            Button word = new Button(words[i]);
            word.getStyleClass().add("word");  // CSS class for styling words and their selection
            word.getStyleClass().add("search");  // CSS class for styling text search
            word.setStyle("-fx-background-color: rgba(0,0,0,0);");
            for (Chain c : chains) {
                if (controller.chainContainsWord(c, i)) {
                    word.setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                            c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
                    break;
                }
            }
            final int iF = i;
            if (i < selectedSentenceStart) {
                word.getStyleClass().add("highlight");  // CSS class for styling sentence selection
            } else if (sentence) {
                if (selectedSentenceEnd != textSizeInWords - 1 && !isSentenceStart(words[i], words[i + 1])) {
                    selectedSentenceEnd++;
                } else {
                    toSetSentence = false;
                }
                word.getStyleClass().add("highlight-selected");
                word.setOnAction(event -> {
                    if (controller.pressedButton(words[iF], iF)) toggleSelected(word, "word");
                });
            } else {
                word.getStyleClass().add("highlight");
            }
            Button clone = new Button(words[i]);
            clone.getStyleClass().add("word");
            flow.getChildren().add(clone);
            pane.applyCss();
            pane.layout();
            if (flow.getBoundsInLocal().getHeight() > outer.getHeight() && !sentence) return;
            textPane.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            for (Chain c : chains) {
                if (controller.chainContainsBlank(c, i)) {
                    space.setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                            c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
                    space.setText("@");
                    break;
                }
            }
            if (i < selectedSentenceStart - 1) {
                space.getStyleClass().add("highlight");  // CSS class for styling sentence selection
            } else if (sentence) {
                space.getStyleClass().add("highlight-selected");
                space.setOnAction(event -> {
                    if (controller.pressedButton("   ", iF)) toggleSelected(space, "word");
                });
            } else {
                space.getStyleClass().add("highlight");
            }
            Button spaceClone = new Button("   ");
            spaceClone.getStyleClass().add("word");
            flow.getChildren().add(spaceClone);
            pane.applyCss();
            pane.layout();
            if (flow.getBoundsInLocal().getHeight() > outer.getHeight() && !sentence) return;
            textPane.getChildren().add(space);
            if (!toSetSentence) sentence = false;
        }
    }

    /**
     * Generates a list of buttons representing the chains, filters them and puts into a chosen pane.
     *
     * @param chainsList a pane to put buttons into
     * @param chains     a list of chains
     */
    private void genChainsList(GridPane chainsList, FlowPane textPane, List<Chain> chains) {
        chainsList.getChildren().clear();
        for (int i = 0; i < chains.size(); i++) {
            Chain c = chains.get(i);
            Button chain = new Button(c.getName());
            chain.setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                    c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
            chain.getStyleClass().add("chain");  // CSS class for styling chosen chains
            final int iF = i;
            chain.setOnAction(ev -> {
                int prev = controller.selectChain(iF);
                toggleSelected(chain, "chain");
                for (Location l : chains.get(iF).getLocations()) {
                    updateColoring(l, c, textPane);
                }
                if (prev != -1 && prev != iF) toggleSelected((Button) chainsList.getChildren().get(prev), "chain");
            });
            Tooltip fullChain = new Tooltip(c.toString());
            Tooltip.install(chain, fullChain);
            if (c.toString().toLowerCase().contains(chainFilter.toLowerCase())
                    || c.getName().toLowerCase().contains(chainFilter.toLowerCase())) chainsList.add(chain, 0, i);
        }
    }

    /**
     * Updates the text coloring according to the last action made (adding to chain or creating a new one)
     *
     * @param l    a Location describing the action of the user
     * @param c    the chain that was affected by that action
     * @param text a pane that contains the text
     */
    private void updateColoring(Location l, Chain c, FlowPane text) {
        if (l instanceof Blank) {
            text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)
                    .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                            c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                text.getChildren().get(2 * (i - displayedIndex))
                        .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                                c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
            }
        }
    }

    public void updateColoring(Action ac, Chain c, FlowPane text) {
        updateColoring(ac.getLocation(), c, text);
    }

    /**
     * Updates the text coloring if the last action was cancelled by the user.
     *
     * @param ac     an Action describing the cancelled action
     * @param text   a pane that contains the text
     * @param chains a list of chains used to determine whether words that were affected by the cancel appeared
     *               in any other chain
     */
    private void undoAction(Action ac, FlowPane text, List<Chain> chains) {
        Location l = ac.getLocation();
        if (l instanceof Blank) {
            text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)
                    .setStyle("-fx-background-color: rgba(0,0,0,0)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                Color c = chains.stream().filter(ch -> ch.getLocations().stream().filter(li -> li instanceof Phrase)
                        .map(ph -> ((Phrase) ph).getPositions()).anyMatch(s -> s.contains(i))).findAny()
                        .orElseGet(() -> new ChainImpl("", new Color(0, 0, 0), 0, new Blank(0))).getColor();
                text.getChildren().get(2 * (i - displayedIndex))
                        .setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                                c.getGreen() + "," + c.getBlue() + ((c.getRGB() == RGB_BLACK) ? ",0)" : ",0.3)"));
            }
        }
    }

    /**
     * Toggles a button's CSS class specified by the second argument. If a button had a class "type", it would become
     * "type-selected" and vice versa.
     *
     * @param button a button to toggle
     * @param type   a CSS class to toggle. Use "word", "highlight" or "search" for toggling words in the text and
     *               "chain" to toggle chain selection.
     */
    private void toggleSelected(Button button, String type) {
        if (!button.getStyleClass().filtered(s -> s.contains(type)).isEmpty()) {
            String wasSelected = button.getStyleClass().filtered(s -> s.contains(type)).get(0);
            button.getStyleClass().remove(wasSelected);
            if (wasSelected.equals(type)) button.getStyleClass().add(type + "-selected");
            else button.getStyleClass().add(type);
        }
    }

    /**
     * Removes selection from the words in the text.
     *
     * @param selected unique positions of the words that have to be toggled
     * @param text     a pane containing the text
     */
    private void removeSelectionFromText(Set<Integer> selected, FlowPane text) {
        for (Integer i : selected) toggleSelected((Button) text.getChildren().get(2 * (i - displayedIndex)), "word");
    }

    /**
     * Returns true whether cur is a start of a new sentence.
     *
     * @param prev the word before the current one
     * @param cur  the current word
     * @return true if cur is a start of a new sentence
     */
    private boolean isSentenceStart(String prev, String cur) {
        return prev.length() > 3 && Character.isUpperCase(cur.charAt(0)) &&
                (prev.endsWith(".") ||
                        prev.endsWith("?") ||
                        prev.endsWith("!"));
    }

    /**
     * Opens a new error window with a custom error message.
     *
     * @param primaryStage a stage to bind the error window to
     * @param errorMessage a custom error message to display
     */
    private void generateErrorScreen(Stage primaryStage, String errorMessage) {
        // TODO: style this properly
        Stage stage = new Stage();
        stage.setTitle("Ошибка");
        GridPane root = new GridPane();
        root.add(new Text(errorMessage), 0, 0);
        Button ok = new Button("OK");
        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });
        root.add(ok, 0, 1);
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });
        stage.setScene(new Scene(root, 320, 50));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }

    /**
     * Opens a new prompt window asking a user to input a name for a new chain.
     *
     * @param primaryStage a stage to bind the window to
     */
    private void openChainNameDialogue(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Введите название новой цепочки");
        GridPane root = new GridPane();
        root.add(new Text("Введите название новой цепочки:"), 0, 0);
        TextField name = new TextField();
        root.add(name, 0, 1);
        Button ok = new Button("OK");
        ok.setOnAction(event -> {
            if (!name.getText().isEmpty()) {
                stage.getScene().getWindow().hide();
                controller.setNewChainName(name.getText());
            }
        });
//        ok.setAlignment(Pos.CENTER);  // TODO: style this properly
        root.add(ok, 0, 2);
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });
//        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root, 190, 70));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }
}