package lexer;

import java.util.regex.*;

public class RegexTester {
    public static final String TOKEN_REGEX =
          "(//.*)|" +                        // Single-line comments
          "(/\\*.*?\\*/)|" +                 // Multi-line comments
          "\\b(int|double|bool|char)\\b|" +  // Data types
          "\\b(true|false)\\b|" +            // Boolean values
          "'[a-zA-Z0-9]'" +                  // Character literals
          "|\"[^\"]*\"" +                   // Strings
          "|\\b[a-z]+\\b|" +                 // Variable names (only lowercase letters)
          "|\\b\\d+\\b|" +                   // Integer numbers
          "|\\b\\d+\\.\\d{1,5}\\b|" +        // Decimal numbers
          "|[+\\-*/%^=]|" +                  // Arithmetic operators and assignment
          "|[;,(){}]";                      // Delimiters

    public static void main(String[] args) {
        String code = "int x = 10;";
        // Suppose you have a tokenization routine that splits this into tokens:
        String[] tokens = {"int", "x", "=", "10", ";"};
        Pattern pattern = Pattern.compile(TOKEN_REGEX);

        for (String token : tokens) {
            Matcher matcher = pattern.matcher(token);
            if (matcher.matches()) {
                System.out.println("Token: \"" + token + "\" -> Valid");
            } else {
                System.out.println("Token: \"" + token + "\" -> Invalid");
            }
        }
    }
}
