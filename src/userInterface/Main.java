package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import client.User;
import document.Data;
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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main extends Application {

    private User user;
    final private int APP_WIDTH = 800;
    final private int APP_HEIGHT = 600;
    final private int RGB_BLACK = -16777216;
    private ControllerImpl controller;
    private int selectedSentenceStart = 0, selectedSentenceEnd = 0, textSizeInWords, displayedIndex, unsentSentences = 0;
    private String[] words = null;
    private boolean checkSentences = true;
    private Stage primaryStage;
    /**
     * A search criteria for chains. Only works on separate links in chain (i.e. a chain link
     * must contain the whole string).
     */
    private String chainFilter = "";


    @Override
    public void start(Stage primaryStage) {
        controller = new ControllerImpl(primaryStage);

        this.primaryStage = primaryStage;
        userLoginScreen();
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
            genScene();
            stage.getScene().getWindow().hide();
        });

        enterOnline.setOnAction(event -> {
            String login = id.getText();
            id.clear();
            user = new User(login, 3333, "localhost");
            int ans = user.joinOnline();
            if (ans == 2) {
                generateErrorScreen(stage, "Пользователь с таким ID уже авторизован!");
            } else if (ans == 3) {
                generateErrorScreen(stage, "Не удалось подключиться к серверу");
            } else {
                stage.getScene().getWindow().hide();
                Platform.runLater(() -> {
                    genScene();
                    if (ans == 0) {
                        controller.onlineMode();
                        words = null;
                        controller.setText(user.getText());
                    } else if (ans == 1) {
                        controller.onlineMode();
                        Data data = user.getData();
                        words = null;
                        controller.restoreState(data.getText(), data.getActions());
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

    private void addTooltip(Node node, String tooltipText) {
        Tooltip t = new Tooltip(tooltipText);
        Tooltip.install(node, t);
    }

    /**
     * Generates the main UI window for the app.
     *
     *
     * @return the scene
     */
    private void genScene() {

        primaryStage.setOnCloseRequest(event ->

        {
            user.kill();
            primaryStage.getScene().getWindow().hide();
        });
        //buttons and words search
        HBox buttons = new HBox(1);
        buttons.getStyleClass().add("default-background");

        Button nnew = genButton("new");
        addTooltip(nnew, "Новая цепочка");
        Button add = genButton("plus");
        addTooltip(add, "Продолжить цепочку");
        Button empty = genButton("empty");
        addTooltip(empty, "Добавить нулевую анафору");
        Button undo = genButton("undo");
        addTooltip(undo, "Отменить");
        Button del = genButton("delete");
        addTooltip(del, "Удалить цепочку или ее фрагмент");
        Button fileSelect = genButton("download");
        addTooltip(fileSelect, "Открыть текстовый файл");
        Button dump = genButton("upload");
        addTooltip(dump, "Сохранить разметку в файл");
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
        addTooltip(left, "Предыдущее предложение");
        Button right = genButton("next");
        addTooltip(right, "Следующее предложение");

        search.getChildren().addAll(textField, left, right);
        GridPane.setValignment(search, VPos.CENTER);
        GridPane.setHalignment(search, HPos.RIGHT);
        GridPane.setFillWidth(search, false);
        GridPane.setFillHeight(search, false);

        GridPane buttonsSearch = new GridPane();
        {
            //TODO make easier to change parameters
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
        Pane spacer = new Pane();
        GridPane.setHgrow(spacer, Priority.ALWAYS);
        buttonsSearch.add(spacer, 1, 0);
        buttonsSearch.add(search, 2, 0);
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

            RowConstraints row1 = new RowConstraints(0.1 * APP_HEIGHT);
            row1.setFillHeight(true);
            row1.setVgrow(Priority.NEVER);

            RowConstraints row2 = new RowConstraints();
            row2.setVgrow(Priority.ALWAYS);

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
        primaryStage.setOnCloseRequest(event -> {
            sc.getWindow().hide();
        });
        primaryStage.setMinWidth(0.8 * APP_WIDTH);
        primaryStage.setMinHeight(0.8 * APP_HEIGHT);
        primaryStage.setScene(sc);
        primaryStage.show();

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

        //TODO let user close chainNameDialog and create chain only after name selection and confirm
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
                updateColoring(ac, chains.get(0), text);
                undo.setDisable(false);
            }
        });

        del.setOnAction(event -> {
            int selectedBlank = controller.getSelectedBlank();
            Set<Integer> selected = controller.getSelected();
            int selectedChain = controller.getSelectedChain();
            if (selectedBlank == -1 && selectedChain == -1 && selected.isEmpty()) {
                generateErrorScreen(primaryStage, "Не выбрана комбинация слов/цепочка!");
                return;
            }
            List<Chain> chains = controller.getChains();
            if (selectedChain != -1) {
                Chain c = controller.deleteChain();
                removeChainColoring(c, text, chains);
            } else {
                Action ac = controller.deletePhrase();
                if (ac == null) {
                    generateErrorScreen(primaryStage, "Выделенные слова не присутствуют в цепочках!");
                } else {
                    if (selectedBlank != -1) {
                        controller.pressedButton(" ", selectedBlank);  // sets selectedBlank to -1
                        toggleSelected((Button) text.getChildren().get(2 * (selectedBlank - displayedIndex) + 1), "word");
                    } else {
                        removeSelectionFromText(selected, text);
                    }
                    removeColoringFromLocation(ac.getLocation(), text, chains);
                }
            }
            genChainsList(chainsList, text, chains);
            undo.setDisable(false);
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
                    displayedIndex = 0;
                    selectedSentenceEnd = 0;
                    selectedSentenceStart = 0;
                    controller.clearSelected();
                    controller.pressedButton(" ", controller.getSelectedBlank()); // sets selectedBlank to -1
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
            if (!controller.getActions().isEmpty() && controller.isOnline())
                while (user.sendUpdates(controller.getActions()) != 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            controller.saveStateOffline();
            unsentSentences = 0;
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
                controller.clearSelected();
                controller.pressedButton(" ", controller.getSelectedBlank()); // sets selectedBlank to -1
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
                    if (!controller.getActions().isEmpty() && controller.isOnline())
                        while (user.sendUpdates(controller.getActions()) != 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
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
//                controller.clearSelected();
//                controller.pressedButton(" ", controller.getSelectedBlank()); // sets selectedBlank to -1
            } else {
                if (controller.isOnline()) {
                    user.close(controller.getActions());
                    controller.clearActions();
                }
            }
        });

        undo.setOnAction(event -> {
            Action ac = controller.cancel();
            genChainsList(chainsList, text, controller.getChains());
            undoAction(ac, text, controller.getChains(), left, right);
            int remaining = controller.getPrevStatesSize();
            if (remaining == 0) undo.setDisable(true);
        });

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
                ((Button) text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1))
                        .setText("@");
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
        Integer from = undoActionNoSentences(ac, text, chains);
        if (from == -1) return;
        while (from < selectedSentenceStart) {
            backBtn.fire();
        }
        checkSentences = false;
        while (from > selectedSentenceEnd) {
            forwardBtn.fire();
        }
        checkSentences = true;
    }

    private int removeColoringFromLocation(Location l, FlowPane text, List<Chain> chains) {
        Integer from = 0;
        if (l instanceof Blank) {
            if (2 * (((Blank) l).getPosition() - displayedIndex) + 1 >= 0
                    && 2 * (((Blank) l).getPosition() - displayedIndex) + 1 < text.getChildren().size()) {
                text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)
                        .setStyle("-fx-background-color: rgba(0,0,0,0)");
                ((Button) text.getChildren().get(2 * (((Blank) l).getPosition() - displayedIndex) + 1)).setText("   ");
            }
            from = ((Blank) l).getPosition();
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                Color c = getOtherColorIfPresent(chains, i);
                if (2 * (i - displayedIndex) >= 0 && 2 * (i - displayedIndex) < text.getChildren().size()) {
                    text.getChildren().get(2 * (i - displayedIndex))
                            .setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                                    c.getGreen() + "," + c.getBlue() + ((c.getRGB() == RGB_BLACK) ? ",0)" : ",0.3)"));
                }
            }
            from = pos.iterator().next();
        }
        return from;
    }

    private int undoActionNoSentences(Action ac, FlowPane text, List<Chain> chains) {
        if (ac.getAction() == ControllerImpl.ADDWORD || ac.getAction() == ControllerImpl.ADDCHAIN) {
            return removeColoringFromLocation(ac.getLocation(), text, chains);
        } else if (ac.getAction() == ControllerImpl.DELWORD) {
            Chain to = chains.stream().filter(c -> c.getId() == ac.getChainId()).findAny().orElse(null);
            if (to == null) return -1;
            updateColoring(ac.getLocation(), to, text);
        } else if (ac.getAction() == ControllerImpl.DELCHAIN) {
            Chain restored = chains.stream().filter(c -> c.getId() == ac.getChainId()).findAny().orElse(null);
            if (restored == null) return -1;
            for (Location l : restored.getLocations()) {
                updateColoring(l, restored, text);
            }
        }
        return -1;
    }

    private Color getOtherColorIfPresent(List<Chain> chains, Integer pos) {
        return chains.stream().filter(ch -> ch.getLocations().stream().filter(li -> li instanceof Phrase)
                .map(ph -> ((Phrase) ph).getPositions()).anyMatch(s -> s.contains(pos))).findAny()
                .orElseGet(() -> new ChainImpl("", new Color(0, 0, 0), 0, new Blank(0))).getColor();
    }

    private void removeChainColoring(Chain c, FlowPane text, List<Chain> chains) {
        for (Location l : c.getLocations()) {
            removeColoringFromLocation(l, text, chains);
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
        return prev.length() > 3 && (Character.isUpperCase(cur.charAt(0)) || Character.isDigit(cur.charAt(0))) &&
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
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }
}