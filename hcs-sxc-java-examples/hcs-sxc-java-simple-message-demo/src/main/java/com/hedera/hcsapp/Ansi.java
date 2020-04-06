package com.hedera.hcsapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Ansi {
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    private static Map<String, String> colors = new HashMap<String, String>();
    
    static {
        colors.put("(reset)", (isWindows) ? "" : "\u001B[0m"); // reset
        colors.put("(black)", (isWindows) ? "" : "\u001B[30m"); // black
        colors.put("(red)", (isWindows) ? "" : "\u001B[31m"); // red
        colors.put("(green)", (isWindows) ? "" : "\u001B[32m"); // green
        colors.put("(yellow)", (isWindows) ? "" : "\u001B[33m"); // yellow
        colors.put("(blue)", (isWindows) ? "" : "\u001B[34m"); // blue
        colors.put("(purple)", (isWindows) ? "" : "\u001B[35m"); // purple
        colors.put("(cyan)", (isWindows) ? "" : "\u001B[36m"); // cyan
        colors.put("(white)", (isWindows) ? "" : "\u001B[37m"); // white
        colors.put("(bold)", (isWindows) ? "**" : "\033[0;1m"); // bold
    }
    
    public static void print(String toPrint) {
        for (Entry<String, String> color : colors.entrySet()) {
            toPrint = toPrint.replace(color.getKey(), color.getValue());
        }
        System.out.println(toPrint);
    }
}
