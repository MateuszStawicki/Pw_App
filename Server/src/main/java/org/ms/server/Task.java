package org.ms.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.ms.server.scheduler.SocketManager;
import org.ms.shared.FileCommand;
import org.ms.shared.Logger;
import org.ms.shared.Response;
import org.ms.shared.Transmission;

@Setter
@Getter
@RequiredArgsConstructor
public class Task extends Thread {

    private User user;
    private FileCommand command;

    private static final int FILE_SLOWER = 100;
    private static final String SLASH = "/";
    private boolean withBlock = false;
    private boolean priority = false;
    private boolean pauseable = true;
    private AtomicBoolean paused = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private SocketManager socketManager;
    private TaskType taskType;

    @SneakyThrows
    @Override
    public void run() {
        Task blockedTask = null;
        if (withBlock) {
            blockedTask = findTaskToBlock();
            if (blockedTask != null) {
                blockedTask.pauseTask();
            }
        }
        Path filePath = Paths.get(getUser().getDirectoryPath().toString() + SLASH + command.getFileName());
        switch (taskType) {
            case SEND:
                send(filePath);
                break;
            case DOWNLOAD:
                download();
                break;
            case DELETE:
                delete(filePath);
                break;
        }

        if (withBlock && blockedTask != null) {
            socketManager.releaseWithPriority(serverSocket);
            blockedTask.resumeTask();
        } else {
            socketManager.release(serverSocket);
        }
    }

@SneakyThrows
    private void delete(Path filePath) {

    Logger.log("Removing file: " + command.getFileName() + "started");

        if (filePath.toFile().exists()) {
            Files.delete(filePath);
        }

    Logger.log("Removing file: " + command.getFileName() + "stopped");
        setPauseable(false);
    }

    @SneakyThrows
    private void download() {
        Logger.log("Downloading file: " + command.getFileName() + "started");

        getUser().getWriter().println(new Response(getCommand().getId(), getServerSocket().getLocalPort()).toString());
        Socket socket = getServerSocket().accept();

        Transmission.loadAndWriteFile(socket.getOutputStream(), Paths.get(getUser().getDirectoryPath().toString() + SLASH + getCommand().getFileName()), FILE_SLOWER,
                                      getPaused());
        socket.close();

        Logger.log("Downloading file: " + command.getFileName() + " stopped");
    }

    @SneakyThrows
    private void send(Path filePath) {
        Logger.log("Creating file: " + command.getFileName() + "started");

        if (filePath.toFile().exists()) {
            Files.delete(filePath);
        }

        getUser().getWriter().println(new Response(getCommand().getId(), getServerSocket().getLocalPort()).toString());
        Socket socket = getServerSocket().accept();
        Transmission.readAndSaveFile(socket.getInputStream(), Paths.get(getUser().getDirectoryPath().toString() + SLASH + getCommand().getFileName()), FILE_SLOWER, getPaused());
        socket.close();

        Logger.log("Creating file: " + command.getFileName() + " stopped");
    }

    @SneakyThrows
    public synchronized void pauseTask() {
        if (priority || !pauseable) {
            throw new RuntimeException("Cannot pause this task");
        }

        paused.set(true);
        Logger.log(getCommand().getId() + "paused");
    }

    public synchronized void resumeTask() {
        paused.set(false);
        Logger.log(getCommand().getId() + "resumed");
    }


    private Task findTaskToBlock() {
        return Thread.getAllStackTraces()
                     .keySet()
                     .stream()
                     .filter(thread -> thread instanceof Task)
                     .map(task -> (Task) task)
                     .filter(task -> !task.priority && task.pauseable && !task.paused.get())
                     .findAny()
                     .orElse(null);
    }

    public enum TaskType {
        SEND,
        DOWNLOAD,
        DELETE
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCommand(FileCommand command) {
        this.command = command;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

}
