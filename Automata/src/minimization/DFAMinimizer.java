package minimization;

import automata.DFA;
import automata.MapBasedDFA;
import automata.State;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// TODO : Document me.
// TODO : Bug Check
public class DFAMinimizer {
    public static DFA minimize(DFA dfa) {
        DFAMinimizer minimizer = new DFAMinimizer(dfa);
        return minimizer.minimize();
    }
    private DFA originalDFA;
    private Set<Character> alphabet;
    private Set<State> finalStates;
    private Set<State> nonFinalStates;
    private HashMap<State, Set<State>> mergedStates;
    private HashMap<State, HashMap<State, Boolean>> distinguishableStates;

    private DFAMinimizer(DFA dfa) {
        finalStates = dfa.finalStates();

        nonFinalStates = dfa.allStates();
        nonFinalStates.removeAll(finalStates);

        alphabet = dfa.alphabet();

        this.originalDFA = dfa;

        mergedStates = new HashMap<State, Set<State>>();
        distinguishableStates = new HashMap<State, HashMap<State, Boolean>>();
    }

    private boolean isDistinguishable(State state1, State state2) {
        if (distinguishableStates.containsKey(state1) && distinguishableStates.get(state1).containsKey(state2)) {
            return distinguishableStates.get(state1).get(state2);
        }

        //Only accept TT, FF. Not TF, FT.
        if (!(state1.isFinal() ^ state2.isFinal())) {
            saveDistinguishability(state1, state2, true);
            return true;
        }
        for (Character character : alphabet) {
            State state1Trans = originalDFA.transition(state1, character);
            State state2Trans = originalDFA.transition(state2, character);

            boolean subdistinguishable = isDistinguishable(state1Trans, state2Trans);

            if (subdistinguishable) {
                saveDistinguishability(state1, state2, true);
                return true;
            }
        }

        saveDistinguishability(state1, state2, false);
        return false;
    }

    private void saveDistinguishability(State state1, State state2, boolean b) {
        if (distinguishableStates.containsKey(state1)) {
            distinguishableStates.get(state1).put(state2, b);
        } else {
            HashMap<State, Boolean> subMap = new HashMap<State, Boolean>();
            subMap.put(state2, b);
            distinguishableStates.put(state1, subMap);
        }
        if (distinguishableStates.containsKey(state2)) {
            distinguishableStates.get(state2).put(state1, b);
        } else {
            HashMap<State, Boolean> subMap = new HashMap<State, Boolean>();
            subMap.put(state1, b);
            distinguishableStates.put(state2, subMap);
        }
    }

    private void setMerge(State state1, State state2) {
        if (mergedStates.containsKey(state2)) {
            if (mergedStates.containsKey(state2)) {
                Set<State> oldMerges1 = mergedStates.get(state1);
                Set<State> oldMerges2 = mergedStates.get(state2);
                oldMerges1.addAll(oldMerges2);
                for(State state: oldMerges2) {
                    mergedStates.put(state, oldMerges1);
                }
            } else {
                Set<State> oldMerges = mergedStates.get(state1);
                oldMerges.add(state2);
                mergedStates.put(state2, oldMerges);
            }
        } else if (mergedStates.containsKey(state2)) {
            Set<State> oldMerges = mergedStates.get(state2);
            oldMerges.add(state1);
            mergedStates.put(state1, oldMerges);
        } else {
            Set<State> setToMerge = new HashSet<State>();
            setToMerge.add(state1);
            setToMerge.add(state2);
            mergedStates.put(state1, setToMerge);
            mergedStates.put(state2, setToMerge);
        }
    }

    private DFA minimize() {
        
        // Find needed merges
        State[] finalStateArray = finalStates.toArray(new State[0]);
        for (int i = 0; i < finalStateArray.length; i++) {
            for (int j = i + 1; j < finalStateArray.length; j++) {
                State firstState = finalStateArray[i];
                State secondState = finalStateArray[j];

                if (!isDistinguishable(firstState, secondState)) {
                    setMerge(firstState, secondState);
                }
            }
        }
        State[] nonFinalStateArray = nonFinalStates.toArray(new State[0]);
        for (int i = 0; i < nonFinalStateArray.length; i++) {
            for (int j = i + 1; j < nonFinalStateArray.length; j++) {
                State firstState = nonFinalStateArray[i];
                State secondState = nonFinalStateArray[j];

                if (!isDistinguishable(firstState, secondState)) {
                    setMerge(firstState, secondState);
                }
            }
        }
        
        // Create new states for the merges
        // Map old states being merged to the new state (or just the old state)
        HashMap<State, State> stateMergeMap = new HashMap<State, State>();
        HashMap<Set<State>, State> mergeMap = new HashMap<Set<State>, State>();

        for(State currState: originalDFA.allStates()) {
            if(mergedStates.containsKey(currState)) {
                Set<State> mergingStates = mergedStates.get(currState);
                if(mergeMap.containsKey(mergingStates)) {
                    State newState = mergeMap.get(mergingStates);
                } else {
                    // TODO : token merging
                    State newState = new State();
                    if(currState.isFinal()) newState.setFinal(true);
                    mergeMap.put(mergingStates, newState);
                    stateMergeMap.put(currState, newState);
                }
            } else {
                stateMergeMap.put(currState, currState);
            }
        }
        
        State startState = stateMergeMap.get(originalDFA.startState());
        MapBasedDFA dfa = new MapBasedDFA(startState);

        addStates(originalDFA.startState(), dfa, stateMergeMap, new HashSet<State>());

        return dfa;
    }

    private void addStates(State origCurrState, MapBasedDFA dfa, HashMap<State, State> stateMergeMap, Set<State> alreadyChecked) {
        State newCurrState = stateMergeMap.get(origCurrState);
        
        alreadyChecked.add(newCurrState);
        for(Character letter: alphabet) {
            State origNextState = originalDFA.transition(origCurrState, letter);
            State newNextState = stateMergeMap.get(origNextState);
            dfa.addTransition(newCurrState, letter, newNextState);
            if(!alreadyChecked.contains(newNextState)) {
                addStates(origNextState, dfa, stateMergeMap, alreadyChecked);
            }
        }
    }
}
