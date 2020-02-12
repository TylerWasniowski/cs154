// From Chris Pollett's Lecture: http://www.cs.sjsu.edu/faculty/pollett/154.1.20s/Lec20200210.html#(1)
public abstract class Automata
{
    public Automata()
    {
    }

    public void setState(int state)
    {
        currentState = state;
    }

    public boolean processString(String w)
    {
        for(int i = 0; i < w.length(); i++)
        {
            currentState = delta(currentState, w.charAt(i));
        }
        boolean accept = finalState(currentState);
        return accept;
    }

    public abstract int delta(int state, char c);
    public abstract boolean finalState(int state);

    int currentState;
}