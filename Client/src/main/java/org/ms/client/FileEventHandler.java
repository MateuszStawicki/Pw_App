package org.ms.client;

import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.ms.shared.FileCommand;
import org.ms.shared.FileCommand.CommandType;
import org.ms.shared.Logger;
import org.ms.shared.Transmission;

public class FileEventHandler extends Thread {

    private static final long MAX_BYTES_SIZE_FOR_PRIORITY = 10000L;
    private static final String SLASH = "/";
    private final Path clientDirectoryPath;
    private static final int FILE_SLOWER = 5000;
    @Getter
    private final FileCommand command;

    @Setter
    private int port;
    @Setter
    private String host;

    public FileEventHandler(PrintWriter writer, Path clientDirectoryPath, WatchEvent<?> event) {
        this.clientDirectoryPath = clientDirectoryPath;

        command = new FileCommand(event.context().toString());

        if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Logger.log("File '" + event.context() + "' created");
            if (Paths.get(clientDirectoryPath.toString() + "/" + event.context()).toFile().length() <= MAX_BYTES_SIZE_FOR_PRIORITY) {
                command.setType(FileCommand.CommandType.SEND_WITH_PRIORITY);
            } else {
                command.setType(FileCommand.CommandType.SEND);
            }
        } else {
            Logger.log("File '" + event.context() + "' deleted");
            command.setType(FileCommand.CommandType.DELETE);
        }
        writer.println(command.toString());
    }

    public FileEventHandler(PrintWriter writer, Path clientDirectoryPath, String fileName) {
        this.clientDirectoryPath = clientDirectoryPath;
        command = new FileCommand(fileName, FileCommand.CommandType.DOWNLOAD);
        Logger.log("File '" + fileName + "' will be downloaded from server");

        writer.println(command.toString());
    }

    @SneakyThrows
    @Override
    public void run() {
        Socket socket = new Socket(host, port);

        if (FileCommand.CommandType.SEND.equals(command.getType()) || CommandType.SEND_WITH_PRIORITY.equals(command.getType())) {
            Logger.log("Sending file '" + command.getFileName() + "' to server");
            Transmission.loadAndWriteFile(socket.getOutputStream(), Paths.get(clientDirectoryPath.toString() + SLASH + command.getFileName()),FILE_SLOWER);
        } else if (FileCommand.CommandType.DOWNLOAD.equals(command.getType())) {
            Logger.log("Downloading file '" + command.getFileName() + "' from server");
            Transmission.readAndSaveFile(socket.getInputStream(), Paths.get(clientDirectoryPath.toString() + SLASH + command.getFileName()));
        }
        socket.close();
    }

}
