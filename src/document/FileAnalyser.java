package document;

import chain.Action;
import javafx.util.Pair;
import org.omg.PortableInterceptor.ACTIVE;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * From judge file make approved dump which can be opened by user
 */
public class FileAnalyser {
    static class DataFromFile {
        List<Action> firstActions;
        List<Action> secondActions;
        List<Integer> decisions;

        Map<Integer, Set<Integer>> firstIdToSecondIds;
        Map<Integer, Set<Integer>> secondIdToFirstIds;

        Map<Integer, Pair<Set<Integer>, Set<Integer>>> localIdToFirstAndSecondIds;

        Map<Integer, Integer> firstIdToLocalId;
        Map<Integer, Integer> secondIdToLocalId;

        Map<Integer, List<Action>> actions;

        DataFromFile() {
            firstActions = new ArrayList<>();
            secondActions = new ArrayList<>();
            decisions = new ArrayList<>();

            firstIdToSecondIds = new HashMap<>();
            secondIdToFirstIds = new HashMap<>();

            localIdToFirstAndSecondIds = new HashMap<>();

            actions = new HashMap<>();

            firstIdToLocalId = new HashMap<>();
            secondIdToLocalId = new HashMap<>();
        }

        void makeMaps() {
            for (int i = 0; i < firstActions.size(); i++) {
                if (!firstIdToSecondIds.containsKey(firstActions.get(i).getChainId())) {
                    firstIdToSecondIds.put(firstActions.get(i).getChainId(), new HashSet<>());
                }
                if (!secondIdToFirstIds.containsKey(secondActions.get(i).getChainId())) {
                    secondIdToFirstIds.put(secondActions.get(i).getChainId(), new HashSet<>());
                }
                if (decisions.get(i) == 3) {
                    firstIdToSecondIds.get(firstActions.get(i).getChainId()).add(secondActions.get(i).getChainId());
                    secondIdToFirstIds.get(secondActions.get(i).getChainId()).add(firstActions.get(i).getChainId());
                }
            }
        }

        void analyze() {
            int localId = 0;
            while (!firstIdToSecondIds.isEmpty()) {
                Set<Integer> first = new HashSet<>();

                int id = new ArrayList<>(firstIdToSecondIds.keySet()).get(0);
                first.add(id);
                Set<Integer> second = new HashSet<>(firstIdToSecondIds.get(id));
                firstIdToSecondIds.remove(id);
                boolean change = true;
                while (change) {
                    change = false;
                    for (int k: second) {
                        if (secondIdToFirstIds.containsKey(k) && !secondIdToFirstIds.get(k).isEmpty()) {
                            first.addAll(secondIdToFirstIds.get(k));
                            secondIdToFirstIds.remove(k);
                            change = true;
                        }
                    }

                    for (int k: first) {
                        if (firstIdToSecondIds.containsKey(k) && !firstIdToSecondIds.get(k).isEmpty()) {
                            second.addAll(firstIdToSecondIds.get(k));
                            firstIdToSecondIds.remove(k);
                            change = true;
                        }
                    }
                }
                localIdToFirstAndSecondIds.put(localId++, new Pair<>(first, second));
            }

            while (!secondIdToFirstIds.isEmpty()) {
                Set<Integer> second = new HashSet<>();

                int id = new ArrayList<>(secondIdToFirstIds.keySet()).get(0);
                second.add(id);
                Set<Integer> first = new HashSet<>(secondIdToFirstIds.get(id));
                secondIdToFirstIds.remove(id);
                boolean change = true;
                while (change) {
                    change = false;

                    for (int k: first) {
                        if (firstIdToSecondIds.containsKey(k) && !firstIdToSecondIds.get(k).isEmpty()) {
                            second.addAll(firstIdToSecondIds.get(k));
                            firstIdToSecondIds.remove(k);
                            change = true;
                        }
                    }

                    for (int k: second) {
                        if (secondIdToFirstIds.containsKey(k) && !secondIdToFirstIds.get(k).isEmpty()) {
                            first.addAll(secondIdToFirstIds.get(k));
                            secondIdToFirstIds.remove(k);
                            change = true;
                        }
                    }
                }
                localIdToFirstAndSecondIds.put(localId++, new Pair<>(first, second));
            }
        }

        void extractPairMap() {
            for (int i: localIdToFirstAndSecondIds.keySet()) {
                Set<Integer> f = localIdToFirstAndSecondIds.get(i).getKey();
                Set<Integer> s = localIdToFirstAndSecondIds.get(i).getValue();

                for (int j: f) {
                    firstIdToLocalId.put(j, i);
                }

                for (int j: s) {
                    secondIdToLocalId.put(j, i);
                }
            }
        }

        void makeNewChains() {
            extractPairMap();
            for (int i = 0; i < decisions.size(); i++) {
                if (decisions.get(i) == 1 && !firstActions.get(i).isEmpty()) {
                    int id = firstActions.get(i).getChainId();
                    int localId = firstIdToLocalId.get(id);
                    if (actions.containsKey(localId)) {
                        String name = actions.get(localId).get(0).getName();
                        actions.get(localId).add(new Action(0, localId, firstActions.get(i).getLocation(), name));
                    } else {
                        List<Action> tmp = new ArrayList<>();
                        tmp.add(new Action(1, localId, firstActions.get(i).getLocation(), firstActions.get(i).getName()));
                        actions.put(localId, tmp);
                    }
                } else if (decisions.get(i) == 2 && !secondActions.get(i).isEmpty()) {
                    int id = secondActions.get(i).getChainId();
                    int localId = secondIdToLocalId.get(id);
                    if (actions.containsKey(localId)) {
                        String name = actions.get(localId).get(0).getName();
                        actions.get(localId).add(new Action(0, localId, secondActions.get(i).getLocation(), name));
                    } else {
                        List<Action> tmp = new ArrayList<>();
                        tmp.add(new Action(1, localId, secondActions.get(i).getLocation(), secondActions.get(i).getName()));
                        actions.put(localId, tmp);
                    }
                } else if (decisions.get(i) == 3) {
                    Action action;
                    int localId;
                    if (!firstActions.get(i).isEmpty()) {
                        action = firstActions.get(i);
                        localId = firstIdToLocalId.get(action.getChainId());
                    } else {
                        action = secondActions.get(i);
                        localId = secondIdToLocalId.get(action.getChainId());
                    }

                    if (actions.containsKey(localId)) {
                        String name = actions.get(localId).get(0).getName();
                        actions.get(localId).add(new Action(0, localId, action.getLocation(), name));
                    } else {
                        List<Action> tmp = new ArrayList<>();
                        tmp.add(new Action(1, localId, action.getLocation(), action.getName()));
                        actions.put(localId, tmp);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Map<Integer, Map<Integer, List<Integer>>> firstIdToSecondIds = new HashMap<>();
        Map<Integer, Map<Integer, List<Integer>>> secondIdTofirstIds = new HashMap<>();

        String dirName = ".";
        String to = "";
        if (args.length >= 1) {
            dirName = args[0];
        }
        if (args.length == 2) {
            to = args[1] + "/";
        }

        try (Stream<Path> paths = Files.walk(Paths.get(dirName))) {
            List<String> files = paths.filter(Files::isRegularFile).map(Path::toString).filter(a -> a.contains("vs")).collect(Collectors.toList());
            for (String path : files) {
                BufferedReader readerFirst = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
                List<String> lines = readerFirst.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                DataFromFile dataFromFile = new DataFromFile();
                for (String line: lines) {
                    String[] array = line.split("@");
                    dataFromFile.firstActions.add(new Action(array[0]));
                    dataFromFile.secondActions.add(new Action(array[1]));
                    dataFromFile.decisions.add(Integer.parseInt(array[2]));
                }
                dataFromFile.makeMaps();
                dataFromFile.analyze();
                dataFromFile.makeNewChains();

                String textId = path.split("text=")[1];
                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(to + "dump" + textId + ".txt"), StandardCharsets.UTF_8)))) {
                    for (int j: dataFromFile.actions.keySet()) {
                        for (Action action: dataFromFile.actions.get(j)) {
                            writer.println(action.pack());
                            writer.flush();
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Hi");
    }
}
