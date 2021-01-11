package org.ms.shared;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.SneakyThrows;

public class Transmission {

    @SneakyThrows
    public static void loadAndWriteFile(OutputStream outStream, Path filePath) {
        loadAndWriteFile(outStream, filePath, 0L, new AtomicBoolean(false));
    }

    @SneakyThrows
    public static void loadAndWriteFile(OutputStream outStream, Path filePath, long slower, AtomicBoolean paused) {
        BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(filePath.toFile()));

        int read;
        byte[] bytes = new byte[1024];
        while ((read = inStream.read(bytes)) != -1) {
            while (paused.get()) {
            }
            outStream.write(bytes, 0, read);
            Thread.sleep(slower);
        }
        outStream.flush();
        inStream.close();
    }

    @SneakyThrows
    public static void readAndSaveFile(InputStream inStream, Path filePath) {

        readAndSaveFile(inStream, filePath, 0L, new AtomicBoolean(false));
    }

    @SneakyThrows
    public static void readAndSaveFile(InputStream inStream, Path filePath, long slower, AtomicBoolean paused) {
        if (filePath.toFile().exists()) {
            Files.delete(filePath);
        }

        int read;
        byte[] bytes = new byte[1024];
        FileOutputStream outStream = new FileOutputStream(filePath.toFile());
        while ((read = inStream.read(bytes)) != -1) {
            while (paused.get()) {
            }
            outStream.write(bytes, 0, read);
            Thread.sleep(slower);
        }
        outStream.close();
    }

}
