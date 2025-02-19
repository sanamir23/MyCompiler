package main;

import nfa.*;
import dfa.*;

import java.util.*;

public class Main {
    private static final Map<Character, Integer> precedence = new HashMap<>();
    private static int stateCounter = 0;
    private static int dfaStateCounter = 0;

    static {
        precedence.put('(', 0);
        precedence.put('|', 1);
        precedence.put('·', 2);
        precedence.put('*', 3);
        precedence.put('+', 3);
        precedence.put('?', 3);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a regular expression as an argument.");
            return;
        }
        String regex = args[0];
        System.out.println("Regex: " + regex);

        String preprocessed = insertConcatenation(regex);
        System.out.println("Preprocessed: " + preprocessed);

        String postfix = regexToPostfix(preprocessed);
        System.out.println("Postfix: " + postfix);

        NFA nfa = buildNFA(postfix);
        System.out.println("NFA built successfully.");
        printTransitionTable(nfa);
        printInitialAndFinalStates(nfa);

        DFA dfa = subsetConstruction(nfa);
        System.out.println("\nDFA built successfully.");
        printDFATransitionTable(dfa);
        printDFAInitialAndFinalStates(dfa);
    }
    
// ---------------------- NFA Construction Methods ----------------------
    
    private static String insertConcatenation(String regex) {
        if (regex.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = regex.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (i > 0) {
                char prev = chars[i - 1];
                if (shouldConcatenate(prev, current)) {
                    sb.append('·');
                }
            }
            sb.append(current);
        }

        return sb.toString();
    }

    private static boolean shouldConcatenate(char prev, char current) {
        boolean prevConcatenatable = isLiteral(prev) || prev == ')' || prev == '*' || prev == '+' || prev == '?';
        boolean currentConcatenatable = isLiteral(current) || current == '(';
        return prevConcatenatable && currentConcatenatable;
    }

    private static boolean isLiteral(char c) {
        return !isOperator(c) && c != '(' && c != ')';
    }

    private static boolean isOperator(char c) {
        return c == '|' || c == '·' || c == '*' || c == '+' || c == '?';
    }

    private static String regexToPostfix(String regex) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : regex.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (!stack.isEmpty()) {
                    stack.pop(); // Pop '('
                }
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && precedence.get(c) <= precedence.getOrDefault(stack.peek(), 0)) {
                    output.append(stack.pop());
                }
                stack.push(c);
            } else {
                output.append(c);
            }
        }

        while (!stack.isEmpty()) {
            output.append(stack.pop());
        }

        return output.toString();
    }

    private static NFA buildNFA(String postfix) {
        Stack<NFA> stack = new Stack<>();

        for (char c : postfix.toCharArray()) {
            if (c == '|') {
                NFA right = stack.pop();
                NFA left = stack.pop();
                stack.push(union(left, right));
            } else if (c == '·') {
                NFA right = stack.pop();
                NFA left = stack.pop();
                stack.push(concatenate(left, right));
            } else if (c == '*' || c == '+' || c == '?') {
                NFA nfa = stack.pop();
                stack.push(applyClosure(nfa, c));
            } else {
                stack.push(createBasicNFA(c));
            }
        }

        return stack.pop();
    }

    private static NFA applyClosure(NFA nfa, char type) {
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

        NFA newNFA = new NFA(start, end);
        newNFA.states.addAll(nfa.states);
        return newNFA;
    }

    private static NFA createBasicNFA(char c) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(c, end);
        return new NFA(start, end);
    }

    private static NFA concatenate(NFA a, NFA b) {
        a.end.addTransition(null, b.start);
        NFA newNFA = new NFA(a.start, b.end);
        newNFA.states.addAll(a.states);
        newNFA.states.addAll(b.states);
        return newNFA;
    }

    private static NFA union(NFA a, NFA b) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition(null, a.start);
        start.addTransition(null, b.start);

        a.end.addTransition(null, end);
        b.end.addTransition(null, end);

        NFA newNFA = new NFA(start, end);
        newNFA.states.addAll(a.states);
        newNFA.states.addAll(b.states);
        return newNFA;
    }

    private static void printTransitionTable(NFA nfa) {
        System.out.println("\nTransition Table for NFA:");
        System.out.println("-------------------------");
        System.out.println("State\tSymbol\tNext State");

        for (State state : nfa.states) {
            for (Transition transition : state.transitions) {
                System.out.println("q" + state.id + "\t" +
                        (transition.character == null ? "ε" : transition.character) + "\t" +
                        "q" + transition.to.id);
            }
        }

        System.out.println("\nTotal number of states: " + nfa.states.size());
    }

    private static void printInitialAndFinalStates(NFA nfa) {
        System.out.println("\nInitial State: q" + nfa.start.id);
        System.out.println("Final State: q" + nfa.end.id);
    }

    // ---------------------- Subset Construction Algorithm ----------------------
    private static DFA subsetConstruction(NFA nfa) {
        DFA dfa = new DFA();
        Set<Character> alphabet = getAlphabet(nfa); // Get all input symbols (excluding ε)
        dfa.alphabet = alphabet;

        // Compute initial DFA state (epsilon closure of NFA's start state)
        Set<State> initialNFAStates = epsilonClosure(Collections.singleton(nfa.start));
        DFAState initialState = new DFAState(initialNFAStates, dfaStateCounter++);
        initialState.isFinal = isFinalState(initialNFAStates, nfa.end);
        dfa.start = initialState;
        dfa.states.add(initialState);
        if (initialState.isFinal) dfa.finalStates.add(initialState);

        Queue<DFAState> queue = new LinkedList<>();
        queue.add(initialState);

        while (!queue.isEmpty()) {
            DFAState currentDFAState = queue.poll();

            for (Character symbol : dfa.alphabet) {
                Set<State> movedStates = move(currentDFAState.nfaStates, symbol);
                Set<State> nextNFAStates = epsilonClosure(movedStates);

                if (nextNFAStates.isEmpty()) continue;

                // Check if this DFA state already exists
                DFAState nextDFAState = findDFAState(dfa.states, nextNFAStates);
                if (nextDFAState == null) {
                    nextDFAState = new DFAState(nextNFAStates, dfaStateCounter++);
                    nextDFAState.isFinal = isFinalState(nextNFAStates, nfa.end);
                    dfa.states.add(nextDFAState);
                    if (nextDFAState.isFinal) dfa.finalStates.add(nextDFAState);
                    queue.add(nextDFAState);
                }

                // Update transition table
                Map<Character, DFAState> transitions = dfa.transitionTable.computeIfAbsent(currentDFAState, k -> new HashMap<>());
                transitions.put(symbol, nextDFAState);
            }
        }

        return dfa;
    }

    // Compute epsilon closure of a set of NFA states
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        stack.addAll(states);

        while (!stack.isEmpty()) {
            State current = stack.pop();
            for (Transition transition : current.transitions) {
                if (transition.character == null && !closure.contains(transition.to)) {
                    closure.add(transition.to);
                    stack.push(transition.to);
                }
            }
        }

        return closure;
    }

    // Move from a set of NFA states on a symbol (excluding ε)
    private static Set<State> move(Set<State> states, Character symbol) {
        Set<State> result = new HashSet<>();
        for (State state : states) {
            for (Transition transition : state.transitions) {
                if (transition.character != null && transition.character.equals(symbol)) {
                    result.add(transition.to);
                }
            }
        }
        return result;
    }

    // Check if any state in the set is the NFA's final state
    private static boolean isFinalState(Set<State> states, State nfaFinalState) {
        return states.contains(nfaFinalState);
    }

    // Get all input symbols (excluding ε) from the NFA
    private static Set<Character> getAlphabet(NFA nfa) {
        Set<Character> alphabet = new HashSet<>();
        for (State state : nfa.states) {
            for (Transition transition : state.transitions) {
                if (transition.character != null) {
                    alphabet.add(transition.character);
                }
            }
        }
        return alphabet;
    }

    // Check if a DFA state with the same NFA states already exists
    private static DFAState findDFAState(Set<DFAState> dfaStates, Set<State> nfaStates) {
        for (DFAState dfaState : dfaStates) {
            if (dfaState.nfaStates.equals(nfaStates)) {
                return dfaState;
            }
        }
        return null;
    }

    // ---------------------- Output Methods ----------------------
    private static void printDFATransitionTable(DFA dfa) {
        System.out.println("\nTransition Table for DFA:");
        System.out.println("-------------------------");
        System.out.print("State\t");

        // Print header with symbols
        for (Character symbol : dfa.alphabet) {
            System.out.print(symbol + "\t");
        }
        System.out.println();

        // Print transitions
        for (DFAState state : dfa.states) {
            System.out.print("q" + state.id + "\t");
            for (Character symbol : dfa.alphabet) {
                DFAState nextState = dfa.transitionTable.getOrDefault(state, new HashMap<>()).get(symbol);
                System.out.print((nextState != null ? "q" + nextState.id : "-") + "\t");
            }
            System.out.println();
        }

        System.out.println("\nTotal number of DFA states: " + dfa.states.size());
    }

    private static void printDFAInitialAndFinalStates(DFA dfa) {
        System.out.println("\nInitial DFA State: q" + dfa.start.id);
        System.out.print("Final DFA States: ");
        for (DFAState finalState : dfa.finalStates) {
            System.out.print("q" + finalState.id + " ");
        }
        System.out.println();
    }
}
