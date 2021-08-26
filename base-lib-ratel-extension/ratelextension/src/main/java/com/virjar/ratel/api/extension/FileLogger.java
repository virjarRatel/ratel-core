package com.virjar.ratel.api.extension;

import android.support.annotation.NonNull;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedBridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.io.IOUtils;

public class FileLogger {
    //异步任务队列，最多2048个，超过后忽略日志
    private static BlockingDeque<LogMessage> blockingDeque = new LinkedBlockingDeque<>(2048);
    private static Thread syncLogThread = null;
    private static OutputStream outputStream = null;
    private static byte[] newLine = System.getProperty("line.separator", "\n").getBytes();

    private static File mLogFile = null;

    public static File getLogFile() {
        return mLogFile;
    }

    public synchronized static void startRecord(File baseDir) {
        if (syncLogThread != null) {
            return;
        }

        RposedBridge.log("file start");
        File dir = new File(baseDir, "file_log");
        String processName = RatelToolKit.processName;
        if (!dir.exists()) {
            boolean create = dir.mkdirs();
            RposedBridge.log("create file : " + create + ",processName=" + processName);

        }
        processName = processName.replace(":", "_");


        File filename = new File(dir, processName + ".txt");
        mLogFile = filename;
        RposedBridge.log("filename:" + filename.getAbsolutePath());
        try {
            if (!filename.exists()) {
                if (!filename.createNewFile()) {
                    RposedBridge.log("failed to create log file :" + filename.getAbsolutePath());
                }
            } else {
                if (filename.length() > 2 * 1024 * 1024) {
                    RandomAccessFile accessFile = new RandomAccessFile(filename, "r");
                    long total = accessFile.length();
                    long index = total - 2 * 1024 * 1024;
                    if (index > 0) {
                        accessFile.seek(index);
                        byte[] bytes = new byte[2 * 1024 * 1024 + 1024];
                        int read = accessFile.read(bytes);
                        FileUtils.writeByteArrayToFile(filename, bytes, 0, read);
                    }
                }
            }
            outputStream = new FileOutputStream(filename, true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RposedBridge.log("start thread");
        syncLogThread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        blockingDeque.take().handle(outputStream);
                        outputStream.write(newLine);
                        //多增加两次回车换行，分割各个报文
                        outputStream.write(newLine);
                        outputStream.write(newLine);
                        outputStream.flush();
                    } catch (InterruptedException e) {
                        blockingDeque.clear();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        outLog("\n<=========================================================>\n");
        syncLogThread.setDaemon(true);
        syncLogThread.start();
    }

    public synchronized static void stopRecord() {
        if (syncLogThread == null) {
            return;
        }
        if (syncLogThread.isInterrupted()) {
            syncLogThread = null;
            return;
        }
        syncLogThread.interrupt();
        syncLogThread = null;
        IOUtils.closeQuietly(outputStream);
        outputStream = null;
        blockingDeque.clear();
    }

    public static boolean isComponentStarted() {
        return syncLogThread != null;
    }

    public static void outLog(final String message) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                outputStreamWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date()) + message);
                outputStreamWriter.flush();

            }
        });
    }

    public static void outLog(final InputStream inputStream) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
            }
        });
    }

    public static void outLog(final byte[] data) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                outputStream.write(data);
            }
        });
    }

    public static void outLog(final String tag, final String message) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                outputStreamWriter.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date()))
                        .append(tag);
                outputStreamWriter.append(message);
                outputStreamWriter.flush();
            }
        });
    }

    public static void outLog(final String tag, final InputStream inputStream) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                outputStream.write(tag.getBytes());
                IOUtils.copy(inputStream, outputStream);
            }
        });
    }

    public static void outLog(final String tag, final byte[] data) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(OutputStream outputStream) throws IOException {
                outputStream.write(tag.getBytes());
                outputStream.write(data);
            }
        });
    }

    public static void outLog(final LogMessage logMessage) {
        if (!isComponentStarted()) {
            return;
        }
        blockingDeque.offer(new LogMessage() {
            @Override
            public void handle(final OutputStream outputStream) throws IOException {
                logMessage.handle(new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        outputStream.write(b);
                    }

                    @Override
                    public void write(@NonNull byte[] b) throws IOException {
                        outputStream.write(b);
                    }

                    @Override
                    public void write(@NonNull byte[] b, int off, int len) throws IOException {
                        outputStream.write(b, off, len);
                    }

                    @Override
                    public void flush() throws IOException {
                        outputStream.flush();
                    }

                    @Override
                    public void close() throws IOException {
                    }
                });
            }
        });
    }

    public interface LogMessage {
        void handle(OutputStream outputStream) throws IOException;
    }


    public static void outTrack(String append) {
        String msg = append + getTrack();
        outLog(msg);
    }

    public static String getTrack() {
        return getTrack(new Throwable());
    }

    public static String getOwnerThreadTrack() {
        return getTrack(new Throwable());
    }

    public static String getTrack(Throwable e) {
        StringBuilder msg = new StringBuilder("\n=============>\n");
        while (e != null) {
            msg.append(e.getClass().getName()).append(":").append(e.getMessage()).append("\n");
            StackTraceElement[] ste = e.getStackTrace();
            for (StackTraceElement stackTraceElement : ste) {
                msg.append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append(":").append(stackTraceElement.getLineNumber()).append("\n");
            }
            e = e.getCause();
            if (e != null) {
                msg.append("cause:").append(e.getMessage()).append("\n\n");
            }
        }
        msg.append("<================\n");
        return msg.toString();
    }

    /**
     * 分段打印出较长log文本
     *
     * @param log       原log文本
     * @param showCount 规定每段显示的长度（最好不要超过eclipse限制长度）
     */
    public static void showLogCompletion(String tag, String log, int showCount) {
        if (log.length() > showCount) {
            String show = log.substring(0, showCount);
//			System.out.println(show);
            Log.e(tag, show + "");
            if ((log.length() - showCount) > showCount) {//剩下的文本还是大于规定长度
                String partLog = log.substring(showCount, log.length());
                showLogCompletion(tag, partLog, showCount);
            } else {
                String surplusLog = log.substring(showCount, log.length());
//				System.out.println(surplusLog);
                Log.e(tag, surplusLog + "");
            }

        } else {
//			System.out.println(log);
            Log.e(tag, log + "");
        }
    }
}
