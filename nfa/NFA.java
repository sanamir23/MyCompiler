package nfa;

import java.util.HashSet;
import java.util.Set;

public class NFA {
    public State start;
    public State end;
    public Set<State> states = new HashSet<>();

    public NFA(State start, State end) {
        this.start = start;
        this.end = end;
        states.add(start);
        states.add(end);
    }
}
