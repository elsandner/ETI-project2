package ab2.impl.KleeweinOrazeSandner;

import ab2.DFATransition;
import ab2.RSA;

import java.util.Set;

//TODO: implement method
public class RSAImpl extends DFAImpl implements RSA {

    public RSAImpl(Set<Character> symbols, Set<Integer> acceptingStates, Set<DFATransition> transitions, int numStates) {
        super(symbols, acceptingStates, transitions, numStates);
    }

    @Override
    public RSA minimize() {
        return null;
    }
}
