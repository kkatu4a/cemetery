package com.katu4a.cladbise.api;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TestBackend implements AutoCloseable {
    private final HttpServer server;
    public final Map<String, String> responses = new ConcurrentHashMap<>();
    public volatile String method, path, body;
    public volatile int status = 200;
    public TestBackend() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            method = exchange.getRequestMethod(); path = exchange.getRequestURI().getPath();
            body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = responses.getOrDefault(path, "[]").getBytes(StandardCharsets.UTF_8);
            if (method.equals("DELETE")) exchange.sendResponseHeaders(204, -1);
            else { exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); }
            exchange.close();
        });
        server.start();
    }
    public String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
    public void close() { server.stop(0); }
}
