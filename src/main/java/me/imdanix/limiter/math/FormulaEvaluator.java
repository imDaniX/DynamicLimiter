package me.imdanix.limiter.math;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Locale;

/*
   All the code is inspired by Boann's answer on https://stackoverflow.com/questions/3422673
   You can use this code how ever you want to
*/

/**
 * Adjusted version of me.imdanix.math.FormulaEvaluator class
 * More on https://github.com/imDaniX/EzMath
 */
public class FormulaEvaluator {
    private static final Expression ZERO = () -> 0;

    private final Expression expression;
    private final float[] parameters;
    private int limit;

    public FormulaEvaluator(String expression) {
        this.parameters = new float[3];
        this.expression = thirdImportance(new PointerHolder(StringUtils.remove(expression, ' ').toLowerCase(Locale.ENGLISH)));
    }

    public double eval(int limit) {
        this.limit = limit;
        return expression.eval();
    }

    public void setParameters(int online, int worldOnline, float tps) {
        parameters[0] = online;
        parameters[1] = worldOnline;
        parameters[2] = tps;
    }

    private Expression thirdImportance(PointerHolder holder) {
        Expression x = secondImportance(holder);
        while(true) {
            if(holder.tryNext('+')) {
                Expression a = x;
                Expression b = secondImportance(holder);
                x = () -> a.eval() + b.eval();
            } else
            if(holder.tryNext('-')) {
                Expression a = x;
                Expression b = secondImportance(holder);
                x = () -> a.eval() - b.eval();
            } else
                return x;
        }
    }

    private Expression secondImportance(PointerHolder holder) {
        Expression x = firstImportance(holder);
        while(true) {
            if(holder.tryNext('*')) {
                Expression a = x;
                Expression b = firstImportance(holder);
                x = () -> a.eval() * b.eval();
            } else
            if(holder.tryNext('/')) {
                Expression a = x;
                Expression b = firstImportance(holder);
                x = () -> a.eval() / b.eval();
            } else
            if(holder.tryNext('%')) {
                Expression a = x;
                Expression b = firstImportance(holder);
                x = () -> a.eval() % b.eval();
            } else
                return x;
        }
    }

    private Expression firstImportance(PointerHolder holder) {
        if(holder.tryNext('-')) { // "-5", "--5"..
            Expression a = firstImportance(holder);
            return () -> -a.eval();
        }
        if(holder.tryNext('+')) // "+5", "++5"..
            return firstImportance(holder);
        Expression x = ZERO;
        int start = holder.pointer;
        if(holder.tryNext('(')) {
            x = thirdImportance(holder);
            holder.tryNext(')');
        } else if(MathBase.isNumberChar(holder.current())) {
            holder.pointer++;
            while(MathBase.isNumberChar(holder.current())) holder.pointer++;
            double a = MathBase.getDouble(holder.substring(start, holder.pointer), 0);
            x = () -> a;
        } else if(MathBase.isWordChar(holder.current())) {
            holder.pointer++;
            while(MathBase.isWordChar(holder.current()) || MathBase.isNumberChar(holder.current())) holder.pointer++;
            String str = holder.substring(start, holder.pointer);
            if(holder.tryNext('(')) {
                Expression a = thirdImportance(holder);
                Expression[] args = new Expression[0];
                while(holder.tryNext(',')) {
                    args = Arrays.copyOfRange(args, 0, args.length + 1);
                    args[args.length - 1] = thirdImportance(holder);
                }
                if(args.length > 0) {
                    Expression[] args2 = args;
                    double[] argsD = new double[args2.length];
                    x = () -> {
                        MathBase.Function function = MathBase.getFunction(str);
                        for(int i = 0; i < args2.length; i++)
                            argsD[i] = args2[i].eval();
                        return function == null ? 0 : function.eval(a.eval(), argsD);
                    };
                } else {
                    x = () -> {
                        MathBase.Function function = MathBase.getFunction(str);
                        return function == null ? 0 : function.eval(a.eval());
                    };
                }
                holder.tryNext(')');
            } else {
                switch (str) {
                    case "limit": x = () -> limit; break;
                    case "online": x = () -> parameters[0]; break;
                    case "wonline": x = () -> parameters[1]; break;
                    case "tps": x = () -> parameters[2]; break;
                    // TODO: Error
                    default: x = ZERO;
                }
            }
        }

        if(holder.tryNext('^')) {
            Expression a = x;
            Expression b = firstImportance(holder);
            x = () -> Math.pow(a.eval(), b.eval());
        }
        return x;
    }

    @FunctionalInterface
    private interface Expression {
        double eval();
    }

    /**
     * Used in parse process to unload origin and pointer itself after ending of parse
     * Because of this class everything looks a bit more sh!tty... but still readable
     */
    private static final class PointerHolder {
        final String origin;
        int pointer;

        PointerHolder(String origin) {
            this.origin = origin;
            this.pointer = 0;
        }

        String substring(int start, int end) {
            return origin.substring(start, end);
        }

        char current() {
            return origin.length() > pointer ? origin.charAt(pointer) : ' ';
        }

        boolean tryNext(char c) {
            if(current() == c) {
                pointer++;
                return true;
            }
            return false;
        }
    }
}
