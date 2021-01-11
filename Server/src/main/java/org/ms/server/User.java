package org.ms.server;

import java.io.PrintWriter;
import java.nio.file.Path;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class User {

    private final String name;
    private final PrintWriter writer;
    private final Path directoryPath;
    private int queuedTasks = 0;
    public void taskQueued() {
        queuedTasks += 1;
    }

}
