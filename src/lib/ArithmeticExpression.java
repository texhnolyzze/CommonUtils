package lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;

/**
 *
 * @author Texhnolyze
 */
public class ArithmeticExpression {

    private Node root;
    private final Map<String, OperandNode> vars = new HashMap<>();
    
    public double getVarVal(String var) {
        OperandNode n = vars.get(var);
        if (n != null) 
            return n.val;
        else 
            throw new IllegalArgumentException("There is no variable named " + var + ".");
    }
    
    public void setVarVal(String var, double val) {
        OperandNode n = vars.get(var);
        if (n != null) 
            n.val = val;
        else 
            throw new IllegalArgumentException("There is no variable named " + var + ".");
    }
    
    public double compute() {
        return compute(root);
    }
    
    private double compute(Node n) {
        if (n.getClass() == OperandNode.class)
            return ((OperandNode) n).val;
        else {
            OperationNode op = (OperationNode) n;
            double[] vals = new double[op.operands.length];
            for (int i = 0; i < vals.length; i++) vals[i] = compute(op.operands[i]);
            
            return op.func.compute(vals);
            
        }
    }
    
    public static ArithmeticExpression create(String ex) {
        ArithmeticExpression aex = new ArithmeticExpression();
        String trimmedEx = ex.replaceAll("\\s+", "");
        String[] tokens = split(trimmedEx);
        tokens = convertToPostfix(tokens);
        aex.root = buildTree(tokens, tokens.length - 1, aex.vars).getKey();
        return aex;
    }
    
    private static Pair<Node, Integer> buildTree(String[] tokens, int idx, Map<String, OperandNode> vars) {
        String token = tokens[idx];
        char c = token.charAt(0);
        switch (c) {
            case 'n':
                return new Pair<>(new OperandNode(Double.parseDouble(token.substring(1))), idx);
            case 'v':
                OperandNode n = null;
                String varName = token.substring(1);
                n = vars.get(varName);
                if (n == null) {
                    n = new OperandNode(Double.NaN);
                    vars.put(varName, n);
                }
                return new Pair<>(n, idx);
            default:
                int i = idx;
                OperationNode op = new OperationNode();
                if (c == 'o') {
                    if (token.charAt(1) == 'u') { // unary minus
                        op.func = Function.FUNCS.get("u-");
                        op.operands = new Node[1];
                        Pair<Node, Integer> p = buildTree(tokens, i - 1, vars);
                        op.operands[0] = p.getKey();
                        i = p.getValue();
                    } else {
                        op.func = Function.FUNCS.get(token.substring(2));
                        op.operands = new Node[2];
                        Pair<Node, Integer> child = buildTree(tokens, i - 1, vars);
                        boolean commutative = isCommutative(op.func.name);
                        
                        if (commutative) op.operands[0] = child.getKey();
                        else op.operands[1] = child.getKey();
                        
                        i = child.getValue();
                        
                        child = buildTree(tokens, i - 1, vars);

                        if (commutative) op.operands[1] = child.getKey();
                        else op.operands[0] = child.getKey();
                        
                        i = child.getValue();
                        
                    }
                } else {
                    
                    String funcName = token.substring(1);
                    Function f = Function.FUNCS.get(funcName);
                    op.func = f;
                    op.operands = new Node[f.numArgs];
                    for (int j = 0; j < f.numArgs; j++) {
                        Pair<Node, Integer> p = buildTree(tokens, i - 1, vars);
                        i = p.getValue();
                        op.operands[j] = p.getKey();
                    }
                    
                }
                
                return new Pair<>(op, i);
                
        }
    }
    
    private static final Pattern NUM  = Pattern.compile("[0-9]+(\\.[0-9]+)?");
    private static final Pattern VAR  = Pattern.compile("[_$a-zA-Z][_$0-9a-zA-Z]*");
    private static final Pattern FUNC = Pattern.compile("(sqrt|ln|log|sin|cos|tg|exp)");
    
    private static String[] split(String ex) {
        
        List<String> tokens = new ArrayList<>();
        
        Matcher num = NUM.matcher(ex), var = VAR.matcher(ex), func = FUNC.matcher(ex);
        
        for (int i = 0; i < ex.length();) {
            char c = ex.charAt(i);
            switch (c) {
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
                    num.find(i);
                    int offset = num.end();
                    tokens.add("n" + ex.substring(i, offset));
                    i = offset;
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
//                  Only the unary minus at the 
//                  beginning of the line or after the opening bracket is allowed
                    boolean uMinus = c == '-' && (i == 0 || ex.charAt(i - 1) == '(');
                    tokens.add(uMinus ? "ou-" : "ob" + c);
                    i++;
                    break;
                default:
                    if (func.find(i)) {
                        offset = func.end();
                        String funcName = ex.substring(i, offset);
                        Function f = Function.FUNCS.get(funcName);
                        tokens.add("f" + f.name);
                    } else {
                        var.find(i);
                        offset = var.end();
                        String varName = ex.substring(i, offset);
                        tokens.add("v" + varName);
                    }
                    i = offset;
                    break;
            }
        }
        
        return tokens.toArray(new String[tokens.size()]);
        
    }
    
    
    // the implementation of the sorting station algorithm 
    // whose idea belongs to Edsger Dijkstra
    private static String[] convertToPostfix(String[] tokens) {
        
        Stack<String> s = new Stack<>();
        List<String> postfix = new ArrayList<>();
        
        boolean ok;
        
        for (int i = 0; i < tokens.length; i++) {
            
            String token = tokens[i];
            char c = token.charAt(0);
            
            switch (c) {
                case 'n':
                case 'v':
                    postfix.add(token);
                    break;
                case 'f':
                case '(':
                    s.push(token);
                    break;
                case ',':
                    ok = false;
                    while (!s.isEmpty()) {
                        String t = s.peek();
                        if (t.equals("(")) {
                            ok = true;
                            break;
                        } else s.push(t);
                    }
                    if (!ok)
                        throw new IllegalArgumentException("Bracket or comma passed.");
                    break;
                case ')':
                    ok = false;
                    while (!s.isEmpty()) {
                        String op = s.pop();
                        if (op.equals("(")) { 
                            ok = true;
                            if (!s.isEmpty() && s.peek().charAt(0) == 'f') 
                                postfix.add(s.pop());
                            break;
                        } else postfix.add(op);
                    }
                    if (!ok) 
                        throw new IllegalArgumentException("Bracket passed.");
                    break;
                case 'o':
                    int p = priorityOf(token);
                    boolean left = isLeft(token);
                    while (!s.isEmpty()) {
                        String op = s.peek();
                        int pp = priorityOf(op);
                        if (left) {
                            if (p <= pp) postfix.add(s.pop());
                            else break;
                        } else {
                            if (p < pp) postfix.add(s.pop());
                            else break;
                        }
                    }
                    s.push(token);
                    break;
            }
            
        }
        
        while (!s.isEmpty()) {
            String op = s.pop();
            if (op.charAt(0) == '(' || op.charAt(0) == ')') 
                throw new IllegalArgumentException("Bracket passed.");
            postfix.add(op);
        }
        
        return postfix.toArray(new String[postfix.size()]);
        
    }
    
    private static int priorityOf(String op) {
        switch (op) {
            case "ob+":
            case "ob-":
                return 1;
            case "ob*":
            case "ob/":
                return 2;
            case "ob^":
                return 3;
            case "ou-":
                return 4;
            default:
                return -1;
        }
    }
    
    private static boolean isLeft(String op) {
        return !op.equals("ob^") && !op.equals("ou-");
    }
    
    private static boolean isCommutative(String name) {
        switch (name) {
            case "+":
            case "*":
                return true;
            case "-":
            case "/":
            case "^":
                return false;
            default:
                throw new IllegalArgumentException("Unknown operation.");
        }
    }
    
    private static class Function {
        
        static final Map<String, Function> FUNCS = new HashMap<>();
        
        static {
            FUNCS.put("+", new Function("+", 2));
            FUNCS.put("-", new Function("-", 2));
            FUNCS.put("*", new Function("*", 2));
            FUNCS.put("/", new Function("/", 2));
            FUNCS.put("^", new Function("^", 2));
            FUNCS.put("u-", new Function("u-", 1));
            FUNCS.put("sqrt", new Function("sqrt", 1));
            FUNCS.put("ln",   new Function("ln", 1));
            FUNCS.put("log",  new Function("log", 2));
            FUNCS.put("sin",  new Function("sin", 1));
            FUNCS.put("cos",  new Function("cos", 1));
            FUNCS.put("tan",  new Function("tan", 1));
            FUNCS.put("exp",  new Function("exp", 1));
        }
        
        final String name;
        final int numArgs;
        
        Function(String name, int numArgs) {
            this.name = name;
            this.numArgs = numArgs;
        }
        
        double compute(double[] vals) {
            switch (name) {
                case "+":    return vals[0] + vals[1];
                case "-":    return vals[0] - vals[1];
                case "*":    return vals[0] * vals[1];
                case "/":    return vals[0] / vals[1];
                case "^":    return Math.pow(vals[0], vals[1]);
                case "u-":   return -vals[0];
                case "sqrt": return Math.sqrt(vals[0]);
                case "ln":   return Math.log(vals[0]);
                case "log":  return Math.log10(vals[1]) / Math.log10(vals[0]); 
                case "sin":  return Math.sin(vals[0]);
                case "cos":  return Math.cos(vals[0]);
                case "tan":  return Math.tan(vals[0]);
                case "exp":  return Math.exp(vals[0]);
                default:     return Double.NaN;
            }
        }
        
    }
    
    private interface Node {} // marker
    
    private static class OperandNode implements Node {
        
        double val;
        OperandNode(double val) {this.val = val;}
        
    }
    
    private static class OperationNode implements Node {
        
        Function func;
        Node[] operands;
        
    }
    
}
