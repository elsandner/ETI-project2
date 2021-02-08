package ab2.impl.KleeweinOrazeSandner;

import ab2.FATransition;

/*********************************
 Created by Fabian Oraze on 08.02.21
 *********************************/

public class FATransitionImpl implements FATransition {

    private int from;
    private int to;
    private String symbols;

    public FATransitionImpl(int from, int to, String symbols) {
        this.from = from;
        this.to = to;
        this.symbols = symbols;
    }

    @Override
    public String symbols() {
        return this.symbols;
    }

    @Override
    public int from() {
        return this.from;
    }

    @Override
    public int to() {
        return this.to;
    }
}
