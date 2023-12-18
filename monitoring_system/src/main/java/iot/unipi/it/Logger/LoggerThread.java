package iot.unipi.it.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

class LoggerThread extends Thread {

    private static String logFilePath="log/logFile.txt";
    private String msg;

    LoggerThread(String msg){
        this.msg=msg;
    }

    public void run(){
        String newMsg = Instant.now().toString() + " " + msg + "\n";
        writeOnFile(newMsg);
    }

    private static synchronized void writeOnFile(String msg){
        try {
            File file = new File(logFilePath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            Files.write(Paths.get(logFilePath), msg.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException i){
            i.printStackTrace();
        }
    }
}