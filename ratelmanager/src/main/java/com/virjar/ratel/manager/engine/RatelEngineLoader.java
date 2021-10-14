package com.virjar.ratel.manager.engine;

import android.content.Context;
import android.widget.Toast;

import com.virjar.ratel.allcommon.NewConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RatelEngineLoader {


    public static String ratelEngineVersionName = null;
    public static String ratelEngineVersionCode = null;
    public static String ratelEngineBuildTimestamp = null;

    public static void init(Context context) {
        File configFile = context.getFileStreamPath("ratel_engine.properties");
        try {
            releaseDefault(context, configFile);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            readConfig(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File releaseReBuilderDexResource(Context context) throws IOException {
        File fileStreamPath = context.getFileStreamPath(NewConstants.DEX_BUILDER_ASSETS_NAME);
        if (fileStreamPath.exists()) {
            return fileStreamPath;
        }
        try {
            try (InputStream inputStream = context.getAssets().open(NewConstants.DEX_BUILDER_ASSETS_NAME)) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileStreamPath)) {
                    copy(inputStream, fileOutputStream);
                }
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Toast.makeText(context, "请使用脚本打包RM apk", Toast.LENGTH_LONG).show();
            throw new IOException("Builder文件不存在，RM apk构建方式错误？", fileNotFoundException);
        }
        return fileStreamPath;
    }

    private static void releaseDefault(Context context, File configFile) throws IOException {
        File fileStreamPath = releaseReBuilderDexResource(context);
        loadJar(fileStreamPath, configFile);
    }

    public static void loadJar(File jarFilePath, File configFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarFilePath)) {
            ZipEntry entry = zipFile.getEntry("ratel_engine.properties");
            InputStream inputStream1 = zipFile.getInputStream(entry);
            Properties properties = new Properties();
            properties.load(inputStream1);
            inputStream1.close();
            properties.store(new FileOutputStream(configFile), "ReGen by ratel manager");

            readConfig(configFile);
        }
    }

    private static void readConfig(File configFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            ratelEngineVersionName = properties.getProperty("ratel_engine_versionName", "unknonwn");

            ratelEngineVersionCode = properties.getProperty("ratel_engine_versionCode", "unknonwn");

            ratelEngineBuildTimestamp = properties.getProperty("ratel_engine_buildTimestamp", "unknonwn");
        }
    }


    public static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Copies bytes from an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of
     * <code>-1</code> after the copy has completed since the correct
     * number of bytes cannot be returned as an int. For large streams
     * use the <code>copyLarge(InputStream, OutputStream)</code> method.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Copies bytes from an <code>InputStream</code> to an <code>OutputStream</code> using an internal buffer of the
     * given size.
     * <p>
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input      the <code>InputStream</code> to read from
     * @param output     the <code>OutputStream</code> to write to
     * @param bufferSize the bufferSize used to copy from the input to the output
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.5
     */
    public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    /**
     * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.3
     */
    public static long copyLarge(final InputStream input, final OutputStream output)
            throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
