package org.ms.server;

public class ServerApplication {

    public static boolean RUNNING = false;

    public static void main(String... args) {
        RUNNING = true;
        new Server().start();
    }

}
