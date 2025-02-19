package nfa;

import java.util.ArrayList;
import java.util.List;

public class State {
    public int id;
    public List<Transition> transitions = new ArrayList<>();

    public State(int id) {
        this.id = id;
    }

    public void addTransition(Character c, State to) {
        transitions.add(new Transition(c, to));
    }
}
