package me.imdanix.limiter.math;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class MathBase {
    private static final Pattern FLOAT = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Map<String, Function> functions = new HashMap<>();
    static {
        for(DefaultFunctions func : DefaultFunctions.values())
            MathBase.registerFunction(func.name(), func);
    }

    private MathBase() {
    }

    public static boolean registerFunction(String name, Function function) {
        name = name.toLowerCase(Locale.ENGLISH);
        if(isAllowedName(name) && !functions.containsKey(name)) {
            functions.put(name, function);
            return true;
        }
        return false;
    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }

    public static boolean isNumberChar(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    public static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z');
    }

    private static boolean isAllowedName(String str) {
        for(char c : str.toCharArray())
            if(!(isNumberChar(c) && isWordChar(c))) return false;
        return isWordChar(str.charAt(0));
    }

    public static double getDouble(String str, double def) {
        if(!FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    @FunctionalInterface
    public interface Function {
        double eval(double a, double... num);
    }
}
