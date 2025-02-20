package lexer;

import java.util.*;
import java.util.regex.*;

public class Lexer {
	
	public static final String COMBINED_REGEX =
		    "(/\\*.*?\\*/)|(//.*)|" +                        // Comments
		    "\\b(true|false)\\b|" +                           // Booleans
		    "[+-]?([0-9]+\\.[0-9]{1,5}|\\.[0-9]{1,5}|[0-9]+)|" + // Numbers (Integers and Decimals)
		    "'[a-z]'|" +                                      // Characters
		    "[+\\-*/%^<>]=?|" +                               // Operators
		    "[=;(){}\\[\\]]|" +                               // Delimiters
		    "[a-z]+";                                         // Identifiers (lowercase only)                                                                                                                               

    public static final Pattern TOKEN_PATTERN = Pattern.compile(COMBINED_REGEX);
    
    public static final String SIMPLIFIED_REGEX =
    	    "(true|false)" +                                            // Booleans
    	    "|((a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)+)" +    // Variable names (identifiers)
    	    "|((0|1|2|3|4|5|6|7|8|9)+)" +                                // Integers
    	    "|(((0|1|2|3|4|5|6|7|8|9)+)\\.((0|1|2|3|4|5|6|7|8|9){1,5}))" + // Decimals (up to 5 decimal places)
    	    "|('[a-z]')" +                                              // Character literal (a single lowercase letter)
    	    "|((\\+)|(\\-)|(\\*)|(\\/)|(%)|(\\^))" +                     // Arithmetic operators (+, -, *, /, %, ^)
    	    "|(=)" +                                                    // Assignment operator
    	    "|([;,(){}])";                                              // Delimiters


    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);

        while (matcher.find()) {
            String token = matcher.group().trim();
            
            // Ignore comments (both single-line and multi-line)
            if (token.startsWith("//") || token.startsWith("/*")) {
                continue;
            }

            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
