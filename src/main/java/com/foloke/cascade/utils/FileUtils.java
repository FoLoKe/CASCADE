package com.foloke.cascade.utils;

import java.io.BufferedWriter;

public class FileUtils {
    public static void writeToFile(BufferedWriter bufferedWriter, String string) {
        Writer writer = new Writer(bufferedWriter, string);
        Thread thread = new Thread(writer);
        thread.start();
    }

    private static class Writer implements Runnable {
        final BufferedWriter bufferedWriter;
        String string;

        public Writer(BufferedWriter writer, String s) {
            this.bufferedWriter = writer;
            this.string = s;
        }

        @Override
        public void run() {
            synchronized (bufferedWriter) {
                try {
                    bufferedWriter.write(string);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (Exception e) {
                    LogUtils.log(e.toString());
                }
            }
        }
    }
}
