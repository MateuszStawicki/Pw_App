package org.ms.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Response {

    private static final String SLASH = "/";
    private final String commandId;
    private final int port;

    @Override
    public String toString() {
        return commandId + SLASH + port;
    }

    public static Response fromString(String response) {
        String[] splitResponse = response.split(SLASH);
        return new Response(splitResponse[0], Integer.parseInt(splitResponse[1]));
    }

}
