public class DoubleA extends Automata {
    @Override
    public int delta(int state, char c) {
        if (state == 0) {
            if (c == 'a')
                return 1;
            else
                return 0;
        } else if (state == 1) {
            if (c == 'a')
                return 2;
            else
                return 0;
        }

        return 2;
    }

    @Override
    public boolean finalState(int state) {
        return state == 0 || state == 1;
    }

    public static void main(String[] args) {
        String in;
        if (args.length == 0)
            in = "";
        else if (args.length == 1)
            in = args[0];
        else {
            System.out.println("Wrong number of args. (Expected 1)");
            return;
        }

        if (in.matches(".*[^ab].*")) {
            System.out.println("Only expected a or b.");
            return;
        }

        if ((new DoubleA()).processString(in))
            System.out.println("YES");
        else
            System.out.println("NO");
    }
}
