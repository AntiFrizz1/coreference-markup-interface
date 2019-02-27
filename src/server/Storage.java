package server;

import chain.Action;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import client.ConflictImpl;
import client.ConflictWithSuffix;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class Storage {
    class Game {
        Socket teamOneSocket;
        Socket teamTwoSocket;

        Map<String, Integer> clientToServer;

        List<Chain> approved;
        HashMap<Location, Action> unApproved;

        List<Action> teamOneAct;
        List<Action> teamTwoAct;

        AtomicReference<String> ansOne;
        AtomicReference<String> ansTwo;

        Game(Socket socketOne, Socket socketTwo) {
            teamOneSocket = socketOne;
            teamTwoSocket = socketTwo;

            clientToServer = new HashMap<>();
            approved = new ArrayList<>();
            unApproved = new HashMap<>();

            teamOneAct = new ArrayList<>();
            teamTwoAct = new ArrayList<>();

            ansOne = new AtomicReference<>(null);
            ansTwo = new AtomicReference<>(null);
        }

        public Chain getChain(String name) {
            return approved.get(clientToServer.get(name));
        }
    };

    List<Game> games;
    AtomicIntegerArray atomicIntegerArray;

    Storage() {
        games = new ArrayList<>();
        atomicIntegerArray = new AtomicIntegerArray(100);
    }

    public boolean put(Action action, int textNum, int teamNum) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            if ((teamNum == 1)) {
                games.get(textNum).teamOneAct.add(action);
            } else {
                games.get(textNum).teamTwoAct.add(action);
            }
            atomicIntegerArray.compareAndSet(textNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    public List<ConflictImpl> applyActions(HashMap<String, Integer> clientToServer, HashMap<Location, Action> unApproved, List<Chain> approved, List<Action> teamOneAct, List<Action> teamTwoAct) {
        ArrayList<ConflictImpl> conflicts = new ArrayList<>();
        for (Action actionOne : teamOneAct) {
            boolean found = false;
            for (Action actionTwo : teamTwoAct) {
                if (actionOne.getLocation().equals(actionTwo.getLocation())) {
                    int typeOne = actionOne.getAction();
                    int typeTwo = actionTwo.getAction();
                    String linkOne = "f" + actionOne.getChainId();
                    String linkTwo = "s" + actionTwo.getChainId();
                    if (typeOne == typeTwo) {
                        if (typeOne == 1) {
                            clientToServer.put(linkOne, approved.size());
                            clientToServer.put(linkTwo, approved.size());
                            approved.add(new ChainImpl(actionOne));
                        } else {
                            if (clientToServer.containsKey(linkOne) && clientToServer.containsKey(linkTwo)) {
                                if (Objects.equals(clientToServer.get(linkOne), clientToServer.get(linkTwo))) {
                                    approved.get(clientToServer.get(linkOne)).addPart(actionOne.getLocation());
                                } else {
                                    conflicts.add(new ConflictImpl(approved.get(clientToServer.get(linkOne)), approved.get(clientToServer.get(linkTwo)), actionOne.getLocation()));
                                }
                            } else if (!clientToServer.containsKey(linkOne) && !clientToServer.containsKey(linkTwo)) {
                                Action searchOne = null, searchTwo = null;
                                for (Map.Entry<Location, Action> entry : unApproved.entrySet()) {
                                    if (entry.getValue().getChainId() == actionOne.getChainId()) {
                                        searchOne = entry.getValue();
                                    }
                                    if (entry.getValue().getChainId() == actionTwo.getChainId()) {
                                        searchTwo = entry.getValue();
                                    }
                                }
                                conflicts.add(new ConflictImpl(new ChainImpl(searchOne), new ChainImpl(searchTwo), actionOne.getLocation()));
                            } else if (clientToServer.containsKey(linkOne)) {
                                Action search = null;
                                for (Map.Entry<Location, Action> entry : unApproved.entrySet()) {
                                    if (entry.getValue().getChainId() == actionTwo.getChainId()) {
                                        search = entry.getValue();
                                    }
                                }
                                conflicts.add(new ConflictImpl(approved.get(clientToServer.get(linkOne)), new ChainImpl(search), actionOne.getLocation()));
                            } else {
                                Action search = null;
                                for (Map.Entry<Location, Action> entry : unApproved.entrySet()) {
                                    if (entry.getValue().getChainId() == actionTwo.getChainId()) {
                                        search = entry.getValue();
                                    }
                                }
                                conflicts.add(new ConflictImpl(new ChainImpl(search), approved.get(clientToServer.get(linkTwo)), actionOne.getLocation()));
                            }
                        }
                    } else {
                        if (typeOne == 1) {
                            conflicts.add(new ConflictImpl(new ChainImpl(actionOne), approved.get(clientToServer.get(linkTwo)), actionOne.getLocation()));
                        } else {
                            conflicts.add(new ConflictImpl(approved.get(clientToServer.get(linkOne)), new ChainImpl(actionTwo), actionOne.getLocation()));
                        }
                    }
                    found = true;
                    break;
                }
            }
            if(!found) {
                unApproved.put(actionOne.getLocation(), actionOne);
            }
        }
        return conflicts;
    }

    public ArrayList<ConflictWithSuffix> getSuffix(List<ConflictImpl> conflicts, HashMap<String, Integer> clientToServer, HashMap<Location, Action> unApproved, int preferableSize) {
        ArrayList<ConflictWithSuffix> result = new ArrayList<>();
        for(ConflictImpl conflict : conflicts) {
            Chain chainOne = conflict.getFirstSolution();
            Chain chainTwo = conflict.getSecondSolution();
            int idOne = chainOne.getId();
            int idTwo = chainTwo.getId();
            ArrayList<Location> followOne = new ArrayList<>(), followTwo = new ArrayList<>();
            int sizeOne = 0, sizeTwo = 0;
            for(Map.Entry<Location, Action> entry : unApproved.entrySet()) {
                if(sizeOne == preferableSize && sizeTwo == preferableSize) break;
                if(entry.getValue().getChainId() == idOne) {
                    followOne.add(entry.getKey());
                    sizeOne++;
                } else if(entry.getValue().getChainId() == idTwo) {
                    sizeTwo++;
                    followTwo.add(entry.getKey());
                }
            }
            ConflictWithSuffix conflictWithSuffix = new ConflictWithSuffix(chainOne, chainTwo, conflict.getCollision(), new ChainImpl(chainOne.getName(), chainOne.getColor(), chainOne.getId(), followOne), new ChainImpl(chainTwo.getName(), chainTwo.getColor(), chainTwo.getId(), followTwo));
            result.add(conflictWithSuffix);
        }
        return result;
    }

    public void applyCorrections(ArrayList<Action> decision, HashMap<String, Integer> clientToServer, List<Chain> approved) {
        for(Action action : decision) {
            int id = action.getChainId();
            approved.get(clientToServer.get("f" + id)).addPart(action.getLocation());
        }
    }

}
