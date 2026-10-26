package com.katu4a.cladbise.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;

public final class ApiClient {
    private final URI base;
    private final HttpClient client;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public ApiClient() {
        this(System.getProperty("cemetery.backendUrl", System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8080")));
    }
    public ApiClient(String baseUrl) {
        base = URI.create(baseUrl.replaceAll("/+$", "") + "/");
        if (!("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getHost() == null)
            throw new IllegalArgumentException("BACKEND_URL должен быть HTTP(S) адресом сервера");
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    public <T> List<T> list(String path, Class<T> type) {
        try {
            return json.readValue(send("GET", path, null), json.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) { throw invalidResponse(e); }
    }
    public <T> T call(String method, String path, Object body, Class<T> type) {
        String response = send(method, path, body);
        if (type == Void.class) return null;
        try { return json.readValue(response, type); }
        catch (IOException e) { throw invalidResponse(e); }
    }
    private String send(String method, String path, Object body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(base.resolve(path.replaceFirst("^/", "")))
                    .timeout(Duration.ofSeconds(20)).header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = switch (response.statusCode()) {
                    case 400 -> "Проверьте заполненные поля";
                    case 404 -> "Запись не найдена. Обновите список";
                    case 409 -> "Конфликт данных: запись уже существует или связана с другими записями";
                    default -> "Ошибка сервера";
                };
                try {
                    String detail = json.readTree(response.body()).path("detail").asText("");
                    if (!detail.isBlank()) message += ": " + detail;
                } catch (IOException ignored) { /* The server may return an HTML error. */ }
                throw new ApiException(response.statusCode(), message + " (HTTP " + response.statusCode() + ")", null);
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Запрос прерван", e);
        } catch (IOException e) {
            throw new ApiException(0, "Не удалось связаться с сервером " + base + ". Проверьте подключение и запуск бэкенда", e);
        }
    }
    private ApiException invalidResponse(IOException e) {
        return new ApiException(0, "Сервер вернул данные в неожиданном формате", e);
    }
}
