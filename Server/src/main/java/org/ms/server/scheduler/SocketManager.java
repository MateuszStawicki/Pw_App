package org.ms.server.scheduler;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import lombok.SneakyThrows;

public class SocketManager {

    private static final int THREADS_PORT_START = 10000;

    private final Semaphore threadPoolSemaphore;
    private final Semaphore priorityThreadPoolSemaphore;
    private final List<ServerSocket> availableThreads = Collections.synchronizedList(new ArrayList<>());
    private final List<ServerSocket> availablePriorityThreads = Collections.synchronizedList(new ArrayList<>());

    @SneakyThrows
    public SocketManager(int threadPoolSize, int priorityThreadPoolSize) {
        threadPoolSemaphore = new Semaphore(threadPoolSize);
        priorityThreadPoolSemaphore = new Semaphore(priorityThreadPoolSize);

        int i;
        int j;
        for (i = 0; i < threadPoolSize; i++) {
            availableThreads.add(new ServerSocket(THREADS_PORT_START + i));
        }
        for (j = 0; j < priorityThreadPoolSize; j++) {
            availablePriorityThreads.add(new ServerSocket(THREADS_PORT_START + i + j));
        }
    }

    public boolean tryAcquire() {
        return threadPoolSemaphore.tryAcquire();
    }

    public boolean tryAcquireWithPriority() {
        return priorityThreadPoolSemaphore.tryAcquire();
    }

    public ServerSocket acquire() {
        return availableThreads.remove(0);
    }

    public ServerSocket acquireWithPriority() {
        return availablePriorityThreads.remove(0);
    }

    public void release(ServerSocket serverSocket) {
        availableThreads.add(serverSocket);
        threadPoolSemaphore.release();
    }

    public void releaseWithPriority(ServerSocket serverSocket) {
        availablePriorityThreads.add(serverSocket);
        priorityThreadPoolSemaphore.release();
    }

}
