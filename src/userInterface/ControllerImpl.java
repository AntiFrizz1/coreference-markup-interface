package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import client.ConflictImpl;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ControllerImpl implements Controller {
    private final int ADDWORD = 0;
    private final int ADDCHAIN = 1;
    private final int DELCHAIN = 2;
    private final int DELWORD = 3;
    private final int POSSIBLE_CANCELS = 5;

    private List<Chain> chains;
    private List<Pair<Action, Integer>> prevStates;
    private int textId;
    private String text;
    final private String TEXT_PLACEHOLDER = "Приехав с утренним поездом в Москву, Левин остановился у своего " +
            "старшего брата по матери Кознышева и, переодевшись, вошел к нему в кабинет, намереваясь тотчас же " +
            "рассказать ему, для чего он приехал, и просить его совета: но брат был не один. У него сидел известный " +
            "профессор философии, приехавший из Харькова, собственно, затем, чтобы разъяснить недоразумение, " +
            "возникшее между ними по весьма важному философскому вопросу. Профессор вел жаркую полемику против " +
            "материалистов, а Сергей Кознышев с интересом следил за этою полемикой и, прочтя последнюю статью " +
            "профессора, написал ему в письме свои возражения; он упрекал профессора за слишком большие уступки " +
            "материалистам. И профессор тотчас же приехал, чтобы столковаться. Речь шла о модном вопросе: есть ли " +
            "граница между психическими и физиологическими явлениями в деятельности человека и где она?";
    private Chain curChain;
    private Map<Integer, String> selected;
    private int selectedBlank = -1;
    private String newChainName;
    private List<Action> actions;  // TODO: send this to the server and then empty it after each send
    private Stage primaryStage;  // TODO: this will be used to show the conflict window

    ControllerImpl(Stage primaryStage) {
        chains = new ArrayList<>();
        prevStates = new ArrayList<>();
        selected = new HashMap<>();
        actions = new ArrayList<>();
        this.primaryStage = primaryStage;
    }

    @Override
    public void showUI() {

    }

    public String getText(int id) {
        textId = id;
        // TODO: do a server call here or try to find the text locally; right now it only returns a placeholder.
        text = TEXT_PLACEHOLDER;
        return text;
    }

    @Override
    public void onlineMode() {

    }

    @Override
    public void offlineMode() {

    }

    List<Chain> getChains() {
        return chains;
    }

    Set<Integer> getSelected() {
        return new HashSet<>(selected.keySet());
    }

    int getSelectedBlank() {
        return selectedBlank;
    }

    boolean isSelectedAlreadyBound() {
        return chains.stream().map(Chain::getLocations)
                .anyMatch(list -> list.contains(new Phrase("", new HashSet<>(selected.keySet()))));
    }

    boolean isSelectedBlankAlreadyBound() {
        return chains.stream().map(Chain::getLocations)
                .anyMatch(list -> list.contains(new Blank(selectedBlank)));
    }

    void clearSelected() {
        selected.clear();
    }

    public Integer getSelectedChain() {
        return (curChain == null) ? -1 : chains.indexOf(curChain);
    }

    /**
     * Adds all of the selected words whose positions are contained in {@link ControllerImpl#selected} to a selected
     * chain in {@link ControllerImpl#curChain}. If no chain or no words are selected, does nothing.
     * @return an action describing this operation
     */
    @Override
    public Action addToChain() {
        if (selected.isEmpty() || curChain == null) return null;
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        Location phrase = new Phrase(result, new HashSet<>(selected.keySet()));
        curChain.addPart(phrase);
        Action ac = new Action(ADDWORD, curChain.getId(), phrase);
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selected.clear();
        curChain = null;
        return ac;
    }

    /**
     * Adds a new anaphora specified by {@link ControllerImpl#selectedBlank} to a selected chain in
     * {@link ControllerImpl#curChain}. If no chain or no blank is selected, does nothing.
     * @return an action describing this operation
     */
    @Override
    public Action addAnaphoraToChain() {
        if (selectedBlank == -1 || curChain == null) return null;
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        Location blank = new Blank(selectedBlank);
        curChain.addPart(blank);
        Action ac = new Action(ADDWORD, curChain.getId(), blank);
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selectedBlank = -1;
        curChain = null;
        return ac;
    }

    /**
     * Sets the chain name to be used when creating a new chain.
     * @param name the chain's name
     */
    void setNewChainName(String name) {
        newChainName = name;
    }

    /**
     * Adds all of the selected words whose positions are contained in {@link ControllerImpl#selected} to a newly
     * created chain. If no words are selected, does nothing.
     * @return an action describing this operation
     */
    @Override
    public Action addNewChain() {
        if (selected.isEmpty()) return null;
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        // TODO: maybe Utils.generateRandomColor()?
        Phrase phrase = new Phrase(result, new HashSet<>(selected.keySet()));
        ChainImpl newChain = new ChainImpl(newChainName, generateRandomColor(),
                chains.size(), phrase);
        Action ac = new Action(ADDCHAIN, newChain.getId(), phrase);
        saveState(ac, -1);
        newChainName = "";
        chains.add(0, newChain);
        curChain = null;
        selected.clear();
        return ac;
    }

    /**
     * Registers a text button press from the user and updates the logic accordingly.
     * @param btn the text on the pressed button
     * @param position the position of this button in the text
     * @return if the press was recorded or not
     */
    boolean pressedButton(String btn, int position) {
        if (!btn.trim().isEmpty() && selectedBlank == -1) {
            if (!selected.containsKey(position)) selected.put(position, btn);
            else selected.remove(position);
            return true;
        }
        else if (selected.isEmpty() && (selectedBlank == -1 || selectedBlank == position)) {
            if (selectedBlank == -1) selectedBlank = position;
            else selectedBlank = -1;
            return true;
        }
        return false;
    }

    /**
     * Registers a chain button press from the user and updates the logic accordingly.
     * @param num the number of the selected chain
     * @return the number of the previous selected chain
     */
    int selectChain(int num) {
        int prev = chains.indexOf(curChain);
        if (prev != num) curChain = chains.get(num);
        else curChain = null;
        return prev;
    }

    @Override
    public void deleteChain(Chain chain) {

    }

    @Override
    public void saveStateOffline() {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(new File("dump.txt")));
            StringBuilder sb = new StringBuilder();
            for (Chain c: chains) c.pack(sb);
            w.write(sb.toString());
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels the last action done by the user, if possible. Note that we probably won't support cancelling resolving
     * conflicts.
     * @return an Action describing the user's last action
     */
    @Override
    public Action cancel() {
        Pair<Action, Integer> ac = prevStates.get(prevStates.size() - 1);
        if (actions.size() != 0) {
            actions.remove(actions.size() - 1);
        }
        prevStates.remove(prevStates.size() - 1);
        if (ac.getKey().getAction() == ADDCHAIN) {
            chains.remove(0);
        }
        if (ac.getKey().getAction() == ADDWORD) {
            Chain c = chains.remove(0);
            c.getLocations().remove(ac.getKey().getLocation());
            chains.add(ac.getValue(), c);
        }
        curChain = null;
        return ac.getKey();
    }

    public int getPrevStatesSize() {
        return prevStates.size();
    }

    /**
     * Shows a new window prompting the user to resolve the conflict. The main window is paused until the conflict is
     * resolved
     * @param conflict a conflict to show
     */
    @Override
    public void showConflict(ConflictImpl conflict) {
        Stage stage = new Stage();
        stage.setTitle("Решение конфликта");
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        Text header = new Text("Возник конфликт с другой командой!");
        GridPane.setHalignment(header, HPos.CENTER);
        header.setStyle("-fx-font-size: 25pt;");
        root.add(header, 0, 0);

        String[] words = text.split(" ");
        int index = 0;
        if (conflict.getCollision() instanceof Blank) {
            index = ((Blank) conflict.getCollision()).getPosition();
        }
        else if (conflict.getCollision() instanceof Phrase) {
            index = ((Phrase) conflict.getCollision()).getPositions().stream().min(Comparator.naturalOrder())
                    .orElseGet(() -> 0);
        }

        javafx.scene.control.ScrollPane textWrapperOur = new ScrollPane();
        textWrapperOur.setFitToWidth(true);
        FlowPane textPaneOur = new FlowPane();
        textWrapperOur.setContent(textPaneOur);
        textPaneOur.setPadding(new Insets(5));
        /*
        Generate the panel with our chain.
         */
        for (int i = index - 20; i < index + 20; i++) {
            if (i < 0 || i > words.length) continue;
            Button word = new Button(words[i]);
            word.getStyleClass().add("word");  // CSS class for styling words and their selection
            word.setStyle("-fx-background-color: rgba(0,0,0,0);");
            if (chainContainsWord(conflict.getFirstSolution(), i)) {
                Color c = conflict.getFirstSolution().getColor();
                word.setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                        c.getGreen() + "," + c.getBlue() + ",0.3)");
            }
            textPaneOur.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            if (chainContainsBlank(conflict.getFirstSolution(), i)) {
                Color c = conflict.getFirstSolution().getColor();
                space.setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                        c.getGreen() + "," + c.getBlue() + ",0.3)");
            }
            textPaneOur.getChildren().add(space);
        }

        javafx.scene.control.ScrollPane textWrapperTheir = new ScrollPane();
        textWrapperTheir.setFitToWidth(true);
        FlowPane textPaneTheir = new FlowPane();
        textWrapperTheir.setContent(textPaneTheir);
        textPaneTheir.setPadding(new Insets(5));
        /*
        Generate the panel with their chain.
         */
        for (int i = index - 20; i < index + 20; i++) {
            if (i < 0 || i > words.length) continue;
            Button word = new Button(words[i]);
            word.getStyleClass().add("word");  // CSS class for styling words and their selection
            word.setStyle("-fx-background-color: rgba(0,0,0,0);");
            if (chainContainsWord(conflict.getSecondSolution(), i)) {
                Color c = conflict.getSecondSolution().getColor();
                word.setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                        c.getGreen() + "," + c.getBlue() + ",0.3)");
            }
            textPaneTheir.getChildren().add(word);
            Button space = new Button("   ");
            space.getStyleClass().add("word");
            space.setStyle("-fx-background-color: rgba(0,0,0,0)");
            if (chainContainsBlank(conflict.getSecondSolution(), i)) {
                Color c = conflict.getSecondSolution().getColor();
                space.setStyle("-fx-background-color: rgba(" + c.getRed() + "," +
                        c.getGreen() + "," + c.getBlue() + ",0.3)");
            }
            textPaneTheir.getChildren().add(space);
        }
        GridPane texts = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        texts.getColumnConstraints().addAll(col1, col2);

        GridPane our = new GridPane();
        Text ourText = new Text("Ваша цепочка:\n" + conflict.getFirstSolution().toString());
        GridPane.setHalignment(ourText, HPos.CENTER);
        ourText.setTextAlignment(TextAlignment.CENTER);
        our.add(ourText, 0, 0);
        our.add(textPaneOur, 0, 1);
        GridPane their = new GridPane();
        Text theirText = new Text("Цепочка другой команды:\n" + conflict.getSecondSolution().toString());
        GridPane.setHalignment(theirText, HPos.CENTER);
        theirText.setTextAlignment(TextAlignment.CENTER);
        their.add(theirText, 0, 0);
        their.add(textPaneTheir, 0, 1);
        texts.add(our, 0, 0);
        texts.add(their, 1, 0);
        texts.setGridLinesVisible(true);

        root.add(texts, 0, 1);

        Text confDesc = new Text("Конфликтующее слово: " + conflict.getCollision().toString());
        confDesc.setStyle("-fx-font-size: 25pt;");
        GridPane.setHalignment(confDesc, HPos.CENTER);
        confDesc.setTextAlignment(TextAlignment.CENTER);
        root.add(confDesc, 0, 2);

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        Button reject = new Button("Оставить свою версию");
        reject.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            resolveConflict(conflict, 0);
        });
        Button undo = new Button("Убрать это слово из цепочки");
        undo.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            resolveConflict(conflict, 1);
        });
        Button accept = new Button("Принять версию другой команды");
        accept.setOnAction(event -> {
            stage.getScene().getWindow().hide();
            resolveConflict(conflict, 2);
        });
        buttons.getChildren().addAll(reject, undo, accept);

        root.add(buttons, 0, 3);

        stage.setScene(new Scene(root, 800, 600));
        stage.getScene().getStylesheets().add("styles.css");
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }

    @Override
    public void resolveConflict(ConflictImpl conflict, int decision) {
        // TODO: this method should send the information to the server and update the chains according to the decision
    }

    @Override
    public void disableOption(int option) {

    }

    @Override
    public void enableOption(int option) {

    }


    /**
     * Generates a random color. It tries to not make too dark/bright ones.
     * @return a color
     */
    private Color generateRandomColor() {
        Random R = new Random();
        int r = R.nextInt(256);
        int g = R.nextInt(256);
        int b = R.nextInt(256);
        if (r + g + b < 120) return generateRandomColor();
        if (r + g + b > 600) return generateRandomColor();
        else return new Color(r, g, b);
    }

    /**
     * Saves an action made by the user. It is necessary to call this function in every function that somehow changes
     * the structure of the chains (except for cancelling an action).
     * @param a an Action describing the last action of the user
     * @param prevIndex index of the modified chain in the current list; would be used if a user decides to cancel
     *                  the action to restore the order of the chains
     */
    private void saveState(Action a, Integer prevIndex) {
        if (prevStates.size() >= POSSIBLE_CANCELS) prevStates.remove(0);
        actions.add(a);
        prevStates.add(new Pair<>(a, prevIndex));
    }

    /**
     * Checks whether a chain contains a word with a given position in the text.
     * @param chain a chain
     * @param wordId the word's position in the whole text
     * @return true whether a chain contains this word in one of its locations, false otherwise
     */
    private boolean chainContainsWord(Chain chain, int wordId) {
        return chain.getLocations().stream().filter(l -> l instanceof Phrase).map(ph -> ((Phrase) ph).getPositions())
                .anyMatch(s -> s.contains(wordId));
    }

    /**
     * Checks whether a chain contains a blank with a given position in the text.
     * @param chain a chain
     * @param blankId the blank's position in the whole text
     * @return true whether a chain contains this blank in one of its locations, false otherwise
     */
    private boolean chainContainsBlank(Chain chain, int blankId) {
        return chain.getLocations().stream().filter(l -> l instanceof Blank).map(bl -> ((Blank) bl).getPosition())
                .anyMatch(s -> s == blankId);
    }

}
