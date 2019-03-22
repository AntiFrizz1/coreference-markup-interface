package document;

import chain.Action;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * From judge file make approved dump which can be opened by user
 */
public class FileAnalyser {
    public static void main(String[] args) {
        Map<Integer, Action> firstIdToSecondId = new HashMap<>();
        Map<Integer, Action> secondIdToFirstId = new HashMap<>();

        Map<Integer, Action> firstIdToAction = new HashMap<>();
        Map<Integer, Action> secondIdToAction = new HashMap<>();
        Map<Action, Integer> actionToLocalId = new HashMap<>();

        List<Action> actions = new ArrayList<>();

        int localId = 0;
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
            for (String path: files) {
                BufferedReader readerFirst = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
                List<String> lines = readerFirst.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                for (String line: lines) {
                    String[] strings = line.split("@");
                    if (strings.length != 3) {
                        System.out.println("Unexpected data in file: " + path);
                        continue;
                    }

                    Action first = new Action(strings[0]);
                    Action second = new Action(strings[1]);
                    int decision;
                    try {
                        decision = Integer.parseInt(strings[2]);
                    } catch (Exception e) {
                        System.out.println("Unexpected data in file: " + path);
                        continue;
                    }

                    switch (decision) {
                        case 1:
                            if (!first.isEmpty()) {
                                if (firstIdToAction.containsKey(first.getChainId())) {
                                    actions.add(new Action(0, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                } else {
                                    firstIdToAction.put(first.getChainId(), first);
                                    actionToLocalId.put(first, localId++);
                                    actions.add(new Action(1, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                }
                            }
                            break;
                        case 2:
                            if (!second.isEmpty()) {
                                if (secondIdToAction.containsKey(second.getChainId())) {
                                    actions.add(new Action(0, actionToLocalId.get(secondIdToAction.get(second.getChainId())), second.getLocation(), secondIdToAction.get(second.getChainId()).getName()));
                                } else {
                                    secondIdToAction.put(second.getChainId(), second);
                                    actionToLocalId.put(second, localId++);
                                    actions.add(new Action(1, actionToLocalId.get(secondIdToAction.get(second.getChainId())), second.getLocation(), secondIdToAction.get(second.getChainId()).getName()));
                                }
                            }
                            break;
                        case 3:
                            if (!first.isEmpty()) {
                                if (firstIdToAction.containsKey(first.getChainId()) && secondIdToAction.containsKey(second.getChainId())) {
                                    actions.add(new Action(0, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                } else if (firstIdToAction.containsKey(first.getChainId()) && !secondIdToAction.containsKey(second.getChainId())) {
                                    secondIdToAction.put(second.getChainId(), firstIdToAction.get(first.getChainId()));
                                    actions.add(new Action(0, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                } else if (!firstIdToAction.containsKey(first.getChainId()) && secondIdToAction.containsKey(second.getChainId())) {
                                    firstIdToAction.put(first.getChainId(), secondIdToAction.get(second.getChainId()));
                                    actions.add(new Action(0, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                } else {
                                    firstIdToAction.put(first.getChainId(), first);
                                    secondIdToAction.put(second.getChainId(), first);
                                    actionToLocalId.put(first, localId++);
                                    actions.add(new Action(1, actionToLocalId.get(firstIdToAction.get(first.getChainId())), first.getLocation(), firstIdToAction.get(first.getChainId()).getName()));
                                }
                            }
                            break;
                    }
                }
                String textIdString;
                int textId = Integer.parseInt(path.split("text=")[1]) + 1;
                if (textId < 10) {
                    textIdString = "0" + textId;
                } else {
                    textIdString = String.valueOf(textId);
                }
                PrintWriter writerFirst = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(to + "dump" + textIdString + ".txt"), StandardCharsets.UTF_8)));
                UpdateDocument document = new UpdateDocument(actions);

                writerFirst.write(document.pack());
                writerFirst.flush();
                writerFirst.close();
                actions.clear();
                firstIdToAction.clear();
                secondIdToAction.clear();
                actionToLocalId.clear();
                localId = 0;
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
