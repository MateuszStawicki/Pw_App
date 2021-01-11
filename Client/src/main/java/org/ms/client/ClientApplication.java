package org.ms.client;

public class ClientApplication {

    private static final Client CLIENT = new Client();

    public static boolean RUNNING = false;

    public static void main(String... args) {
        RUNNING = true;
        CLIENT.init();
    }

}
