package org.ms.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.ms.server.Task.TaskType;
import org.ms.server.scheduler.Scheduler;
import org.ms.shared.FileCommand;
import org.ms.shared.FileCommand.CommandType;
import org.ms.shared.Logger;

@Getter
@RequiredArgsConstructor
public class ClientHandler extends Thread {

    private static final String BASE_DIRECTORY_PATH = "C:\\Users\\Mateusz\\Desktop\\PW\\Server\\";;
    private static final String SLASH = "/";
    private final Socket socket;
    private final Scheduler scheduler;

    private PrintWriter writer;
    private BufferedReader reader;
    private User user;

    @Override
    @SneakyThrows
    public void run() {
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String username = reader.readLine();
        user = new User(username, writer, prepareDirectory(username));
        writer.println(Arrays.asList(user.getDirectoryPath().toFile().listFiles()).stream().
                map(File::getName).collect(Collectors.toList()).toString().replaceAll("[\\[\\]\\s]", "").replace(" ",""));
        Logger.log("Client " + getUser().getName() + " connected");

        while (ServerApplication.RUNNING) {
            FileCommand command = FileCommand.fromString(reader.readLine());
            createTask(command, user);
        }
        disconnect();
    }

    private Path prepareDirectory(String login) {
        Path directoryPath = Paths.get(BASE_DIRECTORY_PATH + SLASH + login);

        File directory = directoryPath.toFile();
        if (!directory.exists()) {
            directory.mkdir();
        }
        return directoryPath;
    }

    private void createTask(FileCommand command, User user) {
        Task task = new Task();
        task.setCommand(command);
        task.setUser(user);

        if (CommandType.SEND.equals(command.getType())) {
            task.setTaskType(TaskType.SEND);
        } else if (CommandType.SEND_WITH_PRIORITY.equals(command.getType())) {
            task.setTaskType(TaskType.SEND);
            task.setPriority(true);
        } else if (CommandType.DOWNLOAD.equals(command.getType())) {
            task.setTaskType(TaskType.DOWNLOAD);
        } else if (CommandType.DELETE.equals(command.getType())) {
            task.setTaskType(TaskType.DELETE);
        }
        scheduler.schedule(task);
    }

    @SneakyThrows
    public void disconnect() {
        reader.close();
        writer.close();
        socket.close();
        Server.removeClient(this);
    }

}
