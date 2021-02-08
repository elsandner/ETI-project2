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
        Factory factory = new FactoryImpl();
        int offsetCurr = 1000;
        int offsetNext = -1000;

        // combine symbols
        Set<Character> symbolsNew = new HashSet<>();
        symbolsNew.addAll(this.symbols);
        symbolsNew.addAll(a.getSymbols());

        // combine transitions
        Set<FATransition> transitionsNew = new HashSet<>();
        for (FATransition t : this.transitions) {
            transitionsNew.add(factory.createTransition(t.from() + offsetCurr, t.to() + offsetCurr, t.symbols()));
        }
        for (FATransition t : a.getTransitions()) {
            transitionsNew.add(factory.createTransition(t.from() + offsetNext, t.to() + offsetNext, t.symbols()));
        }

        // combine accepting states
        Set<Integer> acceptingStatesNew = new HashSet<>();
        for (Integer state : this.acceptingStates) {
            acceptingStatesNew.add(state + offsetCurr);
        }
        for (Integer state : a.getAcceptingStates()) {
            acceptingStatesNew.add(state + offsetNext);
        }

        int numStatesNew = a.getNumStates() + this.numStates;

        // epsilon transitions into each FA
        transitionsNew.add(factory.createTransition(0, offsetCurr, ""));
        transitionsNew.add(factory.createTransition(0, offsetNext, ""));

        return factory.createFA(numStatesNew, symbolsNew, acceptingStatesNew, transitionsNew);


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

    /**
     * helper function to split edges with more then one symbol e.g "abc" into separate edges and nodes
     */
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

        for (int i = 0; i < word.size(); i++) {
            Character c = word.get(i);
            Set<Integer> currNew = new HashSet<>();
            boolean hadEpsilon = false;
            for (FATransition t : this.transitions) {
                if (curr.contains(t.from()) && t.symbols().equals("")) {
                    curr.add(t.to());
                    currNew.add(t.to());
                    hadEpsilon = true;
                }
            }
            for (FATransition t : this.transitions) {
                if (curr.contains(t.from()) && t.symbols().equals(c.toString())) {
                    currNew.add(t.to());
                    hadEpsilon = false;
                }
            }
            if (hadEpsilon) i--;
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
            if (this.acceptingStates.contains(0)) return false;
        }
        return true;
    }

    /**
     * helper function to get the possible reachable states
     *
     * @param reachable       set of starting states
     * @param possibleSymbols the symbols allowed to traverse to other states
     * @return a set of all reachable states given a starting configuration
     */
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
        return !isFinite();
    }

    @Override
    public boolean isFinite() {
        Set<Integer> visitedStates = new HashSet<>();
        visitedStates.add(0);
        Set<String> allSymbols = new HashSet<>();
        allSymbols.add("");
        for (Character c : this.symbols) {
            allSymbols.add(c.toString());
        }
        visitedStates = loopForReachable(visitedStates, allSymbols);
        Set<Integer> comp = new HashSet<>(visitedStates);
        for (FATransition t : this.transitions) {
            if (comp.contains(t.from())) {
                if (t.from() == t.to() || t.to() == 0) return false;
                if (!visitedStates.remove(t.to())) return false;
            }
        }
        return true;
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
