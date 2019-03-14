package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import client.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
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
    private int selectedSentenceStart = 0, selectedSentenceEnd = 0, textSizeInWords, displayedIndex, unsentSentences = 0;
    private String[] words = null;
    private boolean checkSentences = true;
    /**
     * A search criteria for chains. Only works on separate links in chain (i.e. a chain link
     * must contain the whole string).
     */
    private String chainFilter = "";

    private JudgeInterface judgeInterface = new JudgeInterface();

    @Override
    public void start(Stage primaryStage) {
        controller = new ControllerImpl(primaryStage);

        loginUser();

        if (controller.isJudge()) {
            judgeInterface.start(primaryStage);
        } else {
            primaryStage.setTitle("Разметка кореференсов");
            Scene sc = genScene(primaryStage);
            primaryStage.setMinWidth(MIN_APP_WIDTH);
            primaryStage.setMinHeight(MIN_APP_HEIGHT);
            primaryStage.setScene(sc);
            primaryStage.show();
        }
    }


    public static void main(String[] args) {
        launch(args);
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

    private void loginUser() {
        Stage stage = new Stage();
        stage.setTitle("Выберите роль");
        GridPane root = new GridPane();

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");

        root.getColumnConstraints().addAll(
                makeColFromPercent(10),
                makeColFromPercent(35),
                makeColFromPercent(10),
                makeColFromPercent(35),
                makeColFromPercent(10)
        );

        root.getRowConstraints().addAll(
                makeRowFromPercent(30),
                makeRowFromPercent(40),
                makeRowFromPercent(30)
        );

        Button judge = new Button("Войти как судья");
        Button user = new Button("Войти как пользователь");
        judge.getStyleClass().add("button-font");
        user.getStyleClass().add("button-font");

        GridPane.setValignment(judge, VPos.CENTER);
        GridPane.setHalignment(judge, HPos.CENTER);

        GridPane.setValignment(user, VPos.CENTER);
        GridPane.setHalignment(user, HPos.CENTER);

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
                controller.loginJudge();
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

    /**
     * Generates the UI for user login screen, which prompts the user to enter an ID.
     */
    private void userLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите ID пользователя");

        GridPane root = baseUserPart();
        GridPane subRoot = baseUserSubPart();

        TextField id = new TextField();
        id.setPromptText("Введите ID пользователя...");

        GridPane.setValignment(id, VPos.CENTER);
        GridPane.setHalignment(id, HPos.CENTER);

        Button enter = new Button("Войти");
        Button back = new Button("Назад");
        Button enterOffline = new Button("Войти оффлайн");

        enter.getStyleClass().add("button-font");
        back.getStyleClass().add("button-font");
        enterOffline.getStyleClass().add("button-font");

        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);


        GridPane.setValignment(back, VPos.CENTER);
        GridPane.setHalignment(back, HPos.CENTER);

        GridPane.setValignment(enterOffline, VPos.CENTER);
        GridPane.setHalignment(enterOffline, HPos.CENTER);

        enterOffline.setOnAction(event -> {
            controller.offlineMode();
            stage.getScene().getWindow().hide();
        });

        Text error = new Text("");
        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");

        enter.setOnAction(event -> {
            // TODO: call to server
            Integer ID = 0;
            stage.getScene().getWindow().hide();
            Platform.runLater(() -> {
                controller.setText(controller.simpleText());
            });
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event ->

        {
            if (event.getCode() == KeyCode.ENTER) {
                enter.fire();
                event.consume();
            }
        });

        back.setOnAction(event ->

        {
            stage.getScene().getWindow().hide();
        });


        subRoot.add(enter, 0, 1, 2, 1);
        subRoot.add(back, 3, 1, 2, 1);
        subRoot.add(enterOffline, 0, 2, 5, 1);
        root.add(id, 1, 1);
        root.add(error, 0, 2);
        root.add(subRoot, 1, 3);
        stage.setScene(new

                Scene(root, 400, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(event ->

        {
            stage.getScene().getWindow().hide();
        });
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    private Button genButton(String image) {
        ImageView imageView = new ImageView(image + ".png");
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        Button button = new Button("", imageView);
        button.setMinSize(20, 20);
        button.setStyle("-fx-background-color: burlywood");
        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        return button;
    }

    /**
     * Generates the main UI window for the app.
     *
     * @param primaryStage stage to bind the scene to
     * @return the scene
     */
    private Scene genScene(Stage primaryStage) {
        //buttons and words search
        HBox buttons = new HBox(1);
        buttons.getStyleClass().add("default-background");

        Button nnew = genButton("new");
        Button add = genButton("plus");
        Button empty = genButton("empty");
        Button undo = genButton("undo");
        Button del = genButton("delete");
        Button fileSelect = genButton("download");
        Button dump = genButton("upload");
        undo.setDisable(true);

        buttons.getChildren().addAll(nnew, add, empty, undo, del, fileSelect, dump);

        GridPane.setFillWidth(buttons, false);
        GridPane.setFillHeight(buttons, false);
        GridPane.setHalignment(buttons, HPos.LEFT);
        GridPane.setValignment(buttons, VPos.CENTER);

        HBox search = new HBox(5);

        TextField textField = new TextField();
        textField.setPromptText("Введите слово для поиска...");
        Button left = genButton("prev");
        Button right = genButton("next");

        search.getChildren().addAll(textField, left, right);
        GridPane.setValignment(search, VPos.CENTER);
        GridPane.setHalignment(search, HPos.RIGHT);
        GridPane.setFillWidth(search, false);
        GridPane.setFillHeight(search, false);

        GridPane buttonsSearch = new GridPane();
        {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setMinWidth(buttons.getMinWidth());


            ColumnConstraints col3 = new ColumnConstraints();
            col3.setMinWidth(search.getMinWidth());

            buttonsSearch.getColumnConstraints().addAll(
                    col1,
                    col3
            );

            RowConstraints row1 = new RowConstraints();
            row1.setPercentHeight(100);

            buttonsSearch.getRowConstraints().add(row1);
        }

        buttonsSearch.add(buttons, 0, 0);
        buttonsSearch.add(search, 1, 0);
        GridPane.setMargin(buttonsSearch, new Insets(10));
        GridPane.setFillWidth(buttonsSearch, true);

        //text
        ScrollPane textWrapper = new ScrollPane();
        textWrapper.setFitToWidth(true);
        textWrapper.setFitToHeight(true);
        textWrapper.setPadding(new Insets(20, 20, 20, 20));
        FlowPane text = new FlowPane();
        textWrapper.setContent(text);
        generateText(text, textWrapper);
        textWrapper.getStyleClass().add("default-outline");
        GridPane.setMargin(textWrapper, new Insets(10));

        //chains
        ScrollPane chainsField = new ScrollPane();
        GridPane chainsList = new GridPane();
        chainsField.setContent(chainsList);
        chainsField.prefWidthProperty().bind(primaryStage.widthProperty().divide(4));
        GridPane.setMargin(chainsField, new Insets(10));
        chainsField.getStyleClass().add("default-outline");

        //chains search
        TextField field = new TextField();
        field.setPromptText("Введите текст для поиска в цепочках...");
        GridPane.setHalignment(field, HPos.LEFT);
        GridPane.setValignment(field, VPos.CENTER);
        GridPane.setFillWidth(field, false);
        GridPane.setMargin(field, new Insets(10));

        GridPane overall = new GridPane();
        overall.getStylesheets().add("styles.css");
        overall.getStyleClass().add("default-background");
        {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(70);
            col1.setMinWidth(buttonsSearch.getMinWidth());
            col1.setFillWidth(true);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(30);
            col2.setFillWidth(true);

            overall.getColumnConstraints().addAll(
                    col1,
                    col2
            );

            RowConstraints row1 = new RowConstraints(0.1 * APP_HEIGHT, 0.1 * APP_HEIGHT, 0.1 * APP_HEIGHT);
            row1.setFillHeight(true);
            row1.setVgrow(Priority.NEVER);

            RowConstraints row2 = new RowConstraints();
            row2.setPercentHeight(90);
            row2.setFillHeight(true);

            overall.getRowConstraints().addAll(
                    row1,
                    row2
            );
        }

        overall.add(field, 1, 0);
        overall.add(buttonsSearch, 0, 0);
        overall.add(textWrapper, 0, 1);
        overall.add(chainsField, 1, 1);
        Scene sc = new Scene(overall, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");

        //if (controller.isOnline()) fileSelect.setVisible(false);


        field.textProperty().addListener((observable, oldValue, newValue) -> {
            chainFilter = newValue;
            genChainsList(chainsList, text, controller.getChains());
        });

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
        The event handler used to generate the text when restoring the state of the program.
         */
        primaryStage.addEventHandler(ControllerImpl.MoveSelectedSentenceEvent.MOVE_SELECTED_SENTENCE,
                event -> {
                    while (selectedSentenceStart < event.id) right.fire();
                    event.consume();
                });

        add.setOnAction(event -> {
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
                undo.setDisable(false);
            }
        });

        nnew.setOnAction(event -> {
            Set<Integer> selected = controller.getSelected();
            int selectedBlank = controller.getSelectedBlank();
            if (selected.isEmpty() && selectedBlank == -1) {
                generateErrorScreen(primaryStage, "Не выбрано ни одно слово/пробел!");
            } else {
                if (!selected.isEmpty() && controller.isSelectedAlreadyBound()) {
                    generateErrorScreen(primaryStage, "Выбранные вами слова уже добавлены в другую цепочку!");
                    controller.clearSelected();
                    removeSelectionFromText(selected, text);
                    return;
                }
                if (selectedBlank != -1 && controller.isSelectedBlankAlreadyBound()) {
                    generateErrorScreen(primaryStage, "Выбранные вами слова уже добавлены в другую цепочку!");
                    controller.pressedButton(" ", selectedBlank);  // sets selectedBlank to -1
                    toggleSelected((Button) text.getChildren().get(2 * (selectedBlank - displayedIndex) + 1), "word");
                    return;
                }
                openChainNameDialogue(primaryStage);
            }
            Action ac = controller.addNewChain();
            if (ac != null) {
                List<Chain> chains = controller.getChains();
                genChainsList(chainsList, text, chains);
                updateColoring(ac, chains.get(0), text);
                if (selected.isEmpty()) {
                    ((Button) text.getChildren().get(2 * (selectedBlank - displayedIndex) + 1)).setText("@");
                    toggleSelected((Button) text.getChildren().get(2 * (selectedBlank - displayedIndex) + 1), "word");
                } else {
                    removeSelectionFromText(selected, text);
                }
                undo.setDisable(false);
            }
        });

        empty.setOnAction(event -> {
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
                undo.setDisable(false);
            }
        });

        fileSelect.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    String txt = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8)).lines().collect(Collectors.joining(". "));
                    txt = txt.replaceAll("\\s+", " ").replaceAll("\\.+", ".").replaceAll("(\\. )+", ". ");
                    words = null;
                    controller.setText(txt);
                    unsentSentences = 0;
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

        dump.setOnAction(event -> {
            unsentSentences = 0;
            controller.saveStateOffline();
            undo.setDisable(true);
        });

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
        left.setOnAction(event -> {
            if (selectedSentenceStart != 0) {
                selectedSentenceStart--;
                while (selectedSentenceStart != 0 &&
                        !isSentenceStart(words[selectedSentenceStart - 1],
                                words[selectedSentenceStart])) {
                    selectedSentenceStart--;
                }
                selectedSentenceEnd = selectedSentenceStart;
                generateText(text, textWrapper);
            }
        });

        /*
        The following action basically searches for the next occurrence of a sentence start and shifts the
        indices accordingly, while toggling the buttons styles.
         */
        right.setOnAction(event -> {
            if (checkSentences) {
                unsentSentences++;
                if (unsentSentences >= 5) {
                    controller.saveStateOffline();
                    unsentSentences = 0;
                    undo.setDisable(true);
                }
            }
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
            } else {
                if (controller.isOnline()) {
                    user.close(controller.getActions());
                    controller.clearActions();
                }
            }
            System.out.println(selectedSentenceStart + "   " + selectedSentenceEnd);
        });

        undo.setOnAction(event -> {
            Action ac = controller.cancel();
            genChainsList(chainsList, text, controller.getChains());
            undoAction(ac, text, controller.getChains(), left, right);
            int remaining = controller.getPrevStatesSize();
            if (remaining == 0) undo.setDisable(true);
        });


        return sc;
    }

    /**
     * Generates a list of buttons containing the text and puts them into a chosen pane.
     *
     * @param textPane a pane to put the text buttons into
     */
    private void generateText(FlowPane textPane, ScrollPane outer) {
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
        if (words == null) {
            String text = controller.getText();
            words = text.split(" ");
            textSizeInWords = words.length;
        }
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
            fullChain.setWrapText(true);
            fullChain.setPrefWidth(550);
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
            if (2 * ((((Blank) l).getPosition() - displayedIndex)) >= 0
                    && 2 * ((((Blank) l).getPosition() - displayedIndex)) < text.getChildren().size()) {
                text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)
                        .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                                c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
            }
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                if (2 * (i - displayedIndex) >= 0 && 2 * (i - displayedIndex) < text.getChildren().size()) {
                    text.getChildren().get(2 * (i - displayedIndex))
                            .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                                    c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
                }
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
    private void undoAction(Action ac, FlowPane text, List<Chain> chains, Button backBtn, Button forwardBtn) {
        Location l = ac.getLocation();
        Integer from = 0;
        if (l instanceof Blank) {
            text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)
                    .setStyle("-fx-background-color: rgba(0,0,0,0)");
            ((Button) text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)).setText("   ");
            from = ((Blank) l).getPosition();
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
            from = pos.iterator().next();
        }
        while (from < selectedSentenceStart) {
            backBtn.fire();
        }
        checkSentences = false;
        while (from > selectedSentenceEnd) {
            forwardBtn.fire();
        }
        checkSentences = true;
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

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");
        root.getRowConstraints().addAll(
                makeRowFromPercent(50),
                makeRowFromPercent(50)
        );

        root.getColumnConstraints().addAll(
                makeColFromPercent(20),
                makeColFromPercent(60),
                makeColFromPercent(20)
        );


        Text text = new Text(errorMessage);
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.CENTER);


        Button ok = new Button("OK");
        GridPane.setValignment(ok, VPos.CENTER);
        GridPane.setHalignment(ok, HPos.CENTER);
        ok.getStyleClass().add("button-font");

        root.add(text, 1, 0);
        root.add(ok, 1, 1);

        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });

        stage.setScene(new Scene(root, 320, 70));
        stage.setResizable(false);
//        stage.setOnCloseRequest(Event::consume);
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
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");

        root.getRowConstraints().addAll(
                makeRowFromPercent(35),
                makeRowFromPercent(35),
                makeRowFromPercent(30)
        );

        root.getColumnConstraints().addAll(
                makeColFromPercent(20),
                makeColFromPercent(60),
                makeColFromPercent(20)
        );

        //text
        Text text = new Text("Введите название новой цепочки");
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.CENTER);


        //text field
        TextField name = new TextField();
        GridPane.setHalignment(name, HPos.CENTER);
        GridPane.setValignment(name, VPos.CENTER);

        //ok
        Button ok = new Button("OK");
        ok.getStyleClass().add("button-font");
        GridPane.setValignment(ok, VPos.CENTER);
        GridPane.setHalignment(ok, HPos.CENTER);

        root.add(text, 1, 0);
        root.add(name, 1, 1);
        root.add(ok, 1, 2);


        ok.setOnAction(event -> {
            if (!name.getText().isEmpty()) {
                stage.getScene().getWindow().hide();
                controller.setNewChainName(name.getText());
            }
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });

        stage.setScene(new Scene(root, 300, 120));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }
}