package lexer;
import java.util.*;

class Symbol {
    String name;
    String type;
    String value;
    String scope;

    public Symbol(String name, String type, String value, String scope) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-15s %-10s", name, type, value, scope);
    }
    public void display() {
        System.out.printf("%-15s %-12s %-15s %-10s%n", name, type, value, scope);
    }
}
