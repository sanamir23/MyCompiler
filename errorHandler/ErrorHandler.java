package errorHandler;
import java.util.*;
import java.util.regex.*;

public class ErrorHandler {
    private List<String> errors = new ArrayList<>();
    private int lineNumber = 1; // Assuming single-line input for now

    // Method to check for errors
    public void checkErrors(String code) {
        String[] tokens = code.split(";"); // Splitting by semicolon to process statements
        Set<String> declaredVariables = new HashSet<>();
        
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            // Check for capital letter in variable names (rule: only lowercase allowed)
            Pattern varPattern = Pattern.compile("^([a-zA-Z]+)\\s*=.*");
            Matcher varMatcher = varPattern.matcher(token);
            if (varMatcher.find()) {
                String varName = varMatcher.group(1);
                if (!varName.matches("[a-z]+")) { // Only lowercase allowed
                    errors.add("Error: Variable '" + varName + "' contains uppercase letters (Line " + lineNumber + ")");
                }
            }

            // Check for incorrect decimal precision
            Pattern decimalPattern = Pattern.compile("=\\s*([0-9]+\\.[0-9]+)");
            Matcher decimalMatcher = decimalPattern.matcher(token);
            if (decimalMatcher.find()) {
                String decimalValue = decimalMatcher.group(1);
                if (decimalValue.contains(".") && decimalValue.split("\\.")[1].length() > 5) {
                    errors.add("Error: Decimal '" + decimalValue + "' exceeds 5 decimal places (Line " + lineNumber + ")");
                }
            }

            // Check for similar variable names (basic check: existing variable with a minor difference)
            if (varMatcher.find()) {
                String varName = varMatcher.group(1);
                for (String declared : declaredVariables) {
                    if (declared.equalsIgnoreCase(varName) && !declared.equals(varName)) {
                        errors.add("Warning: Similar variable '" + varName + "' might cause confusion (Line " + lineNumber + ")");
                    }
                }
                declaredVariables.add(varName);
            }
        }
    }

    public void displayErrors() {
        if (errors.isEmpty()) {
            System.out.println("No errors found.");
        } else {
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }
}

