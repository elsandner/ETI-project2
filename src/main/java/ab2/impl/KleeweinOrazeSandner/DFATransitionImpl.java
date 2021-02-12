package ab2.impl.KleeweinOrazeSandner;

import ab2.DFATransition;

public class DFATransitionImpl extends FATransitionImpl implements DFATransition  {

    private char symbol;

    public DFATransitionImpl(int from, int to, char symbol) {
        super(from, to, null);
        this.symbol=symbol;
    }

    @Override
    public char symbol() {
        return symbol;
    }
}
