package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.Location;
import chain.Phrase;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Set;

public class Main extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;
    final private int MIN_APP_WIDTH = 700;
    final private int MIN_APP_HEIGHT = 300;
    final private String TEXT_PLACEHOLDER = "Приехав с утренним поездом в Москву, Левин остановился у своего " +
            "старшего брата по матери Кознышева и, переодевшись, вошел к нему в кабинет, намереваясь тотчас же " +
            "рассказать ему, для чего он приехал, и просить его совета: но брат был не один. У него сидел известный " +
            "профессор философии, приехавший из Харькова, собственно, затем, чтобы разъяснить недоразумение, " +
            "возникшее между ними по весьма важному философскому вопросу. Профессор вел жаркую полемику против " +
            "материалистов, а Сергей Кознышев с интересом следил за этою полемикой и, прочтя последнюю статью " +
            "профессора, написал ему в письме свои возражения; он упрекал профессора за слишком большие уступки " +
            "материалистам. И профессор тотчас же приехал, чтобы столковаться. Речь шла о модном вопросе: есть ли " +
            "граница между психическими и физиологическими явлениями в деятельности человека и где она?";
    final ControllerImpl controller = new ControllerImpl(TEXT_PLACEHOLDER);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Разметка кореференсов");
        Scene sc = genScene(primaryStage);
        primaryStage.setMinWidth(MIN_APP_WIDTH);
        primaryStage.setMinHeight(MIN_APP_HEIGHT);
        primaryStage.setScene(sc);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    private Scene genScene(Stage primaryStage) {
        GridPane overall = new GridPane();
        {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
//            col1.setFillWidth(true);  // looks like we can omit this
            ColumnConstraints col2 = new ColumnConstraints();
            overall.getColumnConstraints().addAll(col1, col2);
        }
        GridPane rightSide = new GridPane();

        TextField field = new TextField();
        ScrollPane pane = new ScrollPane();
        pane.prefWidthProperty().bind(primaryStage.widthProperty().divide(4));
        GridPane chainsList = new GridPane();
        pane.setContent(chainsList);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
//            row2.setFillHeight(true);  // looks like we can omit this
        rightSide.getRowConstraints().addAll(row1, row2);
        rightSide.add(field, 0, 0);
        rightSide.add(pane, 0, 1);
        rightSide.setGridLinesVisible(true);

        BorderPane leftSide = new BorderPane();
        {
            ScrollPane textWrapper = new ScrollPane();
            textWrapper.setFitToWidth(true);
            FlowPane text = new FlowPane();
            textWrapper.setContent(text);
            text.setPadding(new Insets(5));
            generateText(text);
            leftSide.setCenter(textWrapper);

            HBox box = new HBox(5);
            Button b4 = new Button("Отменить");
            b4.setOnAction(event -> {
                Action ac = controller.cancel();
                genChainsList(chainsList, controller.getChains());
                undoAction(ac, text);
                int remaining = controller.getPrevStatesSize();
                if (remaining == 0) b4.setDisable(true);
            });
            b4.setDisable(true);
            Button b1 = new Button("Продолжить цепочку");
            b1.setOnAction(event -> {
                Set<Integer> selected = controller.getSelected();
                if (controller.isSelectedAlreadyBound()) {
                    openSelectedAlreadyBoundError(primaryStage);
                    controller.clearSelected();
                    removeSelectionFromText(selected, text);
                    return;
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
                    openSelectedAlreadyBoundError(primaryStage);
                    controller.clearSelected();
                    removeSelectionFromText(selected, text);
                    return;
                }
                if (!selected.isEmpty()) {
                    openChainNameDialogue(primaryStage);
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
                    openSelectedAlreadyBoundError(primaryStage);
                    controller.pressedButton(" ", selectedBlank);  // sets selectedBlank to -1
                    toggleSelected((Button) text.getChildren().get(2 * selectedBlank + 1), "word");
                    return;
                }
                Action ac = controller.addAnaphoraToChain();
                if (ac != null) {
                    List<Chain> chains = controller.getChains();
                    genChainsList(chainsList, chains);
                    Blank b = (Blank) ac.getLocation();
                    toggleSelected((Button) text.getChildren().get(2 * b.getPosition() + 1), "word");
                    updateColoring(ac, chains.get(0), text);
                    b4.setDisable(false);
                }
            });
            box.getChildren().addAll(b1, b2, b3, b4);
            leftSide.setTop(box);

            GridPane bottom = new GridPane();
            Button left = new Button("<");
            TextField textField = new TextField();
            textField.setPromptText("Введите слово для поиска...");
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!oldValue.isEmpty()) {
                }
                System.out.println("textfield changed from " + oldValue + " to " + newValue);
            });
            Button right = new Button(">");
            ColumnConstraints col1 = new ColumnConstraints();
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
//                col2.setFillWidth(true);  // looks like we can omit this
            ColumnConstraints col3 = new ColumnConstraints();
            bottom.getColumnConstraints().addAll(col1, col2, col3);
            bottom.add(left, 0, 0);
            bottom.add(textField, 1, 0);
            bottom.add(right, 2, 0);

            leftSide.setBottom(bottom);
        }
        leftSide.prefHeightProperty().bind(primaryStage.heightProperty());
        overall.add(leftSide, 0, 0);
        overall.add(rightSide, 1, 0);
        Scene sc = new Scene(overall, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        return sc;
    }

    private void generateText(FlowPane textPane) {
        String text = TEXT_PLACEHOLDER;
        textPane.getChildren().clear();
        // TODO: should probably remove punctuation from buttons into separate TextAreas
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            Button word = new Button(words[i]);
            word.getStyleClass().add("word");
            word.setStyle("-fx-background-color: rgba(0,0,0,0); -fx-text-fill: rgba(0,0,0,1);");
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
            textPane.getChildren().add(space);
        }
    }

    private void genChainsList(GridPane chainsList, List<Chain> chains) {
        chainsList.getChildren().clear();
        for (int i = 0; i < chains.size(); i++) {
            Chain c = chains.get(i);
            Button chain = new Button(c.getName());
            chain.setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                    c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.1)");
            chain.getStyleClass().add("chain");
            final int iF = i;
            chain.setOnAction(ev -> {
                int prev = controller.selectChain(iF);
                toggleSelected(chain, "chain");
                if (prev != -1) toggleSelected((Button) chainsList.getChildren().get(prev), "chain");
            });
            Tooltip fullChain = new Tooltip(c.toString());
            Tooltip.install(chain, fullChain);
            chainsList.add(chain, 0, i);
        }
    }

    private void updateColoring(Action ac, Chain c, FlowPane text) {
        Location l = ac.getLocation();
        if (l instanceof Blank) {
            text.getChildren().get(2 * ((Blank) l).getPosition() + 1)
                    .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                            c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.1)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                text.getChildren().get(2 * i)
                        .setStyle("-fx-background-color: rgba(" + c.getColor().getRed() + "," +
                                c.getColor().getGreen() + "," + c.getColor().getBlue() + ",0.1)");
            }
        }
    }

    private void undoAction(Action ac, FlowPane text) {
        Location l = ac.getLocation();
        if (l instanceof Blank) {
            text.getChildren().get(2 * ((Blank) l).getPosition() + 1)
                    .setStyle("-fx-background-color: rgba(0,0,0,0)");
        } else if (l instanceof Phrase) {
            Set<Integer> pos = ((Phrase) l).getPositions();
            for (Integer i : pos) {
                text.getChildren().get(2 * i)
                        .setStyle("-fx-background-color: rgba(0,0,0,0)");
            }
        }
    }

    private void toggleSelected(Button button, String type) {  // word for words, chain for chains
        String wasSelected = button.getStyleClass().get(1);
        button.getStyleClass().remove(1); // 0 - button, 1 - word/word-selected
        if (wasSelected.equals(type)) button.getStyleClass().add(type + "-selected");
        else button.getStyleClass().add(type);
    }

    private void removeSelectionFromText(Set<Integer> selected, FlowPane text) {
        for (Integer i : selected) toggleSelected((Button) text.getChildren().get(2 * i), "word");
    }

    private void openSelectedAlreadyBoundError(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Ошибка");
        GridPane root = new GridPane();
        root.add(new Text("Выбранные вами слова уже добавлены в другую цепочку!"), 0, 0);
        Button ok = new Button("OK");
        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });
        root.add(ok, 0, 1);
        stage.setScene(new Scene(root, 320, 50));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }

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
//        ok.setAlignment(Pos.CENTER);
        root.add(ok, 0, 2);
//        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root, 190, 70));
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
//        while (stage.isShowing()) {}
    }
}
