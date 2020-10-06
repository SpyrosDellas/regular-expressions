import java.util.*;

/**
 * Regular expression pattern matching
 * <p>
 * This is the classic implementation based on an NFA and supports the following operators:
 * <p>
 * - parentheses -> ()
 * <p>
 * - multiway or -> |
 * <p>
 * - closure -> *
 * <p>
 * - wildcard -> .
 * <p>
 * - one or more -> +
 * <p>
 * - zero or one -> ?
 *
 * <p>
 * Performance:
 * <p>
 * Typical performance is O(N+M) in the sizes of the text and the regular expression
 * <p>
 * Worst case performance is O(NM) in the sizes of the text and the regular expression
 */
public class NFA {

    private final char[] regex;
    private final boolean[] isOperator;
    private final int acceptState;
    private final Digraph nfa;

    private static class Digraph {
        private final int vertices;
        private final List<List<Integer>> adj;

        public Digraph(int vertices) {
            this.vertices = vertices;
            adj = new ArrayList<>(vertices);
            for (int i = 0; i < vertices; i++) {
                adj.add(i, new ArrayList<>());
            }
        }

        public void addEdge(int from, int to) {
            adj.get(from).add(to);
        }

        public Set<Integer> dfs(int source) {
            return dfs(Collections.singleton(source));
        }

        public Set<Integer> dfs(Iterable<Integer> sources) {
            Set<Integer> marked = new HashSet<>();
            Stack<Integer> stack = new Stack<>();
            for (int source : sources) {
                stack.push(source);
            }
            while (!stack.isEmpty()) {
                int fromVertex = stack.pop();
                if (marked.contains(fromVertex)) {
                    continue;
                } else {
                    marked.add(fromVertex);
                }
                for (int toVertex : adj.get(fromVertex)) {
                    stack.push(toVertex);
                }
            }
            return marked;
        }
    }

    /**
     * Construct the NFA for the given regular expression
     *
     * @param re The regular expression to be parsed
     */
    public NFA(String re) {
        regex = ("(" + re + ")").toCharArray();
        int size = regex.length;
        isOperator = new boolean[size];
        acceptState = size;
        nfa = new Digraph(size + 1);  // the length of the regular expression plus one to account for the accept state
        Stack<Integer> parenthesis = new Stack<>();
        Stack<Integer> or = new Stack<>();

        for (int index = 0; index < size; index++) {
            char c = regex[index];
            if (operator(c))
                isOperator[index] = true;
            if (c == '(') {
                nfa.addEdge(index, index + 1);
                parenthesis.push(index);
            } else if (c == '|') {
                or.push(index);
            } else if (c == '*' || c == '+' || c == '?') {
                nfa.addEdge(index, index + 1);
            } else if (c == ')') {
                nfa.addEdge(index, index + 1);
                if (parenthesis.isEmpty())
                    throw new IllegalArgumentException("Provided regular expression in invalid: parentheses mismatch");
                int leftParenthesisIndex = parenthesis.pop();
                while (!or.isEmpty() && or.peek() > leftParenthesisIndex) {
                    int orIndex = or.pop();
                    nfa.addEdge(leftParenthesisIndex, orIndex + 1);
                    nfa.addEdge(orIndex, index);
                }
                if (index < size - 1 && regex[index + 1] == '*') {
                    nfa.addEdge(leftParenthesisIndex, index + 1);
                    nfa.addEdge(index + 1, leftParenthesisIndex);
                }
                if (index < size - 1 && regex[index + 1] == '+') {
                    nfa.addEdge(index + 1, leftParenthesisIndex);
                }
                if (index < size - 1 && regex[index + 1] == '?') {
                    nfa.addEdge(leftParenthesisIndex, index + 1);
                }
            } else if (regex[index + 1] == '*') {
                nfa.addEdge(index, index + 1);
                nfa.addEdge(index + 1, index);
            } else if (regex[index + 1] == '+') {
                nfa.addEdge(index + 1, index);
            } else if (regex[index + 1] == '?') {
                nfa.addEdge(index, index + 1);
            }
        }
    }

    private boolean operator(char c) {
        return (c == '(' || c == ')' || c == '|' || c == '*' ||
                c == '+' || c == '?' || c == '.' || c == '\\');
    }

    public boolean recognizes(String text) {
        Set<Integer> reachableStates = nfa.dfs(0);
        // System.out.println(reachableStates);
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            List<Integer> matchingStates = new ArrayList<>();
            for (int state : reachableStates) {
                if (state == acceptState)
                    continue;
                if (isOperator[state]) {
                    if (regex[state] == '.')
                        matchingStates.add(state + 1);
                    continue;
                }
                if (c == regex[state])
                    matchingStates.add(state + 1);
            }
            reachableStates = nfa.dfs(matchingStates);
            // System.out.println(reachableStates);
        }
        return reachableStates.contains(acceptState);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int vertex = 0; vertex < nfa.vertices; vertex++) {
            sb.append(vertex).append(": ").append(nfa.adj.get(vertex)).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // String regex = ".*AB((C|D*E)F)*G";
        // String regex = ".*AB((C|D|E)F)*G";
        String regex = "S..ros D(a|e)llas";
        NFA pattern = new NFA(regex);
        System.out.println(Arrays.toString(pattern.regex));
        // System.out.println(pattern);
        String[] texts = new String[]{"@@ABCFCFDDEFDEFCFG", "ABG", "SpyrosABCFDFEFG", "Sypros Dallas", "Spyros Dellas",
                "Spyros Dillas"};
        for (String text : texts) {
            System.out.println("Text = " + text + ", recognizes = " + pattern.recognizes(text));
        }
    }

}
