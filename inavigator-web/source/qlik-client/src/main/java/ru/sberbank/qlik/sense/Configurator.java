package ru.sberbank.qlik.sense;

import javax.websocket.ClientEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Configurator extends ClientEndpointConfig.Configurator {
    private final String domain;
    private final String username;

    public Configurator(String domain, String username) {
        this.domain = domain;
        this.username = username;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        headers.put("X-Qlik-User", Arrays.asList("UserDirectory=" + domain + ";UserId=" + username));
    }
}
