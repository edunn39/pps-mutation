package mutation.g7;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {

    private Random random;
    private Map<String, Double> lhs;
    private Map<String, Double> rhs;
    private Set<String> combs;
    private int count = 0;
    private double lhsPerm = 0.0;
    private double rhsPerm = 0.0;
    private String[] genes = "acgt".split("");

    private int numTrials = 1000;
    private Double modifierStep = 1.05;

    // An array to record the wrong guesses so we don't repeat them
    private Vector<Mutagen> wrongMutagens = new Vector<Mutagen>();

    private int maxMutagenLength = 2;

    public Player() {
        random = new Random();
        lhs = new HashMap<>();
        rhs = new HashMap<>();
        combs = new HashSet<>();
        setLHSPerm(2);
        setRHSPerm(2);
        generateCombinations("", 4);
        generateLHSMap("", 2);
        generateRHSMap("", 2);
        System.out.println(count);
    }

    private void setLHSPerm(int n){
        for(int i = 1; i <= n; i++){
            lhsPerm += Math.pow(340, i);
        }
    }

    private void setRHSPerm(int n){
        for(int i = 1; i <= n; i++){
            rhsPerm += Math.pow(4, i);
        }
    }

    private void generateLHSMap(String result, int n){
        if(n == 0){
            lhs.put(result, 1 / lhsPerm);
            return;
        }
        String tmp = result;
        for(String c : combs){
            if(!result.equals("")) tmp = result + ";";
            generateLHSMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            lhs.put(result, 1 / lhsPerm);
        }
    }

    private void generateRHSMap(String result, int n){
        if(n == 0){
            rhs.put(result, 1 / rhsPerm);
            count++;
            return;
        }
        String tmp = result;
        for(String c : genes){
            if(!result.equals("")) tmp = result + ";";
            generateRHSMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            count++;
            rhs.put(result, 1 / rhsPerm);
        }
    }

    private void generateCombinations(String result, int n){
        if(n == 0){
            combs.add(result);
            return;
        }
        for(int i = 0; i < 4; i++){
            generateCombinations(result + genes[i], n - 1);
        }
        if(!result.equals("")) combs.add(result);
    }

    private Mutagen sampleMutagen() {
        // TODO: Implement a sampling based on distribution
        return new Mutagen();
    }

    private void modifyPatternDistribution(String pattern, Double modifier) {
        // TODO: Implement a pattern distribution modification
    }

    private void modifyActionDistribution(String action, Double modifier) {
        // TODO: Implement a action distribution modification
    }

    private void modifyMutagenLengthDistribution(Integer mutagenLength, Double modifier) {
        // TODO: Implement a mutagen length distribution modification
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++ i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        for (int i = 0; i < numTrials; ++ i) {
            // Get the genome
            String genome = randomString();
            String mutated = console.Mutate(genome);
            // Collect the change windows
            Vector<Pair<Integer, Integer>> changeWindows = new Vector<Pair<Integer, Integer>>();
            for (int j = 0; j < genome.length(); j++) {
                char before = genome.charAt(j);
                char after = mutated.charAt(j);
                if(before != after) {
                    int finish = j;
                    for(int forwardIndex = j + 1; forwardIndex < j + 10; forwardIndex++) {
                        // TODO: Handle the case when we are at the end of the genome (i.e. when j is 999)
                        if(genome.charAt(forwardIndex) == mutated.charAt(forwardIndex)) {
                            finish = forwardIndex - 1;
                            break;
                        }
                    }
                    changeWindows.add(new Pair<Integer, Integer>(j, finish));
                }
            }
            // Get the window sizes distribution and generate all possible windows
            int[] windowSizesCounts = new int[maxMutagenLength];
            Vector<Pair<Integer, Integer>> possibleWindows = new Vector<Pair<Integer, Integer>>();
            for (Pair<Integer, Integer> window: changeWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                int windowLength = finish - start + 1;
                windowSizesCounts[windowLength]++;
                if(windowLength == maxMutagenLength) {
                    // TODO: Handle the case if two smaller mutations occured side by side
                    possibleWindows.add(window);
                } else {
                    for (int proposedWindowLength = windowLength; proposedWindowLength <= maxMutagenLength; proposedWindowLength++) {
                        int diff = proposedWindowLength - windowLength;
                        // TODO: Handle the edge cases (i.e. start = 0 || 999)
                        for(int offset = -diff; offset <= 0; offset++) {
                            int newStart = start + offset;
                            int newFinish = newStart + proposedWindowLength;
                            possibleWindows.add(new Pair<Integer, Integer>(newStart, newFinish));
                        }
                    }
                }
            }
            // Modify the distributions for length
            for (int j = 0; j < maxMutagenLength; j++) {
                Double modifier = windowSizesCounts[j] * 1.0 / changeWindows.size();
                modifyMutagenLengthDistribution(j, modifier);
            }
            // Modify the distributions for pattens and actions
            for (Pair<Integer, Integer> window: possibleWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                // Get the string from
                String before = genome.substring(start, finish + 1);
                // Get the string after
                String after = mutated.substring(start, finish + 1);
                // Modify the distribution
                modifyPatternDistribution(before, modifierStep);
                modifyActionDistribution(after, modifierStep);
            }

            // Sample a mutagen
            boolean foundGuess = false;
            Mutagen guess = new Mutagen();
            while (!foundGuess) {
                guess = sampleMutagen();
                if(!wrongMutagens.contains(guess)) {
                    foundGuess = true;
                }
            }
            boolean isCorrect = console.Guess(guess);
            if(isCorrect) {
                return guess;
            } else {
                // Record that this is not a correct mutagen
                wrongMutagens.add(guess);
            }
        }
        Mutagen guess = sampleMutagen();
        return guess;
    }
}
