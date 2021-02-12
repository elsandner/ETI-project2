package ab2.impl.KleeweinOrazeSandner;

import ab2.DFA;
import ab2.DFATransition;
import ab2.FATransition;
import ab2.IllegalCharacterException;

import java.util.Set;

//TODO: implement methods
public class DFAImpl extends FAImpl implements DFA {

    private Set<DFATransition> transitions;

    public DFAImpl(Set<Character> symbols, Set<Integer> acceptingStates, Set<DFATransition> transitions, int numStates) {
        super(symbols, acceptingStates, numStates);
        this.transitions=transitions;
    }

    @Override
    public void reset() {

    }

    @Override
    public int getActState() {
        return 0;
    }

    @Override
    public int doStep(char c) throws IllegalArgumentException, IllegalStateException {
        return 0;
    }

    @Override
    public Integer getNextState(int s, char c) throws IllegalCharacterException, IllegalStateException {
        return null;
    }

    @Override
    public boolean isAcceptingState() {
        return false;
    }

    @Override
    public Set<DFATransition> getTransitions() {
        return this.transitions;
    }
}
