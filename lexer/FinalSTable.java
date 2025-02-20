package lexer;
import java.util.*;
import errorHandler.ErrorHandler;

public class FinalSTable {
    private SymbolTable symbolTable = new SymbolTable();
    private Stack<String> scopeStack = new Stack<>();
    private String currentScope = "Global";
    private ErrorHandler errorHandler = new ErrorHandler();
    private int lineNumber = 1;
    private List<String> keywords = Arrays.asList("if", "else", "while", "for", "return");

    public void processTokens(List<String> tokens) {
        String currentType = null;
        String scope = "Global"; 

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i).trim();

            if (token.isEmpty()) continue;

            if (token.startsWith("//")) {
                break; 
            }
            if (token.startsWith("/*")) {
                while (i < tokens.size() && !tokens.get(i).endsWith("*/")) {
                   i++;
                }
                continue;
            }

            if (token.equals("System.out.print") || token.equals("System.out.println")) {
                symbolTable.addSymbol(token, "Output", "N/A", scope);
            }
            if (token.equals("Scanner")) {
                symbolTable.addSymbol(token, "Input", "N/A", scope);
            }

            if (tokens.size() > i + 1 && tokens.get(i + 1).equals("(")) {
                symbolTable.addSymbol(token, "Function", "N/A", scope);
                scope = "Local"; // Function introduces a new scope
                continue;
            }
            if (isDataType(token)) {
                currentType = token;
            }
            else if (currentType != null && isIdentifier(token)) {
                String value = "undefined";

                if (i + 2 < tokens.size() && tokens.get(i + 1).equals("=")) {
                    value = tokens.get(i + 2); 
                    i += 2; 
                }

                symbolTable.addSymbol(token, currentType, value, scope);
                currentType = null; 
                continue;
            }
            else if (isIdentifier(token) && i + 1 < tokens.size() && tokens.get(i + 1).equals("=")) {
                String value = (i + 2 < tokens.size()) ? tokens.get(i + 2) : "undefined";
                
                if (symbolTable.hasSymbol(token)) {
                    symbolTable.updateValue(token, value);
                } else {
                    symbolTable.addSymbol(token, "Unknown", value, scope);
                }
                i += 2;
            }

            else if (token.equals("final") && i + 2 < tokens.size()) {
                String constName = tokens.get(i + 2);
                symbolTable.addSymbol(constName, "Constant", "N/A", scope);
                i += 2;
            }
            else if (isOperator(token)) {
                symbolTable.addSymbol(token, "Operator", "N/A", scope);
            }
        }
    }
    private boolean isDataType(String token) {
        return token.equals("int") || token.equals("float") || token.equals("char") || token.equals("String");
    }

    private boolean isIdentifier(String token) {
        return token.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private boolean isOperator(String token) {
        return "+-*/%^=".contains(token);
    }

    public void displaySymbolTable() {
        symbolTable.display();
    }
}
