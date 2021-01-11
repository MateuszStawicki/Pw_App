package org.ms.shared;

import java.util.UUID;


public class FileCommand {
    public String getId() {
        return id;
    }

    private static final String SLASH = "/";
    private String id;
    private String fileName;
    private CommandType type;

    public void setType(CommandType type) {
        this.type = type;
    }

    public FileCommand(String fileName) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public CommandType getType() {
        return type;
    }

    public FileCommand(String fileName, CommandType type) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.type = type;
    }

    public FileCommand(String id, String fileName, CommandType type) {
        this.id = id;
        this.fileName = fileName;
        this.type = type;
    }

    public enum CommandType {
        SEND,
        SEND_WITH_PRIORITY,
        DOWNLOAD,
        DELETE
    }

    @Override
    public String toString() {
        return id + SLASH + fileName + SLASH + type.name();
    }

    public static FileCommand fromString(String command) {
        String[] splitCommand = command.split(SLASH);
        return new FileCommand(splitCommand[0], splitCommand[1], CommandType.valueOf(splitCommand[2]));
    }


}

