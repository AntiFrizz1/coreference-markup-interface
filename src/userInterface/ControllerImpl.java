package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import document.UpdateDocument;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ControllerImpl implements Controller {
    public static final int ADDWORD = 0;
    public static final int ADDCHAIN = 1;
    public static final int DELCHAIN = 2;
    public static final int DELWORD = 3;
    private final int POSSIBLE_CANCELS = 15;

    private boolean online = false;
    private boolean offline = false;
    private List<Chain> chains;
    private List<Pair<Action, Integer>> prevStates;
    private int textId;
    private int userId;
    private String text = "";
    final private String TEXT_PLACEHOLDER = /*"Приехав с утренним поездом в Москву, Левин остановился у своего " +
            "старшего брата по матери Кознышева и, переодевшись, вошел к нему в кабинет, намереваясь тотчас же " +
            "рассказать ему, для чего он приехал, и просить его совета: но брат был не один. У него сидел известный " +
            "профессор философии, приехавший из Харькова, собственно, затем, чтобы разъяснить недоразумение, " +
            "возникшее между ними по весьма важному философскому вопросу. Профессор вел жаркую полемику против " +
            "материалистов, а Сергей Кознышев с интересом следил за этою полемикой и, прочтя последнюю статью " +
            "профессора, написал ему в письме свои возражения; он упрекал профессора за слишком большие уступки " +
            "материалистам. И профессор тотчас же приехал, чтобы столковаться. Речь шла о модном вопросе: есть ли " +
            "граница между психическими и физиологическими явлениями в деятельности человека и где она?"*/ "";
    private Chain curChain;
    private Map<Integer, String> selected;
    private int selectedBlank = -1, maxChainId = 0;
    private String newChainName;
    private List<Action> actions;  // TODO: send this to the server and then empty it after each send
    private List<Chain> removedChains;
    private Stage primaryStage;  // TODO: this will be used to show the conflict window
    private boolean isLoggedUser = false, isJudge = false;


    ControllerImpl(Stage primaryStage) {
        chains = new ArrayList<>();
        prevStates = new ArrayList<>();
        selected = new HashMap<>();
        actions = new ArrayList<>();
        removedChains = new ArrayList<>();
        this.primaryStage = primaryStage;
    }

    String simpleText(){
        return TEXT_PLACEHOLDER;
    }
    List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public void showUI() {

    }

    boolean isOnline() {
        return online;
    }

    public String getText() {
        return text;
    }

    public void setText(String txt) {
        txt = txt.replaceAll("\\s+", " ").replaceAll("\\.+", ".").replaceAll("(\\. )+", ". ");
        text = txt;
        callTextRefresh();
    }

    public void setText(int id) {
        textId = id;
        // TODO: do a server call here, get the text and save it
        // text = getText(id);
    }

    boolean isJudge() {
        return isJudge;
    }

    boolean isLoggedUser() {
        return isLoggedUser;
    }

    public void loginJudge() {
        isJudge = true;
    }

    public void loginUser(int id) {
        userId = id;
        if (userId > 100) isJudge = true;
            // TODO: call to server to mark that a user has logged in
        else isLoggedUser = true;
        onlineMode();
    }

    @Override
    public void onlineMode() {
        online = true;
    }

    @Override
    public void offlineMode() {
        offline = true;
    }

    boolean isOfflineMode() {
        return offline;
    }

    public void restoreState(String text, List<Action> actions) {
        Map<Integer, Chain> chain = new HashMap<>();
        int maxId = 0;
        for (Action a : actions) {
            int id = a.getChainId();
            if (chain.containsKey(id)) {
                chain.get(id).addPart(a.getLocation());
            } else {
                ChainImpl newChain = new ChainImpl(a);
                newChain.setColor(generateRandomColor());
                chain.put(id, newChain);
            }
            if (a.getLocation() instanceof Blank) {
                maxId = Math.max(maxId, ((Blank) a.getLocation()).getPosition());
            } else if (a.getLocation() instanceof Phrase) {
                maxId = Math.max(maxId, ((Phrase) a.getLocation()).getPositions().stream()
                        .max(Comparator.naturalOrder()).orElse(0));
            }
        }
        chains = new ArrayList<>(chain.values());
        System.out.println(chains);
        setText(text);
        callChainRefresh();
        callMoveSentence(maxId);
    }

    public void restoreFromDump(File file) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
//        String textPath = r.readLine();
//        File f = new File(textPath);
//        String txt = new BufferedReader(new InputStreamReader(new FileInputStream(f), UTF_8)).lines().collect(Collectors.joining(". "));
//        txt = txt.replaceAll("\\s+", " ").replaceAll("\\.+", ".").replaceAll("(\\. )+", ". ");
        List<String> lines = r.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
        List<Action> actions = new ArrayList<>();
        for (String s : lines) {
            actions.addAll(new UpdateDocument(s).getActions());
        }
//        List<Action> actions = new UpdateDocument(r.readLine()).getActions();
        Map<Integer, Chain> chain = new HashMap<>();
        int maxId = 0;
        for (Action a : actions) {
            if (a.getAction() == ADDWORD || a.getAction() == ADDCHAIN) {
                int id = a.getChainId();
                maxChainId = Math.max(maxChainId, id);
                if (chain.containsKey(id)) {
                    chain.get(id).addPart(a.getLocation());
                } else {
                    ChainImpl newChain = new ChainImpl(a);
                    newChain.setColor(generateRandomColor());
                    chain.put(id, newChain);
                }
                if (a.getLocation() instanceof Blank) {
                    maxId = Math.max(maxId, ((Blank) a.getLocation()).getPosition());
                } else if (a.getLocation() instanceof Phrase) {
                    maxId = Math.max(maxId, ((Phrase) a.getLocation()).getPositions().stream()
                            .max(Comparator.naturalOrder()).orElse(0));
                }
            } else if (a.getAction() == DELCHAIN) {
                chain.remove(a.getChainId());
            } else if (a.getAction() == DELWORD) {
                Chain c = chain.get(a.getChainId());
                c.getLocations().remove(a.getLocation());
                chain.put(a.getChainId(), c);
            }
        }
        chains = new ArrayList<>(chain.values());
//        System.out.println(chains);
//        setText(text);
        callChainRefresh();
        callMoveSentence(maxId);
    }


    public void callTextRefresh() {
        //System.out.println("Refresh");
        //System.out.println(text);
        RefreshEvent event = new RefreshEvent();  // TODO: do smth so that the source is actually this
        primaryStage.fireEvent(event);
    }

    public void callChainRefresh() {
        //System.out.println("Refresh");
        //System.out.println(text);
        RefreshChainEvent event = new RefreshChainEvent();  // TODO: do smth so that the source is actually this
        primaryStage.fireEvent(event);
    }

    public void callMoveSentence(int id) {
        //System.out.println("Refresh");
        //System.out.println(text);
        MoveSelectedSentenceEvent event = new MoveSelectedSentenceEvent(id); // TODO: do smth so that the source is actually this
        primaryStage.fireEvent(event);
    }

    public void clearActions() {
        actions.clear();
        prevStates.clear();
        removedChains.clear();
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
     *
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
        Action ac = new Action(ADDWORD, curChain.getId(), phrase, curChain.getName());
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selected.clear();
        curChain = null;
        return ac;
    }

    /**
     * Adds a new anaphora specified by {@link ControllerImpl#selectedBlank} to a selected chain in
     * {@link ControllerImpl#curChain}. If no chain or no blank is selected, does nothing.
     *
     * @return an action describing this operation
     */
    @Override
    public Action addAnaphoraToChain() {
        if (selectedBlank == -1 || curChain == null) return null;
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        Location blank = new Blank(selectedBlank);
        curChain.addPart(blank);
        Action ac = new Action(ADDWORD, curChain.getId(), blank, curChain.getName());
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selectedBlank = -1;
        curChain = null;
        return ac;
    }

    /**
     * Sets the chain name to be used when creating a new chain.
     *
     * @param name the chain's name
     */
    void setNewChainName(String name) {
        newChainName = name;
    }

    /**
     * Adds all of the selected words whose positions are contained in {@link ControllerImpl#selected} to a newly
     * created chain. If no words are selected, does nothing.
     *
     * @return an action describing this operation
     */
    @Override
    public Action addNewChain() {
        if (selected.isEmpty() && selectedBlank == -1) return null;
        if (!selected.isEmpty()) {
            String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(Map.Entry::getValue).collect(Collectors.joining(" "));
            // TODO: maybe Utils.generateRandomColor()?
            Phrase phrase = new Phrase(result, new HashSet<>(selected.keySet()));
            ChainImpl newChain = new ChainImpl(newChainName, generateRandomColor(),
                    ++maxChainId, phrase);
            Action ac = new Action(ADDCHAIN, newChain.getId(), phrase, newChain.getName());
            saveState(ac, -1);
            newChainName = "";
            chains.add(0, newChain);
            curChain = null;
            selected.clear();
            return ac;
        } else {
            Blank blank = new Blank(selectedBlank);
            ChainImpl newChain = new ChainImpl(newChainName, generateRandomColor(),
                    chains.size(), blank);
            Action ac = new Action(ADDCHAIN, newChain.getId(), blank, newChain.getName());
            saveState(ac, -1);
            newChainName = "";
            chains.add(0, newChain);
            curChain = null;
            selectedBlank = -1;
            return ac;
        }
    }

    public Chain deleteChain() {
        Action ac = new Action(DELCHAIN, curChain.getId(), new Blank(0), curChain.getName());
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        removedChains.add(curChain);
        Chain curChainOld = curChain;
        curChain = null;
        saveState(ac, prevIndex);
        return curChainOld;
    }

    public Action deletePhrase() {
        if (!selected.isEmpty()) {
            String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(Map.Entry::getValue).collect(Collectors.joining(" "));
            Phrase phrase = new Phrase(result, new HashSet<>(selected.keySet()));
            Chain from = chains.stream().filter(ch -> ch.getLocations().contains(phrase)).findAny().orElse(null);
            if (from == null) return null;
            int prevIndex = chains.indexOf(from);
            chains.remove(from);
            Action ac = new Action(DELWORD, from.getId(), phrase, from.getName());
            from.getLocations().remove(phrase);
            saveState(ac, prevIndex);
            if (!from.getLocations().isEmpty()) chains.add(0, from);
            selected.clear();
            return ac;
        } else {
            Blank blank = new Blank(selectedBlank);
            Chain from = chains.stream().filter(ch -> ch.getLocations().contains(blank)).findAny().orElse(null);
            if (from == null) return null;
            int prevIndex = chains.indexOf(from);
            chains.remove(from);
            Action ac = new Action(DELWORD, from.getId(), blank, from.getName());
            from.getLocations().remove(blank);
            saveState(ac, prevIndex);
            if (!from.getLocations().isEmpty()) chains.add(0, from);
            selectedBlank = -1;
            return ac;
        }
    }

    /**
     * Registers a text button press from the user and updates the logic accordingly.
     *
     * @param btn      the text on the pressed button
     * @param position the position of this button in the text
     * @return if the press was recorded or not
     */
    boolean pressedButton(String btn, int position) {
        if (!btn.trim().isEmpty() && selectedBlank == -1) {
            if (!selected.containsKey(position)) selected.put(position, btn);
            else selected.remove(position);
            return true;
        } else if (btn.trim().isEmpty() && selected.isEmpty() && (selectedBlank == -1 || selectedBlank == position)) {
            if (selectedBlank == -1) selectedBlank = position;
            else selectedBlank = -1;
            return true;
        }
        return false;
    }

    /**
     * Registers a chain button press from the user and updates the logic accordingly.
     *
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
//    long timeStamp = 0;

    String textPath;

    public void setTextPath(String path) {
        textPath = path;
    }

    BufferedWriter w = null;

    private void initTimestamp() {
        if (w == null) {
            try {
                w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dump" + textPath, true), StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveStateOffline() {
        try {
            initTimestamp();


//            BufferedWriter w = new BufferedWriter(new FileWriter(new File("dump" + timeStamp + "-" + textPath)));
//            StringBuilder sb = new StringBuilder();
//            sb.append(Arrays.stream(text.split(" ")).limit(10).collect(Collectors.joining(" ")))
//                    .append("\n");
//            System.out.println(chains.size());
//            for (Chain c : chains) sb.append(c.pack()).append("\n\n");
//            w.write(sb.toString());
            UpdateDocument document = new UpdateDocument(actions);
//            w.write(textPath + "\n");
            String packed = document.pack();
            if (!packed.trim().isEmpty()) w.write(document.pack() + "\n");
            w.flush();
            clearActions();
//            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels the last action done by the user, if possible. Note that we probably won't support cancelling resolving
     * conflicts.
     *
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
        if (ac.getKey().getAction() == DELWORD) {
            Chain c = chains.get(0);
            if (c.getId() == ac.getKey().getChainId()) {
                chains.remove(0);
                c.getLocations().add(ac.getKey().getLocation());
                chains.add(ac.getValue(), c);
            } else {
                ChainImpl restored = new ChainImpl(ac.getKey().getName(), generateRandomColor(),
                        ++maxChainId, ac.getKey().getLocation());
                chains.add(ac.getValue(), restored);
            }
        }
        if (ac.getKey().getAction() == DELCHAIN) {
            Chain restored = removedChains.get(removedChains.size() - 1);
            chains.add(ac.getValue(), restored);
        }
        curChain = null;
        return ac.getKey();
    }

    public int getPrevStatesSize() {
        return prevStates.size();
    }

    @Override
    public void disableOption(int option) {

    }

    @Override
    public void enableOption(int option) {

    }


    /**
     * Generates a random color. It tries to not make too dark/bright ones.
     *
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
     *
     * @param a         an Action describing the last action of the user
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
     *
     * @param chain  a chain
     * @param wordId the word's position in the whole text
     * @return true whether a chain contains this word in one of its locations, false otherwise
     */
    public boolean chainContainsWord(Chain chain, int wordId) {
        return chain.getLocations().stream().filter(l -> l instanceof Phrase).map(ph -> ((Phrase) ph).getPositions())
                .anyMatch(s -> s.contains(wordId));
    }

    /**
     * Checks whether a chain contains a blank with a given position in the text.
     *
     * @param chain   a chain
     * @param blankId the blank's position in the whole text
     * @return true whether a chain contains this blank in one of its locations, false otherwise
     */
    public boolean chainContainsBlank(Chain chain, int blankId) {
        return chain.getLocations().stream().filter(l -> l instanceof Blank).map(bl -> ((Blank) bl).getPosition())
                .anyMatch(s -> s == blankId);
    }

    static class RefreshEvent extends Event {
        public static final EventType<RefreshEvent> REFRESH_TEXT =
                new EventType<>(Event.ANY, "REFRESH_TEXT");

        public RefreshEvent() {
            super(REFRESH_TEXT);
        }
    }

    static class RefreshChainEvent extends Event {
        public static final EventType<RefreshChainEvent> REFRESH_CHAIN =
                new EventType<>(Event.ANY, "REFRESH_CHAIN");

        public RefreshChainEvent() {
            super(REFRESH_CHAIN);
        }
    }

    static class MoveSelectedSentenceEvent extends Event {
        public static final EventType<MoveSelectedSentenceEvent> MOVE_SELECTED_SENTENCE =
                new EventType<>(Event.ANY, "MOVE_SELECTED_SENTENCE");

        public int id;

        public MoveSelectedSentenceEvent(int location) {
            super(MOVE_SELECTED_SENTENCE);
            id = location;
        }
    }
}