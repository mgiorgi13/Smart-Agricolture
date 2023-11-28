package iot.unipi.it.Logger;

public class Logger {
    public static void warning(String text){
        if (nullMsg(text))
            return;

        LoggerThread lt = new LoggerThread("[WARNING] " + text);
        lt.start();
    }

    public static void error(String text){
        if (nullMsg(text))
            return;

        LoggerThread lt = new LoggerThread("[ERROR] " + text);
        lt.start();
    }

    public static void log(String text){
        if (nullMsg(text))
            return;

        LoggerThread lt = new LoggerThread("[LOG] " + text);
        lt.start();
    }

    private static boolean nullMsg(String text) {
        if (text == null) {
            warning("NullPointerException avoided");
            return true;
        }
        return false;
    }
}