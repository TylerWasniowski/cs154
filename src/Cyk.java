import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Cyk {

    static abstract class Grammar {
        private List<List<String>> rules;
        private Set<String> variables;
        private String startVariable;

        /**
         * Uses the first LHS of rule as start variable. Ignores "e". (A:e,e -> ["A"])
         * First string in a rule is the LHS, added to the set of variables.
         *
         * @param fileName name of file for Grammar input.
         */
        public Grammar(String fileName) {
            rules = new ArrayList<>();
            variables = new HashSet<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))  {
                reader.lines().forEach((line) -> {
                    String[] pieces = line.trim().split("[:,]");
                    List<String> rule = Arrays.stream(pieces)
                            .filter((piece) -> !piece.equals("e"))
                            .collect(Collectors.toList());

                    if (rule.isEmpty())
                        throw new IllegalArgumentException("Found empty rule (Expected at least a variable)");

                    rules.add(rule);
                    variables.add(rule.get(0));
                });
            } catch (IOException e) {
                System.err.println("Error reading grammar file");
                e.printStackTrace();
            }

            startVariable = rules.get(0).get(0);
        }

        public Grammar(List<List<String>> rules) {
            this.rules = rules;
            this.variables = rules.stream()
                    .map((rule) -> rule.get(0))
                    .collect(Collectors.toSet());
            this.startVariable = rules.get(0).get(0);
        }

        public abstract boolean processString(String input);

        public List<List<String>> getRules() {
            return rules;
        }

        public Set<String> getVariables() {
            return variables;
        }

        public String getStartVariable() {
            return startVariable;
        }

        @Override
        public String toString() {
            return rules.stream()
                    .map((rule) -> (rule.get(0) + " -> '" +
                            String.join("}{", rule.subList(1, rule.size())))
                            + "'")
                    .collect(Collectors.joining("\n"));
        }
    }

    static class SecondNormalFormGrammar extends Grammar {
        public SecondNormalFormGrammar(String fileName) {
            super(fileName);
            checkIfValid();
        }

        public SecondNormalFormGrammar(List<List<String>> rules) {
            super(rules);
            checkIfValid();
        }

        public ChomskyNormalFormGrammar getChomskyNormalForm() {
            // TODO: Transform to CNF
            return new ChomskyNormalFormGrammar(getRules());
        }

        @Override
        public boolean processString(String input) {
            return getChomskyNormalForm().processString(input);
        }

        private void checkIfValid() {
            boolean isValid = getRules().stream().allMatch((rule) -> rule.size() <= 3);

            if (!isValid)
                throw new IllegalArgumentException("Found less than 2 elements on the RHS of a rule (Expected 2 or less");
        }
    }

    static class ChomskyNormalFormGrammar extends SecondNormalFormGrammar {
        public ChomskyNormalFormGrammar(String fileName) {
            super(fileName);
            checkIfValid();
        }

        public ChomskyNormalFormGrammar(List<List<String>> rules) {
            super(rules);
            checkIfValid();
        }

        public boolean processString(String input) {
            // TODO: Cyk algo
            return true;
        }

        private void checkIfValid() {
            boolean isValid = getRules().stream()
                    .allMatch((rule) -> (rule.size() == 1 && rule.get(0).equals(getStartVariable()) ||
                            (rule.size() == 2 && !getVariables().contains(rule.get(1))) ||
                            (rule.size() == 3 && getVariables().containsAll(rule))
                    ));

            if (!isValid)
                throw new IllegalArgumentException("Grammar is not in Chomsky Normal Form");
        }
    }

    public static void main(String[] args) {
        String input = "hi";
//        if (args.length == 1)
//            input = "";
//        else if (args.length == 2)
//            input = args[1];
//        else {
//            System.out.println("Wrong number of args. (Expected 2)");
//            return;
//        }

//        String fileName = args[0];
        String fileName = "2nf_hw3.txt";

        if ((new SecondNormalFormGrammar(fileName)).processString(input))
            System.out.println("YES");
        else
            System.out.println("NO");
        System.out.println(new SecondNormalFormGrammar(fileName));
    }
}
