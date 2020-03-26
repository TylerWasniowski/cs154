import java.io.*;
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
            return startVariable + "\n" +
                    rules.stream()
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

        // Modified from: http://www.cs.sjsu.edu/faculty/pollett/154.1.20s/Lec20200323.html#(6)
        @Override
        public boolean processString(String input) {
            // Line 1.
            if (input.isEmpty() && getRules().get(0).size() == 1)
                return true;

            List<List<Set<String>>> table = new ArrayList<>(input.length());
            input.chars().forEach((ch) -> {
                List<Set<String>> row = new ArrayList<>(input.length());
                input.chars().forEach((ch2) -> row.add(new HashSet<>()));
                table.add(row);
            });

            // Line 2.
            for (int i = 0; i < input.length(); i++) {
                // So the forEach rule can use i (captured variables must be effectively final)
                final int iCaptured = i;
                // Lines 3 to 5
                getRules().forEach((rule) -> {
                    if (rule.size() == 2 && rule.get(1).equals(input.substring(iCaptured, iCaptured + 1)))
                        table.get(iCaptured).get(iCaptured).add(rule.get(0));
                });
            }

            // Line 6.
            for (int l = 1; l <= input.length(); l++) {
                // Line 7.
                for (int i = 0; i <= input.length() - l; i++) {
                    // Line 8.
                    int j = i + l - 1;
                    // Line 9 (Modified to allow k to be equal to j to handle unit productions).
                    for (int k = i; k <= j; k++) {
                        final int iCaptured = i;
                        final int jCaptured = j;
                        final int kCaptured = k;

                        // Lines 10 and 11 (with modification for 2NF).
                        getRules().forEach((rule) -> {
                            if (rule.size() == 2 && table.get(iCaptured).get(jCaptured).contains(rule.get(1))) {
                                /*
                                 * Handle unit productions (I think this is not possible in 2NF but the professor
                                 * had one in his previous example) I want to stay on the safe side
                                 */
                                table.get(iCaptured).get(jCaptured).add(rule.get(0));
                            } else if (rule.size() == 3 && kCaptured < jCaptured) {
                                // 2 Variables case
                                boolean tableHasFirstPart = table.get(iCaptured).get(kCaptured)
                                        .contains(rule.get(1));
                                boolean tableHasLastPart = table.get(kCaptured + 1).get(jCaptured)
                                        .contains(rule.get(2));

                                // Other cases
                                boolean substringStartsWithFirstTerminal =
                                        input.substring(iCaptured, jCaptured + 1).startsWith(rule.get(1));
                                boolean tableHasRest =  iCaptured + rule.get(1).length() < input.length() &&
                                        table.get(iCaptured + rule.get(1).length()).get(jCaptured)
                                        .contains(rule.get(2));

                                boolean substringEndsWithSecondTerminal =
                                        input.substring(iCaptured, jCaptured + 1).endsWith(rule.get(2));
                                boolean tableHasFirst = jCaptured - rule.get(2).length() >= 0 &&
                                        table.get(iCaptured).get(jCaptured - rule.get(2).length())
                                                .contains(rule.get(1));

                                boolean substringIsRule = input.substring(iCaptured, jCaptured + 1)
                                        .equals(rule.get(1) + rule.get(2));

                                if (
                                        (tableHasFirstPart && tableHasLastPart) ||
                                        (substringStartsWithFirstTerminal && tableHasRest) ||
                                        (substringEndsWithSecondTerminal && tableHasFirst) ||
                                        substringIsRule
                                )
                                    table.get(iCaptured).get(jCaptured).add(rule.get(0));
                            }
                        });
                    }
                }
            }

            return table.get(0).get(input.length() - 1).contains(getStartVariable());
        }

        private void checkIfValid() {
            boolean isValid = getRules().stream().allMatch((rule) -> rule.size() <= 3);

            if (!isValid)
                throw new IllegalArgumentException("Found less than 2 elements on the RHS of a rule (Expected 2 or less)");
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
        String input;
        if (args.length == 1)
            input = "";
        else if (args.length == 2)
            input = args[1];
        else {
            System.out.println("Wrong number of args. (Expected 2)");
            return;
        }

        String fileName = args[0];

        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            boolean linesAreValid = in.lines().allMatch((line) -> {
                if (!line.isEmpty()) {
                    return line.matches("([01])+:(([01])+|a|b),(([01])+|a|b|e)");
                }

                return true;
            });

            if (!linesAreValid) {
                System.out.println("NO");
                return;
            }
        } catch (IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

        if ((new SecondNormalFormGrammar(fileName)).processString(input))
            System.out.println("YES");
        else
            System.out.println("NO");
    }
}
