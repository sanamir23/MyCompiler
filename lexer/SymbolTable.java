package lexer;
import java.util.*;


class SymbolTable {
    private List<Symbol> symbols = new ArrayList<>();

    public void addSymbol(String name, String type, String value, String scope) {
        for (Symbol sym : symbols) {
        	if(sym.name.equals(name)) {
        		sym.value=value;
        		return;
        	}
        }
    	symbols.add(new Symbol(name, type, value, scope));
    }
    public String getValue(String name) {
        for (Symbol symbol : symbols) {
            if (symbol.name.equals(name)) {
                return symbol.value;
            }
        }
        return "undefined"; 
    }
    public void updateValue(String name, String value) {
        for (Symbol symbol : symbols) {
            if (symbol.name.equals(name)) {
                symbol.value = value;
                return;
            }
        }
    }


    public boolean hasSymbol(String name) {
        for (Symbol symbol : symbols) {
            if (symbol.name.equals(name)) {
                return true;
            }
        }
        return false;
    }



    public String getType(String name) {
        for (Symbol symbol : symbols) {
            if (symbol.name.equals(name)) {
                return symbol.type;
            }
        }
        return "Unknown";
    }

    public void display() {
        System.out.println("-------------------------------------------------");
        System.out.printf("%-15s %-12s %-15s %-10s%n", "Name", "Type", "Value", "Scope");
        System.out.println("-------------------------------------------------");
        for (Symbol symbol : symbols) {
            symbol.display();
        }
    }
}
