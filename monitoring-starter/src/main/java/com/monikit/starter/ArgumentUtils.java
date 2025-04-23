package com.monikit.starter;

public class ArgumentUtils {

    public static String safeArgsToString(Object[] args) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            sb.append("arg").append(i).append("=");
            try {
                sb.append(args[i]);
            } catch (Exception e) {
                sb.append("[unserializable]");
            }
            if (i < args.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    public static String safeOutputToString(Object result) {
        if (result == null) return "null";
        try {
            return result.toString();
        } catch (Exception e) {
            return "[unserializable]";
        }
    }

}
