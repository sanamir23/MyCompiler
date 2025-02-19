package dfa;

import java.util.*;

public class DFA {
    public DFAState start;
    public Set<DFAState> states = new HashSet<>();
    public Set<Character> alphabet = new HashSet<>();
    public Map<DFAState, Map<Character, DFAState>> transitionTable = new HashMap<>();
    public Set<DFAState> finalStates = new HashSet<>();
}

