package userInterface;

import chain.*;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;
    final private int MIN_APP_WIDTH = 700;
    final private int MIN_APP_HEIGHT = 300;
    final private int RGB_BLACK = -16777216;
    private ControllerImpl controller;
    private int selectedSentenceStart, selectedSentenceEnd, textSizeInWords;
    /**
     * A search criteria for chains. Only works on separate links in chain (i.e. a chain link
     * must contain the whole string).
     */
    private String chainFilter = "";
    private String judgePassword = "password";
    private Set<Integer> userIds;

    private JudgeInterface judgeInterface = new JudgeInterface();

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        controller = new ControllerImpl(primaryStage);
        // TODO: ask the server to send judge's password and set of users
        userIds = new HashSet<>();
        userIds.add(1);

        loginUser();

        // TODO: display either the main window if it's a user or a judge window if it's a judge


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


//        Chain first = new ChainImpl("kek", new Color(255, 0, 0), 0, new Phrase("Приехав", 0), new Phrase("Москву,", 5));
//        Chain second = new ChainImpl("mda", new Color(0, 255, 0), 0, new Phrase("Приехав", 0), new Phrase("поездом", 3));
//        controller.showConflict(new ConflictImpl(first, second, new Phrase("Москву,", 5)));
    }


    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Generates the UI for user login, which is shown before the application is started.
     */
    private void loginUser() {
        Stage stage = new Stage();
        stage.setTitle("Выберите роль");
        GridPane root = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
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
            if (controller.isJudge() || controller.isLoggedUser()) stage.getScene().getWindow().hide();
        });
        root.add(judge, 0, 0);
        root.add(user, 1, 0);
        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    /**
     * Generates the UI for judge login screen, which prompts the judge to enter the password.
     */
    private void judgeLoginScreen() {
        Stage stage = new Stage();
        stage.setTitle("Введите пароль");
        GridPane root = new GridPane();
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
        root.add(back, 0, 0);
        root.add(password, 0, 1);
        root.add(enter, 0, 2);
        root.add(error, 0, 3);
        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
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
        TextField id = new TextField();
        id.setPromptText("Введите ID пользователя...");
        GridPane.setValignment(id, VPos.CENTER);
        GridPane.setHalignment(id, HPos.CENTER);
        Button enter = new Button("Войти");
        GridPane.setValignment(enter, VPos.CENTER);
        GridPane.setHalignment(enter, HPos.CENTER);
        Text error = new Text("");
        error.setStyle("-fx-fill: red; -fx-font-size: 15pt;");
        enter.setOnAction(event -> {
            if (!id.getText().isEmpty() && userIds.contains(Integer.valueOf(id.getText()))) {
                // TODO: call to server to ensure that this user hasn't logged in yet
                stage.getScene().getWindow().hide();
                controller.loginUser(Integer.valueOf(id.getText()));
            } else {
                error.setText("Пользователя не существует!");
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
        root.add(back, 0, 0);
        root.add(id, 0, 1);
        root.add(enter, 0, 2);
        root.add(error, 0, 3);
        stage.setScene(new Scene(root, 400, 200));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
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
            genChainsList(chainsList, controller.getChains());
        });
        field.setPromptText("Введите текст для поиска в цепочках...");

        rightSide.getRowConstraints().addAll(row1, row2);
        rightSide.add(field, 0, 0);
        rightSide.add(pane, 0, 1);

        BorderPane leftSide = new BorderPane();

        /*
        A pane containing the selected text in form of buttons.
         */
        ScrollPane textWrapper = new ScrollPane();
        textWrapper.setFitToWidth(true);
        FlowPane text = new FlowPane();
        textWrapper.setContent(text);
        text.setPadding(new Insets(5));
        generateText(text);
        leftSide.setCenter(textWrapper);

        /*
        A box that contains the buttons at the top.
         */
        HBox box = new HBox();

        Button b4 = new Button("Отменить");
        b4.setOnAction(event -> {
            Action ac = controller.cancel();
            genChainsList(chainsList, controller.getChains());
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
                genChainsList(chainsList, chains);
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
                genChainsList(chainsList, chains);
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
                toggleSelected((Button) text.getChildren().get(2 * selectedBlank + 1), "word");
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
                genChainsList(chainsList, chains);
                Blank b = (Blank) ac.getLocation();
                toggleSelected((Button) text.getChildren().get(2 * b.getPosition() + 1), "word");
                ((Button) text.getChildren().get(2 * b.getPosition() + 1)).setText("@");
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
            System.out.println(fileChooser.getExtensionFilters());
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    String txt = new BufferedReader(new FileReader(file)).lines().collect(Collectors.joining(" "));
                    controller.setText(txt);
                    generateText(text);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        Button dump = new Button("Сохранить разметку в файл");
        dump.setOnAction(event -> {
            controller.saveStateOffline();
        });

        box.getChildren().addAll(b1, b2, b3, b4, spacer, fileSelect, dump);
        leftSide.setTop(box);

        /*
        A bottom pane with sentence navigation and text search.
         */
        GridPane bottom = new GridPane();

        Button left = new Button("<");
        /*
        The following action basically searches for the next occurrence of a sentence start and shifts the
        indices accordingly, while toggling the buttons styles.
         */
        left.setOnAction(event -> {
            if (selectedSentenceStart != 0) {
                for (int i = 2 * selectedSentenceStart; i <= 2 * selectedSentenceEnd; i++) {
                    toggleSelected((Button) text.getChildren().get(i), "highlight");
                }
                selectedSentenceEnd = selectedSentenceStart - 1;
                selectedSentenceStart--;
                while (selectedSentenceStart != 0 &&
                        !isSentenceStart(((Button) text.getChildren().get(2 * selectedSentenceStart - 2)).getText(),
                                ((Button) text.getChildren().get(2 * selectedSentenceStart)).getText())) {
                    selectedSentenceStart--;
                }
                for (int i = 2 * selectedSentenceStart; i <= 2 * selectedSentenceEnd; i++) {
                    toggleSelected((Button) text.getChildren().get(i), "highlight");
                }
            }
        });

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
            if (selectedSentenceEnd != textSizeInWords - 1) {
                for (int i = 2 * selectedSentenceStart; i <= 2 * selectedSentenceEnd; i++) {
                    toggleSelected((Button) text.getChildren().get(i), "highlight");
                }
                selectedSentenceStart = selectedSentenceEnd + 1;
                selectedSentenceEnd++;
                while (selectedSentenceEnd != textSizeInWords - 1 &&
                        !isSentenceStart(((Button) text.getChildren().get(2 * selectedSentenceEnd)).getText(),
                                ((Button) text.getChildren().get(2 * selectedSentenceEnd + 2)).getText())) {
                    selectedSentenceEnd++;
                }
                for (int i = 2 * selectedSentenceStart; i <= 2 * selectedSentenceEnd; i++) {
                    toggleSelected((Button) text.getChildren().get(i), "highlight");
                }
            }
        });

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        bottom.getColumnConstraints().addAll(col1, col2, col3);
        bottom.add(left, 0, 0);
        bottom.add(textField, 1, 0);
        bottom.add(right, 2, 0);

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
    private void generateText(FlowPane textPane) {
        String text = controller.getText();
        textPane.getChildren().clear();  // in case this method is called multiple times (which shouldn't really happen)
        // TODO: should probably remove punctuation from buttons into separate TextAreas
        String[] words = text.split(" ");
        textSizeInWords = words.length;
        boolean firstSentence = true;
        selectedSentenceStart = 0;
        for (int i = 0; i < words.length; i++) {
            Button word = new Button(words[i]);
            word.getStyleClass().add("word");  // CSS class for styling words and their selection
            word.getStyleClass().add("search");  // CSS class for styling text search
            word.setStyle("-fx-background-color: rgba(0,0,0,0);");
            if (i != 0 && firstSentence) {
                if (isSentenceStart(words[i - 1], words[i])) {
                    firstSentence = false;
                    selectedSentenceEnd = i - 1;
                }
            }
            if (firstSentence) {
                word.getStyleClass().add("highlight-selected");  // CSS class for styling sentence selection
            } else {
                word.getStyleClass().add("highlight");
            }
            final int iF = i;
            word.setOnAction(event -> {
                if (controller.pressedButton(words[iF], iF)) toggleSelected(word, "word");
            });
            textPane.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            space.setOnAction(event -> {
                if (controller.pressedButton("   ", iF)) toggleSelected(space, "word");
            });
            if (firstSentence) space.getStyleClass().add("highlight-selected");
            else {
                space.getStyleClass().add("highlight");
            }
            textPane.getChildren().add(space);
        }
    }

    /**
     * Generates a list of buttons representing the chains, filters them and puts into a chosen pane.
     *
     * @param chainsList a pane to put buttons into
     * @param chains     a list of chains
     */
    private void genChainsList(GridPane chainsList, List<Chain> chains) {
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
     * @param ac   an Action describing the last action of the user
     * @param c    the chain that was affected by that action
     * @param text a pane that contains the text
     */
    private void updateColoring(Action ac, Chain c, FlowPane text) {
        Location l = ac.getLocation();
        if (l instanceof Blank) {
            text.getChildren().get(2 * ((Blank) l).getPosition() + 1)
                    .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                            c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                text.getChildren().get(2 * i)
                        .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                                c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.3)");
            }
        }
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
            text.getChildren().get(2 * ((Blank) l).getPosition() + 1)
                    .setStyle("-fx-background-color: rgba(0,0,0,0)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                Color c = chains.stream().filter(ch -> ch.getLocations().stream().filter(li -> li instanceof Phrase)
                        .map(ph -> ((Phrase) ph).getPositions()).anyMatch(s -> s.contains(i))).findAny()
                        .orElseGet(() -> new ChainImpl("", new Color(0, 0, 0), 0, new Blank(0))).getColor();
                text.getChildren().get(2 * i)
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
        for (Integer i : selected) toggleSelected((Button) text.getChildren().get(2 * i), "word");
    }

    /**
     * Returns true whether cur is a start of a new sentence.
     *
     * @param prev the word before the current one
     * @param cur  the current word
     * @return true if cur is a start of a new sentence
     */
    private boolean isSentenceStart(String prev, String cur) {
        return Character.isUpperCase(cur.charAt(0)) &&
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
