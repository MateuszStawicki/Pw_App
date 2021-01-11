package org.ms.server.scheduler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ms.server.Task;
import org.ms.shared.Logger;

@RequiredArgsConstructor
public class Queue {

    @Getter
    private final List<Task> taskList = Collections.synchronizedList(new LinkedList<>());
    @Getter
    private final List<Task> priorityTasksList = Collections.synchronizedList(new LinkedList<>());

    public void scheduleTask(Task task) {
        if (task.isPriority()) {
            priorityTasksList.add(task);
        } else {
            taskList.add(task);
        }

        task.getUser().taskQueued();
        queueStatus();
    }

    public void nextTask(SocketManager socketManager) {
        Task task = taskList.remove(0);
        task.setServerSocket(socketManager.acquire());
        task.setSocketManager(socketManager);
        task.start();
        queueStatus();
    }

    public void nextPriorityTask(SocketManager socketManager, boolean withBlock) {
        Task task = priorityTasksList.remove(0);
        if (withBlock) {
            task.setServerSocket(socketManager.acquireWithPriority());
            task.setWithBlock(true);
        } else {
            task.setServerSocket(socketManager.acquire());
        }
        task.setSocketManager(socketManager);
        task.start();
        queueStatus();
    }

    public boolean hasTasks() {
        return !taskList.isEmpty();
    }

    public boolean hasPriorityTasks() {
        return !priorityTasksList.isEmpty();
    }

    private void queueStatus() {
        synchronized (taskList) {
            Logger.log("Queue status: [size=" + taskList.size() + "; taskIds={" +
                      String.join(",", taskList.stream().map(task -> task.getCommand().getId()).map(Object::toString).collect(Collectors.joining(","))) + "}]");
        }
        synchronized (priorityTasksList) {
            Logger.log("PriorityQueue status: [size=" + priorityTasksList.size() + "; taskIds={" +
                        String.join(",", priorityTasksList.stream().map(task -> task.getCommand().getId()).map(Object::toString).collect(Collectors.joining(","))) + "}]");
        }
    }

}
