package ab2.impl.KleeweinOrazeSandner;

import ab2.*;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    //only used as super-constructor
    public FAImpl(Set<Character> symbols, Set<Integer> acceptingStates, int numStates) {
        this.symbols = symbols;
        this.acceptingStates = acceptingStates;
        this.numStates = numStates;
        this.count = -1;
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
            transitionsNew.add(factory.createTransition(-t.from() + offsetNext, -t.to() + offsetNext, t.symbols()));
        }

        // combine accepting states
        Set<Integer> acceptingStatesNew = new HashSet<>();
        for (Integer state : this.acceptingStates) {
            acceptingStatesNew.add(state + offsetCurr);
        }
        for (Integer state : a.getAcceptingStates()) {
            acceptingStatesNew.add(-state + offsetNext);
        }

        int numStatesNew = a.getNumStates() + this.numStates;

        // epsilon transitions into each FA
        transitionsNew.add(factory.createTransition(0, offsetCurr, ""));
        transitionsNew.add(factory.createTransition(0, offsetNext, ""));

        return factory.createFA(numStatesNew, symbolsNew, acceptingStatesNew, transitionsNew);


    }

    @Override
    public FA intersection(FA a) {
        //TODO: implement toRSA() first, then use komplement and union
        return null;
    }

    @Override
    public FA minus(FA a) {
        return null;
    }

    @Override
    public FA concat(FA a) {
        Factory factory = new FactoryImpl();
        int offset=1000;

        int numStatesNew=this.getNumStates()+a.getNumStates();

        // combine symbols
        Set<Character> symbolsNew = new HashSet<>();
        symbolsNew.addAll(this.symbols);
        symbolsNew.addAll(a.getSymbols());

        // combine transitions
        Set<FATransition> transitionsNew = new HashSet<>(this.transitions);

        //transitions from this FA to a
        for(int acceptingState :acceptingStates){
            transitionsNew.add(factory.createTransition(acceptingState,offset,""));
        }

        //transitions in a
        for (FATransition t : a.getTransitions()) {
            transitionsNew.add(factory.createTransition(t.from()+offset, t.to()+offset, t.symbols()));
        }

        Set<Integer> acceptingStatesNew=new HashSet<>();
        for(int state: a.getAcceptingStates()){
            acceptingStatesNew.add(state+offset);
        }


        return factory.createFA(numStatesNew, symbolsNew, acceptingStatesNew, transitionsNew);
    }

    @Override
    public FA complement() {
        RSA rsa = this.toRSA();

        //create new FATransitions
        Set<FATransition> newTransitions = new HashSet<>();
        for(DFATransition rsaT: rsa.getTransitions()){
            String newSymbols = Character.toString(rsaT.symbol());
            newTransitions.add(new FATransitionImpl(rsaT.from(),rsaT.to(),newSymbols));
        }

        //Endzustände und !Endzustände werden vertauscht
        Set<Integer> NewAcceptingStates=new HashSet<>();

        if(!rsa.getAcceptingStates().contains(0))//in case that there is no transition from start-state
            NewAcceptingStates.add(0);

        Set<Integer> checkedStates = new HashSet<>();
        checkedStates.add(0);
        for(FATransition t: rsa.getTransitions()){

            if(!rsa.getAcceptingStates().contains(t.from()) && !checkedStates.contains(t.from())) {
                NewAcceptingStates.add(t.from());
                checkedStates.add(t.from());
            }
            if(!rsa.getAcceptingStates().contains(t.to()) && !checkedStates.contains(t.to())) {
                NewAcceptingStates.add(t.to());
                checkedStates.add(t.to());
            }
        }

        Factory factory = new FactoryImpl();
        FA fa = factory.createFA(this.numStates, this.symbols, NewAcceptingStates, newTransitions);
        return fa;

    }

    @Override
    public FA kleeneStar() {

        Factory factory = new FactoryImpl();
        int offset = 1000;

        //change states
        Set<FATransition> newTransitions = new HashSet<>();
        for (FATransition t : this.transitions) {
            newTransitions.add(factory.createTransition(t.from() + offset, t.to() + offset, t.symbols()));
        }

        //create new enter state
        newTransitions.add(factory.createTransition(0, offset, ""));

        //epsilon transitions from end to offset
        Set<Integer> newAcceptingStates = new HashSet<>();
        for (Integer state : this.acceptingStates) {
            newAcceptingStates.add(state + offset);
            newTransitions.add(factory.createTransition(state + offset, offset, ""));
        }
        newAcceptingStates.add(0);
        return factory.createFA(this.numStates + 1, this.symbols, newAcceptingStates, newTransitions);
    }

    @Override
    public FA plus() {
        Factory factory = new FactoryImpl();
        Set<FATransition> newTransitions = new HashSet<>(this.transitions);
        for (Integer state : this.acceptingStates) {
            newTransitions.add(factory.createTransition(state, 0, ""));
        }

        return factory.createFA(this.numStates, this.symbols, this.acceptingStates, newTransitions);
    }

    @Override
    public RSA toRSA() {
        //1. Epsilon Hüllen bilden
        //2. Algorithmus mit Epsilon Kanten
        //3. Fehlende Kanten hinzufügen
        Factory factory = new FactoryImpl();
        Set<String> epsilon = new HashSet<>();
        epsilon.add("");

        List<Set<Integer>> epsilonCovers=findEpsilonCovers();

        List<Set<Integer>> newStates = new ArrayList<>(); //RSA states as epsilon-cover (= all Z in algorithms table)
        newStates.add(epsilonCovers.get(0));

        Set<DFATransition> newTransitions = new HashSet<>();
        Set<Integer> newAcceptingStates=new HashSet<>();

        List<Set<Integer>> nextZ = new ArrayList<>(); //FIFO Queue //Why is-always-empty warning?
        nextZ.add(epsilonCovers.get(0));//Start algorithm with epsilon cover of start State
        if(isNewAcceptingState(this.acceptingStates, epsilonCovers.get(0)))
            newAcceptingStates.addAll(epsilonCovers.get(0));

        while(!nextZ.isEmpty()){
            Set<Integer> z = nextZ.remove(0);

            for(char currChar: symbols){
                Set<Integer> reached = new HashSet<>();
                for(int from: z) {
                    for (FATransition t : this.transitions) {
                        if (from==t.from() && Character.toString(currChar).equals(t.symbols())){
                            reached.add(t.to());
                        }
                    }
                }

                Set<Integer> reachedEpsilon = new HashSet<>();
                for(int state: reached){
                    reachedEpsilon.addAll(epsilonCovers.get(state));
                }

                if(!allreadyChecked(newStates, reachedEpsilon)){
                   nextZ.add(reachedEpsilon);
                   newStates.add(reachedEpsilon);
                }

                if(isNewAcceptingState(this.acceptingStates, reached))
                    newAcceptingStates.add(newStates.indexOf(reached));

                int from=newStates.indexOf(z); //index of Z
                int to=newStates.indexOf(reached);  //index of current reached
                newTransitions.add(new DFATransitionImpl(from,to, currChar));
            }
        }


        return factory.createRSA(newStates.size(), this.symbols, newAcceptingStates, newTransitions);

    }

    private boolean allreadyChecked(List<Set<Integer>> stateList, Set<Integer> stateB){

        for(Set<Integer> stateA: stateList){//loop all states
            boolean testA=true;
            for(int value: stateA){
                if(!stateB.contains(value))
                    testA=false;
            }

            boolean testB=true;
            for(int value: stateB){//loop all values of state
                if(!stateA.contains(value))
                    testB=false;
            }
            if(testA && testB) return true;
        }
        return false;
    }


    /**
     * helper function to find epsilon covers of all States (used in toRSA)
     */
    private List<Set<Integer>> findEpsilonCovers(){
        Set<Integer> states = new HashSet<>();

        //all default states
        for(int i=0; i<this.numStates;i++)
            states.add(i);

        //all states created with offset
        for(FATransition t: this.transitions){
            states.add(t.from());
            states.add(t.to());
        }

        Set<String> epsilon = new HashSet<>();
        epsilon.add("");

        List<Set<Integer>> epsilonCovers = new ArrayList<>();
        for(int i=0; i<states.size(); i++){
            Set<Integer> reachable = new HashSet<>();
            reachable.add(i);
            epsilonCovers.add(loopForReachable(reachable, epsilon));
        }

        return epsilonCovers;
    }

    /**
     * helper function to figure out if new node created in toRSA is a finite state
     */
    private boolean isNewAcceptingState(Set<Integer> acceptingStates, Set<Integer> testSet){
        for(int state: testSet){
            if(acceptingStates.contains(state))
                return true;
        }
        return false;
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

        for (int i = 0; i < word.size(); i++) {
            Character c = word.get(i);

            Set<String> epsilon = new HashSet<>();
            epsilon.add("");
            Set<Integer> reachable;
            Set<Integer> currNew = new HashSet<>();
            reachable = loopForReachable(curr, epsilon);
            curr.addAll(reachable);

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
            reachable.add(0);
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
                /*if (t.from() == 0 && possibleSymbols.contains(t.symbols()) || t.from() == 0 && t.symbols().equals(""))
                    reachableNew.add(t.to());*/
                if (reachableNew.contains(t.from()) && t.symbols().equals("")) reachableNew.add(t.to());
                if (reachableNew.contains(t.from()) && possibleSymbols.contains(t.symbols())) reachableNew.add(t.to());
            }
            if (!reachableNew.equals(reachable)) return loopForReachable(reachableNew, possibleSymbols);
            else return reachableNew;
        }

    @Override
    public boolean acceptsEpsilonOnly() {
        if (this.acceptingStates.size() == 0) return false;

        Set<String> allSymbols = new HashSet<>();

        for (FATransition t : this.transitions) {
            if (!t.symbols().equals("")) allSymbols.add(t.symbols());
        }

        Set<Integer> reached = new HashSet<>();
        reached.add(0);
        reached = loopForReachable(reached, allSymbols);
        for (FATransition t : this.transitions) {
            if (!t.symbols().equals("")) {
                if (reached.contains(t.from())) {
                    Set<Integer> reachableFromSymbol = new HashSet<>();
                    reachableFromSymbol.add(t.to());
                    reachableFromSymbol = loopForReachable(reachableFromSymbol, allSymbols);
                    for (Integer state : this.acceptingStates) {
                        if (reachableFromSymbol.contains(state)) return false;
                    }
                }
            }
        }
        return true;
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
                if (!(t.from() == t.to() && t.symbols().equals(""))) {
                    if (t.from() == t.to() || t.to() == 0) return false;
                    if (!visitedStates.remove(t.to())) return false;
                }
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
        return acceptsEpsilon();
    }
}
