package lexer;

import nfa.NFA;
import nfa.State;
import nfa.Transition;
import dfa.DFA;
import dfa.DFAState;

import java.util.*;

public class NFABuilder {

    private static int stateCounter = 0;
    private static int dfaStateCounter = 0;
    private static final Map<Character, Integer> precedence = new HashMap<>();
    
    static {
        precedence.put('(', 0);
        precedence.put('|', 1);
        precedence.put('·', 2);
        precedence.put('*', 3);
        precedence.put('+', 3);
        precedence.put('?', 3);
    }
    
    // Create an NFA that recognizes a single literal character.
    public static NFA createBasicNFA(char c) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(c, end);
        NFA nfa = new NFA(start, end);
        nfa.states.add(start);
        nfa.states.add(end);
        return nfa;
    }
    
    // Create an NFA that recognizes any character in the range [start, end].
    public static NFA createCharRange(char startChar, char endChar) {
        NFA result = null;
        for (char c = startChar; c <= endChar; c++) {
            NFA charNFA = createBasicNFA(c);
            if (result == null) {
                result = charNFA;
            } else {
                result = union(result, charNFA);
            }
        }
        return result;
    }
    
    // Build an NFA that recognizes one or more occurrences (plus) of the given NFA.
    public static NFA plus(NFA nfa) {
        // nfa+ is equivalent to nfa concatenated with nfa*
        return concatenate(nfa, star(nfa));
    }
    
    // Build an NFA that recognizes zero or more occurrences (star) of the given NFA.
    public static NFA star(NFA nfa) {
        return applyClosure(nfa, '*');
    }
    
    // Build an NFA that recognizes the exact string s.
    public static NFA buildStringNFA(String s) {
        if (s == null || s.isEmpty()) {
            State state = new State(stateCounter++);
            return new NFA(state, state);
        }
        NFA result = createBasicNFA(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            NFA next = createBasicNFA(s.charAt(i));
            result = concatenate(result, next);
        }
        return result;
    }
    
    // Return the union of two NFAs.
    public static NFA union(NFA a, NFA b) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(null, a.start);
        start.addTransition(null, b.start);
        a.end.addTransition(null, end);
        b.end.addTransition(null, end);
        NFA nfa = new NFA(start, end);
        nfa.states.addAll(a.states);
        nfa.states.addAll(b.states);
        nfa.states.add(start);
        nfa.states.add(end);
        return nfa;
    }
    
    // Return the concatenation of two NFAs.
    public static NFA concatenate(NFA a, NFA b) {
        a.end.addTransition(null, b.start);
        NFA nfa = new NFA(a.start, b.end);
        nfa.states.addAll(a.states);
        nfa.states.addAll(b.states);
        return nfa;
    }
    
    // Apply a closure operator to an NFA.
    // type can be '*', '+', or '?'.
    public static NFA applyClosure(NFA nfa, char type) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(null, nfa.start);
        if (type == '*' || type == '?') {
            start.addTransition(null, end);
        }
        nfa.end.addTransition(null, end);
        if (type == '*' || type == '+') {
            nfa.end.addTransition(null, nfa.start);
        }
        NFA result = new NFA(start, end);
        result.states.addAll(nfa.states);
        result.states.add(start);
        result.states.add(end);
        return result;
    }
    
    // Convert an NFA to a DFA using subset construction.
    public static DFA convertToDFA(NFA nfa) {
        DFA dfa = new DFA();
        Set<Character> alphabet = getAlphabet(nfa);
        dfa.alphabet = alphabet;
        Set<State> initialNFAStates = epsilonClosure(Collections.singleton(nfa.start));
        DFAState initialState = new DFAState(initialNFAStates, dfaStateCounter++);
        initialState.isFinal = isFinalState(initialNFAStates, nfa.end);
        dfa.start = initialState;
        dfa.states.add(initialState);
        if (initialState.isFinal) dfa.finalStates.add(initialState);
        Queue<DFAState> queue = new LinkedList<>();
        queue.add(initialState);
        while (!queue.isEmpty()) {
            DFAState current = queue.poll();
            for (Character symbol : alphabet) {
                Set<State> moved = move(current.nfaStates, symbol);
                Set<State> next = epsilonClosure(moved);
                if (next.isEmpty()) continue;
                DFAState nextDFA = findDFAState(dfa.states, next);
                if (nextDFA == null) {
                    nextDFA = new DFAState(next, dfaStateCounter++);
                    nextDFA.isFinal = isFinalState(next, nfa.end);
                    dfa.states.add(nextDFA);
                    if (nextDFA.isFinal) dfa.finalStates.add(nextDFA);
                    queue.add(nextDFA);
                }
                Map<Character, DFAState> trans = dfa.transitionTable.computeIfAbsent(current, k -> new HashMap<>());
                trans.put(symbol, nextDFA);
            }
        }
        return dfa;
    }
    
    // Compute the epsilon closure of a set of NFA states.
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        stack.addAll(states);
        while (!stack.isEmpty()) {
            State s = stack.pop();
            for (Transition t : s.transitions) {
                if (t.character == null && !closure.contains(t.to)) {
                    closure.add(t.to);
                    stack.push(t.to);
                }
            }
        }
        return closure;
    }
    
    // Given a set of NFA states, return all states reachable by the given symbol.
    private static Set<State> move(Set<State> states, Character symbol) {
        Set<State> result = new HashSet<>();
        for (State s : states) {
            for (Transition t : s.transitions) {
                if (t.character != null && t.character.equals(symbol)) {
                    result.add(t.to);
                }
            }
        }
        return result;
    }
    
    // Check if the set of NFA states contains the final state.
    private static boolean isFinalState(Set<State> states, State nfaFinal) {
        return states.contains(nfaFinal);
    }
    
    // Compute the alphabet from all transitions in the NFA.
    private static Set<Character> getAlphabet(NFA nfa) {
        Set<Character> alphabet = new HashSet<>();
        for (State s : nfa.states) {
            for (Transition t : s.transitions) {
                if (t.character != null) {
                    alphabet.add(t.character);
                }
            }
        }
        return alphabet;
    }
    
    // Find a DFAState in the set whose NFA state set equals the given set.
    private static DFAState findDFAState(Set<DFAState> dfaStates, Set<State> nfaStates) {
        for (DFAState d : dfaStates) {
            if (d.nfaStates.equals(nfaStates)) {
                return d;
            }
        }
        return null;
    }
    
    // For debugging: print the DFA transition table.
    public static void printDFATransitionTable(DFA dfa) {
        System.out.println("DFA Transition Table:");
        System.out.print("State\t");
        for (Character c : dfa.alphabet) {
            System.out.print(c + "\t");
        }
        System.out.println();
        for (DFAState s : dfa.states) {
            System.out.print("q" + s.id + "\t");
            for (Character c : dfa.alphabet) {
                DFAState next = dfa.transitionTable.getOrDefault(s, new HashMap<>()).get(c);
                System.out.print((next != null ? "q" + next.id : "-") + "\t");
            }
            System.out.println();
        }
        System.out.println("Total DFA States: " + dfa.states.size());
    }
}
