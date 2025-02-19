package dfa;

import nfa.State;
import java.util.Set;

public class DFAState {
    public Set<State> nfaStates;
    public int id;
    public boolean isFinal;

    public DFAState(Set<State> nfaStates, int id) {
        this.nfaStates = nfaStates;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DFAState) {
            return this.nfaStates.equals(((DFAState) obj).nfaStates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nfaStates.hashCode();
    }
}
