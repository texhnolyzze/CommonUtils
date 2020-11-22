package lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Texhnolyze
 */
public final class ArithmeticExpression {
    
//  add some common functions
    static {
        Function.newFunction("sqrt", vals -> Math.sqrt(vals[0]), 1);
        Function.newFunction("sin", vals -> Math.sin(vals[0]), 1);
        Function.newFunction("cos", vals -> Math.cos(vals[0]), 1);
        Function.newFunction("tan", vals -> Math.tan(vals[0]), 1);
        Function.newFunction("ln", vals -> Math.log(vals[0]), 1);
        Function.newFunction("lg", vals -> Math.log10(vals[0]), 1);
        Function.newFunction("log", vals -> MathUtils.log(vals[0], vals[1]), 2);
    }
    
    private Node root;
    private final Map<String, OperandNode> vars = new HashMap<>();
    
    public boolean contains(String var) {
        return vars.containsKey(var);
    }
    
    public double getVariableValue(String var) {
        OperandNode n = vars.get(var);
        if (n != null) 
            return n.val;
        else 
            throw new IllegalArgumentException("There is no variable named " + var + ".");
    }
    
    public void setVariableValue(String var, double val) {
        OperandNode n = vars.get(var);
        if (n != null) 
            n.val = val;
        else 
            throw new IllegalArgumentException("There is no variable named " + var + ".");
    }
    
    public void forEachVariable(BiConsumer<String, Double> c) {
        for (Map.Entry<String, OperandNode> e : vars.entrySet())
            c.accept(e.getKey(), e.getValue().val);
    } 
    
    public double eval() {
        return eval(root);
    }

    private double eval(Node n) {
        switch (n.getClass().getSimpleName()) {
            case "OperandNode":
                return ((OperandNode) n).val;
            case "OperatorNode":
                OperatorNode op_node = (OperatorNode) n;
                for (int i = 0; i < op_node.childs.length; i++) 
                    op_node.temp_vals[i] = eval(op_node.childs[i]);
                return op_node.operator.apply(op_node.temp_vals);
            case "FunctionNode":
                FunctionNode func_node = (FunctionNode) n;
                for (int i = 0; i < func_node.childs.length; i++) 
                    func_node.temp_vals[i] = eval(func_node.childs[i]);
                return func_node.function.apply(func_node.temp_vals);
        }
        return Double.NaN;
    }
    
    public static ArithmeticExpression parse(String ex) {
        try {
            String[] tokens = toTokens(ex);
            String[] postfix = toPostfix(tokens);
            testPostfix(postfix);
            return buildTree(postfix, new ArithmeticExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse arithmetic expression");
        }
    }
    
    public static final Pattern NUM_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)?");
    public static final Pattern VAR_PATTERN = Pattern.compile("[_$a-zA-Z][_$0-9a-zA-Z]*");

    private static String[] toTokens(String ex) {
        List<String> tokens = new ArrayList<>();
        Matcher num_matcher = NUM_PATTERN.matcher(ex);
        Matcher var_matcher = VAR_PATTERN.matcher(ex);
        Matcher func_matcher = Function.FUNC_PATTERN.matcher(ex);
        for (int i = 0; i < ex.length();) {
            char c = ex.charAt(i);
            switch (c) {
                case ' ':
                    i++;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    num_matcher.find(i);
                    tokens.add("n:" + ex.substring(i, num_matcher.end()));
                    i = num_matcher.end();
                    break;
                case '(':
                case ')':
                case ',':
                    tokens.add(Character.toString(c));
                    i++;
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                case '%':
                    boolean u_minus = c == '-' && (tokens.isEmpty() || tokens.get(tokens.size() - 1).charAt(0) == '(' || tokens.get(tokens.size() - 1).charAt(0) == 'o');
                    if (u_minus) {
                        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).equals("o:u-"))
                            throw new RuntimeException();
                        tokens.add("o:u-");
                    } else 
                        tokens.add("o:" + c);
                    i++;
                    break;
                default:
                    if (var_matcher.find(i) && var_matcher.start() == i) {
                        String varName = ex.substring(i, var_matcher.end());
                        if (varName.matches(Function.FUNC_PATTERN.pattern()))
                            tokens.add("f:" + varName);
                        else
                            tokens.add("v:" + varName);
                        i = var_matcher.end();
                    } else {
                        if (func_matcher.find(i) && func_matcher.start() == i) {
                            String funcName = ex.substring(i, func_matcher.end());
                            tokens.add("f:" + funcName);
                            i = func_matcher.end();
                        } else
                            throw new RuntimeException();
                    }
                    break;
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }
    
//  the implementation of the sorting station algorithm 
//  whose idea belongs to Edsger Dijkstra
    private static String[] toPostfix(String[] tokens) {
        Stack<String> s = new Stack<>();
        List<String> postfix = new ArrayList<>();        
        for (String token : tokens) {
            char id = token.charAt(0);            
            switch (id) {
                case 'n':
                case 'v':
                    postfix.add(token);
                    break;
                case 'f':
                case '(':
                    s.push(token);
                    break;
                case ',':
                    while (!s.peek().equals("(")) 
                        postfix.add(s.pop());
                    break;
                case ')':
                    while (!s.peek().equals("(")) 
                        postfix.add(s.pop());
                    s.pop();
                    if (!s.isEmpty() && s.peek().charAt(0) == 'f')
                        postfix.add(s.pop());
                    break;
                case 'o':
                    int p1 = priorityOf(token);
                    boolean left = isLeft(token);
                    while (!s.isEmpty() && s.peek().charAt(0) != '(') {
                        int p2 = priorityOf(s.peek());
                        if ((left && p1 <= p2) || (!left && p1 < p2))
                            postfix.add(s.pop());
                        else 
                            break;
                    }
                    s.push(token);
                    break;
            }
        }
        while (!s.isEmpty()) {
            String token = s.pop();
            if (token.charAt(0) == '(' || token.charAt(0) == ')') 
                throw new RuntimeException();
            postfix.add(token);
        }
        return postfix.toArray(new String[postfix.size()]);
    }
    
    private static int priorityOf(String opToken) {
        switch (opToken) {
            case ")":
                return 0;
            case "o:+":
            case "o:-":
                return 1;
            case "o:*":
            case "o:/":
            case "o:%":
                return 2;
            case "o:^":
                return 3;
            case "o:u-":
                return 4;
            default:
                throw new RuntimeException();
        }
    }
    
    private static boolean isLeft(String opToken) {
        switch (opToken) {
            case "o:^":
            case "o:u-":
                return false;
            default:
                return true;
        }
    }
    
//  check that this is correct reverse polish notation
    private static void testPostfix(String[] postfix) {
        int numArgs;
        int stackSize = 0;
        for (String token : postfix) {
            char id = token.charAt(0);
            switch (id) {
                case 'n':
                case 'v':
                    stackSize++;
                    break;
                case 'o':
                    numArgs = Operator.getByOperatorToken(token).numArgs;
                    stackSize -= (numArgs - 1);
                    break;
                case 'f':
                    numArgs = Function.getByFunctionToken(token).numArgs;
                    stackSize -= (numArgs - 1);
                    break;
            }
        }
        if (stackSize != 1)
            throw new RuntimeException();
    }
    
    private static ArithmeticExpression buildTree(String[] postfix, ArithmeticExpression dest) {
        dest.root = buildTree(postfix, postfix.length - 1, dest).x();
        return dest;
    }
    
    private static Pair<Node, Integer> buildTree(String[] postfix, int i, ArithmeticExpression dest) {
        Node node;
        int subtreeEndIndex = i;
        String token = postfix[i];
        char id = token.charAt(0);
        switch (id) {
            case 'n':
                node = new OperandNode(Double.parseDouble(token.substring(2)));
                break;
            case 'v':
                String varName = token.substring(2);
                node = dest.vars.get(varName);
                if (node == null) {
                    dest.vars.put(varName, new OperandNode(Double.NaN));
                    node = dest.vars.get(varName);
                }
                break;
            case 'o':
                Operator operator = Operator.getByOperatorToken(token);
                OperatorNode operatorNode = new OperatorNode(operator);
                node = operatorNode;
                for (int j = 0; j < operator.numArgs; j++) {
                    Pair<Node, Integer> next = buildTree(postfix, subtreeEndIndex - 1, dest);
                    operatorNode.childs[operator.numArgs - j - 1] = next.x();
                    subtreeEndIndex = next.y();
                }
                break;
            case 'f':
                Function function = Function.getByFunctionToken(token);
                FunctionNode functionNode = new FunctionNode(function);
                node = functionNode;
                for (int j = 0; j < function.numArgs; j++) {
                    Pair<Node, Integer> next = buildTree(postfix, subtreeEndIndex - 1, dest);
                    functionNode.childs[function.numArgs - j - 1] = next.x();
                    subtreeEndIndex = next.y();
                }
                break;
            default:
                throw new RuntimeException();
        }
        return new Pair<>(node, subtreeEndIndex);
    }
    
    private static interface Node {}
    
    private static class OperandNode implements Node {
        double val;
        OperandNode(double val) {this.val = val;}
    }
    
    private static class OperatorNode implements Node {
        
        final double[] temp_vals;
        
        final Operator operator;
        final Node[] childs;
        
        OperatorNode(Operator operator) {
            this.operator = operator;
            childs = new Node[operator.numArgs];
            temp_vals = new double[operator.numArgs];
        }
        
    }
    
    private static class FunctionNode implements Node {
        
        final double[] temp_vals;
        
        final Function function;
        final Node[] childs;
        
        FunctionNode(Function function) {
            this.function = function;
            childs = new Node[function.numArgs];
            this.temp_vals = new double[function.numArgs];
        }
        
    }
    
    private static enum Operator {
        
        UNARY_MINUS((args) -> {return -args[0];}, 1),
        PLUS((args) -> {return args[0] + args[1];}, 2), 
        MINUS((args) -> {return args[0] - args[1];}, 2), 
        MULTIPLE((args) -> {return args[0] * args[1];}, 2),
        DIVIDE((args) -> {return args[0] / args[1];}, 2), 
        POWER((args) -> {return Math.pow(args[0], args[1]);}, 2),
        MODULO((args) -> {return args[0] % args[1];}, 2);
        
        final int numArgs;
        final java.util.function.Function<double[], Double> f;
        
        Operator(java.util.function.Function<double[], Double> f, int numArgs) {
            this.f = f;
            this.numArgs = numArgs;
        }
        
        double apply(double[] vals) {
            return f.apply(vals);
        }
        
        static Operator getByOperatorToken(String opToken) {
            switch (opToken) {
                case "o:u-": return UNARY_MINUS;
                case "o:+":  return PLUS;
                case "o:-":  return MINUS;
                case "o:*":  return MULTIPLE;
                case "o:/":  return DIVIDE;
                case "o:^":  return POWER;
                case "o:%":  return MODULO;
                default:
                    throw new RuntimeException();
            }
        }
        
    }
    
    public static class Function {
        
        private static Pattern FUNC_PATTERN = Pattern.compile("");
        public static Pattern ALLOWABLE_FUNC_NAME = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
        
        private static final Map<String, Function> ALL = new HashMap<>();
        
        private final int numArgs;
        private final java.util.function.Function<double[], Double> f;
        
        private Function(java.util.function.Function<double[], Double> f, int numArgs) {
            this.f = f;
            this.numArgs = numArgs;
        }
        
        public int numArgs() {
            return numArgs;
        }
        
        public double apply(double[] vals) {
            return f.apply(vals);
        }
        
        public static Iterable<Map.Entry<String, Function>> getAllFunctions() {
            return Collections.unmodifiableSet(ALL.entrySet());
        }
        
        public static Function getByName(String name) {
            return ALL.get(name);
        }
        
        static Function getByFunctionToken(String funcToken) {
            return ALL.get(funcToken.substring(2));
        }
        
        public static void newFunction(String name, java.util.function.Function<double[], Double> f, int numArgs) {
            if (ALL.containsKey(name))
                throw new IllegalArgumentException("Function with name " + name + " already exists.");
            Matcher m = ALLOWABLE_FUNC_NAME.matcher(name);
            if (!m.find() || m.start() != 0 || m.end() != name.length())
                throw new IllegalArgumentException("Illegal function name.");
            ALL.put(name, new Function(f, numArgs));
            String prev_pattern = FUNC_PATTERN.pattern();
            FUNC_PATTERN = Pattern.compile(name + (prev_pattern.isEmpty() ? "" : "|") + prev_pattern);
        }
        
        public static void removeFunction(String name) {
            if (ALL.containsKey(name)) {
                ALL.remove(name);
                String[] prev_pattern = FUNC_PATTERN.pattern().split("\\|");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < prev_pattern.length; i++) {
                    String func_name = prev_pattern[i];
                    if (func_name.equals(name)) 
                        continue;
                    sb.append(func_name);
                    if (i != prev_pattern.length - 1)
                        sb.append("|");
                }
                FUNC_PATTERN = Pattern.compile(sb.toString());
            }
        }
        
    }
    
}
