package main;

import nfa.*;
import dfa.*;
import lexer.*;

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
        String code = "x = 10;";
        System.out.println("Input Code: " + code);

        // Tokenize the input code
        List<String> tokens = Lexer.tokenize(code);
        System.out.println("\nTokens: " + tokens);

        // Build the NFA/DFA for validation
        String combinedRegex = Lexer.SIMPLIFIED_REGEX;
        System.out.println("\nCombined Regex: " + combinedRegex);
        
        String preprocessed = insertConcatenation(combinedRegex);
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
        
        for (String token : tokens) {
            boolean isValid = validateToken(dfa, token);
            System.out.println("Token: " + token + " -> " + (isValid ? "Valid" : "Invalid"));
        }
    }
    
    private static boolean validateToken(DFA dfa, String token) {
        DFAState currentState = dfa.start;
        for (char c : token.toCharArray()) {
            System.out.println("Processing char: " + c + " in state q" + currentState.id);
            Map<Character, DFAState> transitions = dfa.transitionTable.get(currentState);
            if (transitions == null || !transitions.containsKey(c)) {
                return false;
            }
            currentState = transitions.get(c);
        }
        return dfa.finalStates.contains(currentState);
    }
    
    // --- Keep your existing regexToPostfix, NFA, and DFA conversion routines as-is ---
    // (They still support only a simplified subset of regex syntax.)
    
    private static String insertConcatenation(String regex) {
        StringBuilder sb = new StringBuilder();
        boolean inBracket = false; // Track if we're inside a character class (e.g., [a-z])
        boolean escape = false; // Track if the current character is escaped

        for (int i = 0; i < regex.length(); i++) {
            char current = regex.charAt(i);

            if (escape) {
                // If the current character is escaped, append it and reset the escape flag
                sb.append(current);
                escape = false;
                continue;
            }

            if (current == '\\') {
                // If the current character is a backslash, set the escape flag
                sb.append(current);
                escape = true;
                continue;
            }

            if (current == '[') {
                // If we encounter an opening bracket, mark that we're inside a character class
                inBracket = true;
            } else if (current == ']') {
                // If we encounter a closing bracket, mark that we're outside a character class
                inBracket = false;
            }

            if (i > 0 && !inBracket) {
                char previous = regex.charAt(i - 1);

                // Check if we need to insert a concatenation operator
                if (shouldConcatenate(previous, current)) {
                    sb.append('·');
                }
            }

            sb.append(current);
        }

        return sb.toString();
    }

    private static boolean shouldConcatenate(char previous, char current) {
        // Cases where concatenation should be inserted:
        // 1. Between two literals (e.g., 'a' and 'b' in "ab")
        // 2. Between a literal and an opening parenthesis (e.g., 'a' and '(' in "a(")
        // 3. Between a closing parenthesis and a literal (e.g., ')' and 'a' in ")a")
        // 4. Between a quantifier (*, +, ?) and a literal or opening parenthesis
        // 5. Between a closing bracket and a literal or opening parenthesis

        boolean isPreviousLiteral = isLiteral(previous);
        boolean isCurrentLiteral = isLiteral(current);

        boolean isPreviousQuantifier = isQuantifier(previous);
        boolean isPreviousClosingBracket = previous == ']';
        boolean isPreviousClosingParenthesis = previous == ')';

        boolean isCurrentOpeningParenthesis = current == '(';

        return (isPreviousLiteral && isCurrentLiteral) ||
               (isPreviousLiteral && isCurrentOpeningParenthesis) ||
               (isPreviousClosingParenthesis && isCurrentLiteral) ||
               (isPreviousClosingParenthesis && isCurrentOpeningParenthesis) ||
               (isPreviousQuantifier && isCurrentLiteral) ||
               (isPreviousQuantifier && isCurrentOpeningParenthesis) ||
               (isPreviousClosingBracket && isCurrentLiteral) ||
               (isPreviousClosingBracket && isCurrentOpeningParenthesis);
    }

    private static boolean isLiteral(char c) {
        // A character is a literal if it's not an operator, parenthesis, or bracket
        return !isOperator(c) && c != '(' && c != ')' && c != '[' && c != ']';
    }

    private static boolean isQuantifier(char c) {
        // Quantifiers in regex: *, +, ?
        return c == '*' || c == '+' || c == '?';
    }

    private static String regexToPostfix(String regex) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        boolean escape = false;
        
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (escape) {
                output.append('\\').append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses in regex");
                }
                stack.pop(); // pop '('
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
            if (stack.peek() == '(') {
                throw new IllegalArgumentException("Mismatched parentheses in regex");
            }
            output.append(stack.pop());
        }
        return output.toString();
    }

    private static boolean isOperator(char c) {
        return c == '|' || c == '·' || c == '*' || c == '+' || c == '?';
    }

    private static NFA buildNFA(String postfix) {
        Stack<NFA> stack = new Stack<>();
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);
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
        newNFA.states.add(start);
        newNFA.states.add(end);
        return newNFA;
    }

    private static NFA createBasicNFA(char c) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(c, end);
        NFA nfa = new NFA(start, end);
        nfa.states.add(start);
        nfa.states.add(end);
        return nfa;
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
        newNFA.states.add(start);
        newNFA.states.add(end);
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

    private static DFA subsetConstruction(NFA nfa) {
        DFA dfa = new DFA();
        Set<Character> alphabet = getAlphabet(nfa);
        System.out.println("DFA Alphabet: " + alphabet);
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
            DFAState currentDFAState = queue.poll();
            for (Character symbol : dfa.alphabet) {
                Set<State> movedStates = move(currentDFAState.nfaStates, symbol);
                Set<State> nextNFAStates = epsilonClosure(movedStates);
                if (nextNFAStates.isEmpty()) continue;
                DFAState nextDFAState = findDFAState(dfa.states, nextNFAStates);
                if (nextDFAState == null) {
                    nextDFAState = new DFAState(nextNFAStates, dfaStateCounter++);
                    nextDFAState.isFinal = isFinalState(nextNFAStates, nfa.end);
                    dfa.states.add(nextDFAState);
                    if (nextDFAState.isFinal) dfa.finalStates.add(nextDFAState);
                    queue.add(nextDFAState);
                }
                Map<Character, DFAState> transitions = dfa.transitionTable.computeIfAbsent(currentDFAState, k -> new HashMap<>());
                transitions.put(symbol, nextDFAState);
            }
        }
        return dfa;
    }

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

    private static boolean isFinalState(Set<State> states, State nfaFinalState) {
        return states.contains(nfaFinalState);
    }

    private static Set<Character> getAlphabet(NFA nfa) {
        Set<Character> alphabet = new HashSet<>();
        for (State state : nfa.states) {
            for (Transition transition : state.transitions) {
                Character c = transition.character;
                if (c != null) {
                    alphabet.add(c);
                }
            }
        }
        // Add additional characters that might appear in tokens
        alphabet.addAll(Arrays.asList('=', ';', '(', ')', '{', '}', '[', ']', '+', '-', '*', '/', '%', '^', '<', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));
        return alphabet;
    }

    private static DFAState findDFAState(Set<DFAState> dfaStates, Set<State> nfaStates) {
        for (DFAState dfaState : dfaStates) {
            if (dfaState.nfaStates.equals(nfaStates)) {
                return dfaState;
            }
        }
        return null;
    }

    private static void printDFATransitionTable(DFA dfa) {
        System.out.println("\nTransition Table for DFA:");
        System.out.println("-------------------------");
        System.out.print("State\t");
        for (Character symbol : dfa.alphabet) {
            System.out.print(symbol + "\t");
        }
        System.out.println();
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
