package lexer;

import dfa.DFA;
import dfa.DFAState;
import errorHandler.ErrorHandler;
import nfa.NFA;
import java.util.*;

public class TokenDFABuilder {
    public static void main(String[] args) {
        // Map to hold DFAs for each token type.
        Map<String, DFA> tokenDFAs = new HashMap<>();
        FinalSTable compiler = new FinalSTable();
        ErrorHandler errorHandler = new ErrorHandler();

        
        // INTEGER: one or more digits (0-9)
        NFA digitNFA = NFABuilder.createCharRange('0', '9');
        NFA integerNFA = NFABuilder.plus(digitNFA);
        DFA integerDFA = NFABuilder.convertToDFA(integerNFA);
        tokenDFAs.put("INTEGER", integerDFA);

        // DECIMAL: integer part, dot, fractional part (up to 5 digits)
        NFA integerPart = NFABuilder.plus(NFABuilder.createCharRange('0', '9'));
        NFA dot = NFABuilder.createBasicNFA('.');
        NFA fractionalPart = NFABuilder.plus(NFABuilder.createCharRange('0', '9'));
        NFA decimalNFA = NFABuilder.concatenate(integerPart,
                            NFABuilder.concatenate(dot, fractionalPart));
        DFA decimalDFA = NFABuilder.convertToDFA(decimalNFA);
        tokenDFAs.put("DECIMAL", decimalDFA);

        // IDENTIFIER: one or more lowercase letters (no digits)
        NFA letterNFA = NFABuilder.createCharRange('a', 'z');
        NFA identifierNFA = NFABuilder.plus(letterNFA);
        DFA identifierDFA = NFABuilder.convertToDFA(identifierNFA);
        tokenDFAs.put("IDENTIFIER", identifierDFA);

        // BOOLEAN: "true" or "false"
        NFA trueNFA = NFABuilder.buildStringNFA("true");
        NFA falseNFA = NFABuilder.buildStringNFA("false");
        NFA booleanNFA = NFABuilder.union(trueNFA, falseNFA);
        DFA booleanDFA = NFABuilder.convertToDFA(booleanNFA);
        tokenDFAs.put("BOOLEAN", booleanDFA);

        // OPERATOR: +, -, *, /, %, ^
        NFA opNFA = NFABuilder.createBasicNFA('+');
        opNFA = NFABuilder.union(opNFA, NFABuilder.createBasicNFA('-'));
        opNFA = NFABuilder.union(opNFA, NFABuilder.createBasicNFA('*'));
        opNFA = NFABuilder.union(opNFA, NFABuilder.createBasicNFA('/'));
        opNFA = NFABuilder.union(opNFA, NFABuilder.createBasicNFA('%'));
        opNFA = NFABuilder.union(opNFA, NFABuilder.createBasicNFA('^'));
        DFA operatorDFA = NFABuilder.convertToDFA(opNFA);
        tokenDFAs.put("OPERATOR", operatorDFA);

        // ASSIGNMENT: '='
        NFA assignNFA = NFABuilder.createBasicNFA('=');
        DFA assignDFA = NFABuilder.convertToDFA(assignNFA);
        tokenDFAs.put("ASSIGNMENT", assignDFA);

        // DELIMITER: ; , ( ) { } [ ]
        NFA delimNFA = NFABuilder.createBasicNFA(';');
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA(','));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA('('));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA(')'));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA('{'));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA('}'));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA('['));
        delimNFA = NFABuilder.union(delimNFA, NFABuilder.createBasicNFA(']'));
        DFA delimDFA = NFABuilder.convertToDFA(delimNFA);
        tokenDFAs.put("DELIMITER", delimDFA);

        // Print a summary of DFAs
        for (Map.Entry<String, DFA> entry : tokenDFAs.entrySet()) {
            System.out.println("DFA for " + entry.getKey() + ":");
            NFABuilder.printDFATransitionTable(entry.getValue());
            System.out.println();
        }

        
        String code = "xAz = 10; y = 3.141596; z = x + y; if (true) { z = z * 2; } /* comment */";

        // Use your existing Lexer to get tokens from the code.
        List<String> tokens = Lexer.tokenize(code);
        System.out.println("Input Code: " + code);
        System.out.println("Tokens (from Lexer): " + tokens);
        
        // Classify each token by running it against all DFAs.
        for (String token : tokens) {
            String type = classifyToken(token, tokenDFAs);
            System.out.println("Token: \"" + token + "\" classified as: " + type);
        }
        errorHandler.checkErrors(code);
        errorHandler.displayErrors();
        
        compiler.processTokens(tokens);
        compiler.displaySymbolTable();
    }
    
    // Validate a token using a DFA by simulating its transitions.
    private static boolean validateToken(DFA dfa, String token) {
        DFAState currentState = dfa.start;
        for (char c : token.toCharArray()) {
            Map<Character, DFAState> transitions = dfa.transitionTable.get(currentState);
            if (transitions == null || !transitions.containsKey(c)) {
                return false;
            }
            currentState = transitions.get(c);
        }
        return dfa.finalStates.contains(currentState);
    }
    
    // Classify a token by checking which DFA in the map accepts it.
    private static String classifyToken(String token, Map<String, DFA> dfas) {
        // Prioritize keywords like BOOLEAN over generic IDENTIFIER
        List<String> priorityOrder = Arrays.asList("BOOLEAN", "INTEGER", "DECIMAL", "OPERATOR", "ASSIGNMENT", "DELIMITER", "IDENTIFIER");

        for (String type : priorityOrder) {
            if (dfas.containsKey(type) && validateToken(dfas.get(type), token)) {
                return type;
            }
        }
        return "UNKNOWN";
    }

}
