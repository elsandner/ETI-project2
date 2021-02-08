package ab2.impl.KleeweinOrazeSandner;

import ab2.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*********************************
 Created by Fabian Oraze on 08.02.21
 *********************************/

public class FAImpl implements FA {

    private Set<Character> symbols;
    private Set<Integer> acceptingStates;
    private Set<FATransition> transitions;
    private int numStates;
    private int count;

    public FAImpl(Set<Character> symbols, Set<Integer> acceptingStates, Set<FATransition> transitions, int numStates) {
        this.symbols = symbols;
        this.acceptingStates = acceptingStates;
        this.transitions = transitions;
        this.numStates = numStates;
        this.count = -1;
        extractNodes();
    }

    @Override
    public Set<Character> getSymbols() {
        return this.symbols;
    }

    @Override
    public Set<Integer> getAcceptingStates() {
        return this.acceptingStates;
    }

    @Override
    public boolean isAcceptingState(int s) throws IllegalStateException {
        return acceptingStates.contains(s);
    }

    @Override
    public Set<? extends FATransition> getTransitions() {
        return this.transitions;
    }

    @Override
    public int getNumStates() {
        return this.numStates;
    }

    @Override
    public FA union(FA a) {
        return null;
    }

    @Override
    public FA intersection(FA a) {
        return null;
    }

    @Override
    public FA minus(FA a) {
        return null;
    }

    @Override
    public FA concat(FA a) {
        return null;
    }

    @Override
    public FA complement() {
        return null;
    }

    @Override
    public FA kleeneStar() {
        return null;
    }

    @Override
    public FA plus() {
        return null;
    }

    @Override
    public RSA toRSA() {
        return null;
    }

    private void extractNodes() {
        for (FATransition t : this.transitions) {
            if (t.symbols().length() > 1) {
                Character cNew = t.symbols().charAt(0);
                String subString = t.symbols().substring(1);
                FATransition tNew = new FATransitionImpl(t.from(), this.count--, cNew.toString());
                FATransition tSecond = new FATransitionImpl(tNew.to(), t.to(), subString);
                this.transitions.remove(t);
                this.transitions.add(tNew);
                this.transitions.add(tSecond);
                this.numStates++;
                extractNodes();
                break;
            }
        }
    }

    @Override
    public boolean accepts(String w) throws IllegalCharacterException {
        if (w.equals("")) return acceptsEpsilon();
        List<Character> word = new ArrayList<>();
        for (int i = 0; i < w.length(); i++) {
            word.add(w.charAt(i));
        }
        Set<Integer> curr = new HashSet<>();
        curr.add(0);
        for (FATransition t : this.transitions) {
            if (t.symbols().equals("") && t.from() == 0) curr.add(t.to());
        }
        for (Character c : word) {
            Set<Integer> currNew = new HashSet<>();
            for (FATransition t : this.transitions) {
                if (curr.contains(t.from()) && t.symbols().equals("")) {
                    curr.add(t.to());
                    currNew.add(t.to());
                }
            }
            for (FATransition t : this.transitions) {
                if (curr.contains(t.from()) && t.symbols().equals(c.toString())) {
                    currNew.add(t.to());
                }
            }
            curr = currNew;
        }
        for (Integer state : this.acceptingStates) {
            if (curr.contains(state)) return true;
        }
        return false;
    }

    @Override
    public boolean acceptsNothing() {
        if (this.acceptingStates.size() == 0) {
            // skip else
        } else {
            Set<Integer> reachable = new HashSet<>();
            Set<String> allSymbols = new HashSet<>();
            for (Character c : this.symbols) {
                allSymbols.add(c.toString());
            }
            reachable = loopForReachable(reachable, allSymbols);

            for (Integer state : this.acceptingStates) {
                if (reachable.contains(state)) return false;
            }
        }
        return true;
    }

    private Set<Integer> loopForReachable(Set<Integer> reachable, Set<String> possibleSymbols) {
        Set<Integer> reachableNew = new HashSet<>(reachable);
        for (FATransition t : this.transitions) {
            if (t.from() == 0 && possibleSymbols.contains(t.symbols())) reachableNew.add(t.to());
            if (reachableNew.contains(t.from()) && possibleSymbols.contains(t.symbols())) reachableNew.add(t.to());
        }
        if (!reachableNew.equals(reachable)) return loopForReachable(reachableNew, possibleSymbols);
        else return reachableNew;
    }

    @Override
    public boolean acceptsEpsilonOnly() {
        Set<Integer> reachableOthers = new HashSet<>();
        Set<String> allSymbols = new HashSet<>();
        for (FATransition t : this.transitions) {
            if (!t.symbols().equals("")) allSymbols.add(t.symbols());
        }
        reachableOthers = loopForReachable(reachableOthers, allSymbols);
        for (Integer state : this.acceptingStates) {
            if (reachableOthers.contains(state)) return false;
        }
        return acceptsEpsilon();

    }

    @Override
    public boolean acceptsEpsilon() {
        if (this.acceptingStates.size() == 1 && this.acceptingStates.contains(0)) {
            return true;
        }
        Set<Integer> reachableEpsilon = new HashSet<>();
        reachableEpsilon.add(0);
        Set<String> epsilon = new HashSet<>();
        epsilon.add("");
        reachableEpsilon = loopForReachable(reachableEpsilon, epsilon);
        for (Integer state : this.acceptingStates) {
            if (reachableEpsilon.contains(state)) return true;
        }
        return false;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public boolean isFinite() {
        return false;
    }

    @Override
    public boolean subSetOf(FA a) {
        return false;
    }

    @Override
    public boolean equalTo(FA b) {
        return false;
    }

    @Override
    public Boolean equalsPlusAndStar() {
        return null;
    }
}
