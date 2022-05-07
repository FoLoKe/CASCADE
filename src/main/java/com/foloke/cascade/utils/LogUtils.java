package com.foloke.cascade.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class LogUtils {
    static TextArea logTextArea;
    static final String sysLogName = "log";
    static final String logsDirName = "logs";
    static BufferedWriter logWriter;

    static Map<String, BufferedWriter> writerMap;

    public static void init(TextArea textArea) {
        logTextArea = textArea;
        writerMap = new TreeMap<>();

        try {
            File logsDir = new File(logsDirName);
            if(!logsDir.mkdir()) {
                System.out.println("log folder write error");
                return;
            }

            File logFile = new File(logsDirName + "\\" + sysLogName);

            if(!logFile.createNewFile()) {
                System.out.println("log folder write error");
                return;
            }

            FileWriter writer = new FileWriter(logFile);
            logWriter = new BufferedWriter(writer);
        } catch (IOException e) {
            log(e.toString());
        }

        log("started");
    }

    public static void log(String string) {
        System.out.println(string);
        Platform.runLater(()->logTextArea.appendText(string + "\n"));

        try {
            if(logWriter == null)
                return;
            logWriter.write(Calendar.getInstance().getTime() + ": " + string);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public static void logToFile(String fileName, String string) {
        if(writerMap.containsKey(fileName)) {
            BufferedWriter bufferedWriter = writerMap.get(fileName);
            FileUtils.writeToFile(bufferedWriter, Calendar.getInstance().getTime() + ": " + string);
        } else {
            try {
                File logFile = new File(logsDirName + "\\" + fileName);
                if(!logFile.createNewFile())
                    return;

                FileWriter writer = new FileWriter(logFile);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                writerMap.put(fileName, bufferedWriter);
                logToFile(fileName, string);
            } catch (IOException e) {
                log(e.toString());
            }
        }
    }
}
