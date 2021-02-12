package ab2.impl.KleeweinOrazeSandner;

import ab2.DFATransition;

public class DFATransitionImpl extends FATransitionImpl implements DFATransition  {

    //TODO: find better way to handle inheritance

    private char symbol;

    public DFATransitionImpl(int from, int to, String symbols, char symbol) {
        super(from, to, symbols);
        this.symbol=symbol;
    }

    @Override
    public char symbol() {
        return symbol;
    }
}
