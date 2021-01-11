package org.ms.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.ms.shared.Logger;
import org.ms.shared.Response;


public class Client extends Thread {

    private static final String SERVER_IP = "127.0.0.1";
    private static final String CLIENT_PATH = "C:\\Users\\Mateusz\\Desktop\\PW\\Client\\";
    private static final String SLASH = "/";

    private static final int SERVER_PORT = 1234;
    private Path clientDirPath;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private List<FileEventHandler> fileEventHandlers = Collections.synchronizedList(new ArrayList<>());
    private List<String> originalServerFilesNames = Collections.synchronizedList(new ArrayList<>());

    public void init() {
        System.out.println("Klient " + Thread.currentThread().getName());
        System.out.println("Wpisz login\n" + ">");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.next();
        clientDirPath = Paths.get(CLIENT_PATH + SLASH + login);
        prepareDirectory();
        connectClientToServer(login);
        synchronize();
        start();
        startWatcher();


    }

    @SneakyThrows
    private void connectClientToServer(String login) {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter.println(login);
        Logger.log("Connected to server");
    }

    @SneakyThrows
    private void prepareDirectory() {
        File directory = clientDirPath.toFile();
        if (directory.exists()) {
            directory.delete();
        }
        directory.mkdir();
    }

    @SneakyThrows
    private void startWatcher() {
        Logger.log("Watching client directory started");
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(clientDirPath.toString());
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                if (!originalServerFilesNames.contains(event.context().toString())) {
                    fileEventHandlers.add(new FileEventHandler(printWriter, clientDirPath, event));
                }
            }
            key.reset();
        }
        Logger.log("Watching client directory stopped");
    }

    @SneakyThrows
    private void synchronize() {
        Logger.log("Synchronization started");
        originalServerFilesNames = Arrays.asList(bufferedReader.readLine().split(","));
        List<String> userFileNames = Arrays.asList(clientDirPath.toFile().listFiles()).stream().map(File::getName).collect(Collectors.toList());
        originalServerFilesNames.stream()
                                .filter(fileName -> !userFileNames.contains(fileName) && !fileName.equals(""))
                                .forEach(fileName -> fileEventHandlers.add(new FileEventHandler(printWriter, clientDirPath, fileName)));
        Logger.log("Synchronization finished");
    }

    @SneakyThrows
    @Override
    public void run() {
        while (ClientApplication.RUNNING) {
            Response commandResponse = Response.fromString(bufferedReader.readLine());

            fileEventHandlers.stream().filter(handler -> handler.getCommand().getId().equals(commandResponse.getCommandId())).findFirst().ifPresent(handler -> {
                handler.setHost(SERVER_IP);
                handler.setPort(commandResponse.getPort());
                handler.start();
                fileEventHandlers.remove(handler);
            });

        }
    }

}
