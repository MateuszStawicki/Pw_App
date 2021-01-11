package org.ms.shared;

import java.util.Date;

public class Logger {
    public static void log(String info) {
        System.out.println(new Date().toString() + ">> " + info);
    }

}
