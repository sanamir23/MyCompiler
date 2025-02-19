package nfa;

public class Transition {
    public Character character;
    public State to;

    public Transition(Character character, State to) {
        this.character = character;
        this.to = to;
    }
}
