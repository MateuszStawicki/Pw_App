package org.ms.server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.SneakyThrows;
import org.ms.server.scheduler.Scheduler;
import org.ms.shared.Logger;

public class Server extends Thread {

    private static final int PORT = 1234;
    private static final List<ClientHandler> CLIENTS = Collections.synchronizedList(new ArrayList<>());

    private final Scheduler scheduler;
    private ServerSocket socket;
    private static final int THREAD_POOL_PRIORITY_SIZE = 1;
    private static final int THREAD_POOL_SIZE = 2;

    public Server() {
        scheduler = new Scheduler(THREAD_POOL_SIZE, THREAD_POOL_PRIORITY_SIZE);
    }

    @Override
    @SneakyThrows
    public void run() {
        Logger.log("Server started");
        socket = new ServerSocket(PORT);
        scheduler.start();

        while (ServerApplication.RUNNING) {
            ClientHandler clientHandler = new ClientHandler(socket.accept(), scheduler);
            addClient(clientHandler);
            clientHandler.start();
        }

        disconnect();

        Logger.log("Server stopped");
    }

    @SneakyThrows
    public void disconnect() {
        CLIENTS.forEach(ClientHandler::disconnect);
        socket.close();
    }

    private static void addClient(ClientHandler clientHandler) {
        CLIENTS.add(clientHandler);
    }

    public static void removeClient(ClientHandler clientHandler) {
        CLIENTS.remove(clientHandler);
        Logger.log("Client " + clientHandler.getUser().getName() + " disconnected");
    }

}
