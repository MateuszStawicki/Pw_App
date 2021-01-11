package org.ms.server.scheduler;

import lombok.SneakyThrows;
import org.ms.server.ServerApplication;
import org.ms.server.Task;
import org.ms.shared.Logger;

public class Scheduler extends Thread {

    private final Queue queue;
    private final SocketManager socketManager;

    public Scheduler(int threadPoolSize, int priorityThreadPoolSize) {
        queue = new Queue();
        socketManager = new SocketManager(threadPoolSize, priorityThreadPoolSize);
        logSchedulerConfiguration(threadPoolSize, priorityThreadPoolSize);
    }

    public void schedule(Task task) {
        if (ServerApplication.RUNNING) {
            queue.scheduleTask(task);
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        Logger.log("Scheduler started");

        while (ServerApplication.RUNNING || queue.hasTasks() || queue.hasPriorityTasks()) {
            if (queue.hasPriorityTasks()) {
                if (socketManager.tryAcquire()) {
                    queue.nextPriorityTask(socketManager, false);
                } else if (socketManager.tryAcquireWithPriority()) {
                    queue.nextPriorityTask(socketManager, true);
                }
            } else if (queue.hasTasks() && socketManager.tryAcquire()) {
                queue.nextTask(socketManager);
            }
        }

        Logger.log("Scheduler stopped");
    }

    private void logSchedulerConfiguration(int threadPoolSize, int priorityThreadPoolSize) {
        Logger.log("Scheduler configuration: [thread.pool.size=" + threadPoolSize + "; thread.pool.priority.size=" + priorityThreadPoolSize + "]");
    }

}
